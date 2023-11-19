package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;

import java.util.List;

/**
 * Created on 05.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ValidationEvent extends GwtEvent<ValidationHandler> {
    private static Type<ValidationHandler> TYPE;
    private final String id;
    protected final String message;
    protected final boolean error;
    protected final List<ValidationResponse> responses;

    public static Type<ValidationHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public static void fire(String id, String message, boolean error, List<ValidationResponse> responses) {
        EventBusRegistry.get().fireEvent(new ValidationEvent(id, message, error, responses));
    }

    public static void fire(String id, List<ValidationResponse> responses) {
        EventBusRegistry.get().fireEvent(new ValidationEvent(id, null, false, responses));
    }

    @Override
    public Type<ValidationHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(ValidationHandler handler) {
        handler.onValidation(this);
    }

    public ValidationEvent(String id, String message, boolean error, List<ValidationResponse> responses) {
        this.id = id;
        this.message = message;
        this.responses = responses;
        this.error = error;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isError() {
        return this.error;
    }

    public List<ValidationResponse> getResponses() {
        return this.responses;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return "ValidationEvent{" + // $NON-NLS$
                "id='" + id + '\'' + // $NON-NLS$
                ", message=" + message + // $NON-NLS$
                ", error=" + error + // $NON-NLS$
                ", responses=" + responses + // $NON-NLS$
                '}';
    }
}