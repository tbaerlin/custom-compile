/*
 * DocResponse.java
 *
 * Created on 28.03.11 14:09
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import org.joda.time.DateTime;

/**
 * Result for a corresponding PibRequest, containing the pdfId, possibly the pdf itself as raw
 * data and the isin of the underlying instrument, if no symbol was given in the request.
 * <p/>
 * @author oflege
 * @author mdick
 */
public class DocResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 3L;

    public static final DocResponse INVALID = new DocResponse();

    private final byte[] pdf;

    private final String pdfId;

    private final String isin;

    private final String wkn;

    private final String name;

    private final String type;

    private final DateTime creationTime;

    private final String language;

    private final String country;

    private final String version;

    private final String deri;

    private final boolean hasDeeplink;

    private final String deeplinkUrl;
    
    private final String errorCode;
    
    private final String errorMessage;

    private DocResponse() {
        this.pdf = null;
        this.pdfId = null;
        this.isin = null;
        this.wkn = null;
        this.name = null;
        this.type = null;
        this.creationTime = null;
        this.country = null;
        this.language = null;
        this.version = null;
        this.deri = null;
        this.hasDeeplink = false;
        this.deeplinkUrl = null;
        this.errorCode = null;
        this.errorMessage = null;
        setInvalid();
    }
    
    public DocResponse(String errorCode, String errorMessage) {
        if(errorCode == null || errorCode.isEmpty() || errorMessage == null || errorMessage.isEmpty()) {
            throw new IllegalArgumentException("neither errorCode nor errorMessage must be null");
        }
        this.pdf = null;
        this.pdfId = null;
        this.isin = null;
        this.wkn = null;
        this.name = null;
        this.type = null;
        this.creationTime = null;
        this.country = null;
        this.language = null;
        this.version = null;
        this.deri = null;
        this.hasDeeplink = false;
        this.deeplinkUrl = null;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        setInvalid();
    }

    public DocResponse(String pdfId, byte[] pdf, String isin, String wkn, String name, String type,
        DateTime creationTime, String language, String country, String version, String deri) {
        this.pdf = pdf;
        this.pdfId = pdfId;
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
        this.type = type;
        this.creationTime = creationTime;
        this.language = language;
        this.country = country;
        this.version = version;
        this.deri = deri;
        this.hasDeeplink = false;
        this.deeplinkUrl = null;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public DocResponse(String pdfId, byte[] pdf, String isin, String wkn, String name, String type,
        DateTime creationTime, String language, String country, String version, String deri, boolean hasDeeplink,
        String deeplinkUrl) {
        this.pdf = pdf;
        this.pdfId = pdfId;
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
        this.type = type;
        this.creationTime = creationTime;
        this.language = language;
        this.country = country;
        this.version = version;
        this.deri = deri;
        this.hasDeeplink = hasDeeplink;
        this.deeplinkUrl = deeplinkUrl;
        this.errorCode = null;
        this.errorMessage = null;
    }

    public DocResponse(String pdfId, byte[] pdf, String isin, String wkn, String name, String type,
        DateTime creationTime, String language, String country, String version, boolean hasDeeplink,
        String deeplinkUrl) {
        this(pdfId, pdf, isin, wkn, name, type, creationTime, language, country, version, null, hasDeeplink,
            deeplinkUrl);
    }

    public DocResponse(String pdfId, byte[] pdf, String isin, String wkn, String name, String type,
        DateTime creationTime, String language, String country, String version) {
        this(pdfId, pdf, isin, wkn, name, type, creationTime, language, country, version, null, false,
            null);
    }

    public String getType() {
        return type;
    }

    public String getPdfId() {
        return pdfId;
    }

    public byte[] getPdf() {
        return pdf;
    }

    public String getIsin() {
        return isin;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public String getWkn() {
        return wkn;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getVersion() {
        return version;
    }

    public String getDeri() {
        return deri;
    }

    public boolean getHasDeeplink() {
        return this.hasDeeplink;
    }

    public String getDeeplinkUrl() {
        return this.deeplinkUrl;
    }
    
    public String getErrorCode() {
        return this.errorCode;
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
