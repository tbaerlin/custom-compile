/*
 * IsoCurrencyResult.java
 *
 * Created on 06.05.2010 13:34:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.instrument.Quote;

import static de.marketmaker.istar.merger.Constants.MC;

/**
* @author oflege
*/
class IsoCurrencyResult {
    private static final String PREFIX_FACTOR = "factor:";

    enum Type { FACTOR, KEY }

    private final String isoKey;

    private final Type type;

    private final BigDecimal factor;

    private final String vwdfeedSymbol;

    private Quote quote;

    static IsoCurrencyResult create(String isoKey, String property) {
        if (property.startsWith(PREFIX_FACTOR)) {
            final String value = property.substring(PREFIX_FACTOR.length());
            return new IsoCurrencyResult(isoKey, new BigDecimal(value), null);
        }
        final int mult = property.indexOf('*');
        if (mult > 0) {
            final BigDecimal factor = BigDecimal.ONE.divide(new BigDecimal(property.substring(mult + 1)), MC);
            return new IsoCurrencyResult(isoKey, factor, property.substring(0, mult));
        }
        final int div = property.indexOf('/');
        if (div > 0) {
            final BigDecimal factor = new BigDecimal(property.substring(div + 1));
            return new IsoCurrencyResult(isoKey, factor, property.substring(0, div));
        }
        return new IsoCurrencyResult(isoKey, null, property);
    }

    private IsoCurrencyResult(String isoKey, BigDecimal factor, String vwdfeedSymbol) {
        this.isoKey = isoKey;
        this.factor = factor;
        this.type = (vwdfeedSymbol == null) ? Type.FACTOR : Type.KEY;
        this.vwdfeedSymbol = vwdfeedSymbol;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }

    public Quote getQuote() {
        return this.quote;
    }

    public String getIsoKey() {
        return isoKey;
    }

    public Type getType() {
        return type;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public BigDecimal getFactor(boolean reverse) {
        return reverse ? BigDecimal.ONE.divide(this.factor, MC) : this.factor;
    }

    public String getVwdfeedSymbol() {
        return vwdfeedSymbol;
    }

    public String toString() {
        return "IsoCurrencyResult[isokey=" + this.isoKey
                + ", type=" + this.type
                + ", factor=" + (this.factor != null ? this.factor.toPlainString() : "")
                + ", vwdfeedSymbol=" + this.vwdfeedSymbol
                + "]";
    }

}
