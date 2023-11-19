/*
 * ConfigChangeEvent.java
 *
 * Created on 04.12.2009 15:02:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Informs handlers about changes in the AppConfig.
 * @author oflege
 */
public class ConfigChangedEvent extends GwtEvent<ConfigChangedHandler> {
    private static Type<ConfigChangedHandler> TYPE;

    public static Type<ConfigChangedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<ConfigChangedHandler>();
        }
        return TYPE;
    }

    private final Object newValue;

    private final Object oldValue;

    private final String property;

    public ConfigChangedEvent(String property, Object oldValue, Object newValue) {
        this.property = property;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Object getNewValue() {
        return this.newValue;
    }

    public Object getOldValue() {
        return this.oldValue;
    }

    public String getProperty() {
        return this.property;
    }

    public Type<ConfigChangedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(ConfigChangedHandler configChangedHandler) {
        configChangedHandler.onConfigChange(this);
    }
}
