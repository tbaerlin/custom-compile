/*
 * FileSnapStoreMethod.java
 *
 * Created on 26.10.2010 16:50:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;

/**
 * @author oflege
 */
public class FileSnapStoreMethod {
    private static class IaoContext {

        private final Map<IndexAndOffset, Integer> iaoIds
                = new IdentityHashMap<>();
        private int nextId = 0;

        int getId(IndexAndOffset iao) {
            final Integer anInt = this.iaoIds.get(iao);
            if (anInt != null) {
                return anInt;
            }
            final int id = this.nextId++;
            this.iaoIds.put(iao, id);
            return id;
        }

    }
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IaoContext context = new IaoContext();

    private final ByteBuffer bb = ByteBuffer.allocate(256 * 1024);

    private final FeedDataRegistry registry;

    public FileSnapStoreMethod(FeedDataRegistry registry) {
        this.registry = registry;
    }

    public void store(File file, boolean storeRealtime, boolean storeDelayed) throws IOException {
        this.logger.info("<store> writing snaps to " + file.getAbsolutePath()
                + ", rt=" + storeRealtime + ", nt=" + storeDelayed);

        backupExistingFile(file);

        final DataFile df = new DataFile(file, false);

        df.writeInt(FileSnapStore.VERSION);

        storeData(storeRealtime, storeDelayed, df);

        writeContext(df);

        IoUtils.close(df);
    }

    private void storeData(boolean storeRealtime, boolean storeDelayed,
            DataFile df) throws IOException {
        int numRt = 0;
        int numNt = 0;
        for (final FeedData fd : this.registry.getElements()) {
            int offset;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (fd) {
                final SnapData snapRt = storeRealtime ? fd.getSnapData(true) : null;
                final SnapData snapNt = storeDelayed ? fd.getSnapData(false) : null;

                boolean writeRt = (snapRt != null) && snapRt.isInitialized();
                boolean writeNt = (snapNt != null) && snapNt.isInitialized();

                if (!writeRt && !writeNt) {
                    continue;
                }

                offset = bb.position();
                bb.position(bb.position() + 4); // placeholder for length
                bb.put(FileSnapStore.getEncodedRtNt(writeRt, writeNt));

                fd.getVendorkey().toByteString().writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);

                if (writeRt) {
                    if (fd.getMarket().isMarketDepth()) {
                        writeMarketDepthSnap(snapRt, bb);
                    }
                    else {
                        writeSnap(snapRt);
                    }
                    numRt++;
                }

                if (writeNt) {
                    writeSnap(snapNt);
                    numNt++;
                }
            }

            bb.putInt(offset, bb.position() - offset - 4); // do not count length
            if (bb.remaining() < 16348) {
                flipWriteClear(df, bb);
            }
        }
        flipWriteClear(df, bb);
        this.logger.info("<storeData> stored " + numRt + " RT and " + numNt + " NT snaps");
    }

    private void writeContext(DataFile df) throws IOException {
        final long iaoStart = df.position();
        df.writeInt(context.iaoIds.size());

        for (Map.Entry<IndexAndOffset, Integer> entry : context.iaoIds.entrySet()) {
            bb.putInt(0); // placeholder for length
            bb.putInt(entry.getValue());

            final IndexAndOffset iao = entry.getKey();
            final int[] fields = iao.getIndexArray();
            bb.putInt(fields.length);
            for (int n : fields) {
                bb.putInt(n);
            }
            bb.putInt(0, bb.position() - 4); // do not count length
            flipWriteClear(df, bb);
        }
        df.writeLong(iaoStart);
    }

    private void backupExistingFile(File file) {
        if (!file.exists()) {
            return;
        }
        final File oldFile = new File(file.getParent(), file.getName() + ".old");
        if (oldFile.exists() && !oldFile.delete()) {
            this.logger.warn("<backupExistingFile> failed to delete " + oldFile.getAbsolutePath());
        }
        if (!file.renameTo(oldFile)) {
            this.logger.warn("<backupExistingFile> failed to mv " + file.getAbsolutePath()
                + " to " + oldFile.getAbsolutePath());
        }
    }

    private void flipWriteClear(DataFile df, ByteBuffer bb) throws IOException {
        bb.flip();
        if (bb.hasRemaining()) {
            df.write(bb);
        }
        bb.clear();
    }

    private void writeMarketDepthSnap(final SnapData snapRt, final ByteBuffer bb) {
        final byte[] data = snapRt.getData(false);
        bb.putInt(data.length);
        bb.put(data);
    }

    private void writeSnap(final SnapData snap) {
        final IndexAndOffset snapIao = snap.getIndexAndOffset();
        bb.putInt(context.getId(snapIao));
        final byte[] data = snap.getData(false);
        bb.putShort((short) data.length);
        bb.put(data);
    }

}
