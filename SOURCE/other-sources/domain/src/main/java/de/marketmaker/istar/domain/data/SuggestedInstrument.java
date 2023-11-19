/*
 * SuggestedInstrument.java
 *
 * Created on 17.06.2009 11:36:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SuggestedInstrument<E extends SuggestedInstrument> extends Comparable<E> {
    long getId();
    InstrumentTypeEnum getInstrumentType();
    String getName();
    String getSymbolIsin();
    String getSymbolWkn();
}
