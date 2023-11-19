package de.marketmaker.istar.merger.web.easytrade.misc;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domain.special.DzBankRecord;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataProvider;
import de.marketmaker.istar.merger.provider.DzBankRecordProvider;
import de.marketmaker.istar.merger.provider.gis.DynamicGisReportRequest;
import de.marketmaker.istar.merger.provider.gis.DynamicGisReportResponse;
import de.marketmaker.istar.merger.provider.gis.GisDocumentType;
import de.marketmaker.istar.merger.provider.gis.GisProductType;
import de.marketmaker.istar.merger.provider.gis.GisServiceProvider;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.UserMasterData;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataRequest;
import de.marketmaker.istar.domainimpl.profile.UserMasterDataResponse;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DynamicPibController extends AbstractController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String BASE_URL = "pibAdaptor/ePib.pdf";

    public static final String WKN_PARAM_KEY = "wkn";

    public static final String ISIN_PARAM_KEY = "isin";

    /**
     * A document Id used and given by the Geno-Interface in the PibAdapter
     */
    public static final String DOCUMENT_GUID_PARAM_KEY = "docGUID";

    public static final String MARGIN_PARAM_KEY = "margin";

    public static final String PRODUCT_TYP_PARAM_KEY = "typ";

    public static final String DOCUMENT_TYPE_PARAM_KEY = "docType";

    public static final String CREDENTIAL_PARAM_KEY = "credential";

    private UserMasterDataProvider userMasterDataProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private DzBankRecordProvider dzProvider;

    private GisServiceProvider gisServiceProvider;

    public void setUserMasterDataProvider(UserMasterDataProvider userMasterDataProvider) {
        this.userMasterDataProvider = userMasterDataProvider;
    }

    public void setGisServiceProvider(GisServiceProvider gisServiceProvider) {
        this.gisServiceProvider = gisServiceProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setDzBankRecordProvider(DzBankRecordProvider dzProvider) {
        this.dzProvider = dzProvider;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {

        DynamicGisReportRequest gisReportRequest = null;
        try  {
            final RequestContext requestContext = RequestContextHolder.getRequestContext();
            final Profile profile = requestContext.getProfile();

            checkAuthorization(profile);

            final UserMasterDataResponse dataResponse;
            if (profile instanceof VwdProfile) {
                final UserMasterDataRequest userDataRequest = UserMasterDataRequest.forVwdId(((VwdProfile)profile).getVwdId());
                userDataRequest.setAppId(((VwdProfile)profile).getAppId());
                dataResponse = userMasterDataProvider.getUserMasterData(userDataRequest);
            } else {
                logger.error("no vwd profile");
                dataResponse = UserMasterDataResponse.createInvalid();
            }

            if (!dataResponse.isValid()) {
                logger.error("invalid user data for profile: " + profile);
                throw new IllegalArgumentException("invalid user data for profile: " + profile);
            }

            final UserMasterData userdata = dataResponse.getMasterData();
            final String genoId = userdata.getGenoId();
            final String blz = userdata.attributeText("Mandator[@id='10']/Masterdata/Data[@type='111']/@value");
            final String margin = findParameter(MARGIN_PARAM_KEY, request);

            final String wkn = findParameter(WKN_PARAM_KEY, request);
            final String isin = findParameter(ISIN_PARAM_KEY, request);
            final String guid = findParameter(DOCUMENT_GUID_PARAM_KEY, request);

            final boolean hasSymbol = StringUtils.hasLength(wkn) || StringUtils.hasLength(isin);
            if (hasSymbol) {
                GisProductType gisProductType = null;
                String productTypeName = findParameter(PRODUCT_TYP_PARAM_KEY, request);
                if (StringUtils.hasLength(productTypeName)) {
                    gisProductType = GisProductType.resolve(productTypeName);
                } else {
                    productTypeName = getProductTyp(wkn);
                    gisProductType = GisProductType.resolve(productTypeName);
                }
                GisDocumentType gisDocumentType = null;
                String documentTypeName = findParameter(DOCUMENT_TYPE_PARAM_KEY, request);
                if (StringUtils.hasLength(documentTypeName)) {
                    try {
                        gisDocumentType = GisDocumentType.valueOf(documentTypeName);
                    } catch (Exception e) {
                        logger.warn("<handleRequestInternal> documentTypeName " + documentTypeName + " is no enum member of GisDocumentType");
                        gisDocumentType = GisDocumentType.DEFAULT;
                    }
                }
                gisReportRequest = new DynamicGisReportRequest(wkn, isin, margin, genoId, blz, gisProductType, gisDocumentType);
            } else if (StringUtils.hasLength(guid)) {
                gisReportRequest = new DynamicGisReportRequest(guid, genoId, blz);
            } else {
                throw new IllegalArgumentException("wkn, isin or guid has to be set");
            }

            final DynamicGisReportResponse gisReportResponse = this.gisServiceProvider.fetchReport(gisReportRequest);
            if (gisReportResponse == null) {
                deliverError(request, response, 0, "gisReportResponse is null");
                return null;
            }
            final int errorCode = gisReportResponse.getErrorCode();
            if (errorCode > 0) {
                deliverError(request, response, errorCode, gisReportResponse.getErrorMessage());
                return null;
            }

            if (gisReportResponse.getContent() == null) {
                deliverError(request, response, 0, "gisReportResponse.content is null");
                return null;
            }

            deliverResponse(response, gisReportResponse);
        } catch (RemoteAccessException ex) {
            logger.info("RemoteAccessException: can't create pib, gisReportRequest was: " + gisReportRequest
                    + " exception message is: '" + ex.getMessage() + "'");
            deliverError(request, response, 0, "Serviceaufruf war nicht erfolgreich. Bitte versuchen Sie es erneut.");
        } catch (Exception ex) {
            logger.error("can't create pib, gisReportRequest was: " + gisReportRequest, ex);
            deliverError(request, response, 0, "Serviceaufruf war nicht erfolgreich. Bitte versuchen Sie es erneut.");
        }
        return null;
    }

    private void deliverError(HttpServletRequest request, HttpServletResponse response,
                              int code, String message) {
        final String codedMessage = "Bei der Verarbeitung ist ein Fehler aufgetreten: "
                + message + (code == 0 ? "" : (" [" + code + "]"));
        if ("marketmanager".equalsIgnoreCase(getZoneName(request))) {
            deliverErrorPdf(response, codedMessage);
        } else {
            deliverErrorHtml(response, codedMessage);
        }
    }

    private void checkAuthorization(Profile profile) throws IllegalAccessException {
        if (profile != null && !profile.isAllowed(Selector.PRODUCT_WITH_PIB)) {
            throw new PermissionDeniedException(Selector.PRODUCT_WITH_PIB);
        }
    }

    private String getZoneName(HttpServletRequest request) {
        final Zone zone = (Zone) (request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE));
        return (zone != null) ? zone.getName() : null;
    }

    private void deliverResponse(HttpServletResponse response, DynamicGisReportResponse gisReportResponse) {
        response.setHeader("Content-Disposition", MessageFormat.format("attachment; filename=\"{0}\"", gisReportResponse.getFilename()));
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setContentType("application/pdf");

        try {
            final BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            outputStream.write(gisReportResponse.getContent());
            outputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deliverErrorHtml(HttpServletResponse response, String message) {
        response.setContentType("text/html");
        try {
            final PrintWriter out = response.getWriter();
            out.print(new MessageFormat(SIMPLE_HTML).format(new Object[]{message}));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deliverErrorPdf(HttpServletResponse response, String message) {
        final FopFactory fopFactory = FopFactory.newInstance();
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"error.pdf\"");
        try {
            final Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, response.getOutputStream());
            final TransformerFactory factory = TransformerFactory.newInstance();
            final Transformer transformer = factory.newTransformer(); // identity transformer
            final Source src = new StreamSource(new StringReader(new MessageFormat(SIMPLE_FOP).format(new Object[] {message})));
            final Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String findParameter(String key, HttpServletRequest request) {
        final Map<String, String[]> parameters = request.getParameterMap();

        final String[] values = parameters.get(key);
        String value = null;
        if (values != null && values.length == 1) {
            value = values[0];
        }
        return value;
    }

    private static final Map<String, String> OFFERTENKATEGORIE_2_SOAP_MAP = new HashMap<String, String>() {{
        put("Renten", GisProductType.Zinsprodukt.name());
        put("Aktienanleihen", GisProductType.Flowprodukt.name());
        put("Zertifikate", GisProductType.Flowprodukt.name());
    }};

    public String getProductTyp(String wkn) {
        String typ;
        final Instrument instrument = this.instrumentProvider.identifyByIsinOrWkn(wkn);
        final long iid = instrument.getId();
        final List<DzBankRecord> records = dzProvider.getDzBankRecords(Collections.singletonList(iid));
        final DzBankRecord record = records.get(0);
        final String offertenkategorie = record.getOffertenkategorie();
        if (OFFERTENKATEGORIE_2_SOAP_MAP.containsKey(offertenkategorie)) {
            typ = OFFERTENKATEGORIE_2_SOAP_MAP.get(offertenkategorie);
        } else {
            logger.error("can't resolve type paramter for offertenkategorie '" + offertenkategorie
                    + "', using 'Zeichnungsprodukt' as fallback");
            typ = GisProductType.DEFAULT.name();
        }
        return typ;
    }

    public static class PibAdaptorDownloadLink {
        private String margin;
        private String wkn;
        private String encodedCredentials;
        private String guid;
        private GisDocumentType documentType;
        private GisProductType productType;
        private String isin;

        public void setMargin(String margin) {
            this.margin = margin;
        }

        public void setWkn(String wkn) {
            this.wkn = wkn;
        }

        public void setIsin(String isin) {
            this.isin = isin;
        }

        @Deprecated
        public void setTyp(String typ) {
            if (typ == null || "".equals(typ)) {
                this.productType = null;
            }
            GisProductType gisProductType = GisProductType.valueOf(typ);
            if (gisProductType != null) {
                this.productType = gisProductType;
            } else {
                throw new IllegalArgumentException("Typ must be one of GisProductType: typ = " + typ + " is not in GisProductType!");
            }
        }

        public void setProductType(GisProductType productType) {
            this.productType = productType;
        }

        public void setEncodedCredentials(String encodedCredentials) {
            this.encodedCredentials = encodedCredentials;
        }

        public void setDocumentType(GisDocumentType documentType) {
            this.documentType = documentType;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public String asString() {
            String result = "";
            result += BASE_URL + "?";

            if (wkn != null) {
                try {
                    result += WKN_PARAM_KEY + "=";
                    result += URLEncoder.encode(wkn, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += wkn;
                }
            }
            if (isin != null) {
                if (wkn != null) {
                    result += "&";
                }
                try {
                    result += ISIN_PARAM_KEY + "=";
                    result += URLEncoder.encode(isin, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += isin;
                }
            }
            if ((wkn == null) && (isin == null) && (guid != null)) {
                try {
                    result += DOCUMENT_GUID_PARAM_KEY + "=";
                    result += URLEncoder.encode(guid, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += guid;
                }
            }

            if (encodedCredentials != null) {
                try {
                    result += "&" + CREDENTIAL_PARAM_KEY + "=";
                    result += URLEncoder.encode(encodedCredentials, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += encodedCredentials;
                }
            } else {
                throw new IllegalArgumentException("need to specify encodedCredentials for a valid link");
            }

            if (margin != null) {
                try {
                    result += "&" + MARGIN_PARAM_KEY + "=";
                    result += URLEncoder.encode(margin, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += margin;
                }

            }

            if (productType != null) {
                try {
                    result += "&" + PRODUCT_TYP_PARAM_KEY + "=";
                    result += URLEncoder.encode(productType.name(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += productType.name();
                }
            }

            if (documentType != null) {
                try {
                    result += "&" + DOCUMENT_TYPE_PARAM_KEY + "=";
                    result += URLEncoder.encode(documentType.name(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    result += documentType.name();
                }
            }

            return result;
        }
    }

    private static final String SIMPLE_FOP = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
            + "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\"> \n"
            + "<fo:layout-master-set>\n"
            + "<fo:simple-page-master margin-right=\"2cm\" margin-left=\"2cm\" margin-bottom=\"1cm\" margin-top=\"0.5cm\" font-family=\"sans-serif\" page-width=\"21cm\" page-height=\"29.7cm\" master-name=\"main\">\n"
            + "<fo:region-body margin-bottom=\"1cm\" margin-top=\"4.0cm\"/>\n"
            + "<fo:region-before extent=\"1.5cm\"/>\n"
            + "</fo:simple-page-master>\n"
            + "</fo:layout-master-set>\n"
            + "<fo:page-sequence master-reference=\"main\">\n"
            + "<fo:flow flow-name=\"xsl-region-body\">\n"
            + "<fo:block>{0}</fo:block>\n"
            + "</fo:flow>\n"
            + "</fo:page-sequence>\n"
            + "</fo:root>";

    private static final String SIMPLE_HTML = ""
            + "<!DOCTYPE HTML>"
            + "<html>"
            + "<head>"
            + "<title>{0}</title>"
            + "</head>"
            + "<body>"
            + "{0}"
            + "</body>"
            + "</html>";
}
