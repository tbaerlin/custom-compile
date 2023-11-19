/*
* ItemId.java
*
* Created on 12.08.2008 13:49:05
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * @author Michael Lösch
 */
public class ItemId extends MenuItem {
    private final String id;

    public ItemId(String text, String id) {
        super(text);
        this.id = id;
    }

    public ItemId(String text, String id, SelectionListener<MenuEvent> listener) {
        super(text, listener);
        this.id = id;
    }

    public ItemId(String text, String id, SelectionListener<MenuEvent> listener, String icon) {
        this(text, id, listener);
        IconImage.setIconStyle(this, icon);
    }

    public String getId() {
        return id;
    }
}
