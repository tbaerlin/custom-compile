/*
 * IAggregatedValue.java
 *
 * Created on 16.08.13 14:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.ReadableInterval;

/**
 * @author zzhao
 */
public interface AggregatedValue {
    List<SnapField> getAdditionalFields();

    AggregatedValue multiply(BigDecimal factor);

    AggregatedValue divide(BigDecimal factor);

    ReadableInterval getInterval();

    BigDecimal getOpen();

    BigDecimal getHigh();

    BigDecimal getLow();

    BigDecimal getClose();

    Long getVolume();

    Integer getContract();

    BigDecimal getIssuePrice();

    BigDecimal getRedemptionPrice();

    BigDecimal getNetAssetValue();

    BigDecimal getYield();

    BigDecimal getSettlement();

    Long getTotalVolumeCall();

    Long getTotalVolumePut();

    Long getTotalBlockTradeVolumeCall();

    Long getTotalBlockTradeVolumePut();

    Long getTotalVolumeFutures();

    Long getTotalBlockTradeVolumeFutures();
}
