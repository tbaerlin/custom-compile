package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Created on 26.05.2010 09:14:10
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface DataAvailableHandler extends EventHandler {
    void onDataAvailable(DataAvailableEvent event);
}
