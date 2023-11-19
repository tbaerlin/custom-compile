/*
 * HasActionPerformedHandlers.java
 *
 * Created on 28.06.13 14:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Markus Dick
 */
public interface HasActionPerformedHandlers extends HasHandlers {
    HandlerRegistration addActionPerformedHandler(ActionPerformedHandler handler);
}
