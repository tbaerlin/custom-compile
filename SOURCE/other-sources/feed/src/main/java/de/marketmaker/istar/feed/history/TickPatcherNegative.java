/*
 * Csv2History.java
 *
 * Created on 27.09.12 15:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;

import org.joda.time.LocalDate;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

/**
 * @author zzhao
 */
public class TickPatcherNegative extends TickHistoryArchive
        implements HistoryDataSource<ByteString> {

    private final TickDirectory tickDir;

    private final String[] markets;

    private final TickHistoryContext context;

    public TickPatcherNegative(File tickDir, TickType tickType, String[] markets,
            TickHistoryContext context) throws IOException {
        super(tickType.name());
        this.tickDir = TickDirectory.open(tickDir);
        this.markets = markets;
        this.context = context;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.err.println("Usage: target_dir src_dir yyyyMMdd-yyyyMMdd tick_type market_1 [market_2] ...");
            System.exit(1);
        }

        final TickType tickType = TickType.valueOf(args[3]);
        final File targetDir = new File(new File(args[0]), tickType.name().toLowerCase());
        final File srcDir = new File(args[1]);

        final LocalDate fromDate = DateUtil.yyyyMmDdToLocalDate(
                Integer.parseInt(args[2].substring(0, args[2].indexOf("-"))));
        final LocalDate toDate = DateUtil.yyyyMmDdToLocalDate(
                Integer.parseInt(args[2].substring(args[2].indexOf("-") + 1)));

        final String[] markets = Arrays.copyOfRange(args, 4, args.length);
        Arrays.sort(markets);

        LocalDate date = fromDate;
        while (!date.isAfter(toDate)) {
            final int yyyyMMdd = DateUtil.toYyyyMmDd(date);
            final File tickDir = findTickDir(srcDir, "" + yyyyMMdd);
            if (tickDir != null) {
                final TickPatcherNegative tickPatcher = new TickPatcherNegative(tickDir,
                        tickType, markets, new TickHistoryContextImpl());
                tickPatcher.setWorkDir(targetDir);

                final File changeFile = HistoryUtil.createChangeFile(ByteString.class,
                    TickHistoryContextImpl.LENGTH_BITS, tickPatcher.getContentType(), targetDir,
                    tickPatcher, yyyyMMdd, yyyyMMdd);
                tickPatcher.update(HistoryUnit.Change, changeFile);
            }
            else {
                LoggerFactory.getLogger(TickPatcherNegative.class)
                        .warn("<main> tick dir: " + yyyyMMdd + " not found under: " + srcDir.getAbsolutePath());
            }
            date = date.plusDays(1);
        }
    }

    private static File findTickDir(File srcDir, String yyyyMMdd) throws IOException {
        try (final DirectoryStream<Path> ds = Files.newDirectoryStream(srcDir.toPath(),
                new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        return Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)
                                && !entry.toFile().getName().contains("+");
                    }
                }
        )) {
            for (Path path : ds) {
                if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                    if (path.toFile().getName().equals(yyyyMMdd)) {
                        final Path fxx = path.resolve("FXX-" + yyyyMMdd + ".tdz");
                        if (Files.exists(fxx) && Files.isRegularFile(fxx)) {
                            return path.toFile();
                        }
                    }
                    else {
                        final File file = findTickDir(path.toFile(), yyyyMMdd);
                        if (null != file) {
                            return file;
                        }
                    }
                }
            }
            return null;
        }
    }

    private void writeDayTicks(HistoryWriter<ByteString> writer, String market) throws IOException {
        DayTickExtractorNegative dte = null;
        try {
            dte = new DayTickExtractorNegative(this.tickDir.getTickFileReader(market), tickType);
            dte.readVendorKeys();
            final int days = HistoryUtil.daysFromBegin(TickHistoryContextImpl.GENESIS,
                    DateUtil.yyyyMmDdToLocalDate(dte.getDate()));
            if (!HistoryUtil.isUnsignedShort(days)) {
                throw new IllegalStateException("date coding overflow: " + dte.getDate());
            }

            for (DayTick dayTick : dte) {
                if (null != dayTick) {
                    final AbstractTickRecord.TickItem tickItem = dayTick.getTickItem(tickType);
                    if (null == tickItem || tickItem.getData().length == 0) {
                        continue;
                    }

                    final byte[] data = context.postProcessTickData(tickType, tickItem.getData());
                    final int dataLen = data.length;
                    if (!HistoryUtil.isUnsignedShort(dataLen)) {
                        logger.error("<writeDayTicks> data out of range {}, length {}",
                                dayTick.getSymbol(), dataLen);
                        continue;
                    }
                    final ByteBuffer bb = ByteBuffer.allocate(6 + dataLen);
                    bb.putShort((short) days);
                    bb.putShort((short) tickItem.getNumTicks());
                    bb.putShort((short) dataLen);
                    bb.put(data);
                    writer.withEntry(dayTick.getSymbol(), bb.array());
                }
            }
        } catch (Exception e) {
            logger.error("<writeDayTicks> cannot write day ticks for: {}", market, e);
        } finally {
            IoUtils.close(dte);
        }
    }

    @Override
    public void transfer(HistoryWriter<ByteString> writer) throws IOException {
        for (String market : markets) {
            writeDayTicks(writer, market);
        }
    }
}
