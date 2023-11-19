/*
 * ModelChangeHandler.java
 *
 * Created on 13.06.13 10:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Markus Dick
 */
public interface ModelChangeHandler<T> extends EventHandler {
    void onModelChange(ModelChangeEvent<T> event);
}
