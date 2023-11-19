/*
 * HttpRequestUtil.java
 *
 * Created on 18.03.2009 14:00:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

import static de.marketmaker.istar.common.log.LoggingUtil.UNIQUE_ID;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HttpRequestUtil {

    private HttpRequestUtil() {
    }

    /**
     * Returns the last component of the request's path (e.g. uri=/foo/bar/x.pdf would
     * return x.pdf).
     * @param request the request
     * @return name
     */
    public static String getRequestName(HttpServletRequest request) {
        // in tomcat7, the requestURI contains the ;jessionid=... parameter, which we have to remove
        final String uri = request.getRequestURI();
        int semicolonAt = uri.length();
        int n = uri.length();
        while (n-- > 0 && uri.charAt(n) != '/') {
            if (uri.charAt(n) == ';') {
                semicolonAt = n;
            }
        }
        return uri.substring(n + 1, semicolonAt);
    }

    /**
     * Returns the first component of the request's path (e.g. uri=/foo/bar/x.pdf would
     * return foo).
     * @param request the request
     * @return name
     */
    public static String getWebappName(HttpServletRequest request) {
        final String contextPath = RequestContextUtils.getWebApplicationContext(request).getServletContext().getContextPath();
        if (!StringUtils.hasText(contextPath)) {
            return contextPath;
        }
        return contextPath.substring(1); // remove '/' prefix
    }

    /**
     * Returns a value associated with a request by first looking in the request's session (if existing),
     * then in the request's parameters, and finally in the request's attributes. As soon
     * as a non-null value is found, it will be returned.
     * @param request contains parameter
     * @param name identifies parameter
     * @return value for name or null if no such parameter is defined
     */
    public static String getValue(HttpServletRequest request, String name) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final String fromSession = (String) session.getAttribute(name);
            if (fromSession != null) {
                return fromSession;
            }
        }

        return getParameterOrAttribute(request, name);
    }

    public static String getValueBySession(HttpServletRequest request, String name) {
        final HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        return (String) session.getAttribute(name);
    }

    /**
     * Returns a parameter or attribute value with the given name. If the value is enclosed
     * in '(' and ')', it is supposed to be an indirection, i.e., the value is the name of
     * another parameter or attribute whose value will then be returned.
     * @param request contains parameter
     * @param name identifies parameter
     * @return value of parameter or attribute
     */
    private static String getParameterOrAttribute(HttpServletRequest request, String name) {
        final String result = getRawParameterOrAttribute(request, name);
        if (isIndirection(result)) {
            return getParameterOrAttribute(request, result.substring(1, result.length() - 1));
        }
        return result;
    }

    private static boolean isIndirection(String result) {
        return result != null && result.startsWith("(") && result.endsWith(")");
    }

    private static String getRawParameterOrAttribute(HttpServletRequest request, String name) {
        final String result = request.getParameter(name);
        return (result != null) ? result : (String) request.getAttribute(name);
    }

    public static String getUniqueId(HttpServletRequest request) {
        return request.getHeader(UNIQUE_ID);
    }

    public static String toString(HttpServletRequest request) {
        return toString(request.getRequestURI(), null, request.getParameterMap());
    }

    public static String toString(String uri, MoleculeRequest mr,
            Map<String, String[]> parameters) {
        final StringBuilder sb = new StringBuilder(1000);
        sb.append("request=").append(uri);
        if (mr != null) {
            sb.append(", moleculeRequest=").append(GsonUtil.toJson(mr));
        }
        else {
            sb.append(", parameters=[");
            appendParameters(sb, parameters);
            sb.append("]");
        }
        return sb.toString();
    }

    public static void appendParameters(StringBuilder sb, HttpServletRequest request) {
        appendParameters(sb, request.getParameterMap());
    }

    public static void appendParameters(StringBuilder sb, Map<String, String[]> parameters) {
        int n = 0;
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            final String[] values = entry.getValue();
            if (n++ > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=");
            sb.append(values.length == 1 ? values[0] : Arrays.toString(values));
        }
    }

    public static String[] filterParametersWithText(String[] in) {
        String[] result = Arrays.stream(in).filter(StringUtils::hasText).toArray(String[]::new);
        return result.length > 0 ? result : null;
    }
}
