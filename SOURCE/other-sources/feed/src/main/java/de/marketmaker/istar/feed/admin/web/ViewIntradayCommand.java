/*
 * ViewStatisticsCommand.java
 *
 * Created on 25.04.2005 09:03:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewIntradayCommand extends AbstractTicksCommand {
    private boolean namesort;

    private boolean reverseOrder = true;

    private String view = "snap";

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public boolean isNamesort() {
        return namesort;
    }

    public void setNamesort(boolean namesort) {
        this.namesort = namesort;
    }

    public boolean isReverseOrder() {
        return reverseOrder;
    }

    public void setReverseOrder(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }

    public boolean isSnapView() {
        return "snap".equals(this.view);
    }

    @Override
    public int getDay() {
        return DateUtil.dateToYyyyMmDd();
    }

    public String toString() {
        return "ViewIntradayCommand["
            + "key=" + getKey()
            + ", view="  + view
            + ", from="  + getFrom()
            + ", to="  + getTo()
            + ", onlyTrades="  + onlyTrades
            + "]";
    }
}
