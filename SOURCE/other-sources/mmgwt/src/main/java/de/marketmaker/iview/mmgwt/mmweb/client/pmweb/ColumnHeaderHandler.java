/*
 * ColumnHeaderHandler.java
 *
 * Created on 18.03.2015 09:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author mdick
 */
public interface ColumnHeaderHandler extends EventHandler {
    void onColumnHeader(ColumnHeaderEvent event);
}
