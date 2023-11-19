/*
 * ProviderMethod.java
 *
 * Created on 23.10.13 11:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.AggregatedTick;
import de.marketmaker.istar.domain.data.AggregatedTickRecord;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.data.PriceRecordImpl;
import de.marketmaker.istar.domainimpl.profile.EoDProfileAdapter;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.EntitlementProvider;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;

import static de.marketmaker.istar.domain.data.PriceQuality.END_OF_DAY;
import static de.marketmaker.istar.merger.context.RequestContextHolder.getRequestContext;
import static de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils.getLastDayThatCanBeRequested;
import static de.marketmaker.istar.merger.web.easytrade.block.MscEodMethod.DURATION_DAY;
import static de.marketmaker.istar.merger.web.easytrade.block.MscEodMethod.DURATION_MINUTE;

/**
 * @author zzhao
 */
public abstract class AbstractHistoricDataMethod {
    protected final Set<String> TICK_BASED_MARKETS
            = new HashSet<>(Arrays.asList("TPI", "TFI", "FXVWD", "FX", "FXX"));

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Map<String, Object> model = new HashMap<>();

    protected LocalDate baseDate;

    protected final Quote quote;

    protected final PriceRecord intradayPrice;

    protected final HistoricTimeseriesProvider provider;

    protected final MscHistoricData.Command cmd;

    private final IntradayProvider intradayProvider;

    private BitSet allowedFields;

    private final HistoricDataProfiler historicDataProfiler;

    protected AbstractHistoricDataMethod(HistoricTimeseriesProvider provider, Quote quote,
            MscHistoricData.Command cmd,
            final IntradayProvider intradayProvider, EntitlementProvider entitlementProvider) {
        this.quote = quote;
        this.provider = provider;
        this.cmd = cmd;
        this.allowedFields = (entitlementProvider != null)
                ? entitlementProvider.getAllowedFields(quote, getRequestContext().getProfile())
                : null;
        this.intradayProvider = intradayProvider;
        this.intradayPrice = getPriceRecord();
        this.historicDataProfiler = new HistoricDataProfiler();

        this.model.put("quote", quote);
        this.model.put("tickType", cmd.getTickType().name());
        this.model.put("type", cmd.getType());
        if (this.cmd.getAggregation() == null) {
            this.model.put("aggregation", "P1D");
        }
        else {
            this.model.put("aggregation", cmd.getAggregation().toString());
        }
        this.model.put("aggregated", "P1D".equals(this.model.get("aggregation")));
    }

    abstract Map<String, Object> invoke() throws IOException;

    public boolean isFieldAllowed(VwdFieldDescription.Field f) {
        return (this.allowedFields == null) || this.allowedFields.get(f.id());
    }

    protected PriceRecord getPriceRecordIntern() {
        if (this.quote.getQuotedef() != 2022) {
            return this.intradayPrice;
        }

        final DateTime dateTime = this.intradayPrice.getDate();
        if (dateTime == null) {
            return this.intradayPrice;
        }

        final Interval interval = dateTime.toLocalDate().toInterval();
        try {
            final IntradayData intradayData = getIntradayData(interval);
            final TickRecord tr = intradayData.getTicks();
            if (tr == null) {
                return this.intradayPrice;
            }

            final AggregatedTickRecord agg = tr.aggregate(DURATION_DAY, TickType.SYNTHETIC_TRADE);
            for (DataWithInterval<AggregatedTick> at : agg.getTimeseries(interval)) {
                if (at != null) {
                    final PriceRecordImpl priceRecord = new PriceRecordImpl();
                    final AggregatedTick tick = at.getData();
                    priceRecord.setOpen(new PriceImpl(PriceCoder.decode(tick.getOpen()),
                            null, null, dateTime, END_OF_DAY));
                    priceRecord.setHighDay(new PriceImpl(PriceCoder.decode(tick.getHigh()),
                            null, null, dateTime, END_OF_DAY));
                    priceRecord.setLowDay(new PriceImpl(PriceCoder.decode(tick.getLow()),
                            null, null, dateTime, END_OF_DAY));
                    priceRecord.setClose(new PriceImpl(PriceCoder.decode(tick.getClose()),
                            null, null, dateTime, END_OF_DAY));
                    priceRecord.setDate(dateTime);
                    priceRecord.setPrice(priceRecord.getClose());
                    priceRecord.setDate(dateTime);
                    return priceRecord;
                }
            }
        } catch (Exception e) {
            this.logger.warn("<getPriceRecordIntern> failed", e);
        }
        return this.intradayPrice;
    }

