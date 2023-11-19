/*
 * IpoDataImpl.java
 *
 * Created on 15.09.2006 14:08:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.CompanyDate;
import de.marketmaker.istar.domain.data.LocalizedString;
import org.joda.time.YearMonthDay;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyDateImpl implements CompanyDate, Serializable {
    protected static final long serialVersionUID = 1L;

    private final Long instrumentid;
    private final LocalizedString event;
    private final YearMonthDay date;

    public CompanyDateImpl(Long instrumentid, LocalizedString event, YearMonthDay date) {
        this.instrumentid = instrumentid;
        this.event = event;
        this.date = date;
    }

    public Long getInstrumentid() {
        return instrumentid;
    }

    public LocalizedString getEvent() {
        return event;
    }

    public YearMonthDay getDate() {
        return date;
    }

    public String toString() {
        return "CompanyDateImpl[iid=" + instrumentid
                + ", date=" + date
                + ", event=" + event
                + "]";
    }
}
