/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.HistoryWriter;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

import static de.marketmaker.istar.feed.history.HistoryUnit.Rest;
import static de.marketmaker.istar.merger.provider.history.eod.write.EodHistoryArchive.CONTENT_TYPE;
import static de.marketmaker.istar.merger.provider.history.eod.write.EodHistoryArchive.LENGTH_BITS;

/**
 * @author zzhao
 */
public class EodPriceHistoryConsolidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws IOException {
        if (null == args || args.length < 2) {
            usage();
            System.exit(1);
        }

        final Path eodDirPath = Paths.get(args[0]);
        final Path quoteIdFilePath = Paths.get(args[1]);

        if (!Files.exists(quoteIdFilePath) || !Files.isReadable(quoteIdFilePath)) {
            System.err.println("quote id file " + quoteIdFilePath + " not exist or not readable");
            System.exit(1);
        }

        if (!Files.exists(eodDirPath) || !Files.isDirectory(eodDirPath)) {
            System.err.println(eodDirPath + " invalid");
            System.exit(1);
        }

        TimeTaker tt = new TimeTaker();
        final EodPriceHistoryConsolidator consolidator = new EodPriceHistoryConsolidator();
        consolidator.consolidate(eodDirPath, quoteIdFilePath);
        System.out.println("took: " + tt);
    }

    void consolidate(Path eodDirPath, Path quoteIdFilePath) throws IOException {
        final File latestRestFile = Rest.getLatestFile(eodDirPath.toFile());
        if (latestRestFile == null) {
            System.out.println("no EOD rest file exists in " + eodDirPath + ", no consolidation necessary");
            return;
        }

        EodUtil.updateWithinLock(EodUtil.getUpdateLockFile(eodDirPath.toFile()),
                () -> consolidateWithinLock(latestRestFile, quoteIdFilePath));
    }

    private void consolidateWithinLock(File restFile, Path quoteIdFilePath) throws IOException {
        final File tmpFile = Rest.createTmpFile(CONTENT_TYPE, restFile.getParentFile(),
                Rest.getFromDate(restFile), Rest.getToDate(restFile));
        final Path eodDirPath = restFile.getParentFile().toPath();
        this.logger.info("<consolidateWithinLock> into {}", tmpFile.getAbsolutePath());
        try (
                final EodReader eodReader = new EodFieldsReader(new DataFile(restFile, true));
                final Stream<String> quoteIdStream = Files.lines(quoteIdFilePath);
                final HistoryWriter<Long> writer = new HistoryWriter<>(tmpFile, LENGTH_BITS, Long.class);
                final BufferedWriter quoteExpired = Files.newBufferedWriter(
                        eodDirPath.resolve("quote_expired.txt"), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                final BufferedWriter quoteNoEod = Files.newBufferedWriter(
                        eodDirPath.resolve("quote_no_eod.txt"), StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
        ) {
            consolidate(writer, eodReader.iterator(), quoteIdStream.iterator(), quoteExpired, quoteNoEod);
        }

        final File unitFile = Rest.convert(tmpFile, Rest);
        HistoryUtil.replaceFile(unitFile, tmpFile);
        this.logger.info("<consolidateWithinLock> done with: {}", unitFile);
    }

    private void consolidate(HistoryWriter<Long> writer, EodIterator<EodFields> baseIt,
            Iterator<String> quoteIdIt, BufferedWriter quotesExpired, BufferedWriter quotesNoEod)
            throws IOException {
        EodFields baseItem = HistoryUtil.nextItem(baseIt); // from rest file
        Long qid = nextQuoteId(quoteIdIt); // from quote id file

        while (null != baseItem) {
            if (null == qid) {
                // no more entries from quote id file, all remaining eod prices are obsolete
                quotesExpired.write(baseIt.getQuote() + "\n");
                baseItem = HistoryUtil.nextItem(baseIt);
            }
            else {
                if (baseIt.getQuote() > qid) {
                    // advance quote id, report candidate qid of missing EOD prices
                    quotesNoEod.write(qid + "\n");
                    qid = nextQuoteId(quoteIdIt);
                }
                else if (baseIt.getQuote() < qid) {
                    // obsolete EOD prices, report
                    quotesExpired.write(baseIt.getQuote() + "\n");
                    baseItem = HistoryUtil.nextItem(baseIt);
                }
                else {
                    // retain active EOD prices
                    EodHistoryArchiveDaily.writeEntry(writer, baseIt.getQuote(), baseItem.getBytes(false, 0));
                    baseItem = HistoryUtil.nextItem(baseIt);
                    qid = nextQuoteId(quoteIdIt);
                }
            }
        }
        while (qid != null) {
            quotesNoEod.write(qid + "\n");
            qid = nextQuoteId(quoteIdIt);
        }
    }

    private static Matcher qidMatcher = Pattern.compile("(\\d+)").matcher("");

    private static Long nextQuoteId(Iterator<String> quoteIdIt) {
        if (!quoteIdIt.hasNext()) {
            return null;
        }
        do {
            final String next = quoteIdIt.next();
            qidMatcher.reset(next);
            if (qidMatcher.find()) {
                return Long.parseLong(qidMatcher.group(1));
            }

        } while (quoteIdIt.hasNext());
        return null;
    }

    private static void usage() {
        System.err.println("Usage: eod_dir quote_id_file");
    }
}
