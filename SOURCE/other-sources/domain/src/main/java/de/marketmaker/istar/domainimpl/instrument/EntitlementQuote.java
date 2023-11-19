/*
 * EntilementQuote.java
 *
 * Created on 14.02.2007 18:40:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.util.Arrays;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;

/**
 * A {@link de.marketmaker.istar.domain.instrument.Quote} implementation that provides
 * entitlements for the KeysystemEnum VWDFEED, so that it can be used by a profile to determine
 * whether the caller has any access rights. The only methods
 * supported by this class are {@link #getEntitlement()} and {@link #getId()} (which always
 * returns 0). Invoking any other method defined in the Quote interface will yield an
 * {@link java.lang.UnsupportedOperationException}.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EntitlementQuote implements Quote, Entitlement, Market {
    private final String[] entitlements;

    private final MarketcategoryEnum marketcategory;

    private final String marketCountrySymbolIso;

    public static EntitlementQuote create(MarketcategoryEnum marketcategory,
            String marketCountrySymbolIso, String... entitlements) {
        return new EntitlementQuote(marketcategory, marketCountrySymbolIso, entitlements);
    }

    protected EntitlementQuote(MarketcategoryEnum marketcategory, String marketCountrySymbolIso,
            String... entitlements) {
        this.entitlements = entitlements;
        this.marketcategory = marketcategory;
        this.marketCountrySymbolIso = marketCountrySymbolIso;
    }

    public boolean isNullQuote() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntitlementQuote that = (EntitlementQuote) o;

        if (!Arrays.equals(entitlements, that.entitlements)) return false;
        if (marketCountrySymbolIso != null ? !marketCountrySymbolIso.equals(that.marketCountrySymbolIso) : that.marketCountrySymbolIso != null)
            return false;
        if (marketcategory != that.marketcategory) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entitlements != null ? Arrays.hashCode(entitlements) : 0;
        result = 31 * result + (marketcategory != null ? marketcategory.hashCode() : 0);
        result = 31 * result + (marketCountrySymbolIso != null ? marketCountrySymbolIso.hashCode() : 0);
        return result;
    }

    public String getMarketCountrySymbolIso() {
        return marketCountrySymbolIso;
    }

    public long getId() {
        return 0;
    }

    public Entitlement getEntitlement() {
        return this;
    }

    public String[] getEntitlements(KeysystemEnum id) {
        return (id == KeysystemEnum.VWDFEED) ? this.entitlements : null;
    }

    public Currency getCurrency() {
        throw new UnsupportedOperationException();
    }

    public Market getMarket() {
        return this;
    }

    public Instrument getInstrument() {
        throw new UnsupportedOperationException();
    }

    public int getFirstHistoricPriceYyyymmdd() {
        throw new UnsupportedOperationException();
    }

    public int getQuotedef() {
        throw new UnsupportedOperationException();
    }

    public MinimumQuotationSize getMinimumQuotationSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContentFlags getContentFlags() {
        return ContentFlagsDp2.NO_FLAGS_SET;
    }

    public int getOrder(QuoteOrder quoteOrder) {
        return 0;
    }

    public String getSymbolVwdfeed() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolVwdfeedMarket() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolVwdcode() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolVwdsymbol() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolMmwkn() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolWmTicker() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolWmWpNameKurz() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolWmWpNameLang() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolWmWpNameZusatz() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolWmWpk() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolBisKey() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolBisKeyMarket() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolNameSuffix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSymbolVwdfeedSecondary() {
        throw new UnsupportedOperationException();
    }

    public String getSymbol(KeysystemEnum id) {
        throw new UnsupportedOperationException();
    }

    public String getSymbol(String id) {
        throw new UnsupportedOperationException();
    }

    // ---------
    // MARKET interface
    // ---------
    public String getName() {
        throw new UnsupportedOperationException();
    }

    public String getName(Language language) {
        throw new UnsupportedOperationException();
    }

    public String getNameOrDefault(Language language) {
        throw new UnsupportedOperationException();
    }

    public MarketcategoryEnum getMarketcategory() {
        return this.marketcategory;
    }

    public Country getCountry() {
        return new Country() {
            public String getName() {
                return null;
            }

            public String getName(Language language) {
                return null;
            }

            public String getNameOrDefault(Language language) {
                return null;
            }

            public Currency getCurrency() {
                return null;
            }

            public String getSymbolIso() {
                return marketCountrySymbolIso;
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
        };
    }

    public String getSymbolIso() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolWm() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolMm() {
        throw new UnsupportedOperationException();
    }

    public String getSymbolDpTeam() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSymbolMicSegment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSymbolMicOperating() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getSymbolInfrontId() {
        throw new UnsupportedOperationException();
    }
}
