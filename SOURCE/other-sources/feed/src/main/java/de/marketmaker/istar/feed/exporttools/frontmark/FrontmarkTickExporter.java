/*
 * FrontmarkTickExporter.java
 *
 * Created on 28.01.2015 11:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.frontmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.joda.time.LocalTime;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.exporttools.ExporterBaseWriter;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.tick.DefaultIndexHandler;
import de.marketmaker.istar.feed.ordered.tick.FileTickStore;
import de.marketmaker.istar.feed.ordered.tick.OrderedTickData;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import de.marketmaker.istar.feed.ordered.tick.TickFile;
import de.marketmaker.istar.feed.ordered.tick.TickFileIndexReader;
import de.marketmaker.istar.feed.ordered.tick.TickFileProcessor;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory.RT_TICKS;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;
import static de.marketmaker.istar.feed.vwd.VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ;

/**
 * @author mwilke
 */
public class FrontmarkTickExporter extends TickFileProcessor {

    private final File outputDir;

    private static final Map<String, Set<ByteString>> marketToVwdcodes = new HashMap<>();


    private final Map<ByteString, String> vwdcodeToName = new HashMap<>();

    public FrontmarkTickExporter(File symbols, File outputDir) throws FileNotFoundException {
        this.outputDir = outputDir;
        this.numThreads = 4;
        try (Scanner sc = new Scanner(symbols)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] fields = line.split(";");
                String vwdcode = fields[0];
                String isin = fields[1];
                String wkn = fields[2];
                String name = fields[3];
                String type = fields[4];
                String currency = fields.length == 6 ? fields[5] : "";

                String market = getMarket(vwdcode);
                Set<ByteString> s = marketToVwdcodes.computeIfAbsent(market, k -> new HashSet<>());
                ByteString bs = new ByteString(vwdcode);
                s.add(bs);

                vwdcodeToName.put(bs, vwdcode + ";" + isin + ";" + wkn + ";" + name + ";" + type + ";" + currency);
            }
        }
    }

    private String getMarket(String vwdcode) {
        final int start = vwdcode.indexOf('.', 1) + 1;
        final int end = vwdcode.indexOf('.', start + 1);
        return vwdcode.substring(start, (end == -1) ? vwdcode.length() : end);

    }


    public static void main(String[] args) throws Exception {
        final File symbols = new File(args[0]);
        final File inputDir = new File(args[1]);
        final File outputDir = new File(args[2]);
        FrontmarkTickExporter exporter = new FrontmarkTickExporter(symbols, outputDir);

        exporter.process(inputDir.listFiles(f -> f.isFile() && f.length() > 8
                && marketToVwdcodes.containsKey(getMarketName(f))
                && FileTickStore.canHandle(f)));
    }

    private static String getMarketName(File f) {
        return f.getName().substring(0, f.getName().lastIndexOf('-'));
    }

    @Override
    protected TickFileProcessor.Task createTask() {
        return new MyTask();
    }

    private class MyTask extends TickFileProcessor.Task {
        @Override
        protected void process(File f, File out) throws IOException {
            final String marketName = getMarketName(f);
            final MyWriter writer = new MyWriter(f, marketToVwdcodes.get(marketName));
            writer.process(out);
        }

        @Override
        protected File getOutFile(File f) {
            return new File(outputDir, f.getName().replace(".tdz", ".csv.gz").replace(".td3", ".csv.gz"));
        }
    }

    private class MyWriter extends ExporterBaseWriter {

        private static final int BID_ASK_TRADE
                = FeedUpdateFlags.FLAG_WITH_TRADE
                + FeedUpdateFlags.FLAG_WITH_BID
                + FeedUpdateFlags.FLAG_WITH_ASK;

        private final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

        private final AbstractTickRecord.TickItem.Encoding encoding;

        private final DefaultIndexHandler handler;

        private final Set<ByteString> symbols;

        private final List<FeedData> items = new ArrayList<>(8192);

        private final int date;

        private final StringBuilder line = new StringBuilder();

        private final long[] data = new long[6];

        private final String[] supplements = new String[3];

        public MyWriter(File file, Set<ByteString> symbols) {
            super(ByteOrder.LITTLE_ENDIAN, file);
            this.encoding = file.getName().endsWith(".td3") ? TICK3 : TICKZ;
            this.symbols = symbols;
            this.handler = new DefaultIndexHandler(file) {
                @Override
                protected void doHandle(ByteString vwdcode, long position, int length) {
                    addItem(vwdcode, position, length);
                }
            };
            this.date = TickFile.getDay(this.file);

            this.df.applyLocalizedPattern("0.####");
        }

        public void process(File out) throws IOException {
            if (this.symbols == null) {
                return;
            }

            super.process(out);
        }

        protected void process(FileChannel ch, PrintWriter pw) throws IOException {
            TickFileIndexReader.readEntries(ch, this.handler);
            pw.println("vwd-Symbol;ISIN;WKN;WP_Name;SecType_MDP;Waehrung;Geld;Geld_Umsatz;Brief;Brief_Umsatz;Bezahlt;Bezahlt_Umsatz;Zeit;");

            mapFile(ch);

            for (FeedData item : items) {
                OrderedTickData td = ((OrderedFeedData) item).getOrderedTickData();
                try {
                    byte[] ticks = addTicks(td);
                    Map<LocalTime, String> ticksByTime = decodeTickData(item, ticks);
                    writeSorted(ticksByTime, pw);
                } catch (Throwable e) {
                    this.logger.error(file.getName() + " process failed for " + item.getVwdcode(), e);
                }
            }
        }

        private Map<LocalTime, String> decodeTickData(FeedData item, byte[] ticks) {
            this.line.setLength(0);
            this.line.append(vwdcodeToName.get(item.getVendorkey().toVwdcode())).append(";");
            int mark = this.line.length();

            TickDecompressor td = new TickDecompressor(ticks, this.encoding);
            Map<LocalTime, String> ticksByTime = new HashMap<>();
            for (TickDecompressor.Element e : td) {
                if (!e.hasFlag(BID_ASK_TRADE)) {
                    continue;
                }
                Arrays.fill(data, Long.MIN_VALUE);
                Arrays.fill(supplements, null);

                BufferFieldData fd = e.getData();
                final int mdpsTime = fd.getInt();
                for (int oid = fd.readNext(); oid > 0 && oid <= ORDER_ADF_BEZAHLT_UMSATZ; oid = fd.readNext()) {
                    switch (oid) {
                        case VwdFieldOrder.ORDER_ADF_GELD:
                            data[0] = MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
                            break;
                        case VwdFieldOrder.ORDER_ADF_GELD_UMSATZ:
                            data[1] = fd.getUnsignedInt();
                            break;
                        case VwdFieldOrder.ORDER_ADF_BRIEF:
                            data[2] = MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
                            break;
                        case VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ:
                            data[3] = fd.getUnsignedInt();
                            break;
                        case VwdFieldOrder.ORDER_ADF_BEZAHLT:
                            data[4] = MdpsFeedUtils.encodePrice(fd.getInt(), fd.getByte());
                            break;
                        case VwdFieldOrder.ORDER_ADF_BEZAHLT_UMSATZ:
                            data[5] = fd.getUnsignedInt();
                            break;
                        default:
                            fd.skipCurrent();
                    }
                }
                this.line.setLength(mark);
                append(data[0], data[1], supplements[0]);
                append(data[2], data[3], supplements[0]);
                append(data[4], data[5], supplements[0]);
                final LocalTime time = MdpsFeedUtils.decodeLocalTime(mdpsTime);
                ticksByTime.put(time, this.line.append(time).toString());
            }
            return ticksByTime;
        }

        private void writeSorted(Map<LocalTime, String> ticksByTime, PrintWriter pw) {
            LocalTime[] mdpsTimes = new LocalTime[ticksByTime.size()];
            mdpsTimes = ticksByTime.keySet().toArray(mdpsTimes);
            Arrays.sort(mdpsTimes);
            for (int i = 0; i < mdpsTimes.length; i++) {
                LocalTime time = mdpsTimes[i];
                pw.println(ticksByTime.get(time));
            }
        }

        private void append(long price, long volume, String supplement) {
            if (price != Long.MIN_VALUE) {
                this.line.append(this.df.format(MdpsFeedUtils.decodePrice(price)));
                this.line.append(";");
                if (volume != Long.MIN_VALUE) {
                    this.line.append(volume);
                }
                this.line.append(";");
            }
            else {
                this.line.append(";;");
            }
        }

        private byte[] addTicks(OrderedTickData td) {
            byte[] ticks = new byte[td.getLength()];
            fillFileTickStoreTicks(td.getStoreAddress(), ticks);
            return ticks;
        }

        protected void addItem(ByteString vwdcode, long address, int length) {
            if (this.symbols.contains(vwdcode)) {
                FeedData fd = RT_TICKS.create(VendorkeyVwd.getInstance(vwdcode, 1), null);
                OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
                td.setDate(this.date);
                td.setStoreAddress(address);
                td.setLength(length);

                this.items.add(fd);
            }
        }
    }

}
