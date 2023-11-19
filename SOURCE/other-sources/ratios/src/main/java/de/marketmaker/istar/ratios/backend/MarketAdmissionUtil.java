/*
 * RatioFieldConverter.java
 *
 * Created on 24.08.2010 11:34:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * Contains utility methods that are relevant for permission implementation based on market
 * admission of fund data.
 *
 * <p>
 * JIRA ticket ISTAR98, ISTAR103.
 *
 * @author zzhao
 * @since 1.1.1
 */
public final class MarketAdmissionUtil {

    public static final String DEFAULT_SEPARATOR = ",";

    /**
     * A map between ISO-3166 alpha3 and alpha2 symbols. To support further symbols, just add new
     * mappings.
     */
    private static final Map<String, String> ISO_MAP;

    static {
        ISO_MAP = new HashMap<>(9);
        ISO_MAP.put("DEU", "DE");
        ISO_MAP.put("AUT", "AT");
        ISO_MAP.put("CHE", "CH");
        ISO_MAP.put("ITA", "IT");
    }

    public static final Collection<String> MARKET_ADMISSIONS =
            Collections.unmodifiableCollection(ISO_MAP.values());

    private static final Map<String, String> ISO_CACHE = new ConcurrentHashMap<>();

    /**
     * A map between ISO-3166 alpha2 symbols and iStar-selectors. To support further permissions,
     * just add new mappings.
     */
    private static final Map<String, Selector> SELECTOR_MAP_MORNINGSTAR;

    private static final Map<RatioDataRecord.Field, RatioFieldDescription.Field>
            MORNINGSTAR_RATIO_FIELDS_MAPPING = RatioFieldDescription.FIELDNAMES_BY_PERMISSION.get("MORNINGSTAR");

    private static final Collection<RatioFieldDescription.Field> MORNINGSTAR_RATIO_FIELDS =
            Collections.unmodifiableCollection(MORNINGSTAR_RATIO_FIELDS_MAPPING.values());

    static {
        SELECTOR_MAP_MORNINGSTAR = new HashMap<>(9);
        SELECTOR_MAP_MORNINGSTAR.put("DE", Selector.FUNDDATA_MORNINGSTAR_DE);
        SELECTOR_MAP_MORNINGSTAR.put("AT", Selector.FUNDDATA_MORNINGSTAR_AT);
        SELECTOR_MAP_MORNINGSTAR.put("CH", Selector.FUNDDATA_MORNINGSTAR_CH);
        SELECTOR_MAP_MORNINGSTAR.put("IT", Selector.FUNDDATA_MORNINGSTAR_IT);
    }

    private static final Map<String, Selector> SELECTOR_MAP_STOCK_SELECTION_FUND;

    static {
        SELECTOR_MAP_STOCK_SELECTION_FUND = new HashMap<>(9);
        SELECTOR_MAP_STOCK_SELECTION_FUND.put("DE", Selector.STOCKSELECTION_FUND_REPORTS_DE);
        SELECTOR_MAP_STOCK_SELECTION_FUND.put("CH", Selector.STOCKSELECTION_FUND_REPORTS_CH);
        SELECTOR_MAP_STOCK_SELECTION_FUND.put("IT", Selector.STOCKSELECTION_FUND_REPORTS_IT);
        SELECTOR_MAP_STOCK_SELECTION_FUND.put("AT", Selector.STOCKSELECTION_FUND_REPORTS_AT);
    }

    private static final Collection<Selector> VALID_SELECTORS_MORNINGSTAR = Collections.unmodifiableCollection(
            SELECTOR_MAP_MORNINGSTAR.values());

    private static final Collection<Selector> VALID_SELECTORS_STOCK_SELECTION = Collections.unmodifiableCollection(
            SELECTOR_MAP_STOCK_SELECTION_FUND.values());

