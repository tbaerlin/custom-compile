/*
 * ValidationEvent.java
 *
 * Created on 15.01.13 14:30
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.event.shared.GwtEvent;

public class ValidationEvent extends GwtEvent<ValidationHandler> {
    public static final Type<ValidationHandler> TYPE = new Type<ValidationHandler>();

    private final boolean valid;

    public ValidationEvent(boolean valid) {
        this.valid = valid;
    }

    @Override
    public Type<ValidationHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ValidationHandler handler) {
        handler.onValidation(this);
    }

    public boolean isValid() {
        return valid;
    }
}