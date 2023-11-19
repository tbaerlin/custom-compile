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
public class HistoricTimeseriesRequest implements HistoricRequest {
    private final Quote quote;

    private TickImpl.Type specialBaseType;

    private String currency;

    private Boolean kapitalmassnahmen;

    private Boolean dividenden;

    private boolean alignStartWithAggregationPeriod = false;

    private boolean alignEndWithAggregationPeriod = false;

    private Period aggregationPeriod;

    private final LocalDate from;

    private final LocalDate to;

    private BigDecimal factor;

    private final List<Formula> formulas = new ArrayList<>();

    private PriceRecord priceRecord;

    private boolean singleDayData;

    public String getCurrency() {
        return currency;
    }

    public boolean isWithSingleDayData() {
        return singleDayData;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public interface Formula {
        String toMmTalk();

        Aggregation getAggregation();
    }

    private static class AdfField implements Formula {

        private final VwdFieldDescription.Field vwdField;

        private AdfField(VwdFieldDescription.Field vwdField) {
            this.vwdField = vwdField;
        }

        @Override
        public String toMmTalk() {
            return "ADF_" + this.vwdField.id();
        }

        @Override
        public Aggregation getAggregation() {
            return null;
        }
    }

    private static class MmTalkFormula implements Formula {
        private final String mmtalk;

        public MmTalkFormula(String mmtalk) {
            this.mmtalk = mmtalk;
        }

        public String toMmTalk() {
            return this.mmtalk;
        }

        public Aggregation getAggregation() {
            return null;
        }
    }

    private class BasicTimeseries implements Formula {
        private final String name;

        private final LocalDate basisdatum;

        private boolean withWaehrung = true;

        private boolean withFactor = true;

        private final Aggregation aggregation;

        private BasicTimeseries(String name, LocalDate basisdatum, Aggregation aggregation) {
            this.name = name;
            this.basisdatum = basisdatum;
            this.aggregation = aggregation;
        }

        BasicTimeseries withoutWaehrung() {
            this.withWaehrung = false;
            return this;
        }

        BasicTimeseries withoutFactor() {
            this.withFactor = false;
            return this;
        }

        public Aggregation getAggregation() {
            return aggregation;
        }

        public String toMmTalk() {
            // CAREFUL: de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProviderImpl.getSingleDayData() uses
            // string replacement in formula constructed below

            final StringBuilder sb = new StringBuilder(30).append(this.name);
            sb.append("[");
            if (this.withWaehrung) {
                sb.append(mmtalk(currency)).append(';');
            }
            sb.append(mmtalk(kapitalmassnahmen)).append(';')
                    .append(mmtalk(dividenden)).append(';')
                    .append("_;") // interval, default is 1
                    .append(mmtalk(this.basisdatum)).append("]");
            if (this.withFactor && factor != null) {
                sb.append("*").append(mmtalk(factor.doubleValue()));
            }

            return sb.toString();
        }
    }

    static String mmtalk(String s) {
        return (s != null) ? ("\"" + s + "\"") : "_";
    }

    static String mmtalk(Boolean b) {
        return (b != null) ? b.toString() : "_";
    }

    static String mmtalk(Number n) {
        return ((n != null) ? n.toString() : "_").replace('.', ',');
    }

    static String mmtalk(LocalDate ymd) {
        if (ymd == null) {
            return "_";
        }
        return "\"" + ymd.getDayOfMonth() + "." + ymd.getMonthOfYear() + "." + ymd.getYear() + "\"";
    }

    public HistoricTimeseriesRequest(Quote quote, LocalDate from, LocalDate to) {
        this.quote = quote;
        this.from = from;
        this.to = to;
        this.factor = BigDecimal.ONE;
        this.currency = quote.getCurrency().getSymbolIso();
    }

    public HistoricTimeseriesRequest withVwdField(VwdFieldDescription.Field field) {
        if (null != this.specialBaseType && TickImpl.Type.ADDITIONAL_FIELDS != this.specialBaseType) {
            throw new IllegalArgumentException("cannot take vwd field in case of: " + this.specialBaseType);
        }
        if (null == this.specialBaseType) {
            this.specialBaseType = TickImpl.Type.ADDITIONAL_FIELDS;
        }
        this.formulas.add(new AdfField(field));
        return this;
    }

    /**
     * Uses a heuristic to figure out whether this request's quote is yield based
     * and adapts this request accordingly
     * @return this
     */
    public HistoricTimeseriesRequest withYieldBasedFromQuote() {
        final InstrumentTypeEnum t = this.quote.getInstrument().getInstrumentType();
        if (((t == InstrumentTypeEnum.IND || t == InstrumentTypeEnum.MK)
                && "XXZ".equals(this.quote.getCurrency().getSymbolIso()))
                && !"VWL".equals(this.quote.getSymbolVwdfeedMarket())) {
            return withSpecialBaseType(TickImpl.Type.YIELD);
        }
        return this;
    }

    public HistoricTimeseriesRequest withSpecialBaseType(TickImpl.Type specialBaseType) {
        this.specialBaseType = specialBaseType;
        return this;
    }

    public HistoricTimeseriesRequest withSplit(boolean kap) {
        this.kapitalmassnahmen = kap;
        return this;
    }

    public HistoricTimeseriesRequest withDividend(boolean div) {
        this.dividenden = div;
        return this;
    }

    public HistoricTimeseriesRequest withSingleDayData(boolean singleDayData) {
        this.singleDayData = singleDayData;
        return this;
    }

    @Override
    public HistoricRequest withFactor(BigDecimal factor) {
        this.factor = factor;
        return this;
    }

    public HistoricTimeseriesRequest withCurrency(String currency) {
        if (currency != null) {
            this.currency = currency;
        }

        return this;
    }

    public HistoricTimeseriesRequest withAggregationPeriod(Period p) {
        this.aggregationPeriod = p;
        return this;
    }

    public HistoricTimeseriesRequest withAlignedStart(boolean align) {
        this.alignStartWithAggregationPeriod = align;
        return this;
    }

    public HistoricTimeseriesRequest withAlignedEnd(boolean align) {
        this.alignEndWithAggregationPeriod = align;
        return this;
    }

    public HistoricTimeseriesRequest withPriceRecord(PriceRecord priceRecord) {
        this.priceRecord = priceRecord;
        return this;
    }

    public boolean isYieldBased() {
        return this.specialBaseType == TickImpl.Type.YIELD;
    }

    public boolean isSettlementBased() {
        return this.specialBaseType == TickImpl.Type.SETTLEMENT;
    }

    public Period getAggregationPeriod() {
        return this.aggregationPeriod;
    }

    public String[] getFormulas() {
        assertValid();
        final String[] result = new String[this.formulas.size()];
        for (int i = 0; i < this.formulas.size(); i++) {
            final Formula formula = this.formulas.get(i);
            result[i] = formula.toMmTalk();
        }
        return result;
    }

    public boolean onlyBasicTimeseriesFormulas() {
        for (Formula formula : formulas) {
            if (!(formula instanceof BasicTimeseries)) {
                return false;
            }
        }
        return true;
    }

    private void assertValid() {
        if (TickImpl.Type.ADDITIONAL_FIELDS == this.specialBaseType) {
            for (Formula formula : formulas) {
                if (!(formula instanceof AdfField)) {
                    throw new IllegalStateException("only vwd adf field allowed for type: " + this.specialBaseType);
                }
            }
            if (null != this.aggregationPeriod) {
                throw new IllegalStateException("no support for aggregation on vwd adf field");
            }
        }
        else {
            for (Formula formula : formulas) {
                if (formula instanceof AdfField) {
                    throw new IllegalStateException("vwd adf price query not allowed for type:" + this.specialBaseType);
                }
            }
        }
    }

    public PriceRecord getPriceRecord() {
        return priceRecord;
    }

    public int getSize() {
        return this.formulas.size();
    }

    @Override
    public boolean isAlignStartWithAggregationPeriod() {
        return this.alignStartWithAggregationPeriod;
    }

    @Override
    public boolean isAlignEndWithAggregationPeriod() {
        return this.alignEndWithAggregationPeriod;
    }

    public Aggregation getAggregation(int i) {
        return this.formulas.get(i).getAggregation();
    }

    public LocalDate getFrom() {
        return this.from;
    }

    public Quote getQuote() {
        return this.quote;
    }

    @Override
    public boolean isWithSplit() {
        return null == kapitalmassnahmen ? false : kapitalmassnahmen;
    }

    @Override
    public boolean isWithDividend() {
        return null == dividenden ? false : dividenden;
    }

    @Override
    public LocalDate getBaseDate() {
        return null; // no support for explicit base date
    }

    @Override
    public LocalDate getCorporateActionReferenceDate() {
        return null; // no support for explicit reference date for corporate actions
    }

    public LocalDate getTo() {
        return this.to;
    }

    public void addOpen(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.FIRST);
    }

