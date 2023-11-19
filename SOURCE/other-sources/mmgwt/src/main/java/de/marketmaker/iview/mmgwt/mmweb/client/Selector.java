/*
 * Selector.java
 *
 * Created on 06.10.2008 15:47:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppProfile;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NonNLS
public enum Selector {

    VWL(13, Aspect.FUNCTION),
    FUNDDATA_MORNINGSTAR_DE(196, Aspect.FUNCTION),
    SCREENER(210, Aspect.FUNCTION),
    CONVENSYS_I(211, Aspect.FUNCTION),
    CONVENSYS_II(212, Aspect.FUNCTION),
    FERI(213, Aspect.FUNCTION),
    FITCH_CDS(214, Aspect.PRICE),
    DZBANK_USER_MESSAGES(223, Aspect.NEWS),
    FACTSET(244, Aspect.PRICE),
    COMEX(295, Aspect.PAGE),
    IBOXX_INDICES(313, Aspect.PRICE),
    VOLA_INDIZES(338, Aspect.PRICE),
    XRATE(347, Aspect.PRICE),
    CBOT_OPTIONS(393, Aspect.PRICE),
    EUREX_FUTURES(394, Aspect.PRICE),
    EUREX_OPTIONS(395, Aspect.PRICE),
    RATING_FITCH(396, Aspect.FUNCTION),
    RATING_MOODYS(414, Aspect.PRICE),
    NEWS_SPORT(487, Aspect.NEWS),
    EZB(508, Aspect.PRICE),
    DZBANK_WEB_INVESTOR(2003, Aspect.PRODUCT),
    DZBANK_WEB_INFORMER(2004, Aspect.PRODUCT),
    RATING_MORNINGSTAR(2007, Aspect.FUNCTION),
    UNION_PIFS(2008, Aspect.FUNCTION),
    ATTRAX(2009, Aspect.FUNCTION),
    PAGES_DZBANK(2010, Aspect.FUNCTION),
    VALOR_DATA_BROWSER(2016, Aspect.FUNCTION),
    NEWS_DPA(2018, Aspect.FUNCTION),
    PLATOW(2020, Aspect.FUNCTION),
    NEWS_REUTERS(2022, Aspect.FUNCTION),
    SMARTHOUSE_ANALYSES(2023, Aspect.FUNCTION),
    PROFIT_ESTIMATES_AND_PER(2024, Aspect.FUNCTION),
    TIMES_AND_SALES(2025, Aspect.FUNCTION),
    HISTORICAL_PERFORMANCE(2029, Aspect.FUNCTION),
    FINDER_ANALYSIS(2034, Aspect.FUNCTION),
    PAGES_VWD(2038, Aspect.PAGE),
    MY_WORKSPACE_ADVANCED(2047, Aspect.FUNCTION),
    WARRANT_CALCULATOR(2048, Aspect.FUNCTION),
    BOND_CALCULATOR(2049, Aspect.FUNCTION),
    FINDER_EUREX(2059, Aspect.FUNCTION),
    YIELD_STRUCTURE(2063, Aspect.FUNCTION),
    WEB_XL(2065, Aspect.FUNCTION),
    NEWS_NO_ADS(2066, Aspect.FUNCTION),
    DZ_CER_IPO_MATRIX(2083, Aspect.FUNCTION),
    TICKER(2086, Aspect.FUNCTION),
    SEND_NEWS_MAIL(2095, Aspect.FUNCTION),
    DZ_BANK_USER(2075, Aspect.FUNCTION),
    WGZ_BANK_USER(2076, Aspect.FUNCTION),
    DZ_BANK_SPECIAL_USER(2077, Aspect.FUNCTION),
    KWT_NO_MARKETDATA(2090, Aspect.PRODUCT),
    FLEX_CHART(2097, Aspect.FUNCTION),
    INSTRUMENT_SEARCH_SUGGESTION(2109, Aspect.FUNCTION),
    EDG_RATING(2119, Aspect.FUNCTION),
    CSV_EXPORT(2125, Aspect.FUNCTION),
    DZB_KWT_FUNCTION(2129, Aspect.FUNCTION),
    FUNDDATA_FWW(2130, Aspect.FUNCTION),
    DZB_DEUTSCHE_EINGEBER(2150, Aspect.FUNCTION),
    DZBANK_WEB_INVESTOR_PUSH(2152, Aspect.PRODUCT),
    CAMPAIGN(2186, Aspect.FUNCTION),
    STATISTICS(2188, Aspect.FUNCTION),
    SEARCH_VWD_PAGES(2205, Aspect.FUNCTION),
    VWD_DOCUMENT_MANAGER_PIB(2206, Aspect.PRODUCT),
    PRODUCT_HIGHLIGHTING(2217, Aspect.FUNCTION),
    GISPORTAL(2227, Aspect.FUNCTION),
    ANY_VWD_TERMINAL_PROFILE(2239, Aspect.FUNCTION),
    LIVEFINDER(2251, Aspect.FUNCTION),    // deprecated
    PRODUCTALTERNATIVES(2253, Aspect.FUNCTION),
    MSG_OF_THE_DAY_EDIT(2263, Aspect.FUNCTION),
    ENITEO_CONTENT(2264, Aspect.FUNCTION),
    BEST_OF_LIST(2265, Aspect.FUNCTION),
    MOST_ACTIVE(2266, Aspect.FUNCTION),
    NEW_PRODUCTS_TRADED(2267, Aspect.FUNCTION),
    PRODUCT_WITH_PIB(2268, Aspect.FUNCTION),
    DZ_KAPITALMARKT(2269, Aspect.FUNCTION),
    DZ_TEASER(2279, Aspect.FUNCTION),
    DZ_TEASER_ADMIN(2280, Aspect.FUNCTION),
    KIID_ARCHIVE(2281, Aspect.FUNCTION),
    EXCEL_WEB_QUERY_EXPORT(2287, Aspect.FUNCTION),
    DOCM_EDIT_ADM(2313, Aspect.FUNCTION), // document manager: admin edit for text section allowed
    DOCM_EDIT_USER(2314, Aspect.FUNCTION), // document manager: user edit for text section allowed
    DOCM_CERT_CONF(2315, Aspect.FUNCTION), // document manager: only download of certificate pibs from issuer with conformity
    DOCM_CERT_ALL(2316, Aspect.FUNCTION), // document manager: download of (all) certificate pibs from issuer
    DZ_WERTENTICKLUNGSRECHNER(2397, Aspect.FUNCTION),
    DZ_RESEARCH(2398, Aspect.FUNCTION),
    DZHM1(2403, Aspect.FUNCTION), // DZ: grant access to documents from HM1 package (there is a matching content flag)
    DZHM2(2404, Aspect.FUNCTION),
    DZHM3(2405, Aspect.FUNCTION),
    DZFP4(2406, Aspect.FUNCTION),
    DOCM_VENDOR_PIB(2468, Aspect.FUNCTION), // document manager: upload & management of vendor-specific PIBs allowed
    VWL_EMERGING_MARKETS(3001, Aspect.PRICE),
    THOMSONREUTERS_ESTIMATES_DZBANK(3006, Aspect.PRICE),
    RATING_FERI(3007, Aspect.PRICE),
    STOCKSELECTION_FUND_REPORTS_DE(3020, Aspect.PRICE),
    FWW_FUND_REPORTS(3022, Aspect.PRICE),
    STOCKSELECTION_CERTIFICATE_REPORTS(3023, Aspect.PRICE),
    DZBANK_CERTIFICATE_REPORTS(3026, Aspect.PRICE),
    IRS_COMPOSITES_FXVWD(3043, Aspect.PRICE),
    FUNDINFO_REPORTS(3044, Aspect.PRICE),
    IL_SOLE_COMPANY_DATA_PDF(3364, Aspect.PRICE),
    IL_SOLE_COMPANY_DATA_XML(3365, Aspect.PRICE),
    SOFTWARESYSTEMS_AT_REPORTS(3068, Aspect.PRICE),
    FUNDDATA_VWD_BENL(3069, Aspect.PRICE),
    DPA_AFX_ANALYSES(3082, Aspect.NEWS),
    COMPANYDATA_VWD_BENL(3083, Aspect.FUNCTION),
    FUNDDATA_MORNINGSTAR_CH(3085, Aspect.PRICE),
    FUNDDATA_MORNINGSTAR_IT(3086, Aspect.PRICE),
    FUNDDATA_MORNINGSTAR_AT(3087, Aspect.PRICE),
    STOCKSELECTION_FUND_REPORTS_CH(3088, Aspect.PRICE),
    STOCKSELECTION_FUND_REPORTS_IT(3089, Aspect.PRICE),
    STOCKSELECTION_FUND_REPORTS_AT(3090, Aspect.PRICE),
    RATING_SuP(3455, Aspect.PRICE),
    SSAT_FUND_DATA(3456, Aspect.PRICE),
    FIDA_FUND_DATA(4136, Aspect.PRICE),
    FIDA_FUND_RATING(4137, Aspect.PRICE),
    FIDA_FUND_REPORTS_KIID(4138, Aspect.PRICE),
    FUNDINFO_FACTSHEET(4140, Aspect.PRICE),
    VWD_FUND_DATA(4410, Aspect.PRICE),
    VWD_FUND_REPORTS(4411, Aspect.PRICE),
    VWD_FUND_REPORTS_KIID(4412, Aspect.PRICE),
    AS_MARKETDATA("Server.MarketData", Aspect.AS_FEATURE),
    AS_INHOUSE("Server.Inhouse", Aspect.AS_FEATURE),
    AS_CREATE_PROSPECT("Server.CreateInteressent", Aspect.AS_FEATURE),
    AS_CREATE_PERSON("Server.CreatePerson", Aspect.AS_FEATURE),
    AS_DOCMAN("Server.DocMan", Aspect.AS_FEATURE),
    AS_ORDERING("Server.Ordering", Aspect.AS_FEATURE),
    AS_ACTIVITIES("Server.Activities", Aspect.AS_FEATURE),
    AS_CREATE_INVESTOR("Server.CreateInhaber", Aspect.AS_FEATURE),
    AS_DMS("Misc.DocMgmtSystem", Aspect.AS_FEATURE),
    RATING_MORNINGSTAR_UNION_FND(4443, Aspect.PRICE),
    AS_ORDERING_VIA_ACTIVITIES("Server.WebLoginBroking", Aspect.AS_FEATURE),
    AS_POSTBOX("Server.Postbox", Aspect.AS_FEATURE);

    private final int id;

    private final String descriptor;

    private final Aspect aspect;

    private Selector(int id, Aspect aspect) {
        this.id = id;
        this.aspect = aspect;
        this.descriptor = this.name().toUpperCase();
    }

    private Selector(String descriptor, Aspect aspect) {
        this.id = -1;
        this.aspect = aspect;
        this.descriptor = descriptor.toUpperCase();
    }

    public int getId() {
        return id;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public boolean isAllowed() {
        final String id = this.id > -1
                ? Integer.toString(this.id)
                : this.getDescriptor();
        switch (this.aspect) {
            case PRODUCT:
                return getAppProfile().isProductAllowed(id);
            case NEWS:
                return getAppProfile().isNewsAllowed(id);
            case PAGE:
                return getAppProfile().isPageAllowed(id);
            case FUNCTION:
            case PRICE: // HACK since vwd encodes many functions as PRICE, see also UserServiceImpl#toAppProfile
                return getAppProfile().isFunctionAllowed(id);
            case AS_FEATURE:
                return getAppProfile().isFunctionAllowed(id);
        }

        return false;
    }

    protected AppProfile getAppProfile() {
        return SessionData.INSTANCE.getUser().getAppProfile();
    }

    public String toString() {
        return name() + "[" + this.id + "/" + this.aspect + "]";
    }

    private static enum Aspect {
        PRICE, NEWS, FUNCTION, AS_FEATURE, PRODUCT, PAGE
    }

    public static boolean isAllAllowed(Selector... selectors) {
        for (Selector selector : selectors) {
            if (!selector.isAllowed()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllowed(String selector) {
        try {
            if (selector.startsWith("!")) {
                return Selector.valueOf(selector.substring(1)).isAllowed();
            }
            else {
                return Selector.valueOf(selector).isAllowed();
            }
        } catch (IllegalArgumentException e) {
            DebugUtil.logToServer("ERROR invalid selector '" + selector + "'");
            return false;
        }
    }

    public static boolean isReportsAllowed() {
        return STOCKSELECTION_CERTIFICATE_REPORTS.isAllowed() || DZBANK_CERTIFICATE_REPORTS.isAllowed();
    }

    public static boolean isMorningstarFunddataAllowed() {
        return FUNDDATA_MORNINGSTAR_AT.isAllowed() || FUNDDATA_MORNINGSTAR_CH.isAllowed()
                || FUNDDATA_MORNINGSTAR_DE.isAllowed() || FUNDDATA_MORNINGSTAR_IT.isAllowed();
    }

    public static boolean isFunddataAllowed() {
        return isMorningstarFunddataAllowed()
                || FUNDDATA_FWW.isAllowed()
                || VWD_FUND_DATA.isAllowed()
                || SSAT_FUND_DATA.isAllowed()
                || FIDA_FUND_DATA.isAllowed();
    }

    public static boolean isStandardFndAllowed() {
        return isKiidAllowed();
    }

    public static boolean isKiidAllowed() {
        return Selector.SOFTWARESYSTEMS_AT_REPORTS.isAllowed()
                || Selector.FWW_FUND_REPORTS.isAllowed()
                || Selector.STOCKSELECTION_FUND_REPORTS_DE.isAllowed()
                || Selector.STOCKSELECTION_FUND_REPORTS_CH.isAllowed()
                || Selector.STOCKSELECTION_FUND_REPORTS_AT.isAllowed()
                || Selector.STOCKSELECTION_FUND_REPORTS_IT.isAllowed()
                || Selector.FIDA_FUND_REPORTS_KIID.isAllowed()
                || Selector.FUNDINFO_REPORTS.isAllowed()
                || Selector.VWD_FUND_REPORTS_KIID.isAllowed();
    }

    public static boolean isDZResearch() {
        return DZ_RESEARCH.isAllowed() && (DZHM1.isAllowed() || DZHM2.isAllowed() || DZHM3.isAllowed() || DZFP4.isAllowed());
    }
    public static boolean isDzProfitEstimate() {
        return PROFIT_ESTIMATES_AND_PER.isAllowed() && (DZ_BANK_USER.isAllowed() || WGZ_BANK_USER.isAllowed() || DZ_BANK_SPECIAL_USER.isAllowed());
    }
}
