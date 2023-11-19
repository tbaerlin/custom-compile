/*
 * TradingSignalImpl.java
 *
 * Created on 05.10.2006 17:28:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.TradingPhase;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class TradingPhaseImpl implements TradingPhase, Serializable {
    protected static final long serialVersionUID = 1L;

    private final LocalDate startDate;

    private final LocalDate endDate;

    private final BigDecimal changePercent;

    private final boolean shortPhase;

    private final SignalSystem signalSystem;

    private final SignalSystem.Strategy signalSystemStrategy;


    public TradingPhaseImpl(TradingPhase.SignalSystem signalSystem,
            SignalSystem.Strategy signalSystemStrategy, LocalDate startDate, LocalDate endDate,
            BigDecimal changePercent,
            boolean shortPhase) {
        this.signalSystem = signalSystem;
        this.signalSystemStrategy = signalSystemStrategy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.changePercent = changePercent;
        this.shortPhase = shortPhase;
    }

    public SignalSystem getSignalSystem() {
        return signalSystem;
    }

    public SignalSystem.Strategy getSignalSystemStrategy() {
        return signalSystemStrategy;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isShortPhase() {
        return shortPhase;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(60);
        sb.append("TradingPhaseImpl[");
        sb.append(this.signalSystem.name()).append("/").append(this.signalSystemStrategy.name()).append(", ");
        sb.append(this.startDate).append("..");
        if (this.endDate != null) {
            sb.append(this.endDate);
        }
        if (this.changePercent != null) {
            sb.append(", ").append("change=").append(this.changePercent.toPlainString());
        }
        sb.append(", ").append(this.isShortPhase() ? "Short" : "Long").append("]");
        return sb.toString();
    }
}
