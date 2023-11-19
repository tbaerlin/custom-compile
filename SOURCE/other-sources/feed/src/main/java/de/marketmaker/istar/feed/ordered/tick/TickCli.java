/*
 * TickCli.java
 *
 * Created on 31.01.13 13:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import org.joda.time.LocalTime;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.FieldData;
import de.marketmaker.istar.feed.ordered.FieldDataBuilder;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.ordered.OrderedSnapRecord;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.*;
import static de.marketmaker.istar.feed.ordered.tick.TickFile.TICK_FILE_NAME;

/**
 * command line interface for td3/tdz files
 *
 * @author oflege
 */
public class TickCli {
    static final Charset UTF_8 = StandardCharsets.UTF_8;

    static final Pattern TICK_FILE = Pattern.compile("(\\w+)-(20\\d{6}).td[3z]");

    /**
     * When working with tick files we don't care about the actual type of a symbol
     * (and we do not even know it) so in all cases where this value is required by
     * downstream-APIs we use this dummy value instead.
     */
    static final int DUMMY_TYPE_MAPPING = 1;

    static class CliIndexHandler extends DefaultIndexHandler {
        private final FeedDataRepository repository;

        private final int day;

        CliIndexHandler(FeedDataRepository repository, File f) {
            super(f);
            this.repository = repository;
            this.day = TickFiles.getDay(f);
        }

        @Override
        protected void doHandle(ByteString vwdcode, long position, int length) {
            final FeedData fd = repository.register(VendorkeyVwd.getInstance(vwdcode, TickCli.DUMMY_TYPE_MAPPING));
            final OrderedTickData otd = ((OrderedFeedData) fd).getOrderedTickData();
            otd.setDate(day);
            otd.setLength(day, length);
            otd.setStoreAddress(day, position);
        }
    }

    public static void checkForAssertionsEnabled() {
        // For most sub-commands to work correctly the JDK parameter -ea needs to be added
        // The following construct will throw a RuntimeException if this parameter has not been provided
        // because assertions will not be evaluated without this parameter.
        // It will throw an AssertionError if -ea is provided due to the assertion being hard-coded to false
        // and will skip the RuntimeException by jumping into the empty catch-block - basically a goto, uargh!
        try {
            assert false;
            throw new RuntimeException("run java with -ea to detect invalid ticks");
        } catch (AssertionError ignored) {
            // When we get here -ea was provided and everything will be fine
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }

        checkForAssertionsEnabled();

        final String cmd = args[0];
        final String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        if ("export".startsWith(cmd)) {
            export(cmdArgs);
        }
        else if ("import".startsWith(cmd)) {
            import_(cmdArgs);
        }
        else if ("inspect".startsWith(cmd)) {
            inspect(cmdArgs);
        }
        else if ("correct".startsWith(cmd)) {
            correct(cmdArgs);
        }
        else if ("fixIndex".startsWith(cmd)) {
            fixIndex(cmdArgs);
        }
        else if ("changeIndex".startsWith(cmd)) {
            changeIndex(cmdArgs);
        }
        else if ("fixTimes".startsWith(cmd)) {
            fixTimes(cmdArgs);
        }
        else if ("fixDuplicates".startsWith(cmd)) {
            fixDuplicates(cmdArgs);
        }
        else if ("symbols".startsWith(cmd)) {
            symbols(cmdArgs);
        }
        else if ("delete".startsWith(cmd)) {
            System.err.println();
            System.err.println("delete command is deprecated. Please use deleteTicks");
            // delete(cmdArgs);
        }
        else if ("deleteTicks".startsWith(cmd)) {
            deleteTicks(cmdArgs);
        }
        else if ("merge".startsWith(cmd)) {
            merge(cmdArgs);
        }
        else if ("dump".startsWith(cmd)) {
            dump(cmdArgs);
        }
        else if ("explain".startsWith(cmd)) {
            explain(cmdArgs);
        }
        else if ("top".startsWith(cmd)) {
            top(cmdArgs);
        }
        else {
            usage();
        }
    }

    private static void usage() {
        System.err.println("Usage: TickCli <command> <options>");
        System.err.println("commands and their resp. options are:");
        System.err.println(" inspect [-num n] <td3/tdz-File>");
        System.err.println(" -- shows information about the file's content");
        System.err.println(" -- -num show top n symbols, default is 20");
        System.err.println();
        System.err.println(" correct <tdz-directory> <patchfile>");
        System.err.println(" -- apply corrections from patchfile to tick file(s) in tdz-directory");
        System.err.println("    and move patchfile to tdz-directory/patches");
        System.err.println();
        System.err.println(" export [(-k keyfile|--export-all-symbols)] [-c] [--with-field-names] [-o <outfile>] [(-p <pattern>|-q <text>)] <td3/tdz-File> <vwdcode>+");
        System.err.println(" -- export ticks for a number of vwdcodes");
        System.err.println(" -- to stdout or the file defined using -o option");
        System.err.println(" -- -c do NOT use corrections file (default is to use it)");
        System.err.println(" -- -p export line if pattern.matcher(line).find() is true");
        System.err.println(" -- -q export line if line.contains(text)");
        System.err.println(" -- --with-field-names output ADF field names instead of IDs (default: false)");
        System.err.println(" -- --export-all-symbols exports all data for all symbols (use with care!). Mutually exclusive with -k and providing vwdcodes on the command line.");
        System.err.println();
        System.err.println(" import [-n] [--with-field-names] <tdz-File> <export-File>+");
        System.err.println(" -- import data from a number of (edited) export files");
        System.err.println(" -- into a tdz file");
        System.err.println(" -- -n requests a dry-run, the tdz-File will not be modified");
        System.err.println(" -- --with-field-names expect ADF field names instead of IDs (default: false)");
        System.err.println();
        System.err.println(" symbols <td3/tdz-File>");
        System.err.println(" -- print all symbols with ticks in that file");
        System.err.println();
        System.err.println(" changeIndex [-a <aliasSpec>] [-r <renameSpec>] <inputSpec>+");
        System.err.println(" -- modifies index Entries");
        System.err.println(" -- options:");
        System.err.println(" -- -a add alias(es): new symbols that share same ticks");
        System.err.println(" --    <aliasSpec> vwdcode=alias or properties file with aliases");
        System.err.println(" -- -r rename symbol(s)");
        System.err.println(" --    <renameSpec> vwdcode=newCode or properties file with renames");
        System.err.println(" --                 an empty newCode can be used to delete the entry");
        System.err.println(" -- <inputSpec>");
        System.err.println(" --    tick file, tick directory, or text file with file/dir names");
        System.err.println();
        System.err.println(" merge <tdz-1-In> <tdz-2-In> <tdz-Out>");
        System.err.println(" -- merges ticks from two input files");
        System.err.println();
        System.err.println(" fixTimes [options] <tdz-In> <tdz-Out>");
        System.err.println(" -- fix timestamps in tickfile");
        System.err.println(" -- options:");
        System.err.println(" -- -t fix only trade ticks");
        System.err.println(" -- -f <fid> use value from this field as new time, ignored if -o is used");
        System.err.println(" -- -o <offset> add offset seconds to original tick time");
        System.err.println();
        System.err.println(" deleteTicks [options] <tdz-In> <tdz-Out>");
        System.err.println(" -- delete ticks for a certain time range in tickfiles (R-72396)");
        System.err.println(" -- options:");
        System.err.println(" -- -k <keyfile> delete ticks for vwdcodes in this file, required unless --all-market is given");
        System.err.println(" -- --all-market delete ticks for all vwdcodes in this file - this overrides the keyfile");
        System.err.println(" -- -s <starttime> delete ticks >= starttime, required (e.g., 17:00:00)");
        System.err.println(" -- -e <endtime> delete ticks < endtime, required (e.g., 17:30:00)");
        System.err.println();
        System.err.println(" fixDuplicates [options] <tdz-In> <tdz-Out>");
        System.err.println(" -- remove duplicate ticks - which occur if two linehandlers");
        System.err.println(" -- run in parallel for the same market (e.g., R-72292)");
        System.err.println(" -- options:");
        System.err.println(" -- -s <starttime> only ticks >= starttime, default 00:00:00");
        System.err.println(" -- -e <endtime> only ticks < endtime, default 24:00:00");
        System.err.println();
        System.err.println(" dump <td3/tdz-File> <vwdcode> <outfile>?");
        System.err.println(" -- dump raw ticks for a vwdcode");
        System.err.println(" -- to outfile or <vwdcode>.bin");
        System.err.println();
        System.err.println(" explain <td3/tdz-File> <vwdcode>+");
        System.err.println(" -- shows info about how ticks are stored");
        System.err.println();
        System.err.println(" top [options] <tick-directory>");
        System.err.println(" -- prints symbols with most tick bytes");
        System.err.println(" -- options:");
        System.err.println(" -- -n <number> number of symbols to print, default is 100");
        System.err.println(" -- -o <outfile> default is stdout");
        System.err.println();
    }

