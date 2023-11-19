package de.marketmaker.istar.domain.instrument;

import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.domain.ItemWithNames;
import de.marketmaker.istar.domain.Language;

/**
 * @author umaurer
 */
public enum CertificateTypeEnum implements ItemWithNames {

    CALL,
    CERT_AIRBAG,
    CERT_BASKET,
    CERT_BONUS,
    CERT_DISCOUNT,
    CERT_EXPRESS,
    CERT_FACTOR,
    CERT_GUARANTEE,
    CERT_INDEX,
    CERT_KNOCKOUT,
    CERT_LOCKIN,
    CERT_MBI,
    CERT_OTHER,
    CERT_OUTPERFORMANCE,
    CERT_REVERSE_CONVERTIBLE,
    CERT_REVERSE_CONVERTIBLE_COM,
    CERT_REVERSE_CONVERTIBLE_CUR,
    CERT_SPRINT,
    CERT_SPRINTER,
    CERT_STRUCTURED_BOND,
    CERT_TWINWIN,
    ESOT_STRUTT,
    ETC,
    ETCS_ETC_LEVERA,
    ETCS_ETC_SHORT,
    ETCS_INDEX_COMM,
    ETCS_INDUSTRIAL,
    ETCS_PRECIOUS_M,
    ETC_BESTIAME,
    ETC_ENERGIA,
    ETC_INDICE_DI_C,
    ETC_LEVERAGED,
    ETC_MATERIE_PRI,
    ETC_METALLI_IND,
    ETC_METALLI_PRE,
    ETC_PRODOTTI_AG,
    ETC_SHORT,
    INV_CERT,
    KNOCK,
    LEV_CERT_BEAR,
    LEV_CERT_BULL,
    PUT,
    WARR_DISCOUNT,
    WARR_OTHER;

    private static final MessageSource MESSAGES
            = MessageSourceFactory.create(CertificateTypeEnum.class);

    public String getDescription() {
        return getName(Language.de);
    }

    public static String getCertificateTypeDescription(String shortcut) {
        if (shortcut == null) {
            return CertificateTypeEnum.CERT_OTHER.getDescription();
        }
        try {
            return CertificateTypeEnum.valueOf(shortcut).getDescription();
        }
        catch (Exception e) {
            LoggerFactory.getLogger(CertificateTypeEnum.class).warn("unknown certificate type: " + shortcut);
            return CertificateTypeEnum.CERT_OTHER.getDescription();
        }
    }

    @Override
    public String getName(Language language) {
        return MESSAGES.getMessage(name(), null, language.getLocale());
    }

    @Override
    public String getNameOrDefault(Language language) {
        String name = getName(language);
        if(name == null) {
            name = getName(Language.en);
            if(name == null) {
                name = getName(Language.de);
            }
        }
        return name;
    }
}
