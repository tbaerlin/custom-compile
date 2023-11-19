/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.distruptor.DisruptTicker;

import static de.marketmaker.istar.feed.history.HistoryUnit.Day;

/**
 * @author zzhao
 */
@ManagedResource(description = "Tick history controller")
public class TickHistoryController extends HistoryController {

    protected MinuteTicker ticker;

    private Map<TickType, TickHistoryArchive> archives;

    private Map<TickType, File> workDirs;

    public TickHistoryController() {
        super();
    }

    public void setArchives(Map<String, TickHistoryArchive> archives) {
        this.archives = new EnumMap<>(TickType.class);
        this.workDirs = new EnumMap<>(TickType.class);
        for (Map.Entry<String, TickHistoryArchive> entry : archives.entrySet()) {
            final TickType tickType = TickType.valueOf(entry.getKey().toUpperCase());
            this.archives.put(tickType, entry.getValue());
            this.workDirs.put(tickType, entry.getValue().getWorkDir());
        }
    }

    public void setTicker(MinuteTicker ticker) {
        this.ticker = ticker;
    }

    @ManagedOperation(description = "invoke to view the working status")
    public String status() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<TickType, File> entry : workDirs.entrySet()) {
            sb.append(entry.getKey()).append(" into: ").append(entry.getValue().getAbsolutePath());
            sb.append("\n");
        }
        sb.append(getWorkingStatus());
        return sb.toString();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.isTrue(null != this.ticker, "minute ticker required");
    }

    protected final void tickIntern(File tickDir) throws IOException {
        final EnumMap<TickType, File> map = this.ticker.produceMinuteTicks(tickDir, this.workDirs);
        for (Map.Entry<TickType, File> entry : map.entrySet()) {
            this.archives.get(entry.getKey()).update(Day, entry.getValue());
        }
    }

    private static final Pattern TICK_DIR = Pattern.compile("^tick([0-9]+)$");

    private static final FileFilter FF_TICK = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() && TICK_DIR.matcher(file.getName()).matches();
        }
    };

    public static void main(String[] args) throws Exception {
        if (null == args || args.length < 6) {
            System.err.println("Usage: src_dir date tar_dir file_market_neg tick_types future_markets [market_filter_file]");
            System.exit(1);
        }

        TickHistoryContextImpl ctx = new TickHistoryContextImpl();
        ctx.setMarketsWithNegativeTicks(new File(args[3]/*negative market file*/));
        if (args.length == 7) {
            ctx.setMarketFilter(new File(args[6]));
        }

        final File tickDir = findTickDir(new File(args[0]), args[1]);
        if (null == tickDir) {
            System.err.println(args[1] + " not found within: " + args[0]);
            System.exit(1);
        }

        final TickHistoryController controller = new TickHistoryController();
        final File workDir = new File(args[2]);
        FutureSymbolRetainer symbolRetainer = new FutureSymbolRetainer();
        symbolRetainer.setMarketsWithFutures(args[5]);
        controller.setArchives(getArchives(workDir, args[4]/*tick types*/, symbolRetainer, ctx));

        final DisruptTicker ticker = new DisruptTicker();
        ticker.setHistoryContext(ctx);

        try {
            ticker.afterPropertiesSet();
            controller.setTicker(ticker);
            controller.tickIntern(tickDir);
        } finally {
            ticker.destroy();
        }
    }

    private static final Comparator<File> TICK_DIR_COMP = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            final Matcher matcher = TICK_DIR.matcher("");
            final int num1 = getNum(o1, matcher);
            final int num2 = getNum(o2, matcher);

            return num1 - num2;
        }

        private int getNum(File o1, Matcher matcher) {
            if (!matcher.reset(o1.getName()).matches()) {
                throw new IllegalStateException("wrong file filter for tick folders");
            }
            return Integer.parseInt(matcher.group(1));
        }
    };

    static File findTickDir(File srcDir, final String yyyyMMdd) {
        final File[] ticksDirs = srcDir.listFiles(FF_TICK);
        if (null == ticksDirs || ticksDirs.length == 0) {
            return null;
        }

        final TreeSet<File> toSearch = new TreeSet<>(TICK_DIR_COMP);
        toSearch.addAll(Arrays.asList(ticksDirs));

        for (File ticksDir : toSearch) {
            final File dataDir = new File(ticksDir, "data");
            final File[] files = dataDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() && file.getName().equals(yyyyMMdd);
                }
            });

            if (null != files && files.length > 0) {
                return files[0];
            }
        }

        return null;
    }

    public static File getTickResultDir(File resultDir, TickType tickType) {
        return new File(resultDir, tickType.name().toLowerCase());
    }

    static Map<String, TickHistoryArchive> getArchives(File workDir, String tts,
            FutureSymbolRetainer symbolRetainer, TickHistoryContext ctx) {
        final HashMap<String, TickHistoryArchive> map = new HashMap<>();
        final String[] strs = tts.split(",");
        for (String str : strs) {
            final TickType tickType = TickType.valueOf(str);
            TickHistoryArchive archive = getTickArchive(workDir, tickType, symbolRetainer);
            archive.setContext(ctx);
            map.put(tickType.name(), archive);
        }
        return map;
    }

    private static TickHistoryArchive getTickArchive(File workDir, TickType tickType,
            FutureSymbolRetainer symbolRetainer) {
        final TickHistoryArchive archive;
        switch (tickType) {
            case TRADE:
                archive = new TickHistoryArchiveBA(tickType.name());
                archive.setSymbolRetainer(symbolRetainer);
                break;
            case ASK:
            case BID:
            case SYNTHETIC_TRADE:
                archive = new TickHistoryArchiveBA(tickType.name());
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + tickType);
        }

        archive.setWorkDir(getTickResultDir(workDir, tickType));
        return archive;
    }
}
