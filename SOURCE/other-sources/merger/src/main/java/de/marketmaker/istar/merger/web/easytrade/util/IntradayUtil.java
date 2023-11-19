/*
 * IntradayUtil.java
 *
 * Created on 26.11.12 14:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.TickHistoryRequest;

/**
 * @author zzhao
 */
public class IntradayUtil {

    private static final EnumSet<TickType> ALLOWED_TICK_TYPES = EnumSet.of(
            TickType.TRADE,
            TickType.BID,
            TickType.ASK,
            TickType.SYNTHETIC_TRADE
    );

    private IntradayUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static boolean canUseTickHistory(Duration aggregation, TickType tickType) {
        return (aggregation != null) && !aggregation.isShorterThan(TickHistoryRequest.AGGREGATION)
                && (tickType != null) && ALLOWED_TICK_TYPES.contains(tickType);
    }
}
