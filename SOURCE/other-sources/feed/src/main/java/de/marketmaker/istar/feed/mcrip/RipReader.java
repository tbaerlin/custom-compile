/*
 * RipReader.java
 *
 * Created on 07.07.11 14:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mcrip;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedUpdate;
import de.marketmaker.istar.feed.ordered.OrderedUpdateBuilder;

/**
 * @author oflege
 */
public class RipReader extends AbstractRipReader implements OrderedUpdateBuilder {

    private File baseDir = new File(new File(System.getProperty("user.home")),
            "produktion/var/data/mcdump");

    private LocalDate from = new LocalDate();

    private LocalDate to = from;

    private File file = null;

    private PrintWriter out = new PrintWriter(System.out);

    private FileChannel fc = null;

    private int minTimestamp = 0;

    private int maxTimestamp = Integer.MAX_VALUE;

    private boolean withVwdcode = false;

    public static void main(String[] args) throws Exception {
        RipReader reader = new RipReader();

        int n = 0;
        while (args.length > n && args[n].startsWith("-")) {
            if ("-b".equals(args[n])) {
                reader.baseDir = new File(args[++n]);
                if (!reader.baseDir.isDirectory()) {
                    System.err.println("not a directory: " + reader.baseDir.getAbsolutePath());
                    System.exit(-1);
                }
            }
            else if ("-f".equals(args[n])) {
                reader.file = new File(args[++n]);
                if (!reader.file.isFile()) {
                    System.err.println("not a file: " + reader.file.getAbsolutePath());
                    System.exit(-1);
                }
            }
            else if (args[n].matches("-\\d+")) {
                reader.from = reader.from.minusDays(Integer.parseInt(args[n].substring(1)));
                reader.to = reader.from;
            }
            else if ("-d".equals(args[n])) {
                final String[] fromTo = args[++n].split(Pattern.quote(".."));
                reader.from = parseDate(fromTo[0]);
                reader.to = fromTo.length > 1 ? parseDate(fromTo[1]) : reader.from;
            }
            else if ("-tmin".equals(args[n])) {
                reader.minTimestamp = parseTimestamp(args[++n]);
            }
            else if ("-tmax".equals(args[n])) {
                reader.maxTimestamp = parseTimestamp(args[++n]);
            }
            else if ("-v".equals(args[n])) {
                reader.withVwdcode = true;
            }
            else if ("-o".equals(args[n])) {
                File outfile = new File(args[++n]);
                if (outfile.exists() && !outfile.delete()) {
                    System.err.println("failed to delete " + outfile.getAbsolutePath());
                    return;
                }
                reader.out = new PrintWriter(outfile);
                System.err.println("out : " + outfile.getAbsolutePath());
            }
            else if ("-r".equals(args[n])) {
                File outfile = new File(args[++n]);
                if (outfile.exists() && !outfile.delete()) {
                    System.err.println("failed to delete " + outfile.getAbsolutePath());
                    return;
                }
                reader.fc = new RandomAccessFile(outfile, "rw").getChannel();
                System.err.println("out : " + outfile.getAbsolutePath());
            }
            else {
                usage();
            }
            n++;
        }

        while (args.length > n) {
            reader.dump(args[n++].toUpperCase());
        }

        reader.out.close();
        if (reader.fc != null) {
            reader.fc.close();
        }
    }

    private static int parseTimestamp(String arg) {
        return new DateTimeProvider.Timestamp(DateTime.parse(arg)).feedTimestamp;
    }

    private static LocalDate parseDate(String arg) {
        return ISODateTimeFormat.date().parseDateTime(arg).toLocalDate();
    }

    private static void usage() {
        System.err.println("Usage: java de.marketmaker.istar.feed.mcrip.RipReader <options> vwdcode");
        System.err.println("  Dumps feed data for a symbol on a given day (default: today)");
        System.err.println(" Options are");
        System.err.println(" -<n>          -- day = today minus n days");
        System.err.println(" -d yyyy-MM-dd -- day = yyyy-MM-dd");
        System.err.println("    or use yyyy-MM-dd..yyyy-MM-dd to dump multiple days");
        System.err.println(" -b basedir    -- <basedir>/yyyyMMdd/mcdump_yyyyMMdd-hash.dat");
        System.err.println(" -o filename   -- dump to file (default: stdout)");
        System.err.println(" -r filename   -- dump raw records to file");
        System.err.println(" -f filename   -- ignore -d & -b, just scan this file");
        System.err.println("               -- with -f, vwdcode is interpreted as a general vendorkey filter");
        System.err.println("               -- so you can use '*' to dump all records");
        System.err.println("== the following options are (only) useful if -f is used:");
        System.err.println(" -v            -- include vwdcode for each record");
        System.err.println(" -tmin <ts>    -- only print records with feed timestamp >= ts");
        System.err.println(" -tmax <ts>    -- only print records with feed timestamp < ts");
        System.err.println("               -- Format for ts is 'yyyy-MM-ddTHH:mm:ss'");
        System.exit(-2);
    }

    private void dump(String vwdcode) throws Exception {
        final VendorkeyFilter filter = VendorkeyFilterFactory.create(vwdcode);
        final int shard = new ByteString(vwdcode).hashCode() & 0xFF;

        LocalDate date = this.from;
        while (!date.isAfter(this.to)) {
            read(getFile(date, shard), filter, this);
            date = date.plusDays(1);
        }
    }

    private File getFile(LocalDate date, int shard) {
        if (this.file != null) {
            return this.file;
        }
        final File dir = new File(this.baseDir, MulticastFeedRipper.createDirname(date));
        return new File(dir, MulticastFeedRipper.createFilename(date, shard));
    }

    @Override
    public void process(OrderedFeedData data, OrderedUpdate update) {
        if (update.getTimestamp() < this.minTimestamp || update.getTimestamp() >= this.maxTimestamp) {
            return;
        }
        if (this.fc == null) {
            this.out.println(this.fmt.format(withVwdcode ? data : null, update));
        }
        else {
            try {
                fc.write(update.asMessageWithLength());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
