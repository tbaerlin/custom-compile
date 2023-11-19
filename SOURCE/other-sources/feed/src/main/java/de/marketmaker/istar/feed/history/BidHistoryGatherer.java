/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
@ManagedResource(description = "Bid history gatherer")
public class BidHistoryGatherer extends HistoryGathererTickBA {

    @Override
    public TickType getTickType() {
        return TickType.BID;
    }
}
