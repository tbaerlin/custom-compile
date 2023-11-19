/*
 * PmWebExchangeData.java
 *
 * Created on 23.08.2012 10:28:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.amqp.ServiceProviderSelection;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.easytrade.Error;
import de.marketmaker.istar.merger.web.easytrade.Period;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.itools.amqprpc.impl.RemoteAccessTimeoutException;
import de.marketmaker.itools.pmxml.frontend.DeactivatedModuleException;
import de.marketmaker.itools.pmxml.frontend.InvalidSessionException;
import de.marketmaker.itools.pmxml.frontend.PmxmlException;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeData;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataRequest;
import de.marketmaker.itools.pmxml.frontend.PmxmlExchangeDataResponse;

/**
 * Use {@link PmExchangeData} if you need to work with xml (e.g. blocks).
 * If you need to work with subclasses of ExchangeDataRequest/ExchangeDataResponse use {@link de.marketmaker.iview.pmxml.PmxmlExchangeDataImpl}.
 *
 * @author Michael Lösch
 */
@MmInternal
public class PmExchangeData extends EasytradeCommandController {

    public static final String SERVER_ID = "serverId";

    public static class Command implements Serializable {
        private String request;
        private String functionKey;
        private Class responseType;
        private Class requestType;
        private boolean serverBinding = false;
        private PmExchangeDataLogger exchangeDataLogger = PmExchangeDataLogger.SILENT;
        private String ttl = "PT1M";

        @NotNull
        @Period
        public String getTtl() {
            return this.ttl;
        }

        public void setTtl(String ttl) {
            this.ttl = ttl;
        }

        public PmExchangeDataLogger getExchangeDataLogger() {
            return this.exchangeDataLogger;
        }

        public void setExchangeDataLogger(PmExchangeDataLogger exchangeDataLogger) {
            this.exchangeDataLogger = exchangeDataLogger;
        }

        public void setResponseType(Class responseType) {
            this.responseType = responseType;
        }

        public void setRequest(String request) {
            this.request = request;
        }

        public void setFunctionKey(String functionKey) {
            this.functionKey = functionKey;
        }

        public void setRequestType(Class requestType) {
            this.requestType = requestType;
        }

        public boolean isServerBinding() {
            return serverBinding;
        }

        public void setServerBinding(boolean serverBinding) {
            this.serverBinding = serverBinding;
        }

        @NotNull
        public Class getRequestType() {
            return requestType;
        }

        @NotNull
        public Class getResponseType() {
            return responseType;
        }

        @NotNull
        public String getRequest() {
            return request;
        }

        @NotNull
        public String getFunctionKey() {
            return functionKey;
        }
    }

    static class ResponseWrapper {
        private final String xml;
        private final String rawXml;
        private final String type;
        private final String namespace;

        ResponseWrapper(String type, String namespace, String xml, String rawXml) {
            this.type = type;
            this.xml = xml;
            this.namespace = namespace;
            this.rawXml = rawXml;
        }

        public String getRawXml() {
            return this.rawXml;
        }

        public String getXml() {
            return xml;
        }

        public String getType() {
            return type;
        }

        public String getNamespace() {
            return namespace;
        }
    }

    protected PmxmlExchangeData pmxml;
    private PmLoginInterceptor loginInterceptor;

    //template is defined in istar!
    protected String template = "pmexchangedata";

    private static final Pattern responseTypePattern = Pattern.compile("^<[^:]+:answer[^>]*xsi:type=\"([^:]+):([^\"]*)\".*");

    public PmExchangeData() {
        super(Command.class);
    }

