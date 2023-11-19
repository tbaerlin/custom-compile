/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;
import java.math.BigDecimal;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.instrument.Future;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FutureDp2 extends DerivativeDp2 implements Future, Serializable {
    static final long serialVersionUID = -106L;

    private long underlyingProductId;

    private BigDecimal tickSize;
    private BigDecimal tickValue;
    private Currency tickCurrency;
    private BigDecimal contractValue;
    private Currency contractCurrency;

    public FutureDp2() {
    }

    public FutureDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.FUT;
    }

    public long getUnderlyingProductId() {
        return underlyingProductId;
    }

    public void setUnderlyingProductId(long underlyingProductId) {
        this.underlyingProductId = underlyingProductId;
    }

    @Override
    public BigDecimal getTickSize() {
        return tickSize;
    }

    public void setTickSize(BigDecimal tickSize) {
        this.tickSize = tickSize;
    }

    @Override
    public BigDecimal getTickValue() {
        return tickValue;
    }

    public void setTickValue(BigDecimal tickValue) {
        this.tickValue = tickValue;
    }

    @Override
    public Currency getTickCurrency() {
        return tickCurrency;
    }

    public void setTickCurrency(Currency tickCurrency) {
        this.tickCurrency = tickCurrency;
    }

    @Override
    public BigDecimal getContractValue() {
        return contractValue;
    }

    public void setContractValue(BigDecimal contractValue) {
        this.contractValue = contractValue;
    }

    @Override
    public Currency getContractCurrency() {
        return contractCurrency;
    }

    public void setContractCurrency(Currency contractCurrency) {
        this.contractCurrency = contractCurrency;
    }

    private static boolean equals(Object o1, Object o2) {
        return (o1 != null) ? o1.equals(o2) : o2 == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final FutureDp2 futureDp2 = (FutureDp2) o;

        return  this.underlyingProductId==futureDp2.underlyingProductId
                && equals(this.contractCurrency, futureDp2.contractCurrency)
                && equals(this.contractValue, futureDp2.contractValue)
                && equals(this.tickCurrency, futureDp2.tickCurrency)
                && equals(this.tickSize, futureDp2.tickSize)
                && equals(this.tickValue, futureDp2.tickValue);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (underlyingProductId ^ (underlyingProductId >>> 32));
        result = 31 * result + (tickSize != null ? tickSize.hashCode() : 0);
        result = 31 * result + (tickValue != null ? tickValue.hashCode() : 0);
        result = 31 * result + (tickCurrency != null ? tickCurrency.hashCode() : 0);
        result = 31 * result + (contractValue != null ? contractValue.hashCode() : 0);
        result = 31 * result + (contractCurrency != null ? contractCurrency.hashCode() : 0);
        return result;
    }
}
