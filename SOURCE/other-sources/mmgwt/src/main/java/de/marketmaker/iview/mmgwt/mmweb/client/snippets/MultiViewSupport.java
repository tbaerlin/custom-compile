/*
 * MultiViewSupport.java
 *
 * Created on 03.04.2008 18:19:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModelImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MultiViewSupport implements IndexedViewSelectionModel.Callback, HasValueChangeHandlers<Integer> {
    private static int viewGroupCounter = 0;

    protected static String uniqueViewGroupId() {
        return "mvs-" + viewGroupCounter++; // $NON-NLS-0$
    }

    private String viewGroup;

    private ViewSpec[] viewSpec;

    private IndexedViewSelectionModelImpl viewSelectionModel;

    private HandlerManager manager;

    public MultiViewSupport(String[] viewNames, String viewGroup) {
        this(getViewSpec(viewNames), viewGroup);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
        if (this.manager == null) {
            this.manager = new HandlerManager(this);
        }
        return this.manager.addHandler(ValueChangeEvent.getType(), handler);
    }

    public void fireEvent(GwtEvent<?> gwtEvent) {
        if (this.manager != null) {
            this.manager.fireEvent(gwtEvent);
        }
    }

    private static ViewSpec[] getViewSpec(String[] viewNames) {
        final ViewSpec[] viewSpec = new ViewSpec[viewNames.length];
        for (int i = 0; i < viewNames.length; i++) {
            viewSpec[i] = new ViewSpec(viewNames[i]);
        }
        return viewSpec;
    }

    public MultiViewSupport(ViewSpec[] viewSpec, String viewGroup) {
        this.viewSpec = viewSpec;
        this.viewGroup = viewGroup != null ? viewGroup : uniqueViewGroupId();
        initViewSelectionModel();
    }

    public int getSelectedView() {
        return this.viewSelectionModel.getSelectedView();
    }

    public ViewSpec getSelectedViewSpec() {
        return getViewSpec(this.viewSelectionModel.getSelectedView());
    }

    public String getSelectedViewName() {
        return getSelectedViewSpec().getName();
    }

    public int getViewCount() {
        return this.viewSelectionModel.getViewCount();
    }

    public IndexedViewSelectionModel getViewSelectionModel() {
        return this.viewSelectionModel;
    }

    protected ViewSpec getViewSpec(int i) {
        return this.viewSpec[i];
    }

    private void initViewSelectionModel() {
        this.viewSelectionModel = new IndexedViewSelectionModelImpl(this, this.viewSpec, 0, this.viewGroup);
    }

    /**
     * Called every time the user changed the active view or some other component than
     * this object invoked
     * {@link de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel#selectView(int)}.
     * The selected view can be obtained from the viewSelectionModel.
     */
    public void onViewChanged() {
        ValueChangeEvent.fire(this, this.viewSelectionModel.getSelectedView());
    }
}
