/*
 * PresenterDisposedEventHandler.java
 *
 * Created on 15.03.13 15:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Markus Dick
 */
public interface PresenterDisposedHandler extends EventHandler {
    void onPresenterDisposed(PresenterDisposedEvent event);
}
