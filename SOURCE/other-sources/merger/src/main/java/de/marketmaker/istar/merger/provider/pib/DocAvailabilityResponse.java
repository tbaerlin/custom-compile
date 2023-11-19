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
 * @author zzhao
 */
public class DocAvailabilityResponse extends AbstractIstarResponse {

    public static final DocAvailabilityResponse INVALID = new DocAvailabilityResponse();

    static final long serialVersionUID = 5L;

    private final String isin;

    private final String wkn;

    private final String language;

    private final String country;

    private final String docType;

    protected DocAvailabilityResponse() {
        this(null, null, null, null, null);
        setInvalid();
    }

    public DocAvailabilityResponse(String isin, String wkn, String language, String country,
            String docType) {
        this.isin = isin;
        this.wkn = wkn;
        this.language = language;
        this.country = country;
        this.docType = docType;
    }

    public String getIsin() {
        return isin;
    }

    public String getWkn() {
        return wkn;
    }

    public String getSymbol() {
        return this.isin != null ? this.isin : this.wkn;
    }

    public String getDocType() {
        return docType;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", ")
                .append(wkn).append(";")
                .append(isin).append(";")
                .append(language).append(";")
                .append(country).append(";")
                .append(docType);
    }
}
