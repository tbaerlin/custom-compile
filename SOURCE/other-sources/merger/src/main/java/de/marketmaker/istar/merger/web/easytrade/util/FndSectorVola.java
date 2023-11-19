/*
 * FndSectorPerformance.java
 *
 * Created on 10.12.2007 14:25:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.util.Map;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;


/**
 * Method object that creates a model containing MinMaxAvg-values for the volatilities
 * of a fund's sector for various intervals
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndSectorVola extends FndSectorMMA {
    private static final String MAX_VOLA = "1000"; // more than 1000% vola is prob. data error

    private static final String VOLAS
            = "volatility1m,volatility6m,volatility1y,volatility3y,volatility5y";

    public FndSectorVola(RatiosProvider ratiosProvider, RatioDataRecord fundRatios) {
        super(ratiosProvider, fundRatios);
    }

    protected void initMMAParameters(Map<String, String> mmaParameters) {
        mmaParameters.put("volatility1m:U", MAX_VOLA);
        mmaParameters.put("volatility6m:U", MAX_VOLA);
        mmaParameters.put("volatility1y:U", MAX_VOLA);
        mmaParameters.put("volatility3y:U", MAX_VOLA);
        mmaParameters.put("volatility5y:U", MAX_VOLA);

        mmaParameters.put(MinMaxAvgVisitor.KEY_SOURCE, VOLAS);
    }

    protected void processResponse() {
        this.result.put("mma1Month", getMma(RatioFieldDescription.volatility1m));
        this.result.put("mma6Months", getMma(RatioFieldDescription.volatility6m));
        this.result.put("mma1Year", getMma(RatioFieldDescription.volatility1y));
        this.result.put("mma3Years", getMma(RatioFieldDescription.volatility3y));
        this.result.put("mma5Years", getMma(RatioFieldDescription.volatility5y));
    }
}