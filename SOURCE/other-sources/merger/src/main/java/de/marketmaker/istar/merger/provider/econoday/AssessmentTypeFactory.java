/*
 * ValueTypeFactory.java
 *
 * Created on 15.03.12 16:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * @author zzhao
 */
public class AssessmentTypeFactory extends AbstractEntityFactory<Long, AssessmentType> {

    protected AssessmentTypeFactory() {
        super("t_assessment_type", "id");
    }

    @Override
    protected Map<Long, AssessmentType> query(long fromRevision) {
        final HashMap<Long, AssessmentType> result = new HashMap<>();
        getJdbcTemplate().query("SELECT id, name, prefix, suffix" +
                " FROM " + getTable() +
                " WHERE id > ?",
                new Object[]{fromRevision},
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        final AssessmentType assessmentType = new AssessmentType(rs.getLong(1),
                                rs.getString(2), rs.getString(3), rs.getString(4));
                        result.put(assessmentType.getRevision(), assessmentType);
                    }
                });

        return result;
    }
}
