/*
 * QtxExporter.java
 *
 * Created on 13.10.14 13:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.zip.GZIPOutputStream;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_WITH_ASK;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_WITH_BID;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICKZ;

/**
 * @author oflege
 */
public class QtxExporter {

    private static final ByteString QTX = new ByteString("QTX");

    public static void main(String[] args) throws IOException {
        try (PrintWriter pw = new PrintWriter(new GZIPOutputStream(new FileOutputStream("qtx.out.tgz")));
            Scanner sc = new Scanner(new File("qtx_files.txt"))) {
            pw.println("SYMBOL;DAY;TIME;BID;BID_VOLUME;ASK;ASK_VOLUME");
            while (sc.hasNextLine()) {
                process(pw, new File(sc.nextLine()));
            }
        }
    }

    private static void process(PrintWriter pw, File f) throws IOException {
        FeedDataRepository r = new FeedDataRepository(8000);
        r.setDataFactory(OrderedFeedDataFactory.RT_TICKS);
        int day = TickFiles.getDay(f);
        StringBuilder sb = new StringBuilder();
        TimeTaker tt = new TimeTaker();

        try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
            TickFileIndexReader.readEntries(fc, new TickCli.CliIndexHandler(r, f));
            int n = 0;
            FeedMarket m = r.getMarket(QTX);
            for (FeedData fd : m.getElements(false)) {
                sb.setLength(0);
                sb.append(fd.getVwdcode()).append(';').append(day).append(';');
                int p = sb.length();

                final OrderedTickData otd = ((OrderedFeedData) fd).getOrderedTickData();
                final int length = otd.getLength(day);
                byte[] ticks = new byte[length];
                if (ticks.length == 0) {
                    continue;
                }
                FileTickStore.fillData(fc, otd.getStoreAddress(day), ByteBuffer.wrap(ticks));
                for (TickDecompressor.Element e : new TickDecompressor(ticks, TICKZ)) {
                    if (!e.hasFlag(FLAG_WITH_BID) && !e.hasFlag(FLAG_WITH_ASK)) {
                        continue;
                    }
                    sb.setLength(p);
                    BufferFieldData bfd = e.getData();
                    TickCli.LineBuilder.appendTime(sb, bfd.getInt());
                    sb.append(';');

                    BigDecimal bid = null, ask = null;
                    long bidVol = Long.MIN_VALUE;
                    long askVol = Long.MIN_VALUE;
                    for (int oid = bfd.readNext(); oid > 0; oid = bfd.readNext()) {
                        switch (oid) {
                            case VwdFieldOrder.ORDER_ADF_GELD:
                                bid = BigDecimal.valueOf(bfd.getInt(), -bfd.getByte());
                                break;
                            case VwdFieldOrder.ORDER_ADF_GELD_UMSATZ:
                                bidVol = bfd.getUnsignedInt();
                                break;
                            case VwdFieldOrder.ORDER_ADF_BRIEF:
                                ask = BigDecimal.valueOf(bfd.getInt(), -bfd.getByte());
                                break;
                            case VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ:
                                askVol = bfd.getUnsignedInt();
                                break;
                            default:
                                bfd.skipCurrent();
                        }
                        if (oid >= VwdFieldOrder.ORDER_ADF_BRIEF_UMSATZ) {
                            break;
                        }
                    }

                    if (e.hasFlag(FLAG_WITH_BID)) {
                        if (bid != null) {
                            sb.append(bid.toPlainString());
                        }
                        sb.append(';');
                        if (bidVol != Long.MIN_VALUE) {
                            sb.append(bidVol);
                        }
                        sb.append(';');
                    }
                    else {
                        sb.append(";;");
                    }

                    if (e.hasFlag(FLAG_WITH_BID)) {
                        if (ask != null) {
                            sb.append(ask.toPlainString());
                        }
                        sb.append(';');
                        if (askVol != Long.MIN_VALUE) {
                            sb.append(askVol);
                        }
                    }

                    pw.println(sb.toString());
                    n++;
                }
            }
            System.err.println(f.getName() + ", #symbols=" + r.getNumSymbols() + ", #ticks=" + n
                + ", took " + tt);
        }
    }
}
