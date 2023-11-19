package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.MenuBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTreeItemIdentifier;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * Author: umaurer
 * Created: 26.05.15
 */
public class DashboardNavigationWidget implements NavigationWidget, DashboardStateChangeHandler {
    public static final String DASHBOARD_NAV_ROOT = "root"; // $NON-NLS$
    private final DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.PX);
    private NavTree<NavItemSpec> tree;
    private final Button buttonAddDashboard;
    private final Button buttonCopyDashboard;
    private final Button buttonRemoveDashboard;
    private final Button buttonEditDashboard;

    private HistoryToken lastHistoryToken;
    private String selectedDashboardId;

    public DashboardNavigationWidget() {
        final Label labelHeader = new Label(I18n.I.dashboards());
        labelHeader.setStyleName("as-navHeader");
        labelHeader.getElement().getStyle().setHeight(MainView.CONTENT_HEADER_HEIGHT, Style.Unit.PX);

        final FloatingToolbar toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_M);
        toolbar.add(labelHeader);
        toolbar.addFill();

        this.buttonAddDashboard = Button.icon("x-tool-btn-plus") // $NON-NLS$
                .tooltip(PermStr.DASHBOARD_ADD.value())
                .clickHandler(clickEvent -> createNewDashboard())
                .build();
        this.buttonCopyDashboard = Button.icon("x-tool-copy") // $NON-NLS$
                .tooltip(PermStr.DASHBOARD_COPY.value())
                .clickHandler(clickEvent -> copyDashboard())
                .build();
        this.buttonRemoveDashboard = Button.icon("x-tool-btn-minus") // $NON-NLS$
                .tooltip(PermStr.DASHBOARD_DELETE.value())
                .clickHandler(clickEvent -> deleteDashboard())
                .build();
        this.buttonEditDashboard = Button.icon("x-tool-btn-edit") // $NON-NLS$
                .tooltip(PermStr.DASHBOARD_EDIT.value())
                .clickHandler(clickEvent -> editDashboard())
                .build();
        toolbar.add(this.buttonAddDashboard);
        toolbar.add(this.buttonCopyDashboard);
        toolbar.add(this.buttonRemoveDashboard);
        toolbar.add(this.buttonEditDashboard);
        final FlowPanel panelHeader = new FlowPanel();
        panelHeader.add(toolbar);

        this.layoutPanel.addNorth(panelHeader, MainView.CONTENT_HEADER_HEIGHT);

        reloadDashboards(null);
        EventBusRegistry.get().addHandler(DashboardStateChangeEvent.getType(), this);
    }

    @Override
    public void onStateChanged(final DashboardStateChangeEvent event) {
        switch (event.getAction()) {
            case UPDATE:
                reloadDashboards(event.getId());
                break;
        }
    }

    void reloadDashboards(String dashboardId) {
        if (this.tree != null) {
            this.tree.removeFromParent();
        }

        final NavItemSpec rootSpec = new NavItemSpec(DASHBOARD_NAV_ROOT, DASHBOARD_NAV_ROOT);
        NavItemSpec publicSpec = null;
        NavItemSpec privateSpec = null;
        NavItemSpec myspaceSpec = null;
        final List<DashboardConfig> listGlobalDashboards = ConfigDao.getInstance().getConfigsByRole(ConfigDao.DASHBOARD_ROLE_GLOBAL);
        Collections.sort(listGlobalDashboards, ConfigComparator.COMPARE_ACCESS_THEN_NAME);
        for (DashboardConfig dc : listGlobalDashboards) {
            final HistoryToken historyToken = createHistoryToken(dc.getId());
            final NavItemSpec nis = new NavItemSpec(dc.getId(), dc.getName(), historyToken);
            switch (dc.getAccess()) {
                case PUBLIC:
                    if (publicSpec == null) {
                        publicSpec = new NavItemSpec("public", I18n.I.dashboardGroupPublic()).withAlwaysOpen(); // $NON-NLS$
                    }
                    publicSpec.addChild(nis);
                    break;
                case PRIVATE:
                    if (privateSpec == null) {
                        privateSpec = new NavItemSpec("private", I18n.I.dashboardGroupPrivate()).withAlwaysOpen(); // $NON-NLS$
                    }
                    privateSpec.addChild(nis);
                    break;
                case MYSPACE:
                    if (myspaceSpec == null) {
                        myspaceSpec = new NavItemSpec("myspace", I18n.I.workspace()).withAlwaysOpen(); // $NON-NLS$
                    }
                    myspaceSpec.addChild(nis);
                    break;
            }
            if (this.lastHistoryToken == null) {
                this.lastHistoryToken = historyToken;
            }
        }
        if (publicSpec != null) {
            rootSpec.addChild(publicSpec);
        }
        if (privateSpec != null) {
            rootSpec.addChild(privateSpec);
        }
        if (myspaceSpec != null) {
            rootSpec.addChild(myspaceSpec);
        }

        this.tree = new NavTree<>(rootSpec);
        this.tree.addSelectionHandler(event -> {
            final NavItemSpec item = event.getSelectedItem();
            if (!item.isEnabled()) {
                return;
            }
            NavTree.select(tree, item);
            lastHistoryToken = item.getHistoryToken();
            setSelectedId(item.getId());
            lastHistoryToken.fire();
        });
        if (dashboardId != null && !dashboardId.equals(this.selectedDashboardId)) {
            selectDashboard(dashboardId, true);
        }
        else if (this.selectedDashboardId != null) {
            selectDashboard(this.selectedDashboardId, false);
        }
        this.layoutPanel.add(this.tree);
    }

    @Override
    public void onNavWidgetSelected() {
        if (this.lastHistoryToken == null) {
            PlaceUtil.goTo(DashboardPageController.HISTORY_TOKEN_DASHBOARDS);
        }
        else {
            this.lastHistoryToken.fire();
        }
    }

    @Override
    public void changeSelection(String[] ids) {
        final HistoryToken currentToken = HistoryToken.current();

        String dashboardId = currentToken.get("id"); // $NON-NLS$
        if (selectDashboard(dashboardId, false)) {
            this.lastHistoryToken = currentToken;
            return;
        }

        if (this.lastHistoryToken != null) {
            dashboardId = this.lastHistoryToken.get("id"); // $NON-NLS$
            selectDashboard(dashboardId, true);
        }
    }

    boolean selectDashboard(String dashboardId, boolean fireEvent) {
        setSelectedId(dashboardId);

        if (dashboardId == null) {
            return false;
        }

        final NavTree<NavItemSpec> treeItem = this.tree.getChildById(dashboardId, NavTreeItemIdentifier.NIS_IDENTIFIER);
        if (treeItem == null) {
            return false;
        }

        treeItem.setSelected(true, fireEvent);
        return true;
    }

    private void setSelectedId(String dashboardId) {
        this.selectedDashboardId = dashboardId;
        updateButtons(dashboardId);
    }

    @Override
    public boolean isResponsibleFor(String[] ids) {
        return ids != null && ids.length >= 1 && ids[0].equals(MenuBuilder.DASHBOARD_ID);
    }

    @Override
    public Widget asWidget() {
        return this.layoutPanel;
    }

    public HistoryToken createHistoryToken(String dashboardId) {
        return HistoryToken.builder(DashboardPageController.HISTORY_TOKEN_DASHBOARDS)
                .with("id", dashboardId) // $NON-NLS$
                .build();
    }

    private void updateButtons(String dashboardId) {
        final ConfigDao configDao = ConfigDao.getInstance();
        this.buttonAddDashboard.setEnabled(configDao.isCreateAllowed());
        final boolean dashExists = configDao.getConfigById(dashboardId) != null;
        this.buttonCopyDashboard.setEnabled(configDao.isCreateAllowed() && dashExists);
        this.buttonEditDashboard.setEnabled(configDao.isEditAllowed(dashboardId) && dashExists);
        this.buttonRemoveDashboard.setEnabled(configDao.isDeleteAllowed(dashboardId) && dashExists);
    }

    private void createNewDashboard() {
        DashboardStateChangeEvent.Action.EDIT_NEW.fire(DashboardController.getNextNewDashboardId());
    }

    private void copyDashboard() {
        DashboardStateChangeEvent.Action.EDIT_CLONE.fire(DashboardController.getCopyDashboardId(this.selectedDashboardId));
    }

    private void editDashboard() {
        DashboardStateChangeEvent.Action.EDIT_SELECTED.fire(this.selectedDashboardId);
    }

    private void deleteDashboard() {
        if (this.lastHistoryToken == null) {
            throw new IllegalStateException("<DashboardNavigationViewWidget.deleteDashboard> lastHistoryToken is null"); // $NON-NLS$
        }

        final String id = this.lastHistoryToken.get("id"); // $NON-NLS$

        this.tree.getChildById(id, NavTreeItemIdentifier.NIS_IDENTIFIER);
        final NavTree<NavItemSpec> navItem = this.tree.getChildById(id, NavTreeItemIdentifier.NIS_IDENTIFIER);
        final String name = navItem.getItem().getName();

        Dialog.confirm(PermStr.DASHBOARD.value(), PermStr.DASHBOARD_DELETE_CONFIRM.value(name), this::doDeleteDashboard);
    }

    private void doDeleteDashboard() {
        if (this.lastHistoryToken == null) {
            throw new IllegalStateException("<DashboardNavigationViewWidget.doDeleteDashboard> lastHistoryToken is null"); // $NON-NLS$
        }

        final String id = this.lastHistoryToken.get("id"); // $NON-NLS$
        final String followingId = getFollowingId(id);

        ConfigDao.getInstance().delete(id);
        final NavTree<NavItemSpec> navItem = this.tree.removeById(id, NavTreeItemIdentifier.NIS_IDENTIFIER);
        if (navItem.getParentNavTree() != null //delete parent if it has no children anymore
                && navItem.getParentNavTree().getChildren().isEmpty()
                && !DASHBOARD_NAV_ROOT.equals(navItem.getParentNavTree().getItem().getName())) {
            this.tree.removeById(navItem.getParentNavTree().getItem().getId(), NavTreeItemIdentifier.NIS_IDENTIFIER);
        }

        if (followingId == null) {
            this.lastHistoryToken.toBuilder().without("id").fire(); // $NON-NLS$
            updateButtons(null);
        }
        else {
            selectDashboard(followingId, true);
        }
    }

    private String getFollowingId(String id) {
        final List<DashboardConfig> listGlobalDashboards = ConfigDao.getInstance().getConfigsByRole(ConfigDao.DASHBOARD_ROLE_GLOBAL);
        for (int i = 0, size = listGlobalDashboards.size(); i < size; i++) {
            final DashboardConfig dc = listGlobalDashboards.get(i);
            if (id.equals(dc.getId())) {
                return i + 1 < size
                        ? listGlobalDashboards.get(i + 1).getId()
                        : i - 1 >= 0 ? listGlobalDashboards.get(i - 1).getId() : null;
            }
        }
        return null;
    }
}
