/*
 * ExportPerformer2.java
 *
 * Created on 09.08.2010 13:57:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.Properties;

import de.marketmaker.istar.domainimpl.DomainContextImpl;

/**
 * An instrument exporter specifies how instruments are exported into a given directory via a temporary
 * directory and some export properties.
 * <p>
 * Results related with an export run are stored in an {@link ExporterResult}.
 *
 * @author zzhao
 * @since 1.2
 */
interface InstrumentExporter {

    /**
     * Performs instrument export and writes instruments into the given data directory. Export relevant
     * properties' changes could be made against the given properties.
     *
     * @param dataDir a directory where the final instruments artifact would be.
     * @param tmpDir a directory into which the temporary instruments artifact is written.
     * @param prop export relevant properties.
     */
    ExporterResult export(File dataDir, File tmpDir, Properties prop) throws Exception;

    /**
     * An exporter result contains artifacts created during export and some status information.
     */
    class ExporterResult {
        /**
         * Domain context created during export
         */
        final DomainContextImpl domainContext;

        /**
         * Indicates whether further export steps should be skipped.
         */
        boolean skipRestSteps = false;

        /**
         * Indicates the number of instruments exported.
         */
        int numberExported = 0;

        ExporterResult(DomainContextImpl domainContext) {
            this.domainContext = domainContext;
        }

        public ExporterResult withSkipRestSteps(boolean skip) {
            this.skipRestSteps = skip;
            return this;
        }

        public ExporterResult withNumberExported(int numExporter) {
            this.numberExported = numExporter;
            return this;
        }
    }
}
