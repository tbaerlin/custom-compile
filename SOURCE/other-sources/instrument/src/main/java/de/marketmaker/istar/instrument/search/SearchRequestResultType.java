/*
 * SearchRequestResultType.java
 *
 * Created on 11.03.2005 12:57:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum SearchRequestResultType {
    QUOTE_WITH_VWDSYMBOL,
    QUOTE_WITH_MMSYMBOL_OR_VWDSYMBOL,
    QUOTE_ANY,
    QUOTE_WITH_VWDSYMBOL_AND_INSTRUMENT_WITH_ISIN;
}
