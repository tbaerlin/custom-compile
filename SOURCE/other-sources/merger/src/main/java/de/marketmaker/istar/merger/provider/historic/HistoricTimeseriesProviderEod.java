/*
 * HistoricTimeseriesProviderImpl.java
 *
 * Created on 31.08.2006 16:04:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.merger.Constants;

/**
 * @author zzhao
 */
@ManagedResource
public class HistoricTimeseriesProviderEod extends EodPriceHistorySupport {

    private CorporateActionSupport corporateActionSupport;

    public void setCorporateActionSupport(CorporateActionSupport corporateActionSupport) {
        this.corporateActionSupport = corporateActionSupport;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.notNull(this.corporateActionSupport, "corporate action factor required");
    }

    public HistoricTimeseries[] getTimeseries(HistoricRequest req, List<String> formulas,
            Interval interval, PriceRecord priceRecord) {
        if (formulas.isEmpty()) {
            return new HistoricTimeseries[0];
        }
        final HistoricTimeseries[] result = getHistoricTimeSeries(req, formulas, interval);
        withPriceRecord(req, result, priceRecord, formulas);
        return result;
    }

    private void withPriceRecord(HistoricRequest req, HistoricTimeseries[] result,
            PriceRecord priceRecord, List<String> formulas) {
        if (null != priceRecord) {
            final BigDecimal crossRate = this.corporateActionSupport.getCurrentCrossRate(
                    req.getCurrency(), getQuoteCurrency(req));
            for (int i = 0; i < formulas.size(); i++) {
                final EodTermRepo.Term term = getTermRepo().getTerm(
                        PriceType.fromFormula(formulas.get(i)), req.getQuote().getQuotedef(),
                        formulas.get(i));
                if (null != term) {
                    final Price price = term.getPrice(priceRecord);
                    if (null != price && null != price.getValue()) {
                        final double value = price.getValue().multiply(crossRate, Constants.MC).doubleValue();
                        withPrice(result, i, value, price.getDate().toLocalDate());
                    }
                }
            }
        }
    }

    private String getQuoteCurrency(HistoricRequest req) {
        final Currency currency = req.getQuote().getCurrency();
        return null == currency ? req.getCurrency() : currency.getSymbolIso();
    }

    private void withPrice(HistoricTimeseries[] result, int idx, double price, LocalDate date) {
        final HistoricTimeseries ht = result[idx];
        final int index = DateUtil.daysBetween(ht.getStartDay(), date);
        if (index < 0) {
            // specific date for this field is before timeseries start => ignore
            return;
        }

        if (index >= ht.size()) {
            final double[] values = new double[index + 1];
            Arrays.fill(values, Double.NaN);
            System.arraycopy(ht.getValues(),0,values,0,ht.size());
            values[index] = price;
            result[idx] = new HistoricTimeseries(values, ht.getStartDay());
        }
        else {
            ht.getValues()[index] = price;
        }
    }

    @Override
    protected void postProcessTimeSeries(HistoricTimeseries[] hts, List<Triple> list,
            HistoricRequest req, Interval interval, HistoricTimeseries cts) {
        if (!isAdjustmentNecessary(req, list)) {
            return;
        }

        final CorporateActionSupport.Factor factor = this.corporateActionSupport.getFactor(req,
                interval, cts);
        for (int i = 0; i < hts.length; i++) {
            if (null != hts[i]) {
                hts[i] = factor.adjust(hts[i], list.get(i));
            }
        }
    }

    private boolean isAdjustmentNecessary(HistoricRequest req, List<Triple> list) {
        if (!BigDecimal.ONE.equals(req.getFactor())) {
            return true;
        }

        final Currency cur = req.getQuote().getCurrency();
        if (null != req.getCurrency() && !req.getCurrency().equalsIgnoreCase(cur.getSymbolIso())) {
            return true;
        }

        if (req.isWithSplit() || req.isWithDividend()) {
            return this.corporateActionSupport.isAdjustmentNecessary(list);
        }

        return false;
    }
}
