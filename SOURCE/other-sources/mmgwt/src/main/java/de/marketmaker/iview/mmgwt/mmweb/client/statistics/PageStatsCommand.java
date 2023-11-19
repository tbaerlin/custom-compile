/*
 * StatsCommand.java
 *
 * Created on 14.12.2009 11:28:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

/**
 * Specifies which information should be retrieved about visited pages.
 * @author oflege
 */
public class PageStatsCommand extends StatsCommand {

    private String module;

    public void setModule(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }

}
