package de.marketmaker.istar.merger.provider.gis;

import java.util.List;

public class DynamicGisReportListResponse extends DynamicGisResponse {

    private static final long serialVersionUID = 1L;

    private final List<GisReportDoc> reportDocs;

    public DynamicGisReportListResponse(List<GisReportDoc> reportDocs) {
        this.reportDocs = reportDocs;
        if (reportDocs == null) {
            setInvalid();
        }
    }

    public DynamicGisReportListResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
        this.reportDocs = null;
    }

    public DynamicGisReportListResponse(DynamicGisResponse response) {
        super(response.getErrorCode(), response.getErrorMessage());
        this.reportDocs = null;
    }

    public List<GisReportDoc> getReportDocs() {
        return reportDocs;
    }

    protected void appendToString(StringBuilder sb) {
        if (getErrorCode() == 0) {
            sb.append(", reportDocs.size() = ").append(this.reportDocs.size());
        } else {
            sb.append(", errorCode=").append(getErrorCode()).append(", errorMessage=").append(getErrorMessage());
        }
    }
}
