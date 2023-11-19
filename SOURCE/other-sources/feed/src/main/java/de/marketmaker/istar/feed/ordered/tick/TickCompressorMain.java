/*
 * TickCompressor.java
 *
 * Created on 17.12.12 15:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.GZIPOutputStream;

import de.marketmaker.istar.common.util.ByteString;

import static de.marketmaker.istar.feed.ordered.tick.FileTickStore.DDZ;
import static de.marketmaker.istar.feed.ordered.tick.FileTickStore.TDZ;

/**
 * Command line tool to convert <code>.tda</code> and <code>.td3</code> files into <code>.tdz</code>
 * files. In the latter, ticks data is compressed using a {@link GZIPOutputStream}, which yields
 * better compression than "Snappy" (used in td3) or the custom bit-twiddling approach taken in
 * tda-Files. Input files are mapped completely into memory, so running this on a machine with
 * much memory should help a lot.
 * @author oflege
 */
class TickCompressorMain extends TickFileProcessor {

    private static final String MARKET_DEPTH_SUFFIX = "MT";

    private FileFilter getFileFilter() {
        FileFilter fileFilter = f -> {
            if (f.length() <= 8) {
                return false;
            }
            final String name = f.getName();
            final Matcher m = TickFile.TICK_FILE_NAME.matcher(name);
            boolean isAcceptable = m.matches() && !m.group(1).endsWith(MARKET_DEPTH_SUFFIX);
            if (isAcceptable && marketExcludes != null) {
                String[] market = name.split("-");
                return !marketExcludes.contains(market[0]);
            } else {
                return isAcceptable;
            }
        };
        return fileFilter;
    }

    private class CompressTask extends TickFileProcessor.Task implements TickCompressor.Filter {
        @Override
        protected void process(File f, File out) throws IOException {
            createCompressorFor(f, out, this).compress();
        }

        protected File getOutFile(File f) {
            if (out != null && isFileName(out)) {
                return out;
            }
            final File dir = (out != null) ? out : f.getParentFile();
            return new File(dir, f.getName().replaceFirst("([dt])d3$", "$1dz"));
        }

        @Override
        public boolean isIncluded(ByteString bs) {
            return restriction == null || restrictionIncludes == restriction.contains(bs);
        }
    }

    private static TickCompressor createCompressorFor(File f, File out,
            TickCompressor.Filter filter) {
        if (f.getName().endsWith(FileTickStore.TD3) || f.getName().endsWith(TDZ)) {
            return new TickCompressorTd3(f, out, filter);
        }
        if (f.getName().endsWith(FileTickStore.DD3) || f.getName().endsWith(DDZ)) {
            return new DumpCompressorDd3(f, out, filter);
        }
        throw new IllegalArgumentException(f.getAbsolutePath());
    }

    private static void usage() {
        System.err.println("Usage: TickCompressor [-o outputDir] (directory|file [file]*)");
    }

    public static void main(String[] args) throws Exception {
        TickCli.checkForAssertionsEnabled();

        final TickCompressorMain tcm = new TickCompressorMain();

        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if (args[n].equals("-o")) {
                tcm.setOut(new File(args[++n]));
            }
            else if (args[n].equals("-i")) {
                tcm.readRestriction(new File(args[++n]), true);
            }
            else if (args[n].equals("-x")) {
                tcm.readRestriction(new File(args[++n]), false);
            }
            else if (args[n].equals("-xf")) {
                tcm.excludeMarkets(args[++n]);
            }
            else if (args[n].equals("-t")) {
                tcm.numThreads = Integer.parseInt(args[++n]);
            }
            else if (args[n].equals("-g")) {
                tcm.maxMappedBytes = 1024 * 1024 * 1024 * Long.parseLong(args[++n]);
            }
            else {
                usage();
                throw new IllegalArgumentException("unknown option " + args[n]);
            }
            n++;
        }

        tcm.process(getFiles(Arrays.copyOfRange(args, n, args.length), tcm.getFileFilter()));
    }

    private Set<String> marketExcludes;

    private Set<ByteString> restriction;

    private Boolean restrictionIncludes;

    private File out;

    @Override
    protected Task createTask() {
        return new CompressTask();
    }

    void setOut(File out) throws Exception {
        if (!isFileName(out) && !out.isDirectory() && !out.mkdirs()) {
            throw new Exception("could not create directory " + out.getAbsolutePath());
        }
        this.out = out;
    }

    private boolean isFileName(File out) {
        return out.getName().endsWith(TDZ) || out.getName().endsWith(DDZ);
    }

    private void excludeMarkets(String markets) throws IOException {
        this.marketExcludes = new HashSet<>();
        String[] ms = markets.split(",");
        for (String m : ms) {
            marketExcludes.add(m);
            System.out.println("Excluding market: " + m);

        }
    }

    private void readRestriction(File f, boolean includes) throws IOException {
        this.restrictionIncludes = includes;
        this.restriction = new HashSet<>();
        try (Scanner sc = new Scanner(f)) {
            while (sc.hasNextLine()) {
                restriction.add(new ByteString(sc.nextLine()));
            }
        }
        System.out.println("restrictions #" + restriction.size());
    }
}
