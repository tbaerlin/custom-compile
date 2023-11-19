/*
 * ProtobufDataWriter.java
 *
 * Created on 03.11.11 10:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.apache.commons.io.output.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.PropertiesLoader;

/**
 * A {@link ResultSetExtractor} that is supposed to be used by an instance of
 * <tt>de.marketmaker.dp2.diverseexporter.GenericExportWriter</tt>
 * <p>
 * Data can be written in two different formats:
 * <dl>
 * <dt>with index
 * <dd>At runtime, the {@link ProtobufDataReader} only keeps the index in memory,
 * data will be retrieved from disk for each requested item. This format requires the presence
 * of a (numeric) column containing the data's primary key that will also be
 * used to retrieve the data.
 * <dt>without index
 * <dd>Use this if the file is supposed to be read completely by some other process, data can
 * be deserialized using a {@link ProtobufDataStream}
 * </dl>
 * </p>
 * <p>Configuration options:
 * <dl>
 * <dt><tt>out</tt></dt>
 * <dd>Name of the output file, without suffix
 * <dt><tt>suffix</tt></dt>
 * <dd>Suffix of the output file (default is <tt>.buf</tt>).
 * Files can be compressed by setting this property
 * to a value that ends with <tt>.gz</tt>. In order to be able to read a file with an index, the
 * file needs to be uncompressed as the index is stored at the end of the file and without it the
 * data records cannot be deserialized. Records from a file without an index can be deserialized
 * by reading directly from the compressed file.
 * <dt><tt>protobufClassName</tt></dt>
 * <dd>Qualified name of the protobuf message class that corresponds to each row.
 * <dt><tt>keyColumn</tt></dt>
 * <dd>Name of the column that contains the messages key. If undefined, the column names
 * "QID", "QUOTE", "IID", or "SECURITY" will be used to find the key column</dd>
 * </dl>
 * <p>
 * Instances are {@link ApplicationContextAware}. If an application context is available, this class
 * will query it for {@link ProtobufDataReader} instances and will try to match the name/type of
 * repeated fields in the datastructures created by those readers
 * with a field/type in the message to be created. If such a match is found, the reader will be used
 * to provide data for each row.
 * </p>
 *
 * @author oflege
 */
public class ProtobufDataWriter implements ResultSetExtractor, ApplicationContextAware {

    private static final String CLASSPATH_PREFIX = "classpath:";

    private static final Pattern LOCALIZED_NAME
            = Pattern.compile("(.*?)_+([a-z]{2})", Pattern.CASE_INSENSITIVE);

    private static final int LENGTH_MASK = 0xFFFFFF;

    interface FieldBuilder {
        void apply(GeneratedMessage.Builder builder, Object o) throws Exception;

        void addCachedStrings(Map<String, Map<String, String>> result);

        void disableCache();
    }

    static class ColumnInfo {
        final int index;

        final String name;

        final FieldBuilder builder;

        /**
         * A column that is not contained in the ResultSet but will be provided by some other means
         */
        ColumnInfo(String name, FieldBuilder builder) {
            this(0, name, builder);
        }

        ColumnInfo(int index, String name, FieldBuilder builder) {
            this.index = index;
            this.name = name;
            this.builder = builder;
        }
    }

    static boolean isLocalizedColumn(String name) {
        return LOCALIZED_NAME.matcher(name).matches();
    }

    private static String getNameWithoutLocaleSuffix(String name) {
        final Matcher matcher = LOCALIZED_NAME.matcher(name);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException();
    }

    static String getLocaleSuffix(String name) {
        final Matcher matcher = LOCALIZED_NAME.matcher(name);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return "DE";
    }

    private static final int NO_INDEX_FILE_VERSION = 0;

    private static final int FILE_VERSION = 1;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext context;

    private File workfile;

    private ResultSet rs;

    private Method createBuilderMethod;

    protected Properties props;

    protected int keyColumn;

    private long[] keys;

    private long[] offsets;

    private int numObjects = 0;

    // current offset in workfile, only used if keyColumn is defined
    private long offset;

    // one object for each db column that can be mapped to a protobuf message field
    private List<ColumnInfo> columns = null;

    private int row = 0;

    private long numBytes = 0;

    private DataOutputStream os;

    /**
     * Since {@link java.io.DataOutputStream#size()} is limited to at most 2gb and we may write more,
     * we need to add this additional stream to count bytes.
     */
    private CountingOutputStream osCounter;

    private CodedOutputStream cos;

    private final ProtobufFieldFactory fieldFactory = new ProtobufFieldFactory();

