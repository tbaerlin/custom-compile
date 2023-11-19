/*
 * RequestParserMethod.java
 *
 * Created on 06.02.2009 11:03:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;

/**
 * Method object that parses a given request and adds it as an attribute to the request.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RequestParserMethod {
    public static final int MAX_NUM_ATOMS = 100;

    public static final int MAX_CONTENT_LENGTH = 1 << 17; // 128k

    private final StopWatch sw;

    private final HttpServletRequest request;

    private XMLOutputter outputter = null;

    public static final String REQUEST_PARAMETER = "request";

    private static final Set<String> SUPPORTED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "text/xml", "application/xml", "application/json"
    ));

    public RequestParserMethod(HttpServletRequest request) {
        this.request = request;
        this.sw = StopWatchHolder.getStopWatch();
    }

    private void start(String name) {
        if (this.sw == null) {
            return;
        }
        stop();
        this.sw.start(name);
    }

    private void stop() {
        if (this.sw != null && this.sw.isRunning()) {
            this.sw.stop();
        }
    }

    void invoke() throws Exception {
        this.request.setCharacterEncoding("UTF-8");

        final MoleculeRequest result = createRequest();

        try {
            result.afterPropertiesSet();
        } catch (BadRequestException e) {
            stop();
            throw e;
        }

        this.request.setAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME, result);

        if (StringUtils.hasText(result.getKey())) {
            this.request.setAttribute(ProfileResolver.KEY_KEY, result.getKey());
        }
        if (StringUtils.hasText(result.getAuthentication())) {
            this.request.setAttribute(ProfileResolver.AUTHENTICATION_KEY, result.getAuthentication());
        }
        if (StringUtils.hasText(result.getAuthenticationType())) {
            this.request.setAttribute(ProfileResolver.AUTHENTICATION_TYPE_KEY, result.getAuthenticationType());
        }
        stop();

        final int numAtoms = result.getAtomRequests().size();
        if (numAtoms > MAX_NUM_ATOMS) {
            throw new BadRequestException("Too many atoms: " + numAtoms + " > " + MAX_NUM_ATOMS);
        }
    }

    private MoleculeRequest createRequest() throws Exception {
        if ("POST".equals(this.request.getMethod())
                || this.request.getParameter(REQUEST_PARAMETER) != null) {
            return parsePostRequest();
        }
        if ("GET".equals(this.request.getMethod())) {
            return parseGetRequest();
        }
        throw new BadRequestException("Unsupported method: " + this.request.getMethod());
    }

    private MoleculeRequest parseGetRequest() throws Exception {
        start("parse");
        final MoleculeRequest mr = new MoleculeRequest();

        String atomName = null;
        String id = null;
        final Map<String, String[]> m = new HashMap<>();

        final Enumeration pNames = this.request.getParameterNames();
        while (pNames.hasMoreElements()) {
            final String pName = (String) pNames.nextElement();
            final String[] values = this.request.getParameterValues(pName);
            switch (pName) {
                case "atoms":
                    atomName = values[0];
                    break;
                case "key":
                    mr.setKey(values[0]);
                    break;
                case "authentication":
                    mr.setAuthentication(values[0]);
                    break;
                case "authenticationType":
                    mr.setAuthenticationType(values[0]);
                    break;
                case "locale":
                    parseLocales(mr, values[0]);
                    break;
                default:
                    m.put(pName, values);
                    break;
            }
        }
        if (atomName == null) {
            throw new BadRequestException("missing atoms parameter");
        }
        mr.addAtom(id, atomName, m, null);

        return mr;
    }

    private void parseLocales(MoleculeRequest mr, String value) {
        final List<Locale> locales = parseLocales(value);
        if (locales != null) {
            mr.setLocales(locales);
        }
    }

    public static List<Locale> parseLocales(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        final String[] strings = value.split(",");
        final List<Locale> result = new ArrayList<>(strings.length);
        for (final String s : strings) {
            final String locale = s.trim();
            try {
                result.add(new Locale(locale));
            } catch (Exception e) {
                throw new BadRequestException("invalid locale '" + locale + "'");
            }
        }
        return result;
    }

    private MoleculeRequest parsePostRequest() throws Exception {
        start("getParameter");
        final String requestStr = getRequestParameter();
        stop();

        if (!StringUtils.hasText(requestStr)) {
            throw new BadRequestException("missing request parameter");
        }

        return parseRequest(requestStr);
    }

    private String getRequestParameter() throws IOException {
        final String requestParameter = this.request.getParameter(REQUEST_PARAMETER);
        if (requestParameter != null) {
            return requestParameter;
        }

        if (SUPPORTED_CONTENT_TYPES.contains(getContentTypeWithoutCharset())) {
            final int length = request.getContentLength();
            if (length <= 0) {
                throw new BadRequestException("contentLength is undefined");
            }
            if (length > MAX_CONTENT_LENGTH) {
                throw new BadRequestException("content is too long");
            }
            final StringWriter sw = new StringWriter(length);
            FileCopyUtils.copy(this.request.getReader(), sw);
            return sw.toString();
        }
        return null;
    }

    private String getContentTypeWithoutCharset() {
        final String value = request.getContentType();
        if (value != null) {
            final int p = value.indexOf(';');
            if (p > 0) {
                return value.substring(0, p);
            }
        }
        return value;
    }

    private MoleculeRequest parseRequest(String requestStr) throws IOException {
        if (requestStr.startsWith("{")) {
            return parseJson(requestStr);
        }
        return parseXml(requestStr);
    }

    private MoleculeRequest parseJson(String requestStr) {
        return GsonUtil.fromJson(requestStr, MoleculeRequest.class);
    }

    private MoleculeRequest parseXml(String requestStr) throws IOException {
        final MoleculeRequest mr = new MoleculeRequest();

        start("build");
        final Document document = toDocument(requestStr);
        stop();

        start("parse");
        final Element rootElement = document.getRootElement();
        mr.setKey(rootElement.getChildTextTrim("key"));
        mr.setAuthentication(rootElement.getChildTextTrim("authentication"));
        mr.setAuthenticationType(rootElement.getChildTextTrim("authenticationType"));
        mr.setSkipRequestRendering(rootElement.getChild("skipRequestRendering") != null);

        parseLocales(mr, rootElement.getChildTextTrim("locale"));

        //noinspection unchecked
        final List<Element> children = rootElement.getChildren("block");
        for (Element e : children) {
            final String atomName = e.getAttributeValue("key");
            if (atomName == null) {
                throw new BadRequestException("block without atomName attribute");
            }
            final String id = e.getAttributeValue("id");
            final Map<String, String[]> parameters = parseParameters(e, atomName);
            final String dependsOnId = e.getAttributeValue("depends-on-id");
            mr.addAtom(id, atomName, parameters, dependsOnId);
        }
        return mr;
    }

    private Map<String, String[]> parseParameters(Element e, String atomName) {
        final Map<String, String[]> result = new LinkedHashMap<>();
        //noinspection unchecked
        final List<Element> params = e.getChildren("parameter");
        for (Element pe : params) {
            final String key = pe.getAttributeValue("key");
            if (key == null) {
                throw new BadRequestException("parameter without key attribute in block " + atomName);
            }
            final String value = getValue(pe);
            result.put(key, join(result.get(key), value));
        }
        return result;
    }

    private Document toDocument(String requestStr) throws IOException {
        try {
            return new SAXBuilder().build(new StringReader(requestStr));
        } catch (JDOMException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private String getValue(Element pe) {
        final String value = pe.getAttributeValue("value");
        if (value != null) {
            return value;
        }
        return getContentAsXmlString(pe);
    }

    private String getContentAsXmlString(Element pe) {
        if (this.outputter == null) {
            this.outputter = new XMLOutputter(Format.getCompactFormat());
        }
        return this.outputter.outputString(pe);
    }

    private static String[] join(String[] values, String value) {
        if (values == null) {
            return new String[]{value};
        }
        final String[] result = new String[values.length + 1];
        System.arraycopy(values, 0, result, 0, values.length);
        result[result.length - 1] = value;
        return result;
    }

    String requestToString() {
        return HttpRequestUtil.toString(this.request.getRequestURI(), null,
                this.request.getParameterMap());
    }

}
