/*
 * AnalysesMetaResponse.java
 *
 * Created on 21.03.12 08:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author oflege
 */
public class AnalysesMetaResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private Map<String, Map<String, String>> data;

    public AnalysesMetaResponse(Map<String, Map<String, String>> data) {
        this.data = data;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.data != null;
    }
}
