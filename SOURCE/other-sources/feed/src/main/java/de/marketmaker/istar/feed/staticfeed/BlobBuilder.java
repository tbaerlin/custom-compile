/*
 * BlobBuilder.java
 *
 * Created on 08.05.14 11:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static com.netflix.servo.annotations.DataSourceType.GAUGE;
import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.toDateTime;
import static org.joda.time.DateTimeConstants.MILLIS_PER_HOUR;
import static org.joda.time.DateTimeConstants.MILLIS_PER_MINUTE;

/**
 * @author oflege
 */
@ManagedResource
public class BlobBuilder implements InitializingBean, DisposableBean {

    private static final int MAX_FILE_SIZE = 1 << 24; // 16MB

    static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd_HHmmss_SSS");

    private static final byte[] LINEFEED = new byte[]{0x0a};

    private class BlobFile {

        private long created = System.currentTimeMillis();

        private boolean autoChunk;

        private int lastAppendTimestamp = 0;

        private int id;

        private String fileName;

        private File tmpFile;

        private FileChannel fc;

        private int expectedChunkNo = 0;

        private int numBytes = 0;

        private boolean endsWithLinefeed = true;

        private BlobFile(int id, String fileName) {
            this.id = id;
            this.fileName = fileName;
            this.autoChunk = false;
            this.tmpFile = null;
            this.fc = null;
        }

        private BlobFile(int id, String fileName, File tmpFile) throws IOException {
            this.id = id;
            this.fileName = fileName;
            this.autoChunk = autoChunkFilenames.contains(fileName);

            this.tmpFile = tmpFile;
            this.fc = new RandomAccessFile(tmpFile, "rw").getChannel();
        }

        private BlobFile(DataInputStream is) throws IOException {
            this.created = is.readLong();
            this.autoChunk = is.readBoolean();
            this.lastAppendTimestamp = is.readInt();
            this.id = is.readInt();
            this.expectedChunkNo = is.readInt();
            this.numBytes = is.readInt();
            this.endsWithLinefeed = is.readBoolean();

            this.fileName = is.readUTF();
            String fn = is.readUTF();
            this.tmpFile = StringUtils.hasText(fn) ? new File(outDir, fn) : null;

            if (this.tmpFile != null) {
                if (this.tmpFile.canRead()) {
                    this.fc = new RandomAccessFile(this.tmpFile, "rw").getChannel();
                    this.fc.position(this.tmpFile.length());
                }
                else {
                    logger.warn("<BlobFile> cannot read " + this.tmpFile.getAbsolutePath());
                    this.tmpFile = null;
                }
            }
        }

