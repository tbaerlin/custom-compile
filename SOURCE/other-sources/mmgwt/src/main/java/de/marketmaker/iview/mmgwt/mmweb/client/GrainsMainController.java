/*
 * GrainsMainController.java
 *
 * Created on 17.03.2008 14:58:41
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.as.AsChangePasswordPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitCURController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitFUTController;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.PortraitMERController;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ArbitrageSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartcenterSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurablePriceListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.RatiosSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SimpleHtmlSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.StaticDataSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBoxAs;

/**
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
public class GrainsMainController extends AbstractMainController {
    private static final ChangePasswordDisplay.MmfWebPasswordStrategy MMF_WEB_PASSWORD_STRATEGY = new ChangePasswordDisplay.MmfWebPasswordStrategy();

    public GrainsMainController() {
    }

    public GrainsMainController(String contextPath) {
        super(contextPath);
    }

    @Override
    protected void initSnippetClasses() {
        SnippetClass.addClass(new ArbitrageSnippet.Class());
        SnippetClass.addClass(new ChartcenterSnippet.Class());
        SnippetClass.addClass(new ConfigurablePriceListSnippet.Class());
        SnippetClass.addClass(new ChartListSnippet.Class());
        SnippetClass.addClass(new NewsEntrySnippet.Class());
        SnippetClass.addClass(new NewsHeadlinesSnippet.Class());
        SnippetClass.addClass(new PortraitChartSnippet.Class());
        SnippetClass.addClass(new PriceTeaserSnippet.Class());
        SnippetClass.addClass(new PriceSnippet.Class());
        SnippetClass.addClass(new RatiosSnippet.Class());
        SnippetClass.addClass(new SimpleHtmlSnippet.Class());
        SnippetClass.addClass(new StaticDataSnippet.Class());
    }

    @Override
    public AbstractMainView createView() {
        return this.sessionData.isIceDesign()
                ? new MainView(new DefaultTopToolbar(new RightLogoSupplier()).forGrains(), Ginjector.INSTANCE.getSouthPanel(), this.sessionData)
                : new LegacyGrainsMainView(this);
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Override
    public boolean isWithStoreAppConfig() {
        return false;
    }

    @Override
    protected void initControllers() {
        final AbstractMainView cc = this.getView();

        addControllerCheckJson(false, "GS_G", new SimpleOverviewController(cc, "ov_g")); // $NON-NLS-0$ $NON-NLS-1$
        addControllerCheckJson(false, "GS_F", new SimpleOverviewController(cc, "ov_f")); // $NON-NLS-0$ $NON-NLS-1$
        addControllerCheckJson(false, "GS_CUR", new SimpleOverviewController(cc, "ov_cur")); // $NON-NLS-0$ $NON-NLS-1$

        addControllerCheckJson(false, VwdPageController.KEY, new VwdPageController(cc));

        addControllerCheckJson(false, "B_AC", ApplicationConfigForm.INSTANCE); // $NON-NLS-0$
        if (this.sessionData.isIceDesign()) {
            if (getPasswordStrategy().isShowViewAvailable()) {
                addControllerCheckJson(false, "B_PW", new AsChangePasswordPageController(cc)); // $NON-NLS$
            }
        }
        else {
            addControllerCheckJson(false, "B_PW", MainControllerInitializerCommand.PW_CHANGE_CONTROLLER); // $NON-NLS$
        }

        addControllerCheckJson(false, "N_UB", new OverviewNWSController(cc)); // $NON-NLS-0$
        addControllerCheckJson(false, "N_D", new NewsDetailController(cc)); // $NON-NLS-0$

        addControllerCheckJson(false, "P_MER", new PortraitMERController(cc)); // $NON-NLS-0$
        addControllerCheckJson(false, "P_CUR", new PortraitCURController(cc)); // $NON-NLS-0$
        addControllerCheckJson(false, "P_FUT", new PortraitFUTController(cc)); // $NON-NLS-0$
        addControllerCheckJson(false, "P_UND", new PortraitFUTController(cc)); // $NON-NLS-0$

        addControllerCheckJson(false, "M_S", new PriceSearchController(cc)); // $NON-NLS$

        if(this.sessionData.isIceDesign()) {
            addControllerCheckJson(true, "H_CS", SimpleHtmlController.createVwdCustomerServiceInfo(cc));  // $NON-NLS$
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
        return new GrainsMenuBuilder().getModel();
    }

    @Override
    public ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy() {
        return MMF_WEB_PASSWORD_STRATEGY;
    }
}
