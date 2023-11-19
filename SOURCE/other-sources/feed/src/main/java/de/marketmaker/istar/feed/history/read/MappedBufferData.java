/*
 * EodDataBuffer.java
 *
 * Created on 20.11.13 09:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.read;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.Item;

/**
 * @author zzhao
 */
public class MappedBufferData<T extends Comparable<T>> {

    private static final Logger log = LoggerFactory.getLogger(MappedBufferData.class);

    private final List<T> keys = new ArrayList<>();

    private final List<Long> offsets = new ArrayList<>();

    private final List<ByteBuffer> mappedBuffers = new ArrayList<>();

    private final List<Long> mappedBufferOffsets = new ArrayList<>();

    private final HistoryUnit unit;

    private final Class<T> clazz;

    private OffsetLengthCoder olCoder;

    public MappedBufferData(HistoryUnit unit, Class<T> clazz) {
        this.unit = unit;
        this.clazz = clazz;
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
                int overlapSize = this.olCoder.maxLength();
                final int levelOneIndexSize = (int) (indexStartPos - indexStart);
                final ByteBuffer bb = ByteBuffer.allocate(levelOneIndexSize);
                dataFile.seek(indexStart);
                dataFile.read(bb);
                bb.flip();
                do {
                    this.offsets.add(bb.getLong());
                    if (bb.hasRemaining()) {
                        this.keys.add(Item.createKey(this.clazz, bb));
                    }
                } while (bb.hasRemaining());
                this.offsets.add(indexStart);

                final FileChannel channel = dataFile.getChannel();
                long offset = 0;
                channel.position(offset);
                long remainingSize;
                do {
                    remainingSize = indexStart - offset;
                    final long len = Math.min((long) Integer.MAX_VALUE, remainingSize);
                    this.mappedBufferOffsets.add(offset);
                    this.mappedBuffers.add(channel.map(FileChannel.MapMode.READ_ONLY, offset, len));
                    offset += (len - overlapSize);
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

    ByteBuffer loadData(T key) throws IOException {
        if (null != this.olCoder) {
            final ByteBuffer chunkBuf = getPageBuffer(key);
            while (chunkBuf.hasRemaining()) {
                final T myKey = Item.createKey(this.clazz, chunkBuf);
                final long entry = chunkBuf.getLong();
                if (myKey.compareTo(key) == 0) {
                    final long offset = this.olCoder.decodeOffset(entry);
                    final int length = this.olCoder.decodeLength(entry);
                    return getBuffer(offset, length);
                }
            }
        }

        return HistoryUtil.EMPTY_BB;
    }

    private ByteBuffer getPageBuffer(T key) {
        if (this.offsets.isEmpty()) {
            return HistoryUtil.EMPTY_BB;
        }
        int idx = Collections.binarySearch(this.keys, key);
        if (idx < 0) {
            idx = -idx - 1;
        }
        else {
            idx = idx + 1;
        }
        final long offset = this.offsets.get(idx);
        return getBuffer(offset, (int) (this.offsets.get(idx + 1) - offset));
    }

    private ByteBuffer getBuffer(long offset, int length) {
        int idx = Collections.binarySearch(this.mappedBufferOffsets, offset);
        if (idx < 0) {
            idx = -idx - 1;
            if (idx == 0) { // offset not found
                return HistoryUtil.EMPTY_BB;
            }
            else {
                idx--; // the buffer before insert position contains the data
            }
        }

        final long bbOffset = this.mappedBufferOffsets.get(idx);
        final ByteBuffer bb = this.mappedBuffers.get(idx).asReadOnlyBuffer();
        final int newPosition = (int) (offset - bbOffset);
        bb.position(newPosition);
        bb.limit(newPosition + length);
        return bb.slice();
    }
}
