package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.dnd.ListViewDropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.TreeStoreModel;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author umaurer
 */
public class GroupConfigPanel implements InvestorItemView {
    private final ContentPanel panel;
    private final TextBox tbGroupName;
    private InvestorItem item;
    private final ListStore<InvestorItem> store;
    private final InvestorConfigController controller;

    public GroupConfigPanel(InvestorConfigController controller) {
        this.controller = controller;
        this.panel = new ContentPanel(new BorderLayout());
        this.panel.setHeaderVisible(false);
        this.panel.setStyleName("mm-investor-config"); // $NON-NLS-0$

        this.tbGroupName = new TextBox();
        this.tbGroupName.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                save();
            }
        });
        final Grid grid = new Grid(1, 2);
        final AbstractImagePrototype iconGroup = InvestorItem.Type.Gruppe.getIconLarge();
        grid.getCellFormatter().setWidth(0, 0, "32px"); // $NON-NLS-0$
        grid.getCellFormatter().setHeight(0, 0, "32px"); // $NON-NLS-0$
        grid.setHTML(0, 0, iconGroup.getHTML());
        grid.setWidget(0, 1, this.tbGroupName);

        final BorderLayoutData northData = new BorderLayoutData(Style.LayoutRegion.NORTH, 38);
        northData.setSplit(false);
        northData.setMargins(new Margins(0, 0, 0, 3));
        this.panel.add(grid, northData);

        this.store = new ListStore<>();
        final ListView<InvestorItem> listView = new ListView<InvestorItem>(this.store){
            final Image dummyImage = new Image();

            @Override
            protected InvestorItem prepareData(InvestorItem item) {
                item.set("imgHtml", item.getType().getIcon().getHTML()); // $NON-NLS-0$
                return item;
            }
        };
        listView.setDisplayProperty("name"); // $NON-NLS-0$
        listView.setHeight("200px"); // $NON-NLS-0$
        listView.setItemSelector("div.investor-wrap"); // $NON-NLS-0$
        listView.setTemplate("<tpl for=\".\"><div class=\"investor-wrap\">{imgHtml}{name}</div></tpl>"); // $NON-NLS-0$
        listView.getSelectionModel().setSelectionMode(Style.SelectionMode.SINGLE);
        this.panel.add(listView, new BorderLayoutData(Style.LayoutRegion.CENTER));

        initDropTarget(listView);
    }

    private void initDropTarget(ListView<InvestorItem> listView) {
        final ListViewDropTarget target = new ListViewDropTarget(listView);
        target.setGroup("pm-investor"); // $NON-NLS-0$
        target.addDNDListener(new DNDListener(){
            @Override
            public void dragDrop(DNDEvent e) {
                final List<TreeStoreModel> list = e.getData();
                addItems(list);
            }

            @Override
            public void dragEnter(DNDEvent e) {
                final List<TreeStoreModel> list = e.getData();
                final String dropError = getDropError(list);
                if (dropError != null) {
                    AbstractMainController.INSTANCE.showError(dropError);
                    e.setCancelled(true);
                }
            }
        });
    }

    private String getDropError(List<TreeStoreModel> list) {
        // check, if some of the dragged items are already in list
        final Set<InvestorItem> intersection = getIntersection(list);
        if (!intersection.isEmpty()) {
            if (intersection.size() == 1) {
                return I18n.I.customerEntryExistsInList(intersection.iterator().next().getName()); 
            }
            return I18n.I.nCustomerEntriesExistInList(intersection.size()); 
        }

        // check, if one of the dragged items is ancestor of the current group
        if (containsAncestor(list)) {
            return I18n.I.customerEntryCannotBeMovedToDependent(); 
        }
        return null;
    }

    private Set<InvestorItem> getIntersection(List<TreeStoreModel> list) {
        @SuppressWarnings({"unchecked"})
        final Set setChildren = new HashSet(this.item.getChildren());
        final Set<InvestorItem> setIntersection = new HashSet<>(setChildren.size());
        for (final TreeStoreModel model : list) {
            if (setChildren.contains(model.getModel())) {
                setIntersection.add((InvestorItem) model.getModel());
            }
        }
        return setIntersection;
    }

    private boolean containsAncestor(List<TreeStoreModel> list) {
        for (TreeStoreModel model : list) {
            final InvestorItem item = (InvestorItem) model.getModel();
            if (item.isAncestorOf(this.item)) {
                return true;
            }
        }
        return false;
    }


    private void addItems(List<TreeStoreModel> list) {
        for (TreeStoreModel model : list) {
            final InvestorItem treeItem = (InvestorItem) model.getModel();
            final InvestorItem listItem = treeItem.deepCopy();
            this.item.add(listItem);
        }
        save();
    }

    private void save() {
        this.item.setName(tbGroupName.getValue());
        this.controller.saveGroup(this.item);
    }

    public void setItem(InvestorItem item) {
        assert(item.getType() == InvestorItem.Type.Gruppe);
        this.item = item;
        final String name = item.getName();
        this.tbGroupName.setEnabled(item.isEditable());
        this.tbGroupName.setValue(name);
        final List<ModelData> listChildren = this.item.getChildren();
        this.store.removeAll();
        for (ModelData child : listChildren) {
            this.store.add((InvestorItem) child);
        }
        if (item.isEditable()) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    tbGroupName.setFocus(true);
                    tbGroupName.setSelectionRange(0, name.length());
                }
            });
        }
    }

    public Widget getWidget() {
        return this.panel;
    }
}
