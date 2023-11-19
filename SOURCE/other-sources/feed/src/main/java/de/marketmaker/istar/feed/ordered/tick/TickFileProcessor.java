/*
 * TickFileProcessor.java
 *
 * Created on 13.09.13 12:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.monitor.BasicStopwatch;
import com.netflix.servo.monitor.DurationTimer;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Stopwatch;

import de.marketmaker.istar.common.servo.ServoMonitoring;
import de.marketmaker.istar.common.util.LoggingUncaughtExceptionHandler;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oflege
 */
public abstract class TickFileProcessor {

    static File[] getFiles(String[] args, FileFilter filter) {
        if (args.length == 1 && new File(args[0]).isDirectory()) {
            return new File(args[0]).listFiles(filter);
        }
        else {
            final File[] files = new File[args.length];
            int n = 0;
            for (String arg : args) {
                final File f = new File(arg);
                if (filter.accept(f)) {
                    files[n++] = f;
                }
            }
            return Arrays.copyOfRange(files, 0, n);
        }
    }

    public abstract class Task implements Runnable {
        protected final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void run() {
            File f;
            while((f = getNextFile()) != null) {
                Stopwatch sw = getStopwatch();
                File out = getOutFile(f);

                try {
                    if (out == null || !out.exists() || out.lastModified() <= f.lastModified()) {
                        process(f, out);
                    }
                } catch (Exception e) {
                    this.logger.error("<run> failed for " + f.getName(), e);
                } finally {
                    sw.stop();
                    ackFileProcessed(f);
                }
            }
        }

        protected abstract void process(File f, File out) throws IOException;

        protected abstract File getOutFile(File f);
    }

    protected Stopwatch getStopwatch() {
        if (this.durationTimer != null) {
            return durationTimer.start();
        }
        return new BasicStopwatch();
    }

    private static final long QUARTER_GB = 1L << 28;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    long maxMappedBytes = NumberUtil.parseLong(System.getProperty("maxMappedBytes", "32g"));

    private long numMappedBytes = 0L;

    protected int numThreads = 1;

    private int numBusyThreads = 0;

    private File out = null;

    private LinkedList<File> files = new LinkedList<>();

    private final Object filesMutex = new Object();

    private DurationTimer durationTimer;

    protected void process(File[] tmp) throws Exception {
        new LoggingUncaughtExceptionHandler().afterPropertiesSet();

        if (tmp == null || tmp.length == 0) {
            return;
        }
        this.files = setupFiles(tmp);

        if (ServoMonitoring.MONITORING_AVAILABLE) {
            initMonitoring();
        }

        final int numFiles = files.size();
        final TimeTaker tt = new TimeTaker();

        if (!TickCompressor.UNMAP_SUPPORTED && files.size() > 1) {
            this.logger.error("<process> unmap not supported, can only handle one file");
            return;
        }
        if (out != null && !out.isDirectory() && files.size() > 1) {
            this.logger.error("<process> cannot use out file with more than one input file");
            return;
        }

        ExecutorService es = Executors.newFixedThreadPool(numThreads, new ThreadFactory() {
            AtomicInteger n = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Thread-" + n.incrementAndGet());
            }
        });
        for (int i = 0; i < numThreads; i++) {
            es.submit(createTask());
        }
        es.shutdown();
        if (!es.awaitTermination(8, TimeUnit.HOURS)) {
            this.logger.error("<process> timeout while waiting for ExecutorService!?");
            return;
        }
        if (ServoMonitoring.MONITORING_AVAILABLE) {
            stopMonitoring();
        }
        logger.info("Processed " + numFiles + " files, took " + tt);
    }

    private void stopMonitoring() {
        ServoMonitoring.getSetup().stop();
    }

    private void initMonitoring() {
        this.durationTimer = new DurationTimer(MonitorConfig.builder(getClass().getSimpleName()).build());
        DefaultMonitorRegistry.getInstance().register(durationTimer);
        ServoMonitoring.getSetup().start();
    }

    private LinkedList<File> setupFiles(File[] tmp) {
        Arrays.sort(tmp, (f1, f2) -> Long.compare(f1.length(), f2.length()));
        ensureFileCanBeMapped(tmp[tmp.length - 1]); // only need to check largest file
        return new LinkedList<>(Arrays.asList(tmp));
    }

    private void ensureFileCanBeMapped(File f) {
        if (!isAcceptable(f, 0L)) {
            throw new IllegalArgumentException(f.getName() + " exceeds max size of " + this.maxMappedBytes);
        }
    }

    protected abstract Task createTask();

    private void ackFileProcessed(File f) {
        synchronized (filesMutex) {
            numMappedBytes -= f.length();
            numBusyThreads -= 1;
            filesMutex.notifyAll();
        }
    }

    private File getNextFile() {
        synchronized (filesMutex) {
            while (!files.isEmpty()) {
                final File file = doGetNextFile();
                if (file != null) {
                    numMappedBytes += file.length();
                    numBusyThreads += 1;
                    return file;
                }
                // no acceptable file available (due to memory constraints etc.),
                // wait until another task finishes and those constraints have changed
                try {
                    filesMutex.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }
    }

    private File doGetNextFile() {
        if (isAcceptable(files.peekLast())) {
            return files.removeLast();
        }
        if (isAcceptable(files.peekFirst())) {
            return files.removeFirst();
        }
        return null;
    }

    private boolean isAcceptable(File f) {
        return isAcceptable(f, this.numMappedBytes);
    }

    private boolean isAcceptable(File f, long mappedBytes) {
        return mappedBytes + f.length() < (maxMappedBytes - (getNumIdleThreads() * QUARTER_GB));
    }

    private int getNumIdleThreads() {
        return numThreads - numBusyThreads - 1;
    }

}
