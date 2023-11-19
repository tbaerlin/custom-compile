package de.marketmaker.istar.analyses.analyzer;

import java.math.BigDecimal;
import java.util.Optional;

import de.marketmaker.istar.domain.profile.Profile;

/**
 * cache / provider for price data for analyses
 */
public interface PriceCache {

    // return previous close at a certain date
    Optional<BigDecimal> getPreviousClose(Profile profile, String currency, Security security, int yyyyMmDd);

    // return true if
    // the target value of the analysis was successful at or before the date
    Optional<Boolean> getSuccess(Profile profile, Analysis analysis, int yyyyMmDd);

}
