/*
 * PropertyValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;

import org.springframework.beans.BeanWrapper;
import org.springframework.validation.Errors;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PropertyValidator<A extends Annotation> {
    /**
     * does the object/element pass the constraints
     */
    public void validate(BeanWrapper value, Errors errors);

    /**
     * Take the annotations values
     *
     * @param parameters
     * @param propertyDescriptor
     */
    public void initialize(A parameters, PropertyDescriptor propertyDescriptor);

    /**
     * Returns the property name associated with this validator
     * @return
     */
    String getPropertyName();
}
