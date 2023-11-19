/*
 * FTADataRequest.java
 *
 * Created on 5/17/13 1:52 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.ftadata;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Stefan Willenbrock
 */
public class FTADataRequest extends AbstractIstarRequest {
    private final static long serialVersionUID = 1L;

    private final long instrumentid;

    public FTADataRequest(long instrumentid) {
        this.instrumentid = instrumentid;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

}
