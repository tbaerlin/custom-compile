/*
 * TickHistoryPersisterJMXClient.java
 *
 * Created on 26.07.12 14:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

import static de.marketmaker.istar.feed.history.HistoryUnit.Day;
import static de.marketmaker.istar.feed.history.HistoryUnit.Month;

/**
 * @author zzhao
 */
public class MinuteTickerImpl implements MinuteTicker, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final EnumMap<TickType, File> EMPTY = new EnumMap<>(TickType.class);

    protected TickHistoryContext historyContext;

    public void setHistoryContext(TickHistoryContext historyContext) {
        this.historyContext = historyContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.historyContext, "history context required");
    }

    public static void main(String[] args) throws Exception {
        if (null == args || args.length < 3) {
            System.err.println("Usage: tick_dir tick_types tar_dir");
            System.err.println("Options:" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_GENESIS + "=20040101" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_LENGTH_BITS + "=24" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_MARKETS_WITH_NEGATIVE_TICKS + "={file}" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_MARKET_FILTER + "={file}");
            System.exit(1);
        }

        final MinuteTickerImpl ticker = new MinuteTickerImpl();
        ticker.setHistoryContext(TickHistoryContextImpl.fromEnv());

        final File targetDir = new File(args[2]);
        final String[] split = args[1].split(",");
        final EnumMap<TickType, File> map = new EnumMap<>(TickType.class);
        for (String str : split) {
            map.put(TickType.valueOf(str.toUpperCase()), targetDir);
        }

        ticker.produceMinuteTicks(new File(args[0]), map);
    }


    @Override
    public EnumMap<TickType, File> produceMinuteTicks(File tickDir, Map<TickType, File> workDirs)
            throws IOException {
        if (null == tickDir || !tickDir.exists() || !tickDir.isDirectory()) {
            throw new IllegalArgumentException("invalid tick dir: " +
                    (null == tickDir ? "null" : tickDir.getAbsolutePath()));
        }

        if (workDirs.isEmpty()) {
            throw new IllegalStateException("no tick types are given");
        }
        ensureDateOrder(tickDir, workDirs);
        return produce(tickDir, workDirs);
    }

    private void ensureDateOrder(File tickDir, Map<TickType, File> workDirs) {
        for (Map.Entry<TickType, File> entry : workDirs.entrySet()) {
            final List<File> dayFiles = Day.getRelevantFiles(entry.getValue());
            if (!CollectionUtils.isEmpty(dayFiles)) {
                throw new IllegalStateException("non-processed day file exists, " +
                        " resolve problem first then continue");
            }
            final File latestFile = Month.getLatestFile(entry.getValue());
            if (null == latestFile) {
                return;
            }
            final LocalDate toDate = DateUtil.yyyyMmDdToLocalDate(Month.getToDate(latestFile));
            final LocalDate date = DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(tickDir.getName()));

            if (!date.isAfter(toDate)) {
                throw new IllegalStateException("invalid history order: "
                        + "existing: " + latestFile.getName()
                        + ", tick dir: " + tickDir.getAbsolutePath());
            }
            else {
                final LocalDate expectedDate = toDate.plusDays(1);
                final boolean jumpDay = Boolean.parseBoolean(
                        System.getProperty("tick.history.gap", "false"));
                if (!expectedDate.isEqual(date) && !jumpDay) {
                    throw new IllegalStateException("won't produce tick history because of gap: "
                            + latestFile.getName() + ", tick dir: " + tickDir.getName());
                }
            }
        }
    }

    private EnumMap<TickType, File> produce(File dir, Map<TickType, File> workDirs)
            throws IOException {
        final TreeSet<File> tickFiles = new TreeSet<>(Arrays.asList(dir.listFiles(
                this.historyContext.getMarketFileFilter())));
        if (tickFiles.isEmpty()) {
            return EMPTY;
        }

        final LocalDate today = DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(dir.getName()));
        final int days = HistoryUtil.daysFromBegin(this.historyContext.getGenesis(), today);
        if (!HistoryUtil.isUnsignedShort(days)) {
            throw new IllegalStateException("cannot encode days in unsigned short: " + days);
        }

        final TimeTaker tt = new TimeTaker();
        final EnumMap<TickType, File> files = getTickResultFiles(workDirs, dir.getName());
        final EnumMap<TickType, HistoryWriter<ByteString>> writers = new EnumMap<>(TickType.class);
        try {
            for (Map.Entry<TickType, File> entry : files.entrySet()) {
                writers.put(entry.getKey(), new HistoryWriter<>(entry.getValue(),
                        this.historyContext.getOffsetLengthCoder(), ByteString.class));
            }

            produce(TickDirectory.open(dir), days, tickFiles, writers);
            this.logger.info("<produce> from: {}, took: {}", dir.getAbsolutePath(), tt);
        } finally {
            for (HistoryWriter<ByteString> writer : writers.values()) {
                IoUtils.close(writer);
            }
        }

        return files;
    }

    protected void produce(TickDirectory tickDirectory, int days, TreeSet<File> tickFiles,
            EnumMap<TickType, HistoryWriter<ByteString>> writers) {
        final EnumSet<TickType> tickTypes = EnumSet.copyOf(writers.keySet());

        for (File file : tickFiles) {
            DayTickExtractor extractor = null;
            try {
                extractor = new DayTickExtractor(tickDirectory.getTickFileReader(file), tickTypes, this.historyContext);
                extractor.readVendorKeys();
                for (DayTick dayTick : extractor) {
                    if (null == dayTick) {
                        continue;
                    }
                    for (TickType tickType : tickTypes) {
                        final AbstractTickRecord.TickItem tickItem = dayTick.getTickItem(tickType);
                        if (null == tickItem || tickItem.getData().length == 0) {
                            continue;
                        }
                        final byte[] data = this.historyContext.postProcessTickData(tickType,
                                tickItem.getData());
                        if (!HistoryUtil.isUnsignedShort(data.length)) {
                            this.logger.error("<produce> tick data out of range {}", dayTick.getSymbol());
                            continue;
                        }
                        final ByteBuffer bb = ByteBuffer.allocate(6 + data.length);
                        bb.putShort((short) days);
                        bb.putShort((short) tickItem.getNumTicks());
                        bb.putShort((short) data.length);
                        bb.put(data);
                        writers.get(tickType).withEntry(dayTick.getSymbol(), bb.array());
                    }
                }
            } catch (Exception e) {
                this.logger.error("<produce> cannot extract minute ticks from: "
                        + file.getAbsolutePath(), e);
            } finally {
                IoUtils.close(extractor);
            }
        }
    }

    private EnumMap<TickType, File> getTickResultFiles(Map<TickType, File> workDirs, String date) {
        final EnumMap<TickType, File> ret = new EnumMap<>(TickType.class);
        for (Map.Entry<TickType, File> entry : workDirs.entrySet()) {
            ret.put(entry.getKey(), Day.createFile(entry.getKey().name(), entry.getValue(), date));
        }
        return ret;
    }
}
