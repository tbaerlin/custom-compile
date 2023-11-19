/*
 * EodHistoryReaderBase.java
 *
 * Created on 16.01.13 14:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronization hint:
 * <ul>
 * <li>{@link #setFile(java.io.File)} and data loading methods for a quote invoked outside
 * this class are serialized by a monitor in {@link EodPriceHistoryGatherer}. The reason
 * for not having this class responsible for synchronization is that the entire status
 * across Rest, Months and Patch has to be synchronized.</li>
 * <li>the loading of history data into memory(if switched on by means of capacity and executor
 * service) and unloading are serialized using one thread.</li>
 * <li>for each file set by {@link #setFile(java.io.File)} a flag is created to indicate if loading
 * data from that file into {@link #eodData} is completed. The in memory history data is used upon
 * querying, ONLY when the current flag is set to true, and this happens ONLY when the loading of
 * that file is finished successfully.</li>
 * </ul>
 *
 * @author zzhao
 */
abstract class EodHistoryReaderBase implements EodHistoryReader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EodData eodData;

    private final Executor executorService;

    private final HistoryUnit unit;

    private File file;

    private Interval interval;

    private EodHistoryLoader loader;

    private AtomicBoolean loaded = null;

    public EodHistoryReaderBase(HistoryUnit unit, int capacity, Executor executorService) {
        this.unit = unit;
        this.eodData = capacity > 0 && null != executorService ? new EodData(capacity) : null;
        this.executorService = executorService;
    }

    public EodHistoryReaderBase(HistoryUnit unit) {
        this(unit, 0, null);
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
            close();
        }
        else if (historyFile.equals(this.file)) {
            this.logger.info("<setFile> {} no updates", this.unit);
        }
        else {
            this.logger.info("<setFile> {} {}", this.unit, historyFile.getAbsolutePath());
            close();
            this.file = historyFile;
            if (this.file.length() <= 0) {
                this.logger.warn("<setFile> {}, empty history file: {}", this.unit,
                        this.file.getAbsolutePath());
                return;
            }
            this.interval = this.unit.getInterval(this.file);
            this.loader = new EodHistoryLoader(this.unit, this.file);
            this.loaded = new AtomicBoolean(false);
            if (null != this.eodData && null != this.executorService) {
                loadCollection(tt);
            }
        }

        this.logger.info("<setFile> {} took: {}", this.unit, tt);
    }

    private void loadCollection(final TimeTaker tt) {
        final EodHistoryLoader myLoader = this.loader; // copy reference
        final AtomicBoolean myLoaded = this.loaded; // copy reference
        this.executorService.execute(() -> {
            try {
                myLoader.forEntries((item, bbTrans) -> {
                    final OffsetLengthCoder olCoder = myLoader.getOffsetLengthCoder();
                    eodData.loadQuoteData(olCoder, item.getKey(),
                        olCoder.encode(item.getOffset(), item.getLength()), bbTrans);
                    if (logger.isDebugEnabled()) {
                        if (eodData.size() % 1000000 == 0) {
                            logger.debug("<withEntry> {} {}", unit, eodData.size() + " in " + tt);
                        }
                    }
                });

                myLoaded.set(myLoader.isValid());
                logger.info("<withEntry> {} {}", unit, eodData.size() + " in " + tt);
            } catch (Exception e) {
                logger.error("<run> {} cannot load eod prices", unit, e);
            }
        });
    }

    private void clear() {
        if (null != this.eodData) {
            this.executorService.execute(() -> eodData.clear());
        }
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

    void dumpIndex(PrintStream ps) {
        this.eodData.dumpIndex(ps);
    }

    @Override
    public ByteBuffer loadData(long quote) throws IOException {
        if (null == this.loader) {
            return EodUtil.EMPTY_BB;
        }
        if (isLoaded()) {
            return this.eodData.loadData(this.loader.getOffsetLengthCoder(), quote);
        }
        return this.loader.loadData(quote);
    }

    private boolean isLoaded() {
        return null != this.loaded && this.loaded.get();
    }

    @Override
    public void close() throws IOException {
        IoUtils.close(this.loader);
        this.loader = null;
        this.loaded = null;
        clear();
        this.file = null;
        this.interval = null;
    }
}
