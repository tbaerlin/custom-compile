/*
 * PricesUpdatedHandler.java
 *
 * Created on 02.02.2010 12:55:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author oflege
 */
public interface PricesUpdatedHandler extends EventHandler {
    void onPricesUpdated(PricesUpdatedEvent event);
}
