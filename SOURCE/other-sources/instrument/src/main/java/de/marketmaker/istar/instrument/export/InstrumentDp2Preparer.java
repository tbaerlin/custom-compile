/*
 * InstrumentDp2Preparer.java
 *
 * Created on 16.04.2010 14:21:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.domainimpl.EntitlementProvider;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;

/**
 * An instrument Dp2 preparer processes an Dp2 instrument using the following artifacts if they are
 * configured properly:
 * <dl>
 * <dt>entitlement provider</dt><dd>sets the entitlement on each quote contained in an instrument
 * according to the quote's symbol: VWD feed, or market, or just an empty entitlement</dd>
 * <dt>instrument adaptor</dt><dd>adjusts an instrument, dependent on adaptor implementation</dd>
 * <dt>quote sorter</dt><dd>sets the order of each quote contained in an instrument for all kinds
 * of quote orders defined in {@link de.marketmaker.istar.domain.instrument.QuoteOrder}</dd>
 * </dl>
 * @author zzhao
 * @since 1.2
 */
public class InstrumentDp2Preparer {

    private EntitlementProvider entitlementProvider;

    private List<InstrumentAdaptor> adaptors = Collections.emptyList();

    private Map<QuoteOrder, QuoteSorter> quoteSorters = null;

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setAdaptors(List<InstrumentAdaptor> adaptors) {
        this.adaptors = adaptors;
    }

    public void setQuoteSorters(Map<String, QuoteSorter> map) {
        this.quoteSorters = new EnumMap<>(QuoteOrder.class);
        for (Map.Entry<String, QuoteSorter> entry : map.entrySet()) {
            this.quoteSorters.put(QuoteOrder.valueOf(entry.getKey()), entry.getValue());
        }
    }

    public void prepare(InstrumentDp2 instrumentDp2) {
        for (InstrumentAdaptor adaptor : this.adaptors) {
            adaptor.adapt(instrumentDp2);
        }
        if (!instrumentDp2.getQuotes().isEmpty()) {
            if (this.entitlementProvider != null) {
                setEntitlements(instrumentDp2);
            }
            if (this.quoteSorters != null) {
                setQuoteOrders(instrumentDp2);
            }
        }
    }

    private void setQuoteOrders(InstrumentDp2 instrument) {
        for (QuoteOrder order : quoteSorters.keySet()) {
            setQuoteOrder(instrument, order);
        }
    }

    private void setQuoteOrder(InstrumentDp2 instrument, QuoteOrder order) {
        final QuoteSorter sorter = this.quoteSorters.get(order);
        sorter.prepare(instrument);
        for (QuoteDp2 quote : instrument.getQuotesDp2()) {
            quote.setOrder(order, sorter.getOrder(quote));
        }
    }

    private void setEntitlements(InstrumentDp2 instrument) {
        for (QuoteDp2 quote : instrument.getQuotesDp2()) {
            final String vwdFeed = quote.getSymbol(KeysystemEnum.VWDFEED);
            final String[] entitlements = getEntitlements(quote, vwdFeed);
            if (entitlements != null) {
                quote.addEntitlement(KeysystemEnum.VWDFEED, entitlements);
            }
        }
    }

    private String[] getEntitlements(QuoteDp2 quote, String vwdFeed) {
        if (vwdFeed != null) {
            return getEntitlementVwdfeed(getEntitlements(vwdFeed));
        }
        else {
            final Market market = quote.getMarket();
            if (market != null) {
                return getEntitlementVwdfeed(getEntitlements(market));
            }
        }
        return null;
    }

    private String[] getEntitlementVwdfeed(Entitlement e) {
        return (e != null) ? e.getEntitlements(KeysystemEnum.VWDFEED) : null;
    }

    private Entitlement getEntitlements(Market market) {
        if (this.entitlementProvider != null && market != null) {
            return this.entitlementProvider.getEntitlement(market);
        }
        return null;
    }

    private Entitlement getEntitlements(String vwdFeed) {
        if (this.entitlementProvider != null && vwdFeed != null) {
            return this.entitlementProvider.getEntitlement(vwdFeed);
        }
        return null;
    }
}
