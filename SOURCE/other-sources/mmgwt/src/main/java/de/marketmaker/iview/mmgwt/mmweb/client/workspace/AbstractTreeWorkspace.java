/*
 * AbstractTreeWorkspace.java
 *
 * Created on 30.04.2008 16:47:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Folder;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.terminalpages.PageType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractTreeWorkspace<V extends TreeContentItem> extends AbstractWorkspaceItem {

    private boolean afterInit = false;

    private boolean guiPopulated = false;

    private TreeStore<TreeModel> store;

    private final TreePanel<TreeModel> tree;

    private int size = 0;

    protected final ToolBar toolbar = new ToolBar();

    protected AbstractTreeWorkspace(final String heading) {
        this(heading, Style.SelectionMode.SINGLE);
    }

    protected AbstractTreeWorkspace(final String heading, final Style.SelectionMode selectionMode) {
        super(heading);

        addListener(Events.Expand, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent componentEvent) {
                doOnExpand();
            }
        });
        setLayout(new FitLayout());

        setTopComponent(this.toolbar);

        this.store = new TreeStore<>();
        this.store.setStoreSorter(new StoreSorter<TreeModel>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Folder) {
                    if (((Folder) o1).getName().equals(PageType.CUSTOM.toString())) {
                        return -1;
                    }
                    if (((Folder) o2).getName().equals(PageType.CUSTOM.toString())) {
                        return 1;
                    }
                    return ((Folder) o1).getName().compareTo(((Folder) o2).getName());
                }
                return ((TreeContentItem) o1).getOrder() - ((TreeContentItem) o2).getOrder();
            }
        }));

        this.tree = new TreePanel<>(this.store);
        this.tree.setBorders(false);
        this.tree.setDisplayProperty("name"); // $NON-NLS-0$
        this.tree.getStyle().setLeafIcon(getLeafIcon());
        this.tree.getStyle().setNodeCloseIcon(IconImage.get("mm-tree-folder-closed")); // $NON-NLS-0$
        this.tree.getStyle().setNodeOpenIcon(IconImage.get("mm-tree-folder-open")); // $NON-NLS-0$
        this.tree.getSelectionModel().setSelectionMode(selectionMode);
        this.tree.setIconProvider(new ModelIconProvider<TreeModel>() {
            @Override
            public AbstractImagePrototype getIcon(TreeModel model) {
                return getTreeIcon(model);
            }
        });

        this.tree.sinkEvents(Events.OnClick.getEventCode());
        this.tree.addListener(Events.OnClick, new Listener<TreePanelEvent>() {
            public void handleEvent(TreePanelEvent e) {
                onClick(e);
            }
        });

        add(this.tree);

        if (getDdGroup() != null) {
            initDragDrop();
        }
    }

    protected boolean isExpanded(TreeModel model) {
        return this.tree.isExpanded(model);
    }

    protected abstract void restoreState();

    protected AbstractImagePrototype getLeafIcon() {
        return IconImage.get("mm-tree-instrument"); // $NON-NLS$
    }

    protected AbstractImagePrototype getTreeIcon(TreeModel model) {
        return null;
    }

    private void doOnExpand() {
        // this solves various problems in gxt-2.1.0 with toolbars in collapsible panels:
        this.tree.syncSize();
        if (!this.guiPopulated) {
            populateToolbar();

            restoreState();
            onAfterInit();

            this.guiPopulated = true;
        }
        else {
            this.toolbar.layout();
        }
    }

    private void populateToolbar() {
        final Button removeButton = new Button(null, new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                removeSelected();
            }
        });
        IconImage.setIconStyle(removeButton, "mm-finder-btn-delete"); // $NON-NLS-0$
        removeButton.setToolTip(I18n.I.deleteSelection());
        this.toolbar.add(removeButton);

        this.toolbar.add(new SeparatorToolItem());

        final Button editButton = new Button(null, new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                renameSelected();
            }
        });
        IconImage.setIconStyle(editButton, "space-edit-icon"); // $NON-NLS-0$
        editButton.setToolTip(I18n.I.renameSelection());
        this.toolbar.add(editButton);

        this.toolbar.add(new SeparatorToolItem());

        final Button upButton = new Button(null, new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                moveSelected(true);
            }
        });
        IconImage.setIconStyle(upButton, "mm-list-move-up"); // $NON-NLS-0$
        upButton.setToolTip(I18n.I.moveSelectionUp());
        this.toolbar.add(upButton);

        this.toolbar.add(new SeparatorToolItem());

        final Button downButton = new Button(null, new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                moveSelected(false);
            }
        });
        IconImage.setIconStyle(downButton, "mm-list-move-down"); // $NON-NLS-0$
        downButton.setToolTip(I18n.I.moveSelectionDown());
        this.toolbar.add(downButton);
        this.toolbar.layout();
    }

    private void initDragDrop() {
        final TreePanelDragSource source = new TreePanelDragSource(this.tree) {
            @Override
            public String getStatusText() {
                final List<ModelData> listItems = tree.getSelectionModel().getSelectedItems();
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < listItems.size() && i < 3; i++) {
                    sb.append(i == 0 ? "" : "<br/>").append(listItems.get(i).get(tree.getDisplayProperty()).toString()); // $NON-NLS$
                }
                if (listItems.size() > 3) {
                    sb.append("<br/>..."); // $NON-NLS-0$
                }
                return sb.toString();
            }

            @Override
            protected void onDragDrop(DNDEvent dndEvent) {
                // empty, prevent node from being removed from tree
            }
        };
        source.setTreeSource(DND.TreeSource.LEAF);
        source.setGroup(getDdGroup());
        source.addDNDListener(new DNDListener() {
            @Override
            public void dragStart(DNDEvent e) {
                AbstractTreeWorkspace.this.onStartDrag(e);
            }
        });
    }


    protected void onClick(TreePanelEvent event) {
        final ModelData data = event.getItem();
        if (data instanceof Folder) {
            TreePanel.TreeNode node = event.getNode();
            node.setExpanded(!node.isExpanded());
        }
        if (data instanceof TreeContentItem) {
            PlaceUtil.goTo(((TreeContentItem) data).getHistoryToken());
        }
    }

    protected V getSelectedLeaf() {
        final TreeModel data = this.tree.getSelectionModel().getSelectedItem();
        if (data == null || !(data instanceof TreeContentItem)) {
            return null;
        }
        return (V) data;
    }

    protected List<V> getSelectedLeafs() {
        final List list = this.tree.getSelectionModel().getSelectedItems();
        final List<V> result = new ArrayList<>(list.size());
        for (Object elt : list) {
            if (elt instanceof TreeContentItem) {
                result.add((V) elt);
            }
        }
        return result;
    }

    protected void moveSelected(boolean up) {
        final TreeContentItem node = getSelectedLeaf();
        if (node == null) {
            return;
        }

        final TreeContentItem sibling = (TreeContentItem)
                (up ? this.store.getPreviousSibling(node) : this.store.getNextSibling(node));
        if (sibling == null) {
            return;
        }

        swapOrder(node, sibling);

        insertInOrder(node.getParent(), sibling);

        this.store.update(node.getParent());

        SessionData.INSTANCE.getUser().getAppConfig().firePropertyChange("workspace.resort", null, this); // $NON-NLS-0$
    }

    private void swapOrder(TreeContentItem content, TreeContentItem sibling) {
        final int tmp = sibling.getOrder();
        sibling.setOrder(content.getOrder());
        content.setOrder(tmp);
    }

    private void renameSelected() {
        final TreeContentItem node = getSelectedLeaf();
        if (node == null) {
            return;
        }
        renameNode(node);
    }

    private void removeSelected() {
        final TreeContentItem node = getSelectedLeaf();
        if (node == null) {
            return;
        }
        removeNode(node);
    }

    protected void onAfterInit() {
        this.afterInit = true;
        // needed here to fix inconsistent data created by a bug in an earlier version:
        ensureStandardOrder();
    }

    protected String getDdGroup() {
        return null;
    }

    protected void onStartDrag(DNDEvent e) {
        // empty
    }

    protected TreeModel addLeaf(V content) {
        final Folder folder = getFolder(content);
        final TreeModel result = insert(folder, content);

        if (isCollapsed()) {
            setExpanded(true);
        }
        if (this.afterInit) {
            doLayout();
            if (result != null) {
                SessionData.INSTANCE.getUser().getAppConfig().firePropertyChange("workspace.insert", null, this); // $NON-NLS-0$
            }
        }
        return result;
    }

    protected Folder createFolder(String name, int i) {
        final Folder result = new Folder(name);
        if (i < 0) {
            this.store.add(result, false);
        }
        else {
            this.store.insert(result, i, false);
        }
        return result;
    }

    protected ArrayList<V> getContent() {
        final ArrayList<V> result = new ArrayList<>();
        final List<TreeModel> rootItems = this.store.getRootItems();
        for (TreeModel item : rootItems) {
            add(item, result);
        }
        sortContentForConfig(result);
        return result;
    }

    void sortContentForConfig(List<V> items) {
        Collections.sort(items, new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return o1.getOrder() - o2.getOrder();
            }
        });
    }

    private void add(TreeModel item, ArrayList<V> list) {
        if (item.isLeaf()) {
            list.add((V) item);
        }
        else {
            for (ModelData data : item.getChildren()) {
                add((TreeModel) data, list);
            }
        }
    }

    private Folder getFolder(V p) {
        final String name = p.getFolderName();

        final List<TreeModel> rootItems = this.store.getRootItems();

        if (rootItems == null) {
            return createFolder(name, -1);
        }

        int idx = -1;
        for (int i = 0; i < rootItems.size(); i++) {
            final Folder folder = (Folder) rootItems.get(i);
            final int cmp = name.compareTo(folder.getName());
            if (cmp == 0) {
                return folder;
            }
            if (cmp < 0) {
                idx = i;
            }
        }
        return createFolder(name, idx);
    }

    protected void removeNode(TreeModel node) {
        final TreeModel parent = node.getParent();
        remove(parent, node);
        if (parent.getChildCount() == 0) {
            this.store.remove(parent);
        }
        else {
            ensureStandardOrder(parent);
        }
        SessionData.INSTANCE.getUser().getAppConfig().firePropertyChange("workspace.remove", null, this); // $NON-NLS-0$
        this.size--;
    }

    protected void renameNode(final TreeContentItem node) {
        MessageBox.prompt(I18n.I.renameElement(), " " + node.getDisplayName(), new Listener<MessageBoxEvent>() {  // $NON-NLS-0$

            public void handleEvent(MessageBoxEvent messageBoxEvent) {
                final Button button = messageBoxEvent.getButtonClicked();
                final String value = messageBoxEvent.getValue();
                if ("ok".equalsIgnoreCase(button.getText())) { // $NON-NLS-0$
                    onRenameNode(node, value);
                }
            }
        }
        );
    }

    private void onRenameNode(final TreeContentItem node, String name) {
        node.setNameInWorkspace(name);
        SessionData.INSTANCE.getUser().getAppConfig().firePropertyChange("workspace.rename.node", null, this); // $NON-NLS-0$
        this.store.update(node);
    }

    private boolean exists(List<ModelData> children, V v) {
        if (children == null) {
            return false;
        }
        for (ModelData child : children) {
            if (child.equals(v)) {
                return true;
            }
        }
        return false;
    }

    private TreeModel insert(Folder folder, V v) {
        final List<ModelData> children = folder.getChildren();
        if (children != null) {
            if (exists(children, v)) {
                AbstractMainController.INSTANCE.showError(I18n.I.elementAlreadyExists());
                return null;
            }
            final int maxElementsPerFolder = getMaxElementsPerFolder();
            if (this.size >= maxElementsPerFolder) {
                AbstractMainController.INSTANCE.showError(I18n.I.maximumElementNumberReached(maxElementsPerFolder));
                return null;
            }
        }

        this.size++;

        if (isNew(v)) {
            v.setOrder(children != null ? children.size() + 1 : 1);
        }

        add(folder, v);

        return v;
    }

    protected int getMaxElementsPerFolder() {
        return 50;
    }

    private boolean isNew(V v) {
        return v.getOrder() == 0;
    }

    private void ensureStandardOrder() {
        final List<TreeModel> folders = this.store.getRootItems();
        if (folders == null) {
            return;
        }
        for (TreeModel folder : folders) {
            ensureStandardOrder(folder);
        }
    }

    /**
     * Assign order values from 1..n to the n Nodes in folder. Used to prevent child nodes from
     * having the same order which would cause sorting problems.
     *
     * @param folder with nodes to be ordered
     */
    private void ensureStandardOrder(TreeModel folder) {
        final List<ModelData> nodes = folder.getChildren();
        if (nodes == null) {
            return;
        }
        int i = 0;
        for (ModelData node : nodes) {
            ((TreeContentItem) node).setOrder(++i);
        }
    }

    private void insertInOrder(TreeModel folder, TreeContentItem node) {
        folder.remove(node);
        folder.insert(node, node.getOrder() - 1);
        this.store.remove(node);
        this.store.insert(folder, node, node.getOrder() - 1, false);
    }

    private void remove(TreeModel folder, TreeModel node) {
        folder.remove(node);
        this.store.remove(node);
    }

    private void add(TreeModel folder, TreeModel node) {
        folder.add(node);
        this.store.add(folder, node, false);
    }
}
