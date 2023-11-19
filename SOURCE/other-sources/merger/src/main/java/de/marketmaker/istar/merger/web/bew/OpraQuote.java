/*
 * OpraQuote.java
 *
 * Created on 03.08.2010 11:49:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.math.BigDecimal;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.domainimpl.CountryDp2;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.EntitlementDp2;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.data.PriceRecordImpl;
import de.marketmaker.istar.domainimpl.instrument.ContentFlagsDp2;
import de.marketmaker.istar.domainimpl.instrument.MinimumQuotationSizeDp2;
import de.marketmaker.istar.ratios.opra.OpraItem;

/**
 * @author oflege
 */
class OpraQuote implements Quote {
    private static final Currency USD = new CurrencyDp2(0, "USD") {
        public String getSymbolIso() {
            return "USD";
        }
    };

    private static final Country US = new CountryDp2(0, "US") {
        @Override
        public Currency getCurrency() {
            return USD;
        }
    };

    static final EntitlementDp2 ENTITLEMENT = new EntitlementDp2();

    static {
        ENTITLEMENT.setEntitlements(KeysystemEnum.VWDFEED, new String[]{"16E"});
    }

    private final Instrument instrument;

    private final OpraItem item;

    OpraQuote(OpraItem item, Instrument instrument) {
        this.item = item;
        this.instrument = instrument;
    }

    public boolean isNullQuote() {
        return false;
    }

    @Override
    public String toString() {
        return "OpraQuote[" + this.item.getVwdcode() + "]";
    }

    public Currency getCurrency() {
        return USD;
    }

    public Market getMarket() {
        return new MarketDp2(0, this.item.getExchange()) {
            public String getSymbolVwdfeed() {
                return OpraQuote.this.item.getExchange();
            }

            @Override
            public Country getCountry() {
                return US;
            }
        };
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public Entitlement getEntitlement() {
        return ENTITLEMENT;
    }

    public int getFirstHistoricPriceYyyymmdd() {
        return 0;
    }

    public int getQuotedef() {
        return 0;
    }

    public MinimumQuotationSize getMinimumQuotationSize() {
        return new MinimumQuotationSizeDp2(BigDecimal.ONE, MinimumQuotationSize.Unit.UNIT, USD);
    }

    @Override
    public ContentFlags getContentFlags() {
        return ContentFlagsDp2.NO_FLAGS_SET;
    }

    public int getOrder(QuoteOrder quoteOrder) {
        return 0;
    }

    public String getSymbolVwdfeed() {
        return "2." + this.item.getVwdcode();
    }

    public String getSymbolVwdfeedMarket() {
        final String s = this.item.getVwdcode();
        final int p = s.indexOf('.') + 1;
        return s.substring(p, s.indexOf('.', p));
    }

    public String getSymbolVwdcode() {
        return this.item.getVwdcode();
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

    public PriceRecord getPriceRecord() {
        final PriceRecordImpl result = new PriceRecordImpl();
        Price p = getPrice();
        result.setValuationPriceEoD(p);
        result.setSettlement(p);
        return result;
    }

    private Price getPrice() {
        if (this.item.getSettlementDate() == null) {
            return NullPrice.INSTANCE;
        }
        return new PriceImpl(PriceCoder.decode(this.item.getSettlement()), 0L, null,
                this.item.getSettlementDate().toDateTimeAtStartOfDay(), PriceQuality.END_OF_DAY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpraQuote opraQuote = (OpraQuote) o;

        if (!item.equals(opraQuote.item)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }
}
