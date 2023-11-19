package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.NavigationWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.MenuItemAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;

import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 08.01.13
 */
public class MenuTree implements NavigationWidget, NeedsScrollLayout {
    private final DockLayoutPanel layoutPanel = new DockLayoutPanel(Style.Unit.PX);
    private final FloatingPanel floatingPanel = new FloatingPanel(FloatingPanel.Orientation.VERTICAL);
    private final NavTree<MenuItemAdapter> tree;

    private NavTree selectedTreeItem = null;

    public MenuTree(final MenuModel.Item rootItem) {
        this.tree = new NavTree<>(new MenuItemAdapter(rootItem));
        this.tree.addSelectionHandler(new SelectionHandler<MenuItemAdapter>() {
            @Override
            public void onSelection(SelectionEvent<MenuItemAdapter> event) {
                Firebug.log("selected: " + event.getSelectedItem().getName());
                final MenuModel.Item menuItem = event.getSelectedItem().getMenuItem();
                if (menuItem.isLinkItem()) {
                    Window.open(menuItem.getControllerId(), "mmweb-" + menuItem.getId(), ""); // $NON-NLS$
                    Firebug.debug("link item click: " + menuItem.getControllerId());
                }
                else if (menuItem.getControllerId() != null) {
                    MainController.INSTANCE.selectionChanged(menuItem.getId());
                }
            }
        });
        this.tree.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                Firebug.log("tree.onResize");
                floatingPanel.onResize();
            }
        });
        this.floatingPanel.setWidget(this.tree);
        final Label labelHeader = new Label(rootItem.getName());
        labelHeader.setStyleName("as-navHeader");
        this.layoutPanel.addNorth(labelHeader, MainView.CONTENT_HEADER_HEIGHT);
        this.layoutPanel.add(this.floatingPanel);
    }

    public void onNavWidgetSelected() {
        if (this.selectedTreeItem == null || !this.selectedTreeItem.getItem().hasSelectionHandler()) {
            this.selectedTreeItem = findSelectable(tree);
            this.selectedTreeItem.setSelected(true, true);
        }
        else {
            this.selectedTreeItem.fireSelectionEvent();
        }
    }

    private NavTree<MenuItemAdapter> findSelectable(NavTree<MenuItemAdapter> navTree) {
        if (navTree.getItem().hasSelectionHandler()) {
            return navTree;
        }
        final List<NavTree<MenuItemAdapter>> children = navTree.getChildren();
        if (children != null) {
            for (NavTree<MenuItemAdapter> child : children) {
                final NavTree<MenuItemAdapter> selectable = findSelectable(child);
                if (selectable != null) {
                    return selectable;
                }
            }
        }
        return null;
    }

    public void changeSelection(String[] ids) {
        NavTree<MenuItemAdapter> treeItem = this.tree;
        for (int i = 1; i < ids.length; i++) {
            final NavTree<MenuItemAdapter> child = getChild(treeItem, ids[i]);
            if (child == null) {
                break;
            }
            treeItem = child;
        }
        setSelected(treeItem);
    }

    private NavTree<MenuItemAdapter> getChild(NavTree<MenuItemAdapter> treeItem, String id) {
        final List<NavTree<MenuItemAdapter>> childs = treeItem.getChildren();
        if (childs == null) {
            return null;
        }
        for (NavTree<MenuItemAdapter> child : childs) {
            if (id.equals(child.getItem().getMenuItem().getId())) {
                return child;
            }
        }
        return null;
    }

    private void setSelected(NavTree<MenuItemAdapter> treeItem) {
        if (this.selectedTreeItem != null) {
            this.selectedTreeItem.setSelected(false, false);
        }
        this.selectedTreeItem = treeItem;
        this.selectedTreeItem.setSelected(true, false);
    }

    @Override
    public Widget asWidget() {
        return this.layoutPanel;
    }

    @Override
    public boolean isResponsibleFor(String[] ids) {
        if (this.tree == null || ids == null || ids.length < 2) {
            return false;
        }
        NavTree<MenuItemAdapter> branch = this.tree;
        for (int i = 1; i < ids.length; i++) {
            branch = branch.findNavTreeById(ids[i], MenuItemAdapter.MIA_IDENTIFIER);
            if (branch == null) {
                return false;
            }
        }
        return true;
    }
}