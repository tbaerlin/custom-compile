/*
 * PmByteDocument.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;
import de.marketmaker.iview.pmxml.EvalLayoutReportResponse;
import de.marketmaker.iview.pmxml.EvalLayoutResponse;
import org.jdom.JDOMException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class PmByteDocument extends EasytradeCommandController {

    public static class Command {
        private String key;

        @NotNull
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public enum ContentType {
        PDF("application/pdf"),
        PNG("image/png"),
        XLS("application/vnd.ms-excel");
        private final String contentType;

        private ContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getContentType() {
            return this.contentType;
        }
    }

    public PmByteDocument() {
        super(Command.class);
    }

    private PmxmlExchangeData pmxml;

    private ContentType contentType;

    private JaxbHandler jaxbHandler;

    public void setPmxml(PmxmlExchangeData pmxml) {
        this.pmxml = pmxml;
    }

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {

        final RequestContext oldContext = RequestContextHolder.getRequestContext();
        try {
            final PmByteReport.Command cmd = PmByteReport.Command.decode(((Command) o).getKey());

            final Locale locale = Locale.forLanguageTag(cmd.getLanguage());
            RequestContextHolder.setRequestContext(new RequestContext(null, Collections.<String, MarketStrategy>emptyMap())
                    .withLocales(Collections.singletonList(locale)));

            final String authToken = PmExchangeData.getAuthToken(request);
            final PmxmlExchangeDataRequest pmxmlRequest = PmExchangeData.createRequest(request, authToken, cmd);
            final PmxmlExchangeDataResponse pmxmlResponse = PmExchangeData.exchangeData(pmxmlRequest, this.pmxml, this.logger);
            final byte[] data = decodeResponse(pmxmlResponse);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(this.contentType.getContentType());
            response.setContentLength(data.length);
            response.getOutputStream().write(data);
            return null;
        } catch (Exception e) {
            this.logger.error("<doHandle> failed", e);
            throw new EvaluationException("failed", e);
        } finally {
            RequestContextHolder.setRequestContext(oldContext);
        }
    }

    private byte[] decodeResponse(PmxmlExchangeDataResponse response) throws JDOMException, IOException, InvalidResponseException {
        switch (this.contentType) {
            case PDF:
                final PmExchangeData.ResponseWrapper result = PmExchangeData.extractResponse(response, EvalLayoutResponse.class, false);
                final EvalLayoutReportResponse reportResponse = this.jaxbHandler.unmarshal(result.getXml(), EvalLayoutReportResponse.class);
                return reportResponse.getContent();
            case PNG:
                return PmByteUtil.getChart(this.jaxbHandler, response);
            case XLS:
                return PmByteUtil.getExcelSheet(this.jaxbHandler, response);
            default:
                throw new IllegalStateException("unknown contenttype: " + contentType);
        }
    }
}