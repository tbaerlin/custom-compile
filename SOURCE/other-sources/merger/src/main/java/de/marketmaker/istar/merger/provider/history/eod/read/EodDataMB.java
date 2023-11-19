/*
 * EodDataBuffer.java
 *
 * Created on 20.11.13 09:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.read.MappedBufferUtil;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
class EodDataMB {

    private static final Logger log = LoggerFactory.getLogger(EodDataMB.class);

    private static final int OVERLAP_SIZE = 32 * 1024 * 1024;// 32M

    private final LongArrayList keys = new LongArrayList();

    private final LongArrayList offsets = new LongArrayList();

    private final List<ByteBuffer> mappedBuffers = new ArrayList<>();

    private final LongArrayList mappedBufferOffsets = new LongArrayList();

    private final int overlapSize;

    private final HistoryUnit unit;

    private OffsetLengthCoder olCoder;

    public EodDataMB(HistoryUnit unit) {
        this(unit, OVERLAP_SIZE);
    }

    public EodDataMB(HistoryUnit unit, int overlapSize) {
        this.unit = unit;
        this.overlapSize = overlapSize;
    }

    void clear() {
        this.keys.clear();
        this.offsets.clear();
        this.mappedBufferOffsets.clear();
        for (ByteBuffer mappedBuffer : mappedBuffers) {
            try {
                MappedBufferUtil.unmap(mappedBuffer);
            } catch (IOException e) {
                log.error("<clear> cannot un-map mapped buffer", e);
            }
        }
        this.mappedBuffers.clear();
    }

    boolean setFile(File file) throws IOException {
        try (final DataFile dataFile = new DataFile(file, true)) {
            if (dataFile.size() > 0) {
                log.info("<setFile> {}, {}", this.unit, file.getAbsolutePath());
                clear();
                final long indexStartPos = dataFile.size() - 8 - 1;
                final long indexStart = dataFile.readLong(indexStartPos);
                this.olCoder = new OffsetLengthCoder(dataFile.readByte());
                final int levelOneIndexSize = (int) (indexStartPos - indexStart);
                final ByteBuffer bb = ByteBuffer.allocate(levelOneIndexSize);
                dataFile.seek(indexStart);
                dataFile.read(bb);
                bb.flip();
                do {
                    this.offsets.add(bb.getLong());
                    if (bb.hasRemaining()) {
                        this.keys.add(bb.getLong());
                    }
                } while (bb.hasRemaining());
                this.offsets.add(indexStart);

                final FileChannel channel = dataFile.getChannel();
                long offset = 0;
                channel.position(offset);
                long remainingSize;
                do {
                    remainingSize = indexStart - offset;
                    final long len = Math.min(Integer.MAX_VALUE, remainingSize);
                    this.mappedBufferOffsets.add(offset);
                    this.mappedBuffers.add(channel.map(FileChannel.MapMode.READ_ONLY, offset, len));
                    offset += (len - this.overlapSize);
                } while (remainingSize > Integer.MAX_VALUE);
                return true;
            }
            else {
                this.olCoder = null;
                log.warn("<setFile> empty file {} for {}", file.getAbsolutePath(), this.unit);
                return false;
            }
        }
    }

    ByteBuffer loadData(long quote) throws IOException {
        if (null != this.olCoder) {
            final ByteBuffer chunkBuf = getPageBuffer(quote);
            while (chunkBuf.hasRemaining()) {
                final long key = chunkBuf.getLong();
                final long entry = chunkBuf.getLong();
                if (key == quote) {
                    final long offset = this.olCoder.decodeOffset(entry);
                    final int length = this.olCoder.decodeLength(entry);
                    return getBuffer(offset, length);
                }
            }
        }

        return EodUtil.EMPTY_BB;
    }

    ByteBuffer loadData(long quote, int fieldId) throws IOException {
        final ByteBuffer bb = loadData(quote);
        if (bb.hasRemaining()) {
            final int fieldSize = bb.get();
            for (int i = 0; i < fieldSize; i++) {
                final int lengthField = bb.getInt();
                final byte b = EodUtil.decodeField(lengthField);
                final int len = EodUtil.decodeFieldLength(lengthField);
                final int field = HistoryUtil.fromUnsignedByte(b);
                if (field == fieldId) {
                    bb.limit(bb.position() + len);
                    return bb.slice();
                }
                bb.position(bb.position() + len);
            }
        }
        return EodUtil.EMPTY_BB;
    }

    private ByteBuffer getPageBuffer(long quote) {
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
        final long offset = this.offsets.getLong(idx);
        return getBuffer(offset, (int) (this.offsets.getLong(idx + 1) - offset));
    }

    private ByteBuffer getBuffer(long offset, int length) {
        int idx = Collections.binarySearch(this.mappedBufferOffsets, offset);
        if (idx < 0) {
            idx = -idx - 1;
            if (idx == 0) { // offset not found
                return EodUtil.EMPTY_BB;
            }
            else {
                idx--; // the buffer before insert position contains the data
            }
        }

        final long bbOffset = this.mappedBufferOffsets.getLong(idx);
        final ByteBuffer bb = this.mappedBuffers.get(idx).asReadOnlyBuffer();
        final int newPosition = (int) (offset - bbOffset);
        bb.position(newPosition);
        bb.limit(newPosition + length);
        return bb.slice();
    }
}
