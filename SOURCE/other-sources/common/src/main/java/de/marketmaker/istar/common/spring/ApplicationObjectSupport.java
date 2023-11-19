/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shadows jcl logger from {@link ApplicationObjectSupport}.
 */
public abstract class ApplicationObjectSupport extends org.springframework.context.support.ApplicationObjectSupport {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

}
