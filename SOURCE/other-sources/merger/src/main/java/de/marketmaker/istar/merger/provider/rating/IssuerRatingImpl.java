/*
 * IssuerRating.java
 *
 * Created on 04.05.12 15:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import de.marketmaker.istar.merger.provider.rating.Entry.Action;
import org.joda.time.LocalDate;

/**
 * @author zzhao
 */
public class IssuerRatingImpl implements IssuerRating {

    static final long serialVersionUID = 275995441386662516L;

    private RatingSource source;

    private String issuerName;

    private String lei;

    private String vwdSymbol;

    private String countryIso;

    private String currencyIso;

    private String ratingFitchIssuerLongTerm;

    private Entry.Action ratingFitchIssuerLongTermAction;

    private LocalDate ratingFitchIssuerLongTermDate;

    private String ratingFitchIssuerShortTerm;

    private Entry.Action ratingFitchIssuerShortTermAction;

    private LocalDate ratingFitchIssuerShortTermDate;

    private String ratingFitchIssuerIFS;

    private Entry.Action ratingFitchIssuerIFSAction;

    private LocalDate ratingFitchIssuerIFSDate;

    // Counterparty Rating (CTP)

    private String ratingMoodysIssuerLongTerm;

    private Entry.Action ratingMoodysIssuerLongTermAction;

    private LocalDate ratingMoodysIssuerLongTermDate;

    private String ratingMoodysIssuerShortTerm;

    private Entry.Action ratingMoodysIssuerShortTermAction;

    private LocalDate ratingMoodysIssuerShortTermDate;

    // Counterparty Rating (CTP) backed

    private String ratingMoodysIssuerLongTermBacked;

    private Entry.Action ratingMoodysIssuerLongTermActionBacked;

    private LocalDate ratingMoodysIssuerLongTermDateBacked;

    private String ratingMoodysIssuerShortTermBacked;

    private Entry.Action ratingMoodysIssuerShortTermActionBacked;

    private LocalDate ratingMoodysIssuerShortTermDateBacked;

    // Senior Unsecured Rating (SU)

    private String ratingMoodysIssuerLongTermSu;

    private Entry.Action ratingMoodysIssuerLongTermActionSu;

    private LocalDate ratingMoodysIssuerLongTermDateSu;

    private String ratingMoodysIssuerShortTermSu;

    private Entry.Action ratingMoodysIssuerShortTermActionSu;

    private LocalDate ratingMoodysIssuerShortTermDateSu;

    // Senior Unsecured Rating (SU) backed

    private String ratingMoodysIssuerLongTermSuBacked;

    private Entry.Action ratingMoodysIssuerLongTermActionSuBacked;

    private LocalDate ratingMoodysIssuerLongTermDateSuBacked;

    private String ratingMoodysIssuerShortTermSuBacked;

    private Entry.Action ratingMoodysIssuerShortTermActionSuBacked;

    private LocalDate ratingMoodysIssuerShortTermDateSuBacked;

    // Bank Deposit Rating (BDR)

    private String ratingMoodysIssuerLongTermBdr;

    private Entry.Action ratingMoodysIssuerLongTermActionBdr;

    private LocalDate ratingMoodysIssuerLongTermDateBdr;

    private String ratingMoodysIssuerShortTermBdr;

    private Entry.Action ratingMoodysIssuerShortTermActionBdr;

    private LocalDate ratingMoodysIssuerShortTermDateBdr;

    // Bank Deposit Rating (BDR) backed

    private String ratingMoodysIssuerLongTermBdrBacked;

    private Entry.Action ratingMoodysIssuerLongTermActionBdrBacked;

    private LocalDate ratingMoodysIssuerLongTermDateBdrBacked;

    private String ratingMoodysIssuerShortTermBdrBacked;

    private Entry.Action ratingMoodysIssuerShortTermActionBdrBacked;

    private LocalDate ratingMoodysIssuerShortTermDateBdrBacked;

    // Insurance Financial Strength Rating (IFS)

    private String ratingMoodysIssuerLongTermIfsr;

    private Entry.Action ratingMoodysIssuerLongTermActionIfsr;

    private LocalDate ratingMoodysIssuerLongTermDateIfsr;

    private String ratingMoodysIssuerShortTermIfsr;

    private Entry.Action ratingMoodysIssuerShortTermActionIfsr;

    private LocalDate ratingMoodysIssuerShortTermDateIfsr;

    // Insurance Financial Strength Rating (IFS) backed

    private String ratingMoodysIssuerLongTermIfsrBacked;

    private Entry.Action ratingMoodysIssuerLongTermActionIfsrBacked;

    private LocalDate ratingMoodysIssuerLongTermDateIfsrBacked;

    private String ratingMoodysIssuerShortTermIfsrBacked;

    private Entry.Action ratingMoodysIssuerShortTermActionIfsrBacked;

    private LocalDate ratingMoodysIssuerShortTermDateIfsrBacked;

    // ---
    
    private String ratingStandardAndPoorsIssuerLongTerm;

    private Entry.Action ratingStandardAndPoorsIssuerLongTermAction;

    private LocalDate ratingStandardAndPoorsIssuerLongTermDate;

    private Entry.RegulatoryId ratingStandardAndPoorsIssuerLongTermRegulatoryId;

    private String ratingStandardAndPoorsIssuerShortTerm;

    private Entry.Action ratingStandardAndPoorsIssuerShortTermAction;

    private LocalDate ratingStandardAndPoorsIssuerShortTermDate;

    private String ratingStandardAndPoorsIssuerLongTermFSR;

    private Entry.Action ratingStandardAndPoorsIssuerLongTermActionFSR;

    private LocalDate ratingStandardAndPoorsIssuerLongTermDateFSR;

    private String ratingStandardAndPoorsIssuerShortTermFSR;

    private Entry.Action ratingStandardAndPoorsIssuerShortTermActionFSR;

    private LocalDate ratingStandardAndPoorsIssuerShortTermDateFSR;

    private String ratingStandardAndPoorsIssuerLongTermFER;

    private Entry.Action ratingStandardAndPoorsIssuerLongTermActionFER;

    private LocalDate ratingStandardAndPoorsIssuerLongTermDateFER;

    private String ratingStandardAndPoorsIssuerShortTermFER;

    private Entry.Action ratingStandardAndPoorsIssuerShortTermActionFER;

    private LocalDate ratingStandardAndPoorsIssuerShortTermDateFER;


    public IssuerRatingImpl() {
    }

