/*
 * StockAnalysisImpl.java
 *
 * Created on 10.08.2006 13:55:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import de.marketmaker.istar.domain.data.StockAnalysisAims;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisAimsImpl implements Serializable, StockAnalysisAims {
    protected static final long serialVersionUID = 1L;
    
    public static final StockAnalysisAims NULL
            = new StockAnalysisAimsImpl(null, BigDecimal.ZERO, BigDecimal.ZERO);

    private final String currency;
    private final BigDecimal minimum;
    private final BigDecimal maximum;

    public StockAnalysisAimsImpl(String currency, BigDecimal minimum, BigDecimal maximum) {
        this.currency = currency;
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getMinimum() {
        return minimum;
    }

    public BigDecimal getMaximum() {
        return maximum;
    }

    public String toString() {
        return "StockAnalysisAimsImpl[currency=" + currency
                + ", minimum=" + minimum
                + ", maximum=" + maximum
                + "]";
    }
}
