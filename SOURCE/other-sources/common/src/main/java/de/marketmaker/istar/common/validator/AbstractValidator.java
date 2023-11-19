/*
 * AbstractValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;

import org.springframework.validation.Errors;
import org.springframework.beans.BeanWrapper;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractValidator<A extends Annotation> implements PropertyValidator<A> {
    private PropertyDescriptor propertyDescriptor;

    public final void initialize(A parameters, PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
        doInitialize(parameters);
    }

    protected void doInitialize(A parameters) {
        // empty
    }

    protected PropertyDescriptor getPropertyDescriptor() {
        return this.propertyDescriptor;
    }

    public String getPropertyName() {
        return this.propertyDescriptor.getName();
    }

    public void validate(BeanWrapper bw, Errors errors) {
        validateValue(getValue(bw), errors);
    }

    protected Object getValue(BeanWrapper bw) {
        return getValue(bw, getPropertyName());
    }

    protected Object getValue(BeanWrapper bw, String name) {
        return bw.getPropertyValue(name);
    }

    public void validateValue(Object value, Errors errors) {
        // empty
    }
}
