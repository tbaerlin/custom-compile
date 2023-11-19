/*
 * DayProviderStatic.java
 *
 * Created on 08.12.2004 13:26:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateTimeProviderStatic implements DateTimeProvider {

    private volatile Timestamp timestamp;

    public DateTimeProviderStatic() {
        this.timestamp = new Timestamp(new DateTime());
    }

    public DateTimeProviderStatic(DateTime dt) {
        this.timestamp = new Timestamp(dt);
    }

    public void setDayAndTime(DateTime dt) {
        this.timestamp = new Timestamp(dt);
    }

    public Timestamp current() {
        return this.timestamp;
    }

    public int dayAsYyyyMmDd() {
        return this.timestamp.yyyyMmDd;
    }

    public int secondOfDay() {
        return this.timestamp.secondOfDay;
    }
}
