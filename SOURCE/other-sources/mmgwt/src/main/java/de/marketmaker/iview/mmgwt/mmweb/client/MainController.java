/*
 * MainController.java
 *
 * Created on 17.03.2008 14:58:41
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.UpdatePortfolioMenuMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.UpdateWatchlistMenuMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBoxAs;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.DzBankTeaserUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MainController extends AbstractMainController<AbstractMainView> {
    private static final ChangePasswordDisplay.MmfWebPasswordStrategy MMF_WEB_PASSWORD_STRATEGY = new ChangePasswordDisplay.MmfWebPasswordStrategy();

    public MainController() {
    }

    public MainController(String contextPath) {
        super(contextPath);
    }

    @Override
    protected void initSnippetClasses() {
        Ginjector.INSTANCE.getSnippetInitializer().execute();
    }

    protected void initControllers() {
        Ginjector.INSTANCE.getMainControllerInitializer().execute();
    }

    @Override
    protected void onAfterFirstPage() {
        super.onAfterFirstPage();
        DzBankTeaserUtil.fireTeaserUpdatedEvent();
    }

    @Override
    protected void checkStartupAllowed() {
        if (this.sessionData.isIceDesign() && !this.featureFlags.isEnabled0(FeatureFlags.Feature.ICE_DESIGN)) {
            Firebug.warn("ICE_DESIGN not allowed, use index.html instead");
            Window.Location.replace("index.html"); // $NON-NLS$
        }
    }

    @Override
    protected InitSequenceProgressBox createProgressBox() {
        return this.sessionData.isIceDesign()
                ? new InitSequenceProgressBoxAs()
                : super.createProgressBox();
    }

    @Override
    public AbstractMainView createView() {
        return this.sessionData.isIceDesign()
                ? new MainView(new DefaultTopToolbar(Customer.INSTANCE.getRightLogoSupplier()), Ginjector.INSTANCE.getSouthPanel(), this.sessionData)
                : new LegacyMainView(this);
    }

    protected MenuModel initMenuModel() {
        final MenuModel model = new MenuBuilder().getModel();
        if(this.sessionData.isIceDesign()) {
            new UpdateWatchlistMenuMethod().withMenuModel(model).doNotUpdateView().execute();
            new UpdatePortfolioMenuMethod().withMenuModel(model).doNotUpdateView().execute();
        }
        return model;
    }

    @Override
    public ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy() {
        return MMF_WEB_PASSWORD_STRATEGY;
    }
}
