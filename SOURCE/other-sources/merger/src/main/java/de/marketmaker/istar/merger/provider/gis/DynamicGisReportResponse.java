/*
 * DynamicPibResponse.java
 *
 * Created on 04.04.14 09:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gis;

/**
 * @author oflege
 */
public class DynamicGisReportResponse extends DynamicGisResponse {

    private static final long serialVersionUID = 1L;

    private final String filename;

    private final byte[] content;

    public DynamicGisReportResponse(String filename, byte[] content) {
        this.filename = filename;
        this.content = content;
    }

    public DynamicGisReportResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.filename = null;
        this.content = null;
    }

    public DynamicGisReportResponse(DynamicGisResponse response) {
        super(response.getErrorCode(), response.getErrorMessage());
        this.filename = null;
        this.content = null;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", filename=").append(this.filename)
          .append(", content-length=").append(content != null ? content.length : -1)
          .append(", errorCode=").append(getErrorCode())
          .append(", errorMessage=").append(getErrorMessage());
    }
}
