/*
 * VersionableResource.java
 *
 * Created on 25.05.11 09:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import de.marketmaker.istar.common.monitor.Resource;

/**
 * @author zzhao
 */
public class DBResource extends Resource {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String DEFAULT_COL = "DMLDATE" ;

    private final JdbcTemplate jt;

    private final String column;

    private int type;

    /**
     * Constructs an instance of database resource with the given name of a table, which contains the
     * given column to be monitored.
     *
     * @param jt
     * @param table
     * @param column
     * @throws Exception
     */
    public DBResource(JdbcTemplate jt, String table, String column) throws Exception {
        super(table);
        this.jt = jt;
        this.column = column;
        jt.query("SELECT " + column + " FROM " + table + " LIMIT 1", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                type = rs.getMetaData().getColumnType(1);
            }
        });
    }

    /**
     * Constructs an instance of database resource with the given name of a table, which contains a
     * column named {@value #DEFAULT_COL} to be monitored.
     *
     * @param jt
     * @param table
     * @throws Exception
     */
    public DBResource(JdbcTemplate jt, String table) throws Exception {
        this(jt, table, DEFAULT_COL);
    }

    @Override
    public long lastModified() {
        final AtomicReference<Long> ref = new AtomicReference<>();
        jt.query("SELECT MAX(" + this.column + ") FROM " + getResourceKey(),
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        switch (type) {
                            case Types.BIGINT:
                            case Types.INTEGER:
                                ref.set(rs.getLong(1));
                                break;
                            case Types.TIMESTAMP:
                                ref.set(rs.getTimestamp(1).getTime());
                                break;
                            default:
                                throw new UnsupportedOperationException("not support " + type + " yet");
                        }
                    }
                });

        return ref.get();
    }

    @Override
    public void testModifiedAfter(long time) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<testModifiedAfter> checked at " + new Date(time));
        }
        final long lastModified = lastModified();
        if (lastModified > getPreviousModified()) {
            fireAndSetModifiedTime(lastModified);
        }
    }
}
