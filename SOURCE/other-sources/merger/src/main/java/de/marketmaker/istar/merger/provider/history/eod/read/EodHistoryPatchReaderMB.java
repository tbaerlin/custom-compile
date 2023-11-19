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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.history.HistoryUnit;

/**
 * @author zzhao
 */
public class EodHistoryPatchReaderMB extends EodHistoryReaderBaseMB {

    public EodHistoryPatchReaderMB() {
        super(HistoryUnit.Patch);
    }

    @Override
    public Map<Integer, ByteBuffer> loadData(long quote,
            Collection<Integer> fields) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Map<Integer, ByteBuffer> loadData(long quote, Interval interval,
            Collection<Integer> fields) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: eod_dir [out_file]");
            System.exit(1);
        }

        final HistoryUnit unit = HistoryUnit.Patch;
        try (final EodHistoryPatchReaderMB reader = new EodHistoryPatchReaderMB()) {
            reader.setFile(unit.getLatestFile(new File(args[0])));

            String line;
            final TimeTaker tt = new TimeTaker();
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                do {
                    System.out.print("quote[return to quit]: ");
                    line = br.readLine();
                    if (StringUtils.isNotBlank(line)) {
                        try {
                            tt.reset();
                            tt.start();

                            final long quote = Long.parseLong(line);
                            final ByteBuffer data = reader.loadData(quote);
                            if (!data.hasRemaining()) {
                                System.out.println("no patch data found for " + quote);
                                continue;
                            }
                            final Iterator<EodPrice> it = new EodPriceIterator(data);
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

    private static String formatPrice(EodPrice pair) {
        return pair.toString(NF);
    }

    private static void emitPrices(Iterator<EodPrice> it, String time) {
        while (it.hasNext()) {
            System.out.println(it.next());
        }
        System.out.println("took: " + time);
    }

    private static void emitPrices2File(File file, Iterator<EodPrice> it, String time)
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