        void writeTo(DataOutputStream os) throws IOException {
            os.writeLong(this.created);
            os.writeBoolean(this.autoChunk);
            os.writeInt(this.lastAppendTimestamp);
            os.writeInt(this.id);
            os.writeInt(this.expectedChunkNo);
            os.writeInt(this.numBytes);
            os.writeBoolean(this.endsWithLinefeed);
            os.writeUTF(this.fileName);
            if (this.tmpFile != null) {
                os.writeUTF(this.tmpFile.getName());
                this.fc.close();
                logger.info("<writeTo> closed " + this.tmpFile.getName());
            }
            else {
                os.writeUTF("");
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder().append("BlobFile{").append(this.id).append('@')
                    .append(System.identityHashCode(this));
            if (this.autoChunk) {
                sb.append('*');
            }
            sb.append(", '").append(this.fileName).append('\'');
            if (this.tmpFile != null) {
                sb.append(", tmpFile='").append(this.tmpFile.getName()).append('\'')
                    .append(", open=").append(this.fc.isOpen());
            }
            if (this.expectedChunkNo > 0) {
                sb.append(", created=").append(ISODateTimeFormat.dateTime().print(this.created));
                sb.append(", chunkNo=").append(this.expectedChunkNo).append(", #bytes=").append(this.numBytes);
            }
            return sb.append('}').toString();
        }

        int getExpectedChunkNo() {
            return expectedChunkNo & 0xFFFF;
        }

        private void onError() {
            IoUtils.close(this.fc);
            this.fc = null;
            if (!this.tmpFile.delete()) {
                logger.error("<onError> failed to delete " + this.tmpFile.getName());
            }
            this.tmpFile = null;
        }

        private void append(int chunkNo, ByteBuffer bb) {
            if (this.fc == null) {
                return;
            }

            if (!this.autoChunk && getExpectedChunkNo() != chunkNo) {
                logger.warn("<append> received chunk " + chunkNo + " but expected "
                        + getExpectedChunkNo() + " for " + this);
                onError();
                return;
            }

            this.numBytes += bb.remaining();
            if (!this.autoChunk && this.numBytes > MAX_FILE_SIZE) {
                logger.error("<append> max file size exceeded for " + this);
                onError();
                return;
            }

            try {
                if (this.endsWithLinefeed) {
                    this.fc.write(bb);
                }
                else {
                    this.fc.write(new ByteBuffer[]{ ByteBuffer.wrap(LINEFEED), bb});
                    this.numBytes++;
                }
            } catch (IOException e) {
                logger.error("<append> write failed for " + this, e);
                onError();
                return;
            }

            if (this.autoChunk) {
                ackLast(bb.get(bb.limit() - 1));
            }

            this.expectedChunkNo++;
        }

        private void ackLast(byte last) {
            this.endsWithLinefeed = (last == 0x0a || last == 0x0d);
        }

        void close() {
            if (this.fc == null) {
                return;
            }
            IoUtils.close(this.fc);
            File dest = getOutputFile(this);
            if (this.tmpFile.renameTo(dest)) {
                numFilesWritten.incrementAndGet();
                logger.info("<close> " + toString() + " => " + dest.getName());
            }
            else {
                logger.error("<close> failed to rename " + this.tmpFile.getName() + " to " + dest.getName());
            }
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Monitor(type = COUNTER)
    private final AtomicInteger numFilesWritten = new AtomicInteger();

    @Monitor(type = COUNTER)
    private final AtomicInteger numFilesReceived = new AtomicInteger();

    @Monitor(type = COUNTER)
    private final AtomicInteger numFilesIgnored = new AtomicInteger();

    private File outDir;

    private final Map<Integer, BlobFile> currentFiles = new HashMap<>();

    private final Map<String, Integer> fileIds = new HashMap<>();

    private final CopyOnWriteArraySet<String> autoChunkFilenames = new CopyOnWriteArraySet<>();
    
    private volatile String[] filenamePrefixesToIgnore = null;

    @Monitor(type = GAUGE)
    private volatile int numCurrentFiles;

    public int getNumFilesWritten() {
        return this.numFilesWritten.get();
    }

    public int getNumFilesReceived() {
        return this.numFilesReceived.get();
    }

    public int getNumFilesIgnored() {
        return this.numFilesIgnored.get();
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    public void setAutoChunkFilenames(String[] names) {
        this.autoChunkFilenames.clear();
        this.autoChunkFilenames.addAll(Arrays.asList(names));
    }

    @ManagedAttribute
    public String getAutoChunkFilenames() {
        return this.autoChunkFilenames.toString();
    }

    @ManagedOperation
    public boolean addAutoChunkFilename(String name) {
        return this.autoChunkFilenames.add(name);
    }

    @ManagedOperation
    public boolean removeAutoChunkFilename(String name) {
        return this.autoChunkFilenames.remove(name);
    }

    @ManagedOperation
    public void setFilenamePrefixesToIgnore(String names) {
        setFilenamePrefixesToIgnore(StringUtils.tokenizeToStringArray(names, ","));
    }

    public void setFilenamePrefixesToIgnore(String[] names) {
        this.filenamePrefixesToIgnore = (names != null && names.length > 0)
                ? Arrays.copyOf(names, names.length) : null;
    }

    @ManagedAttribute
    public String getFilenamePrefixesToIgnore() {
        return Arrays.toString(this.filenamePrefixesToIgnore);
    }

    @ManagedAttribute
    public int getNumCurrentFiles() {
        return this.numCurrentFiles;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        File file = getStateStore();
        if (!file.canRead()) {
            return;
        }
        try (DataInputStream is = new DataInputStream(new FileInputStream(file))) {
            for (int i = 0, n = is.readInt(); i < n; i++) {
                BlobFile bf = new BlobFile(is);
                addCurrentFile(bf);
                this.logger.info("<afterPropertiesSet> add " + bf);
            }
        }
        updateNumCurrentFiles();
    }

    @Override
    public void destroy() throws Exception {
        flushAutoChunkFiles(Long.MAX_VALUE);
        if (this.currentFiles.isEmpty()) {
            return;
        }
        File file = getStateStore();
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file, false))) {
            dos.writeInt(this.currentFiles.size());
            for (BlobFile blobFile : this.currentFiles.values()) {
                blobFile.writeTo(dos);
            }
        }
        this.logger.info("<destroy> wrote state to " + file.getAbsolutePath());
    }

    private File getStateStore() {
        return new File(this.outDir, "blobFiles.ser");
    }

    void flush() {
        flush(System.currentTimeMillis());
    }

    void flush(long now) {
        flushAutoChunkFiles(now);
        flushCurrentFiles(now);

        updateNumCurrentFiles();
    }

