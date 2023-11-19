/*
 * TickHistoryReader.java
 *
 * Created on 15.05.2014 16:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInterval;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.tick.AggregatedTickDecoder;
import de.marketmaker.istar.feed.tick.RawAggregatedTick;

/**
 * @author zzhao
 */
public class TickHistoryReaderCli {

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: genesis history_dir history_unit");
            System.exit(1);
        }
        LocalDate genesis = HistoryUtil.DTF_DAY.parseLocalDate(args[0]);
        final File dir = new File(args[1]);
        final HistoryUnit unit = HistoryUnit.valueOf(args[2]);
        final File file = unit.getLatestFile(dir);
        try (
                final HistoryReader<ByteString> reader = new HistoryReader<>(ByteString.class,
                        unit, false);
                final BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        ) {
            final TickType tickType = TickType.valueOf(HistoryUnit.getContentType(file));
            reader.setFile(file);
            final ByteBuffer bb = ByteBuffer.allocate(reader.getOffsetLengthCoder().maxLength());
            String line;
            String symbol;
            boolean outputAll;
            do {
                System.out.print("symbol[710000.mch / empty to quit]: ");
                line = br.readLine();
                if (line.endsWith("*")) {
                    symbol = line.substring(0, line.lastIndexOf("*"));
                    outputAll = true;
                }
                else {
                    symbol = line;
                    outputAll = false;
                }
                if (StringUtils.isNotBlank(symbol)) {
                    if ("keys".equals(symbol)) {
                        reader.emitKeys(System.out, outputAll);
                    }
                    else {
                        final TimeTaker tt = new TimeTaker();
                        final ByteString key = HistoryUtil.getKey(new ByteString(symbol.toUpperCase()));
                        bb.clear();
                        reader.loadData(key, bb);
                        bb.flip();
                        System.out.println(symbol + "#" + tickType + ", took: " + tt);
                        emitTickHistory(genesis, tickType, outputAll, bb, true);
                    }
                }
            } while (StringUtils.isNotBlank(symbol));
        }
    }

    static void emitTickHistory(LocalDate genesis, TickType tickType, boolean outputAll,
            ByteBuffer bb, boolean output) {
        while (bb.hasRemaining()) {
            final int days = HistoryUtil.fromUnsignedShort(bb.getShort());
            final int tickNum = bb.getShort();//tick num
            final int len = HistoryUtil.fromUnsignedShort(bb.getShort());
            final byte[] bytes = new byte[len];
            bb.get(bytes);

            if (output) {
                System.out.print(HistoryUtil.daysFromBegin2Date(genesis, days) + ", " + tickNum
                        + " ticks, " + len + " bytes");
            }
            emit(outputAll, fromTickEntry(ByteBuffer.wrap(EntryFactory.decompress(bytes)),
                    HistoryUtil.daysFromBegin2Date(genesis, days), tickType), output);
            //                    fromTickEntry(ByteBuffer.wrap(EntryFactory.decompress(bytes)),
            //                            HistoryUtil.daysFromBegin2Date(days), tickType);
        }
    }

    private static void emit(boolean outputAll, List<AggregatedTickImpl> ticks, boolean output) {
        final StringBuilder sb = new StringBuilder();
        if (outputAll || ticks.size() <= 3) {
            for (int i = 0; i < ticks.size(); i++) {
                appendTickAndIndex(sb, i, ticks);
            }
        }
        else {
            appendTickAndIndex(sb, 0, ticks);
            appendTickAndIndex(sb,
                    ticks.size() % 2 == 0 ? ticks.size() / 2 : ticks.size() / 2 + 1, ticks);
            appendTickAndIndex(sb, ticks.size() - 1, ticks);
        }

        if (output) {
            System.out.println(sb.toString());
        }
    }

    private static void appendTickAndIndex(StringBuilder sb, int idx,
            List<AggregatedTickImpl> ticks) {
        sb.append("\n").append(idx + 1).append("# ");
        appendTick(sb, ticks.get(idx));
    }

    public static List<AggregatedTickImpl> fromTickEntry(ByteBuffer bb, int date,
            TickType tickType) {
        final AggregatedTickDecoder decoder = new AggregatedTickDecoder(bb);
        final DateTime dateTime = DateUtil.yyyymmddToDateTime(date);
        final ArrayList<AggregatedTickImpl> list = new ArrayList<>();
        for (RawAggregatedTick tick : decoder) {
            final AggregatedTickImpl at = new AggregatedTickImpl(
                    toInterval(dateTime.plusSeconds(tick.getTime())),
                    PriceCoder.decode(tick.getOpen()), PriceCoder.decode(tick.getHigh()),
                    PriceCoder.decode(tick.getLow()), PriceCoder.decode(tick.getClose()),
                    tick.getVolume(), tick.getNumberOfAggregatedTicks(), tickType);
            list.add(at);
        }

        return list;
    }

    private static ReadableInterval toInterval(DateTime dateTime) {
        return new Interval(dateTime, dateTime.plusMinutes(1));
    }

    private static String toIntervalString(ReadableInterval interval) {
        return HistoryUtil.DTF_MINUTE.print(interval.getStart()) + " ~ "
                + HistoryUtil.DTF_MINUTE.print(interval.getEnd());
    }

    private static void appendTick(StringBuilder sb, AggregatedTickImpl at) {
        sb.append(toIntervalString(at.getInterval()))
                .append(", O: ").append(at.getOpen()).append(", H: ")
                .append(at.getHigh()).append(", L: ").append(at.getLow()).append(", C: ")
                .append(at.getClose()).append(", V: ").append(at.getVolume()).append(", NoAT: ")
                .append(at.getNumberOfAggregatedTicks());
    }
}
