/*
 * CorporateActionFactor.java
 *
 * Created on 19.02.13 11:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.CorporateAction;
import de.marketmaker.istar.domain.instrument.CurrencyCrossrate;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.merger.provider.SymbolQuote;

import static de.marketmaker.istar.merger.Constants.MC;

/**
 * @author zzhao
 */
@ManagedResource
public class CorporateActionSupport extends EodPriceHistorySupport {

    private static final EnumSet<PriceType> ADJUSTABLE_PRICE_TYPES = EnumSet.of(
            PriceType.OPEN,
            PriceType.HIGH,
            PriceType.LOW,
            PriceType.CLOSE,
            PriceType.VOLUME,
            PriceType.CONTRACT,
            PriceType.KASSA
    );

    private Path adjustableVwdFieldsPath;

    private final AtomicReference<BitSet> adjustableVwdFieldsRef = new AtomicReference<>();

    private HistoricRatiosProvider historicRatiosProvider;

    private IsoCurrencyConversionProvider currencyConversionProvider;

    private InstrumentProvider instrumentProvider;

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setCurrencyConversionProvider(
            IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(this.historicRatiosProvider, "historic ratios provider required");
        Assert.notNull(this.currencyConversionProvider, "currency conversion provider required");
        Assert.notNull(this.instrumentProvider, "instrument provider required");
    }

    public void setAdjustableVwdFieldsPath(String adjustableVwdFieldsPath) throws IOException {
        this.adjustableVwdFieldsPath = Paths.get(adjustableVwdFieldsPath);
        final String error = reloadAdjustableVwdFields();
        if (null != error) {
            throw new IllegalStateException(error);
        }
    }