    private void flushCurrentFiles(long now) {
        for (Iterator<BlobFile> it = this.currentFiles.values().iterator(); it.hasNext(); ) {
            BlobFile blobFile = it.next();
            if ((now - blobFile.created) > MILLIS_PER_HOUR
                    && (now - toDateTime(blobFile.lastAppendTimestamp).getMillis()) > MILLIS_PER_HOUR) {
                this.logger.warn("<closeOldFiles> " + blobFile);
                it.remove();
                blobFile.onError();
            }
        }
    }

    private void flushAutoChunkFiles(long now) {
        for (Iterator<Integer> it = this.fileIds.values().iterator(); it.hasNext(); ) {
            Integer id = it.next();
            BlobFile blobFile = this.currentFiles.get(id);
            if ((now - blobFile.created) > MILLIS_PER_MINUTE) {
                this.currentFiles.remove(id);
                it.remove();
                blobFile.close();
            }
        }
    }

    void process(ParsedRecord pr) {
        final String blobFileName = pr.getString(VwdFieldDescription.ADF_Blob_File.id());
        if (blobFileName == null) {
            this.logger.warn("<process> blob message w/o " + VwdFieldDescription.ADF_Blob_File);
            return;
        }

        if (isWithPrefixToIgnore(blobFileName)) {
            this.numFilesIgnored.incrementAndGet();
            return;
        }

        final byte[] blob = pr.getBytes(VwdFieldDescription.ADF_Blob_Content.id());
        if (blob == null) {
            this.logger.warn("<process> blob message w/o " + VwdFieldDescription.ADF_Blob_Content
                    + " for file " + blobFileName);
            return;
        }

        final ByteBuffer bb = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN);

        final int flags = bb.get() & 0xFF;
        final boolean moreToFollow = (flags & 0x01) != 0;
        if (!moreToFollow) {
            this.numFilesReceived.incrementAndGet();
        }

        final int fileId = bb.getShort() & 0xFFFF;
        final int chunkNo = bb.getShort() & 0xFFFF;

        BlobFile bf = getBlobFile(blobFileName, fileId, moreToFollow);

        if (!blobFileName.equals(bf.fileName)) {
            this.logger.warn("<process> received update for fileId " + fileId + ", but existing "
                    + bf + " has different name than '" + blobFileName + "'");
            bf.onError();
            remove(fileId);
            return;
        }

        bf.append(chunkNo, bb);
        bf.lastAppendTimestamp = pr.getMessageTimestamp();

        if (moreToFollow || bf.autoChunk) {
            return;
        }

        remove(fileId);
        bf.close();
    }

    private boolean isWithPrefixToIgnore(String blobFileName) {
        String[] prefixes = this.filenamePrefixesToIgnore;
        if (prefixes == null) {
            return false;
        }
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < prefixes.length; i++) {
            if (blobFileName.startsWith(prefixes[i])) {
                return true;
            }
        }
        return false;
    }

    private File getOutputFile(BlobFile bf) {
        File result = getOutputFile(bf.fileName);
        while (result.exists()) {
            try {
                // we have millis in the filename, so this should not happen more than once
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            result = getOutputFile(bf.fileName);
        }
        return result;
    }

    private void remove(int fileId) {
        this.currentFiles.remove(fileId);
        updateNumCurrentFiles();
    }

    private void updateNumCurrentFiles() {
        this.numCurrentFiles = this.currentFiles.size();
    }

    private void addCurrentFile(BlobFile blobFile) {
        this.currentFiles.put(blobFile.id, blobFile);
    }

    private BlobFile getBlobFile(String blobFileName, int fileId, boolean moreToFollow) {
        if (!moreToFollow) {
            final Integer id = this.fileIds.get(blobFileName);
            if (id != null) {
                return this.currentFiles.get(id);
            }
        }

        BlobFile result = this.currentFiles.get(fileId);
        if (result == null) {
            addCurrentFile(result = createBlobFile(fileId, blobFileName));
            if (result.autoChunk) {
                this.fileIds.put(blobFileName, fileId);
            }
            updateNumCurrentFiles();
            this.logger.info("<getBlobFile> created " + fileId + " => " + result);
        }
        return result;
    }

    private File getOutputFile(final String name) {
        return new File(this.outDir, addTimestamp(name));
    }

    private String addTimestamp(String name) {
        int p = name.lastIndexOf('.');
        if (p > 0) {
            return name.substring(0, p) + "-" + DTF.print(new DateTime()) + name.substring(p);
        }
        return name + "-" + DTF.print(new DateTime());
    }

    private BlobFile createBlobFile(int id, String blobFileName) {
        try {
            final File f = File.createTempFile(".blob-", ".tmp", this.outDir);
            return new BlobFile(id, blobFileName, f);
        } catch (IOException e) {
            this.logger.error("<createBlobFile> failed", e);
            return new BlobFile(id, blobFileName);
        }
    }
}
