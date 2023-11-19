/*
 * Period.java
 *
 * Created on 16.11.2006 10:24:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.marketmaker.istar.common.validator.ValidatorClass;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Documented
@ValidatorClass(PeriodValidator.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Period {
}
