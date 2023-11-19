/*
 * CertificateMasterDataProvider.java
 *
 * Created on 6/12/14 5:34 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;

import com.google.protobuf.InvalidProtocolBufferException;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.data.NullMasterDataCertificate;
import de.marketmaker.istar.domainimpl.data.MasterDataCertificateImpl;
import de.marketmaker.istar.merger.provider.MasterDataProvider;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.merger.provider.protobuf.ProviderProtos;

/**
 * @author Stefan Willenbrock
 */
public class MasterDataCertificateProviderVwd extends ProtobufDataReader implements MasterDataProvider<MasterDataCertificate> {

    public MasterDataCertificateProviderVwd() {
        super(ProviderProtos.MasterDataCertificate.getDescriptor());
    }

    public MasterDataCertificate getMasterData(long instrumentid) {
        ProviderProtos.MasterDataCertificate.Builder builder
                = ProviderProtos.MasterDataCertificate.newBuilder();
        try {
            if (build(instrumentid, builder) && builder.isInitialized()) {
                return asMasterDataCertificate(builder.build());
            }
        } catch (InvalidProtocolBufferException e) {
            this.logger.error("<getMasterData> failed to deserialize data for " + instrumentid, e);
        }
        return NullMasterDataCertificate.INSTANCE;
    }

