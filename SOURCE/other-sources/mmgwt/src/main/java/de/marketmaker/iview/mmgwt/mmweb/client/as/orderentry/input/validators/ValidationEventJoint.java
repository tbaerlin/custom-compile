/*
 * ValidationEventJoint.java
 *
 * Created on 14.01.13 10:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import java.util.LinkedHashMap;

/**
 * @author Markus Dick
 */
public class ValidationEventJoint implements HasValidationHandlers, ValidationHandler {
    private final LinkedHashMap<ValidatorHost, HandlerRegistration> validators = new LinkedHashMap<ValidatorHost, HandlerRegistration>();

    private final HandlerManager handlerManager = new HandlerManager(this);

    private boolean valid = true;

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    @Override
    public void onValidation(ValidationEvent validationEvent) {
        validateAll();
    }

    private void validateAll() {
        boolean allValid = true;

        for(ValidatorHost v : this.validators.keySet()) {
            allValid &= v.isValid();
        }
        this.valid = allValid;

        fireEvent(new ValidationEvent(allValid));
    }

    public void checkAllValid() {
        validateAll();
    }

    public boolean isValid() {
        return this.valid;
    }

    public ValidationEventJoint attach(ValidatorHost validator) {
        validators.put(validator, validator.addValidationHandler(this));
        return this;
    }

    public ValidationEventJoint detach(ValidatorHost validator) {
        HandlerRegistration handlerRegistration = validators.remove(validator);
        handlerRegistration.removeHandler();
        return this;
    }

    public HandlerRegistration addValidationHandler(ValidationHandler handler) {
        return this.handlerManager.addHandler(ValidationEvent.TYPE, handler);
    }
}
