/*
* SymbolListSnippet.java
*
* Created on 02.09.2008 15:10:09
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.InstrumentData;

import java.util.List;

/**
 * @author Michael Lösch
 */
public interface SymbolListSnippet {
    void setSymbols(List<InstrumentData> symbols);
} 
