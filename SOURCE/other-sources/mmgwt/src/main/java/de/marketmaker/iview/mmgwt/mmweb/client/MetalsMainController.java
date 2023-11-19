/*
 * MetalsMainController.java
 *
 * Created on 17.03.2008 14:58:41
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderNews;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.MyspaceController;
import de.marketmaker.iview.mmgwt.mmweb.client.myspace.SnippetMenuConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitCURController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitMERController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AlertSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ArbitrageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartcenterSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.CompactQuoteSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurablePriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ListDetailsHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MarketSelectionButton;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MultiPriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.OHLCVSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.RatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SimpleHtmlSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TimesAndSalesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.VwdPageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.MetalsVwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBoxAs;

/**
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
public class MetalsMainController extends AbstractMainController {
    private static final ChangePasswordDisplay.MmfWebPasswordStrategy MMF_WEB_PASSWORD_STRATEGY = new ChangePasswordDisplay.MmfWebPasswordStrategy();

    public MetalsMainController() {
    }

    public MetalsMainController(String contextPath) {
        super(contextPath);
    }

    @Override
    protected void onBeforeInit() {
        super.onBeforeInit();

        MarketSelectionButton.NAME_STRATEGY = QuoteData::getVwdcode;

        ArbitrageSnippet.LINK_TYPE = ListDetailsHelper.LinkType.VWDCODE;

        SnippetConfigurationView.DEFAULT_FILTER_TYPES = new String[] { "MER", "CUR" };// $NON-NLS$
        SnippetConfigurationView.QUOTE_SELECTION_WITH_VWDCODE = true;
    }

    @Override
    protected void initSnippetClasses() {
        SnippetClass.addClass(new AlertSnippet.Class());
        SnippetClass.addClass(new ArbitrageSnippet.Class());
        SnippetClass.addClass(new ChartcenterSnippet.Class());
        SnippetClass.addClass(new ChartListSnippet.Class());
        SnippetClass.addClass(new CompactQuoteSnippet.Class());
        SnippetClass.addClass(new ConfigurablePriceListSnippet.Class());
        SnippetClass.addClass(new MultiPriceListSnippet.Class());
        SnippetClass.addClass(new NewsEntrySnippet.Class());
        SnippetClass.addClass(new NewsHeadlinesSnippet.Class());
        SnippetClass.addClass(new PortraitChartSnippet.Class());
        SnippetClass.addClass(new PriceTeaserSnippet.Class());
        SnippetClass.addClass(new PriceSnippet.Class());
        SnippetClass.addClass(new RatiosSnippet.Class());
        SnippetClass.addClass(new SimpleHtmlSnippet.Class());
        SnippetClass.addClass(new StaticDataSnippet.Class());
        SnippetClass.addClass(new VwdPageSnippet.Class());
        SnippetClass.addClass(new OHLCVSnippet.Class());
        SnippetClass.addClass(new TimesAndSalesSnippet.Class());
    }

    @Override
    public AbstractMainView createView() {
        return this.sessionData.isIceDesign()
                ? new MainView(new DefaultTopToolbar(new RightLogoSupplier()).forMetals(), Ginjector.INSTANCE.getSouthPanel(), this.sessionData)
                : new LegacyMetalsMainView(this);
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    protected void initControllers() {
        SnippetMenuConfig.INSTANCE = new SnippetMenuConfig(
                SnippetMenuConfig.COMPACT_QUOTE
                , SnippetMenuConfig.ARBITRAGE
                , SnippetMenuConfig.CHART
                , SnippetMenuConfig.NEWS
                , SnippetMenuConfig.VWD_PAGES
        );

        final AbstractMainView cc = this.getView();

        if(this.sessionData.isIceDesign()) {
            addControllerCheckJson(false, DashboardPageController.HISTORY_TOKEN_DASHBOARDS, new DashboardPageController(DashboardPageController.DashboardIdStrategy.BY_ID_PARAMETER));
        }

        addControllerCheckJson(false, "MS_I", new SimpleOverviewController(cc, "ov_ms1")); // $NON-NLS-0$ $NON-NLS-1$
        addControllerCheckJson(false, "MS_II", new SimpleOverviewController(cc, "ov_ms2")); // $NON-NLS-0$ $NON-NLS-1$
        addControllerCheckJson(false, "MS_CUR", new SimpleOverviewController(cc, "ov_ms_cur")); // $NON-NLS-0$ $NON-NLS-1$

        addControllerCheckJson(false, MetalsVwdPageController.KEY, new MetalsVwdPageController(cc));
        addControllerCheckJsonAndSelector(false, "M_EXP", new SimpleExportPageController(cc), Selector.EXCEL_WEB_QUERY_EXPORT) ; // $NON-NLS$

        addControllerCheckJson(false, "B_AC", ApplicationConfigForm.INSTANCE); // $NON-NLS-0$
//        addControllerCheckJson(false, "B_PW", PW_CHANGE_CONTROLLER);

        addControllerCheckJson(false, "N_UB", new OverviewNWSController(cc)); // $NON-NLS-0$

        addControllerCheckJson(false, "N_D", new NewsDetailController(cc)); // $NON-NLS-0$
        addControllerCheckJson(false, "N_S", FinderNews.INSTANCE); // $NON-NLS-0$

        addControllerCheckJson(false, "P_MER", new PortraitMERController(cc)); // $NON-NLS-0$

        addControllerCheckJson(false, "P_CUR", new PortraitCURController(cc)); // $NON-NLS-0$
        addControllerCheckJson(false, "P_CUR_DR", new SimpleOverviewController(cc, "CurrencyCalculator(row=0;col=0;betrag=1;isocodeFrom=EUR;isocodeTo=EUR;title=Devisenrechner)")); // $NON-NLS-0$ $NON-NLS-1$
        addControllerCheckJson(false, "M_S", new PriceSearchController(cc)); // $NON-NLS$

        if(!this.sessionData.isIceDesign()) {
            addControllerCheckJson(false, "B_A", MyspaceController.getInstance()); // $NON-NLS$
        }

        if(this.sessionData.isIceDesign()) {
            addControllerCheckJson(true, "H_CS", SimpleHtmlController.createVwdCustomerServiceInfo(cc)); // $NON-NLS$
            addControllerCheckJson(false, "H_TOU", new HtmlContentController(cc, I18n.I.termsOfUseFilename())); // $NON-NLS$
        }
    }

    @Override
    protected InitSequenceProgressBox createProgressBox() {
        return this.sessionData.isIceDesign()
                ? new InitSequenceProgressBoxAs()
                : super.createProgressBox();
    }

    @Override
    protected MenuModel initMenuModel() {
        return new MetalsMenuBuilder().getModel();
    }

    @Override
    public ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy() {
        return MMF_WEB_PASSWORD_STRATEGY;
    }
}
