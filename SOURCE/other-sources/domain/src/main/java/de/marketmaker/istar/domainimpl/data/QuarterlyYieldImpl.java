package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.QuarterlyYield;
import org.joda.time.Interval;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * QuarterlyYieldImpl.java
 * Created on Jan 5, 2009 2:33:37 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class QuarterlyYieldImpl implements QuarterlyYield, Serializable {
    static final long serialVersionUID = 1L;
    private final Interval reference;
    private final BigDecimal yield;

    public QuarterlyYieldImpl(Interval reference, BigDecimal yield) {
        this.reference = reference;
        this.yield = yield;
    }

    public Interval getReference() {
        return reference;
    }

    public BigDecimal getYield() {
        return yield;
    }

    @Override
    public String toString() {
        return "QuarterlyYieldImpl{" +
                "reference=" + reference +
                ", yield=" + yield +
                '}';
    }
}
