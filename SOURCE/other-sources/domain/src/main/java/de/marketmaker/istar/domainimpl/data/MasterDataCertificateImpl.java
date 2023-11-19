/*
 * MasterDataCertificateImpl.java
 *
 * Created on 11.08.2006 18:54:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataCertificate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MasterDataCertificateImpl implements Serializable, MasterDataCertificate {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;

    // old types
    private final String type;

    private final String typeKey;

    @Deprecated
    // TODO: remove after localization is done
    private final String category;


    private final LocalizedString localizedCategory;

    // new types: versions for vwd, DZ, WGZ
    private final String typeKeyVwd;

    private final LocalizedString typeVwd;

    private final String subtypeKeyVwd;

    private final String subtypeVwd;

    private final String typeKeyDZ;

    private final String typeDZ;

    private final String subtypeKeyDZ;

    private final String subtypeDZ;

    private final String typeKeyWGZ;

    private final String typeWGZ;

    private final String subtypeWGZ;

    private final BigDecimal cap;

    private final BigDecimal knockin;

    private final BigDecimal bonuslevel;

    private final BigDecimal barrier;

    private final BigDecimal startvalue;

    private final BigDecimal stopvalue;

    private final BigDecimal charge;

    private final BigDecimal coupon;

    private final LocalDate firsttradingday;

    private final LocalDate lasttradingday;

    private final LocalDate lasttradingdayEuwax;

    private final BigDecimal guaranteelevel;

    private final String exercisetypeName;

    private final String interestPaymentInterval;

    private final LocalDate issuedate;

    private final String issuerName;

    private final String issuerCountryCode;

    private final LocalDate knockindate;

    private final Boolean knockout;

    private final DateTime knockoutdate;

    private final DateTime lowerBarrierDate;

    private final DateTime upperBarrierDate;

    private final BigDecimal nominal;

    private final BigDecimal protectlevel;

    private final BigDecimal quantity;

    private final Boolean quanto;

    private final String range;

    private final String rollover;

    private final String guaranteeType;

    private final String leverageType;

    private final BigDecimal stoploss;

    private final BigDecimal strikePrice;

    private final BigDecimal issuePrice;

    private final BigDecimal variabletradingamount;

    private final String priceType;

    private final String annotation;

    private String productNameIssuer;

    private BigDecimal maxSpreadEuwax;

    private BigDecimal refundMaximum;

    private BigDecimal refund;

    private BigDecimal floor;

    private BigDecimal participationFactor;

    private BigDecimal participationLevel;

    private BigDecimal subscriptionRatio;

    private LocalDate deadlineDate1;

    private LocalDate deadlineDate2;

    private LocalDate deadlineDate3;

    private LocalDate deadlineDate4;

    private LocalDate paymentDate;

    private String multiassetName;

    private String currencyStrike;

    private List<Long> basketIids;

    private final LocalDate settlementday;


    public MasterDataCertificateImpl(long instrumentid,
            String typeKeyVwd, LocalizedString typeVwd, String subtypeKeyVwd, String subtypeVwd,
            String typeKeyDZ, String typeDZ, String subtyeKeyDZ, String subtypeDZ,
            String typeKeyWGZ, String typeWGZ, String subtypeWGZ,
            BigDecimal cap, BigDecimal knockin, BigDecimal bonuslevel,
            BigDecimal barrier, BigDecimal startvalue, BigDecimal stopvalue, BigDecimal charge,
            BigDecimal coupon, String type, String typeKey,
            LocalDate firsttradingday, LocalDate lasttradingday,
            LocalDate lasttradingdayEuwax,
            BigDecimal guaranteelevel, String exercisetypeName, String interestPaymentInterval,
            LocalDate issuedate, String issuerName, String issuerCountryCode, LocalDate knockindate, Boolean knockout,
            DateTime knockoutdate, DateTime lowerBarrierDate, DateTime upperBarrierDate,
            BigDecimal nominal, BigDecimal protectlevel,
            BigDecimal quantity, Boolean quanto, String range, String rollover,
            String guaranteeType, String leverageType, BigDecimal stoploss,
            BigDecimal strikePrice, BigDecimal issuePrice, BigDecimal variabletradingamount,
            String pricetype, String annotation, String productNameIssuer, String category,
            LocalizedString localizedCategory,
            LocalDate paymentDate, BigDecimal maxSpreadEuwax, BigDecimal refundMaximum, BigDecimal refund,
            LocalDate deadlineDate1, LocalDate deadlineDate2,
            LocalDate deadlineDate3, LocalDate deadlineDate4, BigDecimal floor,
            BigDecimal participationFactor, BigDecimal participationLevel,
            BigDecimal subscriptionRatio, String multiassetName, String currencyStrike,
            List<Long> basketIids,
            LocalDate settlementday) {
        this.instrumentid = instrumentid;
        this.typeKeyVwd = typeKeyVwd;
        this.typeVwd = typeVwd;
        this.subtypeKeyVwd = subtypeKeyVwd;
        this.subtypeVwd = subtypeVwd;
        this.typeKeyDZ = typeKeyDZ;
        this.typeDZ = typeDZ;
        this.subtypeKeyDZ = subtyeKeyDZ;
        this.subtypeDZ = subtypeDZ;
        this.typeKeyWGZ = typeKeyWGZ;
        this.typeWGZ = typeWGZ;
        this.subtypeWGZ = subtypeWGZ;
        this.cap = cap;
        this.knockin = knockin;
        this.bonuslevel = bonuslevel;
        this.barrier = barrier;
        this.startvalue = startvalue;
        this.stopvalue = stopvalue;
        this.charge = charge;
        this.coupon = coupon;
        this.type = type;
        this.typeKey = typeKey;
        this.firsttradingday = firsttradingday;
        this.lasttradingday = lasttradingday;
        this.lasttradingdayEuwax = lasttradingdayEuwax;
        this.guaranteelevel = guaranteelevel;
        this.exercisetypeName = exercisetypeName;
        this.interestPaymentInterval = interestPaymentInterval;
        this.issuedate = issuedate;
        this.issuerName = issuerName;
        this.issuerCountryCode = issuerCountryCode;
        this.knockindate = knockindate;
        this.knockout = knockout;
        this.knockoutdate = knockoutdate;
        this.upperBarrierDate = upperBarrierDate;
        this.lowerBarrierDate = lowerBarrierDate;
        this.nominal = nominal;
        this.protectlevel = protectlevel;
        this.quantity = quantity;
        this.quanto = quanto;
        this.range = range;
        this.rollover = rollover;
        this.guaranteeType = guaranteeType;
        this.leverageType = leverageType;
        this.stoploss = stoploss;
        this.strikePrice = strikePrice;
        this.issuePrice = issuePrice;
        this.variabletradingamount = variabletradingamount;
        this.priceType = pricetype;
        this.annotation = annotation;
        this.productNameIssuer = productNameIssuer;
        this.category = category;
        this.localizedCategory = localizedCategory;
        this.maxSpreadEuwax = maxSpreadEuwax;
        this.refundMaximum = refundMaximum;
        this.refund = refund;
        this.floor = floor;
        this.participationFactor = participationFactor;
        this.participationLevel = participationLevel;
        this.deadlineDate1 = deadlineDate1;
        this.deadlineDate2 = deadlineDate2;
        this.deadlineDate3 = deadlineDate3;
        this.deadlineDate4 = deadlineDate4;
        this.paymentDate = paymentDate;
        this.subscriptionRatio = subscriptionRatio;
        this.multiassetName = multiassetName;
        this.currencyStrike = currencyStrike;
        this.basketIids = basketIids;
        this.settlementday = settlementday;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public BigDecimal getCap() {
        return cap;
    }

    public BigDecimal getKnockin() {
        return knockin;
    }

    public BigDecimal getBonuslevel() {
        return bonuslevel;
    }

    public BigDecimal getBarrier() {
        return barrier;
    }

    public BigDecimal getStartvalue() {
        return startvalue;
    }

    public BigDecimal getStopvalue() {
        return stopvalue;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public BigDecimal getCoupon() {
        return coupon;
    }

    public LocalizedString getType() {
        return LocalizedString.createDefault(type);
    }

    public String getTypeKey() {
        return typeKey;
    }

    public LocalDate getFirsttradingday() {
        return firsttradingday;
    }

    public LocalDate getLasttradingday() {
        return lasttradingday;
    }

    public LocalDate getLasttradingdayEuwax() {
        return lasttradingdayEuwax;
    }

    public BigDecimal getGuaranteelevel() {
        return guaranteelevel;
    }

    public LocalizedString getExercisetypeName() {
        return LocalizedString.createDefault(this.exercisetypeName, Language.en,
                new String[][]{new String[]{"amerikanisch", "american"}, new String[]{"europäisch", "european"}});
    }

    public LocalizedString getInterestPaymentInterval() {
        return LocalizedString.createDefault(interestPaymentInterval);
    }

    public LocalDate getIssuedate() {
        return issuedate;
    }

    public String getIssuerName() {
        return issuerName;
    }

    @Override
    public String getIssuerCountryCode() {
        return issuerCountryCode;
    }

    public LocalDate getKnockindate() {
        return knockindate;
    }

    public Boolean getKnockout() {
        return knockout;
    }

    public DateTime getKnockoutdate() {
        return knockoutdate;
    }

    public DateTime getLowerBarrierDate() {
        return lowerBarrierDate;
    }

    public DateTime getUpperBarrierDate() {
        return upperBarrierDate;
    }

    public BigDecimal getNominal() {
        return nominal;
    }

    public BigDecimal getProtectlevel() {
        return protectlevel;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public Boolean getQuanto() {
        return quanto;
    }

    public String getRange() {
        return range;
    }

    public String getRollover() {
        return rollover;
    }

    public LocalizedString getGuaranteeType() {
        return LocalizedString.createDefault(guaranteeType, Language.en,
                new String[][]{new String[]{"100% Garantie", "100%"}, new String[]{"Teilgarantie", "partial"}});
    }

    public LocalizedString getLeverageType() {
        return LocalizedString.createDefault(leverageType);
    }

    public BigDecimal getStoploss() {
        return stoploss;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public BigDecimal getIssuePrice() {
        return issuePrice;
    }

    public BigDecimal getVariabletradingamount() {
        return variabletradingamount;
    }

    public LocalizedString getAnnotation() {
        return LocalizedString.createDefault(annotation);
    }

    public LocalizedString getPriceType() {
        return LocalizedString.createDefault(priceType);
    }

    public String getProductNameIssuer() {
        return productNameIssuer;
    }

    public LocalizedString getCategory() {
        return this.localizedCategory != null
                ? this.localizedCategory
                : LocalizedString.createDefault(this.category);
    }

    public BigDecimal getMaxSpreadEuwax() {
        return maxSpreadEuwax;
    }

    public BigDecimal getRefundMaximum() {
        return refundMaximum;
    }

    public BigDecimal getRefund() {
        return refund;
    }

    public BigDecimal getFloor() {
        return floor;
    }

    public BigDecimal getParticipationFactor() {
        return participationFactor;
    }

    public BigDecimal getParticipationLevel() {
        return participationLevel;
    }

    public LocalDate getDeadlineDate1() {
        return deadlineDate1;
    }

    public LocalDate getDeadlineDate2() {
        return deadlineDate2;
    }

    public LocalDate getDeadlineDate3() {
        return deadlineDate3;
    }

    public LocalDate getDeadlineDate4() {
        return deadlineDate4;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getSubscriptionRatio() {
        return subscriptionRatio;
    }

    public String getMultiassetName() {
        return multiassetName;
    }

    public String getCurrencyStrike() {
        return currencyStrike;
    }

    public List<Long> getBasketIids() {
        return basketIids;
    }

    public LocalDate getSettlementday() {
        return settlementday;
    }

    @Override
    public String getTypeKeyVwd() {
        return typeKeyVwd;
    }

    @Override
    public LocalizedString getTypeVwd() {
        return typeVwd;
    }

    @Override
    public String getSubtypeKeyVwd() {
        return subtypeKeyVwd;
    }

    @Override
    public String getSubtypeVwd() {
        return subtypeVwd;
    }

    @Override
    public String getTypeKeyDZ() {
        return typeKeyDZ;
    }

    @Override
    public LocalizedString getTypeDZ() {
        return LocalizedString.createDefault(typeDZ);
    }

    @Override
    public String getSubtypeKeyDZ() {
        return subtypeKeyDZ;
    }

    @Override
    public String getSubtypeDZ() {
        return subtypeDZ;
    }

    @Override
    public String getTypeKeyWGZ() {
        return typeKeyWGZ;
    }

    @Override
    public LocalizedString getTypeWGZ() {
        return LocalizedString.createDefault(typeWGZ);
    }

    @Override
    public String getSubtypeWGZ() {
        return subtypeWGZ;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}