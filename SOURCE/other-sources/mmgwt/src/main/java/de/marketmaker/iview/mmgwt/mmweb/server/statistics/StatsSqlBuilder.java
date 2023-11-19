/*
 * StatsSqlBuilder.java
 *
 * Created on 12.04.2010 16:18:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsCommand;

/**
 * @author oflege
 */
abstract class StatsSqlBuilder<T extends StatsCommand>
        implements PreparedStatementCreator, PreparedStatementSetter, ResultSetExtractor<StatsResult> {
    private static final int MAX_DAILY_STATS = 14;

    private static final int MAX_WEEKLY_STATS = 12;

    private static class Parameter {
        private final int type;

        private final Object value;

        private Parameter(int type, Object value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(this.value);
        }
    }

    protected final Log logger = LogFactory.getLog(getClass());

    protected final List<Parameter> parameters = new ArrayList<>();

    protected final T command;

    protected StatsSqlBuilder(T command) {
        this.command = command;
    }

    protected void addParameter(int type, Object value) {
        this.parameters.add(new Parameter(type, value));
    }

    protected Interval getInterval() {
        final DateTime from = new DateTime(this.command.getFrom());
        final DateTime to = new DateTime(this.command.getTo());
        final DateTime today = new LocalDate().toDateTimeAtStartOfDay();
        final DateTime max = getMax(from, today);
        return new Interval(from, max.isBefore(to) ? max : to);
    }

    private DateTime getMax(DateTime from, DateTime today) {
        switch (this.command.getIntervalType()) {
            case DAILY:
                return getMinDate(from, Period.days(MAX_DAILY_STATS), today);
            case WEEKLY:
                return getMinDate(from, Period.weeks(MAX_WEEKLY_STATS), today);
            default:
                return today;
        }
    }

    private DateTime getMinDate(DateTime from, Period p, DateTime min) {
        if (from.plus(p).isBefore(min)) {
            return from.plus(p);
        }
        return min;
    }

    protected String getDateFormat() {
        switch (this.command.getIntervalType()) {
            case DAILY:
                return "%Y%j"; // yyyyddd
            case WEEKLY:
                return "%Y%u"; // yyyyww
            case MONTHLY:
                return "%Y%m"; // yyyyMM
        }
        throw new IllegalStateException();
    }

    public void setValues(PreparedStatement ps) throws SQLException {
        for (int i = 0; i < this.parameters.size();) {
            final Parameter parameter = this.parameters.get(i);
            ps.setObject(++i, parameter.value, parameter.type);
        }
    }

    protected void appendSelector(StringBuilder sb, int i, String value) {
        sb.append(" AND selector").append(i).append("=?");
        addParameter(Types.VARCHAR, value);
    }

    protected void appendSelectors(StringBuilder sb) {
        // selectors[n] is more specific than selectors[n-1] and identifies a subset, so it is
        // sufficient to append the non-null selector value for the highest n
        for (int i = this.command.getSelectors().length; i-- > 0; ) {
            if (this.command.getSelectors()[i] != null) {
                appendSelector(sb, i + 1, this.command.getSelectors()[i]);
                return;
            }
        }
    }

    protected void appendDate(StringBuilder sb, String dateColumn, boolean less, Date d) {
        sb.append(" AND ").append(dateColumn).append(less ? " <" : " >=").append(" ?");
        addParameter(Types.DATE, d);
    }

    protected void appendInterval(StringBuilder sb, String dateColumn) {
        final Interval interval = getInterval();
        appendDate(sb, dateColumn, false, new Date(interval.getStartMillis()));
        appendDate(sb, dateColumn, true, new Date(interval.getEndMillis()));
    }
}
