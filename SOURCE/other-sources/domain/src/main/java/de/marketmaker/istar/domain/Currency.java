/*
 * Currency.java
 *
 * Created on 17.09.2004 11:13:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Currency extends ItemWithSymbols, ItemWithNames {
    String getName();

    String getSymbolIso();

    /**
     * @return true iff this is cent based unit derived from some base currency (e.g., GBX from GBP)
     */
    boolean isCent();

    /**
     * @return the iso symbol for the base currency if {@link #isCent()} returns true, otherwise
     * the same as {@link #getSymbolIso()}.
     */
    String getBaseCurrencyIso();
}
