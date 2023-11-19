/*
 * DpPricesValidator.java
 *
 * Created on 29.04.2005 15:35:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp.web;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id$
 */
public class DpPricesValidator implements Validator {

    public boolean supports(Class aClass) {
        return aClass == DpPricesCommand.class;
    }

    public void validate(Object o, Errors errors) {
        final DpPricesCommand command = (DpPricesCommand) o;

        if (!StringUtils.hasText(command.getRequest())) {
            errors.rejectValue("request", "request is empty");
        }
    }
}
