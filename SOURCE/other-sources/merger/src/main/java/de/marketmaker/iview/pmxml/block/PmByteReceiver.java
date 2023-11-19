/*
 * PmByteReceiver.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import de.marketmaker.iview.pmxml.GetSecurityDocumentsResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.JDOMException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;
import de.marketmaker.iview.pmxml.AsyncLoadDocumentResponse;
import de.marketmaker.iview.pmxml.DMSGetDocumentResponse;
import de.marketmaker.iview.pmxml.DMSHandleRequest;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.EvalLayoutChartResponse;
import de.marketmaker.iview.pmxml.EvalLayoutReportResponse;
import de.marketmaker.iview.pmxml.EvalLayoutResponse;
import de.marketmaker.iview.pmxml.EvalLayoutTableResponse;
import de.marketmaker.iview.pmxml.HandleRequest;

/**
 * @author Michael Lösch
 * @author mdick 
 */
@MmInternal
public class PmByteReceiver extends EasytradeCommandController {
    public static class Command {
        private String handle;

        @NotNull
        public String getHandle() {
            return handle;
        }

        public void setHandle(String handle) {
            this.handle = handle;
        }
    }

    private PmxmlExchangeData pmxml;

    private JaxbHandler jaxbHandler;

    private PmByteReceiverStrategy strategy = PmByteReceiverStrategy.EVALUATE_LAYOUT;

    public PmByteReceiver() {
        super(Command.class);
    }

