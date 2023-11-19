/*
 * CompanyDateDaysRequest.java
 *
 * Created on 11.06.2010 11:14:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author oflege
 */
public class CompanyDateDaysResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private List<LocalDate> daysWithEvents;

    public CompanyDateDaysResponse(List<LocalDate> daysWithEvents) {
        this.daysWithEvents = daysWithEvents;
    }

    public List<LocalDate> getDaysWithEvents() {
        return this.daysWithEvents;
    }
}