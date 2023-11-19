/*
 * ErrorPage.java
 *
 * Created on 03.08.2009 13:59:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ErrorPage {
    public static final String ERROR_PAGE_NAME = ErrorPage.class.getSimpleName();

    private final int errorCode;

    private final String errorMessage;

    private final String contentType;

    public static ErrorPage from(ModelAndView mav) {
        if (ERROR_PAGE_NAME.equals(mav.getViewName())) {
            return (ErrorPage) mav.getModel().get(ERROR_PAGE_NAME);
        }
        return null;
    }

    public ErrorPage(int errorCode, String errorMessage, String contentType) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.contentType = contentType;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getContentType() {
        return contentType;
    }

    public ErrorPage resolve(String errorMessage) {
        String msg = this.errorMessage;
        if (msg.contains("$date")) {
            msg = msg.replace("$date", ISODateTimeFormat.dateTimeNoMillis().print(new DateTime()));
        }
        if (msg.contains("$errorMessage")) {
            msg = msg.replace("$errorMessage", String.valueOf(errorMessage));
        }

        //noinspection StringEquality
        return (msg == this.errorMessage) ? this : new ErrorPage(errorCode, msg, this.contentType);
    }

    public ModelAndView asModelAndView() {
        return new ModelAndView(ERROR_PAGE_NAME, ERROR_PAGE_NAME, this);
    }
}
