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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import de.marketmaker.iview.mmgwt.mmweb.client.statistics.StatsResult;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.PageStatsCommand;

/**
 * Builds/executes sql used to query information about pages visited by certain users in
 * a certain period.
 * @author oflege
 */
class PageStatsSqlBuilder extends StatsSqlBuilder<PageStatsCommand>
        implements PreparedStatementCreator, PreparedStatementSetter, ResultSetExtractor<StatsResult> {

    public PageStatsSqlBuilder(PageStatsCommand command) {
        super(command);
    }

    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
        final StringBuilder sb = new StringBuilder();
        sb.append("select pd.id, sum(pa.num) num, DATE_FORMAT(pa.day, '")
                .append(getDateFormat())
                .append("') d from page_aggregations pa, page_defs pd");
        sb.append(" WHERE pa.page_def=pd.id");
        sb.append(" AND pa.client=?");
        addParameter(Types.INTEGER, this.command.getClient());

        appendSelectors(sb);
        appendInterval(sb, "pa.day");

        if (this.command.getModule() != null) {
            sb.append(" AND pd.module=?");
            addParameter(Types.VARCHAR, this.command.getModule());
        }

        sb.append(" GROUP BY pa.page_def, d");
        sb.append(" ORDER BY pd.name, pd.id, d");

        this.logger.info("<createPreparedStatement> " + sb.toString() + " " + this.parameters);
        return connection.prepareStatement(sb.toString());
    }

    public StatsResult extractData(ResultSet rs) throws SQLException, DataAccessException {
        final StatsResult result = new StatsResult();
        while (rs.next()) {
            result.add(rs.getString(1), rs.getInt(2), rs.getInt(3));
        }
        return result;
    }
}
