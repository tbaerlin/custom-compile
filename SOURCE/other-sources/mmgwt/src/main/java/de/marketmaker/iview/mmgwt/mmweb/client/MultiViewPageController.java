/*
 * MultiViewPageController.java
 *
 * Created on 27.03.2008 14:17:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModelImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class MultiViewPageController extends AbstractPageController
        implements IndexedViewSelectionModel.Callback {

    private IndexedViewSelectionModelImpl viewSelectionModel;

    protected MultiViewPageController() {
        this(AbstractMainController.INSTANCE.getView());
    }

    protected MultiViewPageController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    protected MultiViewPageController(ContentContainer contentContainer, DmxmlContext context) {
        super(contentContainer, context);
    }

    protected void initViewSelectionModel(String[] viewNames, String viewGroup) {
        final ViewSpec[] viewSpec = new ViewSpec[viewNames.length];
        for (int i = 0; i < viewNames.length; i++) {
            viewSpec[i] = new ViewSpec(viewNames[i]);
        }
        initViewSelectionModel(viewSpec, 0, viewGroup);
    }

    protected void initViewSelectionModel(ViewSpec[] viewSpec, int selected, String viewGroup) {
        this.viewSelectionModel = new IndexedViewSelectionModelImpl(this, viewSpec, selected, viewGroup);
    }

    /**
     * Called every time the user changed the active view or some other component than
     * this object invoked
     * {@link de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel#selectView(int)}.
     * The selected view can be obtained from the viewSelectionModel.
     */
    public void onViewChanged() {
        // empty, subclasses should override
    }

    /**
     * to be called by subclasses to change the active view programmatically; this object's
     * {@link #onViewChanged()} method will not be triggered. Will not change the
     * actual view representation.
     *
     * @param i the view that should be selected
     */
    protected final void changeSelectedView(int i) {
        this.viewSelectionModel.selectView(i, false);
    }

    protected void setUnselected() {
        this.viewSelectionModel.setUnselected(true);
    }

    public IndexedViewSelectionModel getViewSelectionModel() {
        return this.viewSelectionModel;
    }

    protected void setViewSelectable(int i, boolean value) {
        this.viewSelectionModel.setSelectable(i, value);
    }

    protected void setViewVisible(int i, boolean value) {
        this.viewSelectionModel.setVisible(i, value);
    }

    protected boolean isViewVisible(int i) {
        return this.viewSelectionModel.isVisible(i);
    }

    protected void setViewName(int i, String viewName) {
        this.viewSelectionModel.setViewSpec(i, new ViewSpec(viewName));
    }
}
