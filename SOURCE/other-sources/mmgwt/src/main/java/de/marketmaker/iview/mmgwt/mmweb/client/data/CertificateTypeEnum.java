package de.marketmaker.iview.mmgwt.mmweb.client.data;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Arrays;

/**
 * This class is a copy of de.marketmaker.istar.domain.instrument.CertificateTypeEnum.
 * CertificateTypeEnumTest checks, if the two enums are equal.
 * @author Ulrich Maurer
 */
@NonNLS
public enum CertificateTypeEnum {
    CALL(I18n.I.sedexProductTypeCoveredWarrantPlainVanillaCall(), Scope.VWD),
    CERT_AIRBAG(I18n.I.certAirbag(), Scope.WGZ),
    CERT_BASKET(I18n.I.certBasket(), Scope.DZ, Scope.WGZ),
    CERT_BONUS(I18n.I.certBonus(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_DISCOUNT(I18n.I.certDiscount(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_EXPRESS(I18n.I.certExpress(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_FACTOR(I18n.I.certFactor(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_GUARANTEE(I18n.I.certGuarantee(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_INDEX(I18n.I.certIndex(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_KNOCKOUT(I18n.I.certKnockout(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_LOCKIN(I18n.I.certLockin(), Scope.WGZ),
    CERT_MBI(I18n.I.certMBI(), Scope.WGZ),
    CERT_OTHER(I18n.I.certOthers(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_OUTPERFORMANCE(I18n.I.certOutperformanceAbbr(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_REVERSE_CONVERTIBLE(I18n.I.certReverseConvertible(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_REVERSE_CONVERTIBLE_COM(I18n.I.reverseConvertibleCommodity(), Scope.DZ, Scope.WGZ),
    CERT_REVERSE_CONVERTIBLE_CUR(I18n.I.reverseConvertibleCurrency(), Scope.WGZ),
    CERT_SPRINT(I18n.I.certSprinter(), Scope.DZ, Scope.WGZ),
    CERT_SPRINTER(I18n.I.certSprinter(), Scope.VWD, Scope.DZ, Scope.WGZ),
    CERT_STRUCTURED_BOND(I18n.I.certStructuredBond(), Scope.DZ, Scope.WGZ),
    CERT_TWINWIN(I18n.I.certTwinwin(), Scope.WGZ),
    ESOT_STRUTT(I18n.I.sedexProductTypeStructuredExoticCoveredWarrants(), Scope.VWD),
    ETC("ETC", Scope.VWD, Scope.DZ, Scope.WGZ),
    ETCS_ETC_LEVERA(I18n.I.sedexProductTypeEtcsLeveraged(), Scope.VWD),
    ETCS_ETC_SHORT(I18n.I.sedexProductTypeEtcsShort(), Scope.VWD),
    ETCS_INDEX_COMM(I18n.I.sedexProductTypeEtcsIndexCommodities(), Scope.VWD),
    ETCS_INDUSTRIAL(I18n.I.sedexProductTypeEtcsIndustrialMetals(), Scope.VWD),
    ETCS_PRECIOUS_M(I18n.I.sedexProductTypeEtcsPreciousMetals(), Scope.VWD),
    ETC_BESTIAME(I18n.I.sedexProductTypeEtcCattle(), Scope.VWD),
    ETC_ENERGIA(I18n.I.sedexProductTypeEtcEnergy(), Scope.VWD),
    ETC_INDICE_DI_C(I18n.I.sedexProductTypeEtcIndexCommodities(), Scope.VWD),
    ETC_LEVERAGED(I18n.I.sedexProductTypeEtcLeveraged(), Scope.VWD),
    ETC_MATERIE_PRI(I18n.I.sedexProductTypeEtcRawMaterial(), Scope.VWD),
    ETC_METALLI_IND(I18n.I.sedexProductTypeEtcIndustrialMetals(), Scope.VWD),
    ETC_METALLI_PRE(I18n.I.sedexProductTypeEtcPreciousMetals(), Scope.VWD),
    ETC_PRODOTTI_AG(I18n.I.sedexProductTypeEtcAgriculturalProducts(), Scope.VWD),
    ETC_SHORT(I18n.I.sedexProductTypeEtcShort(), Scope.VWD),
    INV_CERT(I18n.I.sedexProductTypeInvestmentCertificates(), Scope.VWD),
    KNOCK(I18n.I.certKnockout(), Scope.VWD, Scope.DZ, Scope.WGZ),
    LEV_CERT_BEAR(I18n.I.sedexProductTypeStopLossBearCertificates(), Scope.VWD),
    LEV_CERT_BULL(I18n.I.sedexProductTypeStopLossBullCertificates(), Scope.VWD),
    PUT(I18n.I.sedexProductTypeCoveredWarrantPlainVanillaPut(), Scope.VWD),
    WARR_DISCOUNT(I18n.I.warrantDiscountFx(), Scope.DZ, Scope.WGZ),
    WARR_OTHER(I18n.I.warrantOther(), Scope.VWD, Scope.DZ, Scope.WGZ);

    public static enum Scope {
        VWD, DZ, WGZ
    }

    private final String description;

    private final Scope[] scope;

    CertificateTypeEnum(String description, Scope... scope) {
        this.description = description;
        this.scope = scope;
    }

    public String getDescription() {
        return description;
    }

    public boolean isScopeDZ() {
        return Arrays.binarySearch(this.scope, Scope.DZ) >= 0;
    }

    public boolean isScopeWGZ() {
        return Arrays.binarySearch(this.scope, Scope.WGZ) >= 0;
    }

    public boolean isScopeVWD() {
        return Arrays.binarySearch(this.scope, Scope.VWD) >= 0;
    }

    public static String getCertificateTypeDescription(String shortcut) {
        if (shortcut == null) {
            return CertificateTypeEnum.CERT_OTHER.getDescription();
        }
        try {
            return CertificateTypeEnum.valueOf(shortcut).getDescription();
        } catch (Exception e) {
            Firebug.log("CertificateTypeEnum: unknown certificate type: " + shortcut);
            return CertificateTypeEnum.CERT_OTHER.getDescription();
        }
    }

    public static boolean isCertificateAllowed(String type, boolean leverage) {
        try {
            return isCertificateAllowed(valueOf(type), leverage);
        } catch (IllegalArgumentException e) {
            Firebug.log("CertificateTypeEnum: unknown certificate type: " + type);
            return false;
        }
    }

    public static boolean isCertificateAllowed(CertificateTypeEnum type, boolean leverage) {
        if (leverage) {
            return false;
        }
        if (Selector.DZ_BANK_USER.isAllowed() || Selector.DZB_KWT_FUNCTION.isAllowed()) {
            return type.isScopeDZ();
        }
        if (Selector.WGZ_BANK_USER.isAllowed()) {
            return type.isScopeWGZ();
        }
        return type.isScopeVWD();
    }
}