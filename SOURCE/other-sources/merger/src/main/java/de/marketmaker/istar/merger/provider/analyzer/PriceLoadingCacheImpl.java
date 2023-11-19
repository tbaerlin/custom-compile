package de.marketmaker.istar.merger.provider.analyzer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.PriceCache;
import de.marketmaker.istar.analyses.analyzer.Security;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.feed.api.TickConnector;
import de.marketmaker.istar.feed.history.TickHistoryRequest;
import de.marketmaker.istar.feed.history.TickHistoryResponse;

/**
 * data container for price data retrieved from tickserver...
 *
 * needed for:
 *  - get the previous day price in order to determine (potential-) performance for analyses
 *  - get the day when a analysis target price was met in order to calculate success state of an
 *    analysis at a certain date
 *
 * estimates:
 *    analyses for a specific provider cover ~500 stocks
 *    we need to retain the analyses data for 10 years
 *
 */
@Deprecated
public class PriceLoadingCacheImpl implements PriceCache, InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceLoadingCacheImpl.class);

    private final int MAX_SIZE_PREVIOUS_CLOSE = 9_000;
    private final int MAX_SIZE_SUCCESS_DATES = 200_000;

    // cache for previous-day-price, matching (vwdCode, date) -> previousClosePrice
    // the instruments don't depend on specific analysis provider so the key doesn't need
    // to contain a provider part
    private LoadingCache<VwdCodeAndDay, Optional<BigDecimal>> closePrices;

    // cache for analysis success date, we store the date this way we can calculate the success
    // rates for past dates, also we need to store the analysis provider
    private LoadingCache<SuccessDateKey, Optional<Integer>> successDates;

    // pricedata/previous day on demand
    private TickConnector tickConnector;

    public void setTickConnector(TickConnector tickConnector) {
        this.tickConnector = tickConnector;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.closePrices = createPreviousPriceCache();
        this.successDates = createSuccessDateCache();
    }

    private LoadingCache<VwdCodeAndDay, Optional<BigDecimal>> createPreviousPriceCache() {
        return CacheBuilder.newBuilder().maximumSize(MAX_SIZE_PREVIOUS_CLOSE).build(
                new CacheLoader<VwdCodeAndDay, Optional<BigDecimal>>() {
                    @Override
                    public Optional<BigDecimal> load(VwdCodeAndDay previousCloseKey) throws Exception {
                        return resolvePreviousClose(previousCloseKey.vwdCode, previousCloseKey.yyyyMmDd);
                    }
                }
        );
    }

    private LoadingCache<SuccessDateKey, Optional<Integer>> createSuccessDateCache() {
        return CacheBuilder.newBuilder().maximumSize(MAX_SIZE_SUCCESS_DATES).build(
                new CacheLoader<SuccessDateKey, Optional<Integer>>() {
                    @Override
                    public Optional<Integer> load(SuccessDateKey successDateKey) throws Exception {
                        return resolveSuccessDate(successDateKey.analysisId);
                    }
                }
        );
    }

    @Override
    public Optional<BigDecimal> getPreviousClose(Profile profile, String currency, Security security, int yyyyMmDd) {
        LOGGER.info("<getPreviousClose> for " + security.getVwdCode() + " on " + yyyyMmDd);
        if (StringUtils.isEmpty(security.getVwdCode())) {
            return Optional.empty();
        }
        try {
            return closePrices.get(new VwdCodeAndDay(security.getVwdCode(), yyyyMmDd));
        } catch (ExecutionException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> getSuccess(Profile profile, Analysis analysis, int yyyyMmDd) {
        try {
            Optional<Integer> optionalDate = successDates.get(new SuccessDateKey(analysis.getAnalysisId()));
            if (optionalDate.isPresent()) {
                return Optional.of(optionalDate.get() < yyyyMmDd);
            }
            return Optional.empty();
        } catch (ExecutionException e) {
            return Optional.empty();
        }
    }

    private Optional<Integer> resolveSuccessDate(long analysisId) {
        return Optional.empty();
    }

    // called by the cache implementation to retrieve the data
    private Optional<BigDecimal> resolvePreviousClose(String vwdCode, int yyyyMmDd) {
        final TickHistoryResponse result = tickHistoryRequest(yyyyMmDd, vwdCode);
        if (!result.isValid()) {
            return Optional.empty();
        }
        final AggregatedTickRecord record = result.getRecord();
        final Timeseries<AggregatedTick> aggregatedTicks = record.getTimeseries(record.getInterval());
        return StreamSupport.stream(aggregatedTicks.spliterator(), false)
                .reduce((a,b) -> b)
                .map(DataWithInterval::getData)
                .map(AggregatedTick::getClose)
                .map(PriceCoder::decode)
                ;
    }

    private TickHistoryResponse tickHistoryRequest(int yyyyMmDd, String vwdCode) {
        DateTime end = DateUtil.yyyymmddToDateTime(yyyyMmDd);
        DateTime start = end.minusDays(7);
        Interval interval = new Interval(start, end);
        Duration duration = Duration.standardDays(1);
        return tickConnector.getTickHistory(new TickHistoryRequest(
                vwdCode,
                interval,
                duration,
                7,
                true,
                TickType.TRADE
        ));
    }

    /**
     * hash key for a specific (date, vwdCode) pair
     */
    private static class VwdCodeAndDay implements Serializable {
        private final String vwdCode;
        private final int yyyyMmDd;

        VwdCodeAndDay(String vwdCode, int yyyyMmDd) {
            assert vwdCode != null;
            this.vwdCode = vwdCode;
            this.yyyyMmDd = yyyyMmDd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VwdCodeAndDay that = (VwdCodeAndDay) o;
            if (yyyyMmDd != that.yyyyMmDd) return false;
            return vwdCode.equals(that.vwdCode);
        }

        @Override
        public int hashCode() {
            int result = yyyyMmDd;
            result = 31 * result + vwdCode.hashCode();
            return result;
        }
    }

    private static class SuccessDateKey implements Serializable {
        final long analysisId;

        SuccessDateKey(long analysisId) {
            this.analysisId = analysisId;
        }
    }

    private static class AnalysisSuccessState {
        int lastEvaluationDate;
        boolean success = false;

    }

}