    public ProtobufDataWriter() throws Exception {
        this.props = loadProperties();

        // has to be like this, see de.marketmaker.dp2.diverseexporter.GenericExportWriter
        final String workdir = props.getProperty("workdir", System.getProperty("java.io.tmpdir"));
        final String suffix = this.props.getProperty("suffix", ".buf");
        final String name = props.getProperty("workfile", "." + props.getProperty("out") + suffix);
        final String fileName = resolveFileName(name);
        this.workfile = new File(workdir, fileName);
        if (!this.workfile.getParentFile().isDirectory() && !this.workfile.getParentFile().mkdirs()) {
            throw new IOException("failed to create " + this.workfile.getParentFile().getAbsolutePath());
        }
    }

    private static final Pattern FILE_NAME_TEMPLATE = Pattern.compile("\\$\\{[yMdHms_-]+\\}");

    protected static String resolveFileName(String name) {
        final Matcher matcher = FILE_NAME_TEMPLATE.matcher(name);
        if (matcher.find()) {
            return matcher.replaceAll(resolveTimeExpr(
                    name.substring(matcher.start() + 2, matcher.end() - 1)));
        }
        else {
            return name;
        }
    }

    private static String resolveTimeExpr(String timeExpr) {
        return DateTimeFormat.forPattern(timeExpr).print(
                new DateTime(ManagementFactory.getRuntimeMXBean().getStartTime()));
    }

    protected Properties loadProperties() throws IOException {
        return PropertiesLoader.load(getConfig());
    }

    private static InputStream getConfig() throws IOException {
        final String cfgfile = System.getProperty("cfgfile");
        if (cfgfile.startsWith(CLASSPATH_PREFIX)) {
            return new ClassPathResource(cfgfile.substring(CLASSPATH_PREFIX.length())).getInputStream();
        }
        return new FileInputStream(new File(cfgfile));
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    protected GeneratedMessage.Builder createBuilder() throws Exception {
        if (this.createBuilderMethod == null) {
            final String className = props.getProperty("protobufClassName");
            final Class<?> clazz = Class.forName(className);
            this.createBuilderMethod = clazz.getDeclaredMethod("newBuilder");
        }
        return (GeneratedMessage.Builder) this.createBuilderMethod.invoke(null);
    }

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
        this.rs = rs;
        try {
            beforeFirstRow();
            createOutputStreams();

            while (rs.next()) {
                this.row++;
                handleRow();
            }
            afterLastRow();
            this.logger.info("<extractData> succeeded");
        } catch (Exception e) {
            IoUtils.close(this.os);
            if (this.workfile.exists() && !this.workfile.delete()) {
                this.logger.error("<extractData> failed to delete " + this.workfile.getAbsolutePath());
            }
            this.logger.error("<extractData> failed", e);
            throw new RuntimeException(e);
        }
        return this.row;
    }

    protected void beforeFirstRow() throws Exception {
        this.columns = initColumns(createBuilder().getDescriptorForType());
        initKeyColumn();
    }

    protected void createOutputStreams() throws IOException {
        final FileOutputStream fos = new FileOutputStream(this.workfile);
        final FilterOutputStream os = this.workfile.getName().endsWith(".gz")
                ? new GZIPOutputStream(fos)
                : new BufferedOutputStream(fos);
        this.osCounter = new CountingOutputStream(os);
        this.os = new DataOutputStream(this.osCounter);
        this.os.writeUTF(getMainMessageClassName());
        this.offset = this.os.size();

        this.cos = CodedOutputStream.newInstance(this.os, getBufferSize());
    }

    private int getBufferSize() {
        final String bufferSize = this.props.getProperty("outputBufferSize");
        return StringUtils.hasText(bufferSize)
                ? Integer.parseInt(bufferSize.trim()) : CodedOutputStream.DEFAULT_BUFFER_SIZE;
    }

    protected String getMainMessageClassName() {
        return this.props.getProperty("protobufClassName");
    }

    private void initKeyColumn() throws Exception {
        this.keyColumn = findKeyColumnIndex();
        this.logger.info("<initKeyColumn> keyColumn = " + this.keyColumn);
        if (this.keyColumn > 0) {
            this.keys = new long[1 << 16];
            this.offsets = new long[this.keys.length];
        }
        else {
            for (ColumnInfo column : this.columns) {
                column.builder.disableCache();
            }
        }
    }

