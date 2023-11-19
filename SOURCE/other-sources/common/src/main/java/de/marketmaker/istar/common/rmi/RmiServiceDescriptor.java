/*
 * RmiServiceDescriptor.java
 *
 * Created on 02.03.2005 10:36:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.rmi;

import java.rmi.Remote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id$
 */
public class RmiServiceDescriptor implements Comparable<RmiServiceDescriptor> {
    private int priority;
    private String serviceUrl;
    private Remote service;

    public RmiServiceDescriptor() {
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public void setService(Remote service) {
        this.service = service;
    }

    public int getPriority() {
        return priority;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public Remote getService() {
        return service;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final RmiServiceDescriptor that = (RmiServiceDescriptor) o;

        return priority == that.priority && serviceUrl.equals(that.serviceUrl);
    }

    public int hashCode() {
        int result;
        result = priority;
        result = 31 * result + serviceUrl.hashCode();
        return result;
    }

    public int compareTo(RmiServiceDescriptor cmp) {
        final int diff = this.priority - cmp.priority;
        if (diff != 0) {
            return diff;
        }

        return this.serviceUrl.compareTo(cmp.serviceUrl);
    }

    public String toString() {
        return "RmiServiceDescriptor[prio=" + priority
                + ", serviceUrl=" + serviceUrl
                + ", service=" + service
                + "]";
    }
}
