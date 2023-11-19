/*
 * Min.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Value can be comma separated list, BUT without whitespace in between.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Documented
@ValidatorClass(RestrictedSetValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface RestrictedSet {

  String value();
}
