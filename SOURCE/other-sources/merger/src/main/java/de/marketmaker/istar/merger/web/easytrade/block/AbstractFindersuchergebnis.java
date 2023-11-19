/*
 * AbstractFindersuchergebnis.java
 *
 * Created on 20.08.2007 16:30:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.ServletRequestDataBinder;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.funddata.FidaProfiler;
import de.marketmaker.istar.merger.web.QueryCommand;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;
import de.marketmaker.istar.ratios.frontend.DataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.InstrumentRatiosDerivative;
import de.marketmaker.istar.ratios.frontend.MetaDataMapKey;
import de.marketmaker.istar.ratios.frontend.RatioDataRecordImpl;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

import static de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum.IID;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.PREFIX_SORT;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.SUFFIX_DESC;
import static java.util.stream.Collectors.toList;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractFindersuchergebnis extends EasytradeCommandController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    static final Logger slogger = LoggerFactory.getLogger(AbstractFindersuchergebnis.class);

    protected boolean isWithPrices(Command cmd, boolean def) {
        return cmd.isWithPrices() != null
                ? cmd.isWithPrices()
                : def;
    }

    public static class Command extends ListCommand implements QueryCommand {

        private static final boolean DEFAULT_WITH_METADATA_COUNT = true;

        private static final boolean DEFAULT_WITH_SIBLINGS = true;

        private String query;

        private Long[] iid;

        private DataRecordStrategy.Type dataRecordStrategy;

        private Boolean withPrices;

        private InstrumentTypeEnum[] additionalType;

        private RatioFieldDescription.Field fieldForResultCount;

        private boolean withSiblingsForUnderlying = DEFAULT_WITH_SIBLINGS;

        private boolean withMetadataCount = DEFAULT_WITH_METADATA_COUNT;

        /**
         * {@inheritDoc}
         * <p>
         * Valid fields can be obtained from the available sort fields contained in the response.
         * </p>
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return an array of instrument ids.
         */
        public Long[] getIid() {
            return iid;
        }

        public void setIid(Long[] iid) {
            this.iid = iid;
        }

        /**
         * @return preferred data record type.
         */
        public DataRecordStrategy.Type getDataRecordStrategy() {
            return dataRecordStrategy;
        }

        public void setDataRecordStrategy(DataRecordStrategy.Type dataRecordStrategy) {
            this.dataRecordStrategy = dataRecordStrategy;
        }

        /**
         * @return if set to true intra-day prices of those instrument found will be returned as well.
         */
        public Boolean isWithPrices() {
            return withPrices;
        }

        public void setWithPrices(Boolean withPrices) {
            this.withPrices = withPrices;
        }

        /**
         * @return if true, the result includes data on how many times each enum field matched
         * the query; default is <tt>{@value #DEFAULT_WITH_METADATA_COUNT}</tt>. A query without
         * metadata count will execute faster, so clients should disable the count if they don't
         * need it.
         */
        public boolean isWithMetadataCount() {
            return withMetadataCount;
        }

        public void setWithMetadataCount(boolean withMetadataCount) {
            this.withMetadataCount = withMetadataCount;
        }

        /**
         * @return additional instrument types which should also be searched.
         */
        public InstrumentTypeEnum[] getAdditionalType() {
            return additionalType;
        }

        public void setAdditionalType(InstrumentTypeEnum[] additionalType) {
            this.additionalType = additionalType;
        }

        /**
         * @return a ratio field whose values are counted.
         */
        public RatioFieldDescription.Field getFieldForResultCount() {
            return fieldForResultCount;
        }

        public void setFieldForResultCount(RatioFieldDescription.Field fieldForResultCount) {
            this.fieldForResultCount = fieldForResultCount;
        }

        /**
         * @return if set to true, instruments having the same underlying will also be returned;
         * default is <tt>{@value #DEFAULT_WITH_SIBLINGS}</tt>.
         */
        public boolean isWithSiblingsForUnderlying() {
            return withSiblingsForUnderlying;
        }

        public void setWithSiblingsForUnderlying(boolean withSiblingsForUnderlying) {
            this.withSiblingsForUnderlying = withSiblingsForUnderlying;
        }
    }

    public static class ProviderCommand
            extends AbstractFindersuchergebnis.Command
            implements ProviderSelectionCommand {

        private String providerPreference;

        @RestrictedSet("VWD,SMF,SEDEX")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    @Override
    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);

        binder.registerCustomEditor(DataRecordStrategy.Type.class,
                new EnumEditor<>(DataRecordStrategy.Type.class));
        binder.registerCustomEditor(InstrumentTypeEnum.class,
                new EnumEditor<>(InstrumentTypeEnum.class));
        binder.registerCustomEditor(RatioFieldDescription.Field.class,
                new RatioFieldDescription.FieldEditor());
    }

    protected void replaceUnderlyingSymbol(Map<String, String> parameters, boolean withSiblings) {
        final int numParams = parameters.size();

        final UnderlyingResolver resolver = createUnderlyingResolver();
        addUnderlyingSymbols(resolver, parameters, RatioDataRecord.Field.underlyingVwdcode, SymbolStrategyEnum.VWDCODE);
        addUnderlyingSymbols(resolver, parameters, RatioDataRecord.Field.underlyingQid, SymbolStrategyEnum.QID);
        addUnderlyingSymbols(resolver, parameters, RatioDataRecord.Field.underlyingIid, IID);

        final Set<Long> underlyingIids = resolver.getUnderlyingIids(withSiblings);
        if (!underlyingIids.isEmpty()) {
            // TODO: check how to handle RatioFieldDescription.underlyingProductIid
            parameters.put(RatioFieldDescription.underlyingIid.name(),
                    StringUtils.collectionToDelimitedString(underlyingIids, "@"));
        }
        else if (parameters.size() < numParams) {
            // at least one underlying field was found and removed, but no corresponding iid was found
            parameters.put(RatioFieldDescription.underlyingIid.name(), "0");
        }
    }

    private UnderlyingResolver createUnderlyingResolver() {
        return new UnderlyingResolver(this.instrumentProvider, this.underlyingShadowProvider);
    }

    private void addUnderlyingSymbols(UnderlyingResolver resolver, Map<String, String> parameters,
                                      RatioDataRecord.Field field, SymbolStrategyEnum strategyEnum) {
        final String key = parameters.containsKey(field.name())
                ? field.name() : field.name().toLowerCase();
        final String symbols = parameters.remove(key);
        if (symbols == null) {
            return;
        }
        for (String symbol : symbols.split("@")) {
            resolver.addSymbol(symbol, strategyEnum);
        }
    }

    protected Set<Long> getUnderlyingIids(Instrument instrument, boolean withSiblings) {
        return createUnderlyingResolver().getUnderlyingIids(instrument, withSiblings);
    }

    public static RatioFieldDescription.Field getField(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields, String fieldname) {
        if (fieldname == null) {
            return null;
        }

        try {
            return fields.get(RatioDataRecord.Field.valueOf(fieldname));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("unknown field: " + fieldname);
        }
    }

    /**
     * @param providerPreference id for the preferred provider, depends on type of instrument
     *                           e.g. for CER/WNT in {SMF, SEDEX}
     *                                for FND in {FWW,VWDIT,MORNINGSTAR,SSAT,FIDA}
     */
    public static Map<RatioDataRecord.Field, RatioFieldDescription.Field> getFields(
            InstrumentTypeEnum type, String providerPreference, PermissionType... permissions) {
        if (type == InstrumentTypeEnum.CER || type == InstrumentTypeEnum.WNT) {
            if ("SMF".equalsIgnoreCase(providerPreference)) {
                final Profile profile = RequestContextHolder.getRequestContext().getProfile();
                final Map<RatioDataRecord.Field, RatioFieldDescription.Field> result
                        = new HashMap<>();

                addIfAllowed(profile, Selector.SMF_STATIC_DATA, type.name() + "-STATIC-SMF", result);
                addIfAllowed(profile, Selector.SMF_RATIO_DATA, type.name() + "-RATIO-SMF", result);

                if (!result.isEmpty()) {
                    if (isEdgAllowed(profile)) {
                        result.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get("EDG"));
                    }
                    return result;
                }
            }
            else if ("SEDEX".equalsIgnoreCase(providerPreference)) {
                final Map<RatioDataRecord.Field, RatioFieldDescription.Field> result
                        = getFields(type, permissions);
                result.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(type.name() + "-STATIC-SEDEX"));
                return result;
            }
        }

        return getFields(providerPreference, type, permissions);
    }

    private static void addIfAllowed(Profile p, Selector s, String key,
                                     Map<RatioDataRecord.Field, RatioFieldDescription.Field> result) {
        if (p.isAllowed(s)) {
            result.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(key));
        }
    }

    private static void addFiltered(String key, Map<RatioDataRecord.Field, RatioFieldDescription.Field> result,
                                    Predicate<? super Map.Entry<RatioDataRecord.Field, RatioFieldDescription.Field>> filter) {
        result.putAll(
                RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(key).entrySet().stream()
                        .filter(filter)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    public static Map<RatioDataRecord.Field, RatioFieldDescription.Field> getFields(
            InstrumentTypeEnum type, PermissionType... permissions) {
        return getFields(null, type, permissions);
    }

    /**
     * @param providerPreference might be null if we have no preference, also puts the fields into the request context
     */
    public static Map<RatioDataRecord.Field, RatioFieldDescription.Field> getFields(
            String providerPreference, InstrumentTypeEnum type, PermissionType... permissions) {
        final String key = "__fields" + type.name();

        final Object cached = RequestContextHolder.getRequestContext().get(key);
        if (cached != null) {
            //noinspection unchecked
            return (Map<RatioDataRecord.Field, RatioFieldDescription.Field>) cached;
        }

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = computeFields(providerPreference, type, permissions);

        RequestContextHolder.getRequestContext().put(key, fields);

        return fields;
    }

    private static HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> computeFields(
            String providerPreference, InstrumentTypeEnum type, PermissionType[] permissions) {
        final HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = new HashMap<>(RatioFieldDescription.FIELDNAMES.get(type));
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (permissions != null) {
            for (final PermissionType permission : permissions) {
                for (final String s : profile.getPermission(permission)) {
                    final Map<RatioDataRecord.Field, RatioFieldDescription.Field> map
                            = RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(s);
                    if (map != null) {
                        fields.putAll(map);
                    }
                }
            }
        }

        if (type == InstrumentTypeEnum.FND) {
            // R-77208,ISTAR-716:
            collectStaticFndFields(providerPreference, profile, fields);
            collectRatingFields(profile, fields);
            addIfAllowed(profile, Selector.DZ_BANK_USER, "FND_DZ", fields);
        }
        if (type == InstrumentTypeEnum.STK) {
            addIfAllowed(profile, Selector.FACTSET, "MMXML", fields);
            addIfAllowed(profile, Selector.THOMSONREUTERS_ESTIMATES_DZBANK, "TR-ESTIMATES", fields);
        }
        if (type == InstrumentTypeEnum.CER || type == InstrumentTypeEnum.WNT) {
            if (isEdgAllowed(profile)) {
                fields.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get("EDG"));
            }
        }
        if (type == InstrumentTypeEnum.CER) {
            addIfAllowed(profile, Selector.WGZ_BANK_USER, "CER_WGZ", fields);
            addIfAllowed(profile, Selector.DZ_BANK_USER, "CER_DZ", fields);
        }
        if (type == InstrumentTypeEnum.BND) {
            addIfAllowed(profile, Selector.RATING_FITCH, "RATING_FITCH", fields);
            addIfAllowed(profile, Selector.RATING_MOODYS, "RATING_MOODYS", fields);
            addIfAllowed(profile, Selector.RATING_SuP, "RATING_SNP", fields);
        }

        if (type == InstrumentTypeEnum.FUT && RetrieveUnderlyingsMethod.isWith1DTBQuotes()) {
            fields.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(RatioFieldDescription.UNDERLYING_PRODUCT_IID));
        }

        if ((type == InstrumentTypeEnum.FUT || type == InstrumentTypeEnum.OPT) && RetrieveUnderlyingsMethod.isWith1DTBQuotes()) {
            // TODO: needs refactoring to really use the current zone -> put zone into RequestContext?
            fields.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(RatioFieldDescription.VWD_RIMPAR_FIELDS));
        }

        if (profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)) {
            final String fieldsKey = type.name() + RatioFieldDescription.ANY_VWD_TP;
            if (RatioFieldDescription.FIELDNAMES_BY_PERMISSION.containsKey(fieldsKey)) {
                fields.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(fieldsKey));
            }
        }

        if (profile.isAllowed(Selector.MARKETMANAGER_PROFILE)) {
            fields.put(RatioDataRecord.Field.name, RatioFieldDescription.marketmanagerName);
        }

        return fields;
    }

    /**
     * see: ISTAR-716, R-77208 collects the static fields from a single provider
     */
    private static void collectStaticFndFields(String providerPreference, Profile profile,
                                               HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> result) {
        if (hasProviderAndPermission(profile, providerPreference)) {
            collectFields(Optional.of(providerPreference), result);
        } else {
            collectFields(findFirstAllowedProvider(profile), result);
        }
    }

    private static void collectFields(Optional<String> key,
                                      HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> result) {
        if (key.isPresent() // key for a static provider fieldset
                && RatioFieldDescription.FIELDNAMES_BY_PERMISSION.containsKey(key.get())) {
            result.putAll(RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get(key.get()));
        }
    }

    // order must be the same as in FundDataProviderImpl.getProvider() to stay consistent
    static Optional<String> findFirstAllowedProvider(Profile profile) {
        if (profile.isAllowed(Selector.VWD_FUND_DATA) || profile.isAllowed(Selector.VWD_FUND_MAPBIT_BREAKDOWNS)) {
            return Optional.of("VWD");
        }
        if (MarketAdmissionUtil.allowByMarketAdmissionMorningstar(profile)) {
            return Optional.of("MORNINGSTAR");
        }
        if (profile.isAllowed(Selector.FUNDDATA_VWD_BENL)) {
            return Optional.of("VWD_BENL");
        }
        if (profile.isAllowed(Selector.FIDA_FUND_DATA) || profile.isAllowed(Selector.FIDA_FUND_DATA_I)) {
            return Optional.of("FIDA");
        }
        if (profile.isAllowed(Selector.FUNDDATA_FWW)) {
            slogger.warn("<findFirstAllowedProvider> FUNDDATA_FWW should not be used any more");
            return Optional.of("FWW");
        }
        if (profile.isAllowed(Selector.FUNDDATA_VWD_IT)) {
            slogger.warn("<findFirstAllowedProvider> FUNDDATA_VWD_IT should not be used any more");
            return Optional.of("VWD_IT");
        }
        if (profile.isAllowed(Selector.SSAT_FUND_DATA)) {
            slogger.warn("<findFirstAllowedProvider> SSAT_FUND_DATA should not be used any more");
            return Optional.of("SSAT");
        }
        return Optional.empty();
    }

    // return true iff user has access to the selected provider (false for no provider or no access)
    static boolean hasProviderAndPermission(Profile profile, String selectedProviderId) {
        if ("VWD".equals(selectedProviderId)) {
            return profile.isAllowed(Selector.VWD_FUND_DATA) || profile.isAllowed(Selector.VWD_FUND_MAPBIT_BREAKDOWNS);
        }
        if ("MORNINGSTAR".equals(selectedProviderId)) {
            return MarketAdmissionUtil.allowByMarketAdmissionMorningstar(profile);
        }
        if ("VWD_BENL".equals(selectedProviderId)) {
            return profile.isAllowed(Selector.FUNDDATA_VWD_BENL);
        }
        if ("FIDA".equals(selectedProviderId)) {
            return profile.isAllowed(Selector.FIDA_FUND_DATA) || profile.isAllowed(Selector.FIDA_FUND_DATA_I);
        }
        if ("FWW".equals(selectedProviderId)) {
            slogger.warn("<hasProviderAndPermission> FUNDDATA_FWW should not be used any more");
            return profile.isAllowed(Selector.FUNDDATA_FWW);
        }
        if ("VWD_IT".equals(selectedProviderId)) {
            slogger.warn("<hasProviderAndPermission> FUNDDATA_VWD_IT should not be used any more");
            return profile.isAllowed(Selector.FUNDDATA_VWD_IT);
        }
        if ("SSAT".equals(selectedProviderId)) {
            slogger.warn("<hasProviderAndPermission> SSAT_FUND_DATA should not be used any more");
            return profile.isAllowed(Selector.SSAT_FUND_DATA);
        }
        return false; // fallback for null, mean we gonna lookup the hardcoded prio list
    }

    // adding the rating fields, order of providers doesn't matter here since the fields should be disjoint
    private static void collectRatingFields(Profile profile, HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> result) {
        addIfAllowed(profile, Selector.RATING_MORNINGSTAR_UNION_FND, Selector.RATING_MORNINGSTAR_UNION_FND.name(), result);
        addIfAllowed(profile, Selector.RATING_MORNINGSTAR, Selector.RATING_MORNINGSTAR.name(), result);
        addIfAllowed(profile, Selector.DIAMOND_RATING, Selector.DIAMOND_RATING.name(), result);
        addIfAllowed(profile, Selector.FUNDDATA_VWD_IT, "VWD_IT_RATING", result);
        addIfAllowed(profile, Selector.RATING_FERI, Selector.RATING_FERI.name(), result);
        addFidaRatingIfAllowed(profile, result);
    }

    private static void addFidaRatingIfAllowed(Profile p, Map<RatioDataRecord.Field, RatioFieldDescription.Field> result) {
        if (p.isAllowed(Selector.FIDA_FUND_RATING) && p.isAllowed(Selector.FIDA_FUND_RATING_I)) {
            result.put(RatioDataRecord.Field.fidaPermissionType, RatioFieldDescription.fidaPermissionType);
            result.put(RatioDataRecord.Field.fidaRating, RatioFieldDescription.fidaRating);
        } else if (p.isAllowed(Selector.FIDA_FUND_RATING)) {
            result.put(RatioDataRecord.Field.fidaPermissionType, RatioFieldDescription.fidaPermissionType);
            result.put(RatioDataRecord.Field.fidaRating, RatioFieldDescription.fidaRatingROnly);
        } else if (p.isAllowed(Selector.FIDA_FUND_RATING_I)) {
            result.put(RatioDataRecord.Field.fidaPermissionType, RatioFieldDescription.fidaPermissionType);
            result.put(RatioDataRecord.Field.fidaRating, RatioFieldDescription.fidaRatingIOnly);
        }
    }

    private static boolean isEdgAllowed(Profile profile) {
        return profile.isAllowed(Selector.EDG_RATING) || profile.isAllowed(Selector.EDG_DATA) || profile.isAllowed(Selector.EDG_DATA_2);
    }

    /**
     * turn a query string into a map
     *  - key of the map element is the field plus the encoded relation
     *  - value of the map element is the value for the relation
     *
     * @param query the query string
     * @param fields allowed fields, used for name lookup when parsing the query
     * @return map with expressions
     */
    public static Map<String, String> parseQuery(String query, Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        return new FinderQueryParserSupport(fields).parseQuery(query);
    }

    public static String ensureQuotedValues(String query) {
        return FinderQueryParserSupport.ensureQuotedValues(query);
    }

    protected static final int MAX_NUM_SEARCHSTRINGS = 50;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected RatiosProvider ratiosProvider;

    protected IntradayProvider intradayProvider;

    protected UnderlyingShadowProvider underlyingShadowProvider;

    protected AbstractFindersuchergebnis(Class cmdClass) {
        super(cmdClass);
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    static protected RatioSearchRequest createRequest(InstrumentTypeEnum type) {
        return createRequest(type, new String[0], null);
    }

    static protected RatioSearchRequest createRequest(InstrumentTypeEnum type,
                                                      String[] searchstring,
                                                      DataRecordStrategy.Type dataRecordStrategy) {
        return createRequest(type, null, dataRecordStrategy, null, false, searchstring);
    }

    static protected RatioSearchRequest createRequest(InstrumentTypeEnum type, Command cmd,
                                                      Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        return createRequest(type, cmd.getAdditionalType(), cmd.getDataRecordStrategy(),
                fields, cmd.isWithMetadataCount(), null);
    }

    static protected RatioSearchRequest createRequest(InstrumentTypeEnum type,
                                                      InstrumentTypeEnum[] additionalTypes, DataRecordStrategy.Type strategy,
                                                      Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
                                                      boolean withMetadataCount, String[] searchstring) {
        return createRequest(type, additionalTypes, strategy, fields, withMetadataCount, searchstring, null);
    }

    static protected RatioSearchRequest createRequest(InstrumentTypeEnum type,
                                                      InstrumentTypeEnum[] additionalTypes, DataRecordStrategy.Type strategy,
                                                      Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
                                                      boolean withMetadataCount, String[] searchstring, String selectedProvider) {
        final RatioSearchRequest result = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        result.setType(type);
        result.setAdditionalTypes(additionalTypes);
        result.setSelectedProvider(selectedProvider);

        if (fields != null && withMetadataCount) {
            final Set<Integer> metadataFieldids =
                    fields.values()
                            .stream()
                            .filter(field -> field != null && (field.isEnum() || field.isEnumSet()))
                            .map(RatioFieldDescription.Field::id)
                            .collect(Collectors.toSet());
            // remove because of larger answers typically not needed
            metadataFieldids.remove(RatioFieldDescription.underlyingIsin.id());
            metadataFieldids.remove(RatioFieldDescription.underlyingWkn.id());
            metadataFieldids.remove(RatioFieldDescription.underlyingName.id());
            metadataFieldids.remove(RatioFieldDescription.merHandelsmonat.id());
            result.setMetadataFieldids(new ArrayList<>(metadataFieldids));
        }

        if (strategy != null) {
            result.setDataRecordStrategyClass(strategy.getClazz());
        }

        if (searchstring != null && searchstring.length > 0) {
            final StringBuilder stb = new StringBuilder();
            for (int i = 0; i < Math.min(MAX_NUM_SEARCHSTRINGS, searchstring.length); i++) {
                final String s = searchstring[i];
                if (!StringUtils.hasText(s)) {
                    continue;
                }
                if (stb.length() > 0) {
                    stb.append("@");
                }
                stb.append(s);
            }
            result.addParameter(getSearchStringKey(fields), stb.toString());
        }

        return result;
    }

    private static final RatioDataRecord.Field[] SEARCH_STRING_KEY = {
            RatioDataRecord.Field.isin,
            RatioDataRecord.Field.wkn,
            RatioDataRecord.Field.name
    };

    private static String getSearchStringKey(Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final StringBuilder sb = new StringBuilder(30);
        for (RatioDataRecord.Field field : SEARCH_STRING_KEY) {
            if (fields != null && fields.containsKey(field)) {
                sb.append(fields.get(field).name());
            }
            else {
                sb.append(field.name());
            }
            sb.append("@");
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    protected boolean addParameter(RatioSearchRequest rsr, String key, String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        rsr.addParameter(key, value);
        return true;
    }

    /**
     * Adds sort parameters to a request
     * @param rsr target
     * @param listResult contains the Sort fields
     * @param fields mappings from RatioDataRecord.Field to RatioFieldDescription.Field; a Sort's name
     * refers to a RatioDataRecord.Field and the corresponding RatioFieldDescription.Field is
     * the actual field to be used for sorting in the backend.
     */
    protected void addSorts(RatioSearchRequest rsr, ListResult listResult,
                            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final List<ListResult.Sort> sorts = listResult.getSorts();
        for (int i = 0; i < sorts.size(); ) {
            final ListResult.Sort sort = sorts.get(i);
            final RatioDataRecord.Field dataRecordField = RatioDataRecord.Field.valueOf(sort.getName());
            final RatioFieldDescription.Field field = fields.get(dataRecordField);
            i++;
            rsr.addParameter(PREFIX_SORT + i, field != null ? field.name() : sort.getName());
            rsr.addParameter(PREFIX_SORT + i + SUFFIX_DESC, Boolean.toString(!sort.isAscending()));
        }
    }

    /**
     * Filters list of only sortable ratio field names.
     */
    protected List<String> asSortfields(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        return fields.entrySet()
                .stream()
                .filter(x -> x.getValue() != null)
                .filter(x -> RatioFieldDescription.Type.ENUMSET != x.getValue().type())
                .map(x -> x.getKey().name())
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected Map<String, Object> createResultModel(InstrumentTypeEnum type,
                                                    RatioSearchResponse sr, ListResult listResult) {
        return createResultModel(sr, listResult, RatioFieldDescription.FIELDNAMES.get(type), false, false);
    }

    protected Map<String, Object> createResultModel(RatioSearchResponse sr, ListResult listResult,
                                                    Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
                                                    boolean withPrices,
                                                    boolean withAllQuotes) {
        final Map<String, Object> model = new HashMap<>();

        final DefaultRatioSearchResponse drsr = (DefaultRatioSearchResponse) sr;

        final List<String> uids = getUnderlyingIids(drsr);
        final List<RatioDataRecord> records = getRecords(drsr, fields);

        List<Long> qids = getQids(drsr);
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);

        int numRemoved = CollectionUtils.removeNulls(quotes, uids, records);
        if (numRemoved > 0) {
            final Set<Long> unknownQids = new HashSet<>(qids);
            quotes.stream().forEach(q -> unknownQids.remove(q.getId()));
            this.logger.warn("<createResultModel> failed to identify " +
                    unknownQids.stream().map(qid -> qid + ".qid").collect(Collectors.joining(", ")));
        }

        if (withAllQuotes) {
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();

            final List<List<Quote>> allQuotes =
                    quotes.stream()
                            .map(quote -> ProfiledInstrument.quotesWithPrices(quote.getInstrument(), profile))
                            .collect(Collectors.toList());
            model.put("allQuotes", allQuotes);
        }

        model.put("withPrices", withPrices);

        if (withPrices) {
            final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
            model.put("priceRecords", priceRecords);

            final List<Quote> uQuotes
                    = this.instrumentProvider.identifyQuotes(uids, IID, null, "underlying:XXP,EUR,USD");
            ensureStrikeCurrencyForUnderlyingQuotes(quotes, uQuotes);
            final List<PriceRecord> uPriceRecords = this.intradayProvider.getPriceRecords(uQuotes);
            model.put("uQuotes", uQuotes);
            model.put("uPriceRecords", uPriceRecords);
        }

        listResult.setOffset(drsr.getOffset());
        listResult.setCount(drsr.getElements().size() - numRemoved);
        listResult.setTotalCount(drsr.getNumTotal() - numRemoved);

        model.put("quotes", quotes);
        model.put("records", records);
        model.put("listinfo", listResult);
        model.put("partitions", drsr.getPartition());

        final Map<Integer, Map<String, Integer>> backendMetadata = drsr.getMetadata();
        if (backendMetadata != null) {
            final Map<MetaDataMapKey, List<FinderMetaItem>> metadata = new TreeMap<>();
            for (final Map.Entry<RatioDataRecord.Field, RatioFieldDescription.Field> entry : fields.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                final Map<String, Integer> countByValue = backendMetadata.get(entry.getValue().id());
                if (countByValue != null) {
                    final List<FinderMetaItem> values = toMetaItemList(countByValue);
                    metadata.put(new MetaDataMapKey(entry.getKey(), entry.getValue()), values);
                }
            }
            model.put("metadata", metadata);
        }

        return model;
    }

    private List<FinderMetaItem> toMetaItemList(Map<String, Integer> countByValue) {
        return countByValue.entrySet()
                .stream()
                .map(entry -> new FinderMetaItem(entry.getKey(), entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void ensureStrikeCurrencyForUnderlyingQuotes(List<Quote> quotes, List<Quote> uQuotes) {
        for (int i = 0; i < quotes.size(); i++) {
            final Quote q = quotes.get(i);
            final Quote uq = uQuotes.get(i);
            if (q == null || uq == null) {
                continue;
            }
            if (q.getCurrency().getId() == uq.getCurrency().getId()) {
                continue;
            }
            if (i > 0 && quotes.get(i) == quotes.get(i - 1)) {
                uQuotes.set(i, uQuotes.get(i - 1));
                continue;
            }
            final Quote cq = this.instrumentProvider.getQuote(uq.getInstrument(), null, "currency:" + q.getCurrency().getSymbolIso());
            uQuotes.set(i, cq);
        }
    }

    private List<Long> getQids(DefaultRatioSearchResponse drsr) {
        return drsr.getElements().stream().map(RatioDataResult::getQuoteid).collect(toList());
    }

    private List<String> getUnderlyingIids(DefaultRatioSearchResponse drsr) {
        return drsr.getElements().stream().map(this::getUnderlyingIid).collect(toList());
    }

    private String getUnderlyingIid(RatioDataResult rdr) {
        if (rdr.getInstrumentRatios() instanceof InstrumentRatiosDerivative) {
            final InstrumentRatiosDerivative idr = (InstrumentRatiosDerivative) rdr.getInstrumentRatios();
            final long uid = idr.getUnderlyingIid();
            final Long suid = this.underlyingShadowProvider.getShadowInstrumentId(uid);
            if (suid != null) {
                return suid.toString();
            }
            if (uid != Long.MIN_VALUE) {
                return Long.toString(uid);
            }
        }
        // cannot use null as that would confuse the instrument provider during identification
        // so use an iid value that cannot be mapped to a valid instrument
        return "0";
    }

    protected List<RatioDataRecord> getRecords(DefaultRatioSearchResponse drsr,
                                               Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final List<RatioDataRecord> result = new ArrayList<>(drsr.getElements().size());

        if (fields.containsKey(RatioDataRecord.Field.fidaRating)) {
            HashMap<RatioDataRecord.Field, RatioFieldDescription.Field> reducedFields = new HashMap<>(fields);
            reducedFields.remove(RatioDataRecord.Field.fidaRating);

            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            for (final RatioDataResult rdr : drsr.getElements()) {
                String permissionType = rdr.getInstrumentRatios().getString(
                        RatioFieldDescription.fidaPermissionType.id());
                if (FidaProfiler.allowByPermissionType(profile, permissionType,
                        Selector.FIDA_FUND_RATING, Selector.FIDA_FUND_RATING_I)) {
                    result.add(new RatioDataRecordImpl(rdr.getInstrumentRatios(), rdr.getQuoteData(),
                            fields, RequestContextHolder.getRequestContext().getLocales()));
                }
                else {
                    result.add(new RatioDataRecordImpl(rdr.getInstrumentRatios(), rdr.getQuoteData(),
                            reducedFields, RequestContextHolder.getRequestContext().getLocales()));
                }
            }
        }
        else {
            result.addAll(
                    drsr.getElements()
                            .stream()
                            .map(rdr ->
                                    new RatioDataRecordImpl(rdr.getInstrumentRatios(),
                                            rdr.getQuoteData(),
                                            fields,
                                            RequestContextHolder.getRequestContext().getLocales())
                            )
                            .collect(Collectors.toList()));
        }

        return result;
    }
}
