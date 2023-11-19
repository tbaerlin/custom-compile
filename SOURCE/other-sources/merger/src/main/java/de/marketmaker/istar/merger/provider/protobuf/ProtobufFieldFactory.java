/*
 * ProtobufDataExtractors.java
 *
 * Created on 08.12.11 11:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import java.io.File;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.GeneratedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.merger.provider.history.eod.BCD;

/**
 * Contains many helper classes that know how to map a value in a database column with a
 * particular type to a specific value that can be used with a GeneratedMessage.Builder
 * to set a particular field.
 *
 * @author oflege
 */
class ProtobufFieldFactory {
    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final static DateTimeFormatter DTF_DATE1 = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final static DateTimeFormatter DTF_DATE2 = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        DF.applyLocalizedPattern("0.##########");
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static boolean isValidDate(DateTime dt) {
        return dt.getYear() < 2200 && dt.getYear() > 1900;
    }

    private static boolean isValidDouble(double d) {
        return (!Double.isInfinite(d) && !Double.isNaN(d)
                && Math.abs(d) >= 1E-10 && Math.abs(d) < 1E40) || d == 0d;
    }

    private static final Pattern NUMBER = Pattern.compile("[0-9]+");

    private static final char[] KEY_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static final Set<String> TRUE_VALUES
            = new HashSet<>(Arrays.asList("true", "t", "ja", "j", "1"));

    private abstract class AbstractBuilder implements ProtobufDataWriter.FieldBuilder {
        protected final Descriptors.FieldDescriptor fieldDescriptor;

        protected AbstractBuilder(Descriptors.FieldDescriptor fieldDescriptor) {
            this.fieldDescriptor = fieldDescriptor;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[" + fieldDescriptor.getName() + "]";
        }

        protected void doApply(GeneratedMessage.Builder builder, Object o) {
            if (o != null) {
                if (fieldDescriptor.isRepeated()) {
                    builder.addRepeatedField(fieldDescriptor, o);
                }
                else {
                    builder.setField(fieldDescriptor, o);
                }
            }
        }

        @Override
        public void addCachedStrings(Map<String, Map<String, String>> result) {
            final Map<String, String> map = getCachedStrings();
            if (map != null) {
                result.put(getCacheName(), map);
            }
        }

        protected Map<String, String> getCachedStrings() {
            return null;
        }

        protected String getCacheName() {
            return this.fieldDescriptor.getName();
        }

        @Override
        public void disableCache() {
        }
    }

    private class AsStringBuilder extends AbstractBuilder {
        // the cache maps long strings that are supposed to re-occur frequently to very short strings
        // that will be used in the serialized messages
        private Map<String, String> cache;

        private AsStringBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
            this.cache = fd.getOptions().getExtension(ProviderProtos.cachedStringOption)
                    ? getStringCache(fd) : null;
        }

        @Override
        public void disableCache() {
            this.cache = null;
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            doApply(builder, getCached(getString(o)));
        }

        protected String getCached(String value) {
            if (this.cache == null) {
                return value;
            }
            final String existing = this.cache.get(value);
            if (existing != null) {
                return existing;
            }
            final String newValue = nextCacheKey();
            this.cache.put(value, newValue);
            return newValue;
        }

        protected String getString(Object o) throws SQLException {
            return String.valueOf(o);
        }

        @Override
        public Map<String, String> getCachedStrings() {
            return this.cache;
        }

