/*
 * MMServiceResponse.java
 *
 * Created on 29.10.2008 14:55:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MMServiceResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private final Object[] data;

    public MMServiceResponse(Object[] data) {
        this.data = data;
    }

    public Object[] getData() {
        return this.data;
    }
}
