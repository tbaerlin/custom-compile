/*
 * MainView.java
 *
 * Created on 30.11.12
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.as.AsTopLine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.HistoryThreadWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.ModulePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardNavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.DzBankTeaserUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.RefreshDmxmlContextEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.workspace.ExplorerWorkspace;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CssValues;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Finalizer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentViewAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ResizableContentPanelAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SimpleGlassableLayoutPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.DzBankTeaserUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.SearchWorkspace;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.TeaserConfigData;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author Ulrich Maurer
 */
public class MainView extends AbstractMainView {
    public static final double TOP_LINE_HEIGHT = 21d;

    public static final double FOOTER_HEIGHT = TOP_LINE_HEIGHT;

    public static final int MODULE_WIDTH = 100;

    public static final int NAVIGATION_WIDTH = 200;

    public static final double CONTENT_HEADER_HEIGHT = 32d;

    private static final int DZ_BANK_TEASER_OUTER_HEIGHT = 165; // Determined by experiment for our min screen resolution so that the module strip does not scroll.

    private static final int DZ_BANK_TEASER_PADDING = 10;

    public static final int DZ_BANK_TEASER_INNER_HEIGHT = DZ_BANK_TEASER_OUTER_HEIGHT - DZ_BANK_TEASER_PADDING;

    private static final int MODULE_NAV_TOGGLE_BUTTON_WIDTH = 16; // Icon is 15 px width, but there should be a little margin to the right, therefore just 16px width.

    private static final String ICE_MODULE_AND_NAV_TOGGLE_BUTTON = "ice-moduleAndNavToggleButton"; // $NON-NLS$

    private static MainView instance = null;

    private final SimpleGlassableLayoutPanel sglp = new SimpleGlassableLayoutPanel();

    private final TopToolbar topToolbar;

    @SuppressWarnings("FieldCanBeLocal")
    private final DockLayoutPanel mainDockPanel = new DockLayoutPanel(Style.Unit.PX);

    @SuppressWarnings("FieldCanBeLocal")
    private final DockLayoutPanel contentDockPanel = new DockLayoutPanel(Style.Unit.PX);

    private final SimpleLayoutPanel contentPanel = new SimpleLayoutPanel();

    @SuppressWarnings("FieldCanBeLocal")
    private final FlowPanel contentHeaderPanel = new FlowPanel();

    private final HTML contentHeader = new HTML();

    private final ModulePanel modulePanel = new ModulePanel();

    private final FloatingPanel modulePanelFloater = new FloatingPanel(FloatingPanel.Orientation.VERTICAL, false).withWidget(this.modulePanel);

    private final SimpleLayoutPanel navigationPanel = new SimpleLayoutPanel();

    private NavigationWidget currentNavigationWidget = null;

    @SuppressWarnings("FieldCanBeLocal")
    private final Panel southPanel;

    private HandlerRegistration dzBankTeaserHandlerRegistration;

    private HistoryToken privacyModeCustomerToken;

    private String privacyModeCustomerIconMappingName;

    protected final SessionData sessionData;

