/*
 * HistoricTimeseriesProviderImpl.java
 *
 * Created on 31.08.2006 16:04:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.mm.MMKeyType;
import de.marketmaker.istar.common.mm.MMService;
import de.marketmaker.istar.common.mm.MMServiceResponse;
import de.marketmaker.istar.common.mm.MMTalkException;
import de.marketmaker.istar.common.mm.MMTalkTableRequest;
import de.marketmaker.istar.common.mm.MMTimeseriesRequest;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HistoricRatiosProviderImpl;
import de.marketmaker.istar.merger.provider.PortfolioRatiosRequest;

import static de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils.FIRST_DAY;

/**
 * Provider for historic timeseries, controls how those timeseries are retrieved and how
 * they are cached.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricTimeseriesProviderImpl implements InitializingBean,
        HistoricTimeseriesProvider {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private MMService mmservice;

    private Ehcache timeseriesCache;

    private final QuotedefMapper mapper = new QuotedefMapper();

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    protected static class CacheableTimeseries implements Serializable {
        static final long serialVersionUID = 1L;

        // start of period this data was requested for; may differ from from if data
        // at the start of that period is not available.
        private LocalDate requestedFrom;

        // day for values[0]
        private LocalDate from;

        // values for consecutive days: NaN for days w/o data.
        private double[] values;

        public CacheableTimeseries(LocalDate requestedFrom, LocalDate from, double[] values) {
            this.requestedFrom = requestedFrom;
            this.from = from;
            this.values = values;
        }

        public LocalDate getFrom() {
            return from;
        }

        public LocalDate getRequestedFrom() {
            return requestedFrom;
        }

        public double[] getValues() {
            return values;
        }
    }

    public void setMmservice(MMService mmservice) {
        this.mmservice = mmservice;
    }

    public void setTimeseriesCache(Ehcache timeseriesCache) {
        this.timeseriesCache = timeseriesCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.mapper.afterPropertiesSet();
    }

    @Override
    public List<HistoricTimeseries> getTimeseries(HistoricTimeseriesRequest request) {
        final boolean singleDayData = request.isWithSingleDayData() && request.onlyBasicTimeseriesFormulas();

        final String[] formulas = request.getFormulas();
        final HistoricTimeseries[] result = new HistoricTimeseries[formulas.length];

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        if (profile.getPriceQuality(request.getQuote()) == PriceQuality.NONE
                || request.getTo().isBefore(FIRST_DAY)) {
            return Arrays.asList(result);
        }

        final String mmwkn = request.getQuote().getSymbolMmwkn();
        if (mmwkn == null) {
            this.logger.warn("<getTimeseries> no mmwkn for " + request.getQuote().getId() + ".qid");
            return Arrays.asList(result);
        }

        final LocalDate end = HistoricTimeseriesUtils.computeEnd(request);
        final LocalDate begin = HistoricTimeseriesUtils.computeBegin(request);
        final PriceRecord priceRecord = getPriceRecord(request, end);

        if (end.isBefore(begin)) {
            return Arrays.asList(result);
        }

        final String[] formulasWithoutCachedResult = new String[formulas.length];
        int without = 0;
        for (int i = 0; i < formulas.length; i++) {
            final HistoricTimeseries ts = singleDayData ? null : getCachedTimeseries(mmwkn, formulas[i], begin, end, priceRecord);
            if (ts != null) {
                result[i] = ts;
            }
            else {
                formulasWithoutCachedResult[without++] = formulas[i];
            }
        }

        if (without == 0) {
            return Arrays.asList(result);
        }

        // request results for formulas without cached results

        final String[] requestFormulas = new String[without];
        System.arraycopy(formulasWithoutCachedResult, 0, requestFormulas, 0, without);

        if (singleDayData) {
            try {
                return getSingleDayData(request.getQuote(), end, requestFormulas, result);
            } catch (Exception e) {
                this.logger.warn("<getSingleDayData> failed for "
                        + request.getQuote() + ", formulas="
                        + Arrays.toString(requestFormulas), e);
            }

            Arrays.fill(result, null); // reset to null
        }

        // always request at least one year
        final LocalDate requestFrom = new LocalDate(Math.max(FIRST_DAY.getYear(), begin.getYear() - 1), 1, 1);

        final Object[] timeseries;
        try {
            timeseries = getTimeseries(request.getQuote(), requestFormulas, requestFrom, LocalDate.now(), priceRecord);
        } catch (MMTalkException e) {
            this.logger.warn("<getTimeseries> failed for " + request.getQuote()
                    + " and " + Arrays.toString(requestFormulas), e);
            return Arrays.asList(result);
        }

        // merge cached with requested results

        final Object[] myTimeseries = (Object[]) timeseries[0];

        int j = 0;
        for (int i = 0; i < result.length; i++) {
            if (result[i] != null) {
                continue;
            }
            final double[] values = (double[]) myTimeseries[j++];
            final CacheableTimeseries ct = toCacheableTimeseries(values, requestFrom);
            if (isCacheable(priceRecord)) {
                this.timeseriesCache.put(new Element(getKey(mmwkn, formulas[i]), ct));
            }

            result[i] = toHistoricTimeseries(ct, begin, end);
        }

        applyAggregations(request, result);

        return Arrays.asList(result);
    }

    @Override
    public List<HistoricTimeseries> getTimeseries(PortfolioRatiosRequest request, LocalDate rfrom, LocalDate rto) {
        final String[] formulas = new String[]{HistoricRatiosProviderImpl.getPortfolioFormula(request)};
        final HistoricTimeseries[] result = new HistoricTimeseries[formulas.length];

        final LocalDate to = HistoricTimeseriesUtils.clampToMaxFuture(rto);
        final LocalDate from = HistoricTimeseriesUtils.clampToMaxPast(rfrom);

        if (to.isBefore(from)) {
            return Arrays.asList(result);
        }

        // always request at least one year
        final LocalDate requestFrom = new LocalDate(Math.max(FIRST_DAY.getYear(), from.getYear() - 1), 1, 1);

        final Object[] timeseries;
        try {
            final MMTimeseriesRequest tsrequest = new MMTimeseriesRequest(MMKeyType.SECURITY_WKN, requestFrom, LocalDate.now());
            tsrequest.appendKey("I846900"); // dummy

            for (final String requestFormula : formulas) {
                tsrequest.appendFormula(requestFormula);
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getTimeseries> mmTalk: {} {}", from + ";" + to, tsrequest.getFormulas());
            }
            final MMServiceResponse response = this.mmservice.getTimeseries(tsrequest);
            timeseries = response.getData();
        } catch (MMTalkException e) {
            this.logger.warn("<getTimeseries> failed for " + request + " and " + Arrays.toString(formulas), e);
            return Arrays.asList(result);
        }

        final Object[] myTimeseries = (Object[]) timeseries[0];
        for (int i = 0; i < result.length; i++) {
            final double[] values = (double[]) myTimeseries[i];
            final CacheableTimeseries ct = toCacheableTimeseries(values, requestFrom);
            result[i] = toHistoricTimeseries(ct, from, to);
        }

        return Arrays.asList(result);
    }

    private List<HistoricTimeseries> getSingleDayData(Quote quote, LocalDate date,
            String[] rawFormulas, HistoricTimeseries[] result) {

        final List<String> formulas = toSingleDayFormulas(date, rawFormulas);

        final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                .withFormulas(formulas)
                .withKey(quote.getSymbolMmwkn());

        final MMServiceResponse response;
        try {
            response = this.mmservice.getMMTalkTable(tableRequest);
        } catch (MMTalkException e) {
            this.logger.warn("<getSingleDayData> failed for " + quote + " and " + formulas, e);
            return Arrays.asList(result);
        }

        final Object[] mmtt = response.getData();

        final Price[] prices = new Price[result.length];
        DateTime maxDate = FIRST_DAY.toDateTimeAtStartOfDay();
        for (int i = 0; i < result.length; i++) {
            final BigDecimal v = HistoricRatiosProviderImpl.getValue(mmtt[i * 2]);
            final LocalDate d = HistoricRatiosProviderImpl.getLocalDate(mmtt[i * 2 + 1]);

            if (v != null && d != null) {
                final DateTime dt = d.toDateTimeAtStartOfDay();
                prices[i] = new PriceImpl(v, null, null, dt, PriceQuality.END_OF_DAY);
                if (dt.isAfter(maxDate)) {
                    maxDate = dt;
                }
            }
        }

        for (int i = 0; i < result.length; i++) {
            final LocalDate ld = maxDate.toLocalDate();
            if (prices[i] != null && maxDate.equals(prices[i].getDate())) {
                result[i] = new HistoricTimeseries(new double[]{prices[i].getValue().doubleValue()}, ld);
            }
            else {
                result[i] = new HistoricTimeseries(new double[0], ld);
            }
        }

        return Arrays.asList(result);
    }

    List<String> toSingleDayFormulas(LocalDate date, String[] rawFormulas) {
        final List<String> result = new ArrayList<>(rawFormulas.length * 2);
        for (final String rawFormula : rawFormulas) {
            final int index = rawFormula.indexOf("]");

            final String prefix = rawFormula.substring(0, index + 1);
            final String suffix = rawFormula.substring(index + 1, rawFormula.length());
            final String atFormula = ".at[" + HistoricTimeseriesRequest.mmtalk(date) + ";100]";
            result.add(prefix + atFormula + suffix);
            result.add(prefix + atFormula + ".Datum");
        }
        return result;
    }

    @Override
    public List<HistoricTimeseries> getTimeseries(HistoricRequestImpl request) {
        throw new UnsupportedOperationException("not implemented");
    }

    private boolean isCacheable(PriceRecord priceRecord) {
        return this.timeseriesCache != null && priceRecord == null;
    }

    private void applyAggregations(HistoricTimeseriesRequest request, HistoricTimeseries[] result) {
        if (!HistoricTimeseriesUtils.isWithAggregation(request)) {
            return;
        }
        for (int i = 0; i < result.length; i++) {
            final Aggregation aggregation = request.getAggregation(i);
            if (aggregation == null || result[i].size() == 0) {
                continue;
            }
            result[i] = result[i].aggregate(request.getAggregationPeriod(), aggregation);
        }
    }

    private Object[] getTimeseries(Quote quote, String[] requestFormulas, LocalDate requestFrom,
            LocalDate now, PriceRecord priceRecord) throws MMTalkException {
        final MMTimeseriesRequest request = new MMTimeseriesRequest(MMKeyType.SECURITY_WKN, requestFrom, now);
        request.appendKey(quote.getSymbolMmwkn());

        if (priceRecord != null) {
            request.withPriceUpdate(MMPriceUpdateBuilder.byMapper(this.mapper, quote, priceRecord).build());
        }

        for (final String requestFormula : requestFormulas) {
            request.appendFormula(requestFormula);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getTimeseries> mmTalk: {} {}",
                    request.getFrom() + ";" + request.getTo(), request.getKeys() + ";" + request.getFormulas());
        }
        final MMServiceResponse response = this.mmservice.getTimeseries(request);
        return response.getData();
    }

    private CacheableTimeseries toCacheableTimeseries(double[] values, LocalDate requestFrom) {
        if (values == null) { // no data at all, perhaps not even known
            return new CacheableTimeseries(FIRST_DAY, new LocalDate(), new double[0]);
        }
        int n = 0;
        while (n < values.length && Double.isNaN(values[n])) {
            n++;
        }
        if (n == values.length) {
            return new CacheableTimeseries(requestFrom, new LocalDate(), new double[0]);
        }
        if (n < 5) {
            return new CacheableTimeseries(requestFrom, requestFrom, values);
        }

        // too many NaNs at beginning, clip them off
        final double[] tmp = new double[values.length - n];
        System.arraycopy(values, n, tmp, 0, tmp.length);
        return new CacheableTimeseries(requestFrom, requestFrom.plusDays(n), tmp);
    }

    private HistoricTimeseries getCachedTimeseries(String mmwkn, String formula,
            LocalDate from, LocalDate to, PriceRecord priceRecord) {
        if (!isCacheable(priceRecord)) {
            return null;
        }

        final Element element = this.timeseriesCache.get(getKey(mmwkn, formula));
        if (element == null) {
            return null;
        }
        final CacheableTimeseries ct = (CacheableTimeseries) element.getValue();
        if (from.isBefore(ct.requestedFrom)) {
            return null;
        }
        return toHistoricTimeseries(ct, from, to);
    }

    private String getKey(String mmwkn, String formula) {
        return mmwkn + "." + formula;
    }

    protected HistoricTimeseries toHistoricTimeseries(CacheableTimeseries ct, LocalDate from,
            LocalDate to) {
        // to is inclusive, so add a day
        final int numDays = DateUtil.daysBetween(from, to.plusDays(1));
        if (numDays <= 0) {
            return new HistoricTimeseries(new double[0], from);
        }
        final double[] values = new double[numDays];

        if (ct.getFrom().isAfter(to)) {
            Arrays.fill(values, Double.NaN);
            return new HistoricTimeseries(values, from);
        }

        final int srcPos;
        final int destPos;
        if (from.isBefore(ct.getFrom())) {
            srcPos = 0;
            destPos = DateUtil.daysBetween(from, ct.getFrom());
        }
        else {
            srcPos = DateUtil.daysBetween(ct.getFrom(), from);
            destPos = 0;
        }

        final double[] ctValues = ct.getValues();
        final int length = Math.min(ctValues.length - srcPos, values.length - destPos);

        System.arraycopy(ctValues, srcPos, values, destPos, length);
        if (destPos > 0) {
            Arrays.fill(values, 0, destPos, Double.NaN);
        }
        if (destPos + length < values.length) {
            Arrays.fill(values, destPos + length, values.length, Double.NaN);
        }

        final HistoricTimeseries result = new HistoricTimeseries(values, from);
        for (int i = srcPos; i-- > 0; ) {
            if (!Double.isNaN(ctValues[i])) {
                result.setPreviousValue(ctValues[i], from.minusDays(srcPos - i));
                break;
            }
        }
        return result;
    }
}
