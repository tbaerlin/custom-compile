/*
 * ProtobufDataReader.java
 *
 * Created on 02.11.11 12:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;

/**
 * Reads data that has been created using a {@link ProtobufDataWriter} and contains index based records.
 * The most complicated aspect is that if the file has been created with cached strings, these strings
 * have to be resolved during deserialization.
 * @author oflege
 */
public class ProtobufDataReader implements InitializingBean, DisposableBean {
    /**
     * info about data serialized in a specific file
     */
    private static class FileInfo {
        final String mainClassName;

        final long[][] keysAndOffsets;

        final Map<String, Map<String, String>> cachedStrings
                = new HashMap<>();

        private FileInfo(String mainClassName, long[][] keysAndOffsets) {
            this.mainClassName = mainClassName;
            this.keysAndOffsets = keysAndOffsets;
        }

        void add(String name, Map<String, String> cache) {
            this.cachedStrings.put(name, cache);
        }

        Descriptors.Descriptor getDescriptor() throws Exception {
            final Class<?> clazz = Class.forName(this.mainClassName);
            Method getDecriptor = clazz.getDeclaredMethod("getDescriptor");
            return (Descriptors.Descriptor) getDecriptor.invoke(null);
        }

        public Map<Descriptors.Descriptor, Set<Descriptors.FieldDescriptor>>
        getFieldsWithCachedStrings(Descriptors.Descriptor d) {
            if (d == null || this.cachedStrings.isEmpty()) {
                return Collections.emptyMap();
            }
            final Map<Descriptors.Descriptor, Set<Descriptors.FieldDescriptor>> result
                    = new HashMap<>();

            addFieldsWithCachedStrings(result, d);
            return result;
        }

