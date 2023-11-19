/*
 * BondRatios.java
 *
 * Created on 16.07.2006 21:38:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface BondRatios {
    BigDecimal getYield();

    BigDecimal getBrokenPeriodInterest();

    BigDecimal getDuration();

    BigDecimal getConvexity();

    BigDecimal getInterestRateElasticity();

    BigDecimal getBasePointValue();

    BigDecimal getBuyingPrice();

    BigDecimal getBuyingYield();

    BigDecimal getSellingPrice();

    BigDecimal getSellingYield();

}
