/*
 * MasterDataBond.java
 *
 * Created on 17.07.2006 09:57:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MasterDataBond extends MasterData {
    LocalizedString getBondType();

    String getBondRank();

    LocalizedString getCouponType();

    DateTime getIssueDate();

    BigDecimal getNominalInterest();

    BigDecimal getFaceValue();

    BigDecimal getRedemptionPrice();

    BigDecimal getIssueVolume();

    Integer getNumberOfCoupons();

    DateTime getInterestDate();

    LocalizedString getInterestPeriod();

    String getIssuerName();

    String getCountryOfIssuer();

    String getCountryOfIssuerCode();

    String getCouponDateDayNumber();

    String getCouponDateMonthNumber();

    BigDecimal getIssuePrice();

    String getIssueCurrency();

    String getInterestType();

    String getInterestRunDeviation();

    String getInterestCalculationMethod();

    DateTime getExpirationDate();

    String getCouponFrequency();

    Integer getCouponDateDays();

    DateTime getFirstCouponDate();

    DateTime getLastCouponDate();

    DateTime getCouponDate();

    BigDecimal getSmallestTransferableUnit();

    BigDecimal getMinAmountOfTransferableUnit();

    BigDecimal getConversionFactor();
}
