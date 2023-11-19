/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


/**
 * Defines the callback to be used to handle errors in
 * asynchronous method invocations.
 *
 * When an operation is executed asynchronously it is not
 * possible to use exceptions for error handling. Instead,
 * the caller must provide a callback to be used in case
 * of error.
 * @author Oliver Flege
 */
public interface CallbackErrorHandler {
    /**
     * Called when an exception is raised when executing an
     * asynchronous method.
     * @param message
     * @param ex
     */
    public void handleError(String message, Exception ex);
}
