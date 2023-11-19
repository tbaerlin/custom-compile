/*
 * BewExecutor.java
 *
 * Created on 27.05.2010 13:21:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.DisposableBean;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * Executes BewTasks and allows to query a tasks state.
 * @author oflege
 */
public class BewExecutor implements DisposableBean {
    private static final DateTimeFormatter DIR_NAME_FORMATTER
            = DateTimeFormat.forPattern("yyyyMMdd_HHmmss");

    private static final String FAILURE_LOG = "failure.log";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private File baseDir;

    private BewDao dao;

    public void setDao(BewDao dao) {
        this.dao = dao;
    }

    private final ExecutorService es = Executors.newSingleThreadExecutor(r
            -> new Thread(r, BewExecutor.class.getSimpleName())
    );

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        if (!this.baseDir.isDirectory() && !this.baseDir.mkdirs()) {
            throw new IllegalArgumentException("invalid baseDir " + this.baseDir.getAbsolutePath());
        }
    }

    public void destroy() throws Exception {
        this.es.shutdown();
        if (!this.es.awaitTermination(120, TimeUnit.SECONDS)) {
            this.logger.error("<destroy> executor service won't terminate?!");
        }
    }

    File getUserDir(BewCommand command) {
        return new File(this.baseDir, command.getCustomer());
    }

    File getTaskDir(File userDir) throws InterruptedException {
        while (true) {
            final File result = new File(userDir, DIR_NAME_FORMATTER.print(new DateTime()));
            if (!result.exists()) {
                return result;
            }
            this.logger.info("<getTaskDir> sleeping 1s, taskDir exists: " + result.getAbsolutePath());
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public int getStatus(String name, String jobId) {
        final String key = createKey(name, jobId);
        final File taskDir = new File(new File(this.baseDir, name), jobId);
        return getStatus(key, taskDir);
    }

    private int getStatus(String key, File taskDir) {
        final File f = new File(taskDir, BewTask.TASKID_FILE_NAME);
        if (!f.canRead()) {
            this.logger.warn("<getStatus> no taskid.txt for " + key);
            return -1;
        }
        final int taskId;
        try {
            taskId = Integer.parseInt(Files.readFirstLine(f, StandardCharsets.US_ASCII));
        } catch (IOException e) {
            this.logger.warn("<getStatus> failed to parse taskId from " + f.getAbsolutePath() + ": " + e);
            return -1;
        }
        final TaskInfo info = dao.getTask(taskId);
        if (info == null) {
            this.logger.warn("<getStatus> no info for task " + taskId + " in " + key);
            return -1;
        }
        return info.getPercentage();
    }

    public File getFile(String name, String jobId, boolean messageFile) {
        final String key = createKey(name, jobId);
        final File taskDir = new File(new File(this.baseDir, name), jobId);
        final int status = getStatus(key, taskDir);
        if (status != 100) {
            this.logger.warn("<getFile> invalid status " + status + " for " + key);
            return null;
        }
        final File result = new File(taskDir, messageFile
                ? BewTask.MESSAGES_FILE_NAME : BewTask.RESULT_FILE_NAME);
        if (!result.canRead()) {
            this.logger.warn("<getFile> no such file " + result.getAbsolutePath());
            return null;
        }
        return result;
    }

    private String createKey(String name, String jobId) {
        return name + "/" + jobId;
    }

    public void submit(final BewTask task) {
        this.es.submit(() -> {
            RequestContextHolder.setRequestContext(task.getContext());
            TimeTaker tt = new TimeTaker();
            try {
                task.execute();
                logger.info("<submit> executed " + task + ", took " + tt);
            } catch (Throwable t) {
                createFailureLog(task.getDir(), t);
            } finally {
                RequestContextHolder.setRequestContext(null);
            }
        });
        this.logger.info("<submit> added " + task);
    }

    private void createFailureLog(File dir, Throwable t) {
        final File file = new File(dir, FAILURE_LOG);
        try (PrintWriter pw = new PrintWriter(file)) {
            t.printStackTrace(pw);
            this.logger.error("<createFailureLog> " + file.getAbsolutePath());
        } catch (IOException e) {
            this.logger.error("<createFailureLog> failed for " + file.getAbsolutePath(), e);
            this.logger.error("<createFailureLog> ... when trying to log ", t);
        }
    }
}
