/*
 * AnalysisImageResponse.java
 *
 * Created on 20.04.12 10:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author oflege
 */
public class AnalysisImageResponse extends AbstractIstarResponse {
    protected static final long serialVersionUID = 1L;

    private final byte[] data;

    public AnalysisImageResponse(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", #data=").append(this.data != null ? this.data.length : -1);
    }
}
