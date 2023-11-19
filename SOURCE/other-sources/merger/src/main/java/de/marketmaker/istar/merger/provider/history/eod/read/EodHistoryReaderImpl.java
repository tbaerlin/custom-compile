/*
 * EodPriceHistory.java
 *
 * Created on 12.12.12 10:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.merger.provider.history.eod.EodUtil;

/**
 * @author zzhao
 */
public class EodHistoryReaderImpl extends EodHistoryReaderBase {

    public EodHistoryReaderImpl(String unit, int capacity, Executor executorService) {
        super(HistoryUnit.valueOf(unit), capacity, executorService);
    }

    public EodHistoryReaderImpl(String unit) {
        super(HistoryUnit.valueOf(unit));
    }

    public EodHistoryReaderImpl(HistoryUnit unit) {
        super(unit);
    }

    @Override
    public Map<Integer, ByteBuffer> loadData(long quote,
            Collection<Integer> fields) throws IOException {
        final ByteBuffer data = loadData(quote);
        if (data.hasRemaining()) {
            return getData4Field(data, fields);
        }
        else {
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<Integer, ByteBuffer> loadData(long quote, Interval interval,
            Collection<Integer> fields) throws IOException {
        final ByteBuffer data = loadData(quote, interval);
        if (data.hasRemaining()) {
            return getData4Field(data, fields);
        }
        else {
            return Collections.emptyMap();
        }
    }

    private Map<Integer, ByteBuffer> getData4Field(ByteBuffer bb, Collection<Integer> fields) {
        if (bb.hasRemaining()) {
            final Map<Integer, ByteBuffer> map = new HashMap<>();
            final int fieldSize = bb.get();
            for (int i = 0; i < fieldSize; i++) {
                final int lengthField = bb.getInt();
                final byte b = EodUtil.decodeField(lengthField);
                final int len = EodUtil.decodeFieldLength(lengthField);
                final int field = HistoryUtil.fromUnsignedByte(b);
                if (fields.contains(field)) {
                    final ByteBuffer buf = bb.asReadOnlyBuffer();
                    buf.limit(bb.position() + len);
                    map.put(field, buf);
                    if (map.size() == fields.size()) {
                        break;
                    }
                }
                bb.position(bb.position() + len);
            }
            return map;
        }
        else {
            return Collections.emptyMap();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: eod_dir unit [out file]");
            System.exit(1);
        }

        final File file = new File(args[0]);
        final HistoryUnit unit;
        if (args.length == 1) {
            unit = HistoryUnit.fromExt(file);
        }
        else {
            unit = HistoryUnit.valueOf(args[1]);
        }

        try (final EodHistoryReaderImpl reader = new EodHistoryReaderImpl(unit)) {
            reader.setFile(args.length == 1 ? file : unit.getLatestFile(file));

            String line;
            final TimeTaker tt = new TimeTaker();
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                do {
                    System.out.print("quote,field [return to quit]: ");
                    line = br.readLine();
                    if (StringUtils.isNotBlank(line)) {
                        try {
                            final String[] split = StringUtils.split(line, ",");
                            tt.reset();
                            tt.start();

                            final long quote = Long.parseLong(split[0]);
                            final int field = Integer.parseInt(split[1]);
                            final Map<Integer, ByteBuffer> map = reader.loadData(quote, Arrays.asList(field));
                            final ByteBuffer data = map.get(field);
                            if (null == data || !data.hasRemaining()) {
                                System.out.println("no data found for " + quote + " and field " + field);
                                continue;
                            }
                            final Iterator<EodFieldPrice> it = new EodFieldPriceIterator(data);
                            if (args.length == 3) {
                                emitPrices2File(new File(args[2]), it, tt.toString());
                            }
                            else {
                                emitPrices(it, tt.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } while (StringUtils.isNotBlank(line));
            }
        }
    }

    private static final DecimalFormat NF = new DecimalFormat(".00");

    static {
        NF.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    }

    private static String formatPrice(EodFieldPrice pair) {
        return pair.getDate() + ":" + NF.format(pair.getPrice());
    }

    private static void emitPrices(Iterator<EodFieldPrice> it, String time) {
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        System.out.println("took: " + time);
    }

    private static void emitPrices2File(File file, Iterator<EodFieldPrice> it, String time)
            throws IOException {
        final List<String> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(formatPrice(it.next()));
        }

        Collections.reverse(list);
        FileUtils.writeLines(file, list);
        System.out.println("took: " + time);
    }
}
