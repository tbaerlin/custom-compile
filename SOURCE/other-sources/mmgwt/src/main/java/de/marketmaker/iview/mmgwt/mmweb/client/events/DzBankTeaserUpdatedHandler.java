/*
 * DzBankTeaserUpdatedEvent.java
 *
 * Created on 09.02.2016 07:48:42
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author mdick
 */
public interface DzBankTeaserUpdatedHandler extends EventHandler {
    void onDzBankTeaserUpdated(DzBankTeaserUpdatedEvent event);
}
