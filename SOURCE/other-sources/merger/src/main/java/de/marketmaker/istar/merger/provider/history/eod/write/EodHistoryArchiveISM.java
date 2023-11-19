/*
 * EodPriceHistoryJHM.java
 *
 * Created on 10.12.12 11:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.HistoryWriter;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

import static de.marketmaker.istar.feed.history.HistoryUnit.*;

/**
 * Used to manually update EoD history and can handle the following types with conditions:
 * <ul>
 * <li>eod_i: in case of initial export or huge corrections, i.e. eod_c huge and has to handled
 * through initial export with proper partitioning</li>
 * <li>eod_e: in case of ADF_Fields extension</li>
 * <li>eod_s: in case of series for ADF_Fields extension for small files</li>
 * <li>eod_c: in case of daily update failed for eod_c, with condition that the production date
 * of eod_c is not after the {to date} of months unit file. Otherwise use daily archive instead</li>
 * <li>eod_a: in case of daily update failed for eod_a, with condition that the production date
 * of eod_a is not after the {to date} of months unit file. Otherwise use daily archive instead</li>
 * </ul>
 *
 * @author zzhao
 */
public class EodHistoryArchiveISM implements EodHistoryArchive {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EodPriceHistory restHist;

    private final EodPriceHistory monthsHist;

    private final EodPriceHistory patchHist;

    private File workDir;

    private EnumSet<HistoryUnit> units;

    private int currentDate;

    private int months = 1;

    private int pivotDate;

    private EodTicker.Type eodType;

    private EnumSet<HistoryUnit> updateUnits = EnumSet.allOf(HistoryUnit.class);

    public void setEodType(EodTicker.Type eodType) {
        this.eodType = eodType;
    }

    public EodTicker.Type getEodType() {
        return eodType;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public EodHistoryArchiveISM() {
        this.restHist = new EodPriceHistory();
        this.monthsHist = new EodPriceHistory();
        this.patchHist = new EodPriceHistory();
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    @Override
    public File getUpdateLockFile() {
        return EodUtil.getUpdateLockFile(this.workDir);
    }

    @Override
    public void begin(int date) throws IOException {
        this.units = HistoryUtil.loadHistoryUnits(this.workDir);
        final File monthsFile = Months.getLatestFile(this.workDir);
        if (null != monthsFile) {
            this.pivotDate = Months.getFromDate(monthsFile);
            final int toDate = Months.getToDate(monthsFile);
            final LocalDate toLocalDate = DateUtil.yyyyMmDdToLocalDate(toDate);
            if (this.eodType == EodTicker.Type.EOD_A || this.eodType == EodTicker.Type.EOD_C) {
                if (date > toDate) {
                    throw new IllegalStateException("production date " + date
                            + " after to-date " + toDate + " of months unit"
                            + ", use daily archive instead");
                }
                this.currentDate = date;
            }
            else {
                if (toLocalDate.isBefore(
                        DateUtil.yyyyMmDdToLocalDate(this.pivotDate).plusMonths(this.months))) {
                    this.currentDate = DateUtil.toYyyyMmDd(new LocalDate());
                }
                else {
                    this.currentDate = toDate;
                }
            }
        }
        else {
            final LocalDate today = new LocalDate();
            this.pivotDate = DateUtil.toYyyyMmDd(today.minusMonths(this.months).plusDays(1));
            this.currentDate = DateUtil.toYyyyMmDd(today);
        }
        this.logger.info("<begin> for date {}, separation date {}", this.currentDate, this.pivotDate);
    }

    private void reset() {
        this.currentDate = 0;
        this.pivotDate = 0;
        this.units = null;
    }

    @Override
    public void update(long quote, int date, Int2ObjectSortedMap<byte[]> data) {
        if (date > this.currentDate) {
            this.logger.warn("<update> future price {}:{} ignored", quote, date);
            return;
        }
        switch (this.eodType) {
            case EOD_I:
            case EOD_E:
            case EOD_A:
                if (date < this.pivotDate) {
                    update(Rest, quote, date, data, this.restHist);
                    if (this.eodType == EodTicker.Type.EOD_E) {
                        // gatherer uses patch content with priority
                        update(Patch, quote, date, data, this.patchHist);
                    }
                }
                else {
                    update(Months, quote, date, data, this.monthsHist);
                }
                break;
            case EOD_C:
                if (date < this.pivotDate) {
                    update(Patch, quote, date, data, this.patchHist);
                }
                else {
                    update(Months, quote, date, data, this.monthsHist);
                }
                break;
            case EOD_P: // for small eod_i file, consider to just merge it on patch
            case EOD_S: // for small eod_s file, consider to just merge it on patch
                update(Patch, quote, date, data, this.patchHist);
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + this.eodType);
        }
    }

    private void update(HistoryUnit unit, long quote, int date, Int2ObjectSortedMap<byte[]> data,
            EodPriceHistory ph) {
        if (this.updateUnits.contains(unit)) {
            ph.update(quote, date, data);
        }
    }

    @Override
    public void finish() throws IOException {
        try (
                final EodPriceHistory eodRest = this.restHist;
                final EodPriceHistory eodMonths = this.monthsHist;
                final EodPriceHistory eodPatch = this.patchHist
        ) {
            finishUnit(eodRest, Rest);
            finishUnit(eodMonths, Months);
            finishUnit(eodPatch, Patch);

            if (this.eodType == EodTicker.Type.EOD_I && this.units.contains(Patch)) {
                if (!eodRest.isEmpty() || !eodMonths.isEmpty()) {
                    reducePatch(eodRest, eodMonths);
                }
            }

            HistoryUtil.deleteOlderFiles(this.workDir, Rest, Months, Patch);
            HistoryUtil.updateHistoryUnits(this.workDir, this.units);
        } finally {
            reset();
        }
    }

    private void finishUnit(EodPriceHistory eodPriceHistory, HistoryUnit unit) throws IOException {
        if (!eodPriceHistory.isEmpty()) {
            if (!this.units.contains(unit)) {
                EodHistoryArchiveDaily.store(unit, eodPriceHistory, this.workDir);
                this.units.add(unit);
            }
            else {
                mergeUnit(unit, eodPriceHistory, this.eodType == EodTicker.Type.EOD_E);
            }
        }
    }

    private void reducePatch(EodPriceHistory eodRest, EodPriceHistory eodMonths)
            throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File latestFile = Patch.getLatestFile(this.workDir);
        final File tmpFile = Patch.createTmpFile(CONTENT_TYPE, this.workDir,
                Patch.getFromDate(latestFile), Patch.getToDate(latestFile));
        this.logger.info("<reducePatch> {}", tmpFile.getAbsolutePath());
        final DateContext dc = new DateContext();
        try (
                final EodReader<EodPrices> pathReader =
                        new EodPricesReader(new DataFile(latestFile, true));
                final EodReader<EodPrices> combiReader =
                        new EodPricesCombiReader(eodRest, eodMonths);
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            reducePatch(writer, pathReader.iterator(), combiReader.iterator(), dc);
        }
        final File unitFile = Patch.createFile(CONTENT_TYPE, this.workDir, dc.from, dc.to);
        HistoryUtil.replaceFile(unitFile, tmpFile);
        this.logger.info("<reducePatch> {} took: {}", unitFile.getAbsolutePath(), tt);
    }

