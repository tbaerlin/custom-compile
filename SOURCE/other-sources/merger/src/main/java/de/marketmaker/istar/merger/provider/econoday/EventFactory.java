/*
 * EventFactory.java
 *
 * Created on 15.03.12 15:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * @author zzhao
 */
public class EventFactory extends AbstractEntityFactory<String, Event> {

    public EventFactory() {
        super("t_event");
        initRevision(0);
    }

    @Override
    protected Map<String, Event> query(long fromRevision) {
        final HashMap<String, Event> result = new HashMap<>();
        getJdbcTemplate().query("SELECT code, name, country, frequency, " +
                "UNCOMPRESS(definition), UNCOMPRESS(description), dmldate" +
                " FROM " + getTable() +
                " WHERE dmldate > ?",
                new Object[]{new Timestamp(fromRevision)},
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        final Event event =
                                new Event(rs.getTimestamp("dmldate").getTime(), rs.getString(1));
                        event.setName(rs.getString(2));
                        event.setCountry(rs.getString(3));
                        event.setFrequency(FrequencyEnum.fromValue(rs.getInt(4)));
                        event.setDefinition(rs.getString(5));
                        event.setDescription(rs.getString(6));

                        result.put(event.getCode(), event);
                    }
                });

        return result;
    }
}