    void setProperty(IssuerRatingDescriptor desc, Object val) {
        switch (desc) {
            case SOURCE:
                setSource((RatingSource) val);
                break;
            case VWDSYMBOL:
                setVwdSymbol((String) val);
                break;
            case ISSUERNAME:
                setIssuerName((String) val);
                break;
            case LEI:
                setLei((String) val);
                break;
            case COUNTRYISO:
                setCountryIso((String) val);
                break;
            case CURRENCYISO:
                setCurrencyIso((String) val);
                break;
            case RATING_FITCH_ISSUER_LT:
                setRatingFitchIssuerLongTerm((String) val);
                break;
            case RATING_FITCH_ISSUER_LT_ACTION:
                setRatingFitchIssuerLongTermAction((Entry.Action) val);
                break;
            case RATING_FITCH_ISSUER_LT_DATE:
                setRatingFitchIssuerLongTermDate((LocalDate) val);
                break;
            case RATING_FITCH_ISSUER_ST:
                setRatingFitchIssuerShortTerm((String) val);
                break;
            case RATING_FITCH_ISSUER_ST_ACTION:
                setRatingFitchIssuerShortTermAction((Entry.Action) val);
                break;
            case RATING_FITCH_ISSUER_ST_DATE:
                setRatingFitchIssuerShortTermDate((LocalDate) val);
                break;
            case RATING_FITCH_ISSUER_IFS:
                setRatingFitchIssuerIFS((String) val);
                break;
            case RATING_FITCH_ISSUER_IFS_ACTION:
                setRatingFitchIssuerIFSAction((Entry.Action) val);
                break;
            case RATING_FITCH_ISSUER_IFS_DATE:
                setRatingFitchIssuerIFSDate((LocalDate) val);
                break;
            // Counterparty Rating (CTP)
            case RATING_MDYS_ISSR_LT:
                setRatingMoodysIssuerLongTerm((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A:
                setRatingMoodysIssuerLongTermAction((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D:
                setRatingMoodysIssuerLongTermDate((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST:
                setRatingMoodysIssuerShortTerm((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A:
                setRatingMoodysIssuerShortTermAction((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D:
                setRatingMoodysIssuerShortTermDate((LocalDate) val);
                break;
            // Counterparty Rating (CTP) backed
            case RATING_MDYS_ISSR_LT_B:
                setRatingMoodysIssuerLongTermBacked((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_B:
                setRatingMoodysIssuerLongTermActionBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_B:
                setRatingMoodysIssuerLongTermDateBacked((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_B:
                setRatingMoodysIssuerShortTermBacked((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_B:
                setRatingMoodysIssuerShortTermActionBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_B:
                setRatingMoodysIssuerShortTermDateBacked((LocalDate) val);
                break;
            // Senior Unsecured Rating (SU)
            case RATING_MDYS_ISSR_LT_SU:
                setRatingMoodysIssuerLongTermSu((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_SU:
                setRatingMoodysIssuerLongTermActionSu((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_SU:
                setRatingMoodysIssuerLongTermDateSu((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_SU:
                setRatingMoodysIssuerShortTermSu((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_SU:
                setRatingMoodysIssuerShortTermActionSu((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_SU:
                setRatingMoodysIssuerShortTermDateSu((LocalDate) val);
                break;
            // Senior Unsecured Rating (SU) backed
            case RATING_MDYS_ISSR_LT_SU_B:
                setRatingMoodysIssuerLongTermSuBacked((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_SU_B:
                setRatingMoodysIssuerLongTermActionSuBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_SU_B:
                setRatingMoodysIssuerLongTermDateSuBacked((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_SU_B:
                setRatingMoodysIssuerShortTermSuBacked((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_SU_B:
                setRatingMoodysIssuerShortTermActionSuBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_SU_B:
                setRatingMoodysIssuerShortTermDateSuBacked((LocalDate) val);
                break;
            // Bank Deposit Rating (BDR)
            case RATING_MDYS_ISSR_LT_BDR:
                setRatingMoodysIssuerLongTermBdr((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_BDR:
                setRatingMoodysIssuerLongTermActionBdr((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_BDR:
                setRatingMoodysIssuerLongTermDateBdr((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_BDR:
                setRatingMoodysIssuerShortTermBdr((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_BDR:
                setRatingMoodysIssuerShortTermActionBdr((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_BDR:
                setRatingMoodysIssuerShortTermDateBdr((LocalDate) val);
                break;
            // Bank Deposit Rating (BDR) backed
            case RATING_MDYS_ISSR_LT_BDR_B:
                setRatingMoodysIssuerLongTermBdrBacked((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_BDR_B:
                setRatingMoodysIssuerLongTermActionBdrBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_BDR_B:
                setRatingMoodysIssuerLongTermDateBdrBacked((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_BDR_B:
                setRatingMoodysIssuerShortTermBdrBacked((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_BDR_B:
                setRatingMoodysIssuerShortTermActionBdrBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_BDR_B:
                setRatingMoodysIssuerShortTermDateBdrBacked((LocalDate) val);
                break;
            // Insurance Financial Strength Rating (IFS)
            case RATING_MDYS_ISSR_LT_IFSR:
                setRatingMoodysIssuerLongTermIfsr((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_IFSR:
                setRatingMoodysIssuerLongTermActionIfsr((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_IFSR:
                setRatingMoodysIssuerLongTermDateIfsr((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_IFSR:
                setRatingMoodysIssuerShortTermIfsr((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_IFSR:
                setRatingMoodysIssuerShortTermActionIfsr((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_IFSR:
                setRatingMoodysIssuerShortTermDateIfsr((LocalDate) val);
                break;
            // Insurance Financial Strength Rating (IFS) backed
            case RATING_MDYS_ISSR_LT_IFSR_B:
                setRatingMoodysIssuerLongTermIfsrBacked((String) val);
                break;
            case RATING_MDYS_ISSR_LT_A_IFSR_B:
                setRatingMoodysIssuerLongTermActionIfsrBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_LT_D_IFSR_B:
                setRatingMoodysIssuerLongTermDateIfsrBacked((LocalDate) val);
                break;
            case RATING_MDYS_ISSR_ST_IFSR_B:
                setRatingMoodysIssuerShortTermIfsrBacked((String) val);
                break;
            case RATING_MDYS_ISSR_ST_A_IFSR_B:
                setRatingMoodysIssuerShortTermActionIfsrBacked((Entry.Action) val);
                break;
            case RATING_MDYS_ISSR_ST_D_IFSR_B:
                setRatingMoodysIssuerShortTermDateIfsrBacked((LocalDate) val);
                break;
            // ---
            case RATING_SNP_ISSUER_LT:
                setRatingStandardAndPoorsIssuerLongTerm((String) val);
                break;
            case RATING_SNP_ISSUER_LT_ACTION:
                setRatingStandardAndPoorsIssuerLongTermAction((Entry.Action) val);
                break;
            case RATING_SNP_ISSUER_LT_DATE:
                setRatingStandardAndPoorsIssuerLongTermDate((LocalDate) val);
                break;
            case RATING_SNP_ISSUER_LT_RID:
                setRatingStandardAndPoorsIssuerLongTermRegulatoryId((Entry.RegulatoryId) val);
                break;
            case RATING_SNP_ISSUER_ST:
                setRatingStandardAndPoorsIssuerShortTerm((String) val);
                break;
            case RATING_SNP_ISSUER_ST_ACTION:
                setRatingStandardAndPoorsIssuerShortTermAction((Entry.Action) val);
                break;
            case RATING_SNP_ISSUER_ST_DATE:
                setRatingStandardAndPoorsIssuerShortTermDate((LocalDate) val);
                break;
            case RATING_SNP_ISSUER_LT_FSR:
                setRatingStandardAndPoorsIssuerLongTermFSR((String) val);
                break;
            case RATING_SNP_ISSUER_LT_FSR_ACTN:
                setRatingStandardAndPoorsIssuerLongTermActionFSR((Entry.Action) val);
                break;
            case RATING_SNP_ISSUER_LT_FSR_DATE:
                setRatingStandardAndPoorsIssuerLongTermDateFSR((LocalDate) val);
                break;
            case RATING_SNP_ISSUER_ST_FSR:
                setRatingStandardAndPoorsIssuerShortTermFSR((String) val);
                break;
            case RATING_SNP_ISSUER_ST_FSR_ACTN:
                setRatingStandardAndPoorsIssuerShortTermActionFSR((Entry.Action) val);
                break;
            case RATING_SNP_ISSUER_ST_FSR_DATE:
                setRatingStandardAndPoorsIssuerShortTermDateFSR((LocalDate) val);
                break;
            case RATING_SNP_ISSUER_LT_FER:
                setRatingStandardAndPoorsIssuerLongTermFER((String) val);
                break;
            case RATING_SNP_ISSUER_LT_FER_ACTN:
                setRatingStandardAndPoorsIssuerLongTermActionFER((Entry.Action) val);
                break;
            case RATING_SNP_ISSUER_LT_FER_DATE:
                setRatingStandardAndPoorsIssuerLongTermDateFER((LocalDate) val);
                break;
            case RATING_SNP_ISSUER_ST_FER:
                setRatingStandardAndPoorsIssuerShortTermFER((String) val);
                break;
            case RATING_SNP_ISSUER_ST_FER_ACTN:
                setRatingStandardAndPoorsIssuerShortTermActionFER((Entry.Action) val);
                break;
            case RATING_SNP_ISSUER_ST_FER_DATE:
                setRatingStandardAndPoorsIssuerShortTermDateFER((LocalDate) val);
                break;
            default:
                throw new UnsupportedOperationException("no support for: " + desc);
        }
    }

    void setSource(RatingSource source) {
        this.source = source;
    }

    void setVwdSymbol(String vwdSymbol) {
        this.vwdSymbol = vwdSymbol;
    }

    // Counterparty Rating (CTP)

    void setRatingMoodysIssuerLongTerm(String ratingMoodysIssuerLongTerm) {
        this.ratingMoodysIssuerLongTerm = ratingMoodysIssuerLongTerm;
    }

    void setRatingMoodysIssuerLongTermAction(Entry.Action ratingMoodysIssuerLongTermAction) {
        this.ratingMoodysIssuerLongTermAction = ratingMoodysIssuerLongTermAction;
    }

    void setRatingMoodysIssuerLongTermDate(LocalDate ratingMoodysIssuerLongTermDate) {
        this.ratingMoodysIssuerLongTermDate = ratingMoodysIssuerLongTermDate;
    }

    void setRatingMoodysIssuerShortTerm(String ratingMoodysIssuerShortTerm) {
        this.ratingMoodysIssuerShortTerm = ratingMoodysIssuerShortTerm;
    }

    void setRatingMoodysIssuerShortTermAction(
            Entry.Action ratingMoodysIssuerShortTermAction) {
        this.ratingMoodysIssuerShortTermAction = ratingMoodysIssuerShortTermAction;
    }

    void setRatingMoodysIssuerShortTermDate(LocalDate ratingMoodysIssuerShortTermDate) {
        this.ratingMoodysIssuerShortTermDate = ratingMoodysIssuerShortTermDate;
    }

    // Counterparty Rating (CTP) backed

    public void setRatingMoodysIssuerLongTermBacked(String rating) {
        this.ratingMoodysIssuerLongTermBacked = rating;
    }

    public void setRatingMoodysIssuerLongTermActionBacked(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionBacked = action;
    }

    public void setRatingMoodysIssuerLongTermDateBacked(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateBacked = date;
    }

    public void setRatingMoodysIssuerShortTermBacked(String rating) {
        this.ratingMoodysIssuerShortTermBacked = rating;
    }

    public void setRatingMoodysIssuerShortTermActionBacked(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionBacked = action;
    }

    public void setRatingMoodysIssuerShortTermDateBacked(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateBacked = date;
    }

    // Senior Unsecured Rating (SU)

    public void setRatingMoodysIssuerLongTermSu(String rating) {
        this.ratingMoodysIssuerLongTermSu = rating;
    }

    public void setRatingMoodysIssuerLongTermActionSu(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionSu = action;
    }

    public void setRatingMoodysIssuerLongTermDateSu(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateSu = date;
    }

    public void setRatingMoodysIssuerShortTermSu(String rating) {
        this.ratingMoodysIssuerShortTermSu = rating;
    }

    public void setRatingMoodysIssuerShortTermActionSu(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionSu = action;
    }

    public void setRatingMoodysIssuerShortTermDateSu(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateSu = date;
    }

    // Senior Unsecured Rating (SU) backed

    public void setRatingMoodysIssuerLongTermSuBacked(String rating) {
        this.ratingMoodysIssuerLongTermSuBacked = rating;
    }

    public void setRatingMoodysIssuerLongTermActionSuBacked(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionSuBacked = action;
    }

    public void setRatingMoodysIssuerLongTermDateSuBacked(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateSuBacked = date;
    }

    public void setRatingMoodysIssuerShortTermSuBacked(String rating) {
        this.ratingMoodysIssuerShortTermSuBacked = rating;
    }

    public void setRatingMoodysIssuerShortTermActionSuBacked(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionSuBacked = action;
    }

    public void setRatingMoodysIssuerShortTermDateSuBacked(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateSuBacked = date;
    }

    // Bank Deposit Rating (BDR)

    public void setRatingMoodysIssuerLongTermBdr(String rating) {
        this.ratingMoodysIssuerLongTermBdr = rating;
    }

    public void setRatingMoodysIssuerLongTermActionBdr(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionBdr = action;
    }

    public void setRatingMoodysIssuerLongTermDateBdr(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateBdr = date;
    }

    public void setRatingMoodysIssuerShortTermBdr(String rating) {
        this.ratingMoodysIssuerShortTermBdr = rating;
    }

    public void setRatingMoodysIssuerShortTermActionBdr(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionBdr = action;
    }

    public void setRatingMoodysIssuerShortTermDateBdr(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateBdr = date;
    }

    // Bank Deposit Rating (BDR) backed

    public void setRatingMoodysIssuerLongTermBdrBacked(String rating) {
        this.ratingMoodysIssuerLongTermBdrBacked = rating;
    }

    public void setRatingMoodysIssuerLongTermActionBdrBacked(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionBdrBacked = action;
    }

    public void setRatingMoodysIssuerLongTermDateBdrBacked(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateBdrBacked = date;
    }

    public void setRatingMoodysIssuerShortTermBdrBacked(String rating) {
        this.ratingMoodysIssuerShortTermBdrBacked = rating;
    }

    public void setRatingMoodysIssuerShortTermActionBdrBacked(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionBdrBacked = action;
    }

    public void setRatingMoodysIssuerShortTermDateBdrBacked(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateBdrBacked = date;
    }

    // Insurance Financial Strength Rating (IFS)

    public void setRatingMoodysIssuerLongTermIfsr(String rating) {
        this.ratingMoodysIssuerLongTermIfsr = rating;
    }

    public void setRatingMoodysIssuerLongTermActionIfsr(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionIfsr = action;
    }

    public void setRatingMoodysIssuerLongTermDateIfsr(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateIfsr = date;
    }

    public void setRatingMoodysIssuerShortTermIfsr(String rating) {
        this.ratingMoodysIssuerShortTermIfsr = rating;
    }

    public void setRatingMoodysIssuerShortTermActionIfsr(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionIfsr = action;
    }

    public void setRatingMoodysIssuerShortTermDateIfsr(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateIfsr = date;
    }

    // Insurance Financial Strength Rating (IFS) backed

    public void setRatingMoodysIssuerLongTermIfsrBacked(String rating) {
        this.ratingMoodysIssuerLongTermIfsrBacked = rating;
    }

    public void setRatingMoodysIssuerLongTermActionIfsrBacked(Entry.Action action) {
        this.ratingMoodysIssuerLongTermActionIfsrBacked = action;
    }

    public void setRatingMoodysIssuerLongTermDateIfsrBacked(LocalDate date) {
        this.ratingMoodysIssuerLongTermDateIfsrBacked = date;
    }

    public void setRatingMoodysIssuerShortTermIfsrBacked(String rating) {
        this.ratingMoodysIssuerShortTermIfsrBacked = rating;
    }

    public void setRatingMoodysIssuerShortTermActionIfsrBacked(Entry.Action action) {
        this.ratingMoodysIssuerShortTermActionIfsrBacked = action;
    }

    public void setRatingMoodysIssuerShortTermDateIfsrBacked(LocalDate date) {
        this.ratingMoodysIssuerShortTermDateIfsrBacked = date;
    }
    
    // ---

    void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    void setLei(String lei) {
        this.lei = lei;
    }

    void setCountryIso(String countryIso) {
        this.countryIso = countryIso;
    }

    void setCurrencyIso(String currencyIso) {
        this.currencyIso = currencyIso;
    }

    void setRatingFitchIssuerLongTerm(String ratingFitchIssuerLongTerm) {
        this.ratingFitchIssuerLongTerm = ratingFitchIssuerLongTerm;
    }

    void setRatingFitchIssuerLongTermAction(Entry.Action ratingFitchIssuerLongTermAction) {
        this.ratingFitchIssuerLongTermAction = ratingFitchIssuerLongTermAction;
    }

    void setRatingFitchIssuerLongTermDate(LocalDate ratingFitchIssuerLongTermDate) {
        this.ratingFitchIssuerLongTermDate = ratingFitchIssuerLongTermDate;
    }

    void setRatingFitchIssuerShortTerm(String ratingFitchIssuerShortTerm) {
        this.ratingFitchIssuerShortTerm = ratingFitchIssuerShortTerm;
    }

    void setRatingFitchIssuerShortTermAction(Entry.Action ratingFitchIssuerShortTermAction) {
        this.ratingFitchIssuerShortTermAction = ratingFitchIssuerShortTermAction;
    }

    void setRatingFitchIssuerShortTermDate(LocalDate ratingFitchIssuerShortTermDate) {
        this.ratingFitchIssuerShortTermDate = ratingFitchIssuerShortTermDate;
    }

    void setRatingFitchIssuerIFS(String ratingFitchIssuerIFS) {
        this.ratingFitchIssuerIFS = ratingFitchIssuerIFS;
    }

    void setRatingFitchIssuerIFSAction(Entry.Action ratingFitchIssuerIFSAction) {
        this.ratingFitchIssuerIFSAction = ratingFitchIssuerIFSAction;
    }

    void setRatingFitchIssuerIFSDate(LocalDate ratingFitchIssuerIFSDate) {
        this.ratingFitchIssuerIFSDate = ratingFitchIssuerIFSDate;
    }

    public String getRatingMoodysIssuerBFS() {
        return null;
    }

    public Entry.Action getRatingMoodysIssuerBFSAction() {
        return null;
    }

    public LocalDate getRatingMoodysIssuerBFSDate() {
        return null;
    }

    @Override
    public String getIssuerName() {
        return issuerName;
    }

    @Override
    public String getLei() {
        return lei;
    }

    @Override
    public String getVwdSymbol() {
        return vwdSymbol;
    }

    @Override
    public String getCountryIso() {
        return countryIso;
    }

    @Override
    public String getCurrencyIso() {
        return currencyIso;
    }

    @Override
    public String getRatingFitchIssuerLongTerm() {
        return ratingFitchIssuerLongTerm;
    }

    @Override
    public Entry.Action getRatingFitchIssuerLongTermAction() {
        return ratingFitchIssuerLongTermAction;
    }

    @Override
    public LocalDate getRatingFitchIssuerLongTermDate() {
        return ratingFitchIssuerLongTermDate;
    }

    @Override
    public String getRatingFitchIssuerShortTerm() {
        return ratingFitchIssuerShortTerm;
    }

    @Override
    public Entry.Action getRatingFitchIssuerShortTermAction() {
        return ratingFitchIssuerShortTermAction;
    }

    @Override
    public LocalDate getRatingFitchIssuerShortTermDate() {
        return ratingFitchIssuerShortTermDate;
    }

    @Override
    public String getRatingFitchIssuerIFS() {
        return ratingFitchIssuerIFS;
    }

    @Override
    public Entry.Action getRatingFitchIssuerIFSAction() {
        return ratingFitchIssuerIFSAction;
    }

    @Override
    public LocalDate getRatingFitchIssuerIFSDate() {
        return ratingFitchIssuerIFSDate;
    }

    public RatingSource getSource() {
        return source;
    }

    // Counterparty Rating (CTP)

    public String getRatingMoodysIssuerLongTerm() {
        return ratingMoodysIssuerLongTerm;
    }

    public Entry.Action getRatingMoodysIssuerLongTermAction() {
        return ratingMoodysIssuerLongTermAction;
    }

    public LocalDate getRatingMoodysIssuerLongTermDate() {
        return ratingMoodysIssuerLongTermDate;
    }

    public String getRatingMoodysIssuerShortTerm() {
        return ratingMoodysIssuerShortTerm;
    }

    public Entry.Action getRatingMoodysIssuerShortTermAction() {
        return ratingMoodysIssuerShortTermAction;
    }

    public LocalDate getRatingMoodysIssuerShortTermDate() {
        return ratingMoodysIssuerShortTermDate;
    }

    // Counterparty Rating (CTP) backed

    @Override
    public String getRatingMoodysIssuerLongTermBacked() {
        return ratingMoodysIssuerLongTermBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionBacked() {
        return ratingMoodysIssuerLongTermActionBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateBacked() {
        return ratingMoodysIssuerLongTermDateBacked;
    }

    @Override
    public String getRatingMoodysIssuerShortTermBacked() {
        return ratingMoodysIssuerShortTermBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionBacked() {
        return ratingMoodysIssuerShortTermActionBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateBacked() {
        return ratingMoodysIssuerShortTermDateBacked;
    }

    // Senior Unsecured Rating (SU)

    @Override
    public String getRatingMoodysIssuerLongTermSu() {
        return ratingMoodysIssuerLongTermSu;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionSu() {
        return ratingMoodysIssuerLongTermActionSu;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateSu() {
        return ratingMoodysIssuerLongTermDateSu;
    }

    @Override
    public String getRatingMoodysIssuerShortTermSu() {
        return ratingMoodysIssuerShortTermSu;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionSu() {
        return ratingMoodysIssuerShortTermActionSu;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateSu() {
        return ratingMoodysIssuerShortTermDateSu;
    }

    // Senior Unsecured Rating (SU) backed

    @Override
    public String getRatingMoodysIssuerLongTermSuBacked() {
        return ratingMoodysIssuerLongTermSuBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionSuBacked() {
        return ratingMoodysIssuerLongTermActionSuBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateSuBacked() {
        return ratingMoodysIssuerLongTermDateSuBacked;
    }

    @Override
    public String getRatingMoodysIssuerShortTermSuBacked() {
        return ratingMoodysIssuerShortTermSuBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionSuBacked() {
        return ratingMoodysIssuerShortTermActionSuBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateSuBacked() {
        return ratingMoodysIssuerShortTermDateSuBacked;
    }

    // Bank Deposit Rating (BDR)

    @Override
    public String getRatingMoodysIssuerLongTermBdr() {
        return ratingMoodysIssuerLongTermBdr;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionBdr() {
        return ratingMoodysIssuerLongTermActionBdr;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateBdr() {
        return ratingMoodysIssuerLongTermDateBdr;
    }

    @Override
    public String getRatingMoodysIssuerShortTermBdr() {
        return ratingMoodysIssuerShortTermBdr;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionBdr() {
        return ratingMoodysIssuerShortTermActionBdr;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateBdr() {
        return ratingMoodysIssuerShortTermDateBdr;
    }

    // Bank Deposit Rating (BDR) backed

    @Override
    public String getRatingMoodysIssuerLongTermBdrBacked() {
        return ratingMoodysIssuerLongTermBdrBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionBdrBacked() {
        return ratingMoodysIssuerLongTermActionBdrBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateBdrBacked() {
        return ratingMoodysIssuerLongTermDateBdrBacked;
    }

    @Override
    public String getRatingMoodysIssuerShortTermBdrBacked() {
        return ratingMoodysIssuerShortTermBdrBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionBdrBacked() {
        return ratingMoodysIssuerShortTermActionBdrBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateBdrBacked() {
        return ratingMoodysIssuerShortTermDateBdrBacked;
    }

    // Insurance Financial Strength Rating (IFS)

    @Override
    public String getRatingMoodysIssuerLongTermIfsr() {
        return ratingMoodysIssuerLongTermIfsr;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionIfsr() {
        return ratingMoodysIssuerLongTermActionIfsr;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateIfsr() {
        return ratingMoodysIssuerLongTermDateIfsr;
    }

    @Override
    public String getRatingMoodysIssuerShortTermIfsr() {
        return ratingMoodysIssuerShortTermIfsr;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionIfsr() {
        return ratingMoodysIssuerShortTermActionIfsr;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateIfsr() {
        return ratingMoodysIssuerShortTermDateIfsr;
    }

    // Insurance Financial Strength Rating (IFS) backed

    @Override
    public String getRatingMoodysIssuerLongTermIfsrBacked() {
        return ratingMoodysIssuerLongTermIfsrBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerLongTermActionIfsrBacked() {
        return ratingMoodysIssuerLongTermActionIfsrBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerLongTermDateIfsrBacked() {
        return ratingMoodysIssuerLongTermDateIfsrBacked;
    }

    @Override
    public String getRatingMoodysIssuerShortTermIfsrBacked() {
        return ratingMoodysIssuerShortTermIfsrBacked;
    }

    @Override
    public Entry.Action getRatingMoodysIssuerShortTermActionIfsrBacked() {
        return ratingMoodysIssuerShortTermActionIfsrBacked;
    }

    @Override
    public LocalDate getRatingMoodysIssuerShortTermDateIfsrBacked() {
        return ratingMoodysIssuerShortTermDateIfsrBacked;
    }

    // ---

    public String getRatingStandardAndPoorsIssuerLongTerm() {
        return ratingStandardAndPoorsIssuerLongTerm;
    }

    public void setRatingStandardAndPoorsIssuerLongTerm(
            String ratingStandardAndPoorsIssuerLongTerm) {
        this.ratingStandardAndPoorsIssuerLongTerm = ratingStandardAndPoorsIssuerLongTerm;
    }

    public Entry.Action getRatingStandardAndPoorsIssuerLongTermAction() {
        return ratingStandardAndPoorsIssuerLongTermAction;
    }

    public void setRatingStandardAndPoorsIssuerLongTermAction(
            Entry.Action ratingStandardAndPoorsIssuerLongTermAction) {
        this.ratingStandardAndPoorsIssuerLongTermAction = ratingStandardAndPoorsIssuerLongTermAction;
    }

    public LocalDate getRatingStandardAndPoorsIssuerLongTermDate() {
        return ratingStandardAndPoorsIssuerLongTermDate;
    }

    public void setRatingStandardAndPoorsIssuerLongTermDate(
            LocalDate ratingStandardAndPoorsIssuerLongTermDate) {
        this.ratingStandardAndPoorsIssuerLongTermDate = ratingStandardAndPoorsIssuerLongTermDate;
    }

    public Entry.RegulatoryId getRatingStandardAndPoorsIssuerLongTermRegulatoryId() {
        return ratingStandardAndPoorsIssuerLongTermRegulatoryId;
    }

    public void setRatingStandardAndPoorsIssuerLongTermRegulatoryId(
            Entry.RegulatoryId ratingStandardAndPoorsIssuerLongTermRegulatoryId) {
        this.ratingStandardAndPoorsIssuerLongTermRegulatoryId = ratingStandardAndPoorsIssuerLongTermRegulatoryId;
    }

    public String getRatingStandardAndPoorsIssuerShortTerm() {
        return ratingStandardAndPoorsIssuerShortTerm;
    }

    public void setRatingStandardAndPoorsIssuerShortTerm(
            String ratingStandardAndPoorsIssuerShortTerm) {
        this.ratingStandardAndPoorsIssuerShortTerm = ratingStandardAndPoorsIssuerShortTerm;
    }

    public Entry.Action getRatingStandardAndPoorsIssuerShortTermAction() {
        return ratingStandardAndPoorsIssuerShortTermAction;
    }

    public void setRatingStandardAndPoorsIssuerShortTermAction(
            Entry.Action ratingStandardAndPoorsIssuerShortTermAction) {
        this.ratingStandardAndPoorsIssuerShortTermAction = ratingStandardAndPoorsIssuerShortTermAction;
    }

    public LocalDate getRatingStandardAndPoorsIssuerShortTermDate() {
        return ratingStandardAndPoorsIssuerShortTermDate;
    }

    public void setRatingStandardAndPoorsIssuerShortTermDate(
            LocalDate ratingStandardAndPoorsIssuerShortTermDate) {
        this.ratingStandardAndPoorsIssuerShortTermDate = ratingStandardAndPoorsIssuerShortTermDate;
    }

    public String getRatingStandardAndPoorsIssuerLongTermFSR() {
        return ratingStandardAndPoorsIssuerLongTermFSR;
    }

    public void setRatingStandardAndPoorsIssuerLongTermFSR(
            String ratingStandardAndPoorsIssuerLongTermFSR) {
        this.ratingStandardAndPoorsIssuerLongTermFSR = ratingStandardAndPoorsIssuerLongTermFSR;
    }

    public Action getRatingStandardAndPoorsIssuerLongTermActionFSR() {
        return ratingStandardAndPoorsIssuerLongTermActionFSR;
    }

    public void setRatingStandardAndPoorsIssuerLongTermActionFSR(
            Action ratingStandardAndPoorsIssuerLongTermActionFSR) {
        this.ratingStandardAndPoorsIssuerLongTermActionFSR = ratingStandardAndPoorsIssuerLongTermActionFSR;
    }

    public LocalDate getRatingStandardAndPoorsIssuerLongTermDateFSR() {
        return ratingStandardAndPoorsIssuerLongTermDateFSR;
    }

    public void setRatingStandardAndPoorsIssuerLongTermDateFSR(
            LocalDate ratingStandardAndPoorsIssuerLongTermDateFSR) {
        this.ratingStandardAndPoorsIssuerLongTermDateFSR = ratingStandardAndPoorsIssuerLongTermDateFSR;
    }

    public String getRatingStandardAndPoorsIssuerShortTermFSR() {
        return ratingStandardAndPoorsIssuerShortTermFSR;
    }

    public void setRatingStandardAndPoorsIssuerShortTermFSR(
            String ratingStandardAndPoorsIssuerShortTermFSR) {
        this.ratingStandardAndPoorsIssuerShortTermFSR = ratingStandardAndPoorsIssuerShortTermFSR;
    }

    public Action getRatingStandardAndPoorsIssuerShortTermActionFSR() {
        return ratingStandardAndPoorsIssuerShortTermActionFSR;
    }

    public void setRatingStandardAndPoorsIssuerShortTermActionFSR(
            Action ratingStandardAndPoorsIssuerShortTermActionFSR) {
        this.ratingStandardAndPoorsIssuerShortTermActionFSR = ratingStandardAndPoorsIssuerShortTermActionFSR;
    }

    public LocalDate getRatingStandardAndPoorsIssuerShortTermDateFSR() {
        return ratingStandardAndPoorsIssuerShortTermDateFSR;
    }

    public void setRatingStandardAndPoorsIssuerShortTermDateFSR(
            LocalDate ratingStandardAndPoorsIssuerShortTermDateFSR) {
        this.ratingStandardAndPoorsIssuerShortTermDateFSR = ratingStandardAndPoorsIssuerShortTermDateFSR;
    }

    public String getRatingStandardAndPoorsIssuerLongTermFER() {
        return ratingStandardAndPoorsIssuerLongTermFER;
    }

    public void setRatingStandardAndPoorsIssuerLongTermFER(
            String ratingStandardAndPoorsIssuerLongTermFER) {
        this.ratingStandardAndPoorsIssuerLongTermFER = ratingStandardAndPoorsIssuerLongTermFER;
    }

    public Action getRatingStandardAndPoorsIssuerLongTermActionFER() {
        return ratingStandardAndPoorsIssuerLongTermActionFER;
    }

    public void setRatingStandardAndPoorsIssuerLongTermActionFER(
            Action ratingStandardAndPoorsIssuerLongTermActionFER) {
        this.ratingStandardAndPoorsIssuerLongTermActionFER = ratingStandardAndPoorsIssuerLongTermActionFER;
    }

    public LocalDate getRatingStandardAndPoorsIssuerLongTermDateFER() {
        return ratingStandardAndPoorsIssuerLongTermDateFER;
    }

    public void setRatingStandardAndPoorsIssuerLongTermDateFER(
            LocalDate ratingStandardAndPoorsIssuerLongTermDateFER) {
        this.ratingStandardAndPoorsIssuerLongTermDateFER = ratingStandardAndPoorsIssuerLongTermDateFER;
    }

    public String getRatingStandardAndPoorsIssuerShortTermFER() {
        return ratingStandardAndPoorsIssuerShortTermFER;
    }

    public void setRatingStandardAndPoorsIssuerShortTermFER(
            String ratingStandardAndPoorsIssuerShortTermFER) {
        this.ratingStandardAndPoorsIssuerShortTermFER = ratingStandardAndPoorsIssuerShortTermFER;
    }

    public Action getRatingStandardAndPoorsIssuerShortTermActionFER() {
        return ratingStandardAndPoorsIssuerShortTermActionFER;
    }

    public void setRatingStandardAndPoorsIssuerShortTermActionFER(
            Action ratingStandardAndPoorsIssuerShortTermActionFER) {
        this.ratingStandardAndPoorsIssuerShortTermActionFER = ratingStandardAndPoorsIssuerShortTermActionFER;
    }

    public LocalDate getRatingStandardAndPoorsIssuerShortTermDateFER() {
        return ratingStandardAndPoorsIssuerShortTermDateFER;
    }

    public void setRatingStandardAndPoorsIssuerShortTermDateFER(
            LocalDate ratingStandardAndPoorsIssuerShortTermDateFER) {
        this.ratingStandardAndPoorsIssuerShortTermDateFER = ratingStandardAndPoorsIssuerShortTermDateFER;
    }

    @Override
    public String toString() {
        return "IssuerRatingImpl{" +
                "source=" + source +
                ", issuerName='" + issuerName + '\'' +
                ", lei='" + lei + '\'' +
                ", vwdSymbol='" + vwdSymbol + '\'' +
                ", countryIso='" + countryIso + '\'' +
                ", currencyIso='" + currencyIso + '\'' +
                ", ratingFitchIssuerLongTerm=" + ratingFitchIssuerLongTerm +
                ", ratingFitchIssuerLongTermAction=" + ratingFitchIssuerLongTermAction +
                ", ratingFitchIssuerLongTermDate=" + ratingFitchIssuerLongTermDate +
                ", ratingFitchIssuerShortTerm=" + ratingFitchIssuerShortTerm +
                ", ratingFitchIssuerShortTermAction=" + ratingFitchIssuerShortTermAction +
                ", ratingFitchIssuerShortTermDate=" + ratingFitchIssuerShortTermDate +
                ", ratingFitchIssuerIFS=" + ratingFitchIssuerIFS +
                ", ratingFitchIssuerIFSAction=" + ratingFitchIssuerIFSAction +
                ", ratingFitchIssuerIFSDate=" + ratingFitchIssuerIFSDate +
                // Counterparty Rating (CTP)
                ", ratingMoodysIssuerLongTerm=" + ratingMoodysIssuerLongTerm +
                ", ratingMoodysIssuerLongTermAction=" + ratingMoodysIssuerLongTermAction +
                ", ratingMoodysIssuerLongTermDate=" + ratingMoodysIssuerLongTermDate +
                ", ratingMoodysIssuerShortTerm=" + ratingMoodysIssuerShortTerm +
                ", ratingMoodysIssuerShortTermAction=" + ratingMoodysIssuerShortTermAction +
                ", ratingMoodysIssuerShortTermDate=" + ratingMoodysIssuerShortTermDate +
                // Counterparty Rating (CTP) backed
                ", ratingMoodysIssuerLongTermBacked=" + ratingMoodysIssuerLongTermBacked +
                ", ratingMoodysIssuerLongTermActionBacked=" + ratingMoodysIssuerLongTermActionBacked +
                ", ratingMoodysIssuerLongTermDateBacked=" + ratingMoodysIssuerLongTermDateBacked +
                ", ratingMoodysIssuerShortTermBacked=" + ratingMoodysIssuerShortTermBacked +
                ", ratingMoodysIssuerShortTermActionBacked=" + ratingMoodysIssuerShortTermActionBacked +
                ", ratingMoodysIssuerShortTermDateBacked=" + ratingMoodysIssuerShortTermDateBacked +
                // Senior Unsecured Rating (SU)
                ", ratingMoodysIssuerLongTermSu=" + ratingMoodysIssuerLongTermSu +
                ", ratingMoodysIssuerLongTermActionSu=" + ratingMoodysIssuerLongTermActionSu +
                ", ratingMoodysIssuerLongTermDateSu=" + ratingMoodysIssuerLongTermDateSu +
                ", ratingMoodysIssuerShortTermSu=" + ratingMoodysIssuerShortTermSu +
                ", ratingMoodysIssuerShortTermActionSu=" + ratingMoodysIssuerShortTermActionSu +
                ", ratingMoodysIssuerShortTermDateSu=" + ratingMoodysIssuerShortTermDateSu +
                // Senior Unsecured Rating (SU) backed
                ", ratingMoodysIssuerLongTermSuBacked=" + ratingMoodysIssuerLongTermSuBacked +
                ", ratingMoodysIssuerLongTermActionSuBacked=" + ratingMoodysIssuerLongTermActionSuBacked +
                ", ratingMoodysIssuerLongTermDateSuBacked=" + ratingMoodysIssuerLongTermDateSuBacked +
                ", ratingMoodysIssuerShortTermSuBacked=" + ratingMoodysIssuerShortTermSuBacked +
                ", ratingMoodysIssuerShortTermActionSuBacked=" + ratingMoodysIssuerShortTermActionSuBacked +
                ", ratingMoodysIssuerShortTermDateSuBacked=" + ratingMoodysIssuerShortTermDateSuBacked +
                // Bank Deposit Rating (BDR)
                ", ratingMoodysIssuerLongTermBdr=" + ratingMoodysIssuerLongTermBdr +
                ", ratingMoodysIssuerLongTermActionBdr=" + ratingMoodysIssuerLongTermActionBdr +
                ", ratingMoodysIssuerLongTermDateBdr=" + ratingMoodysIssuerLongTermDateBdr +
                ", ratingMoodysIssuerShortTermBdr=" + ratingMoodysIssuerShortTermBdr +
                ", ratingMoodysIssuerShortTermActionBdr=" + ratingMoodysIssuerShortTermActionBdr +
                ", ratingMoodysIssuerShortTermDateBdr=" + ratingMoodysIssuerShortTermDateBdr +
                // Bank Deposit Rating (BDR) backed
                ", ratingMoodysIssuerLongTermBdrBacked=" + ratingMoodysIssuerLongTermBdrBacked +
                ", ratingMoodysIssuerLongTermActionBdrBacked=" + ratingMoodysIssuerLongTermActionBdrBacked +
                ", ratingMoodysIssuerLongTermDateBdrBacked=" + ratingMoodysIssuerLongTermDateBdrBacked +
                ", ratingMoodysIssuerShortTermBdrBacked=" + ratingMoodysIssuerShortTermBdrBacked +
                ", ratingMoodysIssuerShortTermActionBdrBacked=" + ratingMoodysIssuerShortTermActionBdrBacked +
                ", ratingMoodysIssuerShortTermDateBdrBacked=" + ratingMoodysIssuerShortTermDateBdrBacked +
                // Insurance Financial Strength Rating (IFS)
                ", ratingMoodysIssuerLongTermIfsr=" + ratingMoodysIssuerLongTermIfsr +
                ", ratingMoodysIssuerLongTermActionIfsr=" + ratingMoodysIssuerLongTermActionIfsr +
                ", ratingMoodysIssuerLongTermDateIfsr=" + ratingMoodysIssuerLongTermDateIfsr +
                ", ratingMoodysIssuerShortTermIfsr=" + ratingMoodysIssuerShortTermIfsr +
                ", ratingMoodysIssuerShortTermActionIfsr=" + ratingMoodysIssuerShortTermActionIfsr +
                ", ratingMoodysIssuerShortTermDateIfsr=" + ratingMoodysIssuerShortTermDateIfsr +
                // Insurance Financial Strength Rating (IFS) backed
                ", ratingMoodysIssuerLongTermIfsrBacked=" + ratingMoodysIssuerLongTermIfsrBacked +
                ", ratingMoodysIssuerLongTermActionIfsrBacked=" + ratingMoodysIssuerLongTermActionIfsrBacked +
                ", ratingMoodysIssuerLongTermDateIfsrBacked=" + ratingMoodysIssuerLongTermDateIfsrBacked +
                ", ratingMoodysIssuerShortTermIfsrBacked=" + ratingMoodysIssuerShortTermIfsrBacked +
                ", ratingMoodysIssuerShortTermActionIfsrBacked=" + ratingMoodysIssuerShortTermActionIfsrBacked +
                ", ratingMoodysIssuerShortTermDateIfsrBacked=" + ratingMoodysIssuerShortTermDateIfsrBacked +
                // ---
                ", ratingSnPIssuerLongTerm=" + ratingStandardAndPoorsIssuerLongTerm +
                ", ratingSnPIssuerLongTermAction=" + ratingStandardAndPoorsIssuerLongTermAction +
                ", ratingSnPIssuerLongTermDate=" + ratingStandardAndPoorsIssuerLongTermDate +
                ", ratingSnPIssuerLongTermRID=" + ratingStandardAndPoorsIssuerLongTermRegulatoryId +
                ", ratingSnPIssuerShortTerm=" + ratingStandardAndPoorsIssuerShortTerm +
                ", ratingSnPIssuerShortTermAction=" + ratingStandardAndPoorsIssuerShortTermAction +
                ", ratingSnPIssuerShortTermDate=" + ratingStandardAndPoorsIssuerShortTermDate +
                ", ratingSnPIssuerLongTermFSR=" + ratingStandardAndPoorsIssuerLongTermFSR +
                ", ratingSnPIssuerLongTermActionFSR=" + ratingStandardAndPoorsIssuerLongTermActionFSR +
                ", ratingSnPIssuerLongTermDateFSR=" + ratingStandardAndPoorsIssuerLongTermDateFSR +
                ", ratingSnPIssuerShortTermFSR=" + ratingStandardAndPoorsIssuerShortTermFSR +
                ", ratingSnPIssuerShortTermActionFSR=" + ratingStandardAndPoorsIssuerShortTermActionFSR +
                ", ratingSnPIssuerShortTermDateFSR=" + ratingStandardAndPoorsIssuerShortTermDateFSR +
                ", ratingSnPIssuerLongTermFER=" + ratingStandardAndPoorsIssuerLongTermFER +
                ", ratingSnPIssuerLongTermActionFER=" + ratingStandardAndPoorsIssuerLongTermActionFER +
                ", ratingSnPIssuerLongTermDateFER=" + ratingStandardAndPoorsIssuerLongTermDateFER +
                ", ratingSnPIssuerShortTermFER=" + ratingStandardAndPoorsIssuerShortTermFER +
                ", ratingSnPIssuerShortTermActionFER=" + ratingStandardAndPoorsIssuerShortTermActionFER +
                ", ratingSnPIssuerShortTermDateFER=" + ratingStandardAndPoorsIssuerShortTermDateFER +
                '}';
    }

    public Comparable getValue(IssuerRatingDescriptor desc) {
        switch (desc) {
            case SOURCE:
                return getSource();
            case ISSUERNAME:
                return getIssuerName();
            case LEI:
                return getLei();
            case RATING_FITCH_ISSUER_LT:
                return getRatingFitchIssuerLongTerm();
            case RATING_FITCH_ISSUER_LT_DATE:
                return getRatingFitchIssuerLongTermDate();
            case RATING_FITCH_ISSUER_ST:
                return getRatingFitchIssuerShortTerm();
            case RATING_FITCH_ISSUER_ST_DATE:
                return getRatingFitchIssuerShortTermDate();
            case RATING_FITCH_ISSUER_IFS:
                return getRatingFitchIssuerIFS();
            case RATING_FITCH_ISSUER_IFS_DATE:
                return getRatingFitchIssuerIFSDate();
            // Counterparty Rating (CTP)
            case RATING_MDYS_ISSR_LT:
                return getRatingMoodysIssuerLongTerm();
            case RATING_MDYS_ISSR_LT_D:
                return getRatingMoodysIssuerLongTermDate();
            case RATING_MDYS_ISSR_ST:
                return getRatingMoodysIssuerShortTerm();
            case RATING_MDYS_ISSR_ST_D:
                return getRatingMoodysIssuerShortTermDate();
            // Counterparty Rating (CTP) backed
            case RATING_MDYS_ISSR_LT_B:
                return getRatingMoodysIssuerLongTermBacked();
            case RATING_MDYS_ISSR_LT_D_B:
                return getRatingMoodysIssuerLongTermDateBacked();
            case RATING_MDYS_ISSR_ST_B:
                return getRatingMoodysIssuerShortTermBacked();
            case RATING_MDYS_ISSR_ST_D_B:
                return getRatingMoodysIssuerShortTermDateBacked();
            // Senior Unsecured Rating (SU)
            case RATING_MDYS_ISSR_LT_SU:
                return getRatingMoodysIssuerLongTermSu();
            case RATING_MDYS_ISSR_LT_D_SU:
                return getRatingMoodysIssuerLongTermDateSu();
            case RATING_MDYS_ISSR_ST_SU:
                return getRatingMoodysIssuerShortTermSu();
            case RATING_MDYS_ISSR_ST_D_SU:
                return getRatingMoodysIssuerShortTermDateSu();
            // Senior Unsecured Rating (SU) backed
            case RATING_MDYS_ISSR_LT_SU_B:
                return getRatingMoodysIssuerLongTermSuBacked();
            case RATING_MDYS_ISSR_LT_D_SU_B:
                return getRatingMoodysIssuerLongTermDateSuBacked();
            case RATING_MDYS_ISSR_ST_SU_B:
                return getRatingMoodysIssuerShortTermSuBacked();
            case RATING_MDYS_ISSR_ST_D_SU_B:
                return getRatingMoodysIssuerShortTermDateSuBacked();
            // Bank Deposit Rating (BDR)
            case RATING_MDYS_ISSR_LT_BDR:
                return getRatingMoodysIssuerLongTermBdr();
            case RATING_MDYS_ISSR_LT_D_BDR:
                return getRatingMoodysIssuerLongTermDateBdr();
            case RATING_MDYS_ISSR_ST_BDR:
                return getRatingMoodysIssuerShortTermBdr();
            case RATING_MDYS_ISSR_ST_D_BDR:
                return getRatingMoodysIssuerShortTermDateBdr();
            // Bank Deposit Rating (BDR) backed
            case RATING_MDYS_ISSR_LT_BDR_B:
                return getRatingMoodysIssuerLongTermBdrBacked();
            case RATING_MDYS_ISSR_LT_D_BDR_B:
                return getRatingMoodysIssuerLongTermDateBdrBacked();
            case RATING_MDYS_ISSR_ST_BDR_B:
                return getRatingMoodysIssuerShortTermBdrBacked();
            case RATING_MDYS_ISSR_ST_D_BDR_B:
                return getRatingMoodysIssuerShortTermDateBdrBacked();
            // Insurance Financial Strength Rating (IFS)
            case RATING_MDYS_ISSR_LT_IFSR:
                return getRatingMoodysIssuerLongTermIfsr();
            case RATING_MDYS_ISSR_LT_D_IFSR:
                return getRatingMoodysIssuerLongTermDateIfsr();
            case RATING_MDYS_ISSR_ST_IFSR:
                return getRatingMoodysIssuerShortTermIfsr();
            case RATING_MDYS_ISSR_ST_D_IFSR:
                return getRatingMoodysIssuerShortTermDateIfsr();
            // Insurance Financial Strength Rating (IFS) backed
            case RATING_MDYS_ISSR_LT_IFSR_B:
                return getRatingMoodysIssuerLongTermIfsrBacked();
            case RATING_MDYS_ISSR_LT_D_IFSR_B:
                return getRatingMoodysIssuerLongTermDateIfsrBacked();
            case RATING_MDYS_ISSR_ST_IFSR_B:
                return getRatingMoodysIssuerShortTermIfsrBacked();
            case RATING_MDYS_ISSR_ST_D_IFSR_B:
                return getRatingMoodysIssuerShortTermDateIfsrBacked();
            // ---
            case RATING_SNP_ISSUER_LT:
                return getRatingStandardAndPoorsIssuerLongTerm();
            case RATING_SNP_ISSUER_LT_DATE:
                return getRatingStandardAndPoorsIssuerLongTermDate();
            case RATING_SNP_ISSUER_LT_RID:
                return getRatingStandardAndPoorsIssuerLongTermRegulatoryId();
            case RATING_SNP_ISSUER_ST:
                return getRatingStandardAndPoorsIssuerShortTerm();
            case RATING_SNP_ISSUER_ST_DATE:
                return getRatingStandardAndPoorsIssuerShortTermDate();
            case RATING_SNP_ISSUER_LT_FSR:
                return getRatingStandardAndPoorsIssuerLongTermFSR();
            case RATING_SNP_ISSUER_LT_FSR_DATE:
                return getRatingStandardAndPoorsIssuerLongTermDateFSR();
            case RATING_SNP_ISSUER_ST_FSR:
                return getRatingStandardAndPoorsIssuerShortTermFSR();
            case RATING_SNP_ISSUER_ST_FSR_DATE:
                return getRatingStandardAndPoorsIssuerShortTermDateFSR();
            case RATING_SNP_ISSUER_LT_FER:
                return getRatingStandardAndPoorsIssuerLongTermFER();
            case RATING_SNP_ISSUER_LT_FER_DATE:
                return getRatingStandardAndPoorsIssuerLongTermDateFER();
            case RATING_SNP_ISSUER_ST_FER:
                return getRatingStandardAndPoorsIssuerShortTermFER();
            case RATING_SNP_ISSUER_ST_FER_DATE:
                return getRatingStandardAndPoorsIssuerShortTermDateFER();
                
            default:
                throw new UnsupportedOperationException("no support for sort field: " + desc);
        }
    }

    public Object getProperty(IssuerRatingDescriptor desc) {
        switch (desc) {
            case SOURCE:
                return getSource();
            case ISSUERNAME:
                return getIssuerName();
            case VWDSYMBOL:
                return getVwdSymbol();
            case COUNTRYISO:
                return getCountryIso();
            case CURRENCYISO:
                return getCurrencyIso();
            case RATING_FITCH_ISSUER_LT:
                return getRatingFitchIssuerLongTerm();
            case RATING_FITCH_ISSUER_LT_DATE:
                return getRatingFitchIssuerLongTermDate();
            case RATING_FITCH_ISSUER_LT_ACTION:
                return getRatingFitchIssuerLongTermAction();
            case RATING_FITCH_ISSUER_ST:
                return getRatingFitchIssuerShortTerm();
            case RATING_FITCH_ISSUER_ST_DATE:
                return getRatingFitchIssuerShortTermDate();
            case RATING_FITCH_ISSUER_ST_ACTION:
                return getRatingFitchIssuerShortTermAction();
            case RATING_FITCH_ISSUER_IFS:
                return getRatingFitchIssuerIFS();
            case RATING_FITCH_ISSUER_IFS_DATE:
                return getRatingFitchIssuerIFSDate();
            case RATING_FITCH_ISSUER_IFS_ACTION:
                return getRatingFitchIssuerIFSAction();
            // Counterparty Rating (CTP)
            case RATING_MDYS_ISSR_LT:
                return getRatingMoodysIssuerLongTerm();
            case RATING_MDYS_ISSR_LT_D:
                return getRatingMoodysIssuerLongTermDate();
            case RATING_MDYS_ISSR_LT_A:
                return getRatingMoodysIssuerLongTermAction();
            case RATING_MDYS_ISSR_ST:
                return getRatingMoodysIssuerShortTerm();
            case RATING_MDYS_ISSR_ST_D:
                return getRatingMoodysIssuerShortTermDate();
            case RATING_MDYS_ISSR_ST_A:
                return getRatingMoodysIssuerShortTermAction();
            // Counterparty Rating (CTP) backed
            case RATING_MDYS_ISSR_LT_B:
                return getRatingMoodysIssuerLongTermBacked();
            case RATING_MDYS_ISSR_LT_D_B:
                return getRatingMoodysIssuerLongTermDateBacked();
            case RATING_MDYS_ISSR_LT_A_B:
                return getRatingMoodysIssuerLongTermActionBacked();
            case RATING_MDYS_ISSR_ST_B:
                return getRatingMoodysIssuerShortTermBacked();
            case RATING_MDYS_ISSR_ST_D_B:
                return getRatingMoodysIssuerShortTermDateBacked();
            case RATING_MDYS_ISSR_ST_A_B:
                return getRatingMoodysIssuerShortTermActionBacked();
            // Senior Unsecured Rating (SU)
            case RATING_MDYS_ISSR_LT_SU:
                return getRatingMoodysIssuerLongTermSu();
            case RATING_MDYS_ISSR_LT_D_SU:
                return getRatingMoodysIssuerLongTermDateSu();
            case RATING_MDYS_ISSR_LT_A_SU:
                return getRatingMoodysIssuerLongTermActionSu();
            case RATING_MDYS_ISSR_ST_SU:
                return getRatingMoodysIssuerShortTermSu();
            case RATING_MDYS_ISSR_ST_D_SU:
                return getRatingMoodysIssuerShortTermDateSu();
            case RATING_MDYS_ISSR_ST_A_SU:
                return getRatingMoodysIssuerShortTermActionSu();
            // Senior Unsecured Rating (SU) backed
            case RATING_MDYS_ISSR_LT_SU_B:
                return getRatingMoodysIssuerLongTermSuBacked();
            case RATING_MDYS_ISSR_LT_D_SU_B:
                return getRatingMoodysIssuerLongTermDateSuBacked();
            case RATING_MDYS_ISSR_LT_A_SU_B:
                return getRatingMoodysIssuerLongTermActionSuBacked();
            case RATING_MDYS_ISSR_ST_SU_B:
                return getRatingMoodysIssuerShortTermSuBacked();
            case RATING_MDYS_ISSR_ST_D_SU_B:
                return getRatingMoodysIssuerShortTermDateSuBacked();
            case RATING_MDYS_ISSR_ST_A_SU_B:
                return getRatingMoodysIssuerShortTermActionSuBacked();
            // Bank Deposit Rating (BDR)
            case RATING_MDYS_ISSR_LT_BDR:
                return getRatingMoodysIssuerLongTermBdr();
            case RATING_MDYS_ISSR_LT_D_BDR:
                return getRatingMoodysIssuerLongTermDateBdr();
            case RATING_MDYS_ISSR_LT_A_BDR:
                return getRatingMoodysIssuerLongTermActionBdr();
            case RATING_MDYS_ISSR_ST_BDR:
                return getRatingMoodysIssuerShortTermBdr();
            case RATING_MDYS_ISSR_ST_D_BDR:
                return getRatingMoodysIssuerShortTermDateBdr();
            case RATING_MDYS_ISSR_ST_A_BDR:
                return getRatingMoodysIssuerShortTermActionBdr();
            // Bank Deposit Rating (BDR) backed
            case RATING_MDYS_ISSR_LT_BDR_B:
                return getRatingMoodysIssuerLongTermBdrBacked();
            case RATING_MDYS_ISSR_LT_D_BDR_B:
                return getRatingMoodysIssuerLongTermDateBdrBacked();
            case RATING_MDYS_ISSR_LT_A_BDR_B:
                return getRatingMoodysIssuerLongTermActionBdrBacked();
            case RATING_MDYS_ISSR_ST_BDR_B:
                return getRatingMoodysIssuerShortTermBdrBacked();
            case RATING_MDYS_ISSR_ST_D_BDR_B:
                return getRatingMoodysIssuerShortTermDateBdrBacked();
            case RATING_MDYS_ISSR_ST_A_BDR_B:
                return getRatingMoodysIssuerShortTermActionBdrBacked();
            // Insurance Financial Strength Rating (IFS)
            case RATING_MDYS_ISSR_LT_IFSR:
                return getRatingMoodysIssuerLongTermIfsr();
            case RATING_MDYS_ISSR_LT_D_IFSR:
                return getRatingMoodysIssuerLongTermDateIfsr();
            case RATING_MDYS_ISSR_LT_A_IFSR:
                return getRatingMoodysIssuerLongTermActionIfsr();
            case RATING_MDYS_ISSR_ST_IFSR:
                return getRatingMoodysIssuerShortTermIfsr();
            case RATING_MDYS_ISSR_ST_D_IFSR:
                return getRatingMoodysIssuerShortTermDateIfsr();
            case RATING_MDYS_ISSR_ST_A_IFSR:
                return getRatingMoodysIssuerShortTermActionIfsr();
            // Insurance Financial Strength Rating (IFS) backed
            case RATING_MDYS_ISSR_LT_IFSR_B:
                return getRatingMoodysIssuerLongTermIfsrBacked();
            case RATING_MDYS_ISSR_LT_D_IFSR_B:
                return getRatingMoodysIssuerLongTermDateIfsrBacked();
            case RATING_MDYS_ISSR_LT_A_IFSR_B:
                return getRatingMoodysIssuerLongTermActionIfsrBacked();
            case RATING_MDYS_ISSR_ST_IFSR_B:
                return getRatingMoodysIssuerShortTermIfsrBacked();
            case RATING_MDYS_ISSR_ST_D_IFSR_B:
                return getRatingMoodysIssuerShortTermDateIfsrBacked();
            case RATING_MDYS_ISSR_ST_A_IFSR_B:
                return getRatingMoodysIssuerShortTermActionIfsrBacked();
            // ---
            case RATING_SNP_ISSUER_LT:
                return getRatingStandardAndPoorsIssuerLongTerm();
            case RATING_SNP_ISSUER_LT_DATE:
                return getRatingStandardAndPoorsIssuerLongTermDate();
            case RATING_SNP_ISSUER_LT_ACTION:
                return getRatingStandardAndPoorsIssuerLongTermAction();
            case RATING_SNP_ISSUER_LT_RID:
                return getRatingStandardAndPoorsIssuerLongTermRegulatoryId();
            case RATING_SNP_ISSUER_ST:
                return getRatingStandardAndPoorsIssuerShortTerm();
            case RATING_SNP_ISSUER_ST_DATE:
                return getRatingStandardAndPoorsIssuerShortTermDate();
            case RATING_SNP_ISSUER_ST_ACTION:
                return getRatingStandardAndPoorsIssuerShortTermAction();
            case RATING_SNP_ISSUER_LT_FSR:
                return getRatingStandardAndPoorsIssuerLongTermFSR();
            case RATING_SNP_ISSUER_LT_FSR_DATE:
                return getRatingStandardAndPoorsIssuerLongTermDateFSR();
            case RATING_SNP_ISSUER_LT_FSR_ACTN:
                return getRatingStandardAndPoorsIssuerLongTermActionFSR();
            case RATING_SNP_ISSUER_ST_FSR:
                return getRatingStandardAndPoorsIssuerShortTermFSR();
            case RATING_SNP_ISSUER_ST_FSR_DATE:
                return getRatingStandardAndPoorsIssuerShortTermDateFSR();
            case RATING_SNP_ISSUER_ST_FSR_ACTN:
                return getRatingStandardAndPoorsIssuerShortTermActionFSR();
            case RATING_SNP_ISSUER_LT_FER:
                return getRatingStandardAndPoorsIssuerLongTermFER();
            case RATING_SNP_ISSUER_LT_FER_DATE:
                return getRatingStandardAndPoorsIssuerLongTermDateFER();
            case RATING_SNP_ISSUER_LT_FER_ACTN:
                return getRatingStandardAndPoorsIssuerLongTermActionFER();
            case RATING_SNP_ISSUER_ST_FER:
                return getRatingStandardAndPoorsIssuerShortTermFER();
            case RATING_SNP_ISSUER_ST_FER_DATE:
                return getRatingStandardAndPoorsIssuerShortTermDateFER();
            case RATING_SNP_ISSUER_ST_FER_ACTN:
                return getRatingStandardAndPoorsIssuerShortTermActionFER();
            default:
                throw new UnsupportedOperationException("no support for sort field: " + desc);
        }
    }
}