    @ManagedOperation
    public String reloadAdjustableVwdFields() throws IOException {
        final List<String> lines = FileUtils.readLines(this.adjustableVwdFieldsPath.toFile(), "UTF-8");
        final BitSet bitSet = new BitSet();
        final StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            final VwdFieldDescription.Field vwdField = VwdFieldDescription.getFieldByName(line.trim());
            if (null == vwdField) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(line.trim());
            }
            else {
                bitSet.set(vwdField.id());
            }
        }
        if (sb.length() != 0) {
            sb.insert(0, "Unknown VWD field: ");
            return sb.toString();
        }

        this.adjustableVwdFieldsRef.set(bitSet);
        return null;
    }

    public boolean isAdjustmentNecessary(List<Triple> list) {
        final BitSet bitSet = this.adjustableVwdFieldsRef.get();
        for (Triple triple : list) {
            final PriceType priceType = triple.getPriceType();
            if (PriceType.ADF == priceType) {
                if (bitSet.get(triple.getVwdField().id())) {
                    return true;
                }
            }
            else if (ADJUSTABLE_PRICE_TYPES.contains(priceType)) {
                return true;
            }
        }

        return false;
    }

    public Factor getFactor(HistoricRequest req, Interval interval, HistoricTimeseries closeHT) {
        final List<CorporateAction> cal;
        if (req.isWithSplit() || req.isWithDividend()) {
            cal = this.historicRatiosProvider.getCorporateActions(
                    SymbolQuote.create(req.getQuote()), interval, false);
        }
        else {
            cal = Collections.emptyList();
        }

        if (cal.size() > 1) {
            cal.sort(COMP);
        }

        return new Factor(req, interval, cal, closeHT);
    }

    /**
     * @param isoA
     * @param isoB
     * @return introday cross rate.
     */
    public BigDecimal getCurrentCrossRate(String isoA, String isoB) {
        if (null != isoA && null != isoB && !isoA.equalsIgnoreCase(isoB)) {
            final IsoCurrencyConversionProviderImpl.ConversionResult result =
                    this.currencyConversionProvider.getConversion(isoA, isoB);
            if (null != result) {
                return result.getFactor();
            }
        }
        else if (null == isoA || null == isoB) {
            if (logger.isDebugEnabled()) {
                logger.debug("<getCurrentCrossRate> cannot convert between {} and {}",
                        String.valueOf(isoA), String.valueOf(isoB));
            }
        }

        return BigDecimal.ONE;
    }

    /**
     * Comparator for corporate actions:
     * <ol>
     * <li>first order by occurring date</li>
     * <li>second order by factoring type, determined by enum ordinal, {@link CorporateAction.Type}</li>
     * </ol>
     */
    private static final Comparator<CorporateAction> COMP = (o1, o2) -> {
        final int res = o1.getDate().compareTo(o2.getDate());
        if (res != 0) {
            return res;
        }
        final CorporateAction.Type cat1 = o1.getType();
        final CorporateAction.Type cat2 = o2.getType();
        if (cat1 == cat2) {
            throw new IllegalStateException("identical corporate actions: " + o1 + ", " + o2);
        }
        else {
            return cat1.ordinal() - cat2.ordinal();
        }
    };

    private Map<String, String> fromConversionProvider(HistoricRequest req,
            List<CorporateAction> corpActions) {
        final TreeSet<String> set = new TreeSet<>();
        addCurrency(set, req.getCurrency());
        addCurrency(set, req.getQuote().getCurrency().getSymbolIso());
        for (CorporateAction ca : corpActions) {
            addCurrency(set, ca.getCurrency());
        }

        return this.currencyConversionProvider.getCrossRateSymbols(set);
    }

    private void addCurrency(Set<String> set, String curIso) {
        if (StringUtils.hasText(curIso)) {
            set.add(curIso);
        }
    }

    private Quote getQuote(String vwdFeed) {
        return this.instrumentProvider.identifyByVwdfeed(vwdFeed);
    }

    final class Factor {

        private HistoricRequest req;

        private final Interval interval;

        private final Map<String, String> curConMap;

        private final Map<String, CurrencyCrossrate> crossRatesInstruments;

        private final Map<String, HistoricTimeseries> crossRates;

        private final FactorSlots factorSlots;

        private final BitSet adjustableVwdFields =
                CorporateActionSupport.this.adjustableVwdFieldsRef.get();

        private Factor(HistoricRequest req, Interval iv, List<CorporateAction> cas,
                HistoricTimeseries ht) {
            this.req = req;
            this.interval = iv;
            this.curConMap = fromConversionProvider(req, cas);
            if (logger.isDebugEnabled()) {
                logger.debug("<Factor>: {}", this.curConMap);
            }
            this.crossRates = new HashMap<>();
            this.crossRatesInstruments = new HashMap<>();

            this.factorSlots = determineFactors(cas, ht).withBaseDate(req.getBaseDate());
        }

        private PriceFactors determineFactors(List<CorporateAction> cas, HistoricTimeseries ht) {
            // determine factor based on instrument type and price type | factor and dividends
            if (cas.isEmpty()) {
                return PriceFactors.EMPTY;
            }
            final PriceFactors ret = new PriceFactors(cas.size());
            for (final CorporateAction ca : cas) {
                final LocalDate exDay = ca.getDate().toLocalDate();
                switch (ca.getType()) {
                    case FACTOR:
                        if (this.req.isWithSplit()) {
                            ret.addFactor(ca.getType(), exDay, ca.getFactor().doubleValue());
                        }
                        break;
                    case DIVIDEND:
                        if (this.req.isWithDividend()) {
                            final InstrumentTypeEnum insType = getInstrumentType();
                            ret.addFactor(ca.getType(), exDay,
                                    CorporateActionUtil.getDividendFactor(
                                            getDividend(ca, exDay), insType,
                                            CorporateActionUtil.findExPrice(insType, exDay, ht)));
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("no support for: " + ca.getType());
                }
            }
            return ret;
        }

        private InstrumentTypeEnum getInstrumentType() {
            return this.req.getQuote().getInstrument().getInstrumentType();
        }

        private boolean shouldConvert(String reqCur, String isCur) {
            return reqCur != null &&  isCur != null && !reqCur.isEmpty() && !reqCur.equalsIgnoreCase(isCur);
        }

        private BigDecimal getDividend(CorporateAction ca, LocalDate exDate) {
            BigDecimal ret = ca.getFactor();
            final String quoteCur = getQuoteCurrency();
            if (shouldConvert(quoteCur, ca.getCurrency())) {
                final String cc = ca.getCurrency() + "-" + quoteCur;
                final CC cr = getConversion(cc, exDate);
                if (cr != null) {
                    ret = ca.getFactor().multiply(cr.getFactor(), MC);
                    if (logger.isDebugEnabled()) {
                        logger.debug("<getDividend> " + cc + ": " + cr.getFactor().toPlainString()
                                + "," + this.req.getQuote().getId()
                                + ",CA:" + HistoryUtil.DTF_DAY.print(ca.getDate())
                                + ",EX:" + HistoryUtil.DTF_DAY.print(exDate));
                    }
                }
            }

            return ret;
        }

        private CC getConversion(String cc, LocalDate exDate) {
            queryCrossRate(cc);
            return fromCrossRates(this.crossRates.get(cc), this.crossRatesInstruments.get(cc), exDate);
        }

        private void queryCrossRate(String cc) {
            if (!this.crossRates.containsKey(cc)) {
                final String vwdFeed = this.curConMap.get(cc);
                if (null == vwdFeed) {
                    // todo to and from USD??
                    logger.warn("<queryCrossRate> no cross rate symbol for: " + cc);
                    return;
                }
                final Quote quote = getQuote(vwdFeed);
                if (null == quote || null == quote.getInstrument()
                        || !(quote.getInstrument() instanceof CurrencyCrossrate)) {
                    throw new IllegalStateException("invalid vwd feed for currency cross rate: "
                            + cc + ", " + vwdFeed);
                }
                final CurrencyCrossrate curCro = (CurrencyCrossrate) quote.getInstrument();
                this.crossRatesInstruments.put(cc, curCro);
                this.crossRates.put(cc, getCrossRates(quote, this.interval));
            }
        }

        private CC fromCrossRates(HistoricTimeseries ht, CurrencyCrossrate cc, LocalDate exDate) {
            if (null == ht) {
                return null;
            }
            return new CC(new BigDecimal(ht.getValueAtOrBeforeOrAfter(exDate)),
                    getSourceToTargetFactor(cc));
        }

        public HistoricTimeseries adjust(HistoricTimeseries ht, Triple triple) {
            switch (triple.getPriceType()) {
                case VOLUME:
                    return this.factorSlots.inverse().applySplit(ht);
                case CONTRACT:
                    return this.factorSlots.applySplit(ht);
                case OPEN:
                case HIGH:
                case LOW:
                case CLOSE:
                case KASSA:
//                case ISSUEPRICE:
//                case REDEMPTIONPRICE:
                    return convertBasedOnActionAndCurrency(ht);
                case ADF:
                    if (this.adjustableVwdFields.get(triple.getVwdField().id())) {
                        return convertBasedOnActionAndCurrency(ht);
                    }
                default:
                    return ht.multiply(1);
            }
        }

        private HistoricTimeseries convertBasedOnActionAndCurrency(HistoricTimeseries ht) {
            final String reqCur = this.req.getCurrency();
            final String quoCur = getQuoteCurrency();
            if (!shouldConvert(reqCur, quoCur)) {
                return this.factorSlots.apply(ht, this.req.getFactor());
            }
            else {
                final String crossRateKey = quoCur + "-" + reqCur;
                ensureCrossRate(crossRateKey);
                final HistoricTimeseries ccs = this.crossRates.get(crossRateKey);
                return this.factorSlots.apply(ht, this.req.getFactor(), ccs);
            }
        }

        private String getQuoteCurrency() {
            return this.req.getQuote().getCurrency().getSymbolIso();
        }

        private void ensureCrossRate(String key) {
            queryCrossRate(key);
            if (!this.crossRates.containsKey(key) || this.crossRates.get(key).size() <= 0) {
                throw new IllegalStateException("no cross rate found for: " + key);
            }
        }
    }

    private BigDecimal getSourceToTargetFactor(CurrencyCrossrate cc) {
        final double factor = cc.getSourceToTargetFactor();
        return factor == 0d ? BigDecimal.ONE : new BigDecimal(factor);
    }

    private static final class CC {

        private final BigDecimal rate;

        private final BigDecimal sourceToTargetFactor;

        private BigDecimal factor;

        private CC(BigDecimal rate, BigDecimal sourceToTargetFactor) {
            this.rate = rate;
            this.sourceToTargetFactor = sourceToTargetFactor;
            this.factor = this.sourceToTargetFactor.multiply(this.rate, MC);
        }

        private BigDecimal getFactor() {
            return factor;
        }
    }
}