    @Deprecated
    private static void delete(String[] args) throws IOException {
        final File f = new File(args[0]);
        final Matcher m = Pattern.compile("(.+)-(20[0-9]{6}).td[3z]").matcher(f.getName());
        if (!m.matches()) {
            System.err.println("not a valid tick File: " + f.getName());
            return;
        }

        final ByteString marketName = new ByteString(m.group(1));
        final int yyyymmdd = Integer.parseInt(m.group(2));

        FeedDataRepository registry = new FeedDataRepository(8192);
        registry.setDataFactory(OrderedFeedDataFactory.RT_TICKS);
        final FileTickStore store = new FileTickStore();
        store.setTypeMapping(TickCli.DUMMY_TYPE_MAPPING);
        store.setRegistry(registry);
        store.addFile(f, yyyymmdd);

        System.out.println("read " + registry.getNumSymbols() + " entries");

        final FeedMarket market = registry.getMarket(marketName);
        final TickFile tf = store.getTickFile(MarketDay.create(marketName, yyyymmdd));
        final long indexPosition = tf.getIndexPosition();

        int numDeleted = 0;
        for (int i = 1; i < args.length; i++) {
            final String vwdcode = args[i];
            if (registry.unregister(VendorkeyVwd.getInstance(TickCli.DUMMY_TYPE_MAPPING + "." + vwdcode))) {
                System.out.println("Removed '" + vwdcode + "'");
                numDeleted++;
            }
            else {
                System.err.println("No such key: '" + vwdcode + "'");
            }
        }

        if (numDeleted > 0) {
            tf.write(ByteBuffer.allocate(0)); // ensure file is open for writing
            final List<FeedData> elements = market.getElements(true);
            new TickFileIndexWriter(tf, indexPosition).append(elements, yyyymmdd);
            tf.setIndexPosition(indexPosition);
            tf.close();
            System.out.println("wrote " + elements.size() + " index entries");
        }
    }

