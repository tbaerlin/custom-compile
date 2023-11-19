/*
 * ProfileInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Felix Hoffmann
 */
public class SelectorInterceptor extends WebApplicationObjectSupport implements InitializingBean, HandlerInterceptor {
    private Selector requiredSelector;

    public void setRequiredSelector(String requiredSelector) {
        this.requiredSelector = Selector.valueOf(requiredSelector);
    }

    public void afterPropertiesSet() throws Exception {
        logger.info("<afterPropertiesSet> required Selector: " + requiredSelector.name());
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile == null) {
            throw new NoProfileException("no profile found");
        }
        if (this.requiredSelector != null && !profile.isAllowed(this.requiredSelector)) {
            throw new PermissionDeniedException(this.requiredSelector);
        }
        return true;
    }

    protected RequestContext getRequestContext(HttpServletRequest request, Profile profile) {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        return z.getRequestContext(request, profile);
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }
}
