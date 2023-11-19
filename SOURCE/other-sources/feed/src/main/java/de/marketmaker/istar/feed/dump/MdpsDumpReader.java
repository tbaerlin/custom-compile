/*
 * MdpsDumpReader.java
 *
 * Created on 26.06.14 08:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dump;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarketRepository;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.mcrip.FeedUpdateFormatter;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.mdps.MdpsKeyConverter;
import de.marketmaker.istar.feed.mdps.SimpleMdpsRecordSource;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedUpdateBuilder;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;
import de.marketmaker.istar.feed.staticfeed.BlobBuilder;
import de.marketmaker.istar.feed.staticfeed.DiffBuilder;
import de.marketmaker.istar.feed.staticfeed.DiffFormatter;
import de.marketmaker.istar.feed.staticfeed.StaticFeedData;
import de.marketmaker.istar.feed.staticfeed.StaticFeedUpdateBuilder;
import de.marketmaker.istar.feed.staticfeed.StaticMdpsParser;
import de.marketmaker.istar.feed.staticfeed.StaticSnapBuilder;

/**
 * @author oflege
 */
class MdpsDumpReader implements OrderedUpdateBuilder, InitializingBean {

    private static final FileFilter DEFAULT_FILTER = pathname -> pathname.getName().endsWith(".bin");

    private PrintWriter out = new PrintWriter(System.out);

    protected FeedUpdateFormatter fmt = new FeedUpdateFormatter(true);

    private final StaticMdpsParser p = new StaticMdpsParser() {
        @Override
        protected int getMessageTimestamp() {
            return 0;
        }
    };

    private FeedDataRegistry registry = new FeedDataRepository(1 << 16);

    private final byte[] buffer = new byte[1024 * 1024];

    private ByteBuffer bb = ByteBuffer.wrap(buffer);

    private SimpleMdpsRecordSource rs;

    private ThroughputLimiter limiter;

    private int protocolVersion = 3;

    private int srcId = 0;

    private boolean inflate = true;

    private boolean mdpsFormat = false;

    private BitSet requiredFields = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.registry.setDataFactory(StaticFeedData.FACTORY);
        OrderedFeedUpdateBuilder builder = new StaticFeedUpdateBuilder() {
            @Override
            public void process(FeedData data, ParsedRecord pr) {
                if (requiredFields == null || pr.isAnyFieldPresent(requiredFields)) {
                    super.process(data, pr);
                }
            }

            @Override
            protected void ackMessageTimestamp(int messageTimestamp) {
                // ignore
            }
        };
        builder.setWithSourceId(true);
        DiffFormatter fmt = new DiffFormatter();
        fmt.setHandler(out::println);
        DiffBuilder db = new DiffBuilder();
        db.setFormatter(fmt);
        builder.setBuilders(new OrderedUpdateBuilder[]{
                db, new StaticSnapBuilder()});

        p.setRepository(this.registry);

