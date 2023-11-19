/*
 * PmLocaleResolver.java
 *
 * Created on 30.01.14 15:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.WebUtils;

import de.marketmaker.istar.merger.web.HttpRequestUtil;

import static de.marketmaker.iview.mmgwt.mmweb.server.UserServiceImpl.SESSION_LOCALE;

/**
 * @author oflege
 */
public class AsLocaleResolver implements LocaleResolver {

    static final Locale EN = Locale.ENGLISH;

    static final Locale DE = Locale.GERMAN;

    private final Log logger = LogFactory.getLog(getClass());

    private Locale toLocale(String language) {
        return "en".equals(language) ? EN : DE;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String language = (String) WebUtils.getSessionAttribute(request, SESSION_LOCALE);
        if (!StringUtils.hasText(language)) {
            // should only happen for login,
            // see de.marketmaker.iview.mmgwt.mmweb.server.PmUserLoginMethod
            language = (String) request.getAttribute(SESSION_LOCALE);
        }
        if (language == null) {
            this.logger.warn("<getLocale> no locale found in " + HttpRequestUtil.toString(request));
        }
        return toLocale(language);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException();
    }
}
