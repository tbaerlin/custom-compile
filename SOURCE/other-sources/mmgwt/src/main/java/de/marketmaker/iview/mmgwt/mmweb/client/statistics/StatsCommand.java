/*
 * StatsComman.java
 *
 * Created on 12.04.2010 16:02:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;
import java.util.Date;

/**
 * @author oflege
 */
public class StatsCommand implements Serializable {
    public enum IntervalType { DAILY, WEEKLY, MONTHLY }

    protected int client;

    protected Date from;

    protected Date to;

    protected IntervalType intervalType = IntervalType.DAILY;

    protected String[] selectors = new String[4];

    public void setFrom(Date from) {
        this.from = from;
    }

    public void setIntervalType(IntervalType intervalType) {
        this.intervalType = intervalType;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public int getClient() {
        return client;
    }

    public void setClient(int client) {
        this.client = client;
    }

    public Date getFrom() {
        return from;
    }

    public IntervalType getIntervalType() {
        return intervalType;
    }

    public String[] getSelectors() {
        return this.selectors;
    }

    public String getSelector1() {
        return this.selectors[0];
    }

    public void setSelector1(String selector1) {
        this.selectors[0] = selector1;
    }

    public String getSelector2() {
        return this.selectors[1];
    }

    public void setSelector2(String selector2) {
        this.selectors[1] = selector2;
    }

    public String getSelector3() {
        return this.selectors[2];
    }

    public void setSelector3(String selector3) {
        this.selectors[2] = selector3;
    }

    public String getSelector4() {
        return this.selectors[3];
    }

    public void setSelector4(String selector4) {
        this.selectors[3] = selector4;
    }

    public Date getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "client=" + client // $NON-NLS-0$
                + ", from=" + from // $NON-NLS-0$
                + ", to=" + to // $NON-NLS-0$
                + ", interval=" + intervalType // $NON-NLS-0$
                + ", s1=" + getSelector1() // $NON-NLS-0$
                + ", s2=" + getSelector2() // $NON-NLS-0$
                + ", s3=" + getSelector3() // $NON-NLS-0$
                + ", s4=" + getSelector4(); // $NON-NLS-0$
    }
}
