/*
 * TopStatsCommand.java
 *
 * Created on 12.04.2010 16:06:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

/**
 * @author oflege
 */
public class TopStatsCommand extends StatsCommand {
    public enum Subject { BROWSER, IP_ADDRESS, PAGE }

    private int count = 20;

    private Subject subject = Subject.BROWSER;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
