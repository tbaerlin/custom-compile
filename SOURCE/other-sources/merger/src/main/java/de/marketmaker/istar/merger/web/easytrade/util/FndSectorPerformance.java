/*
 * FndSectorPerformance.java
 *
 * Created on 10.12.2007 14:25:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.util;

import java.math.BigDecimal;
import java.util.Map;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;

/**
 * Method object that creates a model containing MinMaxAvg-values for the bvi-performances
 * of a fund's sector and also the differences between the fund's performance and the average
 * sector performances for various intervals.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndSectorPerformance extends FndSectorMMA {
    private static final String MIN_PERF = "-99"; // more than 99% loss is probably a data error

    private static final String MAX_PERF = "10000"; // more than 10000% gain is prob. data error

    private static final String BVIPERFORMANCES
            = "bviperformance1m,bviperformance6m,bviperformance1y,bviperformance3y,bviperformance5y,bviperformance10y";

    public FndSectorPerformance(RatiosProvider ratiosProvider, RatioDataRecord fundRatios) {
        super(ratiosProvider, fundRatios);
    }

    protected void initMMAParameters(Map<String, String> mmaParameters) {
//        mmaParameters.put("bviperformance1m:L", MIN_PERF);
//        mmaParameters.put("bviperformance1m:U", MAX_PERF);
//        mmaParameters.put("bviperformance6m:L", MIN_PERF);
//        mmaParameters.put("bviperformance6m:U", MAX_PERF);
//        mmaParameters.put("bviperformance1y:L", MIN_PERF);
//        mmaParameters.put("bviperformance1y:U", MAX_PERF);
//        mmaParameters.put("bviperformance3y:L", MIN_PERF);
//        mmaParameters.put("bviperformance3y:U", MAX_PERF);
//        mmaParameters.put("bviperformance5y:L", MIN_PERF);
//        mmaParameters.put("bviperformance5y:U", MAX_PERF);
//        mmaParameters.put("bviperformance10y:L", MIN_PERF);
//        mmaParameters.put("bviperformance10y:U", MAX_PERF);

        mmaParameters.put(MinMaxAvgVisitor.KEY_SOURCE, BVIPERFORMANCES);
    }

    protected void processResponse() {
        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma1Month = getMma(RatioFieldDescription.bviperformance1m);
        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma6Months = getMma(RatioFieldDescription.bviperformance6m);
        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma1Year = getMma(RatioFieldDescription.bviperformance1y);
        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma3Years = getMma(RatioFieldDescription.bviperformance3y);
        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma5Years = getMma(RatioFieldDescription.bviperformance5y);
        final MinMaxAvgRatioSearchResponse.MinMaxAvg mma10Years = getMma(RatioFieldDescription.bviperformance10y);

        this.result.put("mma1Month", mma1Month);
        this.result.put("mma6Months", mma6Months);
        this.result.put("mma1Year", mma1Year);
        this.result.put("mma3Years", mma3Years);
        this.result.put("mma5Years", mma5Years);
        this.result.put("mma10Years", mma10Years);

        this.result.put("f2s1Month", getDifference(fundRatios.getBVIPerformance1Month(), mma1Month));
        this.result.put("f2s6Months", getDifference(fundRatios.getBVIPerformance6Months(), mma6Months));
        this.result.put("f2s1Year", getDifference(fundRatios.getBVIPerformance1Year(), mma1Year));
        this.result.put("f2s3Years", getDifference(fundRatios.getBVIPerformance3Years(), mma3Years));
        this.result.put("f2s5Years", getDifference(fundRatios.getBVIPerformance5Years(), mma5Years));
        this.result.put("f2s10Years", getDifference(fundRatios.getBVIPerformance10Years(), mma10Years));
    }


    private BigDecimal getDifference(BigDecimal p, MinMaxAvgRatioSearchResponse.MinMaxAvg mma) {
        if (p == null || mma == null) {
            return null;
        }
        return p.subtract(mma.getAvg(), Constants.MC);
    }

}


