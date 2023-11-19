/*
 * KeysystemEnum.java
 *
 * Created on 17.09.2004 09:30:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum KeysystemEnum {
    // ======================================================================================
    // whenever you add items to this enum
    // you have to update all clients that receive serialized instruments _before_ you
    // can actually include those enums in the instrument index; otherwise, the deserialization will fail.
    // enum serialization cannot be customized, see
    // http://docs.oracle.com/javase/7/docs/platform/serialization/spec/serial-arch.html#6469
    // for details
    // ======================================================================================

    // General Purpose
    DUMMY,
    ISO(1),
    MM,
    WM,

    // Instruments
    ISIN,
    WKN,
    VALOR,
    VALORSYMBOL(-1, 169),
    SEDOL(-1, 174),
    CUSIP(-1, 175),
    DEFAULTMMSYMBOL,

    // Quotes
    WM_TICKER(-1, 278), // ADF_EDV_Kuerzel -- vermutlich GD625
    VWDFEED(2), // 1.710000.ETR
    VWDCODE, // 710000.ETR
    VWDSYMBOL, // 710000
    WM_WP_NAME_KURZ,
    WM_WP_NAME_LANG,
    WM_WP_NAME_ZUSATZ,
    WM_WP_NAME,
    WM_WPK,

    MMWKN(7),

    WM_GD195_ID,
    WM_GD195_NAME,
    DP_TEAM(13),

    FWW,
    GATRIXX,

    TICKER(-1, 278),

    GD664,

    EUREXTICKER(-1, 41),

    MDP_SYNONYM_NAME,
    US_TICKER(-1, 171),

    BIS_KEY,

    OEWKN(-1, 123), // austrian wkn
    MMNAME(9),

    GD260, // WM WP_NAME_KURZ

    WM_GD198B_ID,
    WM_GD198C_ID,

    PM_INSTRUMENT_NAME(-1, 2778),
    PM_INSTRUMENT_NAME_FREE(-1, 2779),

    NAMESUFFIX_QUOTE, // ISTAR-458
    VWDFEED_SECONDARY, // ISTAR-534
    MIC,
    OPERATING_MIC,

    INFRONT_ID(3400);

    /** id in mdp keystem table */
    private final int keysystemId;

    /** id in mdp code table */
    private final int codeId;

    KeysystemEnum() {
        this(-1, -1);
    }

    KeysystemEnum(int keysystemId) {
        this(keysystemId, -1);
    }

    KeysystemEnum(int keysystemId, int codeId) {
        this.keysystemId = keysystemId;
        this.codeId = codeId;
    }

    public int getId() {
        return ordinal();
    }

    public static KeysystemEnum valueOf(int id) {
        try {
            return values()[id];
        }
        catch (Exception e) {
            throw new IllegalArgumentException("unknown keysystem id: " + id);
        }
    }
}