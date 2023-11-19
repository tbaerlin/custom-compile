/*
 * SymbolCommand.java
 *
 * Created on 01.08.2006 13:06:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

/**
 * A command that specifies multiple symbols.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MultiSymbolCommand extends HasSymbolArray, HasSymbolCommand {
    /**
     * @return Market strategies for individual symbols, format is <tt>symbol=marketStrategy</tt>
     */
    String[] getMarketStrategyOverride();
}