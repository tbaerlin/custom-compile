/*
 * TimeUtil.java
 *
 * Created on 03.03.2011 17:05:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.amqprpc.impl;

/**
 * @author oflege
 */
public class TimeUtil {
    private static final long ONE_SECOND = 1000;

    private static final long ONE_MINUTE = 60 * ONE_SECOND;

    private static final long ONE_HOUR = 60 * ONE_MINUTE;

    public static String formatMillis(long n) {
        final long h = n / ONE_HOUR;
        final long m = (n % ONE_HOUR) / ONE_MINUTE;
        final long s = (n % ONE_MINUTE) / ONE_SECOND;
        final long ms = (n % ONE_SECOND);
        return h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s + "." +
                (ms < 10 ? "00" : (ms < 100 ? "0" : "")) + ms;
    }

}