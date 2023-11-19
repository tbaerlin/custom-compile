/*
 * Pattern.java
 *
 * Created on 31.07.2006 17:17:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Documented
@ValidatorClass(DateFormatValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface DateFormat {
	/** regular expression */
	String format();
}
