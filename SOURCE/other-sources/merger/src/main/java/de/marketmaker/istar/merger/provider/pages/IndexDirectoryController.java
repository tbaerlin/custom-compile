/*
 * IndexDirectoryController.java
 *
 * Created on 19.07.2010 16:26:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.util.FileUtil;

/**
 * This controller handles on-the-fly updates of indices.
 * It uses a file system base directory and two subdirectories, work0 and work1,
 * for a Lucene index. Exactly one of work0 and work1 is active at a time. <br/>
 * This class offers a method
 * {@link #checkIncoming()} to poll for a new index and switch to it immediately.
 * <br /> <br />
 * <b>Never</b> use two instances of this class on the same base directory. This class
 * is not designed to handle this. Calling {@link #checkIncoming()} from several
 * threads is OK, but calls will return immediately without any effect, if an update is still in
 * progress.
 * @author Sebastian Wild
 */
public class IndexDirectoryController implements InitializingBean {

    /**
     * The name of the update file to look for. Use {@link #checkIncoming()} to update,
     * once this file has been created.
     */
    public static final String NEW_INDEX_ZIP_FILE = "incoming/pages-index.zip";

    /**
     * The name of the <em>lock file</em>. <br/>
     * A file of this name in a work0/1 directory indicates that this directory is currently
     * active.<br/>
     * Make sure, no file with this name is needed as part of the index data.
     */
    public static final String ACTIVEDIR_NAME = "activedir.lck";

    private List<UpdatableDirectory> targets = new LinkedList<>();

    private File baseDir;

    /**
     * Used to switch between the current work dir work0 (true) and work1 (false)
     */
    private boolean work0Active;

    private File work0CheckFile;

    private File work1CheckFile;

    private long maxBps = -1; // 100 * 1024 * 1024 means 100 MB/s

    private final AtomicBoolean updateBusy = new AtomicBoolean(false);

    private Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * This method is intended to be called on a regular basis, polling for pending updates.
     * If a new update is found, working directories are immediately switched in all registered
     * targets. <br />
     * <b>This method will block, until all targets could be updated, i.e. until all calls
     * to {@link de.marketmaker.istar.merger.provider.pages.UpdatableDirectory#setDirectory(org.apache.lucene.store.Directory)}
     * have returned.</b> If an update is already in progress, this method will immediately
     * return false.
     * @return true iff the update was successfully finished or no update was pending, i.e.
     *         false is returned in case of error during update or if a concurrent update is
     *         in progress.
     */
    public boolean checkIncoming() {
        if (this.updateBusy.getAndSet(true)) {
            this.logger.warn("<checkIncoming> update busy, returning");
            return false;
        }

        try {
            doCheckIncoming();
            return true;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            this.updateBusy.set(false);
        }
    }

    /**
     * Perform the actual update. This method asserts, that it is never called concurrently.
     */
    private void doCheckIncoming() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<doCheckIncoming> ");
        }

        final File zipFile = new File(this.baseDir, NEW_INDEX_ZIP_FILE);

