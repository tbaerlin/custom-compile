/*
 * MenuBuilder.java
 *
 * Created on 26.02.2009 10:37:03
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.apo.ApoFundPricelistsController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.KwtCustomListController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageSearch;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FlashCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Initializer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.SearchWorkspaceWithToolsMenu;
import de.marketmaker.iview.tools.i18n.NonNLS;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NonNLS
public class MenuBuilder {
    protected final MenuModel model = new MenuModel();

    protected final AbstractMainController mc;

    public static final String DASHBOARD_ID = "D";

    public static final String EXPLORER_ID = "K";

    public static final String SEARCH_ID = "E";

    public MenuBuilder() {
        this.mc = AbstractMainController.INSTANCE;
        init();
    }

    public MenuModel getModel() {
        return model;
    }

    final Initializer<MenuModel.Item> pagesInitializer = () -> {
        MenuModel.Item itemPages;
        MenuModel.Item itemPagesVwd;
        if (PAGES_DZBANK.isAllowed()) {
            itemPages = createMenu("P", I18n.I.pages())
                    .add(PAGES_DZBANK, createItem("P_D", "DZ BANK", "mm-icon-dzbank")
                                    .add(createItem("P_D_U", "P_D/A100", I18n.I.overview(), "mm-icon-dzbank"))
                                    .add(createItem("P_D_Q", "P_D/A200", I18n.I.quickcodes(), "mm-icon-dzbank"))
                    )
                    .add(PAGES_VWD, itemPagesVwd = createMenu("P_V", "vwd"))
            ;
        }
        else if (PAGES_VWD.isAllowed()) {
            itemPages = itemPagesVwd = createMenu("P_V", I18n.I.pages());
        }
        else {
            return null;
        }
        /* The following items are dummy items to provide a location for
         * vwd page searches. Note that all of them are hidden. */
        itemPagesVwd.add(createItem("P_V_U", "P_V/1", I18n.I.overview(), "mm-icon-vwdpage").hide())
                .add(SEARCH_VWD_PAGES, createItem(VwdPageSearch.TEXT_SEARCH_KEY, I18n.I.vwdPageSearchResults()).hide())
                .add(SEARCH_VWD_PAGES, createItem(VwdPageSearch.SYMBOL_SEARCH_KEY, I18n.I.vwdPageSearchResults()).hide())
                .add(SEARCH_VWD_PAGES, createItem(VwdPageSearch.POINTER_SEARCH_KEY, I18n.I.vwdPageSearchResults()).hide());
        itemPages.addIfAny(new Selector[]{DZBANK_WEB_INVESTOR, DZBANK_WEB_INFORMER}, createItem("P_DH", I18n.I.reutersPagesAlternatives()));
        return itemPages;
    };

    void init() {
        if (isDz()) {
            final MenuModel.Item dzMenu = createMenu("DZ", "DZ BANK")
                    .add(PAGES_DZBANK, createItem("DZ_UB", "P_D/A100", I18n.I.overview(), "mm-icon-dzbank"))
                    .add(PAGES_DZBANK, createItem("DZ_KL", "P_D/A200", I18n.I.quickcodes(), "mm-icon-dzbank"))
                    .add(createItem("DZ_NE", I18n.I.certificateIPO(), "mm-icon-dzbank"))
                    .add(createItem("DZ_Z", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_GUARANTEE, I18n.I.dzBankCertificates(), null)
                            .add(createItem("DZ_Z_AA", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE, I18n.I.reverseConvertible(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_BA", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_BASKET, I18n.I.certBasket(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_BO", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_BONUS, I18n.I.certBonus(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_D", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_DISCOUNT, I18n.I.certDiscount(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_F", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_FACTOR, I18n.I.certFactor(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_E", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_EXPRESS, I18n.I.certExpress(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_G", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_GUARANTEE, I18n.I.certGuarantee(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_I", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_INDEX, I18n.I.certIndex(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_K", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.KNOCK, I18n.I.certKnockout(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_O", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_OUTPERFORMANCE, I18n.I.certOutperformance(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_S", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_SPRINTER, I18n.I.certSprinter(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_RA", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_REVERSE_CONVERTIBLE_COM, I18n.I.reverseConvertibleCommodity(), "mm-icon-dzbank"))
                            .add(createItem("DZ_Z_M", "DZ_Z/M_LF_CER/DZ BANK/" + CertificateTypeEnum.CERT_OTHER, I18n.I.certOthers(), "mm-icon-dzbank"))
                    )
                    .add(createItem("DZ_PUB", I18n.I.publications(), "mm-icon-dzbank"))
                    .add(Selector.isDZResearch(), createItem("DZ_LF_RES1", "DZ_LF_RES", I18n.I.research(), "mm-icon-dzbank"))
                    .add(createItem("DZ_S", I18n.I.yourServiceTeam(), "mm-icon-dzbank"))
                    .add(DZ_TEASER_ADMIN.isAllowed(), createItem("DZ_TAT", I18n.I.teaserAdminTool(), "mm-icon-dzbank"));
            if(SessionData.isAsDesign()) {
                tryAddDzbankWgzbankStatistics(dzMenu);
            }
            this.model.add(dzMenu);
        }

        if (isWgz()) {
            final MenuModel.Item wgzMenu = createMenu("WGZ", "WGZ BANK")
                    .add(createItem("WZ_NE", I18n.I.certificateIPO(), "mm-icon-dzbank"))
                    .add(createItem("WZ_C", I18n.I.certificateOverview(), "mm-icon-dzbank"))
                    .add(Selector.isDZResearch(), createItem("WZ_LF_RES1", "DZ_LF_RES", I18n.I.research(), "mm-icon-dzbank"))
                    .add(createItem("WZ_S", I18n.I.yourServiceTeam(), "mm-icon-dzbank"));
            if(SessionData.isAsDesign()) {
                tryAddDzbankWgzbankStatistics(wgzMenu);
            }
            this.model.add(wgzMenu);
        }

        if (Customer.INSTANCE.isKwt() && DZB_KWT_FUNCTION.isAllowed() || KWT_NO_MARKETDATA.isAllowed()) {
            initKwtMenu();

            if (KWT_NO_MARKETDATA.isAllowed()) {
                return;
            }
        }

        if (Customer.INSTANCE.isOlb()) {
            this.model.add(createModuleMenu("OLB", "OLB", "module-olb")
                            .add(createItem("OLB_UB", I18n.I.overview()))
                            .add(createItem("OLB_AUS", I18n.I.notice()))
                            .add(!SessionData.isAsDesign(), createItem("OLB_PDFS", "OLB-Info"))
            );
        }

        if (SessionData.isWithMarketData()) {
            if (!SessionData.isAsDesign()) {
                this.model.add(createMenu("B", I18n.I.user())
                                .add(createItem("B_A", I18n.I.workspace()))
                                .add(createItem("B_P", I18n.I.portfolioSample()))
                                .add(createItem("B_W", I18n.I.watchlist()))
                                .add(createItem("B_L", I18n.I.limit()))
                                .add(createMenu("B_V", I18n.I.administration())
                                        .add(createItem("B_PW", I18n.I.changePassword()))
                                        .add(createItem("B_AC", I18n.I.settings()))
                                        .add(MessageOfTheDayEditor.isActive() && MSG_OF_THE_DAY_EDIT.isAllowed(), createItem("B_MOTD", I18n.I.motdMessageOfTheDay())))
                );
            }

            final MenuModel.Item itemMarkets;
            final MenuModel.Item portraitMK = createItem("P_MK", I18n.I.portrait()).hide();
            this.model.add(itemMarkets = createModuleMenu("M", I18n.I.markets(), "cg-market")
                            .add(createMenu("M_UB", getOverviewId(), I18n.I.overview())
                                            .addIfAny(new Selector[]{DZBANK_WEB_INFORMER, DZBANK_WEB_INVESTOR}, createItem("M_UB_A", I18n.I.notice(), "mm-icon-dzbank").appendSeparator())
                                            .add("de".equals(I18n.I.locale()), createItem("M_UB_BA", I18n.I.exchangeNotice(), "mm-icon-pdf").appendSeparator())
                                            .add(Customer.INSTANCE.isJsonMenuElementTrue("M_MKT_UB"), createItem("M_MKT_UB", I18n.I.markets()))
                                            .add(DZB_KWT_FUNCTION, createItem("M_UB_KWT", I18n.I.marketOverview()))
                                            .add(PAGES_VWD, createItem("M_UB_D", "P_V/109", I18n.I.germanyOverview(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_UB_DA", "P_V/100", I18n.I.germanyStockInfo(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_UB_CH", "P_V/111", I18n.I.swissOverview(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_UB_CHA", "P_V/3474", I18n.I.swissStockInfo(), "mm-icon-vwdpage"))
                                            .add(createItem("M_UB_WW", "M_UB_MPL/multilistid=overview_worldwide", I18n.I.overviewWorldWide(), null))
                                            .add(Customer.INSTANCE.isJsonMenuElementTrue("M_UB_IT_WW"), createItem("M_UB_IT_WW", "M_UB_MPL/multilistid=overview_italy_worldwide", I18n.I.overviewWorldWide(), null))
                                            .add(PAGES_VWD.isAllowed() && Selector.isDzProfitEstimate(), createItem("M_UB_I", "P_V/2560", I18n.I.indexKGVs(), "mm-icon-vwdpage"))
                                            .add(createItem("M_UB_K", I18n.I.pricelists()).appendSeparator())
                                            .add(SessionData.INSTANCE.isWithProfiDepots() ? createItem("M_UB_PD", I18n.I.profiDepots(), null).appendSeparator() : null)
                                            .add(HISTORICAL_PERFORMANCE, createItem("M_UB_HP", I18n.I.historicalPerformance()).appendSeparator())
                                            .add(createItem("M_UB_KP", I18n.I.list()).hide())
                                            .add(createItem("P_ZNS", I18n.I.interestRatePortrait()).hide())
                                            .add(createItem("P_GNS", I18n.I.bonusSharePortrait()).hide())
                                            .add(createItem("M_UB_MPL", I18n.I.multiPricelist()).hide())
                                            .add(createItem("P_IND", I18n.I.indexPortrait()).hide())
                                            .add(createItem("P_PE", I18n.I.kgvPortrait()).hide())
                                            .add(createItem("M_UB_C", I18n.I.exchangeCalendar()))
                                            .add(PAGES_VWD, createItem("M_UB_B", "P_V/911", I18n.I.exchangeHolidays(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_UB_Z", "P_V/29970", I18n.I.tradingTimes(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_UB_M", "P_V/29980", I18n.I.exchangePlaces(), "mm-icon-vwdpage"))
                                            .add((!VWL.isAllowed()), portraitMK)
                            )
                            .add(createItem(SessionData.isAsDesign() && SessionData.INSTANCE.isUserPropertyTrue("developer"), "M_DASH", "D_DBS/id=global", PermStr.DASHBOARD.value(), null))
                            .add(SessionData.isAsDesign(), this.pagesInitializer) // "P_D" and "P_V"
                            .add(createMenu("M_A_UB", I18n.I.stock())
                                            .add(createItem("M_LF_STK", I18n.I.stockSearch(), "mm-icon-finder"))
                                            .add(createItem("P_STK", I18n.I.portrait()).hide().appendSeparator())
                                            .add(createItem("M_A_KL", I18n.I.pricelists(), null).appendSeparator())
                                            .add(PAGES_VWD, createItem("M_A_S", "P_V/912", I18n.I.stockSplits(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_A_K1", "P_V/5013", I18n.I.asmCalendarSomething("DAX"), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD.isAllowed() && !DZBANK_WEB_INFORMER.isAllowed(), createItem("M_A_K2", "P_V/2594", I18n.I.asmCalendarSomething("E.Stoxx 50"), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD.isAllowed() && !DZBANK_WEB_INFORMER.isAllowed(), createItem("M_A_K3", "P_V/2595", I18n.I.asmCalendarSomething("Stoxx 50"), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD, createItem("M_A_N", "P_V/1120", I18n.I.ipoTrade(), "mm-icon-vwdpage"))
                            )
                            .add(createMenu("M_F_UB", I18n.I.funds())
                                            .add(createItem("M_LF_FND", I18n.I.fundsSearch(), "mm-icon-finder"))
                                            .add(createItem("P_FND", I18n.I.portrait()).hide().appendSeparator())
                                            .addIfJsonExists(ApoFundPricelistsController.JSON_KEY_PRICELISTS, createItem("M_F_APO", I18n.I.apoBrokerageFundCollection(), "mm-icon-apo").appendSeparator())
                                            .addIfAny(new Selector[]{WGZ_BANK_USER, DZ_BANK_USER}, createItem("M_F_V", I18n.I.compositeFunds(), "mm-icon-dzbank").appendSeparator())
                                            .addIfAny(new Selector[]{WGZ_BANK_USER, DZ_BANK_USER}, createItem("M_F_FF", "P_V/1500", I18n.I.foreignFund(), "mm-icon-vwdpage").appendSeparator())
                                            .add(ATTRAX, createItem("M_F_A", "ATTRAX FONDSADVISOR").appendSeparator())
                                            .add(PAGES_VWD.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_F_IT_IDX"),
                                                    createItem("M_F_IT_IDX", "P_V/2954", "Index Italy Funds", "mm-icon-vwdpage").appendSeparator())
                                            .add(FlashCheck.isFlashAvailable(), createItem("M_F_I", I18n.I.investmentCalculator()).appendSeparator())
                                            .add(createKwtMenuItem(I18n.I.funds()))
                            )
                            .add(createMenu("M_R_UB", I18n.I.bonds())
                                            .add(createItem("M_LF_BND", I18n.I.bondsSearch(), "mm-icon-finder"))
                                            .add((FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled())
                                                            && (Selector.RATING_FITCH.isAllowed() || Selector.RATING_MOODYS.isAllowed() || Selector.RATING_SuP.isAllowed()),
                                                    createItem("M_LF_BNDI", I18n.I.issuerRatingsSearch(), "mm-icon-finder"))
                                            .add(createItem("P_BND", I18n.I.portrait()).hide().appendSeparator())
                                            .add(createItem("M_R_Z", I18n.I.interestOverview()).appendSeparator())
                                            .add(PAGES_VWD.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_R_IT_IDX"), createItem("M_R_IT_IDX", "P_V/4810", I18n.I.indexBonds(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_R_O"), createItem("M_R_O", "P_V/102", I18n.I.overview(), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_R", "P_V/224", I18n.I.yields(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_R_I"), createItem("M_R_I", "P_V/1837", "Euribor", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_GD", "P_V/221", I18n.I.moneyMarketSomething("D"), "mm-icon-vwdpage"))
                                                    // NOTE: investor and informer are exclusive-or
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INFORMER}, createItem("M_R_GEUF", "P_V/1200", I18n.I.moneyMarketSomething("EU"), "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_GEUV", "P_V/222", I18n.I.moneyMarketSomething("EU"), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{IRS_COMPOSITES_FXVWD, PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_K", "P_V/219", I18n.I.capitalMarket(), "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_A1", "P_V/1010", I18n.I.depotXSomething("A", I18n.I.governmentBondsGermany()), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_A2", "P_V/1160", I18n.I.depotXSomething("A", I18n.I.jumboBonds()), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{DZBANK_WEB_INVESTOR, PAGES_DZBANK}, createItem("M_R_A3", "P_D/D500", I18n.I.depotXSomething("A", I18n.I.structuredProducts()), "mm-icon-dzbank"))
                                            .addIfAll(new Selector[]{PAGES_VWD, DZBANK_WEB_INVESTOR}, createItem("M_R_A4", "P_V/11750", I18n.I.depotXSomething("A", I18n.I.companyBonds()), "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PRODUCT_WITH_PIB, DZ_BANK_USER}, createItem("MM_BND_PIB", I18n.I.dzBankBondsWithPib(), "mm-icon-dzbank-pib"))
                                            .addIfAll(new Selector[]{DZBANK_WEB_INVESTOR, PAGES_DZBANK}, createItem("M_R_B1", "P_D/D400", I18n.I.depotXSomething("B", I18n.I.emergingMarkets()), "mm-icon-dzbank"))
                                            .add(PAGES_DZBANK, createItem("M_R_B2", "P_D/D340", I18n.I.depotXSomething("B", I18n.I.euroLand()), "mm-icon-dzbank"))
                                            .add(PAGES_DZBANK, createItem("M_R_B3", "P_D/D500", I18n.I.depotXSomething("B", I18n.I.structuredBonds()), "mm-icon-dzbank"))
                                            .add(PAGES_DZBANK, createItem("M_R_B4", "P_D/D430", I18n.I.depotXSomething("B", I18n.I.currencyBonds()), "mm-icon-dzbank"))
                                            .addIfAny(new Selector[]{WGZ_BANK_USER, DZ_BANK_USER, DZB_KWT_FUNCTION}, createItem("M_R_B5",
                                                    (Selector.PAGES_VWD.isAllowed() ? "P_V/11765" : "P_D/D120"),
                                                    I18n.I.depotXSomething("B", "DZ Euribor"),
                                                    (Selector.PAGES_VWD.isAllowed() ? "mm-icon-vwdpage" : "mm-icon-dzbank")))
                                            .add(PAGES_VWD.isAllowed() && (WGZ_BANK_USER.isAllowed() || DZ_BANK_USER.isAllowed() || DZB_KWT_FUNCTION.isAllowed()),
                                                    createItem("M_R_B6", "P_V/1010", I18n.I.depotXSomething("B", I18n.I.governmentBondsGermanyFloor()), "mm-icon-vwdpage").appendSeparator())
                                            .add(FlashCheck.isFlashAvailable() && BOND_CALCULATOR.isAllowed(), createItem("M_R_RR", I18n.I.bondCalculator()).appendSeparator())  // $NON-NLS$
                                            .add(createKwtMenuItem(I18n.I.bonds()))
                            )
                            .add(createMenu("M_Z_UB", I18n.I.certificates())
                                            .add(createItem("M_LF_CER", I18n.I.certificatesSearch(), "mm-icon-finder"))
                                            .addIfAll(new Selector[]{DZ_BANK_USER}, createItem("M_LF_LEV", I18n.I.certificatesLeverageSearch(), "mm-icon-finder"))
                                            .add(createItem("P_CER", I18n.I.portrait()).hide().appendSeparator())
                                            .add(ENITEO_CONTENT.isAllowed() && !FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled(), createItem("MM_CER_P", I18n.I.specialCerts(), "mm-icon-dzbank")) // also remove M_CER_P from MainControllerInitializer
                                            .addIfAll(new Selector[]{DZ_CER_IPO_MATRIX, DZ_BANK_USER}, createItem("M_Z_N", I18n.I.iposDZBank(), "mm-icon-dzbank"))
                                            .add(NEW_PRODUCTS_TRADED, createItem("MM_CER_NP", I18n.I.newCerProductsTraded(), "mm-icon-dzbank"))
                                            .addIfAll(new Selector[]{NEW_PRODUCTS_TRADED, DZ_BANK_USER}, createItem("MM_LEV_NP", I18n.I.newLevProductsTraded(), "mm-icon-dzbank"))
                                            .addIfAll(new Selector[]{PRODUCT_WITH_PIB, DZ_BANK_USER}, createItem("MM_CER_PIB", I18n.I.dzBankCertificatesWithPib(), "mm-icon-dzbank-pib"))
                                            .add(MOST_ACTIVE.isAllowed() && !FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled(), createItem("M_CER_PI", I18n.I.popularInstruments(), "mm-icon-dzbank")) // also remove M_CER_PI from MainControllerInitializer
                                            .add(DZ_KAPITALMARKT, createItem("M_CER_DZ_PUB", "DZ_PUB", "KapitalmarktFavoriten", "mm-icon-dzbank"))
                                            .add(DZB_KWT_FUNCTION, createItem("M_Z_KWT", I18n.I.certificateUniverse(), "mm-icon-raiba"))
                                            .add(ENITEO_CONTENT.isAllowed() && !FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled(), createItem("MM_CER_T", I18n.I.themenprodukte(), "mm-icon-dzbank")) // also remove M_CER_T from MainControllerInitializer
                                            .add(BEST_OF_LIST, createItem("M_CER_BEST", I18n.I.bestOfLists(), "mm-icon-dzbank").appendSeparator())
                                            .add(createItem("M_CER_COMP", I18n.I.certificatesComparison(), "mm-icon-comparison"))
                                            .add(createItem("M_Z_EM", I18n.I.issuerCategoryMatrix()))
                                            .add(FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled(), createItem("M_O_LM", I18n.I.issuerLeverageMatrix()))
                                            .add(PAGES_VWD, createItem("M_Z_FZ", "P_V/114", Customer.INSTANCE.isDzWgzApoKwt() ? I18n.I.foreignCertificates() : I18n.I.certificatesByIssuer(), "mm-icon-vwdpage"))
                                            .add(PAGES_VWD.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_Z_FZ_IT"), createItem("M_Z_FZ_IT", "P_V/7800", I18n.I.certificatesByIssuer(), "mm-icon-vwdpage"))
                                            .add(createKwtMenuItem(I18n.I.certificates()))
                                            .add(EDG_RATING, createLinkItem("M_Z_EE", "http://mmfweb.vwd.com/edg-info/edg-rating.html", I18n.I.edgExplanations(), null))
                            )
                            .add(createMenu("M_O_UB", I18n.I.warrants())
                                            .add(createItem("M_LF_WNT", I18n.I.warrantsSearch(), "mm-icon-finder"))
                                            .add(createItem("P_WNT", I18n.I.portrait()).hide().appendSeparator())
                                            .add(createItem("M_O_EM", I18n.I.issuerCategoryMatrix()).appendSeparator())
                                            .add(PAGES_DZBANK, createItem("M_O_A", "P_D/O104", I18n.I.stock(), "mm-icon-dzbank"))
                                            .add(PAGES_DZBANK, createItem("M_O_I", "P_D/O102", I18n.I.indices(), "mm-icon-dzbank"))
                                            .add(PAGES_DZBANK, createItem("M_O_T", "P_D/O107", I18n.I.turbos(), "mm-icon-dzbank"))
                                            .add(NEW_PRODUCTS_TRADED, createItem("M_O_NP", I18n.I.newProductsTraded(), "mm-icon-dzbank"))
                                            .add(PAGES_VWD, createItem("M_O_FS", "P_V/114", Customer.INSTANCE.isDzWgzApoKwt() ? I18n.I.foreignWarrants() : I18n.I.warrantsByIssuer(), "mm-icon-vwdpage").appendSeparator())
                                            .add(PAGES_VWD.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_O_FS_IT"), createItem("M_O_FS_IT", "P_V/7800", I18n.I.warrantsByIssuer(), "mm-icon-vwdpage").appendSeparator())
                                            .add(FlashCheck.isFlashAvailable() && WARRANT_CALCULATOR.isAllowed(), createItem("M_O_R", I18n.I.warrantCalculator()))
                            )
                            .add(createMenu("M_D_UB", I18n.I.currencies1())
                                            .add(createItem("P_CUR", I18n.I.portrait()).hide())
                                            .add(Customer.INSTANCE.isJsonMenuElementTrue("M_D_CC1"), createItem("M_D_CC1", "M_UB_KP/devisen-crossrates-chf", I18n.I.crossRatesSth("CHF"), null))
                                            .add(createItem("M_D_C", "M_UB_KP/devisen-crossrates-eur", I18n.I.crossRatesSth("EUR"), null))
                                            .add(EZB, createItem("M_D_EZBEUR", "M_UB_KP/devisen-ezbeuro/" + I18n.I.sdEzbEuroReferenzkurse(), I18n.I.somethingReferencePrices(I18n.I.europeanCentralBankAbbr()), null))
                                            .add(DZB_KWT_FUNCTION, createItem("M_D_CC", I18n.I.crossRatesSth("CHF")))
                                            .add(DZB_KWT_FUNCTION, createItem("M_D_CU", I18n.I.crossRatesSth("USD")).appendSeparator())
                                            .add(createItem("M_D_D", I18n.I.currencyCalculator()).appendSeparator())
                                            .add(PAGES_VWD, createItem("M_D_EZB", "P_V/2002", I18n.I.ezbEuroConversionRates(), "mm-icon-vwdpage"))
                                            .add(createKwtMenuItem(I18n.I.currencies1()))
                            )
                            .addIfAny(new Selector[]{FINDER_EUREX, VOLA_INDIZES}, createMenu("M_K", "M_UB_MPL/futures", I18n.I.futures1())
                                            .add(FINDER_EUREX, createItem("M_K_F", "M_UB_MPL/futures", I18n.I.futuresPriceList(), null).appendSeparator())
                                            .add(CBOT_OPTIONS, createItem("M_K_USF", "M_UB_MPL/us_futures", I18n.I.usFuturesPriceList(), null).appendSeparator())
                                            .add(FINDER_EUREX, createItem("M_LF_FUT", I18n.I.futuresSearch(), "mm-icon-finder"))
                                            .add(createItem("P_FUT", I18n.I.futuresPortrait()).hide())
                                            .add(FINDER_EUREX, createItem("M_LF_OPT", I18n.I.optionsSearch(), "mm-icon-finder"))
                                            .add(MenuModel.SEPARATOR)
                                            .add(createItem("P_OPT", I18n.I.optionsPortrait()).hide())
                                            .add(createItem("P_UND", I18n.I.underlyingPortrait()).hide())
                                            .add(PAGES_VWD, createItem("M_K_SW", "P_V/3203", I18n.I.swaps(), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, DZB_DEUTSCHE_EINGEBER}, createItem("M_K_ES", "P_V/255", I18n.I.euniaSwaps(), "mm-icon-vwdpage"))
//                        .add(createItem("M_K_CF", "Caps/Floors"))
//                        .add(createItem("M_K_FR", "FRAs"))
                                            .add(MenuModel.SEPARATOR)
                                            .add(PAGES_VWD.isAllowed() && FINDER_EUREX.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_K_IT_SO_IDX"),
                                                    createItem("M_K_IT_SO_IDX", "P_V/34000", "ITDER Stock Options Index", "mm-icon-vwdpage"))
                                            .add(PAGES_VWD.isAllowed() && FINDER_EUREX.isAllowed() && Customer.INSTANCE.isJsonMenuElementTrue("M_K_IT_IO_IDX"),
                                                    createItem("M_K_IT_IO_IDX", "P_V/34500", "ITDER Index Options Index", "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU1", "P_V/2700", "Eurex - DE", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU2", "P_V/16800", "Eurex - FI", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU3", "P_V/17200", "Eurex - FR", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU4", "P_V/17050", "Eurex - NL", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU5", "P_V/18710", "Eurex - IT", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU6", "P_V/17800", "Eurex - SE", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU7", "P_V/16200", "Eurex - RU", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU8", "P_V/16500", "Eurex - CH", "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU9", "P_V/18500", "Eurex - ES", "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_FU10", "P_V/2720", I18n.I.eurexFutureList(), "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PAGES_VWD, FINDER_EUREX}, createItem("M_K_E1", "P_V/31001", "Euronext", "mm-icon-vwdpage").appendSeparator())
                                            .addIfAll(new Selector[]{PAGES_VWD, VOLA_INDIZES}, createItem("M_K_V1", "P_V/2561", I18n.I.volatilityIndex("V-DAX New"), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, VOLA_INDIZES}, createItem("M_K_V2", "P_V/2562", I18n.I.volatilityIndex("VSTOXX"), "mm-icon-vwdpage"))
                                            .addIfAll(new Selector[]{PAGES_VWD, VOLA_INDIZES}, createItem("M_K_V3", "P_V/2563", I18n.I.volatilityIndex("VSMI"), "mm-icon-vwdpage").appendSeparator())
                            )
                            .add(createMenu("M_C_UB", I18n.I.commodities())
                                    .addIfAll(new Selector[]{PAGES_VWD, DZB_DEUTSCHE_EINGEBER}, createItem("M_C_I", "P_V/11410", I18n.I.commodityIndications(), "mm-icon-vwdpage"))
                                    .add(createItem("P_MER", I18n.I.portrait()).hide()))
                            .add(FITCH_CDS, createMenu("M_LF_CDS", I18n.I.cds()))
                            .add(DZ_KAPITALMARKT, createMenu("M_LF_GIS", I18n.I.offer())
                                    .add(createItem("DZ_KMFL", I18n.I.capitalMarketFavorites())))
                            .add(Selector.isDZResearch(), createItem("DZ_LF_RES2", "DZ_LF_RES", I18n.I.dzResearch(), "mm-icon-dzbank"))
                            .add(createItem(!SessionData.isAsDesign(), "M_S", I18n.I.searchResults()))
                            .add(createHiddenItem(SessionData.isAsDesign(), PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY, I18n.I.portrait()))
            );
            if(SessionData.isAsDesign()) {
                this.model.add(createModuleMenu("N", I18n.I.news(), "cg-news")
                        .add(OverviewNWSController.createMenuModel(this.model, this.mc, "N_UB")
                                .add(createItem("N_D", I18n.I.detail()).hide()))
                        .add(createItem("N_S", I18n.I.newsSearch()))
                        .add(FINDER_ANALYSIS, createMenu("N_A_UB", I18n.I.analyses())
                                .add(createItem("N_A_LF", I18n.I.analysesSearch(), "mm-icon-finder")))
                        .add(PLATOW, createItem("N_P", I18n.I.platow()))
                        .addIfAny(new Selector[]{DZBANK_WEB_INVESTOR, DZBANK_WEB_INFORMER}, createItem("DZ_TA", I18n.I.technicalAnalyses()))
                        .add(DZBANK_USER_MESSAGES, createItem("N_BN", I18n.I.userMessages()))
                );
            }
            else {
                this.model.add(createModuleMenu("N", I18n.I.news(), "cg-news")
                        .add(createItem("N_UB", SessionData.isAsDesign() ? I18n.I.newsOverview() : I18n.I.overview())
                                .add(createItem("N_D", I18n.I.detail()).hide()))
                        .add(createItem("N_S", I18n.I.newsSearch()))
                        .add(FINDER_ANALYSIS, createMenu("N_A_UB", I18n.I.analyses())
                                .add(createItem("N_A_LF", I18n.I.analysesSearch(), "mm-icon-finder")))
                        .add(PLATOW, createItem("N_P", I18n.I.platow()))
                        .addIfAny(new Selector[]{DZBANK_WEB_INVESTOR, DZBANK_WEB_INFORMER}, createItem("DZ_TA", I18n.I.technicalAnalyses()))
                        .add(DZBANK_USER_MESSAGES, createItem("N_BN", I18n.I.userMessages()))
                );
            }
            if (VWL.isAllowed()) {
                final MenuModel.Item itemNationalEconomy = createMenu("V", I18n.I.nationalEconomy())
                        .addIfJsonExists("ov_vwl_ch", createItem("V_CH", I18n.I.switzerland()))
                        .add(createItem("V_D", I18n.I.germany()))
                        .add(createItem("V_7", I18n.I.g7ChEuroland()))
                        .add(VWL_EMERGING_MARKETS, createMenu("V_E", I18n.I.emergingMarkets())
                                .add(createItem("V_EO", I18n.I.easternEurope()))
                                .add(createItem("V_EMS", I18n.I.centralSouthAmerica()))
                                .add(createItem("V_EA", I18n.I.asia())))
                        .add(createItem("V_UB", I18n.I.economicComparison())
                                        .add(portraitMK)
                        );
                if (SessionData.isAsDesign()) {
                    itemMarkets.add(itemNationalEconomy);
                }
                else {
                    this.model.add(itemNationalEconomy);
                }
            }
            if (!SessionData.isAsDesign()) {
                this.model.add(this.pagesInitializer); // "P_D" and "P_V"
            }
            if(SessionData.isAsDesign() && Selector.GISPORTAL.isAllowed()) {
                final MenuModel.Item item = createModuleItem("G", "GIS Portal", "gisportal-48", Ginjector.INSTANCE.getOpenGisPortalCommand());
                this.model.add(item);
            }
            if (SessionData.isAsDesign()) {
                final MenuModel.Item item = createModuleMenu("T", I18n.I.tools(), "cg-tools")
                        .add(createMenu("B_WS", I18n.I.watchlists())) //children are dynamically added!
                        .add(createMenu("B_PS", I18n.I.portfolioSamples())) //children are dynamically added!
                        .add(SessionData.isWithLimits(), createItem("B_L", I18n.I.limit()))
                        .add(createItem("T_D", I18n.I.currencyCalculator(), null))
                        .add(FlashCheck.isFlashAvailable(), createItem("T_I", I18n.I.investmentCalculator(), null))
                        .add(FlashCheck.isFlashAvailable() && WARRANT_CALCULATOR.isAllowed(), createItem("T_O", I18n.I.warrantCalculator(), null))
                        .add(FlashCheck.isFlashAvailable() && BOND_CALCULATOR.isAllowed(), createItem("T_R", I18n.I.bondCalculator(), null))
                        .add(FlashCheck.isFlashAvailable() && FLEX_CHART.isAllowed(), createItem("T_FC", "flexChartAnalyser"))
                        .add(VALOR_DATA_BROWSER, createItem("T_VDB", I18n.I.valordataBrowser()))
                        .add(WEB_XL, createItem("T_W", "Download web.XL (DDE)"))
                        .add(DZ_WERTENTICKLUNGSRECHNER, createItem("T_P_T", I18n.I.performanceCalculatorShort(), null))
                        .add(createMenu("T_F", PermStr.LIVE_FINDER.value())
                                        .add(createItem("T_F_STK", "M_LF_STK", I18n.I.stock(), "mm-icon-finder"))
                                        .add(createItem("T_F_FND", "M_LF_FND", I18n.I.funds(), "mm-icon-finder"))
                                        .add(createItem("T_F_BND", "M_LF_BND", I18n.I.bonds(), "mm-icon-finder"))
                                        .add(createItem("T_F_CER", "M_LF_CER", I18n.I.certificates(), "mm-icon-finder"))
                                        .add(createItem("T_F_WNT", "M_LF_WNT", I18n.I.warrants(), "mm-icon-finder"))
                                        .add(FINDER_EUREX, createItem("T_F_FUT", "M_LF_FUT", I18n.I.futures(), "mm-icon-finder"))
                                        .add(FINDER_EUREX, createItem("T_F_OPT", "M_LF_OPT", I18n.I.options(), "mm-icon-finder"))
                        );
                        // Only vwd advisory solution uses the "search" module icon.
                        // ICE mm[web] has the search results in tools below the live finders
                        if(!SessionData.isWithPmBackend()) {
                            item.add(createMenu(SearchWorkspaceWithToolsMenu.SEARCH_RESULT_MENU_ID, I18n.I.searchResults()))
                                    // Note: those items are not children of the search result menu, but of its parent!
                                    // This is necessary, because otherwise the search result menu will not be disabled
                                    // if it has no search result children. The NavTree decides to enable a menu
                                    // simply if it has children. It does not evaluate if those children are hidden or
                                    // disabled.
                                    .add(createItem("M_S", I18n.I.searchResults()).hide())
                                    .add(createItem(PmWebModule.HISTORY_TOKEN_SEARCH_DEPOT, "Suche Depotobjekte").hide())
                                    .add(createItem(PmWebModule.HISTORY_TOKEN_SEARCH_INSTRUMENT, "Suche Instrument").hide());
                        }
                this.model.add(item);
            }
            else {
                final MenuModel.Item item = createModuleMenu("T", I18n.I.tools(), "cg-tools")
                        .add(createItem("T_D", I18n.I.currencyCalculator(), null))
                        .add(FlashCheck.isFlashAvailable(), createItem("T_I", I18n.I.investmentCalculator(), null))
                        .add(FlashCheck.isFlashAvailable() && WARRANT_CALCULATOR.isAllowed(), createItem("T_O", I18n.I.warrantCalculator(), null))
                        .add(FlashCheck.isFlashAvailable() && BOND_CALCULATOR.isAllowed(), createItem("T_R", I18n.I.bondCalculator(), null))
                        .add(FlashCheck.isFlashAvailable() && FLEX_CHART.isAllowed(), createItem("T_FC", "flexChartAnalyser"))
                        .add(VALOR_DATA_BROWSER, createItem("T_VDB", I18n.I.valordataBrowser()))
                        .add(WEB_XL, createItem("T_W", "Download web.XL (DDE)"))
                        .add(FlashCheck.isFlashAvailable() && TICKER.isAllowed(), createItem("T_T", I18n.I.ticker()));
                if (Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed()) {
                    item.add(createItem("T_P_T", I18n.I.performanceCalculatorShort(), null));
                }
                this.model.add(item);
            }
        }

        if (SessionData.INSTANCE.getUser().getAppConfig().getBooleanProperty("pmreports", false)) {
            this.model.add(createMenu("PM", "pm[web]")
                            .add(createItem(PmWebModule.HISTORY_TOKEN_REPORT, I18n.I.singleReport()))
                            .add(createItem(PmWebModule.HISTORY_TOKEN_INVESTOR_CONFIG, I18n.I.configureInvestors()))
            );
        }
        if (SessionData.isAsDesign()) {
            this.model.add(createMenu(DASHBOARD_ID, "noname")
                            .add(createItem(DashboardPageController.HISTORY_TOKEN_DASHBOARDS, "Dashboards").hide())
            );
            if (SessionData.isWithPmBackend()) {
                this.model.add(createMenu(EXPLORER_ID, "noname") // $NON-NLS$
                                .add(createItem(PmWebModule.HISTORY_TOKEN_EXPLORER, "Explorer"))
                                .add(createItem(PmWebModule.HISTORY_TOKEN_GLOBAL_ANALYSIS, I18n.I.workspaceNamePmWeb()).hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE, "Investor", "pm-investor").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_PORTFOLIO, "Portfolio", "pm-investor-portfolio").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_ACCOUNT, "Account", "pm-investor-account").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_DEPOT, "Depot", "pm-investor-depot").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_PERSON, "Person", "pm-investor-person").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_PROSPECT, I18n.I.prospect(), "pm-investor-prospect").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_USER_DEFINED_FIELDS, I18n.I.userDefinedFields()).hide())
                                .add(createHiddenItem(!SessionData.isWithMarketData(), PmWebModule.HISTORY_TOKEN_CUSTOM_SECURITY, I18n.I.portrait()))
                                .add(createItem(PmWebModule.HISTORY_TOKEN_ACTIVITY, "Aktivität").hide())
                                .add(createItem(PmWebModule.HISTORY_TOKEN_CREATE_PROSPECT, "Interessent").hide())
                );
                // Only vwd advisory solution uses the "search" module icon.
                // ICE mm[web] has the search results in tools adjacent to the live finders
                this.model.add(createMenu(SEARCH_ID, I18n.I.searchResults())
                                .add(createItem("M_S", I18n.I.searchResults()))
                                .add(createItem(PmWebModule.HISTORY_TOKEN_SEARCH_DEPOT, "Suche Depotobjekte"))
                                .add(createItem(PmWebModule.HISTORY_TOKEN_SEARCH_INSTRUMENT, "Suche Instrument"))
                );
            }
        }

        if (!SessionData.isAsDesign() && GISPORTAL.isAllowed()) {
            this.model.add(createMenu("G", "GIS Portal")
                    .add(createItem("G_P", "Portal")));
        }

        if (SessionData.isAsDesign() && !PrivacyMode.isActive()) {
            this.model.add(createModuleMenu("S", I18n.I.administration(), "cg-settings")
                    .add(createItem("B_AC", I18n.I.settings()))
                    .add(createItem("B_PW", I18n.I.changePassword()))
                    .add(Selector.AS_ORDERING_VIA_ACTIVITIES, createItem("B_SPW", I18n.I.orderEntrySpecifyBrokingPassword()))
                    .add(MessageOfTheDayEditor.isActive() && MSG_OF_THE_DAY_EDIT.isAllowed(), createItem("B_MOTD", I18n.I.motdMessageOfTheDay())));
        }

        this.model.add(createModuleMenu("H", I18n.I.help(), "cg-help")
                        .add(createItem("H_H", I18n.I.productDescription()))
                        .add(createItem("H_K", I18n.I.contactForm()))
                        .add(!SessionData.isWithPmBackend(), createItem("H_CS", I18n.I.customerService()))
                        .add(createItem("H_ZCS", I18n.I.customerService()))  //see guidefs: (default-)menu-elements
                        .add(SessionData.isWithMarketData() && SessionData.isAsDesign(), createItem("H_TOU", I18n.I.termsOfUseMenuItem()))
                        .add(PAGES_VWD, createItem("H_D", "P_V/10", I18n.I.disclaimer(), "mm-icon-vwdpage"))
                        .add(SessionData.isWithPmBackend(), createItem("H_I", I18n.I.info()))
                        .add(SessionData.isWithPmBackend(), createItem("H_UM", I18n.I.pmUpdateManual()))
                        .add(SessionData.isWithPmBackend(), createItem("H_SAM", I18n.I.pmStandardActivitiesManual()))
                        .add("de".equals(I18n.I.locale()) && SessionData.isWithMarketData(), createItem("H_L", I18n.I.dictionary()))
                        .addIfJsonExists(ApoFundPricelistsController.JSON_KEY_PRICELISTS, createItem("H_SA", I18n.I.yourServiceTeam(), "mm-icon-dzbank"))
                        .add(DZB_KWT_FUNCTION, createItem("H_SK", I18n.I.yourServiceTeam(), "mm-icon-dzbank"))
                        .add(Customer.INSTANCE.isOlb(), createItem("OLB_V", I18n.I.trainingVideo(), null))
                        .add(!SessionData.isWithPmBackend(), createItem("H_RI", I18n.I.releaseInformation(), null))
                        .add(CAMPAIGN, createItem("H_CP", "Information Newex April 2011", null))
                        .add(createItem("H_SM", I18n.I.sitemap()).hide())
                        .add(SessionData.isWithPmBackend(), createItem("H_H_AS", I18n.I.help()))
        );
        if(!SessionData.isAsDesign()) {
            tryAddDzbankWgzbankStatistics(this.model.getRootItem());
        }
    }

    private void tryAddDzbankWgzbankStatistics(MenuModel.Item targetMenu) {
        // Do never add the STA menu item twice, e.g. if s.o. has both selectors DZ BANK
        // and WGZ BANK, which is true for many internal test accounts!
        if(this.model.getElement("STA") == null) {
            targetMenu.add(Selector.STATISTICS.isAllowed(), createMenu("STA", I18n.I.statistics())
                    .add(createItem("STA_S", I18n.I.statisticPages()))
                    .add(createItem("STA_T", I18n.I.statisticTop())));
        }
    }

    private boolean isWgz() {
        return Customer.INSTANCE.isDzWgz() && WGZ_BANK_USER.isAllowed();
    }

    private boolean isDz() {
        return Customer.INSTANCE.isDzWgz() && DZ_BANK_USER.isAllowed();
    }

    private MenuModel.Item createKwtMenuItem(String s) {
        if (!DZB_KWT_FUNCTION.isAllowed() && !KWT_NO_MARKETDATA.isAllowed()) {
            return null;
        }

        final JSONWrapper wrapper = getKwtStructure(s);
        if (wrapper == JSONWrapper.INVALID) {
            return null;
        }
        String path = getKwtStructurePath(s);
        return createKwtMenuItem(wrapper, "KWT - " + s, path);
    }

    private void initKwtMenu() {
        final JSONWrapper guiDef = getKwtStructure("KWT");
        if (guiDef == JSONWrapper.INVALID) {
            DebugUtil.logToServer("kwt menu not found!");
            return;
        }

        String path = getKwtStructurePath("KWT");

        try {
            final MenuModel.Item menu = createModuleMenu("KWT", guiDef.get("name").stringValue(), "module-kwt");
            this.model.add(menu);
            final JSONWrapper children = guiDef.get("children");
            for (int i = 0; i < children.size(); i++) {
                menu.add(createKwtMenuItem(children.get(i), null,
                        path + ".children[name:" + children.get(i).get("name").stringValue() + "]"));
            }

            if (KWT_NO_MARKETDATA.isAllowed()) {
                menu.add(createMenu("S", I18n.I.others())
                        .add(createKwtMenuItem(I18n.I.funds()))
                        .add(createKwtMenuItem(I18n.I.bonds()))
                        .add(createKwtMenuItem(I18n.I.certificates()))
                        .add(createKwtMenuItem(I18n.I.currencies1())));
            }
            final MenuModel.Item listMenu = createKwtListMenuItem();
            if (listMenu != null) {
                menu.add(listMenu);
            }

            final MenuModel.Item productMap = createKwtProductMapItem();
            if (productMap != null) {
                menu.add(productMap);
            }
        } catch (Exception e) {
            DebugUtil.logToServer("initKwtMenu failed", e);
        }
    }

    private MenuModel.Item createKwtListMenuItem() {
        final JSONWrapper wrapper = SessionData.INSTANCE.getGuiDef("kwt_kurslisten");
        if (wrapper == JSONWrapper.INVALID) {
            return null;
        }
        final MenuModel.Item menu = createMenu("LST", I18n.I.pricelists());
        addKwtListItemsToMenu(wrapper, menu);
        return menu;
    }

    private void addKwtListItemsToMenu(JSONWrapper wrapper, MenuModel.Item menu) {
        for (int i = 0; i < wrapper.size(); i++) {
            final JSONWrapper element = wrapper.get(i);
            final String id = element.get("id").stringValue();
            final String title = element.get("title").stringValue();
            final String listid = element.get("listid").stringValue();
            if (listid != null) {
                final String shortId = listid.substring(KwtCustomListController.LIST_NAME_PREFIX.length());
                menu.add(createItem("LST" + id, "LST/" + shortId, title, null));
                continue;
            }
            // admintool supports nested lists, so we have to get recursive...
            final JSONWrapper subWrapper = element.get("elements");
            if (subWrapper != JSONWrapper.INVALID) {
                final MenuModel.Item subMenu = createMenu("LST" + id, title);
                addKwtListItemsToMenu(subWrapper, subMenu);
                if (!subMenu.isLeaf()) {
                    menu.add(subMenu);
                }
            }
        }
    }

    private MenuModel.Item createKwtProductMapItem() {
        final JSONWrapper wrapper = SessionData.INSTANCE.getGuiDef("kwt_productmap");
        if (wrapper == JSONWrapper.INVALID || wrapper.size() == 0) {
            return null;
        }
        final String firstListId = wrapper.get(0).get("listid").stringValue();
        final MenuModel.Item menu = createMenu("KPM", "KPM/listid=" + firstListId, I18n.I.kwtProductMap());
//        menu.add(createItem("KPM", I18n.I.overview()).hide());
        addKwtProductMapToMenu(wrapper, menu);
        return menu;
    }

    private void addKwtProductMapToMenu(JSONWrapper wrapper, MenuModel.Item menu) {
        for (int i = 0; i < wrapper.size(); i++) {
            final JSONWrapper element = wrapper.get(i);
            final String id = element.get("id").stringValue();
            final String title = element.get("title").stringValue();
            final String listid = element.get("listid").stringValue();
            menu.add(createItem("KPM" + id, "KPM/listid=" + listid, title, null));
        }
    }

    private MenuModel.Item createKwtMenuItem(JSONWrapper wrapper, String name, String jsonPath) {
        final KwtPageController controller = new KwtPageController(wrapper, jsonPath);
        this.mc.addController(controller.getKey(), controller);
        return createItem(controller.getKey(), (name != null) ? name : wrapper.get("name").stringValue(), "mm-icon-raiba");
    }

    private JSONWrapper getKwtStructure(final String key) {
        final JSONWrapper contentKeys = SessionData.INSTANCE.getGuiDef("kwt_content_keys");
        if (contentKeys == JSONWrapper.INVALID) {
            return contentKeys;
        }
        final String s = contentKeys.get(key).stringValue();
        return (s != null) ? SessionData.INSTANCE.getGuiDef("Kwtgui").get(s) : JSONWrapper.INVALID;
    }

    private String getKwtStructurePath(final String key) {
        final JSONWrapper contentKeys = SessionData.INSTANCE.getGuiDef("kwt_content_keys");
        if (contentKeys == JSONWrapper.INVALID) {
            return null;
        }
        final String val = contentKeys.get(key).stringValue();
        return "Kwtgui." + val;
    }

    protected MenuModel.Item createItem(String id, String name) {
        return createItem(id, name, null);
    }

    protected MenuModel.Item createItem(boolean active, String id, String name) {
        return active ? createItem(id, name, null) : null;
    }

    protected MenuModel.Item createHiddenItem(boolean active, String id, String name) {
        return active ? createItem(id, name, null).hide() : null;
    }

    private MenuModel.Item createItem(String id, String name, String iconStyle) {
        final boolean enabled = this.mc.hasController(id);
        return this.model.createItem(id, id, name, iconStyle, enabled);
    }

    protected MenuModel.Item createItem(boolean active, String id, String controllerId, String name,
            String iconStyle) {
        return active ? createItem(id, controllerId, name, iconStyle) : null;
    }

    protected MenuModel.Item createItem(String id, String controllerId, String name,
            String iconStyle) {
        return this.model.createItem(id, controllerId, name, iconStyle, true);
    }

    private MenuModel.Item createLinkItem(String id, String url, String name, String iconStyle) {
        return this.model.createLinkItem(id, url, name, iconStyle, true);
    }

    protected MenuModel.Item createMenu(String id, String name) {
        final String cid = this.mc.hasController(id) ? id : null;
        return createMenu(id, cid, name);
    }

    private MenuModel.Item createMenu(String id, String cid, String name) {
        return this.model.createMenu(id, cid, name);
    }

    protected MenuModel.Item createModuleMenu(String id, String name, String iconStyle) {
        final String cid = this.mc.hasController(id) ? id : null;
        return this.model.createItem(id, cid, name, iconStyle, true);
    }

    protected MenuModel.Item createModuleItem(String id, String name, String iconStyle, Command onClickCommand) {
        final String cid = this.mc.hasController(id) ? id : null;
        return this.model.createItem(id, cid, name, iconStyle, true).withOnClickCommand(onClickCommand);
    }

    private String getOverviewId() {
        String overviewPage = SessionData.INSTANCE.getGuiDefValue("overview_page");
        if (overviewPage != null) {
            return overviewPage;
        }
        if (DZB_KWT_FUNCTION.isAllowed()) {
            return "M_UB_KWT";
        }
        overviewPage = SessionData.INSTANCE.getGuiDefValue("market-overview-variant");
        if ("IT".equals(overviewPage)) {
            return "M_UB_KIT";
        }
        return "M_UB_K";
    }

}