    static void top(String[] args) throws IOException {
        class Item implements Comparable<Item> {
            final ByteString key;
            final int size;

            Item(ByteString key, int size) {
                this.key = key;
                this.size = size;
            }

            @Override
            public int compareTo(Item o) {
                int cmp = Integer.compare(this.size, o.size);
                if (cmp != 0) {
                    return -cmp;
                }
                return this.key.compareTo(o.key);
            }
        }
        PrintWriter pw = new PrintWriter(System.out);
        int num = 100;
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            if (args[i].equals("-n")) {
                num = Integer.parseInt(args[++i]);
            }
            if (args[i].equals("-o")) {
                pw = new PrintWriter(new File(args[++i]));
            }
            i++;
        }
        IntSummaryStatistics allStats = new IntSummaryStatistics();
        PagedResultSorter<Item> prs = new PagedResultSorter<>(0, num, Item::compareTo);
        File[] files = new File(args[i])
                .listFiles(f -> TICK_FILE_NAME.matcher(f.getName()).matches());
        for (File f : files) {
            try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
                TickFileIndexReader.readEntries(fc, new DefaultIndexHandler(f) {
                    @Override
                    protected void doHandle(ByteString vwdcode, long position, int length) {
                        prs.add(new Item(vwdcode, length));
                        allStats.accept(length);
                    }
                });
            }
        }
        IntSummaryStatistics topStats = new IntSummaryStatistics();
        for (Item item : prs.getResult()) {
            pw.println(item.key + " " + item.size);
            topStats.accept(item.size);
        }
        pw.close();
        System.out.println("all: " + allStats);
        System.out.println("top: " + topStats);
    }

    static void explain(String[] args) throws IOException {
        final File f = new File(args[0]);
        final FileTickStore fts = new FileTickStore();
        final String key = FileTickStore.toVwdcodeWithoutMarket(args[1]);

        final long[] addressAndLength;
        try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
            addressAndLength = new TickFileIndexReader(fc).find(new ByteString(key));
            if (addressAndLength == null) {
                System.out.println("no data for " + key + " in " + f.getName());
                return;
            }

            final StringBuilder sb = new StringBuilder(200).append("length=").append(addressAndLength[1]);
            final int lengthInFile = fts.explain(f, addressAndLength[0], sb);
            System.out.println(sb);

            if (lengthInFile < addressAndLength[1]) {
                final ByteBuffer bb = ByteBuffer.allocate(lengthInFile);
                FileTickStore.fillData(fc, addressAndLength[0], bb);
                bb.flip();
                final TickDecompressor td = new TickDecompressor(bb.array(), TickFiles.getItemType(f));
                LineBuilder lb = new LineBuilder();
                for (Iterator<TickDecompressor.Element> it = td.iterator(true); it.hasNext(); ) {
                    TickDecompressor.Element e = it.next();
                    String line = lb.build(e.getFlags(), e.getData());
                    System.out.println(line);
                }
            }
        }
    }

    static void dump(String[] args) throws IOException {
        final File f = new File(args[0]);
        final FileTickStore fts = new FileTickStore();
        final String key = args[1];
        final byte[] bytes = fts.readTicks(f, key);
        if (bytes == null) {
            System.err.println("No ticks for " + key);
            return;
        }
        final File out = new File(args.length > 2 ? args[2] : args[1] + f.getName().substring(f.getName().lastIndexOf('.')));
        FileCopyUtils.copy(bytes, out);
        System.out.println("Stored " + bytes.length + " bytes in " + out.getName());
    }


    private static void export(String[] args) throws IOException {
        PrintWriter pw = new PrintWriter(System.out);
        Pattern pattern = null;
        boolean withCorrections = true;
        boolean useFieldNames = false;
        boolean exportAllSymbols = false;
        List<String> keyList = null;
        int i = 0;
        while (i < args.length && args[i].startsWith("-")) {
            if ("-o".equals(args[i])) {
                pw = new PrintWriter(new File(args[++i]), UTF_8.name());
            }
            else if ("-p".equals(args[i])) {
                pattern = Pattern.compile(args[++i]);
            }
            else if ("-q".equals(args[i])) {
                pattern = Pattern.compile(Pattern.quote(args[++i]));
            }
            else if ("-c".equals(args[i])) {
                withCorrections = false;
            }
            else if ("-k".equals(args[i])) {
                keyList = Files.readAllLines(new File(args[++i]).toPath(), UTF_8);
            }
            else if ("--with-field-names".equals(args[i])) {
                useFieldNames = true;
            }
            else if ("--export-all-symbols".equals(args[i])) {
                exportAllSymbols = true;
            }
            i++;
        }

        final File f = new File(args[i++]).getAbsoluteFile();
        TickDirectory dir = withCorrections && f.getParentFile().getName().startsWith("20")
                ? TickDirectory.open(f.getParentFile()) : null;
        System.err.println("withCorrections=" + withCorrections + ", dir=" + dir);

        final FileTickStore fts = new FileTickStore();
        Iterable<String> keys;
        if (exportAllSymbols) {
            List<String> allSymbols = getAllSymbols(f);
            keys = allSymbols;
            System.err.println("Exporting data for " + allSymbols.size() + " symbols (that's all of them ;-) )");
        } else {
            keys = (keyList != null) ? keyList : Arrays.asList(Arrays.copyOfRange(args, i, args.length));
        }
        for (String key : keys) {
            final byte[] bytes = (dir != null) ? dir.readTicks(fts, key) : fts.readTicks(f, key);
            if (bytes == null) {
                System.err.println("No ticks for " + key);
                continue;
            }

            pw.println("# " + key);
            final TickDecompressor td = new TickDecompressor(bytes, TickFiles.getItemType(f));
            LineBuilder lb = new LineBuilder();
            lb.useFieldNames = useFieldNames;
            try {
                for (Iterator<TickDecompressor.Element> it = td.iterator(true); it.hasNext(); ) {
                    TickDecompressor.Element e = it.next();
                    String line = lb.build(e.getFlags(), e.getData());
                    if (pattern == null || pattern.matcher(line).find()) {
                        pw.println(line);
                    }
                }
            } catch (Throwable e) {
                System.out.println("Processing aborted abnormally! Closing incomplete export file.");
                e.printStackTrace();
                pw.close();
                System.exit(-1);
            }
        }
        pw.close();
    }

    public static void import_(String[] cmdArgs) throws IOException {
        int i = 0;
        boolean dryRun = false;
        boolean useFieldNames = false;
        while (i < cmdArgs.length && cmdArgs[i].startsWith("-")) {
            if ("-n".equals(cmdArgs[i])) {
                System.out.println("DRY-RUN");
                dryRun = true;
            }
            else if ("--with-field-names".equals(cmdArgs[i])) {
                useFieldNames = true;
            }
            ++i;
        }
        final File f = new File(cmdArgs[i++]);
        final Matcher m = Pattern.compile("(.+)-(20[0-9]{6}).tdz").matcher(f.getName());
        if (!m.matches()) {
            System.err.println("not a *.tdz File: " + f.getName());
            return;
        }

        final Importer importer = new Importer(f, new ByteString(m.group(1)), Integer.parseInt(m.group(2)), dryRun, useFieldNames);
        while (i < cmdArgs.length) {
            importer.doImport(new File(cmdArgs[i++]));
        }
        importer.close();
    }

    private static void changeIndex(String[] args) throws Exception {
        ChangeIndex ci = new ChangeIndex();
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-r".equals(args[n])) {
                ci.addRename(args[++n]);
            }
            else if ("-a".equals(args[n])) {
                ci.addAlias(args[++n]);
            }
            else {
                throw new IllegalArgumentException("Unknown option " + args[n]);
            }
            n++;
        }
        if (!ci.argsOk || ci.markets.isEmpty()) {
            return;
        }
        while (n < args.length) {
            ci.changeIndexFor(new File(args[n++]));
        }
    }

    private static void fixIndex(String[] cmdArgs) throws Exception {
        for (String arg : cmdArgs) {
            File f = new File(arg);
            if (f.isDirectory()) {
                final File[] files = f.listFiles((dir, name) -> {
                    return TICK_FILE.matcher(name).matches();
                });
                for (File file : files) {
                    fixIndex(file);
                }
            }
            else {
                fixIndex(f);
            }
        }
    }

    private static void fixIndex(File f) throws Exception {
        final Matcher matcher = TICK_FILE.matcher(f.getName());
        if (!matcher.matches() || f.length() < (1024 * 1024)) {
            return;
        }

        ByteString marketName = new ByteString(matcher.group(1));
        int yyyymmdd = Integer.parseInt(matcher.group(2));

        Thread.sleep(50);
        final int depth;
        try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
            depth = new TickFileIndexReader(fc).depth();
        }
        if (depth <= 2) {
            return;
        }
        System.out.println("Fixing " + f.getName() + "...");

        FeedDataRepository registry = new FeedDataRepository(1 << 20);
        registry.setDataFactory(OrderedFeedDataFactory.RT_TICKS);
        final FileTickStore store = new FileTickStore();
        store.setTypeMapping(TickCli.DUMMY_TYPE_MAPPING);
        store.setRegistry(registry);
        store.addFile(f, yyyymmdd);

        System.out.println("read " + registry.getNumSymbols() + " entries");

        final FeedMarket market = registry.getMarket(marketName);
        final TickFile tf = store.getTickFile(MarketDay.create(marketName, yyyymmdd));
        final long indexPosition = tf.getIndexPosition();

        tf.write(ByteBuffer.allocate(0)); // ensure file is open for writing
        final List<FeedData> elements = market.getElements(true);
        new TickFileIndexWriter(tf, indexPosition).append(elements, yyyymmdd);
        tf.setIndexPosition(indexPosition);
        tf.close();
        System.out.println("wrote " + elements.size() + " index entries, fixed index");
    }

    private static void fixTimes(String[] args) throws Exception {
        int n = 0;
        int flags = 0;
        int timeOid = 0;
        int offset = 0;

        while (n < args.length && args[n].startsWith("-")) {
            if ("-t".equals(args[n])) {
                flags = FLAG_WITH_TRADE;
            }
            else if ("-f".equals(args[n])) {
                final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(args[++n]);
                if (field == null) {
                    throw new IllegalArgumentException("Unknown field " + args[n]);
                }
                timeOid = VwdFieldOrder.getOrder(field.id());
            }
            else if ("-o".equals(args[n])) {
                offset = Integer.parseInt(args[++n]);
            }
            n++;
        }
        if (timeOid == 0 && offset == 0) {
            throw new IllegalArgumentException("neither field nor offset specified");
        }

        while (n + 1 < args.length) {
            new TimeFixer(flags, timeOid, offset).fix(new File(args[n++]), new File(args[n++]));
        }
    }

    private static void deleteTicks(String[] args) throws Exception {
        int i = 0;
        int startTime = -1;
        int endTime = -1;
        List<String> keyList = null;
        boolean allMarket = false;

        while (i < args.length && args[i].startsWith("-")) {
            if ("-k".equals(args[i])) {
                keyList = Files.readAllLines(new File(args[++i]).toPath(), UTF_8);
            }
            else if ("--all-market".equals(args[i])) {
                allMarket = true;
            }
            else if ("-s".equals(args[i])) {
                startTime = LocalTime.parse(args[++i]).getMillisOfDay() / 1000;
            }
            else if ("-e".equals(args[i])) {
                endTime = LocalTime.parse(args[++i]).getMillisOfDay() / 1000;
            }
            i++;
        }
        if (startTime == -1) {
            throw new IllegalArgumentException("no startTime (-s)");
        }
        if (endTime == -1) {
            throw new IllegalArgumentException("no endTime (-e)");
        }

        File in = new File(args[i++]);
        File out = new File(args[i]);
        if (allMarket) {
            keyList = getAllSymbols(in);
        }
        new TickDeleter(keyList, startTime, endTime).deleteTicks(in, out);
    }

    private static void fixDuplicates(String[] args) throws Exception {
        int n = 0;
        int startTime = 0;
        int endTime = 86400;

        while (n < args.length && args[n].startsWith("-")) {
            if ("-s".equals(args[n])) {
                startTime = LocalTime.parse(args[++n]).getMillisOfDay() / 1000;
            }
            else if ("-e".equals(args[n])) {
                endTime = LocalTime.parse(args[++n]).getMillisOfDay() / 1000;
            }
            else {
                throw new IllegalArgumentException("unknown flag: " + args[n]);
            }
            n++;
        }
        while (n + 1 < args.length) {
            new DuplicateFixer(startTime, endTime).fix(new File(args[n++]), new File(args[n++]));
        }
    }

    private static void correct(String[] args) throws Exception {
        File tdzDir = new File(args[0]).getAbsoluteFile();
        File patchFile = new File(args[1]);

        HistoricTickCorrections htc = new HistoricTickCorrections();
        htc.setBaseDirectories(Collections.singleton(tdzDir.getParentFile()));
        htc.setStore(new FileTickStore());
        htc.applyCorrection(Integer.parseInt(tdzDir.getName()), patchFile);
    }

    private static void inspect(String[] args) throws Exception {
        class Item implements Comparable<Item> {
            ByteString key;

            int length;

            Item(ByteString key, int length) {
                this.key = key;
                this.length = length;
            }

            @Override
            public int compareTo(Item o) {
                return o.length - this.length;
            }
        }

        final List<Item> items = new ArrayList<>();

        int num = 20;
        int i = 0;
        if ("-num".startsWith(args[0])) {
            num = Integer.parseInt(args[1]);
            i = 2;
        }

        final File f = new File(args[i]);
        final long[] sum = new long[1];
        try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
            TickFileIndexReader.readEntries(fc, new DefaultIndexHandler(f) {
                @Override
                protected void doHandle(ByteString vwdcode, long position, int length) {
                    items.add(new Item(vwdcode, length));
                    sum[0] += length;
                }
            });
        }
        items.sort(null);
        System.out.println("---------------------------");
        System.out.println("#symbols        : " + items.size());
        System.out.println("#tick bytes avg : " + (sum[0] / items.size()));
        System.out.println("#tick bytes mean: " + items.get(items.size() / 2).length);
        System.out.println("---------------------------");
        for (int j = 0, n = Math.min(items.size(), num); j < n; j++) {
            final Item item = items.get(j);
            System.out.printf("%-24s %8d%n", item.key, item.length);
        }
    }

    private static List<String> getAllSymbols(File tickFile) throws IOException {
        try (FileChannel fc = new RandomAccessFile(tickFile, "r").getChannel()) {
            final String dottedMarket = "." + TickFiles.getMarketName(tickFile);
            List<String> allKeys = new ArrayList<>();
            StringBuilder complexNameRestorer = new StringBuilder(1024);
            TickFileIndexReader.readEntries(fc, (key, position, length) -> {

                // Symbol names are stored without market name in the index so we need to restore original here
                // See: de.marketmaker.istar.feed.ordered.tick.TickFileIndexWriter#removeMarket()

                int dot = key.indexOf('.');
                if (dot == -1) {
                    // Simple case: remaining name is just the symbol so just add ".MARKTNAME" at the end
                    // Example: 710000.ETR was shorted to just 710000
                    allKeys.add(key + dottedMarket);
                }
                else {
                    // If it contains a dot we need to insert ".MARKETNAME" right at that location
                    // Example: ABC.DTB.CON.105 was shorted to just ABC.CON.105
                    complexNameRestorer.setLength(0);
                    complexNameRestorer.append(key);
                    complexNameRestorer.insert(dot, dottedMarket);
                    allKeys.add(complexNameRestorer.toString());
                }
            });
            Collections.sort(allKeys);
            return allKeys;
        }
    }

    private static void symbols(String[] args) throws Exception {
        final File f = new File(args[0]);
        getAllSymbols(f).forEach(System.out::println);
    }

    private static void merge(String[] args) throws Exception {
        if (args.length != 3) {
            usage();
            return;
        }
        File in1 = new File(args[0]);
        if (!in1.isFile() && in1.getName().endsWith(".tdz")) {
            System.err.println("arg[0] is not a valid tdz file");
            return;
        }
        File in2 = new File(args[1]);
        if (!in2.isFile() && in2.getName().endsWith(".tdz")) {
            System.err.println("arg[1] is not a valid tdz file");
            return;
        }
        File out = new File(args[2]);
        if (!out.getName().endsWith(".tdz")) {
            System.err.println("out must a tdz file");
            return;
        }
        if (out.exists()) {
            System.err.println("out file exists");
            return;
        }

        new Merger().merge(in1, in2, out);
    }

    public static class LineBuilder {
        private final StringBuilder sb = new StringBuilder(80);

        private boolean useFieldNames = false;

        String build(int flags, int time, FieldData fd) {
            this.sb.setLength(0);
            appendFlags(sb, flags);
            this.sb.append(';');
            appendTime(sb, time);
            appendFields(fd);
            return sb.toString();
        }

        public String build(int flags, FieldData fd) {
            this.sb.setLength(0);
            appendFlags(sb, flags);
            if ((flags & FLAGS_WITH_TICK) != 0) { // == 0 is for professional trades which do not start with time
                this.sb.append(';');
                appendTime(sb, fd.getInt());
            }
            appendFields(fd);
            return sb.toString();
        }

        private void appendFields(FieldData fd) {
            for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
                this.sb.append(';');
                VwdFieldDescription.Field field = VwdFieldOrder.getField(fd.getId());
                if (field == null) {
                    continue;
                }
                if (this.useFieldNames) {
                    this.sb.append(field.name());
                } else {
                    this.sb.append(field.id());
                }
                this.sb.append('=');
                formatData(sb, fd, field, false);
            }
        }

        public static void formatData(StringBuilder sb, FieldData fd, VwdFieldDescription.Field field, boolean quotedEscapes) {
            switch (fd.getType()) {
                case FieldData.TYPE_INT:
                    if (field.type() == VwdFieldDescription.Type.UINT) {
                        sb.append(fd.getUnsignedInt());
                    } else {
                        sb.append(fd.getInt());
                    }
                    break;
                case FieldData.TYPE_TIME:
                    appendTime(sb, fd.getInt());
                    break;
                case FieldData.TYPE_PRICE:
                    final BigDecimal bd = BigDecimal.valueOf(fd.getInt(), -fd.getByte());
                    sb.append(bd.toPlainString());
                    break;
                case FieldData.TYPE_STRING:
                    final byte[] bs = fd.getBytes();
                    appendEscaped(sb, OrderedSnapRecord.toString(bs), quotedEscapes);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        private static void appendEscaped(StringBuilder sb, String s, boolean quoted) {
            if (quoted) {
                sb.append('"');
            }
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == ';' || c == '\\') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            if (quoted) {
                sb.append('"');
            }
        }

        public static void appendTime(StringBuilder sb, int time) {
            final int secs = MdpsFeedUtils.decodeTime(time);
            final int ms = MdpsFeedUtils.decodeTimeMillis(time);
            sb.append(TimeFormatter.formatSecondsInDay(secs));
            if (ms != 0) {
                sb.append('.');
                if (ms < 100) {
                    sb.append((ms < 10) ? "00" : "0");
                }
                sb.append(ms);
            }
        }

        public static void appendFlags(StringBuilder sb, int flags) {
            if (isSet(flags, FLAGS_TICK_CORRECTION)) {
                if (isSet(flags, FLAG_TICK_CORRECTION_DELETE)) {
                    sb.append('-');
                }
                if (isSet(flags, FeedUpdateFlags.FLAG_TICK_CORRECTION_INSERT)) {
                    sb.append('+');
                }
                sb.append(';');
            }
            sb.append(isSet(flags, FLAG_WITH_TRADE)         ? 'T' : '-');
            sb.append(isSet(flags, FLAG_WITH_BID)           ? 'B' : '-');
            sb.append(isSet(flags, FLAG_WITH_ASK)           ? 'A' : '-');
            sb.append(isSet(flags, FLAG_WITH_TICK_FIELD)    ? 'X' : '-');
        }

        public static boolean isSet(int flags, int flag) {
            return (flags & flag) != 0;
        }
    }

    static class LineParser {
        FieldDataBuilder builder = new FieldDataBuilder(256);

        int matchCount;

        int flags;

        private String line;

        private int k;

        private boolean useFieldNames = false;

        void parse(String line) {
            this.line = line;
            this.k = 0;
            parseMatchCount();
            parseFlags();
            parseFields();
        }

        private FieldDataBuilder parseFields() {
            this.builder.reset();
            this.builder.putInt(parseTime());
            while (nextIf(';')) {
                VwdFieldDescription.Field field;
                if (this.useFieldNames) {
                    String fieldName = getFieldName();
                    field = VwdFieldDescription.getFieldByName(fieldName);
                } else {
                    final int fid = (int)nextNum();
                    field = VwdFieldDescription.getField(fid);
                }
                consume('=');
                final int order = VwdFieldOrder.getOrder(field.id());
                switch (field.type()) {
                    case DATE:
                    case USHORT:
                    case UINT:
                        builder.putIntFid(order);
                        builder.putInt((int)nextNum());
                        break;
                    case TIME:
                        builder.putTimeFid(order);
                        builder.putInt(parseTime());
                        break;
                    case PRICE:
                        builder.putPriceFid(order);
                        builder.putPrice(parsePrice());
                        break;
                    case STRING:
                        builder.putStringFid(order);
                        builder.putString(parseString());
                        break;
                }
            }
            return builder;
        }

        private String getFieldName() {
            byte[] key = parseString('=');
            return new String(key, OrderedSnapRecord.DEFAULT_CHARSET);
        }

        private byte[] parseString() {
            return parseString(';');
        }

        private byte[] parseString(char delimiter) {
            StringBuilder sb = new StringBuilder();
            while (hasNext() && !peek(delimiter)) {
                char c = next();
                sb.append(c != '\\' ? c : next());
            }
            return sb.toString().getBytes(OrderedSnapRecord.DEFAULT_CHARSET);
        }

        private long parsePrice() {
            final int from = k;
            while (hasNext() && !peek(';')) {
                next();
            }
            return MdpsFeedUtils.encodePrice(new BigDecimal(line.substring(from, k)));
        }

        private int parseTime() {
            int hh = (int)nextNum();
            if (hh == -1) {
                return -1;
            }
            int mm = (int)consume(':').nextNum();
            int ss = (int)consume(':').nextNum();
            int ms = nextIf('.') ? (int)nextNum() : 0;
            return MdpsFeedUtils.encodeTime(hh, mm, ss, ms);
        }

        private void parseMatchCount() {
            if (nextIf('(')) {
                this.matchCount = (int)nextNum();
                consume(')');
            }
            else {
                this.matchCount = 1;
            }
        }

        private void parseFlags() {
            this.flags = parseCorrectionFlags();
            if ('T' == next()) this.flags += FLAG_WITH_TRADE;
            if ('B' == next()) this.flags += FLAG_WITH_BID;
            if ('A' == next()) this.flags += FLAG_WITH_ASK;
            if ('X' == next()) this.flags += FLAG_WITH_TICK_FIELD;
            consume(';');
        }

        private int parseCorrectionFlags() {
            // new format: correction flags are separated from tick flags by ';'
            if (peek(1) == ';' || peek(2) == ';') {
                int result = 0;
                while (!peek(';')) {
                    result |= parseCorrectionFlag();
                }
                consume(';');
                return result;
            }

            // old format: correction flags precede the 4 tick flags but are not separated
            switch (this.line.indexOf(';', this.k + 4) - this.k) {
                case 4:
                    return 0;
                case 5:
                    return parseCorrectionFlag();
                case 6:
                    return parseCorrectionFlag() | parseCorrectionFlag();
                default:
                    throw new IllegalStateException("line must start with 4-6 flag chars, this doesn't: " + this.line);
            }
        }

        private int parseCorrectionFlag() {
            final char c = next();
            switch (c) {
                case '-':
                    return FLAG_TICK_CORRECTION_DELETE;
                case '+':
                    return FLAG_TICK_CORRECTION_INSERT;
                default:
                    throw new IllegalStateException("correction flag '" + c + "' not in [-+]");
            }
        }

        private LineParser consume(char c) {
            if (!hasNext()) {
                throw new IllegalStateException("eol");
            }
            if (!peek(c)) {
                throw new IllegalStateException("'" + peek() + "' <> '" + c + "'");
            }
            this.k++;
            return this;
        }

        private long nextNum() {
            final boolean neg = nextIf('-');
            long n = 0;
            while (hasNext() && Character.isDigit(peek())) {
                n = (n * 10L) + (next() - '0');
            }
            return neg ? -n : n;
        }

        private char next() {
            return this.line.charAt(this.k++);
        }

        private boolean nextIf(char c) {
            if (hasNext() && peek(c)) {
                next();
                return true;
            }
            return false;
        }

        private boolean peek(char c) {
            return c == peek();
        }

        private char peek(int offset) {
            return this.line.charAt(this.k + offset);
        }

        private char peek() {
            return this.line.charAt(this.k);
        }

        private boolean hasNext() {
            return k < this.line.length();
        }
    }

    private static class Importer {
        private final TickDeflater deflater;

        private final long oldIndexPosition;

        private final FeedDataRepository registry;

        private final TickFile tf;

        private final FeedMarket market;

        private final int yyyymmdd;

        private final boolean useFieldNames;

        Importer(File f, ByteString marketName, int yyyymmdd, boolean dryRun, boolean useFieldNames) throws IOException {
            registry = new FeedDataRepository(8192);
            registry.setDataFactory(OrderedFeedDataFactory.RT_TICKS);
            final FileTickStore store = new FileTickStore();
            store.setTypeMapping(TickCli.DUMMY_TYPE_MAPPING);
            store.setRegistry(registry);
            this.yyyymmdd = yyyymmdd;
            if (!f.exists()) {
                if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
                    throw new IOException("failed create necessary tick folder: " + f.getParent());
                }
                this.tf = new TickFile(f);
                registry.registerMarket(marketName);
            }
            else {
                store.addFile(f, yyyymmdd);
                this.tf = store.getTickFile(MarketDay.create(marketName, yyyymmdd));
            }

            this.market = registry.getMarket(marketName);

            this.oldIndexPosition = tf.getIndexPosition();
            this.deflater = new TickDeflater(dryRun ? null : tf, oldIndexPosition);
            this.useFieldNames = useFieldNames;

            System.out.println("read " + registry.getNumSymbols() + " entries, index @" + oldIndexPosition);
        }

        void doImport(File f) throws IOException {
            final LineParser parser = new LineParser();
            parser.useFieldNames = this.useFieldNames;

            FeedData fd = null;
            boolean symbolHasData = false;
            VendorkeyVwd vendorKey = null;
            try (Scanner sc = new Scanner(f, UTF_8.name())) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    if (StringUtils.isEmpty(line)) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        // If there was no data for this symbol we need to deregister it
                        // otherwise it will end up as empty entry in the index and that
                        // makes for an invalid TDZ file - chicago would filter them also
                        if (!symbolHasData && vendorKey != null) {
                            registry.unregister(vendorKey);
                        } else {
                            flush(fd);
                        }

                        final String vwdcode = line.substring(1).trim();
                        vendorKey = VendorkeyVwd.getInstance(TickCli.DUMMY_TYPE_MAPPING + "." + vwdcode);
                        symbolHasData = false;
                        fd = registry.register(vendorKey);
                        if (fd.getMarket() != market) {
                            System.err.println(vwdcode + " cannot be imported into " + market);
                            fd = null;
                        }
                        else {
                            deflater.reset();
                        }
                        continue;
                    }
                    if (fd == null) {
                        continue;
                    }
                    parser.parse(line);
                    symbolHasData = true;
                    this.deflater.add(parser.flags, parser.builder);
                }
            }
            if (!symbolHasData && vendorKey != null) {
                registry.unregister(vendorKey);
            } else {
                flush(fd);
            }

            deflater.flushWriteBuffer();
        }

        private void flush(FeedData fd) throws IOException {
            if (fd != null) {
                final OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
                this.deflater.flushCompressedTicks();
                td.setDate(yyyymmdd);
                td.setStoreAddress(deflater.getFileAddress());
                td.setLength(deflater.getNumTickBytes());
                System.out.println(fd.getVendorkey() + " #" + td.getLength(yyyymmdd)
                        + ", @" + Long.toHexString(td.getStoreAddress(yyyymmdd)));
            }
        }

        void close() throws IOException {
            final long newIndexPosition = oldIndexPosition + deflater.getNumBytesOut();
            System.out.println("Encoded " + deflater.getNumBytesOut() + " bytes");

            // In case we found symbols with no data we unregistered them
            // so we need to run internal GC first before writing the index
            this.registry.gc();

            if (tf.isOpen()) {
                final List<FeedData> elements = market.getElements(true);
                new TickFileIndexWriter(tf, newIndexPosition).append(elements, yyyymmdd);
                tf.setIndexPosition(newIndexPosition);
                tf.close();
                System.out.println("wrote index with " + elements.size() + " elements @" + newIndexPosition);
            }
            else {
                System.out.println("did not import anything");
            }
        }

    }
}

