/*
 * FeedDumper.java
 *
 * Created on 25.05.2007 09:25:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dump;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.RecordSource;
import de.marketmaker.istar.feed.connect.BufferWriter;

import static org.joda.time.DateTimeConstants.MILLIS_PER_MINUTE;

/**
 * Dumps feed content in inflated files, dumped data will be in mdps record format.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsFeedDumper implements InitializingBean, DisposableBean, BeanNameAware, Lifecycle,
        BufferWriter, Consumer<FeedRecord> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final FileFilter FILTER = p -> p.isFile() && p.getName().endsWith(".bin");

    private final ByteBuffer bb;

    private int numWritten;

    /** This is used to limit max file size on disk. NOTE that this corresponds to uncompressed data amount */
    private int maxBytesPerFile;

    private int maxMinutesPerFile;

    private File baseDir;

    private DateTimeFormatter filenameFormatter;

    private final Timer timer = new Timer("feedDumper-timer", true);

    private TimerTask flushTask;

    private OutputStream os;

    private File currentFile;

    private volatile boolean stopped = false;

    private Thread dumpThread;

    private int compressionLevel = 3;

    private RecordSource recordSource;

    private String name = ClassUtils.getShortName(getClass());

    public MdpsFeedDumper() {
        this(96 * 1024);
    }

    @Override
    public void setBeanName(String name) {
        this.name = name;
    }

    public MdpsFeedDumper(int bufferSize) {
        this.bb = ByteBuffer.allocate(bufferSize);
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public void setRecordSource(RecordSource recordSource) {
        this.recordSource = recordSource;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setMaxBytesPerFile(int maxBytesPerFile) {
        this.maxBytesPerFile = maxBytesPerFile;
    }

    public void setFilenameFormatter(String pattern) {
        this.filenameFormatter = DateTimeFormat.forPattern(pattern);
    }

    public void setMaxMinutesPerFile(int maxMinutesPerFile) {
        this.maxMinutesPerFile = maxMinutesPerFile;
    }

    public void afterPropertiesSet() throws Exception {
        reset();
    }

    public void destroy() throws Exception {
        this.stopped = true;
        dumpBuffer(true);
        flush(this.currentFile);
    }

    @Override
    public boolean isRunning() {
        return this.dumpThread != null;
    }

    public void start() {
        if (this.recordSource == null) {
            this.logger.info("<start> no recordSource, do not create dumpThread");
            return;
        }
        this.dumpThread = new Thread(this::doRun, this.name + "-dumpThread");
        this.dumpThread.start();
    }

    public void stop() {
        this.stopped = true;
        if (this.dumpThread == null) {
            return;
        }
        try {
            this.dumpThread.join(1000);
            if (this.dumpThread.isAlive()) {
                this.logger.info("<stop> interrupting dumpThread...");
                this.dumpThread.interrupt();
                this.dumpThread.join();
            }
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
        this.dumpThread = null;
    }

    private File getTempFile() {
        return new File(this.currentFile.getParentFile(), ".current.dump");
    }

    private synchronized void reset() {
        final DateTime now = new DateTime();
        final File f = new File(this.baseDir, this.filenameFormatter.print(now));
        final File dir = f.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            this.logger.error("<initFile> could not create " + dir.getAbsolutePath());
            throw new RuntimeException();
        }

        this.currentFile = f;

        try {
            final Deflater def = new Deflater(this.compressionLevel, true);
            this.os = new DeflaterOutputStream(new FileOutputStream(getTempFile()), def, 8 * 1024);
        } catch (IOException e) {
            this.logger.error("<initStream> failed to create output stream");
            throw new RuntimeException();
        }

        if (this.flushTask != null) {
            this.flushTask.cancel();
        }

        this.flushTask = new TimerTask() {
            public void run() {
                flush(f);
            }
        };
        this.timer.schedule(this.flushTask, this.maxMinutesPerFile * MILLIS_PER_MINUTE);

        this.numWritten = 0;
    }

    private void doRun() {
        while (!this.stopped) {
            try {
                dumpNext();
            } catch (InterruptedException e) {
                this.logger.info("<run> interrupted, returning");
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable t) {
                this.logger.error("<run> failed: " + t.getMessage());
            }
        }
    }

    protected synchronized void dumpBuffer(boolean forceFlush) throws IOException {
        this.bb.flip();
        this.numWritten += this.bb.remaining();
        this.os.write(this.bb.array(), 0, this.bb.remaining());

        if (forceFlush || this.numWritten > this.maxBytesPerFile) {
            flush(this.currentFile);
        }
    }

    private synchronized void flush(final File f) {
        if (f != this.currentFile) {
            return;
        }

        try {
            this.os.close();

            final File tmp = getTempFile();

            if (!tmp.renameTo(this.currentFile)) {
                logger.error("<flush> failed to rename " + tmp.getAbsolutePath()
                        + " to " + f.getAbsolutePath());
            }
            else {
                logger.info("<flush> wrote " + f.getAbsolutePath());
            }
        } catch (IOException e) {
            this.logger.error("<flush> failed", e);
        } finally {
            if (!this.stopped) {
                reset();
            }
        }
    }


    private FeedRecord getFeedRecord() throws InterruptedException {
        return this.recordSource.getFeedRecord();
    }

    protected void dumpNext() throws InterruptedException, IOException {
        final FeedRecord fr = getFeedRecord();
        if (fr != null) {
            write(fr.getAsByteBuffer());
        }
    }

    @Override
    public void accept(FeedRecord fr) {
        write(fr.getAsByteBuffer());
    }

    @Override
    public synchronized void write(ByteBuffer toDump) {
        if (this.bb.remaining() < (toDump.remaining())) {
            try {
                dumpBuffer(false);
            } catch (IOException e) {
                this.logger.error("<process> failed to write buffer", e);
            }
            this.bb.clear();
        }
        this.bb.put(toDump);
    }

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            File fileIn = new File(arg);
            if (fileIn.isDirectory()) {
                for (File f : fileIn.listFiles(FILTER)) {
                    uncompress(f);
                }
            }
            else {
                uncompress(fileIn);
            }
        }
    }

    private static void uncompress(File fileIn) throws IOException {
        File fileOut = new File(fileIn.getParentFile(), fileIn.getName() + ".mdps");
        FileCopyUtils.copy(new InflaterInputStream(new FileInputStream(fileIn),
                new Inflater(true), 8 * 1024), new FileOutputStream(fileOut));
    }
}
