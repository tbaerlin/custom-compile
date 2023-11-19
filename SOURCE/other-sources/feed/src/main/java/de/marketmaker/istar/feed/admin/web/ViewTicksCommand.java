/*
 * ViewStatisticsCommand.java
 *
 * Created on 25.04.2005 09:03:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import org.joda.time.DateTimeConstants;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewTicksCommand extends AbstractTicksCommand {

    private int day = DateUtil.getDate(-1);

    @Override
    public String getQueryString() {
        return super.getQueryString() + "&day=" + getDay();
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

}
