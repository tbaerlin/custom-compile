/*
 * VendorkeyFilterImpl.java
 *
 * Created on 15.04.2005 14:58:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.ByteStringMap;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PeriodEditor;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * A vendor key filter factory which can create {@link VendorkeyFilter} based on a given specification.
 * This class can also work as a spring factory bean({@link VendorkeyFilter}) with a pre-configured
 * specification.
 *
 * <p>
 * A vendor key filter specification is defined as following:
 * <dl>
 * <dt>SPEC</dt><dd>NOT_TERM | COMP_TERM</dd>
 * <dt>NOT_TERM</dt><dd>!SPEC | !( SPEC )</dd>
 * <dt>COMP_TERM</dt><dd>TERM | OR_TERM</dd>
 * <dt>OR_TERM</dt><dd>AND_TERM [|| AND_TERM]+</dd>
 * <dt>AND_TERM</dt><dd>SPEC [&& SPEC]+</dd>
 * <dt>TERM</dt><dd>"" | "*" | t:[0-9]+ | [^]?[m:]?[OP]?[A-Z0-9]+[$]?</dd>
 * <dt>OP</dt><dd>">" | "<" | "<=" | ">="</dd>
 * </dl>
 * Example:
 * <table border="1">
 * <tr><th>Specification</th><th>Description</th></tr>
 * <tr><td>""</td><td>Empty string accepts no vendor key</td></tr>
 * <tr><td>"*"</td><td>Accepts any vendor keys</td></tr>
 * <tr><td>"A0"</td><td>Accepts any vendor keys that contain "A0"</td></tr>
 * <tr><td>"!A0C4CB"</td><td>Accepts any vendor key except "A0C4CB"</td></tr>
 * <tr><td>"&gt;=A0C4CB &amp;&amp; &lt;A0C4CF"</td><td>Accepts vendor keys that are greater equal "A0C4CB"
 * but less than "A0C4CF"</td></tr>
 * <tr><td>"^YC"</td><td>Accepts vendor keys that start with "YC"</td></tr>
 * <tr><td>"YC$"</td><td>Accepts vendor keys that end with "YC"</td></tr>
 * <tr><td>"^YC$"</td><td>Accepts vendor key "YC" exactly</td></tr>
 * <tr><td>"^m:FFM$"</td><td>Accepts vendor key which has market name "FFM" exactly</td></tr>
 * <tr><td>"t:[0-9]+"</td><td>Accepts vendor key with the given numeric (=xfeed) type</td></tr>
 * <tr><td>"mat:<em>yyyymmdd</em>"</td><td>Accepts all keys except options with maturity < yyyymmdd<br>
 * <tr><td>"mat:<em>Period</em>"</td><td>Accepts all keys except options with maturity < now()-Period<br>
 * <em>Period</em> is specified using iso format, e.g., <tt>P3M</tt></td></tr>
 * </table>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @see de.marketmaker.istar.feed.VendorkeyFilterEditor
 */
public class VendorkeyFilterFactory implements FactoryBean<VendorkeyFilter> {

    private enum MatchType {
        STARTS_WITH,
        ENDS_WITH,
        LESS_THAN,
        LESS_THAN_OR_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
        EXACT,
        CONTAINS,
        ALL,
        NONE
    }

    public static final VendorkeyFilter ACCEPT_ALL = new SimpleFilter(MatchType.ALL);

    public static final VendorkeyFilter ACCEPT_NONE = new SimpleFilter(MatchType.NONE);

    private String spec;


    private static class VwdCodeSetFilter implements VendorkeyFilter {
        private final Set<ByteString> vwdcodes;

        private VwdCodeSetFilter(Set<ByteString> vwdcodes) {
            this.vwdcodes = vwdcodes;
        }

        @Override
        public boolean test(Vendorkey vkey) {
            return test(vkey.toVwdcode());
        }

        private boolean test(ByteString bs) {
            return this.vwdcodes.contains(bs);
        }
    }