        p.setKeyConverter(new MdpsKeyConverter(false, false));
        p.setAddToaAndDoa(false);
        p.setProcessInvalidDates(true);
//        p.setIgnoreInvalidDates(false);
        p.setFeedBuilders(builder);
        p.setProcessBigSourceId(true);
        p.afterPropertiesSet();
    }

    public static void main(String[] args) throws Exception {
        MdpsDumpReader reader = new MdpsDumpReader();
        int n = 0;
        while (args.length > n && args[n].startsWith("-")) {
            if ("-o".equals(args[n])) {
                File outfile = new File(args[++n]);
                if (outfile.exists() && !outfile.delete()) {
                    System.err.println("failed to delete " + outfile.getAbsolutePath());
                    return;
                }
                reader.out = new PrintWriter(outfile);
                System.err.println("out : " + outfile.getAbsolutePath());
            }
            else if ("-blobdir".equals(args[n])) {
                BlobBuilder blobBuilder = new BlobBuilder();
                blobBuilder.setOutDir(new File(args[++n]));
                reader.p.setBlobBuilder(blobBuilder);
            }
            else if ("-filter".equals(args[n])) {
                reader.p.setVendorkeyFilter(VendorkeyFilterFactory.create(args[++n]));
            }
            else if ("-p1".equals(args[n])) {
                reader.protocolVersion = 1;
            }
            else if ("-i".equals(args[n])) {
                reader.inflate = false;
            }
            else if ("-s".equals(args[n])) {
                reader.srcId = Integer.parseInt(args[++n]);
            }
            else if ("-l".equals(args[n])) {
                reader.limiter = new ThroughputLimiter(NumberUtil.parseInt(args[++n].toUpperCase()));
            }
            else if ("-v".equals(args[n])) {
                // with the default registry, snap data will be stored and real diffs will be written
                // with this registry, each update will be written
                reader.registry = new VolatileFeedDataRegistry(new FeedMarketRepository(false));
            }
            else if ("-f".equals(args[n])) {
                if (reader.requiredFields == null) {
                    reader.requiredFields = new BitSet();
                }
                for (String fid : args[++n].split(",")) {
                    reader.requiredFields.set(Integer.parseInt(fid.trim()));
                }
            }
            else if ("-mdpsFormat".equals(args[n])) {
                reader.mdpsFormat = true;
            }
            else {
                usage();
            }
            n++;
        }
        if (n == args.length) {
            usage();
        }

        reader.afterPropertiesSet();
        reader.dump(Arrays.copyOfRange(args, n, args.length));
    }

    private static void usage() {
        System.err.println("Usage: java de.marketmaker.istar.feed.dump.MdpsDumpReader <options> <file>+");
        System.err.println("  Dumps feed data for symbol(s) from file(s)");
        System.err.println(" Options are");
        System.err.println(" -o filename    -- dump to file (default: stdout)");
        System.err.println(" -blobdir dir   -- store blobs in dir, default is to ignore blobs");
        System.err.println(" -filter spec   -- dump only records that match this vendorkey filter");
        System.err.println(" -p1            -- use mdps protocol version 1, default is 3");
        System.err.println(" -i             -- do not inflate files (default: do inflate)");
        System.err.println(" -s sourceId    -- dump only records with this BigSourceId");
        System.err.println(" -l limit       -- process only limit bytes/s (suffixes can be used: 500k, 1m)");
        System.err.println(" -v             -- output every update instead of only diffs (ignores -filter)");
        System.err.println(" -f fid(,fid)*  -- dump only records with one of these fields");
        System.err.println(" -mdpsFormat    -- output MDPS source data");
        System.exit(-2);
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (this.srcId == 0 || this.srcId == update.getSourceId()) {
            if (mdpsFormat) {
                out.println(update.toDebugString());
            } else {
                out.println(this.fmt.format(data, update));
            }
        }
    }

    private void dump(String[] args) throws Exception {
//        this.builder.setBuilders(new OrderedUpdateBuilder[]{this});

        this.bb.order(getOrder());
        this.rs = new SimpleMdpsRecordSource(this.bb);

        for (File f : getFiles(args)) {
            dump(f);
        }

        this.out.close();
    }

    private ByteOrder getOrder() {
        return MdpsFeedUtils.getByteOrder(this.protocolVersion);
    }

    private List<File> getFiles(String[] args) {
        Set<File> files = new HashSet<>();
        for (final String arg : args) {
            File f = new File(arg);
            if (f.isFile()) {
                files.add(f);
            }
            else if (f.isDirectory()) {
                files.addAll(getFiles(f, DEFAULT_FILTER));
            }
        }
        List<File> result = new ArrayList<>(files);
        result.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        return result;
    }

    private List<File> getFiles(final File dir, FileFilter filter) {
        return Arrays.asList(dir.listFiles(filter));
    }

    private void dump(File file) throws Exception {
        System.err.println("# " + file.getName());

        if (!this.inflate) {
            this.bb = ByteBuffer.wrap(Files.readAllBytes(file.toPath())).order(getOrder());
            this.rs = new SimpleMdpsRecordSource(this.bb);
            write(this.bb);
            return;
        }

        Inflater inf = new Inflater(true);
        try (InflaterInputStream is = new InflaterInputStream(new FileInputStream(file), inf, 1 << 16)) {
            int off = 0;
            int len;
            while ((len = is.read(this.buffer, off, this.buffer.length - off)) != -1) {
                this.bb.clear().limit(off + len);
                write(this.bb);
                this.bb.compact();
                off = this.bb.position();
            }
        } finally {
            inf.end();
        }
    }

    private void write(ByteBuffer bb) throws Exception {
        int oldLimit = bb.limit();
        applyLimit(bb);
        if (this.limiter != null) {
            this.limiter.ackActions(bb.remaining());
        }
        while (bb.hasRemaining()) {
            FeedRecord feedRecord = rs.getFeedRecord();
            if (mdpsFormat) {
                this.out.println(feedRecord.toDebugString());
            } else {
                this.p.accept(feedRecord);
            }
        }
        bb.limit(oldLimit);
    }

    private void applyLimit(ByteBuffer bb) throws IOException {
        int pos = bb.position();
        int result = pos;
        while (pos < bb.limit() - 2) {
            final int msgLen = MdpsFeedUtils.getUnsignedShort(bb, pos);
            if (msgLen <= 0) {
                throw new IOException("invalid record length: " + msgLen);
            }
            result = pos;
            pos += msgLen;
        }
        bb.limit(result);
    }
}
