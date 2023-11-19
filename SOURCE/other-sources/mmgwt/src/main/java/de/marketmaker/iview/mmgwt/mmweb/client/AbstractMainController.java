/*
 * AbstractMainController.java
 *
 * Created on 29.10.12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.Dictionary;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsChangePasswordWindowView;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RefreshDmxmlContextEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RequestCompletedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ResponseReceivedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ShutdownHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigSavedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryPlaceManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadLogWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceStore;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushModule;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.ServiceStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.UsageStatistics;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.VwdPageSearch;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Finalizer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LogWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PrintWindow;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SymbolUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsProvidesPrintableView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasNavWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ProvidesContentHeader;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InitSequenceProgressBoxLegacy;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SimpleGlassableLayoutPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.MarketsWorkspace;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.PortfolioWorkspace;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WatchlistWorkspace;

/**
 * @author Ulrich Maurer
 */
public abstract class AbstractMainController<V extends AbstractMainView>
        implements PlaceChangeHandler, ControllerRegistry, ShutdownHandler {
    public static AbstractMainController<?> INSTANCE;

    private final HashMap<String, PageController> controllers = new HashMap<>();

    private final ArrayList<Dependency<PageController>> pageControllerDependencies = new ArrayList<>();

    /**
     * If a controller is invoked with additional parameters, the history string is stored in this
     * map (i.e., "P_STK/1.qid/T" would be added as entry "P_STK" => "P_STK/1.qid/T").
     * If that controller is then called without params (e.g., by calling #selectionChanged), the latest
     * known parameters are appended to the call
     */
    private final HashMap<String, String> controllerParams = new HashMap<>();

    public final String contextPath;

    /**
     * Menu model, may be null for subclasses!
     */
    private MenuModel model;

    private final List<MainControllerListener> listeners = new ArrayList<>();

    // set to true if selection is made by menu (and not by history) und thus the menu is already set correctly
    // relevant for items with id!=controllerId

    private boolean forceChangeSelection = true;

    private boolean firstPageShown = false;

    private V view;

    protected PageController currentPageController;

    protected HistoryItem currentHistoryItem;

    protected String defaultStartPage;

    /**
     * A series of commands that have to be executed in this order after login succeeded.
     * Each Command is expected to call {@link #runInitSequence()} on completion to
     * execute the next command.
     */
    private List<Command> initCommands;

    private int nextInitCommand = 0;

    protected final Command loadStylesheetsInitFunction = () -> {
        updateProgress(I18n.I.loadStylesheets());
        loadStylesheets();
        runInitSequence();
    };

    protected final Command enableStatisticFunction = () -> {
        final JSONWrapper stats = this.sessionData.getGuiDef("stats"); // $NON-NLS$
        if (stats.isValid() && "true".equals(stats.stringValue())) { // $NON-NLS$
            ServiceStatistics.getOrCreate().reset();
        }
        runInitSequence();
    };

    private InitSequenceProgressBox progressBox;

    private DmxmlContext.Block<MSCPriceData> searchBlock;

    protected HistoryPlaceManager placeManager;

    private HistoryThreadManager historyThreadManager;

    private HandlerRegistration closingHandlerReg;

    private List<String> pendingErrors;

    protected final Logger logger = Ginjector.INSTANCE.getLogger(); // migrate to constructor injection

    protected final SessionData sessionData = Ginjector.INSTANCE.getSessionData();  // migrate to constructor injection

    protected final FeatureFlags featureFlags = Ginjector.INSTANCE.getFeatureFlags(); // migrate to constructor injection

    protected AbstractMainController() {
        this("/dmxml-1"); // $NON-NLS$
    }

    protected AbstractMainController(String contextPath) {
        assert INSTANCE == null;
        INSTANCE = this;
        this.contextPath = contextPath;
        this.closingHandlerReg = registerWindowClosingHandler();
    }

    @Override
    public void onShutdown(final ShutdownEvent event) {
        //shutdown has been triggered by someone else. don't fire again.
        this.closingHandlerReg.removeHandler();
        serverLogout(event);
    }

    private void serverLogout(final ShutdownEvent shutdownEvent) {
        //execute logout deferred because of session invalidation
        Scheduler.get().scheduleDeferred(() -> UserServiceAsync.App.getInstance().logout(new AsyncCallback<Void>() {
            public void onFailure(Throwable throwable) {
                onAfterLogout(shutdownEvent);
            }

            public void onSuccess(Void o) {
                onAfterLogout(shutdownEvent);
            }
        }));
    }

    private HandlerRegistration registerWindowClosingHandler() {
        return Window.addWindowClosingHandler(event -> fireShutdownEvent(true, false));
    }

    public void onEnterPrivacyMode() {

    }

    public void onLeavePrivacyMode() {

    }

    public String getHeaderSeparator() {
        if(this.sessionData.isIceDesign()) {
            return " 〉 ";
        }
        else {
            return " :: ";
        }
    }

    public MenuModel getMenuModel() {
        return this.model;
    }

    protected void setMenuModel(MenuModel menuModel) {
        this.model = menuModel;
    }

    protected abstract V createView();

    public V getView() {
        if (this.view == null) {
            this.view = createView();
            if ("true".equals(this.sessionData.getUserProperty("showLogOnInit"))) { // $NON-NLS$
                LogWindow.show();
            }
        }
        return this.view;
    }

    public boolean isAnonymous() {
        return false;
    }

    public boolean isWithStoreAppConfig() {
        return true;
    }

    public void addListener(MainControllerListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    @SuppressWarnings("unused")
    public void removeListener(MainControllerListener listener) {
        this.listeners.remove(listener);
    }

    public void logout() {
        Dialog.confirm(I18n.I.closeApplicationConfirm(), () -> {
            fireShutdownEvent(true, true);
        });
    }

    public void logoutExpiredSession() {
        logoutExpiredSession(true);
    }

    private boolean logoutDialogShowing = false;

    protected void logoutExpiredSession(final boolean storeSession) {
        if (this.logoutDialogShowing) {
            return;
        }
        this.logoutDialogShowing = true;
        Dialog.getImpl().createDialog()
                .withTitle(I18n.I.sessionExpired())
                .withImage("dialog-warning") // $NON-NLS$
                .withMessage(I18n.I.closeApplication())
                .withDefaultButton(I18n.I.ok(), null)
                .withCloseButton()
                .withCloseCommand(() -> fireShutdownEvent(storeSession, true))
                .show();
    }

    public void onPlaceChange(PlaceChangeEvent e) {
        final HistoryToken historyToken = e.getHistoryToken();
        this.logger.debug("<AbstractMainController.onPlaceChange> " + historyToken); // $NON-NLS$
        if (this.view == null) {
            return; // view will be null after logout, nothing to do
        }

        MmwebServiceAsyncProxy.cancelPending();
        maybeShowLogWindow(historyToken);

        final String controllerId = historyToken.getControllerId();
        if (maybeGoToStartPage(controllerId)) {
            return;
        }

        if (historyToken.getAllParamCount() > 1) {
            String savedToken = historyToken.get("savedToken"); // $NON-NLS$
            this.controllerParams.put(controllerId, savedToken != null ? savedToken : historyToken.toString());
        }

        final PageController tmpController = this.controllers.get(controllerId);
        final PageController pageController;
        final PlaceChangeEvent event;
        if (tmpController != null) {
            pageController = tmpController;
            event = e;
        }
        else {
            final MenuModel.Item item = this.model.getElement(controllerId);
            if (item == null) {
                pageController = null;
                event = e;
            }
            else {
                this.logger.debug("<AbstractMainController.onPlaceChange> Controller with ID '" + controllerId + "' not found. Use controller ID of menu item: " + item.getControllerId());
                event = new PlaceChangeEvent(e, item.getControllerId());
                pageController = this.controllers.get(event.getHistoryToken().getControllerId());
            }
        }

        if (this.currentPageController != null && this.currentHistoryItem != null) {
            if (this.currentPageController != pageController) {
                deactivatePageController(this.currentPageController);
                this.currentPageController = null;
            }
        }

        if (pageController == null || !isValidToken(controllerId)) {
            setErrorContent(controllerId);
            return;
        }

        syncMenuModelAndView(historyToken);

        this.forceChangeSelection = false;

        if (pageController != this.currentPageController) {
            activatePageController(pageController);
        }

        this.currentPageController = pageController;
        this.currentHistoryItem = this.historyThreadManager.getActiveThreadHistoryItem();
        delegatePlaceChange(this.currentPageController, event);
        SafeHtml contentHeader = null;
        if (this.currentPageController instanceof ProvidesContentHeader) {
            contentHeader = ((ProvidesContentHeader) this.currentPageController).getContentHeader();
        }
        if (contentHeader == null) {
            contentHeader = getContentHeader(controllerId);
        }
        setContentHeader(contentHeader, this.currentPageController);

        this.view.getTopToolbar().updatePdfButtonState();
        this.view.getTopToolbar().setPrintButtonEnabled(isPrintable());

        if (!this.firstPageShown) {
            this.firstPageShown = true;
            onAfterFirstPage();
        }
    }

    protected void setContentHeader(SafeHtml contentHeader, PageController pageController) {
        if(this.sessionData.isIceDesign()) {
            if (!(pageController instanceof HasNavWidget) || !((HasNavWidget) pageController).providesContentHeader()) {
                getView().setContentHeader(contentHeader);
            }
        }
        else {
            getView().setContentHeader(contentHeader);
        }
    }

    private void setErrorContent(String controllerId) {
        DebugUtil.logToServer("unsupported history token: " + controllerId); // $NON-NLS$
        this.view.setContentHeader(I18n.I.error());
        this.view.setContent(new HTML(
                new SafeHtmlBuilder()
                        .appendHtmlConstant("<font color=\"red\"><b>") // $NON-NLS$
                        .appendEscaped(I18n.I.unsupportedContent(controllerId))
                        .appendHtmlConstant("</b></font>") // $NON-NLS$
                        .toSafeHtml()
        ));
    }


    private boolean maybeGoToStartPage(final String controllerId) {
        if ("B_S".equals(controllerId) || !StringUtil.hasText(controllerId)) { // $NON-NLS$
            final String startPage = getStartPage();
            if (!StringUtil.hasText(startPage)) {
                return true;
            }
            PlaceUtil.goTo(startPage);
            return true;
        }
        return false;
    }

    private void maybeShowLogWindow(HistoryToken historyToken) {
        final String tokenLog = historyToken.get("log"); // $NON-NLS$
        if (tokenLog != null) {
            if ("true".equals(tokenLog)) { // $NON-NLS$
                LogWindow.show();
            }
            else if ("user".equals(tokenLog)) { // $NON-NLS$
                LogWindow.showUserData();
            }
        }
    }

    protected void deactivatePageController(final PageController pageController) {
        pageController.deactivate();
    }

    protected void activatePageController(PageController pageController) {
        pageController.activate();
    }

    protected void delegatePlaceChange(PageController pageController, PlaceChangeEvent e) {
        pageController.onPlaceChange(e);

        if (pageController instanceof HasNavWidget && getView() instanceof MainView) {
            final MainView view = (MainView) getView();
            final SimpleGlassableLayoutPanel panel;
            final IsWidget oldNavWidget = view.getNavWidget();
            if (oldNavWidget instanceof SimpleGlassableLayoutPanel) {
                panel = (SimpleGlassableLayoutPanel) oldNavWidget;
            }
            else {
                panel = new SimpleGlassableLayoutPanel();
                panel.setGlassStyleName("as-transparentGlassPanel"); // $NON-NLS$
                panel.setWidget(oldNavWidget);
                view.setNavWidget(panel);
            }


            ((HasNavWidget) pageController).requestNavWidget(new HasNavWidget.NavWidgetCallback() {
                @Override
                public void setNavWidget(NavigationWidget widget) {
                    if (widget != null) {
                        panel.setWidget(widget);
                    }
                    else {
                        view.setNavWidget(null);
                    }
                }

                @Override
                public void showGlass() {
                    panel.showGlass(100);
                }
            });
        }
    }

    protected void syncMenuModelAndView(HistoryToken historyToken) {
        final MenuModel.Item selectedItem = this.model.getSelectedItem();
        final MenuModel.Item newSelection = this.model.select(historyToken.getControllerId());
        if (this.forceChangeSelection || (selectedItem != newSelection)) {
            this.logger.debug(this.forceChangeSelection + " -> " + Arrays.asList(this.model.getSelectedIds()));
            final PageController pageController = this.controllers.get(newSelection.getId());
            this.view.changeSelection(this.model.getSelectedIds(), !(pageController != null && pageController instanceof HasNavWidget));
        }
        else {
            this.logger.warn("syncMenuModelAndView -> force: " + false + "  selectedItem: " + selectedItem.getName() + "   newSelection: " + newSelection.getName());
        }
    }

    public boolean isValidToken(String token) {
        return this.model.getElement(token) != null;
    }

    public void refresh() {
        DmxmlContext.refreshRequested = true;

        String guidefImportTrigger = this.sessionData.getGuiDef("guidefs-import-trigger").stringValue(); // $NON-NLS$
        if ("manual".equals(guidefImportTrigger)) { // $NON-NLS$
            new GuiDefsUpdater().execute();
        }

        if (this.currentPageController != null) {
            this.currentPageController.refresh();
        }
        if (!this.sessionData.isIceDesign()) {
            MarketsWorkspace.INSTANCE.refresh();
            WatchlistWorkspace.INSTANCE.refresh();
            PortfolioWorkspace.INSTANCE.refresh();
        }
        if(this.sessionData.isIceDesign()) {
            EventBusRegistry.get().fireEvent(new RefreshDmxmlContextEvent());
        }

        DmxmlContext.refreshRequested = false;
    }

    public String getStartPage() {
        final AppConfig config = this.sessionData.getUser().getAppConfig();
        String result = config.getProperty(AppConfig.PROP_KEY_STARTPAGE);
        String log = "AppConfig." + AppConfig.PROP_KEY_STARTPAGE + ": '" + result + "'"; // $NON-NLS$
        if (!isValidStartPage(result)) {
            result = this.defaultStartPage;
            log = "defaultStartPage: '" + result + "'"; // $NON-NLS$
            if (!isValidStartPage(result)) {
                result = this.model.getFirstItem().getControllerId();
                log = "first menu model item: '" + result + "'"; // $NON-NLS$
            }
            config.addProperty(AppConfig.PROP_KEY_STARTPAGE, result);
            log += "; AppConfig." + AppConfig.PROP_KEY_STARTPAGE + ": '" + result + "'"; // $NON-NLS$
        }

        this.logger.info("<AbstractMainController.getStartPage> " + log); // $NON-NLS$

        // Removes any HIDs present in the default page string
        return HistoryToken.fromToken(result).toString();
    }

    private boolean isValidStartPage(String token) {
        if (token == null) {
            this.logger.debug("<AbstractMainController.isValidStartpage> token is null, is valid? false");
            return false;
        }
        final int pos = token.indexOf('/');
        final String pageToken = pos < 0 ? token : token.substring(0, pos);
        final boolean isModelElement = this.model.getElement(pageToken) != null;
        final boolean isController = this.controllers.get(pageToken) != null;
        final boolean result = isModelElement || isController;

        this.logger.debug("<AbstractMainController.isValidStartpage> token: '" + token + "', pos of '/': " + pos + ", pageToken: '" + pageToken + "', is menu model element? " + isModelElement + ", is controller? " + isController + ", is valid? " + result);

        return result;
    }

    public void onModuleLoad() {
        initEventBus();

        try {
            final Dictionary theme = Dictionary.getDictionary("theme"); // $NON-NLS$
            final String stylesheet = theme.get("stylesheet"); // $NON-NLS$
            if (stylesheet != null) {
                DOMUtil.loadStylesheet(stylesheet);
            }
        } catch (Exception e) {
            // ignore
        }

        // set uncaught exception handler
        GWT.setUncaughtExceptionHandler(t -> {
            final String message = "Uncaught Exception: " + t.getMessage(); // $NON-NLS$
            this.logger.error(message, t);
            DebugUtil.logToServer(message, t);
            DebugUtil.showDeveloperNotification("Uncaught Exception", t); // $NON-NLS$
        });

        // use a deferred command so that the handler catches onModuleLoad2() exceptions
        Scheduler.get().scheduleDeferred(getOnModuleLoadCommand());
    }

    private void initEventBus() {
        final HandlerManager eventBus = new HandlerManager(null);
        EventBusRegistry.set(eventBus);
        // ORDER IS IMPORTANT!!! HistoryThreadManager must be FIRST!
        this.historyThreadManager = new HistoryThreadManager(eventBus);
        this.placeManager = new HistoryPlaceManager(eventBus, this.historyThreadManager);
        eventBus.addHandler(PlaceChangeEvent.getType(), this);

        EventBusRegistry.get().addHandler(ShutdownEvent.getType(), this);
        if(isWithStoreAppConfig()) {
            EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), createConfigChangeLogger());
            EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), new AppConfigAutoSaveHandler(this));
        }
    }

    private ConfigChangedHandler createConfigChangeLogger() {
        final Finalizer<Integer> counter = new Finalizer<>(0);
        return event -> {
            int count = counter.get() + 1;
            counter.set(count);
            final Object oldValue = event.getOldValue();
            this.logger.info("<AbstractMainController..ConfigChangeLogger> [" + count + "] " + event.getProperty() + "=" + event.getNewValue() + (oldValue != null ? " was " + oldValue : ""));
        };
    }

    protected Command getOnModuleLoadCommand() {
        return LoginView::new;
    }

    public void search(String ctrlKey, String query) {
        search(ctrlKey, query, null);
    }

    public void search(String ctrlKey, String query, HistoryContext context) {
        if (TopToolbar.WP_SEARCH_KEY.equals(ctrlKey) && query.endsWith(".qid")) { // $NON-NLS$
            qidSearch(query);
        }
        else if (TopToolbar.WP_SEARCH_KEY.equals(ctrlKey) && query.indexOf(".") > 0 && !query.contains(" ")) { // $NON-NLS$
            vwdcodeSearch(query);
        }
        else if (TopToolbar.WP_SEARCH_KEY.equals(ctrlKey)
                && (SymbolUtil.isIsin(query) || SymbolUtil.isWkn(query))) { // we assume 6 chars is a WKN
            isinSearch(query, context);
        }
        else if (VwdPageController.KEY.equals(ctrlKey) && !query.matches("(s=)?\\d+") && Selector.SEARCH_VWD_PAGES.isAllowed()) { // $NON-NLS$
            // search for pages containing the entered text
            INSTANCE.quickSearch(VwdPageSearch.TEXT_SEARCH_KEY, query);
        }
        else {
            INSTANCE.quickSearch(ctrlKey, query);
        }
    }

    private void isinSearch(final String isin, final HistoryContext context) {
        final DmxmlContext.Block<MSCPriceData> searchBlock = getSearchBlock();
        searchBlock.setParameter("symbol", isin); // $NON-NLS$
        searchBlock.removeParameter("symbolStrategy"); // $NON-NLS$
        searchBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (searchBlock.isResponseOk()) {
                    final MSCPriceData element = searchBlock.getResult();
                    if (context == null) {
                        PlaceUtil.goToPortrait(element.getInstrumentdata(), element.getQuotedata());
                    }
                    else {
                        PlaceUtil.goToPortrait(element.getInstrumentdata(), element.getQuotedata(), context);
                    }
                }
                else {
                    quickSearch(TopToolbar.WP_SEARCH_KEY, isin);
                }
            }
        });
    }

    private DmxmlContext.Block<MSCPriceData> getSearchBlock() {
        if (this.searchBlock == null) {
            this.searchBlock = new DmxmlContext().addBlock("MSC_PriceData"); // $NON-NLS$
        }
        return this.searchBlock;
    }

    private void qidSearch(final String qid) {
        final DmxmlContext.Block<MSCPriceData> searchBlock = getSearchBlock();
        searchBlock.setParameter("symbol", qid); // $NON-NLS$
        searchBlock.removeParameter("symbolStrategy"); // $NON-NLS$
        searchBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (searchBlock.isResponseOk()) {
                    final MSCPriceData element = searchBlock.getResult();
                    PlaceUtil.goToPortrait(element.getInstrumentdata(), element.getQuotedata());
                }
                else {
                    INSTANCE.showError(qid + I18n.I.notFound());
                }
            }
        });
    }

    private void vwdcodeSearch(final String vwdcode) {
        final DmxmlContext.Block<MSCPriceData> searchBlock = getSearchBlock();
        searchBlock.setParameter("symbol", vwdcode.toUpperCase()); // $NON-NLS$
        searchBlock.setParameter("symbolStrategy", "vwdcode"); // $NON-NLS$
        searchBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (searchBlock.isResponseOk()) {
                    final MSCPriceData element = searchBlock.getResult();
                    PlaceUtil.goToPortrait(element.getInstrumentdata(), element.getQuotedata());
                }
                else {
                    quickSearch(TopToolbar.WP_SEARCH_KEY, vwdcode);
                }
            }
        });
    }

    public void quickSearch(String ctrlKey, String query) {
        final PageController qsController = this.controllers.get(ctrlKey);
        if (qsController instanceof QuickSearchController) {
            ((QuickSearchController) qsController).quickSearch(ctrlKey, query);
        }
        else {
            PlaceUtil.goTo(StringUtil.joinTokens(ctrlKey, "s=" + query)); // $NON-NLS$
        }
    }

    public void showError(String html) {
        final SafeHtml error = SafeHtmlUtils.fromTrustedString(html);
        if (this.view != null) {
            this.view.showError(error);
        }
        else {
            if (this.pendingErrors == null) {
                this.pendingErrors = new ArrayList<>();
            }
            this.pendingErrors.add(html);
        }
    }

    public void showMessage(String html) {
        if (this.view != null) {
            this.view.showMessage(SafeHtmlUtils.fromTrustedString(html));
        }
    }

    public SafeHtml getContentHeader(String controllerId) {
        return this.model.getPath();
    }

    public void setCurrentAsStart() {
        final AppConfig config = this.sessionData.getUser().getAppConfig();
        if(this.sessionData.isIceDesign()) {
            config.addProperty(AppConfig.PROP_KEY_STARTPAGE, this.historyThreadManager.getActiveThreadHistoryItem().getPlaceChangeEvent().getHistoryToken().toString());
        }
        else {
            config.addProperty(AppConfig.PROP_KEY_STARTPAGE, this.placeManager.getCurrentPlace());
        }
        showMessage(I18n.I.currentPageSetAsStartPage());
    }

    public void goHome() {
        PlaceUtil.goTo(getStartPage());
    }

    public void selectionChanged(final String id) {
        MmwebServiceAsyncProxy.cancelPending();
        final PageController pageController = this.controllers.get(id);
        if (pageController != null && !pageController.supportsHistory()) {
            pageController.onPlaceChange(null);
            return;
        }
        this.forceChangeSelection = true;
        final MenuModel.Item selectedMenuItem = this.model.select(id);
        final String cid = selectedMenuItem.getControllerId();
        if (cid != null && !(cid.equals(selectedMenuItem.getId()) || cid.startsWith(selectedMenuItem.getId() + "/"))) {
            PlaceUtil.goTo(selectedMenuItem.getId());
            return;
        }
        final String cidWithParams = this.controllerParams.get(cid);
        String s = cidWithParams != null ? cidWithParams : cid;
        PlaceUtil.goTo(s);
    }

    public void fireShutdownEvent(boolean storeSession, boolean clearUrl) {
        EventBusRegistry.get().fireEvent(new ShutdownEvent(storeSession, clearUrl));
    }

    public void save() {
        if(!isWithStoreAppConfig()) {
            return;
        }
        save(createSaveCallback());
    }

    protected AsyncCallback<Void> createSaveCallback() {
        return this.sessionData.isIceDesign() ? createIceStoreSessionCallback() : createLegacyStoreSessionCallback();
    }

    protected void saveSilently() {
        if(!isWithStoreAppConfig()) {
            return;
        }
        save(createSilentSaveCallback());
    }

    private void save(final AsyncCallback<Void> saveCallback) {
        doStoreSession(saveCallback);
    }

    /**
     * @return A callback that does neither show a progress bar nor a success message.
     */
    protected AsyncCallback<Void> createSilentSaveCallback() {
        return new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable throwable) {
                showError(I18n.I.errorSavingSettings());
            }

            @Override
            public void onSuccess(Void aVoid) {
                logger.info("<AbstractMainController..SilentSaveCallback> settings successfully saved");
            }
        };
    }

    private AsyncCallback<Void> createIceStoreSessionCallback() {
        return new IceStoreSessionCallback() {
            @Override
            public void onFailure(Throwable throwable) {
                super.onFailure(throwable);
            }

            @Override
            public void onSuccess(Void o) {
                super.onSuccess(o);
            }
        };
    }

    private AsyncCallback<Void> createLegacyStoreSessionCallback() {
        return new LegacyStoreSessionCallback() {
            @Override
            public void onFailure(Throwable caught) {
                showError(I18n.I.errorSavingSettings());
            }

            @Override
            public void onSuccess(Void result) {
                showMessage(I18n.I.settingsSaved());
            }
        };
    }

    private void doStoreSession(final AsyncCallback<Void> delegate) {
        if(!isWithStoreAppConfig()) {
            return;
        }

        // do under no circumstance defer those calls. Due to AppConfigAutoSaveHandler, the calls
        // are in fact deferred.
        this.logger.debug("<AbstractMainController.doStoreSession>");  // $NON-NLS$
        for (MainControllerListener listener : this.listeners) {
            listener.beforeStoreState();
        }
        final User user = this.sessionData.getUser();
        UserServiceAsync.App.getInstance().storeUserConfig(user.getUid(), user.getAppConfig(),
                new AsyncCallback<Void>() {
                    public void onFailure(Throwable throwable) {
                        delegate.onFailure(throwable);
                        Scheduler.get().scheduleDeferred(() -> ConfigSavedEvent.fire(false));
                    }

                    public void onSuccess(Void o) {
                        view.getTopToolbar().ackSave();
                        delegate.onSuccess(o);
                        Scheduler.get().scheduleDeferred(() -> ConfigSavedEvent.fire(true));
                    }
                });
    }

    public boolean hasController(String id) {
        return this.controllers.containsKey(id);
    }

    public PageController addController(String key, PageController pageController) {
        for (Dependency<PageController> injector : this.pageControllerDependencies) {
            injector.inject(key, pageController);
        }
        return this.controllers.put(key, pageController);
    }

    void addControllerCheckJson(boolean jsonMenuElementNeeded, String[] menuItemIds,
            PageController value) {
        for (String menuItemId : menuItemIds) {
            addControllerCheckJson(jsonMenuElementNeeded, menuItemId, value);
        }
    }

    public void addControllerCheckJson(boolean jsonMenuElementNeeded, String menuItemId,
            PageController value) {
        if (jsonMenuElementNeeded ?
                Customer.INSTANCE.isJsonMenuElementTrue(menuItemId) :
                Customer.INSTANCE.isJsonMenuElementNotFalse(menuItemId)) {
            addController(menuItemId, value);
        }
    }

    void addControllerCheckSelector(String menuItemId, PageController value,
            Selector... selectors) {
        for (Selector selector : selectors) {
            if (!selector.isAllowed()) {
                return;
            }
        }
        addController(menuItemId, value);
    }

    void addControllerCheckJsonAndSelector(boolean jsonMenuElementNeeded, String menuItemId,
            PageController value,
            Selector... selectors) {
        if (jsonMenuElementNeeded ?
                Customer.INSTANCE.isJsonMenuElementTrue(menuItemId) :
                Customer.INSTANCE.isJsonMenuElementNotFalse(menuItemId)) {
            for (Selector selector : selectors) {
                if (!selector.isAllowed()) {
                    return;
                }
            }
            addController(menuItemId, value);
        }
    }

    private void onAfterLogout(ShutdownEvent shutdownEvent) {
        for (PageController controller : controllers.values()) {
            controller.destroy();
        }
        this.controllers.clear();

        if (this.view != null) {
            this.view.onLogout();
            this.view = null;
        }

        if (shutdownEvent.isClearUrl()) {
            PlaceUtil.goTo("");
        }
        Window.Location.reload();
    }

    public final void runInitSequence() {
        if (this.nextInitCommand == 0) {
            checkStartupAllowed();
            initRequestAuthentication();
            initPriceStore();
            onBeforeInit();
            this.initCommands = initInitCommands();

            this.progressBox = createProgressBox();
        }

        if (this.nextInitCommand < this.initCommands.size()) {
            this.initCommands.get(this.nextInitCommand++).execute();
        }
        else {
            if ("stopProgress".equals(this.placeManager.getCurrentPlace())) { // $NON-NLS$
                return;
            }
            this.progressBox.update("", this.nextInitCommand, this.initCommands.size());
            this.progressBox.close();
            this.initCommands = null;
            onAfterInit();
        }
    }

    protected InitSequenceProgressBox createProgressBox() {
        return new InitSequenceProgressBoxLegacy();
    }

    public boolean withIcePushButton() {
        return true;
    }

    protected void checkStartupAllowed() {
        // subclasses may override
    }

    protected void initRequestAuthentication() {
        // subclasses may override
    }

    void onInitFailed(String s) {
        this.progressBox.close();
        MessageBox.alert(I18n.I.error(), s, messageBoxEvent -> {
            // empty
        });
    }

    public void updateProgress(String s) {
        this.progressBox.update(s, this.nextInitCommand, this.initCommands.size());
    }


    protected List<Command> initInitCommands() {
        // easier to set this up now, as the user data will be available from SessionData which
        // simplifies constructors for the various functions
        final List<Command> list = new ArrayList<>(10);
        list.add(new GuiDefsLoader());
        list.add(new BrowserSpecific.BrowserSpecificLoader());
        addMmfLoaders(list);
        list.add(this.loadStylesheetsInitFunction);
        return list;
    }

    protected void addMmfLoaders(List<Command> list) {
        list.add(new ListLoader(I18n.I.loadPriceLists()));
        list.add(new UserDataLoader());
        list.add(new ServerContextLoader());
        list.add(AlertController.INSTANCE);
        list.add(PushModule.createLoader(this.sessionData));
    }

    protected void onBeforeInit() {
        final HandlerManager eventBus = EventBusRegistry.get();
        new UsageStatistics(eventBus);
        new UserAlertWatchdog(eventBus);
    }

    private HandlerManager initPriceStore() {
        final HandlerManager eventBus = EventBusRegistry.get();
        eventBus.addHandler(ResponseReceivedEvent.getType(), PriceStore.INSTANCE);
        eventBus.addHandler(RequestCompletedEvent.getType(), PriceStore.INSTANCE);
        return eventBus;
    }

    protected void onAfterInit() {
        if (this.sessionData.getUser().isPasswordChangeRequired()) {
            new ChangePasswordPresenter(
                    this.sessionData.isIceDesign() ? new AsChangePasswordWindowView() : new ChangePasswordView(),
                    getPasswordStrategy(),
                    o -> setupView()).show(false);
        }
        else {
            setupView();
        }
    }

    protected void onAfterFirstPage() {
        if (Selector.DZBANK_USER_MESSAGES.isAllowed()) {
            new UserMessageWatchdog(EventBusRegistry.get());
        }
        if (Selector.DZ_BANK_USER.isAllowed()) {
            new KapitalmarktFavoritenWatchdog(EventBusRegistry.get());
        }
    }

    private void loadStylesheets() {
        final String cssFilename = this.sessionData.getGuiDefValue("css-import", "filename"); // $NON-NLS$
        if (cssFilename != null) {
            DOMUtil.loadStylesheet(cssFilename);
        }
    }

    protected abstract MenuModel initMenuModel();

    protected void setupView() {
        this.defaultStartPage = this.sessionData.getGuiDef(Permutation.AS.isActive() ? "default_start_page_as" : "default_start_page").stringValue(); // $NON-NLS$
        initSnippetClasses();
        initControllers();
        this.model = initMenuModel();
        this.view.init();
        this.placeManager.fireCurrentPlace(getStartPage());
        initializeNativeFunctions();
        showPendingErrors();
    }

    protected abstract void initSnippetClasses();

    protected abstract void initControllers();

    protected native void initializeNativeFunctions() /*-{
        $wnd.mmSetHistory = @de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil::goTo(Ljava/lang/String;);
        $wnd.mmSearch = this.@de.marketmaker.iview.mmgwt.mmweb.client.MainController::search(Ljava/lang/String;Ljava/lang/String;);
    }-*/;

    private void showPendingErrors() {
        if (this.pendingErrors == null) {
            return;
        }
        for (String pendingError : this.pendingErrors) {
            showError(pendingError);
        }
    }

    public void print() {
        if (this.currentPageController instanceof HasIsProvidesPrintableView) {
            final Widget printView = ((HasIsProvidesPrintableView) this.currentPageController).getPrintView();
            if (printView != null) {
                PrintWindow.print(printView, this.currentPageController.getAdditionalStyleSheetsForPrintHtml());
                return;
            }
        }

        final String printHtml = this.currentPageController.getPrintHtml();
        if (printHtml != null) {
            PrintWindow.print(printHtml, this.currentPageController.getAdditionalStyleSheetsForPrintHtml());
        }
    }

    public PdfOptionSpec getPdfOptionSpec() {
        if (this.currentPageController == null) {
            return null;
        }
        return this.currentPageController.getPdfOptionSpec();
    }

    public boolean isPrintable() {
        return this.currentPageController != null && this.currentPageController.isPrintable();
    }

    public void addPdfPageParameters(Map<String, String> map) {
        if (this.currentPageController != null) {
            this.currentPageController.addPdfPageParameters(map);
        }
    }

    public void setDefaultWindowTitle() {
        getView().setDefaultWindowTitle();
    }

    public void setWindowTitlePrefix(String prefix) {
        getView().setWindowTitlePrefix(prefix);
    }

    public void updateLimitsIcon(String pending) {
        getView().getTopToolbar().updateLimitsIcon(pending);
    }

    public void next() {
        this.historyThreadManager.next();
    }

    public void back() {
        this.historyThreadManager.back();
    }

    public void openHistoryThreadLog() {
        HistoryThreadLogWindow.createHTLogWindow(this.historyThreadManager);
    }

    public HistoryThreadManager getHistoryThreadManager() {
        return this.historyThreadManager;
    }

    public abstract ChangePasswordDisplay.Presenter.PasswordStrategy getPasswordStrategy();

    public void addPageControllerDependency(Dependency<PageController> dependency) {
        this.pageControllerDependencies.add(dependency);
    }

    /**
     * @author mdick
     */
    private static final class AppConfigAutoSaveHandler implements ConfigChangedHandler {
        private final AbstractMainController mainController;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<Scheduler.RepeatingCommand> optionalWatchdog = Optional.empty();

        private boolean changed;

        private AppConfigAutoSaveHandler(AbstractMainController mainController) {
            this.mainController = mainController;
        }

        private Logger logger = Ginjector.INSTANCE.getLogger();

        @Override
        public void onConfigChange(ConfigChangedEvent event) {
            this.logger.info("<AppConfigAutoSaveHandler.onConfigChange> " + event.getProperty() + "=" + event.getNewValue());
            if (this.optionalWatchdog.isPresent()) {
                logger.debug("<AppConfigAutoSaveHandler.onConfigChange> watchdog running, setting changed flag");
                this.changed = true;
            }
            else {
                this.logger.debug("<AppConfigAutoSaveHandler.onConfigChange> starting watchdog");
                final Scheduler.RepeatingCommand repeatingCommand = () -> {
                    if (this.changed) {
                        this.logger.debug("<AppConfigAutoSaveHandler.onConfigChange..watchdog> resetting changed flag");
                        this.changed = false;
                        return true;
                    }
                    this.logger.debug("<AppConfigAutoSaveHandler.onConfigChange..watchdog> changed flag not touched... saving silently and stopping watchdog");
                    doSave();
                    this.optionalWatchdog = Optional.empty();
                    this.changed = false;
                    return false;
                };
                this.optionalWatchdog = Optional.of(repeatingCommand);
                Scheduler.get().scheduleFixedDelay(repeatingCommand, 50);
            }
        }

        private void doSave() {
            try {
                this.mainController.saveSilently();
            } catch (Exception e) {
                this.logger.warn("<AppConfigAutoSaveHandler> caught exception during save", e);
            }
        }
    }

    public interface Dependency<T> {
        void inject(String key, T target);
    }

    abstract static class LegacyStoreSessionCallback implements AsyncCallback<Void> {
        public LegacyStoreSessionCallback() {
            final MessageBox box = MessageBox.wait(I18n.I.progressAction(), I18n.I.saveSessionSettings(), I18n.I.pleaseWait());
            final Timer t = new Timer() {
                @Override
                public void run() {
                    box.close();
                }
            };
            t.schedule(500);
        }
    }

    public static class IceStoreSessionCallback implements AsyncCallback<Void> {
        protected final NotificationMessage message;

        protected final FlowPanel panel;

        public IceStoreSessionCallback() {
            this.panel = new FlowPanel();
            this.panel.add(new Label(I18n.I.pleaseWait()));
            this.message = Notifications.add(I18n.I.saveSessionSettings(), this.panel, 0d);
        }

        @Override
        public void onFailure(Throwable caught) {
            this.message.setProgress(1d);
            this.panel.clear();
            final Image image = IconImage.get("dialog-error-16").createImage(); // $NON-NLS$
            final Style style = image.getElement().getStyle();
            style.setFloat(Style.Float.LEFT);
            style.setMarginRight(1, Style.Unit.EM);
            this.panel.add(image);
            this.panel.add(new Label(I18n.I.errorSavingSettings()));
            this.message.requestStateDelayed(NotificationMessage.State.DELETED, 15);
        }

        @Override
        public void onSuccess(Void result) {
            this.message.setProgress(1d);
            this.panel.clear();
            this.panel.add(new Label(I18n.I.settingsSaved()));
            this.message.requestStateDelayed(NotificationMessage.State.DELETED, 5);
        }
    }
}