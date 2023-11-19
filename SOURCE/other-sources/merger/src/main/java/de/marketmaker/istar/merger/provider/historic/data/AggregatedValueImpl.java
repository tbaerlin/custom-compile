/*
 * HasSnapFields.java
 *
 * Created on 06.08.13 17:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.data.AggregatedValue;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;

/**
 * @author zzhao
 */
public class AggregatedValueImpl implements AggregatedValue {

    private final ReadableInterval interval;

    private final List<SnapField> additionalFields;

    private final List<BigDecimal> values;

    private final TickImpl.Type tickType;

    private final TickDataCommand.ElementDataType resultType;

    AggregatedValueImpl(ReadableInterval interval, TickImpl.Type tickType,
            TickDataCommand.ElementDataType resultType, int valueSlots, int additionalFieldSlots) {
        this.interval = interval;
        this.tickType = tickType;
        this.resultType = resultType;
        this.values = new ArrayList<>(valueSlots);
        this.additionalFields = new ArrayList<>(additionalFieldSlots);
    }

    void addAdditionalField(SnapField snapField) {
        this.additionalFields.add(snapField);
    }

    void addValue(BigDecimal price) {
        this.values.add(price);
    }

    @Override
    public List<SnapField> getAdditionalFields() {
        return new ArrayList<>(this.additionalFields);
    }

    @Override
    public AggregatedValue multiply(BigDecimal factor) {
        final AggregatedValueImpl price = new AggregatedValueImpl(this.interval, this.tickType,
                this.resultType, this.values.size(), this.additionalFields.size());
        for (BigDecimal ap : values) {
            price.addValue(multiply(ap, factor));
        }

        for (SnapField additionalField : additionalFields) {
            price.addAdditionalField(additionalField);
        }

        return price;
    }

    private BigDecimal multiply(BigDecimal bd, BigDecimal factor) {
        return (bd == null) ? null : bd.multiply(factor, Constants.MC);
    }

    @Override
    public AggregatedValue divide(BigDecimal factor) {
        final AggregatedValueImpl price = new AggregatedValueImpl(this.interval, this.tickType,
                this.resultType, this.values.size(), this.additionalFields.size());
        for (BigDecimal ap : values) {
            price.addValue(divide(ap, factor));
        }

        for (SnapField additionalField : additionalFields) {
            price.addAdditionalField(additionalField);
        }

        return price;
    }

    private BigDecimal divide(BigDecimal bd, BigDecimal factor) {
        return (bd == null) ? null : bd.divide(factor, Constants.MC);
    }

    @Override
    public ReadableInterval getInterval() {
        return interval;
    }

    @Override
    public BigDecimal getOpen() {
        switch (this.tickType) {
            case TRADE:
            case BID:
            case ASK:
                switch (this.resultType) {
                    case OHLC:
                    case OHLCV:
                        return this.values.get(0);
                }
                break;
            case YIELD:
            case SETTLEMENT:
                return this.values.get(0);

        }

        return null;
    }

    @Override
    public BigDecimal getHigh() {
        switch (this.tickType) {
            case TRADE:
            case BID:
            case ASK:
                switch (this.resultType) {
                    case OHLC:
                    case OHLCV:
                        return this.values.get(1);
                }
                break;
            case YIELD:
            case SETTLEMENT:
                return this.values.get(0);
        }

        return null;
    }

    @Override
    public BigDecimal getLow() {
        switch (this.tickType) {
            case TRADE:
            case BID:
            case ASK:
                switch (this.resultType) {
                    case OHLC:
                    case OHLCV:
                        return this.values.get(2);
                }
                break;
            case YIELD:
            case SETTLEMENT:
                return this.values.get(0);
        }

        return null;
    }

    @Override
    public BigDecimal getClose() {
        switch (this.tickType) {
            case TRADE:
                switch (this.resultType) {
                    case PERFORMANCE:
                    case CLOSE:
                        return this.values.get(0);
                    case OHLC:
                    case OHLCV:
                        return this.values.get(3);
                }
                break;
            case BID:
            case ASK:
                switch (this.resultType) {
                    case OHLC:
                    case OHLCV:
                        return this.values.get(3);
                }
                break;
            case YIELD:
            case SETTLEMENT:
                return this.values.get(0);
        }

        return null;
    }

    @Override
    public Long getVolume() {
        switch (this.tickType) {
            case TRADE:
                switch (this.resultType) {
                    case FUND:
                        return getLongValue(3);
                }
            case BID:
            case ASK:
                switch (this.resultType) {
                    case OHLCV:
                        return getLongValue(4);
                }
        }

        return null;
    }

    private Long getLongValue(int idx) {
        return null == this.values.get(idx) ? null : this.values.get(idx).longValue();
    }

    private Integer getIntValue(int idx) {
        return null == this.values.get(idx) ? null : this.values.get(idx).intValue();
    }

    @Override
    public Integer getContract() {
        switch (this.tickType) {
            case TRADE:
                switch (this.resultType) {
                    case OHLCV:
                        return getIntValue(5);
                }
        }

        return null;
    }

    @Override
    public BigDecimal getIssuePrice() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.FUND) {
            return this.values.get(0);
        }

        return null;
    }

    @Override
    public BigDecimal getRedemptionPrice() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.FUND) {
            return this.values.get(1);
        }

        return null;
    }

    @Override
    public BigDecimal getNetAssetValue() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.FUND) {
            return this.values.get(2);
        }

        return null;
    }

    @Override
    public BigDecimal getYield() {
        if (this.tickType == TickImpl.Type.YIELD) {
            return this.values.get(0);
        }

        return null;
    }

    @Override
    public BigDecimal getSettlement() {
        if (this.tickType == TickImpl.Type.SETTLEMENT) {
            return this.values.get(0);
        }

        return null;
    }

    @Override
    public Long getTotalVolumeCall() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.VOLUME_AGGREGATION) {
            return getLongValue(0);
        }

        return null;
    }

    @Override
    public Long getTotalVolumePut() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.VOLUME_AGGREGATION) {
            return getLongValue(1);
        }

        return null;
    }

    @Override
    public Long getTotalBlockTradeVolumeCall() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.VOLUME_AGGREGATION) {
            return getLongValue(2);
        }

        return null;
    }

    @Override
    public Long getTotalBlockTradeVolumePut() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.VOLUME_AGGREGATION) {
            return getLongValue(3);
        }

        return null;
    }

    @Override
    public Long getTotalVolumeFutures() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.VOLUME_AGGREGATION) {
            return getLongValue(4);
        }

        return null;
    }

    @Override
    public Long getTotalBlockTradeVolumeFutures() {
        if (this.tickType == TickImpl.Type.TRADE
                && this.resultType == TickDataCommand.ElementDataType.VOLUME_AGGREGATION) {
            return getLongValue(5);
        }

        return null;
    }
}
