/*
 * QuoteDp2.java
 *
 * Created on 20.12.2004 13:46:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.domainimpl.EntitlementDp2;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteDp2 extends ItemWithSymbolsDp2 implements Quote, Serializable {
    static final long serialVersionUID = -7250843217942178290L;

    private Market market;

    private Currency currency;

    private Instrument instrument;

    private int firstHistoricPriceYyyymmdd;

    private int quotedef;

    private EntitlementDp2 entitlement = new EntitlementDp2();

    private MinimumQuotationSizeDp2 minimumQuotationSize;

    private long[] flags = new long[1];

    /**
     * Stores the order of this quote among all other quotes of the same instrument.
     * Since each instrument has only a small number of quotes, this long is used to store up to
     * 7 different orders in its respective bytes.
     */
    private long orders;

    public QuoteDp2() {
    }

    public QuoteDp2(long id) {
        super(id);
    }

    public boolean isNullQuote() {
        return false;
    }

    public long[] getFlags() {
        return Arrays.copyOf(this.flags, this.flags.length);
    }

    public void setFlags(int i, long val) {
        if (i >= this.flags.length) {
            this.flags = Arrays.copyOf(this.flags, i + 1);
        }
        this.flags[i] = val;
    }

    public long getFlags(int i) {
        return this.flags[i];
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public int getFirstHistoricPriceYyyymmdd() {
        return firstHistoricPriceYyyymmdd;
    }

    public void setFirstHistoricPriceYyyymmdd(int firstHistoricPriceYyyymmdd) {
        this.firstHistoricPriceYyyymmdd = firstHistoricPriceYyyymmdd;
    }

    public int getQuotedef() {
        return quotedef;
    }

    public MinimumQuotationSize getMinimumQuotationSize() {
        return minimumQuotationSize;
    }

    @Override
    public ContentFlags getContentFlags() {
        return new ContentFlagsDp2(this.flags);
    }

    public int getOrder(QuoteOrder quoteOrder) {
        return (int) ((this.orders >> (quoteOrder.ordinal() * 8)) & 0xFFL);
    }

    public void setOrder(QuoteOrder quoteOrder, int value) {
        if (value < 0 || value > 0xFFL) {
            throw new IllegalArgumentException("not in [0..255]: " + value);
        }
        this.orders &= (0x7FFFFFFFFFFFFFFFL ^ (0xFFL << (quoteOrder.ordinal() * 8))); // clear current
        this.orders |= (((long) value) << (quoteOrder.ordinal() * 8));
    }

    // for FAST serialization
    public long getOrders() {
        return this.orders;
    }

    // for FAST de-serialization
    public void setOrders(long orders) {
        this.orders = orders;
    }

    @Override
    public void setSymbol(KeysystemEnum anEnum, String symbol) {
        super.setSymbol(anEnum, symbol);
        // ensure consistent symbols:
        if (symbol != null && anEnum == KeysystemEnum.VWDFEED) {
            final int index = symbol.indexOf(".");
            super.setSymbol(KeysystemEnum.VWDCODE, symbol.substring(index + 1));

            final int indexSymbolEnd = symbol.indexOf(".", index + 1);
            super.setSymbol(KeysystemEnum.VWDSYMBOL, symbol.substring(index + 1, indexSymbolEnd));
        }
    }

    public String getSymbolVwdfeed() {
        return getSymbol(KeysystemEnum.VWDFEED);
    }

    public String getSymbolVwdfeedMarket() {
        final String s = getSymbolVwdfeed();
        if (s == null) {
            return null;
        }
        final int start = s.indexOf('.', 3) + 1;
        if (start == 0) {
            return null;
        }

        int end = s.indexOf('.', start + 1);
        if (end == -1) {
            end = s.length();
        }
        return s.substring(start, end);
    }

    public String getSymbolMmwkn() {
        return getSymbol(KeysystemEnum.MMWKN);
    }

    public String getSymbolVwdcode() {
        return getSymbol(KeysystemEnum.VWDCODE);
    }

    public String getSymbolVwdsymbol() {
        return getSymbol(KeysystemEnum.VWDSYMBOL);
    }

    public String getSymbolWmTicker() {
        return getSymbol(KeysystemEnum.WM_TICKER);
    }

    public String getSymbolWmWpk() {
        return getSymbol(KeysystemEnum.WM_WPK);
    }

    public String getSymbolBisKey() {
        return getSymbol(KeysystemEnum.BIS_KEY);
    }

    public String getSymbolBisKeyMarket() {
        final String symbol = getSymbol(KeysystemEnum.BIS_KEY);
        if (symbol == null) {
            return null;
        }
        final int index = symbol.indexOf("_");
        return index > 0 ? symbol.substring(0, index) : null;
    }

    public String getSymbolWmWpNameKurz() {
        return getSymbol(KeysystemEnum.WM_WP_NAME_KURZ);
    }

    public String getSymbolWmWpNameLang() {
        return getSymbol(KeysystemEnum.WM_WP_NAME_LANG);
    }

    public String getSymbolWmWpNameZusatz() {
        return getSymbol(KeysystemEnum.WM_WP_NAME_ZUSATZ);
    }

    @Override
    public String getSymbolNameSuffix() {
        return getSymbol(KeysystemEnum.NAMESUFFIX_QUOTE);
    }

    @Override
    public String getSymbolVwdfeedSecondary() {
        return getSymbol(KeysystemEnum.VWDFEED_SECONDARY);
    }

    @Override
    public String getSymbolMicSegment() {
        return getSymbol(KeysystemEnum.MIC);
    }

    @Override
    public String getSymbolMicOperating() {
        return getSymbol(KeysystemEnum.OPERATING_MIC);
    }

    @Override
    public String getSymbolInfrontId() {
        return getSymbol(KeysystemEnum.INFRONT_ID);
    }

    public void setMinimumQuotationSize(MinimumQuotationSize minimumQuotationSize) {
        this.minimumQuotationSize = (MinimumQuotationSizeDp2) minimumQuotationSize;
    }

    public void setQuotedef(int quotedef) {
        this.quotedef = quotedef;
    }

    public Entitlement getEntitlement() {
        return entitlement;
    }

    // needed for de-serialization, do not remove.
    public void setEntitlement(Entitlement entitlement) {
        this.entitlement = (EntitlementDp2) entitlement;
    }

    public void addEntitlement(KeysystemEnum ks, String[] value) {
        this.entitlement.setEntitlements(ks, value);
    }

    public String toString() {
        return getId() + ".qid/" + getSymbolVwdfeed();
    }

    public String toDebugString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (this == o) {
            return true;
        }

        final QuoteDp2 that = (QuoteDp2) o;

        if (firstHistoricPriceYyyymmdd != that.firstHistoricPriceYyyymmdd) return false;
        if (quotedef != that.quotedef) return false;
        if (!equalsById(this.currency, that.currency)) return false;
        if (entitlement != null ? !entitlement.equals(that.entitlement) : that.entitlement != null)
            return false;
        if (!equalsById(this.instrument, that.instrument)) return false;
        if (!equalsById(this.market, that.market)) return false;
        if (minimumQuotationSize != null ? !minimumQuotationSize.equals(that.minimumQuotationSize) : that.minimumQuotationSize != null)
            return false;

        return true;
    }
}
