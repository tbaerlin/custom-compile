/*
 * MscHistoricDataMethod.java
 *
 * Created on 12.08.2009 13:46:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.protobuf.AggregatedTickSerializer;
import de.marketmaker.istar.domainimpl.protobuf.FundPriceTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.PerformanceTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.VolumeAggregationTimeseriesSerializer;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesProvider;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesRequest;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscHistoricDataMethod extends AbstractHistoricDataMethod {

    public MscHistoricDataMethod(MscHistoricData historicData, Quote quote,
            MscHistoricData.Command cmd) {
        super(historicData.getHistoricTimeseriesProvider(), quote, cmd, historicData.getIntradayProvider(),
                historicData.getEntitlementProvider());
    }

    public MscHistoricDataMethod(HistoricTimeseriesProvider timeseriesProvider, Quote quote,
            MscHistoricData.Command cmd, IntradayProvider intradayProvider) {
        super(timeseriesProvider, quote, cmd, intradayProvider, null);
    }

    @Override
    public Map<String, Object> invoke() throws IOException {
        final HistoricTimeseriesRequest historicTimeseriesRequest = createRequest();
        final List<HistoricTimeseries> historicTimeseriesList = this.provider.getTimeseries(historicTimeseriesRequest);

        final ArrayList<AggregatedTickImpl> myTicks = new ArrayList<>();
        final String modelKey = retrieveTicks(historicTimeseriesList, myTicks);
        if (myTicks.size() > 1 && StringUtils.hasText(this.cmd.getOutlierRule())) {
            final List<Outlier> outliers = searchOutlier(this.cmd.getOutlierRule(),
                    this.cmd.getType(), myTicks);
            if (!outliers.isEmpty()) {
                model.put("outliers", outliers);
            }
        }
        addTicks(myTicks, modelKey);

        return this.model;
    }

    private void addTicks(ArrayList<AggregatedTickImpl> myTicks, String modelKey)
            throws IOException {
        switch (this.cmd.getFormat()) {
            case XML:
                model.put(modelKey, myTicks);
                break;
            case PROTOBUF:
                AggregatedTickSerializer serializer = null;
                switch (this.cmd.getType()) {
                    case VOLUME_AGGREGATION:
                        serializer = new VolumeAggregationTimeseriesSerializer();
                        break;
                    case FUND:
                        serializer = new FundPriceTimeseriesSerializer();
                        break;
                    case PERFORMANCE:
                        serializer = new PerformanceTimeseriesSerializer();
                        break;
                }

                if (null != serializer) {
                    final ByteBuffer bb = serializer.serialize(myTicks);
                    MscTickData.addToModel(this.model, bb, serializer.getNumObjects());
                }
                else {
                    MscTickData.addToModel(this.model, myTicks, this.cmd.getType());
                }
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + this.cmd.getFormat());
        }
    }

    static List<Outlier> searchOutlier(String outlierRule, TickDataCommand.ElementDataType type,
            List<AggregatedTickImpl> myTicks) {
        switch (type) {
            case OHLC:
            case OHLCV:
                return searchOutlier(outlierRule, myTicks,
                        EntryTypeEnum.open,
                        EntryTypeEnum.high,
                        EntryTypeEnum.low,
                        EntryTypeEnum.close);
            case CLOSE:
                return searchOutlier(outlierRule, myTicks, EntryTypeEnum.close);
            case PERFORMANCE:
                return searchOutlier(outlierRule, myTicks, EntryTypeEnum.value);
            case FUND:
                return searchOutlier(outlierRule, myTicks,
                        EntryTypeEnum.issuePrice,
                        EntryTypeEnum.repurchasePrice);
            default:
                return Collections.emptyList();
        }
    }

    private static List<Outlier> searchOutlier(String outlierRule, List<AggregatedTickImpl> myTicks,
            EntryTypeEnum... entryTypes) {
        if (null == entryTypes || entryTypes.length == 0) {
            return Collections.emptyList();
        }

        if ("5-sigma".equals(outlierRule)) {
            return searchOutlier5Sigma(myTicks, entryTypes);
        }
        else {
            throw new UnsupportedOperationException("no support for outlier rule: " + outlierRule);
        }
    }

    private static List<Outlier> searchOutlier5Sigma(List<AggregatedTickImpl> myTicks,
            EntryTypeEnum[] entryTypes) {
        final Double[] sigma5 = calculate5Sigma(myTicks, entryTypes);
        final ArrayList<Outlier> ret = new ArrayList<>();
        final EnumSet<EntryTypeEnum> set = EnumSet.noneOf(EntryTypeEnum.class);

        // take the predecessor as pivot for absolute deviation
        for (int i = 1; i < myTicks.size(); i++) {
            set.clear();
            for (int j = 0; j < entryTypes.length; j++) {
                final BigDecimal value = getValue(myTicks.get(i), entryTypes[j]);
                final BigDecimal valueV = getValue(myTicks.get(i - 1), entryTypes[j]);
                if (null != sigma5[j] && isOutlier(value, valueV, sigma5[j])) {
                    set.add(entryTypes[j]);
                }
            }
            if (!set.isEmpty()) {
                ret.add(new Outlier(i, set));
            }
        }

        return ret;
    }

    private static Double[] calculate5Sigma(List<AggregatedTickImpl> myTicks,
            EntryTypeEnum[] entryTypes) {
        final StandardDeviation[] sds = new StandardDeviation[entryTypes.length];
        for (int i = 0; i < sds.length; i++) {
            sds[i] = new StandardDeviation();
        }

        for (AggregatedTickImpl myTick : myTicks) {
            for (int i = 0; i < entryTypes.length; i++) {
                final BigDecimal dv = getValue(myTick, entryTypes[i]);
                if (null != dv) {
                    sds[i].increment(dv.doubleValue());
                }
            }
        }

        final Double[] sigma5 = new Double[entryTypes.length];
        for (int i = 0; i < entryTypes.length; i++) {
            final double sigma = sds[i].getResult();
            if (isValidStat(sigma)) {
                sigma5[i] = 5 * sigma;
            }
        }
        return sigma5;
    }

    private static BigDecimal getValue(AggregatedTickImpl myTick, EntryTypeEnum entryType) {
        switch (entryType) {
            case open:
                return myTick.getOpen();
            case high:
            case issuePrice:
                return myTick.getHigh();
            case low:
                return myTick.getLow();
            case close:
            case repurchasePrice:
            case value:
                return myTick.getClose();
            default:
                throw new UnsupportedOperationException("no support for: " + entryType);
        }
    }

    private static boolean isOutlier(BigDecimal val, BigDecimal pivot, double sigma5) {
        return null != val && null != pivot
                && Math.abs(val.doubleValue() - pivot.doubleValue()) > sigma5;
    }

    private static boolean isValidStat(double val) {
        return !(Double.isInfinite(val) || Double.isNaN(val));
    }

    public enum EntryTypeEnum {
        open, high, low, close, issuePrice, repurchasePrice, value
    }

    public static class Outlier {
        private final int index;

        private final Set<EntryTypeEnum> entryTypes;

        public Outlier(int index, EnumSet<EntryTypeEnum> entryTypes) {
            this.index = index;
            this.entryTypes = EnumSet.copyOf(entryTypes);
        }

        public int getIndex() {
            return index;
        }

        public Set<EntryTypeEnum> getEntryTypes() {
            return EnumSet.copyOf(this.entryTypes);
        }
    }

    private String retrieveTicks(List<HistoricTimeseries> historicTimeseriesList,
            ArrayList<AggregatedTickImpl> myTicks) {
        switch (this.cmd.getType()) {
            case VOLUME_AGGREGATION:
                if (!"XEQDV".equals(this.quote.getSymbolVwdfeedMarket())) {
                    throw new BadRequestException("no volume aggregation data for a market != XEQDV");
                }
                transformInto(getVolumeAggregation(historicTimeseriesList), cmd.getNumTrades(), myTicks);
                return "volumes";
            case FUND:
                transformInto(getFund(historicTimeseriesList), cmd.getNumTrades(), myTicks);
                return "fundTs";
            case PERFORMANCE:
                final List<AggregatedTickImpl> trades = getTrades(historicTimeseriesList);
                if (!trades.isEmpty()) {
                    final BigDecimal first = trades.get(0).getClose();
                    myTicks.addAll(
                            trades.stream()
                                    .map(
                                            trade -> new AggregatedTickImpl(trade.getInterval(),
                                                    null,
                                                    null,
                                                    null,
                                                    trade.getClose().divide(first, Constants.MC),
                                                    null,
                                                    1,
                                                    TickType.TRADE)
                                    )
                                    .collect(Collectors.toList()));
                }
                return "trades";
            default:
                transformInto(getTrades(historicTimeseriesList), cmd.getNumTrades(), myTicks);
                return "trades";
        }
    }

    private <V> void transformInto(List<V> list, int numTrades, List<V> intoList) {
        if (numTrades == 0 || list.size() <= numTrades) {
            intoList.addAll(list);
        }
        else {
            final int count = list.size();
            final int start = Math.max(0, count - numTrades);
            intoList.addAll(list.subList(start, count));
        }
    }

    private HistoricTimeseriesRequest createRequest() {
        if (!isHistoricFieldsAllowed()) {
            throw new PermissionDeniedException("historic data not allowed");
        }
        final HistoricTimeseriesRequest historicTimeseriesRequest =
                new HistoricTimeseriesRequest(this.quote, getStartDay(), getEndDay())
                        .withCurrency(this.cmd.getCurrency())
                        .withSplit(this.cmd.isBlendCorporateActions())
                        .withDividend(this.cmd.isBlendDividends())
                        .withAggregationPeriod(this.cmd.getAggregation())
                        .withAlignedStart(this.cmd.isAlignStartWithAggregationPeriod())
                        .withAlignedEnd(this.cmd.isAlignEndWithAggregationPeriod())
                        .withPriceRecord(getIntradayPriceRecord())
                        .withSingleDayData(this.cmd.getNumTrades() == 1);

        if (this.cmd.isInferTickType() && this.cmd.getTickType() == TickImpl.Type.TRADE) {
            historicTimeseriesRequest.withYieldBasedFromQuote();
        }

        if (this.cmd.isEndAsReferenceDate()) {
            this.baseDate = historicTimeseriesRequest.getTo();
        }

        if (this.cmd.getTickType() == TickImpl.Type.YIELD) {
            historicTimeseriesRequest.withSpecialBaseType(TickImpl.Type.YIELD);
        }
        if (this.cmd.getTickType() == TickImpl.Type.SETTLEMENT) {
            historicTimeseriesRequest.withSpecialBaseType(TickImpl.Type.SETTLEMENT);
        }

        switch (cmd.getType()) {
            case PERFORMANCE:
            case CLOSE:
                addClose(historicTimeseriesRequest);
                break;
            case OHLC:
                addClose(historicTimeseriesRequest);
                addOHL(historicTimeseriesRequest);
                break;
            case OHLCV:
                addClose(historicTimeseriesRequest);
                addOHL(historicTimeseriesRequest);
                addVolume(historicTimeseriesRequest);
                break;
            case FUND:
                addFund(historicTimeseriesRequest);
                addVolume(historicTimeseriesRequest);
                break;
            case VOLUME_AGGREGATION:
                addVolumeAggregation(historicTimeseriesRequest);
                break;
        }
        return historicTimeseriesRequest;
    }

    private boolean isHistoricFieldsAllowed() {
        if (this.quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.IMO) {
            return isFieldAllowed(VwdFieldDescription.ADF_Avg_Price_per_SquareMetre);
        }
        // we don't know which fields are used to create the pm timeseries, so we test for some
        // of the more obvious ones; the MscEodMethod should improve this a lot.
        // this, however, is enough to solve R-71240 (deny access to OHLCV if only Screener is allowed)
        return isFieldAllowed(VwdFieldDescription.ADF_Schluss)
                || isFieldAllowed(VwdFieldDescription.ADF_Schluss_Vortag)
                || isFieldAllowed(VwdFieldDescription.ADF_Bezahlt)
                || isFieldAllowed(VwdFieldDescription.ADF_Anfang)
                || isFieldAllowed(VwdFieldDescription.ADF_Tageshoch)
                || isFieldAllowed(VwdFieldDescription.ADF_Tagestief)
                || isFieldAllowed(VwdFieldDescription.ADF_NAV)
                || isFieldAllowed(VwdFieldDescription.ADF_Ruecknahme)
                || isFieldAllowed(VwdFieldDescription.ADF_Ausgabe)
                || isFieldAllowed(VwdFieldDescription.ADF_Rendite)
                || isFieldAllowed(VwdFieldDescription.ADF_Rendite_ISMA)
                || isFieldAllowed(VwdFieldDescription.ADF_Settlement)
                || isFieldAllowed(VwdFieldDescription.ADF_Umsatz_gesamt)
                || isFieldAllowed(VwdFieldDescription.ADF_Geld)
                || isFieldAllowed(VwdFieldDescription.ADF_Brief)
                ;
    }

    private void addClose(HistoricTimeseriesRequest historicTimeseriesRequest) {
        historicTimeseriesRequest.addClose(this.baseDate);
    }

    private void addFund(HistoricTimeseriesRequest historicTimeseriesRequest) {
        historicTimeseriesRequest.addFundRepurchaingPrice(this.baseDate);
        historicTimeseriesRequest.addFundIssuePrice(this.baseDate);
    }

    private void addOHL(HistoricTimeseriesRequest historicTimeseriesRequest) {
        historicTimeseriesRequest.addOpen(this.baseDate);
        historicTimeseriesRequest.addHigh(this.baseDate);
        historicTimeseriesRequest.addLow(this.baseDate);
    }

    private void addVolumeAggregation(HistoricTimeseriesRequest historicTimeseriesRequest) {
        historicTimeseriesRequest.addClose(this.baseDate);
        historicTimeseriesRequest.addHigh(this.baseDate);
        historicTimeseriesRequest.addKontrakt(this.baseDate);
        historicTimeseriesRequest.addLow(this.baseDate);
        historicTimeseriesRequest.addOpen(this.baseDate);
        historicTimeseriesRequest.addVolume(this.baseDate);
    }

    private void addVolume(HistoricTimeseriesRequest historicTimeseriesRequest) {
        historicTimeseriesRequest.addVolume(this.baseDate);
        historicTimeseriesRequest.addKontrakt(this.baseDate);
    }

    private HistoricTimeseries getTs(int i, List<HistoricTimeseries> historicTimeseriesList) {
        if (i >= historicTimeseriesList.size()) {
            return null;
        }
        return historicTimeseriesList.get(i);
    }

    private List<AggregatedTickImpl> getFund(List<HistoricTimeseries> historicTimeseriesList) {
        final HistoricTimeseries ts = historicTimeseriesList.get(0);

        if (ts == null) {
            return Collections.emptyList();
        }

        final HistoricTimeseries issueTs = getTs(1, historicTimeseriesList);
        final HistoricTimeseries volumeTs = getTs(2, historicTimeseriesList);

        final List<AggregatedTickImpl> trades = new ArrayList<>();
        final int size = getSize(historicTimeseriesList);

        int i = 0;
        int j = getNextDefinedPosition(historicTimeseriesList, i, size);
        LocalDate day = ts.getStartDay().plusDays(j - i);

        while (j >= 0) {
            trades.add(new AggregatedTickImpl(day.toInterval(), null,
                    getPrice(issueTs, j), null, getPrice(ts, j), getLong(volumeTs, j), 0, TickType.TRADE));
            i = j;
            j = getNextDefinedPosition(historicTimeseriesList, j + 1, size);
            day = day.plusDays(j - i);
        }

        return trades;
    }

    private List<AggregatedTickImpl> getTrades(List<HistoricTimeseries> historicTimeseriesList) {
        final HistoricTimeseries closeTs = historicTimeseriesList.get(0);

        if (closeTs == null) {
            return Collections.emptyList();
        }

        final HistoricTimeseries openTs = getTs(1, historicTimeseriesList);
        final HistoricTimeseries highTs = getTs(2, historicTimeseriesList);
        final HistoricTimeseries lowTs = getTs(3, historicTimeseriesList);
        final HistoricTimeseries volumeTs = getTs(4, historicTimeseriesList);
        final HistoricTimeseries countTs = getTs(5, historicTimeseriesList);

        final List<AggregatedTickImpl> trades = new ArrayList<>();
        final int size = getSize(historicTimeseriesList);

        int i = 0;
        int j = getNextDefinedPosition(historicTimeseriesList, i, size);
        LocalDate day = closeTs.getStartDay().plusDays(j - i);

        while (j >= 0) {
            final Long count = getLong(countTs, j);
            trades.add(
                    new AggregatedTickImpl(
                            day.toInterval(),
                            getPrice(openTs, j),
                            getPrice(highTs, j),
                            getPrice(lowTs, j),
                            getPrice(closeTs, j),
                            getLong(volumeTs, j),
                            count == null ? 0 : count.intValue(),
                            TickType.TRADE
                    )
            );
            i = j;
            j = getNextDefinedPosition(historicTimeseriesList, j + 1, size);
            day = day.plusDays(j - i);
        }

        return trades;
    }

    private List<AggregatedTickImpl> getVolumeAggregation(List<HistoricTimeseries> historicTimeseriesList) {
        final HistoricTimeseries volumePutTs = historicTimeseriesList.get(0);

        if (volumePutTs == null) {
            return Collections.emptyList();
        }

        final HistoricTimeseries volumeFuturesTs = getTs(1, historicTimeseriesList);
        final HistoricTimeseries volumeBlockPutTs = getTs(2, historicTimeseriesList);
        final HistoricTimeseries volumeBlockCallTs = getTs(3, historicTimeseriesList);
        final HistoricTimeseries volumeCallTs = getTs(4, historicTimeseriesList);
        final HistoricTimeseries volumeBlockFuturesTs = getTs(5, historicTimeseriesList);

        final List<AggregatedTickImpl> volumes = new ArrayList<>();
        final int size = getSize(historicTimeseriesList);

        int i = 0;
        int j = getNextDefinedPosition(historicTimeseriesList, i, size);
        LocalDate day = volumePutTs.getStartDay().plusDays(j - i);

        while (j >= 0) {
            final Long count = getLong(volumeBlockPutTs, j);
            // HACK: reuse AggregatedTickImpl since values fit
            volumes.add(
                    new AggregatedTickImpl(
                            day.toInterval(),
                            getPrice(volumeCallTs, j),
                            getPrice(volumeFuturesTs, j),
                            getPrice(volumeBlockCallTs, j),
                            getPrice(volumePutTs, j),
                            getLong(volumeBlockFuturesTs, j),
                            count == null ? 0 : count.intValue(),
                            TickType.TRADE
                    )
            );
            i = j;
            j = getNextDefinedPosition(historicTimeseriesList, j + 1, size);
            day = day.plusDays(j - i);
        }

        return volumes;
    }

    private int getSize(List<HistoricTimeseries> historicTimeseriesList) {
        int ret = 0;
        for (HistoricTimeseries lt : historicTimeseriesList) {
            ret = Math.max(ret, null == lt ? 0 : lt.size());
        }
        return ret;
    }

    private int getNextDefinedPosition(List<HistoricTimeseries> historicTimeseriesList, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            for (HistoricTimeseries lt : historicTimeseriesList) {
                if (null != lt && !Double.isNaN(lt.getValue(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    private BigDecimal getPrice(HistoricTimeseries timeseries, int index) {
        if (timeseries == null || index >= timeseries.size()) {
            return null;
        }
        final double value = timeseries.getValue(index);
        return Double.isNaN(value) ? null : BigDecimal.valueOf(value);
    }

    private Long getLong(HistoricTimeseries timeseries, int index) {
        if (timeseries == null || index >= timeseries.size()) {
            return null;
        }
        final double value = timeseries.getValue(index);
        return Double.isNaN(value) ? null : (long) value;
    }
}
