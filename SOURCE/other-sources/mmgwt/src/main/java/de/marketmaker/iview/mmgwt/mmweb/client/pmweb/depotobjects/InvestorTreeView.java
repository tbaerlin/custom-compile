package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import java.util.List;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.TreePanelDragSource;
import com.extjs.gxt.ui.client.dnd.TreePanelDropTarget;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.Scheduler;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCardController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.CreateCardEvent;

/**
 * @author umaurer
 */
public class InvestorTreeView extends ContentPanel {
    private final InvestorTreeModel model;

    private final TreePanel<InvestorItem> tree;

    private Button buttonGroupDelete;

    private Button buttonInvestorDelete;

    private InvestorItem currentItem;

    private int maxGroupId = 0;

    public InvestorTreeView(final Style.SelectionMode selectionMode, boolean draggable) {
        this.model = InvestorTreeModel.getInstance();

        this.tree = new TreePanel<>(this.model.getStore());
        this.tree.setDisplayProperty("label"); // $NON-NLS-0$
        this.tree.getStyle().setLeafIcon(null);
        this.tree.getStyle().setNodeCloseIcon(null);
        this.tree.getStyle().setNodeOpenIcon(null);
/*
        this.tree.getStyle().setLeafIcon(IconImage.get("mm-tree-instrument")); // $NON-NLS-0$
        this.tree.getStyle().setNodeCloseIcon(IconImage.get("mm-tree-folder-closed")); // $NON-NLS-0$
        this.tree.getStyle().setNodeOpenIcon(IconImage.get("mm-tree-folder-open")); // $NON-NLS-0$
*/
        this.tree.getSelectionModel().setSelectionMode(selectionMode);
/*
        this.tree.setIconProvider(new ModelIconProvider<InvestorItem>() {
            public AbstractImagePrototype getIcon(InvestorItem item) {
                return item.getType().getIcon();
            }
        });
*/
        this.tree.addListener(Events.BeforeExpand, new Listener<TreePanelEvent<InvestorItem>>() {
            public void handleEvent(TreePanelEvent<InvestorItem> event) {
                Firebug.log("BeforeExpand - " + event.getItem().getName());
            }
        });
        this.tree.addListener(Events.Expand, new Listener<TreePanelEvent<InvestorItem>>() {
            public void handleEvent(TreePanelEvent<InvestorItem> event) {
                requestChildrenFromServer(event.getItem());
            }
        });

        addStyleName("pm-borderPanel"); // $NON-NLS$
        setLayout(new FitLayout());
        setHeaderVisible(false);
        add(this.tree);

        if (draggable) {
            initDragSource();
        }

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                initializeForm();
            }
        });
    }

    private void requestChildrenFromServer(InvestorItem item) {
        final InvestorItem.HasChildren hasChildren = item.getHasChildren();
        if (hasChildren == InvestorItem.HasChildren.UNKNOWN) {
            Firebug.log("requestChildrenFromServer(" + item.getName() + ") -> UNKNOWN -> set to YES"); // $NON-NLS$
            item.setHasChildren(InvestorItem.HasChildren.YES);
        }
        else {
            Firebug.log("requestChildrenFromServer(" + item.getName() + ") -> " + hasChildren); // $NON-NLS$
        }
    }

    private native void initializeForm() /*-{
        $wnd.mmInvestorTreeItemClick = @de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorTreeView::mmInvestorTreeItemClick(Ljava/lang/String;);
    }-*/;

    private static void mmInvestorTreeItemClick(String investorKey) {
        final InvestorItem investorItem = InvestorItem.getByKey(investorKey);
        PmReportCardController.getInstance(); // make sure, PmReportCardController is active to receive the following event
        CreateCardEvent.fire(investorItem);
    }

    public void add(InvestorItem item) {
        this.model.add(item);
        this.tree.setExpanded(item, true);
    }


    public void addEditToolbar() {
        final ToolBar toolbar = new ToolBar();
        toolbar.addStyleName("mm-viewWidget"); // $NON-NLS-0$

        final Button buttonGroupAdd = new Button();
        buttonGroupAdd.setIcon(IconImage.get("pm-investor-group-add")); // $NON-NLS-0$
        buttonGroupAdd.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                addGroup();
            }
        });
        toolbar.add(buttonGroupAdd);

        buttonGroupDelete = new Button();
        this.buttonGroupDelete.setEnabled(false);
        buttonGroupDelete.setIcon(IconImage.get("pm-investor-group-delete")); // $NON-NLS-0$
        buttonGroupDelete.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                deleteSelectedItem();
            }
        });
        toolbar.add(buttonGroupDelete);

        buttonInvestorDelete = new Button();
        this.buttonInvestorDelete.setEnabled(false);

        buttonInvestorDelete.setIcon(IconImage.get("pm-investor-delete")); // $NON-NLS-0$
        buttonInvestorDelete.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                deleteSelectedItem();
            }
        });
        toolbar.add(buttonInvestorDelete);

        this.tree.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<InvestorItem>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<InvestorItem> se) {
                enableButtons(tree.getSelectionModel().getSelectedItem());
            }
        });

        setTopComponent(toolbar);
    }

    private void enableButtons(InvestorItem item) {
        this.buttonGroupDelete.setEnabled(isEditableGroup(item));
        this.buttonInvestorDelete.setEnabled(item != null && isEditableGroup((InvestorItem) item.getParent()));
    }

    private boolean isEditableGroup(InvestorItem item) {
        return item != null && item.isType(InvestorItem.Type.Gruppe) && item.isEditable();
    }

    private void addGroup() {
        final InvestorItem item = new InvestorItem("g-" + ++this.maxGroupId, InvestorItem.Type.Gruppe, I18n.I.newGroup(), InvestorItem.ZONE_PMWEB_LOCAL, true);  // $NON-NLS-0$
        this.model.add(item);
        this.tree.getSelectionModel().setSelection(item.asList());
    }

    private void deleteSelectedItem() {
        final InvestorItem item = this.tree.getSelectionModel().getSelectedItem();
        this.model.delete(item);
    }

    private void initDragSource() {
        final TreePanelDragSource source = new TreePanelDragSource(this.tree) {
            @Override
            public String getStatusText() {
                final List<ModelData> listItems = tree.getSelectionModel().getSelectedItems();
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < listItems.size() && i < 3; i++) {
                    ModelData item = listItems.get(i);
                    sb.append(i == 0 ? "" : "<br/>") // $NON-NLS$
                            .append(item.<String>get(tree.getDisplayProperty()));
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
        source.setTreeSource(DND.TreeSource.BOTH);
        source.setGroup("pm-investor"); // $NON-NLS-0$
        source.addDNDListener(new DNDListener() {
            @Override
            public void dragStart(DNDEvent e) {
                final List list = (List) e.getData();
                for (Object model : list) {
                    final InvestorItem item = (InvestorItem) ((TreeStoreModel) model).getModel();
                    final String dragError = getDragError(item);
                    if (dragError != null) {
                        e.setCancelled(true);
                        AbstractMainController.INSTANCE.showError(dragError);
                        return;
                    }
                    if (!isDraggable(item)) {
                        e.setCancelled(true);
                        AbstractMainController.INSTANCE.showError(I18n.I.somethingCannotBeMoved(item.getName()));
                        return;
                    }
                }
            }

            @Override
            public void dragDrop(DNDEvent e) {
                final List list = e.getData();
                for (Object model : list) {
                    final InvestorItem item = (InvestorItem) ((TreeStoreModel) model).getModel();
                    if (item.isType(InvestorItem.Type.Gruppe)) {
                        InvestorTreeView.this.model.delete(item);
                    }
                }
            }
        });
    }

    private void initDropTarget() {
        final TreePanelDropTarget target = new TreePanelDropTarget(this.tree);
        target.setAllowSelfAsSource(true);
        target.setFeedback(DND.Feedback.BOTH);
        target.setGroup("pm-investor"); // $NON-NLS-0$
    }

    private String getDragError(InvestorItem item) {
        if (item == null) {
            return "item is null"; // $NON-NLS-0$
        }
        if (item.isType(InvestorItem.Type.Gruppe) && !item.isEditable()) {
            return I18n.I.somethingCannotBeMoved(item.getName());
        }

        return null;
    }

    private boolean isDraggable(InvestorItem item) {
        if (item == null) {
            Firebug.log("item is null"); // $NON-NLS-0$
            return false;
        }
        final InvestorItem parent = (InvestorItem) item.getParent();
        final boolean groupAllInvestors = item.isType(InvestorItem.Type.Gruppe) && !item.isEditable();
        final boolean currentItemIsGroup = this.currentItem != null && this.currentItem.isType(InvestorItem.Type.Gruppe);
        final boolean currentItemIsGroupAllInvestors = currentItemIsGroup && !this.currentItem.isEditable();
        final boolean topLevelOrUnderGroup = parent == null || parent.isType(InvestorItem.Type.Gruppe);
        final boolean isItemCurrentItem = this.currentItem != null && item.getId().equals(this.currentItem.getId());

        return topLevelOrUnderGroup && !groupAllInvestors && !currentItemIsGroupAllInvestors
                && !item.isAncestorOf(this.currentItem) && currentItemIsGroup && !isItemCurrentItem;
    }

    public void reload(InvestorItem item) {
        this.model.reload(item);
        this.tree.setExpanded(item, true);
    }
}