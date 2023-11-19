/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.TickType;

import static de.marketmaker.istar.feed.history.HistoryUnit.*;

/**
 * @author zzhao
 */
public class TickHistoryArchive extends HistoryArchive<ByteString> {

    protected final TickType tickType;

    public TickHistoryArchive(String tickType) {
        super(ByteString.class);
        this.tickType = TickType.valueOf(tickType.toUpperCase());
    }

    @Override
    public EntryMerger getEntryMergerJoin(int days) {
        return new EntryMergerJoin<>(days, MutableTickEntry.class);
    }

    @Override
    public EntryMerger getEntryMergerCompact(int days) {
        return new EntryMergerCompact<>(days, MutableTickEntry.class);
    }

    @Override
    public final String getContentType() {
        return this.tickType.name();
    }

    @Override
    protected void onUpdateWithDay(File file, EnumSet<HistoryUnit> units) throws IOException {
        dayOntoMonth(file, units);
        HistoryUtil.deleteOlderFiles(file.getParentFile(), Month, Year);
    }

    protected void dayOntoMonth(File file, EnumSet<HistoryUnit> units) throws IOException {
        final int date = Day.getFromDate(file);
        if (!units.contains(Month)) {
            // day file will be month file
            final File monthFile = Month.createFile(getContentType(), getWorkDir(), date, date);
            HistoryUtil.replaceFile(monthFile, file);
            units.add(Month);
        }
        else {
            final LocalDate dateTime = DateUtil.yyyyMmDdToLocalDate(date);
            final File curMonthFile = Month.getLatestFile(getWorkDir());
            final LocalDate toDay = DateUtil.yyyyMmDdToLocalDate(Month.getToDate(curMonthFile));
            if (dateTime.isAfter(toDay)) {
                if (dateTime.getMonthOfYear() != toDay.getMonthOfYear()) {
                    onNewMonthBegin(curMonthFile, units);
                    final File monthFile = Month.createFile(getContentType(), getWorkDir(), date, date);
                    HistoryUtil.replaceFile(monthFile, file);
                }
                else {
                    // create new month file merging current month file and day file
                    final int fromDate = Month.getFromDate(curMonthFile);
                    final File tmp = Month.createTmpFile(getContentType(), getWorkDir(), fromDate, date);
                    fastForward(curMonthFile, file, tmp);
                    final File monthFile = Month.createFile(getContentType(), getWorkDir(), fromDate, date);
                    HistoryUtil.replaceFile(monthFile, tmp);
                    HistoryUtil.ensureFileOpOK(file.delete(), "deleting " + file.getAbsolutePath());
                }
            }
            else {
                this.logger.warn("<dayOntoMonth> ignore older day file:" +
                        " " + date + ", month tick: " + curMonthFile.getName());
            }
        }
    }

    protected void onNewMonthBegin(File curMonthFile,
            EnumSet<HistoryUnit> units) throws IOException {
        // merge current month file onto year file
        monthOntoYear(curMonthFile, units);
    }

    private void monthOntoYear(File monthFile, EnumSet<HistoryUnit> units) throws IOException {
        if (!units.contains(Year)) {
            final File yearFile = Month.convert(monthFile, Year);
            if (!units.contains(Patch)) {
                // month file will be year file
                FileUtils.copyFile(monthFile, yearFile, true);
            }
            else {
                // merge month file and patch file to get year file
                final File curPatchFile = Patch.getLatestFile(getWorkDir());
                final LocalDate yearFromDate = DateUtil.yyyyMmDdToLocalDate(Year.getFromDate(yearFile));
                final PatchMerger<ByteString> merger =
                        new PatchMerger<>(monthFile, curPatchFile, ByteString.class);
                merger.merge(yearFile, getEntryMergerJoin(
                        HistoryUtil.daysFromBegin(this.context.getGenesis(), yearFromDate)));
                units.remove(Patch);
            }
            units.add(Year);
        }
        else {
            final File curYearFile = Year.getLatestFile(getWorkDir());
            final LocalDate yearFromDate = DateUtil.yyyyMmDdToLocalDate(Year.getFromDate(curYearFile));
            final LocalDate toDay = DateUtil.yyyyMmDdToLocalDate(Month.getToDate(monthFile));
            final LocalDate dayLastYear = toDay.minusYears(1).plusDays(1);
            final int days = HistoryUtil.daysFromBegin(this.context.getGenesis(), dayLastYear);
            // create new year file merging current year file and month file
            final File tmp = Year.createTmpFile(getContentType(), getWorkDir(),
                    yearFromDate.isAfter(dayLastYear) ? yearFromDate : dayLastYear, toDay);
            if (!units.contains(Patch)) {
                compactAndForward(curYearFile, monthFile, tmp, days);
            }
            else {
                final File curPatchFile = Patch.getLatestFile(getWorkDir());
                mergeHistory(curYearFile, monthFile, curPatchFile, tmp, days);
                units.remove(Patch);
            }
            final File yearFile = Year.convert(tmp, Year);
            HistoryUtil.replaceFile(yearFile, tmp);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: [target dir] [day file]");
            System.exit(1);
        }

        final File targetDir = new File(args[0]);
        final File dayFile = new File(args[1]);
        TickHistoryContext ctx = TickHistoryContextImpl.fromEnv();

        final File lmf = Month.getLatestFile(targetDir);
        if (null == lmf) {
            merge(targetDir, dayFile, ctx);
        }
        else {
            if (Day.getFromDate(dayFile) <= Month.getToDate(lmf)) {
                System.err.println("cannot merge outdated file: " + dayFile.getAbsolutePath()
                        + ", current month file: " + lmf.getAbsolutePath());
            }
            else {
                System.out.println(dayFile.getAbsolutePath());
                System.out.println(">onto<");
                System.out.println(lmf.getAbsolutePath());
                System.out.print("Are you sure? (y/n): ");
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(System.in));
                    final String cmd = br.readLine();
                    if ("y".equalsIgnoreCase(cmd)) {
                        merge(targetDir, dayFile, ctx);
                    }
                    else {
                        System.out.println("cancelled.");
                    }
                } finally {
                    IoUtils.close(br);
                }
            }
        }
    }

    private static void merge(File targetDir, File dayFile, TickHistoryContext ctx)
            throws IOException {
        final TickHistoryArchive archive = getArchive(TickType.valueOf(HistoryUnit.getContentType(dayFile)));
        archive.setContext(ctx);
        archive.setWorkDir(targetDir);
        archive.update(Day, dayFile);
    }

    private static TickHistoryArchive getArchive(TickType tickType) {
        switch (tickType) {
            case TRADE:
                return new TickHistoryArchive(tickType.name());
            case ASK:
            case BID:
                return new TickHistoryArchiveBA(tickType.name());
            default:
                throw new UnsupportedOperationException("no support for: " + tickType);
        }
    }
}
