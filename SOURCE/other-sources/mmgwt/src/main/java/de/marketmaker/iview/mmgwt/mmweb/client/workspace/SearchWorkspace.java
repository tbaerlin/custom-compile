/*
 * SearchWorkspace.java
 *
 * Created on 13.02.13 07:31
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.Optional;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.TopToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.SearchHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.SearchResultEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel;

/**
 * @author Michael Lösch
 * @author mdick
 */
public class SearchWorkspace implements WorkspaceItem, NavigationWidget, SearchHandler,
        PrivacyMode.InterestedParty {
    private final DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.PX);

    private final SimplePanel panelHeader = new SimplePanel();

    private final SimplePanel panel = new SimplePanel();

    private static SearchWorkspace instance = null;

    private NavItemSpec depotObjectSpec;

    private NavItemSpec marketDataSpec;

    protected NavTree<NavItemSpec> tree;

    private Optional<NavTree<NavItemSpec>> selectedTreeItem = Optional.empty();

    private boolean searching = false;

    private SearchResultEvent lastEvent;

    private boolean privacyMode;

    private Optional<NavItemSpec> rootSpec = Optional.empty();

    public static SearchWorkspace getInstance() {
        if (instance == null) {
            if (SessionData.isAsDesign() && !SessionData.isWithPmBackend()) {
                instance = new SearchWorkspaceWithToolsMenu();
            }
            else {
                instance = new SearchWorkspace();
            }
        }
        return instance;
    }

    public SearchWorkspace() {
        this.layoutPanel.addNorth(this.panelHeader, MainView.CONTENT_HEADER_HEIGHT);
        this.layoutPanel.add(this.panel);
        EventBusRegistry.get().addHandler(SearchResultEvent.getType(), this);
        PrivacyMode.subscribe(this);
    }

    @Override
    public String getHeaderText() {
        return I18n.I.searchName();
    }

    @Override
    public void setHeaderVisible(boolean visible) {
        // nothing to do
    }

    @Override
    public void onNavWidgetSelected() {
        if (this.searching) {
            return;
        }
        if (this.privacyMode) {
            if (!SessionData.isWithMarketData()) {
                return;
            }
            PlaceUtil.goTo(SearchResultEvent.SearchDestination.DMXML.getControllerId());
            return;
        }
        final NavItemSpec selectedNavItemSpec = getLastEventSelectedNavItemSpec();
        if (selectedNavItemSpec == null) {
            PlaceUtil.goTo(SessionData.isWithPmBackend()
                    ? PmWebModule.HISTORY_TOKEN_SEARCH_DEPOT
                    : SearchResultEvent.SearchDestination.DMXML.getControllerId());
            return;
        }
        selectedNavItemSpec.goTo(this.lastEvent.getSearchText());
    }

    protected NavItemSpec getLastEventSelectedNavItemSpec() {
        if (this.lastEvent == null) {
            return null;
        }
        final NavItemSelectionModel selectionModel = this.lastEvent.getNavItemSelectionModel();
        if (selectionModel == null) {
            return null;
        }
        return selectionModel.getSelected();
    }

    @Override
    public void changeSelection(String[] ids) {
        if (this.lastEvent == null || !StringUtil.hasText(this.lastEvent.getSearchText())) {
            final Image image = IconImage.get("search-arrow-up").createImage(); // $NON-NLS$
            final Style style = image.getElement().getStyle();
            style.setMarginTop(4, Style.Unit.PX);
            style.setMarginLeft(20, Style.Unit.PX);
            this.panelHeader.setWidget(image);
            this.panel.setWidget(new HTML(I18n.I.searchPleaseEnterTerm()));
        }
    }

    @Override
    public Widget asWidget() {
        return this.layoutPanel;
    }

    @Override
    public String getStateKey() {
        return StateSupport.SEARCH;
    }


    @Override
    public void onSearchResult(final SearchResultEvent searchResultEvent) {
        this.lastEvent = searchResultEvent;
        this.panel.setWidget(new Label());

        if (searchResultEvent.getNavItemSelectionModel() instanceof ObjectTreeModel) {
            amendNavItemSpec();
            createTreeAndUpdatePanel(getRootNavItemSpec(), searchResultEvent.getSearchText());
        }

        final NavItemSpec subRoot2 = getDestNavItem(searchResultEvent.getSearchDestination());
        if (subRoot2 == null) {
            clear();
            return;
        }

        setSelectedTreeItem(NavTree.select(this.tree, searchResultEvent.getNavItemSelectionModel().getSelected()));
        this.searching = false;
    }

    protected void amendNavItemSpec() {
        if (this.lastEvent == null) {
            return;
        }
        if (this.lastEvent.getNavItemSelectionModel() instanceof ObjectTreeModel) {
            final ObjectTreeModel objectTreeModel = (ObjectTreeModel) this.lastEvent.getNavItemSelectionModel();

            final NavItemSpec rootSpec = getRootNavItemSpec();

            final NavItemSpec targetSpec = getSearchNavItemSpec(rootSpec);
            if(targetSpec == null) {
                Firebug.warn("<SearchWorkspace.amendNavItemSpec> Cannot amend search result items: getSearchNavItemSpec returned null.");
                return;
            }
            if (targetSpec.getChildren() != null) {
                targetSpec.getChildren().clear();
            }

            this.depotObjectSpec = null;
            this.marketDataSpec = null;

            if (!this.privacyMode && SessionData.isWithPmBackend()) {
                this.depotObjectSpec = new NavItemSpec("PMD", I18n.I.pmDepotobjects(), getAllPmDpToken(this.lastEvent)); // $NON-NLS$
                targetSpec.addChildren(this.depotObjectSpec.withAlwaysOpen());
            }

            if (SessionData.isWithMarketData()) {
                if (SessionData.isWithPmBackend()) {
                    this.marketDataSpec = new NavItemSpec("MMF", I18n.I.marketdata(), getAllDmxmlToken(this.lastEvent)); // $NON-NLS$
                    targetSpec.addChildren(this.marketDataSpec.withAlwaysOpen());
                }
                else {
                    this.marketDataSpec = targetSpec;
                }
            }

            final NavItemSpec subRoot = getDestNavItem(this.lastEvent.getSearchDestination());
            if (subRoot != null) {
                subRoot.addChildren(objectTreeModel.getRoot().getChildren());
                targetSpec.setEnabled(true);
            }
        }
    }

    protected NavItemSpec getRootNavItemSpec() {
        return this.rootSpec.orElseGet(() -> {
            final NavItemSpec value = new NavItemSpec("root", "root"); // $NON-NLS$
            this.rootSpec = Optional.of(value);
            return value;
        });
    }

    public void setRootSpec(NavItemSpec rootSpec) {
        this.rootSpec = Optional.ofNullable(rootSpec);
    }

    protected NavItemSpec getSearchNavItemSpec(NavItemSpec navItemSpec) {
        return navItemSpec;
    }

    protected void createAndSetContentHeader() {
        final Label labelHeader = createContentHeader();
        this.panelHeader.setWidget(labelHeader);
    }

    protected Label createContentHeader() {
        return doCreateContentHeader(I18n.I.resultType());
    }

    protected Label doCreateContentHeader(String label) {
        final Label labelHeader = new Label(label);
        labelHeader.setStyleName("as-navHeader");
        labelHeader.getElement().getStyle().setHeight(MainView.CONTENT_HEADER_HEIGHT, Style.Unit.PX);
        return labelHeader;
    }

    protected void createTreeAndUpdatePanel(NavItemSpec rootSpec, String searchText) {
        createAndSetContentHeader();

        this.tree = new NavTree<>(rootSpec);
        this.tree.addSelectionHandler(event -> onNavItemSelected(searchText, event));

        this.panel.setWidget(this.tree);
    }

    protected void onNavItemSelected(String searchText, SelectionEvent<NavItemSpec> event) {
        final NavItemSpec item = event.getSelectedItem();
        if (!item.isEnabled()) {
            return;
        }
        searching = true;
        if (searchText != null) {
            item.goTo(searchText);
        }
    }

    protected void clear() {
        this.panel.setWidget(new Label());
        this.panelHeader.setWidget(new Label());
        this.lastEvent = null;
        this.searching = false;
    }

    private NavItemSpec getDestNavItem(SearchResultEvent.SearchDestination dest) {
        final NavItemSpec subRoot;
        if (dest == SearchResultEvent.SearchDestination.PM_DEPOT) {
            subRoot = depotObjectSpec;
        }
        else if (dest == SearchResultEvent.SearchDestination.DMXML && SessionData.isWithMarketData()) {
            subRoot = marketDataSpec;
        }
        else {
            throw new IllegalStateException("unknown destination " + dest); // $NON-NLS$
        }
        return subRoot;
    }

    private HistoryToken getAllPmDpToken(SearchResultEvent searchResultEvent) {
        return HistoryToken.builder(SearchResultEvent.SearchDestination.PM_DEPOT.getControllerId())
                .with("s", searchResultEvent.getSearchText()) // $NON-NLS$
                .build();
    }

    private HistoryToken getAllDmxmlToken(SearchResultEvent searchResultEvent) {
        return HistoryToken.builder(SearchResultEvent.SearchDestination.DMXML.getControllerId())
                .with("s", searchResultEvent.getSearchText()) // $NON-NLS$
                .build();
    }

    public void searchDmxml(String searchText) {
        this.searching = true;
        TopToolbar.Util.search(TopToolbar.WP_SEARCH_KEY, searchText);
    }

    public void searchPm(String searchText) {
        if (this.privacyMode) {
            DebugUtil.showDeveloperNotification("searching PM objects is not allowed in privacy mode"); // $NON-NLS$
            return;
        }
        if (!StringUtil.hasText(searchText)) {
            return;
        }
        final String trimmedValue = searchText.trim();
        if (TopToolbar.Util.processInternalCommand(trimmedValue)) {
            AbstractMainController.INSTANCE.showMessage(I18n.I.ok());
            return;
        }
        this.searching = true;
        PlaceUtil.goTo(StringUtil.joinTokens(PmWebModule.HISTORY_TOKEN_SEARCH_DEPOT, "s=" + searchText)); // $NON-NLS$
    }

    @Override
    public boolean isResponsibleFor(String[] ids) {
        return !(ids == null || ids.length < 2) &&
                (ids[1].equals(SearchResultEvent.SearchDestination.DMXML.getControllerId())
                        || ids[1].equals(SearchResultEvent.SearchDestination.PM_DEPOT.getControllerId()));
    }

    @Override
    public void privacyModeStateChanged(boolean privacyModeActive,
            PrivacyMode.StateChangeProcessedCallback processed) {
        this.privacyMode = privacyModeActive;
        clear();
        processed.privacyModeStateChangeProcessed(this);
    }

    protected Optional<NavTree<NavItemSpec>> getSelectedTreeItem() {
        return selectedTreeItem;
    }

    protected void setSelectedTreeItem(Optional<NavTree<NavItemSpec>> selectedTreeItem) {
        if (selectedTreeItem == null) {
            throw new NullPointerException("Parameter selectedTreeItem of type Optional must not be null"); // $NON-NLS$
        }
        this.selectedTreeItem = selectedTreeItem;
    }

    protected void setSelectedTreeItem(NavTree<NavItemSpec> selectedTreeItem) {
        this.selectedTreeItem = Optional.ofNullable(selectedTreeItem);
    }
}