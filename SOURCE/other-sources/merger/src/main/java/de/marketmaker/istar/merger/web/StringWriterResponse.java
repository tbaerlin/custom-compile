/*
 * ResponseWrapper.java
 *
 * Created on 12.02.2008 15:09:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StringWriterResponse implements HttpServletResponse {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final StringWriter sw = new StringWriter(4096);
    private int errorCode = -1;
    private String errorMessage = null;

    public String toString() {
        return this.sw.toString();
    }

    public void addCookie(Cookie cookie) {
    }

    public void addDateHeader(String s, long l) {
    }

    public void addHeader(String s, String s1) {
    }

    public void addIntHeader(String s, int i) {
    }

    public boolean containsHeader(String s) {
        return false;
    }

    public String encodeRedirectURL(String s) {
        return null;
    }

    public String encodeRedirectUrl(String s) {
        return null;
    }

    public String encodeURL(String s) {
        return null;
    }

    public String encodeUrl(String s) {
        return null;
    }

    public void sendError(int errorCode) throws IOException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("sendError(" + errorCode + ")");
        }
        this.errorCode = errorCode;
    }

    public void sendError(int errorCode, String errorMessage) throws IOException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("sendError(" + errorCode + ", " + errorMessage + ")");
        }
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void sendRedirect(String s) throws IOException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("sendRedirect(" + s + ")");
        }
    }

    public boolean isError() {
        return this.errorCode >= 0 && this.errorCode != HttpServletResponse.SC_OK;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setDateHeader(String s, long l) {
    }

    public void setHeader(String s, String s1) {
    }

    public void setIntHeader(String s, int i) {
    }

    public void setStatus(int sc) {
        setStatus(sc, null);
    }

    public void setStatus(int sc, String message) {
        if (sc != HttpServletResponse.SC_OK) {
            this.errorCode = sc;
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("setStatus(" + sc + ", " + message + ")");
        }
    }

    public void flushBuffer() throws IOException {
    }

    public int getBufferSize() {
        return 0;
    }

    public String getCharacterEncoding() {
        return "ISO-8859-1";
    }

    public String getContentType() {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream(){
            public void write(int b) throws IOException {
                sw.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(this.sw);
    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {
    }

    public void resetBuffer() {
    }

    public void setBufferSize(int i) {
    }

    public void setCharacterEncoding(String s) {
    }

    public void setContentLength(int i) {
    }

    @Override
    public void setContentLengthLong(long len) {
    }

    public void setContentType(String s) {
    }

    public void setLocale(Locale locale) {
    }

    // --Servlet 3.0 additions---------------------------------------

    public String getHeader(String s) {
        return null;
    }

    public Collection<String> getHeaderNames() {
        return null;
    }

    public Collection<String> getHeaders(String s) {
        return null;
    }

    public int getStatus() {
        return 0;
    }

}
