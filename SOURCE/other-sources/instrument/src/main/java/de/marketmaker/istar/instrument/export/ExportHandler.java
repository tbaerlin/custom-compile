/*
 * ExportHandler.java
 *
 * Created on 21.02.2008 10:13:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;

/**
 * An export handler processes raw instrument related data and meta-data during which the given
 * domain context or instrument may or may not adjusted.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ExportHandler {

    /**
     * Processes instrument related meta-data contained in a
     * {@link de.marketmaker.istar.domainimpl.DomainContextImpl}.
     *
     * @param context a {@link de.marketmaker.istar.domainimpl.DomainContextImpl}.
     * @throws Exception if any exception occurred during processing.
     */
    void handle(DomainContextImpl context) throws Exception;

    /**
     * Processes an instrument.
     *
     * @param instrument an {@link de.marketmaker.istar.domainimpl.instrument.InstrumentDp2}
     * @throws Exception if any exception occurred during processing.
     */
    void handle(InstrumentDp2 instrument) throws Exception;
}
