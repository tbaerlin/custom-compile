/*
 * TickTimeseries.java
 *
 * Created on 08.01.13 11:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.util.Iterator;

import org.joda.time.ReadableInterval;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.Tick;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author oflege
 */
public class TickTimeseries extends AbstractTickTimeseries<Tick> {

    private final TickType type;

    public TickTimeseries(TickRecordImpl tickRecord, TickType type, ReadableInterval interval) {
        super(tickRecord, interval);
        this.type = type;
    }

    public Iterator<DataWithInterval<Tick>> iterator() {
        final TickWithInterval twi = new TickWithInterval(this.type);

        return new RawTickIterator<Tick>(this, this.type) {
            protected DataWithInterval<Tick> getData(RawTick rt, ReadableInterval i) {
                twi.rawTick = rt;
                twi.interval = i;
                return twi;
            }
        };
    }
}