    public void addHigh(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.MAX);
    }

    public void addLow(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.MIN);
    }

    public void addClose(LocalDate basisdatum) {
        addOhlc(basisdatum, Aggregation.LAST);
    }

    public void addFundRepurchaingPrice(LocalDate basisdatum) {
        this.formulas.add(new BasicTimeseries("Rücknahme", basisdatum, Aggregation.LAST));
    }

    public void addFundIssuePrice(LocalDate basisdatum) {
        this.formulas.add(new BasicTimeseries("Ausgabe", basisdatum, Aggregation.LAST));
    }

    private void addOhlc(LocalDate basisdatum, Aggregation aggregation) {
        if (isFund()) {
            this.formulas.add(new BasicTimeseries("Rücknahme", basisdatum, aggregation));
        }
        else if (isYieldBased()) {
            addOpenInterest(basisdatum, aggregation);
        }
        else if (isSettlementBased()) {
            addKassa(basisdatum, aggregation);
        }
        else {
            this.formulas.add(new BasicTimeseries(getFormula(aggregation), basisdatum, aggregation));
        }
    }

    private String getFormula(Aggregation aggregation) {
        switch (aggregation) {
            case FIRST:
                return "Open";
            case MAX:
                return "High";
            case MIN:
                return "Low";
            case LAST:
                return "Close";
            default:
                throw new IllegalArgumentException(aggregation.name());
        }
    }

    private boolean isFund() {
        return InstrumentUtil.isVwdFund(quote);
    }

    public void addBviPerformance(LocalDate referencedate) {
        if (this.quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND) {
            this.formulas.add(new MmTalkFormula("BVIPerformanceZR[" + mmtalk(referencedate) + "]+100"));
        }
    }

    public void addVolume(LocalDate basisdatum) {
        this.formulas.add(new BasicTimeseries("Volume", basisdatum, Aggregation.SUM).withoutFactor().withoutWaehrung());
    }

    public void addKassa(LocalDate basisdatum, Aggregation aggregation) {
        this.formulas.add(new BasicTimeseries("Kassa", basisdatum, aggregation));
    }

    private void addOpenInterest(LocalDate basisdatum, Aggregation aggregation) {
        this.formulas.add(new BasicTimeseries("OpenInterest", basisdatum, aggregation)
                .withoutWaehrung().withoutFactor());
    }

    public void addOpenInterest(LocalDate basisdatum) {
        addOpenInterest(basisdatum, Aggregation.SUM);
    }

    public void addEvaluationPrice(LocalDate basisdatum) {
        this.formulas.add(new BasicTimeseries("EvaluationPrice", basisdatum, Aggregation.LAST));
    }

    public void addKontrakt(LocalDate basisdatum) {
        this.formulas.add(new BasicTimeseries("Kontrakt", basisdatum, Aggregation.SUM).withoutWaehrung().withoutFactor());
    }

    public void addGd(int zeitraum, String methode) {
        this.formulas.add(new MmTalkFormula("GD[" + zeitraum + ";" + mmtalk(methode) + "]"));
    }

    public void addMmTalk(String mmtalk) {
        this.formulas.add(new MmTalkFormula(mmtalk));
    }

}
