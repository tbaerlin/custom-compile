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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.FeedUpdateFlags.*;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.toXfeedMessageType;

/**
 * command line interface for dd3/ddz files
 *
 * @author oflege
 */
public class DumpCli {
    static final Charset UTF_8 = Charset.forName("UTF-8");

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }
        final String cmd = args[0];
        final String[] cmdArgs = Arrays.copyOfRange(args, 1, args.length);
        if ("export".startsWith(cmd)) {
            export(cmdArgs);
        }
        else if ("inspect".startsWith(cmd)) {
            inspect(cmdArgs);
        }
        else if ("symbols".startsWith(cmd)) {
            symbols(cmdArgs);
        }
        else if ("dump".startsWith(cmd)) {
            TickCli.dump(cmdArgs);
        }
        else if ("explain".startsWith(cmd)) {
            explain(cmdArgs);
        }
        else {
            usage();
        }
    }

    private static void usage() {
        System.err.println("Usage: DumpCli <command> <options>");
        System.err.println("commands and their resp. options are:");
        System.err.println(" inspect [-num n] <dd3/ddz-File>");
        System.err.println(" -- shows information about the file's content");
        System.err.println(" -- -num show top n symbols, default is 20");
        System.err.println();
        System.err.println(" export [-k keyfile] [-c] [-o <outfile>] [(-p <pattern>|-q <text>)] <dd3/ddz-File-or-Dir> <vwdcode>+");
        System.err.println(" -- export dump for a number of vwdcodes");
        System.err.println(" -- to stdout or the file defined using -o option");
        System.err.println(" -- -p export line if pattern.matcher(line).find() is true");
        System.err.println(" -- -q export line if line.contains(text)");
        System.err.println();
        System.err.println(" symbols <dd3/ddz-File>");
        System.err.println(" -- print all symbols with data in that file");
        System.err.println();
        System.err.println(" explain <dd3/ddz-File> <vwdcode>+");
        System.err.println(" -- shows info about how data is stored");
        System.err.println();
        System.err.println(" dump <dd3/ddz-File> <vwdcode> <outfile>?");
        System.err.println(" -- dump raw data for a vwdcode");
        System.err.println(" -- to outfile or <vwdcode>.bin");
        System.err.println();
    }

    private static void export(String[] args) throws IOException {
        PrintWriter pw = new PrintWriter(System.out);
        Pattern pattern = null;
        List<String> keyList = null;
        LocalDate from;
        LocalDate to;
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
            else if ("-k".equals(args[i])) {
                keyList = Files.readAllLines(new File(args[++i]).toPath(), UTF_8);
            }
            i++;
        }

        final FileTickStore fts = new FileTickStore();

        final File f = new File(args[i++]).getAbsoluteFile();
        TickDirectory td = null;
        AbstractTickRecord.TickItem.Encoding encoding;
        if (f.isDirectory()) {
            td = TickDirectory.open(f);
            encoding = td.getEncoding();
        }
        else {
            encoding = TickFiles.getItemType(f);
        }

        Iterable<String> keys = (keyList != null) ? keyList : Arrays.asList(Arrays.copyOfRange(args, i, args.length));
        for (String key : keys) {
            final byte[] bytes = (td != null) ? td.readTicks(fts, key) : fts.readTicks(f, key);
            if (bytes == null) {
                System.err.println("No data for " + key);
                continue;
            }

            pw.println("# " + key);
            LineBuilder lb = new LineBuilder();
            for (DumpDecompressor.Element e : new DumpDecompressor(bytes, encoding)) {
                String line = lb.build(e);
                if (pattern == null || pattern.matcher(line).find()) {
                    pw.println(line);
                }
            }
        }
        pw.close();
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

    private static void symbols(String[] args) throws Exception {
        final File f = new File(args[0]);
        try (FileChannel fc = new RandomAccessFile(f, "r").getChannel()) {
            TickFileIndexReader.readEntries(fc, new DefaultIndexHandler(f) {
                @Override
                protected void doHandle(ByteString vwdcode, long position, int length) {
                    System.out.println(vwdcode);
                }
            });
        }
    }

    private static void explain(String[] args) throws Exception {
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

                LineBuilder lb = new LineBuilder();
                final DumpDecompressor dd = new DumpDecompressor(bb.array(), TickFiles.getItemType(f));
                for (DumpDecompressor.Element e: dd) {
                    System.out.println(lb.build(e));
                }
            }
        }
    }

    static class LineBuilder {
        private final StringBuilder sb = new StringBuilder(80);

        public String build(DumpDecompressor.Element e) {
            sb.setLength(0);
            final int date = DateTimeProvider.Timestamp.decodeDate(e.getTimestamp());
            sb.append(date).append(' ');
            final int time = DateTimeProvider.Timestamp.decodeTime(e.getTimestamp());
            sb.append(TimeFormatter.formatSecondsInDay(time)).append(' ');
            int mdpsMsgType = e.getMdpsMsgType();
            if (mdpsMsgType > 0) {
                sb.append(mdpsMsgType).append(' ').append((char) toXfeedMessageType(mdpsMsgType));
            }
            else {
                sb.append("0 ").append((char) (-mdpsMsgType));
            }
            String mdpsKeyType = String.valueOf(MdpsTypeMappings.getMdpsKeyTypeById(e.getMdpsKeyType()));
            sb.append(' ').append(mdpsKeyType);
            sb.append(' ').append(MdpsTypeMappings.toNumericType(mdpsKeyType)).append(' ');
            appendFlags(e.getFlags());
            sb.append(' ');
            appendFields(e.getData());
            return sb.toString();
        }

        private void appendFields(BufferFieldData fd) {
            char sep = ' ';
            for (int id = fd.readNext(); id != 0; id = fd.readNext(), sep = ';') {
                VwdFieldDescription.Field field = VwdFieldOrder.getField(fd.getId());
                this.sb.append(sep).append(field.id()).append('=');
                TickCli.LineBuilder.formatData(sb, fd, field, true);
            }
        }

        protected void appendFlags(int flags) {
            TickCli.LineBuilder.appendFlags(sb, flags);

            if (TickCli.LineBuilder.isSet(flags, FLAG_WITH_SPECIAL_TICK_FIELDS)) {
                sb.append('S');
            }
            if (TickCli.LineBuilder.isSet(flags, FLAG_WITH_QUELLE)) {
                sb.append('Q');
            }
            if (TickCli.LineBuilder.isSet(flags, FLAG_WITH_CLOSE_DATE_YESTERDAY)) {
                sb.append('C');
            }
            if (TickCli.LineBuilder.isSet(flags, FLAG_WITH_OLD_HANDELSDATUM)) {
                sb.append('O');
            }
            if (TickCli.LineBuilder.isSet(flags, FLAG_YESTERDAY)) {
                sb.append('Y');
            }
        }

    }
}


