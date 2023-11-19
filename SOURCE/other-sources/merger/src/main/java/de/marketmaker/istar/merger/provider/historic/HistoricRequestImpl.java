/*
 * HistoricTimeseriesRequest.java
 *
 * Created on 31.08.2006 14:49:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricRequestImpl implements HistoricRequest {
    private final Quote quote;

    private final LocalDate from;

    private final LocalDate to;

    private BigDecimal factor;

    private final List<HistoricTerm> historicTerms = new ArrayList<>();

    private String currency;

    private boolean withSplit;

    private boolean withDividend;

    private LocalDate baseDate;

    private boolean alignStartWithAggregationPeriod = false;

    private boolean alignEndWithAggregationPeriod = false;

    private Period aggregationPeriod;

    private PriceRecord priceRecord;

    private TickImpl.Type specialBaseType;

    private LocalDate corporateActionRefDate;

    public HistoricRequestImpl(Quote quote, LocalDate from, LocalDate to) {
        this.quote = quote;
        this.from = from;
        this.to = to;
        /**
         * PM delivers price always in GBP or ZAR, MDP stores prices in GBX or ZAC (no conversion,
         * just as they arrived from feed). Before our own EoD-Implementation we obtain prices from
         * PM and those prices are always in GBP or ZAR, therefore we apply factor 100 if the quote
         * currency is GBX or ZAC.
         *
         * Now we obtain prices from MDP directly, no factor required. For now just return default
         * factor 1. If all parties agree, we can remove this factor field completely from request.
         */
        this.factor = HistoricTimeseriesUtils.DEFAULT_FACTOR;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isWithSplit() {
        return withSplit;
    }

    public boolean isWithDividend() {
        return withDividend;
    }

    public LocalDate getBaseDate() {
        return baseDate;
    }

    @Override
    public LocalDate getCorporateActionReferenceDate() {
        return this.corporateActionRefDate;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void addHistoricTerm(HistoricTerm term) {
        this.historicTerms.add(term);
    }

    public List<HistoricTerm> getHistoricTerms() {
        return historicTerms;
    }

    public HistoricRequestImpl withSplit(boolean kap) {
        this.withSplit = kap;
        return this;
    }

    public HistoricRequestImpl withDividend(boolean div) {
        this.withDividend = div;
        return this;
    }

    public HistoricRequestImpl withBaseDate(LocalDate baseDate) {
        this.baseDate = baseDate;
        return this;
    }

    public HistoricRequestImpl withCorporateActionReferenceDate(LocalDate refDate) {
        this.corporateActionRefDate = refDate;
        return this;
    }

    public HistoricRequestImpl withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    @Deprecated
    public HistoricRequestImpl withSpecialBaseType(TickImpl.Type type) {
        this.specialBaseType = type;
        return this;
    }

    public HistoricRequestImpl withAggregationPeriod(Period p) {
        this.aggregationPeriod = p;
        return this;
    }

    public HistoricRequestImpl withAlignedStart(boolean align) {
        this.alignStartWithAggregationPeriod = align;
        return this;
    }

    public HistoricRequestImpl withAlignedEnd(boolean align) {
        this.alignEndWithAggregationPeriod = align;
        return this;
    }

    public HistoricRequestImpl withPriceRecord(PriceRecord priceRecord) {
        this.priceRecord = priceRecord;
        return this;
    }

    public Period getAggregationPeriod() {
        return this.aggregationPeriod;
    }

    public PriceRecord getPriceRecord() {
        return priceRecord;
    }

    @Override
    public HistoricRequest withFactor(BigDecimal factor) {
        this.factor = factor;
        return this;
    }

    @Override
    public boolean isAlignStartWithAggregationPeriod() {
        return this.alignStartWithAggregationPeriod;
    }

    @Override
    public boolean isAlignEndWithAggregationPeriod() {
        return this.alignEndWithAggregationPeriod;
    }

    @Override
    public LocalDate getFrom() {
        return this.from;
    }

    @Override
    public LocalDate getTo() {
        return this.to;
    }

    @Override
    public Quote getQuote() {
        return this.quote;
    }

    @Deprecated
    public void addBviPerformance(LocalDate referencedate) {
        HistoricTimeseriesUtils.addBviPerformance(this, referencedate);
    }

    @Deprecated
    public void addOpen(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.FIRST);
    }

    @Deprecated
    public void addHigh(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.MAX);
    }

    @Deprecated
    public void addLow(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.MIN);
    }

    @Deprecated
    public void addClose(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.LAST);
    }

    @Deprecated
    public boolean isYieldBased() {
        return this.specialBaseType == TickImpl.Type.YIELD;
    }

    @Deprecated
    public boolean isSettlementBased() {
        return this.specialBaseType == TickImpl.Type.SETTLEMENT;
    }

    private void addOhlc(LocalDate basisdatum, Aggregation aggregation) {
        if (isFund()) {
            addHistoricTerm(HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Ruecknahme, aggregation));
        }
        else if (isYieldBased()) {
            addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.OPENINTEREST, aggregation));
        }
        else if (isSettlementBased()) {
            addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.KASSA, aggregation));
        }
        else {
            switch (aggregation) {
                case FIRST:
                    addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.OPEN));
                    break;
                case MAX:
                    addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.HIGH));
                    break;
                case MIN:
                    addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.LOW));
                    break;
                case LAST:
                    addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.CLOSE));
                    break;
                default:
                    throw new IllegalArgumentException("no support for: " + aggregation);
            }
        }
    }

    private boolean isFund() {
        return InstrumentUtil.isVwdFund(quote);
    }

    @Deprecated
    public void addMmTalk(String mmtalk) {
        addHistoricTerm(HistoricTerm.fromMmTalk(mmtalk));
    }

    @Deprecated
    public void addVolume(LocalDate basisdatum) {
        addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.VOLUME));
    }

    @Deprecated
    public void addKassa(LocalDate basisdatum, Aggregation aggregation) {
        addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.KASSA));
    }

    @Deprecated
    public void addOpenInterest(LocalDate basisdatum) {
        addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.OPENINTEREST));
    }

    @Deprecated
    public void addKontrakt(LocalDate basisdatum) {
        addHistoricTerm(HistoricTerm.fromFunctionalPrice(PriceType.CONTRACT));
    }

}
