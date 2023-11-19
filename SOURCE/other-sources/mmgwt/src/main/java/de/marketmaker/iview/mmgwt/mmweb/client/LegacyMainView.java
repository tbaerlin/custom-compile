/*
 * MainView.java
 *
 * Created on 03.03.2008 08:29:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Message;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JsUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentViewAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsCenterPanelHeadingInvisible;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.Workspace;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceList;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceLists;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LegacyMainView extends AbstractMainView {
    private static final int MESSAGE_DISPLAY_TIME_SECONDS = 5;

    private static final int ERROR_MESSAGE_DISPLAY_TIME_SECONDS = 10;

    private static final String TOPMENU_ID_PREFIX = "topmenu-"; // $NON-NLS-0$

    protected final AbstractMainController controller;

    private TabPanel topMenu;

    private final Map<String, Button> subMenuButtons = new HashMap<>();

    private final Map<String, TabItem> itemsById = new HashMap<>();

    private Button subMenuButtonSelected = null;

    protected LayoutContainer layoutContainer;

    private final FlowPanel message = new FlowPanel();

    private boolean topMenuListenerEnabled = true;

    protected ContentPanel centerPanel;
    protected CardLayout centerPanelCardLayout;
    protected ContentPanel centerPanelContent;
    private SafeHtml contentHeader;

    private boolean centerPanelHeaderVisible = true;

    protected Workspace workspaceWest = null;
    private Workspace workspaceEast = null;
    protected final TopToolbar topToolbar;

    public LegacyMainView(final AbstractMainController mc) {
        Document.get().getBody().addClassName("legacyView"); // $NON-NLS$
        this.controller = mc;
        this.topToolbar = createTopToolbar();
    }

    protected TopToolbar createTopToolbar() {
        return new LegacyTopToolbar(ToolbarConfiguration.DEFAULT);
    }

    public void init() {
        this.layoutContainer = createLayoutContainer();
        this.layoutContainer.addStyleName("mm-mainPanel"); // $NON-NLS-0$
        this.layoutContainer.setBorders(false);
        this.layoutContainer.setLayout(new BorderLayout());

        initSouthPanel();
        initWestPanel();
        initEastPanel();
        initNorthPanel();
        initCenterPanel();
        onPanelsInitialized();

        BrowserSpecific.addBodyStyles();

        if (SessionData.INSTANCE.getUser().getAppTitle() != null) {
            Window.setTitle(SessionData.INSTANCE.getUser().getAppTitle());
        }

        layout();
    }

    protected void layout() {
        this.layoutContainer.layout();
    }

    protected LayoutContainer createLayoutContainer() {
        final LayoutContainer result = new Viewport();
        RootPanel.get().add(result);
        return result;
    }

    @SuppressWarnings({"GWTStyleCheck"})
    private void initCenterPanel() {
        this.centerPanelCardLayout = new CardLayout();
        this.centerPanel = new ContentPanel(this.centerPanelCardLayout);
        this.centerPanel.setHeaderVisible(false);
        this.centerPanel.addStyleName("mm-content"); // $NON-NLS-0$
        this.centerPanel.add(new Label("")); // $NON-NLS$

        if (!Tooltip.initialize()) { // try to initialize itools/Tooltip or use gxt/QuickTip below
            new QuickTip(this.centerPanel) {
                @Override
                public void show() {
                    //ToDo: HACK! There is no other way to get the value of "text"
                    //ToDo: This workaround averts upcoming tooltips with empty text
                    if (!StringUtil.hasText(this.text)) {
                        return;
                    }
                    super.show();
                }
            };
        }

        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        this.layoutContainer.add(this.centerPanel, centerData);
    }

    protected void initNorthPanel() {
        this.topMenu = new TabPanel();
        this.topMenu.addStyleName("mm-topMenu");
        this.topMenu.setBorders(false);
        for (MenuModel.Item e : this.controller.getMenuModel().getItems()) {
            final TabItem item = createMenuBar(e);
            this.topMenu.add(item);
            this.itemsById.put(e.getId(), item);
        }

        this.topMenu.addListener(Events.Select, new Listener<TabPanelEvent>() {
            private boolean seenFirst = false;

            public void handleEvent(TabPanelEvent event) {
                // onTabChange will also be called when first rendered, but we need to ignore that one
                if (topMenuListenerEnabled && this.seenFirst) {
                    final TabItem item = event.getItem();
                    selectionChanged(item.getId().substring(TOPMENU_ID_PREFIX.length()));
                }
                this.seenFirst = true;
            }
        });


        final ContentPanel northPanel = new ContentPanel(new BorderLayout());
        northPanel.setHeaderVisible(false);
        northPanel.addStyleName("mm-topPanel");
        northPanel.addListener(Events.Render, (Listener<ComponentEvent>) event -> {
            final String backgroundImage = getBackgroundImage();
            if (backgroundImage != null) {
                northPanel.getElement().getStyle().setBackgroundImage(backgroundImage);
            }
        });

        final int topBannerHeight;
        final String jvTopBanner = SessionData.INSTANCE.getGuiDefValue("topBanner", "height"); // $NON-NLS$
        if (jvTopBanner == null) {
            topBannerHeight = 0;
        }
        else {
            topBannerHeight = Integer.parseInt(jvTopBanner);
            final FlowPanel fp = new FlowPanel();
            fp.addStyleName("mm-topBannerDivOuter");
            final Label lbl = new HTML("&nbsp;"); // $NON-NLS$
            lbl.addStyleName("mm-topBannerDivInner");
            fp.add(lbl);
            northPanel.add(fp, noSplitBorderLayoutData(Style.LayoutRegion.NORTH, topBannerHeight));
        }

        northPanel.add(this.topToolbar.asWidget(), new BorderLayoutData(Style.LayoutRegion.CENTER));
        northPanel.add(this.topMenu, noSplitBorderLayoutData(Style.LayoutRegion.SOUTH, 56));

        // TODO 2.0: How to avoid absolute sizes?
        this.layoutContainer.add(northPanel, noSplitBorderLayoutData(Style.LayoutRegion.NORTH, topBannerHeight + 80));
    }

    private String getBackgroundImage() {
        final String legacyLogoUrl = JsUtil.getMetaValue("legacyLogoUrl"); // logo url is defined in meta tag of html file // $NON-NLS$
        if (legacyLogoUrl != null) {
            return legacyLogoUrl;
        }
        @SuppressWarnings("deprecation") final String userProductLogo = Customer.INSTANCE.getLegacyLogoBackgroundImage();
        if (userProductLogo != null) {
            return userProductLogo;
        }
        final String topPanelLogo = SessionData.INSTANCE.getGuiDefValue("legacyTopPanelLogo"); // $NON-NLS$
        if (topPanelLogo == null) {
            return null;
        }
        final String customerName = SessionData.INSTANCE.getUser().getCustomerName();
        if (topPanelLogo.contains("{text}") && customerName == null) { // $NON-NLS$
            return null;
        }
        return "url(" + UrlBuilder.forDmxml(topPanelLogo.replace("{text}", URL.encodeQueryString(customerName))).toURL() + ")"; // $NON-NLS$
    }

    public void resetWestAndEastPanel() {
        WorkspaceLists.INSTANCE.reset();
        initWestPanel();
        initEastPanel();
        this.layoutContainer.layout();
    }

    protected void initWestPanel() {
        final WorkspaceList workspaceListWest = WorkspaceLists.INSTANCE.getWorkspaceListWest();
        initPanel(this.workspaceWest, workspaceListWest, Style.LayoutRegion.WEST, new Margins(0, 4, 0, 0));
        if (workspaceListWest == null ) {
            this.workspaceWest = null;
        } else {
            this.workspaceWest = workspaceListWest.getWorkspace();
            this.workspaceWest.setupTeaser();
        }
    }

    protected void initEastPanel() {
        final WorkspaceList workspaceListEast = WorkspaceLists.INSTANCE.getWorkspaceListEast();
        initPanel(this.workspaceEast, workspaceListEast, Style.LayoutRegion.EAST, new Margins(0, 0, 0, 4));
        if (workspaceListEast == null) {
            this.workspaceEast = null;
        } else {
            this.workspaceEast = workspaceListEast.getWorkspace();
            this.workspaceEast.setupTeaser();
        }
    }

    protected void initPanel(Workspace previousWorkspace, WorkspaceList workspaceList, Style.LayoutRegion layoutRegion, Margins margins) {
        final BorderLayout layout = (BorderLayout) layoutContainer.getLayout();
        boolean expanded = true;
        if (previousWorkspace != null) {
            expanded = previousWorkspace.getContentPanel().isExpanded();
            layout.expand(layoutRegion);
            this.layoutContainer.remove(previousWorkspace.getContentPanel());
        }
        if (workspaceList != null) {
            final int width = Math.min(400, Math.max(100, workspaceList.getPropertyWidth(150)));
            final BorderLayoutData data = new BorderLayoutData(layoutRegion, width, 100, 400);
            data.setMargins(margins);
            data.setCollapsible(true);
            data.setFloatable(true);
            data.setSplit(true);
            this.layoutContainer.add(workspaceList.getWorkspace().getContentPanel(), data);
            if (!expanded) {
                layout.collapse(layoutRegion);
            }
        }
    }

    protected void initSouthPanel() {
        initMessages();
        layoutMessagePanel();
    }

    private void initMessages() {
        final Map<String, Message> map = new LinkedHashMap<>();
        addMessagesToMap("footer_messages", map); // $NON-NLS$
        addMessagesToMap("footer_messages_zone", map); // $NON-NLS$
        for (Message message : map.values()) {
            addMessage(message, false);
        }
    }

    private void addMessagesToMap(String guidefKey, Map<String, Message> map) {
        final JSONValue jvFooterMessages = SessionData.INSTANCE.getGuiDef(guidefKey).getValue();
        if (jvFooterMessages == null) {
            return;
        }
        final JSONArray ja = jvFooterMessages.isArray();
        for (int i = 0, size = ja.size(); i < size; i++) {
            final JSONObject joMessage = ja.get(i).isObject();
            String html = joMessage.get("html").isString().stringValue(); // $NON-NLS$
            final JSONValue jvSelector = joMessage.get("selector"); // $NON-NLS$
            if (jvSelector != null) {
                final String selector = jvSelector.isString().stringValue();
                if (!Selector.valueOf(selector).isAllowed()) {
                    continue;
                }
            }
            // use the id to allow overwriting messages of common guidefs.json in guidefs-zone.json
            final String id = joMessage.get("id").isString().stringValue(); // $NON-NLS$
            final boolean copyrightMessage = "copyright".equals(id); // $NON-NLS$
            if (copyrightMessage) {
                html = html.replace("@year@", String.valueOf(new MmJsDate().getFullYear())); // $NON-NLS$
                html = html.replace("@version@", Version.INSTANCE.build()); // $NON-NLS$
                html = html.replace("@login@", SessionData.INSTANCE.getUser() == null ? "~" : SessionData.INSTANCE.getUser().getLogin()); // $NON-NLS$
            }
            final String style = joMessage.get("style").isString().stringValue(); // $NON-NLS$
            final int displayTimeSeconds = Integer.valueOf(joMessage.get("displayTimeSeconds").isString().stringValue()); // $NON-NLS$
            final SafeHtml safeHtml = new SafeHtmlBuilder().appendHtmlConstant(html).toSafeHtml();
            final Message message = new Message(id, safeHtml, style, displayTimeSeconds);
            if (!copyrightMessage) {
                message.addCloseButton(event -> removeMessage(message));
            }
            map.put(id, message);
        }
    }

    protected void onPanelsInitialized() {
        // implementation in subclasses
    }

    protected BorderLayoutData noSplitBorderLayoutData(Style.LayoutRegion region, float size) {
        final BorderLayoutData result = new BorderLayoutData(region, size);
        result.setSplit(false);
        return result;
    }

    public void showError(SafeHtml safeHtml) {
        addMessage(new Message("error", safeHtml, "mm-error", ERROR_MESSAGE_DISPLAY_TIME_SECONDS), true); // $NON-NLS$
    }

    public void showMessage(SafeHtml safeHtml) {
        addMessage(new Message("message", safeHtml, "message", MESSAGE_DISPLAY_TIME_SECONDS), true); // $NON-NLS$
    }

    private void addMessage(Message message, final boolean layout) {
        this.message.insert(message, 0);
        final int displayTimeSeconds = message.getDisplayTimeSeconds();
        if (displayTimeSeconds > 0) {
            scheduleRemove(message, displayTimeSeconds * 1000);
            if (layout) {
                layoutMessagePanel();
            }
        }
    }

    private void removeMessage(Message message) {
        this.message.remove(message);
        layoutMessagePanel();
    }

    private void layoutMessagePanel() {
        if (this.layoutContainer == null) {
            return;
        }
        this.layoutContainer.remove(this.message);
        final BorderLayoutData southernLayoutData = new BorderLayoutData(Style.LayoutRegion.SOUTH, 18f * this.message.getWidgetCount());
        southernLayoutData.setSplit(false);
        this.layoutContainer.add(this.message, southernLayoutData);
        this.layoutContainer.layout();
    }

    private void scheduleRemove(final Message message, int displayTimeMillis) {
        new Timer() {
            public void run() {
                removeMessage(message);
            }
        }.schedule(displayTimeMillis);
    }

    public void setContent(Widget widget) {
        setContent(new ContentViewAdapter(widget));
    }

    @Override
    protected void setContentAfterChange(ContentView contentView) {
        removeAllNormalContentViews();

        final Widget content = contentView.getWidget();
        Firebug.log("content: " + (content != null));

        final boolean headerVisible = !(contentView instanceof NeedsCenterPanelHeadingInvisible);
        this.centerPanelContent = new ContentPanel();
        this.centerPanelContent.setHeaderVisible(this.centerPanelHeaderVisible && headerVisible);
        this.centerPanelContent.getHeader().setText(this.contentHeader == null ? null : this.contentHeader.asString());
        this.centerPanelContent.add(content);

        if (content instanceof NeedsScrollLayout) {
            this.centerPanelContent.addStyleName("mm-contentScroll");
            this.centerPanelContent.setScrollMode(Style.Scroll.AUTO);
        }
        else {
            this.centerPanelContent.setLayout(new FitLayout());
        }
        this.centerPanel.add(this.centerPanelContent);
        this.centerPanel.layout();
    }

    private void removeAllNormalContentViews() {
        for (int i = this.centerPanel.getItemCount() - 1; i >= 0; i--) {
            final Component component = this.centerPanel.getItem(i);
            if (!component.getStyleName().contains("mm-mainCardContent")) { // $NON-NLS$
                this.centerPanel.remove(component);
            }
        }
    }

    public void changeSelection(final String[] ids, boolean setNavWidget) {
        final String topmenuId = TOPMENU_ID_PREFIX + ids[0];
        if (!topmenuId.equals(this.topMenu.getSelectedItem().getId())) {
            this.topMenuListenerEnabled = false;
            this.topMenu.setSelection(this.itemsById.get(ids[0]));
            this.topMenuListenerEnabled = true;
        }
        final String key = ids.length >= 2 ? ids[1] : null;
        selectSubMenuButton(this.subMenuButtons.get(key));
    }

    public void setContentHeader(SafeHtml safeHtml) {
        this.contentHeader = safeHtml;
        if (this.centerPanelContent != null) {
            Firebug.debug("setContentHeader(" + safeHtml.asString() + ")");
            this.centerPanelContent.getHeader().setText(safeHtml.asString());
        }
        else {
            Firebug.debug("setContentHeader(" + safeHtml.asString() + ")  --->   cpc == null");
        }
        final HistoryThreadManager historyThreadManager = MainController.INSTANCE.getHistoryThreadManager();
        historyThreadManager.setThreadTitle(historyThreadManager.getActiveThreadId(), safeHtml);
    }

    public void setContentHeaderVisible(boolean visible) {
        this.centerPanelHeaderVisible = visible;
    }

    public SafeHtml getContentHeader() {
        return this.contentHeader;
    }

    public void onLogout() {
        if (this.workspaceWest != null) {
            this.layoutContainer.remove(this.workspaceWest.getContentPanel());
            this.workspaceWest = null;
        }
        if (this.workspaceEast != null) {
            this.layoutContainer.remove(this.workspaceEast.getContentPanel());
            this.workspaceWest = null;
        }
    }

    private void selectSubMenuButton(Button button) {
        if (this.subMenuButtonSelected != null) {
            this.subMenuButtonSelected.setActive(false);
        }
        this.subMenuButtonSelected = button;
        if (this.subMenuButtonSelected != null) {
            this.subMenuButtonSelected.setActive(true);
        }
    }

    public void setWindowTitlePrefix(String prefix) {
        Window.setTitle(prefix + " - " + SessionData.INSTANCE.getUser().getAppTitle()); // $NON-NLS-0$
    }

    public void setDefaultWindowTitle() {
        Window.setTitle(SessionData.INSTANCE.getUser().getAppTitle());
    }

    class MenuBarButtonAdapter implements ClickHandler {
        private final String id;

        public MenuBarButtonAdapter(String id) {
            this.id = id;
        }

        @Override
        public void onClick(ClickEvent event) {
            selectionChanged(this.id);
            selectSubMenuButton((Button) event.getSource());
        }
    }

    class SubMenuAdapter implements ClickHandler {
        private final String id;

        public SubMenuAdapter(String id) {
            this.id = id;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            selectionChanged(this.id);
        }
    }

    class MenuLinkAdapter implements ClickHandler {
        private final String id;
        private final String url;

        public MenuLinkAdapter(String id, String url) {
            this.id = id;
            this.url = url;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            Window.open(this.url, "mmweb-" + this.id, ""); // $NON-NLS$
        }
    }

    protected TabItem createMenuBar(MenuModel.Item e) {
        final FloatingToolbar menuBar = new FloatingToolbar();
        menuBar.addStyleName("mm-subMenu"); // $NON-NLS-0$
        boolean first = true;
        for (final MenuModel.Item item : e.getItems()) {
            if (item.isHidden()) {
                continue;
            }

            if (first) {
                first = false;
            }
            else {
                menuBar.addSeparator();
            }

            if (item.isLeaf() || onlyHiddenItems(item)) {
                final Button button = Button.text(item.getName())
                        .clickHandler(new MenuBarButtonAdapter(item.getId()))
                        .build();
                button.setEnabled(item.isEnabled());
                if (this.subMenuButtons.containsKey(item.getId())) {
                    Dialog.error(I18n.I.errorUppercase(), "double menu key: " + item.getId());  // $NON-NLS-0$
                }
                else {
                    this.subMenuButtons.put(item.getId(), button);
                }
                menuBar.add(button);
            }
            else {
                final Menu menu = new Menu();
                for (final MenuModel.Item e2 : item.getItems()) {
                    if (e2 == MenuModel.SEPARATOR) {
                        menu.addSeparator();
                        continue;
                    }

                    if (e2.isLinkItem()) {
                        final MenuItem menuItem = new MenuItem(e2.getName(), new MenuLinkAdapter(e2.getId(), e2.getControllerId()));
                        IconImage.setIconStyle(menuItem, e2.getIconStyle());
                        menuItem.setEnabled(e2.isEnabled());
                        menu.add(menuItem);

                    }
                    else if (!e2.isHidden()) {
                        final MenuItem menuItem = new MenuItem(e2.getName(), new SubMenuAdapter(e2.getId()));
                        IconImage.setIconStyle(menuItem, e2.getIconStyle());
                        menuItem.setEnabled(e2.isEnabled());
                        menu.add(menuItem);
                    }

                    if (e2.isWithSeparator()) {
                        menu.addSeparator();
                    }
                }
                final MenuButton button = new MenuButton(item.getName()).withClickHandler(new MenuBarButtonAdapter(item.getId())).withMenu(menu);
                button.setEnabled(item.isEnabled());
                if (this.subMenuButtons.containsKey(item.getId())) {
                    Dialog.error(I18n.I.errorUppercase(), "double menu key: " + item.getId());  // $NON-NLS-0$
                }
                else {
                    this.subMenuButtons.put(item.getId(), button);
                }
                menuBar.add(button);
            }
        }
        final ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setBorders(false);
        panel.setTopComponent(menuBar);

        final TabItem result = new TabItem(e.getName());
        result.setLayout(new FitLayout());
        result.setId(TOPMENU_ID_PREFIX + e.getId());
        result.add(panel);
        return result;
    }

    private boolean onlyHiddenItems(MenuModel.Item item) {
        for (final MenuModel.Item child : item.getItems()) {
            if (!child.isHidden() && child != MenuModel.SEPARATOR) {
                return false;
            }
        }
        return true;
    }


    private void selectionChanged(final String id) {
        this.controller.selectionChanged(id);
    }

    @Override
    public TopToolbar getTopToolbar() {
        return this.topToolbar;
    }
}
