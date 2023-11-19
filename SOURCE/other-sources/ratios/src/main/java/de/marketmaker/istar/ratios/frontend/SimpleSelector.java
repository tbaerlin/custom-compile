/*
 * SimpleSelector.java
 *
 * Created on 26.10.2005 11:03:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.stream.IntStream;
import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * SimpleSelector to test a {@link Selectable} object.
 * All different types of criteria are implemented by different subclasses to be as efficient
 * as possible. Since searches using these selectors might be performed in parallel by multiple
 * threads, all subclasses need to be thread-safe and are implemented as Immutable.
 *
 * @author Oliver Flege
 */
@Immutable
abstract class SimpleSelector implements Selector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleSelector.class);

    /**
     * Identifies the field that allows to retrieve a value from a {@link Selectable}
     * object in order to test whether it satisfies this object's criterion
     */
    protected final int fieldid;

    @Immutable
    private static final class NullSelector extends SimpleSelector {

        private final int localeIndex;

        private NullSelector(int fieldid) {
            this(fieldid, 0);
        }

        private NullSelector(int fieldid, int localeIndex) {
            super(fieldid);
            this.localeIndex = localeIndex;
        }

        @Override
        public int getCost() {
            return 2;
        }

        @Override
        public boolean select(Selectable s) {
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(this.fieldid);

            switch (field.type()) {
                case DECIMAL:
                case NUMBER:
                case TIMESTAMP:
                    final Long l = s.getLong(this.fieldid);
                    return l == null || l == Long.MIN_VALUE;
                case STRING:
                    return s.getString(this.fieldid, this.localeIndex) == null;
                case DATE:
                case TIME:
                    final Integer i = s.getInt(this.fieldid);
                    return i == null || i == Integer.MIN_VALUE;
                case ENUMSET:
                    final BitSet bitSet = s.getBitSet(this.fieldid);
                    return bitSet == null || bitSet.isEmpty();
                case BOOLEAN:
                    return s.getBoolean(this.fieldid) == null;
            }
            return false;
        }

        public String toString() {
            return "f(" + this.fieldid + ") IS NULL";
        }
    }

    @Immutable
    private static final class IntInSelector extends SimpleSelector {
        private final int upper;

        private final int lower;

        private final boolean upperInclusive;

        private final boolean lowerInclusive;

        private IntInSelector(int fieldid, int lower, boolean lowerInclusive, int upper,
                boolean upperInclusive) {
            super(fieldid);
            this.upper = upper;
            this.lower = lower;
            this.upperInclusive = upperInclusive;
            this.lowerInclusive = lowerInclusive;
        }

        public boolean select(Selectable s) {
            final Integer value = s.getInt(this.fieldid);

            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(this.fieldid);
            if (value == null && field.isNullAsMin() != null) {
                return field.isNullAsMin() && this.lower == Integer.MIN_VALUE
                        || !field.isNullAsMin() && this.upper == Integer.MAX_VALUE;
            }

            return value != null
                    && (this.lowerInclusive ? value >= this.lower : value > this.lower)
                    && (this.upperInclusive ? value <= this.upper : value < this.upper);
        }

        public String toString() {
            return "f(" + this.fieldid + ") in " + (this.lowerInclusive ? "[" : "]")
                    + (this.lower != Integer.MIN_VALUE ? Integer.toString(this.lower) : "")
                    + ".."
                    + (this.upper != Integer.MAX_VALUE ? Integer.toString(this.upper) : "")
                    + (this.upperInclusive ? "]" : "[");
        }

        @Override
        public int getCost() {
            return 5;
        }
    }

    @Immutable
    private static final class IntOutSelector extends SimpleSelector {
        private final int upper;

        private final int lower;

        private final boolean upperInclusive;

        private final boolean lowerInclusive;

        private IntOutSelector(int fieldid, int lower, boolean lowerInclusive, int upper,
                boolean upperInclusive) {
            super(fieldid);
            this.upper = upper;
            this.lower = lower;
            this.upperInclusive = upperInclusive;
            this.lowerInclusive = lowerInclusive;
        }

        public boolean select(Selectable s) {
            final Integer value = s.getInt(this.fieldid);

            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(this.fieldid);
            if (value == null && field.isNullAsMin() != null) {
                return field.isNullAsMin() && this.lower == Integer.MIN_VALUE
                        || !field.isNullAsMin() && this.upper == Integer.MAX_VALUE;
            }

            return value != null
                    && (this.lowerInclusive ? value < this.lower : value <= this.lower)
                    && (this.upperInclusive ? value > this.upper : value >= this.upper);
        }

        public String toString() {
            return "f(" + this.fieldid + ") not in " + (this.lowerInclusive ? "[" : "]")
                    + (this.lower != Integer.MIN_VALUE ? Integer.toString(this.lower) : "")
                    + ".."
                    + (this.upper != Integer.MAX_VALUE ? Integer.toString(this.upper) : "")
                    + (this.upperInclusive ? "]" : "[");
        }

        @Override
        public int getCost() {
            return 5;
        }
    }

    @Immutable
    private static final class EnumSetSelector extends SimpleSelector {
        private final BitSet bits;

        private final boolean matchAllBits;

        private EnumSetSelector(int fieldid, BitSet bits, boolean matchAllBits) {
            super(fieldid);
            this.bits = bits;
            this.matchAllBits = matchAllBits;
        }

        @Override
        public boolean select(Selectable s) {
            return this.matchAllBits
                    ? s.getBitSet(this.fieldid).equals(this.bits) // match all bits
                    : s.getBitSet(this.fieldid).intersects(this.bits); // at least one bit match
        }

        @Override
        public int getCost() {
            return 1;
        }

        @Override
        public String toString() {
            return "f(" + this.fieldid + ") HAS [" + RatioEnumSet.toString(this.bits) + ", " + this.matchAllBits + "]";
        }
    }

    @Immutable
    private static final class LongSetSelector extends SimpleSelector {
        private final List<Long> values;

        private final boolean testIn;

        private LongSetSelector(int fieldid, List<Long> values, boolean testIn) {
            super(fieldid);
            this.values = values;
            this.testIn = testIn;
        }

        public boolean select(Selectable s) {
            final Long value = s.getLong(this.fieldid);
            final boolean b = this.values.contains(value);
            return this.testIn == b;
        }

        public String toString() {
            return "f(" + this.fieldid + ") " + (this.testIn ? "" : "not ") + "in " + this.values;
        }

        @Override
        public int getCost() {
            return 6 + this.values.size();
        }
    }

    @Immutable
    private static final class LongInSelector extends SimpleSelector {
        private final long upper;

        private final long lower;

        private final boolean upperInclusive;

        private final boolean lowerInclusive;

        private LongInSelector(int fieldid, long lower, boolean lowerInclusive, long upper,
                boolean upperInclusive) {
            super(fieldid);
            this.upper = upper;
            this.lower = lower;
            this.upperInclusive = upperInclusive;
            this.lowerInclusive = lowerInclusive;
        }

        public boolean select(Selectable s) {
            final Long value = s.getLong(this.fieldid);
            return value != null
                    && (this.lowerInclusive ? value >= this.lower : value > this.lower)
                    && (this.upperInclusive ? value <= this.upper : value < this.upper);
        }

        public String toString() {
            return "f(" + this.fieldid + ") in " + (this.lowerInclusive ? "[" : "]")
                    + (this.lower != Long.MIN_VALUE ? Long.toString(this.lower) : "")
                    + ".."
                    + (this.upper != Long.MAX_VALUE ? Long.toString(this.upper) : "")
                    + (this.upperInclusive ? "]" : "[");
        }

        @Override
        public int getCost() {
            return 6;
        }
    }

    @Immutable
    private static final class LongOutSelector extends SimpleSelector {
        private final long upper;

        private final long lower;

        private final boolean upperInclusive;

        private final boolean lowerInclusive;

        private LongOutSelector(int fieldid, long lower, boolean lowerInclusive, long upper,
                boolean upperInclusive) {
            super(fieldid);
            this.upper = upper;
            this.lower = lower;
            this.upperInclusive = upperInclusive;
            this.lowerInclusive = lowerInclusive;
        }

        public boolean select(Selectable s) {
            final Long value = s.getLong(this.fieldid);
            return value != null
                    && (this.lowerInclusive ? value < this.lower : value <= this.lower)
                    && (this.upperInclusive ? value > this.upper : value >= this.upper);
        }

        public String toString() {
            return "f(" + this.fieldid + ") not in " + (this.lowerInclusive ? "[" : "]")
                    + (this.lower != Long.MIN_VALUE ? Long.toString(this.lower) : "")
                    + ".."
                    + (this.upper != Long.MAX_VALUE ? Long.toString(this.upper) : "")
                    + (this.upperInclusive ? "]" : "[");
        }

        @Override
        public int getCost() {
            return 6;
        }
    }

    @Immutable
    private static final class CompareRatingSelector extends SimpleSelector {
        private final Rating rating;

        private final RatingSystem rs;

        private final boolean withDetailedSymbol;

        private final boolean inclusive;

        private final boolean less;

        private CompareRatingSelector(boolean less, int fieldid, Rating rating, boolean inclusive,
                RatingSystem rs, boolean withDetailedSymbol) {
            super(fieldid);
            this.inclusive = inclusive;
            this.rating = rating;
            this.rs = rs;
            this.withDetailedSymbol = withDetailedSymbol;
            this.less = less;
        }

        public boolean select(Selectable s) {
            final String str = s.getString(this.fieldid);
            if (null == str) {
                return false;
            }
            final Rating ratingB = this.rs.getRating(str); // no localization for ratings on purpose
            final int result = this.withDetailedSymbol ? ratingB.compareTo(this.rating) :
                    ratingB.ordinal() - this.rating.ordinal();

            return this.inclusive ?
                    (this.less ? result >= 0 : result <= 0) :
                    (this.less ? result > 0 : result < 0);
        }

        public String toString() {
            return "f(" + this.fieldid + ") compare(" + this.less + ")" + this.rating;
        }

        @Override
        public int getCost() {
            return 8;
        }
    }

    @Immutable
    private static final class RatingSelector extends SimpleSelector {
        private final Rating rating;

        private final RatingSystem rs;

        private final boolean withDetailedSymbol;

        private RatingSelector(int fieldid, Rating rating, RatingSystem rs,
                boolean withDetailedSymbol) {
            super(fieldid);
            this.rating = rating;
            this.rs = rs;
            this.withDetailedSymbol = withDetailedSymbol;
        }

        public boolean select(Selectable s) {
            final String str = s.getString(this.fieldid);
            if (null == str) {
                return false;
            }
            final Rating ratingB = this.rs.getRating(str); // no localization for ratings on purpose
            return this.withDetailedSymbol ? this.rating.compareTo(ratingB) == 0 :
                    this.rating.ordinal() == ratingB.ordinal();
        }

        public String toString() {
            return "f(" + this.fieldid + ") is [" + this.rating + "]";
        }

        @Override
        public int getCost() {
            return 8;
        }
    }

    @Immutable
    private static final class BooleanSelector extends SimpleSelector {
        private final boolean expected;

        private BooleanSelector(int fieldid, boolean expected) {
            super(fieldid);
            this.expected = expected;
        }

        public boolean select(Selectable s) {
            final Boolean value = s.getBoolean(this.fieldid);
            return value != null && value == this.expected;
        }

        public String toString() {
            return "f(" + this.fieldid + ")=" + this.expected;
        }

        @Override
        public int getCost() {
            return 3;
        }
    }

    @Immutable
    private static final class EnumSelector extends SimpleSelector {
        private final String expected;

        private final int localeIndex;

        private EnumSelector(int fieldid, int localeIndex, String expected) {
            super(fieldid);
            this.expected = expected;
            this.localeIndex = localeIndex;
        }

        public boolean select(Selectable s) {
            //noinspection StringEquality
            return this.expected == s.getString(this.fieldid, this.localeIndex);
        }

        @Override
        public int getCost() {
            return 2;
        }

        public String toString() {
            return "f(" + this.fieldid + ")==" + this.expected;
        }

    }

    @Immutable
    private static final class StringSelector extends SimpleSelector {
        private final String expected;

        private final int localeIndex;

        private StringSelector(int fieldid, int localeIndex, String expected) {
            super(fieldid);
            this.expected = expected;
            this.localeIndex = localeIndex;
        }

        public boolean select(Selectable s) {
            final String value = s.getString(this.fieldid, this.localeIndex);
            return this.expected.equalsIgnoreCase(value);
        }

        public String toString() {
            return "f(" + this.fieldid + ")=" + this.expected;
        }

        @Override
        public int getCost() {
            return 10;
        }
    }

    @Immutable
    private static final class StringSetSelector extends SimpleSelector {
        private final Set<String> expected = new HashSet<>();

        private final int localeIndex;

        private StringSetSelector(int fieldid, int localeIndex, String... expected) {
            super(fieldid);
            this.localeIndex = localeIndex;
            for (String s : expected) {
                this.expected.add(s.toUpperCase());
            }
        }

        private StringSetSelector(int fieldid, int localeIndex, Set<String> expected) {
            super(fieldid);
            this.localeIndex = localeIndex;
            this.expected.addAll(expected);
        }

        public boolean select(Selectable s) {
            final String value = s.getString(this.fieldid, this.localeIndex);
            return (value != null) && this.expected.contains(value);
        }

        public String toString() {
            return "f(" + this.fieldid + ") IN " + this.expected;
        }

        @Override
        public int getCost() {
            return 7;
        }
    }

    @Immutable
    private static final class PatternSelector extends SimpleSelector {
        private final Pattern pattern;

        private final String s;

        private final int localeIndex;

        private PatternSelector(int fieldid, int localeIndex, Pattern pattern, String s) {
            super(fieldid);
            this.pattern = pattern;
            this.s = s;
            this.localeIndex = localeIndex;
        }

        public boolean select(Selectable s) {
            final String value = s.getString(this.fieldid, this.localeIndex);
            return value != null && this.pattern.matcher(value).find();
        }

        public String toString() {
            return "f(" + this.fieldid + ")=~" + this.s;
        }

        @Override
        public int getCost() {
            return 20;
        }
    }

    protected SimpleSelector(int fieldid) {
        this.fieldid = fieldid;
    }

    static Selector create(int fieldid, int lower, boolean lowerInclusive, int upper,
            boolean upperInclusive, boolean testIn) {
        return testIn
                ? new IntInSelector(fieldid, lower, lowerInclusive, upper, upperInclusive)
                : new IntOutSelector(fieldid, lower, lowerInclusive, upper, upperInclusive);
    }

    static Selector create(int fieldid, long lower, boolean lowerInclusive, long upper,
            boolean upperInclusive, boolean testIn) {
        return testIn
                ? new LongInSelector(fieldid, lower, lowerInclusive, upper, upperInclusive)
                : new LongOutSelector(fieldid, lower, lowerInclusive, upper, upperInclusive);
    }

    static Selector createEnumSetSelector(int fieldId, BitSet bits, boolean matchAllBits) {
        return new EnumSetSelector(fieldId, bits, matchAllBits);
    }

    static Selector create(int fieldid, List<Long> values, boolean testIn) {
        return new LongSetSelector(fieldid, values, testIn);
    }

    static Selector createNotNull(int fieldid, int localeIndex) {
        return new NotSelector(new NullSelector(fieldid, localeIndex));
    }

    static Selector createNull(int fieldid, int localeIndex) {
        return new NullSelector(fieldid, localeIndex);
    }

    static Selector negate(Selector selector) {
        return new NotSelector(selector);
    }

    static Selector createBetterThan(int fieldid, RatingSystem rs, boolean withDetailedSymbol,
            Rating rating, boolean inclusive) {
        return new CompareRatingSelector(false, fieldid, rating, inclusive, rs, withDetailedSymbol);
    }

    static Selector createWorseThan(int fieldid, RatingSystem rs, boolean withDetailedSymbol,
            Rating rating, boolean inclusive) {
        return new CompareRatingSelector(true, fieldid, rating, inclusive, rs, withDetailedSymbol);
    }

    static Selector create(int fieldid, boolean expected) {
        return new BooleanSelector(fieldid, expected);
    }

    static Selector any(int fieldid, int localeIndex, String... elements) {
        return new StringSetSelector(fieldid, localeIndex, elements);
    }

    static Selector create(int fieldid, int localeIndex, String expr, boolean inout) {
        return inout
                ? create(fieldid, localeIndex, expr)
                : new NotSelector(create(fieldid, localeIndex, expr));
    }

    static Selector createRatingSelectorExact(int fieldId, Rating rating,
            boolean withDetailedSymbol, RatingSystem rs, boolean negate) {
        final RatingSelector selector = new RatingSelector(fieldId, rating, rs, withDetailedSymbol);
        return negate ? new NotSelector(selector) : selector;
    }

    /**
     * Create new Criterion that checks for a String value.
     *
     * @param fieldid identifies field to be tested
     * @param expr String value to be tested. If s is enclosed in "'", an exact match will
     * be required, otherwise a case insensitive pattern match will be performed.
     * @return new string based selector
     */
    private static Selector create(int fieldid, int localeIndex, String expr) {
        if (expr == null || expr.trim().length() == 0 || (expr.startsWith("~") && expr.trim().length() == 1)) {
            throw new IllegalArgumentException("empty string not allowed");
        }

        if (isQuoted(expr) && expr.length() >= 2) {
            return createProperSelector(fieldid, localeIndex, expr.substring(1, expr.length() - 1));
        }
        else if (isIsinField(fieldid) && isIsinForAll(expr)) {
            return createProperSelector(fieldid, localeIndex, expr.toUpperCase());
        }
        else {
            final boolean exactMatch = expr.startsWith("+");
            if (exactMatch) {
                return createProperSelector(fieldid, localeIndex, expr.substring(1));
            }
            final boolean regexSearch = expr.startsWith("~");
            final String s = regexSearch ? expr.substring(1) : expr;

            try {
                final Pattern p = regexSearch
                        ? Pattern.compile(s, Pattern.CASE_INSENSITIVE)
                        : Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE);

                if (isEnumField(fieldid)) {
                    return createEnumMatch(fieldid, localeIndex, p);
                }

                return new PatternSelector(fieldid, localeIndex, p, s);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("illegal pattern syntax in " + expr);
            }
        }
    }

    private static Selector createEnumMatch(int fieldid, int localeIndex, Pattern p) {
        Set<String> expected = new HashSet<>();
        for (String value : EnumFlyweightFactory.values(fieldid)) {
            if (p.matcher(value).find()) {
                expected.add(value);
            }
        }
        if (expected.isEmpty()) {
            return Selector.FALSE;
        }
        if (expected.size() == 1) {
            return new EnumSelector(fieldid, localeIndex, expected.iterator().next());
        }
        return new StringSetSelector(fieldid, localeIndex, expected);
    }

    static boolean isIsinForAll(String... expr) {
        for (String anExpr : expr) {
            if (!IsinUtil.isIsin(anExpr)) {
                return false;
            }
        }
        return true;
    }

    static boolean isIsinField(int fieldid) {
        return fieldid == RatioFieldDescription.isin.id()
                || fieldid == RatioFieldDescription.underlyingIsin.id();
    }

    static boolean isVwdcodeField(int fieldid) {
        return fieldid == RatioFieldDescription.vwdCode.id();
    }

    static boolean isFondsVwdcodeForAll(String... expr) {
        for (String anExpr : expr) {
            final String[] parts = anExpr.split("\\.");
            if (parts.length != 3 || !IsinUtil.isIsin(parts[0]) || !"FONDS".equals(parts[1]) || parts[2].length() != 3) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEnumField(int fieldid) {
        return RatioFieldDescription.getFieldById(fieldid).isEnum();
    }

    private static Selector createProperSelector(int fieldid, int localeIndex, String expr) {
        if (isEnumField(fieldid)) {
            String expected = EnumFlyweightFactory.get(fieldid, expr);
            if (expected == null) {
                if (EnumFlyweightFactory.WITH_LAZY_ENUMS) {
                    expected = EnumFlyweightFactory.intern(fieldid, expr);
                }
                else {
                    LOGGER.warn("<createProperSelector> '" + expr + "' matches nothing in f(" + fieldid + ")");
                    return Selector.FALSE;
                }
            }
            return new EnumSelector(fieldid, localeIndex, expected);
        }
        else {
            return new StringSelector(fieldid, localeIndex, expr);
        }
    }

    private static boolean isQuoted(String expr) {
        return (expr.startsWith("'") && expr.endsWith("'"))
                || (expr.startsWith("\"") && expr.endsWith("\""));
    }
}
