/*
 * AbstractValidatorHost.java
 *
 * Created on 11.01.13 12:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.FormatterHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.HasValidationHandlers;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorException;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.MessageSink;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Markus Dick
 */
public abstract class AbstractFormatterValidatorHost<E, T> implements ValidatorHost<E, T>, HasValidationHandlers, FormatterHost<T> {
    private static final String CLASS_NAME = AbstractFormatterValidatorHost.class.getName();
    private static final String SIMPLE_CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);

    private String message = "";
    private boolean valid = false;
    private boolean validated = false;
    private boolean enabled = true;

    private final LinkedHashSet<MessageSink> messageSinks = new LinkedHashSet<MessageSink>();
    private final HandlerManager handlerManager = new HandlerManager(this);

    private final List<Validator<T>> validators;
    private final Formatter<T> formatter;

    public AbstractFormatterValidatorHost(Formatter<T> formatter, List<Validator<T>> validators) {
        this.formatter = formatter;

        this.validators = new ArrayList<Validator<T>>();
        this.validators.addAll(validators);
    }

    /**
     * In your host implementation, call this method in your event handler and in @link{#doValidate} to format
     * and validate the formatted value.
     * @param value The value to format and validate.
     */
    protected T doFormatAndValidate(T value) {
        final T formattedValue = doFormat(value);
        doValidate(formattedValue);
        return formattedValue;
    }

    /**
     * In your host implementation, call this method in your event handler and in @link{#doValidate} to format a value.
     * @param value The value to format.
     */
    protected T doFormat(T value) {
        if(this.formatter == null || !this.enabled || !isValueSourceEnabled()) {
            return value;
        }

        return formatter.format(value);
    }

    /**
     * In your host implementation, call this method in your event handler and in @link{#doValidate} to validate a value.
     * @param value The value to validate.
     */
    protected void doValidate(T value) {
        if(!this.enabled || !isValueSourceEnabled()) {
            clear();
            return;
        }
        this.validated = true;

        final int validationHandlerCount = this.handlerManager.getHandlerCount(ValidationEvent.TYPE);

        try {
            for(Validator<T> validator : this.validators) {
                validator.validate(value);
            }
            this.valid = true;

            for(MessageSink m : this.messageSinks) {
                try {
                    m.clearMessages(this.getValueSource(), this);
                }
                catch(Throwable t) {
                    Firebug.error("<" + SIMPLE_CLASS_NAME + "> Exception while clearing validation messages", t);
                }
            }

            if(validationHandlerCount > 0) {
                fireEvent(new ValidationEvent(true));
            }
        }
        catch(ValidatorException ve) {
            this.valid = false;

            this.message = ve.getMessage();

            for(MessageSink m : this.messageSinks) {
                try {
                    m.addMessage(getValueSource(), this, this.message);
                }
                catch(Throwable t) {
                    Firebug.error("<" + SIMPLE_CLASS_NAME + "> Exception while adding validation messages", t);
                }
            }

            if(validationHandlerCount > 0) {
                fireEvent(new ValidationEvent(false));
            }
        }
    }

    @Override
    public AbstractFormatterValidatorHost<E, T> attach(MessageSink messageSink) {
        if(messageSink == null) throw new IllegalArgumentException("<" + SIMPLE_CLASS_NAME + ".attach()> messageCollector must not be null!"); //$NON-NLS$
        this.messageSinks.add(messageSink);
        return this;
    }

    @Override
    public ValidatorHost<E, T> detach(MessageSink messageSink) {
        this.messageSinks.remove(messageSink);
        return this;
    }

    @Override
    public void clear() {
//        DebugUtil.logToFirebugConsole("<"+SIMPLE_CLASS_NAME+".clear>");

        this.valid = false;
        this.validated = false;
        this.message = null;

        for(MessageSink messageSink : messageSinks) {
            try {
//                DebugUtil.logToFirebugConsole("<"+SIMPLE_CLASS_NAME+".clear>clearing messageSink:" + messageSink);

                messageSink.clearMessages(getValueSource(), this);
            }
            catch(Throwable t) {
                Firebug.error("<" + SIMPLE_CLASS_NAME + "> Exception while clearing validation messages", t);
            }
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isValid() {
        return !this.enabled || !isValueSourceEnabled() || !validated || valid;
    }

    @Override
    public void validate() {
        doValidate(getValue());
    }

    @Override
    public void format() {
        setValue(doFormat(getValue()));
    }

    public void formatAndValidate() {
        setValue(doFormatAndValidate(getValue()));
    }

    @Override
    public HandlerRegistration addValidationHandler(ValidationHandler handler) {
        return this.handlerManager.addHandler(ValidationEvent.TYPE, handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        //clear messages if an enabled validator is disabled!
        if(this.enabled && !enabled) {
            clear();
        }

        this.enabled = enabled;
    }

    protected abstract E getValueSource();

    protected abstract T getValue();

    /**
     * Implementations must not fire a value change event.
     */
    protected abstract void setValue(T value);

    protected abstract boolean isValueSourceEnabled();
}
