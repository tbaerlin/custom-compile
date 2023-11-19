/*
 * AbstractInstrumentExporter.java
 *
 * Created on 10.08.2010 10:47:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;

/**
 * Abstract instrument exporter that provides an instance of {@link Exporter} for fetching instruments.
 * An instance of {@link InstrumentDp2Preparer} may also be provided for customizing those fetched
 * instruments before they are written to instrument files.
 * <p>
 * Subclasses control the export behavior by overwriting {@link #exportInstruments(File, File, Properties)}
 * and calling {@link #exportWith(InstrumentDirWriter, ExporterCallback)} with a specific {@link ExporterCallback}.
 *
 * @author zzhao
 * @since 1.2
 */
public abstract class AbstractInstrumentExporter implements InstrumentExporter, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Exporter exporter;

    protected ExportParameters parameters = new ExportParameters();

    private InstrumentDp2Preparer instrumentPreparer;

    private File censorDir;

    public void setWhichsystem(String whichsystem) {
        this.parameters.setWhichsystem(whichsystem);
    }

    public void setForLastHours(int forLastHours) {
        this.parameters.setForLastHours(forLastHours);
    }

    public void setListOfSecurities(String listOfSecurities) {
        this.parameters.setListOfSecurities(listOfSecurities);
    }

    public final void setInstrumentPreparer(InstrumentDp2Preparer instrumentPreparer) {
        this.instrumentPreparer = instrumentPreparer;
    }

    public final void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.exporter, "An instanceof exporter is required");
    }

    public final void setCensorDir(File censorDir) {
        this.censorDir = censorDir;
    }

    public final File getCensorDir() {
        return censorDir;
    }

    /**
     * {@inheritDoc}
     */
    public ExporterResult export(File dataDir, File tmpDir, Properties prop) throws Exception {
        this.logger.info("<export> to export instruments ...");
        final TimeTaker tt = new TimeTaker();

        ExporterResult result = exportInstruments(dataDir, tmpDir, prop);

        this.logger.info("<export> #" + result.numberExported + "# instruments exported, took: " + tt);
        return result;
    }

    /**
     * Subclasses have to implement this method to perform some preparations using the given folders,
     * export instruments using {@link #exportWith(InstrumentDirWriter, ExporterCallback)} and persists
     * any relevant export properties.
     *
     * @param dataDir a directory into which the instrument files are saved
     * @param tmpDir a temporary directory for intermediate staffs
     * @param prop to access and store relevant export properties
     * @return an export result
     * @throws Exception if any thrown during export
     */
    protected abstract ExporterResult exportInstruments(File dataDir, File tmpDir, Properties prop)
            throws Exception;

    /**
     * Subclasses call this method after {@link InstrumentDirWriter} and {@link ExporterCallback} are
     * properly prepared to trigger instrument export.
     *
     * @param writer
     * @param callback
     * @return the amount of instruments exported
     * @throws Exception if any thrown during export
     */
    protected final int exportWith(InstrumentDirWriter writer, ExporterCallback callback)
            throws Exception {
        MyExportHandler myExportHandler = new MyExportHandler(writer, this.instrumentPreparer);
        callback.export(this.exporter, myExportHandler);

        return myExportHandler.numWritten;
    }

    /**
     * Used to customize the export behavior by subclasses, i.e. subclass decides which export method
     * on the given {@link Exporter} should be called.
     *
     * @author zzhao
     * @since 1.2
     */
    protected interface ExporterCallback {
        /**
         * Decides which export method on the given {@link Exporter} should be called.
         *
         * @param exporter
         * @param exportHandler
         * @throws Exception
         */
        void export(Exporter exporter, ExportHandler exportHandler) throws Exception;
    }

    private static final class MyExportHandler implements ExportHandler {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final InstrumentDp2Preparer instrumentPreparer;

        private final InstrumentDirWriter writer;

        private int numWritten = 0;

        private MyExportHandler(InstrumentDirWriter writer,
                InstrumentDp2Preparer instrumentPreparer) {
            this.instrumentPreparer = instrumentPreparer;
            this.writer = writer;
        }

        public void handle(DomainContextImpl context) throws Exception {
            if (context != null) {
                this.writer.write(context);
            }
        }

        public void handle(InstrumentDp2 instrument) throws Exception {
            if (this.instrumentPreparer != null) {
                this.instrumentPreparer.prepare(instrument);
            }

            if (this.writer.write(instrument)) {
                ++this.numWritten;
                if ((this.numWritten % 10000) == 0) {
                    this.logger.info("<handle> wrote " + this.numWritten);
                }
            }
        }
    }
}
