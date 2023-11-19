/*
 * AbstractIstarResponse.java
 *
 * Created on 02.03.2005 14:19:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.request;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AbstractIstarResponse implements IstarResponse {
    static final long serialVersionUID = -4877688481145307708L;

    private boolean valid = true;

    protected static final String SERVER_ID = ManagementFactory.getRuntimeMXBean().getName();

    private static final AtomicLong RESPONSE_ID = new AtomicLong();

    private final String serverInfo;

    public AbstractIstarResponse() {
        this.serverInfo = SERVER_ID + "," + System.currentTimeMillis() + "," + RESPONSE_ID.incrementAndGet();
    }

    public AbstractIstarResponse(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public String getServerId() {
        final int i = this.serverInfo.indexOf(',');
        return (i > 0) ? this.serverInfo.substring(0, i) : this.serverInfo;
    }

    public void setInvalid() {
        this.valid = false;
    }

    public boolean isValid() {
        return this.valid;
    }

    /**
     * Final version of toString that renders all the relevant information in this class and then,
     * if the response is valid, calls {@link #appendToString(StringBuilder)} which subclasses
     * can override to add their specific information
     * @return a string representation of the object.
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(100)
                .append(getClass().getName()).append("[")
                .append(this.serverInfo)
                .append(", valid=").append(this.valid);
        if (isValid()) {
            appendToString(sb);
        }
        return sb.append("]").toString();
    }

    protected void appendToString(StringBuilder sb) {
        // empty, subclasses can override
    }
}
