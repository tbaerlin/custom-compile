/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.instrument.CurrencyCrossrate;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CurrencyCrossrateDp2 extends InstrumentDp2 implements CurrencyCrossrate, Serializable {
    static final long serialVersionUID = -103L;

    private Currency sourceCurrency;

    private Currency targetCurrency;

    private double sourceToTargetFactor;

    public CurrencyCrossrateDp2() {
    }

    public CurrencyCrossrateDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.CUR;
    }

    public Currency getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(Currency sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public double getSourceToTargetFactor() {
        return sourceToTargetFactor;
    }

    public void setSourceToTargetFactor(double sourceToTargetFactor) {
        this.sourceToTargetFactor = sourceToTargetFactor;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }

        final CurrencyCrossrateDp2 that = (CurrencyCrossrateDp2) o;
        if (!equalsById(this.sourceCurrency, that.sourceCurrency)) return false;
        if (!equalsById(this.targetCurrency, that.targetCurrency)) return false;
        return true;        
    }
}