    protected Interval getInterval(PriceRecord pr) {
        final DateTime date = pr.getPrice().getDate();
        final LocalDate ld = (date == null) ? new LocalDate().minusDays(1) : date.toLocalDate();

        // TODO: HACK to get previous day data shortly after midnight for realtime entitled quotes like *.FXVWD for risk service
        final LocalDate result = new LocalTime().getHourOfDay() < 6 && ld.equals(new LocalDate())
                ? ld.minusDays(1)
                : ld;

        return result.toInterval();
    }

    protected Price getFirst(AggregatedTickRecord tr, Interval interval) {
        final Iterator<DataWithInterval<AggregatedTick>> it = tr.getTimeseries(interval).iterator();
        if (!it.hasNext()) {
            return NullPrice.INSTANCE;
        }
        final DataWithInterval<AggregatedTick> first = it.next();
        return new PriceImpl(PriceCoder.decode(first.getData().getOpen()), null, null,
                first.getInterval().getStart(), END_OF_DAY);
    }

    protected Price getLast(AggregatedTickRecord tr, Interval interval) {
        BigDecimal last = null;
        DateTime dt = null;
        for (final DataWithInterval<AggregatedTick> dwi : tr.getTimeseries(interval)) {
            // use dwi directly as otherwise the internal rawTick is reused which results in wrong data at the end
            last = PriceCoder.decode(dwi.getData().getClose());
            dt = dwi.getInterval().getEnd();
        }
        return getPrice(last, dt);
    }

    protected Price getHigh(AggregatedTickRecord tr, Interval interval) {
        BigDecimal high = null;
        DateTime dt = null;
        for (final DataWithInterval<AggregatedTick> dwi : tr.getTimeseries(interval)) {
            BigDecimal tmp = PriceCoder.decode(dwi.getData().getHigh());
            if (high == null || tmp.compareTo(high) > 0) {
                high = tmp;
                dt = dwi.getInterval().getEnd();
            }
        }
        return getPrice(high, dt);
    }

    protected Price getLow(AggregatedTickRecord tr, Interval interval) {
        BigDecimal low = null;
        DateTime dt = null;
        for (final DataWithInterval<AggregatedTick> dwi : tr.getTimeseries(interval)) {
            BigDecimal tmp = PriceCoder.decode(dwi.getData().getLow());
            if (low == null || tmp.compareTo(low) < 0) {
                low = tmp;
                dt = dwi.getInterval().getEnd();
            }
        }
        return getPrice(low, dt);
    }

    private Price getPrice(BigDecimal bd, DateTime dt) {
        if (bd == null) {
            return NullPrice.INSTANCE;
        }
        // minus(1) as end is exclusive, relevant for records with ticks in the last minute of day
        return new PriceImpl(bd, null, null, dt.minus(1), END_OF_DAY);
    }

    protected LocalDate getStartDay() {
        final DateTime cmdStart = cmd.getStart();
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Entitlement entitlement = this.historicDataProfiler.getEntitlement(profile, this.quote);

        final LocalDate start;
        final Optional<DateTime> restrictedStart = entitlement.getStart();

        if (restrictedStart.isPresent() && restrictedStart.get().isAfter(cmdStart)) {
            start = restrictedStart.get().toLocalDate();
        } else {
            start = cmdStart.toLocalDate();
        }

        if (quote.getFirstHistoricPriceYyyymmdd() > 0) {
            final LocalDate first = DateUtil.yyyyMmDdToLocalDate(quote.getFirstHistoricPriceYyyymmdd());
            if (first.isAfter(start)) {
                return first;
            }
        }
        return start;
    }

    protected LocalDate getEndDay() {
        return DateUtil.min(cmd.getEnd().toLocalDate(), getLastDayThatCanBeRequested(this.intradayPrice));
    }

    protected PriceRecord getIntradayPriceRecord() {
        try {
            return doGetIntradayPriceRecord();
        } catch (Exception e) {
            logger.warn("<getIntradayPriceRecord> failed", e);
        }
        return null;
    }

