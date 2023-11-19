/*
 * ConfigSavedEvent.java
 *
 * Created on 07.03.2016
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that informs handlers about successful or failed app config save operation.
 * @author mdick
 */
public class ConfigSavedEvent extends GwtEvent<ConfigSavedHandler> {
    private static Type<ConfigSavedHandler> TYPE;

    private final boolean successfullySaved;

    public static Type<ConfigSavedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public static void fire(boolean successfullyStored) {
        if(EventBusRegistry.isSet()) {
            EventBusRegistry.get().fireEvent(new ConfigSavedEvent(successfullyStored));
        }
    }

    public ConfigSavedEvent(boolean successfullySaved) {
        this.successfullySaved = successfullySaved;
    }

    public boolean isSuccessfullySaved() {
        return successfullySaved;
    }

    public Type<ConfigSavedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ConfigSavedHandler handler) {
        handler.onConfigSaved(this);
    }
}
