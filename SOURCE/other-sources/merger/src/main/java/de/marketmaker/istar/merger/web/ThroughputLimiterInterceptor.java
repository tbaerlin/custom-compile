/*
 * ThroughputLimiterInterceptor.java
 *
 * Created on 13.12.2012 16:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;

/**
 * @author tkiesgen
 */
@ManagedResource
public class ThroughputLimiterInterceptor extends WebApplicationObjectSupport implements
        InitializingBean, HandlerInterceptor {
    private final Map<String, ThroughputLimiter> limiters = Collections.synchronizedMap(new HashMap<String, ThroughputLimiter>());

    public void setUserid2RequestsPerMinute(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            this.limiters.put(entry.getKey(), new ThroughputLimiter(1, TimeUnit.MINUTES, Integer.parseInt(entry.getValue())));
        }
    }

    @ManagedOperation
    public void setUserid2RequestsPerMinute(String userid, String reqPerMin) {
        this.logger.info("<setUserid2RequestsPerMinute> limit userid " + userid + " to " + reqPerMin + " requests/min");
        this.limiters.put(userid, new ThroughputLimiter(1, TimeUnit.MINUTES, Integer.parseInt(reqPerMin)));
    }

    @ManagedOperation
    public void unsetUserid(String userid) {
        this.logger.info("<unsetUserid> remove userid " + userid);
        this.limiters.remove(userid);
    }

    @ManagedOperation
    public String getLimits() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, ThroughputLimiter> entry : this.limiters.entrySet()) {
            sb.append("limit userid " + entry.getKey() + " to " + entry.getValue() + " requests/min");
        }
        return sb.toString();
    }

    public void afterPropertiesSet() throws Exception {
        for (Map.Entry<String, ThroughputLimiter> entry : this.limiters.entrySet()) {
            this.logger.info("<afterPropertiesSet> limit to " + entry.getValue() + " requests/min for vwd-ID " + entry.getKey());
        }
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile == null || !(profile instanceof VwdProfile)) {
            return true;
        }

        final ThroughputLimiter limiter = this.limiters.get(((VwdProfile) profile).getVwdId());
        if (limiter != null) {
            final float limited = limiter.ackAction();
            if (limited > 0) {
                this.logger.info("<preHandle> enforced throughput limit " + ((VwdProfile) profile).getVwdId() + ", delayed " + limited + " sec");
            }
        }

        return true;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
    }
}
