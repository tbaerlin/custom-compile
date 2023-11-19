/*
 * BHLExecutor.java
 *
 * Created on 16.11.12 15:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.bhl;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author tkiesgen
 */
public class BHLExecutor implements DisposableBean {
    private static final DateTimeFormatter DIR_NAME_FORMATTER = DateTimeFormat.forPattern("'bhl-fme-'yyyyMMdd_HHmmss");

    private static final String FAILURE_LOG = "failure.log";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private File baseDir;

    private final ConcurrentMap<String, BHLTask> pendingTasks
            = new ConcurrentHashMap<>();

    private final ExecutorService es = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            return new Thread(r, ClassUtils.getShortName(BHLExecutor.this.getClass()));
        }
    });

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        if (!this.baseDir.isDirectory() && !this.baseDir.mkdirs()) {
            throw new IllegalArgumentException("invalid workDir " + this.baseDir.getAbsolutePath());
        }
    }

    public void destroy() throws Exception {
        this.es.shutdownNow();
        if (!this.es.awaitTermination(60, TimeUnit.SECONDS)) {
            this.logger.error("<destroy> executor service won't terminate?!");
        }
    }

    File getTaskDir() throws InterruptedException {
        while (true) {
            final File result = new File(this.baseDir, DIR_NAME_FORMATTER.print(new DateTime()));
            if (!result.exists()) {
                return result;
            }
            this.logger.info("<getTaskDir> sleeping 1s, taskDir exists: " + result.getAbsolutePath());
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public int getStatus(String taskId) {
        final BHLTask task = this.pendingTasks.get(taskId);
        if (task != null) {
            return task.getStatus();
        }
        final File taskDir = new File(this.baseDir, taskId);
        if (!taskDir.exists()) {
            this.logger.warn("<getStatus> no dir for task " + taskId);
            return -1;
        }
        if (new File(taskDir, FAILURE_LOG).exists()) {
            this.logger.warn("<getStatus> for failed task " + taskId);
            return -1;
        }
        if (!new File(taskDir, BHLTask.RESULT_FILE_NAME).exists()) {
            this.logger.warn("<getStatus> no result for task " + taskId);
            return -1;
        }
        return 100;
    }

    public File getFile(String taskId) {
        final BHLTask task = this.pendingTasks.get(taskId);
        if (task != null) {
            return null; // still pending
        }
        final File taskDir = new File(this.baseDir, taskId);
        if (!taskDir.exists()) {
            this.logger.warn("<getFile> no dir for task " + taskId);
            return null;
        }
        if (new File(taskDir, FAILURE_LOG).exists()) {
            this.logger.warn("<getFile> for failed task " + taskId);
            return null;
        }
        final File result = new File(taskDir, BHLTask.RESULT_FILE_NAME);
        if (!result.canRead()) {
            this.logger.warn("<getFile> no such file " + result.getAbsolutePath());
            return null;
        }
        return result;
    }

    public void submit(final BHLTask task) {
        final String taskId = task.getDir().getName();
        this.pendingTasks.put(taskId, task);
        this.es.submit(new Runnable() {
            public void run() {
                logger.info("<run> task '" + taskId + "' submitted, starting execution");

                try {
                    RequestContextHolder.setRequestContext(task.getRequestContext());

                    task.execute();
                } catch (Throwable t) {
                    createFailureLog(task.getDir(), t);
                } finally {
                    pendingTasks.remove(taskId);
                    RequestContextHolder.setRequestContext(null);
                    logger.info("<run> finished task '" + taskId + "'");
                }
            }
        });
    }

    private void createFailureLog(File dir, Throwable t) {
        try {
            final File file = new File(dir, FAILURE_LOG);
            final PrintWriter pw = new PrintWriter(file);
            t.printStackTrace(pw);
            pw.close();
            this.logger.error("<createFailureLog> " + file.getAbsolutePath());
        } catch (Exception e) {
            this.logger.error("<createFailureLog> failed in " + dir.getAbsolutePath(), t);
        }
    }
}
