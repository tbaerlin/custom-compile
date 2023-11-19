/*
 * VisibilityUpdatedHandler.java
 *
 * Created on 17.04.13 15:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Markus Dick
 */
public interface VisibilityUpdatedHandler<T> extends EventHandler {
    void onVisibilityUpdated(VisibilityUpdatedEvent<T> event);
}
