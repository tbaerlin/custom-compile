/*
 * InstrumentDeserializer.java
 *
 * Created on 20.06.12 09:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.protobuf;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.marketmaker.istar.common.io.ByteBufferInputStream;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.DerivativeTypeEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.MmInstrumentclass;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;
import de.marketmaker.istar.domainimpl.instrument.BondDp2;
import de.marketmaker.istar.domainimpl.instrument.CertificateDp2;
import de.marketmaker.istar.domainimpl.instrument.CommodityDp2;
import de.marketmaker.istar.domainimpl.instrument.CurrencyCrossrateDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeWithStrikeDp2;
import de.marketmaker.istar.domainimpl.instrument.DetailedInstrumentTypeDp2;
import de.marketmaker.istar.domainimpl.instrument.FundDp2;
import de.marketmaker.istar.domainimpl.instrument.FutureDp2;
import de.marketmaker.istar.domainimpl.instrument.IndexDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.MacroEconomicDataDp2;
import de.marketmaker.istar.domainimpl.instrument.MinimumQuotationSizeDp2;
import de.marketmaker.istar.domainimpl.instrument.OptionDp2;
import de.marketmaker.istar.domainimpl.instrument.ParticipationCertificateDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.domainimpl.instrument.RateDp2;
import de.marketmaker.istar.domainimpl.instrument.RealEstateDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;
import de.marketmaker.istar.domainimpl.instrument.UnderlyingDp2;
import de.marketmaker.istar.domainimpl.instrument.WarrantDp2;

/**
 * @author oflege
 */
public class InstrumentDeserializer {

    private static final InstrumentTypeEnum[] INSTRUMENT_TYPE_ENUMS = InstrumentTypeEnum.values();

    private static final KeysystemEnum[] KEYSYSTEM_ENUMS = KeysystemEnum.values();

    private static final MmInstrumentclass[] INSTRUMENTCLASS_VALUES = MmInstrumentclass.values();

    private static final DerivativeTypeEnum[] DERIVATIVE_TYPE_ENUMS = DerivativeTypeEnum.values();

    private static final MinimumQuotationSize.Unit[] MIN_QS_UNIT_VALUES = MinimumQuotationSize.Unit.values();

    private final DomainContext dc;

    public InstrumentDeserializer(DomainContext dc) {
        this.dc = dc;
    }

    Instrument deserialize(byte[] bb) throws IOException {
        return deserialize(InstrumentProtos.Instrument.parseFrom(bb));
    }

    public Instrument deserialize(ByteBuffer bb) throws IOException {
        return deserialize(InstrumentProtos.Instrument.parseFrom(new ByteBufferInputStream(bb)));
    }