        public String nextCacheKey() {
            int i = this.cache.size();
            // generate the shortest possible values: "A", "B" .. "/", "AA", "AB", and so forth
            final StringBuilder sb = new StringBuilder();
            do {
                sb.append(KEY_CHARS[i & 0x3F]);
                i = i >> 6;
            } while (i != 0);
            return sb.toString();
        }
    }

    private class ClobAsStringBuilder extends AsStringBuilder {
        private ClobAsStringBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        protected String getString(Object o) throws SQLException {
            final Clob cb = (Clob) o;
            return cb.getSubString(1L, (int) cb.length());
        }
    }

    private class DateAsIntBuilder extends AbstractBuilder {
        private DateAsIntBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            if (null == o) {
                return;
            }
            if (o instanceof Integer) {
                doApply(builder, o);
            }
            else if (Date.class.isAssignableFrom(o.getClass())) {
                final DateTime dt = new DateTime(((Date) o).getTime());
                if (isValidDate(dt)) {
                    doApply(builder, dt.getYear() * 10000 + dt.getMonthOfYear() * 100 + dt.getDayOfMonth());
                }
            }
            else {
                throw new UnsupportedOperationException("no support for: " + o.getClass());
            }
        }

    }

    private class DateAsLongBuilder extends AbstractBuilder {
        private DateAsLongBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            doApply(builder, ((Timestamp) o).getTime());
        }
    }

    private class DateAsStringBuilder extends AbstractBuilder {
        private DateAsStringBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            final DateTime dt = new DateTime(((Timestamp) o).getTime());
            if (isValidDate(dt)) {
                doApply(builder, DTF.print(dt));
            }
        }
    }

    private class NumberAsLongBuilder extends AbstractBuilder {
        private NumberAsLongBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            doApply(builder, ((Number) o).longValue());
        }
    }

    private class NumberAsIntBuilder extends AbstractBuilder {
        private NumberAsIntBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            doApply(builder, ((Number) o).intValue());
        }
    }

    private class NumberAsDoubleBuilder extends AbstractBuilder {
        private NumberAsDoubleBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            final double val = Double.valueOf(o.toString());
            if (isValidDouble(val)) {
                doApply(builder, val);
            }
        }
    }

    private class RepeatedIntBuilder extends AbstractBuilder {
        private RepeatedIntBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            Matcher matcher = NUMBER.matcher(String.valueOf(o));
            while (matcher.find()) {
                doApply(builder, Integer.parseInt(matcher.group()));
            }
        }
    }

    private class RepeatedLongBuilder extends AbstractBuilder {
        private RepeatedLongBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            Matcher matcher = NUMBER.matcher(String.valueOf(o));
            while (matcher.find()) {
                doApply(builder, Long.parseLong(matcher.group()));
            }
        }
    }

    private class NumberAsStringBuilder extends AbstractBuilder {
        private NumberAsStringBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            final double val = ((Number) o).doubleValue();
            if (isValidDouble(val)) {
                doApply(builder, DF.format(val));
            }
        }
    }

    private class NumberBCDBuilder extends AbstractBuilder {
        private NumberBCDBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            final double val = ((Number) o).doubleValue();
            if (isValidDouble(val)) {
                doApply(builder, ByteString.copyFrom(BCD.encode(DF.format(val))));
            }
        }
    }

    private class StringAsBoolBuilder extends AbstractBuilder {
        private StringAsBoolBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            doApply(builder, TRUE_VALUES.contains(String.valueOf(o).toLowerCase()));
        }
    }

    private class StringAsIntBuilder extends AbstractBuilder {
        private StringAsIntBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            doApply(builder, Integer.parseInt((String) o));
        }
    }

    private class StringAsIntDateBuilder extends AbstractBuilder {
        private StringAsIntDateBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            String s = String.valueOf(o);
            if (s.length() >= 10) {
                final DateTimeFormatter formatter = getFormatter(s);
                if (formatter != null) {
                    final DateTime dt = formatter.parseDateTime(s.substring(0, 10));
                    if (isValidDate(dt)) {
                        doApply(builder, DateUtil.toYyyyMmDd(dt));
                    }
                }
            }
        }

        private DateTimeFormatter getFormatter(String s) {
            if (s.charAt(2) == '.' && s.charAt(5) == '.') {
                return DTF_DATE1;
            }
            if (s.charAt(4) == '-' && s.charAt(7) == '-') {
                return DTF_DATE2;
            }
            return null;
        }
    }

    private class LocalizedStringBuilder extends AsStringBuilder {
        private ProviderProtos.LocalizedString.Language language;

        private LocalizedStringBuilder(Descriptors.FieldDescriptor fd, String lang) {
            super(fd);
            this.language = ProviderProtos.LocalizedString.Language.valueOf(lang.toUpperCase());
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            ProviderProtos.LocalizedString.Builder b = ProviderProtos.LocalizedString.newBuilder();
            b.setLanguage(this.language);
            b.setLocalization(getCached(String.valueOf(o)));
            doApply(builder, b.build());
        }
    }

    private class UrlBuilder extends AbstractBuilder {
        private final AsStringBuilder base
                = new AsStringBuilder(ProviderProtos.Url.getDescriptor().findFieldByName("base_url"));

        private UrlBuilder(Descriptors.FieldDescriptor fd) {
            super(fd);
        }

        @Override
        public Map<String, String> getCachedStrings() {
            return this.base.getCachedStrings();
        }

        @Override
        public void disableCache() {
            this.base.disableCache();
        }

        @Override
        public String getCacheName() {
            return base.getCacheName();
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            ProviderProtos.Url.Builder b = ProviderProtos.Url.newBuilder();
            final String url = String.valueOf(o);
            final int p = url.lastIndexOf('/') + 1;
            if (p > 0) {
                base.apply(b, url.substring(0, p));
                if (!url.endsWith("/")) {
                    b.setName(url.substring(p));
                }
                doApply(builder, b.build());
            }
        }
    }

    /**
     * Builds repeated fields by copying those fields from a message optained from a
     * {@link de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader}.
     *
     */
    private class JoinFromReaderBuilder implements ProtobufDataWriter.FieldBuilder {

        private final ProtobufDataReader reader;

        private final Descriptors.FieldDescriptor[] sourceFields;

        private final Descriptors.FieldDescriptor[] targetFields;

        private boolean cacheDisabled;

        private JoinFromReaderBuilder(ProtobufDataReader reader,
                Descriptors.FieldDescriptor[] sourceFields,
                Descriptors.FieldDescriptor[] targetFields) {
            this.reader = reader;
            this.sourceFields = sourceFields;
            this.targetFields = targetFields;
        }

        @Override
        public void apply(GeneratedMessage.Builder builder, Object o) throws Exception {
            DynamicMessage message = buildSourceMessage((Long) o);
            if (message != null) {
                copyFields(builder, message);
            }
        }

        private DynamicMessage buildSourceMessage(long id) throws Exception {
            if (this.cacheDisabled) {
                DynamicMessage.Builder dmb = DynamicMessage.newBuilder(this.reader.descriptor);
                return (reader.build(id, dmb)) ? dmb.build() : null;
            }
            final byte[] data = reader.getSerialized(id);
            if (data == null) {
                return null;
            }
            return DynamicMessage.parseFrom(this.reader.descriptor, data);
        }

        private void copyFields(GeneratedMessage.Builder builder, DynamicMessage message) {
            for (int i = 0; i < this.sourceFields.length; i++) {
                copyField(builder, message, this.sourceFields[i], this.targetFields[i]);
            }
        }

        private void copyField(GeneratedMessage.Builder builder, DynamicMessage message,
                Descriptors.FieldDescriptor sourceField, Descriptors.FieldDescriptor targetField) {
            for (int i = 0; i < message.getRepeatedFieldCount(sourceField); i++) {
                builder.addRepeatedField(targetField, message.getRepeatedField(sourceField, i));
            }
        }

        @Override
        public void addCachedStrings(Map<String, Map<String, String>> result) {
            if (!this.cacheDisabled) {
                Map<String, Map<String, String>> cachedStrings = this.reader.getCachedStrings();
                for (Map.Entry<String, Map<String, String>> entry : cachedStrings.entrySet()) {
                    if (!result.containsKey(entry.getKey())) {
                        // map needs to be inverted as writer maps map from real to cached value
                        // and reader maps map from cached to real value.
                        result.put(entry.getKey(), CollectionUtils.invert(entry.getValue()));
                    }
                    else {
                        throw new IllegalStateException("Duplicate string cache name: " + entry.getKey());
                    }
                }
            }
        }

        @Override
        public void disableCache() {
            cacheDisabled = true;
        }
    }

    private final Map<String, Map<String, String>> stringCaches = new HashMap<>();

    private Map<String, String> getStringCache(Descriptors.FieldDescriptor fd) {
        final Map<String, String> existing = this.stringCaches.get(fd.getName());
        if (existing != null) {
            return existing;
        }
        final HashMap<String, String> result = new HashMap<>();
        this.stringCaches.put(fd.getName(), result);
        return result;
    }

    /**
     * Creates a FieldBuilder that copies data from messages read from reader for those fields
     * that are repeated and
     * @param reader
     * @param d
     * @return
     */
    public ProtobufDataWriter.FieldBuilder create(ProtobufDataReader reader,
            Descriptors.Descriptor d) {

        Descriptors.FieldDescriptor[] sources = new Descriptors.FieldDescriptor[
                Math.max(reader.getDescriptor().getFields().size(), d.getFields().size())];
        Descriptors.FieldDescriptor[] targets = new Descriptors.FieldDescriptor[sources.length];
        int n = 0;

        for (Descriptors.FieldDescriptor fd : reader.getDescriptor().getFields()) {
            if (fd.isRepeated()) {
                Descriptors.FieldDescriptor target = d.findFieldByName(fd.getName());
                if (target != null) {
                    if (target.isRepeated() && target.getType() == fd.getType()) {
                        sources[n] = fd;
                        targets[n++] = target;
                    }
                    else {
                        logger.warn("incompatible field '" + fd.getName() + "' in reader and descriptor");
                    }
                }
            }
        }
        if (n == 0) {
            return null;
        }
        return new JoinFromReaderBuilder(reader, Arrays.copyOf(sources, n), Arrays.copyOf(targets, n));
    }


    public ProtobufDataWriter.FieldBuilder create(Descriptors.FieldDescriptor fd,
            int columnType, String columnName) throws Exception {
        if (fd == null) {
            return null;
        }
        // OR-bedingung -> sowohl für alten als auch neuen jdbc-treiber (10g u. 11g)
        if (columnType == Types.DATE || columnType == Types.TIMESTAMP) {
            if (isInt(fd)) {
                return new DateAsIntBuilder(fd);
            }
            if (isLong(fd)) {
                return new DateAsLongBuilder(fd);
            }
            if (fd.getType() == Descriptors.FieldDescriptor.Type.STRING) {
                return new DateAsStringBuilder(fd);
            }
            throw new Exception("DATE/TIMESTAMP conversion to " + fd.getType() + " undefined");
        }

        if (columnIsNumeric(columnType)) {
            if (isLong(fd)) {
                return new NumberAsLongBuilder(fd);
            }
            if (isInt(fd)) {
                return new NumberAsIntBuilder(fd);
            }
            if (fd.getType() == Descriptors.FieldDescriptor.Type.DOUBLE) {
                return new NumberAsDoubleBuilder(fd);
            }
            if (fd.getType() == Descriptors.FieldDescriptor.Type.STRING) {
                return new NumberAsStringBuilder(fd);
            }
            if (fd.getType() == Descriptors.FieldDescriptor.Type.BYTES) {
                return new NumberBCDBuilder(fd);
            }
            throw new Exception("DECIMAL/DOUBLE/FLOAT cannot be mapped to " + fd.getType()
                    + ", columnName=" + columnName);
        }

        if (isInt(fd) && columnType == Types.VARCHAR) {
            if (columnName.toUpperCase().endsWith("DATE")) {
                return new StringAsIntDateBuilder(fd);
            }
            else {
                return new StringAsIntBuilder(fd);
            }
        }

        if (isInt(fd) && fd.isRepeated() && columnType == Types.VARCHAR) {
            return new RepeatedIntBuilder(fd);
        }

        if (isLong(fd) && fd.isRepeated() && columnType == Types.VARCHAR) {
            return new RepeatedLongBuilder(fd);
        }

        if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
            if ("protobuf.LocalizedString".equals(fd.getMessageType().getFullName())
                    && columnType == Types.VARCHAR) {
                return new LocalizedStringBuilder(fd, ProtobufDataWriter.getLocaleSuffix(columnName));
            }
            if ("protobuf.Url".equals(fd.getMessageType().getFullName())
                    && columnType == Types.VARCHAR) {
                return new UrlBuilder(fd);
            }
            throw new Exception("no mapping to " + fd.getMessageType().getFullName()
                    + " from columnType=" + columnType + ", columnName=" + columnName);
        }

        if (fd.getType() == Descriptors.FieldDescriptor.Type.BOOL) {
            if (columnType == Types.VARCHAR) {
                return new StringAsBoolBuilder(fd);
            }
        }

        if (fd.getType() == Descriptors.FieldDescriptor.Type.STRING) {
            if (columnType == Types.CLOB) {
                return new ClobAsStringBuilder(fd);
            }
            return new AsStringBuilder(fd);
        }
        throw new Exception("no mapping to " + fd.getType() + ", columnType=" + columnType
                + ", columnName=" + columnName);
    }

    private boolean columnIsNumeric(int columnType) {
        return columnType == Types.DECIMAL || columnType == Types.NUMERIC
                || columnType == Types.DOUBLE || columnType == Types.FLOAT;
    }

    private static boolean isLong(Descriptors.FieldDescriptor fd) {
        return fd.getType() == Descriptors.FieldDescriptor.Type.INT64
                || fd.getType() == Descriptors.FieldDescriptor.Type.UINT64
                || fd.getType() == Descriptors.FieldDescriptor.Type.SINT64;
    }

    private static boolean isInt(Descriptors.FieldDescriptor fd) {
        return fd.getType() == Descriptors.FieldDescriptor.Type.SINT32
                || fd.getType() == Descriptors.FieldDescriptor.Type.UINT32
                || fd.getType() == Descriptors.FieldDescriptor.Type.INT32;
    }

    public static void main(String[] args) throws Exception {
        ProtobufDataReader r = new ProtobufDataReader();
        r.setFile(new File("d:/temp/istar-wm-gv325.20120206.112605.buf"));
        r.afterPropertiesSet();

        ProtobufFieldFactory factory = new ProtobufFieldFactory();
        ProtobufDataWriter.FieldBuilder gv325
                = factory.create(r, WmDataProtos.WmMasterData.getDescriptor());

        WmDataProtos.WmMasterData.Builder builder = WmDataProtos.WmMasterData.newBuilder();
        builder.setIid(663144);
        gv325.apply(builder, 663144L);
        System.out.println(builder.getGv325(0));
        System.out.println(builder.getGv325(1));
    }
}
