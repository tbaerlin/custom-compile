/*
 * HasModelChangeHandlers.java
 *
 * Created on 13.06.13 10:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Markus Dick
 */
public interface HasModelChangeHandlers<T> extends HasHandlers {
    HandlerRegistration addModelChangeHandler(ModelChangeHandler<T> handler);
}
