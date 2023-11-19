/*
 * ItemBuffer.java
 *
 * Created on 12.12.12 17:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.ThrowingBiConsumer;
import de.marketmaker.istar.feed.history.BufferedBytesTransporter;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.Item;
import de.marketmaker.istar.feed.history.ItemsSegment;
import de.marketmaker.istar.feed.history.OneLevelBsTree;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
class EodHistoryLoader implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(EodHistoryLoader.class);

    private final ByteBuffer chunkBuf = ByteBuffer.allocate(OneLevelBsTree.INDEX_CHUNK_SIZE);

    private final LongArrayList keys = new LongArrayList();

    private final LongArrayList offsets = new LongArrayList();

    private final HistoryUnit unit;

    private int chunkIndex = -1;

    private final Object monitor = new Object();

    private volatile boolean closed = false;

    private final DataFile dataFile;

    private final OffsetLengthCoder olCoder;

    EodHistoryLoader(HistoryUnit unit, File file) throws IOException {
        this.unit = unit;
        this.dataFile = new DataFile(file, true);
        if (this.dataFile.size() > 0) {
            final long indexStartPos = this.dataFile.size() - 8 - 1;
            final long indexStart = this.dataFile.readLong(indexStartPos);
            this.olCoder = new OffsetLengthCoder(this.dataFile.readByte());
            final int levelOneIndexSize = (int) (indexStartPos - indexStart);
            final ByteBuffer bb = ByteBuffer.allocate(levelOneIndexSize);
            this.dataFile.seek(indexStart);
            this.dataFile.read(bb);
            bb.flip();
            do {
                this.offsets.add(bb.getLong());
                if (bb.hasRemaining()) {
                    this.keys.add(bb.getLong());
                }
            } while (bb.hasRemaining());
            this.offsets.add(indexStart);
        }
        else {
            this.olCoder = null;
        }
    }

    boolean isValid() {
        return !this.closed;
    }

    @Override
    public void close() throws IOException {
        this.closed = true;
        synchronized (this.monitor) {
            if (log.isDebugEnabled()) {
                log.debug("<close> {}", this.dataFile);
            }
            IoUtils.close(this.dataFile);
        }
    }

    void forEntries(ThrowingBiConsumer<Item<Long>, BufferedBytesTransporter, IOException> biCon)
        throws IOException {
        final ItemsSegment<Long> itemsSegment;
        synchronized (this.monitor) {
            itemsSegment = this.closed ? new ItemsSegment<>() : getItemsSegment();
        }

        final BufferedBytesTransporter tran;
        synchronized (this.monitor) {
            tran = this.closed ? null
                : new BufferedBytesTransporter(this.dataFile, this.olCoder.maxLength());
        }

        while (itemsSegment.hasNext()) {
            final Item<Long> item = itemsSegment.next();
            synchronized (this.monitor) {
                biCon.accept(item, tran);
            }
        }
    }

    public OffsetLengthCoder getOffsetLengthCoder() {
        return olCoder;
    }

    private ItemsSegment<Long> getItemsSegment() throws IOException {
        if (this.dataFile.size() > 0) {
            final long indexStartPos = this.dataFile.size() - 8 - 1;
            this.dataFile.seek(indexStartPos);
            final long indexStart = this.dataFile.readLong();
            this.dataFile.seek(indexStart);
            final long offset0 = this.dataFile.readLong();
            final long indexLen = indexStart - offset0;
            return new ItemsSegment<>(this.dataFile, offset0, indexLen, this.olCoder, Long.class);
        } else {
            return new ItemsSegment<>();
        }
    }

    ByteBuffer loadData(long quote) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("<loadData> {} from hard drive {}", this.unit, quote);
        }
        if (this.offsets.isEmpty()) {
            return EodUtil.EMPTY_BB;
        }
        int idx = Collections.binarySearch(this.keys, quote);
        if (idx < 0) {
            idx = -idx - 1;
        }
        else {
            idx = idx + 1;
        }

        if (idx != this.chunkIndex) {
            this.chunkBuf.clear();
            final long offset = this.offsets.getLong(idx);
            final int length = (int) (this.offsets.getLong(idx + 1) - offset);
            synchronized (this.monitor) {
                read(this.chunkBuf, offset, length);
            }
            this.chunkBuf.flip();
            this.chunkIndex = idx;
        }

        final int limit = this.chunkBuf.limit();
        try {
            while (this.chunkBuf.hasRemaining()) {
                final long key = this.chunkBuf.getLong();
                final long entry = this.chunkBuf.getLong();
                if (key == quote) {
                    final long offset = this.olCoder.decodeOffset(entry);
                    final int length = this.olCoder.decodeLength(entry);
                    final ByteBuffer buf = ByteBuffer.allocate(length);
                    synchronized (this.monitor) {
                        read(buf, offset, length);
                    }
                    buf.flip();
                    return buf;
                }
            }
        } finally {
            this.chunkBuf.position(0);
            this.chunkBuf.limit(limit);
        }

        return EodUtil.EMPTY_BB;
    }

    private void read(ByteBuffer bb, Long offset, int length) throws IOException {
        this.dataFile.seek(offset);
        this.dataFile.read(bb, length);
    }
}