class ChangeIndex {

    private static final ByteString TYPE = new ByteString(TickCli.DUMMY_TYPE_MAPPING + ".");

    private final Map<ByteString, ByteString> aliases = new HashMap<>();

    private final Map<ByteString, ByteString> renames = new HashMap<>();

    final Set<ByteString> markets = new HashSet<>();

    boolean argsOk = true;

    void addAlias(String s) throws IOException {
        handleOption("-a", s, aliases);
    }

    void addRename(String s) throws IOException {
        handleOption("-r", s, renames);
    }

    private void handleOption(String arg, String value,
            Map<ByteString, ByteString> map) throws IOException {
        File f = new File(value);
        if (f.canRead()) {
            Properties properties = PropertiesLoader.load(f);
            for (String name : properties.stringPropertyNames()) {
                put(name, properties.getProperty(name), map);
            }
        }
        else {
            String[] keyValue = value.split("=", 2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid input for " + arg + ":" + value);
            }
            put(keyValue[0], keyValue[1], map);
        }
    }

    private void put(String key, String value, Map<ByteString, ByteString> map) {
        VendorkeyVwd keyCode = toVwdcode(key);
        if (keyCode != null && !StringUtils.hasText(value) && map == renames) {
            map.put(keyCode.toVwdcode(), ByteString.NULL);
            markets.add(keyCode.getMarketName());
            return;
        }
        VendorkeyVwd valueCode = toVwdcode(value);
        if (keyCode != null && valueCode != null) {
            map.put(keyCode.toVwdcode(), valueCode.toVwdcode());
            markets.add(keyCode.getMarketName());
        }
    }

