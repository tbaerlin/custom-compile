/*
 * ValidationHandler.java
 *
 * Created on 15.01.13 14:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.event.shared.EventHandler;

/**
 * @author Markus Dick
 */
public interface ValidationHandler extends EventHandler {
    void onValidation(ValidationEvent validationEvent);
}
