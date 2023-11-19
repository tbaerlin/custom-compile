/*
 * IntradayWriter.java
 *
 * Created on 19.11.2003 14:13:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.TimeTaker;

/**
 * Creates files with real-time feed data based on input files in some directory.
 * That directory is actively monitored for added, modified and deleted files, so that
 * reconfiguration is very easy.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class DpManager implements InitializingBean, DisposableBean, DpManagerMBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * All currently active DpFiles keyed by their absolute filename
     */
    private final ConcurrentMap<String, DpFile> dpFiles = new ConcurrentHashMap<>();

    /**
     * used to create DpFile objects from files in inputDirectory
     */
    private DpFileFactory factory;

    /**
     * delegate for monitoring an input directory
     */
    private DpInputMonitor inputMonitor;

    /**
     * delegate for scheduling dp file writes
     */
    private DpScheduler scheduler;

    /**
     * delegate for actually writing data to dp files
     */
    private DpFile.Writer writer;

    /**
     * Used to store files that indicate errors in input files.
     */
    private File errorDirectory;

    public void setFactory(DpFileFactory factory) {
        this.factory = factory;
    }

    public void setInputMonitor(DpInputMonitor inputMonitor) {
        this.inputMonitor = inputMonitor;
    }

    public void setScheduler(DpScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setWriter(DpFile.Writer writer) {
        this.writer = writer;
    }

    public void setErrorDirectory(File errorDirectory) {
        this.errorDirectory = errorDirectory;
        this.logger.info("<initialize> errorDirectory = " + this.errorDirectory);
    }

    public void afterPropertiesSet() throws Exception {
        if (this.factory == null) throw new IllegalStateException("property factory not set");
        if (this.inputMonitor == null) throw new IllegalStateException("property inputMonitor not set");
        if (this.scheduler == null) throw new IllegalStateException("property scheduler not set");
        if (this.writer == null) throw new IllegalStateException("property writer not set");
        if (!FileUtil.isDirWriteable(this.errorDirectory)) {
            throw new Exception("errorDirectory not writable: "
                    + this.errorDirectory.getAbsolutePath());
        }
    }

    @Override
    public void destroy() throws Exception {
        for (DpFile dpFile : dpFiles.values()) {
            dpFile.cancel();
        }
    }

    void updateFiles(Collection<File> updatedFiles) {
        addFiles(updatedFiles); // if files exist, they will be removed during add.
    }

    void removeFiles(Collection<File> removedFiles) {
        for (final File removedFile : removedFiles) {
            removeFile(removedFile);
        }
    }

    void addFiles(Collection<File> addedFiles) {
        for (final File addedFile : addedFiles) {
            addFile(addedFile);
        }
    }

    private void addFile(File f) {
        final TimeTaker tt = new TimeTaker();

        removeFile(f);

        final File errorFile = new File(this.errorDirectory, f.getName());
        if (errorFile.exists() && !errorFile.delete()) {
            this.logger.warn("<addFile> could not delete " + errorFile.getAbsolutePath());
        }

        try {
            final DpFile dpFile = this.factory.createFrom(f);
            this.dpFiles.put(f.getAbsolutePath(), dpFile);
            this.scheduler.addJobFor(dpFile);

            this.logger.info("<addFile> " + dpFile + ", took " + tt);

        } catch (Throwable t) {
            this.logger.error("<addFile> failed for " + f.getAbsolutePath() + ": " + t.getMessage(), t);
            writeErrorFile(errorFile, t);
        }
    }

    private void writeErrorFile(File f, Throwable t) {
        try {
            final PrintStream ps = new PrintStream(new FileOutputStream(f));
            ps.println("<?xml version=\"1.0\"?><error><![CDATA[");
            t.printStackTrace(ps);
            ps.println("]]></error>");
            ps.close();
        } catch (FileNotFoundException e) {
            this.logger.error("<createErrorFile> failed for " + f.getAbsolutePath(), e);
        }
    }

    private void removeFile(File f) {
        final DpFile oldFile = this.dpFiles.remove(f.getAbsolutePath());
        if (oldFile == null) {
            return;
        }
        oldFile.cancel();
        if (oldFile.getKeyFile() != null) {
            if (oldFile.getKeyFile().delete()) {
                this.logger.info("<removeFile> removed " + oldFile);
            }
            else {
                this.logger.warn("<removeFile> failed for " + oldFile);
            }
        }
    }

    long doWrite(String spec) {
        final DpFile dpFile = this.dpFiles.get(spec);

        if (dpFile == null) {
            this.logger.warn("<doWrite> no such file: " + spec);
            return 0;
        }

        if (!dpFile.isUpToDate()) {
            this.logger.info("<doWrite> file changed, ignoring " + dpFile);
            return 0;
        }

        final TimeTaker tt = new TimeTaker();

        try {
            final File f = this.writer.write(dpFile);
            dpFile.getInfo().setUpdate(tt.getStartedAt(), tt.getElapsedMs());

            this.logger.info("<doWrite> " + f.getName() + " took " + tt
                    + " for " + dpFile.getSize() + " lines");

        } catch (Exception e) {
            this.logger.error("<doWrite> failed for " + dpFile, e);
        }

        return tt.getElapsedMs();
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "input filename")
    })
    public String triggerWrite(String fileName) {
        File f = new File(fileName);
        if (!f.isAbsolute()) {
            f = this.inputMonitor.findInputFile(fileName);
        }

        final DpFile dpFile = (f != null) ? this.dpFiles.get(f.getAbsolutePath()) : null;
        if (dpFile == null) {
            return "No such input file: " + fileName;
        }

        // NOTE: we do NOT call doWrite(spec) directly, because that method is not
        // thread-safe and it may be used concurrently be the scheduler's writeThread.
        this.scheduler.jobTriggered(dpFile);

        return "Requested output for: " + fileName;
    }

    @ManagedAttribute
    public DpFileInfo[] getDpFileInfo() {
        synchronized (this.dpFiles) {
            return this.dpFiles.values().stream().map(DpFile::getInfo).map(DpFileInfo::new).toArray(DpFileInfo[]::new);
        }
    }
}
