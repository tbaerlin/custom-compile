/*
 * Validator.java
 *
 * Created on 20.12.12 09:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators;

import com.google.gwt.user.client.ui.HasEnabled;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.MessageSink;

/**
 * @author Markus Dick
 */
public interface ValidatorHost<E, T> extends HasValidationHandlers, HasEnabled {
    public String getMessage();

    /**
     * This method must not validate! It returns only the result of a former call to validate.
     * Return only false if the validator has already been validated to false.
     * In all other cases (disabled validator host, disabled target, validate has not been called)
     * the method is expected to return true.
     */
    public boolean isValid();

    /**
     * Performs the validation.
     * If the validator or its target are not enabled it clears the validator and its messages.
     */
    public void validate();
    public ValidatorHost<E, T> attach(MessageSink messageCollector);
    public ValidatorHost<E, T> detach(MessageSink messageCollector);

    /**
     * Clears/resets the validator host and removes all messages of this validator host from the attached message sinks.
     */
    public void clear();

    /**
     * If the validator host is disabled but was enabled, its messages have to be cleared!
     */
    void setEnabled(boolean enabled);
}
