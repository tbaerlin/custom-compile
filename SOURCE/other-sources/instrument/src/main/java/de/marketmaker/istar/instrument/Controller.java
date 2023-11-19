/*
 * Controller.java
 *
 * Created on 21.12.2004 14:48:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.lifecycle.Disposable;
import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.instrument.export.InstrumentDirUpdater;
import de.marketmaker.istar.instrument.export.InstrumentSystemUtil;
import de.marketmaker.istar.instrument.search.AnalyzerFactory;
import de.marketmaker.istar.instrument.search.InstrumentSearcher;
import de.marketmaker.istar.instrument.search.InstrumentSearcherImpl;
import de.marketmaker.istar.instrument.search.SuggestionSearcher;
import de.marketmaker.istar.instrument.search.SuggestionSearcherImpl;

/**
 * This class supervises the baseDir for new instrument index files,
 * reads newly available files and switches the instrumentServer to use
 * the new index.
 * Checks for new instrument index files are expected to be triggered externally by calling the
 * {@see #checkIncoming} method. If that method finds a new file in the incoming directory, it
 * will unzip it, create the components that work on those updated files and inform the
 * instrumentServers.<p>
 * If two or more services on the same machine share an instrument directory, <tt>checkIncoming</tt>
 * must only be invoked for one of them. The other services should periodically call
 * {@link #checkWorkDirSwitch()}; that method does not unzip incoming data, it just updates
 * the instrument components whenever the instrument data has changed.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class Controller implements Initializable, Disposable {

    public static final String ACTIVEDIR_NAME = "activedir.lck";

    public static final String WORK_DIR_INCREMENTAL_UPDATE = "instrument-update";

    public static final String INSTRUMENT_ZIP = "instrument.zip";

    public static final String UPDATE_ZIP = "update.zip";

    public static final String ARCHIVE = "archive";

    public static final String UPDATE_ARCHIVE = "update-archive";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<InstrumentServerUpdateable> instrumentServers = Collections.emptyList();

    private File baseDir;

    /**
     * Used to switch between the current work dir work0 (true) and work1 (false)
     */
    private boolean work0Active;

    private InstrumentDirDao instrumentDao;

    private InstrumentSearcher instrumentSearcher;

    private SuggestionSearcher suggestionSearcher;

    private final AtomicBoolean updateBusy = new AtomicBoolean(false);

    private File work0CheckFile;

    private File work1CheckFile;

    private long checkFileTimestamp;

    private boolean withSuggestionSearcher = false;

    private long maxBps = -1; // 100 * 1024 * 1024 means 100 MB/s

    private InstrumentSearcherWarmUp searcherWarmUp;

    private String searchConstraints;

    private String suggestionSearchConstraints;

    private int maxArchiveFiles = 10;

    private int maxUpdateArchiveFiles = maxArchiveFiles;

    public Controller() {
    }

    public void setMaxUpdateArchiveFiles(int maxUpdateArchiveFiles) {
        this.maxUpdateArchiveFiles = maxUpdateArchiveFiles;
    }

    public void setMaxArchiveFiles(int maxArchiveFiles) {
        this.maxArchiveFiles = maxArchiveFiles;
    }

    public void setSearchConstraints(String searchConstraints) {
        this.searchConstraints = searchConstraints;
    }

    public void setSuggestionSearchConstraints(String suggestionSearchConstraints) {
        this.suggestionSearchConstraints = suggestionSearchConstraints;
    }

    public void setSearcherWarmUp(InstrumentSearcherWarmUp searcherWarmUp) {
        this.searcherWarmUp = searcherWarmUp;
    }

    public void setWithSuggestionSearcher(boolean withSuggestionSearcher) {
        this.withSuggestionSearcher = withSuggestionSearcher;
        this.logger.info("<setWithSuggestionSearcher> " + this.withSuggestionSearcher);
    }

    public void setMaxBps(long maxBps) {
        this.maxBps = maxBps;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void setInstrumentServer(InstrumentServerUpdateable instrumentServer) {
        this.instrumentServers = Collections.singletonList(instrumentServer);
    }

    public void setInstrumentServers(List<InstrumentServerUpdateable> instrumentServers) {
        this.instrumentServers = new ArrayList<>(instrumentServers);
    }

    @Override
    public void initialize() throws Exception {
        this.work0CheckFile = new File(new File(this.baseDir, "work0"), ACTIVEDIR_NAME);
        this.work1CheckFile = new File(new File(this.baseDir, "work1"), ACTIVEDIR_NAME);

        this.work0Active = this.work0CheckFile.exists();
        final boolean work1Active = this.work1CheckFile.exists();

        if (this.work0Active == work1Active) {
            throw new IllegalStateException("none or two active work directories, manual action necessary "
            + " basedir location is '" + this.baseDir + "'");
        }

        useWorkDir(this.work0Active);

        updateCheckFileTimestamp();

        this.logger.info("<initialize> finished, instrument server has dao and searcher");
    }

    private boolean updateCheckFileTimestamp() {
        final long tmp = this.checkFileTimestamp;
        this.checkFileTimestamp = getCheckFile(this.work0Active).lastModified();
        return this.checkFileTimestamp != tmp;
    }

    private File getInstrumentDataDir(File workDir) {
        return new File(workDir, "data/instruments");
    }

    private File getWorkDir(boolean work0) {
        return new File(this.baseDir, "work" + (work0 ? "0" : "1"));
    }

    public void dispose() throws Exception {
        closeActiveComponents(true);
    }

    private void closeActiveComponents(boolean withSuggestionSearcher) {
        try {
            if (this.instrumentDao != null) {
                IoUtils.close(this.instrumentDao);
                this.logger.info("<closeActiveComponents> closed instrumentDaoFile");
            }
        }
        catch (Exception e) {
            this.logger.warn("<closeActiveComponents> failed for instrumentDao", e);
        }
        try {
            if (this.instrumentSearcher != null) {
                ((InstrumentSearcherImpl) this.instrumentSearcher).close();
                this.logger.info("<closeActiveComponents> closed instrumentSearcher");
            }
        }
        catch (IOException e) {
            this.logger.warn("<closeActiveComponents> failed for instrumentSearcher", e);
        }
        if (withSuggestionSearcher) {
            try {
                if (this.suggestionSearcher != null) {
                    ((SuggestionSearcherImpl) this.suggestionSearcher).close();
                    this.logger.info("<closeActiveComponents> closed suggestionSearcher");
                }
            }
            catch (IOException e) {
                this.logger.warn("<closeActiveComponents> failed for suggestionSearcher", e);
            }
        }
    }

    public void checkWorkDirSwitch() {
        if (this.updateBusy.getAndSet(true)) {
            this.logger.warn("<checkIncoming> update busy, returning");
            return;
        }

        try {
            doCheckWorkDirSwitch();
        }
        finally {
            this.updateBusy.set(false);
        }
    }

    private void doCheckWorkDirSwitch() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<checkWorkDirSwitch>");
        }

        final File checkFile = getCheckFile(!this.work0Active);

        if (checkFile.exists()) {
            onWorkDirSwitch();
        }
        else if (updateCheckFileTimestamp()) {
            onWorkDirUpdate();
        }
    }

    private void onWorkDirUpdate() {
        this.logger.info("<onWorkDirUpdate> check file was updated");
        try {
            useUpdatedWorkDir();
            this.logger.info("<onWorkDirUpdate> finished");
        } catch (Exception e) {
            this.logger.error("<onWorkDirUpdate> failed", e);
        }
    }

    private void onWorkDirSwitch() {
        this.logger.info("<onWorkDirSwitch> new check file, switching");

        try {
            useWorkDir(!this.work0Active);
            updateCheckFileTimestamp();
            this.logger.info("<onWorkDirSwitch> finished");
        }
        catch (Exception e) {
            this.logger.error("<onWorkDirSwitch> failed", e);
        }
    }

    public void checkIncoming() {
        if (this.updateBusy.getAndSet(true)) {
            this.logger.warn("<checkIncoming> update busy, returning");
            return;
        }

        try {
            doCheckIncoming();
            updateCheckFileTimestamp();
        }
        finally {
            this.updateBusy.set(false);
        }
    }

    private void doCheckIncoming() {
        this.logger.debug("<doCheckIncoming>");

        final File zipFileComplete = new File(this.baseDir, "incoming/" + INSTRUMENT_ZIP);
        final File zipFileUpdate = new File(this.baseDir, "incoming/" + UPDATE_ZIP);

        // if both zip files exist, the instrument.zip is preferred.
        if (zipFileComplete.exists()) {
            completeUpdate(zipFileComplete);
        }
        else if (zipFileUpdate.exists()) {
            incrementalUpdate(zipFileUpdate);
        }
        else {
            this.logger.debug("<doCheckIncoming> no update found");
        }
    }

    private void incrementalUpdate(File zipFile) {
        final TimeTaker tt = new TimeTaker();
        this.logger.info("<incrementalUpdate> using " + zipFile.getAbsolutePath());
        try {
            doIncrementalUpdate(zipFile);
            this.logger.info("<incrementalUpdate> finished in " + tt);
        } catch (Throwable t) {
            this.logger.error("<incrementalUpdate> failed", t);
        }
    }

    private void completeUpdate(File zipFile) {
        final TimeTaker tt = new TimeTaker();
        this.logger.info("<completeUpdate> using " + zipFile.getAbsolutePath());
        try {
            doCompleteUpdate(zipFile);
            this.logger.info("<completeUpdate> finished in " + tt);
        }
        catch (Throwable t) {
            this.logger.error("<completeUpdate> failed", t);
        }
    }

    private void doIncrementalUpdate(File zipFile) throws Exception {
        final File updateWorkDir = getUpdateWorkDir();
        unZipUpdateFiles(updateWorkDir, zipFile);

        File updateDataDir = getInstrumentDataDir(updateWorkDir);
        List<Long> instrumentIds = getUpdatedInstrumentIds(updateDataDir);

        final File activeWorkDir = getWorkDir(this.work0Active);
        updateData(getInstrumentDataDir(activeWorkDir), updateDataDir);

        Directory activeIndexDir = FSDirectory.open(new File(activeWorkDir, "index"));
        Directory updateIndexDir = FSDirectory.open(new File(updateWorkDir, "index"));
        updateIndexFile(instrumentIds, activeIndexDir, updateIndexDir);


        archiveZipFile(zipFile, new File(this.baseDir, UPDATE_ARCHIVE), maxUpdateArchiveFiles);

        useUpdatedWorkDir();
        touchCheckFile();

        FileUtils.cleanDirectory(updateWorkDir);
    }

    private void updateIndexFile(List<Long> instrumentIds, Directory activeIndexDir,
            Directory updateIndexDir) throws IOException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<updateIndexFile> ");
        }

        deleteIndex(instrumentIds, activeIndexDir);
        mergeIndex(activeIndexDir, updateIndexDir);
    }

    private void mergeIndex(Directory activeIndexDir, Directory updateIndexDir)
            throws IOException {
        IndexWriter iw = null;
        try {
            iw = new IndexWriter(activeIndexDir, AnalyzerFactory.getIndexAnalyzer(),
                    IndexWriter.MaxFieldLength.LIMITED);

            int numDocsBeforeMerge = iw.numDocs();
            iw.addIndexesNoOptimize(updateIndexDir);
            iw.commit();
            int numDocsAfterMerge = iw.numDocs();
            this.logger.info("<mergeIndex> " + (numDocsAfterMerge - numDocsBeforeMerge) + " documents merged");
        } finally {
            IoUtils.close(iw);
        }
    }

    private void deleteIndex(List<Long> iids, Directory dir) throws IOException {
        IndexReader ir = null;
        try {
            ir = IndexReader.open(dir, false);
            int n = 0;
            for (long iid : iids) {
                n += ir.deleteDocuments(new Term(IndexConstants.FIELDNAME_IID, Long.toString(iid)));
            }
            this.logger.info("<deleteIndex> " + n + " documents deleted");
        } finally {
            IoUtils.close(ir);
        }
    }

    private void updateData(File baseDataDir, File updateDataDir) throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<updateDataAndGetDao> start merging instrument data file ...");
        }

        InstrumentDirUpdater updater = new InstrumentDirUpdater();
        updater.setBaseDir(baseDataDir);
        updater.setUpdateDir(updateDataDir);
        updater.update();

        this.logger.info("<updateDataAndGetDao> ... finished merging");
    }

    private InstrumentDirDao createInstrumentDirDao(File baseDataDir) throws Exception {
        final InstrumentDirDao result = new InstrumentDirDao(baseDataDir);
        ensureDaoWorks(result);
        return result;
    }

    private List<Long> getUpdatedInstrumentIds(File updateDataDir) throws Exception {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getUpdatedInstrumentIds> ");
        }
        final List<Long> ret = InstrumentSystemUtil.getUpdatedInstrumentIds(updateDataDir);
        this.logger.info("<getUpdatedInstrumentIds> " + ret.size() + " instrument(s) updated");
        return ret;
    }

    private File getUpdateWorkDir() throws IOException {
        return new File(this.baseDir, WORK_DIR_INCREMENTAL_UPDATE);
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

    private void doCompleteUpdate(File zipFile) throws Exception {
        final File dir = getWorkDir(!this.work0Active);
        unZipUpdateFiles(dir, zipFile);

        archiveZipFile(zipFile, new File(this.baseDir, ARCHIVE), maxArchiveFiles);

        useWorkDir(!this.work0Active);

        switchCheckFile();
    }

    @ManagedOperation(description = "toggles the active dir to activate the other, normally old index")
    public String switchActiveIndex() throws Exception {
        if (this.updateBusy.getAndSet(true)) {
            this.logger.warn("<switchActiveIndex> update is busy, returning");
            return "update is busy, returning";
        }
        try {
            useWorkDir(!this.work0Active);
            switchCheckFile();
            return "switched to " + (this.work0Active ? "work0" : "work1");
        } finally {
            this.updateBusy.set(false);
        }
    }

    private void touchCheckFile() {
        final File f = getCheckFile(this.work0Active);
        if (!f.setLastModified(System.currentTimeMillis())) {
            this.logger.error("<touchCheckFile> failed to set lastModified on " + f.getAbsolutePath());
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

    private File getCheckFile(boolean work0) {
        return work0 ? this.work0CheckFile : this.work1CheckFile;
    }

    private void useWorkDir(boolean work0) throws Exception {
        this.logger.info("<useWorkDir> " + (work0 ? "work0" : "work1") + "...");

        final File newWorkDir = getWorkDir(work0);
        InstrumentDirDao instrumentDaoTmp = createDao(newWorkDir);

        final InstrumentSearcher instrumentSearcherTmp
                = createSearcher(newWorkDir, instrumentDaoTmp);
        this.logger.info("<useWorkDir> created searcher");

        final SuggestionSearcher suggestionSearcherTmp = createSuggestionSearcher(newWorkDir);

        warmUpInstrumentSearcher(instrumentSearcherTmp);

        updateBackends(false, instrumentDaoTmp, instrumentSearcherTmp, suggestionSearcherTmp);

        this.work0Active = work0;

        closeActiveComponents(true);

        this.instrumentDao = instrumentDaoTmp;
        this.instrumentSearcher = instrumentSearcherTmp;
        this.suggestionSearcher = suggestionSearcherTmp;
    }

    private SuggestionSearcher createSuggestionSearcher(File newWorkDir) {
        SuggestionSearcher ret = null;
        if (this.withSuggestionSearcher) {
            ret = SuggestionSearcherImpl.create(newWorkDir, this.suggestionSearchConstraints);
            if (ret != null) {
                this.logger.info("<useWorkDir> created suggestion searcher");
            }
            else {
                this.logger.warn("<useWorkDir> no suggestion searcher available");
            }
        }

        return ret;
    }

    private void warmUpInstrumentSearcher(InstrumentSearcher instrumentSearcherTmp) {
        if (null != this.searcherWarmUp) {
            this.searcherWarmUp.warmUp(instrumentSearcherTmp);
        }
    }

    private void useUpdatedWorkDir() throws Exception {
        final File newWorkDir = getWorkDir(this.work0Active);
        this.logger.info("<useUpdatedWorkDir> " + newWorkDir.getAbsolutePath());

        final InstrumentDirDao dao = createInstrumentDirDao(getInstrumentDataDir(newWorkDir));

        final InstrumentSearcher instrumentSearcherTmp = createSearcher(newWorkDir, dao);
        this.logger.info("<useUpdatedWorkDir> created searcher");

        warmUpInstrumentSearcher(instrumentSearcherTmp);

        updateBackends(true, dao, instrumentSearcherTmp, null);

        closeActiveComponents(false);

        this.instrumentDao = dao;
        this.instrumentSearcher = instrumentSearcherTmp;
    }

    private InstrumentSearcherImpl createSearcher(File workDir, InstrumentDirDao dao)
            throws IOException {
        return new InstrumentSearcherImpl(workDir, dao, this.searchConstraints);
    }

    private void updateBackends(boolean update, InstrumentDirDao dao,
            InstrumentSearcher instrumentSearcherTmp, SuggestionSearcher suggestionSearcher) {
        for (InstrumentServerUpdateable instrumentServer : this.instrumentServers) {
            instrumentServer.setInstrumentBackends(update, dao,
                    instrumentSearcherTmp, suggestionSearcher);
        }
    }

    private InstrumentDirDao createDao(File workDir) throws Exception {
        InstrumentDirDao result = null;
        try {
            result = doCreateDao(workDir);
            ensureDaoWorks(result);
            return result;
        }
        catch (Exception e) {
            IoUtils.close(result);
            throw e;
        }
    }

    private InstrumentDirDao doCreateDao(File workDir) throws Exception {
        final File dataDir = getInstrumentDataDir(workDir);
        if (dataDir.exists()) {
            return createDirDao(dataDir);
        }
        throw new IllegalStateException("no such dir: " + dataDir.getAbsolutePath());
    }

    private InstrumentDirDao createDirDao(File dataDir) throws Exception {
        this.logger.info("<createDirDao> for " + dataDir.getAbsolutePath());
        return new InstrumentDirDao(dataDir);
    }

    /**
     * Tries to read some instruments at the beginning of the instrument file; if reading fails
     * for some reason (version incompatibility) an exception will be thrown.
     *
     * @param dao temp instrument file
     */
    private void ensureDaoWorks(InstrumentDirDao dao) {
        int n = 0;

        for (Instrument instrument : dao) {
            if (++n > 1000) {
                break;
            }
        }

        this.logger.info("<ensureDaoWorks> dao is ok");
    }

    private void archiveZipFile(File zipFile, File archiveDir, int maxFiles) {
        this.logger.info("<archiveZipFile> ...");
        if (!archiveDir.isDirectory() && !archiveDir.mkdirs()) {
            this.logger.warn("<archiveZipFile> archiveDir not available: " + archiveDir.getAbsolutePath());
            // no archive dir, try to rename file in incoming dir so it won't be read again:
            archiveZipFile(zipFile, zipFile.getParentFile(), 42); // do not delete anything
            return;
        }

        final File archiveFile = getArchiveFile(zipFile, archiveDir);
        if (zipFile.renameTo(archiveFile)) {
            this.logger.info("<archiveZipFile> archived input as " + archiveFile.getName());
        }
        else {
            this.logger.warn("<archiveZipFile> failed to mv " + zipFile.getAbsolutePath() +" to "
                + archiveFile.getAbsolutePath());
        }

        deleteOldArchiveFiles(archiveDir, maxFiles);
        this.logger.info("<archiveZipFile> finished");
    }

    private void deleteOldArchiveFiles(File archiveDir, int maxFiles) {
        final File[] files = archiveDir.listFiles((FileFilter) new SuffixFileFilter(".zip"));
        if (files.length > maxFiles) {
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
            for (int i = 0; i < files.length - maxFiles; i++) {
                final boolean deleted = files[i].delete();
                this.logger.info("<archiveZipFile> delete " + files[i].getName() + ": " + deleted);
            }
        }
    }

    private File getArchiveFile(File f, File dir) {
        final String name = f.getName();
        final String archiveName = name.substring(0, name.lastIndexOf("."))
                + "-" + new DateTime().toString("yyyyMMdd-HHmmss") + ".zip";
        return new File(dir, archiveName);
    }


    public static void main(String[] args) throws Exception {
        final Controller controller = new Controller();
        controller.setBaseDir(new File(LocalConfigProvider.getProductionBaseDir(), "istar-instrument"));
        controller.doCompleteUpdate(new File(LocalConfigProvider.getProductionBaseDir(), "istar-instrument/incoming/" + INSTRUMENT_ZIP));
    }
}
