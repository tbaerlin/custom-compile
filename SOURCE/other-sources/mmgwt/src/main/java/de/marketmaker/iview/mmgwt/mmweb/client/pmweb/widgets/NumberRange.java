/*
 * NumberRange.java
 *
 * Created on 11.08.2014 11:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets;

import de.marketmaker.iview.tools.i18n.NonNLS;

import java.math.BigDecimal;

/**
* @author mdick
*/
@NonNLS
public final class NumberRange {
    private final BigDecimal min;
    private final BigDecimal max;

    public NumberRange(BigDecimal min, BigDecimal max) {
        this.min = min;
        this.max = max;
    }

    public boolean hasMin() {
        return this.min != null;
    }

    public boolean hasMax() {
        return this.max != null;
    }

    public boolean isComplete() {
        return hasMin() && hasMax();
    }

    public BigDecimal getMin() {
        return this.min;
    }

    public BigDecimal getMax() {
        return this.max;
    }

    @Override
    public String toString() {
        return  "[" + this.min + ", " + this.max + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberRange)) return false;

        final NumberRange that = (NumberRange) o;

        if (this.max != null ? !max.equals(that.max) : that.max != null) return false;
        if (this.min != null ? !min.equals(that.min) : that.min != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = this.min != null ? this.min.hashCode() : 0;
        result = 31 * result + (this.max != null ? this.max.hashCode() : 0);
        return result;
    }
}
