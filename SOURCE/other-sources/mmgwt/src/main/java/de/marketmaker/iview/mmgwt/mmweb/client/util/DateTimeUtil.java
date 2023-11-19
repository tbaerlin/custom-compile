/*
 * DateTimeUtil.java
 *
 * Created on 21.05.2008 12:31:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.extjs.gxt.ui.client.util.DateWrapper;


/**
 * @author Ulrich Maurer
 */
public class DateTimeUtil {
    public static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;
    public static final DateTimeFormat DATE_FORMAT_DMY = DateTimeFormat.getFormat("dd.MM.yyyy"); // $NON-NLS-0$

    public static final String PERIOD_KEY_YEAR_TO_DATE = "year-to-date"; // $NON-NLS-0$

    public static final String PERIOD_KEY_ALL = "all"; // $NON-NLS-0$

    public enum PeriodEnum {
        PT0S(0L),
        PT1S(1000L),
        PT2S(2000L),
        PT5S(5000L),
        PT1M(60000L),
        PT5M(300000L),
        PT30M(1800000L),
        PT1H(3600000L),
        P1D(86400000L),
        P100D(8640000000L);

        private final long millis;

        private PeriodEnum(long millis) {
            this.millis = millis;
        }

        public long getMillis() {
            return this.millis;
        }
    }

    public enum PeriodMode {
        PAST, FUTURE
    }


    public static Date nowMinus(String period) {
        return minus(new Date(), period);
    }

    public static Date nowPlus(String period) {
        return plus(new Date(), period);
    }

    public static Date plus(Date d, String period) {
        return applyPeriod(d, period, true);
    }

    public static Date minus(Date d, String period) {
        return applyPeriod(d, period, false);
    }

    private static Date applyPeriod(final Date date, final String period, boolean add) {
        boolean dateMode = true;
        DateWrapper result = new DateWrapper(date);
        final StringBuilder sb = new StringBuilder();
        final char[] cp = period.toCharArray();
        for (char c : cp) {
            DateWrapper.Unit interval = null;
            int mult = 1;
            switch (c) {
                case 'P':
                case 'p':
                    dateMode = true; // date mode
                    break;
                case 'Y':
                case 'y':
                    interval = DateWrapper.Unit.YEAR;
                    break;
                case 'M':
                case 'm':
                    interval = dateMode ? DateWrapper.Unit.MONTH : DateWrapper.Unit.MINUTE;
                    break;
                case 'D':
                case 'd':
                    interval = DateWrapper.Unit.DAY;
                    break;
                case 'W':
                case 'w':
                    mult = 7;
                    interval = DateWrapper.Unit.DAY;
                    break;

                case 'T':
                case 't':
                    dateMode = false; // time mode
                    break;

                case 'H':
                case 'h':
                    interval = DateWrapper.Unit.HOUR;
                    break;
                // case 'M': see above
                case 'S':
                case 's':
                    interval = DateWrapper.Unit.SECOND;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    sb.append(c);
                    break;
            }
            if (interval != null) {
                if (!add) {
                    mult = -mult;
                }
                result = result.add(interval, mult * Integer.parseInt(sb.toString()));
                sb.setLength(0);
            }
        }
        return result.asDate();
    }
}