    private Instrument deserialize(InstrumentProtos.Instrument i) {
        final List<String> strings = decodeStrings(i);

        InstrumentTypeEnum typeEnum = INSTRUMENT_TYPE_ENUMS[i.getTypeOrd()];
        InstrumentDp2 result = createInstrument(i.getId(), typeEnum);
        if (i.hasAliases()) {
            result.setAliases(strings.get(i.getAliases()));
        }
        if (i.hasLei()) {
            result.setLei(strings.get(i.getLei()));
        }
        result.setCountry(dc.getCountry(i.getCountryId()));
        DetailedInstrumentTypeDp2 dit = new DetailedInstrumentTypeDp2();
        setSymbols(dit, i.getDetailedInstrumentTypeList(), strings);
        result.setDetailedInstrumentType(dit);
        if (i.getExpirationDate() != 0) {
            result.setExpirationDate(i.getExpirationDate());
        }
        result.setHomeExchange(dc.getMarket(i.getHomeMarketId()));
        if (i.hasInstrumentclassOrd()) {
            result.setMmInstrumentclass(INSTRUMENTCLASS_VALUES[i.getInstrumentclassOrd()]);
        }
        result.setName(strings.get(i.getName()));
        result.setSector(dc.getSector(i.getSectorId()));
        setSymbols(result, i.getSymbolsList(), strings);

        if (result instanceof StockDp2) {
            if (i.hasGeneralMeetingDate()) {
                ((StockDp2)result).setGeneralMeetingDate(i.getGeneralMeetingDate());
            }
        }
        else if (result instanceof CurrencyCrossrateDp2) {
            CurrencyCrossrateDp2 crossrate = (CurrencyCrossrateDp2) result;
            InstrumentProtos.Instrument.Crossrate cr = i.getCrossrate();
            if (cr.hasSourceCurrencyId()) {
                crossrate.setSourceCurrency(dc.getCurrency(cr.getSourceCurrencyId()));
            }
            if (cr.hasTargetCurrencyId()) {
                crossrate.setTargetCurrency(dc.getCurrency(cr.getTargetCurrencyId()));
            }
            if (cr.hasFactor()) {
                crossrate.setSourceToTargetFactor(Double.valueOf(cr.getFactor()));
            }
        }
        else if (result instanceof RateDp2) {
            RateDp2 rate = (RateDp2) result;
            InstrumentProtos.Instrument.Rate r = i.getRate();
            if (r.hasSourceCurrencyId()) {
                rate.setSourceCurrency(dc.getCurrency(r.getSourceCurrencyId()));
            }
            if (r.hasTargetCurrencyId()) {
                rate.setTargetCurrency(dc.getCurrency(r.getTargetCurrencyId()));
            }
            if (r.hasFactor()) {
                rate.setSourceToTargetFactor(Double.valueOf(r.getFactor()));
            }
        }
        else if (result instanceof DerivativeDp2) {
            DerivativeDp2 derivativeDp2 = (DerivativeDp2) result;
            InstrumentProtos.Instrument.Derivative d = i.getDervative();
            derivativeDp2.setUnderlyingId(d.getUnderlyingId());
            if (d.hasSubscriptionRatio()) {
                derivativeDp2.setSubscriptionRatio(toBigDecimal(d.getSubscriptionRatio()));
            }
            if (result instanceof DerivativeWithStrikeDp2) {
                DerivativeWithStrikeDp2 dws = (DerivativeWithStrikeDp2) result;
                if (d.hasStrike()) {
                    dws.setStrike(toBigDecimal(d.getStrike()));
                }
                if (d.hasStrikeCurrencyId()) {
                    dws.setStrikeCurrency(dc.getCurrency(d.getStrikeCurrencyId()));
                }
                if (d.hasTypeOrd()) {
                    dws.setType(DERIVATIVE_TYPE_ENUMS[d.getTypeOrd()]);
                }
            }
            if (result instanceof FutureDp2 && d.hasFuture()) {
                FutureDp2 fut = (FutureDp2) result;
                InstrumentProtos.Instrument.Future f = d.getFuture();
                fut.setUnderlyingProductId(f.getUnderlyingProductId());
                if (f.hasContractCurrencyId()) {
                    fut.setContractCurrency(dc.getCurrency(f.getContractCurrencyId()));
                }
                if (f.hasContractValue()) {
                    fut.setContractValue(toBigDecimal(f.getContractValue()));
                }
                if (f.hasTickCurrencyId()) {
                    fut.setTickCurrency(dc.getCurrency(f.getTickCurrencyId()));
                }
                if (f.hasTickSize()) {
                    fut.setTickSize(toBigDecimal(f.getTickSize()));
                }
                if (f.hasTickValue()) {
                    fut.setTickValue(toBigDecimal(f.getTickValue()));
                }
            }
        }


        MinimumQuotationSize mqs = null;
        for (InstrumentProtos.Quote q : i.getQuotesList()) {
            QuoteDp2 quoteDp2 = new QuoteDp2(q.getId());
            quoteDp2.setCurrency(dc.getCurrency(q.getCurrencyId()));
            quoteDp2.setMarket(dc.getMarket(q.getMarketId()));
            quoteDp2.setFirstHistoricPriceYyyymmdd(q.getFirstHistoricPriceYyyymmdd());
            quoteDp2.setOrders(q.getOrders());
            quoteDp2.setQuotedef(q.getQuotedef());
            for (int k = 0; k < q.getFlagsCount(); k++) {
                quoteDp2.setFlags(k, q.getFlags(k));
            }
            if (q.hasMinimumQuotationSize()) {
                mqs = deserialize(q.getMinimumQuotationSize());
            }
            quoteDp2.setMinimumQuotationSize(mqs);
            setSymbols(quoteDp2, q.getSymbolsList(), strings);
            setEntitlements(quoteDp2, q.getEntitlementsList(), strings);

            result.addQuote(quoteDp2);
        }

        return result;
    }

