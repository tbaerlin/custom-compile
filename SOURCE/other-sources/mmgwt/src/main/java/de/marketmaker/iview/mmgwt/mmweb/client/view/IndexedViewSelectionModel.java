/*
 * ViewSelectionModel.java
 *
 * Created on 26.03.2008 11:51:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.view;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IndexedViewSelectionModel {
    interface Callback {
        void onViewChanged();
    }

    int getViewCount();

    ViewSpec getViewSpec(int i);

    boolean isSelectable(int i);

    void setSelectable(int i, boolean value);

    boolean isVisible(int i);

    void selectView(int i);

    void selectView(int i, boolean callCallback);

    void setUnselected(boolean unselected);

    boolean isUnselected();

    int getSelectedView();

    String getViewGroup();
}
