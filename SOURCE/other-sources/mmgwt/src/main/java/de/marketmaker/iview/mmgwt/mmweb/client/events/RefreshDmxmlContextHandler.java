/*
 * RefreshDmxmlContextHandler.java
 *
 * Created on 01.08.2016 14:31
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Informs handlers that the user triggered a dm[xml] context refresh.
 * @author Markus Dick
 */
public interface RefreshDmxmlContextHandler extends EventHandler {
    void onRefreshDmxmlContext(RefreshDmxmlContextEvent event);
}
