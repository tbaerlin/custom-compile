package de.marketmaker.istar.merger.provider.analyzer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.analyses.analyzer.AnalysesCollector;
import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.PriceCache;
import de.marketmaker.istar.analyses.analyzer.Security;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.merger.provider.historic.EodTermRepo;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryProvider;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryRequest;
import de.marketmaker.istar.merger.provider.history.eod.EodPriceHistoryResponse;

/**
 * caching data that is needed to calculate success state and performance
 * also keep track of exchange rates
 */
public class PriceHistoryCacheImpl implements PriceCache, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceHistoryCacheImpl.class);

    // private static final int DAILY_HIGH = EodTermRepo.getTagNum(VwdFieldDescription.ADF_Tageshoch.id());
    private static final int CLOSE = EodTermRepo.getTagNum(VwdFieldDescription.ADF_Schluss.id());

    public static final MathContext ROUNDING_MODE = new MathContext(3, RoundingMode.HALF_UP);

    // mapping the vwdCode to the historic timeseries, this is per security
    private Map<String, Optional<HistoricTimeseries>> timeseriesCache = new HashMap<>();

    private EodPriceHistoryProvider eodPriceHistoryProvider;

    private IsoCurrencyConversionProvider currencyConversionProvider;

    private LoadingCache<ConversionKey, Double> exchangeRates;

    public void setEodPriceHistoryProvider(EodPriceHistoryProvider eodPriceHistoryProvider) {
        this.eodPriceHistoryProvider = eodPriceHistoryProvider;
    }

    public void setCurrencyConversionProvider(IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    @Override
    public void afterPropertiesSet() {
        exchangeRates = CacheBuilder.newBuilder()
                .maximumSize(30)
                .expireAfterWrite(6, TimeUnit.HOURS)
                .build(new CacheLoader<ConversionKey, Double>() {
                    @Override
                    public Double load(ConversionKey conversionKey) throws Exception {
                        return resolveExchangeRate(conversionKey);
                    }
                });
    }

    private Double resolveExchangeRate(ConversionKey conversionKey) {
        if (conversionKey.source.equals(conversionKey.destination)) {
            return 1d;
        }
        IsoCurrencyConversionProviderImpl.ConversionResult rate = currencyConversionProvider
                .getConversion(
                        conversionKey.source,
                        conversionKey.destination,
                        DateUtil.yyyyMmDdToLocalDate(conversionKey.yyyyMmDd));
        return rate.getFactor().doubleValue();
    }

    /**
     * this returns the previous close prize for a security at a certain date converted into the curency
     */
    @Override
    public Optional<BigDecimal> getPreviousClose(Profile profile, String targetCurrency, Security security, int yyyyMmDd) {
        LOGGER.debug("<getPreviousClose> currency " + targetCurrency
                + " for " + security.getVwdCode()
                + " in " + security.getCurrency()
                + " yyyyMmDd " + yyyyMmDd);
        final String vwdCode = security.getVwdCode();
        if (!timeseriesCache.containsKey(vwdCode)) {
            initializeTimeseries(security);
        }
        final Optional<HistoricTimeseries> timeseries = timeseriesCache.get(vwdCode);
        if (timeseries == null || !timeseries.isPresent()) {
            return Optional.empty();
        }

        // try 5 days in the past in case we hit christmas and weekends...
        double price = Double.NaN;
        for (int i = 1; i <= 5 && !Double.isFinite(price); i++) {
            LocalDate date = DateUtil.yyyyMmDdToLocalDate(yyyyMmDd).minusDays(i);
            price = timeseries.get().getValue(date);
        }
        if (!Double.isFinite(price)) {
            return Optional.empty();
        }

        final ConversionKey conversionKey = new ConversionKey(security.getCurrency(), targetCurrency, yyyyMmDd);
        final double adjusted = price * exchangeRates.getUnchecked(conversionKey);
        return Optional.of(new BigDecimal(adjusted, ROUNDING_MODE));
    }

    @Override
    public Optional<Boolean> getSuccess(Profile profile, Analysis analysis, int yyyyMmDd) {
        final Security security = analysis.getSecurity();
        final String vwdCode = security.getVwdCode();
        if (!timeseriesCache.containsKey(vwdCode)) {
            initializeTimeseries(security);
        }
        final Optional<HistoricTimeseries> optionalTimeseries = timeseriesCache.get(vwdCode);
        if (optionalTimeseries == null || !optionalTimeseries.isPresent()) {
            return Optional.of(false);
        }
        HistoricTimeseries timeseries = optionalTimeseries.get();

        if (analysis.getTarget() == null) {
            return Optional.of(false);
        }

        double target = analysis.getTarget().doubleValue();
        String targetCurrency = analysis.getTargetCurrency();

        LocalDate start = DateUtil.yyyyMmDdToLocalDate(analysis.getStartDate());
        LocalDate end = DateUtil.yyyyMmDdToLocalDate(analysis.getStartDate());
        int startOffset =  timeseries.getOffset(start);
        int endOffset =  timeseries.getOffset(end);

        final ConversionKey conversionKey = new ConversionKey(security.getCurrency(), targetCurrency, yyyyMmDd);
        final double adjusted = target * exchangeRates.getUnchecked(conversionKey);

        for (int i = startOffset; i <= endOffset; i++) {
            if (timeseries.getValue(i) >= adjusted) {
                return Optional.of(true);
            }
        }
        return Optional.of(false);
    }

    /**
     * initialize the cachced timeseries for all securities, we calculate the first and last
     * date that is included in any of the securities
     */
    public void initializeTimeseries(Security security) {
        System.out.print("init timeseries for: " + security.getVwdCode());

        if (security == null) {
            throw new IllegalStateException("security is null");
        }
        // calculate the max range
        int start = Integer.MAX_VALUE;
        int end = Integer.MIN_VALUE;
        for (Analysis analysis : security.getAnalyses()) {
            start = Math.min(start, analysis.getStartDate());
            end = Math.max(end, analysis.getEndDate());
        }

        final String vwdCode = security.getVwdCode();
        final Interval interval = new Interval(DateUtil.toDateTime(start, 0), DateUtil.toDateTime(end, 0));
        Long qid = security.getQid();
        if (qid == AnalysesCollector.NULL_QID) {
            System.out.println("qid is null for: " + security.getVwdCode());
            timeseriesCache.put(vwdCode, Optional.empty());
            return;
        }
        final EodPriceHistoryRequest request = new EodPriceHistoryRequest(qid, interval, CLOSE);
        EodPriceHistoryResponse response = eodPriceHistoryProvider.query(request);
        if (response == null) {
            System.out.println("response is null for: " + security.getVwdCode());
            timeseriesCache.put(vwdCode, Optional.empty());
            return;
        }
        HistoricTimeseries closeHistory = response.getHistory(CLOSE);
        if (closeHistory == null) {
            System.out.println("closeHistory is null for: " + security.getVwdCode());
            timeseriesCache.put(vwdCode, Optional.empty());
            return;
        }
        // finally:
        System.out.println("..found " + closeHistory.size());
        timeseriesCache.put(vwdCode, Optional.of(closeHistory));
    }



    private class ConversionKey {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConversionKey that = (ConversionKey) o;

            if (yyyyMmDd != that.yyyyMmDd) return false;
            if (!source.equals(that.source)) return false;
            return destination.equals(that.destination);

        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + destination.hashCode();
            result = 31 * result + yyyyMmDd;
            return result;
        }

        private final String source;
        private final String destination;
        private final int yyyyMmDd;

        ConversionKey(String source, String destination, int yyyyMmDd) {
            this.source = source;
            this.destination = destination;
            this.yyyyMmDd = yyyyMmDd;
        }


    }


          /*
    private Optional<BigDecimal> getHighestPrice(long start, long end, long qid) {
        if (NULL_QID == qid) {
            return Optional.empty();
        }
        long safeEnd = Math.min(DateTime.now().withTimeAtStartOfDay().minusDays(1).getMillis(), end);
        if (start >= safeEnd) {
            return Optional.empty();
        }
        Interval interval = new Interval(start, safeEnd);
        EodPriceHistoryResponse response = eodPriceHistoryProvider.query(new EodPriceHistoryRequest(qid, interval, DAILY_HIGH));
        if (!response.isValid()) {
            return Optional.empty();
        }
        HistoricTimeseries history = response.getHistory(DAILY_HIGH);
        if (history == null || history.size() == 0) {
            return Optional.empty();
        }

        OptionalDouble value = Arrays.stream(history.getValues()).filter(Double::isFinite).max();
        if (!value.isPresent() || !Double.isFinite(value.getAsDouble())) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(value.getAsDouble()));
    }

    private void evaluateSuccessState(Collection<Analysis> analyses) {
        analyses.stream()
                .filter(analysis -> EVALUATION_REQUIRED.contains(analysis.getSuccess()))
                .forEach(analysis -> {
                    long start = analysis.getStartDate();
                    long end = analysis.getEndDate();
                    long now = System.currentTimeMillis();
                    if (analysis.getSecurity() == null) {
                        logger.info("<evaluateSuccessState> [no security available]"
                                + " unable to compute success state for " + analysis);
                        analysis.setSuccess(SuccessState.FOREVER_UNKNOWN);
                        return;
                    }
                    Long qid = analysis.getSecurity().getQid();
                    if (qid == null || qid == 0) {
                        logger.info("<evaluateSuccessState> [no qid available]" +
                                " unable to compute success state for " + analysis);
                        analysis.setSuccess(SuccessState.FOREVER_UNKNOWN);
                        return;
                    }
                    String currency = analysis.getSecurity().getCurrency();
                    if (currency == null) {
                        logger.info("<evaluateSuccessState> [no currency available]" +
                                " unable to compute success state for " + analysis);
                        analysis.setSuccess(SuccessState.FOREVER_UNKNOWN);
                        return;
                    }
                    Optional<BigDecimal> maxPrice = getHighestPrice(start, end, qid);
                    if (!maxPrice.isPresent()) {
                        logger.info("<evaluateSuccessState> [no max price found]" +
                                " unable to compute success state for " + analysis);
                        analysis.setSuccess(SuccessState.FOREVER_UNKNOWN);
                        return;
                    }
                    analysis.setMaxPrice(maxPrice.get());
                    analysis.setEvalDate(now);

                    if (analysis.getTarget() == null) {
                        logger.info("<evaluateSuccessState> [no target price in analysis]" +
                                " unable to compute success state for " + analysis);
                        analysis.setSuccess(SuccessState.FOREVER_UNKNOWN);
                        return;
                    }

                    if (maxPrice.get().compareTo(analysis.getTarget()) >= 0) {
                        analysis.setSuccess(SuccessState.SUCCESSFUL);
                    } else {
                        analysis.setSuccess(SuccessState.FAILURE_SO_FAR);
                    }
                });
    }
    */
}
