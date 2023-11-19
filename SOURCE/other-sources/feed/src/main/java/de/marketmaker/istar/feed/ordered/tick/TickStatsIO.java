/*
 * TickStatsIO.java
 *
 * Created on 26.11.12 10:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;

import static de.marketmaker.istar.feed.ordered.tick.TickStats.*;

/**
 * Stores/Restores tick stats to/from a simple file. The file's format is
 * <pre>
 *     (|key-length (byte)|key|stats (int)|)*
 * </pre>
 * Must be initialized <em>after</em> the FeedDataRepository has been filled as this object does
 * not register FeedData objects, it just retrieves them. Invocations of {@link #writeStats()} are
 * expected to be scheduled externally on a daily basis.
 * <p>
 * The main method can be used to analyze tickstat-files and to print a tick store layout.
 * @author oflege
 */
@ManagedResource
public class TickStatsIO implements InitializingBean, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private File statsFile;

    private FeedDataRepository repository;

    private int idleBits = 0;

    public void setIdleCount(int n) {
        this.idleBits = TickStats.toIdleBits(n);
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    public void setStatsFile(File statsFile) {
        this.statsFile = statsFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        readStats();
    }

    @Override
    public void destroy() throws Exception {
        this.writeStats();
    }

    @ManagedOperation
    public synchronized void writeStats() throws IOException {
        if (!FileUtil.backupFile(this.statsFile, ".old")) {
            this.logger.warn("<writeStats> failed to delete " + this.statsFile.getAbsolutePath());
            return;
        }
        TimeTaker tt = new TimeTaker();
        int n = 0;

        final ByteBuffer bb = ByteBuffer.allocate(1 << 16);
        try (FileChannel fc = getChannel("rw")) {
            for (FeedMarket m : repository.getMarkets()) {
                for (FeedData fd : m.getElements(false)) {
                    synchronized (fd) {
                        if (fd.isDeleted()) {
                            continue;
                        }
                        final int stats = ((OrderedFeedData) fd).getOrderedTickData().getTickStats();
                        if (stats != 0) {
                            fd.getVwdcode().writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);
                            bb.putInt(withoutIdleBits(stats));
                            n++;
                        }
                    }
                    if (bb.remaining() < 128) {
                        fc.write((ByteBuffer) bb.flip());
                        bb.clear();
                    }
                }
            }
            fc.write((ByteBuffer) bb.flip());
            bb.clear();
        }
        this.logger.info("<writeStats> for " + n + " elements, took " + tt);
    }

    private FileChannel getChannel(final String mode) throws FileNotFoundException {
        return new RandomAccessFile(this.statsFile, mode).getChannel();
    }

    protected void readStats() throws IOException {
        if (!this.statsFile.canRead()) {
            this.logger.info("<readStats> cannot read " + this.statsFile.getAbsolutePath());
            return;
        }
        int n = 0;
        final ByteBuffer bb = (ByteBuffer) ByteBuffer.allocate(1 << 16).flip();
        boolean eof = false;
        try (FileChannel fc = getChannel("r")) {
            while (!eof || bb.hasRemaining()) {
                if (!eof && bb.remaining() < 128) {
                    bb.compact();
                    eof = fc.read(bb) == -1;
                    bb.flip();
                    continue;
                }
                if (add(ByteString.readFrom(bb), withIdleBits(bb.getInt(), this.idleBits))) {
                    n++;
                }
            }
        }
        this.logger.info("<readStats> restored stats for " + n + " elements");
    }

    protected boolean add(ByteString vwdcode, int stats) {
        final FeedData fd = this.repository.get(vwdcode);
        if (fd != null) {
            ((OrderedFeedData) fd).getOrderedTickData().setTickStats(stats);
            return true;
        }
        return false;
    }

    /**
     * read an existing stats file and print a summary of how many items fall into each
     * tick byte size congruence class.
     * @param args name of tick stats file
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Queue<String> q = new ArrayDeque<>(Arrays.asList(args));
        if (!q.isEmpty()) {
            printStats(q);
        }
        else {
            System.err.println("Usage:");
            System.err.println("TickStatsIO [-min minsize] [-s targetSize] statsfile");
            System.err.println("  print summary of data in statsfile and a recommended");
            System.err.println("  sizing of tick stores, targetSize can be used to enlarge");
            System.err.println("  the tick stores (example: -s 4g512m)");
        }
    }

    private static void printStats(Queue<String> args) throws Exception {
        int min = 7;
        final int[] counts = new int[32];
        final int[] nums = new int[32];
        final long[] sizes = new long[32];
        final ByteString[] bs = new ByteString[1];
        final int[] max = new int[1];
        TickStatsIO io = new TickStatsIO() {
            @Override
            protected boolean add(ByteString vwdcode, int stats) {
                final int result = max(stats);
                counts[result]++;
                if (result > max[0]) {
                    max[0] = result;
                    bs[0] = vwdcode;
                }
                return true;
            }
        };

        // MemoryTickStore uses OrderedTickData#getAvgLength() >> newChunkLengthShift to compute the
        // requested store size, the 3 chunkShift is the default value
        int chunkShift = MemoryTickStore.DEFAULT_CHUNK_LENGTH_SHIFT;
        long targetSize = 0L;
        while (args.peek().startsWith("-")) {
            if ("-size".startsWith(args.peek())) {
                args.remove();
                targetSize = parseMem(args.remove().toLowerCase());
                System.out.println("Target Size = " + formatMem(targetSize));
            }
            else if ("-min".startsWith(args.peek())) {
                args.remove();
                min = Integer.parseInt(args.remove());
                System.out.println("Min Size = " + (1 << min));
            }
            else if ("-chunk".startsWith(args.peek())) {
                args.remove();
                chunkShift = Integer.parseInt(args.remove());
                System.out.println("chunkShift = " + chunkShift);
            }
            else {
                System.err.println("Illegal arg: " + args.remove());
                System.exit(1);
            }
        }

        io.setStatsFile(new File(args.remove()));
        io.afterPropertiesSet();
        System.out.println("MAX: " + bs[0] + " " + (1 << max[0]));
        long n = 0;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] != 0) {
                System.out.printf("%2d %8d %7d%n", i, 1 << i, counts[i]);
                n += counts[i];
                nums[Math.max(min, i - chunkShift)] += counts[i];
            }
        }
        System.out.println("--");
        System.out.printf("%2s %8s %7d%n", "", "", n);
        System.out.println();

        n = 0;
        boolean largerWithSize = false;
        for (int i = nums.length; i-- > 0; ) {
            if (nums[i] != 0 || largerWithSize) {
                sizes[i] = (Math.max(8, nums[i]) * (1L << i));
                n += sizes[i];
                largerWithSize = true;
            }
        }

        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                System.out.printf("%2d %8d %10d %10d %10s%n", i, 1 << i, nums[i], sizes[i], formatMem(sizes[i]));
            }
        }
        System.out.println("--");
        System.out.println(formatMem(n));
        System.out.println();

        final long factor = targetSize * 100 / n;
        if (targetSize > 0) {
            if (factor < 100) {
                System.out.println("Normal size: " + formatMem(n));
            }
            n = 0;
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = (sizes[i] * factor / 100) & ~((1L << i) - 1);
                n += sizes[i];
            }
            System.out.println("Adjusted size: " + formatMem(n));
        }

        for (int i = nums.length; i-- > 21; ) {
            sizes[21] += sizes[i];
        }

        System.out.println("# total store size: " + formatMem(n));
        System.out.printf("# %32s%8s %8s%n", "", "#num", "#accum");
        int numTotalChunks = 0;
        for (int i = min; i < 22; i++) {
            int numChunks = (int) (sizes[i] >> i);
            numTotalChunks += numChunks;
            System.out.printf("# %32s%8d %8d%n", "", numChunks, numTotalChunks);
            System.out.println("store." + (1 << i) + ".size=" + sizes[i] /*+ " // " + formatMem(sizes[i])*/);
        }
    }

    private static long parseMem(String s) {
        final int g = s.indexOf('g');
        final int m = s.indexOf('m');
        long result = 0;
        if (g > 0) {
            result += (1L << 30) * Integer.parseInt(s.substring(0, g));
        }
        if (m > 0) {
            result += (1L << 20) * Integer.parseInt(s.substring(g > 0 ? (g + 1) : 0, m));
        }
        if (result == 0) {
            result = Long.parseLong(s);
        }
        return result;
    }

    private static String formatMem(long n) {
        long gb = n >> 30;
        long mb = n >> 20 & 0x3ff;
        return (gb > 0) ? (gb + "g" + mb + "m") : (mb + "m");
    }
}
