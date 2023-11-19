package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.mmgwt.mmweb.client.as.AsApplicationConfigFormPresenter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsApplicationConfigFormView;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsChangePasswordPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsHelpController;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentryactivity.SetPasswordPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.CerComparisonController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.apo.ApoFundPricelistsController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.GisPortal;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.KwtCertificatePricelistsController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.KwtCustomListController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.wgz.WGZCertificateTypesController;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderAnalysis;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderCalendar;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderNews;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.IssuerSelectionController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderAnalysis;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderBND;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCDS;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderFND;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderFUT;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderGIS;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderNews;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderOPT;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderResearch;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderSTK;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderWNT;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.MyspaceController;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AnalyserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PageStatisticsController;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.TopStatisticsController;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.DzPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageSearch;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PortfolioController;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.WatchlistController;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.*;

/**
 * @author Ulrich Maurer
 *         Date: 11.12.12
 */
public class MainControllerInitializerCommand implements Command {
    private final AbstractMainController mainController;

    private final Ginjector ginjector;

    private final FeatureFlags featureFlags;

    private final SessionData sessionData;

    public MainControllerInitializerCommand(
            AbstractMainController mainController,
            Ginjector ginjector) {
        this.mainController = mainController;
        this.ginjector = ginjector;
        this.featureFlags = ginjector.getFeatureFlags();
        this.sessionData = ginjector.getSessionData();
    }