        private void addFieldsWithCachedStrings(Map<Descriptors.Descriptor,
                Set<Descriptors.FieldDescriptor>> result, Descriptors.Descriptor d) {
            final Set<Descriptors.FieldDescriptor> fieldDescriptors
                    = new HashSet<>();
            for (Descriptors.FieldDescriptor fd : d.getFields()) {
                if (this.cachedStrings.containsKey(fd.getName())) {
                    fieldDescriptors.add(fd);
                }
                else if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                    final int sizeBefore = result.size();
                    addFieldsWithCachedStrings(result, fd.getMessageType());
                    if (result.size() > sizeBefore) {
                        fieldDescriptors.add(fd);
                    }
                }
            }
            if (!fieldDescriptors.isEmpty()) {
                result.put(d, fieldDescriptors);
            }
        }
    }

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected ActiveMonitor activeMonitor;

    protected File file;

    private DataFile df;

    private boolean onUpdateMove = true;

    // [0][] contains the keys in ascending order, [1][] the corresponding encoded offset&length
    protected long[][] keysAndOffsets = new long[2][0];

    protected final Object mutex = new Object();

    /**
     * If a message contains string fields with cached values or sub-messages with cached string values,
     * this map will contain an entry for that message's descriptor with a set of all fields that have
     * to be processed to resolve cached string values.
     */
    private Map<Descriptors.Descriptor, Set<Descriptors.FieldDescriptor>> fieldsWithCachedStrings
            = Collections.emptyMap();

    /**
     * descriptor for top-level messages encoded in our file
     */
    protected Descriptors.Descriptor descriptor;

    /**
     * For each field with cached string values, this map contains another map that maps the abbreviated
     * keys that are stored in the message fields to the actual field values.
     */
    private Map<String, Map<String, String>> cachedStrings = new HashMap<>();

    protected ProtobufDataReader() {
    }

    protected ProtobufDataReader(Descriptors.Descriptor d) {
        this.descriptor = d;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setOnUpdateMove(boolean onUpdateMove) {
        this.onUpdateMove = onUpdateMove;
    }

    public void setDescriptor(Descriptors.Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public Descriptors.Descriptor getDescriptor() {
        return descriptor;
    }

    Map<String, Map<String, String>> getCachedStrings() {
        return this.cachedStrings;
    }

    @Override
    public void destroy() throws Exception {
        synchronized (this.mutex) {
            IoUtils.close(this.df);
            this.keysAndOffsets = new long[2][0];
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.file.canRead()) {
            onUpdate(this.file);
        }
        else if (fileToMonitor().canRead()) {
            onUpdate(fileToMonitor());
        }
        else {
            throw new IllegalStateException("no such file " + this.file.getAbsolutePath());
        }

        if (this.activeMonitor != null) {
            final FileResource resource = new FileResource(fileToMonitor());
            resource.addPropertyChangeListener(evt -> onUpdate(resource.getFile()));
            this.activeMonitor.addResource(resource);
            final FileResource gzResource = new FileResource(gzFileToMonitor());
            gzResource.addPropertyChangeListener(evt -> onUpdate(gzResource.getFile()));
            this.activeMonitor.addResource(gzResource);
        }
    }

    private FileInfo readFileInfo(final File file) throws IOException {
        if (!file.canRead()) {
            this.logger.warn("<readFileInfo> cannot read " + file.getAbsolutePath());
            return new FileInfo(null, new long[0][]);
        }
        DataFile df = null;
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            String mainClassName = dis.readUTF();

            df = new DataFile(file, true);
            long indexEnd = df.size() - 12;
            df.seek(indexEnd);
            long indexStart = df.readLong();
            int version = df.readInt();
            if (version != 1) {
                throw new IllegalStateException("Cannot read version " + version);
            }
            df.seek(indexStart);
            final int num = (int) ((indexEnd - indexStart) / 16);
            final FileInfo result = new FileInfo(mainClassName, readKeysAndOffsets(df, num));
            this.logger.info("<readFileInfo> read " + result.keysAndOffsets[0].length
                    + " index entries");

            final long dataEnd = computeDataEnd(result.keysAndOffsets[1]);
            if (dataEnd > indexStart) {
                throw new IOException("dataEnd " + dataEnd + " > indexStart " + indexStart);
            }
            if (dataEnd != indexStart) {
                df.seek(dataEnd);
                ByteBuffer bb = ByteBuffer.allocate((int) (indexStart - dataEnd));
                while (bb.hasRemaining()) {
                    if (df.read(bb) == 0) {
                        throw new IOException();
                    }
                }
                readCaches(result, bb);
                this.logger.info("<readFileInfo> read caches (" + bb.capacity() + ") bytes");
            }

            return result;
        } finally {
            IoUtils.close(dis);
            IoUtils.close(df);
        }
    }

    private void readCaches(FileInfo result, ByteBuffer bb) throws IOException {
        final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bb.array()));
        final int numCaches = dis.readInt();
        for (int i = 0; i < numCaches; i++) {
            final HashMap<String, String> map = new HashMap<>();
            final String name = dis.readUTF();
            final int numEntries = dis.readInt();
            for (int k = 0; k < numEntries; k++) {
                map.put(dis.readUTF(), dis.readUTF());
            }
            result.add(name, map);
            this.logger.info("<readCaches> " + name + ", #" + map.size());
        }
    }

    private long computeDataEnd(long[] offsets) {
        long mx = 0;
        for (long value : offsets) {
            mx = Math.max(mx, value);
        }
        return ProtobufDataWriter.decodeLength(mx) + ProtobufDataWriter.decodeOffset(mx);
    }

    private long[][] readKeysAndOffsets(DataFile df, int num) throws IOException {
        final long[][] result = new long[2][num];
        final ByteBuffer bb = ByteBuffer.allocate(1024 * 16);
        bb.flip();
        for (int i = 0; i < num; i++) {
            if (!bb.hasRemaining()) {
                bb.clear();
                bb.limit(Math.min(num - i, 1024) * 16);
                df.read(bb);
                bb.flip();
            }
            result[0][i] = bb.getLong();
            result[1][i] = bb.getLong();
        }
        return result;
    }

    protected void onUpdate(File f) {
        TimeTaker tt = new TimeTaker();
        try {
            if (f.getAbsolutePath().endsWith(".gz")) {
                this.logger.info("<onUpdate> unGZipping " + f.getAbsolutePath());
                File unGZipped = new File(f.getAbsolutePath().substring(0, f.getAbsolutePath().length() - 3));
                FileUtil.unGZip(f, unGZipped, true);
                f = unGZipped;
            }
            this.logger.info("<onUpdate> starting to read " + f.getAbsolutePath());
            FileInfo info = readFileInfo(f);
            synchronized (this.mutex) {
                IoUtils.close(this.df);
                if (!this.file.equals(f)) {
                    FileUtil.deleteIfExists(this.file);
                    TimeTaker ttm = new TimeTaker();
                    if (this.onUpdateMove) {
                        this.logger.info("<onUpdate> move " + f.getAbsolutePath() + " to " + this.file.getAbsolutePath());
                        FileUtils.moveFile(f, this.file);
                        this.logger.info("<onUpdate> move " + f.getAbsolutePath() + " to " + this.file.getAbsolutePath() + " took " + ttm);
                    }
                    else {
                        this.logger.info("<onUpdate> copy " + f.getAbsolutePath() + " to " + this.file.getAbsolutePath());
                        FileUtils.copyFile(f, this.file);
                        this.logger.info("<onUpdate> copy " + f.getAbsolutePath() + " to " + this.file.getAbsolutePath() + " took " + ttm);
                    }
                }
                if (this.descriptor == null) {
                    this.descriptor = info.getDescriptor();
                }
                this.keysAndOffsets = info.keysAndOffsets;
                this.cachedStrings = info.cachedStrings;
                this.fieldsWithCachedStrings = info.getFieldsWithCachedStrings(this.descriptor);
                if (this.file.canRead()) {
                    this.df = new DataFile(this.file, true);
                }
            }
            this.logger.info("<onUpdate> did read " + f.getAbsolutePath() + " took " + tt);
        } catch (Exception e) {
            this.logger.error("<onUpdate> failed", e);
            IoUtils.close(this.df);
            this.df = null;
            this.keysAndOffsets = null;
            this.cachedStrings = null;
        }
    }

    protected byte[] getSerialized(long id) {
        synchronized (this.mutex) {
            if (this.df == null) {
                return null;
            }
            int idx = Arrays.binarySearch(this.keysAndOffsets[0], id);
            if (idx < 0) {
                return null;
            }
            return getSerialized(id, this.keysAndOffsets[1][idx]);
        }
    }

    protected byte[] getSerialized(long id, long offsetAndLengh) {
        final long offset = ProtobufDataWriter.decodeOffset(offsetAndLengh);
        final int length = ProtobufDataWriter.decodeLength(offsetAndLengh);
        try {
            if (this.df.position() != offset) {
                this.df.seek(offset);
            }
            ByteBuffer bb = ByteBuffer.allocate(length);
            while (bb.hasRemaining()) {
                this.df.read(bb);
            }
            return bb.array();
        } catch (IOException e) {
            this.logger.error("<getSerialized> failed for id=" + id + ", offset=" + offset
                    + ", length=" + length, e);
            return null;
        }
    }

    private File fileToMonitor() {
        return new File(this.file.getParentFile(), "incoming/" + this.file.getName());
    }

    private File gzFileToMonitor() {
        return new File(this.file.getParentFile(), "incoming/" + this.file.getName() + ".gz");
    }

    protected boolean build(long id, Message.Builder builder)
            throws InvalidProtocolBufferException {
        synchronized (this.mutex) {
            final byte[] bytes = getSerialized(id);
            if (bytes == null) {
                return false;
            }
            builder.mergeFrom(bytes);
            if (!this.fieldsWithCachedStrings.isEmpty()) {
                resolveCachedStrings(builder, this.descriptor);
            }
            return true;
        }
    }

    private void resolveCachedStrings(Message.Builder builder, Descriptors.Descriptor d) {
        final Set<Descriptors.FieldDescriptor> withCachedStrings = this.fieldsWithCachedStrings.get(d);
        if (withCachedStrings == null) {
            return;
        }
        for (Descriptors.FieldDescriptor fd : withCachedStrings) {
            if (fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                if (isRepeatedLocalizedString(fd)) {
                    resolveCachedLocalizedStrings(builder, fd);
                }
                else if (fd.isRepeated()) {
                    for (int i = 0; i < builder.getRepeatedFieldCount(fd); i++) {
                        builder.setRepeatedField(fd, i,
                                resolveCachedStrings(builder, fd, builder.getRepeatedField(fd, i)));
                    }
                }
                else if (builder.hasField(fd)) {
                    builder.setField(fd, resolveCachedStrings(builder, fd, builder.getField(fd)));
                }
            }
            else {
                if (fd.isRepeated()) {
                    final int n = builder.getRepeatedFieldCount(fd);
                    for (int i = 0; i < n; i++) {
                        String key = (String) builder.getRepeatedField(fd, i);
                        builder.setRepeatedField(fd, i, getCachedStringValue(fd, key));
                    }
                }
                else {
                    if (builder.hasField(fd)) {
                        final String key = builder.getField(fd).toString();
                        String value = getCachedStringValue(fd, key);
                        if (value == null) {
                            throw new IllegalArgumentException("no cached value for key '" + key
                                    + "', field " + fd.getFullName());
                        }
                        builder.setField(fd, value);
                    }
                }

            }
        }
    }

    private Object resolveCachedStrings(Message.Builder parentBuilder,
            Descriptors.FieldDescriptor fd, Object value) {
        final Message.Builder builder = parentBuilder.newBuilderForField(fd);
        builder.mergeFrom((Message) value);
        resolveCachedStrings(builder, fd.getMessageType());
        return builder.build();
    }

    private void resolveCachedLocalizedStrings(Message.Builder builder,
            Descriptors.FieldDescriptor fd) {
        final int n = builder.getRepeatedFieldCount(fd);
        for (int i = 0; i < n; i++) {
            final ProviderProtos.LocalizedString ls
                    = (ProviderProtos.LocalizedString) builder.getRepeatedField(fd, i);
            final ProviderProtos.LocalizedString.Builder lsb
                    = ProviderProtos.LocalizedString.newBuilder(ls);
            lsb.setLocalization(getCachedStringValue(fd, ls.getLocalization()));
            builder.setRepeatedField(fd, i, lsb.build());
        }
    }

    protected static boolean isRepeatedLocalizedString(Descriptors.FieldDescriptor fd) {
        return fd.isRepeated() && "protobuf.LocalizedString".equals(fd.getMessageType().getFullName());
    }

    private String getCachedStringValue(Descriptors.FieldDescriptor fd, String key) {
        Map<String, String> cache = this.cachedStrings.get(fd.getName());
        return (cache != null) ? cache.get(key) : key;
    }

    protected static String toUrl(ProviderProtos.Url url) {
        return (url.hasName()) ? url.getBaseUrl() + url.getName() : url.getBaseUrl();
    }

    protected static DateTime toDateTime(long millis) {
        if (millis == 0) {
            return null;
        }
        return new DateTime(millis);
    }

    protected static DateTime toDateTime(int yyyymmdd) {
        if (yyyymmdd == 0) {
            return null;
        }
        return toLocalDate(yyyymmdd).toDateTimeAtStartOfDay();
    }

    protected static LocalDate toLocalDate(int yyyymmdd) {
        if (yyyymmdd == 0) {
            return null;
        }
        return DateUtil.yyyyMmDdToLocalDate(yyyymmdd);
    }

    protected static BigDecimal toBigDecimal(String value) {
        return new BigDecimal(value);
    }

    protected static LocalizedString toLocalizedString(List<ProviderProtos.LocalizedString> ls) {
        LocalizedString.Builder builder = new LocalizedString.Builder();
        for (ProviderProtos.LocalizedString l : ls) {
            builder.add(l.getLocalization(), Language.values()[l.getLanguage().getNumber()]);
        }
        return builder.build();
    }
}
