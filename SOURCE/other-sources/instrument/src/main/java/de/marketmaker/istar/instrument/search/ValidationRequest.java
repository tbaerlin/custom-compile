/*
 * ValidationRequest.java
 *
 * Created on 09.06.2010 14:54:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Arrays;
import java.util.Collection;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author oflege
 */
public class ValidationRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 122L;

    private final long[] iids;

    public ValidationRequest(long[] iids) {
        this.iids = Arrays.copyOf(iids, iids.length);
    }

    public ValidationRequest(Collection<Long> iids) {
        this.iids = new long[iids.size()];
        int n = 0;
        for (Long iid : iids) {
            this.iids[n++] = (iid != null) ? iid : 0;
        }
    }

    public long[] getIids() {
        return this.iids != null ? this.iids : new long[0];
    }
}
