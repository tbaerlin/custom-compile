/*
 * ContentFlagsEnum.java
 *
 * Created on 18.11.11 10:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import de.marketmaker.iview.dmxml.QuoteData;

/**
 * @author oflege
 */
public enum ContentFlagsEnum {
    Null,
    SsatFndReport,
    StockselectionReport,
    VRPIF,
    Screener,
    Factset,
    EstimatesReuters,
    Convensys,
    VwdbenlFundamentalData,
    FunddataMorningstar,
    FunddataVwdBeNl,
    HistoricaTimeseriesData,
    WntUnderlying,
    CerUnderlying,
    OptUnderlying,
    FutUnderlying,
    Edg,
    IndexConstituent,
    CerDzbank,
    WntDzbank,
    CerUnderlyingDzbank,
    WntUnderlyingDzbank,
    CerWgzbank,
    CerUnderlyingWgzbank,
    PibDz,
    StockselectionCerReport,
    LeverageProductUnderlyingDzbank,
    OfferteDzbank,             //(27)
    IlSole24OreAmf,            //(28)
    TopproduktDzbank,          //(29)
    KapitalmarktfavoritDzbank, //(30)
    OfferteUnderlyingDzbank,   //(31)
    DzMarginDialogRequired,    //(32)
    LMEComposite,              //(33)
    IndexWithConstituents,     //(34)
    ResearchDzHM1,             //(35)
    ResearchDzHM2,             //(36)
    ResearchDzHM3,             //(37)
    ResearchDzFP4              //(38)
    ;

    private static final int[] base64Values = new int['z' + 1];

    static {
        int n = 0;
        for (char c = 'A'; c <= 'Z'; c++) base64Values[c] = n++;
        for (char c = 'a'; c <= 'z'; c++) base64Values[c] = n++;
        for (char c = '0'; c <= '9'; c++) base64Values[c] = n++;
        base64Values['+'] = n++;
        base64Values['/'] = n;
    }

    public boolean isAvailableFor(QuoteData qd) {
        return (qd != null) && isPresent(qd.getContentFlags(), this);
    }

    public static boolean isPresent(String flags, ContentFlagsEnum cf) {
        // each group of 3 bytes is encoded in 4 chars, so lets first find the position of the
        // char group that contains the flag bytes we are looking for:
        final int pos = (cf.ordinal() / (8 * 3)) * 4;
        if (flags == null || flags.length() < pos + 3) {
            return false;
        }
        final int decoded = (base64Values[flags.charAt(pos)] << 18)
                + (base64Values[flags.charAt(pos + 1)] << 12)
                + (base64Values[flags.charAt(pos + 2)] << 6)
                + (base64Values[flags.charAt(pos + 3)]);
        // whether we are looking for the 0th, 1st, or 2nd byte in the decoded byte triplet
        final int byteNum = (cf.ordinal() % 24) / 8;
        // shift so that flags are in bits 0..7
        final int byteFlags = decoded >> ((2 - byteNum) * 8);
        return ((byteFlags & (1 << (cf.ordinal() % 8))) != 0);
    }
}
