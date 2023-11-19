/*
 * DumpStats.java
 *
 * Created on 13.03.15 13:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.FeedMarketRepository;
import de.marketmaker.istar.feed.mdps.MessageStats;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;

import static de.marketmaker.istar.feed.ordered.tick.FileTickStore.decodePosition;

/**
 * @author oflege
 */
public class DumpStats {

    private static class Entry implements Comparable<Entry> {
        final long position;
        final int length;

        private Entry(long position, int length) {
            this.position = position;
            this.length = length;
        }

        @Override
        public int compareTo(Entry o) {
            return Long.compare(decodePosition(this.position), decodePosition(o.position));
        }
    }
    
    private static final FileFilter FILTER 
            = p -> p.isFile() && p.getName().endsWith(FileTickStore.DDZ);

    private PrintWriter out = new PrintWriter(System.out);

    private final MessageStats stats = new MessageStats();

    private final List<Entry> entries = new ArrayList<>(1 << 17);

    private FeedMarketRepository marketRepository = new FeedMarketRepository(false);

    private FeedMarket market;

    public static void main(String[] args) throws IOException {
        DumpStats reader = new DumpStats();

        int n = 0;
        while (args.length > n && args[n].startsWith("-")) {
            if ("-o".equals(args[n])) {
                File outfile = new File(args[++n]);
                reader.out = new PrintWriter(outfile);
                System.err.println("out : " + outfile.getAbsolutePath());
            }
            else if ("-h".equals(args[n]) || "--help".equals(args[n])) {
                System.err.println("Usage: java de.marketmaker.istar.feed.ordered.tick.DumpStats <file|dir>+");
                System.err.println("  collects and prints feed statistics from istar-feeddump files");
                System.err.println(" Options are");
                System.err.println(" -o filename   -- dump to file (default: stdout)");
                System.exit(-2);
            }
            n++;
        }

        List<File> files = getFiles(args, n);

        reader.read(files);
        reader.stats.printResult(reader.out, false);
        reader.out.close();
    }

    protected static List<File> getFiles(String[] args, int n) {
        List<File> result = new ArrayList<>();
        for (int i = n; i < args.length; i++) {
            File f = new File(args[i]);
            if (f.isDirectory()) {
                Collections.addAll(result, f.listFiles(FILTER));
            }
            else {
                result.add(f);
            }
        }
        return result;
    }

    private void addEntry(ByteString key, long position, int length) {
        this.entries.add(new Entry(position, length));
    }

    private void handle(DumpDecompressor.Element e) {
        this.stats.process(this.market, e.getData());
    }

    private void read(List<File> files) throws IOException {
        for (File f : files) {
            reset(f);
            read(f);
            this.stats.removeCountsSmallerThan(files.size());
        }
    }

    private void reset(File f) {
        this.entries.clear();
        this.market = this.marketRepository.getMarket(new ByteString(getMarketName(f)));
    }

    private String getMarketName(File f) {
        final String m = TickFile.getMarket(f);
        final int p = m.indexOf('^');
        return p > 0 ? m.substring(0, p) : m;
    }

    private void read(File f) throws IOException {
        AbstractTickRecord.TickItem.Encoding enc = TickFiles.getItemType(f);

        try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
            TickFileIndexReader.readEntries(fc, this::addEntry);
            this.entries.sort(null);
            for (Entry e : entries) {
                ByteBuffer bb = ByteBuffer.allocate(e.length);
                FileTickStore.fillData(fc, e.position, bb);
                new DumpDecompressor(bb.array(), enc).forEach(this::handle);
            }
        }
    }
}