    public MainView(TopToolbar topToolbar, Panel southPanel, SessionData sessionData) {
        this.sessionData = sessionData;
        final HandlerManager eventBus = EventBusRegistry.get();

        Document.get().getBody().addClassName("asView"); // $NON-NLS$
        SnippetTableWidget.setAppendSpaceToSortableHeaders(false);

        this.southPanel = southPanel;
        this.topToolbar = topToolbar;
        this.mainDockPanel.setStyleName("mm-mainPanel");
        this.contentDockPanel.setStyleName("mm-content");
        this.contentHeaderPanel.setStyleName("as-contentHeaderPanel");
        this.contentHeader.setStyleName("as-contentHeader as-navigationArea");
        this.navigationPanel.addStyleName("as-navigationArea");

        this.contentHeaderPanel.add(this.contentHeader);
        this.contentHeaderPanel.add(HistoryThreadWidget.getInstance());

        this.contentDockPanel.addNorth(contentHeaderPanel, CONTENT_HEADER_HEIGHT);
        this.contentDockPanel.add(this.contentPanel);
        if (SessionData.isWithMarketData() && !Permutation.GIS.isActive()) {
            final AsTopLine topLine = new AsTopLine(this.sessionData);
            this.mainDockPanel.addNorth(topLine, TOP_LINE_HEIGHT);
            eventBus.addHandler(RefreshDmxmlContextEvent.getType(), topLine);
        }
        final int customerColorLineHeight = CssValues.getCustomerColorLineHeight();
        if (customerColorLineHeight > 0) {
            final Label label = new Label();
            label.setStyleName("ice-customerColorLine");
            this.mainDockPanel.addNorth(label, customerColorLineHeight);
        }
        this.mainDockPanel.addNorth(this.topToolbar, DefaultTopToolbar.TOOLBAR_HEIGHT);

        this.mainDockPanel.addSouth(this.southPanel, FOOTER_HEIGHT);
        if (customerColorLineHeight > 0) {
            final Label label = new Label();
            label.setStyleName("ice-customerColorLine");
            this.mainDockPanel.addSouth(label, customerColorLineHeight);
        }

        final DockLayoutPanel moduleAndNavPanel = new DockLayoutPanel(Style.Unit.PX);
        moduleAndNavPanel.addWest(this.modulePanelFloater, MODULE_WIDTH);
        moduleAndNavPanel.addEast(this.navigationPanel, NAVIGATION_WIDTH);

        final Widget moduleAndNavToggleButton;
        if (Selector.DZ_TEASER.isAllowed()) {
            final DockLayoutPanel moduleNavAndTeaserPanel = createDzBankTeaser(eventBus, moduleAndNavPanel);
            this.mainDockPanel.addWest(moduleNavAndTeaserPanel, MODULE_WIDTH + NAVIGATION_WIDTH);
            moduleAndNavToggleButton = createModuleAndNavToggleButton(moduleNavAndTeaserPanel);
        }
        else {
            this.mainDockPanel.addWest(moduleAndNavPanel, MODULE_WIDTH + NAVIGATION_WIDTH);
            moduleAndNavToggleButton = createModuleAndNavToggleButton(moduleAndNavPanel);
        }
        this.mainDockPanel.addWest(moduleAndNavToggleButton, MODULE_NAV_TOGGLE_BUTTON_WIDTH);

        this.mainDockPanel.add(this.contentDockPanel);

        eventBus.addHandler(PendingRequestsEvent.getType(), event -> {
            if (event.getNumPmPending() > 0) {
                sglp.showGlass();
            }
            else {
                sglp.hideGlass();
            }
        });
        this.sglp.setGlassStyleName("as-glassPanel"); // $NON-NLS$
        this.sglp.setWidget(this.mainDockPanel);
        this.sglp.addGlassClickHandler(e -> Notifications.add(I18n.I.hint(), I18n.I.pleaseWaitWhileLoadingData()).requestStateDelayed(NotificationMessage.State.DELETED, 5));
        BrowserSpecific.addBodyStyles();
        RootLayoutPanel.get().getElement().getStyle().setZIndex(0);
        RootLayoutPanel.get().add(this.sglp);
        Tooltip.initialize();
        instance = this;
    }

