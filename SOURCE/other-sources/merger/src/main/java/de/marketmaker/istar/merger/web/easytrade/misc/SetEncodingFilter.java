package de.marketmaker.istar.merger.web.easytrade.misc;

import javax.servlet.*;

import java.io.IOException;

import org.springframework.util.StringUtils;

public class SetEncodingFilter implements Filter {
    private String encoding;

    public void init(FilterConfig filterConfig) throws ServletException {
        final String str = filterConfig.getInitParameter("encoding");
        this.encoding = StringUtils.hasText(str) ? str : "UTF-8";
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        request.setCharacterEncoding(this.encoding);
        filterChain.doFilter(request, response);
    }

    public void destroy() {
    }
}
