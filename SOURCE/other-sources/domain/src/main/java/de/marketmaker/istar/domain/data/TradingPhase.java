/*
 * TradingSignal.java
 *
 * Created on 05.10.2006 17:20:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TradingPhase {
    public enum SignalSystem {
        macd, momentum, bollinger, gd, tbi, mm;

        public enum Strategy {conservative, speculative}
    }

    /**
     * @return the type of the signal system
     */
    SignalSystem getSignalSystem();

    /**
     * @return the strategy of the signal system
     */
    SignalSystem.Strategy getSignalSystemStrategy();

    /**
     * @return end date of the phase; may be null if phase is still in progress
     */
    LocalDate getStartDate();

    /**
     * @return Start of the phase
     */
    LocalDate getEndDate();

    /**
     * Change of the close prices between startDate.plusDays(1) and endDate.plusDays(1)
     * @return percent change
     */
    BigDecimal getChangePercent();

    /**
     * Whether this phase is a short phase. If it is, the phase starts with selling securities
     * at startDate and buying them at endDate, otherwise it starts with buying at startDate
     * and selling at endDate.
     * @return true iff short phase
     */
    boolean isShortPhase();
}
