/*
 * EncodingFilter.java
 *
 * Created on 16.11.11 07:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Fixes a problem in jira 4.4.x and might be useful in similar situations:
 * When you edit a text in a popup, it is sent with
 * <tt>Content-type: application/x-www-form-urlencoded; charset=UTF-8</tt>
 * and all the parameters are utf-8 encoded accordingly.
 * BUT: There is a filter that will set the request's encoding to iso-8859-1 <em>before</em>
 * the parameters are decoded, so characters &gt; 0x7F not be decoded correctly.
 * <p>
 * This filter ensures that all form parameters are decoded using the charset specified in
 * the Content-type header field.
 * </p>
 * <p>
 * Make sure that this filter is applied before any other filter that messes with the encoding.
 * </p>
 * <h2>Configuration Parameters</h2>
 * <dl>
 *     <dt>contentTypePattern</dt>
 *     <dd>filter will only be applied if a request's content-type matches this pattern;
 *     defaults to <tt>{@value #DEFAULT_CONTENT_TYPE_REGEX}</tt>. The pattern's <tt>group(1)</tt>
 *     has to be the name of the charset to be used for parameter decoding</dd>
 *     <dt>methods</dt>
 *     <dd>comma separated list of http request methods to be handled, defaults to <tt>POST</tt></dd>
 * </dl>
 *
 * @author oflege
 */
public class FormEncodingFilter implements Filter {

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String DEFAULT_CONTENT_TYPE_REGEX
            = "application/x-www-form-urlencoded;\\s+charset=([-_0-9a-zA-Z]+)";

    private Pattern contentTypePattern = Pattern.compile(DEFAULT_CONTENT_TYPE_REGEX);

    private final Set<String> methods = new HashSet<>(Arrays.asList("POST"));

    public void init(FilterConfig filterConfig) throws ServletException {
        final String regex = filterConfig.getInitParameter("contentTypePattern");
        if (regex != null) {
            this.contentTypePattern = Pattern.compile(regex);
            filterConfig.getServletContext().log(getClass().getSimpleName()
                    + " uses pattern '" + regex + "'");
        }
        final String methodNames = filterConfig.getInitParameter("methods");
        if (methodNames != null && methodNames.length() > 0) {
            methods.clear();
            methods.addAll(Arrays.asList(methodNames.split(",")));
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        if (isApplicable(request)) {
            final String ct = ((HttpServletRequest) request).getHeader(CONTENT_TYPE);
            if (ct != null) {
                final Matcher matcher = this.contentTypePattern.matcher(ct);
                if (matcher.matches()) {
                    applyCharset(request, matcher.group(1));
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void applyCharset(ServletRequest request, final String charset)
            throws UnsupportedEncodingException {
        request.setCharacterEncoding(charset);
        // it is not sufficient to set the character encoding since filters later in the chain
        // might override it; tomcat uses lazy parameter parsing, so the character encoding that
        // is set when the parameters are first accessed wins: so let's access them all now:
        final Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            request.getParameterValues(names.nextElement());
        }
    }

    public void destroy() {
    }

    private boolean isApplicable(ServletRequest request) {
        return request instanceof HttpServletRequest
                && this.methods.contains(((HttpServletRequest) request).getMethod());
    }
}
