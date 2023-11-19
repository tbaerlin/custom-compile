/*
 * ObjectTreeModel.java
 *
 * Created on 14.12.2012
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HasModelChangeHandlers;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HasVisibilityUpdatedHandlers;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ModelChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ModelChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedHandler;

/**
 * @author Ulrich Maurer
 */
public class ObjectTreeModel implements NavItemSelectionModel, HasModelChangeHandlers<ObjectTreeModel>, HasVisibilityUpdatedHandlers<NavItemSpec> {
    private final NavItemSpec root;
    private HandlerManager handlerManager = new HandlerManager(this);
    private NavItemSpec selected = null;

    public ObjectTreeModel(NavItemSpec root) {
        this.root = root;
    }

    public void addChild(NavItemSpec parent, NavItemSpec newChild) {
        parent.addChild(newChild);
    }

    public NavItemSpec getRoot() {
        return this.root;
    }

    public NavItemSpec getItem(String id) {
        return root.findChildById(id);
    }

    @Override
    public void setVisibility(NavItemSpec navItemSpec, boolean visible) {
        if (navItemSpec == null) {
            return;
        }
        if(navItemSpec.isVisible() != visible) {
            Firebug.debug("<ObjectTreeModel.setVisibility> navItemSpec={" + navItemSpec.getId() + ", " + navItemSpec.getName() + "} visible=" + visible);
            navItemSpec.setVisible(visible);
            fireVisibilityUpdated(navItemSpec);
        }
    }

    @Override
    public boolean isVisible(NavItemSpec navItemSpec) {
        return navItemSpec.isVisible();
    }

    @Override
    public void setSelectable(NavItemSpec navItemSpec, boolean selectable) {
        if (navItemSpec == null) {
            return;
        }
        Firebug.debug("disable tree element");
    }

    @Override
    public boolean isSelectable(NavItemSpec navItemSpec) {
        return true;
    }

    @Override
    public NavItemSpec getSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(NavItemSpec navItemSpec, boolean fireEvents) {
        setSelected(navItemSpec, fireEvents, false);
    }

    @Override
    public void setSelected(NavItemSpec navItemSpec, boolean fireEvents, boolean fireEvenIfAlreadySelected) {
        fireEvents = fireEvents && (this.selected != navItemSpec || fireEvenIfAlreadySelected);
        this.selected = navItemSpec;
        if (fireEvents) {
            fireSelectionEvent();
        }
    }

    @Override
    public void doUpdate() {
        fireModelChange();
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<NavItemSpec> handler) {
        return this.handlerManager.addHandler(SelectionEvent.getType(), handler);
    }

    private void fireSelectionEvent() {
        SelectionEvent.fire(this, getSelected());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    @Override
    public HandlerRegistration addModelChangeHandler(ModelChangeHandler<ObjectTreeModel> handler) {
        return this.handlerManager.addHandler(ModelChangeEvent.getType(), handler);
    }

    @Override
    public HandlerRegistration addVisibilityUpdatedHandler(VisibilityUpdatedHandler<NavItemSpec> handler) {
        return this.handlerManager.addHandler(VisibilityUpdatedEvent.getType(), handler);
    }

    public void fireModelChange() {
        ModelChangeEvent.fire(this);
    }

    public void fireVisibilityUpdated(NavItemSpec spec) {
        VisibilityUpdatedEvent.fire(this, spec, spec.isVisible());
    }
}