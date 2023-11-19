/*
 * SearchParameterParser.java
 *
 * Created on 26.10.2005 10:38:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.marketmaker.istar.ratios.opra.OpraItem;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.rating.Rating;
import de.marketmaker.istar.domain.rating.RatingSystem;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.rating.RatingSystemProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.backend.Constants;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SearchParameterParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchParameterParser.class);

    static final String NULL_VALUE = "NULL";

    private final static Pattern DATE_RANGE_PATTERN = Pattern.compile("([-+]?\\d+)[dwmy]");

    private static final DataRecordStrategy DEFAULT_DATA_RECORD_STRATEGY = new DefaultDataRecordStrategy();

    private static int SEARCH_ID;

    public static final String PREFIX_SORT = "sort";

    public static final String SUFFIX_DESC = ":D";

    public static final String SUFFIX_UPPER = ":U";

    public static final String SUFFIX_UPPER_INCLUSIVE = ":UI";

    public static final String SUFFIX_INOUT = ":IO";

    public static final String SUFFIX_LOWER = ":L";

    public static final String SUFFIX_LOWER_INCLUSIVE = ":LI";

    public static final String SUFFIX_RANGE = ":R";

    static final String SUFFIX_TEXT = ":T";  // TODO:  is this actually appended anywhere?

    static final String PARAM_ALL = "all";

    static final String PARAM_STARTAT = "i";

    static final String PARAM_NUMRESULTS = "n";

    static final String PARAM_GROUPBY = "group";

    static final String PARAM_GROUPSORTBY = "groupSortBy";

    static final String OR_SEPARATOR = "@";

    static final BitSet RATIO_FIELDS;

    static {
        RATIO_FIELDS = new BitSet();
        RATIO_FIELDS.or(RatioFieldDescription.getQuoteStaticFieldids());
        RATIO_FIELDS.or(RatioFieldDescription.getQuoteRatiosFieldids());
        RATIO_FIELDS.or(RatioFieldDescription.getInstrumentFieldids());
    }

    private List<Long> instrumentIds;

    private final NavigableMap<String, String> parameters = new TreeMap<>();

    private final Set<String> baseParameters = new HashSet<>();

    private final RatingSystemProvider ratingSystemProvider;

    private final SearchEngineVisitor visitor;

    private final String visitorClassname;

    private final boolean withParallelVisitor;

    private final Profile profile;

    private final List<Locale> locales;

    private final int searchId = nextSearchId();

    private int fieldidForResultCount;

    private String filterForResultCount;

    private final InstrumentTypeEnum type;

    private final InstrumentTypeEnum[] additionalTypes;

    private final DataRecordStrategy dataRecordStrategy;

    private List<Integer> metadataFieldids;

    private final Selector selector;

    private boolean instrumentRatioOnly = true;

    private boolean withDetailedSymbol = false;

    private Set<RatioFieldDescription.Field> deprecatedFields;

    private static synchronized int nextSearchId() {
        SEARCH_ID = (SEARCH_ID == Integer.MAX_VALUE) ? 1 : SEARCH_ID + 1;
        return SEARCH_ID;
    }

    public SearchParameterParser(Profile profile, SearchEngineVisitor visitor,
            InstrumentTypeEnum type, long[] iids) throws Exception {
        this.profile = profile;
        this.locales = null;
        this.visitor = visitor;
        this.visitorClassname = null;
        this.withParallelVisitor = false;

        this.type = type;
        this.additionalTypes = null;

        this.selector = ListSelector.create(true,
                new NonEmptyStringSelector(RatioFieldDescription.isin, this.locales),
                new NonEmptyStringSelector(RatioFieldDescription.name, this.locales),
                new NonEmptyStringSelector(RatioFieldDescription.currency, this.locales));
        setInstrumentRatioOnly(false);

        this.instrumentIds = (iids != null) ? new LongArrayList(iids) : null;
        this.ratingSystemProvider = null;
        this.dataRecordStrategy = DEFAULT_DATA_RECORD_STRATEGY;
    }

    public SearchParameterParser(RatioSearchRequest request,
            RatingSystemProvider ratingSystemProvider) throws Exception {
        this.profile = request.getProfile();
        this.locales = request.getLocales();
        this.type = request.getType();
        this.additionalTypes = request.getAdditionalTypes();
        this.metadataFieldids = request.getMetadataFieldids();
        this.withDetailedSymbol = request.isWithDetailedSymbol();

        final RatioFieldDescription.Field field =
                getFieldById(request.getFieldidForResultCount());

        if (field != null && field.isEnum()) {
            this.fieldidForResultCount = request.getFieldidForResultCount();
            this.filterForResultCount = request.getFilterForResultCount();
        }

        this.ratingSystemProvider = ratingSystemProvider;
        this.instrumentIds = request.getInstrumentIds();

        for (final Map.Entry<String, String> entry : request.getParameters().entrySet()) {
            final String key = entry.getKey();

            if (StringUtils.hasText(entry.getValue())) {
                this.parameters.put(key, entry.getValue().trim());
            }
            else {
                this.parameters.put(key, null);
            }

            if (!key.endsWith(SUFFIX_TEXT)) {
                this.baseParameters.add(getBaseParameterName(key));
            }
        }

        this.selector = createSelector(request);

        this.visitor = null;
        this.visitorClassname = request.getVisitorClassname();
        this.withParallelVisitor = MergeableSearchEngineVisitor.class
                .isAssignableFrom(Class.forName(this.visitorClassname));

        this.dataRecordStrategy = initDataRecordStrategy(request);

        if (this.deprecatedFields != null) {
            LOGGER.warn("Deprecated field(s) " + this.deprecatedFields
                    + " found in parameters=" + this.parameters);
        }
    }

    private RatioFieldDescription.Field getFieldById(int x) {
        return detectDeprecatedField(RatioFieldDescription.getFieldById(x));
    }

    private RatioFieldDescription.Field getFieldByName(String x) {
        return detectDeprecatedField(RatioFieldDescription.getFieldByName(x));
    }

    private RatioFieldDescription.Field detectDeprecatedField(RatioFieldDescription.Field f) {
        if (f != null && f.isDeprecated()) {
            if (this.deprecatedFields == null) {
                this.deprecatedFields = new HashSet<>();
            }
            this.deprecatedFields.add(f);
        }
        return f;
    }

    private String getBaseParameterName(String key) {
        final int p = key.indexOf(':');
        return (p >= 0) ? key.substring(0, p) : key;
    }

    public RatingSystemProvider getRatingSystemProvider() {
        return ratingSystemProvider;
    }

    public boolean isWithDetailedSymbol() {
        return withDetailedSymbol;
    }

    public void setWithDetailedSymbol(boolean withDetailedSymbol) {
        this.withDetailedSymbol = withDetailedSymbol;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getSimpleName()).append("[");
        sb.append("type=").append(this.type);
        sb.append(", params=").append(this.parameters);
        sb.append(", baseParams=").append(this.baseParameters);
        if (this.visitorClassname != null) {
            sb.append(", visitorClassname=").append(this.visitorClassname);
        }
        else if (this.visitor != null) {
            sb.append(", visitor=").append(this.visitor.getClass().getName());
        }
        return sb.append("]").toString();
    }

    int getSearchId() {
        return this.searchId;
    }

    public boolean isInstrumentRatioOnly() {
        return instrumentRatioOnly;
    }

    private void setInstrumentRatioOnly(boolean instrumentRatioOnly) {
        this.instrumentRatioOnly = instrumentRatioOnly;
    }

    private Selector createSelector(RatioSearchRequest request) {
        final Selector querySelector = getSelector(RATIO_FIELDS);

        if (InstrumentTypeEnum.FND == request.getType()) {
            final String selectedProvider = request.getSelectedProvider();

            if (("VWD".equals(selectedProvider) || (selectedProvider == null))
                    && (this.profile.isAllowed(de.marketmaker.istar.domain.profile.Selector.VWD_FUND_DATA)
                    || this.profile.isAllowed(de.marketmaker.istar.domain.profile.Selector.VWD_FUND_MAPBIT_BREAKDOWNS))) {

                return null == querySelector ? Selector.TRUE : querySelector;
            }
            else if (("MORNINGSTAR".equals(selectedProvider) || (selectedProvider == null))
                    && MarketAdmissionUtil.containsMorningstarSelector(this.profile)) {
                // limit the instrument selections only to the allowed ones according to market admissions
                // implied by the user's profile
                final Selector maSelector = getMarketAdmissionSelector(
                        MarketAdmissionUtil.getMarketAdmissionsMorningstar(this.profile), request.getLocales());

                if (null == querySelector) {
                    return maSelector;
                }
                else {
                    return ListSelector.create(true, querySelector, maSelector); // conjunction
                }
            }
            else if (("FIDA".equals(selectedProvider) || (selectedProvider == null))
                    && (this.profile.isAllowed(de.marketmaker.istar.domain.profile.Selector.FIDA_FUND_DATA)
                    ^ this.profile.isAllowed(de.marketmaker.istar.domain.profile.Selector.FIDA_FUND_RATING_I))) {
                // limit the instrument selections only to the allowed ones according to fidaPermissionType
                // implied by the user's profile
                final Selector selector = SimpleSelector.create(RatioFieldDescription.fidaPermissionType.id(), -1,
                        this.profile.isAllowed(de.marketmaker.istar.domain.profile.Selector.FIDA_FUND_DATA) ? "R" : "I", true);

                if (null == querySelector) {
                    return selector;
                }
                else {
                    return ListSelector.create(true, querySelector, selector); // conjunction
                }
            }
        }

        return null == querySelector ? Selector.TRUE : querySelector;
    }

    private Selector getMarketAdmissionSelector(List<String> mas, List<Locale> locales) {
        return createEnumSetSelector(RatioFieldDescription.msMarketAdmission.id(), mas);
    }

    private static DataRecordStrategy initDataRecordStrategy(
            RatioSearchRequest request) throws Exception {
        final String classname = request.getDataRecordStrategyClassname();
        if (StringUtils.hasText(classname)) {
            return (DataRecordStrategy) Class.forName(classname).newInstance();
        }
        return new DefaultDataRecordStrategy();
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public boolean isWithParallelVisitor() {
        return this.withParallelVisitor;
    }

    public GenericSearchEngineVisitor<?, ?> createVisitor() {
        if (this.visitor != null) {
            return this.visitor;
        }
        try {
            final GenericSearchEngineVisitor<?, ?> result = (GenericSearchEngineVisitor<?, ?>) Class.forName(this.visitorClassname).newInstance();
            result.init(this);
            return result;
        } catch (Exception e) {
            LOGGER.error("<createVisitor> failed for " + this, e);
            throw new RuntimeException(e);
        }
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    public InstrumentTypeEnum[] getAdditionalTypes() {
        return additionalTypes;
    }

    public List<Long> getInstrumentIds() {
        return this.instrumentIds;
    }

    public long getId() {
        final String id = this.parameters.get("id");
        if (StringUtils.hasText(id)) {
            return Long.parseLong(id);
        }

        return -1;
    }

    private Selector getSelector(BitSet validFieldids) {
        final List<Selector> selectors = new ArrayList<>();

        for (final String baseParameter : this.baseParameters) {
            if (baseParameter.startsWith(PREFIX_SORT)
                    || baseParameter.equalsIgnoreCase(PARAM_GROUPBY)
                    || baseParameter.equalsIgnoreCase(PARAM_GROUPSORTBY)) {
                continue;
            }

            if (baseParameter.contains(OR_SEPARATOR)) {
                final Selector orSelector = getOrSelector(baseParameter, validFieldids);
                if (orSelector != null) {
                    selectors.add(orSelector);
                }
                continue;
            }

            final RatioFieldDescription.Field field = getFieldByName(baseParameter);
            if (field == null || !validFieldids.get(field.id())) {
                continue;
            }

            final Selector s = getSelector(baseParameter, field.id());

            if (s != null) {
                selectors.add(s);
            }
        }

        // HACK to add NotNull-Selector for BestToolVisitor and still keep selector final
        final String sources = getParameterValue(BestToolVisitor.KEY_SOURCE);
        if (sources != null) {
            final String[] sources0 = sources.split(",");
            final RatioFieldDescription.Field field = getFieldByName(sources0[0]);
            if (field != null) {
                selectors.add(SimpleSelector.createNotNull(field.id(), getLocaleIndex(field)));
                this.instrumentRatioOnly &= field.isInstrumentField();
            }
        }

        return ListSelector.create(true, selectors);
    }

    private Selector getOrSelector(final String key, BitSet validFieldids) {
        final List<Selector> selectors = new ArrayList<>();
        for (String token : key.split(OR_SEPARATOR)) {
            final RatioFieldDescription.Field field = getFieldByName(token);
            if (field == null || !validFieldids.get(field.id())) {
                continue;
            }
            final Selector selector = getSelector(key, field.id());
            if (selector != null) {
                selectors.add(selector);
            }
        }
        return ListSelector.create(false, selectors);
    }

    private Selector getSelector(String key, int fieldid) {
        final RatioFieldDescription.Field field = getFieldById(fieldid);
        if (field == null) {
            return null;
        }
        if (!field.isApplicableFor(this.type)) {
            LOGGER.warn("<getSelector> not applicable for " + this.type + ": " + field);
            return null;
        }

        if (field.isQuoteRatioField() || field.isQuoteStaticField()) {
            setInstrumentRatioOnly(false);
        }

        if (hasNullValue(key)) {
            final Selector nullSelector = SimpleSelector.createNull(field.id(), getLocaleIndex(field));
            return isTestIn(key) ? nullSelector : new NotSelector(nullSelector);
        }

        switch (field.type()) {
            case NUMBER:
                return getLongSelector(fieldid, key, false);
            case DECIMAL:
                return getLongSelector(fieldid, key, true);
            case TIME:
                return getIntSelector(fieldid, key);
            case ENUMSET:
                return getEnumSetSelector(fieldid, key);
            case DATE:
                return getDateSelector(fieldid, key);
            case STRING:
                if (this.ratingSystemProvider != null) {
                    final String ratingSystemName = field.getRatingSystemName();
                    final RatingSystem ratingSystem = this.ratingSystemProvider.getRatingSystem(ratingSystemName);
                    if (ratingSystem != null) {
                        return getRatingSelector(fieldid, ratingSystem, key);
                    }
                }
                return getStringSelector(fieldid, getLocaleIndex(field), key);
            case BOOLEAN:
                return getBooleanSelector(fieldid, key);
            case TIMESTAMP:
                return getTimestampSelector(fieldid, key);
            default:
                LOGGER.warn("<getSelector> cannot handle '" + field
                        + "', ignoring search term for this field");
        }

        return null;
    }

    private boolean hasNullValue(String key) {
        if (this.parameters.get(key) != null) {
            return false;
        }
        // create map with all keys that start with "key:"
        final NavigableMap<String, String> submap = this.parameters.subMap(key + ":", true, key + ";", false);
        // :INOUT = false is used for "is not null", all other keys indicate restriction fo non-null values
        return submap.isEmpty() || (submap.size() == 1 && submap.containsKey(key + SUFFIX_INOUT));
    }

    private int getLocaleIndex(RatioFieldDescription.Field field) {
        return RatioFieldDescription.getLocaleIndex(getFieldById(field.id()), this.locales);
    }

    private Selector getBooleanSelector(int fieldid, String key) {
        final String value = getParameterValue(key);
        if (PARAM_ALL.equals(value)) {
            return null;
        }
        return SimpleSelector.create(fieldid, Boolean.valueOf(value));
    }

    private Selector getTimestampSelector(int fieldid, String key) {
        // TODO: range search is not supported here
        if (this.parameters.containsKey(key + SUFFIX_LOWER)
                || this.parameters.containsKey(key + SUFFIX_UPPER)) {
            return getFromUntilTimestampSelector(fieldid, key);
        }

        if (NULL_VALUE.equals(getParameterValue(key))) {
            final RatioFieldDescription.Field field = getFieldById(fieldid);
            if (field == null) {
                return null;
            }
            return field.isNullAsMin()
                    ? SimpleSelector.create(fieldid, Long.MIN_VALUE, true, Long.MIN_VALUE, true, true)
                    : SimpleSelector.create(fieldid, Long.MAX_VALUE, true, Long.MAX_VALUE, true, true);
        }

        final long date = getParameterValueTimestamp(key, Long.MIN_VALUE);

        if (date == Long.MIN_VALUE) {
            return null;
        }

        return SimpleSelector.create(fieldid, date, true, date, true, isTestIn(key));
    }

    private boolean isTestIn(String key) {  // true means "in" false means "out"
        return getParameterValue(key + SUFFIX_INOUT, true);
    }

    private Selector getDateSelector(int fieldid, String key) {
        if (this.parameters.containsKey(key + SUFFIX_RANGE)) {
            return getRangeDateSelector(fieldid, key);
        }
        if (this.parameters.containsKey(key + SUFFIX_LOWER)
                || this.parameters.containsKey(key + SUFFIX_UPPER)) {
            return getFromUntilDateSelector(fieldid, key);
        }

        if (NULL_VALUE.equals(getParameterValue(key))) {
            final RatioFieldDescription.Field field = getFieldById(fieldid);
            if (field == null) {
                return null;
            }
            return field.isNullAsMin()
                    ? SimpleSelector.create(fieldid, Integer.MIN_VALUE, true, Integer.MIN_VALUE, true, true)
                    : SimpleSelector.create(fieldid, Integer.MAX_VALUE, true, Integer.MAX_VALUE, true, true);
        }

        final int date = getParameterValueDate(key, Integer.MIN_VALUE);

        if (date == Integer.MIN_VALUE) {
            return null;
        }

        return SimpleSelector.create(fieldid, date, true, date, true, isTestIn(key));
    }

    private Selector getFromUntilTimestampSelector(int fieldid, String key) {
        final long lower = getParameterValueTimestamp(key + SUFFIX_LOWER, Long.MIN_VALUE);
        final long upper = getParameterValueTimestamp(key + SUFFIX_UPPER, Long.MAX_VALUE);

        if (lower == Long.MIN_VALUE && upper == Long.MAX_VALUE) {
            return null;
        }

        final boolean testIn = isTestIn(key);
        final boolean lowerInclusive = getParameterValue(key + SUFFIX_LOWER_INCLUSIVE, true);
        final boolean upperInclusive = getParameterValue(key + SUFFIX_UPPER_INCLUSIVE, true);

        return SimpleSelector.create(fieldid, lower, lowerInclusive, upper, upperInclusive, testIn);
    }

    private Selector getFromUntilDateSelector(int fieldid, String key) {
        final int lower = getParameterValueDate(key + SUFFIX_LOWER, Integer.MIN_VALUE);
        final int upper = getParameterValueDate(key + SUFFIX_UPPER, Integer.MAX_VALUE);

        if (lower == Integer.MIN_VALUE && upper == Integer.MAX_VALUE) {
            return null;
        }

        final boolean testIn = isTestIn(key);
        final boolean lowerInclusive = getParameterValue(key + SUFFIX_LOWER_INCLUSIVE, true);
        final boolean upperInclusive = getParameterValue(key + SUFFIX_UPPER_INCLUSIVE, true);

        return SimpleSelector.create(fieldid, lower, lowerInclusive, upper, upperInclusive, testIn);
    }

    private Selector getRangeDateSelector(int fieldid, String key) {
        final String value = getParameterValue(key + SUFFIX_RANGE);
        if (PARAM_ALL.equals(value)) {
            return null;
        }
        final String[] tokens = value.split("_");
        if (tokens.length != 2) {
            this.parameters.remove(key);
            return null;
        }

        final int start = getDateRangeParameter(tokens[0], Integer.MIN_VALUE);
        final int end = getDateRangeParameter(tokens[1], Integer.MAX_VALUE);
        if ((start == Integer.MIN_VALUE && end == Integer.MAX_VALUE) || start > end) {
            this.parameters.remove(key);
            return null;
        }

        final boolean testIn = isTestIn(key);
        final boolean lowerInclusive = getParameterValue(key + SUFFIX_LOWER_INCLUSIVE, true);
        final boolean upperInclusive = getParameterValue(key + SUFFIX_UPPER_INCLUSIVE, true);

        return SimpleSelector.create(fieldid, start, lowerInclusive, end, upperInclusive, testIn);
    }

    static int getDateRangeParameter(String token, int defaultValue) {
        if ("x".equals(token) || !StringUtils.hasText(token)) {
            return defaultValue;
        }

        final Matcher m = DATE_RANGE_PATTERN.matcher(token.trim());
        if (!m.find()) {
            return defaultValue;
        }

        final int value = Integer.parseInt(m.group(1));
        LocalDate ld = new LocalDate();
        if (token.endsWith("d")) {
            ld = ld.plusDays(value);
        }
        else if (token.endsWith("w")) {
            ld = ld.plusDays(value * DateTimeConstants.DAYS_PER_WEEK);
        }
        else if (token.endsWith("m")) {
            ld = ld.plusMonths(value);
        }
        else if (token.endsWith("y")) {
            ld = ld.plusYears(value);
        }
        return DateUtil.toYyyyMmDd(ld);
    }

    private Selector getLongSelector(int fieldid, String key, boolean isPrice) {
        final RatioFieldDescription.Field field = getFieldById(fieldid);
        if (field == null) {
            return null;
        }

        final boolean testIn = isTestIn(key);

        if (this.parameters.containsKey(key)) {
            final String value = this.parameters.get(key);
            if (value.contains(OR_SEPARATOR)) {
                final String[] strings = value.split(OR_SEPARATOR);

                final List<Long> values = new ArrayList<>();
                for (final String s : strings) {
                    final long l = getLongValue(s, Long.MIN_VALUE, key);
                    if (l != Long.MIN_VALUE) {
                        values.add(field.isPercent() ? l / 100 : l);
                    }
                }

                return values.isEmpty() ? null : SimpleSelector.create(fieldid, values, testIn);
            }
        }

        long lower;
        long upper;
        if (this.parameters.containsKey(key + SUFFIX_RANGE)) {
            final String value = getParameterValue(key + SUFFIX_RANGE);

            final int index = value.indexOf("_");
            if (index < 0) {
                LOGGER.warn("<getLongSelector> " + SUFFIX_RANGE + " without _ in request: field=" + field + " key=" + key + " value=" + value);
                return null;
            }
            final String ls = value.substring(0, index);
            final String us = value.substring(index + 1, value.length());

            lower = isPrice
                    ? getDecimalValue(ls, Long.MIN_VALUE, key + SUFFIX_RANGE)
                    : getLongValue(ls, Long.MIN_VALUE, key + SUFFIX_RANGE);

            upper = isPrice
                    ? getDecimalValue(us, Long.MAX_VALUE, key + SUFFIX_RANGE)
                    : getLongValue(us, Long.MAX_VALUE, key + SUFFIX_RANGE);
        }
        else if (this.parameters.containsKey(key + SUFFIX_LOWER)
                || this.parameters.containsKey(key + SUFFIX_UPPER)) {
            lower = getLong(isPrice, key + SUFFIX_LOWER, Long.MIN_VALUE);
            upper = getLong(isPrice, key + SUFFIX_UPPER, Long.MAX_VALUE);
        }
        else {
            lower = getLong(isPrice, key, Long.MIN_VALUE);
            upper = lower;
        }

        if (lower == Long.MIN_VALUE && upper == Long.MAX_VALUE) {
            return null;
        }
        final boolean lowerInclusive = getParameterValue(key + SUFFIX_LOWER_INCLUSIVE, true);
        final boolean upperInclusive = getParameterValue(key + SUFFIX_UPPER_INCLUSIVE, true);

        return createLongSelector(field, lower, lowerInclusive, upper, upperInclusive, testIn);
    }

    private Selector createLongSelector(RatioFieldDescription.Field field, long lower,
            boolean lowerInclusive, long upper, boolean upperInclusive, boolean testIn) {
        if (field.isPercent()) {
            return SimpleSelector.create(field.id(), lower / 100, lowerInclusive, upper / 100, upperInclusive, testIn);
        }
        return SimpleSelector.create(field.id(), lower, lowerInclusive, upper, upperInclusive, testIn);
    }

    private long getLong(boolean isPrice, String key, long defaultValue) {
        return isPrice ?
                getParameterDecimal(key, defaultValue) :
                getParameterLong(key, defaultValue);
    }

    private Selector getEnumSetSelector(int fieldid, String key) {
        final String value = getParameterValue(key);
        return value.contains(OR_SEPARATOR)
                ? createEnumSetSelector(fieldid, Arrays.asList(value.split(OR_SEPARATOR)))
                : createEnumSetSelector(fieldid, value);
    }

    private Selector createEnumSetSelector(int fieldid, List<String> mas) {
        ArrayList<Selector> selectors = new ArrayList<>();
        for (String ma : mas) {
            selectors.add(createEnumSetSelector(fieldid, ma));
        }
        return ListSelector.create(false, selectors);
    }

    private Selector createEnumSetSelector(int fieldid, String ma) {
        return ma.contains(RatioEnumSetFactory.SEPARATOR)
                ? SimpleSelector.createEnumSetSelector(fieldid,
                RatioEnumSetFactory.toBits(fieldid, ma), true)  // support and
                : SimpleSelector.createEnumSetSelector(fieldid,
                RatioEnumSetFactory.toBits(fieldid, ma), false);
    }

    private Selector getIntSelector(int fieldid, String key) {
        int lower = getParameterInt(key + SUFFIX_LOWER, Integer.MIN_VALUE);
        int upper = getParameterInt(key + SUFFIX_UPPER, Integer.MAX_VALUE);

        if (lower == Integer.MIN_VALUE && upper == Integer.MAX_VALUE) {
            final int exact = getParameterInt(key, Integer.MAX_VALUE);
            if (exact == Integer.MAX_VALUE) {
                return null;
            }
            lower = exact;
            upper = exact;
        }

        final boolean testIn = isTestIn(key);
        final boolean lowerInclusive = getParameterValue(key + SUFFIX_LOWER_INCLUSIVE, true);
        final boolean upperInclusive = getParameterValue(key + SUFFIX_UPPER_INCLUSIVE, true);

        return SimpleSelector.create(fieldid, lower, lowerInclusive, upper, upperInclusive, testIn);
    }

    private Selector getRatingSelector(int fieldid, RatingSystem ratingSystem, String key) {
        Rating lower = getParameterValue(ratingSystem, key + SUFFIX_LOWER);
        Rating upper = getParameterValue(ratingSystem, key + SUFFIX_UPPER);

        final boolean testIn = isTestIn(key);
        final boolean lowerInclusive = getParameterValue(key + SUFFIX_LOWER_INCLUSIVE, true);
        final boolean upperInclusive = getParameterValue(key + SUFFIX_UPPER_INCLUSIVE, true);

        if (lower == null && upper == null) {
            final String exact = getParameterValue(key);
            if (exact == null) {
                return null;
            }
            return SimpleSelector.createRatingSelectorExact(fieldid,
                    ratingSystem.getRating(exact), isWithDetailedSymbol(), ratingSystem, !testIn);
        }
        else if (null == lower) {
            final Selector worseThan = SimpleSelector.createWorseThan(fieldid, ratingSystem,
                    this.withDetailedSymbol, upper, upperInclusive);
            return testIn ? worseThan : SimpleSelector.negate(worseThan);
        }
        else if (null == upper) {
            final Selector betterThan = SimpleSelector.createBetterThan(fieldid, ratingSystem,
                    this.withDetailedSymbol, lower, lowerInclusive);
            return testIn ? betterThan : SimpleSelector.negate(betterThan);
        }
        else {
            final Selector worseThan = SimpleSelector.createWorseThan(fieldid, ratingSystem,
                    this.withDetailedSymbol, upper, upperInclusive);
            final Selector betterThan = SimpleSelector.createBetterThan(fieldid, ratingSystem,
                    this.withDetailedSymbol, lower, lowerInclusive);
            final Selector ret = ListSelector.create(true, betterThan, worseThan);
            return testIn ? ret : SimpleSelector.negate(ret);
        }
    }

    private Rating getParameterValue(RatingSystem ratingSystem, String key) {
        final String value = getParameterValue(key);
        if (value == null) {
            return null;
        }
        final Rating rating = ratingSystem.getRating(value);
        if (rating != null) {
            return rating;
        }

        this.parameters.remove(key);
        return null;
    }

    private Selector getStringSelector(int fieldid, int localeIndex, String key) {
        final String value = getParameterValue(key);
        if (PARAM_ALL.equals(value) || !StringUtils.hasText(value)) {
            this.parameters.remove(key);
            return null;
        }

        final boolean testIn = isTestIn(key);

        try {
            if (!value.contains(OR_SEPARATOR)) {
                return SimpleSelector.create(fieldid, localeIndex, value, testIn);
            }

            final String[] strings = value.split(OR_SEPARATOR);

            if (testIn) {
                if (SimpleSelector.isIsinField(fieldid) && SimpleSelector.isIsinForAll(strings)) {
                    return SimpleSelector.any(fieldid, localeIndex, strings);
                }
                if (SimpleSelector.isVwdcodeField(fieldid) && SimpleSelector.isFondsVwdcodeForAll(strings)) {
                    return SimpleSelector.any(fieldid, localeIndex, strings);
                }
            }

            final List<Selector> selectors = new ArrayList<>();
            for (final String s : strings) {
                selectors.add(SimpleSelector.create(fieldid, localeIndex, s, testIn));
            }

            return ListSelector.create(!testIn, selectors);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("<getStringSelector> failed: " + e.getMessage());
            this.parameters.remove(key);
            return null;
        }
    }

    public String getParameterValue(String key) {
        return this.parameters.get(key);
    }

    private long getParameterLong(String key, long defaultValue) {
        final String value = getParameterValue(key);
        return getLongValue(value, defaultValue, key);
    }

    private long getLongValue(String value, long defaultValue, String key) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            this.parameters.remove(key);
            return defaultValue;
        }
    }

    public int getParameterInt(String key, int defaultValue) {
        final String value = getParameterValue(key);
        return getIntValue(value, defaultValue, key);
    }

    private int getIntValue(String value, int defaultValue, String key) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            this.parameters.remove(key);
            return defaultValue;
        }
    }

    private long getParameterDecimal(String key, long defaultValue) {
        String value = getParameterValue(key);
        return getDecimalValue(value, defaultValue, key);
    }

    private long getDecimalValue(String value, long defaultValue, String key) {
        if (value == null) {
            return defaultValue;
        }

        if (value.indexOf(',') != -1) {
            return getDecimalValue(value.replace(',', '.'), defaultValue, key);
        }

        try {
            return (long) (Double.parseDouble(value) * Constants.SCALE_FOR_DECIMAL);
        } catch (NumberFormatException e) {
            this.parameters.remove(key);
            return defaultValue;
        }
    }

    private int getParameterValueDate(String key, int defaultValue) {
        final String value = getParameterValue(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return DateUtil.toYyyyMmDd(DateUtil.parseDate(value));
        } catch (Exception e) {
            this.parameters.remove(key);
            return defaultValue;
        }
    }

    private long getParameterValueTimestamp(String key, long defaultValue) {
        final String value = getParameterValue(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            // TODO: this only parses date values, no time parts supported
            DateTime dateTime = DateUtil.parseDate(value);
            return DateUtil.toYyyyMmDd(dateTime) * 100000L + dateTime.getSecondOfDay();
        } catch (Exception e) {
            this.parameters.remove(key);
            return defaultValue;
        }
    }

    private boolean getParameterValue(String key, boolean defaultValue) {
        final String s = getParameterValue(key);
        return (s != null) ? Boolean.valueOf(s) : defaultValue;
    }

    public <T> Comparator<T> getGroupByComparator(BiFunction<RatioFieldDescription.Field, Boolean, Comparator<T>> comparatorFunction) {
        String sortField = getParameterValue(PARAM_GROUPBY);
        if (StringUtils.hasText(sortField)) {
            final RatioFieldDescription.Field field = getFieldByName(sortField);
            if (field != null) {
                if (!field.isApplicableFor(this.type)) {
                    LOGGER.warn("<getGroupByComparator> not applicable for " + this.type + ": " + field);
                    return null;
                }
                return comparatorFunction.apply(field, false);
            }
        }
        return null;
    }

    private <T> SimpleComparatorChain<T> createComparatorChain(BiFunction<RatioFieldDescription.Field, Boolean, Comparator<T>> comparatorFunction) {
        SimpleComparatorChain<T> scc = new SimpleComparatorChain<>();

        // if group by is requested, make this the first sort criterion
        final Comparator<T> c = getGroupByComparator(comparatorFunction);
        if (c != null) {
            scc.add(c);
        }

        int i = 1;
        String sortField = getParameterValue(PREFIX_SORT + i);
        while (StringUtils.hasText(sortField)) {
            final RatioFieldDescription.Field field = getFieldByName(sortField);
            if (field == null) {
                break;
            }
            if (!field.isApplicableFor(this.type)) {
                LOGGER.warn("<getComparator> not applicable for " + this.type + ": " + field);
                break;
            }

            final boolean isReverse = getParameterValue(PREFIX_SORT + i + SUFFIX_DESC, false);
            scc.add(comparatorFunction.apply(field, isReverse));
            sortField = getParameterValue(PREFIX_SORT + ++i);
        }

        return scc;
    }

    public Comparator<QuoteRatios> getComparator() {
        return createComparatorChain((RatioFieldDescription.Field f, Boolean reverse) -> {
            return RatioDataComparator.create(f.id(), this.locales, reverse, this.ratingSystemProvider);
        });
    }

    public int getStartAt() {
        return (int) getParameterLong(PARAM_STARTAT, 0L);
    }

    public int getNumResults() {
        return (int) getParameterLong(PARAM_NUMRESULTS, 100L);
    }

    public Map getParameters() {
        return this.parameters;
    }

    public String getGroupBy() {
        return getParameterValue(PARAM_GROUPBY);
    }

    public boolean isGroupBy() {
        return StringUtils.hasText(getParameterValue(PARAM_GROUPBY));
    }

    public boolean isGroupSortByValue() {
        return "value".equals(getParameterValue(PARAM_GROUPSORTBY));
    }

    public int getFieldidForResultCount() {
        return fieldidForResultCount;
    }

    public String getFilterForResultCount() {
        return filterForResultCount;
    }

    public DataRecordStrategy getDataRecordStrategy() {
        return dataRecordStrategy;
    }

    public List<Integer> getMetadataFieldids() {
        return metadataFieldids;
    }

    boolean isWithMetadataCounting() {
        return this.metadataFieldids != null;
    }

    public static void main(String[] args) throws Exception {
        EnumFlyweightFactory.intern(413, "CERT_REVERSE_CONVERTIBLE");
        EnumFlyweightFactory.intern(413, "CERT_REVERSE_UNCONVERTIBLE");

        final RatioSearchRequest request = new RatioSearchRequest(ProfileFactory.valueOf(true));
        request.setType(InstrumentTypeEnum.CER);
        request.addParameter("i", "0");
        request.addParameter("n", "5");
        request.addParameter("performance1m:L", "80");
        request.addParameter("performance1m:U", "140");
//        request.addParameter("issueVolume:L", "140");
        request.addParameter("sort1", "performance3m");
        request.addParameter("sort1:D", "true");
        request.addParameter("gatrixxtypeftreff", "CERT_REVERSE_CONVERTIBLE");
        request.setDataRecordStrategyClass(DefaultDataRecordStrategy.class);
        final SearchParameterParser spp = new SearchParameterParser(request, null);
        System.out.println(spp.getSelector());
    }

    public Selector getSelector() {
        return this.selector;
    }
}
