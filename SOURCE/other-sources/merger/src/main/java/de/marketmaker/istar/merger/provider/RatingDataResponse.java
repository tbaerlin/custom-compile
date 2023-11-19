/*
 * WMDataResponse.java
 *
 * Created on 02.11.11 18:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.RatingData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tkiesgen
 */
public class RatingDataResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private Map<Long, RatingData> result = new HashMap<>();

    public RatingDataResponse(Map<Long, RatingData> result) {
        if (result != null) {
            this.result.putAll(result);
        }
        else {
            setInvalid();
        }
    }

    public RatingData getData(Long iid) {
        return this.result.get(iid);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("result=").append(this.result);
    }
}
