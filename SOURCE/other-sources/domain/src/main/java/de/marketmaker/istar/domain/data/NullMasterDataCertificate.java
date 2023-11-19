/*
 * NullMasterDataCertificate.java
 *
 * Created on 28.07.2006 10:55:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullMasterDataCertificate implements MasterDataCertificate, Serializable {
    protected static final long serialVersionUID = 1L;

    public final static MasterDataCertificate INSTANCE = new NullMasterDataCertificate();

    private NullMasterDataCertificate() {
    }

    public String getIssuerName() {
        return null;
    }

    @Override
    public String getIssuerCountryCode() {
        return null;
    }

    public BigDecimal getIssuePrice() {
        return null;
    }

    public LocalizedString getType() {
        return null;
    }

    public BigDecimal getStrikePrice() {
        return null;
    }

    public LocalizedString getPriceType() {
        return null;
    }

    public BigDecimal getCoupon() {
        return null;
    }

    public BigDecimal getCap() {
        return null;
    }

    public BigDecimal getKnockin() {
        return null;
    }

    public BigDecimal getBonuslevel() {
        return null;
    }

    public BigDecimal getBarrier() {
        return null;
    }

    public LocalizedString getGuaranteeType() {
        return null;
    }

    public LocalizedString getLeverageType() {
        return null;
    }

    public long getInstrumentid() {
        return Long.MIN_VALUE;
    }

    public BigDecimal getCharge() {
        return null;
    }

    public LocalDate getFirsttradingday() {
        return null;
    }

    public LocalDate getLasttradingday() {
        return null;
    }

    public BigDecimal getGuaranteelevel() {
        return null;
    }

    public LocalizedString getExercisetypeName() {
        return null;
    }

    public LocalizedString getInterestPaymentInterval() {
        return null;
    }

    public LocalDate getIssuedate() {
        return null;
    }

    public LocalDate getKnockindate() {
        return null;
    }

    public Boolean getKnockout() {
        return null;
    }

    public DateTime getKnockoutdate() {
        return null;
    }

    public BigDecimal getNominal() {
        return null;
    }

    public BigDecimal getProtectlevel() {
        return null;
    }

    public BigDecimal getQuantity() {
        return null;
    }

    public Boolean getQuanto() {
        return null;
    }

    public String getRange() {
        return null;
    }

    public String getRollover() {
        return null;
    }

    public BigDecimal getStoploss() {
        return null;
    }

    public BigDecimal getVariabletradingamount() {
        return null;
    }

    public String getProductNameIssuer() {
        return null;
    }

    public LocalizedString getCategory() {
        return null;
    }

    public BigDecimal getMaxSpreadEuwax() {
        return null;
    }

    public BigDecimal getRefundMaximum() {
        return null;
    }


    public BigDecimal getFloor() {
        return null;
    }

    public BigDecimal getParticipationFactor() {
        return null;
    }

    public BigDecimal getParticipationLevel() {
        return null;
    }

    public LocalDate getDeadlineDate1() {
        return null;
    }

    public LocalDate getDeadlineDate2() {
        return null;
    }

    public LocalDate getDeadlineDate3() {
        return null;
    }

    public LocalDate getDeadlineDate4() {
        return null;
    }

    public LocalDate getPaymentDate() {
        return null;
    }

    public BigDecimal getSubscriptionRatio() {
        return null;
    }

    public String getTypeKey() {
        return null;
    }

    public String getMultiassetName() {
        return null;
    }

    public String getCurrencyStrike() {
        return null;
    }

    public BigDecimal getStartvalue() {
        return null;
    }

    public BigDecimal getStopvalue() {
        return null;
    }

    public LocalDate getLasttradingdayEuwax() {
        return null;
    }

    public DateTime getLowerBarrierDate() {
        return null;
    }

    public DateTime getUpperBarrierDate() {
        return null;
    }

    public List<Long> getBasketIids() {
        return null;
    }

    public LocalDate getSettlementday() {
        return null;
    }

    public LocalizedString getAnnotation() {
        return null;
    }

    @Override
    public String getTypeKeyVwd() {
        return null;
    }

    @Override
    public LocalizedString getTypeVwd() {
        return null;
    }

    @Override
    public String getSubtypeKeyVwd() {
        return null;
    }

    @Override
    public String getSubtypeVwd() {
        return null;
    }

    @Override
    public String getTypeKeyDZ() {
        return null;
    }

    @Override
    public LocalizedString getTypeDZ() {
        return null;
    }

    @Override
    public String getSubtypeKeyDZ() {
        return null;
    }

    @Override
    public String getSubtypeDZ() {
        return null;
    }

    @Override
    public String getTypeKeyWGZ() {
        return null;
    }

    @Override
    public LocalizedString getTypeWGZ() {
        return null;
    }

    @Override
    public String getSubtypeWGZ() {
        return null;
    }

    @Override
    public BigDecimal getRefund() {
        return null;
    }

    public String toString() {
        return "NullMasterDataCertificate[]";
    }

    protected Object readResolve() {
        return INSTANCE;
    }
}
