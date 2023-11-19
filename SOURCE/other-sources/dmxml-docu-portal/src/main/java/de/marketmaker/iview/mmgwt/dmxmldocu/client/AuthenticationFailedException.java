/*
 * AuthenticationFailedException.java
 *
 * Created on 21.03.12 13:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class AuthenticationFailedException extends Exception implements IsSerializable {

    public AuthenticationFailedException() {
    }

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthenticationFailedException(Throwable cause) {
        super(cause);
    }
}
