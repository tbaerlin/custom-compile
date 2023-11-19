/*
 * RestoreMethod.java
 *
 * Created on 26.10.2010 15:30:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.delay.DelayProvider;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * Method that restores data from a snap file
 * @author oflege
 */
public class FileSnapRestoreMethod implements InitializingBean, Closeable {
    public interface Callback {
        void ackRestore();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private VendorkeyFilter vendorkeyFilter = VendorkeyFilterFactory.ACCEPT_ALL;

    private DelayProvider delayProvider;

    private Callback callback = null;

    private final File snapFile;

    private final IndexAndOffsetFactory iaoFactory;

    private final FeedDataRegistry registry;

    private long iaoStart;

    private final ByteBuffer bb = ByteBuffer.wrap(new byte[256 * 1024]);

    private List<IndexAndOffset> iaos;

    private boolean restoreRealtime = true;

    private boolean restoreDelayed = true;

    private DataFile df;

    public FileSnapRestoreMethod(File snapFile, FeedDataRegistry registry,
            IndexAndOffsetFactory iaoFactory) {
        this.snapFile = snapFile;
        this.registry = registry;
        this.iaoFactory = iaoFactory;
    }

    public void setDelayProvider(DelayProvider delayProvider) {
        this.delayProvider = delayProvider;
    }

    public void setCallback(final Callback callback) {
        this.callback = callback;
    }

    public void setLimiter(final ThroughputLimiter limiter) {
        setCallback(limiter == null ? null : new Callback() {
            @Override
            public void ackRestore() {
                limiter.ackAction();
            }
        });
    }

    public void setVendorkeyFilter(VendorkeyFilter vendorkeyFilter) {
        this.vendorkeyFilter = vendorkeyFilter;
    }

    public void setRestoreRealtime(boolean restoreRealtime) {
        this.restoreRealtime = restoreRealtime;
    }

    public void setRestoreDelayed(boolean restoreDelayed) {
        this.restoreDelayed = restoreDelayed;
    }

    private boolean isRt(byte encodedRtNt) {
        return FileSnapStore.isRt(encodedRtNt);
    }

    private boolean isNt(byte encodedRtNt) {
        return FileSnapStore.isNt(encodedRtNt);
    }

    @Override
    public void close() throws IOException {
        IoUtils.close(this.df);
    }

    public void afterPropertiesSet() throws Exception {
        if (this.snapFile == null || !this.snapFile.canRead()) {
            this.logger.info("<afterPropertiesSet> no snap file to read, returning");
            return;
        }

        this.df = new DataFile(this.snapFile, true);
        checkVersion();

        final long size = this.df.size();
        this.iaoStart = this.df.readLong(size - 8);

        readIndexAndOffsets(df, size);
    }

    private void checkVersion() throws IOException {
        final int fileVersion = this.df.readInt(0);
        if (fileVersion > FileSnapStore.VERSION) {
            throw new IOException("<restore> failed, incompatible versions: " + fileVersion + " <-> " + FileSnapStore.VERSION);
        }

        if (fileVersion < FileSnapStore.VERSION) {
            throw new IOException("<restore> failed, incompatible versions: " + fileVersion + " <-> " + FileSnapStore.VERSION);
        }
    }

    /**
     * Restores all data from the given snap file and closes the file.
     * @throws Exception on failure
     */
    public void restore() throws Exception {
        restore(true);
    }

    public void restore(boolean doDestroy) throws Exception {
        final TimeTaker tt = new TimeTaker();

        try {
            afterPropertiesSet();
            doRestore();
            this.logger.info("<restore> restored data from " + this.snapFile.getAbsolutePath()
                    + ", took " + tt);
        } finally {
            if (doDestroy) {
                close();
            }
        }
    }

    private void doRestore() throws IOException {
        if (this.df == null) {
            return;
        }
        long readPos = 4;
        long dataPos = 4;
        this.df.seek(readPos);
        this.bb.clear();

        while (readPos < this.iaoStart) {
            if (this.iaoStart - readPos < bb.remaining()) {
                this.bb.limit(this.bb.position() + (int) (this.iaoStart - readPos));
            }

            final int numBytesRead = this.df.read(this.bb);
            readPos += numBytesRead;

            this.bb.flip();
            final int end = this.bb.remaining();

            while (this.bb.remaining() >= 4) {
                this.bb.mark();
                final int len = this.bb.getInt();
                if (this.bb.remaining() < len) {
                    this.bb.reset();
                    break;
                }

                final int dataEnd = this.bb.position() + len;
                this.bb.limit(dataEnd);

                try {
                    doRestoreData(dataPos);
                } catch (Exception e) {
                    this.logger.warn("<doRestoreData> failed for record at " + dataPos, e);
                }

                this.bb.limit(end);
                this.bb.position(dataEnd);
                dataPos = dataPos + 4 + len;

            }
            this.bb.compact();
        }
    }

