/*
 * RatioDataComparator.java
 *
 * Created on 26.10.2005 12:02:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.ibm.icu.text.Collator;

import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class RatioDataComparator implements Comparator<QuoteRatios> {

    /**
     * maturity values for ZNS are strings that denote a period (e.g., "6M", "10Y");
     * this comparator orders those values from shorter to longer periods.
     */
    private static class MaturityComparator implements Comparator<QuoteRatios> {

        private static final int FID = RatioFieldDescription.maturity.id();

        @Override
        public int compare(QuoteRatios leftQuote, QuoteRatios rightQuote) {
            // the slow part is to parse the integer prefix, so we avoid that wherever possible
            final String left = leftQuote.getInstrumentRatios().getString(FID);
            final String right = rightQuote.getInstrumentRatios().getString(FID);
            if (left == right) { // maturity is an enum, so == is ok
                return 0;
            }
            if (isInvalid(left)) {
                return isInvalid(right) ? 0 : 1;
            }
            else if (isInvalid(right)) {
                return -1;
            }
            final char leftUnit = left.charAt(left.length() - 1);
            final char rightUnit = right.charAt(right.length() - 1);
            if (leftUnit == rightUnit) {
                final int cmp = left.length() - right.length();
                return cmp != 0 ? cmp : left.compareTo(right);
            }
            return Integer.compare(toMonths(left, leftUnit), toMonths(right, rightUnit));
        }

        private boolean isInvalid(String maturity) {
            return maturity == null || maturity.length() < 2;
        }

        private int toMonths(String maturitySpec, char unit) {
            try {
                int value = Integer.parseInt(maturitySpec.substring(0, maturitySpec.length() - 1));
                switch (unit) {
                    case 'Y':
                        return 12 * value;
                    case 'M':
                        return  value;
                    default:
                        return Integer.MAX_VALUE;
                }
            } catch (NumberFormatException ex) {
                return Integer.MAX_VALUE;
            }
        }
    }

    static final Comparator<QuoteRatios> COMPARATOR_MATURITY
            = new RatioDataComparator.MaturityComparator();

    private static final Comparator<QuoteRatios> COMPARATOR_MATURITY_REVERSE =
            Collections.reverseOrder(COMPARATOR_MATURITY);


    /**
     * used to sort german strings with umlauts. We cannot use
     * {@link java.text.Collator#getInstance(java.util.Locale)}, as the resulting
     * {@link java.text.RuleBasedCollator} is made thread-safe by declaring its compare method
     * <tt>synchronized</tt> and <tt>getInstance</tt> returns the same object for the same locale,
     * so all threads end up using the same object whose <tt>compare</tt> method becomes the
     * bottleneck (10 parallel PagedResultSorters using the same <tt>java.text.RuleBasedCollator</tt>
     * take about twice as long to process 1m records than a single thread (5s compared to 2.5s
     * on provider machines; using <tt>ThreadLocal&lt;com.ibm.icu.text.Collator&gt;</tt>s in
     * 10 threads takes 0.5s).
     */
    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    private static ThreadLocal<Collator> COLLATOR = new ThreadLocal<Collator>() {
        @Override
        protected Collator initialValue() {
            return GERMAN_COLLATOR.cloneAsThawed();
        }
    };

    /**
     * field used for comparing objects
     */
    private final RatioFieldDescription.Field field;

    private final int localeIndex;

    private final boolean isReverse;

    final RatingSystem ratingSystem;

    static {
        // tell collator to ignore case differences
        GERMAN_COLLATOR.setStrength(Collator.PRIMARY);
    }

    static Comparator<QuoteRatios> create(final int fieldId, List<Locale> locales,
            boolean reverse, RatingSystemProvider ratingSystemProvider) {
        if (fieldId == MaturityComparator.FID) {
            return reverse ? COMPARATOR_MATURITY_REVERSE : COMPARATOR_MATURITY;
        }
        return new RatioDataComparator(fieldId, locales, reverse, ratingSystemProvider);
    }

    private RatioDataComparator(final int fieldid, List<Locale> locales, boolean isReverse,
            RatingSystemProvider ratingSystemProvider) {
        this.field = RatioFieldDescription.getFieldById(fieldid);
        this.localeIndex = RatioFieldDescription.getLocaleIndex(this.field, locales);
        this.isReverse = isReverse;
        this.ratingSystem = (null != ratingSystemProvider && null != this.field.getRatingSystemName()) ?
                ratingSystemProvider.getRatingSystem(this.field.getRatingSystemName()) : null;
    }

    public String toString() {
        return "RatioDataComparator[" + this.field
                + " " + (this.isReverse ? "desc" : "asc")
                + "]";
    }

    /**
     * Returns the field used for sorting
     *
     * @return sort field
     */
    public int getFieldid() {
        return this.field.id();
    }

    public int compare(QuoteRatios dr1, QuoteRatios dr2) {
        int result;

        final boolean isInstrumentField = this.field.isInstrumentField();

        final Selectable sr1 = isInstrumentField ? dr1.getInstrumentRatios() : dr1;
        final Selectable sr2 = isInstrumentField ? dr2.getInstrumentRatios() : dr2;

        if ((this.field.type() == RatioFieldDescription.Type.STRING)) {
            if (null == this.ratingSystem) {
                final String s1 = sr1.getString(this.field.id(), this.localeIndex);
                final String s2 = sr2.getString(this.field.id(), this.localeIndex);

                if (s1 == null) {
                    return (s2 == null) ? 0 : 1;
                }
                if (s2 == null) {
                    return -1;
                }
                result = COLLATOR.get().compare(s1, s2);
            }
            else {
                final String s1 = sr1.getString(this.field.id());
                final String s2 = sr2.getString(this.field.id());
                if (s1 == null) {
                    return (s2 == null) ? 0 : 1;
                }
                if (s2 == null) {
                    return -1;
                }
                else {
                    result = this.ratingSystem.getRating(s2).compareTo(this.ratingSystem.getRating(s1));
                }
            }
        }
        else {
            final Long l1 = getNumber(sr1);
            final Long l2 = getNumber(sr2);

            if (l1 == null) {
                return (l2 == null) ? 0 : 1;
            }
            if (l2 == null) {
                return -1;
            }

            result = l1.compareTo(l2);
        }

        return (this.isReverse) ? -result : result;
    }

    private Long getNumber(Selectable sr) {
        switch (this.field.type()) {
            case NUMBER:
            case DECIMAL:
            case TIMESTAMP:
                return sr.getLong(this.field.id());
            case DATE:
            case TIME:
                final Integer anInt = sr.getInt(this.field.id());
                return (anInt != null) ? anInt.longValue() : null;
            case BOOLEAN:
                final Boolean aBoolean = sr.getBoolean(this.field.id());
                return (aBoolean != null) ? (aBoolean ? 1L : 0L) : null;
            default:
                throw new IllegalArgumentException("Illegal numeric type: " + this.field.type());
        }
    }
}
