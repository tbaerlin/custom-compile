/*
 * AssertTrue.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Documented
@ValidatorClass(AssertTrueValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface AssertTrue {
}
