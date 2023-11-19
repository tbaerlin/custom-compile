/*
 * Country.java
 *
 * Created on 17.09.2004 11:38:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Country extends ItemWithSymbols, ItemWithNames {
    String getName();

    Currency getCurrency();

    String getSymbolIso();
}
