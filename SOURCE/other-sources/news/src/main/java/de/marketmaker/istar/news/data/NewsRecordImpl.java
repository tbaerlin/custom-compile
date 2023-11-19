/*
 * NewsRecordImpl.java
 *
 * Created on 07.03.2007 08:47:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteUtil;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.LiteralSnapField;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapFieldComparators;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.EntitlementDp2;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.SnapRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsRecord;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.NDB_ID_News_Agency;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Wohlfart
 */
public class NewsRecordImpl implements NewsRecord, SnapRecord, Serializable {
    protected static final long serialVersionUID = 1L;

    // primary key in the db, (hopefully a) unique digest value calculated from the news content
    private final String id;

    private String previousId;

    private final SnapRecordVwd snapRecord;

    // extra field as many requests are just for overviews w/o the story
    private final byte[] story;

    // extra field as many requests are just for overviews w/o the story
    private byte[] rawStory;

    // if the news contains advertising subtexts, each element of this array contains an int array
    // of length 2 that contains the starting and end position of the ad. In other words:
    // getText().substring(adPositions[i][0], adPositions[i][1]) returns the text of the i-th ad.
    private int[][] adPositions = null;

    // a single news item can refer to multiple topics
    private Set<String> topics;

    // transient, as these attributes can be recreated from the snapRecord
    private transient volatile EnumMap<NewsAttributeEnum, Set<String>> attributes;