    private MarketAdmissionUtil() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    /**
     * Tests if the given profile contains any Morningstar related selectors.
     *
     * @param profile a user profile.
     * @return true if the given user's profile contains any Morningstar relared selectors, false
     * otherwise.
     */
    public static boolean containsMorningstarSelector(Profile profile) {
        if (null == profile) {
            return false;
        }

        for (final Selector sel : VALID_SELECTORS_MORNINGSTAR) {
            if (profile.isAllowed(sel)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param profile a user profile.
     * @return a list of ISO-3166 alpha2 symbols denoting market admissions allowed by the given
     * user's profile.
     */
    public static List<String> getMarketAdmissionsMorningstar(Profile profile) {
        return getMarketAdmissions(profile, SELECTOR_MAP_MORNINGSTAR);
    }

    private static List<String> getMarketAdmissions(Profile profile, Map<String, Selector> sMap) {
        if (null == profile) {
            return Collections.emptyList();
        }

        final Set<String> ret = new HashSet<>(sMap.size());
        for (final Map.Entry<String, Selector> entry : sMap.entrySet()) {
            if (profile.isAllowed(entry.getValue())) {
                ret.add(entry.getKey());
            }
        }
        return new ArrayList<>(ret);
    }

    /**
     * @param toBeConverted a string of ISO-3166 alpha3 symbols separated by {@link #DEFAULT_SEPARATOR}.
     * @return a string of ISO-3166 alpha2 symbols separated by {@link #DEFAULT_SEPARATOR}.
     */
    public static String iso3166Alpha3To2(String toBeConverted) {
        if (!StringUtils.hasText(toBeConverted)) {
            return "";
        }

        final String cached = ISO_CACHE.get(toBeConverted);
        if (cached != null) {
            return cached;
        }

        final StringBuilder sb = new StringBuilder(toBeConverted.length());
        for (final String alpha3 : toBeConverted.split(DEFAULT_SEPARATOR)) {
            final String alpha2 = ISO_MAP.get(alpha3.trim());
            if (StringUtils.hasText(alpha2)) {
                sb.append(alpha2).append(DEFAULT_SEPARATOR);
            }
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        final String ret = sb.toString();
        ISO_CACHE.put(toBeConverted, ret);

        return ret;
    }

    /**
     * @param alpha2s a string of ISO-3166 alpha2 symbols separated by {@link #DEFAULT_SEPARATOR}.
     * @return a collection of Morningstar {@link Selector}s mapped from the given market admissions
     * denoted by the given ISO-3166 alpha2 symbols.
     */
    public static Collection<Selector> toMorningstarSelectors(String alpha2s) {
        return iso3166Alpha2ToSelectors(alpha2s, SELECTOR_MAP_MORNINGSTAR);
    }

    /**
     * @param alpha2s a string of ISO-3166 alpha2 symbols separated by {@link #DEFAULT_SEPARATOR}.
     * @return a collection of Stock Selection {@link Selector}s mapped from the given market
     * admissions denoted by the given ISO-3166 alpha2 symbols.
     */
    public static Collection<Selector> toStockSelectionSelectors(String alpha2s) {
        return iso3166Alpha2ToSelectors(alpha2s, SELECTOR_MAP_STOCK_SELECTION_FUND);
    }

    private static Collection<Selector> iso3166Alpha2ToSelectors(String alpha2s,
            Map<String, Selector> map) {
        if (!StringUtils.hasText(alpha2s)) {
            return Collections.emptySet();
        }

        final Set<Selector> ret = new HashSet<>();
        for (final String alpha2 : alpha2s.split(DEFAULT_SEPARATOR)) {
            final Selector selector = map.get(alpha2.trim());
            if (null != selector) {
                ret.add(selector);
            }
        }

        return Collections.unmodifiableSet(ret);
    }


    private static boolean allow(Profile profile, Collection<Selector> selectors) {
        if (null == profile) {
            return false;
        }

        if (CollectionUtils.isEmpty(selectors)) {
            return false;
        }
        else {
            for (Selector s : selectors) {
                if (profile.isAllowed(s)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Tests if the given user's profile is allowed to access the Morningstar market admissions
     * denoted by the given ISO-3166 alpha2 symbols.
     *
     * @param profile a user's profile.
     * @param alpha2s a string of ISO-3166 alpha2 symbols separated by {@link #DEFAULT_SEPARATOR}.
     * @return true if the given user's profile is allowed to access at least one of the given market
     * admissions, false otherwise. Also false if the given market admissions are empty.
     */
    public static boolean allowByMarketAdmissionMorningstar(Profile profile, String alpha2s) {
        return allow(profile, toMorningstarSelectors(alpha2s));
    }

    /**
     * Tests if the given user's profile is allowed to access the stock selection market admissions
     * denoted by the given ISO-3166 alpha2 symbols.
     *
     * @param profile a user's profile.
     * @param alpha2s a string of ISO-3166 alpha2 symbols separated by {@link #DEFAULT_SEPARATOR}.
     * @return true if the given user's profile is allowed to access at least one of the given market
     * admissions, false otherwise. Also false if the given market admissions are empty.
     */
    public static boolean allowByMarketAdmissionStockSelection(Profile profile, String alpha2s) {
        return allow(profile, toStockSelectionSelectors(alpha2s));
    }

    /**
     * Tests if the given user's profile is allowed to access any Morningstar market admissions.
     *
     * @param profile a user's profile.
     * @return true if the given user's profile is activated for at least one market admission,
     * false otherwise.
     */
    public static boolean allowByMarketAdmissionMorningstar(Profile profile) {
        return allow(profile, VALID_SELECTORS_MORNINGSTAR);
    }

    /**
     * Tests if the given user's profile is allowed to access any stock selection market admissions.
     *
     * @param profile a user's profile.
     * @return true if the given user's profile is activated for at least one market admission,
     * false otherwise.
     */
    public static boolean isStockSelectionAllowed(Profile profile) {
        return allow(profile, VALID_SELECTORS_STOCK_SELECTION);
    }

    /**
     * Filters the given fields to exclude those fields that are not allowed to be accessed by the
     * given user's profile.
     *
     * @param profile a user's profile.
     * @param fields a map of fields.
     * @return a map of allowed fields.
     */
    public static Map<RatioDataRecord.Field, RatioFieldDescription.Field> getFilteredFieldsMS(
            Profile profile, Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        if (!containsMorningstarSelector(profile) && containsAnyMorningstarFundField(fields)) {
            final Map<RatioDataRecord.Field, RatioFieldDescription.Field> ret =
                    new HashMap<>(fields.size());
            for (final Map.Entry<RatioDataRecord.Field, RatioFieldDescription.Field> entry : fields.entrySet()) {
                if (MORNINGSTAR_RATIO_FIELDS.contains(entry.getValue())) {
                    ret.put(entry.getKey(), null);
                }
                else {
                    ret.put(entry.getKey(), entry.getValue());
                }
            }

            return ret;
        }

        return fields;
    }

    private static boolean containsAnyMorningstarFundField(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields) {
        for (final RatioFieldDescription.Field field : MORNINGSTAR_RATIO_FIELDS) {
            if (fields.containsValue(field)) {
                return true;
            }
        }

        return false;
    }
}
