/*
 * DateTimeReceived.java
 *
 * Created on 22.10.13 08:03
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.util.Timer;
import java.util.TimerTask;

import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.DateTimeProviderImpl;

/**
 * Every update and recap message needs two fields that contain the current timestamp, this
 * class's singleton INSTANCE provides that message fragment und updates it every second.
 *
 * @author oflege
 */
class DateTimeReceived {
    static final int LENGTH = 26;

    private volatile byte[] dateTimeReceived;

    private DateTimeProvider dtp = DateTimeProviderImpl.INSTANCE;

    private final IqsMessageBuilder builder = new IqsMessageBuilder(LENGTH);

    static final DateTimeReceived INSTANCE = new DateTimeReceived();

    private DateTimeReceived() {
        update(dtp.current());
        new Timer(getClass().getSimpleName(), true).scheduleAtFixedRate(new TimerTask() {
            public void run() {
                update(dtp.current());
            }
        }, 1100 - (System.currentTimeMillis() % 1000), 1000);
    }

    byte[] getDateTimeReceived() {
        return this.dateTimeReceived;
    }

    private void update(DateTimeProvider.Timestamp ts) {
        this.dateTimeReceived = builder.clear()
                .dateHeader(Constants.FID_DATE_RECEIVED, ts.yyyyMmDd)
                .timeHeader(Constants.FID_TIME_RECEIVED, ts.secondOfDay)
                .asBytes();
        assert this.dateTimeReceived.length == LENGTH;
    }
}
