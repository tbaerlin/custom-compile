/*
 * MyHttpServletRequest.java
 *
 * Created on 07.03.2008 16:33:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.security.Principal;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import de.marketmaker.istar.merger.web.ProfileResolver;

/**
 * A simple HttpServletRequest implementation that can be used for running a gwt service on the
 * server side without actually invoking it by http. The only methods that actually do something
 * are those that get/set attributes and parameters.
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MyHttpServletRequest implements HttpServletRequest {
    private final Map<String, Object> attributes = new HashMap<>();

    private final Map<String, String[]> parameters = new HashMap<>();

    public MyHttpServletRequest() {
        this.parameters.put(ProfileResolver.AUTHENTICATION_TYPE_KEY, new String[] { "resource" });
        this.parameters.put(ProfileResolver.AUTHENTICATION_KEY, new String[] { "iview" });        
    }

    public Object getAttribute(String s) {
        return this.attributes.get(s);
    }

    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    public String getAuthType() {
        return null;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    public long getDateHeader(String s) {
        return 0;
    }

    public String getHeader(String s) {
        return null;
    }

    public Enumeration<String> getHeaderNames() {
        return null;
    }

    public Enumeration<String> getHeaders(String s) {
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    public int getIntHeader(String s) {
        return 0;
    }

    public String getLocalAddr() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public int getLocalPort() {
        return 0;
    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration<Locale> getLocales() {
        return null;
    }

    public String getMethod() {
        return "POST";
    }

    public String getParameter(String s) {
        final String[] strings = this.parameters.get(s);
        return strings != null ? strings[0] : null;
    }

    public Map<String, String[]> getParameterMap() {
        return this.parameters;
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    public String[] getParameterValues(String s) {
        return this.parameters.get(s);
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getProtocol() {
        return null;
    }

    public String getQueryString() {
        return null;
    }

    public BufferedReader getReader() throws IOException {
        return null;
    }

    /** @deprecated */
    public String getRealPath(String s) {
        return null;
    }

    public String getRemoteAddr() {
        return null;
    }

    public String getRemoteHost() {
        return null;
    }

    public int getRemotePort() {
        return 0;
    }

    public String getRemoteUser() {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return null;
    }

    public String getRequestURI() {
        return null;
    }

    public StringBuffer getRequestURL() {
        return null;
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return null;
    }

    public int getServerPort() {
        return 0;
    }

    public String getServletPath() {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public HttpSession getSession(boolean b) {
        return null;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    /** @deprecated */
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isSecure() {
        return false;
    }

    public boolean isUserInRole(String s) {
        return false;
    }

    public void removeAttribute(String s) {
        this.attributes.remove(s);
    }

    public void setAttribute(String s, Object o) {
        this.attributes.put(s, o);
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String s, String s2) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String s) throws IOException, ServletException {
        return null;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    public String changeSessionId() {
        return null;
    }

    public <T extends HttpUpgradeHandler> T upgrade(
            Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    public long getContentLengthLong() {
        return getContentLength();
    }
}
