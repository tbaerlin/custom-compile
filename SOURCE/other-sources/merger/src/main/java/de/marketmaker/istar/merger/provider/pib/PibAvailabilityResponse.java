/*
 * PibAvailabilityResponse.java
 *
 * Created on 24.06.11 14:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * Result for a corresponding PibRequest, containing the name of an instrument if available.
 * <p/>
 *
 * @author tkiesgen
 */
public class PibAvailabilityResponse extends AbstractIstarResponse {

    public static final PibAvailabilityResponse INVALID = new PibAvailabilityResponse();

    static final long serialVersionUID = 4L;

    private final String isin;

    private final String wkn;

    private final String name;

    private final String issuer;

    private final String gd198c;

    private final boolean available;

    private String reason;

    private final boolean issuerPibSource;

    private final boolean vendorPib;

    protected PibAvailabilityResponse() {
        this(null, null, null, null, false, null, false, false);
        setInvalid();
    }

    public PibAvailabilityResponse(String isin, String wkn, String name, String gd198c,
            boolean available, String issuer, boolean issuerPibSource, boolean vendorPib) {
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
        this.gd198c = gd198c;
        this.available = available;
        this.issuerPibSource = issuerPibSource;
        this.issuer = issuer;
        this.vendorPib = vendorPib;
    }

    public static PibAvailabilityResponse notAvailable(String reason) {
        final PibAvailabilityResponse resp =
                new PibAvailabilityResponse(null, null, null, null, false, null, false, false);
        resp.setReason(reason);
        return resp;
    }

    public static PibAvailabilityResponse notAvailable(String isin, String wkn, String name,
            String gd198c, String issuer, String reason, boolean issuerPibSource) {
        final PibAvailabilityResponse resp =
                new PibAvailabilityResponse(isin, wkn, name, gd198c, false, issuer, issuerPibSource, false);
        resp.setReason(reason);
        return resp;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getReason() {
        return reason;
    }

    private void setReason(String reason) {
        this.reason = reason;
    }

    public String getIsin() {
        return isin;
    }

    public String getWkn() {
        return wkn;
    }

    public String getName() {
        return name;
    }

    public String getGd198c() {
        return gd198c;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isIssuerPibSource() {
        return issuerPibSource;
    }

    public boolean isVendorPib() {
        return vendorPib;
    }

    protected void appendToString(StringBuilder sb) {
        if (this.available) {
            sb.append(", ").append(wkn).append(";").append(isin)
                    .append(";").append(name).append("';");
            if (isIssuerPibSource()) {
                sb.append("Issuer");
            }
            else if (isVendorPib()) {
                sb.append("Vendor");
            }
            else {
                sb.append("vwd");
            }
        }
        else {
            sb.append(", not available, reason: ").append(this.reason);
        }
    }
}
