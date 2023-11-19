/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
public class EodPriceHistoryISM {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws IOException {
        if (null == args || args.length < 2) {
            usage();
            System.exit(1);
        }

        final File eodDir;
        final File history;
        String months = getOption("months");

        eodDir = new File(args[0]);
        history = new File(args[1]);
        if (!history.isFile()) {
            System.err.println("history " + history.getAbsolutePath() + " is not a file");
            System.exit(1);
        }

        if (!eodDir.exists() || !eodDir.isDirectory()) {
            System.err.println(eodDir.getAbsolutePath() + " invalid");
            System.exit(1);
        }

        TimeTaker tt = new TimeTaker();
        final EodPriceHistoryISM init = new EodPriceHistoryISM();
        final EodHistoryArchiveISM archive = new EodHistoryArchiveISM();
        if (null != months) {
            archive.setMonths(Integer.parseInt(months));
        }
        archive.setWorkDir(eodDir);
        archive.setEodType(EodTicker.Type.fromName(history.getName()));
        if (null != getOption("units")) {
            archive.setUpdateUnits(parseUnits(getOption("units")));
        }
        init.tickFile(history, archive);

        System.out.println("took: " + tt);
    }

    private static String getOption(String optionName) {
        return System.getProperty(optionName, null);
    }

    private static EnumSet<HistoryUnit> parseUnits(String units) {
        final EnumSet<HistoryUnit> ret = EnumSet.noneOf(HistoryUnit.class);
        final String[] unitNames = units.split(",");
        for (String unitName : unitNames) {
            ret.add(HistoryUnit.valueOf(unitName));
        }

        return ret;
    }

    private static void usage() {
        System.err.println("Usage: eod_dir history_file");
    }

    private void tickFile(final File historyFile, final EodHistoryArchiveISM archive)
            throws IOException {
        EodUtil.updateWithinLock(archive.getUpdateLockFile(), new EodUtil.EodOperation() {
            @Override
            public void process() throws IOException {
                tickWithinLock(historyFile, archive);
            }
        });
    }

    private void tickWithinLock(File historyFile, EodHistoryArchiveISM archive) throws IOException {
        final TimeTaker tt = new TimeTaker();
        final int date = (archive.getEodType() == EodTicker.Type.EOD_A
                || archive.getEodType() == EodTicker.Type.EOD_C)
                ? DateUtil.toYyyyMmDd(EodUtil.getDateFromProtobuf(historyFile.getName()))
                : -1;
        archive.begin(date);
        this.logger.info("<tickWithinLock> {}", historyFile.getAbsolutePath());
        try (final EodTickerProtobuf ticker = new EodTickerProtobuf(historyFile)) {
            while (ticker.hasNext()) {
                final EodTick tick = ticker.next();
                if (null != tick) {
                    archive.update(tick.getQuote(), tick.getDate(), tick.getValues());
                }
            }
        }
        archive.finish();
        this.logger.info("<tickWithinLock> {} took: {}", historyFile.getAbsolutePath(), tt);
    }
}
