/*
 * EodPriceHistorySupport.java
 *
 * Created on 20.02.13 09:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryProvider;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryRequest;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryResponse;

/**
 * @author zzhao
 */
public abstract class EodPriceHistorySupport extends TimeSeriesCacheSupport
        implements InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private EodPriceHistoryProvider eodProvider;

    private EodTermRepo termRepo;

    public void setTermRepo(EodTermRepo termRepo) {
        this.termRepo = termRepo;
    }

    public void setEodProvider(EodPriceHistoryProvider eodProvider) {
        this.eodProvider = eodProvider;
    }

    protected EodTermRepo getTermRepo() {
        return termRepo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.eodProvider, "end-of-day price history provider required");
        Assert.notNull(this.termRepo, "quote def dictionary required");
    }

    protected HistoricTimeseries[] getHistoricTimeSeries(HistoricRequest req,
            List<String> formulas, Interval interval) {
        return new PriceHelper(this.termRepo, req, formulas).support(this, interval);
    }

    protected HistoricTimeseries getCrossRates(Quote quote, Interval interval) {
        final LocalDate from = interval.getStart().toLocalDate();
        final LocalDate to = interval.getEnd().toLocalDate();
        final String cacheKey = quote.getSymbolVwdfeed();
        final HistoricTimeseries ht = fromCache(cacheKey, from, to);
        if (null != ht) {
            return ht;
        }
        else {
            final EodTermRepo.Term term = this.termRepo.getStandardTerm(quote.getQuotedef(),
                    PriceType.CLOSE);
            if (null == term) {
                throw new IllegalStateException("no close time series for: " + quote);
            }

            final IntArrayList fieldIdList = new IntArrayList(5);
            term.collectFieldIds(fieldIdList);
            final int[] fieldIds = fieldIdList.toIntArray();

            Interval reqInt = extendInterval(interval);
            final EodPriceHistoryResponse resp = query(quote, reqInt, fieldIds);
            final HistoricTimeseries prices = term.getPrice(resp);
            final CachedTimeSeries ct = cacheTimeSeries(cacheKey, prices);
            if (null == ct) {
                return prices;
            }
            else {
                return fromCache(ct, from, to);
            }
        }
    }

    private Interval extendInterval(Interval interval) {
        if (isCacheAvailable()) {
            // always request at least one year
            final DateTime oneYearBefore = interval.getEnd().minusYears(1);
            if (oneYearBefore.isBefore(interval.getStart())) {
                return oneYearBefore.isBefore(HistoricTimeseriesUtils.FIRST_DATE_TIME) ?
                        interval.withStart(HistoricTimeseriesUtils.FIRST_DATE_TIME) :
                        interval.withStart(oneYearBefore);
            }
        }

        return interval;
    }

    /**
     * @param hts
     * @param list
     * @param req
     * @param interval interval for corporate actions
     * @param cts close prices, must be aligned with the given interval
     */
    protected void postProcessTimeSeries(HistoricTimeseries[] hts, List<Triple> list,
            HistoricRequest req, Interval interval, HistoricTimeseries cts) {
        // default empty
    }

    private EodPriceHistoryResponse query(Quote quote, Interval interval, int[] fieldIds) {
        final EodPriceHistoryRequest eodReq = new EodPriceHistoryRequest(quote.getId(),
                interval, fieldIds);
        final EodPriceHistoryResponse resp = this.eodProvider.query(eodReq);
        if (!resp.isValid()) {
            logger.warn("<query> invalid eod price history response for {}", eodReq);
            throw new NoDataException("failed query price history");
        }
        return resp;
    }

    private final class PriceHelper {

        private final HistoricRequest req;

        private final List<Triple> triples;

        private final int quoteDef;

        private final EodTermRepo eodTermRepo;

        private EodTermRepo.Term closeTerm = null;

        private final int resultLength;

        private PriceHelper(EodTermRepo eodTermRepo, HistoricRequest req, List<String> formulas) {
            this.eodTermRepo = eodTermRepo;
            this.req = req;
            this.quoteDef = req.getQuote().getQuotedef();

            final boolean isFund = req.getQuote().getQuotedef() == 3;
            final PriceType closeType = isFund ? PriceType.KASSA : PriceType.CLOSE;
            this.resultLength = formulas.size();
            this.triples = new ArrayList<>(formulas.size() + 1); // just in case close needed
            for (String formula : formulas) {
                final PriceType priceType = PriceType.fromFormula(formula);
                final EodTermRepo.Term term = getTerm(formula, priceType);
                if (term == null) {
                    logger.debug("<PriceHelper> no eod term found for {}.{}", priceType, this.quoteDef);
                }
                this.triples.add(new Triple(formula, priceType, term));
                if (priceType == closeType && null == this.closeTerm) {
                    this.closeTerm = term;
                }
            }
            if (req.isWithDividend() && null == this.closeTerm) {
                this.closeTerm = this.eodTermRepo.getStandardTerm(this.quoteDef, closeType);
                if (this.closeTerm == null) {
                    logger.debug("<PriceHelper> no close term found for {}.{}", closeType, this.quoteDef);
                }
                else {
                    this.triples.add(new Triple(closeType.name(), closeType, this.closeTerm));
                }
            }
        }

        private EodTermRepo.Term getTerm(String formula, PriceType priceType) {
            final EodTermRepo.Term term = this.eodTermRepo.getTerm(priceType, this.quoteDef, formula);
            // if CLOSE was requested but is not defined, potentially KASSA contains the expected data -> try and return
            // currently relevant for quotedefs 4, 6, 16, 922, 1522
            if (priceType == PriceType.CLOSE && term == null) {
                return this.eodTermRepo.getTerm(PriceType.KASSA, this.quoteDef, formula);
            }
            return term;
        }

        private int[] toFieldIds(List<Triple> triples) {
            final IntArrayList list = new IntArrayList(10);
            for (Triple triple : triples) {
                if (null != triple && null != triple.getTerm()) {
                    triple.getTerm().collectFieldIds(list);
                }
            }

            return list.toIntArray();
        }

        private String getCacheKey(int idx) {
            return getCacheKey(this.triples.get(idx));
        }

        private String getCacheKey(Triple triple) {
            final Quote quote = this.req.getQuote();
            if (StringUtils.isBlank(quote.getSymbolMmwkn())) {
                return quote.getId() + "." + triple.getFormula();
            }
            else {
                return quote.getSymbolMmwkn() + "." + triple.getFormula();
            }
        }

        private HistoricTimeseries[] support(EodPriceHistorySupport support, Interval interval) {
            final HistoricTimeseries[] resHts = new HistoricTimeseries[this.triples.size()];
            final LocalDate from = interval.getStart().toLocalDate();
            final LocalDate to = interval.getEnd().toLocalDate();
            final List<Triple> toAsk = new ArrayList<>(resHts.length);
            for (int i = 0; i < resHts.length; i++) {
                resHts[i] = support.fromCache(getCacheKey(i), from, to);
                if (null == resHts[i]) {
                    toAsk.add(this.triples.get(i));
                }
            }

            if (!toAsk.isEmpty()) {
                final int[] fieldIds = toFieldIds(toAsk);
                if (null == fieldIds || fieldIds.length == 0) {
                    if (!anyHistoryFromCache(resHts)) {
                        throw new IllegalArgumentException("data might exist," +
                                " but no VWD field mapping rules found");
                    }
                }
                else {
                    Interval reqInt = support.extendInterval(interval);
                    final LocalDate startDay = reqInt.getStart().toLocalDate();
                    final EodPriceHistoryResponse resp = support.query(this.req.getQuote(),
                            reqInt, fieldIds);
                    final HistoricTimeseries[] hts = new HistoricTimeseries[toAsk.size()];
                    for (int i = 0; i < hts.length; i++) {
                        final EodTermRepo.Term term = toAsk.get(i).getTerm();
                        hts[i] = support.evaluate(term, resp, startDay);
                    }

                    int idx = 0;
                    for (int i = 0; i < resHts.length; i++) {
                        if (null != resHts[i]) {
                            continue;
                        }
                        final CachedTimeSeries ct = support.cacheTimeSeries(getCacheKey(i), hts[idx]);
                        if (null != ct) {
                            resHts[i] = support.fromCache(ct, from, to);
                        }
                        else {
                            resHts[i] = hts[idx];
                        }
                        idx++;
                    }
                }
            }

            final HistoricTimeseries closeTimeSeries = findCloseTimeSeries(resHts);
            final HistoricTimeseries[] ret = this.resultLength == this.triples.size()
                    ? resHts
                    : Arrays.copyOfRange(resHts, 0, this.resultLength);
            final List<Triple> list = this.resultLength == this.triples.size()
                    ? this.triples
                    : this.triples.subList(0, this.resultLength);

            final HistoricTimeseries caCts = null == this.req.getCorporateActionReferenceDate()
                    ? closeTimeSeries
                    : extendCloseTimeseries(support, closeTimeSeries, interval);
            final Interval caInterval = null == this.req.getCorporateActionReferenceDate()
                    ? interval
                    : interval.withEnd(this.req.getCorporateActionReferenceDate().toDateTimeAtStartOfDay());

            support.postProcessTimeSeries(ret, list, this.req, caInterval, caCts);
            return ret;
        }

        private HistoricTimeseries extendCloseTimeseries(EodPriceHistorySupport support,
                HistoricTimeseries closeTimeSeries, Interval interval) {
            if (null != closeTimeSeries && null != this.closeTerm) {
                DateTime refDt = this.req.getCorporateActionReferenceDate().toDateTimeAtStartOfDay();
                if (refDt.isAfter(interval.getEnd())) {
                    Interval reqInt = new Interval(interval.getEnd(), refDt);
                    final LocalDate startDay = reqInt.getStart().toLocalDate();
                    Triple closeTriple = findCloseTriple();
                    final int[] fieldIds = toFieldIds(Collections.singletonList(closeTriple));
                    final EodPriceHistoryResponse resp = support.query(this.req.getQuote(),
                            reqInt, fieldIds);
                    HistoricTimeseries ht = support.evaluate(closeTriple.getTerm(), resp, startDay);
                    return HistoricTimeseriesUtils.merge(closeTimeSeries, ht);
                }
            }
            return closeTimeSeries;
        }

        private boolean anyHistoryFromCache(HistoricTimeseries[] resHts) {
            for (HistoricTimeseries resHt : resHts) {
                if (null != resHt) {
                    return true;
                }
            }
            return false;
        }

        private Triple findCloseTriple() {
            if (this.req.isWithDividend() && null != this.closeTerm) {
                for (int i = this.triples.size() - 1; i >= 0; i--) {
                    if (this.triples.get(i).getTerm() == this.closeTerm) {
                        return this.triples.get(i);
                    }
                }
            }
            return null;
        }

        private HistoricTimeseries findCloseTimeSeries(HistoricTimeseries[] result) {
            if (this.req.isWithDividend() && null != this.closeTerm) {
                for (int i = this.triples.size() - 1; i >= 0; i--) {
                    if (this.triples.get(i).getTerm() == this.closeTerm) {
                        return result[i];
                    }
                }
            }
            return null;
        }
    }

    protected HistoricTimeseries evaluate(EodTermRepo.Term term, EodPriceHistoryResponse resp,
            LocalDate startDay) {
        if (null == term) {
            return HistoricTimeseriesUtils.emptyTimeSeries(startDay);
        }
        final HistoricTimeseries ht = term.getPrice(resp);
        return null == ht ? HistoricTimeseriesUtils.emptyTimeSeries(startDay) : ht;
    }

    static final class Triple {
        private final String formula;

        private final PriceType priceType;

        private final EodTermRepo.Term term;

        private Triple(String formula, PriceType priceType, EodTermRepo.Term term) {
            this.formula = formula;
            this.priceType = priceType;
            this.term = term;
        }

        String getFormula() {
            return formula;
        }

        PriceType getPriceType() {
            return priceType;
        }

        EodTermRepo.Term getTerm() {
            return term;
        }

        VwdFieldDescription.Field getVwdField() {
            if (PriceType.ADF != this.priceType) {
                throw new UnsupportedOperationException("no vwd field support for: " + this.priceType);
            }
            return VwdFieldDescription.getField(Integer.parseInt(this.formula.substring(
                    this.formula.indexOf("_") + 1)));
        }
    }
}
