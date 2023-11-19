/*
 * DocumentType.java
 *
 * Created on 19.03.14 08:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.util.EnumSet;

/**
 * @author oflege
 */
public enum DocumentType {
    AKTIENFLASH_HM2("Unternehmensflash HM2"),
    AKTIENFLASH_HM3("Unternehmensflash HM3"),
    R_F_BANKEN("Credit Research - Financials"),
    R_F_BANKEN_VERBUND("Flash - Banken Verbund"),
    R_F_UNTERNEHMEN("Credit Research - Unternehmensanleihen"),
    R_S_BANKEN("Special - Banken"),
    R_S_UNTERNEHMEN("Special - Unternehmensanleihen"),
    R_CF_BANKEN("Credit Focus - Banken"),
    R_CF_UNTERNEHMEN("Credit Focus - Unternehmensanleihen"),
    R_DAILY_STRATEGIE_CREDITS("Creditmärkte - Daily"),
    DOC_TYPE_SHARE_DAILY("Share Daily" /*todo?*/),
    AKTIENSTRATEGIEFLASH("Flash - Aktienstrategie" /*todo?*/),
    ATA_DAILY_KURZ("Technische Analyse - Daily"),
    ATA_WEEKLY("Technische Analyse - Weekly"),
    OTHER("Sonstige"),
    R_F_EMERGING_MARKETS("Emerging Markets"),
    R_F_SUPRAS_AGENCIES("Supras and Agencies"),
    ASTRAT_ADP("Auf den Punkt"),
    ;

    private static final EnumSet<DocumentType> ASSET_CLASS_SHARE = EnumSet.of(
        ATA_DAILY_KURZ,
        ATA_WEEKLY,
        ASTRAT_ADP
    );

    private final String description;

    public static DocumentType valueOfWithMapping(String s) {
        // phone talk P. Franke / M. Wilke
        // mapping above must be 1:1 resulting in a nice dropdown box, so resolve 1:n the way below

        if ("AKTIENFLASH_KURZV".equals(s) || "AKTIENFLASH_UEBERS_HM2".equals(s)) {
            return AKTIENFLASH_HM2;
        }
        if ("AKTIENFLASH_KURZV_NURHM3".equals(s) || "AKTIENFLASH_UEBERS_NURHM3".equals(s)) {
            return AKTIENFLASH_HM3;
        }
        if ("ATA_WEEKLY_VERBUND".equals(s)) {
            return ATA_WEEKLY;
        }
        if ("R_CF_UNTERNEHMEN_CREDIT".equals(s) || "R_W_U_SP".equals(s)) {
            return R_F_UNTERNEHMEN;
        }
        if ("R_WBI_BANKEN".equals(s) || "R_CF_BANKEN_CREDIT".equals(s)) {
            return R_F_BANKEN;
        }
        if ("R_W_EMERGING_MARKETS".equals(s)) {
            return R_F_EMERGING_MARKETS;
        }
        try {
            return valueOf(s);
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }

    DocumentType(String description) {
        this.description = description;
    }

    public String getAssetClass() {
        return (name().startsWith("AKTIEN") || ASSET_CLASS_SHARE.contains(this))
            ? "Aktien"
            : name().startsWith("R_") ? "Renten" : null;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + "{" + this.description + "}";
    }
}
