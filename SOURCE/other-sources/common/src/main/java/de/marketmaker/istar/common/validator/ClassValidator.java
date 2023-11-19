/*
 * ClassValidator.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ClassValidator<T> implements Validator {
    private final Class<T> beanClass;

    private final List<ValidatorWrapper> validators = new ArrayList<>();

    static final ClassValidator<Object> NULL = doCreate(Object.class);

    static <T> ClassValidator<T> create(Class<T> beanClass) {
        final ClassValidator<T> v = doCreate(beanClass);
        return (v == null || v.isEmpty()) ? (ClassValidator<T>) NULL : v;
    }

    private static <T> ClassValidator<T> doCreate(Class<T> beanClass) {
        try {
            return new ClassValidator<>(beanClass);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            return null;
        }
    }

    @Immutable
    private static class ValidatorWrapper {
        private final PropertyValidator pv;
        private final String unlessProperty;

        ValidatorWrapper(PropertyValidator pv, String unlessProperty) {
            this.pv = pv;
            this.unlessProperty = unlessProperty;
        }

        void validate(BeanWrapper bw, Errors errors) {
            if (this.unlessProperty != null
                    && ((Boolean)bw.getPropertyValue(this.unlessProperty))) {
                return;
            }
            this.pv.validate(bw, errors);
        }
    }


    /**
     * create the validator engine for this bean type
     */
    private ClassValidator(Class<T> beanClass) throws IllegalAccessException, InstantiationException {
        this.beanClass = beanClass;
        initValidator(this.beanClass.newInstance());
    }

    public boolean isEmpty() {
        return validators.isEmpty();
    }


    private void initValidator(T t) {
        final BeanWrapperImpl bw = new BeanWrapperImpl(t);

        for (final PropertyDescriptor pd : bw.getPropertyDescriptors()) {
            final Method readMethod = pd.getReadMethod();
            if (readMethod == null) {
                continue;
            }

            final String unless = getUnless(readMethod, bw);

            final Annotation[] annotations = readMethod.getAnnotations();
            for (final Annotation a : annotations) {
                final PropertyValidator pv = createValidator(a, pd);
                if (pv != null) {
                    this.validators.add(new ValidatorWrapper(pv, unless));
                }
            }
        }
    }

    private String getUnless(Method readMethod, BeanWrapperImpl bw) {
        final ValidateUnless unless = readMethod.getAnnotation(ValidateUnless.class);
        if (unless == null) {
            return null;
        }
        // getPropertyDescriptor to make sure property exists
        return bw.getPropertyDescriptor(unless.value()).getName();
    }

    private PropertyValidator createValidator(Annotation annotation, PropertyDescriptor pd) {
        final ValidatorClass validatorClass = annotation.annotationType().getAnnotation(ValidatorClass.class);
        if (validatorClass == null) {
            return null;
        }
        try {
            final PropertyValidator validator = validatorClass.value().newInstance();
            validator.initialize(annotation, pd);
            return validator;

        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public boolean supports(Class aClass) {
        return aClass == this.beanClass;
    }

    public void validate(Object t, Errors errors) {
        final BeanWrapper bw = new BeanWrapperImpl(t);
        for (ValidatorWrapper wrapper : this.validators) {
            wrapper.validate(bw, errors);
        }
    }
}
