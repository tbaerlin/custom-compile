package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created on 13.11.12 11:03
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface HistoryThreadHandler extends EventHandler {
    void onHistoryThreadChange(HistoryThreadEvent event);
}
