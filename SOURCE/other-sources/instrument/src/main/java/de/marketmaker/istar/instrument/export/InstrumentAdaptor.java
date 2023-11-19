/*
 * QuoteAdaptor.java
 *
 * Created on 22.03.2010 15:03:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;

/**
 * @author oflege
 */
public interface InstrumentAdaptor {
    void adapt(InstrumentDp2 instrument);
}
