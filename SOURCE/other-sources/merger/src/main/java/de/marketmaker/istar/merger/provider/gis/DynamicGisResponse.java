package de.marketmaker.istar.merger.provider.gis;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

public class DynamicGisResponse extends AbstractIstarResponse {

    private static final long serialVersionUID = 1L;

    private final int errorCode;

    private final String errorMessage;

    public DynamicGisResponse() {
        this.errorCode = 0;
        this.errorMessage = null;
    }

    public DynamicGisResponse(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
