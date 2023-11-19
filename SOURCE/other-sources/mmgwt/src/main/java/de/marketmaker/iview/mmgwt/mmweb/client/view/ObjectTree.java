package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SingleSelectionModel;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTree;
import de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.NavTreeItemIdentifier;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ModelChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ModelChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedHandler;

import java.util.HashSet;

/**
 * Created on 03.12.12 16:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class ObjectTree implements IsWidget, HasSelectionHandlers<NavItemSpec>,
        ModelChangeHandler<ObjectTreeModel>, VisibilityUpdatedHandler<NavItemSpec> {
    private FloatingPanel panel = new FloatingPanel(FloatingPanel.Orientation.VERTICAL);
    private NavTree<NavItemSpec> navTree;
    private ObjectTreeSelectionModel selectionModel = new ObjectTreeSelectionModel();
    private final HandlerManager handlerManager = new HandlerManager(this);
    private final HashSet<HandlerRegistration> handlerRegistrations = new HashSet<>();
    private final ObjectTreeModel objectTreeModel;

    public ObjectTree(final ObjectTreeModel model) {
        this.objectTreeModel = model;
        update();
        final NavItemSpec nisSelected = this.objectTreeModel.getSelected();
        if (nisSelected != null) {
            onModelSelection(nisSelected);
        }
    }

    void update() {
        for(HandlerRegistration hr : this.handlerRegistrations) {
            hr.removeHandler();
        }

        final ObjectTreeModel model = this.objectTreeModel;
        final NavTree<NavItemSpec> tree = new NavTree<>(model.getRoot(), 1);
        tree.addStyleName("as-object-navTree");
        this.navTree = tree;
        this.handlerRegistrations.add(model.addSelectionHandler(new SelectionHandler<NavItemSpec>() {
            @Override
            public void onSelection(final SelectionEvent<NavItemSpec> event) {
                onModelSelection(event.getSelectedItem());
            }
        }));
        this.handlerRegistrations.add(tree.addSelectionHandler(new SelectionHandler<NavItemSpec>() {
            @Override
            public void onSelection(SelectionEvent<NavItemSpec> event) {
                final NavItemSpec nis = event.getSelectedItem();
                if (nis.getHistoryToken() != null) {
                    fireSelectionEvent(nis);
                    model.setSelected(nis, false);
                }
            }
        }));
        this.handlerRegistrations.add(tree.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                panel.onResize();
            }
        }));
        this.panel.setWidget(tree);
    }

    private void onModelSelection(NavItemSpec navItemSpec) {
        final NavTree<NavItemSpec> selectedNavTree = NavTree.select(this.navTree, navItemSpec);
        if (selectedNavTree != null) {
            this.panel.scrollTo(selectedNavTree.getElement());
        }
        if (selectionModel.getSelectedObject() == navItemSpec) {
            return;
        }
        selectionModel.setSelected(navItemSpec, true);
    }

    @Override
    public void onModelChange(ModelChangeEvent<ObjectTreeModel> event) {
        update();
    }

    @Override
    public Widget asWidget() {
        return this.panel.asWidget();
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<NavItemSpec> handler) {
        return this.handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    private void fireSelectionEvent(NavItemSpec navItemSpec) {
        SelectionEvent.fire(this, navItemSpec);
    }

    private void setVisible(String itemId, boolean visible) {
        NavTree<NavItemSpec> item = this.navTree.findNavTreeById(itemId, NavTreeItemIdentifier.NIS_IDENTIFIER);
        if(item != null) {
            item.setVisible(visible);
        }
    }

    class ObjectTreeSelectionModel extends SingleSelectionModel<NavItemSpec> {
        @Override
        public void setSelected(NavItemSpec nis, boolean selected) {
            if (nis.isEnabled() && nis.getHistoryToken() != null) {
                super.setSelected(nis, selected);
            }
        }
    }

    @Override
    public void onVisibilityUpdated(VisibilityUpdatedEvent<NavItemSpec> event) {
        final NavItemSpec item = event.getTarget();
        if(item == null) return;
        setVisible(item.getId(), item.isVisible());
    }
}