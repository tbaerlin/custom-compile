/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.request;

import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AbstractIstarRequest implements IstarRequest {
    static final long serialVersionUID = 7059807933787617900L;

    protected static final String CLIENT_ID = ManagementFactory.getRuntimeMXBean().getName();

    private static final AtomicLong REQUEST_ID = new AtomicLong();

    private final String clientInfo;

    private Locale locale = Locale.GERMAN;

    public AbstractIstarRequest() {
        this.clientInfo = getDefaultInfo();
    }

    private String getDefaultInfo() {
        return CLIENT_ID + "," + System.currentTimeMillis() + "," + REQUEST_ID.incrementAndGet();
    }

    public AbstractIstarRequest(String clientInfo, boolean withDefaultInfo) {
        this.clientInfo = (withDefaultInfo ? getDefaultInfo() + ":" : "") + clientInfo;
    }

    public String getClientInfo() {
        return this.clientInfo;
    }

    @Override
    public String toString() {
        final StringBuilder sb =
                new StringBuilder(100).append(getClass().getName()).append("[").append(this.clientInfo);
        appendToString(sb);
        return sb.append("]").toString();
    }

    protected void appendToString(StringBuilder sb) {
        // empty, subclasses can override        
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
