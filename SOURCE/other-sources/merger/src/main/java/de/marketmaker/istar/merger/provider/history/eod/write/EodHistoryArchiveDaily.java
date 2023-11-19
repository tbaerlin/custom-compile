/*
 * EodPriceHistoryJHM.java
 *
 * Created on 10.12.12 11:48
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import static de.marketmaker.istar.feed.history.HistoryUnit.Months;
import static de.marketmaker.istar.feed.history.HistoryUnit.Patch;
import static de.marketmaker.istar.feed.history.HistoryUnit.Rest;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.HistoryWriter;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

/**
 * @author zzhao
 */
public class EodHistoryArchiveDaily implements EodHistoryArchive {

    private static final Logger log = LoggerFactory.getLogger(EodHistoryArchiveDaily.class);

    private final EodPriceHistory mpaHistory;

    private final EodPriceHistory mpcHistory;

    private File workDir;

    private EnumSet<HistoryUnit> units;

    private int currentDate;

    private int months = 1;

    /**
     * pivot date is used as lower boundary for EOD data, i.e. EOD data below or older than pivot
     * date are not included in resulting EOD data.
     */
    private int pivotDate;

    public void setMonths(int months) {
        this.months = months;
    }

    public EodHistoryArchiveDaily() {
        this.mpaHistory = new EodPriceHistory();
        this.mpcHistory = new EodPriceHistory();
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
        assertPriceHistoryEmpty(this.mpaHistory, "MPA");
        assertPriceHistoryEmpty(this.mpcHistory, "MPC");

        final File monthsFile = Months.getLatestFile(this.workDir);
        if (null != monthsFile) {
            if (date <= Months.getToDate(monthsFile)) {
                throw new IllegalStateException("history file: " + monthsFile.getAbsolutePath()
                        + " already contains data for " + date);
            }
        }
        this.units = HistoryUtil.loadHistoryUnits(this.workDir);
        this.currentDate = date; // date of the EOD file to import
        this.pivotDate = determinePivotDate(Months.getFromDate(monthsFile));

        log.info("<begin> for date {}, separation date {}", this.currentDate, this.pivotDate);
    }

    private int determinePivotDate(int monthsFileFromDate) {
        final LocalDate probePivotLocalDate = probePivotLocalDate(this.currentDate, this.months);
        final int probePivotDate = DateUtil.toYyyyMmDd(probePivotLocalDate);

        if (!mergeOntoRestWillHappen(monthsFileFromDate, probePivotDate)) {
            return probePivotDate;
        }

        if (LocalDate.now().getDayOfWeek() == DateTimeConstants.SUNDAY
            || Boolean.parseBoolean(System.getProperty("AnyDay", "false"))) {
            return probePivotDate;
        } else {
            // postpone merge onto Rest by increasing pivotMonths temporarily
            // increase pivotMonths means increasing EOD data that the Months file will take
            log.info("<determinePivotDate> postpone merge onto Rest");
            return DateUtil.toYyyyMmDd(probePivotLocalDate.minusMonths(1));
        }
    }

    private static LocalDate probePivotLocalDate(int date, int pivotMonths) {
        final LocalDate currentLocalDate = DateUtil.yyyyMmDdToLocalDate(date);
        // calculate pivot date this way, so that the Months file always contains data for pivotMonths
        // plus day 1 is necessary because toDate of Months file is inclusive
        return currentLocalDate.minusMonths(pivotMonths).plusDays(1);
    }

    private void assertPriceHistoryEmpty(EodPriceHistory priceHistory, String desc) {
        if (!priceHistory.isEmpty()) {
            log.error("<assertPriceHistoryEmpty> {} eod price not empty," +
                    " failed to clean up from last run", desc);
            throw new IllegalStateException("failed to clean up eod price history from last run");
        }
    }

    private void reset() {
        this.currentDate = 0;
        this.pivotDate = 0;
        this.units = null;
    }

    @Override
    public void update(long quote, int date, Int2ObjectSortedMap<byte[]> data) {
        if (date > this.currentDate) {
            log.warn("<update> future data {} ignored", date);
        }
        else if (date == this.currentDate) {
            this.mpaHistory.update(quote, date, data);
        }
        else {
            this.mpcHistory.update(quote, date, data);
        }
    }

