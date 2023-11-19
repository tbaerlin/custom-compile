/*
 * AbstractMmTalkerController.java
 *
 * Created on 18.12.12 08:03
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.IsBrokingAllowedFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.OrderEntryGoToDelegate;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.ConfigComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.ConfigDao;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardStateChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.GetStateKeyException;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ThreadStateHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ThreadStateSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.AnalysisPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.LayoutNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWorkspaceCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWorkspaceHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PrivFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsTablePageController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityOverviewController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.InstanceLog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsProvidesPrintableView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasNavWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTree;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel;
import de.marketmaker.iview.pmxml.ActivityDefinitionInfo;
import de.marketmaker.iview.pmxml.ActivityDefinitionsRequest;
import de.marketmaker.iview.pmxml.ActivityDefinitionsResponse;
import de.marketmaker.iview.pmxml.ActivityEditDefinitionRequest;
import de.marketmaker.iview.pmxml.ActivityEditDefinitionResponse;
import de.marketmaker.iview.pmxml.AlertsRequest;
import de.marketmaker.iview.pmxml.AlertsResponse;
import de.marketmaker.iview.pmxml.DatabaseId;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.GetWorkspaceResponse;
import de.marketmaker.iview.pmxml.MMClassIndex;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.UMRightBody;
import de.marketmaker.iview.pmxml.WorksheetDefaultMode;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController.DashboardIdStrategy.BY_SUB_CONTROLLER_ID;

/**
 * @author Michael Lösch
 */
