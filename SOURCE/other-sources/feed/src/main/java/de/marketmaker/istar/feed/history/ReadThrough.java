/*
 * Foo.java
 *
 * Created on 28.08.12 10:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;

/**
 * @author zzhao
 */
public class ReadThrough {

    private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocateDirect(128 * 1024 * 1024);

    private static final EnumSet<TickType> TYPES_TRADE = EnumSet.of(TickType.TRADE);

    public static void main(String[] args) throws IOException {
        if (null == args || args.length != 1) {
            System.err.println("Usage: [tick dir]");
            System.exit(1);
        }

        readThrough(new File(args[0]));
    }

    private static void readThrough(File dir) throws IOException {
        final TimeTaker tt = new TimeTaker();

        TickDirectory tickDirectory = TickDirectory.open(dir);

        final LocalDate today = DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(dir.getName()));
        TickHistoryContextImpl ctx = new TickHistoryContextImpl();
        final int days = HistoryUtil.daysFromBegin(ctx.getGenesis(), today);

        SortedSet<File> files = new TreeSet<>(Arrays.asList(dir.listFiles(ctx.getMarketFileFilter())));
        WRITE_BUFFER.clear();
        for (File file : files) {
            DayTickExtractor extractor = null;
            try {
                extractor = new DayTickExtractor(tickDirectory.getTickFileReader(file), TYPES_TRADE, ctx);
                extractor.readVendorKeys();
                for (DayTick dayTick : extractor) {
                    if (null == dayTick) {
                        continue;
                    }
                    final AbstractTickRecord.TickItem tickItem = dayTick.getTickItem(TickType.TRADE);
                    if (null == tickItem) {
                        continue;
                    }
                    final byte[] data = tickItem.getData();
                    if (WRITE_BUFFER.remaining() < 6 + data.length) {
                        WRITE_BUFFER.clear();
                    }

                    WRITE_BUFFER.putShort((short) days);
                    WRITE_BUFFER.putShort((short) tickItem.getNumTicks());
                    WRITE_BUFFER.putShort((short) data.length);
                    WRITE_BUFFER.put(data);
                }
            } catch (Exception e) {
                System.err.println("<produce> cannot extract minute ticks from: " + e.getMessage());
            } finally {
                if (null != extractor) {
                    extractor.close();
                }
            }
        }

        System.out.println("finished in: " + tt);
    }
}