    private DockLayoutPanel createDzBankTeaser(HandlerManager eventBus,
            DockLayoutPanel moduleAndNavPanel) {
        final Image dzBankTeaserImage = new Image();

        final SimplePanel teaserPanel = new SimplePanel();
        teaserPanel.setStyleName("ice-dzbankTeaserPanel");
        teaserPanel.setWidget(dzBankTeaserImage);
        final DockLayoutPanel moduleNavAndTeaserPanel = new DockLayoutPanel(Style.Unit.PX);
        moduleNavAndTeaserPanel.addSouth(teaserPanel, DZ_BANK_TEASER_OUTER_HEIGHT);
        moduleNavAndTeaserPanel.add(moduleAndNavPanel);

        final Finalizer<Boolean> teaserHiddenFinalizer = new Finalizer<>();
        teaserHiddenFinalizer.set(true);
        moduleNavAndTeaserPanel.setWidgetHidden(teaserPanel, true);

        dzBankTeaserImage.addLoadHandler(e -> {
            final int height = dzBankTeaserImage.getHeight();
            if (height > DZ_BANK_TEASER_INNER_HEIGHT) {
                dzBankTeaserImage.setHeight(DZ_BANK_TEASER_INNER_HEIGHT + "px"); // $NON-NLS$
                moduleNavAndTeaserPanel.setWidgetSize(teaserPanel, DZ_BANK_TEASER_OUTER_HEIGHT);
            }
            else {
                moduleNavAndTeaserPanel.setWidgetSize(teaserPanel, height + DZ_BANK_TEASER_PADDING);
            }
            moduleNavAndTeaserPanel.setWidgetHidden(teaserPanel, teaserHiddenFinalizer.get() || DzBankTeaserUtil.isTeaserHiddenByAppConfig());
            dzBankTeaserImage.getElement().getStyle().clearVisibility();
            moduleNavAndTeaserPanel.forceLayout();
        });

        dzBankTeaserImage.addErrorHandler(e -> {
            moduleNavAndTeaserPanel.setWidgetHidden(teaserPanel, true);
            moduleNavAndTeaserPanel.forceLayout();
        });

        eventBus.addHandler(DzBankTeaserUpdatedEvent.getType(), e -> {
            Firebug.debug("<AsView> handle event:" + e);

            final TeaserConfigData config = e.getTeaserConfigData();
            teaserHiddenFinalizer.set(!config.isTeaserEnabled());

            if (config.isTeaserEnabled()) {
                updateDzBankTeaser(dzBankTeaserImage, config);
                moduleNavAndTeaserPanel.setWidgetHidden(teaserPanel, false);
            }
            else {
                moduleNavAndTeaserPanel.setWidgetHidden(teaserPanel, true);
            }
            moduleNavAndTeaserPanel.forceLayout();
        });

        eventBus.addHandler(ConfigChangedEvent.getType(), e -> {
            moduleNavAndTeaserPanel.setWidgetHidden(teaserPanel, teaserHiddenFinalizer.get() || DzBankTeaserUtil.isTeaserHiddenByAppConfig());
            moduleNavAndTeaserPanel.forceLayout();
        });
        return moduleNavAndTeaserPanel;
    }

    private void updateDzBankTeaser(Image dzBankTeaserImage, TeaserConfigData config) {
        if (this.dzBankTeaserHandlerRegistration != null) {
            this.dzBankTeaserHandlerRegistration.removeHandler();
            this.dzBankTeaserHandlerRegistration = null;
        }

        dzBankTeaserImage.setUrl(DzBankTeaserUtil.getCurrentImageUrl());
        dzBankTeaserImage.getElement().getStyle().clearHeight();
        // hidden has no influence on the automatically determined size
        dzBankTeaserImage.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);

