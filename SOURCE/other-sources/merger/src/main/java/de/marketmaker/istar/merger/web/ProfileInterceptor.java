/*
 * ProfileInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.OrderComparator;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ProfileInterceptor
    extends WebApplicationObjectSupport
    implements InitializingBean, HandlerInterceptor {
    private List<ProfileResolver> profileResolvers;

    private boolean requireProfile = true;

    private boolean sendErrorIfNoProfile = false;

    public void setSendErrorIfNoProfile(boolean sendErrorIfNoProfile) {
        this.sendErrorIfNoProfile = sendErrorIfNoProfile;
    }

    public void setRequireProfile(boolean requireProfile) {
        this.requireProfile = requireProfile;
    }

    public void setProfileResolvers(List<ProfileResolver> profileResolvers) {
        this.profileResolvers = profileResolvers;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.profileResolvers != null) {
            this.logger.info("<afterPropertiesSet> using explict profileResolvers");
            return;
        }
        //noinspection unchecked
        final Map<String, ProfileResolver> matchingBeans
                = (Map<String, ProfileResolver>) BeanFactoryUtils.beansOfTypeIncludingAncestors(
                getWebApplicationContext(), ProfileResolver.class, true, false);

        if (matchingBeans.isEmpty()) {
            this.profileResolvers = Collections.emptyList();
            throw new ServletException("no ProfileResolver(s) found");
        }
        else {
            this.profileResolvers = new ArrayList<>(matchingBeans.values());
            //noinspection unchecked
            this.profileResolvers.sort(new OrderComparator());
            this.logger.info("<afterPropertiesSet> using " + matchingBeans.keySet());
        }
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o)
            throws Exception {
        final Profile profile = getProfile(request);
        if (profile == null && this.requireProfile) {
            if (this.sendErrorIfNoProfile) {
                this.logger.warn("<preHandle> forbidden " + request);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            throw new NoProfileException("no profile found");
        }
        final RequestContext context = getRequestContext(request, profile);
        RequestContextHolder.setRequestContext(context);
        return true;
    }

    protected  RequestContext getRequestContext(HttpServletRequest request, Profile profile) {
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        return z.getRequestContext(request, profile);
    }

    private Profile getProfile(HttpServletRequest request) {
        final HttpServletRequest requestToCheck = resolveRequest(request);
        for (final ProfileResolver resolver : this.profileResolvers) {
            final Profile profile = resolver.resolveProfile(requestToCheck);
            if (profile != null) {
                return profile;
            }
        }

        return null;
    }

    private HttpServletRequest resolveRequest(HttpServletRequest request) {
        final HttpServletRequest zoneRequest = (HttpServletRequest)
                request.getAttribute(ZoneInterceptor.ZONE_REQUEST_ATTRIBUTE);
        return zoneRequest != null ? zoneRequest : request;
    }

    public void postHandle(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView)
        throws Exception {
    }

    public void afterCompletion(HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        RequestContextHolder.setRequestContext(null);
    }
}
