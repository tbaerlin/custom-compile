/*
 * ConvensysRawdataResponse.java
 *
 * Created on 09.01.12 14:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.ConvensysRawdata;

/**
 * @author tkiesgen
 */
public class ConvensysRawdataResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private ConvensysRawdata rawdata;

    public ConvensysRawdata getRawdata() {
        return rawdata;
    }

    public void setRawdata(ConvensysRawdata rawdata) {
        this.rawdata = rawdata;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", rawdata=").append(this.rawdata);
    }
}