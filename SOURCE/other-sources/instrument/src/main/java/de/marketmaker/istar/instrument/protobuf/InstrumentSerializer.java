/*
 * InstrumentSerializer.java
 *
 * Created on 19.06.12 17:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.protobuf;

import de.marketmaker.istar.domainimpl.instrument.RateDp2;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domainimpl.EntitlementDp2;
import de.marketmaker.istar.domainimpl.instrument.CurrencyCrossrateDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeWithStrikeDp2;
import de.marketmaker.istar.domainimpl.instrument.DetailedInstrumentTypeDp2;
import de.marketmaker.istar.domainimpl.instrument.FutureDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;

/**
 * Performs protobuf serialization of {@link InstrumentDp2} objects. Takes care to ensure that
 * objects with equal content will have the equal byte[] representations, which usually means that
 * data obtained from maps has to be sorted before it can be encoded.
 * @author oflege
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class InstrumentSerializer {
    private List<String> strings = new ArrayList<>();

    private Set<String> uniqueStrings = new HashSet<>();

    private MinimumQuotationSize lastMqs;

    private int si(String s) {
        return Collections.binarySearch(this.strings, s);
    }

    private void clear() {
        this.uniqueStrings.clear();
        this.strings.clear();
        this.lastMqs = null;
    }

    private void collectStrings(InstrumentDp2 i) {
        add(i.getName());
        add(i.getAliases());
        add(i.getLei());
        for (Map.Entry<KeysystemEnum, String> e : i.getSymbols()) {
            add(e.getValue());
        }
        DetailedInstrumentTypeDp2 dit = (DetailedInstrumentTypeDp2) i.getDetailedInstrumentType();
        for (Map.Entry<KeysystemEnum, String> e : dit.getSymbols()) {
            add(e.getValue());
        }
        for (QuoteDp2 q : i.getQuotesDp2()) {
            for (Map.Entry<KeysystemEnum, String> e : q.getSymbols()) {
                if (e.getKey() != KeysystemEnum.VWDCODE && e.getKey() != KeysystemEnum.VWDSYMBOL) {
                    add(e.getValue());
                }
            }
            for (Map.Entry<KeysystemEnum, String[]> entry :
                    ((EntitlementDp2) q.getEntitlement()).getEntitlements()) {
                for (int k = 0; k < entry.getValue().length; k++) {
                    add(entry.getValue()[k]);
                }
            }
        }
        this.strings.addAll(this.uniqueStrings);
        this.strings.sort(null);
    }

    private void add(String s) {
        if (s != null) {
            this.uniqueStrings.add(s);
        }
    }

    public byte[] serialize(InstrumentDp2 i) {
        clear();
        collectStrings(i);

        InstrumentProtos.Instrument.Builder ib = InstrumentProtos.Instrument.newBuilder();
        ib.setId(i.getId());
        if (i.getAliases() != null && !i.getAliases().equals(i.getName())) {
            ib.setAliases(si(i.getAliases()));
        }
        if (i.getLei() != null) {
            ib.setLei(si(i.getLei()));
        }
        ib.setCountryId(i.getCountry().getId());
        ib.setHomeMarketId(i.getHomeExchange().getId());
        ib.setName(si(i.getName()));
        if (i.getMmInstrumentclass() != null) {
            ib.setInstrumentclassOrd(i.getMmInstrumentclass().ordinal());
        }
        ib.setTypeOrd(i.getInstrumentType().ordinal());
        long sid = i.getSector().getId();
        if (sid != 0) {
            ib.setSectorId(sid);
        }

        if (i.getExpirationDate() != Integer.MIN_VALUE) {
            ib.setExpirationDate(i.getExpirationDate());
        }

        for (Map.Entry<KeysystemEnum, String> entry : i.getSymbols()) {
            InstrumentProtos.SymbolRef.Builder sb = InstrumentProtos.SymbolRef.newBuilder();
            sb.setKeysystemOrd(entry.getKey().ordinal());
            sb.setValueIdx(si(entry.getValue()));
            ib.addSymbols(sb);
        }

        DetailedInstrumentTypeDp2 dit = (DetailedInstrumentTypeDp2) i.getDetailedInstrumentType();
        for (Map.Entry<KeysystemEnum, String> entry : dit.getSymbols()) {
            InstrumentProtos.SymbolRef.Builder sb = InstrumentProtos.SymbolRef.newBuilder();
            sb.setKeysystemOrd(entry.getKey().ordinal());
            sb.setValueIdx(si(entry.getValue()));
            ib.addDetailedInstrumentType(sb);
        }

        if (i instanceof StockDp2) {
            StockDp2 stockDp2 = (StockDp2) i;
            if (stockDp2.getGeneralMeetingDate() != 0) {
                ib.setGeneralMeetingDate(stockDp2.getGeneralMeetingDate());
            }

        }
        else if (i instanceof CurrencyCrossrateDp2) {
            CurrencyCrossrateDp2 c = (CurrencyCrossrateDp2) i;
            InstrumentProtos.Instrument.Crossrate.Builder cb
                    = InstrumentProtos.Instrument.Crossrate.newBuilder();
            if (c.getSourceCurrency() != null) {
                cb.setSourceCurrencyId(c.getSourceCurrency().getId());
            }
            if (c.getTargetCurrency() != null) {
                cb.setTargetCurrencyId(c.getTargetCurrency().getId());
            }
            if (c.getSourceToTargetFactor() != 0d) {
                cb.setFactor(Double.toString(c.getSourceToTargetFactor()));
            }
            ib.setCrossrate(cb);
        }
        else if (i instanceof RateDp2) {
            RateDp2 c = (RateDp2) i;
            InstrumentProtos.Instrument.Rate.Builder rb = InstrumentProtos.Instrument.Rate.newBuilder();
            if (c.getSourceCurrency() != null) {
                rb.setSourceCurrencyId(c.getSourceCurrency().getId());
            }
            if (c.getTargetCurrency() != null) {
                rb.setTargetCurrencyId(c.getTargetCurrency().getId());
            }
            if (c.getSourceToTargetFactor() != 0d) {
                rb.setFactor(Double.toString(c.getSourceToTargetFactor()));
            }
            ib.setRate(rb);
        }
        else if (i instanceof DerivativeDp2) {
            DerivativeDp2 d = (DerivativeDp2) i;
            InstrumentProtos.Instrument.Derivative.Builder db
                    = InstrumentProtos.Instrument.Derivative.newBuilder();
            db.setUnderlyingId(d.getUnderlyingId());
            if (d.getSubscriptionRatio() != null) {
                db.setSubscriptionRatio(format(d.getSubscriptionRatio()));
            }
            if (d instanceof DerivativeWithStrikeDp2) {
                DerivativeWithStrikeDp2 dws = (DerivativeWithStrikeDp2) d;
                if (dws.getStrike() != null) {
                    db.setStrike(dws.getStrike().toPlainString());
                }
                if (dws.getStrikeCurrency() != null && dws.getStrikeCurrency().getId() != 0) {
                    db.setStrikeCurrencyId(dws.getStrikeCurrency().getId());
                }
                if (dws.getType() != null) {
                    db.setTypeOrd(dws.getType().ordinal());
                }
            }
            if (d instanceof FutureDp2) {
                FutureDp2 fut = (FutureDp2) d;
                InstrumentProtos.Instrument.Future.Builder fb
                        = InstrumentProtos.Instrument.Future.newBuilder();
                fb.setUnderlyingProductId(fut.getUnderlyingProductId());
                if (fut.getContractCurrency() != null) {
                    fb.setContractCurrencyId(fut.getContractCurrency().getId());
                }
                if (fut.getContractValue() != null) {
                    fb.setContractValue(format(fut.getContractValue()));
                }
                if (fut.getTickCurrency() != null) {
                    fb.setTickCurrencyId(fut.getTickCurrency().getId());
                }
                if (fut.getTickSize() != null) {
                    fb.setTickSize(format(fut.getTickSize()));
                }
                if (fut.getTickValue() != null) {
                    fb.setTickValue(format(fut.getTickValue()));
                }
                db.setFuture(fb.build());
            }
            ib.setDervative(db);
        }


        final QuoteDp2[] quotes = i.getQuotesDp2();
        Arrays.sort(quotes, QuoteDp2.BY_ID); // important to be able to detect changes
        for (QuoteDp2 q : quotes) {
            InstrumentProtos.Quote.Builder qb = InstrumentProtos.Quote.newBuilder();
            qb.setId(q.getId());
            qb.setMarketId(q.getMarket().getId());
            qb.setCurrencyId(q.getCurrency().getId());
            qb.setFirstHistoricPriceYyyymmdd(q.getFirstHistoricPriceYyyymmdd());
            qb.setOrders(q.getOrders());
            qb.setQuotedef(q.getQuotedef());
            for (long flag : q.getFlags()) {
                qb.addFlags(flag);
            }

            MinimumQuotationSize mqs = q.getMinimumQuotationSize();
            if (!mqs.equals(this.lastMqs)) {
                qb.setMinimumQuotationSize(createMinQuotSizeBuilder(mqs));
                this.lastMqs = mqs;
            }

            for (Map.Entry<KeysystemEnum, String> entry : q.getSymbols()) {
                if (entry.getKey() == KeysystemEnum.VWDCODE || entry.getKey() == KeysystemEnum.VWDSYMBOL) {
                    // these will be derived from KeysystemEnum.VWDFEED, see QuoteDp2#setSymbol
                    continue;
                }
                InstrumentProtos.SymbolRef.Builder sb = InstrumentProtos.SymbolRef.newBuilder();
                sb.setKeysystemOrd(entry.getKey().ordinal());
                sb.setValueIdx(si(entry.getValue()));
                qb.addSymbols(sb);
            }
            for (Map.Entry<KeysystemEnum, String[]> entry :
                    ((EntitlementDp2) q.getEntitlement()).getEntitlements()) {
                InstrumentProtos.Entitlement.Builder eb = InstrumentProtos.Entitlement.newBuilder();
                eb.setKeysystemOrd(entry.getKey().ordinal());
                final String[] entitlements = entry.getValue();
                Arrays.sort(entitlements); // important to be able to detect changes
                for (String value : entitlements) {
                    eb.addValueIdx(si(value));
                }
                qb.addEntitlements(eb);
            }

            ib.addQuotes(qb);
        }

        String previous = "";
        for (String string : this.strings) {
            final int length = commonPrefixLength(previous, string);
            ib.addPrefixLengths(length);
            ib.addSuffixes(string.substring(length));
            previous = string;
        }

        return ib.build().toByteArray();
    }

    private int commonPrefixLength(String s1, String s2) {
        for (int i = 0, n = Math.min(s1.length(), s2.length()); i < n; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        return s1.length();
    }

    private InstrumentProtos.MinimumQuotationSize.Builder createMinQuotSizeBuilder(
            MinimumQuotationSize mqs) {
        InstrumentProtos.MinimumQuotationSize.Builder mqsb
                = InstrumentProtos.MinimumQuotationSize.newBuilder();
        if (mqs.getCurrency() != null && mqs.getCurrency().getId() > 0) {
            mqsb.setCurrencyId(mqs.getCurrency().getId());
        }
        if (mqs.getUnit().ordinal() != 1) {
            mqsb.setUnitOrd(mqs.getUnit().ordinal());
        }

        if (mqs.getNumber() instanceof BigDecimal) {
            mqsb.setNumber(format((BigDecimal) mqs.getNumber()));
        }
        return mqsb;
    }

    private String format(BigDecimal bd) {
        if (BigDecimal.ZERO.compareTo(bd) == 0) {
            return "0";
        }
        if (BigDecimal.ONE.compareTo(bd) == 0) {
            return "1";
        }
        return bd.toPlainString();
    }
}
