/*
 * IssuerRatingFilter.java
 *
 * Created on 07.05.12 16:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.merger.web.finder.Terms;

/**
 * @author zzhao
 */
public abstract class IssuerRatingFilter {

    private static final Logger log = LoggerFactory.getLogger(IssuerRatingFilter.class);

    public abstract boolean evaluate(IssuerRatingImpl imp, boolean withDetailedSymbol);

    public static IssuerRatingFilter TRUE = new IssuerRatingFilter() {
        @Override
        public boolean evaluate(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            return true;
        }
    };

    public static class Or extends IssuerRatingFilter {

        private final List<IssuerRatingFilter> filters;

        public Or() {
            this.filters = new ArrayList<>(3);
        }

        public void add(IssuerRatingFilter filter) {
            this.filters.add(filter);
        }

        @Override
        public boolean evaluate(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            for (IssuerRatingFilter filter : filters) {
                if (filter.evaluate(issuerRating, withDetailedSymbol)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class And extends IssuerRatingFilter {

        private final List<IssuerRatingFilter> filters;

        public And() {
            this.filters = new ArrayList<>(3);
        }

        public void add(IssuerRatingFilter filter) {
            this.filters.add(filter);
        }

        @Override
        public boolean evaluate(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            for (IssuerRatingFilter filter : filters) {
                if (!filter.evaluate(issuerRating, withDetailedSymbol)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class Not extends IssuerRatingFilter {

        private final IssuerRatingFilter filter;

        public Not(IssuerRatingFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean evaluate(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            return !this.filter.evaluate(issuerRating, withDetailedSymbol);
        }
    }

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    public static class In extends IssuerRatingFilter {

        private final IssuerRatingDescriptor desc;

        private final List<String> values;

        private final RatingSystem ratingSystem;

        public In(IssuerRatingDescriptor desc, RatingSystemProvider provider) {
            this.desc = desc;
            this.ratingSystem = desc.isRating() ? provider.getRatingSystem(desc.name()) : null;
            this.values = new ArrayList<>(5);
        }

        public void add(String value) {
            switch (this.desc) {
                case SOURCE:
                case COUNTRYISO:
                case CURRENCYISO:
                    this.values.add(value.startsWith("+") ? value.substring(1) : value);
                    break;
                case ISSUERNAME:
                    this.values.add(value.toLowerCase());
                    break;
                default:
                    this.values.add(value);
                    break;
            }
        }

        @Override
        public boolean evaluate(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            switch (this.desc) {
                case ISSUERNAME:
                    for (String value : this.values) {
                        if (issuerNameEqualFuzzy(issuerRating, value)) {
                            return true;
                        }
                    }
                    return false;
                case COUNTRYISO:
                    return this.values.contains(issuerRating.getCountryIso());
                case CURRENCYISO:
                    return this.values.contains(issuerRating.getCurrencyIso());
                case SOURCE:
                    final RatingSource source = issuerRating.getSource();
                    return this.values.contains(source.name());
                case RATING_FITCH_ISSUER_ST_ACTION:
                case RATING_FITCH_ISSUER_LT_ACTION:
                case RATING_FITCH_ISSUER_IFS_ACTION:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT_A:
                case RATING_MDYS_ISSR_ST_A:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_A_B:
                case RATING_MDYS_ISSR_ST_A_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_A_SU:
                case RATING_MDYS_ISSR_ST_A_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_A_SU_B:
                case RATING_MDYS_ISSR_ST_A_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_A_BDR:
                case RATING_MDYS_ISSR_ST_A_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_A_BDR_B:
                case RATING_MDYS_ISSR_ST_A_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_A_IFSR:
                case RATING_MDYS_ISSR_ST_A_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_A_IFSR_B:
                case RATING_MDYS_ISSR_ST_A_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_ST_ACTION:
                case RATING_SNP_ISSUER_LT_ACTION:
                case RATING_SNP_ISSUER_ST_FSR_ACTN:
                case RATING_SNP_ISSUER_LT_FSR_ACTN:
                case RATING_SNP_ISSUER_ST_FER_ACTN:
                case RATING_SNP_ISSUER_LT_FER_ACTN:
                case RATING_SNP_ISSUER_LT_RID:
                    final Entry.HasValue prop = (Entry.HasValue) issuerRating.getProperty(this.desc);
                    return null != prop && this.values.contains(prop.getValue());
                case RATING_FITCH_ISSUER_ST:
                case RATING_FITCH_ISSUER_LT:
                case RATING_FITCH_ISSUER_IFS:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT:
                case RATING_MDYS_ISSR_ST:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_B:
                case RATING_MDYS_ISSR_ST_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_SU:
                case RATING_MDYS_ISSR_ST_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_SU_B:
                case RATING_MDYS_ISSR_ST_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_BDR:
                case RATING_MDYS_ISSR_ST_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_BDR_B:
                case RATING_MDYS_ISSR_ST_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_IFSR:
                case RATING_MDYS_ISSR_ST_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_IFSR_B:
                case RATING_MDYS_ISSR_ST_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_ST:
                case RATING_SNP_ISSUER_LT:
                case RATING_SNP_ISSUER_ST_FSR:
                case RATING_SNP_ISSUER_LT_FSR:
                case RATING_SNP_ISSUER_ST_FER:
                case RATING_SNP_ISSUER_LT_FER:
                    final Object obj = issuerRating.getProperty(this.desc);
                    if (null == obj) {
                        return false;
                    }
                    final Rating rating = this.ratingSystem.getRating((String) obj);
                    return this.values.contains(withDetailedSymbol ? rating.getFullSymbol() :
                            rating.getSymbol());
                case RATING_FITCH_ISSUER_ST_DATE:
                case RATING_FITCH_ISSUER_LT_DATE:
                case RATING_FITCH_ISSUER_IFS_DATE:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT_D:
                case RATING_MDYS_ISSR_ST_D:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_D_B:
                case RATING_MDYS_ISSR_ST_D_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_D_SU:
                case RATING_MDYS_ISSR_ST_D_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_D_SU_B:
                case RATING_MDYS_ISSR_ST_D_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_D_BDR:
                case RATING_MDYS_ISSR_ST_D_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_D_BDR_B:
                case RATING_MDYS_ISSR_ST_D_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_D_IFSR:
                case RATING_MDYS_ISSR_ST_D_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_D_IFSR_B:
                case RATING_MDYS_ISSR_ST_D_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_ST_DATE:
                case RATING_SNP_ISSUER_LT_DATE:
                case RATING_SNP_ISSUER_ST_FSR_DATE:
                case RATING_SNP_ISSUER_LT_FSR_DATE:
                case RATING_SNP_ISSUER_ST_FER_DATE:
                case RATING_SNP_ISSUER_LT_FER_DATE:
                    final LocalDate date = (LocalDate) issuerRating.getProperty(this.desc);
                    return null != date && this.values.contains(DTF.print(date));
                default:
                    log.warn("<evaluate> no support for {} with op {}", this.desc, "In");
                    return false;
            }
        }
    }

    private static String cleanValue(IssuerRatingDescriptor desc, Terms.Relation.Op op,
            String value) {
        String cleanVal = value;
        switch (desc) {
            case SOURCE:
            case ISSUERNAME:
            case COUNTRYISO:
            case CURRENCYISO:
                cleanVal = value.startsWith("+") ? value.substring(1) : value;
                break;
        }

        return (IssuerRatingDescriptor.ISSUERNAME == desc && op == Terms.Relation.Op.EQ) ?
                cleanVal.toLowerCase() : cleanVal;
    }

    public static class Relation extends IssuerRatingFilter {

        private static final EnumSet<IssuerRatingDescriptor> DATES = EnumSet.of(
                IssuerRatingDescriptor.RATING_FITCH_ISSUER_ST_DATE,
                IssuerRatingDescriptor.RATING_FITCH_ISSUER_LT_DATE,
                IssuerRatingDescriptor.RATING_FITCH_ISSUER_IFS_DATE,
                // Counterparty Rating (CTP)
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D,
                // Counterparty Rating (CTP) backed
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_B,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_B,
                // Senior Unsecured Rating (SU)
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_SU,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_SU,
                // Senior Unsecured Rating (SU) backed
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_SU_B,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_SU_B,
                // Bank Deposit Rating (BDR)
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_BDR,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_BDR,
                // Bank Deposit Rating (BDR) backed
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_BDR_B,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_BDR_B,
                // Insurance Financial Strength Rating (IFS)
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_IFSR,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_IFSR,
                // Insurance Financial Strength Rating (IFS) backed
                IssuerRatingDescriptor.RATING_MDYS_ISSR_LT_D_IFSR_B,
                IssuerRatingDescriptor.RATING_MDYS_ISSR_ST_D_IFSR_B,
                // ---
                IssuerRatingDescriptor.RATING_SNP_ISSUER_ST_DATE,
                IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_DATE,
                IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FSR_DATE
        );

        private static final EnumSet<IssuerRatingDescriptor> CAN_MATCH = EnumSet.of(
                IssuerRatingDescriptor.ISSUERNAME,
                IssuerRatingDescriptor.RATING_FITCH_ISSUER_ST,
                IssuerRatingDescriptor.RATING_FITCH_ISSUER_LT,
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
                IssuerRatingDescriptor.RATING_SNP_ISSUER_ST,
                IssuerRatingDescriptor.RATING_SNP_ISSUER_LT,
                IssuerRatingDescriptor.RATING_SNP_ISSUER_LT_FSR
        );

        private final IssuerRatingDescriptor desc;

        private final Terms.Relation.Op op;

        private final String value;

        private final LocalDate dateValue;

        private final RatingSource source;

        private final RatingSystem ratingSystem;

        private final Rating rating;

        private Matcher matcher;

        public Relation(IssuerRatingDescriptor desc, Terms.Relation.Op op, final String value,
                RatingSystemProvider ratingSystemProvider) {
            if (null == value) {
                throw new IllegalArgumentException("invalid value: '" + value + "'");
            }
            if (Terms.Relation.Op.MATCHES == op && !CAN_MATCH.contains(desc)) {
                throw new IllegalArgumentException("can only apply matches on " + CAN_MATCH);
            }
            this.desc = desc;
            if (desc.isRating()) {
                this.ratingSystem = ratingSystemProvider.getRatingSystem(desc.name());
                this.rating = this.ratingSystem.getRating(value);
            }
            else {
                this.ratingSystem = null;
                this.rating = null;
            }
            this.op = op;
            this.value = cleanValue(desc, op, value);
            this.source = desc == IssuerRatingDescriptor.SOURCE ? RatingSource.valueOf(this.value) : null;
            if (DATES.contains(this.desc)) {
                this.dateValue = DTF.parseLocalDate(this.value);
            }
            else {
                this.dateValue = null;
            }
            if (Terms.Relation.Op.MATCHES == op) {
                this.matcher = Pattern.compile(value, Pattern.CASE_INSENSITIVE).matcher("");
            }
        }


        @Override
        public boolean evaluate(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            switch (this.op) {
                case EQ:
                    return isEqual(issuerRating, withDetailedSymbol);
                case NEQ:
                    return !isEqual(issuerRating, withDetailedSymbol);
                case MATCHES:
                    return isMatched(issuerRating);
                case LT:
                    return propExists(issuerRating) && compareTo(withDetailedSymbol, issuerRating, true);
                case GTE:
                    return propExists(issuerRating) && !compareTo(withDetailedSymbol, issuerRating, true);
                case GT:
                    return propExists(issuerRating) && compareTo(withDetailedSymbol, issuerRating, false);
                case LTE:
                    return propExists(issuerRating) && !compareTo(withDetailedSymbol, issuerRating, false);
                default:
                    throw new UnsupportedOperationException("no support for: " + this.op);
            }
        }

        private boolean propExists(IssuerRatingImpl issuerRating) {
            final Object prop = issuerRating.getProperty(this.desc);
            return null != prop;
        }

        private boolean compareTo(boolean withDetailedSymbol, IssuerRatingImpl issuerRating,
                boolean less) {
            switch (this.desc) {
                case RATING_FITCH_ISSUER_LT:
                case RATING_FITCH_ISSUER_ST:
                case RATING_FITCH_ISSUER_IFS:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT:
                case RATING_MDYS_ISSR_ST:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_B:
                case RATING_MDYS_ISSR_ST_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_SU:
                case RATING_MDYS_ISSR_ST_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_SU_B:
                case RATING_MDYS_ISSR_ST_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_BDR:
                case RATING_MDYS_ISSR_ST_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_BDR_B:
                case RATING_MDYS_ISSR_ST_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_IFSR:
                case RATING_MDYS_ISSR_ST_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_IFSR_B:
                case RATING_MDYS_ISSR_ST_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_LT:
                case RATING_SNP_ISSUER_ST:
                case RATING_SNP_ISSUER_LT_FSR:
                    final Rating ratingB = this.ratingSystem.getRating(
                            (String) issuerRating.getProperty(this.desc));
                    final int result = withDetailedSymbol ?
                            ratingB.compareTo(this.rating) :
                            ratingB.ordinal() - this.rating.ordinal();

                    return less ? result > 0 : result < 0;
                case RATING_FITCH_ISSUER_ST_DATE:
                case RATING_FITCH_ISSUER_LT_DATE:
                case RATING_FITCH_ISSUER_IFS_DATE:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT_D:
                case RATING_MDYS_ISSR_ST_D:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_D_B:
                case RATING_MDYS_ISSR_ST_D_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_D_SU:
                case RATING_MDYS_ISSR_ST_D_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_D_SU_B:
                case RATING_MDYS_ISSR_ST_D_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_D_BDR:
                case RATING_MDYS_ISSR_ST_D_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_D_BDR_B:
                case RATING_MDYS_ISSR_ST_D_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_D_IFSR:
                case RATING_MDYS_ISSR_ST_D_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_D_IFSR_B:
                case RATING_MDYS_ISSR_ST_D_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_ST_DATE:
                case RATING_SNP_ISSUER_LT_DATE:
                case RATING_SNP_ISSUER_LT_FSR_DATE:
                    final LocalDate date = (LocalDate) issuerRating.getProperty(this.desc);
                    return less ? this.dateValue.isAfter(date) : this.dateValue.isBefore(date);
                default:
                    log.warn("<compareTo> no support for {} with op {}", this.desc, "Compare");
                    return false;
            }
        }

        private boolean isMatched(IssuerRatingImpl issuerRating) {
            return this.matcher.reset(String.valueOf(issuerRating.getProperty(this.desc))).matches();
        }

        private boolean isEqual(IssuerRatingImpl issuerRating, boolean withDetailedSymbol) {
            switch (this.desc) {
                case ISSUERNAME:
                    return issuerNameEqualFuzzy(issuerRating, this.value);
                case LEI:
                    return this.value.equalsIgnoreCase(issuerRating.getLei());
                case COUNTRYISO:
                    return this.value.equals(issuerRating.getCountryIso());
                case CURRENCYISO:
                    return this.value.equals(issuerRating.getCurrencyIso());
                case SOURCE:
                    final RatingSource source = issuerRating.getSource();
                    return null != source && source == this.source;
                case RATING_FITCH_ISSUER_LT:
                case RATING_FITCH_ISSUER_ST:
                case RATING_FITCH_ISSUER_IFS:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT:
                case RATING_MDYS_ISSR_ST:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_B:
                case RATING_MDYS_ISSR_ST_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_SU:
                case RATING_MDYS_ISSR_ST_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_SU_B:
                case RATING_MDYS_ISSR_ST_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_BDR:
                case RATING_MDYS_ISSR_ST_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_BDR_B:
                case RATING_MDYS_ISSR_ST_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_IFSR:
                case RATING_MDYS_ISSR_ST_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_IFSR_B:
                case RATING_MDYS_ISSR_ST_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_LT:
                case RATING_SNP_ISSUER_ST:
                case RATING_SNP_ISSUER_LT_FSR:
                    final Object ratingSymbol = issuerRating.getProperty(this.desc);
                    if (null == ratingSymbol) {
                        return false;
                    }
                    final Rating ratingB = this.ratingSystem.getRating((String) ratingSymbol);
                    return withDetailedSymbol ?
                            this.rating.compareTo(ratingB) == 0 :
                            this.rating.ordinal() == ratingB.ordinal();
                case RATING_FITCH_ISSUER_ST_ACTION:
                case RATING_FITCH_ISSUER_LT_ACTION:
                case RATING_FITCH_ISSUER_IFS_ACTION:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT_A:
                case RATING_MDYS_ISSR_ST_A:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_A_B:
                case RATING_MDYS_ISSR_ST_A_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_A_SU:
                case RATING_MDYS_ISSR_ST_A_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_A_SU_B:
                case RATING_MDYS_ISSR_ST_A_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_A_BDR:
                case RATING_MDYS_ISSR_ST_A_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_A_BDR_B:
                case RATING_MDYS_ISSR_ST_A_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_A_IFSR:
                case RATING_MDYS_ISSR_ST_A_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_A_IFSR_B:
                case RATING_MDYS_ISSR_ST_A_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_ST_ACTION:
                case RATING_SNP_ISSUER_LT_ACTION:
                case RATING_SNP_ISSUER_LT_FSR_ACTN:
                case RATING_SNP_ISSUER_LT_RID:
                    final Entry.HasValue prop = (Entry.HasValue) issuerRating.getProperty(this.desc);
                    return null != prop && this.value.equals(prop.getValue());
                case RATING_FITCH_ISSUER_ST_DATE:
                case RATING_FITCH_ISSUER_LT_DATE:
                case RATING_FITCH_ISSUER_IFS_DATE:
                // Counterparty Rating (CTP)
                case RATING_MDYS_ISSR_LT_D:
                case RATING_MDYS_ISSR_ST_D:
                // Counterparty Rating (CTP) backed
                case RATING_MDYS_ISSR_LT_D_B:
                case RATING_MDYS_ISSR_ST_D_B:
                // Senior Unsecured Rating (SU)
                case RATING_MDYS_ISSR_LT_D_SU:
                case RATING_MDYS_ISSR_ST_D_SU:
                // Senior Unsecured Rating (SU) backed
                case RATING_MDYS_ISSR_LT_D_SU_B:
                case RATING_MDYS_ISSR_ST_D_SU_B:
                // Bank Deposit Rating (BDR)
                case RATING_MDYS_ISSR_LT_D_BDR:
                case RATING_MDYS_ISSR_ST_D_BDR:
                // Bank Deposit Rating (BDR) backed
                case RATING_MDYS_ISSR_LT_D_BDR_B:
                case RATING_MDYS_ISSR_ST_D_BDR_B:
                // Insurance Financial Strength Rating (IFS)
                case RATING_MDYS_ISSR_LT_D_IFSR:
                case RATING_MDYS_ISSR_ST_D_IFSR:
                // Insurance Financial Strength Rating (IFS) backed
                case RATING_MDYS_ISSR_LT_D_IFSR_B:
                case RATING_MDYS_ISSR_ST_D_IFSR_B:
                // ---
                case RATING_SNP_ISSUER_ST_DATE:
                case RATING_SNP_ISSUER_LT_DATE:
                case RATING_SNP_ISSUER_LT_FSR_DATE:
                    return this.dateValue.equals(issuerRating.getProperty(this.desc));
                default:
                    log.warn("<isEqual> no support for {} with op {}", this.desc, "Equal");
                    return false;
            }
        }
    }

    private static boolean issuerNameEqualFuzzy(IssuerRatingImpl issuerRating, String value) {
        return issuerRating.getIssuerName().toLowerCase().contains(value);
    }
}
