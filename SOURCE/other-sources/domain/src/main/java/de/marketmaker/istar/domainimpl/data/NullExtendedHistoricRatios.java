/*
 * NullBasicHistoricRatios.java
 *
 * Created on 01.10.2006 13:42:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.ExtendedHistoricRatios;
import org.joda.time.Interval;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullExtendedHistoricRatios implements Serializable, ExtendedHistoricRatios {
    protected static final long serialVersionUID = 1L;
    public final static ExtendedHistoricRatios INSTANCE = new NullExtendedHistoricRatios();

    private NullExtendedHistoricRatios() {
    }

    public Interval getReference() {
        return null;
    }

    public Integer getLongestContinuousNegativeReturnPeriod() {
        return null;
    }

    public BigDecimal getMaximumLossPercent() {
        return null;
    }

    public BigDecimal getSharpeRatio() {
        return null;
    }
}