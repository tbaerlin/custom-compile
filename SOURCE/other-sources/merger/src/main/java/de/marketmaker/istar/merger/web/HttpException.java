/*
 * NoProfileException.java
 *
 * Created on 29.03.2007 10:08:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.merger.MergerException;

/**
 * Represents an error condition in an http setting. Controllers should throw this exception
 * instead of calling {@link javax.servlet.http.HttpServletResponse#sendError(int)} or
 * {@link javax.servlet.http.HttpServletResponse#sendError(int,String)} directly, because throwing
 * an exception allows the ZoneDispatcherServlet to catch it and apply zone specific error handling.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HttpException extends MergerException {
    private int errorCode;

    public HttpException(int errorCode) {
        this(errorCode, null);
    }

    public HttpException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return "http." + this.errorCode;
    }
}