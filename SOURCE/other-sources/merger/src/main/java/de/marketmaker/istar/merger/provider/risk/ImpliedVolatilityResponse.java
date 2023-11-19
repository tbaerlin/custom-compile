/*
 * ImpliedVolatilityResponse.java
 *
 * Created on 14.12.11 13:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.risk;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.ImpliedVolatilities;

/**
 * @author oflege
 */
public class ImpliedVolatilityResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final ImpliedVolatilities result;

    public ImpliedVolatilityResponse(ImpliedVolatilities result) {
        this.result = result;
    }

    public ImpliedVolatilities getResult() {
        return this.result;
    }
}
