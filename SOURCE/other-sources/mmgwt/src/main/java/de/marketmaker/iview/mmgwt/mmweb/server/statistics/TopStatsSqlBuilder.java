/*
 * PageStatsSqlBuilder.java
 *
 * Created on 13.01.2010 10:13:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.springframework.dao.DataAccessException;

import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsResult;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.TopStatsCommand;

/**
 * Builds/executes sql used to query information about top ranked visits by certain users in
 * a certain period.
 * @author oflege
 */
class TopStatsSqlBuilder extends StatsSqlBuilder<TopStatsCommand> {

    TopStatsSqlBuilder(TopStatsCommand command) {
        super(command);
    }

    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        final String column = getColumn();
        final String dateColumn = getDateColumn();
        final String table = getTable();
        final StringBuilder sb = new StringBuilder();
        sb.append("select ").append(column);
        if (isPageTop()) {
            sb.append(", sum(num) c, ");
        }
        else {
            sb.append(", count(").append(column).append(") c, ");
        }
        sb.append("DATE_FORMAT(").append(dateColumn).append(", '")
                .append(getDateFormat())
                .append("') d from ").append(table);
        sb.append(" WHERE client=?");
        addParameter(Types.INTEGER, this.command.getClient());

        appendSelectors(sb);
        appendInterval(sb, dateColumn);

        sb.append(" GROUP BY ").append(column).append(", d")
                .append(" ORDER BY d asc, c desc");

        this.logger.info("<createPreparedStatement> " + sb.toString() + " " + this.parameters);
        return connection.prepareStatement(sb.toString());
    }

    private String getTable() {
        return isPageTop() ? "page_aggregations" : "visits_processed";
    }

    private String getDateColumn() {
        return isPageTop() ? "day" : "created";
    }

    private boolean isPageTop() {
        return this.command.getSubject() == TopStatsCommand.Subject.PAGE;
    }

    private String getColumn() {
        switch (this.command.getSubject()) {
            case IP_ADDRESS:
                return "ip";
            case BROWSER:
                return "user_agent";
            case PAGE:
                return "page_def";
            default:
                throw new IllegalStateException();
        }
    }

    public StatsResult extractData(ResultSet rs) throws SQLException, DataAccessException {
        // since we cannot limit the number of rows based on groups in the group by clause,
        // we ensure the limit when we read all rows:
        final StatsResult result = new StatsResult();
        int lastDate = 0;
        int numPerDate = 0;
        while (rs.next()) {
            final int nextLastDate = rs.getInt(3);
            if (nextLastDate != lastDate) {
                numPerDate = 0;
                lastDate = nextLastDate;
            }
            if (numPerDate++ < this.command.getCount()) {
                result.add(rs.getString(1), rs.getInt(2), lastDate);
            }
        }
        return result;
    }
}