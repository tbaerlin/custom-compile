/*
 * StkKursdaten.java
 *
 * Created on 07.07.2006 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;
import de.marketmaker.iview.pmxml.EvalLayoutChartRequest;
import de.marketmaker.iview.pmxml.EvalLayoutChartResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class PmByteReport extends EasytradeCommandController {

    public static class Command extends PmExchangeData.Command implements Serializable {
        protected static final long serialVersionUID = 1L;

        private String language;
        //timestamp makes url "unique" to avoid browser caching
        private String timestamp;

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public static long getSerialVersionUID() {
            return serialVersionUID;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String encode() throws Exception {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            final ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos));
            oos.writeObject(this);
            oos.close();
            return URLEncoder.encode(Base64.encodeBase64String(baos.toByteArray()), "UTF-8");
        }

        static Command decode(String s) throws Exception {
            final byte[] cmdBytes = Base64.decodeBase64(s);
            final ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(cmdBytes)));
            final Command result = (Command) ois.readObject();
            ois.close();
            return result;
        }
    }

    private PmxmlExchangeData pmxml;
    private PmByteDocument.ContentType contentType;
    private JaxbHandler jaxbHandler;

    public PmByteReport() {
        super(Command.class);
    }

    public void setContentType(PmByteDocument.ContentType contentType) {
        this.contentType = contentType;
    }

    public void setPmxml(PmxmlExchangeData pmxml) {
        this.pmxml = pmxml;
    }

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        cmd.setTimestamp(Long.toString(System.currentTimeMillis()));
        cmd.setLanguage(RequestContextHolder.getRequestContext().getLocale().getLanguage());
        try {
            final Map<String, Object> model = new HashMap<>();
            model.put("key", cmd.encode());
            model.put("type", this.contentType.toString().toLowerCase());
            if (this.contentType == PmByteDocument.ContentType.PNG) {
                final EvalLayoutChartRequest chartRequest = getLayoutChartRequest(cmd, request);
                final EvalLayoutChartResponse chartResponse = doPmRequest(cmd, request, chartRequest);
                model.put("title", getTitle(chartResponse));
                model.put("offset", getOffset(chartRequest));
                model.put("total", getTotal(chartResponse));
            }
            return new ModelAndView("pmbytereport", model);
        } catch (Exception e) {
            throw new EvaluationException("failed", e);
        }
    }

    private EvalLayoutChartRequest getLayoutChartRequest(Command cmd, HttpServletRequest request) {
        return this.jaxbHandler.unmarshal(cmd.getRequest(), EvalLayoutChartRequest.class);
    }

    private String getOffset(EvalLayoutChartRequest request) {
        final String offsetStr = request.getOffset();
        if (StringUtils.hasText(offsetStr)) {
            try {
                return Integer.toString(Integer.parseInt(offsetStr));
            } catch (NumberFormatException nfe) {
                /* do nothing ... return "0" */
            }
        }
        return "0";
    }

    private String getTitle(EvalLayoutChartResponse response) {
        if (response == null || response.getCharts().isEmpty()) {
            return "no chart available";
        }

        return response.getCharts().get(0).getTitle();
    }

    private String getTotal(EvalLayoutChartResponse response) {
        if (response == null) {
            return "0";
        }
        return response.getCompleteCount();
    }

    private EvalLayoutChartResponse doPmRequest(Command cmd, HttpServletRequest request, EvalLayoutChartRequest layoutChartRequest) throws EvaluationException {
        if (this.contentType != PmByteDocument.ContentType.PNG) {
            return null;
        }
        final String authToken = PmExchangeData.getAuthToken(request);
        layoutChartRequest.setCount("1");
        layoutChartRequest.setGenerateChart(false);
        final String xml = this.jaxbHandler.marshal(EvalLayoutChartRequest.class, layoutChartRequest, "parameter");

        final PmxmlExchangeDataRequest pmxmlRequest = new PmxmlExchangeDataRequest(authToken, xml.getBytes(Charset.forName("UTF-8")), "EvalLayout_EvaluateChart");
        final PmxmlExchangeDataResponse pmxmlResponse;
        try {
            pmxmlResponse = PmExchangeData.exchangeData(pmxmlRequest, this.pmxml, this.logger);
            PmExchangeData.ResponseWrapper result = PmExchangeData.extractResponse(pmxmlResponse, cmd);
            return this.jaxbHandler.unmarshal(result.getRawXml(), EvalLayoutChartResponse.class);
        } catch (PmxmlException e) {
            this.logger.error("<doPmRequest> failed", e);
            request.getSession().invalidate();
            throw new EvaluationException(e.getMessage(), e);
        }
    }
}