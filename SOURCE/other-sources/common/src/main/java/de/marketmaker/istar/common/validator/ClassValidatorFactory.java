/*
 * ClassValidatorFactory.java
 *
 * Created on 31.07.2006 16:52:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ClassValidatorFactory implements Validator {

    private static final Map<Class, ClassValidator> CLASS_CACHE =
            new ConcurrentHashMap<>();

    public static ClassValidator forObject(Object o) {
        if (o == null) {
            return null;
        }
        return forClass(o.getClass());
    }

    public static ClassValidator forClass(Class c) {
        return CLASS_CACHE.computeIfAbsent(c, ClassValidator::create);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return forClass(clazz) != ClassValidator.NULL;
    }

    @Override
    public void validate(Object target, Errors errors) {
        forClass(target.getClass()).validate(target, errors);
    }
}
