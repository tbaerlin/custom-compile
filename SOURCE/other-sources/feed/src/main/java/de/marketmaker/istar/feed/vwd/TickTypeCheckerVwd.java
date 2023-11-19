/*
 * TickTypeCheckerImpl.java
 *
 * Created on 09.12.2004 15:38:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.jcip.annotations.Immutable;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.TickTypeChecker;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp;
import static de.marketmaker.istar.feed.FeedUpdateFlags.*;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.*;
import static de.marketmaker.istar.feed.vwd.MarketTickFields.getDefaultTickFields;
import static de.marketmaker.istar.feed.vwd.MarketTickFields.getTickFieldIds;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_ADF_BLOCK_TRADE_ZEIT;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class TickTypeCheckerVwd implements TickTypeChecker {
    private static final Pattern SIMPLE_EXPR
            = Pattern.compile("(ADF_\\w+)\\s*(([=<>])\\s*(.+))?");

    /**
     * checks a parsed record for a certain property
     */
    private interface Check {
        boolean isOk(final ParsedRecord record);
    }

    private static final Check FALSE_CHECK = record -> false;

    /**
     * checks for the negation of another check
     */
    private static class NotCheck implements Check {
        private final Check check;

        public NotCheck(Check check) {
            this.check = check;
        }

        public boolean isOk(ParsedRecord record) {
            return !this.check.isOk(record);
        }

        public String toString() {
            return "!" + this.check.toString();
        }
    }

    /**
     * checks whether a certain field is present in a parsed record
     */
    private static class PresentCheck implements Check {
        protected final int fieldId;

        public PresentCheck(int fieldId) {
            this.fieldId = fieldId;
        }

        public boolean isOk(ParsedRecord record) {
            return record.isFieldPresent(this.fieldId);
        }

        public String toString() {
            return VwdFieldDescription.getField(this.fieldId).toString();
        }
    }

    /**
     * checks whether a number of checks all succeed
     */
    private static class AndCheck implements Check {
        private final Check[] terms;

        public AndCheck(Check[] terms) {
            this.terms = Arrays.copyOf(terms, terms.length);
        }

        public boolean isOk(ParsedRecord record) {
            for (Check check : terms) {
                if (!check.isOk(record)) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            return "AND" + Arrays.toString(this.terms);
        }
    }

    /**
     * checks whether at least one of a number of checks succeeds
     */
    private static class OrCheck implements Check {
        private final Check[] terms;

        public OrCheck(Check[] terms) {
            this.terms = Arrays.copyOf(terms, terms.length);
        }

        public boolean isOk(ParsedRecord record) {
            for (Check check : this.terms) {
                if (check.isOk(record)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "OR" + Arrays.toString(this.terms);
        }
    }

    private static class TimestampCheck implements Check {
        private final OperatorType op;

        private final int time;

        private TimestampCheck(OperatorType op, int time) {
            this.op = op;
            this.time = time;
        }

        public String toString() {
            return "TIMEOFARR" + this.op + TimeFormatter.formatSecondsInDay(this.time);
        }

        public boolean isOk(ParsedRecord pr) {
            return op.isOk(this.time, Timestamp.decodeTime(pr.getMessageTimestamp()));
        }
    }

    private static class PositivePriceCheck extends PresentCheck {
        private PositivePriceCheck(int fieldId) {
            super(fieldId);
        }

        public String toString() {
            return super.toString() + ">0";
        }

        public boolean isOk(ParsedRecord record) {
            if (!super.isOk(record)) {
                return false;
            }
            final long value = record.getNumericValue(this.fieldId);
            return ((int) value) > 0;
        }
    }

    /**
     * checks whether a certain numeric field is present with a defined value
     */
    private static class NumericValueCheck extends PresentCheck {

        private final long value;

        private final OperatorType operator;

        public NumericValueCheck(int fieldId, long value, OperatorType operator) {
            super(fieldId);
            this.value = value;
            this.operator = operator;
        }

        public boolean isOk(ParsedRecord record) {
            return super.isOk(record) && this.operator.isOk(this.value, getValue(record));
        }

        private long getValue(ParsedRecord record) {
            return record.getNumericValue(this.fieldId);
        }

        public String toString() {
            return super.toString() + " " + operator + " " + this.value;
        }
    }

    private enum OperatorType {
        LESS {
            public String toString() {
                return "<";
            }

            @Override
            boolean isOk(long base, long value) {
                return value < base;
            }
        },
        GREATER {
            public String toString() {
                return ">";
            }

            @Override
            boolean isOk(long base, long value) {
                return value > base;
            }
        },
        EQUAL {
            public String toString() {
                return "=";
            }

            @Override
            boolean isOk(long base, long value) {
                return base == value;
            }
        };

        abstract boolean isOk(long base, long value);
    }

    /**
     * checks whether a certain string field is present with a defined value
     */
    private static class StringValueCheck extends PresentCheck {
        private final byte[] value;

        public StringValueCheck(int fieldId, byte[] value) {
            super(fieldId);
            this.value = Arrays.copyOf(value, value.length);
        }

        public boolean isOk(ParsedRecord record) {
            return super.isOk(record) && Arrays.equals(this.value, record.getBytes(this.fieldId));
        }

        public String toString() {
            return super.toString() + "=\"" + new String(this.value) + "\"";
        }
    }


    private static class Factory {
        private final Properties p;

        private final Map<String, Check> checks = new HashMap<>();

        private Factory(Properties p) {
            this.p = p;
        }

        Map<String, TickTypeChecker> build() {
            expandProperties();

            final Set<String> marketNames = collectMarketNames();

            final HashMap<String, TickTypeChecker> result = new HashMap<>();
            for (String marketName : marketNames) {
                result.put(marketName, create(marketName));
            }
            result.put("PROF", PROF);
            return result;
        }

        /**
         * A property may refer to multiple, comma-separated markets. This methods adds the corresponding
         * property for all market names.
         */
        private void expandProperties() {
            for (String name : p.stringPropertyNames()) {
                if (!name.contains(",")) {
                    continue;
                }
                final int dot = name.indexOf('.');
                final String markets = name.substring(0, dot);
                final String[] marketNames = markets.split(",");
                for (String marketName : marketNames) {
                    final String existing
                            = (String) p.put(marketName + name.substring(dot), p.getProperty(name));
                    if (existing != null && !p.getProperty(name).equals(existing)) {
                        throw new IllegalArgumentException("Property " + name.substring(dot + 1) +
                                " for market " + marketName + " defined ambiguously");
                    }
                }
                p.remove(name);
            }
        }

        private Set<String> collectMarketNames() {
            return this.p.stringPropertyNames().stream()
                    .map(name -> name.substring(0, name.indexOf(".")))
                    .collect(Collectors.toSet());
        }

        private TickTypeChecker create(String marketName) {
            return new TickTypeCheckerVwd(marketName,
                    createCheck(marketName + ".trade"),
                    createCheck(marketName + ".ask"),
                    createCheck(marketName + ".bid"));
        }

        private Check createCheck(String s) {
            if (!p.containsKey(s)) {
                if (s.startsWith("default.")) {
                    throw new IllegalStateException("missing property " + s);
                }
                return createCheck("default" + s.substring(s.indexOf(".")));
            }
            final String expr = p.getProperty(s);
            if (this.checks.containsKey(expr)) {
                return this.checks.get(expr);
            }
            final Check result = parseDefinition(expr);
            this.checks.put(expr, result);
            return result;
        }
    }


    public static Map<String, TickTypeChecker> create(Properties p) {
        return new Factory(p).build();
    }

    private static int parseTime(String time) {
        return toMdpsTime(time);
    }

    private static int toMdpsTime(String time) {
        return encodeTime(toSecondOfDay(time));
    }

    private static int toSecondOfDay(String time) {
        if (time.indexOf(':') > 0) {
            final String[] s = time.split(":");
            return Integer.parseInt(s[0]) * 3600 + Integer.parseInt(s[1]) * 60;
        }
        return Integer.parseInt(time);
    }

    /**
     * Parses a textual check definition and returns the corresponding check.
     * @param s textual check definition
     * @return check
     */
    private static Check parseDefinition(String s) {
        if (!StringUtils.hasText(s)) {
            return FALSE_CHECK;
        }
        final String[] andTerms = s.split(Pattern.quote("&&"));
        if (andTerms.length > 1) {
            return new AndCheck(parseTerms(andTerms));
        }
        final String[] orTerms = s.split(Pattern.quote("||"));
        if (orTerms.length > 1) {
            return new OrCheck(parseTerms(orTerms));
        }
        final String term = s.trim();
        if (term.startsWith("!")) {
            return new NotCheck(parseDefinition(term.substring(1)));
        }

        final Matcher m = SIMPLE_EXPR.matcher(term);
        if (!m.matches()) {
            throw new IllegalArgumentException("invalid term '" + term + "'");
        }

        final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(m.group(1));
        if (field == null) {
            throw new IllegalArgumentException("unknown field: " + m.group(1));
        }
        if (m.group(2) == null) {
            return new PresentCheck(field.id());
        }
        else {
            final String opExpr = m.group(3);
            final String valueExpr = m.group(4);
            if (field.type() == VwdFieldDescription.Type.STRING) {
                if (!"=".equals(opExpr)) {
                    throw new IllegalArgumentException("invalid string term " + term);
                }
                final byte[] bytes = valueExpr.startsWith("'")
                        ? valueExpr.substring(1, valueExpr.length() - 1).getBytes()
                        : valueExpr.getBytes();
                return new StringValueCheck(field.id(), bytes);
            }
            else if (field.type() == VwdFieldDescription.Type.PRICE) {
                if (!">".equals(opExpr) || !"0".equals(valueExpr)) {
                    throw new IllegalArgumentException("invalid price term " + term);
                }
                return new PositivePriceCheck(field.id());
            }
            else {
                final OperatorType op = getOperatorType(term);
                if (field == VwdFieldDescription.ADF_TIMEOFARR) {
                    return new TimestampCheck(op, toSecondOfDay(valueExpr));
                }
                final long value = field.type() == VwdFieldDescription.Type.TIME
                        ? parseTime(valueExpr)
                        : Long.parseLong(valueExpr);
                return new NumericValueCheck(field.id(), value, op);
            }
        }
    }

    private static OperatorType getOperatorType(String term) {
        if (term.indexOf('<') != -1) {
            return OperatorType.LESS;
        }
        if (term.indexOf('>') != -1) {
            return OperatorType.GREATER;
        }
        return OperatorType.EQUAL;
    }

    private static Check[] parseTerms(final String[] terms) {
        final Check[] checks = new Check[terms.length];
        for (int i = 0; i < terms.length; i++) {
            checks[i] = parseDefinition(terms[i]);
        }
        return checks;
    }


    public final static TickTypeChecker PROF = new TickTypeChecker() {
        private boolean isProfessionalTrade(ParsedRecord record) {
            return record.isFieldPresent(ID_ADF_BLOCK_TRADE_ZEIT);
        }

        @Override
        public int getTickFlags(ParsedRecord pr) {
            return isProfessionalTrade(pr) ? FLAG_PROFESSIONAL_TRADE : 0;
        }

        @Override
        public TickTypeChecker forMarket(String marketName) {
            throw new UnsupportedOperationException();
        }
    };

    public final static TickTypeChecker DEFAULT = new TickTypeCheckerVwd("_",
            "ADF_Bezahlt>0 && !ADF_Rendite && !ADF_Schluss", "ADF_Brief>0", "ADF_Geld>0"
    );

    private final Check tradeCheck;

    private final Check askCheck;

    private final Check bidCheck;

    private final BitSet additionalTickFields;

    private final String marketName;

    private final int specialTickFlags;

    TickTypeCheckerVwd(String marketName, String tradeString, String askString, String bidString) {
        this(marketName, parseDefinition(tradeString), parseDefinition(askString), parseDefinition(bidString));
    }

    private TickTypeCheckerVwd(String marketName, Check tradeCheck, Check askCheck,
            Check bidCheck) {
        this.marketName = marketName;
        this.tradeCheck = tradeCheck;
        this.askCheck = askCheck;
        this.bidCheck = bidCheck;
        this.additionalTickFields = getTickFieldIds(marketName);
        this.specialTickFlags = (additionalTickFields != getDefaultTickFields())
                ? FLAG_WITH_SPECIAL_TICK_FIELDS : 0;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100).append("TickTypeCheckerVwd[")
                .append(this.marketName)
                .append(", trade=").append(this.tradeCheck)
                .append(", bid=").append(this.bidCheck)
                .append(", ask=").append(this.askCheck);
        if (this.additionalTickFields != getDefaultTickFields()) {
            sb.append(", additional=").append(this.additionalTickFields)
                    .append(this.additionalTickFields != getDefaultTickFields() ? "*" : "");
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int getTickFlags(ParsedRecord pr) {
        int result = 0;

        if (isBidTick(pr)) {
            result |= FLAG_WITH_BID;
        }
        if (isAskTick(pr)) {
            result |= FLAG_WITH_ASK;
        }
        if (isTradeTick(pr)) {
            result |= FLAG_WITH_TRADE;
        }

        // if result has B/A/T, the ordered TickBuilder will process the update anyway
        // and we can skip the costly computation of whether additional fields are present
        if (result == 0) {
            if (!isWithAdditionalTickFields(pr)) {
                return 0;
            }
            result |= FLAG_WITH_TICK_FIELD;
        }

        result |= this.specialTickFlags;
        return result;
    }

    @Override
    public TickTypeChecker forMarket(String marketName) {
        if (this.marketName.equals(marketName)) {
            return this;
        }
        if (this.additionalTickFields == getTickFieldIds(marketName)) {
            return this;
        }
        // the checker for marketName has different additional tick fields, so create it:
        return new TickTypeCheckerVwd(marketName, this.tradeCheck, this.askCheck, this.bidCheck);
    }

    private boolean isAskTick(ParsedRecord pr) {
        return this.askCheck.isOk(pr);
    }

    private boolean isBidTick(ParsedRecord pr) {
        return this.bidCheck.isOk(pr);
    }

    private boolean isTradeTick(ParsedRecord pr) {
        return this.tradeCheck.isOk(pr);
    }

    private boolean isWithAdditionalTickFields(ParsedRecord pr) {
        return pr.isAnyFieldPresent(this.additionalTickFields);
    }
}
