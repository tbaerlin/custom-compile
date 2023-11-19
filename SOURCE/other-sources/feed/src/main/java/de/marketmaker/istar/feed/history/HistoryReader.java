/*
 * HistoryReader.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author zzhao
 */
public class HistoryReader<T extends Comparable<T>> implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HistoryUnit unit;

    private final Class<T> clazz;

    private final boolean inMem;

    private final List<T> keys = new ArrayList<>();

    private final List<Long> offsets = new ArrayList<>();

    private final Map<T, byte[]> data;

    private final ByteBuffer bb = ByteBuffer.allocate(OneLevelBsTree.INDEX_CHUNK_SIZE);

    private File file;

    private DataFile dataFile;

    private Interval interval;

    private int curIndex = -1;

    private OffsetLengthCoder olCoder;

    public HistoryReader(Class<T> clazz, HistoryUnit unit) {
        this(clazz, unit, false);
    }

    public HistoryReader(Class<T> clazz, HistoryUnit unit, boolean inMem) {
        this.clazz = clazz;
        this.unit = unit;
        this.inMem = inMem;
        if (this.inMem) {
            this.data = new HashMap<>();
        }
        else {
            this.data = Collections.emptyMap();
        }
    }

    public OffsetLengthCoder getOffsetLengthCoder() {
        return olCoder;
    }

    public Interval getInterval() {
        return this.interval;
    }

    public HistoryUnit getUnit() {
        return unit;
    }

    private static int fromHistoryFile(DataFile df) throws IOException {
        df.seek(df.size() - 1);
        return df.readByte();
    }

    public static int fromHistoryFile(File file) throws IOException {
        try (final DataFile df = new DataFile(file, true)) {
            return fromHistoryFile(df);
        }
    }

    public void setFile(File file) throws IOException {
        final TimeTaker tt = new TimeTaker();
        if (null == file) {
            close();
        }
        else if (!file.equals(this.file)) {
            close();
            this.logger.info("<setFile> {} {}", this.unit, file.getAbsolutePath());
            this.file = file;
            this.dataFile = new DataFile(this.file, true);
            this.interval = this.unit.getInterval(this.file);
            if (this.dataFile.size() <= 0) {
                this.logger.warn("<setFile> empty history file: " + this.file.getAbsolutePath());
                return;
            }
            if (this.inMem) {
                final ItemExtractor<T> ext = new ItemExtractor<>(this.clazz, this.dataFile);
                this.olCoder = ext.getOffsetLengthCoder();
                for (Item<T> item : ext) {
                    final ByteBuffer bb = ByteBuffer.allocate(item.getLength());
                    this.dataFile.seek(item.getOffset());
                    this.dataFile.read(bb);
                    bb.flip();
                    this.data.put(item.getKey(), bb.array());
                }
            }
            else {
                final long indexStartPos = this.dataFile.size() - 8 - 1;
                final long indexStart = this.dataFile.readLong(indexStartPos);
                this.olCoder = new OffsetLengthCoder(this.dataFile.readByte());

                final int indexSize = (int) (indexStartPos - indexStart);
                final ByteBuffer bb = ByteBuffer.allocate(indexSize);

                this.offsets.clear();
                this.keys.clear();
                this.dataFile.seek(indexStart);
                this.dataFile.read(bb);
                bb.flip();

                do {
                    this.offsets.add(bb.getLong());
                    if (bb.hasRemaining()) {
                        this.keys.add(Item.createKey(this.clazz, bb));
                    }
                } while (bb.hasRemaining());

                this.offsets.add(indexStart);
                this.curIndex = -1;
            }
            this.logger.info("<setFile> {} bits used to encode length", this.olCoder.getLengthBits());
        }
        this.logger.info("<setFile> took: {}", tt);
    }

    public void loadData(T symbol, ByteBuffer buffer) throws IOException {
        loadData(symbol, this.interval, buffer);
    }

    public void loadData(T symbol, Interval interval, ByteBuffer buffer) throws IOException {
        if (null == this.dataFile || !interval.overlaps(this.interval)) {
            return;
        }

        if (this.inMem) {
            if (this.data.containsKey(symbol)) {
                buffer.put(this.data.get(symbol));
            }
            return;
        }

        int idx = Collections.binarySearch(this.keys, symbol);
        if (idx < 0) {
            idx = -idx - 1;
        }
        else {
            idx = idx + 1;
        }

        if (idx != this.curIndex) {
            this.dataFile.seek(this.offsets.get(idx));
            this.bb.clear();
            this.dataFile.read(this.bb, (int) (this.offsets.get(idx + 1) - this.offsets.get(idx)));
            this.bb.flip();
            this.curIndex = idx;
        }

        final int limit = this.bb.limit();
        while (this.bb.hasRemaining()) {
            final T key = Item.createKey(this.clazz, this.bb);
            final long entry = this.bb.getLong();
            if (symbol.equals(key)) {
                loadData(entry, buffer);
                break;
            }
        }
        this.bb.position(0);
        this.bb.limit(limit);
    }

    private void loadData(long entry, ByteBuffer buffer) throws IOException {
        this.dataFile.seek(this.olCoder.decodeOffset(entry));
        this.dataFile.read(buffer, this.olCoder.decodeLength(entry));
    }

    @Override
    public void close() throws IOException {
        if (null != this.file) {
            this.logger.info("<close> {} {}", this.unit, this.file.getAbsolutePath());
            this.dataFile.close();
            this.dataFile = null;
            this.file = null;
            this.interval = null;
            this.data.clear();
        }
    }

    public void emitKeys(PrintStream ps, boolean all) {
        List<T> keys = this.inMem ? new ArrayList<>(this.data.keySet()) : this.keys;
        if (keys.isEmpty()) {
            ps.println("no keys found");
            return;
        }

        int size = keys.size();
        ps.println(size + " keys");
        if (all) {
            for (int i = 0; i < size; i++) {
                ps.println(i + ": " + keys.get(i));
            }
        }
        else {
            ps.println("0: " + keys.get(0));
            if (size >= 3) {
                int idx = size / 2;
                ps.println(idx + ": " + keys.get(idx));
            }
            if (size - 1 != 0) {
                ps.println((size - 1) + ": " + keys.get(size - 1));
            }
        }
    }
}