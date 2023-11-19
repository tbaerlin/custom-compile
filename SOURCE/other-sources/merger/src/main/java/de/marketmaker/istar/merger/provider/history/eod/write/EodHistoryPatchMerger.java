/*
 * EodHistoryMerger.java
 *
 * Created on 14.10.2014 11:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.HistoryWriter;

import static de.marketmaker.istar.feed.history.HistoryUnit.Patch;
import static de.marketmaker.istar.merger.provider.history.eod.write.EodHistoryArchive.CONTENT_TYPE;
import static de.marketmaker.istar.merger.provider.history.eod.write.EodHistoryArchive.LENGTH_BITS;

/**
 * @author zzhao
 */
public class EodHistoryPatchMerger {

    private static final Logger log = LoggerFactory.getLogger(EodHistoryPatchMerger.class);

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd");

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: EodHistoryPatchMerger {base_patch_file} {delta_patch_file}" +
                    " [pivot_date]");
            System.exit(1);
        }

        Path basePath = Paths.get(args[0]);
        Path deltaPath = Paths.get(args[1]);
        final int pivot = args.length >= 3 ? DateUtil.toYyyyMmDd(DTF.parseLocalDate(args[2])) : 0;

        mergePatch(basePath.toFile(), deltaPath.toFile(), pivot);
    }

    private static void mergePatch(File baseFile, File deltaFile, int pivot) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final File tmpFile = Patch.createTmpFile(CONTENT_TYPE, baseFile.getParentFile(),
                getPivotDate(Patch.getFromDate(baseFile), Patch.getFromDate(deltaFile), pivot),
                Math.max(Patch.getToDate(baseFile), Patch.getToDate(deltaFile)));
        log.info("<mergePatch> {} and {} onto {} with pivot {}", baseFile.getAbsolutePath(),
                deltaFile.getAbsolutePath(), tmpFile.getAbsoluteFile(), pivot);

        try (
                final EodReader<? extends EodItem> baseReader = new EodPricesReader(new DataFile(baseFile, true));
                final EodReader<? extends EodItem> deltaReader = new EodPricesReader(new DataFile(deltaFile, true));
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class)
        ) {
            merge(writer, baseReader.iterator(), deltaReader.iterator(), pivot);
        }
        final File unitFile = Patch.convert(tmpFile, Patch);
        HistoryUtil.replaceFile(unitFile, tmpFile);
        log.info("<mergePatch> {} took: {}", unitFile.getAbsolutePath(), tt);
    }

    static void merge(HistoryWriter<Long> writer, EodIterator<? extends EodItem> baseIt,
            EodIterator<? extends EodItem> deltaIt, int pivot) throws IOException {
        EodItem baseItem = HistoryUtil.nextItem(baseIt);
        EodItem deltaItem = HistoryUtil.nextItem(deltaIt);

        while (null != baseItem || null != deltaItem) {
            if (null == baseItem) {
                // no more entries from base file
                EodHistoryArchiveDaily.writeEntry(writer, deltaIt.getQuote(),
                        deltaItem.getBytes(true, 0));
                deltaItem = HistoryUtil.nextItem(deltaIt);
            }
            else if (null == deltaItem) {
                // no more entries from delta file
                EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(),
                        baseItem.getBytes(true, pivot));
                baseItem = HistoryUtil.nextItem(baseIt);
            }
            else {
                if (baseIt.getQuote() > deltaIt.getQuote()) {
                    // write and advance deltaIt
                    EodHistoryArchiveDaily.writeEntry(writer, deltaIt.getQuote(),
                            deltaItem.getBytes(true, 0));
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
                else if (baseIt.getQuote() < deltaIt.getQuote()) {
                    // write and advance baseIt
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(),
                            baseItem.getBytes(true, pivot));
                    baseItem = HistoryUtil.nextItem(baseIt);
                }
                else {
                    baseItem.merge(deltaItem, false);
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(),
                            baseItem.getBytes(true, pivot));
                    baseItem = HistoryUtil.nextItem(baseIt);
                    deltaItem = HistoryUtil.nextItem(deltaIt);
                }
            }
        }
    }

    static int getPivotDate(int baseFromDate, int deltaFromDate, int pivot) {
        return pivot == 0
                ? Math.min(baseFromDate, deltaFromDate)
                : Math.min(Math.max(baseFromDate, pivot), deltaFromDate);
    }
}
