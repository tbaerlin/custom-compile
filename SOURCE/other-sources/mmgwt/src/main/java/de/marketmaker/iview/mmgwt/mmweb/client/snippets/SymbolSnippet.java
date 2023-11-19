/*
 * SymbolSnippet.java
 *
 * Created on 12.06.2008 13:01:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;

/**
 * @author Ulrich Maurer
 */
public interface SymbolSnippet {
    public static final String NO_SYMBOL = "none"; // $NON-NLS-0$
    public static final String SYMBOL_UNDERLYING = "underlying"; // $NON-NLS-0$

    void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols);
}
