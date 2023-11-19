/*
 * AssessmentTypeConsolidator.java
 *
 * Created on 08.01.13 14:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.annotation.Transactional;

import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author zzhao
 */
@Transactional
public class AssessmentTypeConsolidator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Transactional
    public void consolidate() {
        this.logger.info("<consolidate> start");
        final TimeTaker tt = new TimeTaker();
        final Map<Type, Integer> types = new HashMap<>();
        this.jdbcTemplate.query("SELECT MIN(id), name, prefix, suffix" +
                " FROM t_assessment_type GROUP BY name, prefix, suffix",
                new ResultSetExtractor() {
                    @Override
                    public Object extractData(
                            ResultSet rs) throws SQLException, DataAccessException {
                        while (rs.next()) {
                            types.put(new Type(rs.getString(2), rs.getString(3), rs.getString(4)),
                                    rs.getInt(1));
                        }

                        return null;
                    }
                });

        final Map<Integer, List<Integer>> mapping = new HashMap<>();
        for (Integer id : types.values()) {
            mapping.put(id, new ArrayList<Integer>());
        }
        for (final Map.Entry<Type, Integer> entry : types.entrySet()) {
            final Type key = entry.getKey();
            final Integer typeId = entry.getValue();
            final List<Integer> list = mapping.get(typeId);
            this.jdbcTemplate.query(
                    EventReleaseImporterImpl.AssessmentTypeHolder.getSql(key.prefix, key.suffix),
                    EventReleaseImporterImpl.AssessmentTypeHolder.getSqlParam(key.name, key.prefix, key.suffix),
                    new ResultSetExtractor() {
                        @Override
                        public Object extractData(
                                ResultSet rs) throws SQLException, DataAccessException {
                            while (rs.next()) {
                                final int id = rs.getInt(1);
                                if (id != typeId) {
                                    list.add(id);
                                }
                            }
                            return null;
                        }
                    });
        }

        final int typeCountBefore =
                queryForInt("SELECT COUNT(DISTINCT type) FROM t_assessment");
        this.logger.info("<consolidate> type count before in assessment: " + typeCountBefore);

        int cnt = 0;
        for (Map.Entry<Integer, List<Integer>> entry : mapping.entrySet()) {
            final Integer key = entry.getKey();
            final List<Integer> list = entry.getValue();
            final int[] cnts = this.jdbcTemplate.batchUpdate("UPDATE t_assessment SET type = ? WHERE type = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, key);
                            ps.setInt(2, list.get(i));
                        }

                        @Override
                        public int getBatchSize() {
                            return list.size();
                        }
                    });
            for (int i : cnts) {
                cnt += i;
            }
            this.logger.info("<consolidate> updated: " + cnt + " in " + tt);
        }

        final int typeCountAfter =
                queryForInt("SELECT COUNT(DISTINCT type) FROM t_assessment");
        this.logger.info("<consolidate> type count after in assessment: " + typeCountAfter);

        final int typeCountBeforeAssessment =
                queryForInt("SELECT COUNT(id) FROM t_assessment_type");
        this.logger.info("<consolidate> assessment type count before cleanup: " + typeCountBeforeAssessment);

        for (final List<Integer> list : mapping.values()) {
            this.jdbcTemplate.batchUpdate("DELETE FROM t_assessment_type WHERE id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, list.get(i));
                        }

                        @Override
                        public int getBatchSize() {
                            return list.size();
                        }
                    });
        }

        final int typeCountAfterAssessment =
                queryForInt("SELECT COUNT(id) FROM t_assessment_type");
        this.logger.info("<consolidate> assessment type count after cleanup: " + typeCountAfterAssessment);
    }

    protected Integer queryForInt(String sql) {
        return this.jdbcTemplate.queryForObject(sql, Integer.class);
    }

    private static final class Type {
        private final String name;

        private final String prefix;

        private final String suffix;

        private Type(String name, String prefix, String suffix) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Type type = (Type) o;

            if (!name.equals(type.name)) return false;
            if (prefix != null ? !prefix.equals(type.prefix) : type.prefix != null) return false;
            if (suffix != null ? !suffix.equals(type.suffix) : type.suffix != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
            result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
            return result;
        }

        public String toString() {
            return getKey(this.name, this.prefix, this.suffix);
        }
    }

    private static String getKey(String name, String prefix, String suffix) {
        return name + "_" + String.valueOf(prefix) + "_" + String.valueOf(suffix);
    }

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext ctx = null;
        try {
            ctx = new ClassPathXmlApplicationContext("assessment_consolidator_ctx.xml",
                    AssessmentTypeConsolidator.class);
            final AssessmentTypeConsolidator consolidator = (AssessmentTypeConsolidator) ctx.getBean("consolidator");
            consolidator.consolidate();
        } finally {
            if (null != ctx) {
                ctx.close();
            }
        }
    }
}
