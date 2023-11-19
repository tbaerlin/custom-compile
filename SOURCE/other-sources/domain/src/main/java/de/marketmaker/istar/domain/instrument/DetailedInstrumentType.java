/*
 * DetailedInstrumentType.java
 *
 * Created on 17.12.2004 16:41:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import de.marketmaker.istar.domain.ItemWithSymbols;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DetailedInstrumentType extends ItemWithSymbols {
    String getSymbolWmGd198bId();
    String getSymbolWmGd198cId();
}
