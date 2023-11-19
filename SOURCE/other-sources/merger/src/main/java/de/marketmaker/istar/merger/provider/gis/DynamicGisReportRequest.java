/*
 * DynamicGisReportRequest.java
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gis;

/**
 * @author oflege
 */
public class DynamicGisReportRequest extends DynamicGisRequest {

    private static final long serialVersionUID = 1L;

    private String wkn;

    private String isin;

    private String margin;

    private GisProductType productType;

    private GisDocumentType documentType;

    private String documentGUID;

    public DynamicGisReportRequest(String documentGUID, String genoId, String blz) {
        super(genoId, blz);
        this.wkn = null;
        this.isin = null;
        this.margin = null;
        this.documentGUID = documentGUID;
        this.productType = null;
        this.documentType = null;
    }

    public DynamicGisReportRequest(String wkn, String isin, String margin, String genoId,
                                   String blz, GisProductType productType, GisDocumentType documentType) {
        super(genoId, blz);
        this.wkn = wkn;
        this.isin = isin;
        this.margin = margin;
        this.documentGUID = null;
        this.productType = productType;
        this.documentType = documentType;
    }

    public String getWkn() {
        return wkn;
    }

    public String getIsin() {
        return isin;
    }

    public String getMargin() {
        return margin;
    }

    public GisProductType getProductType() {
        return productType;
    }

    public GisDocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentGUID() {
        return documentGUID;
    }

    public DynamicGisReportRequest withProductType(GisProductType productType) {
        return new DynamicGisReportRequest(wkn, isin, margin, getGenoId(), getBlz(), productType, documentType);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", wkn='").append(wkn).append('\'')
                .append(", isin='").append(isin).append('\'')
                .append(", margin='").append(margin).append('\'')
                .append(", documentGUID='").append(documentGUID).append('\'')
                .append(", productType='").append(productType == null ? null : productType.name()).append('\'')
                .append(", documentType='").append(documentType == null ? null : documentType.name()).append('\'');
    }
}