    private boolean mergeOntoRestWillHappen(int monthsFileFromDate, int probePivotDate) {
        return monthsFileFromDate < probePivotDate // condition 1
            && this.units.contains(Rest) // condition 2
            && probePivotDate > Rest.getToDate(Rest.getLatestFile(this.workDir));
    }

    @Override
    public void finish() throws IOException {
        try (
                final EodPriceHistory mpa = this.mpaHistory;
                final EodPriceHistory mpc = this.mpcHistory
        ) {
            boolean reservePatchAfterMerge = false;
            if (!mpa.isEmpty()) {
                if (!this.units.contains(Months)) {
                    store(Months, mpa, this.workDir);
                    this.units.add(Months);
                }
                else {
                    final File monthsFile = Months.getLatestFile(this.workDir);
                    final int fromDate = Months.getFromDate(monthsFile);
                    final int toDate = Months.getToDate(monthsFile);

                    if (fromDate < this.pivotDate) { // condition 1
                        // since fromDate is below pivotDate, data from current Months file will be
                        // truncated. In this case check has to be made to ensure that such data
                        // are still available on Rest file. If not Months file must be merged onto
                        // Rest file.
                        if (!this.units.contains(Rest)) { // this is not merge with Rest
                            // merge the current months and patch file, the result is rest file
                            if (!this.units.contains(Patch)) {
                                // current months file will be rest file
                                final File tmpFileRest = Rest.createTmpFile(CONTENT_TYPE, this.workDir,
                                        fromDate, toDate);
                                FileCopyUtils.copy(monthsFile, tmpFileRest);
                                final File restFile = Months.convert(monthsFile, Rest);
                                HistoryUtil.replaceFile(restFile, tmpFileRest);
                            }
                            else {
                                // merge months and patch
                                File patchFile = Patch.getLatestFile(this.workDir);
                                monthsAndPatch(monthsFile, patchFile, 0);
                                if (Patch.getToDate(patchFile) >= this.pivotDate) {
                                    reservePatchAfterMerge = true;
                                }
                                else {
                                    this.units.remove(Patch);
                                }
                            }
                            this.units.add(Rest);
                        }
                        else {
                            final File restFile = Rest.getLatestFile(this.workDir);
                            final int toDateRest = Rest.getToDate(restFile);
                            if (this.pivotDate > toDateRest) { // condition 2
                                // only preserve the latest Rest file, remove older ones before
                                // creating new one to deal with limited disk size
                                HistoryUtil.deleteOlderFiles(this.workDir, 1, Rest);
                                if (!this.units.contains(Patch)) {
                                    // merge months and rest
                                    monthsOntoRest(restFile, monthsFile);
                                }
                                else {
                                    // merge months and patch(load) onto rest
                                    File patchFile = Patch.getLatestFile(this.workDir);
                                    monthsAndPatchOntoRest(restFile, monthsFile, patchFile);
                                    if (Patch.getToDate(patchFile) >= this.pivotDate) {
                                        reservePatchAfterMerge = true;
                                    }
                                    else {
                                        this.units.remove(Patch);
                                    }
                                }
                            }
                        }
                    }
                    // merge mpaHistory with months
                    updateUnit(Months, mpa, this.pivotDate);
                }
            }
            if (!mpc.isEmpty()) {
                if (!this.units.contains(Patch)) {
                    store(Patch, mpc, this.workDir);
                    this.units.add(Patch);
                }
                else {
                    // merge patch(load) with mpcHistory
                    if (reservePatchAfterMerge) { // ISTAR-588
                        mergePatches(mpc, this.pivotDate);
                    }
                    else {
                        updateUnit(Patch, mpc, 0);
                    }
                }
            }

            HistoryUtil.updateHistoryUnits(this.workDir, this.units);
            HistoryUtil.deleteOlderFiles(this.workDir, 2, Months, Patch);
        } finally {
            reset();
        }
    }

