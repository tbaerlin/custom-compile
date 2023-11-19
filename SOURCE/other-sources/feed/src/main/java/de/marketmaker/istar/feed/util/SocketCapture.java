/*
 * FeedDumper.java
 *
 * Created on 18.07.2007 09:25:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISOPeriodFormat;

/**
 * Allows to capture bytes read from a remote socket in a file or files.
 * Dependencies to other jars are kept to a minimum (just joda-time is needed).
 * The capture has to be limited by either specifying an end time or by specifying
 * a maximum number of bytes. If both are specified,
 * the first threshold that is broken terminates the capture.<p>
 * Captures may start immediately or at a given time.<p>
 * Knows how to interpret the mdps feed versions 1 and 3, so that it only captures complete
 * feed records if either version is specified.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SocketCapture implements Runnable {
    private final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

    private long count = Long.MAX_VALUE;

    private long fileSize = 0;

    private String host;

    private int port;

    private DateTime start = new DateTime();

    private DateTimeFormatter dtf;

    private DateTime end;

    private int mdpsv = 0;

    private volatile SocketChannel sc;

    private volatile CountDownLatch cdl = new CountDownLatch(1);

    private final ByteBuffer bb;

    private final AtomicInteger threadCount = new AtomicInteger();

    private ScheduledExecutorService es = Executors.newScheduledThreadPool(4, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            final Thread result = new Thread(r, "Thread-" + threadCount.incrementAndGet());
            result.setDaemon(true);
            return result;
        }
    });

    private final Runnable printStats = new Runnable() {
        public void run() {
            printStats();
            schedulePrintStats();
        }
    };

    private AtomicBoolean stop = new AtomicBoolean(false);

    private AtomicLong numBytesWritten = new AtomicLong();

    static final int ONE_KB = 1024;

    static final int ONE_MB = ONE_KB * ONE_KB;

    private final File out;

    private int fileNum = 0;

    public SocketCapture(String[] args) throws IOException {
        if (args.length % 2 == 0) {
            usage();
        }

        int n = 0;
        final DateTime now = this.start;
        while (n < args.length - 1) {
            final String key = args[n++];
            final String value = args[n++];

            if ("-names".startsWith(key)) {
                final String[] nameAndSuffix = value.split(".");
                this.dtf = DateTimeFormat.forPattern("yyyy/MM/dd/'" + nameAndSuffix[0]
                        + "_'yyyy_MM_dd__HH_mm_ss.SSS'." + nameAndSuffix[1] + "'");
            }
            else if ("-port".startsWith(key)) {
                this.port = Integer.parseInt(value);
            }
            else if ("-host".startsWith(key)) {
                this.host = value;
            }
            else if ("-mdpsv".startsWith(key)) {
                this.mdpsv = Integer.parseInt(value);
            }
            else if ("-count".startsWith(key)) {
                this.count = toNumberOfBytes(value);
            }
            else if ("-fsize".startsWith(key)) {
                this.fileSize = toNumberOfBytes(value);
            }
            else if ("-start".startsWith(key)) {
                this.start = parseDate(value);
            }
            else if ("-end".startsWith(key)) {
                this.end = (value.startsWith("P"))
                        ? this.start.plus(ISOPeriodFormat.standard().parsePeriod(value)) : parseDate(value);
            }
            else if ("-help".startsWith(key)) {
                usage();
            }
            else {
                System.err.println("unknown argument " + key);
                usage();
            }
        }

        if (this.start.isBefore(now)) {
            System.err.println(DTF.print(this.start) + " before now");
            System.exit(-1);
        }
        if (this.end != null && this.end.isBefore(this.start)) {
            System.err.println("end " + DTF.print(this.end) + " before start " + DTF.print(this.start));
            System.exit(-1);
        }
        if (this.end == null && this.count == Long.MAX_VALUE) {
            System.err.println("either end or count must be specified");
            System.exit(-1);
        }
        if (this.count < 1) {
            System.err.println("count must be > 0, not " + this.count);
            System.exit(-1);
        }

        if (this.mdpsv != 0 && this.mdpsv != 1 && this.mdpsv != 3) {
            System.err.println("mdpsv must either be 1 or 3, not " + this.mdpsv);
            System.exit(-1);
        }

        final int size = Math.max(ONE_KB, count > Integer.MAX_VALUE ? ONE_MB : Math.min(ONE_MB, (int) count));
        this.bb = ByteBuffer.allocateDirect(size);

        System.err.println("Using buffer size " + size);

        System.err.print("About to capture at most " + this.count + " bytes");
        if (this.start != now) {
            System.err.print(" from " + DTF.print(this.start));
        }
        if (this.end != null) {
            System.err.print(" to " + DTF.print(this.end));
        }
        System.err.println();

        if (this.mdpsv > 0) {
            this.bb.order(this.mdpsv == 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }

        this.out = new File(args[n]);
    }

    private long toNumberOfBytes(String value) {
        final long f = getFactor(value);
        return (f == 1)
                ? Long.parseLong(value)
                : Long.parseLong(value.substring(0, value.length() - 1)) * f;
    }

    private DateTime parseDate(String s) {
        if (s.indexOf('T') == -1) {
            final String now = DTF.print(new DateTime());
            return parseDate(now.substring(0, 11) + s);
        }
        return DTF.parseDateTime(s);
    }

    private void usage() {
        System.err.println("Usage: SocketCapture [options] -host <name> -port <num> <outfile>");
        System.err.println("options:");
        System.err.println("-fsize [0-9]*[kmg]? -- write file(s) with max this many bytes");
        System.err.println("-count [0-9]*[kmg]? -- stop after writing this many bytes");
        System.err.println("-mdpsv (1|3) -- only write complete mdps feed records");
        System.err.println("-start (yyyy-MM-ddT)?HH:mm:ss -- start time (default: now)");
        System.err.println("-end   (yyyy-MM-ddT)?HH:mm:ss|<period> -- end time or period relative to start)");
        System.err.println("either end or count have to be specified");
        System.exit(-1);
    }

    private static long getFactor(String s) {
        final char last = Character.toLowerCase(s.charAt(s.length() - 1));
        switch (last) {
            case 'k':
                return 1024L;
            case 'm':
                return 1024L * 1024L;
            case 'g':
                return 1024L * 1024L * 1024L;
            default:
                return 1L;
        }
    }

    private void printStats() {
        System.err.printf("wrote %11d bytes%n", numBytesWritten.get());
    }

    private void execute() {
        final long startTime = Math.max(0, this.start.getMillis() - new DateTime().getMillis());

        final ScheduledFuture<Object> captureFuture = this.es.schedule(new Callable<Object>() {
            public Object call() throws Exception {
                try {
                    capture();
                } finally {
                    cdl.countDown();
                }
                return null;
            }
        }, startTime, TimeUnit.MILLISECONDS);

        if (this.end != null) {
            this.es.schedule(new Runnable() {
                public void run() {
                    stopAndClose();
                }
            }, startTime + (this.end.getMillis() - this.start.getMillis()), TimeUnit.MILLISECONDS);
        }

        try {
            captureFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void schedulePrintStats() {
        if (!this.stop.get()) {
            this.es.schedule(this.printStats, 5, TimeUnit.SECONDS);
        }
    }

    private void capture() throws IOException {
        schedulePrintStats();
        System.err.println("capture starts now");

        FileChannel fc = createFileChannel();

        this.sc = SocketChannel.open(new InetSocketAddress(this.host, this.port));

        long toRead = this.count;
        long bytesInFile = 0;

        while (toRead > 0 && !stop.get()) {
            try {
                this.sc.read(this.bb);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            this.bb.flip();
            final int unadjustedLimit = bb.limit();
            adjustLimit(toRead);

            toRead -= this.bb.remaining();
            this.numBytesWritten.addAndGet(this.bb.remaining());
            bytesInFile += this.bb.remaining();

            fc.write(this.bb);
            this.bb.limit(unadjustedLimit);
            this.bb.compact();

            if (this.fileSize > 0 && bytesInFile > this.fileSize) {
                fc.close();
                fc = createFileChannel();
                bytesInFile = 0L;
            }
        }

        this.sc.close();
        fc.close();
        this.sc = null;

        System.err.println("capture finished");
        printStats();
    }

    private FileChannel createFileChannel() throws IOException {
        final File file = getFile(this.out);        
        return new RandomAccessFile(file, "rw").getChannel();
    }

    private File getFile(File out) throws IOException {
        if (this.dtf != null) {
            File result = new File(out, this.dtf.print(new DateTime()));
            File dir = result.getParentFile();
            if (!dir.isDirectory() && !dir.mkdirs()) {
                throw new IOException("failed to create " + dir.getAbsolutePath());
            }
            return result;
        }

        if (this.fileSize  <= 0) {
            return out;
        }

        final String num = "00" + ++this.fileNum;
        final String name = out.getName() + "." + num.substring(num.length() - 3);
        return new File(this.out.getParentFile(), name);
    }

    private void adjustLimit(long toRead) {
        if (this.mdpsv == 1 || this.mdpsv == 3) {
            int threshold = (toRead > bb.limit()) ? bb.limit() : (int) toRead;
            int n = 0;
            final int end = bb.limit() - 2;
            while (n < end) {
                final int len = bb.getShort(n) & 0xFFFF;
                if ((n + len) > end) {
                    break;
                }
                n += len;
                if (n > threshold) {
                    break;
                }
            }
            this.bb.limit(n);
        }
        else if (toRead < this.bb.remaining()) {
            this.bb.limit((int) toRead);
        }
    }

    public void run() {
        if (this.cdl.getCount() <= 0) {
            return;
        }
        stopAndClose();
        System.err.println("Running shutdown-hook");
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stopAndClose() {
        if (!this.stop.compareAndSet(false, true)) {
            return;
        }
        System.err.println("stop...");

        try {
            if (this.cdl.await(5, TimeUnit.SECONDS)) {
                return;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final SocketChannel tmp = this.sc;
        if (tmp != null) {
            try {
                tmp.close();
                System.err.println("closed SocketChannel");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final SocketCapture sc = new SocketCapture(args);
        Runtime.getRuntime().addShutdownHook(new Thread(sc));
        sc.execute();
    }
}
