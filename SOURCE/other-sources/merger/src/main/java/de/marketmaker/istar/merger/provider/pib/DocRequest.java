/*
 * PibRequest.java
 *
 * Created on 28.03.11 16:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.Zone;

/**
 * @author oflege
 */
public class DocRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 3L;

    private final String symbol;

    private final String client;

    private final String variant;

    private final boolean asPdfA;

    private final boolean testMode;

    private final String userInfo;

    private final String vwdId;

    private final String address;

    private final String pdfId;

    private boolean returnPdfIdOnly;

    private final String localization;

    private Map<String, String> properties = new HashMap<>();

    private String docType;

    private Profile profile;

    private String constraint;

    private boolean downloadIssuerPIB = false;

    /**
     * Request a previously generated pdf
     * @param pdfId identifies a previously generated pdf
     * @param client identifies client
     * @param variant identifies the layout variant
     * @return PibRequest for getting PIBs by ID
     */
    static public DocRequest byId(String pdfId, String client, String variant, Zone zone) {
        final DocRequest request = new DocRequest(null, client, variant, false, false, null, null,
                null, null, pdfId, null);
        constrain(request, zone);
        return request;
    }

    private static final String CTX_ATTRIBUTE_CONSTRAINT = "constraint";

    private static final String CTX_ATTRIBUTE_TEST_MODE = "testMode";

    private static final String CTX_ATTRIBUTE_ALLOWED_DOC_TYPES = "allowedDocTypes";

    private static void constrain(DocRequest request, Zone zone) {
        final Map<String, Object> contextMap = zone.getContextMap("");
        checkDocType(contextMap, request.getDocType());
        Object obj = contextMap.get(CTX_ATTRIBUTE_CONSTRAINT);
        if (null != obj && StringUtils.isNotBlank(String.valueOf(obj))) {
            request.constraint = String.valueOf(obj);
        }

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            request.profile = requestContext.getProfile();
        }
    }

    private static void checkDocType(Map<String, Object> contextMap, String docType) {
        if (null != docType) {
            if (!docTypeAllowed(contextMap, docType)) {
                throw new DocProviderException("invalid.doc.type",
                        docType + " not supported ("
                                + contextMap.get(DocRequest.CTX_ATTRIBUTE_ALLOWED_DOC_TYPES)
                                + " allowed)"
                );
            }
        }
    }

    public static boolean docTypeAllowed(Map<String, Object> contextMap, String docType) {
        final Object obj = contextMap.get(DocRequest.CTX_ATTRIBUTE_ALLOWED_DOC_TYPES);
        if (null != obj) {
            final String types = String.valueOf(obj);
            if (!types.contains(docType)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTestMode(Map<String, Object> contextMap, String variant) {
        if (null == variant) {
            return false;
        }

        final Object obj = contextMap.get(DocRequest.CTX_ATTRIBUTE_TEST_MODE);
        if (null != obj) {
            final String variants = String.valueOf(obj);
            return variants.contains(variant);
        }

        return false;
    }

    /**
     * Requests the generation of a new pdf
     * @param symbol identifies instrument
     * @param client identifies client
     * @param variant identifies the layout variant
     * @param testMode print watermark "Muster" if true
     * @param userInfo user information
     * @param localization defines language and country
     * @param address client's remote address
     * @return PibRequest for getting PIBs by symbol
     */
    static public DocRequest bySymbol(String symbol, String client, String variant,
            boolean testMode, String userInfo, String localization, String address,
            String docType, Zone zone) {

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final String vwdId = profile instanceof VwdProfile ? ((VwdProfile) profile).getVwdId() : null;

        final DocRequest req = new DocRequest(symbol, client, variant,
                profile.isAllowed(Selector.DOCMAN_PDF_A_1B_Format),
                testMode, userInfo, vwdId, localization, address, null, docType);
        constrain(req, zone);
        return req;
    }

    private DocRequest(String symbol, String client, String variant, boolean asPdfA,
            boolean testMode, String userInfo, String vwdId, String localization, String address,
            String pdfId, String docType) {
        this.symbol = symbol;
        this.client = client;
        this.variant = variant;
        this.asPdfA = asPdfA;
        this.testMode = testMode;
        this.userInfo = userInfo;
        this.vwdId = vwdId;
        this.address = address;
        this.pdfId = pdfId;
        this.localization = localization;
        this.docType = docType;
    }

    public boolean isDownloadIssuerPIB() {
        return downloadIssuerPIB;
    }

    public void setDownloadIssuerPIB(boolean downloadIssuerPIB) {
        this.downloadIssuerPIB = downloadIssuerPIB;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setReturnPdfIdOnly() {
        this.returnPdfIdOnly = true;
    }

    public boolean isReturnPdfIdOnly() {
        return returnPdfIdOnly;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getClient() {
        return client;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public String getVwdId() {
        return vwdId;
    }

    public String getAddress() {
        return address;
    }

    public String getPdfId() {
        return pdfId;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public String getLocalization() {
        return localization;
    }

    public String getVariant() {
        return variant;
    }

    public boolean asPdfA() {
        return asPdfA;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public String getDocType() {
        return docType;
    }

    public Profile getProfile() {
        return profile;
    }

    protected void appendToString(StringBuilder sb) {
        if (this.pdfId == null) {
            sb.append(", symbol='").append(symbol).append('\'')
                    .append(", client=").append(client)
                    .append(", variant=").append(variant)
                    .append(", asPdfA=").append(asPdfA)
                    .append(", testMode=").append(testMode)
                    .append(", userInfo=").append(userInfo)
                    .append(", vwdId=").append(vwdId)
                    .append(", localization=").append(localization)
                    .append(", address='").append(address).append('\'')
                    .append(", docType='").append(docType).append('\'')
                    .append(", profile='").append(profile != null ? profile : "<none>").append('\'')
                    .append(", properties=").append(properties);
        }
        else {
            sb.append(", client=").append(client)
                    .append(", variant=").append(variant)
                    .append(", pdfId='").append(this.pdfId).append('\'');
        }
    }
}
