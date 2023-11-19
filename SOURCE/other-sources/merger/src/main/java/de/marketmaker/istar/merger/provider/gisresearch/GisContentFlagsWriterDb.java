/*
 * ContentFlagsWriterDb.java
 *
 * Created on 29.04.14 14:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.transaction.annotation.Transactional;

import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.merger.provider.ContentFlagsWriter;

import static de.marketmaker.istar.domain.instrument.ContentFlags.Flag.*;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;

/**
 * @author oflege
 */
@Transactional
public class GisContentFlagsWriterDb implements ContentFlagsWriter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Map<String, ContentFlags.Flag> COL_TO_FLAG = new HashMap<>();

    static {
        COL_TO_FLAG.put("hm1", ResearchDzHM1);
        COL_TO_FLAG.put("hm2", ResearchDzHM2);
        COL_TO_FLAG.put("hm3", ResearchDzHM3);
        COL_TO_FLAG.put("fp4", ResearchDzFP4);
    }

    private JdbcTemplate jdbcTemplate;

    private static class InsertFlags extends BatchSqlUpdate {
        private InsertFlags(DataSource ds) {
            super(ds, "insert into DP.h_gis_research_flags(key, hm1, hm2, hm3, fp4) values (?, ?, ?, ?, ?)",
                    new int[]{VARCHAR, INTEGER, INTEGER, INTEGER, INTEGER}, 100);
            afterPropertiesSet();
        }
    }

    public void setDataSource(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
        this.jdbcTemplate.afterPropertiesSet();
    }

    @Override
    public void writeContentFlags(Map<String, Set<ContentFlags.Flag>> map) {
        this.logger.info("<writeContentFlags> #rows=" + map.size());

        if (!flagsHaveChanged(map)) {
            this.logger.info("<writeContentFlags> no changes, returning");
            return;
        }

        jdbcTemplate.execute("delete from DP.h_gis_research_flags ");
        InsertFlags insert = new InsertFlags(this.jdbcTemplate.getDataSource());
        for (Map.Entry<String, ? extends Set<ContentFlags.Flag>> e : map.entrySet()) {
            insert.update(e.getKey()
                    , e.getValue().contains(ResearchDzHM1) ? 1 : 0
                    , e.getValue().contains(ResearchDzHM2) ? 1 : 0
                    , e.getValue().contains(ResearchDzHM3) ? 1 : 0
                    , e.getValue().contains(ResearchDzFP4) ? 1 : 0
            );
        }
        insert.flush();
        processGisResearchFlags();
    }

    private boolean flagsHaveChanged(Map<String, Set<ContentFlags.Flag>> map) {
        return !getExistingFlags().equals(map);
    }

    private Map<String, Set<ContentFlags.Flag>> getExistingFlags() {
        return this.jdbcTemplate.query("select key, hm1, hm2, hm3, fp4 from DP.h_gis_research_flags",
                rs -> {
                    final Map<String, Set<ContentFlags.Flag>> result = new HashMap<>();
                    while (rs.next()) {
                        result.put(rs.getString("key"), getFlags(rs));
                    }
                    return result;
                }
        );
    }

    private Set<ContentFlags.Flag> getFlags(ResultSet rs) throws SQLException {
        EnumSet<ContentFlags.Flag> result = EnumSet.noneOf(ContentFlags.Flag.class);
        for (Map.Entry<String, ContentFlags.Flag> e : COL_TO_FLAG.entrySet()) {
            if (1 == rs.getInt(e.getKey())) {
                result.add(e.getValue());
            }
        }
        return result;
    }

    private void processGisResearchFlags() {
        this.logger.info("<processGisResearchFlags> ...");
        this.jdbcTemplate.execute("begin dp.dpcmsdata.process_gis_research_flags ; end;");
    }
}
