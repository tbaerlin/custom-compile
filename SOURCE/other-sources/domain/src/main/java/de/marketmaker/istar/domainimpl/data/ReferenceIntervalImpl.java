/*
 * ReferenceIntervalImpl.java
 *
 * Created on 20.09.2006 15:46:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;

import org.joda.time.Interval;

import de.marketmaker.istar.domain.data.ReferenceInterval;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ReferenceIntervalImpl implements Serializable, ReferenceInterval {
    private final Interval interval;
    private final boolean shortenedFiscalYear;

    public ReferenceIntervalImpl(Interval interval, boolean shortenedFiscalYear) {
        this.interval = interval;
        this.shortenedFiscalYear = shortenedFiscalYear;
    }

    public Interval getInterval() {
        return interval;
    }

    public boolean isShortenedFiscalYear() {
        return shortenedFiscalYear;
    }

    public String toString() {
        return "ReferenceIntervalImpl[interval=" + interval
                + ", shortenedFiscalYear=" + shortenedFiscalYear
                + "]";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReferenceIntervalImpl that = (ReferenceIntervalImpl) o;

        if (shortenedFiscalYear != that.shortenedFiscalYear) {
            return false;
        }

        return interval.equals(that.interval);
    }

    public int hashCode() {
        int result;
        result = interval.hashCode();
        result = 29 * result + (shortenedFiscalYear ? 1 : 0);
        return result;
    }
}