    private VendorkeyVwd toVwdcode(String s) {
        Matcher m = VendorkeyVwd.KEY_PATTERN.matcher(s);
        if (!m.matches()) {
            System.err.println("Invalid vendorkey '" + s + "'");
            this.argsOk = false;
            return null;
        }
        if (StringUtils.hasText(m.group(1))) {
            return VendorkeyVwd.getInstance(s);
        }
        return VendorkeyVwd.getInstance(TickCli.DUMMY_TYPE_MAPPING + "." + s);
    }

    public void changeIndexFor(File file) throws IOException {
        if (TickCli.TICK_FILE.matcher(file.getName()).matches()) {
            changeIndexForFile(file);
        }
        else if (file.isDirectory()) {
            changeIndexForFilesIn(file);
        }
        else if (file.canRead()) {
            for (String s : Files.readAllLines(file.toPath(), Charset.defaultCharset())) {
                if (StringUtils.hasText(s)) {
                    changeIndexFor(new File(s));
                }
            }
        }
    }

    private void changeIndexForFilesIn(File dir) throws IOException {
        for (File f : dir.listFiles(this::needsChange)) {
            changeIndexForFile(f);
        }
    }

    private boolean needsChange(File f) {
        Matcher m = TickCli.TICK_FILE.matcher(f.getName());
        return m.matches() && markets.contains(new ByteString(m.group(1)));
    }

