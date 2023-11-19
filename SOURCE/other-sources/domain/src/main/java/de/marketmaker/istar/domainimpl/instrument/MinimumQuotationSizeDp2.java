/*
* MinimumQuotationSizeDp2.java
*
* Created on 15.02.2006
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Martin Wilke
 */

public class MinimumQuotationSizeDp2 implements MinimumQuotationSize, Serializable {

    static final long serialVersionUID = 8137122711474450216L;

    // we always use BigDecimal, but for serialization's sake this remains a declared Number
    private Number number;

    private Unit unit;

    private Currency currency;

    public MinimumQuotationSizeDp2() {
        this.unit = Unit.NOTHING;
    }

    public MinimumQuotationSizeDp2(BigDecimal number, Unit unit, Currency currency) {
        this.number = number;
        this.unit = unit;
        this.currency = currency;
    }

    public Number getNumber() {
        return this.number;
    }

    public Unit getUnit() {
        return this.unit;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public boolean isUnitPercent() {
        return this.unit == Unit.PERCENT;
    }

    public void setNumber(Number number) {
        if (number == null || number instanceof BigDecimal) {
            this.number = number;
        }
        else if (number instanceof Double) {
            this.number = BigDecimal.valueOf((Double) number);
        }
        else {
            this.number = new BigDecimal(number.toString());
        }
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String toString() {
        return "MinimumQuotationSize[number=" + this.number + ", unit=" + this.unit + ", currency=" + this.currency + "]";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MinimumQuotationSizeDp2 that = (MinimumQuotationSizeDp2) o;

        if (currency != null ? !currency.equals(that.currency) : that.currency != null)
            return false;
        if (unit != that.unit) return false;
        if (number != null) {
            if (that.number == null) {
                return false;
            }
            return ((BigDecimal)this.number).compareTo((BigDecimal) that.number) == 0;
        }

        return that.number == null;
    }
}
