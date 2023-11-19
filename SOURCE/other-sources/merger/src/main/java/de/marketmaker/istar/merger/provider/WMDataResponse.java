/*
 * WMDataResponse.java
 *
 * Created on 02.11.11 18:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.WMData;

/**
 * @author oflege
 */
public class WMDataResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private Map<Long, WMData> result;
    private Map<String, WMData> isinResult;

    public WMDataResponse(Map<Long, WMData> results, Map<String, WMData> isinResults) {
        if (results == null && isinResults==null) {
            setInvalid();
            return;
        }

        if (results != null) {
            this.result = new HashMap<>(results);
        }
        if (isinResults != null) {
            this.isinResult = new HashMap<>(isinResults);
        }
    }

    public WMData getData(Long iid) {
        return this.result.get(iid);
    }
    public WMData getData(String isin) {
        return this.isinResult.get(isin);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append("result=").append(this.result);
        sb.append("isinResults").append(this.isinResult);
    }
}