    private void changeIndexForFile(File f) throws IOException {
        if (!needsChange(f)) {
            return;
        }

        Matcher m = TickCli.TICK_FILE.matcher(f.getName());
        if (!m.matches()) {
            return;
        }
        ByteString marketName = new ByteString(m.group(1));
        int yyyymmdd = Integer.parseInt(m.group(2));

        FeedDataRepository registry = new FeedDataRepository(1 << 18);
        registry.setDataFactory(OrderedFeedDataFactory.RT_TICKS);
        final FileTickStore store = new FileTickStore();
        store.setTypeMapping(TickCli.DUMMY_TYPE_MAPPING);
        store.setRegistry(registry);
        store.addFile(f, yyyymmdd);

        System.out.println("read " + registry.getNumSymbols() + " entries");

        int numChanges = 0;

        for (Map.Entry<ByteString, ByteString> e : aliases.entrySet()) {
            if (change(f, registry, e, false)) {
                numChanges++;
            }
        }
        for (Map.Entry<ByteString, ByteString> e : renames.entrySet()) {
            if (change(f, registry, e, true)) {
                numChanges++;
            }
        }

        if (numChanges == 0) {
            System.out.println("no changes for " + f.getName());
            return;
        }

        final FeedMarket market = registry.getMarket(marketName);
        final TickFile tf = store.getTickFile(MarketDay.create(marketName, yyyymmdd));
        final long indexPosition = tf.getIndexPosition();

        tf.write(ByteBuffer.allocate(0)); // ensure file is open for writing
        final List<FeedData> elements = market.getElements(true);
        new TickFileIndexWriter(tf, indexPosition).append(elements, yyyymmdd);
        tf.setIndexPosition(indexPosition);
        tf.close();

        System.out.println("applied " + numChanges + " change(s) to " + f.getName());
    }

    private boolean change(File f, FeedDataRepository registry,
            Map.Entry<ByteString, ByteString> e, boolean removeKey) {
        OrderedFeedData fd = (OrderedFeedData) registry.get(e.getKey());
        if (fd == null) {
            return false;
        }
        if (e.getValue() == ByteString.NULL) {
            registry.unregister(fd.getVendorkey());
            return true;
        }
        OrderedFeedData fd2 = (OrderedFeedData) registry.get(e.getValue());
        if (fd2 != null) {
            System.err.println(e.getValue() + " already exists in " + f.getName());
            return false;
        }
        fd2 = (OrderedFeedData) registry.register(VendorkeyVwd.getInstance(e.getValue().prepend(TYPE)));
        OrderedTickData ot = fd.getOrderedTickData();
        OrderedTickData ot2 = fd2.getOrderedTickData();
        ot2.setDate(ot.getDate());
        ot2.setStoreAddress(ot.getStoreAddress());
        ot2.setLength(ot.getLength());
        if (removeKey) {
            registry.unregister(fd.getVendorkey());
        }
        return true;
    }
}

class Merger {

    private static final byte[] NO_TICK_DATA = new byte[0];

    private FeedDataRepository repository1 = new FeedDataRepository(1 << 21);
    private FeedDataRepository repository2 = new FeedDataRepository(1 << 21);

