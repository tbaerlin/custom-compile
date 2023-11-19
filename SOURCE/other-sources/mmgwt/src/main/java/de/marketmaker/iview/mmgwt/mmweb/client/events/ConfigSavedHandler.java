/*
 * ConfigSavedHandler.java
 *
 * Created on 07.03.2016
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author mdick
 */
public interface ConfigSavedHandler extends EventHandler {
    void onConfigSaved(ConfigSavedEvent event);
}
