/*
 * NewsRecordBuilder.java
 *
 * Created on 08.03.2007 16:00:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.feed.FeedBuilder;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FieldBuilder;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * Creates a NewsRecord based on a {@link ParsedRecord}. In addition to assembling the fields,
 * this class will also try to add Instruments that are associated with the news to the
 * NewsRecord. Thus, systems that process the NewsRecord can easily access instrument information.
 */
@NotThreadSafe
public class NewsRecordBuilder implements FieldBuilder, FeedBuilder {
    /**
     * Parts in multi-part news are marked with "part_m/n" where m is the number of the current
     * part and n is the total number of parts.
     */
    private static final byte[] PART_PREFIX = "part_".getBytes(StandardCharsets.US_ASCII);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * used to validate the gallery field, thread safe
     */
    private final Schema schema;

    /**
     * Used to compute a unique id for each news. Since it is somewhat uncertain that all
     * input systems will always assign the same id to the same news (and this is certainly not
     * the case for older news), this is the best method to identify identical news items.
     */
    private final MessageDigest digester;

    /**
     * Ids of those fields that will not be used to compute the news's id, because
     * they might be different for otherwise identical messages.
     */
    private static final BitSet NON_DIGEST_FIELDS = new BitSet();

    static {
        NON_DIGEST_FIELDS.set(VwdFieldDescription.NDB_Story_Number.id());
        NON_DIGEST_FIELDS.set(VwdFieldDescription.MMF_Iid_List.id());
        NON_DIGEST_FIELDS.set(VwdFieldDescription.ADF_TIMEOFARR.id());
        NON_DIGEST_FIELDS.set(VwdFieldDescription.ADF_DATEOFARR.id());
    }

    private boolean acceptNewsWithoutTimestamp = true;

    private boolean acceptNewsWithoutSelector = false;

    private boolean acceptNewsWithoutStoryNumber = false;

    private boolean logOldNews = false;

    /**
     * the next step in news processing
     * the handlers for consuming NewsRecord instances
     */
    private List<NewsRecordHandler> handlers = Collections.emptyList();

    private NewsSymbolIdentifier symbolIdentifier;

    private final Map<String, Message> messages = new HashMap<>();

    private Message message;

    private Set<String> allowedSelectors;

    private Set<String> ignoredAgencies;

    /**
     * used to validate symbols related to a news item
     */
    private final Matcher wordMatcher = Pattern.compile("\\w+").matcher("");

    private static class Message {
        private final List<Field> fields = new ArrayList<>(16);

        private Set<String> symbols;

        private byte[] story;

        private byte[] rawStory;

        private String newsId;

        private int storyNumber = 0;

        private final int flags;

        public Message(int flags) {
            this.flags = flags;
        }

        NewsRecordImpl getNewsRecord(String id) {
            if (this.rawStory != null && Arrays.equals(this.rawStory, this.story)) {
                this.rawStory = null;
            }

            this.fields.sort(null);

            final byte[] data = new byte[getDataLength()];

            final int[] indexes = new int[fields.size() + 1];
            final int[] offsets = new int[indexes.length];

            int n = 0;
            int offset = 0;
            for (Field field : fields) {
                indexes[n] = field.vwdField.id();
                offsets[n++] = offset;
                System.arraycopy(field.value, 0, data, offset, field.value.length);
                offset += field.value.length;
            }
            indexes[n] = Integer.MAX_VALUE;
            offsets[n] = offset;

            final SnapRecordVwd snap = new SnapRecordVwd(indexes, offsets, data);
            return new NewsRecordImpl(id, snap, uncompress(false), uncompress(true));
        }

        private byte[] uncompress(boolean raw) {
            byte[] data = raw ? this.rawStory : story;

            if (data == null || !isCompressed()) {
                return data;
            }
            try {
                return ByteUtil.decompress(data);
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).warn("<uncompress> failed for" + (raw ? " raw" : "")
                        + " story in message " + this.newsId);
                return null;
            }
        }

        String getNewsId() {
            return this.newsId;
        }

        boolean isCompressed() {
            return isFlagSet(1);
        }

        boolean isMultipart() {
            return isFlagSet(2);
        }

        boolean isMultipartEnd() {
            return isFlagSet(4);
        }

