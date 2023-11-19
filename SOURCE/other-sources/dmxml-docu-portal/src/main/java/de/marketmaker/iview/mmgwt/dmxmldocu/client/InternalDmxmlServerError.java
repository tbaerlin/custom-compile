/*
 * InternalDmxmlServerError.java
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
public class InternalDmxmlServerError extends RuntimeException implements IsSerializable {

    public InternalDmxmlServerError() {
    }

    public InternalDmxmlServerError(String message) {
        super(message);
    }

    public InternalDmxmlServerError(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalDmxmlServerError(Throwable cause) {
        super(cause);
    }
}
