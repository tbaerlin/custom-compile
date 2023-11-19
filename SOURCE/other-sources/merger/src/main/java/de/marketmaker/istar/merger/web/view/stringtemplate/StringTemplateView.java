/*
 * StringTemplateView.java
 *
 * Created on 19.06.2006 15:36:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.stringtemplate;

import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.view.AbstractTemplateView;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.misc.STMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StringTemplateView extends AbstractTemplateView {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private STGroup group;

    static final ThreadLocal<STMessage> RUN_TIME_ERRORS = new ThreadLocal<>();

    public void setGroup(STGroup group) {
        this.group = group;
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<setGroup> group = " + group);
        }
    }

    protected void renderMergedTemplateModel(Map<String, Object> map, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        applyContentType(response);
        final ST template = getTemplate();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            try {
                // ST does not allow keys containing '.'
                template.add(e.getKey().replace('.', '_'), e.getValue());
            } catch (IllegalArgumentException e1) {
                this.logger.warn("<renderMergedTemplateModel> " + getUrl() + ": " + e1.getMessage());
            }
        }

        try {
            NoIndentWriter out = new NoIndentWriter(response.getWriter());
            template.write(out, getLocale());
            response.flushBuffer();
        } finally {
            if (RUN_TIME_ERRORS.get() != null) {
                this.logger.error(HttpRequestUtil.toString(request.getRequestURI(),
                        (MoleculeRequest)
                                request.getAttribute(MoleculeRequest.REQUEST_ATTRIBUTE_NAME),
                        request.getParameterMap()
                ));
            }
            RUN_TIME_ERRORS.remove();
        }

//        final int n = template.write(new NoIndentWriter(response.getWriter()));
//        this.logger.info("<renderMergedTemplateModel> wrote " + n + " bytes");
    }

    private Locale getLocale() {
        RequestContext ctx = RequestContextHolder.getRequestContext();
        return (ctx != null) ? ctx.getLocale() : RequestContext.DEFAULT_LOCALES.get(0);
    }

    public ST getTemplate() throws IllegalArgumentException {
        ST result = this.group.getInstanceOf(getUrl());
        if (result == null) {
            throw new IllegalArgumentException();
        }
        return result;
    }

    protected void initApplicationContext() {
        super.initApplicationContext();

        checkTemplate();
    }

    protected void checkTemplate() throws ApplicationContextException {
        try {
            getTemplate();
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<checkTemplate> found template [" + getUrl() + "]");
            }
        }
        catch (IllegalArgumentException ex) {
            throw new ApplicationContextException(
                    "Could not load StringTemplate for URL [" + getUrl() + "]", ex);
        }
    }

}
