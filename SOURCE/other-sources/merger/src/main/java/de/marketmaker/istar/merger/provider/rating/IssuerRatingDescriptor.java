/*
 * IssuerRatingDescriptor.java
 *
 * Created on 04.05.12 15:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * @author zzhao
 */
public enum IssuerRatingDescriptor {

    SOURCE("source"),
    ISSUERNAME("issuername"),
    LEI("lei"),
    VWDSYMBOL("vwdsymbol"),
    COUNTRYISO("countryIso"),
    CURRENCYISO("currencyIso"),

    RATING_FITCH_ISSUER_LT("ratingFitchIssuerLongTerm", true, Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_LT_ACTION("ratingFitchIssuerLongTermAction", Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_LT_DATE("ratingFitchIssuerLongTermDate", Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_ST("ratingFitchIssuerShortTerm", true, Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_ST_ACTION("ratingFitchIssuerShortTermAction", Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_ST_DATE("ratingFitchIssuerShortTermDate", Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_IFS("ratingFitchIssuerIFS", true, Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_IFS_ACTION("ratingFitchIssuerIFSAction", Selector.RATING_FITCH),
    RATING_FITCH_ISSUER_IFS_DATE("ratingFitchIssuerIFSDate", Selector.RATING_FITCH),

    // Counterparty Rating (CTP)
    RATING_MDYS_ISSR_LT("ratingMoodysIssuerLongTerm", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A("ratingMoodysIssuerLongTermAction", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D("ratingMoodysIssuerLongTermDate", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST("ratingMoodysIssuerShortTerm", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A("ratingMoodysIssuerShortTermAction", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D("ratingMoodysIssuerShortTermDate", Selector.RATING_MOODYS),

    // Counterparty Rating (CTP) backed
    RATING_MDYS_ISSR_LT_B("ratingMoodysIssuerLongTermBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_B("ratingMoodysIssuerLongTermActionBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_B("ratingMoodysIssuerLongTermDateBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_B("ratingMoodysIssuerShortTermBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_B("ratingMoodysIssuerShortTermActionBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_B("ratingMoodysIssuerShortTermDateBacked", Selector.RATING_MOODYS),

    // Senior Unsecured Rating (SU)
    RATING_MDYS_ISSR_LT_SU("ratingMoodysIssuerLongTermSu", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_SU("ratingMoodysIssuerLongTermActionSu", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_SU("ratingMoodysIssuerLongTermDateSu", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_SU("ratingMoodysIssuerShortTermSu", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_SU("ratingMoodysIssuerShortTermActionSu", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_SU("ratingMoodysIssuerShortTermDateSu", Selector.RATING_MOODYS),

    // Senior Unsecured Rating (SU) backed
    RATING_MDYS_ISSR_LT_SU_B("ratingMoodysIssuerLongTermSuBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_SU_B("ratingMoodysIssuerLongTermActionSuBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_SU_B("ratingMoodysIssuerLongTermDateSuBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_SU_B("ratingMoodysIssuerShortTermSuBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_SU_B("ratingMoodysIssuerShortTermActionSuBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_SU_B("ratingMoodysIssuerShortTermDateSuBacked", Selector.RATING_MOODYS),

    // Bank Deposit Rating (BDR)
    RATING_MDYS_ISSR_LT_BDR("ratingMoodysIssuerLongTermBdr", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_BDR("ratingMoodysIssuerLongTermActionBdr", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_BDR("ratingMoodysIssuerLongTermDateBdr", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_BDR("ratingMoodysIssuerShortTermBdr", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_BDR("ratingMoodysIssuerShortTermActionBdr", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_BDR("ratingMoodysIssuerShortTermDateBdr", Selector.RATING_MOODYS),

    // Bank Deposit Rating (BDR) backed
    RATING_MDYS_ISSR_LT_BDR_B("ratingMoodysIssuerLongTermBdrBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_BDR_B("ratingMoodysIssuerLongTermActionBdrBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_BDR_B("ratingMoodysIssuerLongTermDateBdrBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_BDR_B("ratingMoodysIssuerShortTermBdrBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_BDR_B("ratingMoodysIssuerShortTermActionBdrBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_BDR_B("ratingMoodysIssuerShortTermDateBdrBacked", Selector.RATING_MOODYS),

    // Insurance Financial Strength Rating (IFS)
    RATING_MDYS_ISSR_LT_IFSR("ratingMoodysIssuerLongTermIfsr", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_IFSR("ratingMoodysIssuerLongTermActionIfsr", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_IFSR("ratingMoodysIssuerLongTermDateIfsr", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_IFSR("ratingMoodysIssuerShortTermIfsr", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_IFSR("ratingMoodysIssuerShortTermActionIfsr", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_IFSR("ratingMoodysIssuerShortTermDateIfsr", Selector.RATING_MOODYS),

    // Insurance Financial Strength Rating (IFS) backed
    RATING_MDYS_ISSR_LT_IFSR_B("ratingMoodysIssuerLongTermIfsrBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_A_IFSR_B("ratingMoodysIssuerLongTermActionIfsrBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_LT_D_IFSR_B("ratingMoodysIssuerLongTermDateIfsrBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_IFSR_B("ratingMoodysIssuerShortTermIfsrBacked", true, Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_A_IFSR_B("ratingMoodysIssuerShortTermActionIfsrBacked", Selector.RATING_MOODYS),
    RATING_MDYS_ISSR_ST_D_IFSR_B("ratingMoodysIssuerShortTermDateIfsrBacked", Selector.RATING_MOODYS),

    RATING_SNP_ISSUER_LT("ratingStandardAndPoorsIssuerLongTerm", true, Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_ACTION("ratingStandardAndPoorsIssuerLongTermAction", Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_DATE("ratingStandardAndPoorsIssuerLongTermDate", Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_RID("ratingStandardAndPoorsIssuerLongTermRegulatoryId", Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST("ratingStandardAndPoorsIssuerShortTerm", true, Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_ACTION("ratingStandardAndPoorsIssuerShortTermAction", Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_DATE("ratingStandardAndPoorsIssuerShortTermDate", Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_FSR("ratingStandardAndPoorsIssuerLongTermFSR", true, Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_FSR_ACTN("ratingStandardAndPoorsIssuerLongTermActionFSR", Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_FSR_DATE("ratingStandardAndPoorsIssuerLongTermDateFSR", Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_FSR("ratingStandardAndPoorsIssuerShortTermFSR", true, Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_FSR_ACTN("ratingStandardAndPoorsIssuerShortTermActionFSR", Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_FSR_DATE("ratingStandardAndPoorsIssuerShortTermDateFSR", Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_FER("ratingStandardAndPoorsIssuerLongTermFER", true, Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_FER_ACTN("ratingStandardAndPoorsIssuerLongTermActionFER", Selector.RATING_SuP),
    RATING_SNP_ISSUER_LT_FER_DATE("ratingStandardAndPoorsIssuerLongTermDateFER", Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_FER("ratingStandardAndPoorsIssuerShortTermFER", true, Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_FER_ACTN("ratingStandardAndPoorsIssuerShortTermActionFER", Selector.RATING_SuP),
    RATING_SNP_ISSUER_ST_FER_DATE("ratingStandardAndPoorsIssuerShortTermDateFER", Selector.RATING_SuP);


    private final String value;

    private final boolean rating;

    private final Selector[] selectors;

    IssuerRatingDescriptor(String value) {
        this(value, false);
    }

    IssuerRatingDescriptor(String value, Selector... selectors) {
        this(value, false, selectors);
    }

    IssuerRatingDescriptor(String value, boolean rating, Selector... selectors) {
        this.value = value;
        this.rating = rating;
        this.selectors = selectors;
    }

    public String getValue() {
        return value;
    }

    public boolean isRating() {
        return rating;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public static IssuerRatingDescriptor fromValue(String val) {
        for (IssuerRatingDescriptor desc : values()) {
            if (desc.value.equalsIgnoreCase(val)) {
                return desc;
            }
        }

        throw new IllegalArgumentException("no issuer rating descriptor matches: " + val);
    }

    public boolean accept(Profile profile) {
        if (null == this.selectors || this.selectors.length == 0) {
            return true;
        }

        for (Selector selector : selectors) {
            if (profile.isAllowed(selector)) {
                return true;
            }
        }

        return false;
    }
}