    public void setLoginInterceptor(PmLoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    public void setPmxml(PmxmlExchangeData pmxml) {
        this.pmxml = pmxml;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws JDOMException, IOException, PmxmlException {
        final TimeTaker tt = new TimeTaker();
        final Command cmd = (Command) o;
        final PmExchangeDataLogger edl = cmd.getExchangeDataLogger();

        String authToken = getAuthToken(request);

        if (!StringUtils.hasText(authToken) && this.loginInterceptor != null) {
            authToken = this.loginInterceptor.doLogin(request.getSession()).getSessionToken();
        }

        final PmxmlExchangeDataRequest pmxmlRequest = createRequest(request, authToken, cmd);
        final PmxmlExchangeDataResponse pmxmlResponse;
        try {
            pmxmlResponse = exchangeData(pmxmlRequest, this.pmxml, this.logger, edl);
        }
        catch (InvalidSessionException ie) {
            logger.warn("<doHandle> e.getCause() instanceof InvalidSessionException, so invalidate tomcat session!");
            request.getSession().invalidate();
            throw ie;
        }
        catch (RemoteAccessTimeoutException te) {
            return createErrorModelAndView(o, "remote_timeout", te.getMessage());
        }
        final ResponseWrapper result = extractResponse(pmxmlResponse, cmd);
        final ModelAndView modelAndView = createModelAndView(cmd, new HashMap<String, Object>(), result);
        edl.log(getClass().getSimpleName() + " <doHandle> " + pmxmlRequest.getFunctionKey() + " time elapsed during whole doHandle(): " + tt);
        return modelAndView;
    }

    private static ModelAndView createErrorModelAndView(Object cmd, final String code, final String description) {
        final Map<String, Object> m = new HashMap<>();
        m.put("error", Error.error(code, description));
        m.put("command", cmd);
        return new ModelAndView("error", m);
    }

    static String getAuthToken(HttpServletRequest request) {
        return (String) request.getSession().getAttribute(ProfileResolver.PM_AUTHENTICATION_KEY);
    }

    static ResponseWrapper extractResponse(PmxmlExchangeDataResponse pmxmlResponse, Command cmd) {
        return extractResponse(pmxmlResponse, cmd.getResponseType());
    }

    static ResponseWrapper extractResponse(PmxmlExchangeDataResponse pmxmlResponse, Class responseType) {
        return extractResponse(pmxmlResponse, responseType, true);
    }

    static ResponseWrapper extractResponse(PmxmlExchangeDataResponse pmxmlResponse, Class responseType, boolean removeResponseTag) {
        final String xmlString = new String(pmxmlResponse.getData(), Charset.forName("UTF-8"));
        final String xml = removeXmlHeader(xmlString);
        final String typeName = checkAndGetTypeName(xml, responseType, responseTypePattern);
        final String namespace = getNamespace(xml, responseTypePattern);
        return new ResponseWrapper(typeName, namespace, removeResponseTag ? removePmResponseTag(xml, namespace) : xml, xmlString);
    }

    public static PmxmlExchangeDataResponse exchangeData(PmxmlExchangeDataRequest request, PmxmlExchangeData pmxml,
                                                         Logger logger, PmExchangeDataLogger edl) throws PmxmlException, RemoteAccessTimeoutException {
        final String xmlString = new String(request.getData(), Charset.forName("UTF-8"));
        final int requestHash = xmlString.hashCode();
        if (edl == null) {
            edl = PmExchangeDataLogger.SILENT;
        }
        edl.log("xml request for pmxml.exchangeData(function: " + request.getFunctionKey()
                + " / requestid: " + requestHash + " / user: " + request.getAuthToken() + "): "
                + xmlString);
        final TimeTaker tt = new TimeTaker();
        final PmxmlExchangeDataResponse response;
        try {
            response = pmxml.exchangeData(request);
            edl.log("elapsed time for pmxml.exchangeData(function: " + request.getFunctionKey()
                    + " / requestid: " + requestHash + " / user: " + request.getAuthToken() + "): " + tt);
            edl.log("xml response for pmxml.exchangeData(function: " + request.getFunctionKey()
                    + " / requestid: " + requestHash + " / user: " + request.getAuthToken() + "): "
                    + (response == null ? "null" : new String(response.getData(), Charset.forName("UTF-8"))));
        }
        catch (Exception e) {
            logger.error("<exchangeData> failed", e);
            if (e instanceof RemoteAccessTimeoutException) {
                throw e;
            }
            final Throwable cause = e.getCause();
            if (cause instanceof DeactivatedModuleException) {
                logger.error("<exchangeData> module deactivated!");
                throw new IllegalStateException(cause);
            }
            if (cause instanceof InvalidSessionException) {
                logger.debug("<exchangeData> invalid pm session exception!");
                throw (InvalidSessionException) cause;
            }
            throw e instanceof RuntimeException
                    ? (RuntimeException) e
                    : new IllegalStateException("unhandled exception!", e);
        }
        return response;
    }

    static PmxmlExchangeDataResponse exchangeData(PmxmlExchangeDataRequest request, PmxmlExchangeData pmxml, Logger logger) throws PmxmlException {
        return exchangeData(request, pmxml, logger, PmExchangeDataLogger.SILENT);
    }

    static PmxmlExchangeDataRequest createRequest(HttpServletRequest request, String authToken, Command cmd) {
        return createRequest(request.getSession(), authToken, cmd);
    }

    static PmxmlExchangeDataRequest createRequest(HttpSession session, String authToken, Command cmd) {
        if (cmd.isServerBinding()) {
            bindToServer(session);
        }
        return new PmxmlExchangeDataRequest(authToken,
                parameterToPmNotation(cmd.getRequest()).getBytes(Charset.forName("UTF-8")),
                cmd.getFunctionKey());
    }

    //Also used in mmgwt:PmUserLoginMethod
    public static void bindToServer(HttpSession session) {
        final String serverId = (String) session.getAttribute("serverId");
        if (StringUtils.hasText(serverId)) {
            ServiceProviderSelection.ID_FOR_NEXT_SEND.set(serverId);
        }
    }

    protected ModelAndView createModelAndView(Command cmd, Map<String, Object> model, ResponseWrapper response) {
        model.put("data", response.getXml());
        model.put("xsitype", response.getType());
        model.put("namespace", response.getNamespace());
        model.put("ttl", cmd.getTtl());
        return new ModelAndView(this.template, model);
    }

    private static String removePmResponseTag(String xml, String namespace) {
        final String open = "^<" + namespace + ":answer[^>]*>";
        final String close = "</" + namespace + ":answer>$";
        return xml.replaceFirst(open, "").replaceFirst(close, "");
    }

    private static String removeXmlHeader(String xmlContent) {
        return xmlContent.replaceFirst("<\\?xml version=\".*\" encoding=\".*\"\\?>", "").trim();
    }

    public static String parameterToPmNotation(String request) {
        return request
                .trim()
                .replaceFirst("key=\"[^\"]*\"", "");
    }

    private static String checkAndGetTypeName(String xml, Class type, Pattern pattern) {
        final Matcher m = pattern.matcher(xml);
        if (m.find()) {
            final String foundTypeName = m.group(2);
/* TODO: check wieder aufnehmen für abgeleitete Typen
            if (!type.getSimpleName().equals(foundTypeName)) {
                throw new IllegalStateException("type in xml != defined type: " + type + " != " + foundTypeName);
            }
*/
            return foundTypeName;
        }
        throw new IllegalStateException("could not find type in '" + xml + "'");
    }

    private static String getNamespace(String xml, Pattern pattern) {
        final Matcher m = pattern.matcher(xml);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalStateException("could not find namespace in '" + xml + "'");
    }
}
