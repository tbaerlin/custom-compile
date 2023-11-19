/*
 * CompanyDateDaysRequest.java
 *
 * Created on 11.06.2010 11:14:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author oflege
 */
public class CompanyDateDaysRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private LocalDate from;

    private LocalDate to;

    private boolean wmAllowed;

    private boolean convensysAllowed;

    public CompanyDateDaysRequest(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }

    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getTo() {
        return to;
    }

    public boolean isWmAllowed() {
        return wmAllowed;
    }

    public void setWmAllowed(boolean wmAllowed) {
        this.wmAllowed = wmAllowed;
    }

    public boolean isConvensysAllowed() {
        return convensysAllowed;
    }

    public void setConvensysAllowed(boolean convensysAllowed) {
        this.convensysAllowed = convensysAllowed;
    }
}
