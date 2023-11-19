/*
 * Exporter.java
 *
 * Created on 04.12.2008 13:01:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domainimpl.DomainContextImpl;

/**
 * An exporter retrieves instrument data from a source and exports the data through
 * {@link de.marketmaker.istar.instrument.export.ExportHandler}. The scope of an export is determined
 * by which export method is invoked.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Exporter {

    /**
     * Exports instrument data according to parameters
     * <p>
     * Note that providing a domain context during export can assure the meta-data of the instruments
     * exported by this method is identical with the meta-data managed by the given domain context.
     * This also means that any potential changes in meta-data would not be reflected in export result.
     * This should do little harm given that changes in meta-data are relatively rare operations and
     * it is tolerable to reflect the changes after a work day(that's when the complete export would
     * be done) when they happen.
     *
     * @param domainContext a {@link DomainContextImpl}
     * @param handler an {@link ExportHandler}
     * @param parameters define what should be exported
     * @throws Exception if any exception occurred during exporting
     */
    void export(ExportHandler handler, ExportParameters parameters) throws Exception;
}