    public void merge(File in1, File in2, File out) throws IOException {
        this.repository1.setDataFactory(OrderedFeedDataFactory.RT_NT_TICKS);
        this.repository2.setDataFactory(OrderedFeedDataFactory.RT_NT_TICKS);

        final ByteString market = new ByteString(TickFiles.getMarketName(out));

        try (final FileChannel ch1 = new RandomAccessFile(in1, "r").getChannel();
             final FileChannel ch2 = new RandomAccessFile(in2, "r").getChannel();
             final FileChannel outFileChannel = new RandomAccessFile(out, "rw").getChannel()) {

            readIndex(ch1, in1, repository1);
            readIndex(ch2, in2, repository2);

            int day1 = TickFiles.getDay(in1);
            int day2 = TickFiles.getDay(in2);

            final TickDeflater tickDeflater = new TickDeflater(outFileChannel, 0L);

            final FeedMarket m1 = repository1.getMarket(market);
            final FeedMarket m2 = repository2.getMarket(market);
            int n = 0;

            List<FeedData> elements1 = ImmutableList.copyOf(m1.getElements(true));

            // We need a modifiable list here so we can remove elements contained in both lists
            ArrayList<FeedData> elements2 = (ArrayList<FeedData>) m2.getElements(true);

            int count1 = elements1.size();
            int count2 = elements2.size();

            for (FeedData feedData : elements1) {
                final OrderedTickData orderedTickData1 = ((OrderedFeedData) feedData).getOrderedTickData();
                final int length1 = orderedTickData1.getLength(day1);
                byte[] ticks1 = new byte[length1];
                if (ticks1.length > 0) {
                    FileTickStore.fillData(ch1, orderedTickData1.getStoreAddress(day1), ByteBuffer.wrap(ticks1));
                }
                int index = Collections.binarySearch(elements2, feedData, FeedData.COMPARATOR_BY_VWDCODE);
                byte[] ticks2 = Merger.NO_TICK_DATA;
                if (index >= 0) {
                    // We remove the element so a) search times get shorter and b) we know which ones were not in the first file
                    FeedData fd2 = elements2.remove(index);
                    final OrderedTickData orderedTickData2 = ((OrderedFeedData) fd2).getOrderedTickData();
                    final int length2 = orderedTickData2.getLength(day2);
                    ticks2 = new byte[length2];
                    if (ticks2.length > 0) {
                        FileTickStore.fillData(ch2, orderedTickData2.getStoreAddress(day2), ByteBuffer.wrap(ticks2));
                    }
                }
                tickDeflater.reset();
                addTicks(tickDeflater, ticks1, ticks2);
                tickDeflater.flushCompressedTicks();

                orderedTickData1.setStoreAddress(day1, tickDeflater.getFileAddress());
                orderedTickData1.setLength(day1, tickDeflater.getNumTickBytes());

                tickDeflater.flushWriteBuffer();
                if ((++n & 0xff) == 0) {
                    System.out.println(n);
                }
            }

            int countCommon = count2 - elements2.size();

            // When we get here the remaining entries in elements2 are not contained in elements1
            for (FeedData feedData : elements2) {
                final OrderedTickData orderedTickData = ((OrderedFeedData) feedData).getOrderedTickData();
                final int length1 = orderedTickData.getLength(day1);
                byte[] ticks1 = new byte[length1];
                if (ticks1.length > 0) {
                    FileTickStore.fillData(ch2, orderedTickData.getStoreAddress(day1), ByteBuffer.wrap(ticks1));
                }
                tickDeflater.reset();
                addTicks(tickDeflater, ticks1, Merger.NO_TICK_DATA);
                tickDeflater.flushCompressedTicks();

                // We need to add this element to the first repository because it serves
                // as a source for the index below this loop
                repository1.register(feedData);

                orderedTickData.setStoreAddress(day1, tickDeflater.getFileAddress());
                orderedTickData.setLength(day1, tickDeflater.getNumTickBytes());

                tickDeflater.flushWriteBuffer();
                if ((++n & 0xff) == 0) {
                    System.out.println(n);
                }
            }

            TickFileIndexWriter w = new TickFileIndexWriter(outFileChannel, tickDeflater.getNumBytesOut());
            w.append(m1.getElements(true), day1);

            System.out.println("Merged " + count1  + " symbols from " + in1
                    + " with " + count2 + " symbols from " + in2
                    + " (common symbols: " + countCommon + ") into " + out);
        }
    }

    private void addTicks(TickDeflater td, byte[] ticks1, byte[] ticks2) throws IOException {
        final Iterator<TickDecompressor.Element> it1 = createIterator(ticks1);
        final Iterator<TickDecompressor.Element> it2 = createIterator(ticks2);

        TickDecompressor.Element e1 = next(it1);
        TickDecompressor.Element e2 = next(it2);
        long time1 = getTime(e1);
        long time2 = getTime(e2);
        while (e1 != null || e2 != null) {
            if (e1 == null) {
                td.add(e2);
                e2 = next(it2);
                time2 = getTime(e2);
            }
            else if (e2 == null) {
                td.add(e1);
                e1 = next(it1);
                time1 = getTime(e1);
            }
            else {
                if (time1 < time2) {
                    td.add(e1);
                    e1 = next(it1);
                    time1 = getTime(e1);
                }
                else {
                    td.add(e2);
                    e2 = next(it2);
                    time2 = getTime(e2);
                }
            }
        }
    }

    private long getTime(TickDecompressor.Element e) {
        return e != null ? e.getData().getUnsignedInt() : -1;
    }

    private TickDecompressor.Element next(Iterator<TickDecompressor.Element> it) {
        return it.hasNext() ? it.next() : null;
    }

    private Iterator<TickDecompressor.Element> createIterator(byte[] ticks1) {
        return ticks1.length > 0
                ? new TickDecompressor(ticks1, AbstractTickRecord.TickItem.Encoding.TICKZ).iterator()
                : Collections.<TickDecompressor.Element>emptyIterator();
    }

    private long readIndex(FileChannel arg, File f, FeedDataRepository repository) throws IOException {
        return TickFileIndexReader.readEntries(arg, new TickCli.CliIndexHandler(repository, f));
    }
}

abstract class Fixer {
    protected final FeedDataRepository repository = new FeedDataRepository(1 << 16);

    private final FieldDataBuilder builder = new FieldDataBuilder(512);

    protected Fixer() {
        this.repository.setDataFactory(OrderedFeedDataFactory.RT_TICKS);

    }

    protected long readIndex(FileChannel arg, File f) throws IOException {
        return TickFileIndexReader.readEntries(arg, new TickCli.CliIndexHandler(repository, f));
    }

    protected FieldDataBuilder buildTick(int time, BufferFieldData fd) {
        builder.reset();
        builder.putInt(time);
        for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
            builder.addFieldToBuffer(fd);
        }
        return builder;
    }

    public void fix(File in, File out) throws IOException {
        final FileChannel ch1 = new RandomAccessFile(in, "r").getChannel();
        final int day = TickFile.getDay(in);

        final ByteString market = new ByteString(TickFiles.getMarketName(out));
        readIndex(ch1, in);

        FileChannel fc = new RandomAccessFile(out, "rw").getChannel();
        final TickDeflater td = new TickDeflater(fc, 0L);

        final FeedMarket m = repository.getMarket(market);
        int total = m.size();
        int n = 0;

        for (FeedData fd : m.getElements(true)) {

            final OrderedTickData otd = ((OrderedFeedData) fd).getOrderedTickData();
            final int length = otd.getLength(day);
            byte[] ticks = new byte[length];
            if (ticks.length > 0) {
                FileTickStore.fillData(ch1, otd.getStoreAddress(day), ByteBuffer.wrap(ticks));
            }

            td.reset();
            addTicks(fd, td, createIterator(ticks));
            td.flushCompressedTicks();

            otd.setStoreAddress(day, td.getFileAddress());
            otd.setLength(day, td.getNumTickBytes());

            td.flushWriteBuffer();
            if ((++n & 0xff) == 0) {
                System.out.printf("%5d/%5d%n", n, total);
            }
        }

        TickFileIndexWriter w = new TickFileIndexWriter(fc, td.getNumBytesOut());
        w.append(m.getElements(true), day);

        fc.close();
    }

    private Iterator<TickDecompressor.Element> createIterator(byte[] ticks) {
        return new TickDecompressor(ticks, AbstractTickRecord.TickItem.Encoding.TICKZ).iterator();
    }

    protected abstract void addTicks(FeedData fd, TickDeflater td,
            final Iterator<TickDecompressor.Element> it) throws IOException;
}

class TimeFixer extends Fixer {
    private final int flags;

    private final int timeOid;

    private final int offset;

    TimeFixer(int flags, int timeOid, int offset) {
        this.flags = flags;
        this.timeOid = timeOid;
        this.offset = offset;
    }

