/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.TickType;

import static de.marketmaker.istar.feed.history.HistoryUnit.*;

/**
 * @author zzhao
 */
public class TickHistoryArchiveBA extends TickHistoryArchive {

    private static final int HISTORY_MONTHS = 14;

    public TickHistoryArchiveBA(String tickType) {
        super(tickType);
        //Assert.isTrue(EnumSet.of(TickType.ASK, TickType.BID, TickType.SYNTHETIC_TRADE).contains(
        //        this.tickType), "tick type required: BID/ASK");
    }

    @Override
    protected void onUpdateWithDay(File file, EnumSet<HistoryUnit> units) throws IOException {
        dayOntoMonth(file, units);
        // delete month files older than 13 months, only keep two month files for current month
        final LocalDate curMonthBegin = HistoryUtil.getMonthBeginDate(Day.getFromDate(file));
        final LocalDate pivotMonthBegin = curMonthBegin.minusMonths(HISTORY_MONTHS);
        final HashMap<LocalDate, List<File>> cat = new HashMap<>(20);

        final List<File> files = Month.getRelevantFiles(getWorkDir());
        for (File mf : files) {
            final LocalDate date = HistoryUtil.getMonthBeginDate(Month.getToDate(mf));
            if (!cat.containsKey(date)) {
                cat.put(date, new ArrayList<File>(2));
            }
            cat.get(date).add(mf);
        }

        for (Map.Entry<LocalDate, List<File>> entry : cat.entrySet()) {
            final LocalDate date = entry.getKey();
            final List<File> mfs = entry.getValue();
            if (date.isBefore(pivotMonthBegin)) {
                for (File mf : mfs) {
                    HistoryUtil.reportFileOpStatus(mf.delete(), "deleting: " + mf.getAbsolutePath());
                }
            }
            else {
                if (mfs.size() > 1) {
                    final int fromIdx;
                    if (date.equals(curMonthBegin)) {
                        fromIdx = 2;
                    }
                    else {
                        fromIdx = 1;
                    }
                    for (int i = fromIdx; i < mfs.size(); i++) {
                        HistoryUtil.reportFileOpStatus(mfs.get(i).delete(),
                                "deleting: " + mfs.get(i).getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    protected void onNewMonthBegin(File curMonthFile, EnumSet<HistoryUnit> units)
            throws IOException {
        // no merge onto year file
        // check patch file if exists
        if (!units.contains(Patch)) {
            return;
        }
        // if patch contains only data older than 13 months, remove patch entry from units
        final File curPatchFile = Patch.getLatestFile(getWorkDir());
        final File latestMonthFile = Month.getLatestFile(getWorkDir());
        final LocalDate pivotDate = DateUtil.yyyyMmDdToLocalDate(
                Month.getFromDate(latestMonthFile)).minusMonths(12);

        final LocalDate toDate = DateUtil.yyyyMmDdToLocalDate(Patch.getToDate(curPatchFile));
        if (toDate.isBefore(pivotDate)) {
            units.remove(Patch);
        }
        else {
            final LocalDate fromDate = DateUtil.yyyyMmDdToLocalDate(Patch.getFromDate(curPatchFile));
            if (fromDate.isBefore(pivotDate)) {
                // create new patch file which contains only data within 13 months
                final File tmpFile = Patch.createTmpFile(getContentType(), getWorkDir(),
                        pivotDate, toDate);
                if (tmpFile.exists()) {
                    HistoryUtil.ensureFileOpOK(tmpFile.delete(), "deleting: " + tmpFile.getAbsolutePath());
                }
                HistoryUtil.ensureFileOpOK(tmpFile.createNewFile(), "creating: " + tmpFile.getAbsolutePath());
                final PatchMerger<ByteString> merger = new PatchMerger<>(tmpFile, curPatchFile, ByteString.class);
                merger.merge(tmpFile, getEntryMergerJoin(
                        HistoryUtil.daysFromBegin(this.context.getGenesis(), pivotDate)));
                final File patchFile = Patch.convert(tmpFile, Patch);
                HistoryUtil.replaceFile(tmpFile, patchFile);
            }
        }
    }
}