    protected boolean isTickDataElementsAllowed() {
        final TickImpl.Type tickType = cmd.getTickType();
        final TickDataCommand.ElementDataType elementType = cmd.getType();

        if (!Arrays.asList(TickImpl.Type.TRADE, TickImpl.Type.BID, TickImpl.Type.ASK).contains(tickType)) {
            return true;
        }
        switch (elementType) {
            case PERFORMANCE:
            case CLOSE:
                return isFieldAllowed(VwdFieldDescription.ADF_Schluss);
            case OHLC:
                return isFieldAllowed(VwdFieldDescription.ADF_Anfang)
                        && isFieldAllowed(VwdFieldDescription.ADF_Tageshoch)
                        && isFieldAllowed(VwdFieldDescription.ADF_Tagestief)
                        && isFieldAllowed(VwdFieldDescription.ADF_Schluss);
            case OHLCV:
                return isFieldAllowed(VwdFieldDescription.ADF_Anfang)
                        && isFieldAllowed(VwdFieldDescription.ADF_Tageshoch)
                        && isFieldAllowed(VwdFieldDescription.ADF_Tagestief)
                        && isFieldAllowed(VwdFieldDescription.ADF_Schluss)
                        && isFieldAllowed(VwdFieldDescription.ADF_Umsatz_gesamt);
            case FUND:
                return isFieldAllowed(VwdFieldDescription.ADF_Ausgabe)
                        && isFieldAllowed(VwdFieldDescription.ADF_Ruecknahme)
                        && isFieldAllowed(VwdFieldDescription.ADF_NAV)
                        && isFieldAllowed(VwdFieldDescription.ADF_Umsatz_gesamt);
            case VOLUME_AGGREGATION:
                return isFieldAllowed(VwdFieldDescription.ADF_Umsatz_Gesamt_Call)
                        && isFieldAllowed(VwdFieldDescription.ADF_Umsatz_Gesamt_Put)
                        && isFieldAllowed(VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Call)
                        && isFieldAllowed(VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Put)
                        && isFieldAllowed(VwdFieldDescription.ADF_Umsatz_Gesamt_Futures)
                        && isFieldAllowed(VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Fut);
            default:
                return true;
        }
    }

    private PriceRecord doGetIntradayPriceRecord() {
        if (this.cmd.isFillNightlyGapWithChicagoData()) {
            return doGetEoDIntradayPriceRecord();
        }
        if (this.cmd.isWithIntraday()) {
            return getPriceRecordIntern();
        }
        return null;
    }

    /**
     * Get EoD style intraday data from chicago to fill historic time series w/ yesterday's data
     * to have full time series directly after midnight.
     *
     * @return EoD style price record
     */
    private PriceRecord doGetEoDIntradayPriceRecord() {
        final Profile profile = getRequestContext().getProfile();

        final PriceRecord pr;
        try {
            // TODO: HACK to get previous day data shortly after midnight for realtime entitled quotes like *.FXVWD for risk service
            pr = RequestContextHolder.callWith(new EoDProfileAdapter().adapt(profile),
                                               this::getPriceRecord);
        } catch (Exception e) {
            this.logger.warn("<getIntradayPriceRecord> failed", e);
            return null;
        }


        if (!TICK_BASED_MARKETS.contains(this.quote.getSymbolVwdfeedMarket())) {
            return pr;
        }

        getRequestContext().clearIntradayContext(); // clear to remove (possible) EOD record from above

        final Interval interval = getInterval(pr);

        final IntradayData intradayData;
        try {
            intradayData = RequestContextHolder.callWith(ProfileFactory.valueOf(true), // get ticks w/ realtime profile as EoD returns no ticks
                                                         () -> getIntradayData(interval));
        } catch (Exception e) {
            this.logger.warn("<getIntradayPriceRecord> failed", e);
            return pr;
        }

        final TickRecord tr = intradayData.getTicks();
        if (tr == null) {
            return pr;
        }

        final AggregatedTickRecord attr = tr.aggregate(DURATION_MINUTE, TickType.TRADE);
        final AggregatedTickRecord abtr = tr.aggregate(DURATION_MINUTE, TickType.BID);
        final AggregatedTickRecord aatr = tr.aggregate(DURATION_MINUTE, TickType.ASK);
        final PriceRecordImpl result = new PriceRecordImpl();
        final Price last = getLast(attr, interval);
        final Price bid = getLast(abtr, interval);
        final Price ask = getLast(aatr, interval);
        result.setPrice(last);
        result.setDate(DateUtil.max(DateUtil.max(last.getDate(), bid.getDate()), ask.getDate()));
        result.setClose(last);
        result.setBid(bid);
        result.setAsk(ask);
        result.setOpen(getFirst(attr, interval));
        result.setHighDay(getHigh(attr, interval));
        result.setLowDay(getLow(attr, interval));
        return result;
    }

    private IntradayData getIntradayData(Interval interval) {
        return this.intradayProvider.getIntradayData(quote, interval);
    }

    private PriceRecord getPriceRecord() {
        return getIntradayData(null).getPrice();
    }
}