    protected void afterLastRow() throws Exception {
        this.logger.info("<afterLastRow> read " + this.row + " rows, wrote " + this.numObjects
                + " objects, " + (this.numBytes / Math.max(1, this.numObjects))
                + " bytes/object on avg");
        if (this.keys != null) {
            writeStringCaches();
            this.offset = this.osCounter.getByteCount();

            writeKeysAndOffsets();

            this.os.writeLong(this.offset);
            this.os.writeInt(FILE_VERSION);
        }
        else {
            this.cos.writeRawVarint32(0); // length 0 => end of file
            this.cos.flush();
            this.os.writeInt(NO_INDEX_FILE_VERSION);
        }

        os.close();
    }

    private void writeStringCaches() throws IOException {
        final Map<String, Map<String, String>> caches = getStringCaches();
        this.os.writeInt(caches.size());
        for (Map.Entry<String, Map<String, String>> e : caches.entrySet()) {
            this.os.writeUTF(e.getKey());
            this.os.writeInt(e.getValue().size());
            for (Map.Entry<String, String> entry : e.getValue().entrySet()) {
                this.os.writeUTF(entry.getValue());
                this.os.writeUTF(entry.getKey());
            }
            this.logger.info("<writeStringCaches> '" + e.getKey() + "' #" + e.getValue().size());
        }
    }

    protected Map<String, Map<String, String>> getStringCaches() {
        return getStringCaches(this.columns);
    }

    protected Map<String, Map<String, String>> getStringCaches(List<ColumnInfo> infos) {
        final Map<String, Map<String, String>> result = new HashMap<>();
        for (ColumnInfo column : infos) {
            column.builder.addCachedStrings(result);
        }
        return result;
    }

    private void writeKeysAndOffsets() throws IOException {
        this.keys = Arrays.copyOf(this.keys, this.numObjects);
        this.offsets = Arrays.copyOf(this.offsets, this.numObjects);
        ArraysUtil.sort(this.keys, this.offsets);

        for (int i = 0; i < this.numObjects; i++) {
            this.os.writeLong(keys[i]);
            this.os.writeLong(offsets[i]);
        }
    }

    protected void handleRow() throws Exception {
        final GeneratedMessage.Builder builder = createBuilder();

        long id = 0;
        if (this.keyColumn > 0) {
            id = getKey();
            if (id == 0 && wasNull()) {
                this.logger.warn("<handleRow> no key column in row " + row);
                return;
            }
        }

        handleColumns(builder, id, this.columns);
        handleRowObject(id, builder);
    }