    protected void addTicks(FeedData fd, TickDeflater td,
            final Iterator<TickDecompressor.Element> it) throws IOException {
        while (it.hasNext()) {
            final TickDecompressor.Element e = it.next();
            if (!e.hasFlag(this.flags)) {
                td.add(e);
                continue;
            }

            final BufferFieldData bfd = e.getData();
            int tickTime = bfd.getInt();

            int newTime = getAdjustedTime(bfd, tickTime);

            if (newTime == -1) {
                td.add(e);
                continue;
            }

            td.add(e.getFlags(), buildTick(newTime, bfd));
        }

    }

    private int getAdjustedTime(BufferFieldData fd, int tickTime) {
        if (this.offset != 0) {
            final int secs = MdpsFeedUtils.decodeTime(tickTime);
            final int ms = MdpsFeedUtils.decodeTimeMillis(tickTime);
            int newSecs = secs + offset;
            if (newSecs < 0 || newSecs > 86399) {
                throw new IllegalArgumentException("tickTime " + secs + " + offset " + offset
                        + " results in illegal time " + newSecs);
            }
            return MdpsFeedUtils.encodeTime(newSecs, ms);
        }
        else {
            for (int id = fd.readNext(); id != 0; id = fd.readNext()) {
                if (id == this.timeOid) {
                    int result = fd.getInt();
                    fd.rewind();
                    fd.getInt(); // skip old time
                    return result;
                }
                fd.skipCurrent();
            }
        }
        return -1;
    }
}

class TickDeleter {
    protected final FeedDataRepository repository = new FeedDataRepository(1 << 16);
    private final HashSet<String> vwdcodes;

    private final int startTime;

    private final int endTime;

    TickDeleter(List<String> vwdcodes, int startTime, int endTime) {
        this.repository.setDataFactory(OrderedFeedDataFactory.RT_TICKS);
        this.vwdcodes = new HashSet<>(vwdcodes);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private List<TickDecompressor.Element> ticksToAdd(FeedData fd, final Iterator<TickDecompressor.Element> it) {
        List<TickDecompressor.Element> ticks = new ArrayList<>();

        boolean apply = this.vwdcodes.contains(fd.getVendorkey().toVwdcode().toString());

        while (it.hasNext()) {
            final TickDecompressor.Element e = it.next();
            if (apply && isToBeDeleted(e)) {
                continue;
            }
            ticks.add(e);
        }

        return ticks;
    }

    private boolean isToBeDeleted(TickDecompressor.Element e) {
        int tickTime = e.getData().getInt();
        int secOfDay = MdpsFeedUtils.decodeTime(tickTime);
        return (secOfDay >= this.startTime) && (secOfDay < this.endTime);
    }

    public void deleteTicks(File in, File out) throws IOException {
        final FileChannel ch1 = new RandomAccessFile(in, "r").getChannel();
        final int day = TickFile.getDay(in);

        final ByteString market = new ByteString(TickFiles.getMarketName(out));
        TickFileIndexReader.readEntries(ch1, new TickCli.CliIndexHandler(repository, in));

        FileChannel fc = new RandomAccessFile(out, "rw").getChannel();
        final TickDeflater td = new TickDeflater(fc, 0L);

        final FeedMarket m = repository.getMarket(market);
        int total = m.size();
        int n = 0;

        List<FeedData> nonEmptyFeedData = new ArrayList<>();
        boolean hasAddedData = true;

        for (FeedData fd : m.getElements(true)) {

            final OrderedTickData otd = ((OrderedFeedData) fd).getOrderedTickData();
            final int length = otd.getLength(day);
            byte[] ticks = new byte[length];
            if (ticks.length > 0) {
                FileTickStore.fillData(ch1, otd.getStoreAddress(day), ByteBuffer.wrap(ticks));
            }

            if (hasAddedData) {
                td.reset();
            }

            boolean apply = this.vwdcodes.contains(fd.getVendorkey().toVwdcode().toString());

            hasAddedData = false;

            Iterator<TickDecompressor.Element> it = new TickDecompressor(ticks, AbstractTickRecord.TickItem.Encoding.TICKZ).iterator();

            while (it.hasNext()) {
                final TickDecompressor.Element e = it.next();
                if (apply && isToBeDeleted(e)) {
                    continue;
                }
                td.add(e);
                hasAddedData = true;
            }

            if (hasAddedData) {
                td.flushCompressedTicks();
            }

            otd.setStoreAddress(day, td.getFileAddress());
            otd.setLength(day, td.getNumTickBytes());

            td.flushWriteBuffer();

            if (hasAddedData) {
                nonEmptyFeedData.add(fd);
            } else {
                System.out.println("Filtering FeedData for " + fd.getVendorkey().toVwdcode().toString() + " since no ticks remained after deletion");
            }

            if ((++n & 0xff) == 0) {
                System.out.printf("%5d/%5d%n", n, total);
            }
        }

        TickFileIndexWriter w = new TickFileIndexWriter(fc, td.getNumBytesOut());
        w.append(nonEmptyFeedData, day);

        fc.close();
    }
}

class DuplicateFixer extends Fixer {
    static class Tick {
        private final int flags;

        private final int time;

        private final byte[] bytes;

        Tick(int flags, int time, byte[] bytes) {
            this.flags = flags;
            this.time = time;
            this.bytes = bytes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Tick tick = (Tick) o;

            if (flags != tick.flags) return false;
            if (time != tick.time) return false;
            if (!Arrays.equals(bytes, tick.bytes)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = flags;
            result = 31 * result + time;
            result = 31 * result + Arrays.hashCode(bytes);
            return result;
        }
    }


    private final int startTime;

    private final int endTime;

    private final BufferFieldData tmp = new BufferFieldData();

    DuplicateFixer(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Strategy: read all ticks within the same second, for each tick, try to find <em>one</em> duplicate
     * tick and set its position in the list to null. Finally, add all non-null list elements
     * to the TickDeflater
     */
    protected void addTicks(FeedData fd, TickDeflater td,
            final Iterator<TickDecompressor.Element> it) throws IOException {
        int currentTime = -1;
        List<Tick> ticks = new ArrayList<>(100);

        while (it.hasNext()) {
            final TickDecompressor.Element e = it.next();

            final BufferFieldData bfd = e.getData();
            int time = bfd.getInt();

            int secOfDay = MdpsFeedUtils.decodeTime(time);
            if (secOfDay < startTime || secOfDay >= endTime) {
                if (!ticks.isEmpty()) {
                    addTicksWithoutDuplicates(td, ticks);
                }
                td.add(e.getFlags(), buildTick(time, bfd));
                continue;
            }

            if (secOfDay != currentTime) {
                addTicksWithoutDuplicates(td, ticks);
                currentTime = secOfDay;
            }

            ticks.add(new Tick(e.getFlags(), time, bfd.copyBufferArray()));
        }

        addTicksWithoutDuplicates(td, ticks);
    }

    private void addTicksWithoutDuplicates(TickDeflater td, List<Tick> ticks) throws IOException {
        if (ticks.isEmpty()) {
            return;
        }
        setDuplicatesToNull(ticks);
        addNonNullTicks(td, ticks);
        ticks.clear();
    }

    private void addNonNullTicks(TickDeflater td,
            List<Tick> ticks) throws IOException {
        for (Tick tick : ticks) {
            if (tick != null) {
                tmp.reset(tick.bytes);
                td.add(tick.flags, buildTick(tick.time, tmp));
            }
        }
    }

    private void setDuplicatesToNull(List<Tick> ticks) {
        NEXT:
        for (int i = 0; i < ticks.size(); i++) {
            Tick tick = ticks.get(i);
            if (tick == null) {
                continue;
            }
            for (int j = i + 1; j < ticks.size(); j++) {
                if (tick.equals(ticks.get(j))) {
                    ticks.set(j, null);
                    continue NEXT;
                }
            }
        }
    }
}


