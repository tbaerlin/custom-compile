/*
 * DefaultZoneResolver.java
 *
 * Created on 14.08.2006 16:08:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * Inspects HttpRequest objects to determine the {@link @Zone} the request refers to.
 */
public class DefaultZoneResolver implements ZoneResolver, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ZoneProvider zoneProvider;

    private final Map<Pattern, String> zoneMappings =
            Collections.synchronizedMap(new LinkedHashMap<Pattern, String>());

    public void setZoneProvider(ZoneProvider zoneProvider) {
        this.zoneProvider = zoneProvider;
    }

    public String toString() {
        return "DefaultZoneResolver[" + this.zoneMappings + "]";
    }

    public void setZoneMappings(Map<String, String> zoneMappings) {
        for (Map.Entry<String, String> entry : zoneMappings.entrySet()) {
            this.zoneMappings.put(Pattern.compile(entry.getKey()), entry.getValue());
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (this.zoneMappings == null || this.zoneMappings.isEmpty()) {
            throw new IllegalArgumentException("zoneMappings not set");
        }

        Set<String> undefinedZones = new HashSet<>();
        for (String zoneName : this.zoneMappings.values()) {
            if (!"$1".equals(zoneName) && getZone(zoneName) == null) {
                this.logger.error("<afterPropertiesSet> undefined zone: " + zoneName);
                undefinedZones.add(zoneName);
            }
        }
        if (!undefinedZones.isEmpty()) {
            throw new IllegalArgumentException("undefined zone(s) in mappings: " + undefinedZones);
        }
    }

    public Zone getZone(String id) {
        return this.zoneProvider.getZone(id);
    }

    public Zone resolveZone(HttpServletRequest request) {
        if (this.zoneMappings == null || this.zoneMappings.isEmpty()) {
            return null;
        }

        // resolve by session only (last action resolveByAttribute uses session, too, but
        // additionally checks for parameters; do not lookup parameters in the first step
        final Zone sessionZone = resolveBySession(request);
        if (sessionZone != null) {
            return sessionZone;
        }

        final Zone uriZone = resolveByURI(request);
        if (uriZone != null) {
            return uriZone;
        }

        final Zone attributeZone = resolveByAttribute(request);
        if (attributeZone != null) {
            return attributeZone;
        }

        return null;
    }

    private Zone resolveBySession(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        return session != null
                ? getZone((String) session.getAttribute("zone"))
                : null;
    }

    private Zone resolveByAttribute(HttpServletRequest request) {
        return getZone(HttpRequestUtil.getValue(request, "zone"));
    }

    private Zone resolveByURI(HttpServletRequest request) {
        final String variant = HttpRequestUtil.getValue(request, "variant");

        final String uri = request.getRequestURI();
        for (Map.Entry<Pattern, String> entry : this.zoneMappings.entrySet()) {
            final Matcher m = entry.getKey().matcher(uri);
            if (m.reset(uri).find()) {
                final String id = m.groupCount() > 0 ? m.group(1) : entry.getValue();

                if (StringUtils.hasText(variant)) {
                    final Zone zone = getZone(id + "-" + variant);
                    if (zone != null) {
                        return zone;
                    }
                }
                return getZone(id);
            }
        }

        return null;
    }
}


