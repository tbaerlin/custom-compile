/*
 * ExportControllerDp3.java
 *
 * Created on 09.08.2010 10:50:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;


/**
 * An instrument export controller which collaborates instruments export and indexing.
 * <p>
 * It is used as a spring managed bean. The following properties should be set:
 * <ul>
 * <li>{@link #workDir}</li>
 * <li>{@link #tempDir}</li>
 * <li>{@link #exportProperties}</li>
 * </ul>
 * and the following export modes are supported:
 * <table border='1'>
 * <tr>
 * <th>mode</th>
 * <th>{@link #instrumentExporter}</th>
 * <th>{@link #instrumentIndexer}</th>
 * <th>{@link #mpcIndexer}</th>
 * <th>{@link #suggestionExporter}</th>
 * <th>{@link #suggestionIndexer}</th></tr>
 * <tr><td>Complete export and index</td>
 * <td>required</td><td>required</td><td>optional</td><td>optional</td><td>optional</td></tr>
 * <tr><td>Incremental export and index</td>
 * <td>required</td><td>required</td><td>optional</td><td>optional</td><td>optional</td></tr>
 * <tr><td>Reindex existing instruments</td>
 * <td>don't set</td><td>required</td><td>optional</td><td>optional</td><td>optional</td></tr>
 * </table>
 * Note that those optional indexers depend on the basic instrument indexer or the directory in which
 * those index files reside.
 *
 * @author zzhao
 * @since 1.2
 */
public class ExportControllerDp2 implements InitializingBean, Lifecycle {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Properties exportProperties;

    private File tempDir;

    private File workDir;

    private final ExecutorService es = Executors.newSingleThreadExecutor(r -> new Thread(r, "ecdp2"));

    private InstrumentExporter instrumentExporter;

    private InstrumentIndexer instrumentIndexer;

    private MpcIndexer mpcIndexer;

    private SuggestionExporter suggestionExporter;

    private SuggestionIndexer suggestionIndexer;

    private Future<?> f;

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public void setInstrumentExporter(InstrumentExporter instrumentExporter) {
        this.instrumentExporter = instrumentExporter;
    }

    public void setInstrumentIndexer(InstrumentIndexer instrumentIndexer) {
        this.instrumentIndexer = instrumentIndexer;
    }

    public void setMpcIndexer(MpcIndexer mpcIndexer) {
        this.mpcIndexer = mpcIndexer;
    }

    public void setSuggestionExporter(SuggestionExporter suggestionExporter) {
        this.suggestionExporter = suggestionExporter;
    }

    public void setSuggestionIndexer(SuggestionIndexer suggestionIndexer) {
        this.suggestionIndexer = suggestionIndexer;
    }

    public void setExportProperties(Properties exportProperties) {
        this.exportProperties = exportProperties;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.isTrue(null != this.workDir && this.workDir.exists(), "Working dir required");
        Assert.isTrue(null != this.tempDir && this.tempDir.exists(), "Temporary working dir required");
    }

    protected void export() {
        try {
            doExport();
        } catch (Exception e) {
            this.logger.error("Error while exporting: ", e);
            // Convert this into RuntimeException to be able to
            // be started in an ExecutorService and rethrow
            throw new RuntimeException(e);
        }
    }

    private void doExport() throws Exception {
        final TimeTaker tt = new TimeTaker();

        final File dataDir = getDir(this.workDir, "data");
        final File instrumentDir = getDir(dataDir, "instruments");

        InstrumentExporter.ExporterResult exporterResult = null;
        if (null != this.instrumentExporter) {
            exporterResult = this.instrumentExporter.export(instrumentDir, this.tempDir,
                    this.exportProperties);
            if (exporterResult.skipRestSteps) {
                this.logger.info("<doExport> rest steps skipped: " + tt);
                return;
            }
        }
        else {
            this.logger.info("<doExport> w/o instruments exporter");
        }

        InstrumentDirDao dao = null;
        try {
            if (null != exporterResult) {
                dao = new InstrumentDirDao(instrumentDir, exporterResult.domainContext);
            }
            else {
                dao = new InstrumentDirDao(instrumentDir);
            }

            final File instrumentIndexDir = getDir(this.workDir, "index");
            if (null != this.instrumentIndexer) {
                this.instrumentIndexer.index(instrumentIndexDir, dao);
            }
            else {
                this.logger.info("<doExport> w/o instruments indexer");
            }

            final File mpcIndexDir = getDir(this.workDir, "index-mpc");
            if (null != this.mpcIndexer) {
                this.mpcIndexer.index(mpcIndexDir, instrumentIndexDir);
            }
            else {
                this.logger.info("<doExport> w/o MPC indexer");
            }

            final File suggestionDataFile = getFile(dataDir, "suggest.dat");
            if (null != this.suggestionExporter) {
                this.suggestionExporter.export(suggestionDataFile, instrumentIndexDir, dao,
                        this.tempDir);
            }
            else {
                this.logger.info("<doExport> w/o suggestions exporter");
            }

            if (null != this.suggestionIndexer) {
                final File suggestionIndexDir = getDir(this.workDir, "index-suggest");
                this.suggestionIndexer.index(suggestionDataFile, suggestionIndexDir);
            }
            else {
                this.logger.info("<doExport> w/o suggestions indexer");
            }
        } finally {
            IoUtils.close(dao);
        }

        this.logger.info("<doExport> export process totally took: " + tt);
    }

    @Override
    public void start() {
        this.f = this.es.submit(this::export);
        this.es.shutdown();
    }

    @Override
    public void stop() {
        try {
            this.f.get(6, TimeUnit.HOURS);
            this.logger.info("<stop> termination");
        } catch (InterruptedException e) {
            this.logger.info("<stop> interrupted?!");
        } catch (ExecutionException e) {
            this.logger.error("<stop> export failed", e);
        } catch (TimeoutException e) {
            this.logger.error("<stop> export timeout?!");
        }
    }

    @Override
    public boolean isRunning() {
        return this.f != null;
    }

    private File getDir(File dirName, String childDirName) throws IOException {
        return InstrumentSystemUtil.getDir(dirName, childDirName, false);
    }

    private File getFile(File dirName, String fileName) throws IOException {
        return InstrumentSystemUtil.getFile(dirName, fileName, false);
    }
}
