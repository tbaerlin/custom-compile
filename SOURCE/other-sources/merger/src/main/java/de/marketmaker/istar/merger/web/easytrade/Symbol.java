/*
 * Symbol.java
 *
 * Created on 01.08.2006 10:04:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

import de.marketmaker.istar.common.validator.ValidatorClass;
import de.marketmaker.istar.common.validator.AssertTrueValidator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Documented
@ValidatorClass(SymbolValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface Symbol {
}
