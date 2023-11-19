/*
 * TickExporter.java
 *
 * Created on 14.03.12 07:25
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.ordered.tick.DefaultIndexHandler;
import de.marketmaker.istar.feed.ordered.tick.FileTickStore;
import de.marketmaker.istar.feed.ordered.tick.TickFileIndexReader;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * Aggregates ticks from tick files per day and writes the aggregated ticks for each symbol
 * into a csv file or to stdout.
 * @author oflege
 */
public class TickOhlcWriter {

    private static final Duration DURATION_DAY = new Duration(DateTimeConstants.MILLIS_PER_DAY);

    private final Pattern filter;

    private static class Item {
        final long address;

        final int length;

        private Item(long address, int length) {
            this.address = address;
            this.length = length;
        }
    }


    private static TickType[] TICK_TYPES = new TickType[]{TickType.TRADE, TickType.BID, TickType.ASK};

    private static final Pattern TICK_FILE = Pattern.compile("(\\w+)-(20\\d{6}).td[3z]");

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || "-h".equals(args[0])) {
            System.err.println("Usage: TickOhlcWriter [options] files");
            System.err.println("options:");
            System.err.println("-p             : use only positive prices");
            System.err.println("-f regex       : export data only for vwdcodes that match regex");
            System.err.println("-i infile      : export data only for vwdcodes in infile (one vwdcode per line)");
            System.err.println("-o outfile     : export to file named <outfile>, stdout otherwise");
            System.err.println("-t type[,type] : export tick types, dafault TRADE,BID,ASK");
            return;
        }

        boolean onlyPositivePrices = false;
        Set<ByteString> vwdcodes = null;
        Pattern filter = null;

        String outfile = null;
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-p".equals(args[n])) {
                onlyPositivePrices = true;
            }
            else if ("-t".equals(args[n])) {
                final String[] types = args[++n].split(",");
                TICK_TYPES = new TickType[types.length];
                for (int i = 0; i < types.length; i++) {
                    TICK_TYPES[i] = TickType.valueOf(types[i]);
                }
            }
            else if ("-o".equals(args[n])) {
                outfile = args[++n];
            }
            else if ("-i".equals(args[n])) {
                vwdcodes = readVwdcodes(new File(args[++n]));
            }
            else if ("-f".equals(args[n])) {
                filter = Pattern.compile(args[++n]);
            }
            n++;
        }

        TickOhlcWriter exporter = new TickOhlcWriter(vwdcodes, filter);

        while (n < args.length) {
            final File file = new File(args[n++]);
            if (TICK_FILE.matcher(file.getName()).matches()) {
                exporter.export(file, outfile, onlyPositivePrices);
            }
            else {
                // file is supposed to to be a textfile that contains the name of each tickfile to be read
                try (Scanner sc = new Scanner(file)) {
                    while (sc.hasNextLine()) {
                        File tickFile = new File(sc.nextLine());
                        final Matcher matcher = TICK_FILE.matcher(tickFile.getName());
                        if (!tickFile.canRead() || !matcher.matches()) {
                            continue;
                        }
                        System.err.println(tickFile.getName());
                        exporter.export(tickFile,
                                outfile.replace("market", matcher.group(1)).replace("yyyymmdd", matcher.group(2)),
                                onlyPositivePrices);
                    }
                }
            }
        }
    }

    private static Set<ByteString> readVwdcodes(File source) throws IOException {
        Set<ByteString> vwdcodes = new HashSet<>();
        try (Scanner sc = new Scanner(source, "UTF-8")) {
            while (sc.hasNextLine()) {
                final String symbol = sc.nextLine().trim();
                if (VendorkeyVwd.KEY_PATTERN.matcher(symbol).matches()) {
                    vwdcodes.add(new ByteString(symbol));
                }
                else if (StringUtils.hasText(symbol) && !symbol.startsWith("#")) {
                    System.err.println("Invalid symbol: '" + symbol + "'");
                }
            }
        }
        return vwdcodes;
    }

    private final Map<ByteString, Item> map = new TreeMap<>();

    private final Set<ByteString> vwdcodes;

    private final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    public TickOhlcWriter(Set<ByteString> vwdcodes, Pattern filter) {
        this.vwdcodes = vwdcodes;
        this.filter = filter;
        df.applyLocalizedPattern("0.00########");
    }

    private void add(ByteString vendorkey, Item item) {
        if (isAcceptable(vendorkey)) {
            this.map.put(vendorkey, item);
        }
    }

    private boolean isAcceptable(ByteString vendorkey) {
        if (this.vwdcodes != null) {
            return this.vwdcodes.contains(vendorkey);
        }
        return this.filter == null || this.filter.matcher(vendorkey.toString()).matches();
    }

    private void export(File file, String outfile, boolean onlyPositivePrices) throws Exception {
        this.map.clear();

        final FileChannel fc = new RandomAccessFile(file, "r").getChannel();

        readIndex(file, fc);

        int day = Integer.parseInt(file.getName().substring(file.getName().indexOf('-') + 1,
                file.getName().indexOf('.')));
        StringBuilder sb = new StringBuilder(100);

        PrintWriter pw = createPrintWriter(outfile);

        sb.append("VWDCODE");
        for (TickType tickType : TICK_TYPES) {
            sb.append(";").append(tickType.name()).append("-OPEN");
            sb.append(";").append(tickType.name()).append("-HIGH");
            sb.append(";").append(tickType.name()).append("-LOW");
            sb.append(";").append(tickType.name()).append("-CLOSE");
        }
        pw.println(sb.toString());

        Interval interval = DateUtil.yyyyMmDdToLocalDate(day).toInterval();
        TickRecordImpl tr = new TickRecordImpl();

        final AbstractTickRecord.TickItem.Encoding encoding = TickFiles.getItemType(file);

        for (Map.Entry<ByteString, Item> entry : map.entrySet()) {
            ByteString key = entry.getKey();
            Item item = entry.getValue();

            byte[] ticks = new byte[item.length];

            FileTickStore.fillData(fc, item.address, ByteBuffer.wrap(ticks));

            tr.setAggregateOnlyPositivePrices(onlyPositivePrices);
            tr.add(day, ticks, encoding);
            sb.setLength(0);
            sb.append(key);
            for (TickType tt : TICK_TYPES) {
                AggregatedTickRecord at = tr.aggregate(DURATION_DAY, tt);
                Iterator<DataWithInterval<AggregatedTick>> it = at.getTimeseries(interval).iterator();
                if (it.hasNext()) {
                    AggregatedTick data = it.next().getData();
                    sb.append(";").append(format(data.getOpen()));
                    sb.append(";").append(format(data.getHigh()));
                    sb.append(";").append(format(data.getLow()));
                    sb.append(";").append(format(data.getClose()));
                }
                else {
                    sb.append(";;;;");
                }
            }
            pw.println(sb.toString());
        }
        pw.close();
        fc.close();
    }

    private void readIndex(File file, FileChannel fc) throws IOException {
        if (FileTickStore.canHandle(file)) {
            TickFileIndexReader.readEntries(fc, new DefaultIndexHandler(file) {
                @Override
                protected void doHandle(ByteString vwdcode, long position, int length) {
                    add(vwdcode, new Item(position, length));
                }
            });
        }
    }

    private PrintWriter createPrintWriter(String out) throws FileNotFoundException {
        if (out == null) {
            return new PrintWriter(System.out);
        }
        final File outFile = new File(out);
        return new PrintWriter(new FileOutputStream(outFile));
    }

    private String format(final long value) {
        return df.format(PriceCoder.decode(value));
    }
}
