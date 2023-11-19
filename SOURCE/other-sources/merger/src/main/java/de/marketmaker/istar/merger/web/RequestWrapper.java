/*
 * RequestWrapper.java
 *
 * Created on 01.08.2006 17:56:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RequestWrapper<R extends HttpServletRequest> implements HttpServletRequest {
    public static final String DELEGATE_REQUEST = "DELEGATE_REQUEST";

    protected final R delegate;

    private final Map<String, String[]> parameters;

    private final Map<String, Object> attributes;

    private final String requestUri;

    public static <R extends HttpServletRequest> RequestWrapper<R> create(R delegate,
            Map<String, String[]> parameters) {
        return create(delegate, parameters, delegate.getRequestURI());
    }

    public static <R extends HttpServletRequest> RequestWrapper<R> create(R delegate,
            Map<String, String[]> parameters, String requestUri) {
        return create(delegate, parameters, requestUri, null);
    }

    public static <R extends HttpServletRequest> RequestWrapper<R> create(R delegate,
            Map<String, String[]> parameters,
            String requestUri, Map<String, Object> attributes) {
        if (delegate instanceof MultipartHttpServletRequest) {
            //noinspection unchecked
            return (RequestWrapper<R>) new MultipartRequestWrapper(
                    (MultipartHttpServletRequest) delegate, parameters, requestUri, attributes);
        }
        else {
            return new RequestWrapper<R>(delegate, parameters, requestUri, attributes);
        }
    }

    RequestWrapper(R delegate, Map<String, String[]> parameters,
            String requestUri, Map<String, Object> attributes) {
        this.delegate = delegate;
        this.parameters = parameters;
        if (delegate.isRequestedSessionIdFromURL()) {
            final int semicolonAt = requestUri.indexOf(';');
            this.requestUri = (semicolonAt > 0) ? requestUri.substring(0, semicolonAt) : requestUri;
        }
        else {
            this.requestUri = requestUri;
        }
        this.attributes = attributes;
    }

    public HttpServletRequest getDelegate() {
        return delegate;
    }

    public String getAuthType() {
        return delegate.getAuthType();
    }

    public String getContextPath() {
        return delegate.getContextPath();
    }

    public Cookie[] getCookies() {
        return delegate.getCookies();
    }

    public long getDateHeader(String s) {
        return delegate.getDateHeader(s);
    }

    public String getHeader(String s) {
        return delegate.getHeader(s);
    }

    public Enumeration getHeaderNames() {
        return delegate.getHeaderNames();
    }

    public Enumeration getHeaders(String s) {
        return delegate.getHeaders(s);
    }

    public int getIntHeader(String s) {
        return delegate.getIntHeader(s);
    }

    public String getMethod() {
        return delegate.getMethod();
    }

    public String getPathInfo() {
        return delegate.getPathInfo();
    }

    public String getPathTranslated() {
        return delegate.getPathTranslated();
    }

    public String getQueryString() {
        return delegate.getQueryString();
    }

    public String getRemoteUser() {
        return delegate.getRemoteUser();
    }

    public String getRequestedSessionId() {
        return delegate.getRequestedSessionId();
    }

    public String getRequestURI() {
        return this.requestUri;
    }

    public StringBuffer getRequestURL() {
        return delegate.getRequestURL();
    }

    public String getServletPath() {
        return this.requestUri.substring(this.requestUri.indexOf('/', 1));
    }

    public HttpSession getSession() {
        return delegate.getSession();
    }

    @Override
    public String changeSessionId() {
        return this.delegate.changeSessionId();
    }

    public HttpSession getSession(boolean b) {
        return delegate.getSession(b);
    }

    public Principal getUserPrincipal() {
        return delegate.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return delegate.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return delegate.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return delegate.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdValid() {
        return delegate.isRequestedSessionIdValid();
    }

    public boolean isUserInRole(String s) {
        return delegate.isUserInRole(s);
    }

    public Object getAttribute(String s) {
        if (DELEGATE_REQUEST.equals(s)) {
            return delegate;
        }
        // we have to check for containsKey as tomcat uses special attributes that have to
        // be gettable even if their names do not appear in getAttributeNames (since we don't know
        // these attributes even exist, we always use the delegate if the attribute is not locally
        // available).
        if (this.attributes != null && this.attributes.containsKey(s)) {
            return this.attributes.get(s);
        }
        return delegate.getAttribute(s);
    }

    public Enumeration getAttributeNames() {
        if (this.attributes != null) {
            return Collections.enumeration(this.attributes.keySet());
        }
        return delegate.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    public int getContentLength() {
        return delegate.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return delegate.getContentLengthLong();
    }

    public String getContentType() {
        return delegate.getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    public String getLocalAddr() {
        return delegate.getLocalAddr();
    }

    public Locale getLocale() {
        return delegate.getLocale();
    }

    public Enumeration getLocales() {
        return delegate.getLocales();
    }

    public String getLocalName() {
        return delegate.getLocalName();
    }

    public int getLocalPort() {
        return delegate.getLocalPort();
    }

    public String getParameter(String s) {
        final String[] parameterValues = getParameterValues(s);
        return parameterValues != null ? parameterValues[0] : null;
    }

    public Map getParameterMap() {
        return this.parameters;
    }

    public Enumeration getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    public String[] getParameterValues(String s) {
        return this.parameters.get(s);
    }

    public String getProtocol() {
        return delegate.getProtocol();
    }

    public BufferedReader getReader() throws IOException {
        return delegate.getReader();
    }

    public String getRealPath(String s) {
        return delegate.getRealPath(s);
    }

    public String getRemoteAddr() {
        return delegate.getRemoteAddr();
    }

    public String getRemoteHost() {
        return delegate.getRemoteHost();
    }

    public int getRemotePort() {
        return delegate.getRemotePort();
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return delegate.getRequestDispatcher(s);
    }

    public String getScheme() {
        return delegate.getScheme();
    }

    public String getServerName() {
        return delegate.getServerName();
    }

    public int getServerPort() {
        return delegate.getServerPort();
    }

    public boolean isSecure() {
        return delegate.isSecure();
    }

    public void removeAttribute(String s) {
        if (this.attributes != null) {
            this.attributes.remove(s);
        }
        else {
            this.delegate.removeAttribute(s);
        }
    }

    public void setAttribute(String s, Object o) {
        if (this.attributes != null) {
            this.attributes.put(s, o);
        }
        else {
            this.delegate.setAttribute(s, o);
        }
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        delegate.setCharacterEncoding(s);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append(getRequestURI());
        if (getQueryString() != null) {
            sb.append('?').append(getQueryString());
        }
        sb.append('{');
        int n = 0;
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            if (n++ > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append('=').append(Arrays.toString(entry.getValue()));
        }
        sb.append('{');
        return sb.toString();
    }

    public String getPage() {
        return HttpRequestUtil.getRequestName(this.delegate);
    }

    // --Servlet 3.0 additions---------------------------------------

    public boolean authenticate(
            HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return this.delegate.authenticate(httpServletResponse);
    }

    public Part getPart(String s) throws IOException, ServletException {
        return this.delegate.getPart(s);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(
            Class<T> handlerClass) throws IOException, ServletException {
        return delegate.upgrade(handlerClass);
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return this.delegate.getParts();
    }

    public void login(String s, String s1) throws ServletException {
        throw new ServletException(new UnsupportedOperationException());
    }

    public void logout() throws ServletException {
        throw new ServletException(new UnsupportedOperationException());
    }

    public AsyncContext getAsyncContext() {
        return delegate.getAsyncContext();
    }

    public DispatcherType getDispatcherType() {
        return DispatcherType.REQUEST;
    }

    public ServletContext getServletContext() {
        return delegate.getServletContext();
    }

    public boolean isAsyncStarted() {
        return false;
    }

    public boolean isAsyncSupported() {
        return false;
    }

    public AsyncContext startAsync() {
        throw new UnsupportedOperationException();
    }

    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        throw new UnsupportedOperationException();
    }
}
