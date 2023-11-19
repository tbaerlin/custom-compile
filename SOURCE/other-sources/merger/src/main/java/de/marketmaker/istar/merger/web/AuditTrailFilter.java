/*
 * AuditTrailFilter.java
 *
 * Created on 10.09.12 14:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs requests and responses of filtered requests. The object that captures the audit data is
 * also available as a ServletRequest attribute, so controllers etc. can provider additional
 * information, see {@link #getTrail(javax.servlet.http.HttpServletRequest)}.
 *
 * @author oflege
 */
public class AuditTrailFilter implements Filter {
    public interface Trail {
        public void add(Throwable throwable);
        /** add message to trail, appears as tag=o, if tag is null, just o will be appended*/
        public void add(String tag, Object o);
    }

    private static final Trail NULL_TRAIL = new Trail() {
        @Override
        public void add(Throwable throwable) {
        }

        @Override
        public void add(String tag, Object o) {
        }
    };

    /** the real Trail that writes audit data to a StringWriter */
    private static class LogTrail implements Trail {
        private final StringWriter sb = new StringWriter(4096);

        private final PrintWriter pw = new PrintWriter(sb, true);

        private LogTrail() {
        }

        public void add(Throwable throwable) {
            add("ERROR", throwable.getMessage());
            throwable.printStackTrace(this.pw);
        }

        public void add(String tag, Object o) {
            if (o != null) {
                if (tag != null) {
                    this.pw.append(tag).append("=");
                }
                this.pw.println(String.valueOf(o));
            }
        }

        public String getResult() {
            this.pw.close();
            return this.sb.toString();
        }
    }

    /**
     * A ServletOutputStream that writes output to both its delegate and into an internal
     * buffer so that the output can be added to the audit trail later on.
     */
    private static class TeeServletOutputStream extends ServletOutputStream {
        private final ServletOutputStream underlyingStream;

        private final ByteArrayOutputStream baos;

        private TeeServletOutputStream(ServletResponse httpServletResponse)
                throws IOException {
            // System.out.println("TeeServletOutputStream.constructor() called");
            this.underlyingStream = httpServletResponse.getOutputStream();
            this.baos = new ByteArrayOutputStream(4096);
        }

        byte[] getOutputStreamAsByteArray() {
            return baos.toByteArray();
        }

        @Override
        public void write(int val) throws IOException {
            this.underlyingStream.write(val);
            this.baos.write(val);
        }

        @Override
        public void write(byte byteArray[], int offset, int length)
                throws IOException {
            this.underlyingStream.write(byteArray, offset, length);
            this.baos.write(byteArray, offset, length);
        }

        @Override
        public void close() throws IOException {
            // System.out.println("CLOSE TeeServletOutputStream.close() called");
        }


        @Override
        public void flush() throws IOException {
            underlyingStream.flush();
            baos.flush();
        }

        @Override
        public boolean isReady() {
            return underlyingStream.isReady();
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            underlyingStream.setWriteListener(writeListener);
        }
    }

    /**
     * A ServletResponse that uses a TeeServletOutputStream to capture the response output.
     */
    private static class TeeHttpServletResponse extends HttpServletResponseWrapper {

        private TeeServletOutputStream teeServletOutputStream;

        private PrintWriter teeWriter;

        private TeeHttpServletResponse(HttpServletResponse httpServletResponse) {
            super(httpServletResponse);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (this.teeWriter != null) {
                throw new IOException("cannot call getOutputStream after getWriter");
            }
            if (teeServletOutputStream == null) {
                teeServletOutputStream = new TeeServletOutputStream(getResponse());
            }
            return teeServletOutputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (this.teeWriter == null) {
                if (this.teeServletOutputStream != null) {
                    throw new IOException("cannot call getWriter after getOutputStream");
                }
                setCharacterEncoding(getCharacterEncoding());
                this.teeWriter = new PrintWriter(
                        new OutputStreamWriter(getOutputStream(), getCharacterEncoding()), true);
            }
            return this.teeWriter;
        }

        @Override
        public void flushBuffer() {
            if (this.teeWriter != null) {
                this.teeWriter.flush();
            }
        }

        byte[] getOutputBuffer() {
            // teeServletOutputStream can be null if the getOutputStream method is never
            // called.
            if (teeServletOutputStream != null) {
                return teeServletOutputStream.getOutputStreamAsByteArray();
            }
            else {
                return null;
            }
        }

        void finish() throws IOException {
            if (this.teeWriter != null) {
                this.teeWriter.close();
            }
            if (this.teeServletOutputStream != null) {
                this.teeServletOutputStream.close();
            }
        }
    }

    private static final String AUDIT_TRAIL_ATTRIBUTE
            = AuditTrailFilter.class.getCanonicalName() + ".TRAIL";

    public static Trail getTrail(HttpServletRequest request) {
        final Trail result = (Trail) request.getAttribute(AUDIT_TRAIL_ATTRIBUTE);
        return (result != null) ? result : NULL_TRAIL;
    }

    private Logger logger;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.logger = LoggerFactory.getLogger(filterConfig.getInitParameter("log"));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        final HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        final HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        final TeeHttpServletResponse tee
                = new TeeHttpServletResponse(httpServletResponse);

        final LogTrail trail = new LogTrail();
        servletRequest.setAttribute(AUDIT_TRAIL_ATTRIBUTE, trail);
        trail.add("REQUEST", formatRequest(httpServletRequest));
        trail.add("REQUEST_HEADERS", getRequestHeaders(httpServletRequest));
        trail.add("COOKIES", getCookies(httpServletRequest));
        try {
            filterChain.doFilter(servletRequest, tee);
            tee.finish();
            trail.add("RESPONSE", getResponseContent(httpServletResponse, tee.getOutputBuffer()));
            this.logger.info(trail.getResult());
        } catch (IOException | ServletException e) {
            trail.add(e);
            this.logger.error(trail.getResult());
            throw e;
        }
    }

    private String formatRequest(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(request.getMethod()).append(" ").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }
        else if ("POST".equals(request.getMethod())) {
            final Map<String, String[]> params = request.getParameterMap();
            if (!params.isEmpty()) {
                char sep = '?';
                for (Map.Entry<String, String[]> entry : params.entrySet()) {
                    for (String val : entry.getValue()) {
                        sb.append(sep).append(entry.getKey()).append("=").append(val);
                        sep = '&';
                    }
                }
            }
        }
        return sb.toString();
    }

    private String getResponseContent(HttpServletResponse response,
            byte[] content) throws UnsupportedEncodingException {
        final StringBuilder sb = new StringBuilder(content != null ? (content.length + 100) : 100);
        sb.append(response.getStatus()).append(" ")
                .append(response.getCharacterEncoding()).append(" ");
        if (content != null) {
            sb.append(new String(content, response.getCharacterEncoding()));
        }
        return sb.toString();
    }

    private static String getRequestHeaders(HttpServletRequest request) {
        final Map<String, String> map = new TreeMap<>();
        for (String s: Collections.list(request.getHeaderNames())) {
            map.put(s, request.getHeader(s));
        }
        return (map.isEmpty()) ? null : map.toString();
    }

    private static String getCookies(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        final Map<String, String> map = new TreeMap<>();
        for (Cookie c : cookies) {
            map.put(c.getName(), c.getValue());
        }
        return map.toString();
    }

    @Override
    public void destroy() {
    }
}
