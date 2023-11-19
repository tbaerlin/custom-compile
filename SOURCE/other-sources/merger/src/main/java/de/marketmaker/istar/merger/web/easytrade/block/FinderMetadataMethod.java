/*
 * FinderMetadataMethod.java
 *
 * Created on 31.07.2009 12:30:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * Method object that creates a model with ratio metadata.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderMetadataMethod {
    protected final static Logger logger = LoggerFactory.getLogger(FinderMetadataMethod.class);

    private static final Collator GERMAN_COLLATOR_PRIMARY = Collator.getInstance(Locale.GERMAN);

    static {
        GERMAN_COLLATOR_PRIMARY.setStrength(Collator.PRIMARY);
    }

    private static final Set<String> COUNTRIES = new HashSet<>(Arrays.asList(
            "DE", "CH", "AT", "FR", "IT", "ES", "BE", "NL", "US", "JP", "CN", "AU", "DK",
            "FI", "GB", "HK", "CA", "HR", "NO", "PL", "PT", "SE", "RS", "RU", "TR", "SG", "LU", "IE"
    ));

    private static final RatioDataRecord.Field MARKET = RatioDataRecord.Field.market;

    private final Set<RatioDataRecord.Field> dataRecordFields = new LinkedHashSet<>();

    private final RatiosProvider ratiosProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private final InstrumentTypeEnum type;

    private String providerPreference;

    private final String query;

    private boolean withDetailedSymbol;

    FinderMetadataMethod(InstrumentTypeEnum type, RatiosProvider ratiosProvider,
            EasytradeInstrumentProvider instrumentProvider,
            RatioDataRecord.Field[] dataRecordFields, String providerPreference, String query) {
        this.type = type;
        this.ratiosProvider = ratiosProvider;
        this.instrumentProvider = instrumentProvider;
        this.dataRecordFields.addAll(Arrays.asList(dataRecordFields));
        this.providerPreference = providerPreference;
        this.query = query;
    }

    void setWithDetailedSymbol(boolean withDetailedSymbol) {
        this.withDetailedSymbol = withDetailedSymbol;
    }

    Map<String, Object> invoke() {
        final PermissionType pt = this.type == InstrumentTypeEnum.FND ? PermissionType.FUNDDATA : null;
        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(this.type, this.providerPreference, pt);

        final Map<Integer, Map<String, Integer>> metadata = getMetaData(fields);

        final Map<String, Object> result = new HashMap<>();
        for (RatioDataRecord.Field field : this.dataRecordFields) {
            result.put(field.name(), getItems(metadata, fields, field));
        }

        result.put("category2markets", getCategory2Markets(metadata, fields));

        return result;
    }

    private Map<Integer, Map<String, Integer>> getMetaData(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final RatioSearchRequest request = new RatioSearchRequest(
                profile,
                RequestContextHolder.getRequestContext().getLocales());
        request.setType(this.type);
        request.setWithDetailedSymbol(this.withDetailedSymbol);

        if (AbstractFindersuchergebnis.hasProviderAndPermission(profile, providerPreference)) {
            request.setSelectedProvider(providerPreference);
        } else {
            request.setSelectedProvider(AbstractFindersuchergebnis.findFirstAllowedProvider(profile).orElse(null));
        }

        if (StringUtils.hasText(this.query)) {
            final Map<String, String> parameter
                    = AbstractFindersuchergebnis.parseQuery(this.query, fields);
            request.setParameters(parameter);
        }

        request.setMetadataFieldids(new ArrayList<>(getMetadataFields(fields)));
        request.addParameter("n", "0"); // we are only interested in meta data

        final RatioSearchResponse response = this.ratiosProvider.search(request);
        if (!response.isValid()) {
            throw new InternalFailure("failed to get metadata");
        }

        return ((DefaultRatioSearchResponse) response).getMetadata();
    }

    private Set<Integer> getMetadataFields(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {

        final Set<Integer> result = new HashSet<>();
        for (Map.Entry<RatioDataRecord.Field, RatioFieldDescription.Field> e : fields.entrySet()) {
            if (isRequiredField(e.getKey()) && isAcceptableField(e.getValue())) {
                result.add(e.getValue().id());
            }
        }
        return result;
    }

    private boolean isAcceptableField(final RatioFieldDescription.Field field) {
        return field != null && (field.isEnum() || field.isEnumSet()) && !isUnderlyingField(field);
    }

    private boolean isUnderlyingField(RatioFieldDescription.Field field) {
        return field == RatioFieldDescription.underlyingIsin
                || field == RatioFieldDescription.underlyingWkn
                || field == RatioFieldDescription.underlyingName;
    }

    private boolean isRequiredField(final RatioDataRecord.Field field) {
        // all fields that are used as last parameter when calling getItems(...)
        return dataRecordFields.contains(field) || field == MARKET;
    }

    private Map<String, Collection<String>> getCategory2Markets(
            Map<Integer, Map<String, Integer>> metadata,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {

        final Set<String> rtMarkets = getMarkets(metadata, fields);

        final SearchMetaResponse response = this.instrumentProvider.getSearchMetadata();


        final Map<String, Collection<String>> sortedMarketMap
                = new TreeMap<>(GERMAN_COLLATOR_PRIMARY);

        for (final Market market : response.getMarkets()) {
            if (!isExchange(market)) {
                continue;
            }

            final String marketname = AbstractFinderMetadata.localize(market.getCountry().getSymbolIso());
            if (marketname == null) {
                continue;
            }

            final String marketSymbol = market.getSymbolVwdfeed();
            if (rtMarkets.contains(marketSymbol)) {
                Collection<String> marketsByCountry = sortedMarketMap.get(marketname);
                if (marketsByCountry == null) {
                    marketsByCountry = new TreeSet<>();
                    sortedMarketMap.put(marketname, marketsByCountry);
                }

                marketsByCountry.add(marketSymbol);
            }
        }

        final Map<String, Collection<String>> result
                = new LinkedHashMap<>(sortedMarketMap);
        final List<String> issuers = getIssuers(response.getMarkets(), rtMarkets);
        if (!issuers.isEmpty()) {
            result.put(AbstractFinderMetadata.localize("Issuer"), issuers);
        }
        return result;
    }

    private HashSet<String> getMarkets(
            Map<Integer, Map<String, Integer>> metadata,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        final List<String> items = getItems(metadata, fields, MARKET);
        final HashSet<String> ret = new HashSet<>();
        for (String item : items) {
            ret.add(item);
        }

        return ret;
    }

    private List<String> getIssuers(List<Market> markets, Set<String> rtMarkets) {
        final List<String> result = new ArrayList<>();
        for (final Market market : markets) {
            final String symbol = market.getSymbolVwdfeed();
            if (isIssuer(market) && rtMarkets.contains(symbol)) {
                result.add(symbol);
            }
        }
        return result;
    }

    private boolean isExchange(Market market) {
        return StringUtils.hasText(market.getSymbolVwdfeed())
                && market.getMarketcategory() != null
                && market.getMarketcategory() == MarketcategoryEnum.BOERSE
                && COUNTRIES.contains(market.getCountry().getSymbolIso());
    }

    private boolean isIssuer(Market market) {
        return StringUtils.hasText(market.getSymbolVwdfeed())
                && market.getMarketcategory() != null
                && market.getMarketcategory() != MarketcategoryEnum.BOERSE;
    }

    public static List<String> getItems(
            Map<Integer, Map<String, Integer>> metadata,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            RatioDataRecord.Field field) {
        final RatioFieldDescription.Field ratiofield = fields.get(field);
        if (ratiofield == null) {
            return Collections.emptyList();
        }

        final Map<String, Integer> enums = metadata.get(ratiofield.id());
        if (enums == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(enums.keySet());
    }
}
