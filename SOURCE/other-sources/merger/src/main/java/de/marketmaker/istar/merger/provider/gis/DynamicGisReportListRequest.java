package de.marketmaker.istar.merger.provider.gis;

public class DynamicGisReportListRequest extends DynamicGisRequest {

    private static final long serialVersionUID = 1L;

    private String wkn;

    private String isin;

    private String margin;

    private GisProductType productType;

    public DynamicGisReportListRequest(String wkn, String isin, String margin, String genoId, String blz, GisProductType productType) {
        super(genoId, blz);
        this.wkn = wkn;
        this.isin = isin;
        this.margin = margin;
        this.productType = productType;
    }

    public String getWkn() {
        return this.wkn;
    }

    public String getIsin() {
        return this.isin;
    }

    public String getMargin() {
        return this.margin;
    }

    public GisProductType getProductType() {
        return this.productType;
    }

    public DynamicGisReportListRequest withProductType(GisProductType productType) {
        return new DynamicGisReportListRequest(wkn, isin, margin, getGenoId(), getBlz(), productType);
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(String.format(", wkn=%s, isin=%s, margin=%s, productType=%s", wkn, isin, margin, productType));
    }
}

