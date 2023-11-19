/*
 * GisReportDoc.java
 *
 * Created on 22.02.2019 13:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gis;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Is reproducing the content FinanzproduktDZBANK.2018.1.gibAktuelleProduktinformationen.Antwort.xsd -> AktuelleProduktinformationen.Dokumente
 *
 * @author twiegel
 */
public class GisReportDoc implements Serializable {
    static final long serialVersionUID = 2L;

    private String isin;

    private String wkn;

    private GisProductType productType;

    private GisDocumentType documentType;

    private String referenceID;

    private String documentGUID;

    private String anhangID;

    private String mimeType;

    private String name;

    private String margin;

    private long dateOfExpiry;

    private long timestamp;

    public GisReportDoc(String isin, String wkn, String margin, GisProductType productType, GisDocumentType documentType, String referenceID, String documentGUID, String anhangID, String mimeType, String name, long dateOfExpiry, long timestamp) {
        this.isin = isin;
        this.wkn = wkn;
        this.margin = margin;
        this.productType = productType;
        this.documentType = documentType;
        this.referenceID = referenceID;
        this.documentGUID = documentGUID;
        this.anhangID = anhangID;
        this.mimeType = mimeType;
        this.name = name;
        this.dateOfExpiry = dateOfExpiry;
        this.timestamp = timestamp;
    }

    public String getIsin() {
        return isin;
    }

    public String getWkn() {
        return wkn;
    }

    public GisProductType getProductType() {
        return productType;
    }

    public GisDocumentType getDocumentType() {
        return documentType;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public String getDocumentGUID() {
        return documentGUID;
    }

    public String getAnhangID() {
        return anhangID;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getName() {
        return name;
    }

    public DateTime getDateOfExpiry() {
        return new DateTime(this.dateOfExpiry);
    }

    public DateTime getTimestamp() {
        return new DateTime(this.timestamp);
    }

    public String getMargin() {
        return margin;
    }
}
