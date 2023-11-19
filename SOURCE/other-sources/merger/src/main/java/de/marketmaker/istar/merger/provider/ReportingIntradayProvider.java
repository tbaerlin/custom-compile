package de.marketmaker.istar.merger.provider;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.OrderbookData;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.api.VendorkeyListRequest;
import de.marketmaker.istar.feed.api.VendorkeyListResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ReportingIntradayProvider implements IntradayProvider {
    private IntradayProvider delegate;

    private IntradayReportingDao intradayReportingDao;

    private Set<String> profileNames = Collections.emptySet();

    public void setDelegate(IntradayProvider delegate) {
        this.delegate = delegate;
    }

    public void setIntradayReportingDao(IntradayReportingDao intradayReportingDao) {
        this.intradayReportingDao = intradayReportingDao;
    }

    public void setProfileNames(String[] profileNames) {
        this.profileNames = new HashSet<>(Arrays.asList(profileNames));
    }

    public PageResponse getPage(PageRequest request) {
        return this.delegate.getPage(request);
    }

    @Override
    public VendorkeyListResponse getVendorkeys(VendorkeyListRequest request) {
        return this.delegate.getVendorkeys(request);
    }

    public OrderbookData getOrderbook(Quote quote) {
        final OrderbookData result = delegate.getOrderbook(quote);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!this.profileNames.contains(profile.getName())) {
            return result;
        }

        // important to use result's market depth quote (derived from quote)
        evaluate(result.getQuote(), profile, result.getPriceQuality());

        return result;
    }

    public boolean isWithOrderbook(Quote quote) {
        return this.delegate.isWithOrderbook(quote);
    }

    public TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request) {
        return this.delegate.getTypesForVwdcodes(request);
    }

    public List<PriceRecord> getPriceRecords(List<Quote> quotes) {
        final List<PriceRecord> result = delegate.getPriceRecords(quotes);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!this.profileNames.contains(profile.getName())) {
            return result;
        }

        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord priceRecord = result.get(i);
            evaluate(quote, profile, priceRecord.getPriceQuality());
        }

        return result;
    }

    public IntradayData getIntradayData(Quote q, Interval tickInterval) {
        return getIntradayData(Arrays.asList(q), tickInterval).get(0);
    }

    public List<IntradayData> getIntradayData(List<Quote> quotes, Interval tickInterval) {
        return getIntradayData(quotes, tickInterval, 0);
    }

    public List<IntradayData> getIntradayData(List<Quote> quotes, Interval tickInterval, int ttl) {
        final List<IntradayData> result = delegate.getIntradayData(quotes, tickInterval, ttl);

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!this.profileNames.contains(profile.getName())) {
            return result;
        }

        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final IntradayData intradayData = result.get(i);
            evaluate(quote, profile, intradayData.getPrice().getPriceQuality());
        }

        return result;
    }

    @Override
    public List<AggregatedTickImpl> getAggregatedTrades(Quote quote, DateTime start, DateTime end,
            Duration aggregation, TickType tickType) {
        final List<AggregatedTickImpl> result = delegate.getAggregatedTrades(quote, start, end,
                aggregation, tickType);

        return throughProfileEvaluation(quote, result);
    }

    public List<AggregatedTickImpl> getAggregatedTrades(Quote quote, List<Interval> intervals,
            Duration aggregation, TickType tickType) {
        final List<AggregatedTickImpl> result = delegate.getAggregatedTrades(quote, intervals, aggregation, tickType);
        return throughProfileEvaluation(quote, result);
    }

    @Override
    public List<AggregatedTickImpl> getAggregatedTrades(Quote quote, Interval interval,
            Duration aggregation, TickType tickType, int minTickNum, boolean alignWithStart) {
        final List<AggregatedTickImpl> result =
                delegate.getAggregatedTrades(quote, interval, aggregation, tickType, minTickNum, alignWithStart);
        return throughProfileEvaluation(quote, result);
    }

    @Override
    public List<AggregatedTickImpl> getAggregatedTrades4TradeScreen(Quote quote, Interval interval,
            Duration aggregation) {
        List<AggregatedTickImpl> result =
                delegate.getAggregatedTrades4TradeScreen(quote, interval, aggregation);
        return throughProfileEvaluation(quote, result);
    }

    private <T> T throughProfileEvaluation(Quote quote, T result) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!this.profileNames.contains(profile.getName())) {
            return result;
        }

        evaluate(quote, profile);

        return result;
    }

    private void evaluate(Quote q, Profile profile) {
        evaluate(q, profile, profile.getPriceQuality(q));
    }

    private void evaluate(Quote q, Profile profile, PriceQuality priceQuality) {
        if (priceQuality == PriceQuality.REALTIME) {
            this.intradayReportingDao.insertAccess(q, profile);
        }
    }
}