    public void setPmxml(PmxmlExchangeData pmxml) {
        this.pmxml = pmxml;
    }

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    public void setStrategy(PmByteReceiverStrategy strategy) {
        this.strategy = strategy;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {

        final RequestContext oldContext = RequestContextHolder.getRequestContext();
        final Command cmd = (Command) o;
        try {

            RequestContextHolder.setRequestContext(new RequestContext(null, Collections.emptyMap())
                    .withLocales(Collections.singletonList(RequestContextUtils.getLocale(request))));

            final String authToken = PmExchangeData.getAuthToken(request);
            final PmxmlExchangeDataRequest pmxmlRequest = this.strategy.createRequest(cmd, authToken, this.jaxbHandler);

            final PmxmlExchangeDataResponse pmxmlResponse = PmExchangeData.exchangeData(pmxmlRequest, this.pmxml, this.logger);
            final byte[] data = this.strategy.decodeResponse(cmd.getHandle(), pmxmlResponse, response, this.jaxbHandler);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLength(data.length);
            response.getOutputStream().write(data);
            return null;
        }
        catch (PmResourceForbiddenException | PmResourceHasNoContentException pme) {
            throw pme;
        }
        catch (Exception e) {
            this.logger.error("<doHandle> failed for " + cmd.getHandle(), e);
            throw new EvaluationException("failed for " + cmd.getHandle(), e);
        }
        finally {
            RequestContextHolder.setRequestContext(oldContext);
        }
    }

    @Override
    protected ModelAndView createErrorModel(HttpServletRequest request, HttpServletResponse response, Object cmd, BindException bindException) throws Exception {
        final DefaultMessageSourceResolvable o = bindException.getAllErrors().get(0);

        if (PmResourceForbiddenException.CODE.equals(o.getCode())) {
            sendError(HttpServletResponse.SC_FORBIDDEN, o, response);
            return null;
        }
        if (PmResourceHasNoContentException.CODE.equals(o.getCode())) {
            sendError(HttpServletResponse.SC_NOT_FOUND, o, response);
            return null;
        }

        return super.createErrorModel(request, response, cmd, bindException);
    }

    private void sendError(int httpStatusCode, DefaultMessageSourceResolvable o, HttpServletResponse response) throws IOException {
        response.reset();
        response.sendError(httpStatusCode, o.getDefaultMessage());
    }

    public enum PmByteReceiverStrategy {
        EVALUATE_LAYOUT {
            private static final String PM_EVAL_LAYOUT_FUNCTION_KEY = "Async_GetResponse_EvalLayout_Evaluate";

            @Override
            public PmxmlExchangeDataRequest createRequest(Command cmd, String authToken, JaxbHandler jaxbHandler) {
                final HandleRequest handleRequest = new HandleRequest();
                handleRequest.setHandle(cmd.getHandle());

                return new PmxmlExchangeDataRequest(authToken,
                        PmExchangeData.parameterToPmNotation(
                                jaxbHandler.marshal(HandleRequest.class, handleRequest, "parameter")).getBytes(Charset.forName("UTF-8")),
                        PM_EVAL_LAYOUT_FUNCTION_KEY);
            }

            @Override
            public byte[] decodeResponse(String handle, PmxmlExchangeDataResponse pmxmlResponse, HttpServletResponse response, JaxbHandler jaxbHandler) throws IOException, InvalidResponseException {
                byte[] data = pmxmlResponse.getData();
                if(ObjectUtils.isEmpty(data)) {
                    throw new PmResourceHasNoContentException("The requested resource with handle \"" + handle + "\" has not content or is not available", handle);
                }
                final PmExchangeData.ResponseWrapper result = PmExchangeData.extractResponse(pmxmlResponse, EvalLayoutResponse.class);
                final String resultType = result.getType();
                if (EvalLayoutReportResponse.class.getSimpleName().equals(resultType)) {
                    response.setHeader("Content-Disposition", "inline;filename=report.pdf");
                    response.setContentType("application/pdf");
                    return PmByteUtil.getPdf(jaxbHandler, pmxmlResponse);
                }
                else if (EvalLayoutChartResponse.class.getSimpleName().equals(resultType)) {
                    response.setHeader("Content-Disposition", "inline;filename=report.png");
                    response.setContentType("image/png");
                    return PmByteUtil.getChart(jaxbHandler, pmxmlResponse);
                }
                else if (EvalLayoutTableResponse.class.getSimpleName().equals(resultType)) {
                    response.setHeader("Content-Disposition", "inline;filename=report.xls");
                    response.setContentType("application/vnd.ms-excel");
                    return PmByteUtil.getExcelSheet(jaxbHandler, pmxmlResponse);
                }
                else {
                    throw new IllegalStateException("unhandled response type: " + resultType);
                }
            }
        },

        DMS {
            private static final String PM_DMS_FUNCTION_KEY = "DMS_GetDocument";

            @Override
            public PmxmlExchangeDataRequest createRequest(Command cmd, String authToken, JaxbHandler jaxbHandler) {
                final DMSHandleRequest handleRequest = new DMSHandleRequest();
                handleRequest.setHandle(cmd.getHandle());

                return new PmxmlExchangeDataRequest(authToken,
                        PmExchangeData.parameterToPmNotation(
                                jaxbHandler.marshal(DMSHandleRequest.class, handleRequest, "parameter")).getBytes(Charset.forName("UTF-8")),
                        PM_DMS_FUNCTION_KEY);
            }

            @Override
            public byte[] decodeResponse(String handle, PmxmlExchangeDataResponse pmxmlResponse, HttpServletResponse response, JaxbHandler jaxbHandler) {
                final PmExchangeData.ResponseWrapper result = PmExchangeData.extractResponse(pmxmlResponse, DMSGetDocumentResponse.class, false);
                final String resultType = result.getType();
                if (DMSGetDocumentResponse.class.getSimpleName().equals(resultType)) {
                    final DMSGetDocumentResponse res = jaxbHandler.unmarshal(
                            result.getRawXml(), DMSGetDocumentResponse.class
                    );
                    if (res.isInsufficientRights()) {
                        throw new PmResourceForbiddenException("Insufficient rights to access document with handle \"" + handle + "\"", handle);
                    }
                    final String fileType = res.getFileType();
                    final String fileName = handle.trim() + "." + fileType;
                    final String mimeType = getMimeTypeOrDefault(fileName, APPLICATION_OCTET_STREAM_VALUE);
                    response.setHeader("Content-Disposition", "inline;filename=" + fileName);
                    response.setContentType(mimeType);
                    return res.getData();
                }
                else {
                    throw new IllegalStateException("unhandled response type: " + resultType);
                }
            }
        },

        ACTIVITY_ATTACHMENT {
            private static final String PM_ASYNC_LOAD_DOCUMENT_FUNCTION_KEY = "Async_LoadDocument";

            @Override
            public PmxmlExchangeDataRequest createRequest(Command cmd, String authToken, JaxbHandler jaxbHandler) {
                final HandleRequest handleRequest = new HandleRequest();
                handleRequest.setHandle(cmd.getHandle());

                return new PmxmlExchangeDataRequest(authToken,
                        PmExchangeData.parameterToPmNotation(
                                jaxbHandler.marshal(HandleRequest.class, handleRequest, "parameter")).getBytes(Charset.forName("UTF-8")),
                        PM_ASYNC_LOAD_DOCUMENT_FUNCTION_KEY);
            }

            @Override
            public byte[] decodeResponse(String handle, PmxmlExchangeDataResponse pmxmlResponse, HttpServletResponse response, JaxbHandler jaxbHandler) throws IOException {
                final PmExchangeData.ResponseWrapper result = PmExchangeData.extractResponse(pmxmlResponse, AsyncLoadDocumentResponse.class, false);
                final String resultType = result.getType();
                if (AsyncLoadDocumentResponse.class.getSimpleName().equals(resultType)) {
                    final AsyncLoadDocumentResponse res = jaxbHandler.unmarshal(
                            result.getRawXml(), AsyncLoadDocumentResponse.class
                    );
                    // RFC 2183 states in section 2.3 (Content-Disposition) that the filename may only contain US-ASCII
                    // characters. This old-fashioned behaviour is nowadays only active in IE8.
                    // IEs > 8 and modern browsers implement RFC 2231, which allows to specify the character encoding
                    // and language, which is indicated by an asterisk at the end of the parameter name. Character
                    // encoding and language are separated from the parameter value by single quotes. Both are optional,
                    // but if the asterisk is set, the single quotes must be present.
                    // RFC 6266 states that for compatibility reasons both parameters (with and without asterisk) may be
                    // provided. If RFC2231 is implemented, then the parameter with asterisk should have precedence.
                    // See also:
                    // http://tools.ietf.org/html/rfc2231 Sec. 4 Parameter Value Character Set and Language Information
                    // http://tools.ietf.org/html/rfc6266#section-4.3 Sec. 4.3 Disposition Parameter: 'Filename'
                    final DocumentMetadata docMetaData = res.getDocMetaData();
                    final String fileName = docMetaData.getDocumentName() + "." + docMetaData.getFileType();
                    final String fileNameUSASCII = URLEncoder.encode(fileName, "US-ASCII");
                    final String fileNameUTF8 = URLEncoder.encode(fileName, "UTF-8");
                    final String mimeType = getMimeTypeOrDefault(fileNameUTF8, APPLICATION_OCTET_STREAM_VALUE);
                    response.setHeader("Content-Disposition", "inline;filename=\"" + fileNameUSASCII +"\";filename*=utf-8''" + fileNameUTF8);
                    response.setContentType(mimeType);
                    return res.getContent();
                }
                else {
                    throw new IllegalStateException("unhandled response type: " + resultType);
                }
            }
        },

        SECURITY_DOCUMENTS {
            private static final String PM_SECURITY_DOCUMENTS_FUNCTION_KEY = "Async_GetResponse_Docman_GetSecurityDocuments";

            @Override
            public PmxmlExchangeDataRequest createRequest(Command cmd, String authToken, JaxbHandler jaxbHandler) {
                final HandleRequest handleRequest = new HandleRequest();
                handleRequest.setHandle(cmd.getHandle());

                return new PmxmlExchangeDataRequest(authToken,
                    PmExchangeData.parameterToPmNotation(
                        jaxbHandler.marshal(HandleRequest.class, handleRequest, "parameter")).getBytes(Charset.forName("UTF-8")),
                    PM_SECURITY_DOCUMENTS_FUNCTION_KEY);
            }

            @Override
            public byte[] decodeResponse(String handle, PmxmlExchangeDataResponse pmxmlResponse, HttpServletResponse response, JaxbHandler jaxbHandler) {
                byte[] data = pmxmlResponse.getData();
                if(ObjectUtils.isEmpty(data)) {
                    throw new PmResourceHasNoContentException("The requested resource with handle \"" + handle + "\" has not content or is not available", handle);
                }
                final PmExchangeData.ResponseWrapper result = PmExchangeData.extractResponse(pmxmlResponse, GetSecurityDocumentsResponse.class);
                final String resultType = result.getType();

                if (GetSecurityDocumentsResponse.class.getSimpleName().equals(resultType)) {
                    final GetSecurityDocumentsResponse res = jaxbHandler.unmarshal(
                        result.getRawXml(), GetSecurityDocumentsResponse.class
                    );
                    final String fileType = res.getFileType();
                    final String fileName = String.format("security-documents-%s.%s", handle, fileType);
                    final String mimeType = getMimeTypeOrDefault(fileName, APPLICATION_OCTET_STREAM_VALUE);
                    response.setHeader("Content-Disposition", String.format("inline;filename=%s", fileName));
                    response.setContentType(mimeType);
                    return res.getMergedDocument();
                }
                else {
                    throw new IllegalStateException("unhandled response type: " + resultType);
                }
            }
        };

        private static String getMimeTypeOrDefault(String fileName, String defaultMimeType) {
            final HttpServletRequest request = ((ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
            final String mimeType = request.getServletContext().getMimeType(fileName);
            if(!StringUtils.hasText(mimeType)) {
                return defaultMimeType;
            }
            return mimeType;
        }

        public abstract PmxmlExchangeDataRequest createRequest(Command cmd, String authToken, JaxbHandler jaxbHandler);

        public abstract byte[] decodeResponse(String handle, PmxmlExchangeDataResponse pmxmlResponse, HttpServletResponse response, JaxbHandler jaxbHandler) throws JDOMException, IOException, InvalidResponseException;
    }

    static class PmResourceForbiddenException extends MergerException {
        public static final String CODE = "pm.resourceForbidden";

        PmResourceForbiddenException(String message, String handle) {
            super(message, handle);
        }

        @Override
        public String getCode() {
            return CODE;
        }
    }

    static class PmResourceHasNoContentException extends MergerException {
        public static final String CODE = "pm.resourceHasNoContent";

        PmResourceHasNoContentException(String message, String handle) {
            super(message, handle);
        }

        @Override
        public String getCode() {
            return CODE;
        }
    }
}
