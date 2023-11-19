/*
 * HasPresenterDisposedHandlers.java
 *
 * Created on 15.03.13 15:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Markus Dick
 */
interface HasPresenterDisposedHandlers extends HasHandlers {
    HandlerRegistration addPresenterDisposedHandler(PresenterDisposedHandler handler);
}
