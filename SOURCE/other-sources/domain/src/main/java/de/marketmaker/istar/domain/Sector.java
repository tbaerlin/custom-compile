/*
 * Sector.java
 *
 * Created on 17.12.2004 16:23:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Sector extends ItemWithSymbols, ItemWithNames {
    String getName();

    String getSymbolDpTeam();
}
