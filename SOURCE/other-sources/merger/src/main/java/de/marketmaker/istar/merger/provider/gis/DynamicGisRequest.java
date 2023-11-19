package de.marketmaker.istar.merger.provider.gis;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

public class DynamicGisRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = 1L;

    private final String genoId;

    private final String blz;

    public DynamicGisRequest(String genoId, String blz) {
        this.genoId = genoId;
        this.blz = blz;
    }

    public String getGenoId() {
        return genoId;
    }

    public String getBlz() {
        return blz;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", genoId='").append(genoId).append('\'')
                .append(", blz='").append(blz).append('\'');
    }
}
