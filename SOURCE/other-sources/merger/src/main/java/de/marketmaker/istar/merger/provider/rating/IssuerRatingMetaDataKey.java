/*
 * EconodayMetaDataKey.java
 *
 * Created on 30.03.12 13:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author zzhao
 */
public class IssuerRatingMetaDataKey implements Comparable<IssuerRatingMetaDataKey>, Serializable {

    public static final IssuerRatingMetaDataKey SOURCE =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.SOURCE);

    public static final IssuerRatingMetaDataKey COUNTRY_ISO =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.COUNTRYISO);

    public static final IssuerRatingMetaDataKey CURRENCY_ISO =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.CURRENCYISO);

    public static final IssuerRatingMetaDataKey RATING_FITCH_ISSUER_LT =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_FITCH_ISSUER_LT);

    public static final IssuerRatingMetaDataKey RATING_FITCH_ISSUER_ST =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_FITCH_ISSUER_ST);

    public static final IssuerRatingMetaDataKey RATING_FITCH_ISSUER_IFS =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_FITCH_ISSUER_IFS);

    // Counterparty Rating (CTP)
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST);

    // Counterparty Rating (CTP) backed
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_B);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_B);

    // Senior Unsecured Rating (SU)
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_SU =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_SU);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_SU =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_SU);

    // Senior Unsecured Rating (SU) backed
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_SU_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_SU_B);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_SU_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_SU_B);

    // Bank Deposit Rating (BDR)
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_BDR =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_BDR);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_BDR =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_BDR);

    // Bank Deposit Rating (BDR) backed
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_BDR_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_BDR_B);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_BDR_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_BDR_B);

    // Insurance Financial Strength Rating (IFS)
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_IFSR =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_IFSR);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_IFSR =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_IFSR);

    // Insurance Financial Strength Rating (IFS) backed
    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_LT_IFSR_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_IFSR_B);

    public static final IssuerRatingMetaDataKey RATING_MDYS_ISSR_ST_IFSR_B =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_IFSR_B);

    // ---
    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_LT =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_LT);

    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_LT_RID =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_RID);

    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_ST =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_ST);

    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_LT_FSR =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FSR);

    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_ST_FSR =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FSR);

    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_LT_FER =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FER);

    public static final IssuerRatingMetaDataKey RATING_SNP_ISSUER_ST_FER =
            new IssuerRatingMetaDataKey(IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FER);

    private static final Set<IssuerRatingDescriptor> scala = EnumSet.of(
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_LT,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_ST,
            IssuerRatingDescriptor.RATING_FITCH_ISSUER_IFS,
            // Counterparty Rating (CTP)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST,
            // Counterparty Rating (CTP) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_B,
            // Senior Unsecured Rating (SU)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_SU,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_SU,
            // Senior Unsecured Rating (SU) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_SU_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_SU_B,
            // Bank Deposit Rating (BDR)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_BDR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_BDR,
            // Bank Deposit Rating (BDR) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_BDR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_BDR_B,
            // Insurance Financial Strength Rating (IFS)
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_IFSR,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_IFSR,
            // Insurance Financial Strength Rating (IFS) backed
            IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_IFSR_B,
            IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_IFSR_B,
            // ---
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FSR,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FSR,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FER,
            IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_FER
    );

    public static final List<IssuerRatingMetaDataKey> ALL_KEYS = Arrays.asList(
            IssuerRatingMetaDataKey.SOURCE,
            IssuerRatingMetaDataKey.COUNTRY_ISO,
            IssuerRatingMetaDataKey.CURRENCY_ISO,
            IssuerRatingMetaDataKey.RATING_FITCH_ISSUER_LT,
            IssuerRatingMetaDataKey.RATING_FITCH_ISSUER_ST,
            IssuerRatingMetaDataKey.RATING_FITCH_ISSUER_IFS,
            // Counterparty Rating (CTP)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST,
            // Counterparty Rating (CTP) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_B,
            // Senior Unsecured Rating (SU)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_SU,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_SU,
            // Senior Unsecured Rating (SU) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_SU_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_SU_B,
            // Bank Deposit Rating (BDR)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_BDR,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_BDR,
            // Bank Deposit Rating (BDR) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_BDR_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_BDR_B,
            // Insurance Financial Strength Rating (IFS)
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_IFSR,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_IFSR,
            // Insurance Financial Strength Rating (IFS) backed
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_LT_IFSR_B,
            IssuerRatingMetaDataKey.RATING_MDYS_ISSR_ST_IFSR_B,
            // ---
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT_RID,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_ST,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT_FSR,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_ST_FSR,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_LT_FER,
            IssuerRatingMetaDataKey.RATING_SNP_ISSUER_ST_FER
    );

    private static final long serialVersionUID = 4705688605579301418L;

    private final IssuerRatingDescriptor type;

    public IssuerRatingMetaDataKey(IssuerRatingDescriptor type) {
        this.type = type;
    }

    public IssuerRatingDescriptor getDesc() {
        return this.type;
    }

    public static IssuerRatingMetaDataKey fromEnum(IssuerRatingDescriptor type) {
        switch (type) {
            case SOURCE:
                return SOURCE;
            case COUNTRYISO:
                return COUNTRY_ISO;
            case CURRENCYISO:
                return CURRENCY_ISO;
            case RATING_FITCH_ISSUER_LT:
                return RATING_FITCH_ISSUER_LT;
            case RATING_FITCH_ISSUER_ST:
                return RATING_FITCH_ISSUER_ST;
            case RATING_FITCH_ISSUER_IFS:
                return RATING_FITCH_ISSUER_IFS;
            // Counterparty Rating (CTP)
            case RATING_MDYS_ISSR_LT:
                return RATING_MDYS_ISSR_LT;
            case RATING_MDYS_ISSR_ST:
                return RATING_MDYS_ISSR_ST;
            // Counterparty Rating (CTP) backed
            case RATING_MDYS_ISSR_LT_B:
                return RATING_MDYS_ISSR_LT_B;
            case RATING_MDYS_ISSR_ST_B:
                return RATING_MDYS_ISSR_ST_B;
            // Senior Unsecured Rating (SU)
            case RATING_MDYS_ISSR_LT_SU:
                return RATING_MDYS_ISSR_LT_SU;
            case RATING_MDYS_ISSR_ST_SU:
                return RATING_MDYS_ISSR_ST_SU;
            // Senior Unsecured Rating (SU) backed
            case RATING_MDYS_ISSR_LT_SU_B:
                return RATING_MDYS_ISSR_LT_SU_B;
            case RATING_MDYS_ISSR_ST_SU_B:
                return RATING_MDYS_ISSR_ST_SU_B;
            // Bank Deposit Rating (BDR)
            case RATING_MDYS_ISSR_LT_BDR:
                return RATING_MDYS_ISSR_LT_BDR;
            case RATING_MDYS_ISSR_ST_BDR:
                return RATING_MDYS_ISSR_ST_BDR;
            // Bank Deposit Rating (BDR) backed
            case RATING_MDYS_ISSR_LT_BDR_B:
                return RATING_MDYS_ISSR_LT_BDR_B;
            case RATING_MDYS_ISSR_ST_BDR_B:
                return RATING_MDYS_ISSR_ST_BDR_B;
            // Insurance Financial Strength Rating (IFS)
            case RATING_MDYS_ISSR_LT_IFSR:
                return RATING_MDYS_ISSR_LT_IFSR;
            case RATING_MDYS_ISSR_ST_IFSR:
                return RATING_MDYS_ISSR_ST_IFSR;
            // Insurance Financial Strength Rating (IFS) backed
            case RATING_MDYS_ISSR_LT_IFSR_B:
                return RATING_MDYS_ISSR_LT_IFSR_B;
            case RATING_MDYS_ISSR_ST_IFSR_B:
                return RATING_MDYS_ISSR_ST_IFSR_B;
            // ---
            case RATING_SNP_ISSUER_LT:
                return RATING_SNP_ISSUER_LT;
            case RATING_SNP_ISSUER_LT_RID:
                return RATING_SNP_ISSUER_LT_RID;
            case RATING_SNP_ISSUER_ST:
                return RATING_SNP_ISSUER_ST;
            case RATING_SNP_ISSUER_LT_FSR:
                return RATING_SNP_ISSUER_LT_FSR;
            case RATING_SNP_ISSUER_ST_FSR:
                return RATING_SNP_ISSUER_ST_FSR;
            case RATING_SNP_ISSUER_LT_FER:
                return RATING_SNP_ISSUER_LT_FER;
            case RATING_SNP_ISSUER_ST_FER:
                return RATING_SNP_ISSUER_ST_FER;
            default:
                throw new UnsupportedOperationException("no support for: " + type);
        }
    }

    public String getType() {
        return this.type.getValue();
    }

    public String getName() {
        return this.type.getValue();
    }

    public boolean isEnum() {
        return !scala.contains(this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssuerRatingMetaDataKey that = (IssuerRatingMetaDataKey) o;

        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public int compareTo(IssuerRatingMetaDataKey o) {
        return this.type.compareTo(o.type);
    }

    protected Object readResolve() throws ObjectStreamException {
        return fromEnum(this.type);
    }

    @Override
    public String toString() {
        return "IssuerRatingMetaDataKey{" +
                "type=" + type +
                '}';
    }
}
