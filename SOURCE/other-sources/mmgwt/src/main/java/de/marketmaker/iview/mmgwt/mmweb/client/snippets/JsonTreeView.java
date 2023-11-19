/*
 * JsonTreeView.java
 *
 * Created on 13.03.2009 11:40:59
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreeStyle;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Folder;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TreeLeaf;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class JsonTreeView extends SnippetView<JsonTreeSnippet> {
    private TreePanel<ModelData> treePanel;

    protected TreeStore<ModelData> store;

    private TreeLeaf firstNode;

    private static final String ROOT_NAME = "root"; // $NON-NLS-0$

    public JsonTreeView(JsonTreeSnippet snippet) {
        super(snippet);
        setTitle(snippet.getConfiguration().getString("title", "KWT")); // $NON-NLS-0$ $NON-NLS-1$
        initTreePanel();
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.treePanel);
    }

    String getFirstNodeText() {
        return getNodeText(this.firstNode);
    }

    String getFirstNodeData() {
        return this.firstNode.get("data"); // $NON-NLS-0$
    }

    private String getName(TreeModel tm) {
        return tm.get("name"); // $NON-NLS-0$
    }

    private String getNodeText(TreeModel tm) {
        String s = getName(tm);
        TreeModel parent = tm.getParent();
        while (parent != null && !ROOT_NAME.equals(getName(parent))) {
            s = getName(parent) + AbstractMainController.INSTANCE.getHeaderSeparator() + s;
            parent = parent.getParent();
        }
        return s;
    }

    private void initTreePanel() {
        initTreeStore();

        this.treePanel = new TreePanel<>(this.store);
        this.treePanel.setBorders(false);
        this.treePanel.setDisplayProperty("name"); // $NON-NLS-0$
        final TreeStyle style = this.treePanel.getStyle();
        style.setLeafIcon(IconImage.get("mm-tree-instrument")); // $NON-NLS-0$
        style.setNodeCloseIcon(IconImage.get("mm-tree-folder-closed")); // $NON-NLS-0$
        style.setNodeOpenIcon(IconImage.get("mm-tree-folder-open")); // $NON-NLS-0$
        this.treePanel.addListener(Events.OnClick, new Listener<TreePanelEvent>(){
            public void handleEvent(TreePanelEvent event) {
                onClick(event);
            }
        });
        this.treePanel.getSelectionModel().select(this.firstNode, false);
    }

    private void onClick(TreePanelEvent event) {
        final TreePanel.TreeNode node = event.getNode();
        final TreeModel model = (TreeModel) node.getModel();
        if (model.get("data") != null) { // $NON-NLS-0$
            this.snippet.onSelect(getNodeText(model), model.get("data")); // $NON-NLS-0$
        }
    }

    private void initTreeStore() {
        this.store = new TreeStore<>();

        final Folder root = new Folder(ROOT_NAME);
        final JSONWrapper treedef = SessionData.INSTANCE.getGuiDef(this.snippet.getJsonKey());
        addChildren(root, treedef);

        this.store.add(root.getChildren(), true);
    }

    private void addChildren(Folder parent, JSONWrapper children) {
        for (int i = 0; i < children.size(); i++) {
            final JSONWrapper node = children.get(i);
            addChild(parent, node);
        }
    }

    private void addChild(Folder parent, JSONWrapper wrapper) {
        final String name = wrapper.get("name").stringValue(); // $NON-NLS-0$
        final String data = wrapper.get("data").stringValue(); // $NON-NLS-0$
        if (data != null) {
            final TreeLeaf linkNode = new TreeLeaf(name);
            linkNode.set("data", data); // $NON-NLS-0$
            parent.add(linkNode);
            if (this.firstNode == null) {
                this.firstNode = linkNode;
            }
        }
        else if (wrapper.get("children").isArray()){ // $NON-NLS-0$
            final Folder folder = new Folder(name);
            parent.add(folder);
            addChildren(folder, wrapper.get("children")); // $NON-NLS-0$
        }
    }
}
