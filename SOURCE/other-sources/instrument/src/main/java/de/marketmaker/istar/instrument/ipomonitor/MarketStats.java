/*
 * MarketStats.java
 *
 * Created on 22.08.13 10:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.ipomonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * @author oflege
 */
class MarketStats {

    static class Daily {

        int numCreated;
        int numIndexed;

        int numComplete;

        void ack(Item item) {
            numCreated++;
            if (item.isIndexed()) {
                numIndexed++;
            }
            if (item.isComplete()) {
                numComplete++;
            }
        }

    }
    private Map<LocalDate, Daily> stats = new HashMap<>();

    public MarketStats(List<Item> value) {
        for (Item item : value) {
            ack(item);
        }

    }

    Set<LocalDate> getDates() {
        return this.stats.keySet();
    }

    Daily getStats(LocalDate ld) {
        return this.stats.get(ld);
    }

    private void ack(Item item) {
        Daily daily = stats.get(item.created().toLocalDate());
        if (daily == null) {
            stats.put(item.created().toLocalDate(), daily = new Daily());
        }
        daily.ack(item);
    }
}