    private MasterDataCertificate asMasterDataCertificate(
            ProviderProtos.MasterDataCertificate mdc) {

        String category = mdc.hasCategory() ? mdc.getCategory() : null;
        LocalizedString localizedCategory = mdc.getLocalizedCategoryCount() > 0
                ? toLocalizedString(mdc.getLocalizedCategoryList()) : null;
        if (category != null) {
            if (localizedCategory == null) {
                localizedCategory = LocalizedString.createDefault(category);
            }
            else if (localizedCategory.getDe() == null) {
                localizedCategory = localizedCategory.add(Language.de, category);
            }
        }

        String exercisetypeName = mdc.hasExercisetypeName() ? mdc.getExercisetypeName() : null;
        if (exercisetypeName != null && exercisetypeName.startsWith("europ")) {
            exercisetypeName = "europäisch";
        }

        return new MasterDataCertificateImpl(mdc.getIid(),
                mdc.hasTypeKeyVwd() ? mdc.getTypeKeyVwd() : null,
                mdc.getTypeVwdCount() > 0 ? toLocalizedString(mdc.getTypeVwdList()) : null,
                mdc.hasSubtypeKeyVwd() ? mdc.getSubtypeKeyVwd() : null,
                mdc.hasSubtypeVwd() ? mdc.getSubtypeVwd() : null,
                mdc.hasTypeKeyDz() ? mdc.getTypeKeyDz() : null,
                mdc.hasTypeDz() ? mdc.getTypeDz() : null,
                mdc.hasSubtypeKeyDz() ? mdc.getSubtypeKeyDz() : null,
                mdc.hasSubtypeDz() ? mdc.getSubtypeDz() : null,
                mdc.hasTypeKeyWgz() ? mdc.getTypeKeyWgz() : null,
                mdc.hasTypeWgz() ? mdc.getTypeWgz() : null,
                mdc.hasSubtypeWgz() ? mdc.getSubtypeWgz() : null,
                mdc.hasCap() ? toBigDecimal(mdc.getCap()) : null,
                mdc.hasKnockin() ? toBigDecimal(mdc.getKnockin()) : null,
                mdc.hasBonuslevel() ? toBigDecimal(mdc.getBonuslevel()) : null,
                mdc.hasBarrier() ? toBigDecimal(mdc.getBarrier()) : null,
                mdc.hasStartvalue() ? toBigDecimal(mdc.getStartvalue()) : null,
                mdc.hasStopvalue() ? toBigDecimal(mdc.getStopvalue()) : null,
                mdc.hasCharge() ? toBigDecimal(mdc.getCharge()) : null,
                mdc.hasCoupon() ? toBigDecimal(mdc.getCoupon()) : null,
                mdc.hasType() ? mdc.getType() : null,
                mdc.hasTypeKey() ? mdc.getTypeKey() : null,
                mdc.hasFirsttradingday() ? toLocalDate(mdc.getFirsttradingday()) : null,
                mdc.hasLasttradingday() ? toLocalDate(mdc.getLasttradingday()) : null,
                mdc.hasLasttradingdayEuwax() ? toLocalDate(mdc.getLasttradingdayEuwax()) : null,
                mdc.hasGuaranteelevel() ? toBigDecimal(mdc.getGuaranteelevel()) : null,
                exercisetypeName,
                mdc.hasInterestPaymentInterval() ? mdc.getInterestPaymentInterval() : null,
                mdc.hasIssuedate() ? toLocalDate(mdc.getIssuedate()) : null,
                mdc.hasIssuerName() ? mdc.getIssuerName() : null,
                mdc.hasCountryOfIssuerCode() ? mdc.getCountryOfIssuerCode() : null,
                mdc.hasKnockindate() ? toLocalDate(mdc.getKnockindate()) : null,
                mdc.hasKnockout() ? mdc.getKnockout() : null,
                mdc.hasKnockoutdate() ? toDateTime(mdc.getKnockoutdate()) : null,
                mdc.hasLowerBarrierDate() ? toDateTime(mdc.getLowerBarrierDate()) : null,
                mdc.hasUpperBarrierDate() ? toDateTime(mdc.getUpperBarrierDate()) : null,
                mdc.hasNominal() ? toBigDecimal(mdc.getNominal()) : null,
                mdc.hasProtectlevel() ? toBigDecimal(mdc.getProtectlevel()) : null,
                mdc.hasQuantity() ? toBigDecimal(mdc.getQuantity()) : null,
                mdc.hasQuanto() ? mdc.getQuanto() : null,
                mdc.hasRange() ? mdc.getRange() : null,
                mdc.hasRollover() ? mdc.getRollover() : null,
                mdc.hasGuaranteeType() ? mdc.getGuaranteeType() : null,
                mdc.hasLeverageType() ? mdc.getLeverageType() : null,
                mdc.hasStoploss() ? toBigDecimal(mdc.getStoploss()) : null,
                mdc.hasStrikePrice() ? toBigDecimal(mdc.getStrikePrice()) : null,
                mdc.hasIssuePrice() ? toBigDecimal(mdc.getIssuePrice()) : null,
                mdc.hasVariabletradingamount() ? toBigDecimal(mdc.getVariabletradingamount()) : null,
                mdc.hasPriceType() ? mdc.getPriceType() : null,
                mdc.hasAnnotation() ? mdc.getAnnotation() : null,
                mdc.hasProductNameIssuer() ? mdc.getProductNameIssuer() : null,
                category,
                localizedCategory,
                mdc.hasInterestPaymentDate() ? toLocalDate(mdc.getInterestPaymentDate()) : null,
                mdc.hasMaxSpreadEuwax() ? toBigDecimal(mdc.getMaxSpreadEuwax()) : null,
                mdc.hasRefundMaximum() ? toBigDecimal(mdc.getRefundMaximum()) : null,
                mdc.hasRefund() ? toBigDecimal(mdc.getRefund()) : null,
                mdc.hasDeadlineDate1() ? toLocalDate(mdc.getDeadlineDate1()) : null,
                mdc.hasDeadlineDate2() ? toLocalDate(mdc.getDeadlineDate2()) : null,
                mdc.hasDeadlineDate3() ? toLocalDate(mdc.getDeadlineDate3()) : null,
                mdc.hasDeadlineDate4() ? toLocalDate(mdc.getDeadlineDate4()) : null,
                mdc.hasFloor() ? toBigDecimal(mdc.getFloor()) : null,
                mdc.hasParticipationFactor() ? toBigDecimal(mdc.getParticipationFactor()) : null,
                mdc.hasParticipationLevel() ? toBigDecimal(mdc.getParticipationLevel()) : null,
                mdc.hasSubscriptionRatio() ? toBigDecimal(mdc.getSubscriptionRatio()) : null,
                mdc.hasMultiassetName() ? mdc.getMultiassetName() : null,
                mdc.hasCurrencyStrike() ? mdc.getCurrencyStrike() : null,
                mdc.getBasketIidsCount() > 0 ? mdc.getBasketIidsList() : null,
                mdc.hasSettlementday() ? toLocalDate(mdc.getSettlementday()) : null
        );
    }

    public static void main(String[] args) throws Exception {
        MasterDataCertificateProviderVwd mdp = new MasterDataCertificateProviderVwd();
        mdp.setFile(new File("D:/tmp/istar-gatrixx-certificate-masterdata.20150429.060718.buf"));
        mdp.afterPropertiesSet();
        System.out.println(mdp.getMasterData(185668408L));
        mdp.destroy();

        System.out.println("***********************************************");

        mdp = new MasterDataCertificateProviderVwd();
        mdp.setFile(new File("D:/tmp/istar-gatrixx-certificate-masterdata.20150504.063757.buf"));
        mdp.afterPropertiesSet();
        System.out.println(mdp.getMasterData(185668408L));
        mdp.destroy();

        System.out.println("***********************************************");

        mdp = new MasterDataCertificateProviderVwd();
        mdp.setFile(new File("D:/tmp/istar-gatrixx-certificate-masterdata.20150504.130041.buf"));
        mdp.afterPropertiesSet();
        System.out.println(mdp.getMasterData(185668408L));
        mdp.destroy();


    }
}
