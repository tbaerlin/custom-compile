/*
 * MasterDataStock.java
 *
 * Created on 12.07.2006 14:56:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MasterDataCertificate extends MasterData {
    LocalizedString getType();

    String getIssuerName();

    String getIssuerCountryCode();

    BigDecimal getIssuePrice();

    BigDecimal getStrikePrice();

    LocalizedString getPriceType();

    BigDecimal getCoupon();

    BigDecimal getCap();

    BigDecimal getKnockin();

    BigDecimal getBonuslevel();

    BigDecimal getBarrier();

    LocalizedString getGuaranteeType();

    LocalizedString getLeverageType();

    long getInstrumentid();

    BigDecimal getCharge();

    LocalDate getFirsttradingday();

    LocalDate getLasttradingday();

    BigDecimal getGuaranteelevel();

    LocalizedString getExercisetypeName();

    LocalizedString getInterestPaymentInterval();

    LocalDate getIssuedate();

    LocalDate getKnockindate();

    Boolean getKnockout();

    DateTime getKnockoutdate();

    BigDecimal getNominal();

    BigDecimal getProtectlevel();

    BigDecimal getQuantity();

    Boolean getQuanto();

    String getRange();

    String getRollover();

    BigDecimal getStoploss();

    BigDecimal getVariabletradingamount();

    LocalizedString getAnnotation();

    String getProductNameIssuer();

    LocalizedString getCategory();

    BigDecimal getMaxSpreadEuwax();

    BigDecimal getRefundMaximum();

    BigDecimal getFloor();

    BigDecimal getParticipationFactor();

    BigDecimal getParticipationLevel();

    LocalDate getDeadlineDate1();

    LocalDate getDeadlineDate2();

    LocalDate getDeadlineDate3();

    LocalDate getDeadlineDate4();

    LocalDate getPaymentDate();

    BigDecimal getSubscriptionRatio();

    String getTypeKey();

    String getMultiassetName();

    String getCurrencyStrike();

    BigDecimal getStartvalue();

    BigDecimal getStopvalue();

    LocalDate getLasttradingdayEuwax();

    DateTime getLowerBarrierDate();

    DateTime getUpperBarrierDate();

    List<Long> getBasketIids();

    LocalDate getSettlementday();

    String getTypeKeyVwd();

    LocalizedString getTypeVwd();

    String getSubtypeKeyVwd();

    String getSubtypeVwd();

    String getTypeKeyDZ();

    LocalizedString getTypeDZ();

    String getSubtypeKeyDZ();

    String getSubtypeDZ();

    String getTypeKeyWGZ();

    LocalizedString getTypeWGZ();

    String getSubtypeWGZ();

    BigDecimal getRefund();
}
