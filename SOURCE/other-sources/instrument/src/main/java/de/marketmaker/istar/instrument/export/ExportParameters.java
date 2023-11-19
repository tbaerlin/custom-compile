/*
 * ExportParameters.java
 *
 * Created on 30.06.15 13:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.domainimpl.DomainContextImpl;

/**
 * @author oflege
 */
class ExportParameters {
    private String listOfSecurities = "";

    private Integer forLastHours;

    private String whichsystem = "p";

    private DomainContextImpl domainContext;

    DomainContextImpl getDomainContext() {
        return this.domainContext;
    }

    boolean isForLastHours() {
        return this.forLastHours != null;
    }

    void setDomainContext(DomainContextImpl domainContext) {
        this.domainContext = domainContext;
    }

    void setListOfSecurities(String listOfSecurities) {
        this.listOfSecurities = listOfSecurities;
    }

    void setForLastHours(int forLastHours) {
        this.forLastHours = (forLastHours > 0) ? forLastHours : null;
    }

    void setWhichsystem(String whichsystem) {
        this.whichsystem = whichsystem;
    }

    Map<String, Object> getInParameters() {
        Map<String, Object> m = new HashMap<>();
        m.put("vlistofsecurities", this.listOfSecurities);
        m.put("vforlasthours", this.forLastHours);
        m.put("vwhichsystem", this.whichsystem);
        return m;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder()
                .append("whichsystem='").append(this.whichsystem).append("'");
        if (StringUtils.hasText(this.listOfSecurities)) {
            sb.append(", listOfSecurities='").append(this.listOfSecurities).append("'");
        }
        if (this.forLastHours != null) {
            sb.append(", forLastHours=").append(this.forLastHours);
        }
        return sb.toString();
    }
}
