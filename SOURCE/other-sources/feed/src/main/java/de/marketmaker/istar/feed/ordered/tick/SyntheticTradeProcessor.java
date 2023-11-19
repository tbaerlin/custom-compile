/*
 * SyntheticTradeProcessor.java
 *
 * Created on 13.09.13 12:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.joda.time.LocalTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static de.marketmaker.istar.feed.ordered.tick.SyntheticTradeWriter.HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

/**
 * @author oflege
 */
public class SyntheticTradeProcessor extends TickFileProcessor implements FileFilter {

    private static final Pattern TICK_FILE_NAME = Pattern.compile("(.+)-[0-9]{8}.td[3z]");

    private class MyTask extends Task {
        @Override
        protected void process(File f, File out) throws IOException {
            new SyntheticTradeWriter(f, symbols).process(out);
        }

        @Override
        protected File getOutFile(File f) {
            final String n = f.getName();
            return new File(outDir != null ? outDir : f.getParentFile(), n.substring(0, n.length() - 4) + ".csv");
        }
    }

    @Override
    protected void process(File[] inputFiles) throws Exception {
        super.process(inputFiles);
        ensureFileForEachMarket(); // DP-2191
    }

    private void ensureFileForEachMarket() {
        if (this.outDir == null || this.markets == null) {
            return;
        }
        final String suffix = "-" + this.outDir.getName() + ".csv";
        this.markets.stream()
                .map((bs) -> new File(this.outDir, bs + suffix))
                .filter((f) -> !f.isFile())
                .forEach(this::createEmptyFile);
    }

    private void createEmptyFile(File f) {
        try {
            Files.write(f.toPath(), singletonList(HEADER), UTF_8);
            this.logger.info("<createEmptyFile> " + f.getName());
        } catch (IOException e) {
            this.logger.error("<createEmptyFile> failed for " + f.getAbsolutePath(), e);
        }
    }

    @Override
    protected Task createTask() {
        return new MyTask();
    }

    private File outDir;

    private Set<ByteString> symbols = null;

    private Set<ByteString> markets = null;

    @Override
    public boolean accept(File f) {
        if (!f.isFile() || f.length() <= 8) {
            return false;
        }
        final Matcher m = TICK_FILE_NAME.matcher(f.getName());
        return m.matches() && isAcceptableMarket(m.group(1));
    }

    private boolean isAcceptableMarket(final String market) {
        return !"PROF".equals(market) && (markets == null || markets.contains(new ByteString(market)));
    }

    public void setMarkets(List<String> markets) {
        this.markets = new HashSet<>();
        for (String market : markets) {
            this.markets.add(new ByteString(market));
        }
    }

    private void readSymbols(File file) throws FileNotFoundException {
        this.symbols = new HashSet<>();
        this.markets = new HashSet<>();
        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                if (StringUtils.hasText(line)) {
                    final VendorkeyVwd vkey = VendorkeyVwd.getInstance(new ByteString(line), 1);
                    this.markets.add(vkey.getMarketName());
                    this.symbols.add(vkey.toVwdcode());
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        final SyntheticTradeProcessor stp = new SyntheticTradeProcessor();

        List<String> inputDirs = new ArrayList<>();
        String outputDirName = null;

        if (args.length == 0) {
            usage();
        }

        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if (args[n].equals("-o")) {
                outputDirName = args[++n];
            }
            else if (args[n].equals("-i")) {
                inputDirs.addAll(Files.readAllLines(new File(args[++n]).toPath(), UTF_8));
            }
            else if (args[n].equals("-m")) {
                stp.setMarkets(Files.lines(new File(args[++n]).toPath(), UTF_8)
                        .filter(StringUtils::hasText)
                                // the file may contain patterns such as FOO-*.td3, we need: FOO
                        .map((s) -> s.replaceFirst("-.*", ""))
                        .collect(Collectors.toList()));
            }
            else if (args[n].equals("-t")) {
                stp.numThreads = Integer.parseInt(args[++n]);
            }
            else if (args[n].equals("-g")) {
                stp.maxMappedBytes = 1024 * 1024 * 1024 * Long.parseLong(args[++n]);
            }
            else if (args[n].equals("-s")) {
                final File file = new File(args[++n]);
                stp.readSymbols(file);
            }
            else {
                usage();
            }
            n++;
        }

        if (n < args.length) {
            if (outputDirName != null) {
                stp.outDir = FileUtil.ensureDir(new File(outputDirName));
            }
            stp.process(getFiles(Arrays.copyOfRange(args, n, args.length), stp));
        }

        if (stp.markets != null && stp.symbols == null && !inputDirs.isEmpty()) {
            int firstDate = getDate(inputDirs.get(0));
            int lastDate = getDate(inputDirs.get(inputDirs.size() - 1));
            if (firstDate > lastDate) {
                stp.initSymbols(inputDirs.subList(0, Math.min(inputDirs.size(), 14)));
            }
            else {
                stp.initSymbols(inputDirs.subList(Math.max(0, inputDirs.size() - 14), inputDirs.size()));
            }
        }

        for (String inputDirName : inputDirs) {
            if (new LocalTime().isAfter(new LocalTime(23, 30))) {
                System.err.println("BREAK, next is " + inputDirName);
                break;
            }
            final File inputDir = new File(inputDirName);
            if (!inputDir.isDirectory()) {
                System.err.println("No such directory: '" + inputDir.getAbsolutePath() + "'");
                continue;
            }
            if (outputDirName != null) {
                File outputDir = new File(outputDirName.replace("{date}", inputDir.getName()));
                stp.outDir = FileUtil.ensureDir(outputDir);
            }
            stp.process(getFiles(new String[]{inputDirName}, stp));
        }
    }

    private static int getDate(final String path) {
        return Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
    }

    private void initSymbols(List<String> inputDirs) {
        this.symbols = new HashSet<>(1 << 20);
        for (String inputDirName : inputDirs) {
            final File[] files = getFiles(new String[]{inputDirName}, this);
            for (File file : files) {
                if (FileTickStore.canHandle(file)) {
                    System.err.println("Reading symbols from " + file.getName());
                    try (FileChannel fc = new RandomAccessFile(file, "r").getChannel()) {
                        TickFileIndexReader.readEntries(fc, new DefaultIndexHandler(file) {
                            @Override
                            protected void doHandle(ByteString vwdcode, long position, int length) {
                                symbols.add(vwdcode);
                            }
                        });
                    } catch (IOException e) {
                        System.err.println("<initSymbols> failed for " + file.getAbsolutePath());
                    }
                }
            }
        }
        System.err.println("Read " + symbols.size() + " symbols");
        dumpSymbols();
    }

    private void dumpSymbols() {
        final File f = new File("symbols-" + System.currentTimeMillis() + ".txt");
        try (PrintWriter pw = new PrintWriter(f)) {
            for (ByteString symbol : this.symbols) {
                pw.println(symbol);
            }
            System.err.println("dumped symbols to " + f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void usage() {
        System.err.println("Usage: SyntheticTradeProcessor [options] [infiles]");
        System.err.println("options");
        System.err.println("-o <dirName> output directory, {date} will be replaced with input dir date");
        System.err.println("-i <file> read input directories from file");
        System.err.println("-t <num> number of processing threads");
        System.err.println("-g <num> max size of simultaneously mapped files in gb");
        System.err.println("-s <file> read vwdcodes from file");
        System.err.println("-m <file> read markets from file");
        System.exit(1);
    }

}
