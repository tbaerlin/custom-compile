/*
 * HasVisibilityUpdatedHandlers.java
 *
 * Created on 01.07.13 16:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Markus Dick
 */
public interface HasVisibilityUpdatedHandlers <T> extends HasHandlers {
    HandlerRegistration addVisibilityUpdatedHandler(VisibilityUpdatedHandler<T> handler);
}
