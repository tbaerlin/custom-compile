/*
 * NullQuote.java
 *
 * Created on 22.04.13 15:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;

/**
 * @author tkiesgen
 */
public class NullQuote implements Quote {
    private final Instrument instrument;

    private NullQuote(Instrument instrument) {
        this.instrument = instrument;
    }

    public static Quote create(Instrument instrument) {
        return new NullQuote(instrument);
    }

    public boolean isNullQuote() {
        return true;
    }

    public Currency getCurrency() {
        return null;
    }

    public Market getMarket() {
        return null;
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public Entitlement getEntitlement() {
        return null;
    }

    public int getFirstHistoricPriceYyyymmdd() {
        return 0;
    }

    public int getQuotedef() {
        return 0;
    }

    public MinimumQuotationSize getMinimumQuotationSize() {
        return new MinimumQuotationSizeDp2();
    }

    public ContentFlags getContentFlags() {
        return ContentFlagsDp2.NO_FLAGS_SET;
    }

    public int getOrder(QuoteOrder quoteOrder) {
        return 0;
    }

    public String getSymbolVwdfeed() {
        return null;
    }

    public String getSymbolVwdfeedMarket() {
        return null;
    }

    public String getSymbolVwdcode() {
        return null;
    }

    public String getSymbolVwdsymbol() {
        return null;
    }

    public String getSymbolMmwkn() {
        return null;
    }

    public String getSymbolWmTicker() {
        return null;
    }

    public String getSymbolWmWpNameKurz() {
        return null;
    }

    public String getSymbolWmWpNameLang() {
        return null;
    }

    public String getSymbolWmWpNameZusatz() {
        return null;
    }

    public String getSymbolWmWpk() {
        return null;
    }

    public String getSymbolBisKey() {
        return null;
    }

    public String getSymbolBisKeyMarket() {
        return null;
    }

    @Override
    public String getSymbolNameSuffix() {
        return null;
    }

    @Override
    public String getSymbolVwdfeedSecondary() {
        return null;
    }

    @Override
    public String getSymbolMicSegment() {
        return null;
    }

    @Override
    public String getSymbolMicOperating() {
        return null;
    }

    @Override
    public String getSymbolInfrontId() {
        return null;
    }

    public String getSymbol(KeysystemEnum id) {
        return null;
    }

    public String getSymbol(String id) {
        return null;
    }

    public long getId() {
        return 0;
    }
}
