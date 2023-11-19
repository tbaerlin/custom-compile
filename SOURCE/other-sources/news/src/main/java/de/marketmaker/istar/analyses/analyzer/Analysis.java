package de.marketmaker.istar.analyses.analyzer;

import java.math.BigDecimal;
import java.util.Comparator;

import com.google.common.collect.ComparisonChain;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import de.marketmaker.istar.analyses.backend.Protos.Analysis.Rating;
import de.marketmaker.istar.common.util.DateUtil;

/**
 *
 */
public class Analysis {

    // this is a custom hash-value, we use Long.toString(id, Character.MAX_RADIX) in views
    private final long analysisId;

    @NonNull
    private final Agency agency;

    @NonNull
    private final Security security;

    // normalized to BUY, HOLD, SELL, NONE
    private Rating rating;
    private Rating previousRating;

    @Nullable
    private BigDecimal target;
    @Nullable
    private BigDecimal previousTarget;

    private String targetCurrency = AnalysesCollector.NULL_CURRENCY;

    //  NDB_Agency_Date, NDB_Analyst_Study_Date as yyyyMmDd:
    private int startDate = AnalysesCollector.NULL_DATE;
    private int endDate = AnalysesCollector.NULL_DATE;


    public static final Comparator<Analysis> ID_COMPARATOR
            = (left, right) -> ComparisonChain.start().compare(left.analysisId, right.analysisId).result();

    /**
     * @param analysisId unique id for this analysis
     * @param agency rating agency that published this analysis
     * @param security security/instrument being rated by this analysis
     */
    public Analysis(long analysisId, Agency agency, Security security) {
        this.analysisId = analysisId;
        this.agency = agency;
        this.security = security;
    }

    public long getAnalysisId() {
        return analysisId;
    }

    public String getAnalysisIdString() {
        return Long.toString(analysisId, Character.MAX_RADIX);
    }

    public Security getSecurity() {
        return security;
    }

    public Agency getAgency() {
        return agency;
    }

    // -- plain setter/getter

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    public Rating getRating() {
        return rating;
    }

    public void setPreviousRating(Rating previousRating) {
        this.previousRating = previousRating;
    }

    public Rating getPreviousRating() {
        return previousRating;
    }

    public void setTarget(BigDecimal target) {
        this.target = target;
    }

    public BigDecimal getTarget() {
        return target;
    }

    public void setPreviousTarget(BigDecimal previousTarget) {
        this.previousTarget = previousTarget;
    }

    public BigDecimal getPreviousTarget() {
        return previousTarget;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public int getStartDate() {
        return startDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    @Override
    public String toString() {
        return "Analysis ["
                + "analysisId: " + getAnalysisIdString()
                + " iid: " + security.getIid()
                + " start: " + DateUtil.yyyyMmDdToLocalDate(startDate)
                + " end: " + DateUtil.yyyyMmDdToLocalDate(endDate)
                + " target: " + target + "(" + targetCurrency + ")"
                + "]";
    }

}
