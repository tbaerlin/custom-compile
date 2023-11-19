/*
 * RequestDumper.java
 *
 * Created on 05.10.2006 08:28:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class RequestLogger extends HandlerInterceptorAdapter {
    private final static String LF = System.getProperty("line.separator");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Logger rlog = LoggerFactory.getLogger("request.logger");

    private AtomicBoolean active = new AtomicBoolean(false);

    @ManagedAttribute(description = "true to activate logging")
    public void setActive(boolean active) {
        this.active.set(active);
        this.logger.info("<setActive> " + this.active.get());
    }

    @ManagedAttribute
    public boolean isActive() {
        return this.active.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Object handler, Exception e) throws Exception {
        log(httpServletRequest);
    }

    public void log(HttpServletRequest request) {
        if (!this.active.get() || !rlog.isInfoEnabled()) {
            return;
        }

        final StringBuilder sb = new StringBuilder(8192);

        // Logger pre-service information
        sb.append("REQUEST URI       =").append(request.getRequestURI()).append(LF);
        sb.append("          authType=").append(request.getAuthType()).append(LF);
        sb.append(" characterEncoding=").append(request.getCharacterEncoding()).append(LF);
        sb.append("     contentLength=").append(request.getContentLength()).append(LF);
        sb.append("       contentType=").append(request.getContentType()).append(LF);
        sb.append("       contextPath=").append(request.getContextPath()).append(LF);

        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++)
                sb.append("            cookie=").append(cookies[i].getName()).append("=")
                    .append(cookies[i].getValue()).append(LF);
        }

        Enumeration hnames = request.getHeaderNames();
        while (hnames.hasMoreElements()) {
            String hname = (String) hnames.nextElement();
            Enumeration hvalues = request.getHeaders(hname);
            while (hvalues.hasMoreElements()) {
                String hvalue = (String) hvalues.nextElement();
                sb.append("            header=").append(hname).append("=").append(hvalue).append(LF);
            }
        }
        sb.append("            locale=").append(request.getLocale()).append(LF);
        sb.append("            method=").append(request.getMethod()).append(LF);
        Enumeration pnames = request.getParameterNames();
        while (pnames.hasMoreElements()) {
            sb.append("         parameter=");
            String pname = (String) pnames.nextElement();
            sb.append(pname).append("=");
            String pvalues[] = request.getParameterValues(pname);
            for (int i = 0; i < pvalues.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(pvalues[i]);
            }
            sb.append(LF);
        }

        sb.append("          pathInfo=").append(request.getPathInfo()).append(LF);
        sb.append("          protocol=").append(request.getProtocol()).append(LF);
        sb.append("       queryString=").append(request.getQueryString()).append(LF);
        sb.append("        remoteAddr=").append(request.getRemoteAddr()).append(LF);
        sb.append("        remoteHost=").append(request.getRemoteHost()).append(LF);
        sb.append("        remoteUser=").append(request.getRemoteUser()).append(LF);
        sb.append("requestedSessionId=").append(request.getRequestedSessionId()).append(LF);
        sb.append("            scheme=").append(request.getScheme()).append(LF);
        sb.append("        serverName=").append(request.getServerName()).append(LF);
        sb.append("        serverPort=").append(request.getServerPort()).append(LF);
        sb.append("       servletPath=").append(request.getServletPath()).append(LF);
        sb.append("          isSecure=").append(request.isSecure()).append(LF);
        sb.append("---------------------------------------------------------------").append(LF);

        this.rlog.info(sb.toString());
    }
}
