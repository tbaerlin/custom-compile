/*
 * InstrumentWorkspace.java
 *
 * Created on 05.04.2008 14:55:07
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.data.TreeModel;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.MainControllerListenerAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.PageType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;

import java.util.Collections;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PagesWorkspace extends AbstractTreeWorkspace<PagesWorkspaceItem> {
    public static final PagesWorkspace INSTANCE = new PagesWorkspace();

    public static final String ID = "pages"; // $NON-NLS-0$

    private PagesWorkspace() {
        super(I18n.I.pages());
    }

    @Override
    public String getStateKey() {
        return StateSupport.PAGES;
    }

    private void beforeStoreState() {
        final PagesWorkspaceConfig c = new PagesWorkspaceConfig();
        for (PagesWorkspaceItem workspaceItem : getContent()) {
            if (workspaceItem.getType() == PageType.CUSTOM) {
                continue;
            }
            c.addItem(workspaceItem.getItem());
        }
        SessionData.INSTANCE.getUser().getAppConfig().addWorkspace(ID, c);
    }

    @Override
    protected void restoreState() {
        handleCustomerPages();
        final PagesWorkspaceConfig c = (PagesWorkspaceConfig) SessionData.INSTANCE.getUser().getAppConfig().getWorkspaceConfig(ID);
        if (c != null) {
            List<PagesWorkspaceConfig.Item> items = c.getItems();
            sortItemsOnRestore(items);
            for (PagesWorkspaceConfig.Item item : items) {
                //noinspection deprecation
                if (item.getType() == PageType.REUTERS) {
                    continue;
                }
                addLeaf(new PagesWorkspaceItem(item));
            }
        }

        if(!(SessionData.isAsDesign())) {
            AbstractMainController.INSTANCE.addListener(new MainControllerListenerAdapter() {
                public void beforeStoreState() {
                    PagesWorkspace.this.beforeStoreState();
                }
            });
        }
    }

    private void sortItemsOnRestore(List<PagesWorkspaceConfig.Item> items) {
        Collections.sort(items, (o1, o2) -> o1.getOrder() - o2.getOrder());
    }

    private void handleCustomerPages() {
        final JSONWrapper cp = SessionData.INSTANCE.getGuiDef("customerPages"); // $NON-NLS$
        if (cp != null && cp.isValid()) {
            final JSONArray array = cp.getValue().isArray();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    final JSONValue entry = array.get(i);
                    final JSONValue page = entry.isObject().get("page"); // $NON-NLS$
                    final JSONValue name = entry.isObject().get("name"); // $NON-NLS$
                    ((TreeContentItem) addPage(page.isString().stringValue(), PageType.CUSTOM))
                            .setNameInWorkspace(name.isString().stringValue());
                }
            }
        }
    }

    public TreeModel addPage(String key, PageType type) {
        return addLeaf(new PagesWorkspaceItem(new PagesWorkspaceConfig.Item(key, type)));
    }

    @Override
    protected AbstractImagePrototype getLeafIcon() {
        return IconImage.get("mm-icon-vwdpage"); // $NON-NLS$
    }

    @Override
    protected void moveSelected(boolean up) {
        final TreeContentItem node = getSelectedLeaf();
        if (isCustomNode(node)) {
            AbstractMainController.INSTANCE.showError(I18n.I.pageActionMoveNotAllowed(node.getFolderName(), node.getNameInWorkspace()));
            return;
        }
        super.moveSelected(up);
    }

    @Override
    protected void removeNode(TreeModel node) {
        if (isCustomNode(node)) {
            AbstractMainController.INSTANCE.showError(I18n.I.pageActionDeleteNotAllowed(((PagesWorkspaceItem) node).getFolderName(),
                    ((PagesWorkspaceItem) node).getNameInWorkspace()));
            return;
        }
        super.removeNode(node);
    }

    @Override
    protected void renameNode(TreeContentItem node) {
        if (isCustomNode(node)) {
            AbstractMainController.INSTANCE.showError(I18n.I.pageActionRenameNotAllowed(node.getFolderName(), node.getNameInWorkspace()));
            return;
        }
        super.renameNode(node);
    }

    private boolean isCustomNode(TreeModel node) {
        return node instanceof PagesWorkspaceItem && ((PagesWorkspaceItem) node).getType() == PageType.CUSTOM;
    }

}