    // instruments add a lot of data to a serialized object, so we do not carry them around
    // on a target system, instruments can be added based on the iids available as an attribute
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE",
            justification = "is not set by deserialization on purpose")
    private transient Set<Instrument> instruments = Collections.emptySet();

    // transient, as text can be created from story byte arraySnapRecordVwd
    private transient String text;

    // transient, as text can be created from rawStory byte array
    private transient String rawText;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI",
            justification = "client controls this data container")
    public NewsRecordImpl(String id, SnapRecordVwd snapRecord, byte[] story, byte[] rawStory) {
        this(id, null, snapRecord, story, rawStory);
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI",
            justification = "client controls this data container")
    public NewsRecordImpl(String id, String previousId, SnapRecordVwd snapRecord, byte[] story, byte[] rawStory) {
        this.id = id;
        this.previousId = previousId;
        this.snapRecord = snapRecord;
        this.story = story;
        this.rawStory = rawStory;
    }

    @Override
    public void setPreviousId(String previousId) {
        this.previousId = previousId;
    }

    @Override
    public String getAgency() {
        return getString(NDB_ID_News_Agency);
    }

    public Set<String> getAttributes(NewsAttributeEnum field) {
        final Set<String> strings = getAttributes().get(field);
        if (strings == null) {
            return Collections.emptySet();
        }
        return strings;
    }

    // news attributes are 1-n maps (e.g. Selectors, Countries, ...)
    @Override
    public Map<NewsAttributeEnum, Set<String>> getAttributes() {
        if (this.attributes == null) {
            this.attributes = computeAttributes();
        }
        return new EnumMap<>(this.attributes);
    }

    @Override
    public Entitlement getEntitlement() {
        final Set<String> selectors = getAttributes().get(NewsAttributeEnum.SELECTOR);
        final EntitlementDp2 entl = new EntitlementDp2();
        entl.setEntitlements(KeysystemEnum.VWDFEED, selectors.toArray(new String[selectors.size()]));
        return entl;
    }

    @Override
    public SnapField getField(int fieldId) {
        if (fieldId == VwdFieldDescription.NDB_Story.id()) {
            return LiteralSnapField.createString(fieldId, getText());
        }
        return this.snapRecord.getField(fieldId);
    }

    @Override
    public SnapField getField(String fieldname) {
        return getField(VwdFieldDescription.getFieldByName(fieldname).id());
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }

    @Override
    public String getHeadline() {
        final String headline = resolveControlCharaters(getString(VwdFieldDescription.NDB_Headline));
        final int index = headline.lastIndexOf(" part_");
        if (index < 0) {
            return headline;
        }
        return headline.substring(0, index);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPreviousId() {
        return previousId;
    }

    @Override
    public Set<Instrument> getInstruments() {
        return instruments;
    }

    @Override
    public Set<String> getIsinReferences() {
        return getAttributes(NewsAttributeEnum.ISIN);
    }

    /**
     * Returns iso-639-1 language code (2 characters, lower case) for this news.
     * @return iso-649-1 code or null.
     */
    @Override
    public String getLanguage() {
        return NewsLanguageUtil.getLanguage(this);
    }

    @Override
    public Set<String> getNumericSelectors() {
        final Set<String> result = new HashSet<>();
        for (String s : getSelectors()) {
            try {
                result.add(EntitlementsVwd.toNumericSelector(s));
            } catch (IllegalArgumentException e) {
                // empty, ignore invalid
            }
        }
        return result;
    }

    @Override
    public Integer getPriority() {
        return getInt(VwdFieldDescription.NDB_Priority);
    }

    @Override
    public Set<String> getSelectors() {
        return getAttributes(NewsAttributeEnum.SELECTOR);
    }

    /**
     * this is for market manager [old] that can only process 32bit int story ids; try to
     * create an id that is as unique as possible. The result will not be unique iff the
     * same story number appears again within 3 hours of the same day, which is deemed
     * to be sufficiently unlikely (ZZ by Email, 5 Dec 2007).
     * @return 32bit story id which is likely to be unique
     * @deprecated will be removed after the last market maker [old] installations ceased to exist
     */
    public int getShortId() {
        final SnapField f = getField(VwdFieldDescription.NDB_Story_Number.id());
        if (!f.isDefined()) {
            // market manager [old] will not see this news, as its shortId is not unique
            // since dayOfYear (see below) will always be > 0, no valid shortId will be 0.
            return 0;
        }

        // return |0 |year % 8|day of year|hour of day % 8| story number |
        //        |31|30 .. 28|27  ..   19|18     ..    16|15   ..      0|
        int n = ((Number) f.getValue()).intValue();
        final DateTime ts = getTimestamp();
        n |= ((ts.getYear() % 8) << 28); // never set first bit, valid ids are never negative
        n |= (ts.getDayOfYear() << 19);  // at least 1, so valid shortId always > 1
        n |= ((ts.getHourOfDay() % 8) << 16);
        return n;
    }

    @Override
    public Collection<SnapField> getSnapFields() {
        if (this.story == null) {
            return this.snapRecord.getSnapFields();
        }

        final ArrayList<SnapField> result = new ArrayList<>(this.snapRecord.getSnapFields());
        result.add(getField(VwdFieldDescription.NDB_Story.id()));
        result.sort(SnapFieldComparators.BY_ID);
        return result;
    }

    public SnapRecordVwd getSnapRecord() {
        return snapRecord;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "internal data structure")
    public byte[] getStory() {
        return story;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "internal data structure")
    public byte[] getRawStory() {
        return rawStory;
    }

    @Override
    public String getTeaser() {
        return resolveControlCharaters(getString(VwdFieldDescription.NDB_Teaser));
    }

    @Override
    public String getGallery() {
        return getString(VwdFieldDescription.NDB_dummy_54);
    }

    public String getSupplier() {
        return getString(VwdFieldDescription.NDB_Supplier);
    }

    @Override
    public String getNdbNewsId() {
        return getString(VwdFieldDescription.NDB_News_ID);
    }

    @Override
    public String getNdbRatingId() {
        return getString(VwdFieldDescription.NDB_RatingID);
    }

    public String getNdbAccessionNo() {
        return getString(VwdFieldDescription.NDB_Accession_No);
    }

    public String getNdbGovernment() {
        return getString(VwdFieldDescription.NDB_Government);
    }

    public Integer getNdbStoryNumber() {
        return getInt(VwdFieldDescription.NDB_Story_Number);
    }

    public Set<String> getNdbMarketSector() {
        return getAttributes(NewsAttributeEnum.MARKET_SECTOR);
    }

    public Set<String> getNdbBranch() {
        return getAttributes(NewsAttributeEnum.SECTOR);
    }

    public Set<String> getNdbCountry() {
        return getAttributes(NewsAttributeEnum.COUNTRY);
    }

    public Set<String> getNdbFixedPageCode() {
        return getAttributes(NewsAttributeEnum.FIXED_PAGE_CODE);
    }

    @Override
    public String getText() {
        if (this.text == null && this.story != null) {
            this.text = doGetText(this.story);
        }
        return this.text;
    }

    public String getRawText() {
        if (this.rawText == null && this.rawStory != null) {
            this.rawText = doGetText(this.rawStory);
        }
        return this.rawText;
    }

    private String doGetText(byte[] data) {
        if (this.snapRecord.getCharset() != null) {
            return new String(data, this.snapRecord.getCharset());
        }
        return resolveControlCharaters(ByteUtil.toString(data));
    }

    /**
     * @see <a href="http://electra2/wiki/doku.php?id=technik:cps:documentation:news">Doku</a>
     */
    @Override
    public boolean isHtml() {
        final Integer value = getInt(VwdFieldDescription.NDB_ContentDescriptor);
        return (value != null) && (value & 0x100) == 0x100;
    }

    @Override
    public boolean isNitf() {
        final Integer value = getInt(VwdFieldDescription.NDB_ContentDescriptor);
        return (value != null) && (value & 0x201) == 0x201;
    }

    @Override
    public String getMimetype() {
        if (isHtml()) {
            return "text/html";
        } else if (isNitf()) {
            return "text/xml";
        } else {
            return "text/plain";
        }
    }

    @Override
    public boolean isAd() {
        final String adField = getString(VwdFieldDescription.NDB_Werbung);
        return adField != null && !"0".equals(adField);
    }

    @Override
    public String getTextWithoutAds() {
        final String s = getText();
        if (this.adPositions == null) {
            return s;
        }
        final StringBuilder sb = new StringBuilder(s.length());
        int from = 0;
        for (int[] adPosition : this.adPositions) {
            sb.append(s.substring(from, adPosition[0]));
            from = adPosition[1];
        }
        if (from < s.length()) {
            sb.append(s.substring(from));
        }
        return sb.toString();
    }

    @Override @Nullable
    public DateTime getTimestamp() {
        DateTime result = getDateTime(VwdFieldDescription.ADF_DATEOFARR, VwdFieldDescription.ADF_TIMEOFARR);
        if (result == null) {
            // fallback for dpa-AFX Top-News
            result = getAgencyTimestamp();
        }
        if (result == null) {
            result = getNdbTimestamp();
        }
        return result;
    }

    @Nullable
    public DateTime getAgencyTimestamp() {
        return getDateTime(VwdFieldDescription.NDB_Agency_Date, VwdFieldDescription.NDB_Agency_Time);
    }

    @Nullable
    public DateTime getNdbTimestamp() {
        // see NewsRecordBuilder.doEnsureTimestamp()
        return getDateTime(VwdFieldDescription.ADF_Datum, VwdFieldDescription.ADF_Zeit);
    }

    @Nullable
    private DateTime getDateTime(VwdFieldDescription.Field dateField, VwdFieldDescription.Field timeField) {
        final Integer date = getInt(dateField);
        final Integer time = getInt(timeField);
        if (date != null && time != null) {
            return DateUtil.toDateTime(date, time);
        } else if (date != null) {
            return DateUtil.toDateTime(date, 0);
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getTopics() {
        // this.topics contains only "additional" topics, each selector is a topic of its own, so merge:
        final Set<String> result = new HashSet<>();
        if (this.topics != null) {
            result.addAll(this.topics);
        }
        for (String s : getSelectors()) {
            try {
                result.add(EntitlementsVwd.normalize(s));
            } catch (IllegalArgumentException e) {
                // empty, ignore invalid selector;
            }
        }
        return result;
    }

    @Override
    public Set<String> getWknReferences() {
        return getAttributes(NewsAttributeEnum.WKN);
    }

    @Override
    public Set<String> getCategories() {
        return getAttributes(NewsAttributeEnum.CATEGORY);
    }

    @Override
    public String getProductId() {
        return getString(VwdFieldDescription.NDB_Product_ID);
    }

    @Override
    public boolean isWithVwdsymbols() {
        if (this.instruments == null) {
            return false;
        }

        for (final Instrument instrument : this.instruments) {
            for (final Quote quote : instrument.getQuotes()) {
                if (StringUtils.hasText(quote.getSymbolVwdfeed())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setAdPositions(int[][] adPositions) {
        this.adPositions = adPositions;
    }

    public void setInstruments(Collection<Instrument> instruments) {
        this.instruments =
                instruments.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new));
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public String toString() {
        return "NewsRecordImpl["
                + " id: " + getId() + ","
                + " timestamps: " + getTimestamp() + "/" + getAgencyTimestamp() + "/" + getNdbTimestamp() + ","
                + " headline: '" + getHeadline() + "'"
                + "]";
    }

    private EnumMap<NewsAttributeEnum, Set<String>> computeAttributes() {
        final EnumMap<NewsAttributeEnum, Set<String>> result =
                new EnumMap<>(NewsAttributeEnum.class);

        for (final NewsAttributeEnum nae : NewsAttributeEnum.values()) {
            final VwdFieldDescription.Field field = nae.getField();
            final String str = getStringFieldValue(field);

            if (StringUtils.hasText(str)) {
                Set<String> value = toSet(nae, str.trim());
                if (!value.isEmpty()) {
                    result.put(nae, value);
                }
            }
        }

        return result;
    }

    private Set<String> toSet(NewsAttributeEnum nae, final String value) {
        if (nae == NewsAttributeEnum.SELECTOR && value.indexOf(' ') > 0) {
            // HACK: sometimes, selectors are separated by ' '...
            return toSet(value, "\\s+");
        }
        else if (value.indexOf(',') != -1) {
            //noinspection unchecked
            return toSet(value, ",");
        }
        return Collections.singleton(value);
    }

    private HashSet<String> toSet(String value, String pattern) {
        final HashSet<String> result = new HashSet<>();
        for (String string : value.split(pattern)) {
            if (StringUtils.hasText(string)) {
                result.add(string.trim());
            }
        }
        return result;
    }

    private String getStringFieldValue(VwdFieldDescription.Field field) {
        return (String) this.snapRecord.getField(field.id()).getValue();
    }

    private Integer getInt(final VwdFieldDescription.Field field) {
        final int i = SnapRecordUtils.getInt(this.snapRecord.getField(field.id()));
        return (i == Integer.MIN_VALUE) ? null : i;
    }

    String getString(final VwdFieldDescription.Field field) {
        return (String) this.snapRecord.getField(field.id()).getValue();
    }

    /**
     * Sometimes, news headlines/texts contain control characters that probably result from
     * incorrectly importing windows-1252 encoded data.
     * @param s to be converted to real utf-8
     * @return s with control characters replaced by the probably correct unicode chars
     * @see <a href="http://en.wikipedia.org/wiki/Windows-1252">Windows-1252</a>
     */
    private String resolveControlCharaters(String s) {
        if (s == null) {
            return null;
        }
        // optimization: usually s won't contain control chars, so only convert if we find one
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c >= '\u0080' && c <= '\u0097') {
               return resolveControlCharacters(s, i);
            }
        }
        return s;
    }

    private String resolveControlCharacters(String s, int from) {
        final StringBuilder sb = new StringBuilder(s);
        for (int i = from; i < sb.length(); i++) {
            switch (sb.charAt(i)) {
                case '\u0080': sb.setCharAt(i, '\u20AC'); break;
                case '\u0091': sb.setCharAt(i, '\u2018'); break;
                case '\u0092': sb.setCharAt(i, '\u2019'); break;
                case '\u0093': sb.setCharAt(i, '\u201C'); break;
                case '\u0094': sb.setCharAt(i, '\u201D'); break;
                case '\u0096': sb.setCharAt(i, '\u2013'); break;
                case '\u0097': sb.setCharAt(i, '\u2014'); break;
                default:
                    // empty
            }
        }
        return sb.toString();
    }
}
