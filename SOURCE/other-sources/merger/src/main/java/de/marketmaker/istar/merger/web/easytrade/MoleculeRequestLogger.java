/*
 * RequestParserInterceptor.java
 *
 * Created on 04.07.2006 14:43:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import static de.marketmaker.istar.merger.web.HttpRequestUtil.getValue;
import static de.marketmaker.istar.merger.web.ZoneDispatcherServlet.ZONE_ATTRIBUTE;
import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;

import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.easytrade.access.notifier.AccessProcessor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * This logger will log every valid dm[xml] request independent of its result (success/failure)
 * A valid dm[xml] request has only two requisits:
 * <ul>
 *     <li>A valid dm[xml] request body as determined by {@link RequestParserMethod#invoke()}</li>
 *     <li>A valid Zone as determined by
 *     {@link de.marketmaker.istar.merger.web.ZoneDispatcherServlet#getZoneRequest(HttpServletRequest)}</li></li>
 * </ul>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("JavadocReference")
@Slf4j
public class MoleculeRequestLogger extends HandlerInterceptorAdapter implements InitializingBean {

    private Logger requestLogger;

    private String loggerName;

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.loggerName == null) {
            log.warn("<afterPropertiesSet> requestLogging is disabled");
            return;
        }
        this.requestLogger = LoggerFactory.getLogger(this.loggerName);
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception e) throws Exception {
        if (this.requestLogger == null) {
            return;
        }

        // This will be set by either
        // de.marketmaker.istar.merger.web.ZoneDispatcherServlet#getZoneRequest(HttpServletRequest)
        // or
        // de.marketmaker.istar.merger.web.ZoneInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)
        final Zone zone = (Zone) request.getAttribute(ZONE_ATTRIBUTE);
        if (zone == null) {
            return;
        }

        // This will be set by
        // de.marketmaker.istar.merger.web.easytrade.RequestParserMethod#invoke()
        final MoleculeRequest mr = (MoleculeRequest) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
        if (mr == null) {
            return;
        }

        if (mr.getAuthenticationType() == null) {
            mr.setAuthenticationType(
                getValue(request, ProfileResolver.AUTHENTICATION_TYPE_KEY));
        }
        if (mr.getAuthentication() == null) {
            mr.setAuthentication(getValue(request, ProfileResolver.AUTHENTICATION_KEY));
        }

        if (ProfileResolver.ROOT_AUTHENTICATION_TYPE.equals(mr.getAuthenticationType())) {
            mr.setAuthenticationType(AccessProcessor.ROOT_AUTH_PLACEHOLDER);
        }

        StopWatch sw = StopWatchHolder.getStopWatch();
        if (sw != null) {
            if (sw.isRunning()) {
                sw.stop();
            }
            mr.setMs((int) sw.getTotalTimeMillis());
        }

        this.requestLogger.info(format(zone, mr, e));
    }

    private String format(Zone z, MoleculeRequest mr, Exception e) {
        final String json = GsonUtil.toJson(mr);
        final StringBuilder sb = new StringBuilder(16 + json.length())
                .append(z.getName()).append(' ').append(json);
        if (e != null) {
            sb.append(' ').append(e.getClass().getName());
        }
        return sb.toString();
    }
}
