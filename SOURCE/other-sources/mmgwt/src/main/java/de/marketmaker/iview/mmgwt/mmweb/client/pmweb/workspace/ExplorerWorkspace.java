package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.workspace;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.MainView;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.LayoutNode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer.ExplorerController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.StateSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.WorkspaceItem;
import de.marketmaker.iview.pmxml.GetWorkspaceRequest;
import de.marketmaker.iview.pmxml.GetWorkspaceResponse;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.WorkspacePartition;
import de.marketmaker.iview.pmxml.WorkspaceSheetDesc;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.AS_CREATE_PERSON;
import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.AS_CREATE_PROSPECT;

/**
 * @author Ulrich Maurer
 *         Date: 08.01.13
 */
public class ExplorerWorkspace implements WorkspaceItem, NavigationWidget {
    private static ExplorerWorkspace INSTANCE = null;
    private final DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.PX);
    private final FloatingPanel panel;
    private NavTree<NavItemSpec> tree;
    private NavItemSpec navItemInvestorList;
    private NavItemSpec navItemPortfolioList;
    private NavItemSpec navItemProspectList;
    private ArrayList<NavItemSpec> globalNavItems = new ArrayList<>();

    private final DmxmlContext.Block<GetWorkspaceResponse> investorsWorkspaceBlock;
    private final DmxmlContext.Block<GetWorkspaceResponse> portfoliosWorkspaceBlock;
    private final DmxmlContext.Block<GetWorkspaceResponse> prospectsWorkspaceBlock;
    private final DmxmlContext.Block<GetWorkspaceResponse> othersWorkspaceBlock;
    private final DmxmlContext dmxmlContext;

    /**
     * Explorer Workspaces are not PrivacyMode aware, because using the Explorer in PrivacyMode was not a requirement.
     */
    public static ExplorerWorkspace getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ExplorerWorkspace();
        }
        return INSTANCE;
    }

    private ExplorerWorkspace() {
        this.panel = new FloatingPanel(FloatingPanel.Orientation.VERTICAL);

        final Label labelHeader = new Label(I18n.I.customers());
        labelHeader.setStyleName("as-navHeader");
        labelHeader.getElement().getStyle().setHeight(MainView.CONTENT_HEADER_HEIGHT, Style.Unit.PX);

        final FloatingToolbar toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_M);
        toolbar.add(labelHeader);
        toolbar.addFill();
        final MenuButton newObjectsMenu = createNewObjectsMenu();
        if(newObjectsMenu != null) {
            toolbar.add(newObjectsMenu);
        }

        this.layoutPanel.addNorth(toolbar, MainView.CONTENT_HEADER_HEIGHT);
        this.layoutPanel.add(this.panel);

        this.dmxmlContext = new DmxmlContext();
        this.dmxmlContext.setCancellable(false);

        this.investorsWorkspaceBlock = this.dmxmlContext.addBlock("PM_GetWorkspace");// $NON-NLS$
        initWorkspaceBlock(ShellMMType.ST_INHABER, WorkspacePartition.WSP_STANDARD, this.investorsWorkspaceBlock);

        this.portfoliosWorkspaceBlock = this.dmxmlContext.addBlock("PM_GetWorkspace"); // $NON-NLS$
        initWorkspaceBlock(ShellMMType.ST_PORTFOLIO, WorkspacePartition.WSP_STANDARD, this.portfoliosWorkspaceBlock);

        this.prospectsWorkspaceBlock = this.dmxmlContext.addBlock("PM_GetWorkspace"); // $NON-NLS$
        initWorkspaceBlock(ShellMMType.ST_INTERESSENT, WorkspacePartition.WSP_STANDARD, this.prospectsWorkspaceBlock);
        this.prospectsWorkspaceBlock.setEnabled(Selector.AS_ACTIVITIES.isAllowed());

        this.othersWorkspaceBlock = this.dmxmlContext.addBlock("PM_GetWorkspace"); // $NON-NLS$
        initWorkspaceBlock(ShellMMType.ST_UNBEKANNT, WorkspacePartition.WSP_FINDER_DEP_MAN, this.othersWorkspaceBlock);

        issueRequest();
    }

    private MenuButton createNewObjectsMenu() {
        if (!AS_CREATE_PROSPECT.isAllowed() || !AS_CREATE_PERSON.isAllowed()) {
            return null;
        }
        final Menu menu = new Menu();
        menu.add(new MenuItem(I18n.I.prospectCreate(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_CREATE_PROSPECT);
            }
        }));
        return new MenuButton(I18n.I.createNewOneShort()).withMenu(menu).withClickOpensMenu();
    }

    private void initWorkspaceBlock(ShellMMType type, WorkspacePartition partition, DmxmlContext.Block block) {
        final GetWorkspaceRequest pReq = new GetWorkspaceRequest();
        pReq.setShellMMType(type);
        pReq.setPartition(partition);
        pReq.setListMode(true);
        block.setParameter(pReq);
    }

    @Override
    public String getHeaderText() {
        return I18n.I.customers();
    }

    @Override
    public void setHeaderVisible(boolean visible) {
        //ignored
    }

    private void issueRequest() {
        this.dmxmlContext.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                handleWorkspaceResponse();
            }
        });
    }

    private void handleWorkspaceResponse() {
        if (!this.portfoliosWorkspaceBlock.isResponseOk() ||
                !this.investorsWorkspaceBlock.isResponseOk() ||
                (Selector.AS_ACTIVITIES.isAllowed() && !this.prospectsWorkspaceBlock.isResponseOk()) ||
                !this.othersWorkspaceBlock.isResponseOk()) {
            return;
        }

        final NavItemSpec navItemSpecRoot = new NavItemSpec("root", "root"); // $NON-NLS$

        final ArrayList<NavItemSpec> navItemsList = new ArrayList<>();

        this.navItemInvestorList = new NavItemSpec(ExplorerController.Type.INVESTOR.getToken(),
                ExplorerController.Type.INVESTOR.getDisplayText(),
                ExplorerController.INSTANCE.createHistoryToken(null, null, ExplorerController.Type.INVESTOR.getToken())).withAlwaysOpen();
        navItemsList.add(this.navItemInvestorList);

        this.navItemPortfolioList = new NavItemSpec(ExplorerController.Type.PORTFOLIO.getToken(),
                ExplorerController.Type.PORTFOLIO.getDisplayText(),
                ExplorerController.INSTANCE.createHistoryToken(null, null, ExplorerController.Type.PORTFOLIO.getToken())).withAlwaysOpen();
        navItemsList.add(this.navItemPortfolioList);

        if (Selector.AS_ACTIVITIES.isAllowed()) {
            this.navItemProspectList = new NavItemSpec(ExplorerController.Type.PROSPECT.getToken(),
                    ExplorerController.Type.PROSPECT.getDisplayText(),
                    ExplorerController.INSTANCE.createHistoryToken(null, null, ExplorerController.Type.PROSPECT.getToken())).withAlwaysOpen();
            navItemsList.add(this.navItemProspectList);
        }

        navItemSpecRoot.addChildren(navItemsList.toArray(new NavItemSpec[navItemsList.size()]));

        addToTree(ExplorerController.Type.INVESTOR, this.investorsWorkspaceBlock, this.navItemInvestorList);
        addToTree(ExplorerController.Type.PORTFOLIO, this.portfoliosWorkspaceBlock, this.navItemPortfolioList);

        if (Selector.AS_ACTIVITIES.isAllowed()) {
            addToTree(ExplorerController.Type.PROSPECT, this.prospectsWorkspaceBlock, this.navItemProspectList);
        }

        addGlobalToTree(this.othersWorkspaceBlock, navItemSpecRoot);
        this.tree = new NavTree<>(navItemSpecRoot);
        this.tree.addSelectionHandler(new SelectionHandler<NavItemSpec>() {
            @Override
            public void onSelection(SelectionEvent<NavItemSpec> event) {
                event.getSelectedItem().getHistoryToken().fire();
                NavTree.select(tree, event.getSelectedItem());
            }
        });
        this.tree.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                panel.onResize();
            }
        });
        this.panel.setWidget(this.tree);
    }

    private void addSheetToTree(ExplorerController.Type type, WorkspaceSheetDesc sheet, NavItemSpec rootItem) {
        final NavItemSpec spec;
        final LayoutNode layoutNode = LayoutNode.create(sheet.getNodeId(), sheet.getLayoutGuid());
        if (StringUtil.hasText(sheet.getLayoutGuid())) {
            spec = new NavItemSpec(layoutNode.toString(), sheet.getCaption(),
                    ExplorerController.INSTANCE.createHistoryToken(null, layoutNode, type.getToken()));
        }
        else {
            spec = new NavItemSpec(layoutNode.toString(), sheet.getCaption());
        }
        rootItem.addChild(spec);

        final List<WorkspaceSheetDesc> childSheets = sheet.getSheets();
        if (!childSheets.isEmpty()) {
            spec.withClosingSiblings();
            for (WorkspaceSheetDesc childSheet : childSheets) {
                addSheetToTree(type, childSheet, spec);
            }
        }
    }

    private void addGlobalToTree(DmxmlContext.Block<GetWorkspaceResponse> block, NavItemSpec rootItem) {
        final List<WorkspaceSheetDesc> sheets = block.getResult().getSheets();
        for (WorkspaceSheetDesc sheet : sheets) {
            this.globalNavItems.add(addGlobalSheetToTree(sheet, rootItem, true));
        }
    }

    private NavItemSpec addGlobalSheetToTree(WorkspaceSheetDesc sheet, NavItemSpec rootItem, boolean alwaysOpen) {
        final String caption = SafeHtmlUtils.htmlEscape(sheet.getCaption());

        final NavItemSpec spec;
        final LayoutNode layoutNode = LayoutNode.create(sheet.getNodeId(), sheet.getLayoutGuid());
        if (StringUtil.hasText(sheet.getLayoutGuid())) {
            spec = new NavItemSpec(layoutNode.toString(), caption,
                    createGlobalAnalysisHistoryToken(layoutNode));
        }
        else {
            spec = new NavItemSpec(layoutNode.toString(), caption);
        }
        if (alwaysOpen) {
            spec.withAlwaysOpen();
        }
        rootItem.addChild(spec);

        final List<WorkspaceSheetDesc> childSheets = sheet.getSheets();
        if (!childSheets.isEmpty()) {
            spec.withClosingSiblings();
            for (WorkspaceSheetDesc childSheet : childSheets) {
                addGlobalSheetToTree(childSheet, spec, false);
            }
        }
        return spec;
    }

    public HistoryToken createGlobalAnalysisHistoryToken(LayoutNode layoutNode) {
        final HistoryToken.Builder builder = HistoryToken.builder(PmWebModule.HISTORY_TOKEN_GLOBAL_ANALYSIS);
        if (layoutNode != null) {
            builder.with(NavItemSpec.SUBCONTROLLER_KEY, layoutNode.toString());
        }
        return builder.build();
    }

    private void addToTree(ExplorerController.Type type, DmxmlContext.Block<GetWorkspaceResponse> block, NavItemSpec rootItem) {
        final List<WorkspaceSheetDesc> sheets = block.getResult().getSheets();
        for (WorkspaceSheetDesc sheet : sheets) {
            addSheetToTree(type, sheet, rootItem);
        }
    }

    @Override
    public Widget asWidget() {
        return this.layoutPanel;
    }

    @Override
    public String getStateKey() {
        return StateSupport.INVESTORS;
    }

    public void selectExplorerList(ExplorerController.Type type) {
        if (this.tree == null) {
            Firebug.warn("ExplorerWorkspace <selectExplorerList> tree is null");
            return;
        }
        if (type == ExplorerController.Type.INVESTOR) {
            NavTree.select(this.tree, this.navItemInvestorList);
        }
        if (type == ExplorerController.Type.PORTFOLIO) {
            NavTree.select(this.tree, this.navItemPortfolioList);
        }
        if (type == ExplorerController.Type.PROSPECT) {
            NavTree.select(this.tree, this.navItemProspectList);
        }
    }

    public void selectExplorerLayout(ExplorerController.Type type, LayoutNode layoutNode) {
        if (type == ExplorerController.Type.INVESTOR) {
            selectExplorerLayout(this.navItemInvestorList, layoutNode);
        }
        if (type == ExplorerController.Type.PORTFOLIO) {
            selectExplorerLayout(this.navItemPortfolioList, layoutNode);
        }
        if (type == ExplorerController.Type.PROSPECT) {
            selectExplorerLayout(this.navItemProspectList, layoutNode);
        }
    }

    public void selectGlobalLayout(LayoutNode layoutNode) {
        for (NavItemSpec navItem : this.globalNavItems) {
            selectExplorerLayout(navItem, layoutNode);
        }
    }

    private void selectExplorerLayout(NavItemSpec item, LayoutNode layoutNode) {
        if (layoutNode.equals(item.getId())) {
            NavTree.select(tree, item);
            return;
        }
        if (item.getChildren() != null) {
            final List<NavItemSpec> children = item.getChildren();
            for (NavItemSpec child : children) {
                selectExplorerLayout(child, layoutNode);
            }
        }
    }

    @Override
    public void onNavWidgetSelected() {
        ExplorerController.INSTANCE.createHistoryToken().fire();
    }

    @Override
    public void changeSelection(String[] ids) {
        //nothing to do. this is handled by ExplorerController calling selectExplorerList and/or selectExplorerLayout
    }

    @Override
    public boolean isResponsibleFor(String[] ids) {
        return !(ids == null || ids.length < 2) && (ids[1].equals(PmWebModule.HISTORY_TOKEN_EXPLORER));
    }
}