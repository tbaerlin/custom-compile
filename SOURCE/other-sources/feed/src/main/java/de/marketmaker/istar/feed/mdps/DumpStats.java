/*
 * FeedStats.java
 *
 * Created on 11.06.14 13:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedUpdateBuilder;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Computes statistics for a feed dump that has been captured from an mdps feed server, e.g.
 * <pre>
 * nc <em>host</em> <em>port</em> &gt; outfile.bin
 * </pre>
 * @author oflege
 */
class DumpStats implements OrderedUpdateBuilder {
    private final InflatingMdpsRecordSource rs = new InflatingMdpsRecordSource();

    private final MdpsFeedParser p = new MdpsFeedParser();

    private final MessageStats stats = new MessageStats();

    private final VendorkeyFilter filter = VendorkeyFilterFactory.create("^514000.EUS$");
//    private final VendorkeyFilter filter = VendorkeyFilterFactory.create("^m:ETR,EUS,EEU,FFM,ETROTC,BLN,MCH$");

    private int n;

    DumpStats() throws Exception {
        OrderedFeedUpdateBuilder b = new OrderedFeedUpdateBuilder();
        b.setBuilders(new OrderedUpdateBuilder[]{this});

        VolatileFeedDataRegistry registry = new VolatileFeedDataRegistry();

        p.setRepository(registry);
        p.setAddToaAndDoa(false);
        p.setFeedBuilders(b);
        p.setVendorkeyFilter(filter);
        p.afterPropertiesSet();

        rs.setProtocolVersion(3);
        rs.setBufferSize(131072);
        rs.setParser(p);
        rs.afterPropertiesSet();
        rs.start();
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (!filter.test(data.getVendorkey())) return;
        System.out.println(Integer.toBinaryString(update.getFlags()));
        if (!update.hasFlag(FeedUpdateFlags.FLAG_WITH_TRADE)) return;
        BitSet oids = stats.process(data, update);
        if (n < 10 && oids.get(VwdFieldOrder.getOrder(VwdFieldDescription.ADF_MMT_TRADING_MODE.id()))) {
            System.out.println(data.getVendorkey());
            n++;
        }
    }

    private void write(File f) throws IOException {
        byte[] bytes = FileCopyUtils.copyToByteArray(f);
        rs.write(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN));
    }

    public static void main(String[] args) throws Exception {
        DumpStats fs = new DumpStats();
        fs.write(new File("/Users/oflege/tmp/testfeed.bin.1"));
        fs.rs.stop();
        System.out.println(fs.p.numRecordsParsed());
        try (PrintWriter pw = new PrintWriter(System.out)) {
            fs.stats.printResult(pw, true);
        }
    }
}
