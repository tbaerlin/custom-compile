/*
 * HuestExporterBoerseStg.java
 *
 * Created on 10.06.14 14:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.boersestg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

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

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;
import static de.marketmaker.istar.feed.tick.TickFiles.getMarketName;

/**
 * @author oflege
 */
public class HuestExporterBoerseStg extends TickFileProcessor {

    static final Set<String> MARKETS = new HashSet<>(Arrays.asList(
            "ETR", "EUS", "EEU", "XETF", "ETRI", "FFM", "FFMFO", "FFMST", "BLN", "DDF", "MCH", "MCHMM", "HNV", "HBG", "QTX", "TRADE"
    ));

    private final File outputDir;

    private final Map<String, Set<ByteString>> marketToVwdcodes = new HashMap<>();

    private final Map<ByteString, String> vwdcodeToName = new HashMap<>();

    public HuestExporterBoerseStg(File symbols, File outputDir) throws FileNotFoundException {
        this.outputDir = outputDir;
        this.numThreads = 4;
        try (Scanner sc = new Scanner(symbols)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                int p = line.indexOf(';');
                int p2 = line.indexOf(';', p + 1);
                String vwdcode = line.substring(0, p);
                String isin = line.substring(p + 1, p2);
                String currency = line.substring(p2 + 1);
                String market = vwdcode.substring(vwdcode.lastIndexOf('.') + 1);
                Set<ByteString> s = this.marketToVwdcodes.computeIfAbsent(market, k -> new HashSet<>());
                ByteString bs = new ByteString(vwdcode);
                s.add(bs);
                this.vwdcodeToName.put(bs, isin + ";" + market + ";" + currency);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        final File symbols = new File(args[0]);
        final File inputDir = new File(args[1]);
        final File outputDir = new File(args[2]);
        HuestExporterBoerseStg huest = new HuestExporterBoerseStg(symbols, outputDir);

        try (FileOutputStream fos = new FileOutputStream(new File(outputDir, "huest-header.csv.gz"));
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             PrintWriter pw = new PrintWriter(gzos)) {

            pw.println("#WKN/ISIN;market;currency;bid;bidVolume;bidSupplement;ask;askVolume;askSupplement;trade;tradeVolume;tradeSupplement;time");
        }

        huest.process(inputDir.listFiles(f -> f.isFile() && f.length() > 8
                        && MARKETS.contains(getMarketName(f))
                        && huest.marketToVwdcodes.containsKey(getMarketName(f))
                        && FileTickStore.canHandle(f))
        );
    }

    @Override
    protected Task createTask() {
        return new MyTask();
    }

    private class MyTask extends Task {
        @Override
        protected void process(File f, File out) throws IOException {
            new MyWriter(f, marketToVwdcodes.get(getMarketName(f))).process(out);
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
        }

        public void process(File out) throws IOException {
            if (this.symbols == null) {
                return;
            }
            super.process(out);
        }

        protected void process(FileChannel ch, PrintWriter pw) throws IOException {
            TickFileIndexReader.readEntries(ch, this.handler);

            //            pw.println("#WKN/ISIN;market;currency;bid;bidVolume;bidSupplement;ask;askVolume;askSupplement;trade;tradeVolume;tradeSupplement;time");

            mapFile(ch);

            for (FeedData item : items) {
                OrderedTickData td = ((OrderedFeedData) item).getOrderedTickData();
                try {
                    byte[] ticks = addTicks(td);
                    write(item, ticks, pw);
                } catch (Throwable e) {
                    this.logger.error(file.getName() + " process failed for " + item.getVwdcode(), e);
                }
            }
        }

        private void write(FeedData item, byte[] ticks, PrintWriter pw) {
            this.line.setLength(0);
            this.line.append(vwdcodeToName.get(item.getVendorkey().toVwdcode())).append(";");
            int mark = this.line.length();

            TickDecompressor td = new TickDecompressor(ticks, this.encoding);
            for (TickDecompressor.Element e : td) {
                if (!e.hasFlag(BID_ASK_TRADE)) {
                    continue;
                }
                BufferFieldData fd = e.getData();
                final int mdpsTime = fd.getInt();

                ExporterHelper.collectData(fd, data, supplements);
                this.line.setLength(mark);
                ExporterHelper.append(line, data[0], data[1], supplements[0]);
                ExporterHelper.append(line, data[2], data[3], supplements[0]);
                ExporterHelper.append(line, data[4], data[5], supplements[0]);
                this.line.append(MdpsFeedUtils.decodeLocalTime(mdpsTime));
                pw.println(this.line.toString());
            }
        }

        private byte[] addTicks(OrderedTickData td) {
            byte[] ticks = new byte[td.getLength()];
            fillFileTickStoreTicks(td.getStoreAddress(), ticks);
            return ticks;
        }

        protected void addItem(ByteString vwdcode, long address, int length) {
            if (this.symbols.contains(vwdcode)) {
                ExporterHelper.addItem(items, vwdcode, address, length, date);
            }
        }
    }
}
