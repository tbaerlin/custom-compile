/*
 * ViewSelectionModelImpl.java
 *
 * Created on 26.03.2008 11:52:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexedViewSelectionModelImpl implements IndexedViewSelectionModel {
    private ViewSpec[] viewSpec;

    private boolean[] selectable;

    private boolean[] visible;

    private int selected;

    private boolean unselected = false;

    private String viewGroup;

    private final Callback callback;

    public IndexedViewSelectionModelImpl(Callback callback, ViewSpec[] viewSpec,
                                         int selected, String viewGroup) {
        this.callback = callback;
        update(viewSpec, selected, viewGroup);
    }

    public void update(ViewSpec[] viewSpec, int selected, String viewGroup) {
        this.viewSpec = new ViewSpec[viewSpec.length];
        this.selectable = new boolean[viewSpec.length];
        this.visible = new boolean[viewSpec.length];
        for (int i = 0; i < viewSpec.length; i++) {
            this.viewSpec[i] = viewSpec[i];
            this.selectable[i] = true;
            this.visible[i] = true;
        }
        this.selected = selected;
        this.viewGroup = viewGroup;
        this.unselected = false;
    }

    public int getViewCount() {
        return this.viewSpec.length;
    }

    public ViewSpec getViewSpec(int i) {
        return this.viewSpec[i];
    }

    public boolean isSelectable(int i) {
        return this.selectable[i];
    }

    public void setSelectable(int i, boolean value) {
        this.selectable[i] = value;
    }

    public boolean isVisible(int i) {
        return this.visible[i];
    }

    public void setVisible(int i, boolean value) {
        this.visible[i] = value;
    }

    public void setUnselected(boolean unselected) {
        this.unselected = unselected;
    }

    public boolean isUnselected() {
        return this.unselected;
    }

    public void setViewSpec(int i, ViewSpec viewSpec) {
        this.viewSpec[i] = viewSpec;
    }

    public void selectView(int i) {
        selectView(i, true);
    }

    public void selectView(int i, boolean callCallback) {
        if (this.selected == i && !this.unselected) {
            return;
        }
        this.unselected = false;
        this.selected = i;
        if (callCallback) {
            this.callback.onViewChanged();
        }
    }

    public int getSelectedView() {
        return this.selected;
    }

    public String getViewGroup() {
        return viewGroup;
    }
}
