/*
 * MscEodMethod.java
 *
 * Created on 12.08.2009 13:46:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.featureflags.FeatureFlags;
import de.marketmaker.istar.common.featureflags.FeatureFlags.Flag;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.AggregatedValue;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.protobuf.AggregatedTickSerializer;
import de.marketmaker.istar.domainimpl.protobuf.FundPriceTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.OhlcvPriceTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.PerformanceTimeseriesSerializer;
import de.marketmaker.istar.domainimpl.protobuf.VolumeAggregationTimeseriesSerializer;
import de.marketmaker.istar.merger.provider.historic.HistoricRequestImpl;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils;
import de.marketmaker.istar.merger.provider.historic.data.AggregatedValueSupport;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscEodMethod extends AbstractHistoricDataMethod {

    static final String NO_PERMISSION = "historic data not allowed";

    static final Duration DURATION_MINUTE = new Duration(DateTimeConstants.MILLIS_PER_MINUTE);

    static final Duration DURATION_DAY = new Duration(DateTimeConstants.MILLIS_PER_DAY);

    private final AggregatedValueSupport priceSupport;

    // todo use baseDate for calculation of factor and dividend, not used yet

    public MscEodMethod(MscHistoricData hd, Quote quote,
            MscHistoricData.Command cmd) {
        super(hd.getHistoricTimeseriesProviderEod(), quote, cmd, hd.getIntradayProvider(), hd.getEntitlementProvider());
        this.priceSupport = new AggregatedValueSupport(cmd.getType(), cmd.getBaseField(),
                cmd.getTickType(), cmd.getField());
    }

    public Map<String, Object> invoke() throws IOException {
        final HistoricRequestImpl htr = createRequest();
        final List<HistoricTimeseries> lts = this.provider.getTimeseries(htr);

        final List<AggregatedValue> myTicks = transformInto(this.priceSupport.extractPrices(lts),
                this.cmd.getNumTrades());
        final String modelKey = this.priceSupport.getModelKey();
        if (myTicks.size() > 1 && StringUtils.isNotBlank(this.cmd.getOutlierRule())) {
            final List<HistoricTimeseriesUtils.Outlier> outliers =
                    HistoricTimeseriesUtils.searchOutlier(this.cmd.getOutlierRule(),
                            this.cmd.getType(), myTicks);
            if (!outliers.isEmpty()) {
                model.put("outliers", outliers);
            }
        }
        addTicks(myTicks, modelKey);

        return this.model;
    }

    private void addTicks(List<AggregatedValue> myTicks, String modelKey) throws IOException {
        switch (this.cmd.getFormat()) {
            case XML:
                model.put(modelKey, myTicks);
                break;
            case PROTOBUF:
                final AggregatedTickSerializer serializer = getAggregatedTickSerializer();
                final ByteBuffer bb = serializer.serializeValues(myTicks);
                MscTickData.addToModel(this.model, bb, serializer.getNumObjects());
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + this.cmd.getFormat());
        }
    }

    private AggregatedTickSerializer getAggregatedTickSerializer() {
        switch (this.cmd.getType()) {
            case VOLUME_AGGREGATION:
                return new VolumeAggregationTimeseriesSerializer();
            case FUND:
                return new FundPriceTimeseriesSerializer();
            case PERFORMANCE:
                return new PerformanceTimeseriesSerializer();
            default:
                return new OhlcvPriceTimeseriesSerializer();
        }
    }

    private <V> List<V> transformInto(List<V> list, int numTrades) {
        if (numTrades == 0 || list.size() <= numTrades) {
            return list;
        }
        final int count = list.size();
        final int start = Math.max(0, count - numTrades);
        return list.subList(start, count);
    }

    private HistoricRequestImpl createRequest() {
        if (!isTickDataElementsAllowed()) {
            this.logger.info("<createRequest> {}", NO_PERMISSION);

            if (FeatureFlags.isEnabled(Flag.HISTORIC_DATA_PERMISSION_CHECK)) {
                throw new PermissionDeniedException(NO_PERMISSION);
            }
        }

        final HistoricRequestImpl req = new HistoricRequestImpl(this.quote, getStartDay(), getEndDay())
                .withCurrency(this.cmd.getCurrency())
                .withSplit(this.cmd.isBlendCorporateActions())
                .withDividend(this.cmd.isBlendDividends())
                .withAggregationPeriod(this.cmd.getAggregation())
                .withAlignedStart(this.cmd.isAlignStartWithAggregationPeriod())
                .withAlignedEnd(this.cmd.isAlignEndWithAggregationPeriod())
                .withPriceRecord(getIntradayPriceRecord()); // todo need more specific price fields?

        if (this.cmd.isEndAsReferenceDate()) {
            this.baseDate = req.getTo();
            req.withBaseDate(this.baseDate);
        }
        else {
            req.withCorporateActionReferenceDate(new LocalDate());
        }

        this.priceSupport.addHistoricTerms(req);
        return req;
    }
}