    protected void handleColumns(GeneratedMessage.Builder builder, long id,
            final List<ColumnInfo> columnInfos) throws SQLException {

        final StringBuilder sb = this.logger.isDebugEnabled()
                ? new StringBuilder(200).append(id) : null;

        for (ColumnInfo column : columnInfos) {
            try {
                if (column.index > 0) {
                    final Object o = getObject(column.index);
                    final boolean wasNull = wasNull();
                    if (!wasNull) {
                        column.builder.apply(builder, o);
                    }
                    if (sb != null) {
                        sb.append(";").append(column.name).append("=").append(wasNull ? "<null>" : o);
                    }
                }
                else {
                    column.builder.apply(builder, id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (sb != null) {
            this.logger.debug(sb.toString());
        }
    }

    protected long getKey() throws SQLException {
        return this.rs.getLong(this.keyColumn);
    }

    protected Object getObject(final int index) throws SQLException {
        return this.rs.getObject(index);
    }

    protected String getString(final int index) throws SQLException {
        return this.rs.getString(index);
    }

    protected boolean wasNull() throws SQLException {
        return this.rs.wasNull();
    }

    protected void handleRowObject(long id, GeneratedMessage.Builder builder) throws Exception {
        if (builder.isInitialized()) {
            writeObject(id, builder);
        }
    }

    protected void writeObject(long id, GeneratedMessage.Builder builder) throws IOException {
        final byte[] bytes = builder.build().toByteArray();
        if (this.keys != null) {
            addKeyAndOffset(id, getEncodedOffsetAndLength(bytes));
            this.os.write(bytes);
            this.offset += bytes.length;
        }
        else {
            this.cos.writeRawVarint32(bytes.length);
            this.cos.writeRawBytes(bytes);
        }

        this.numObjects++;
        this.numBytes += bytes.length;
    }

    private void addKeyAndOffset(long id, long encodedOffsetAndLength) {
        if (this.numObjects == this.keys.length) {
            this.keys = Arrays.copyOf(this.keys, this.keys.length + (1 << 16));
            this.offsets = Arrays.copyOf(this.offsets, this.keys.length);
        }
        this.keys[this.numObjects] = id;
        this.offsets[this.numObjects] = encodedOffsetAndLength;
    }

    private long getEncodedOffsetAndLength(byte[] bytes) throws IOException {
        if ((bytes.length & ~LENGTH_MASK) != 0) {
            throw new IOException(new IllegalArgumentException("max size exceeded: " + bytes.length));
        }
        return (this.offset << 24) | bytes.length;
    }

    static long decodeOffset(final long offsetAndLength) {
        return offsetAndLength >> 24;
    }

    static int decodeLength(final long offsetAndLength) {
        return (int) offsetAndLength & LENGTH_MASK;
    }


    protected List<ColumnInfo> initColumns(Descriptors.Descriptor d) throws Exception {
        final Map<String, Descriptors.FieldDescriptor> descriptorsByName
                = getDescriptorsByName(d);
        this.logger.info("<initColumns> descriptor names: "
                + new TreeSet<>(descriptorsByName.keySet()));

        return getColumns(d, descriptorsByName);
    }

    private List<ColumnInfo> getColumns(Descriptors.Descriptor d,
            Map<String, Descriptors.FieldDescriptor> descriptorsByName) throws Exception {
        Map<String, Integer> dbColumns = getColumns();

        final ArrayList<ColumnInfo> result = new ArrayList<>();
        int columnIndex = 0;
        for (Map.Entry<String, Integer> entry : dbColumns.entrySet()) {
            columnIndex++;
            final String name = entry.getKey();
            final int type = entry.getValue();
            final Descriptors.FieldDescriptor fd
                    = findFieldDescriptor(name.toLowerCase(), descriptorsByName);
            if (fd != null) {
                result.add(new ColumnInfo(columnIndex, name, fieldFactory.create(fd, type, name)));
            }
        }
        if (this.context != null) {
            result.addAll(getContextColumns(d));
        }
        return result;
    }

    private List<ColumnInfo> getContextColumns(Descriptors.Descriptor d) throws Exception {
        final ArrayList<ColumnInfo> result = new ArrayList<>();
        for (String name : this.context.getBeanNamesForType(ProtobufDataReader.class)) {
            ProtobufDataReader reader = (ProtobufDataReader) context.getBean(name);
            FieldBuilder builder = fieldFactory.create(reader, d);
            if (builder != null) {
                result.add(new ColumnInfo(name, builder));
            }
        }
        return result;
    }

        private Descriptors.FieldDescriptor findFieldDescriptor(String name,
            Map<String, Descriptors.FieldDescriptor> descriptorsByName) {
        Descriptors.FieldDescriptor fd = descriptorsByName.get(name.toLowerCase());
        if (fd == null) {
            fd = descriptorsByName.get(name.toLowerCase().replace("_", ""));
            if (fd == null && isLocalizedColumn(name)) {
                fd = descriptorsByName.get(getNameWithoutLocaleSuffix(name).toLowerCase());
            }
        }
        return fd;
    }

    protected Map<String, Integer> getColumns() throws SQLException {
        final Map<String, Integer> result = new LinkedHashMap<>();
        final ResultSetMetaData rsmd = this.rs.getMetaData();
        for (int i = 0; i++ < rsmd.getColumnCount(); ) {
            result.put(resolveColumnName(rsmd.getColumnName(i)), rsmd.getColumnType(i));
        }
        return result;
    }

    protected String resolveColumnName(String columnName) {
        return this.props.getProperty("mappedColumnName." + columnName, columnName);
    }

    private Map<String, Descriptors.FieldDescriptor> getDescriptorsByName(Descriptors.Descriptor d)
            throws Exception {
        final Map<String, Descriptors.FieldDescriptor> result = new HashMap<>();
        for (Descriptors.FieldDescriptor fd : d.getFields()) {
            result.put(fd.getName().toLowerCase().replace("_", ""), fd);
        }
        return result;
    }

    protected int findKeyColumnIndex() throws Exception {
        final String keyColumnName = this.props.getProperty("keyColumn");
        if (keyColumnName != null) {
            final int result = findColumnIndex(keyColumnName);
            if (result == 0) {
                throw new Exception("keyColumn " + keyColumnName + " not found");
            }
            return result;
        }
        for (String col : new String[]{"QID", "QUOTE", "IID", "SECURITY"}) {
            int keyColumn = findColumnIndex(col);
            if (keyColumn != 0) {
                return keyColumn;
            }
        }
        return keyColumn;
    }

    protected int findColumnIndex(String name) throws Exception {
        int columnIndex = 0;
        for (String columnName : getColumns().keySet()) {
            columnIndex++;
            if (name.equals(columnName)) {
                return columnIndex;
            }
        }
        return 0;
    }
}
