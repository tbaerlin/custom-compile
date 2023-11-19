/*
 * IntradayProviderImpl.java
 *
 * Created on 07.07.2006 11:30:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInterval;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.OrderbookData;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.feed.api.FeedAndTickConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.api.VendorkeyListRequest;
import de.marketmaker.istar.feed.api.VendorkeyListResponse;
import de.marketmaker.istar.feed.history.AggregatedHistoryTickRecord;
import de.marketmaker.istar.feed.history.TickHistoryRequest;
import de.marketmaker.istar.feed.history.TickHistoryResponse;
import de.marketmaker.istar.feed.tick.AggregatedTickRecordImpl;
import de.marketmaker.istar.feed.tick.TickRecordImpl;
import de.marketmaker.istar.feed.vwd.EndOfDayFilter;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.util.IntradayUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class  IntradayProviderImpl implements IntradayProvider {

    private static final int[] LOW_FIELD_IDS = new int[]{
            VwdFieldDescription.ADF_52_W_Tief.id(),
            VwdFieldDescription.ADF_Jahrestief.id(),
            VwdFieldDescription.ADF_Brief_Tagestief.id(),
            VwdFieldDescription.ADF_Geld_Tagestief.id(),
            VwdFieldDescription.ADF_Tagestief.id(),
            // R-82468:  for IRSEUR.IIRSE.6Y the field above are undefined in the snap record
            VwdFieldDescription.ADF_Brief.id(),
            VwdFieldDescription.ADF_Geld.id(),
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Duration CACHE_AGGREGATION = new Duration(1000);

    private FeedAndTickConnector feedConnector;

    private EntitlementProvider entitlementProvider;

    private Ehcache aggregatedTickRecordCache;

    private Ehcache aggregatedTickRecordCacheCurrentDay;

    private Ehcache tickRecordCache;

    private OrderbookProvider orderbookProvider;

    public void setAggregatedTickRecordCacheCurrentDay(
            Ehcache aggregatedTickRecordCacheCurrentDay) {
        this.aggregatedTickRecordCacheCurrentDay = aggregatedTickRecordCacheCurrentDay;
    }

    public void setAggregatedTickRecordCache(Ehcache aggregatedTickRecordCache) {
        this.aggregatedTickRecordCache = aggregatedTickRecordCache;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "quoteid", description = "quoteid"),
            @ManagedOperationParameter(name = "date", description = "format: yyyyMMdd")
    })
    public String removeAggregatedTicksFromCache(long quoteid, int date) {
        if (this.aggregatedTickRecordCache == null) {
            return "cache not used";
        }
        final LocalDate ld = DateUtil.yyyyMmDdToLocalDate(date);
        final QuoteDp2 dummy = new QuoteDp2(quoteid);
        int n = 0;
        for (TickType tickType : TickType.values()) {
            if (this.aggregatedTickRecordCache.remove(getKey(dummy, ld, tickType, false))) {
                n++;
            }
        }
        return "removed " + n + " entries";
    }

    public void setTickRecordCache(Ehcache tickRecordCache) {
        this.tickRecordCache = tickRecordCache;
    }

    public void setIstarFeedConnector(FeedAndTickConnector istarFeedConnector) {
        this.feedConnector = istarFeedConnector;
    }

    public void setEntitlementProvider(EntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public PageResponse getPage(PageRequest request) {
        return this.feedConnector.getPage(request);
    }

    public VendorkeyListResponse getVendorkeys(VendorkeyListRequest request) {
        return this.feedConnector.getVendorkeys(request);
    }

    public TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request) {
        return feedConnector.getTypesForVwdcodes(request);
    }

    public void setOrderbookProvider(OrderbookProvider orderbookProvider) {
        this.orderbookProvider = orderbookProvider;
    }

    public OrderbookData getOrderbook(Quote quote) {
        return this.orderbookProvider.getOrderbook(quote);
    }

    public boolean isWithOrderbook(Quote quote) {
        return this.orderbookProvider.isWithOrderbook(quote);
    }

    public IntradayData getIntradayData(Quote q, Interval tickInterval) {
        return getIntradayData(Arrays.asList(q), tickInterval).get(0);
    }

    public List<IntradayData> getIntradayData(List<Quote> quotes, Interval tickInterval, int ttl) {
        if (quotes == null || quotes.isEmpty()) {
            return Collections.emptyList();
        }

        return new IntradayProviderMethod(this, quotes, tickInterval, ttl).invoke();
    }

    IntradayResponse getIntradayData(IntradayRequest ir, int ttl) {
        final String cacheKey = ttl > 0 ? getIntradayTicksCacheKey(ir) : null;

        IntradayResponse result = null;
        if (cacheKey != null) {
            final Element element = this.tickRecordCache.get(cacheKey);
            if (element != null) {
                //noinspection unchecked
                result = (IntradayResponse) element.getObjectValue();
            }
        }
        if (result == null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getIntradayData> >>>>>> " + ir);
            }
            result = this.feedConnector.getIntradayData(ir);
            if (cacheKey != null) {
                final Element element = new Element(cacheKey, result);
                element.setTimeToLive(ttl);
                this.tickRecordCache.put(element);
            }
        }
        return result;
    }

    private String getIntradayTicksCacheKey(IntradayRequest ir) {
        if (ir.size() > 1) {
            return null;
        }
        final IntradayRequest.Item item = ir.getItems().get(0);
        if (item.getTicksFrom() != item.getTicksTo() || item.getTicksFrom() == 0) {
            return null;
        }
        return item.toString();
    }

    public List<IntradayData> getIntradayData(List<Quote> quotes, Interval tickInterval) {
        return getIntradayData(quotes, tickInterval, 0);
    }

    private boolean isIntradayAllowed(Quote quote) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final PriceQuality priceQuality = profile.getPriceQuality(quote);
        return priceQuality == PriceQuality.DELAYED || priceQuality == PriceQuality.REALTIME;
    }

    ProfiledSnapRecordFactory getSnapRecordFactory(Quote q, Profile profile) {
        if (this.entitlementProvider == null) {
            return ProfiledSnapRecordFactory.IDENTITY;
        }

        final BitSet allowedFields = this.entitlementProvider.getAllowedFields(q, profile);
        final EndOfDayFilter eoDFilter = this.entitlementProvider.getEoDFilter(q, profile);
        final BitSet allowedEodFields = this.entitlementProvider.getAllowedEodFieldsByProfile(q, profile);
        return new ProfiledSnapRecordFactory(allowedFields, eoDFilter, allowedEodFields);
    }

    public List<PriceRecord> getPriceRecords(List<Quote> quotes) {
        final List<IntradayData> intradayData = getIntradayData(quotes, null);

        final List<PriceRecord> result = new ArrayList<>(intradayData.size());
        for (IntradayData data : intradayData) {
            result.add(data.getPrice());
        }

        return result;
    }

    public List<AggregatedTickImpl> getAggregatedTrades(Quote quote, DateTime from, DateTime to,
            Duration aggregation, TickType tickType) {
        if (!allowQuery(quote, tickType)) {
            return Collections.emptyList();
        }

        final DateTime midnight = new DateTime().plusDays(1).withTimeAtStartOfDay();
        final DateTime end = to.isAfter(midnight) ? midnight : to;

        if (IntradayUtil.canUseTickHistory(aggregation, tickType)) {
            return getAggregatedTrades(quote, DateUtil.toHistoryIntervals(from, end), aggregation, tickType);
        }
        else {
            return getAggregatedTrades(quote, DateUtil.getDailyIntervals(from, end), aggregation, tickType);
        }
    }

    private boolean allowQuery(Quote quote, TickType tickType) {
        return null != tickType  // no intraday ticks for YIELD or BID_ASK_TRADE
                && StringUtils.hasText(quote.getSymbolVwdfeed()) && isIntradayAllowed(quote);
    }

    @Override
    public List<AggregatedTickImpl> getAggregatedTrades(Quote quote, Interval interval,
            Duration aggregation, TickType tickType, int minTickNum, boolean alignWithStart) {
        if (!allowQuery(quote, tickType)) {
            return Collections.emptyList();
        }

        return retrieveAggregatedTrades(quote, interval, aggregation, tickType, minTickNum, alignWithStart);
    }

    @Override
    public List<AggregatedTickImpl> getAggregatedTrades4TradeScreen(Quote quote, Interval interval,
            Duration aggregation) {
        if (!allowQuery(quote, TickType.TRADE)) {
            return Collections.emptyList();
        }

        final DateTime now = new DateTime();
        if (interval.getStart().isAfter(now)) {
            return Collections.emptyList();
        }

        final IntradayData id = getIntradayData(quote, interval);
        final TickRecord tr = id.getTicks();

        if (tr == null || !(tr instanceof TickRecordImpl)) {
            return Collections.emptyList();
        }

        final TickRecordImpl trImpl = (TickRecordImpl) tr;
        if (hasNegativePrices(id.getSnap())) {
            trImpl.setAggregateOnlyPositivePrices(false);
        }

        if (trImpl.getLastTickDateTime() == null || interval.getEnd().isBefore(trImpl.getLastTickDateTime())) {
            trImpl.setLast(interval.getEnd(), NullSnapRecord.INSTANCE);
        }

        final Timeseries<AggregatedTick> timeseries = trImpl.aggregate(aggregation, TickType.TRADE).getTimeseries(interval);
        return toAggregatedTickList(TickType.TRADE, timeseries);
    }

    public List<AggregatedTickImpl> getAggregatedTrades(Quote quote, List<Interval> intervals,
            Duration aggregation, TickType tickType) {
        if (!allowQuery(quote, tickType)) {
            return Collections.emptyList();
        }

        final List<AggregatedTickImpl> result = new ArrayList<>(intervals.size());
        for (Interval interval : intervals) {
            result.addAll(retrieveAggregatedTrades(quote, interval, aggregation, tickType, 0, false));
        }
        return result;
    }

    private List<AggregatedTickImpl> retrieveAggregatedTrades(Quote quote, Interval interval,
            Duration aggregation, TickType tickType, int minTickNum, boolean alignWithStart) {
        final Timeseries<AggregatedTick> timeseries = getCachedAggregatedTickRecord(quote, interval,
                aggregation, tickType, minTickNum, alignWithStart);

        if (timeseries == null) {
            return Collections.emptyList();
        }

        return toAggregatedTickList(tickType, timeseries);
    }

    private List<AggregatedTickImpl> toAggregatedTickList(TickType tickType,
            Timeseries<AggregatedTick> timeseries) {
        final List<AggregatedTickImpl> trades = new ArrayList<>();
        for (final DataWithInterval<AggregatedTick> dwi : timeseries) {
            final AggregatedTick t = dwi.getData();
            trades.add(new AggregatedTickImpl(new Interval(t.getInterval()),
                    PriceCoder.decode(t.getOpen()), PriceCoder.decode(t.getHigh()),
                    PriceCoder.decode(t.getLow()), PriceCoder.decode(t.getClose()),
                    t.getVolume(), t.getNumberOfAggregatedTicks(), tickType));
        }

        return trades;
    }

    private Timeseries<AggregatedTick> getCachedAggregatedTickRecord(Quote quote, Interval interval,
            Duration aggregation, TickType tickType, int minTickNum, boolean alignWithStart) {
        final DateTime now = new DateTime();
        if (interval.getStart().isAfter(now)) {
            return null;
        }

        final boolean inToday = interval.overlaps(now.toLocalDate().toInterval());

        if (!inToday && IntradayUtil.canUseTickHistory(aggregation, tickType)) {
            final TickHistoryResponse tickHistoryResponse = fromTickHistory(quote, interval, aggregation,
                    tickType, minTickNum, alignWithStart);
            if (!tickHistoryResponse.isValid()) {
                return null; // trouble with tick history provider
            }
            final DateTime yesterdayStart = now.withTimeAtStartOfDay().minusDays(1);
            if (isYesterdayDataAskedButNotAnswered(interval, tickHistoryResponse, yesterdayStart)) {
                // ask intraday data for yesterday's data and return the merged result
                final LocalDate localDate = yesterdayStart.toLocalDate();
                final AggregatedTickRecord atr = getCachedIntradayDailyData(quote, localDate,
                        tickType, false);
                if (null != atr) {
                    final AggregatedHistoryTickRecord ahtr =
                            new AggregatedHistoryTickRecord(atr.getAggregation(), tickType);
                    ahtr.add(((AggregatedTickRecordImpl) atr).getItem(DateUtil.toYyyyMmDd(localDate)));
                    return tickHistoryResponse.getRecord().merge(
                            ahtr.aggregate(aggregation, localDate.toInterval())).getTimeseries(interval);
                }
            }
            // either tick history provider delivered all data, or after best effort from intraday provider
            return tickHistoryResponse.getRecord().getTimeseries(interval);
        }

        final LocalDate day = interval.getStart().toLocalDate();
        final AggregatedTickRecord atr = getCachedIntradayDailyData(quote, day, tickType, inToday);
        return null == atr ? null : getTickResult(atr, aggregation, interval);
    }

    private boolean isYesterdayDataAskedButNotAnswered(Interval reqInterval,
            TickHistoryResponse thResp, DateTime yesterdayStart) {
        if (null == thResp.getHistoryEnd()) {
            ReadableInterval respInterval = thResp.getRecord().getInterval();
            return reqInterval.getEnd().isAfter(yesterdayStart) &&
                    (null == respInterval || !respInterval.getEnd().isAfter(yesterdayStart));
        }
        return reqInterval.getEnd().isAfter(yesterdayStart)
                && !thResp.getHistoryEnd().isAfter(yesterdayStart);
    }

    private AggregatedTickRecord getCachedIntradayDailyData(Quote quote, LocalDate day,
            TickType tickType, boolean inToday) {
        final Ehcache cache = inToday
                ? this.aggregatedTickRecordCacheCurrentDay : this.aggregatedTickRecordCache;

        // use day instead of interval for key as tick data will always be requested for a whole day
        // and the aggregation will also cover the whole day. interval is only used to select a
        // subset of the aggregated ticks, but that is irrelevant for the cache.
        final String cacheKey = getKey(quote, day, tickType, inToday);

        if (cache != null) {
            final Element element = cache.get(cacheKey);
            if (element != null) {
                //noinspection unchecked
                return (AggregatedTickRecord) element.getObjectValue();
            }
        }

        final IntradayData id = getIntradayData(quote, day.toInterval());
        final TickRecord tr = id.getTicks();

        if (tr == null) {
            if (cache != null && !inToday) {
                final AggregatedTickRecordImpl empty
                        = new AggregatedTickRecordImpl(CACHE_AGGREGATION, tickType);
                cache.put(new Element(cacheKey, empty));
            }
            return null;
        }

        if (tr instanceof TickRecordImpl) {
            if (hasNegativePrices(id.getSnap())) {
                ((TickRecordImpl) tr).setAggregateOnlyPositivePrices(false);
            }
        }

        final AggregatedTickRecord atr = tr.aggregate(CACHE_AGGREGATION, tickType);

        if (cache != null) {
            final Element element = new Element(cacheKey, atr);
            cache.put(element);
        }

        return atr;
    }

    private TickHistoryResponse fromTickHistory(Quote quote, Interval interval,
            Duration aggregation, TickType tickType, int minTickNum, boolean alignWithStart) {
        final TickHistoryRequest req = new TickHistoryRequest(quote.getSymbolVwdfeed(),
                interval, aggregation, minTickNum, alignWithStart, tickType);
        return this.feedConnector.getTickHistory(req);
    }

    private Timeseries<AggregatedTick> getTickResult(AggregatedTickRecord atr,
            Duration aggregation, Interval interval) {
        if (CACHE_AGGREGATION.equals(aggregation)) {
            return atr.getTimeseries(interval);
        }
        return atr.aggregate(aggregation, interval).getTimeseries(interval);
    }

    private boolean hasNegativePrices(SnapRecord sr) {
        for (int fieldId : LOW_FIELD_IDS) {
            final SnapField field = sr.getField(fieldId);
            if (field.isDefined()) {
                final BigDecimal price = field.getPrice();
                if (price != null && price.signum() != 0) {
                    return price.signum() < 0;
                }
            }
        }
        return false;
    }

    private String getKey(Quote quote, LocalDate day, TickType tickType, boolean inToday) {
        final StringBuilder sb = new StringBuilder(40)
                .append(quote.getId() == 0 ? quote.getSymbolVwdfeed() : quote.getId())
                .append('.').append(day)
                .append('.').append(tickType.name());
        if (inToday) {
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            sb.append('.').append(profile.getPriceQuality(quote).name());
        }
        return sb.toString();
    }
}
