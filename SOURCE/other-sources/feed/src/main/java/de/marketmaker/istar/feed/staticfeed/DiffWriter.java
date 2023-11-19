/*
 * DiffWriterMdp.java
 *
 * Created on 19.10.12 13:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPOutputStream;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

import de.marketmaker.istar.common.util.FileUtil;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * @author oflege
 */
public class DiffWriter implements InitializingBean, DisposableBean, Lifecycle,
    DiffFormatter.LineHandler {

    static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File outDir;

    private File currentFile;

    private long currentTimestamp = System.currentTimeMillis();

    private PrintWriter currentWriter;

    private long lastNumAppended;

    private long numAppended;

    private final Timer timer = new Timer(getClass().getSimpleName(), true);

    private final Object mutex = new Object();

    private volatile boolean running;

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    @Monitor(name = "numAppended", type = COUNTER)
    public long getNumAppended() {
        synchronized (this.mutex) {
            return this.numAppended;
        }
    }

    @Override
    public void destroy() throws Exception {
        synchronized (this.mutex) {
            closeCurrent(anythingAppended());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!FileUtil.isDirWriteable(this.outDir)) {
            throw new IllegalStateException("Not a directory " + this.outDir);
        }
        initWriter();
    }

    private void initWriter() throws IOException {
        this.currentFile = File.createTempFile(".mdp-", ".tmp", this.outDir);
        this.currentWriter = new PrintWriter(new OutputStreamWriter(
                new GZIPOutputStream(new FileOutputStream(this.currentFile), 8192), UTF_8));
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void start() {
        final DateTime now = new DateTime();
        long delay = new Duration(now, now.plusMinutes(2).withSecondOfMinute(0)).getMillis();

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                rotateFile();
            }
        }, delay, 60000);
        this.running = true;
    }

    @Override
    public void stop() {
        this.timer.cancel();
    }

    private void rotateFile() {
        synchronized (this.mutex) {
            if (anythingAppended()) {
                doRotateFile();
                this.lastNumAppended = this.numAppended;
            }
            this.currentTimestamp = System.currentTimeMillis();
        }
    }

    private boolean anythingAppended() {
        return this.numAppended > this.lastNumAppended;
    }

    private void doRotateFile() {
        closeCurrent(true);
        try {
            initWriter();
        } catch (IOException e) {
            this.logger.error("<doRotateFile> failed to init new writer", e);
        }
    }

    private void closeCurrent(boolean rename) {
        if (this.currentWriter == null) {
            return;
        }
        this.currentWriter.close();
        this.currentWriter = null;
        if (rename) {
            final File outFile = new File(this.currentFile.getParentFile(), "mdpsstatic_"
                    + DTF.print(this.currentTimestamp) + ".csv.gz");
            if (!this.currentFile.renameTo(outFile)) {
                this.logger.error("<closeCurrent> failed to rename " + this.currentFile.getName()
                        + " to " + outFile.getName());
            }
        }
        else {
            if (!this.currentFile.delete()) {
                this.logger.error("<closeCurrent> failed to delete " + this.currentFile.getName());
            }
        }
    }

    public void append(String line) {
        synchronized (this.mutex) {
            if (this.currentWriter != null) {
                this.currentWriter.println(line);
                this.numAppended++;
            }
        }
    }
}
