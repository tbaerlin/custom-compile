/*
 * JsonConverterInterceptor.java
 *
 * Created on 04.09.2014 21:29:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.fusion.dmxml.ResponseType;

/**
 * @author tkiesgen
 */
public class JsonConverterFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Unmarshaller unmarshaller;

    public JsonConverterFilter() {
        try {
            final JAXBContext jc = JAXBContext.newInstance("de.marketmaker.istar.fusion.dmxml");
            this.unmarshaller = jc.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("init failed", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException,
            ServletException {
        if (!(request instanceof HttpServletRequest)
                || !((HttpServletRequest) request).getRequestURI().endsWith("retrieve.json")) {
            chain.doFilter(request, response);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");

        final TimeTaker ttt = new TimeTaker();

        final ByteResponseWrapper brw = new ByteResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, brw);

        if (brw.getStatus() != 200) {
            // in case of error, we don't proceed. It will be translated into an error html
            return;
        }

        final String xml = new String(brw.getBytes());

        final TimeTaker ctt = new TimeTaker();

        final ResponseType responseType;
        synchronized (this.unmarshaller) {
            try {
                final StreamSource source = new StreamSource(new StringReader(xml));
                responseType = this.unmarshaller.unmarshal(source, ResponseType.class).getValue();
            } catch (JAXBException e) {
                throw new IOException("failed converting xml to json", e);
            }
        }

        final String json = GsonUtil.toJson(responseType);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(json);

        this.logger.info("<doFilter> conversion in " + ctt + " ms, total " + ttt + " ms");
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    private static class ByteResponseWrapper extends HttpServletResponseWrapper {
        private final PrintWriter writer;

        private final ByteOutputStream output;

        public byte[] getBytes() {
            this.writer.flush();
            return this.output.getBytes();
        }

        public ByteResponseWrapper(HttpServletResponse response) {
            super(response);
            this.output = new ByteOutputStream();
            this.writer = new PrintWriter(this.output);
        }

        @Override
        public PrintWriter getWriter() {
            return this.writer;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return this.output;
        }
    }

    static class ByteOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream bos = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {
            this.bos.write(b);
        }

        public byte[] getBytes() {
            return this.bos.toByteArray();
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }
    }
}
