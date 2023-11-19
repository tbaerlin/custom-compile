/*
 * QuoteDef.java
 *
 * Created on 08.02.13 11:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author zzhao
 */
public enum QuoteDef {
    OCHLKVC(1),
    OCHLSVOI(2),
    FUND(3),
    Geld_Brief(4),
    MSCI_Indexe(5),
    Midprice(6),
    Tullett_Bid_Ask_Vola(7),
    Luxembourg(8),
    Einzelwert(12),
    Qualifizierter_Einzelwert(13),
    Crossrate_calculated(15),
    Fixing(16),
    Waehrung(17),
    Put_Call_Ratios(19),
    LME_Lagerbestaende(21),
    OCHLKVC_und_Rendite_Rendexp(22),
    OCHLKVC_und_Rendite_Kursexp(123),
    FAZ(223),
    OCHLKVC_dt_Praesenzboersen(322),
    MSCI_Indexe_EUR(423),
    externe_Lieferung_Performance(522),
    XETRA_INAV_Indices(722),
    Pfandbriefkurve(823),
    Manuelle_Kursfortschreibung(922),
    Endlosfutures_ExFeed(1024),
    Settlement(1122),
    OCHLKVC_und_Rendite_Geld(1322),
    Geld_Brief_Bezahlt(1423),
    Volatility(1522),
    MarketIndicatorIndex(1622),
    Bezahlt(1722),
    Volume_Information_Octopus(1822),;

    private final int id;

    private QuoteDef(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static QuoteDef fromQuote(Quote quote) {
        return fromId(quote.getQuotedef());
    }

    public static QuoteDef fromId(int id) {
        for (QuoteDef quoteDef : values()) {
            if (quoteDef.id == id) {
                return quoteDef;
            }
        }
        throw new IllegalArgumentException("no quote def found for id: " + id);
    }
}