        if (!zipFile.exists()) {
            this.logger.debug("<doCheckIncoming> no zip file found - update ignored");
        }
        else {
            this.logger.info("<doCheckIncoming> zip file with new pages index found, " +
                    "starting complete update");
            try {
                final File dir = getWorkDir(!this.work0Active);
                unZipUpdateFiles(dir, zipFile);

                archiveZipFile(zipFile, new File(this.baseDir, "archive"));

                useWorkDir(!this.work0Active);

                switchCheckFile();
            }
            catch (Throwable e) {
                this.logger.error("<doCheckIncoming> complete update failed", e);
            }
            this.logger.info("<doCheckIncoming> complete update finished");
        }
    }


    public void afterPropertiesSet() throws Exception {
        this.logger.info("<afterPropertiesSet> initializing new IndexDirectoryController");
        this.work0CheckFile = new File(new File(this.baseDir, "work0"), ACTIVEDIR_NAME);
        this.work1CheckFile = new File(new File(this.baseDir, "work1"), ACTIVEDIR_NAME);

        this.work0Active = this.work0CheckFile.exists();
        final boolean work1Active = this.work1CheckFile.exists();

        if (this.work0Active && work1Active) {
            throw new IllegalStateException("two active work directories, manual action necessary");
        }
        if (!(this.work0Active || work1Active)) {
            throw new IllegalStateException("no active work directory, manual action necessary");
        }

        useWorkDir(this.work0Active);

        this.logger.info("<afterPropertiesSet> IndexDirectoryController initialized");
    }

    /**
     * This method takes the given working directory (work0 if param {@code work0} is true and
     * work1 otherwise), creates a new Lucene Directory for this directory and updates all
     * registered targets. Note: This method does not touch the file locks {@link #ACTIVEDIR_NAME}.
     * @param work0
     */
    private void useWorkDir(boolean work0) {
        this.logger.info("<useWorkDir> Trying to use work" + (work0 ? "0" : "1") + ".");
        this.work0Active = work0;

        Directory indexDir = null;
        try {
            indexDir = FSDirectory.open(getWorkDir(work0));
        } catch (IOException e) {
            this.logger.error("<useWorkDir> Error opening IndexSearcher for directory " + indexDir, e);
        }

        this.logger.info("<useWorkDir> Setting new directory at targets.");
        for (UpdatableDirectory target : targets) {
            target.setDirectory(indexDir);
        }
        this.logger.info("<useWorkDir> Switched to " + getWorkDir(this.work0Active) + ".");
    }

    private void archiveZipFile(File zipFile, File archiveDir) {
        this.logger.info("<archiveZipFile> ...");

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        final File archiveFile = new File(archiveDir, "pages-index-" + sdf.format(new Date()) + ".zip");
        if (zipFile.renameTo(archiveFile)) {
            this.logger.info("<archiveZipFile> archived input as " + archiveFile.getName());
        }
        else {
            this.logger.warn("<archiveZipFile> Could not archive zip-file " +
                    zipFile.getAbsolutePath());
        }

        final String[] files = archiveDir.list();
        if (files != null && files.length > 10) {
            Arrays.sort(files);
            for (int i = 0; i < files.length - 10; i++) {
                final File f = new File(archiveDir, files[i]);
                final boolean deleted = f.delete();
                this.logger.info("<archiveZipFile> deleted " + f.getName() + ": " + deleted);
            }
        }
        this.logger.info("<archiveZipFile> finished");
    }

    private void unZipUpdateFiles(File dir, File zipFile) throws IOException {
        createOrCleanDir(dir);
        if (this.maxBps < 0) {
            this.logger.info("<unZipUpdateFiles> unzipping, unlimited");
            FileUtil.unzipToDir(zipFile, dir);
        }
        else {
            this.logger.info("<unZipUpdateFiles> unzipping, maxBps=" + this.maxBps);
            FileUtil.unzipToDir(zipFile, dir, this.maxBps);
        }
    }

    private void switchCheckFile() {
        final File oldFile = getCheckFile(!this.work0Active);
        final File newFile = getCheckFile(this.work0Active);
        try {
            if (oldFile.delete()) {
                this.logger.info("<switchCheckFile> deleted " + oldFile.getAbsolutePath());
            }
            else {
                this.logger.error("<switchCheckFile> failed deleting old check file: " + oldFile.getAbsolutePath());
            }

            if (newFile.createNewFile()) {
                this.logger.info("<switchCheckFile> created " + newFile.getAbsolutePath());
            }
            else {
                this.logger.error("<switchCheckFile> failed creating check file: " + newFile.getAbsolutePath());
            }
        }
        catch (IOException e) {
            this.logger.error("<switchCheckFile> failed creating check file", e);
        }
    }


    /**
     * Add {@code target} to the list of targets, that are notified on directory changes.
     * @param target the new target to add
     */
    public void registerTarget(UpdatableDirectory target) {
        if (targets.contains(target)) {
            this.logger.warn("<addTarget> Tried to add target twice");
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<addTarget> Added target.");
            }
            targets.add(target);
        }
    }

    /**
     * Remove a registered target. If {@code target} was never registered, nothing happens.
     * @param target target to remove.
     */
    public void removeTarget(UpdatableDirectory target) {
        targets.remove(target);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<removeTarget> Removed a target.");
        }
    }

    private File getWorkDir(boolean work0) {
        return new File(this.baseDir, "work" + (work0 ? "0" : "1"));
    }

    private File getCheckFile(boolean work0) {
        assert this.work0CheckFile != null;
        assert this.work1CheckFile != null;
        return work0 ? this.work0CheckFile : this.work1CheckFile;
    }

    private void createOrCleanDir(File dir) throws IOException {
        if (dir.exists()) {
            FileUtils.cleanDirectory(dir);
        }
        else {
            boolean created = dir.mkdir();
            if (!created) {
                throw new IllegalStateException("cannot create dir: " + dir.getAbsolutePath());
            }
        }
    }


    // properties

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public long getMaxBps() {
        return maxBps;
    }

    public void setMaxBps(long maxBps) {
        this.maxBps = maxBps;
    }

    public List<UpdatableDirectory> getTargets() {
        return targets;
    }

    public void setTargets(List<UpdatableDirectory> targets) {
        this.targets = new ArrayList<>(targets);
    }

    public void setTarget(UpdatableDirectory target) {
        this.targets = Collections.singletonList(target);
    }
}
