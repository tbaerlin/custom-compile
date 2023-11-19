/*
 * IndexConstants.java
 *
 * Created on 24.08.2005 13:42:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.domain.KeysystemEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexConstants {
    public static final String FIELDNAME_COUNTRY = "country";

    public static final String FIELDNAME_CURRENCY = "currency";

    public static final String FIELDNAME_ENTITLEMENT_VWD = "entlvwd";

    public static final String FIELDNAME_ENTITLEMENT_ABO = "entlabo";

    /**
     * Not used, cannot be searched as field.
     */
    public static final String FIELDNAME_EXPIRES = "expires";

    public static final String FIELDNAME_HOME_EXCHANGE = "homeexchange";

    public static final String FIELDNAME_IID = "instrumentid";

    public static final String FIELDNAME_IS_MMXML_BLACKLIST = "mmxml";

    public static final String FIELDNAME_IS_OPRA_BLACKLIST = "opra";

    public static final String FIELDNAME_MARKET = "market";

    /**
     * Not indexed, cannot be searched as field.
     */
    public static final String FIELDNAME_MPC_COUNT = "count";

    public static final String FIELDNAME_MPC_VALUE = "value";

    public static final String FIELDNAME_NAME = "name";

    public static final String FIELDNAME_NAME_COST = "name_cost";

    public static final String FIELDNAME_NAME_FREE = "name_free";

    public static final String FIELDNAME_ALIAS = "alias";

    public static final String FIELDNAME_LEI = "lei";


    /**
     * Field 'names' can be searched for quote. It contains the name of the instrument a quote
     * is associated with, its {@link de.marketmaker.istar.domain.KeysystemEnum#PM_INSTRUMENT_NAME_FREE}
     * and the symbols defined under:
     * <ul>
     * <li>{@link de.marketmaker.istar.domain.KeysystemEnum#WM_WP_NAME_KURZ}</li>
     * <li>{@link de.marketmaker.istar.domain.KeysystemEnum#WM_WP_NAME_LANG}</li>
     * <li>{@link de.marketmaker.istar.domain.KeysystemEnum#WM_WP_NAME_ZUSATZ}</li>
     * </ul>
     * of all the quotes under that instrument.
     */
    public static final String FIELDNAME_NAMES = "names";

    public static final String FIELDNAME_QID = "quoteid";

    public static final String FIELDNAME_QUOTESYMBOLS = "quotesymbols";

    public static final String FIELDNAME_WITH_MMTYPE = "withmmtype";

    public static final String FIELDNAME_NUM_INDEXED = "num";

    public static final String FIELDNAME_SORT_INSTRUMENT_DEFAULT = "isort";

    public static final String FIELDNAME_SORT_QUOTE_DEFAULT = "qsort";

    public static final String FIELDNAME_SORT_QUOTE_VOLUME = "qvsort";

    public static final String FIELDNAME_SORT_QUOTE_VOLUME_PREFER_DE = "qvsortde";

    public static final String FIELDNAME_SORT_QUOTE_PREFER_BE = "qsortbe";

    public static final String FIELDNAME_SORT_QUOTE_PREFER_CH = "qsortch";

    public static final String FIELDNAME_SORT_QUOTE_PREFER_FR = "qsortfr";

    public static final String FIELDNAME_SORT_QUOTE_PREFER_IT = "qsortit";

    public static final String FIELDNAME_SORT_QUOTE_PREFER_NL = "qsortnl";

    public static final String FIELDNAME_TYPE = "type";

    public static final String FIELDNAME_UNDERLYINGID = "underlyingid";

    public static final String FIELDNAME_WMTYPE = "wmtype";

    public static final String FIELDNAME_FLAG = "flag";

    public static final String FIELDNAME_WM_WP_NAME_ZUSATZ
            = KeysystemEnum.WM_WP_NAME_ZUSATZ.name().toLowerCase();

    public static final String FIELDNAME_WM_WP_NAME_LANG
            = KeysystemEnum.WM_WP_NAME_LANG.name().toLowerCase();

    public static final String FIELDNAME_WM_WP_NAME_KURZ
            = KeysystemEnum.WM_WP_NAME_KURZ.name().toLowerCase();

    public static final String FIELDNAME_WM_WP_NAME
            = KeysystemEnum.WM_WP_NAME.name().toLowerCase();

    // don't include FIELDNAME as already in IndexConstants, use shorter names
    public static final String ISIN = KeysystemEnum.ISIN.name().toLowerCase();

    public static final String WKN = KeysystemEnum.WKN.name().toLowerCase();

    public static final String VWDCODE = KeysystemEnum.VWDCODE.name().toLowerCase();

    public static final String INFRONT_ID = KeysystemEnum.INFRONT_ID.name().toLowerCase();

    /**
     * Field value for e.g., {@link #FIELDNAME_IS_MMXML_BLACKLIST}
     * cannot be searched as field
     */
    public static final String FIELD_VALUE_BOOLEAN_FALSE = "n";

    public static final String FIELD_VALUE_BOOLEAN_TRUE = "y";

    /**
     * Instrument symbols whose values can be searched as field.
     */
    public static final Map<KeysystemEnum, String> INSTRUMENT_SYMBOLS;

    public static final List<String> KEYWORD_FIELDS = Collections.unmodifiableList(
            Arrays.asList(FIELDNAME_COUNTRY, FIELDNAME_CURRENCY, FIELDNAME_ENTITLEMENT_VWD,
                    FIELDNAME_HOME_EXCHANGE, FIELDNAME_MARKET, FIELDNAME_TYPE, FIELDNAME_WMTYPE)
    );

    public static final List<String> NAME_FIELDS = Collections.unmodifiableList(
            Arrays.asList(FIELDNAME_NAME, FIELDNAME_NAMES, FIELDNAME_WM_WP_NAME,
                    FIELDNAME_WM_WP_NAME_KURZ, FIELDNAME_WM_WP_NAME_LANG, FIELDNAME_WM_WP_NAME_ZUSATZ)
    );

    public static final Set<String> MARKET_BLACKLIST_MM_XML = new HashSet<>(Arrays.asList(
            "JCF", "JCFON", "WEIGHT", "KENNZ", "CITIF",
            "DTB", "BLB", "CENTRO", "OBUETR", "OBUFFM", "OBUS"));

    /**
     * Quote symbols whose values can be searched as field.
     */
    public static final Map<KeysystemEnum, String> QUOTE_SYMBOLS;

    static {
        QUOTE_SYMBOLS = initMap(
                KeysystemEnum.MMWKN,
                KeysystemEnum.VWDCODE,
                KeysystemEnum.VWDFEED,
                KeysystemEnum.VWDSYMBOL,
                KeysystemEnum.WM_WP_NAME_KURZ,
                KeysystemEnum.WM_WP_NAME_LANG,
                KeysystemEnum.WM_WP_NAME_ZUSATZ,
                KeysystemEnum.WM_WP_NAME,
                KeysystemEnum.WM_WPK,
                KeysystemEnum.BIS_KEY, // istar-135
                KeysystemEnum.VWDFEED_SECONDARY, // istar-534
                KeysystemEnum.INFRONT_ID // DM-484
        );

        INSTRUMENT_SYMBOLS = initMap(
                KeysystemEnum.ISIN,
                KeysystemEnum.WKN,
                KeysystemEnum.TICKER,
                KeysystemEnum.VALOR,
                KeysystemEnum.VALORSYMBOL,
                KeysystemEnum.SEDOL,
                KeysystemEnum.CUSIP,
                KeysystemEnum.FWW,
                KeysystemEnum.EUREXTICKER);
    }

    private static Map<KeysystemEnum, String> initMap(KeysystemEnum... kses) {
        final Map<KeysystemEnum, String> map = new EnumMap<>(KeysystemEnum.class);

        for (final KeysystemEnum kse : kses) {
            map.put(kse, kse.name().toLowerCase());
        }

        return Collections.unmodifiableMap(map);
    }
}
