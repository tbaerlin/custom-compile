/*
 * DayProviderImpl.java
 *
 * Created on 08.12.2004 12:32:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class DateTimeProviderImpl implements DateTimeProvider {
    private static volatile Timestamp timestamp = new Timestamp(new DateTime());

    /**
     * used to trigger periodic actions
     */
    private static final Timer TIMER = new Timer("DayAndTimeProviderImpl", true);

    public static final DateTimeProviderImpl INSTANCE = new DateTimeProviderImpl();

    static {
        TIMER.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                update();
            }
        }, 1000 - (System.currentTimeMillis() % 1000), 100);
    }

    private static void update() {
        try {
            timestamp = new Timestamp(new DateTime());
        } catch (Exception e) {
            // ignore, hack to hide bug in joda
        }
    }

    private DateTimeProviderImpl() {
    }

    public Timestamp current() {
        return timestamp;
    }

    public int dayAsYyyyMmDd() {
        return timestamp.yyyyMmDd;
    }

    public int secondOfDay() {
        return timestamp.secondOfDay;
    }

    public static void main(String[] args) throws InterruptedException {
        final DateTimeProvider dtp = INSTANCE;
        for (int i = 0; i < 10; i++) {
            System.out.println(dtp.current());
            Thread.sleep(900);
        }
    }
}
