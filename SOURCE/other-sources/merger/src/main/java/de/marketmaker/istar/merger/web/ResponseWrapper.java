/*
 * ResponseWrapper.java
 *
 * Created on 03.08.2009 10:02:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.ServletOutputStream;

/**
 * Forwards all method calls to a delegate HttpServletResponse, except for errors, which it
 * intercepts and stores for later retrieval.
 *  
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ResponseWrapper implements HttpServletResponse {
    private final HttpServletResponse delegate;

    private int errorCode = 0;

    private String errorMessage;

    public ResponseWrapper(HttpServletResponse delegate) {
        this.delegate = delegate;
    }

    public void addCookie(Cookie cookie) {
        delegate.addCookie(cookie);
    }

    public boolean containsHeader(String s) {
        return delegate.containsHeader(s);
    }

    public String encodeURL(String s) {
        return delegate.encodeURL(s);
    }

    public String encodeRedirectURL(String s) {
        return delegate.encodeRedirectURL(s);
    }

    public String encodeUrl(String s) {
        return delegate.encodeUrl(s);
    }

    public String encodeRedirectUrl(String s) {
        return delegate.encodeRedirectUrl(s);
    }

    public void sendError(int i, String s) throws IOException {
        this.errorCode = i;
        this.errorMessage = s;
    }

    public void sendError(int i) throws IOException {
        this.errorCode = i;
    }

    public void sendRedirect(String s) throws IOException {
        delegate.sendRedirect(s);
    }

    public void setDateHeader(String s, long l) {
        delegate.setDateHeader(s, l);
    }

    public void addDateHeader(String s, long l) {
        delegate.addDateHeader(s, l);
    }

    public void setHeader(String s, String s1) {
        delegate.setHeader(s, s1);
    }

    public void addHeader(String s, String s1) {
        delegate.addHeader(s, s1);
    }

    public void setIntHeader(String s, int i) {
        delegate.setIntHeader(s, i);
    }

    public void addIntHeader(String s, int i) {
        delegate.addIntHeader(s, i);
    }

    public void setStatus(int i) {
        delegate.setStatus(i);
    }

    public void setStatus(int i, String s) {
        delegate.setStatus(i, s);
    }

    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    public String getContentType() {
        return delegate.getContentType();
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        return delegate.getWriter();
    }

    public void setCharacterEncoding(String s) {
        delegate.setCharacterEncoding(s);
    }

    public void setContentLength(int i) {
        delegate.setContentLength(i);
    }

    public void setContentLengthLong(long len) {
        delegate.setContentLengthLong(len);
    }

    public void setContentType(String s) {
        delegate.setContentType(s);
    }

    public void setBufferSize(int i) {
        delegate.setBufferSize(i);
    }

    public int getBufferSize() {
        return delegate.getBufferSize();
    }

    public void flushBuffer() throws IOException {
        delegate.flushBuffer();
    }

    public void resetBuffer() {
        delegate.resetBuffer();
    }

    public boolean isCommitted() {
        return delegate.isCommitted();
    }

    public void reset() {
        delegate.reset();
    }

    public void setLocale(Locale locale) {
        delegate.setLocale(locale);
    }

    public Locale getLocale() {
        return delegate.getLocale();
    }

    public HttpServletResponse getDelegate() {
        return delegate;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isError() {
        return this.errorCode > 0;
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
