/*
 * PortfolioVaRLightRequest.java
 *
 * Created on 07.09.2010 17:07:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioVaRLightResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final BigDecimal var;

    private final int riskclass;

    private final BigDecimal portfolioValue;

    private final BigDecimal varAbsolute;

    private final String currency;

    private final DateTime date;

    private final Map<String, SymbolQuote> failedQuotes;

    public PortfolioVaRLightResponse(BigDecimal var, int riskclass,
            BigDecimal portfolioValue,
            BigDecimal varAbsolute, String currency, DateTime date,
            Map<String, SymbolQuote> failedQuotes) {
        this.var = var;
        this.riskclass = riskclass;
        this.portfolioValue = portfolioValue;
        this.varAbsolute = varAbsolute;
        this.currency = currency;
        this.date = date;
        this.failedQuotes = failedQuotes;
    }

    public BigDecimal getVar() {
        return var;
    }

    public int getRiskclass() {
        return riskclass;
    }

    public BigDecimal getPortfolioValue() {
        return portfolioValue;
    }

    public BigDecimal getVarAbsolute() {
        return varAbsolute;
    }

    public String getCurrency() {
        return currency;
    }

    public DateTime getDate() {
        return date;
    }

    public Map<String, SymbolQuote> getFailedQuotes() {
        return failedQuotes;
    }
}
