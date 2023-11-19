/*
 * ViewStatisticsCommand.java
 *
 * Created on 25.04.2005 09:03:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ViewDpfilesCommand extends AbstractFeedAdminCommand {
    private String trigger;

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
}
