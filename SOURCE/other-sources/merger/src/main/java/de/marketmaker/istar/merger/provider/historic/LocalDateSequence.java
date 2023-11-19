/*
 * LocalDateSequence.java
 *
 * Created on 14.08.2009 09:18:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.DateTimeConstants;

/**
 * A sequence of LocalDate values where LocalDate<sub>n+1</sub> is derived from
 * LocalDate<sub>n</sub> by adding a fixed Period to it. The elements of the sequence are obtained
 * by calling {@link #getNext()} repeatedly.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class LocalDateSequence {
    private LocalDate ld;

    private Period period;

    /**
     * Creates new LocalDateSequence based on a given startDate
     * @param startDate yyyy-MM-dd base date for aligning this sequence as follows:<dl>
     * <dt>n days
     * <dd>no alignment, first date is n days after startDate
     * <dt>n weeks
     * <dd>first date is the monday in the week n weeks after yyyy-MM-dd
     * <dt>n months
     * <dd>first date is the first day in the month n months after yyyy-MM-dd, and if n > 1
     * and 12 % n == 0, the month will be aligned so that month % n = 0
     * <dt>n years
     * <dd>first date is the fist day in the year n years after yyyy-MM-dd, and if n > 1,
     * the year will be aligned such that year % n == 0;
     * </dl>
     * @param period must match "P\d+[DWMY]" that is, n days, weeks, months, or years, but not
     * a combination of different periods such as P1M2D
     * @return new LocalDateSequence
     */
    static LocalDateSequence create(LocalDate startDate, Period period) {
        if (period.getDays() > 0 && period.equals(Period.days(period.getDays()))) {
            return new LocalDateSequence(firstDay(startDate, period), period);
        }
        else if (period.getWeeks() > 0 && period.equals(Period.weeks(period.getWeeks()))) {
            return new LocalDateSequence(firstWeek(startDate, period), period);
        }
        else if (period.getMonths() > 0 && period.equals(Period.months(period.getMonths()))) {
            if (period.getMonths() % 12 == 0) {
                return create(startDate, Period.years(period.getMonths() / 12));
            }
            return new LocalDateSequence(firstMonth(startDate, period), period);
        }
        else if (period.getYears() > 0 && period.equals(Period.years(period.getYears()))) {
            return new LocalDateSequence(firstYear(startDate, period), period);
        }
        throw new IllegalArgumentException("value " + String.valueOf(period)
                + " doesn't match pattern \"P\\d+[DWMY]\"");
    }

    private static LocalDate firstDay(LocalDate startDate, Period period) {
        return startDate.plusDays(period.getDays());
    }

    private static LocalDate firstWeek(LocalDate startDate, Period period) {
        final LocalDate result = startDate.plusWeeks(period.getWeeks());
        if (result.getDayOfWeek() == DateTimeConstants.MONDAY) {
            return result;
        }
        return result.minusDays(result.getDayOfWeek() - 1);
    }

    private static LocalDate firstMonth(LocalDate startDate, Period period) {
        final int n = period.getMonths();
        final LocalDate ld = startDate.withDayOfMonth(1).plusMonths(n);
        if (n == 1 || (12 % n) != 0) {
            return ld;
        }
        // align so that year is covered by 12 / n periods
        final int mod = (ld.getMonthOfYear() - 1) % n;
        if (mod != 0) {
            return ld.minusMonths(mod);
        }
        return ld;
    }

    private static LocalDate firstYear(LocalDate startDate, Period period) {
        if (period.getYears() == 1) {
            return startDate.withDayOfYear(1).plusYears(period.getYears());
        }
        final int n = period.getYears();
        final int mod = ((startDate.getYear() + 1) % n);
        return new LocalDate(startDate.getYear() + 1 + (n - mod), 1, 1);
    }

    private LocalDateSequence(LocalDate ld, Period period) {
        this.ld = ld;
        this.period = period;
    }

    LocalDate getNext() {
        final LocalDate result = this.ld;
        this.ld = this.ld.plus(this.period);
        return result;
    }
}