        Field getSelectors() {
            for (Field field : fields) {
                if (field.vwdField == VwdFieldDescription.NDB_Selectors) {
                    return field;
                }
            }
            return null;
        }

        boolean isFlagSet(final int bit) {
            return (this.flags & bit) == bit;
        }

        Set<String> getSymbols() {
            return this.symbols != null ? this.symbols : Collections.<String>emptySet();
        }

        void addSymbol(String symbol) {
            if (this.symbols == null) {
                this.symbols = new HashSet<>();
            }
            this.symbols.add(symbol);
        }

        int getDataLength() {
            int result = 0;
            for (Field field : this.fields) {
                result += field.value.length;
            }
            return result;
        }

        void add(Field field) {
            if (isMultipart() && field.vwdField == VwdFieldDescription.NDB_Headline) {
                this.fields.add(new Field(field.vwdField, removePartId(field.value)));
            }
            else {
                this.fields.add(field);
            }
        }

        void joinStory(Message nextPart) {
            this.story = join(this.story, nextPart.story);
            this.rawStory = join(this.rawStory, nextPart.rawStory);
        }

        byte[] join(byte[] story, byte[] part) {
            if (part == null) {
                return story;
            }
            if (story == null) {
                return part;
            }
            return ArrayUtils.addAll(story, part);
        }
    }

    @Immutable
    private static class Field implements Comparable<Field> {
        private final VwdFieldDescription.Field vwdField;

        private final byte[] value;

        public Field(VwdFieldDescription.Field vwdField, byte[] value) {
            this.vwdField = vwdField;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Field field1 = (Field) o;
            return vwdField.equals(field1.vwdField);
        }

        @Override
        public int hashCode() {
            return vwdField.hashCode();
        }

        @Override
        public int compareTo(Field o) {
            return this.vwdField.id() - o.vwdField.id();
        }

        public static Field create(VwdFieldDescription.Field field, int value) {
            final byte[] data = new byte[4];
            ByteBuffer.wrap(data).putInt(value);
            return new Field(field, data);
        }

        public static Field create(VwdFieldDescription.Field field, long value) {
            final byte[] data = new byte[8];
            ByteBuffer.wrap(data).putLong(value);
            return new Field(field, data);
        }
    }

    public NewsRecordBuilder() {
        try {
            this.digester = MessageDigest.getInstance("sha-256");
            this.schema = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(new StreamSource(new StringReader(GALLERY_XSD)));
        } catch (NoSuchAlgorithmException | SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getFieldFlags() {
        return VwdFieldDescription.FLAG_NEWS | VwdFieldDescription.FLAG_DYNAMIC;
    }

    public void setIgnoredAgencies(String[] allowedSelectors) {
        this.ignoredAgencies = new HashSet<>();
        ignoredAgencies.addAll(Arrays.asList(allowedSelectors));
        for (String agency : ignoredAgencies) {
            this.logger.info("<setIgnoredAgencies>   '" + agency + "'");
        }
    }

    public void setAllowedSelectors(String[] allowedSelectors) {
        this.allowedSelectors = new HashSet<>();
        for (String allowedSelector : allowedSelectors) {
            this.allowedSelectors.add(EntitlementsVwd.toNumericSelector(allowedSelector));
        }
        this.logger.info("<setAllowedSelectors> " + this.allowedSelectors);
    }

    public void setHandler(NewsRecordHandler handler) {
        setHandlers(Collections.singletonList(handler));
    }

    public void setHandlers(List<NewsRecordHandler> handlers) {
        this.handlers = handlers;
    }

    public void setAcceptNewsWithoutTimestamp(boolean acceptNewsWithoutTimestamp) {
        this.acceptNewsWithoutTimestamp = acceptNewsWithoutTimestamp;
        this.logger.info("<setAcceptNewsWithoutTimestamp> " + this.acceptNewsWithoutTimestamp);
    }

    public void setAcceptNewsWithoutStoryNumber(boolean acceptNewsWithoutStoryNumber) {
        this.acceptNewsWithoutStoryNumber = acceptNewsWithoutStoryNumber;
        this.logger.info("<setAcceptNewsWithoutStoryNumber> " + this.acceptNewsWithoutStoryNumber);
    }

    public void setAcceptNewsWithoutSelector(boolean acceptNewsWithoutSelector) {
        this.acceptNewsWithoutSelector = acceptNewsWithoutSelector;
        this.logger.info("<setAcceptNewsWithoutSelector> " + this.acceptNewsWithoutSelector);
    }

    public void setSymbolIdentifier(NewsSymbolIdentifier symbolIdentifier) {
        this.symbolIdentifier = symbolIdentifier;
    }

    @Override
    public byte[] getApplicableMessageTypes() {
        return new byte[]{VwdFeedConstants.MESSAGE_TYPE_NEWS};
    }

    public void setLogOldNews(boolean logOldNews) {
        this.logOldNews = logOldNews;
    }

    private void reset(int flags) {
        this.message = new Message(flags);
    }

    private String getDigest() {
        digester.reset();
        this.message.fields.stream()
                .filter(field -> !NON_DIGEST_FIELDS.get(field.vwdField.id()))
                .forEach(field -> digester.update(field.value));
        if (this.message.story != null) {
            digester.update(this.message.story);
        }
        else if (this.message.rawStory != null) {
            digester.update(this.message.rawStory);
        }
        final long l = halfDigest(this.digester.digest());
        return encodeId(l);
    }

    private NewsRecordImpl getNewsRecord() {
        return this.message.getNewsRecord(getDigest());
    }

    private boolean ensureTimestamp(ParsedRecord pr) {
        final boolean result = doEnsureTimestamp(pr);
        if (!result) {
            this.logger.warn("<ensureTimestamp> w/o timestamp: "
                    + pr.getString(VwdFieldDescription.NDB_Headline.id()));
        }
        return result;
    }

    private boolean doEnsureTimestamp(ParsedRecord pr) {
        if (pr.isFieldPresent(VwdFieldDescription.NDB_Agency_Date.id())
                && pr.isFieldPresent(VwdFieldDescription.NDB_Agency_Time.id())) {

            final DateTime dt = getAgencyTimestamp(pr);

            this.message.add(Field.create(VwdFieldDescription.NDB_Agency_Date, DateUtil.toYyyyMmDd(dt)));
            this.message.add(Field.create(VwdFieldDescription.NDB_Agency_Time, dt.secondOfDay().get()));

            if (dt.plusHours(2).isBeforeNow() && this.logOldNews) {
                this.logger.info("<ensureTimestamp> old news: '"
                        + pr.getString(VwdFieldDescription.NDB_Headline.id()) + ", " + dt);
            }

            return true;
        }

        final DateTime dt;
        final String s = pr.getString(VwdFieldDescription.NDB_Timestamp.id());
        if (s != null) {
            dt = computeDate(s);
            if (dt == null) {
                this.logger.warn("<ensureTimestamp> illegal: '" + s + "'");
                return false;
            }
        }
        else if (this.acceptNewsWithoutTimestamp) {
            dt = new DateTime().withSecondOfMinute(0);
        }
        else {
            return false;
        }

        this.message.add(Field.create(VwdFieldDescription.ADF_Datum, DateUtil.toYyyyMmDd(dt)));
        this.message.add(Field.create(VwdFieldDescription.ADF_Zeit, dt.secondOfDay().get()));
        return true;
    }

    private DateTime getAgencyTimestamp(ParsedRecord pr) {
        final int date = (int) pr.getNumericValue(VwdFieldDescription.NDB_Agency_Date.id());
        final int time = MdpsFeedUtils.toSecondOfDay((int) pr.getNumericValue(VwdFieldDescription.NDB_Agency_Time.id()));

        final LocalDate ld = DateUtil.yyyyMmDdToLocalDate(date);
        final LocalTime lt = DateUtil.secondsInDayToLocalTime(time);

        if ("DJN".equals(pr.getString(VwdFieldDescription.NDB_ID_News_Agency.id()))) {
            return ld.toDateTime(lt, DateTimeZone.UTC).toDateTime(DateUtil.DTZ_BERLIN);
        }
        return ld.toDateTime(lt);
    }

    static String encodeId(long l) {
        // for lucene, the id field has to be a string; use MAX_RADIX for a String as short as possible
        return Long.toString(l, Character.MAX_RADIX);
    }

    static long decodeId(String s) {
        return Long.parseLong(s, Character.MAX_RADIX);
    }

    private long halfDigest(byte[] digest) {
        long v = 0;
        for (int i = 0; i < 8; i++) {
            v |= ((digest[i] & 0xffL) << (8 * (7 - i)));
        }
        return v > 0 ? v : -v;
    }

    @Override
    public void process(FeedData data, ParsedRecord pr) {
        final NewsRecordImpl newsRecord = createNewsRecord(pr);
        if (newsRecord != null) {
            process(newsRecord);
        }
    }

    private void process(NewsRecordImpl newsRecord) {
        if (newsRecord.getSelectors().isEmpty() && !this.acceptNewsWithoutSelector) {
            this.logger.warn("<process> without selector: " + newsRecord);
            return;
        }

        final String agency = newsRecord.getAgency();
        if (ignoredAgencies != null && ignoredAgencies.contains(agency)) {
            this.logger.warn("<process> ignoring news, because agency id is blacklisted: " + agency
            + "(data is supposed to be processed by analyzer only)");
            return;
        }

        if (NewsServerImpl.isPageToBeIgnored(newsRecord)) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<process> to be ignored: " + newsRecord);
            }
            return;
        }

        if (!this.acceptNewsWithoutStoryNumber && this.message.storyNumber == 0) {
            this.logger.warn("<process> without story number: " + newsRecord.getHeadline());
            return;
        }

        forwardToHandlers(newsRecord);
    }

    private void forwardToHandlers(NewsRecordImpl newsRecord) {
        for (int i = 0; i < handlers.size(); i++) {
            final NewsRecordHandler handler = handlers.get(i);
            try {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<forwardToHandlers> sending "+newsRecord.getId()+" to "+handler.getClass().getName());
                }
                handler.handle(newsRecord);
            } catch (Exception e) {
                this.logger.error("<forwardToHandlers> failed for handler " + i + ": " + handler, e);
                return;
            }
        }
    }

    NewsRecordImpl createNewsRecord(ParsedRecord pr) {
        reset(pr.getFlags());
        pr.setFields(this);

        if (this.message.isMultipart()) {
            if (this.message.getNewsId() == null) {
                this.logger.warn("<process> multipart w/o news id: "
                        + pr.getString(VwdFieldDescription.NDB_Headline.id()));
                return null;
            }
            final String messageKey = pr.getString(VwdFieldDescription.NDB_ID_News_Agency.id())
                    + "/" + this.message.getNewsId();
            final Message previousPart = this.messages.get(messageKey);
            if (previousPart == null) {
                if (!ensureTimestamp(pr)) {
                    return null;
                }
                this.messages.put(messageKey, this.message);
            }
            else {
                previousPart.joinStory(this.message);
            }

            if (this.message.isMultipartEnd()) {
                this.message = this.messages.remove(messageKey);
            }
            else {
                return null;
            }
        }
        else if (!ensureTimestamp(pr)) {
            return null;
        }

        if (!hasAllowedSelectors()) {
            return null;
        }

        final Map<Long, Instrument> m = addInstrumentIds();

        final NewsRecordImpl newsRecord = getNewsRecord();
        if (m != null && !m.isEmpty()) {
            newsRecord.setInstruments(m.values());
        }
        return newsRecord;
    }

    // returns true as soon as one of the selectors attached to the news is
    // in the set of allowedSelectors
    private boolean hasAllowedSelectors() {
        if (this.allowedSelectors == null) {
            return true;
        }
        final Field selectors = this.message.getSelectors();
        if (selectors == null) {
            return false;
        }
        for (String s : new String(selectors.value).split(",")) {
            if (this.allowedSelectors.contains(EntitlementsVwd.toNumericSelector(s))) {
                return true;
            }
        }
        return false;
    }

    private Map<Long, Instrument> addInstrumentIds() {
        final Map<Long, Instrument> result = identifySymbols();
        if (result != null && !result.isEmpty()) {
            final byte[] tmp = new byte[result.size() * 12];
            final ByteBuffer bb = ByteBuffer.wrap(tmp);
            for (Long iid : result.keySet()) {
                if (bb.position() > 0) {
                    bb.put((byte) ',');
                }
                bb.put(ByteUtil.toBytes(Long.toString(iid)));
            }
            final byte[] iids = new byte[bb.position()];
            bb.flip();
            bb.get(iids);
            this.message.add(new Field(VwdFieldDescription.MMF_Iid_List, iids));
        }
        return result;
    }

    private Map<Long, Instrument> identifySymbols() {
        if (this.symbolIdentifier != null) {
            return this.symbolIdentifier.identify(this.message.getSymbols());
        }
        return null;
    }

    @Override
    public void set(VwdFieldDescription.Field field, int value) {
        if (field == VwdFieldDescription.ADF_Datum
                || field == VwdFieldDescription.ADF_Zeit
                || field == VwdFieldDescription.NDB_Agency_Date
                || field == VwdFieldDescription.NDB_Agency_Time) {
            return;
        }
        if (field == VwdFieldDescription.NDB_Story_Number) {
            this.message.storyNumber = value;
        }
        this.message.add(Field.create(field, value));
    }

    @Override
    public void set(VwdFieldDescription.Field field, long value) {
        this.message.add(Field.create(field, value));
    }

    @Override
    public void set(VwdFieldDescription.Field field, byte[] value, int start, int length) {
        if (VwdFieldDescription.NDB_Timestamp == field) {
            return;
        }

        if (field == VwdFieldDescription.NDB_Wpknlist || field == VwdFieldDescription.NDB_ISINList) {
            parseSymbols(value, start, length);
        }

        if (field == VwdFieldDescription.NDB_Selectors) {
            final byte[] data = getSelectorList(value, start, length);
            if (data.length > 0) {
                this.message.add(new Field(field, data));
            }
        }
        else {
            final int end = start + length;
            if (field == VwdFieldDescription.NDB_Story) {
                final int suffixLength
                        = this.message.isMultipart() ? getPartSuffixLength(value, start, end) : 0;
                this.message.story = Arrays.copyOfRange(value, start, end - suffixLength);
            }
            else {
                final byte[] bytes = Arrays.copyOfRange(value, start, end);
                if (VwdFieldDescription.NDB_News_ID == field) {
                    this.message.newsId = ByteUtil.toString(bytes);
                }
                if (field == VwdFieldDescription.NDB_Story_Raw) {
                    this.message.rawStory = bytes;
                }
                else if (field != VwdFieldDescription.NDB_dummy_54 || isValidGallery(bytes)) {
                    this.message.add(new Field(field, bytes));
                }
            }
        }
    }

    boolean isValidGallery(byte[] bytes) {
        if (bytes.length == 0) {
            return false;
        }
        try {
            schema.newValidator().validate(new StreamSource(new StringReader(new String(bytes, "UTF-8"))));
            return true;
        } catch (Exception ex) {
            logger.warn("invalid field content, validation failed, ignoring content", ex);
            return false;
        }
    }

    private static byte[] removePartId(byte[] src) {
        int length = getPartSuffixLength(src, 0, src.length);
        return (length == 0) ? src : Arrays.copyOf(src, src.length - length);
    }

    private static int getPartSuffixLength(byte[] src, int start, int to) {
        final int from = Math.max(start, to - 16);
        final int i = ByteUtil.lastIndexOf(src, PART_PREFIX, from, to);
        return (i < 0) ? 0 : (to - i);
    }

    static byte[] getSelectorList(byte[] value, int start, int length) {
        final byte[] tmp = new byte[length * 2];
        int n = 0;
        for (int i = start; i < start + length; i++) {
            if (isDigit(value[i])) {
                tmp[n++] = value[i];
            }
            else if (isLetter(value[i])) {
                tmp[n++] = value[i];
                tmp[n++] = (byte) ',';
            }
            else if (n > 0 && isSeparator(value[i]) && !isComma(tmp[n - 1])) {
                tmp[n++] = (byte) ',';
            }
        }
        if (n == 0) {
            return new byte[0];
        }
        return Arrays.copyOfRange(tmp, 0, isComma(tmp[n - 1]) ? n - 1 : n);
    }

    private static boolean isSeparator(byte b) {
        return b <= ' ' || isComma(b);
    }

    private static boolean isComma(byte b) {
        return b == ',';
    }

    private static boolean isLetter(byte b) {
        return b >= 'A' && b <= 'Z';
    }

    private static boolean isDigit(byte b) {
        return b >= '0' && b <= '9';
    }

    // parse symbols separated by commas; sometimes the value starts/ends with a comma
    // or it contains consecutive commas, so this method has to be robust
    private void parseSymbols(byte[] value, final int start, final int length) {
        int n = start;
        for (int i = start; i < start + length; i++) {
            if (value[i] == ',') {
                if (i > n) {
                    addSymbol(ByteUtil.toString(value, n, i - n));
                }
                n = i + 1;
            }
        }
        addSymbol(ByteUtil.toString(value, n, start + length - n));
    }

    private void addSymbol(String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        final String s = raw.trim();
        if (!this.wordMatcher.reset(s).matches()) {
            this.logger.warn("<addSymbol> ignoring invalid symbol: '" + s + "'");
        }
        else {
            this.message.addSymbol(s);
        }
    }

    private DateTime computeDate(String ddHHmm) {
        if (ddHHmm.length() != 6) {
            return null;
        }

        final int ts;
        try {
            ts = Integer.parseInt(ddHHmm);
        } catch (NumberFormatException e) {
            return null;
        }

        LocalDate date = new LocalDate();
        final int dayOfMonth = ts / 10000;
        final LocalTime time = new LocalTime((ts % 10000) / 100, ts % 100);
        if (dayOfMonth != date.getDayOfMonth()) {
            if (time.getHourOfDay() == 0 && dayOfMonth == date.plusDays(1).getDayOfMonth()) {
                // this happens with ripped news: the day's final file may contain news for the
                // following day; since the dayProvider for processing ripped news will be static
                // and set to the day on which the file started, we deal with that as follows:
                date = date.plusDays(1);
            }
            else {
                try {
                    if (dayOfMonth > date.getDayOfMonth()) {
                        date = date.minusMonths(1).dayOfMonth().setCopy(dayOfMonth);
                    }
                    else {
                        date = date.dayOfMonth().setCopy(dayOfMonth);
                    }
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return date.toDateTime(time);
    }

    private static final String GALLERY_XSD = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + " <xsd:element name=\"gallery\" type=\"GalleryType\"/>"
            + " <xsd:complexType name=\"GalleryType\">"
            + "   <xsd:sequence>"
            + "     <xsd:element name=\"img\" type=\"GalleryImgType\" minOccurs=\"1\" maxOccurs=\"unbounded\" />"
            + "   </xsd:sequence>"
            + " </xsd:complexType>"
            + "<xsd:complexType name=\"GalleryImgType\">"
            + "   <xsd:attribute name=\"id\" type=\"xsd:string\" use=\"required\"/>"
            + "   <xsd:attribute name=\"src\" type=\"xsd:string\" use=\"required\"/>"
            + "   <xsd:attribute name=\"width\" type=\"xsd:string\" use=\"required\"/>"
            + "   <xsd:attribute name=\"height\" type=\"xsd:string\" use=\"required\"/>"
            + "   <xsd:attribute name=\"alt\" type=\"xsd:string\" use=\"required\"/>"
            + "   <xsd:attribute name=\"title\" type=\"xsd:string\" use=\"required\"/>"

            + "   <xsd:attribute name=\"data-image-type\" type=\"xsd:string\"/>"
            + "   <xsd:attribute name=\"data-image-name\" type=\"xsd:string\"/>"
            + "   <xsd:attribute name=\"data-image-defaultpreview\" type=\"xsd:string\"/>"
            + "   <xsd:attribute name=\"data-image-aspectratio\" type=\"xsd:string\"/>"

            + "   <xsd:attribute name=\"type\" type=\"xsd:string\"/>"
            + "   <xsd:attribute name=\"name\" type=\"xsd:string\"/>"
            + "   <xsd:attribute name=\"defaultpreview\" type=\"xsd:string\"/>"
            + "   <xsd:attribute name=\"aspectratio\" type=\"xsd:string\"/>"

            + "</xsd:complexType>"
            + "</xsd:schema>";
}
