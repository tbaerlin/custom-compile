/*
 * NotZero.java
 *
 * Created on 4/2/14 4:39 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Stefan Willenbrock
 */
@Documented
@ValidatorClass(NotZeroValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface NotZero {
}