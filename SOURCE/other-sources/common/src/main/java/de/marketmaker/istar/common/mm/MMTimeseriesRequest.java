/*
 * MMTalkTableRequest.java
 *
 * Created on 29.10.2008 14:34:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import java.util.List;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MMTimeseriesRequest extends MMServiceRequest {
    protected static final long serialVersionUID = 1L;

    private final LocalDate from;

    private final LocalDate to;

    public MMTimeseriesRequest(MMKeyType keytype, LocalDate from, LocalDate to) {
        super(keytype);
        if (to.isBefore(from)) {
            throw new IllegalArgumentException(to + " < " + from);
        }
        this.from = from;
        this.to = to;
    }

    public MMTimeseriesRequest appendFormula(String formula) {
        doAddFormula(formula);
        return this;
    }

    public MMTimeseriesRequest appendKey(String key) {
        doAddKey(key);
        return this;
    }

    public LocalDate getFrom() {
        return this.from;
    }

    public LocalDate getTo() {
        return this.to;
    }

    public MMTimeseriesRequest withFormula(String formula) {
        this.formulas.clear();
        doAddFormula(formula);
        return this;
    }

    public MMTimeseriesRequest withFormulas(List<String> formulas) {
        this.formulas.clear();
        for (final String formula : formulas) {
            doAddFormula(formula);
        }
        return this;
    }

    public MMTimeseriesRequest withKey(String key) {
        this.keys.clear();
        doAddKey(key);
        return this;
    }

    public MMTimeseriesRequest withKeys(List<String> keys) {
        this.keys.clear();
        for (final String key : keys) {
            doAddKey(key);
        }
        return this;
    }

    public MMTimeseriesRequest withPriceUpdate(MMPriceUpdate priceUpdate) {
        this.priceUpdates.clear();
        doAddPriceUpdate(priceUpdate);
        return this;
    }

    public MMTimeseriesRequest withPriceUpdates(List<MMPriceUpdate> priceUpdates) {
        this.priceUpdates.clear();
        for (final MMPriceUpdate priceUpdate : priceUpdates) {
            doAddPriceUpdate(priceUpdate);
        }
        return this;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", interval=").append(this.from).append("..").append(this.to);
    }
}