        if (config.isLinkEnabled() && StringUtil.hasText(config.getLinkUrl())) {
            dzBankTeaserImage.addStyleName("mm-link");
            this.dzBankTeaserHandlerRegistration = dzBankTeaserImage.addClickHandler(e1 ->
                    Window.open(config.getLinkUrl(), config.getLinkTarget(), ""));
        }
        else {
            dzBankTeaserImage.removeStyleName("mm-link");
        }
    }

    private Widget createModuleAndNavToggleButton(Widget widgetToSetHidden) {
        final Image moveLeft = IconImage.get("ice-handle-left").createImage(); // $NON-NLS$
        moveLeft.addStyleName("ice-moduleAndNavToggleButton-icon");

        final Image moveRight = IconImage.get("ice-handle-right").createImage(); // $NON-NLS$
        moveRight.addStyleName("ice-moduleAndNavToggleButton-icon");

        final SimpleLayoutPanel moduleAndNavToggleButton = new SimpleLayoutPanel();
        moduleAndNavToggleButton.setStyleName(ICE_MODULE_AND_NAV_TOGGLE_BUTTON);

        final SimplePanel imageContainer = new SimplePanel();

        imageContainer.setWidget(moveLeft);

        moduleAndNavToggleButton.setWidget(imageContainer);

        final Finalizer<Boolean> moduleAndNavHidden = new Finalizer<>(false);

        moduleAndNavToggleButton.addHandler(clickEvent -> {
            final Boolean hidden = !moduleAndNavHidden.get();
            moduleAndNavHidden.set(hidden);
            imageContainer.clear();
            imageContainer.setWidget(hidden ? moveRight : moveLeft);
            this.mainDockPanel.setWidgetHidden(widgetToSetHidden, hidden);
            this.mainDockPanel.forceLayout();
        }, ClickEvent.getType());

        moduleAndNavToggleButton.sinkEvents(Event.ONCLICK);

        return moduleAndNavToggleButton;
    }

    public static MainView getInstance() {
        return instance;
    }

    @Override
    public void init() {
        initModuleIcons();
    }

    private void initModuleIcons() {
        this.modulePanelFloater.addStyleName("as-iconPanelFloat");
        updateAndSyncMenuModelAndModuleIcons(false, null, null);
        this.modulePanelFloater.onResize();
    }

    public void syncMenuModelAndModuleIcons() {
        doSyncMenuModelAndModuleIcons(PrivacyMode.isActive(), this.privacyModeCustomerToken,
                this.privacyModeCustomerIconMappingName);
    }

    public void updateAndSyncMenuModelAndModuleIcons(boolean privacyMode,
            HistoryToken customerToken,
            String customerIconMappingName) {

        this.privacyModeCustomerToken = customerToken;
        this.privacyModeCustomerIconMappingName = customerIconMappingName;

        doSyncMenuModelAndModuleIcons(privacyMode, customerToken, customerIconMappingName);
    }

    private void doSyncMenuModelAndModuleIcons(boolean privacyMode,
            final HistoryToken customerToken,
            String customerIconMappingName) {
        final MenuModel menuModel = AbstractMainController.INSTANCE.getMenuModel();
        this.modulePanel.clear();
        for (MenuModel.Item item : menuModel.getRootItem().getItems()) {
            if ("DZ".equals(item.getId())) { // $NON-NLS$
                this.modulePanel.addIcon("DZ", "logo-dzbank-48", "DZ BANK", new MenuTree(item)); // $NON-NLS$
            }
            else if ("WGZ".equals(item.getId())) { // $NON-NLS$
                this.modulePanel.addIcon("WGZ", "logo-dzbank-48", "WGZ BANK", new MenuTree(item)); // $NON-NLS$
            }
            // possibly, we have to add further menus there, e.g. KWT, OLB
        }

        if (privacyMode) { // privacy mode can only be true in Infront Advisory Solution
            this.modulePanel.addIcon(customerToken.getControllerId(), customerIconMappingName, I18n.I.customer(), createCustomerNavWidget(customerToken));
            this.modulePanel.changeSelection(new String[]{customerToken.getControllerId()});
        }
        else {
            if (menuModel.hasElement(MenuBuilder.DASHBOARD_ID)) {
                this.modulePanel.addIcon(MenuBuilder.DASHBOARD_ID, "cg-dashboard", PermStr.DASHBOARDS.value(), new DashboardNavigationWidget()); // $NON-NLS$
            }
            if (SessionData.isWithPmBackend()) {
                this.modulePanel.addIcon(MenuBuilder.EXPLORER_ID, "cg-customers", I18n.I.customers(), ExplorerWorkspace.getInstance()); // $NON-NLS$
            }
        }
        for (MenuModel.Item item : menuModel.getRootItem().getItems()) {
            if (item.getIconStyle() == null) {
                continue;
            }
            // Only Infront Advisory Solution uses the "search" module icon.
            // ICE mm[web] has the search results in tools adjacent to the live finders
            if (SessionData.isWithPmBackend()) {
                if ((!privacyMode && "cg-settings".equals(item.getIconStyle())) // $NON-NLS$
                        || privacyMode && SessionData.isWithMarketData() && "cg-help".equals(item.getIconStyle())) {  // $NON-NLS$
                    //SearchWorkspace is a NavigationWidget that has to be displayed every time a search is performed.
                    //Usually, a NavigationWidget is only displayed when the user clicks it. This is why the force-parameter
                    //is true in the following addIcon call.
                    this.modulePanel.addIcon(MenuBuilder.SEARCH_ID, "cg-searchresult", I18n.I.searchResults(), SearchWorkspace.getInstance()); // $NON-NLS$
                }
            }
            if (!SessionData.isWithPmBackend() && StringUtil.equals(item.getId(), "T")) { // $NON-NLS$
                // In case of mm[web] in ICE design, the search result should reside in the "Tools"
                // menu. A special search workspace is used here as navigation widget that hosts
                // also the items of the "Tools" menu. This is necessary, because the usual MenuTree
                // fires place change events with the menu ID as the token, but this is not usable
                // for dynamic search results.
                final SearchWorkspace instance = SearchWorkspace.getInstance();
                if (instance instanceof Consumer) {
                    //noinspection unchecked
                    ((Consumer<MenuModel.Item>) instance).accept(item);
                }
                this.modulePanel.addIcon(item.getId(), item.getIconStyle(), item.getName(), instance);
                continue;
            }

            if(item.getDoOnClickCommand() != null) {
                this.modulePanel.addIcon(item.getId(), item.getIconStyle(), item.getName(), item.getDoOnClickCommand());
                continue;
            }

            this.modulePanel.addIcon(item.getId(), item.getIconStyle(), item.getName(), new MenuTree(item));
        }

        if (!privacyMode) {
            this.modulePanel.changeSelection(menuModel.getSelectedIds());
        }

        this.modulePanelFloater.onResize();
    }

    private NavigationWidget createCustomerNavWidget(final HistoryToken costomerToken) {
        return new NavigationWidget() {
            @Override
            public Widget asWidget() {
                return null;
            }

            @Override
            public void onNavWidgetSelected() {
                costomerToken.fire();
            }

            @Override
            public void changeSelection(String[] ids) {
                modulePanel.changeSelection(ids);
            }

            @Override
            public boolean isResponsibleFor(String[] ids) {
                return !(ids == null || ids.length < 2) && (ids[1].equals(costomerToken.getControllerId()));
            }
        };
    }

    public void setPrivacyModeEnabled(boolean visible) {
        final TopToolbar topToolbar = getTopToolbar();
        if (topToolbar instanceof DefaultTopToolbar) {
            ((DefaultTopToolbar) topToolbar).setPrivacyModeEnabled(visible);
        }
    }

    public void setPrivacyMode(boolean privacyMode) {
        final TopToolbar topToolbar = getTopToolbar();
        if (topToolbar instanceof DefaultTopToolbar) {
            ((DefaultTopToolbar) topToolbar).setPrivacyMode(privacyMode);
        }
    }

    @Override
    public void setDefaultWindowTitle() {
        Window.setTitle(this.sessionData.getUser().getAppTitle());
    }

    @Override
    public void setWindowTitlePrefix(String prefix) {
        Window.setTitle(prefix + " - " + this.sessionData.getUser().getAppTitle()); // $NON-NLS-0$
    }

    @Override
    public SafeHtml getContentHeader() {
        return SafeHtmlUtils.fromTrustedString(this.contentHeader.getElement().getInnerHTML());
    }

    @Override
    public void setContentHeader(SafeHtml safeHtml) {
        if (safeHtml == null) {
            this.contentHeader.setVisible(false);
            this.contentHeader.setText("");
            return;
        }
        this.contentHeader.setVisible(true);
        this.contentHeader.setHTML(safeHtml);
        final int activeThreadId = MainController.INSTANCE.getHistoryThreadManager().getActiveThreadId();
        MainController.INSTANCE.getHistoryThreadManager().setThreadTitle(activeThreadId, safeHtml);
    }

    @Override
    public void changeSelection(String[] ids, boolean setNavWidget) {
        Firebug.log("AsView.changeSelection(" + Arrays.asList(ids) + ", " + setNavWidget + ")");
        final NavigationWidget navigationWidget = this.modulePanel.changeSelection(ids);
        if (navigationWidget != null && setNavWidget) {
            Firebug.log("AsView.changeSelection navWidget != null -> set as currentNavWidget");
            this.currentNavigationWidget = navigationWidget;
            setNavWidget(this.currentNavigationWidget);
        }
        if (this.currentNavigationWidget != null && this.currentNavigationWidget.isResponsibleFor(ids)) {
            Firebug.log("AsView.changeSelection currentNavWidget != null -> call its changeSelection method");
            this.currentNavigationWidget.changeSelection(ids);
        }
    }

    @Override
    public void showError(SafeHtml safeHtml) {
        Firebug.log("USER MESSAGE (ERROR):" + safeHtml.asString());
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("as-notificationError");
        final Image image = IconImage.get("dialog-error-16").createImage(); // $NON-NLS$
        image.getElement().getStyle().setFloat(Style.Float.LEFT);
        panel.add(image);
        panel.add(new HTML(safeHtml));
        Notifications.add(I18n.I.error(), panel).requestStateDelayed(NotificationMessage.State.DELETED, 15);

    }

    @Override
    public void showMessage(SafeHtml safeHtml) {
        Firebug.log("USER MESSAGE (MESSAGE):" + safeHtml.asString());
        final HTML html = new HTML(safeHtml);
        html.setStyleName("as-notificationMessage");
        Notifications.add(I18n.I.systemMessage(), html).requestStateDelayed(NotificationMessage.State.DELETED, 15);
    }

    @Override
    public void onLogout() {
        // nothing to do
    }

    @Override
    public void resetWestAndEastPanel() {
        // not implemented
    }

    @Override
    public TopToolbar getTopToolbar() {
        return this.topToolbar;
    }

    @Override
    protected void setContentAfterChange(ContentView view) {
        setWidget(this.contentPanel, view.getWidget());
    }

    private void setWidget(HasOneWidget parent, Widget child) {
        if (child instanceof NeedsScrollLayout) {
            parent.setWidget(new ScrollPanel(child));
        }
        else if (child instanceof ContentPanel) {
            parent.setWidget(new ResizableContentPanelAdapter(((ContentPanel) child)));
        }
        else {
            parent.setWidget(child);
        }
    }

    private void setWidget(HasOneWidget parent, IsWidget child) {
        if (child instanceof NeedsScrollLayout) {
            parent.setWidget(new ScrollPanel(child.asWidget()));
        }
        else {
            parent.setWidget(child);
        }
    }

    @Override
    public boolean hasNavPanel() {
        return true;
    }

    @Override
    public void setNavWidget(NavigationWidget nw) {
        setWidget(this.navigationPanel, nw == null
                ? null
                : nw.asWidget());
    }

    public void setNavWidget(IsWidget widget) {
        setWidget(this.navigationPanel, widget);
    }

    public IsWidget getNavWidget() {
        return this.navigationPanel.getWidget();
    }

    @Override
    public void setContent(Widget widget) {
        setContent(new ContentViewAdapter(widget));
    }

    @Override
    public Widget getContent() {
        return this.contentPanel.getWidget();
    }

    public Dimension getContentSize() {
        return new Dimension(this.contentPanel.getOffsetHeight(), this.contentPanel.getOffsetWidth());
    }

    public void showQuestionToLeavePrivacyMode(final Command callback) {
        Dialog.confirm(I18n.I.leavePrivacyModeQuestion(), callback);
    }

    public int getContentHeaderTop() {
        return this.contentHeaderPanel.getAbsoluteTop();
    }

    public int getContentHeaderLeft() {
        return this.contentHeaderPanel.getAbsoluteLeft();
    }

    public void setScale(float scale) {
        CssUtil.setScale(this.contentPanel.getElement().getStyle(), scale);
    }

    public void resetScale() {
        CssUtil.resetScale(this.contentPanel.getElement().getStyle());
    }
}
