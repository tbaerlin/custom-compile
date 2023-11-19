/*
 * DiagnosticContextFilter.java
 *
 * Created on 16.09.13 10:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

import static de.marketmaker.istar.common.log.LoggingUtil.UNIQUE_ID;

/**
 * Adds the unique request id to slf4j's mapped diagnostic context (MDC). The id is either retrieved
 * from the HttpRequest Header <tt>UNIQUE_ID</tt> (e.g., if it has been added by an apache
 * server), or created by this filter.
 * <p>
 * IDs created by this filter contain 18 chars from <tt>[A-Za-z0-9@-]</tt> and are similar to what
 * apache's unique_id module creates. The 8 char prefix is based on a 32-bit IP address and a 16 bit pid;
 * the 10 byte suffix is based on a 44 bit time stamp and a 16 bit counter.
 *
 * @see <a href="http://httpd.apache.org/docs/2.2/mod/mod_unique_id.html">mod_unique_id</a>
 * @see <a href="http://logback.qos.ch/manual/mdc.html">MDC</a>
 * @author oflege
 */
public class UniqueIdFilter implements Filter {

    private static final char CHARS[]
            = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@-".toCharArray();

    private final AtomicInteger counter = new AtomicInteger();

    private char[] prefix;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.prefix = initPrefix(filterConfig);
    }

    private char[] initPrefix(FilterConfig filterConfig) {
        final String[] tmp = ManagementFactory.getRuntimeMXBean().getName().split("@");
        final int pid = Integer.parseInt(tmp[0]);
        final String hostname = tmp[1];

        final ByteBuffer bb = ByteBuffer.wrap(new byte[8]);
        bb.position(2);
        bb.put(getAddress(filterConfig, hostname), 0, 4).putShort((short) pid).flip();
        final long l = bb.asLongBuffer().get();

        return append(l, new char[8], 0, 8);
    }

    private byte[] getAddress(FilterConfig filterConfig, String host) {
        try {
            return InetAddress.getByName(host).getAddress();
        } catch (UnknownHostException e) {
            final byte[] result = new byte[4];
            ByteBuffer.wrap(result).putInt((int) System.nanoTime());
            filterConfig.getServletContext().log("Unknown host " + host
                    + ", using address " + Arrays.toString(result));
            return result;
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain) throws IOException, ServletException {

        final String uid = getUid((HttpServletRequest) servletRequest);
        MDC.put(UNIQUE_ID, uid);

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(UNIQUE_ID);
        }
    }

    private String getUid(HttpServletRequest servletRequest) {
        final String fromHeader = HttpRequestUtil.getUniqueId(servletRequest);
        return (fromHeader != null) ? fromHeader : createUid();
    }

    @Override
    public void destroy() {
    }

    private String createUid() {
        final long suffix = (System.currentTimeMillis() << 16) + (counter.incrementAndGet() & 0xFFFF);
        return new String(append(suffix, Arrays.copyOf(this.prefix, 18), 8, 10));
    }

    private char[] append(long value, char[] chars, int offset, int length) {
        long l = value;
        for (int i = 0, j = i + offset + length; i < length; i++) {
            chars[--j] = CHARS[(int) (l & 0x3F)];
            l >>= 6;
        }
        return chars;
    }
}