    public static class NotFilter implements VendorkeyFilter {

        private final VendorkeyFilter filter;

        NotFilter(VendorkeyFilter filter) {
            this.filter = filter;
        }

        public VendorkeyFilter getFilter() {
            return filter;
        }

        public String toString() {
            return "!" + this.filter;
        }

        public boolean test(Vendorkey vkey) {
            return !this.filter.test(vkey);
        }

        @Override
        public boolean test(byte[] bytes, int from, int to) {
            return !this.filter.test(bytes, from, to);
        }
    }

    private static class OrFilter implements VendorkeyFilter {
        private final VendorkeyFilter[] filters;

        public OrFilter(VendorkeyFilter[] filters) {
            this.filters = filters;
        }

        public String toString() {
            return Arrays.stream(this.filters)
                    .map(VendorkeyFilter::toString)
                    .collect(Collectors.joining(") || (", "(", ")"));
        }

        public boolean test(Vendorkey vkey) {
            for (VendorkeyFilter filter : filters) {
                if (filter.test(vkey)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean test(byte[] bytes, int from, int to) {
            for (VendorkeyFilter filter : filters) {
                if (filter.test(bytes, from, to)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class AndFilter implements VendorkeyFilter {
        private final VendorkeyFilter[] filters;

        public AndFilter(VendorkeyFilter[] filters) {
            this.filters = filters;
        }

        public String toString() {
            return Arrays.stream(this.filters)
                    .map(VendorkeyFilter::toString)
                    .collect(Collectors.joining(") && (", "(", ")"));
        }

        public boolean test(Vendorkey vkey) {
            for (VendorkeyFilter filter : filters) {
                if (!filter.test(vkey)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean test(byte[] bytes, int from, int to) {
            for (VendorkeyFilter filter : filters) {
                if (!filter.test(bytes, from, to)) {
                    return false;
                }
            }
            return true;
        }
    }


    private static class SimpleFilter implements VendorkeyFilter {
        protected final ByteString match;

        protected final byte[] matchBytes;

        protected final MatchType type;

        private SimpleFilter(MatchType type) {
            this.match = null;
            this.matchBytes = null;
            this.type = type;
        }

        public SimpleFilter(ByteString match, MatchType type) {
            this.match = match;
            this.matchBytes = match.getBytes();
            this.type = type;
        }

        public String toString() {
            switch (this.type) {
                case ALL:
                    return "*";
                case NONE:
                    return "!*";
                default:
                    final StringBuilder sb = new StringBuilder();
                    if (this.type == MatchType.LESS_THAN) {
                        sb.append("<");
                    }
                    else if (this.type == MatchType.LESS_THAN_OR_EQUALS) {
                        sb.append("<=");
                    }
                    else if (this.type == MatchType.GREATER_THAN) {
                        sb.append(">");
                    }
                    else if (this.type == MatchType.GREATER_THAN_OR_EQUALS) {
                        sb.append(">=");
                    }
                    else if (this.type == MatchType.STARTS_WITH || this.type == MatchType.EXACT) {
                        sb.append("^");
                    }
                    sb.append(getMatchToString());
                    if (this.type == MatchType.ENDS_WITH || this.type == MatchType.EXACT) {
                        sb.append("$");
                    }
                    return sb.toString();
            }
        }

        public boolean test(Vendorkey vkey) {
            switch (this.type) {
                case ALL:
                    return true;
                case NONE:
                    return false;
                case STARTS_WITH:
                    return getCompareString(vkey).startsWith(this.match);
                case ENDS_WITH:
                    return getCompareString(vkey).endsWith(this.match);
                case EXACT:
                    return getCompareString(vkey).equals(this.match);
                case CONTAINS:
                    return getCompareString(vkey).indexOf(this.match) != -1;
                case LESS_THAN:
                    return getCompareString(vkey).compareTo(this.match, this.match.length()) < 0;
                case LESS_THAN_OR_EQUALS:
                    return getCompareString(vkey).compareTo(this.match, this.match.length()) < 1;
                case GREATER_THAN:
                    return getCompareString(vkey).compareTo(this.match, this.match.length()) > 0;
                case GREATER_THAN_OR_EQUALS:
                    return getCompareString(vkey).compareTo(this.match, this.match.length()) > -1;
            }
            return false;
        }

        @Override
        public boolean test(byte[] bytes, int from, int to) {
            switch (this.type) {
                case ALL:
                    return true;
                case NONE:
                    return false;
                case STARTS_WITH:
                    return startsWith(bytes, from, to);
                case ENDS_WITH:
                    return endsWith(bytes, from, to);
                case EXACT:
                    return isExactMatch(bytes, from, to);
                case CONTAINS:
                    return contains(bytes, from, to);
                case LESS_THAN:
                    return lessThan(bytes, from, to);
                case LESS_THAN_OR_EQUALS:
                    return lessThanOrEqual(bytes, from, to);
                case GREATER_THAN:
                    return greaterThan(bytes, from, to);
                case GREATER_THAN_OR_EQUALS:
                    return greaterThanOrEqual(bytes, from, to);
            }
            return false;
        }

        private boolean contains(byte[] bytes, int from, int to) {
            NEXT:
            for (int i = from, n = to - this.matchBytes.length; i <= n; i++) {
                for (int j = 0, k = i; j < this.matchBytes.length; j++, k++) {
                    if (this.matchBytes[j] != bytes[k]) {
                        continue NEXT;
                    }
                }
                return true;
            }
            return false;
        }

        private boolean isExactMatch(byte[] bytes, int from, int to) {
            if (this.matchBytes.length != to - from) {
                return false;
            }
            for (int i = 0, j = from; i < this.matchBytes.length && j < to; i++, j++) {
                if (this.matchBytes[i] != bytes[j]) {
                    return false;
                }
            }
            return true;
        }

        private boolean endsWith(byte[] bytes, int from, int to) {
            if (this.matchBytes.length > to - from) {
                return false;
            }
            for (int i = this.matchBytes.length, j = to; i-- != 0 && j-- > from; ) {
                if (this.matchBytes[i] != bytes[j]) {
                    return false;
                }
            }
            return true;
        }

        private boolean startsWith(byte[] bytes, int from, int to) {
            if (this.matchBytes.length > to - from) {
                return false;
            }
            for (int i = 0, j = from; i < this.matchBytes.length && j < to; i++, j++) {
                if (this.matchBytes[i] != bytes[j]) {
                    return false;
                }
            }
            return true;
        }

        private boolean lessThan(byte[] bytes, int from, int to) {
            for (int i = 0, j = from; i < this.matchBytes.length && j < to; i++, j++) {
                if (bytes[j] < this.matchBytes[i]) {
                    return true;
                }
                if (bytes[j] > this.matchBytes[i]) {
                    return false;
                }
            }
            return false;
        }

        private boolean lessThanOrEqual(byte[] bytes, int from, int to) {
            for (int i = 0, j = from; i < this.matchBytes.length && j < to; i++, j++) {
                if (bytes[j] < this.matchBytes[i]) {
                    return true;
                }
                if (bytes[j] > this.matchBytes[i]) {
                    return false;
                }
            }
            return true;
        }

        private boolean greaterThan(byte[] bytes, int from, int to) {
            for (int i = 0, j = from; i < this.matchBytes.length && j < to; i++, j++) {
                if (bytes[j] < this.matchBytes[i]) {
                    return false;
                }
                if (bytes[j] > this.matchBytes[i]) {
                    return true;
                }
            }
            return false;
        }

        private boolean greaterThanOrEqual(byte[] bytes, int from, int to) {
            for (int i = 0, j = from; i < this.matchBytes.length && j < to; i++, j++) {
                if (bytes[j] < this.matchBytes[i]) {
                    return false;
                }
                if (bytes[j] > this.matchBytes[i]) {
                    return true;
                }
            }
            return true;
        }

        protected ByteString getCompareString(Vendorkey vkey) {
            return vkey.toVwdcode();
        }

        protected String getMatchToString() {
            return this.match.toString();
        }
    }

    public static class MarketFilter extends SimpleFilter {
        public MarketFilter(ByteString match, MatchType type) {
            super(match, type);
        }

        protected String getMatchToString() {
            return "m:" + super.getMatchToString();
        }

        public String getMarket() { return super.getMatchToString(); }

        protected ByteString getCompareString(Vendorkey vkey) {
            return vkey.getMarketName();
        }

        @Override
        public boolean test(byte[] bytes, int from, int to) {
            int marketFrom = 0;
            int marketTo = to;
            for (int i = from + 1; i < to; i++) {
                if (bytes[i] == '.') {
                    if (marketFrom == 0) {
                        marketFrom = i + 1;
                    }
                    else {
                        marketTo = i;
                        break;
                    }
                }
            }
            return (marketFrom != 0) && (marketFrom != to)
                    && super.test(bytes, marketFrom, marketTo);
        }
    }

    public static class MarketSetFilter implements VendorkeyFilter {
        private final ByteStringMap<Boolean> markets = new ByteStringMap<>();

        private MarketSetFilter(List<MarketFilter> marketFilters) {
            marketFilters.stream()
                    .map(m -> m.match)
                    .forEach(bs -> this.markets.put(bs, Boolean.TRUE));
        }

        public MarketSetFilter(String[] match) {
            for (String s : match) {
                this.markets.put(new ByteString(s.trim()), Boolean.TRUE);
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("^m:");
            sb.append(
                    this.markets.keySet()
                            .stream()
                            .map(ByteString::toString)
                            .collect(Collectors.joining(","))
            );
            sb.append("$");
            return sb.toString();
        }

        public Set<String> toSet() {
            return this.markets.keySet()
                    .stream()
                    .map(ByteString::toString)
                    .collect(Collectors.toSet());
        }

        @Override
        public boolean test(Vendorkey vkey) {
            return this.markets.containsKey(vkey.getMarketName());
        }

        @Override
        public boolean test(byte[] bytes, int from, int to) {
            int marketFrom = 0;
            int length = 0;
            for (int i = from + 1; i < to; i++) {
                if (bytes[i] == '.') {
                    if (marketFrom == 0) {
                        marketFrom = i + 1;
                        length = to - marketFrom;
                    }
                    else {
                        length = i - marketFrom;
                        break;
                    }
                }
            }
            return (marketFrom != 0) && (marketFrom != to)
                    && this.markets.get(bytes, marketFrom, length) != null;
        }
    }

    private static class TypeFilter implements VendorkeyFilter {
        static final String PREFIX = "t:";

        private final int type;

        public TypeFilter(int type) {
            this.type = type;
        }

        public String toString() {
            return PREFIX + this.type;
        }

        public boolean test(Vendorkey vkey) {
            return this.type == vkey.getType();
        }
    }

    private static class MaturityFilter implements VendorkeyFilter {
        static final String PREFIX = "mat:";

        private final int yyyymmdd;

        public MaturityFilter(String s) {
            if (s.startsWith("P")) {
                this.yyyymmdd = DateUtil.toYyyyMmDd(new LocalDate().minus(PeriodEditor.fromText(s)));
            }
            else {
                this.yyyymmdd = Integer.parseInt(s);
            }
        }

        public String toString() {
            return PREFIX + this.yyyymmdd;
        }

        public boolean test(Vendorkey vkey) {
            final int value = VendorkeyUtils.getOptionMaturity((VendorkeyVwd) vkey);
            return value == 0 || value >= this.yyyymmdd;
        }
    }

    private static MatchType inferType(String s) {
        if (s.startsWith("<=")) {
            return MatchType.LESS_THAN_OR_EQUALS;
        }
        if (s.startsWith("<")) {
            return MatchType.LESS_THAN;
        }
        if (s.startsWith(">=")) {
            return MatchType.GREATER_THAN_OR_EQUALS;
        }
        if (s.startsWith(">")) {
            return MatchType.GREATER_THAN;
        }
        if (s.startsWith("^")) {
            return s.endsWith("$") ? MatchType.EXACT : MatchType.STARTS_WITH;
        }
        return s.endsWith("$") ? MatchType.ENDS_WITH : MatchType.CONTAINS;
    }

    private static ByteString inferMatch(String s) {
        if (s.startsWith("<=") || s.startsWith(">=")) {
            return new ByteString(s.substring(2));
        }
        if (s.startsWith("<") || s.startsWith(">")) {
            return new ByteString(s.substring(1));
        }

        int from = s.startsWith("^") ? 1 : 0;
        from += s.startsWith("m:", from) ? 2 : 0;
        int to = s.length() - (s.endsWith("$") ? 1 : 0);
        return new ByteString(s.substring(from, to));
    }

    private static VendorkeyFilter createSimpleFilter(String s) {
        if (s.startsWith(TypeFilter.PREFIX)) {
            return new TypeFilter(Integer.parseInt(s.substring(TypeFilter.PREFIX.length())));
        }
        if (s.startsWith(PartitionFilter.PREFIX)) {
            return new PartitionFilter(s.substring(PartitionFilter.PREFIX.length()));
        }
        if (s.startsWith(MaturityFilter.PREFIX)) {
            return new MaturityFilter(s.substring(MaturityFilter.PREFIX.length()));
        }

        final ByteString match = inferMatch(s);
        final MatchType type = inferType(s);
        if (type == MatchType.EXACT && s.startsWith("^m:") && s.contains(",")) {
            return new MarketSetFilter(match.toString().split(","));
        }
        return (s.startsWith("m:") || s.startsWith("^m:"))
                ? new MarketFilter(match, type)
                : new SimpleFilter(match, type);
    }

    public static VendorkeyFilter andJoin(List<VendorkeyFilter> filters) {
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new AndFilter(filters.toArray(new VendorkeyFilter[filters.size()]));
    }

    public static VendorkeyFilter orJoin(List<VendorkeyFilter> filters) {
        if (filters.size() == 1) {
            return filters.get(0);
        }
        return new OrFilter(filters.toArray(new VendorkeyFilter[filters.size()]));
    }

    public static VendorkeyFilter create(File f) throws FileNotFoundException {
        final Set<ByteString> vwdcodes = new HashSet<>();
        try (Scanner sc = new Scanner(f)) {
            vwdcodes.add(new ByteString(sc.nextLine()));
        }
        return new VwdCodeSetFilter(vwdcodes);
    }

    public static VendorkeyFilter create(String s) {
        if (!StringUtils.hasText(s)) {
            return ACCEPT_NONE;
        }
        if ("*".equals(s)) {
            return ACCEPT_ALL;
        }
        if (s.startsWith("!(") && s.endsWith(")")) {
            return simplifyNotFilter(new NotFilter(create(s.substring(2, s.length() - 1))));
        }

        final String[] orTerms = s.split(Pattern.quote("||"));
        if (orTerms.length > 1) {
            return simplifyOrFilter(new OrFilter(parseTerms(orTerms)));
        }
        final String[] andTerms = s.split(Pattern.quote("&&"));
        if (andTerms.length > 1) {
            return simplifyAndFilter(new AndFilter(parseTerms(andTerms)));
        }

        if (s.indexOf('(') != -1 || s.indexOf(')') != -1) {
            throw new IllegalArgumentException("() not supported in filter definition");
        }

        final String term = s.trim();
        if (term.startsWith("!")) {
            return simplifyNotFilter(new NotFilter(create(term.substring(1))));
        }
        else {
            return createSimpleFilter(term);
        }
    }
    private static VendorkeyFilter simplifyNotFilter(NotFilter notFilter) {

        // Solve double-negations
        if (notFilter.filter instanceof NotFilter) {
            return ((NotFilter) notFilter.filter).filter;
        }

        if (notFilter.filter == ACCEPT_ALL) {
            return ACCEPT_NONE;
        }

        if (notFilter.filter == ACCEPT_NONE) {
            return ACCEPT_ALL;
        }

        return notFilter;
    }

    private static VendorkeyFilter simplifyAndFilter(AndFilter andFilter) {

        //noinspection SimplifyStreamApiCallChains
        if (Stream.of(andFilter.filters).anyMatch(ACCEPT_NONE::equals)) {
            return ACCEPT_NONE;
        }

        List<VendorkeyFilter> filters =
                Stream.of(andFilter.filters)
                        .filter(f -> !ACCEPT_ALL.equals(f))
                        .collect(Collectors.toList());

        if (filters.isEmpty()) {
            return ACCEPT_ALL;
        } else if (filters.size() == 1) {
            return filters.get(0);
        }

        return new AndFilter(filters.toArray(new VendorkeyFilter[0]));
    }

    private static VendorkeyFilter simplifyOrFilter(OrFilter orFilter) {

        //noinspection SimplifyStreamApiCallChains
        if (Stream.of(orFilter.filters).anyMatch(ACCEPT_ALL::equals)) {
            return ACCEPT_ALL;
        }

        List<VendorkeyFilter> filters =
                Stream.of(orFilter.filters)
                        .filter(f -> !ACCEPT_NONE.equals(f))
                        .collect(Collectors.toList());

        if (filters.isEmpty()) {
            return ACCEPT_NONE;
        } else if (filters.size() == 1) {
            return filters.get(0);
        }

        // Find all MarketFilter instances
        List<MarketFilter> marketFilters =
                filters
                        .stream()
                        .filter(f -> f instanceof MarketFilter)
                        .map(f -> (MarketFilter) f)
                        .collect(Collectors.toList());

        // If there are only MarketFilters replace the entire OrFilter with a MarketSetFilter
        if (marketFilters.size() == filters.size()) {
            return new MarketSetFilter(marketFilters);
        }
        else if (marketFilters.size() > 1) {
            // Replace all single MarketFilter instances with one MarketSetFilter instance
            MarketSetFilter msf = new MarketSetFilter(marketFilters);

            // Get list of all non-MarketFilter instances
            filters = filters
                        .stream()
                        .filter(f -> !(f instanceof MarketFilter))
                        .collect(Collectors.toList());

            filters.add(msf);

            // Since the filters variable is final we need to create a new object
        }
        return new OrFilter(filters.toArray(new VendorkeyFilter[0]));
    }

    private static VendorkeyFilter[] parseTerms(final String[] terms) {
        final VendorkeyFilter[] checks = new VendorkeyFilter[terms.length];
        for (int i = 0; i < terms.length; i++) {
            checks[i] = create(terms[i].trim());
        }
        return checks;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public VendorkeyFilter getObject() {
        return create(this.spec);
    }

    public Class getObjectType() {
        return VendorkeyFilter.class;
    }

    /**
     * @return false iff this filter contains at least one filter expression that depends on the
     * the actual time the filter is created
     */
    public boolean isSingleton() {
        return !this.spec.contains("mat:P");
    }

    private static class PartitionFilter implements VendorkeyFilter {
        static final String PREFIX = "p:";

        private int partition;

        private int partitions;

        public PartitionFilter(String s) {
            // 1_10 means first out of ten
            final String[] split = s.split("_");
            this.partition = Integer.parseInt(split[0]) - 1;
            this.partitions = Integer.parseInt(split[1]);
        }

        @Override
        public boolean test(Vendorkey vkey) {
            return (vkey.hashCode() * Integer.signum(vkey.hashCode())) % this.partitions
                    == this.partition;
        }
    }
}
