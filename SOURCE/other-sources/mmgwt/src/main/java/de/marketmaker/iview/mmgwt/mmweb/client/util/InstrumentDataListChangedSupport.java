/*
 * InstrumentDataListChangedSupport.java
 *
 * Created on 15.04.13 16:03
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.InstrumentData;

import java.util.List;

/**
 * @author Markus Dick
 */
public class InstrumentDataListChangedSupport {
    private int currentSymbolsHashCode = 0;

    public boolean hasChanged(List<InstrumentData> symbols) {
        final int symbolsHashCode = calcualteSymbolsHashCode(symbols);
        if(this.currentSymbolsHashCode != symbolsHashCode) {
            this.currentSymbolsHashCode = symbolsHashCode;
            return true;
        }
        return false;
    }

    public void reset() {
        this.currentSymbolsHashCode = 0;
    }

    private int calcualteSymbolsHashCode(List<InstrumentData> symbols) {
        int hashCode = 7;
        for (InstrumentData aList : symbols) {
            hashCode = hashCode * 31 + aList.getIid().hashCode();
        }
        return hashCode;
    }
}
