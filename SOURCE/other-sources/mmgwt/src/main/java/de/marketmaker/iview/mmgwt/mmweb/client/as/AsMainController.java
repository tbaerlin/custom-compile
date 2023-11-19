/*
 * AsMainController.java
 *
 * Created on 11.12.12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.Ginjector;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.DefaultTopToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.MenuBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.MenuModel;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.ConfigDao;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.UpdatePortfolioMenuMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.UpdateWatchlistMenuMethod;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBoxAs;

import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class AsMainController extends AbstractMainController<MainView> {
    private static final ChangePasswordDisplay.Presenter.PasswordStrategy PM_WEB_PASSWORD_STRATEGY = new ChangePasswordDisplay.PmWebPasswordStrategy();

    private final PmSessionTimeoutWatchdog pmSessionTimeoutWatchdog = new PmSessionTimeoutWatchdog();

    private MenuModel privacyModeMenuModel = null;

    private MenuModel nonPrivacyModeMenuModel = null;

    private HistoryToken privacyModeStartPage = null;

    private HistoryThreadManager.HistoryThreadManagerMemento prePrivacyModeThreadState = null;

    public AsMainController() {
        super("/pmxml-1"); // $NON-NLS$
    }

    @Override
    public void onEnterPrivacyMode() {
        doOnBeforeEnterPrivacyMode();
        PrivacyMode.INSTANCE.setActive(true, this::doOnAfterEnterPrivacyMode);
    }

    private void doOnBeforeEnterPrivacyMode() {
        if (!(this.currentPageController instanceof IsPrivacyModeProvider)) {
            throw new IllegalStateException("current page controller does not implement PrivacyModeProvider");  // $NON-NLS$
        }
        final Integer currentHid = this.currentHistoryItem.getHid();
        if (currentHid == null) {
            throw new IllegalStateException("current HID must not be null");  // $NON-NLS$
        }

        final PrivacyModeProvider privacyModeProvider = ((IsPrivacyModeProvider) this.currentPageController).asPrivacyModeProvider();

        PrivacyMode.INSTANCE.setAllowedObjectIds(privacyModeProvider.getObjectIdsAllowedInPrivacyMode());
    }

    private void doOnAfterEnterPrivacyMode() {
        if (!(this.currentPageController instanceof IsPrivacyModeProvider)) {
            throw new IllegalStateException("current page controller does not implement PrivacyModeProvider");  // $NON-NLS$
        }

        final PrivacyModeProvider privacyModeProvider = ((IsPrivacyModeProvider) this.currentPageController).asPrivacyModeProvider();

        this.privacyModeStartPage = privacyModeProvider.createPrivacyModeStartPageToken();

        final int earliestHid = PlaceChangeEvent.getLastAutoIncId();
        this.placeManager.setCheckForEarliestHid(earliestHid, this.privacyModeStartPage.toString());

        this.prePrivacyModeThreadState = this.getHistoryThreadManager().replaceThreads(privacyModeProvider.getPrivacyModeEntryToken());

        this.nonPrivacyModeMenuModel = getMenuModel();
        if (this.privacyModeMenuModel == null) {
            this.privacyModeMenuModel = new MenuBuilder().getModel();
        }
        updateStaticMenuModelWithDynamicStuff(this.privacyModeMenuModel);
        setMenuModel(this.privacyModeMenuModel);

        this.getView().updateAndSyncMenuModelAndModuleIcons(true, privacyModeProvider.getPrivacyModeCustomerToken(), "cg-customer"); // $NON-NLS$
        this.getView().setPrivacyMode(true);
    }

    @Override
    public void onLeavePrivacyMode() {
        getView().showQuestionToLeavePrivacyMode(() -> PrivacyMode.INSTANCE.setActive(false, this::doLeavePrivacyMode));
    }

    private void doLeavePrivacyMode() {
        this.placeManager.resetCheckForEarliestHid();
        setMenuModel(this.nonPrivacyModeMenuModel);
        updateStaticMenuModelWithDynamicStuff(this.nonPrivacyModeMenuModel);
        this.getView().updateAndSyncMenuModelAndModuleIcons(false, null, null);

        this.getHistoryThreadManager().replaceThreads(this.prePrivacyModeThreadState);
        this.prePrivacyModeThreadState = null;

        PrivacyMode.INSTANCE.resetAllowedObjectIds();
        this.privacyModeStartPage = null;

        final HistoryToken historyToken = this.getHistoryThreadManager().getActiveThreadHistoryItem().getPlaceChangeEvent().getHistoryToken();
        historyToken.fire();

        this.getView().setPrivacyMode(false);
    }

    public AsMainController(String contextPath) {
        super(contextPath);
    }

    @Override
    protected void setupView() {
        super.setupView();
        this.pmSessionTimeoutWatchdog.start();
    }

    @Override
    protected void initRequestAuthentication() {
        // chart requests are submitted to a dmxml-1 backend w/o a session, so add auth
        final String credentials = this.sessionData.getCredentials();
        if (credentials != null) {
            ChartUrlFactory.setCredentials(credentials);
        }
    }

    @Override
    protected InitSequenceProgressBox createProgressBox() {
        return new InitSequenceProgressBoxAs();
    }

    @Override
    public boolean withIcePushButton() {
        return false;
    }

    @Override
    protected void initSnippetClasses() {
        Ginjector.INSTANCE.getSnippetInitializer().execute();
    }

    protected void initControllers() {
        Ginjector.INSTANCE.getMainControllerInitializer().execute();
    }

    @Override
    public MainView createView() {
        return new MainView(new DefaultTopToolbar(new AsRightLogoSupplier()), Ginjector.INSTANCE.getSouthPanel(), this.sessionData);
    }

    protected MenuModel initMenuModel() {
        this.nonPrivacyModeMenuModel = new MenuBuilder().getModel();
        updateStaticMenuModelWithDynamicStuff(this.nonPrivacyModeMenuModel);
        return this.nonPrivacyModeMenuModel;
    }

    private void updateStaticMenuModelWithDynamicStuff(MenuModel menuModel) {
        new UpdateWatchlistMenuMethod().withMenuModel(menuModel).doNotUpdateView().execute();
        new UpdatePortfolioMenuMethod().withMenuModel(menuModel).doNotUpdateView().execute();
    }

    @Override
    protected List<Command> initInitCommands() {
        final List<Command> commands = super.initInitCommands();
        commands.add(PmWebModule.createLoader());
        commands.add(this.enableStatisticFunction);
        return commands;
    }

    @Override
    protected void addMmfLoaders(List<Command> list) {
        if (SessionData.isWithMarketData()) {
            super.addMmfLoaders(list);
        }
    }

    @Override
    protected void delegatePlaceChange(PageController pageController, PlaceChangeEvent e) {
        super.delegatePlaceChange(pageController, e);

        if (pageController instanceof IsPrivacyModeProvider) {
            ((IsPrivacyModeProvider) pageController).asPrivacyModeProvider()
                    .requestPrivacyModeActivatable(activatable ->
                            getView().setPrivacyModeEnabled(PrivacyMode.isActive() || activatable));
        }
        else {
            getView().setPrivacyModeEnabled(PrivacyMode.isActive());
        }
    }

    @Override
    public ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy() {
        return PM_WEB_PASSWORD_STRATEGY;
    }

    public String getStartPage() {
        if (PrivacyMode.isActive()) {
            return this.privacyModeStartPage.toString();
        }

        final List<DashboardConfig> listGlobalDashboards = ConfigDao.getInstance().getConfigsByRole(ConfigDao.DASHBOARD_ROLE_GLOBAL);
        if (!listGlobalDashboards.isEmpty()) {
            return DashboardPageController.HISTORY_TOKEN_DASHBOARDS;
        }

        return this.defaultStartPage;
    }

    @Override
    public void logoutExpiredSession() {
        logoutExpiredSession(false);
    }

    @Override
    public void logout() {
        fireShutdownEvent(true, true);
    }

    public PmSessionTimeoutWatchdog getPmSessionTimeoutWatchdog() {
        return pmSessionTimeoutWatchdog;
    }
}