    private void mergePatches(EodPriceHistory mpc, int pivotDate) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File patchFile = Patch.getLatestFile(this.workDir);
        final File tmpFile = Patch.createTmpFile(CONTENT_TYPE, this.workDir,
                EodHistoryPatchMerger.getPivotDate(Patch.getFromDate(patchFile), mpc.getFromDate(),
                        pivotDate), Math.max(Patch.getToDate(patchFile), mpc.getToDate()));
        log.info("<mergePatches> {} and MPC onto {} pivot {}", patchFile.getAbsolutePath(),
                tmpFile.getAbsolutePath(), pivotDate);
        try (
                final EodReader<? extends EodItem> reader =
                        new EodPricesReader(new DataFile(patchFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            EodHistoryPatchMerger.merge(writer, reader.iterator(), mpc.iterator(), pivotDate);

            final File unitFile = Patch.convert(tmpFile, Patch);
            HistoryUtil.replaceFile(unitFile, tmpFile);
            
            log.info("<mergePatches> {} took: {}", unitFile.getAbsolutePath(), tt);
        } catch (IOException | IllegalStateException e) {
            cleanupOnException(tmpFile);
            throw e;
        }
    }

    private void monthsOntoRest(File restFile, File monthsFile) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File tmpFile = Rest.createTmpFile(CONTENT_TYPE, this.workDir,
                Math.min(Rest.getFromDate(restFile), Months.getFromDate(monthsFile)),
                Months.getToDate(monthsFile));
        log.info("<monthsOntoRest> {} {} into {}", restFile.getName(), monthsFile.getName(),
                tmpFile.getName());
        try (
                final EodReader<EodFields> restReader = new EodFieldsReader(new DataFile(restFile, true));
                final EodReader<EodFields> deltaReader = new EodFieldsReader(new DataFile(monthsFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            merge(writer, restReader.iterator(), deltaReader.iterator(), false, 0);

            final File unitFile = Rest.convert(tmpFile, Rest);
            HistoryUtil.replaceFile(unitFile, tmpFile);

            log.info("<monthsOntoRest> {} took: {}", unitFile.getAbsolutePath(), tt);
        } catch (IOException | IllegalStateException e) {
            cleanupOnException(tmpFile);
            throw e;
        }
    }

    private void monthsAndPatchOntoRest(File restFile, File monthsFile, File patchFile)
            throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File tmpFile = Rest.createTmpFile(CONTENT_TYPE, this.workDir,
                Math.min(Rest.getFromDate(restFile), Patch.getFromDate(patchFile)),
                Months.getToDate(monthsFile));
        log.info("<monthsAndPatchOntoRest> {} {} {} into {}", restFile.getName(),
                monthsFile.getName(), patchFile.getName(), tmpFile.getName());
        try (
                final EodReader<EodFields> baseReader = new EodFieldsCombiReader(
                        new EodFieldsReader(new DataFile(restFile, true)),
                        new EodFieldsReader(new DataFile(monthsFile, true)));
                final EodReader<EodPrices> deltaReader = new EodPricesReader(new DataFile(patchFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            merge(writer, baseReader.iterator(), deltaReader.iterator(), false, 0);

            final File unitFile = Rest.convert(tmpFile, Rest);
            HistoryUtil.replaceFile(unitFile, tmpFile);

            log.info("<monthsAndPatchOntoRest> {} took: {}", unitFile.getAbsolutePath(), tt);
        } catch (IOException | IllegalStateException e) {
            cleanupOnException(tmpFile);
            throw e;
        }
    }

    private static void cleanupOnException(File tmpFile) {
        if (!tmpFile.exists()) {
            return;
        }
        if (tmpFile.delete()) {
            log.info("<cleanupOnException> deleted " + tmpFile.getName());
        }
        else {
            log.error("<cleanupOnException> could not delete " + tmpFile.getName());
        }
    }

    private void monthsAndPatch(File monthsFile, File patchFile, int pivotDate)
            throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File tmpFile = Rest.createTmpFile(CONTENT_TYPE, this.workDir,
                Math.max(pivotDate, Math.min(Months.getFromDate(monthsFile), Patch.getFromDate(patchFile))),
                Math.max(Months.getToDate(monthsFile), Patch.getToDate(patchFile)));
        log.info("<monthsAndPatch> {}", tmpFile.getAbsolutePath());
        try (
                final EodReader<EodFields> baseReader = new EodFieldsReader(new DataFile(monthsFile, true));
                final EodReader<EodPrices> patchReader = new EodPricesReader(new DataFile(patchFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            merge(writer, baseReader.iterator(), patchReader.iterator(), false, pivotDate);

            final File unitFile = Rest.convert(tmpFile, Rest);
            HistoryUtil.replaceFile(unitFile, tmpFile);

            log.info("<monthsAndPatch> {} took: {}", unitFile.getAbsolutePath(), tt);
        } catch (IOException | IllegalStateException e) {
            cleanupOnException(tmpFile);
            throw e;
        }
    }

    static void merge(HistoryWriter<Long> writer, EodIterator<? extends EodItem> baseIt,
            EodIterator<? extends EodItem> deltaIt, boolean isPatch, int pivot)
            throws IOException {
        merge(writer, baseIt, deltaIt, isPatch, pivot, false);
    }

    static void merge(HistoryWriter<Long> writer, EodIterator<? extends EodItem> baseIt,
            EodIterator<? extends EodItem> deltaIt, boolean isPatch, int pivot, boolean extension)
            throws IOException {
        EodItem baseItem = HistoryUtil.nextItem(baseIt);
        EodItem deltaItem = HistoryUtil.nextItem(deltaIt);

        while (null != baseItem || null != deltaItem) {
            if (null == baseItem) {
                // no more entries from months file
                writeEntry(writer, deltaIt.getQuote(), deltaItem.getBytes(isPatch, pivot));
                deltaItem = HistoryUtil.nextItem(deltaIt);
            }
            else if (null == deltaItem) {
                // no more entries from patch file
                writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(isPatch, pivot));
                baseItem = HistoryUtil.nextItem(baseIt);
            }
            else {
                if (baseIt.getQuote() > deltaIt.getQuote()) {
                    // write and advance patchIt
                    writeEntry(writer, deltaIt.getQuote(), deltaItem.getBytes(isPatch, pivot));
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
                else if (baseIt.getQuote() < deltaIt.getQuote()) {
                    // write and advance monthsIt
                    writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(isPatch, pivot));
                    baseItem = HistoryUtil.nextItem(baseIt);
                }
                else {
                    baseItem.merge(deltaItem, extension);
                    writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(isPatch, pivot));
                    baseItem = HistoryUtil.nextItem(baseIt);
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
            }
        }
    }

    static void writeEntry(HistoryWriter<Long> writer, long quote, byte[] bytes)
            throws IOException {
        if (null == bytes) {
            log.error("<writeEntry> cannot write data for quote: {}", quote);
        }
        else {
            writer.withEntry(quote, bytes);
        }
    }

    private void updateUnit(HistoryUnit unit, EodPriceHistory ph, int pivot) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File latestFile = unit.getLatestFile(this.workDir);
        final File tmpFile = unit.createTmpFile(CONTENT_TYPE, this.workDir,
                Math.max(pivot, Math.min(unit.getFromDate(latestFile), ph.getFromDate())),
                Math.max(unit.getToDate(latestFile), ph.getToDate()));
        log.info("<updateUnit> {}", tmpFile.getAbsolutePath());
        try (
                final EodReader<? extends EodItem> reader = Patch == unit ?
                        new EodPricesReader(new DataFile(latestFile, true)) :
                        new EodFieldsReader(new DataFile(latestFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            final boolean isPatch = unit == Patch;
            merge(writer, reader.iterator(), ph.iterator(), isPatch, pivot);

            final File unitFile = unit.convert(tmpFile, unit);
            HistoryUtil.replaceFile(unitFile, tmpFile);

            log.info("<updateUnit> {} took: {}", unitFile.getAbsolutePath(), tt);
        } catch (IOException | IllegalStateException e) {
            cleanupOnException(tmpFile);
            throw e;
        }
    }

    static void store(HistoryUnit unit, EodPriceHistory eph, File workDir) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File tmpFile = unit.createTmpFile(CONTENT_TYPE, workDir, eph.getFromDate(),
                eph.getToDate());
        log.info("<store> {}", tmpFile.getAbsolutePath());
        try (final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)) {
            final boolean isPatch = Patch == unit;
            final EodIterator<EodPrices> it = eph.iterator();
            while (it.hasNext()) {
                final EodPrices price = it.next();
                writeEntry(writer, it.getQuote(), price.getBytes(isPatch, 0));
            }

            final File unitFile = unit.convert(tmpFile, unit);
            HistoryUtil.replaceFile(unitFile, tmpFile);

            log.info("<store> {} took: {}", unitFile.getAbsolutePath(), tt);
        } catch (IOException | IllegalStateException e) {
            cleanupOnException(tmpFile);
            throw e;
        }
    }
}
