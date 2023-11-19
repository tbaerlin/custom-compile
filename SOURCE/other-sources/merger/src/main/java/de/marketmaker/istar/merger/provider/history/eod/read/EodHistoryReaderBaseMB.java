/*
 * EodHistoryReaderBase.java
 *
 * Created on 16.01.13 14:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * Synchronization hint:
 * <ul>
 * <li>{@link #setFile(java.io.File)} and data loading methods for a quote invoked outside
 * this class are serialized by a monitor in {@link de.marketmaker.istar.merger.provider.history.eod.read.EodPriceHistoryGatherer}. The reason
 * for not having this class responsible for synchronization is that the entire status
 * across Rest, Months and Patch has to be synchronized.</li>
 * </ul>
 *
 * @author zzhao
 */
abstract class EodHistoryReaderBaseMB implements EodHistoryReader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HistoryUnit unit;

    private final EodDataMB eodData;

    private File file;

    private Interval interval;

    public EodHistoryReaderBaseMB(HistoryUnit unit) {
        this.unit = unit;
        this.eodData = new EodDataMB(unit);
    }

    @Override
    public HistoryUnit getUnit() {
        return this.unit;
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public boolean contains(Interval another) {
        return null != this.interval && this.interval.contains(another);
    }

    @Override
    public void setFile(File historyFile) throws IOException {
        final TimeTaker tt = new TimeTaker();
        if (null == historyFile) {
            this.logger.info("<setFile> {} null history file, unload if already loaded", this.unit);
            this.eodData.clear();
            this.interval = null;
        }
        else if (historyFile.equals(this.file)) {
            this.logger.info("<setFile> {} no updates", this.unit);
        }
        else {
            final boolean updated = this.eodData.setFile(historyFile);
            if (updated) {
                this.file = historyFile;
                this.interval = this.unit.getInterval(this.file);
            }
        }

        this.logger.info("<setFile> {} took: {}", this.unit, tt);
    }

    @Override
    public ByteBuffer loadData(long quote, Interval interval) throws IOException {
        if (!interval.overlaps(this.interval)) {
            return EodUtil.EMPTY_BB;
        }
        else {
            return loadData(quote);
        }
    }

    @Override
    public ByteBuffer loadData(long quote) throws IOException {
        return this.eodData.loadData(quote);
    }

    @Override
    public void close() throws IOException {
        this.eodData.clear();
    }
}
