/*
 * EodHistoryReaderBase.java
 *
 * Created on 16.01.13 14:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.read;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.history.HistoryGatherer;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;

/**
 * Synchronization hint:
 * <ul>
 * <li>{@link #setFile(java.io.File)} and data loading methods for a quote invoked outside
 * this class are serialized by a monitor in Gatherer. The reason
 * for not having this class responsible for synchronization is that the entire status
 * across Rest, Months and Patch has to be synchronized.</li>
 * </ul>
 *
 * @author zzhao
 */
public abstract class HistoryGathererBaseMB<T extends Comparable<T>>
        implements HistoryGatherer, Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HistoryUnit unit;

    private final MappedBufferData<T> data;

    private File file;

    private Interval interval;

    public HistoryGathererBaseMB(HistoryUnit unit, Class<T> clazz) {
        this.unit = unit;
        this.data = new MappedBufferData<>(unit, clazz);
    }

    public HistoryUnit getUnit() {
        return this.unit;
    }

    public Interval getInterval() {
        return this.interval;
    }

    public boolean contains(Interval another) {
        return null != this.interval && this.interval.contains(another);
    }

    @Override
    public void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException {
        if (units.contains(this.unit)) {
            setFile(this.unit.getLatestFile(dir));
        }
        else {
            this.data.clear();
        }
    }

    public void setFile(File historyFile) throws IOException {
        if (null == historyFile) {
            this.logger.info("<setFile> {} null history file, unload if already loaded", this.unit);
            this.data.clear();
            this.interval = null;
        }
        else if (historyFile.equals(this.file)) {
            this.logger.info("<setFile> {} no updates", this.unit);
        }
        else {
            final boolean updated = this.data.setFile(historyFile);
            if (updated) {
                this.file = historyFile;
                this.interval = this.unit.getInterval(this.file);
            }
        }
    }

    public ByteBuffer loadData(T key, Interval interval) throws IOException {
        if (!interval.overlaps(this.interval)) {
            return HistoryUtil.EMPTY_BB;
        }
        else {
            return loadData(key);
        }
    }

    public ByteBuffer loadData(T key) throws IOException {
        return this.data.loadData(key);
    }

    @Override
    public void close() throws IOException {
        this.data.clear();
    }
}
