/*
 * MasterDataStock.java
 *
 * Created on 12.07.2006 14:56:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface EdgData {
    long getInstrumentid();

    String getIsin();

    LocalDate getEdgRatingDate();

    Integer getEdgScore1();

    Integer getEdgScore2();

    Integer getEdgScore3();

    Integer getEdgScore4();

    Integer getEdgScore5();

    Integer getEdgTopScore();

    Integer getEdgTopClass();

    LocalDate getDdvDate();

    BigDecimal getDdvVar10d();

    BigDecimal getDdvPriceRisk10d();

    BigDecimal getDdvInterestRisk10d();

    BigDecimal getDdvCurrencyRisk10d();

    BigDecimal getDdvIssuerRisk10d();

    BigDecimal getDdvVolatilityRisk10d();

    BigDecimal getDdvDiversificationRisk10d();

    BigDecimal getDdvTimevalue10d();

    BigDecimal getDdvVar250d();

    BigDecimal getDdvPriceRisk250d();

    BigDecimal getDdvInterestRisk250d();

    BigDecimal getDdvCurrencyRisk250d();

    BigDecimal getDdvIssuerRisk250d();

    BigDecimal getDdvVolatilityRisk250d();

    BigDecimal getDdvDiversificationRisk250d();

    BigDecimal getDdvTimevalue250d();

    Integer getDdvRiskclass10d();
}