/*
 * Market.java
 *
 * Created on 17.09.2004 11:13:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Market extends ItemWithSymbols, ItemWithNames {
    String getName();

    MarketcategoryEnum getMarketcategory();

    Country getCountry();

    String getSymbolIso();

    String getSymbolVwdfeed();

    String getSymbolWm();

    String getSymbolMm();

    String getSymbolDpTeam();

    String getSymbolMicSegment();

    String getSymbolMicOperating();
}