    @Override
    public void execute() {
        SimpleControllerInitializer.initControllers(this.mainController);

        final ContentContainer cc = this.mainController.getView();
        if (!this.sessionData.isIceDesign()) {
            this.mainController.addControllerCheckJson(false, "B_A", MyspaceController.getInstance()); // $NON-NLS$
        }
        this.mainController.addControllerCheckJson(false, DashboardPageController.HISTORY_TOKEN_DASHBOARDS, new DashboardPageController(DashboardPageController.DashboardIdStrategy.BY_ID_PARAMETER));
        this.mainController.addControllerCheckJson(false, "B_AC", this.sessionData.isIceDesign() ? new AsApplicationConfigFormPresenter(cc, new AsApplicationConfigFormView()).asPageController() : ApplicationConfigForm.INSTANCE); // $NON-NLS$
        if (MessageOfTheDayEditor.isActive() && Selector.MSG_OF_THE_DAY_EDIT.isAllowed()) {
            this.mainController.addControllerCheckJson(false, "B_MOTD", new MessageOfTheDayEditor()); // $NON-NLS$
        }
        if (this.sessionData.isIceDesign()) {
            if (this.mainController.getPasswordStrategy().isShowViewAvailable()) {
                this.mainController.addControllerCheckJson(false, "B_PW", new AsChangePasswordPageController(cc)); // $NON-NLS$
            }
        }
        else {
            this.mainController.addControllerCheckJson(false, "B_PW", PW_CHANGE_CONTROLLER); // $NON-NLS$
        }
        if(this.sessionData.isIceDesign()) {
            this.mainController.addControllerCheckJson(false, "B_SPW", new SetPasswordPageController(cc)); // $NON-NLS$
        }

        if (VwdPageController.SELECTOR.isAllowed()) {
            this.mainController.addControllerCheckJson(false, VwdPageController.KEY, new VwdPageController(cc));
            if (Selector.SEARCH_VWD_PAGES.isAllowed()) {
                final VwdPageSearch controller = new VwdPageSearch(cc);
                this.mainController.addControllerCheckJson(false, VwdPageSearch.TEXT_SEARCH_KEY, controller);
                this.mainController.addControllerCheckJson(false, VwdPageSearch.SYMBOL_SEARCH_KEY, controller);
                this.mainController.addControllerCheckJson(false, VwdPageSearch.POINTER_SEARCH_KEY, controller);
            }
        }
        if (DzPageController.SELECTOR.isAllowed()) {
            this.mainController.addControllerCheckJson(false, DzPageController.KEY, new DzPageController(cc));
        }

        this.mainController.addControllerCheckJson(false, new String[]{"M_D_D", "T_D"}, new SimpleOverviewController(cc, "CurrencyCalculator(row=0;col=0;betrag=1;isocodeFrom=EUR;isocodeTo=EUR;title=Devisenrechner)")); // $NON-NLS$
        if (Selector.DZ_WERTENTICKLUNGSRECHNER.isAllowed()) {
            this.mainController.addControllerCheckJson(false, "T_P", new SimpleOverviewController(cc, "PerformanceCalculator()")); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "T_P_T", new SimpleOverviewController(cc, "PerformanceCalculatorTool()")); // $NON-NLS$
        }
        this.mainController.addControllerCheckJson(false, "M_R_UB", new SimpleOverviewController(cc, Selector.DZBANK_WEB_INFORMER.isAllowed() ? "ov_bnd_informer" : "ov_bnd_investor")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_R_Z", new SimpleOverviewController(cc, "bnd_yieldcurve")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "T_FC", new AnalyserController(cc, AnalyserSnippet.TYPE_FLEX_CHART)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, new String[]{"M_F_I", "T_I"}, new AnalyserController(cc, AnalyserSnippet.TYPE_INVESTMENT_CALCULATOR)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, new String[]{"M_R_RR", "T_R"}, new AnalyserController(cc, "BND")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_MKT_UB", new SimpleOverviewController(cc, "ov_mkt")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_A_UB", new OverviewSTKController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_F_UB", new SimpleOverviewController(cc, "ov_fnd")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_Z_UB", new SimpleOverviewController(cc, "ov_cer")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_Z_EM", new MultiFinderGroupsController("CER", "issuername", MultiFinderGroupsController.CERTIFICATE_TYPE,  // $NON-NLS$
                "fieldname", DZ_BANK_USER.isAllowed() ? "dzIsLeverageProduct=='false'" : null)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_O_LM", new MultiFinderGroupsController("CER", "issuername", MultiFinderGroupsController.LEVERAGE_TYPE,  // $NON-NLS$
                "fieldname", "dzIsLeverageProduct=='true' && notActive=='false'")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_O_EM", new MultiFinderGroupsController("WNT", "issuername", MultiFinderGroupsController.WARRENT_TYPE,  // $NON-NLS$
                "fieldname", null)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_O_UB", new SimpleOverviewController(cc, "ov_wnt")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, new String[]{"M_O_R", "T_O"}, new AnalyserController(cc, "WNT", 560)); // $NON-NLS$
        this.mainController.addControllerCheckSelector("M_O_NP", new CommandController(cc, () -> { // $NON-NLS$
            LiveFinderWNT.INSTANCE.findNewProducts();
            PlaceUtil.goTo("M_LF_WNT"); // $NON-NLS$
        }), NEW_PRODUCTS_TRADED);
        this.mainController.addControllerCheckJson(false, "M_C_UB", new SimpleOverviewController(cc, Selector.DZBANK_WEB_INFORMER.isAllowed() ? "ov_com_informer" : "ov_com_investor")); // $NON-NLS$

        this.mainController.addControllerCheckJson(false, "M_C_UB", new SimpleOverviewController(cc, Selector.DZBANK_WEB_INFORMER.isAllowed() ? "ov_com_informer" : "ov_com_investor")); // $NON-NLS$
        final PriceListOverviewController pl = new PriceListOverviewController(cc);
        this.mainController.addControllerCheckJson(false, new String[]{"M_UB_K"}, pl); // $NON-NLS$
        if (this.featureFlags.isEnabled0(FeatureFlags.Feature.VWD_IT_LOCALIZATION) && "IT".equals(this.sessionData.getGuiDefValue("market-overview-variant"))) { // $NON-NLS$
            this.mainController.addControllerCheckJson(false, new String[]{"M_A_KL"}, new SimpleOverviewController(cc, "ov_sl")); // $NON-NLS$
        }
        else {
            this.mainController.addControllerCheckJson(false, new String[]{"M_A_KL"}, pl); // $NON-NLS$
        }
        this.mainController.addControllerCheckJson(false, "M_UB_KWT", new SimpleOverviewController(cc, "ov_kwt")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_UB_KP", new PriceListController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_UB_HP", new HistoricalPerformanceController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_UB_MPL", new SimpleOverviewController(cc, "ov_mpl")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_UB_KIT", new SimpleOverviewController(cc, "ov_it")); // $NON-NLS$

        this.mainController.addController("M_LF_STK", LiveFinderSTK.INSTANCE); // $NON-NLS$
        this.mainController.addController("M_LF_BND", LiveFinderBND.INSTANCE); // $NON-NLS$
        this.mainController.addController("M_LF_BNDI", this.ginjector.getLiveFinderBNDIssuer()); // $NON-NLS$

        this.mainController.addController("M_LF_CER", LiveFinderCER.INSTANCE_CER); // $NON-NLS$
        if (DZ_BANK_USER.isAllowed()) {
            this.mainController.addController("M_LF_LEV", LiveFinderCER.INSTANCE_LEV); // $NON-NLS$
        }
        this.mainController.addController("M_LF_FND", LiveFinderFND.INSTANCE); // $NON-NLS$
        this.mainController.addController("M_LF_WNT", LiveFinderWNT.INSTANCE); // $NON-NLS$
        this.mainController.addController("M_LF_FUT", LiveFinderFUT.INSTANCE); // $NON-NLS$
        this.mainController.addController("M_LF_OPT", LiveFinderOPT.INSTANCE); // $NON-NLS$
        this.mainController.addController("M_LF_GIS", LiveFinderGIS.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckSelector("M_LF_CDS", LiveFinderCDS.INSTANCE, FITCH_CDS); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_F_V", new UnionFondsController()); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_UB_C", FinderCalendar.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_D_UB", new SimpleOverviewController(cc, "ov_cur")); // $NON-NLS$
        this.mainController.addControllerCheckJson(true, "M_D_CC1", new SimpleOverviewController(cc, "chf_xrates")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_D_C", new SimpleOverviewController(cc, "Crossrates(isocode=[EUR,CHF,JPY,USD,AUD,CAD,GBP,RUB];col=0;row=0;colSpan=3)")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "N_UB", new OverviewNWSController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "N_A_F", FinderAnalysis.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckSelector("N_A_LF", LiveFinderAnalysis.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "N_A_UB", new SimpleOverviewController(cc, "ov_analysis")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_S", new PriceSearchController(cc)); // $NON-NLS$
        if (this.featureFlags.isEnabled0(FeatureFlags.Feature.VWD_RELEASE_2014) || this.featureFlags.isEnabled0(FeatureFlags.Feature.DZ_RELEASE_2016)) {
            this.mainController.addControllerCheckJson(false, "N_S", LiveFinderNews.INSTANCE); // $NON-NLS$
        }
        else {
            this.mainController.addControllerCheckJson(false, "N_S", FinderNews.INSTANCE); // $NON-NLS$
        }
        this.mainController.addControllerCheckJson(false, "N_P", new PlatowController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_F_A", new AttraxFondsadvisorController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "M_UB_A", SimpleHtmlController.createPlacard(cc, "aushang.pdf")); // $NON-NLS$
        this.mainController.addControllerCheckJson(true, "M_UB_BA", SimpleHtmlController.createPlacard(cc, "bulletin.pdf")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "T_VDB", SimpleHtmlController.createValordataBrowser(cc)); // $NON-NLS$
        if (!this.sessionData.isIceDesign()) {  // TODO: remove in a future release if legacy design is no longer necessary
            this.mainController.addControllerCheckJson(false, "T_T", SimpleHtmlController.createTicker(cc)); // $NON-NLS$
        }
        this.mainController.addControllerCheckJson(true, "H_K", new ContactFormController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "H_L", new DictionaryController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "N_BN", new NewsTopicController(cc, NewsTopicController.USER_MSG_DEF)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "B_W", WatchlistController.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_UB", new SimpleOverviewController(cc, "ov_economic")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_CH", new SimpleOverviewController(cc, "ov_vwl_ch")); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_D", NationalEconomyController.createGER(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_EO", NationalEconomyController.createEASTEUROPE(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_EMS", NationalEconomyController.createAMERICA(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_EA", NationalEconomyController.createASIA(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "V_7", NationalEconomyController.createG7CHEU(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "B_P", PortfolioController.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "B_L", AlertController.INSTANCE); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "T_W", SimpleHtmlController.createWebXl(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "P_DH", new HtmlContentController(cc, Settings.INSTANCE.vwdPageHelp())); // $NON-NLS$
        this.mainController.addControllerCheckJson(true, "H_H", Customer.INSTANCE.createHelpController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "DZ_Z", new IssuerSelectionController(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, "H_SM", new SitemapController(cc)); // $NON-NLS$
        if (Selector.GISPORTAL.isAllowed()) {
            this.mainController.addControllerCheckJson(false, "H_RI", SimpleHtmlController.createReleaseInformationGisPortal(cc)); // $NON-NLS$
        }
        else {
            this.mainController.addControllerCheckJson(false, "H_RI", SimpleHtmlController.createReleaseInformation(cc)); // $NON-NLS$
        }
        this.mainController.addControllerCheckJson(false, "H_CP", SimpleHtmlController.createCampaign(cc)); // $NON-NLS$

        if (SessionData.isWithPmBackend()) {
            this.mainController.addControllerCheckJson(false, "H_H_AS", new AsHelpController()); // $NON-NLS$
            // Note: pm-server provides the update manual (for advisory solution) via rewrite rules
            // So you won't find a file named "update_manual.pdf" in this source code repository.
            this.mainController.addControllerCheckJson(false, "H_UM", new HtmlContentController(cc, "update_manual.pdf", "100%", false)); // $NON-NLS$
            // Note: pm-server provides the standard activities manual (for advisory solution) via rewrite rules
            // So you won't find a file named "default_activities_manual.pdf" in this source code repository.
            // Additionally, due to sales/marketing reasons, the standard activities manual should be visible even if
            // activities are not licensed.
            this.mainController.addControllerCheckJson(false, "H_SAM", new HtmlContentController(cc, "standard_activities_manual.pdf", "100%", false)); // $NON-NLS$
            // other controllers, e.g. H_CS, H_I, see PmWebModule
        }
        else {
            this.mainController.addControllerCheckJson(true, "H_CS", SimpleHtmlController.createVwdCustomerServiceInfo(cc)); // $NON-NLS$
        }

        this.mainController.addControllerCheckJson(false, "H_TOU", new HtmlContentController(cc, Permutation.GIS.isActive() ? "http://cms.vwd.com/mmweb/nutzungsbedingungen/nutzungsbedingungen.html" : I18n.I.termsOfUseFilename())); // $NON-NLS$

        this.mainController.addControllerCheckJson(true, "H_ZCS", SimpleHtmlController.createZoneCustomerServiceInfo(cc)); // $NON-NLS$
        this.mainController.addControllerCheckJson(false, CerComparisonController.TOKEN, CerComparisonController.INSTANCE);

        if (!this.sessionData.isIceDesign() && Selector.GISPORTAL.isAllowed()) {
            this.mainController.addControllerCheckJson(false, "G_P", new GisPortal(cc, this.ginjector.getOpenGisPortalCommand())); // $NON-NLS$
        }

        if (this.sessionData.isWithProfiDepots()) {
            final String uid = this.sessionData.getUserProperty(AppConfig.PROP_KEY_MUSTERDEPOT_USERID);
            this.mainController.addControllerCheckJson(false, "M_UB_PD", PortfolioController.create(uid)); // $NON-NLS$
        }

        final HtmlContentController stc = new HtmlContentController(cc, "http://cms.vwd.com/wissen/serviceteam/serviceteam_gis.html"); // $NON-NLS$

        if (this.sessionData.getGuiDef("bestof_explanations") != JSONWrapper.INVALID) { // $NON-NLS$
            this.mainController.addControllerCheckSelector("M_CER_BEST", new MultiTabController(cc, "cer_bestof", new BestOfController.Factory()), // $NON-NLS$
                    BEST_OF_LIST);
        }
        this.mainController.addControllerCheckSelector("M_CER_PI", new MultiTabController(cc, "cer_popins", new PopularInstrumentsController.Factory()), // $NON-NLS$
                MOST_ACTIVE);
        this.mainController.addControllerCheckSelector("MM_CER_T", new HtmlContentController(cc, "https://www.eniteo.de/dvt2/themenprodukte.htn?gisberater=1"), // $NON-NLS$
                ENITEO_CONTENT);
        this.mainController.addControllerCheckSelector("MM_CER_P", new EniteoProductInFocusController(cc), ENITEO_CONTENT); // $NON-NLS$
        this.mainController.addControllerCheckSelector("MM_CER_NP", new CommandController(cc, () -> { // $NON-NLS$
            LiveFinderCER.INSTANCE_CER.findNewProducts();
            PlaceUtil.goTo("M_LF_CER"); // $NON-NLS$
        }), NEW_PRODUCTS_TRADED);
        this.mainController.addControllerCheckSelector("MM_LEV_NP", new CommandController(cc, () -> { // $NON-NLS$
            LiveFinderCER.INSTANCE_LEV.findNewProducts();
            PlaceUtil.goTo("M_LF_LEV"); // $NON-NLS$
        }), DZ_BANK_USER);
        this.mainController.addControllerCheckSelector("MM_CER_PIB", new CommandController(cc, () -> { // $NON-NLS$
            LiveFinderCER.INSTANCE_CER.findDzPibs();
            PlaceUtil.goTo("M_LF_CER"); // $NON-NLS$
        }), PRODUCT_WITH_PIB, DZ_BANK_USER);
        this.mainController.addControllerCheckSelector("MM_BND_PIB", new CommandController(cc, () -> { // $NON-NLS$
            LiveFinderBND.INSTANCE.findDzPibs();
            PlaceUtil.goTo("M_LF_BND"); // $NON-NLS$
        }), PRODUCT_WITH_PIB, DZ_BANK_USER);

        this.mainController.addControllerCheckJson(false, "DZ_KMFL", new SimpleOverviewController(cc, "dz_kmfl")); // $NON-NLS$

        if (Selector.DZ_BANK_USER.isAllowed()) {
            final GisIpoInstrumentsController dzDerivativeIpo = new GisIpoInstrumentsController(cc, "dz"); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "DZ_NE", dzDerivativeIpo); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "M_Z_N", dzDerivativeIpo); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "DZ_S", stc); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "DZ_PUB", new DZPublicationsController(cc)); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "DZ_TA", SimpleHtmlController.createTechnicalAnalysis(cc)); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "DZ_TAT", new TeaserConfigForm()); // $NON-NLS-0$
            if (Selector.isDZResearch()) {
                this.mainController.addControllerCheckSelector("DZ_LF_RES", LiveFinderResearch.INSTANCE); // $NON-NLS$
            }
        }

        if (Selector.WGZ_BANK_USER.isAllowed()) {
            this.mainController.addControllerCheckJson(false, "WZ_NE", new GisIpoInstrumentsController(cc, "wgz")); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "WZ_S", new HtmlContentController(cc, "http://cms.vwd.com/wissen/serviceteam/serviceteam_wgz.html")); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "WZ_C", new WGZCertificateTypesController(cc)); // $NON-NLS$
            if (Selector.isDZResearch()) {
                this.mainController.addControllerCheckSelector("DZ_LF_RES", LiveFinderResearch.INSTANCE); // $NON-NLS$
            }
        }

        if (Customer.INSTANCE.isKwt()) {
            this.mainController.addControllerCheckJson(false, "M_Z_KWT", new KwtCertificatePricelistsController(cc)); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "H_SK", stc); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "LST", new KwtCustomListController(cc)); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "KPM", new TabbedPricelistTabController(cc)); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "M_D_CU", new SimpleOverviewController(cc, "usd_xrates")); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "M_D_CC", new SimpleOverviewController(cc, "chf_xrates")); // $NON-NLS$
        }

        if (Customer.INSTANCE.isOlb()) {
            this.mainController.addControllerCheckJson(false, "OLB_UB", new SimpleOverviewController(cc, "ov_olb")); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "OLB_AUS", SimpleHtmlController.createPlacard(cc, "OLB-Boersenaushang.pdf")); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "OLB_PDFS", new HtmlContentController(cc, Settings.INSTANCE.olbThemenPdfUrl())); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "OLB_V", SimpleHtmlController.createOlbVideo(cc)); // $NON-NLS$
        }

        if (Customer.INSTANCE.isApobank()) {
            this.mainController.addControllerCheckJson(false, "M_F_APO", new ApoFundPricelistsController(cc)); // $NON-NLS$
            this.mainController.addControllerCheckJson(false, "H_SA", stc); // $NON-NLS$
        }

        if (Selector.STATISTICS.isAllowed()) {
            this.mainController.addController("STA_S", new PageStatisticsController()); // $NON-NLS$
            this.mainController.addController("STA_T", new TopStatisticsController()); // $NON-NLS$
        }
    }

    static final PageController PW_CHANGE_CONTROLLER = new PageControllerAdapter() {
        public void onPlaceChange(PlaceChangeEvent event) {
            new ChangePasswordPresenter(
                    new ChangePasswordView(), AbstractMainController.INSTANCE.getPasswordStrategy(), null
            ).show(true);
        }

        public String getPrintHtml() {
            return "PW_CHANGE_CONTROLLER.getPrintHtml()"; // $NON-NLS$
        }

        public PdfOptionSpec getPdfOptionSpec() {
            return null;
        }
    };
}
