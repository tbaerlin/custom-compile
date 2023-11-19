/*
 * ValidateUnless.java
 *
 * Created on 15.05.2007 09:09:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Specifies the name of a boolean property that, if true, will prevent any validation of
 * another field.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface ValidateUnless {
    String value();
}
