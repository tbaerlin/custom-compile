/*
 * ValidationRequest.java
 *
 * Created on 09.06.2010 14:54:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author oflege
 */
public class ValidationResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 122L;

    private long[] invalidIids;

    public long[] getInvalidIids() {
        return invalidIids;
    }

    public void setInvalidIids(long[] invalidIids) {
        this.invalidIids = invalidIids;
    }
}