public abstract class AbstractDepotObjectPortraitController<U> extends AbstractPageController implements HasNavWidget,
        AsyncCallback<ResponseType>, HasIsProvidesPrintableView, PrivacyMode.InterestedParty {
    public static final String DEFAULT_NAV_ITEM_ID_STATE_KEY = "defaultNavItemId";  // $NON-NLS$

    private static final String DEPOTOBJECT_DEFAULT_TOKENS = "DOT"; // $NON-NLS$
    public static final String HISTORY_TOKEN_ACTIVITY_OVERVIEW = "ACT_OV"; // $NON-NLS$
    public static final String HISTORY_TOKEN_EDIT_ACTIVITY = "EDIT"; // $NON-NLS$
    public static final String OBJECTID_KEY = "objectid"; // $NON-NLS$
    public static final String FORCE_RL = "frl"; // $NON-NLS$

    private PlaceChangeEvent placeChangeEvent;

    private int currentThreadId;

    private ObjectTreeModel model;
    private PageController current;
    private final BlockAndTalker[] bats;
    private NavItemSpec defaultNavItemSpec; //default == pm default layout
    private NavItemSpec fallbackNavItemSpec; // fallback == if pm default is not valid and no user clicked
    protected GetWorkspaceResponse workspace = null;
    protected Map<String, Set<WorksheetDefaultMode>> workspaceDefaultSheetMap = null;
    private NavWidgetCallback navWidgetCallback;
    private ObjectPanel navWidget = null;
    private InstanceLog il = new InstanceLog(this);

    protected final MMClassIndex activityMMClassIndex;
    protected final MMClassIndex editActivityMMClassIndex;

    private final AnalysisPageController analysisPageController = new AnalysisPageController(getClass().getSimpleName());
    private final ActivityOverviewController activityOverviewController;
    private final DmsTablePageController dmsController = new DmsTablePageController();
    private final ActivityPageController editActivityController;

    protected final DmxmlContext.Block<ActivityEditDefinitionResponse> blockEditAct;
    private final DmxmlContext.Block<ActivityDefinitionsResponse> blockActivityDefs;
    private final ActivityDefinitionsRequest activityDefinitionsRequest;

    private final AlertsRequest alertsReq;
    private final DmxmlContext.Block<AlertsResponse> alertsBlock;
    private final ActivityEditDefinitionRequest activityEditDefinitionRequest;
    private ThreadStateSupport threadStateSupport;
    private String pendingDefaultNavItemId;

    protected final PrivFeature priv;
    protected final IsBrokingAllowedFeature brokingAllowedFeature;

    protected PrivacyModeProvider.PrivacyModeActivatableCallback privacyModeActivatableCallback;

    private final HashSet<DashboardPageController> dashboardPageControllers = new HashSet<>();

    public AbstractDepotObjectPortraitController(MMClassIndex activityMMClassIndex, MMClassIndex editActivityMMClassIndex, UMRightBody... umrb) {
        super(new DmxmlContext());

        this.activityMMClassIndex = activityMMClassIndex;
        this.editActivityMMClassIndex = editActivityMMClassIndex;

        this.activityOverviewController = new ActivityOverviewController(this.activityMMClassIndex);

        this.bats = initBats();

        this.brokingAllowedFeature = new IsBrokingAllowedFeature(this.context);

        this.priv = new PrivFeature(this.context, MMClassIndex.CI_T_SHELL_MM, umrb);

        this.blockEditAct = this.context.addBlock("ACT_EditDefinition"); // $NON-NLS$
        this.activityEditDefinitionRequest = new ActivityEditDefinitionRequest();
        this.activityEditDefinitionRequest.setInputType(editActivityMMClassIndex);
        this.activityEditDefinitionRequest.setCustomerDesktopActive(PrivacyMode.isActive());
        this.blockEditAct.setParameter(this.activityEditDefinitionRequest);

        this.blockActivityDefs = this.context.addBlock("ACT_GetDefinitions"); // $NON-NLS$
        this.activityDefinitionsRequest = new ActivityDefinitionsRequest();
        this.activityDefinitionsRequest.setInputType(this.activityMMClassIndex);
        this.activityDefinitionsRequest.setCustomerDesktopActive(PrivacyMode.isActive());
        this.blockActivityDefs.setParameter(this.activityDefinitionsRequest);

        this.alertsBlock = this.context.addBlock("PM_GetAlerts"); // $NON-NLS$
        this.alertsReq = new AlertsRequest();
        this.alertsBlock.setParameter(this.alertsReq);

        this.editActivityController = new ActivityPageController(this.editActivityMMClassIndex, new Command() {
            @Override
            public void execute() {
                onAfterEditSubmit();
            }
        }).withCancelCommand(new Command() {
            @Override
            public void execute() {
                onEditCancelled();
            }
        });

        PrivacyMode.subscribe(this);
    }


    @Override
    public void privacyModeStateChanged(final boolean privacyModeActive, final PrivacyMode.StateChangeProcessedCallback processed) {
        // It is not possible to save the state via ThreadStateSupport, because the corresponding event will be fired
        // right after the whole state of the thread manager has been stored in its memento. Hence, the state will
        // be saved on the wrong thread. Additionally, we have to save the state before a new workspace has been loaded,
        // which will be asynchronously loaded below (resets the default nav item depending on the value of
        // privacyModeActive).
        if (this.threadStateSupport != null) { // threadStateSupport will be null if controller is not active.
            this.threadStateSupport.privacyModeStateChanged(privacyModeActive);
        }

        for (BlockAndTalker bat : this.bats) {
            bat.setPrivacyModeActive(privacyModeActive);
            bat.getBlock().setToBeRequested();
        }

        alertsBlock.setEnabled(!privacyModeActive);

        activityDefinitionsRequest.setCustomerDesktopActive(privacyModeActive);
        blockActivityDefs.setToBeRequested();

        activityEditDefinitionRequest.setCustomerDesktopActive(privacyModeActive);
        blockEditAct.setToBeRequested();

        PmWorkspaceHandler.getInstance().getPmWorkspace(privacyModeActive, getShellMMType(), new PmWorkspaceCallback() {
            @Override
            public void onWorkspaceAvailable(GetWorkspaceResponse response) {
                // "!privacyModeActive" ensures that we are entering the last set default nav item if privacy mode
                // is being set to active. But if we are leaving the privacy mode, ensure that the last active nav
                // item will be recovered from the then active thread's state.
                updateWorkspace(response, !privacyModeActive);
                processed.privacyModeStateChangeProcessed(AbstractDepotObjectPortraitController.this);
            }
        });
    }

    public abstract BlockAndTalker[] initBats();

    protected abstract NavItemSpec initNavItems(List<WorkspaceSheetDesc> sheets);

    protected abstract U getUserObject();

    protected abstract void setUserObject(U userObject);

    protected abstract U createUserObject();

    protected abstract String getName(U userObject);

    protected abstract ShellMMType getShellMMType();

    protected abstract Widget getNavNorthWidget();

    protected abstract String getControllerId();

    public ActivityPageController getEditActivityController() {
        return this.editActivityController;
    }

    private void setId(String id, boolean forceReload) {
        boolean refresh = forceReload;
        this.priv.setId(id);
        this.brokingAllowedFeature.setId(id);

        for (BlockAndTalker bat : this.bats) {
            if (!forceReload) {
                if (bat.getDatabaseId() != null && bat.getDatabaseId().equals(id) && bat.getBlock().isResponseOk()) {
                    continue;
                }
                refresh = true;
            }
            bat.setDatabaseId(id);
        }

        this.blockActivityDefs.setToBeRequested();

        this.alertsReq.setQuery(createDatabaseIdQuery(id));
        this.alertsBlock.setToBeRequested();

        if (refresh) {
            Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMMTalkerController.setId> issuing request");
            this.navWidget = null;
            this.context.issueRequest(this);
        }
        else {
            Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMMTalkerController.setId> user object has not changed; immediately calling doPlaceChange");
            buildModelAndDetermineDefaultNavItemSpec(false);
            handleCurrentController(this.model);
            doPlaceChange(this.model);
            if (this.navWidget != null) {
                this.navWidget.updateHistoryContext();
            }
        }
    }

    private DatabaseIdQuery createDatabaseIdQuery(String id) {
        final DatabaseIdQuery dbq = new DatabaseIdQuery();
        dbq.setRecursive(true);
        final DatabaseId dbId = new DatabaseId();
        dbId.setId(id);
        dbq.getIds().add(dbId);
        return dbq;
    }

    public void onEdit() {
        if (!checkEditActivity()) {
            return;
        }

        HistoryToken.Builder.fromCurrent()
                .with(OBJECTID_KEY, getDatabaseId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractDepotObjectPortraitController.HISTORY_TOKEN_EDIT_ACTIVITY)
                .with(ActivityPageController.PARAM_ACTIVITY_DEFINITION, this.blockEditAct.getResult().getInfo().getId())
                .build().fire();
    }

    public void onAfterEditSubmit() {
        HistoryToken.Builder.fromCurrent()
                .with(OBJECTID_KEY, getDatabaseId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractUserObjectView.SC_STATIC)
                .with(FORCE_RL, "t")  // $NON-NLS$
                .fire();
    }

    public void onEditCancelled() {
        HistoryToken.Builder.fromCurrent()
                .with(OBJECTID_KEY, getDatabaseId())
                .with(NavItemSpec.SUBCONTROLLER_KEY, AbstractUserObjectView.SC_STATIC)
                .fire();
        MainController.INSTANCE.getView().setContentHeader(getName(getUserObject()));
    }

    @Override
    public void onPlaceChange(final PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String objectid = historyToken.get(OBJECTID_KEY);

        if (this.workspace == null || this.workspaceDefaultSheetMap == null) {
            PmWorkspaceHandler.getInstance().getPmWorkspace(PrivacyMode.isActive(), getShellMMType(), new PmWorkspaceCallback() {
                @Override
                public void onWorkspaceAvailable(GetWorkspaceResponse response) {
                    updateWorkspace(response, true);
                    onPlaceChange(event);
                }
            });
            return;
        }

        this.placeChangeEvent = event;

        final boolean forceReload;
        if (this.currentThreadId == -1) {
            this.currentThreadId = getActiveThreadId();
            forceReload = true;
        }
        else if (this.currentThreadId != getActiveThreadId()) {
            this.currentThreadId = getActiveThreadId();
            forceReload = true;
        }
        else {
            forceReload = historyToken.getNamedParams().containsKey(FORCE_RL);
        }

        if (objectid != null) {
            setId(objectid, forceReload);
        }
        else {
            throw new IllegalStateException("url doesn't contain 'objectid'"); // $NON-NLS$
        }
    }

    protected int getActiveThreadId() {
        return AbstractMainController.INSTANCE.getHistoryThreadManager().getActiveThreadId();
    }

    private void handleCurrentController(ObjectTreeModel model) {
        final String sc = getSubControllerKey(this.placeChangeEvent.getHistoryToken());
        if (model == null) {
            Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMmTalkerController.handleCurrentController> model is null! Current controller is left as it is");
            return;
        }
        NavItemSpec spec = model.getItem(sc);
        if (spec == null) {
            spec = getDefaultOrFallback(model);
            if (spec == null) {
                throw new IllegalStateException("spec is null for id " + sc); // $NON-NLS$
            }
        }

        if (this.current != spec.getController()) {
            if (this.current != null) {
                this.current.deactivate();
            }
            this.current = spec.getController();
            if (this.current == null) {
                throw new IllegalStateException("current must not be null!");  // $NON-NLS$
            }
            Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMmTalkerController.handleCurrentController> current controller: " + this.current.getClass().getName());

            this.il.log("handleCurrentController current controller: " + this.current.getClass().getName()); // $NON-NLS$
            this.current.activate();
        }

        if (this.privacyModeActivatableCallback != null) {
            this.privacyModeActivatableCallback.onPrivacyModeActivatable(this.current != this.editActivityController);
        }
    }

    private String getSubControllerKey(HistoryToken historyToken) {
        final String sc = historyToken.get(NavItemSpec.SUBCONTROLLER_KEY);
        if (sc != null) {
            return sc;
        }
        final HistoryToken hdt = getHistoryDefaultToken();
        if (hdt != null) {
            return hdt.get(NavItemSpec.SUBCONTROLLER_KEY);
        }
        return getDefaultToken().get(NavItemSpec.SUBCONTROLLER_KEY);
    }

    @Override
    public void onFailure(Throwable caught) {
        Firebug.error("<" + getClass().getSimpleName() + ".AbstractMmTalkerController.onFailure>", caught);
        setUserObject(null);
        MainController.INSTANCE.getView().setContentHeader(I18n.I.error());
    }

    @Override
    public void onSuccess(ResponseType result) {
        if (this.workspace == null) {
            return;
        }
        for (BlockAndTalker bat : bats) {
            if (!bat.getBlock().isResponseOk()) {
                Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMmTalkerController.doOnSuccess> something is wrong with bat " + bat.getBlock().getKey() // $NON-NLS$
                        + " / " +
                        (bat.getMmtalker().getFormula() != null
                                ? bat.getMmtalker().getFormula()
                                : " no formula")); // $NON-NLS$
                return;
            }
        }

        Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMmTalkerController.doOnSuccess> was issued; user object has changed");
        final U userObject = createUserObject();
        MainController.INSTANCE.getView().setContentHeader(getName(userObject));
        setUserObject(userObject);
        buildModelAndDetermineDefaultNavItemSpec(true);
        handleCurrentController(this.model);
        doPlaceChange(this.model);
        updateNavWidget(buildObjectTree(this.model));
    }

    private void buildModelAndDetermineDefaultNavItemSpec(boolean buildModel) {
        // Handle default controller in case of privacy mode if the old default controller is also available in the
        // refreshed workspace, use the old default controller again as the default. If not, use the new default
        // controller determined during buildModel.
        final NavItemSpec oldDefault;

        if (StringUtil.hasText(this.pendingDefaultNavItemId)) {
            final NavItemSpec childById = this.model.getRoot().findChildById(pendingDefaultNavItemId);
            pendingDefaultNavItemId = null;
            if (childById != null) {
                oldDefault = childById;
            }
            else {
                oldDefault = this.defaultNavItemSpec;
            }
        }
        else {
            oldDefault = this.defaultNavItemSpec;
        }

        this.defaultNavItemSpec = null;
        if (buildModel) {
            this.model = buildModel(this.workspace.getSheets());
        }

        if (oldDefault != null) {
            final NavItemSpec childById = this.model.getRoot().findChildById(oldDefault.getId());
            if (childById != null) {
                setDefault(childById, true);
            }
        }
    }

    NavItemSpec setDefault(NavItemSpec nis, boolean force) {
        if (!nis.isTransientItem()
                && (this.defaultNavItemSpec == null || force)) {
            this.defaultNavItemSpec = nis;
            setHistoryToken(nis);
        }
        return nis;
    }

    private void setHistoryToken(NavItemSpec nis) {
        setHistoryToken(nis.getHistoryToken().with(NavItemSpec.SUBCONTROLLER_KEY, nis.getId()).build());
    }

    private void setHistoryToken(HistoryToken historyToken) {
        final HistoryThreadManager htm = MainController.INSTANCE.getHistoryThreadManager();
        htm.getActiveThreadHistoryItem().getPlaceChangeEvent().withProperty(DEPOTOBJECT_DEFAULT_TOKENS, historyToken.toString());
    }

    private NavItemSpec setDefault(NavItemSpec navItemSpec) {
        return setDefault(navItemSpec, false);
    }

    private void setContextDefault(NavItemSpec nis) {
        if (nis.isTransientItem()) {
            return;
        }
        final HistoryContext historyContext = this.placeChangeEvent.getHistoryContext();
        if (historyContext == null) {
            return;
        }
        final String tokens = nis.getHistoryToken().with(NavItemSpec.SUBCONTROLLER_KEY, nis.getId()).build().toString();
        Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMMTalkerController.setContextDefault> tokens = " + tokens);
        historyContext.putProperty(HistoryContext.USER_DEFAULT_KEY, tokens);
    }

    private String getContextDefaultId() {
        final HistoryContext historyContext = this.placeChangeEvent.getHistoryContext();
        final String ud = historyContext.getProperty(HistoryContext.USER_DEFAULT_KEY);
        return ud.substring(ud.indexOf(NavItemSpec.SUBCONTROLLER_KEY + "=") + NavItemSpec.SUBCONTROLLER_KEY.length() + 1);
    }

    private boolean obeyContextDefault() {
        final HistoryContext historyContext = this.placeChangeEvent.getHistoryContext();
        return historyContext != null && historyContext.getProperty(HistoryContext.USER_DEFAULT_KEY) != null;
    }

    private HistoryToken getDefaultToken() {
        if (this.fallbackNavItemSpec == null) {
            throw new IllegalStateException("fallbackNavItemSpec must not be null!"); // $NON-NLS$
        }
        return HistoryToken.builder(getControllerId())
                .with(OBJECTID_KEY, getDatabaseId())
                .with(NavItemSpec.SUBCONTROLLER_KEY,
                        obeyContextDefault()
                                ? getContextDefaultId()
                                : this.defaultNavItemSpec != null ? this.defaultNavItemSpec.getId() : this.fallbackNavItemSpec.getId()
                )
                .build();
    }

    private HistoryToken getHistoryDefaultToken() {
        final HistoryThreadManager htm = MainController.INSTANCE.getHistoryThreadManager();
        final String tokens = htm.getActiveThreadHistoryItem().getPlaceChangeEvent().getProperty(DEPOTOBJECT_DEFAULT_TOKENS);
        return tokens == null ? null : HistoryToken.Builder.fromToken(tokens).build();
    }

    private void handleDefaultNavItemSpec() {
        if (this.defaultNavItemSpec == null) {
            return;
        }
        if (this.defaultNavItemSpec.isHasDelegate() && this.defaultNavItemSpec.getChildren() != null
                && !this.defaultNavItemSpec.getChildren().isEmpty()) {
            setDefault(this.defaultNavItemSpec.getChildren().get(0), true); // pm default is not a layout...
            handleDefaultNavItemSpec(); // so go deeper
        }
        else if (this.defaultNavItemSpec.getController() == null) { // pm default has no delegate AND no controller (which means no guid)
            this.defaultNavItemSpec = this.fallbackNavItemSpec; // not a valid situation, set fallback as default
        }
    }

    NavItemSpec setFallback(NavItemSpec navItemSpec) {
        this.fallbackNavItemSpec = navItemSpec;
        return navItemSpec;
    }


    private ObjectTreeModel buildModel(List<WorkspaceSheetDesc> sheets) {
        final NavItemSpec root = initNavItems(sheets);
        handleDefaultNavItemSpec();
        return new ObjectTreeModel(root);
    }

    private ObjectTree buildObjectTree(ObjectTreeModel model) {
        ObjectTree objectTree = new ObjectTree(model);
        objectTree.addSelectionHandler(new SelectionHandler<NavItemSpec>() {
            @Override
            public void onSelection(SelectionEvent<NavItemSpec> event) {
                onNavItemSelection(event);
            }
        });
        return objectTree;
    }

    private void onNavItemSelection(SelectionEvent<NavItemSpec> event) {
        final NavItemSpec nis = event.getSelectedItem();
        if (nis.isUpdateContentHeader()) {
            MainController.INSTANCE.getView().setContentHeader(nis.getName());
        }
        setContextDefault(nis);
        setDefault(nis, true);
        nis.goTo(null);
    }

    void addEditNode(NavItemSpec root) {
        if (!checkEditActivity()) {
            return;
        }

        // only necessary to be available for navigation. It is not visible to users, because there should an edit tool
        // button in the user object view; cf. AS-1251
        final NavItemSpec navItemSpecChild = new NavItemSpec(HISTORY_TOKEN_EDIT_ACTIVITY, I18n.I.staticDataEdit(), null
                , getEditActivityController());

        navItemSpecChild.setVisible(false); //visible via user object view but must be in the tree to be accessible

        root.addChild(navItemSpecChild).withIsTransient();
    }

    protected boolean checkEditActivity() {
        if (this.blockEditAct.isResponseOk() && this.blockEditAct.getResult().getInfo() != null) {
            return true;
        }
        MainController.INSTANCE.showError(I18n.I.staticDataEditNoActivityDefinedError(PmRenderers.SHELL_MM_TYPE.render(getShellMMType())));
        return false;
    }

    /**
     * Sets also the default nav item, so if it is called after createLayoutChildren, the dashboard nav items, if any,
     * will not beat the analysis items, which should be the case.
     */
    void addDashboard(NavItemSpec root) {
        destroyDashboardPageControllers();

        final List<DashboardConfig> listConfigs = ConfigDao.getInstance().getConfigsByRole(getShellMMType().value());
        Collections.sort(listConfigs, ConfigComparator.COMPARE_NAME);
        for (final DashboardConfig dc : listConfigs) {
            final DashboardPageController controller = new DashboardPageController(BY_SUB_CONTROLLER_ID);
            this.dashboardPageControllers.add(controller);
            final NavItemSpec nis = new NavItemSpec(DashboardPageController.toSubControllerId(dc.getId()), dc.getName(),
                    HistoryToken.current(), controller);
            if (ConfigDao.getInstance().isEditAllowed(dc.getId()) && !PrivacyMode.isActive()) {
                final IconImageIcon image = IconImage.getIcon("mm-small-edit") // $NON-NLS$
                        .withClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                DashboardStateChangeEvent.Action.EDIT_SELECTED.fire(dc.getId());
                                event.stopPropagation();
                            }
                        });
                Tooltip.addQtip(image, PermStr.DASHBOARD_EDIT.value());
                nis.withEndIcon(image);
                nis.withEndIconCellClass("selectedEndIcon"); // $NON-NLS$
            }
            root.addChild(setDefault(nis));
        }
    }

    private void destroyDashboardPageControllers() {
        if(!this.dashboardPageControllers.isEmpty()) {
            for (DashboardPageController controller : this.dashboardPageControllers) {
                controller.destroy();
            }
            this.dashboardPageControllers.clear();
        }
    }

    void addDmsNode(NavItemSpec root) {
        if (Selector.AS_DMS.isAllowed() && this.priv.allowed(UMRightBody.UMRB_READ_DOCUMENTS)) {
            root.addChild(new NavItemSpec("DMS", I18n.I.dmsNavTitle(), HistoryToken.current(), this.dmsController)); // $NON-NLS$
        }
    }

    void addActivityNode(NavItemSpec root) {
        if (!Selector.AS_ACTIVITIES.isAllowed() || !this.priv.allowed(UMRightBody.UMRB_EDIT_ACTIVITY_INSTANCE) || !this.blockActivityDefs.isResponseOk()) {
            return;
        }
        final List<ActivityDefinitionInfo> definitions = this.blockActivityDefs.getResult().getDefinitions();
        if (definitions.isEmpty()) {
            return;
        }
        root.addChild(new NavItemSpec(HISTORY_TOKEN_ACTIVITY_OVERVIEW, I18n.I.activities(), HistoryToken.current(), this.activityOverviewController));
    }

    private void doPlaceChange(ObjectTreeModel model) {
        final NavItemSpec spec;
        final PlaceChangeEvent pce;
        final HistoryToken historyToken = this.placeChangeEvent.getHistoryToken();
        final String subController = historyToken.get(NavItemSpec.SUBCONTROLLER_KEY);
        if (subController != null) {
            spec = getItemDefaultOrFallback(model, subController);
            pce = this.placeChangeEvent;
        }
        else {
            final HistoryToken hdt = getHistoryDefaultToken();
            if (hdt != null) {
                spec = getItemDefaultOrFallback(model, hdt.get(NavItemSpec.SUBCONTROLLER_KEY));
                pce = new PlaceChangeEvent(this.placeChangeEvent, hdt);
            }
            else {
                final HistoryToken defaultToken = getDefaultToken();
                spec = getItemDefaultOrFallback(model, defaultToken.get(NavItemSpec.SUBCONTROLLER_KEY));
                pce = new PlaceChangeEvent(this.placeChangeEvent, defaultToken);
                setHistoryToken(defaultToken);
            }
        }
        assert spec != null : "spec must not be null";
        assert spec.getController() != null : "controller of spec must not be null";
        il.log("doPlaceChange() -> setSelected(" + spec.getName() + ")"); // $NON-NLS$
        spec.getController().onPlaceChange(pce);
        model.setSelected(spec, true);
    }

    private NavItemSpec getDefaultOrFallback(ObjectTreeModel model) {
        NavItemSpec item = null;
        if (this.defaultNavItemSpec != null && model.getItem(this.defaultNavItemSpec.getId()) != null) {
            item = model.getItem(this.defaultNavItemSpec.getId());
        }
        if (item == null || item.getController() == null) {
            if (this.fallbackNavItemSpec != null && model.getItem(this.fallbackNavItemSpec.getId()) != null) {
                item = model.getItem(this.fallbackNavItemSpec.getId());
            }
        }
        return item;
    }

    private NavItemSpec getItemDefaultOrFallback(ObjectTreeModel model, String subController) {
        final NavItemSpec spec = model.getItem(subController);
        if (spec == null) {
            return getDefaultOrFallback(model);
        }
        return spec;
    }

    protected NavItemSpec[] createLayoutChildren(HistoryToken token, String subtitle, String id, List<WorkspaceSheetDesc> sheets) {
        final List<NavItemSpec> result = new ArrayList<>();
        for (WorkspaceSheetDesc sheet : sheets) {
            final NavItemSpec spec = createLayoutChild(token, sheet, subtitle, id).withSelectFirstChildOnOpen();
            spec.withClosingSiblings();
            result.add(spec);
            if (PmWebSupport.isMainDefaultSheet(sheet, this.workspaceDefaultSheetMap)
                    && StringUtil.hasText(sheet.getLayoutGuid())) {
                setDefault(spec);
            }
        }
        return result.toArray(new NavItemSpec[result.size()]);
    }

    private NavItemSpec createLayoutChild(HistoryToken token, WorkspaceSheetDesc sheet, String titleSuffix, String databaseId) {
        final NavItemSpec navItemSpec;
        final LayoutNode layoutNode = LayoutNode.create(sheet.getNodeId(), sheet.getLayoutGuid());
        final String layoutNodeString = layoutNode.toString();

        if (StringUtil.hasText(sheet.getLayoutGuid())) {
            navItemSpec = new NavItemSpec(layoutNodeString, sheet.getCaption(), token, this.analysisPageController);
        }
        else {
            navItemSpec = new NavItemSpec(layoutNodeString, sheet.getCaption());
        }
        if (sheet.getSheets() != null && !sheet.getSheets().isEmpty()) {
            navItemSpec.addChildren(createLayoutChildren(token, titleSuffix, databaseId, sheet.getSheets()));
        }
        if (sheet.isDelegateOpenToSubSheet()) {
            navItemSpec.withHasDelegate();
        }
        return navItemSpec;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (this.threadStateSupport != null) {
            this.threadStateSupport.unregister();
            this.threadStateSupport = null;
        }
        if (this.current != null) {
            this.current.deactivate();
        }
    }

    public void activate() {
        super.activate();
        this.threadStateSupport = new ThreadStateSupport(new ThreadStateHandler() {
            @Override
            public Map<String, String> saveState(HistoryItem item) {
                final HashMap<String, String> map = new HashMap<>();
                if (defaultNavItemSpec != null) {
                    map.put(DEFAULT_NAV_ITEM_ID_STATE_KEY, defaultNavItemSpec.getId());
                }
                return map;
            }

            @Override
            public void loadState(HistoryItem item, Map<String, String> data) {
                if (data == null) {
                    return;
                }
                pendingDefaultNavItemId = data.get(DEFAULT_NAV_ITEM_ID_STATE_KEY);
            }

            @Override
            public String getStateKey(HistoryItem item) throws GetStateKeyException {
                return AbstractDepotObjectPortraitController.this.getClass().getSimpleName();
            }
        });
        if (this.current != null) {
            this.current.activate();
        }
    }

    @Override
    public boolean providesContentHeader() {
        return true;
    }

    private void updateNavWidget(ObjectTree objectTree) {
        this.il.log("new navWidget"); // $NON-NLS$
        this.navWidget = new ObjectPanel(getNavNorthWidget(), objectTree.asWidget());
        if (this.navWidgetCallback != null) {
            this.navWidgetCallback.setNavWidget(this.navWidget);
        }
    }

    @Override
    public void requestNavWidget(NavWidgetCallback callback) {
        this.navWidgetCallback = callback;
        if (this.navWidget != null) {
            this.navWidgetCallback.setNavWidget(this.navWidget);
        }
        else {
            this.navWidgetCallback.showGlass();
        }
    }

    @Override
    public String getPrintHtml() {
        if (this.current == null) {
            return "";
        }

        return this.current.getPrintHtml();
    }

    @Override
    public Widget getPrintView() {
        if (this.current instanceof HasIsProvidesPrintableView) {
            return ((HasIsProvidesPrintableView) this.current).getPrintView();
        }
        return null;
    }

    @Override
    public boolean isPrintable() {
        return this.current != null && this.current.isPrintable();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        if (this.current == null) {
            return null;
        }
        return this.current.getPdfOptionSpec();
    }

    private String getDatabaseId() {
        if (this.bats == null || this.bats.length == 0) {
            throw new IllegalStateException("no bats available!"); // $NON-NLS$
        }
        return this.bats[0].getDatabaseId();
    }

    @Override
    public void refresh() {
        if (this.current == this.analysisPageController) {
            this.analysisPageController.forceEval();
        }
        super.refresh();
    }

    protected PageController getCurrentPageController() {
        return this.current;
    }

    public boolean isActivityBrokingAllowed() {
        return this.brokingAllowedFeature.isActivityBrokingAllowed();
    }

    public boolean isStandaloneBrokingAllowed() {
        return this.brokingAllowedFeature.isStandaloneBrokingAllowed();
    }

    public String getIdOfBrokingAllowedDepot() {
        return this.brokingAllowedFeature.getDepotId();
    }

    protected void handleOrderEntryNavItems(NavItemSpec parent) {
        Firebug.debug("<" + getClass().getSimpleName() + ".AbstractMMTalkerController.handleOrderEntryNavItems>");
        if (!isStandaloneBrokingAllowed()) {
            return;
        }

        final String depotId = getIdOfBrokingAllowedDepot();
        if (depotId != null) {
            parent.addChild(new NavItemSpec("OE_NOE", I18n.I.enterOrder(), //$NON-NLS$
                            new OrderEntryGoToDelegate(OrderEntryGoToDelegate.Type.BY_DEPOT_ID,
                                    depotId)).withIsTransient().withDoNotUpdateContentHeader()
            );
        }
    }

    protected AlertsResponse getAlerts() {
        if (PrivacyMode.isActive() || !this.alertsBlock.isResponseOk()) {
            return null;
        }
        return this.alertsBlock.getResult();
    }

    private void updateWorkspace(GetWorkspaceResponse response, boolean resetDefault) {
        this.workspace = response;
        this.workspaceDefaultSheetMap = PmWebSupport.toDefaultSheetsMap(response);
        if (resetDefault) {
            this.defaultNavItemSpec = null;
        }
    }
}