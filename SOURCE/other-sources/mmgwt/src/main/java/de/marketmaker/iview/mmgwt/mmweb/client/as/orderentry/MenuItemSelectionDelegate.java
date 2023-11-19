/*
 * MenuItemSelectionDelegate.java
 *
 * Created on 15.08.13 17:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;

/**
* @author Markus Dick
*/
public class MenuItemSelectionDelegate<T> extends Composite implements HasSelectionHandlers<T> {
    public MenuItemSelectionDelegate(HasSelectionHandlers<MenuItem> widgetWithSelectionHandlers) {
        initWidget((Widget)widgetWithSelectionHandlers);
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<T> securityDataSelectionHandler) {
        return addHandler(securityDataSelectionHandler, SelectionEvent.getType());
    }
}
