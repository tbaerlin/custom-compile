/*
 * ItemChooserWidget.java
 *
 * Created on 10/10/14 4:55 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * @author Stefan Willenbrock
 */
public interface ItemChooserWidget extends IsWidget {
    int getSelectedRowsCount();

    String getColumnValue(int idx);

    ItemChooserWidget withStyleForSelectedItemsListBox(String styleName);

    void setLeftColHead(String text);

    void setRightColHead(String text);
}
