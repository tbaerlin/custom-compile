/*
 * ShutdownHandler.java
 *
 * Created on 28.04.2014 15:08
 *
 * Copyright (c)vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Markus Dick
 */
public interface ShutdownHandler extends EventHandler {
    void onShutdown(ShutdownEvent event);
}
