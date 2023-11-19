/*
 * RawCallable.java
 *
 * Created on 09.03.2011 16:44:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

/**
 * This interface represents wire-level remote method calls (server side)
 *
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public interface WireLevelServiceProvider {

    /**
     * Process a request and return a result; the result can be null.
     * <blockquote>
     * <b>This method may not throw <em>any</em> exceptions!<br/>
     * Exceptional situations have to be encoded
     * as byte[].</b></blockquote>
     * @param args the arguments of the call
     * @return the result
     */
    byte[] call(byte[] args);
}
