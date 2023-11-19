/*
 * HasValidationHandlers.java
 *
 * Created on 15.01.13 14:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author Markus Dick
 */
public interface HasValidationHandlers extends HasHandlers {
    HandlerRegistration addValidationHandler(ValidationHandler handler);
}
