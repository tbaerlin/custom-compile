/*
 * PmxmlRequestParserInterceptor.java
 *
 * Created on 06.10.2014 09:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.TestProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.ZoneResolver;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.iview.dmxml.RequestType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;
import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_TYPE_ATTRIBUTE_NAME;

/**
 * Parses a pm XML request and prepares the servlet request as it would be done by MmwebServiceImpl.
 * This impl. is intended to be used internally by pm's QA team for load test purposes.
 *
 * @see de.marketmaker.iview.mmgwt.mmweb.server.MmwebServiceImpl
 * @see de.marketmaker.istar.merger.web.easytrade.multiplex.MoleculeDemultiplexer
 *
 * @author mdick
 */
public class PmxmlRequestParserInterceptor extends HandlerInterceptorAdapter implements InitializingBean {
    private JaxbHandler jaxbHandler;
    private ZoneResolver zoneResolver;
    private ProfileResolver profileResolver;
    private RequestTypeUtil requestTypeUtil;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.requestTypeUtil = new RequestTypeUtil(this.jaxbHandler);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String xml = request.getParameter("request");
        final RequestType rt = this.jaxbHandler.unmarshal(xml, RequestType.class);
        final MoleculeRequest mr = this.requestTypeUtil.toMoleculeRequest(rt);
        final Zone zone = this.zoneResolver.resolveZone(request);

        final HttpSession session = request.getSession(true);
        if(session != null) {
            session.setAttribute(ProfileResolver.AUTHENTICATION_TYPE_KEY, rt.getAuthenticationType());
            session.setAttribute(ProfileResolver.AUTHENTICATION_KEY, rt.getAuthentication());
        }

        Profile profile = this.profileResolver.resolveProfile(request);
        if(profile == null) {
            profile = new TestProfile("pm-test-profile"); //only necessary for PM login
        }

        request.setAttribute(REQUEST_ATTRIBUTE_NAME, mr);
        request.setAttribute(REQUEST_TYPE_ATTRIBUTE_NAME, rt);
        request.setAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE, zone);
        RequestContextHolder.setRequestContext(zone.getRequestContext(request, profile));

        return true;
    }

    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        RequestContextHolder.setRequestContext(null);
    }

    public void setJaxbHandler(JaxbHandler jaxbHandler) {
        this.jaxbHandler = jaxbHandler;
    }

    public void setZoneResolver(ZoneResolver zoneResolver) {
        this.zoneResolver = zoneResolver;
    }

    public void setProfileResolver(ProfileResolver profileResolver) {
        this.profileResolver = profileResolver;
    }
}
