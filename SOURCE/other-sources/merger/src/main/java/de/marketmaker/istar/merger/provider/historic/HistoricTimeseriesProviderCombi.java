/*
 * HistoricTimeseriesProviderImpl.java
 *
 * Created on 31.08.2006 16:04:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import de.marketmaker.istar.common.mm.MMKeyType;
import de.marketmaker.istar.common.mm.MMService;
import de.marketmaker.istar.common.mm.MMServiceResponse;
import de.marketmaker.istar.common.mm.MMTalkException;
import de.marketmaker.istar.common.mm.MMTimeseriesRequest;
import de.marketmaker.istar.common.util.concurrent.ExecutorServiceUtil;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.PortfolioRatiosRequest;

/**
 * @author zzhao
 */
public class HistoricTimeseriesProviderCombi extends TimeSeriesCacheSupport
        implements InitializingBean, HistoricTimeseriesProvider, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService es;

    private HistoricTimeseriesProviderEod delegate;

    private MMService mmService;

    private EodTermRepo termRepo;

    public HistoricTimeseriesProviderCombi() {
    }

    public void setTermRepo(EodTermRepo termRepo) {
        this.termRepo = termRepo;
    }

    public void setDelegate(HistoricTimeseriesProviderEod delegate) {
        this.delegate = delegate;
    }

    public void setMmService(MMService mmService) {
        this.mmService = mmService;
    }

    @Override
    public void destroy() throws Exception {
        ExecutorServiceUtil.shutdownAndAwaitTermination(this.es, 60);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.delegate, "delegate EodPriceHistoryProvider required");
        Assert.notNull(this.mmService, "MM service required");
        Assert.notNull(this.termRepo, "eod term repo required");
        this.es = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "mm-service");
            }
        });
    }

    public List<HistoricTimeseries> getTimeseries(HistoricTimeseriesRequest req) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<HistoricTimeseries> getTimeseries(PortfolioRatiosRequest request, LocalDate from,
            LocalDate to) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public List<HistoricTimeseries> getTimeseries(HistoricRequestImpl req) {
        final List<HistoricTerm> historicTerms = req.getHistoricTerms();
        final HistoricTimeseries[] result = new HistoricTimeseries[historicTerms.size()];
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile.getPriceQuality(req.getQuote()) == PriceQuality.NONE
                || req.getTo().isBefore(HistoricTimeseriesUtils.FIRST_DAY)) {
            return Arrays.asList(result);
        }

        final LocalDate end = HistoricTimeseriesUtils.computeEnd(req);
        final LocalDate begin = HistoricTimeseriesUtils.computeBegin(req);

        if (end.isBefore(begin)) {
            return Arrays.asList(result);
        }

        final PriceRecord priceRecord = getPriceRecord(req, end);
        final Interval interval = calcInterval(begin, end);

        final int[] iaStd = getIndexArray(result.length);
        final List<String> formulasStd = new ArrayList<>(result.length);
        final int[] iaMM = getIndexArray(result.length);
        final List<String> formulasMM = new ArrayList<>(result.length);
        for (int i = 0; i < historicTerms.size(); i++) {
            final HistoricTerm historicTerm = historicTerms.get(i);
            switch (historicTerm.getType()) {
                case ADF:
                    iaStd[formulasStd.size()] = i;
                    formulasStd.add("ADF_" + historicTerm.getContent());
                    break;
                case FunctionalPrice:
                    iaStd[formulasStd.size()] = i;
                    formulasStd.add(historicTerm.getContent());
                    break;
                case MmFormula:
                    iaMM[formulasMM.size()] = i;
                    formulasMM.add(historicTerm.getContent());
                    break;
                default:
                    throw new UnsupportedOperationException("no support for: " + historicTerm.getType());
            }
        }

        Future<HistoricTimeseries[]> future = null;
        if (!formulasMM.isEmpty()) {
            future = fromMMService(req.getQuote(), formulasMM, interval, priceRecord);
        }
        final HistoricTimeseries[] htStd =
                this.delegate.getTimeseries(req, formulasStd, interval, priceRecord);
        assignResult(result, iaStd, htStd);

        if (null != future) {
            try {
                final HistoricTimeseries[] htMM = future.get(10, TimeUnit.SECONDS);
                assignResult(result, iaMM, htMM);
            } catch (Exception e) {
                this.logger.warn("<getTimeseries> failed query mm-service", e);
                throw new IllegalStateException("cannot retrieve prices from mm-service");
            }
        }

        if (isWithAggregation(req)) {
            applyAggregations(req, historicTerms, result);
        }
        return Arrays.asList(result);
    }

    private void assignResult(HistoricTimeseries[] result, int[] ia, HistoricTimeseries[] hts) {
        for (int i = 0; i < hts.length; i++) {
            result[ia[i]] = hts[i];
        }
    }

    private Future<HistoricTimeseries[]> fromMMService(final Quote quote,
            final List<String> formulas, final Interval interval, final PriceRecord priceRecord) {
        return this.es.submit(new Callable<HistoricTimeseries[]>() {
            @Override
            public HistoricTimeseries[] call() throws Exception {
                final HistoricTimeseries[] result = new HistoricTimeseries[formulas.size()];
                final LocalDate from = interval.getStart().toLocalDate();
                final LocalDate to = interval.getEnd().toLocalDate();

                final List<String> mmFormulas;
                if (null == priceRecord) {
                    mmFormulas = new ArrayList<>(formulas.size());
                    for (int i = 0; i < formulas.size(); i++) {
                        result[i] = fromCache(getCacheKey(quote, formulas.get(i)), from, to);
                        if (null == result[i]) {
                            mmFormulas.add(formulas.get(i));
                        }
                    }
                }
                else {
                    mmFormulas = formulas;
                }

                if (!mmFormulas.isEmpty()) {
                    // always request at least one year
                    final LocalDate requestFrom = (null == priceRecord && isCacheAvailable()) ?
                            new LocalDate(Math.max(HistoricTimeseriesUtils.FIRST_DAY.getYear(),
                                    from.getYear() - 1), 1, 1) : from;
                    final LocalDate now = new LocalDate();

                    final Object[] timeseries;
                    try {
                        timeseries = getTimeseries(quote, mmFormulas, requestFrom, now, priceRecord);
                        // merge cached with requested results
                        final Object[] myTimeseries = (Object[]) timeseries[0];
                        int j = 0;
                        for (int i = 0; i < result.length; i++) {
                            if (result[i] != null) {
                                continue;
                            }
                            final double[] values = (double[]) myTimeseries[j++];
                            final CachedTimeSeries ct = toCachedTimeSeries(values, requestFrom);
                            if (null == priceRecord && isCacheAvailable()) {
                                cacheTimeSeries(getCacheKey(quote, formulas.get(i)), ct);
                            }

                            result[i] = fromCache(ct, from, to);
                        }
                    } catch (MMTalkException e) {
                        logger.warn("<getTimeseries> failed for " + quote + " and " + mmFormulas, e);
                        for (int i = 0; i < result.length; i++) {
                            if (null == result[i]) {
                                result[i] = HistoricTimeseriesUtils.emptyTimeSeries(from);
                            }
                        }
                    }
                }

                return result;
            }
        });
    }

    private Object[] getTimeseries(Quote quote, List<String> formulas, LocalDate requestFrom,
            LocalDate now, PriceRecord priceRecord) throws MMTalkException {
        final MMTimeseriesRequest request = new MMTimeseriesRequest(MMKeyType.SECURITY_WKN,
                requestFrom, now);
        request.appendKey(quote.getSymbolMmwkn());

        if (priceRecord != null) {
            request.withPriceUpdate(MMPriceUpdateBuilder.byEodTerms(this.termRepo, quote, priceRecord).build());
        }

        for (final String requestFormula : formulas) {
            request.appendFormula(requestFormula);
        }

        final MMServiceResponse response = this.mmService.getTimeseries(request);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getTimeseries> mmTalk: {} {}",
                    request.getFrom() + ";" + request.getTo(), request.getKeys() + ";" + request.getFormulas());
        }
        return response.getData();
    }

    private String getCacheKey(Quote quote, String suffix) {
        if (StringUtils.isBlank(quote.getSymbolMmwkn())) {
            return quote.getId() + "." + suffix;
        }
        else {
            return quote.getSymbolMmwkn() + "." + suffix;
        }
    }

    private int[] getIndexArray(int len) {
        final int[] ret = new int[len];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = -1;
        }
        return ret;
    }

    private Interval calcInterval(LocalDate from, LocalDate to) {
        return new Interval(from.toDateTimeAtStartOfDay(), to.plusDays(1).toDateTimeAtStartOfDay());
    }

    private void applyAggregations(HistoricRequest request, List<HistoricTerm> historicTerms,
            HistoricTimeseries[] result) {
        for (int i = 0; i < result.length; i++) {
            final Aggregation aggregation = getAggregation(historicTerms.get(i));
            if (aggregation == null || null == result[i] || result[i].size() == 0) {
                continue;
            }
            result[i] = result[i].aggregate(request.getAggregationPeriod(), aggregation);
        }
    }

    private Aggregation getAggregation(HistoricTerm historicTerm) {
        if (null != historicTerm.getAggregation()) {
            return historicTerm.getAggregation();
        }
        switch (historicTerm.getType()) {
            case FunctionalPrice:
                final PriceType priceType = PriceType.valueOf(historicTerm.getContent());
                switch (priceType) {
                    case OPEN:
                        return Aggregation.FIRST;
                    case HIGH:
                        return Aggregation.MAX;
                    case LOW:
                        return Aggregation.MIN;
                    case VOLUME:
                    case CONTRACT:
                    case OPENINTEREST:
                        return Aggregation.SUM;
                }
                break;
            case ADF:
                return fromVwdField(VwdFieldDescription.getField(
                        Integer.parseInt(historicTerm.getContent())));
        }

        // ultimate default
        return Aggregation.LAST;
    }

    private Aggregation fromVwdField(VwdFieldDescription.Field field) {
        if (VwdFieldDescription.ADF_Geld_Eroeffnung == field
                || VwdFieldDescription.ADF_Brief_Eroeffnung == field) {
            return Aggregation.FIRST;
        }
        else if (VwdFieldDescription.ADF_Geld_Tageshoch == field
                || VwdFieldDescription.ADF_Brief_Tageshoch == field) {
            return Aggregation.MAX;
        }
        else if (VwdFieldDescription.ADF_Geld_Tagestief == field
                || VwdFieldDescription.ADF_Brief_Tagestief == field) {
            return Aggregation.MIN;
        }
        else if (VwdFieldDescription.ADF_Geld_Umsatz_in_Whrg == field
                || VwdFieldDescription.ADF_Brief_Umsatz_in_Whrg == field
                || VwdFieldDescription.ADF_Umsatz_Gesamt_Call == field
                || VwdFieldDescription.ADF_Umsatz_Gesamt_Put == field
                || VwdFieldDescription.ADF_Umsatz_Gesamt_Futures == field
                || VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Call == field
                || VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Put == field
                || VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Fut == field) {
            return Aggregation.SUM;
        }

        return Aggregation.LAST;
    }

    private boolean isWithAggregation(HistoricRequest request) {
        final Period period = request.getAggregationPeriod();
        return (period != null && !Constants.ONE_DAY.equals(period));
    }
}
