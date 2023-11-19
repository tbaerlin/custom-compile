/*
 * SignalProvider.java
 *
 * Created on 05.10.2006 17:18:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

import org.joda.time.Interval;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.TradingPhase;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pm5.tradingPhases")
public interface TradingPhaseProvider {
    TradingPhaseResponse getTradingPhases(TradingPhaseRequest request);
}
