package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created on 31.10.12 09:23
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface PageControllerCreatedHandler extends EventHandler {
    void afterCreated(PageControllerCreatedEvent event);
}
