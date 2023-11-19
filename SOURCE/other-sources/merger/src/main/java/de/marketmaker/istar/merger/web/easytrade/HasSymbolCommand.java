/*
 * HasSymbolCommand.java
 *
 * Created on 27.06.12 09:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

/**
 * Specifies how symbols in a command context should be interpreted.
 * @author oflege
 */
public interface HasSymbolCommand extends HasSymbolStrategy, HasMarketStrategy {
    /**
     * @return vwd market name used to identify a particular quote (e.g., <tt>ETR</tt> for symbol
     * <tt>710000</tt> will select the Xetra Quote) if the <tt>symbol</tt> identifies an
     * instrument rather than a specific quote. Will only be used if <tt>marketStrategy</tt>
     * is undefined. In general, client's should prefer to use <tt>marketStrategy</tt> as
     * it is more flexible and does not require knowledge of all the different market symbols.
     */
    String getMarket();
}
