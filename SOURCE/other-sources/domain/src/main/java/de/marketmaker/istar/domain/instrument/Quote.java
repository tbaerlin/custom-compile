/*
 * Quote.java
 *
 * Created on 17.09.2004 09:32:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.Market;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Quote extends ItemWithSymbols{
    boolean isNullQuote();

    Currency getCurrency();

    Market getMarket();

    Instrument getInstrument();

    Entitlement getEntitlement();

    int getFirstHistoricPriceYyyymmdd();

    int getQuotedef();

    MinimumQuotationSize getMinimumQuotationSize();

    ContentFlags getContentFlags();

    /**
     * Returns the order value for the given quoteOrder
     * @param quoteOrder order system, must not be null
     * @return order for this quote in the given order system
     */
    int getOrder(QuoteOrder quoteOrder);

    /**
     * @return feed symbol, e.g., <tt>1.802200.STG</tt>
     */
    String getSymbolVwdfeed();

    /**
     * @return market in getSymbolVwdcde, e.g., <tt>STG</tt>
     */
    String getSymbolVwdfeedMarket();

    /**
     * @return feed symbol without type, e.g., <tt>802200.STG</tt>
     */
    String getSymbolVwdcode();

    /**
     * @return feed symbol without type and market, e.g., <tt>802200</tt>
     */
    String getSymbolVwdsymbol();

    String getSymbolMmwkn();
    String getSymbolWmTicker();
    String getSymbolWmWpNameKurz();
    String getSymbolWmWpNameLang();
    String getSymbolWmWpNameZusatz();
    String getSymbolWmWpk();
    String getSymbolBisKey();
    String getSymbolBisKeyMarket();
    String getSymbolNameSuffix();
    String getSymbolVwdfeedSecondary();
    String getSymbolMicSegment();
    String getSymbolMicOperating();
    String getSymbolInfrontId();
}
