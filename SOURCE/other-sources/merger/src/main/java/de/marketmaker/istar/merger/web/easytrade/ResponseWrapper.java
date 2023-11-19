/*
 * NullHttpServletResponse.java
 *
 * Created on 24.10.2006 19:15:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * A wrapper to be used when dispatching atom requests; since the output will be rendered by the
 * molecule, most accesses to the response will result in an UnsupportedOperationException

 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class ResponseWrapper implements HttpServletResponse {
    private final HttpServletResponse delegate;

    public ResponseWrapper(HttpServletResponse delegate) {
        this.delegate = delegate;
    }

    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();
    }

    public void addDateHeader(String s, long l) {
        // empty
    }

    public void addHeader(String s, String s1) {
        // empty
    }

    public void addIntHeader(String s, int i) {
        // empty
    }

    public boolean containsHeader(String s) {
        return delegate.containsHeader(s);
    }

    public String encodeRedirectURL(String s) {
        return delegate.encodeRedirectURL(s);
    }

    public String encodeRedirectUrl(String s) {
        return delegate.encodeRedirectUrl(s);
    }

    public String encodeURL(String s) {
        return delegate.encodeURL(s);
    }

    public String encodeUrl(String s) {
        return delegate.encodeUrl(s);
    }

    public void sendError(int i) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void sendError(int i, String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void sendRedirect(String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void setDateHeader(String s, long l) {
        // empty
    }

    public void setHeader(String s, String s1) {
        // empty
    }

    public void setIntHeader(String s, int i) {
        // empty
    }

    public void setStatus(int i) {
        // empty
    }

    public void setStatus(int i, String s) {
        // empty
    }

    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException();
    }

    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    public String getContentType() {
        return delegate.getContentType();
    }

    public Locale getLocale() {
        return delegate.getLocale();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
        throw new UnsupportedOperationException();
    }

    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    public void setBufferSize(int i) {
        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String s) {
        throw new UnsupportedOperationException();
    }

    public void setContentLength(int i) {
        throw new UnsupportedOperationException();
    }

    public void setContentLengthLong(long len) {
        throw new UnsupportedOperationException();
    }

    public void setContentType(String s) {
        throw new UnsupportedOperationException();
    }

    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    // --Servlet 3.0 additions---------------------------------------

    public String getHeader(String s) {
        return delegate.getHeader(s);
    }

    public Collection<String> getHeaderNames() {
        return delegate.getHeaderNames();
    }

    public Collection<String> getHeaders(String s) {
        return delegate.getHeaders(s);
    }

    public int getStatus() {
        return delegate.getStatus();
    }
}