    private List<String> decodeStrings(InstrumentProtos.Instrument i) {
        final StringBuilder sb = new StringBuilder(32);
        final List<String> result = new ArrayList<>(i.getSuffixesCount());
        for (int k = 0; k < i.getSuffixesCount(); k++) {
            final int prefixLength = i.getPrefixLengths(k);
            final String suffix = i.getSuffixes(k);
            if (prefixLength == 0) {
                result.add(suffix);
            }
            else {
                sb.setLength(0);
                result.add(sb.append(result.get(k - 1), 0, prefixLength).append(suffix).toString());
            }
        }
        return result;
    }

    private MinimumQuotationSize deserialize(InstrumentProtos.MinimumQuotationSize mqs) {
        MinimumQuotationSizeDp2 result = new MinimumQuotationSizeDp2();
        result.setUnit(MIN_QS_UNIT_VALUES[mqs.getUnitOrd()]);
        if (mqs.hasNumber()) {
            result.setNumber(toBigDecimal(mqs.getNumber()));
        }
        result.setCurrency(dc.getCurrency(mqs.getCurrencyId()));
        return result;
    }

    private BigDecimal toBigDecimal(String s) {
        if ("0".equals(s)) return BigDecimal.ZERO;
        if ("1".equals(s)) return BigDecimal.ONE;
        return new BigDecimal(s);
    }

    private void setSymbols(ItemWithSymbolsDp2 item, List<InstrumentProtos.SymbolRef> symbols,
            List<String> strings) {
        for (InstrumentProtos.SymbolRef ref : symbols) {
            if (ref.getKeysystemOrd() < KEYSYSTEM_ENUMS.length) {
                item.setSymbol(KEYSYSTEM_ENUMS[ref.getKeysystemOrd()], strings.get(ref.getValueIdx()));
            }
        }
    }

    private void setEntitlements(QuoteDp2 item, List<InstrumentProtos.Entitlement> ents,
            List<String> strings) {
        for (InstrumentProtos.Entitlement e : ents) {
            if (e.getKeysystemOrd() < KEYSYSTEM_ENUMS.length) {
                String[] value = new String[e.getValueIdxCount()];
                for (int k = 0; k < value.length; k++) {
                    value[k] = strings.get(e.getValueIdx(k));
                }
                item.addEntitlement(KEYSYSTEM_ENUMS[e.getKeysystemOrd()], value);
            }
        }
    }

    private InstrumentDp2 createInstrument(long id, InstrumentTypeEnum type) {
        switch (type) {
            case BND:
                return new BondDp2(id);
            case CER:
                return new CertificateDp2(id);
            case CUR:
                return new CurrencyCrossrateDp2(id);
            case FND:
                return new FundDp2(id);
            case FUT:
                return new FutureDp2(id);
            case GNS:
                return new ParticipationCertificateDp2(id);
            case IMO:
                return new RealEstateDp2(id);
            case IND:
                return new IndexDp2(id);
            case MER:
                return new CommodityDp2(id);
            case MK:
                return new MacroEconomicDataDp2(id);
            case OPT:
                return new OptionDp2(id);
            case STK:
                return new StockDp2(id);
            case UND:
                return new UnderlyingDp2(id);
            case WNT:
                return new WarrantDp2(id);
            case ZNS:
                return new RateDp2(id);
            default:
                throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }
}