    public synchronized FeedData restoreData(long offset) throws IOException {
        this.df.seek(offset);
        readLengthEncoded();
        return doRestoreData();
    }

    private void readIndexAndOffsets(DataFile df, long size) throws IOException {
        df.seek(this.iaoStart);
        final int numIaos = df.readInt();

        this.iaos = new ArrayList<>(Collections.nCopies(numIaos, (IndexAndOffset) null));

        // first, read iaos
        while (df.position() < (size - 8)) {
            readLengthEncoded();

            Integer id = null;
            try {
                id = bb.getInt();
                final int[] fields = new int[bb.getInt()];
                bb.asIntBuffer().get(fields);
                final IndexAndOffset iao = this.iaoFactory.getIndexAndOffset(fields);
                iaos.set(id, iao);
            } catch (Exception e) {
                this.logger.warn("<readIndexAndOffsets> failed to read iao " + id, e);
            }
        }

        this.logger.info("<readIndexAndOffsets> read " + iaos.size() + " IndexAndOffset objects");
    }

    protected void doRestoreData(long dataPos) throws IOException {
        final FeedData data = doRestoreData();
        if (this.callback != null) {
            this.callback.ackRestore();
        }
    }

    private FeedData doRestoreData() throws IOException {
        final byte encodedRtNt = readRtNtFlags();
        final ByteString vkeyStr = readKey();
        final VendorkeyVwd vkey = VendorkeyVwd.getInstance(vkeyStr);

        if (vkey == VendorkeyVwd.ERROR) {
            this.logger.warn("<restore> Illegal vendorkey: " + vkeyStr);
            return null;
        }

        final FeedData feedData = getFeedData(vkey);
        if (feedData == null) {
            return null; // not acceptable...
        }

        final boolean withRt = isRt(encodedRtNt);
        final boolean withNt = isNt(encodedRtNt);

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (feedData) {
            final boolean isMD = feedData.getMarket().isMarketDepth();

            if (withRt) {
                if (isMD) {
                    final int length = bb.getInt();
                    final byte[] data = new byte[length];
                    bb.get(data);
                    if (this.restoreRealtime) {
                        feedData.getSnapData(this.restoreRealtime).init(null, data);
                    }
                }
                else {
                    final IndexAndOffset iao = getIndexAndOffset();
                    final byte[] data = readSnapBytes(this.bb);
                    if (this.restoreRealtime) {
                        feedData.getSnapData(true).init(iao, data);
                    }
                }
            }

            if (withNt) {
                final IndexAndOffset iao = getIndexAndOffset();
                final byte[] data = readSnapBytes(this.bb);
                if (doRestoreDelayed(feedData, this.restoreDelayed)) {
                    feedData.getSnapData(false).init(iao, data);
                }
            }
            else if (doRestoreDelayed(feedData, this.restoreDelayed)
                    && withRt && this.restoreRealtime && !isMD) {
                // restore delayed data from realtime
                final SnapData rtSnap = feedData.getSnapData(true);
                final byte[] data = rtSnap.getData(true);
                feedData.getSnapData(false).init(rtSnap.getIndexAndOffset(), data);
            }
        }

        return feedData;
    }

    protected IndexAndOffset getIndexAndOffset() {
        return this.iaos.get(this.bb.getInt());
    }

    protected byte readRtNtFlags() {
        return this.bb.get();
    }

    protected ByteString readKey() {
        return ByteString.readFrom(this.bb, ByteString.LENGTH_ENCODING_BYTE);
    }

    private void readLengthEncoded() throws IOException {
        final int len = df.readInt();
        bb.clear().limit(len);
        df.read(bb);
        bb.flip();
    }

    private FeedData getFeedData(VendorkeyVwd vkey) {
        if (this.vendorkeyFilter.test(vkey)) {
            return this.registry.register(vkey);
        }
        return null;
    }

    private boolean doRestoreDelayed(FeedData data, boolean restoreDelayed) {
        return restoreDelayed
                && (this.delayProvider == null || this.delayProvider.getDelayInSeconds(data) > 0);
    }

    private byte[] readSnapBytes(final ByteBuffer bb) throws IOException {
        final byte[] bytes = new byte[bb.getShort() & 0xFFFF];
        bb.get(bytes);
        return bytes;
    }

}
