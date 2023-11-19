/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.Currency;
import java.io.Serializable;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Rate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RateDp2 extends InstrumentDp2 implements Rate, Serializable {
    static final long serialVersionUID = -112L;

    private Currency sourceCurrency;

    private Currency targetCurrency;

    private double sourceToTargetFactor;

    public RateDp2() {
    }

    public RateDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.ZNS;
    }

    @Override
    public Currency getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    @Override
    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    @Override
    public double getSourceToTargetFactor() {
        return sourceToTargetFactor;
    }

    public void setSourceToTargetFactor(double sourceToTargetFactor) {
        this.sourceToTargetFactor = sourceToTargetFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RateDp2 rateDp2 = (RateDp2) o;

        if (sourceCurrency != null ? !sourceCurrency.equals(rateDp2.sourceCurrency) : rateDp2.sourceCurrency != null) {
            return false;
        }
        return targetCurrency != null ? targetCurrency.equals(rateDp2.targetCurrency) : rateDp2.targetCurrency == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sourceCurrency != null ? sourceCurrency.hashCode() : 0);
        result = 31 * result + (targetCurrency != null ? targetCurrency.hashCode() : 0);
        return result;
    }
}
