/*
 * ContenFlags.java
 *
 * Created on 10.10.11 11:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

/**
 * @author zzhao
 */
public interface ContentFlags {
    public enum Flag {
        Null,//0
        SsatFndReport,//(1),
        StockselectionFndReport,//(2),
        VRPIF,//(3),
        Screener,//(4),
        Factset,//(5),
        EstimatesReuters,//(6),
        Convensys,//(7),
        VwdbenlFundamentalData,//(8),
        FunddataMorningstar,//(9),
        FunddataVwdBeNl,//(10),
        HistoricaTimeseriesData,//(11),
        WntUnderlying,//(12),
        CerUnderlying,//(13),
        OptUnderlying,//(14),
        FutUnderlying,//(15),
        Edg,//(16),
        Kursfortschreibung,//(17)
        CerDzbank,//(18),
        WntDzbank,//(19),
        CerUnderlyingDzbank,//(20),
        WntUnderlyingDzbank,//(21),
        CerWgzbank,//(22),
        CerUnderlyingWgzbank,//(23),
        PibDz,//(24),
        StockselectionCerReport, //(25)
        LeverageProductUnderlyingDzbank, //(26)
        OfferteDzbank, //(27)
        IlSole24OreAmf, //(28)
        TopproduktDzbank, //(29)
        KapitalmarktfavoritDzbank, //(30)
        OfferteUnderlyingDzbank, //(31)
        DzMarginDialogRequired, // (32)
        LMEComposite, // (33)
        IndexWithConstituents, // (34)
        ResearchDzHM1, // (35) ISTAR-464
        ResearchDzHM2, // (36) ISTAR-464
        ResearchDzHM3, // (37) ISTAR-464
        ResearchDzFP4, // (38) ISTAR-464
        LMEWarehouse,  // (39) DP-2223, ISTAR-699
        ResearchLBBW,  // (40) T-48474
        HasLEIEquity,  // (41) für Aktien gesetzt, wenn mindestens eine _andere_ Aktie mit demselben LEI existiert.
                       //      für Anleihen und Issuer Ratings gesetzt, wenn mindestens eine Aktie mit demselben
                       //      LEI existiert
        HasLEIBonds,   // (42) für Aktien, Anleihen und Issuer Ratings gesetzt, wenn mindestens eine Anleihe mit
                       //      demselben LEI existiert
        HasLEIIssuerRatings,  // (43)
        ;

        public boolean isValid() {
            return this != Null;
        }
    }

    boolean hasFlag(Flag f);

    boolean isConvensys();

    boolean isEstimatesReuters();

    boolean isScreener();

    boolean isEdg();

    boolean isStockselectionFndReport();

    boolean isStockselectionCerReport();

    boolean isSsatFndReport();

    boolean isFactset();

    boolean isVwdbenlFundamentalData();

    boolean isFunddataMorningstar();

    boolean isFunddataVwdBeNl();

    boolean isCerUnderlying();

    boolean isWntUnderlying();

    boolean isCerUnderlyingDzbank();

    boolean isCerUnderlyingWgzbank();

    boolean isWntUnderlyingDzbank();

    boolean isCerDzbank();

    boolean isCerWgzbank();

    boolean isWntDzbank();

    boolean isOptUnderlying();

    boolean isFutUnderlying();

    boolean isVRPIF();

    boolean isHistoricaTimeseriesData();

    boolean isPibDz();

    boolean isLeverageProductUnderlyingDzbank();

    boolean isOfferteDzbank();

    boolean isIlSole24OreAmf();

    boolean isTopproduktDzbank();

    boolean isKapitalmarktfavoritDzbank();

    boolean isOfferteUnderlyingDzbank();

    boolean isDzMarginDialogRequired();

    boolean isIndexWithConstituents();

    boolean isLMEComposite();

    boolean isResearchDzHM1();

    boolean isResearchDzHM2();

    boolean isResearchDzHM3();

    boolean isResearchDzFP4();

    boolean isKursfortschreibung();

    boolean isLMEWarehouse();

    boolean isResearchLBBW();

    boolean hasLEIEquity();

    boolean hasLEIBonds();

    boolean hasLEIIssuerRatings();

}
