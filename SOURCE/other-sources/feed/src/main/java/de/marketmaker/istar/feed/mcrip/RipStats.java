/*
 * RipReader.java
 *
 * Created on 07.07.11 14:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mcrip;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;

import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.mdps.MessageStats;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;

/**
 * Extract statistics from files dumped by {@link MulticastFeedRipper}. Statistics are important
 * to be able to judge whether the VwdFieldOrder is still appropriate, that is, fields with the
 * highest update rate have one of the lowest orders.
 * @author oflege
 */
public class RipStats extends AbstractRipReader implements OrderedUpdateBuilder {

    private static final FileFilter FILTER
            = pathname -> pathname.isFile() && pathname.getName().endsWith(MulticastFeedRipper.SUFFIX);

    public static void main(String[] args) throws Exception {
        RipStats reader = new RipStats();

        int n = 0;
        while (args.length > n && args[n].startsWith("-")) {
            if ("-o".equals(args[n])) {
                File outfile = new File(args[++n]);
                reader.out = new PrintWriter(outfile);
                System.err.println("out : " + outfile.getAbsolutePath());
            }
            else if ("-h".equals(args[n]) || "--help".equals(args[n])) {
                System.err.println("Usage: java de.marketmaker.istar.feed.mcrip.RipStats <file|dir>+");
                System.err.println("  collects and prints feed statistics from dump files");
                System.err.println(" Options are");
                System.err.println(" -o filename   -- dump to file (default: stdout)");
                System.exit(-2);
            }
            n++;
        }

        while (args.length > n) {
            File f = new File(args[n++]);
            if (f.isDirectory()) {
                for (File file : f.listFiles(FILTER)) {
                    reader.read(file, null, reader);
                }
            }
            else {
                reader.read(f, null, reader);
            }
        }

        reader.stats.printResult(reader.out, false);
        reader.out.close();
    }

    private PrintWriter out = new PrintWriter(System.out);

    private final VolatileFeedDataRegistry registry = new VolatileFeedDataRegistry();

    private final MessageStats stats = new MessageStats();

    @Override
    protected VolatileFeedDataRegistry getRegistry() {
        return this.registry;
    }

    @Override
    protected void read(File f, VendorkeyFilter filter,
            OrderedUpdateBuilder... builders) throws Exception {
        super.read(f, filter, builders);
        this.stats.removeCountsSmallerThan(this.numFiles);
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        this.stats.process(data, update);
    }
}