    private void reducePatch(HistoryWriter<Long> writer, EodIterator<EodPrices> baseIt,
            EodIterator<EodPrices> deltaIt, DateContext dc) throws IOException {
        EodPrices baseItem = HistoryUtil.nextItem(baseIt);
        EodPrices deltaItem = HistoryUtil.nextItem(deltaIt);

        while (null != baseItem) {
            if (null == deltaItem) {
                // no more entries from combi reader
                EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(true, 0));
                baseItem.updateDateContext(dc);
                baseItem = HistoryUtil.nextItem(baseIt);
            }
            else {
                if (baseIt.getQuote() > deltaIt.getQuote()) {
                    // advance patchIt
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
                else if (baseIt.getQuote() < deltaIt.getQuote()) {
                    // write and advance baseIt
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(true, 0));
                    baseItem.updateDateContext(dc);
                    baseItem = HistoryUtil.nextItem(baseIt);
                }
                else {
                    baseItem.reduce(deltaItem, dc);
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(true, 0));
                    baseItem = HistoryUtil.nextItem(baseIt);
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
            }
        }
    }

    public void setUpdateUnits(EnumSet<HistoryUnit> updateUnits) {
        this.updateUnits = updateUnits;
    }

    static final class DateContext {
        private int from = Integer.MAX_VALUE;

        private int to = Integer.MIN_VALUE;

        public void update(int yyyyMMdd) {
            this.from = Math.min(this.from, yyyyMMdd);
            this.to = Math.max(this.to, yyyyMMdd);
        }

        int getFrom() {
            return from;
        }

        int getTo() {
            return to;
        }
    }

    private void mergeUnit(HistoryUnit unit, EodPriceHistory ph, boolean extension)
            throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File latestFile = unit.getLatestFile(this.workDir);
        final File tmpFile = unit.createTmpFile(CONTENT_TYPE, this.workDir,
                Math.min(unit.getFromDate(latestFile), ph.getFromDate()),
                Math.max(unit.getToDate(latestFile), ph.getToDate()));
        this.logger.info("<mergeUnit> {}", tmpFile.getAbsolutePath());
        final boolean isPatch = unit == Patch;
        try (
                final EodReader<? extends EodItem> reader = isPatch
                        ? new EodPricesReader(new DataFile(latestFile, true))
                        : new EodFieldsReader(new DataFile(latestFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            if (isPatch && extension) {
                merge(writer, (EodIterator<EodPrices>) reader.iterator(), ph.iterator());
            }
            else {
                EodHistoryArchiveDaily.merge(writer, reader.iterator(), ph.iterator(), isPatch, 0,
                        extension);
            }
        }
        final File unitFile = unit.convert(tmpFile, unit);
        HistoryUtil.replaceFile(unitFile, tmpFile);
        this.logger.info("<mergeUnit> {} took: {}", unitFile.getAbsolutePath(), tt);
    }

    private void merge(HistoryWriter<Long> writer, EodIterator<EodPrices> baseIt,
            EodIterator<EodPrices> deltaIt) throws IOException {
        EodPrices baseItem = HistoryUtil.nextItem(baseIt); // from patch file
        EodPrices deltaItem = HistoryUtil.nextItem(deltaIt); // from extension

        while (null != baseItem) {
            if (null == deltaItem) {
                // no more entries from extension
                EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(true, 0));
                baseItem = HistoryUtil.nextItem(baseIt);
            }
            else {
                if (baseIt.getQuote() > deltaIt.getQuote()) {
                    // advance items from extension
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
                else if (baseIt.getQuote() < deltaIt.getQuote()) {
                    // write and advance from patch file
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(true, 0));
                    baseItem = HistoryUtil.nextItem(baseIt);
                }
                else {
                    baseItem.merge(deltaItem, true);
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(true, 0));
                    baseItem = HistoryUtil.nextItem(baseIt);
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
            }
        }
    }
}
