/*
 * SearchWorkspaceWithToolsMenu.java
 *
 * Created on 23.02.2016 13:51:25
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MenuModel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

public class SearchWorkspaceWithToolsMenu extends SearchWorkspace implements
        Consumer<MenuModel.Item> {

    public static final String SEARCH_RESULT_MENU_ID = "M_SS"; // $NON-NLS$

    @Override
    public void accept(MenuModel.Item item) {
        this.setRootSpec(createNavItems(item));
        createTreeAndUpdatePanel(getRootNavItemSpec(), null);
    }

    private NavItemSpec createNavItems(MenuModel.Item item) {
        return createChild(item);
    }

    private NavItemSpec createChild(MenuModel.Item item) {
        final NavItemSpec navItemSpec;
        if (item.getControllerId() != null) {
            navItemSpec = new NavItemSpec(item.getId(), item.getName(), HistoryToken.fromToken(item.getControllerId()));
        }
        else {
            navItemSpec = new NavItemSpec(item.getId(), item.getName()).withClosingSiblings().withSelectFirstChildOnOpen();
        }
        navItemSpec.setVisible(!item.isHidden());
        navItemSpec.setEnabled(item.isEnabled());

        if (!item.isLeaf()) {
            addChildren(navItemSpec, item.getItems());
        }
        return navItemSpec;
    }

    private void addChildren(NavItemSpec root, ArrayList<MenuModel.Item> items) {
        for (MenuModel.Item item : items) {
            root.addChild(createChild(item));
        }
    }

    @Override
    protected NavItemSpec getSearchNavItemSpec(NavItemSpec spec) {
        return spec.findChildById(SEARCH_RESULT_MENU_ID);
    }

    @Override
    protected Label createContentHeader() {
        return doCreateContentHeader(getRootNavItemSpec().getName());
    }

    @Override
    protected void onNavItemSelected(String searchText, SelectionEvent<NavItemSpec> event) {
        final NavItemSpec selectedSpec = event.getSelectedItem();
        if (!isSearchResult(selectedSpec)) {
            MainController.INSTANCE.selectionChanged(selectedSpec.getId());
            return;
        }
        super.onNavItemSelected(searchText, event);
    }

    private boolean isSearchResult(NavItemSpec selectedSpec) {
        while (selectedSpec != null) {
            if (SEARCH_RESULT_MENU_ID.equals(selectedSpec.getId())) {
                return true;
            }
            selectedSpec = selectedSpec.getParent();
        }
        return false;
    }

    @Override
    public boolean isResponsibleFor(String[] ids) {
        if (this.tree == null || ids == null || ids.length < 2) {
            return super.isResponsibleFor(ids);
        }
        NavTree<NavItemSpec> branch = this.tree;
        for (int i = 1; i < ids.length; i++) {
            branch = branch.findNavTreeById(ids[i], (navItemSpec, id) ->
                    StringUtil.equals(navItemSpec.getId(), id)
                            || navItemSpec.isLeaf() && navItemSpec.getHistoryToken() != null && StringUtil.equals(navItemSpec.getHistoryToken().get(0, null), id)
                            || super.isResponsibleFor(ids));
            if (branch == null) {
                return super.isResponsibleFor(ids);
            }
        }
        return true;
    }

    @Override
    public void changeSelection(String[] ids) {
        NavTree<NavItemSpec> treeItem = this.tree;
        for (int i = 1; i < ids.length; i++) {
            final NavTree<NavItemSpec> child = NavTree.findNavTreeById(treeItem, ids[i], (navItemSpec, id) -> StringUtil.equals(id, navItemSpec.getId()));
            if (child == null) {
                break;
            }
            treeItem = child;
        }
        setAndSelectTreeItem(treeItem);
    }

    @Override
    public void onNavWidgetSelected() {
        final Optional<NavTree<NavItemSpec>> selectedTreeItem = getSelectedTreeItem();
        if (selectedTreeItem.isPresent() && selectedTreeItem.get().getItem().hasSelectionHandler()) {
            final NavTree<NavItemSpec> treeElement = selectedTreeItem.get();
            treeElement.fireSelectionEvent();
        }
        else {
            final Optional<NavTree<NavItemSpec>> treeElement = findSelectable(this.tree);
            setSelectedTreeItem(treeElement);
            treeElement.ifPresent(selectedTree -> selectedTree.setSelected(true, true));
        }
    }

    private Optional<NavTree<NavItemSpec>> findSelectable(NavTree<NavItemSpec> navTree) {
        final NavItemSpec item = navTree.getItem();

        if (item.hasSelectionHandler() && item.isVisible() && item.isEnabled()) {
            return Optional.of(navTree);
        }
        final List<NavTree<NavItemSpec>> children = navTree.getChildren();
        if (children != null) {
            for (NavTree<NavItemSpec> child : children) {
                final Optional<NavTree<NavItemSpec>> selectable = findSelectable(child);
                if (selectable.isPresent()) {
                    return selectable;
                }
            }
        }
        return Optional.empty();
    }

    private void setAndSelectTreeItem(NavTree<NavItemSpec> treeItem) {
        getSelectedTreeItem().ifPresent(navTree -> navTree.setSelected(false, false));
        setSelectedTreeItem(treeItem);
        getSelectedTreeItem().ifPresent(navTree -> navTree.setSelected(true, false));
    }

    @Override
    protected void clear() {
        // do nothing
    }
}