package de.marketmaker.istar.merger.provider.lbbwresearch;

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

import static de.marketmaker.istar.domain.instrument.ContentFlags.Flag.ResearchLBBW;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;

/**
 * pushing content-flag data into MDP
 * @see de.marketmaker.istar.merger.provider.gisresearch.GisContentFlagsWriterDb
 */
@Transactional
public class LbbwContentFlagsWriterDb implements ContentFlagsWriter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String INSERT_QUERY = "insert into DP.h_lbbw_research_flags(key, docExists) values (?, ?)";

    private static final String DELETE_QUERY = "delete from DP.h_lbbw_research_flags";

    private static final String SELECT_QUERY = "select key, docExists from DP.h_lbbw_research_flags";

    private static final Map<String, ContentFlags.Flag> COLUMN_TO_FLAG = new HashMap<>();

    static {
        COLUMN_TO_FLAG.put("docExists", ResearchLBBW);
    }

    private JdbcTemplate jdbcTemplate;

    @Override
    public void writeContentFlags(Map<String, Set<ContentFlags.Flag>> map) {
        this.logger.info("<writeContentFlags> #rows=" + map.size());

        if (!flagsHaveChanged(map)) {
            this.logger.info("<writeContentFlags> no changes, returning");
            return;
        }

        jdbcTemplate.execute(DELETE_QUERY);
        InsertFlags insert = new InsertFlags(jdbcTemplate.getDataSource());

        for (Map.Entry<String, ? extends Set<ContentFlags.Flag>> entry : map.entrySet()) {
            insert.update(
                    entry.getKey(),
                    entry.getValue().contains(ResearchLBBW) ? 1 : 0
            );
        }
        insert.flush();
        jdbcTemplate.execute("begin dp.DPLBBW.process_lbbw_research_flags ; end; ");
    }

    public void setDataSource(DataSource ds) {
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.afterPropertiesSet();
    }

    private static class InsertFlags extends BatchSqlUpdate {
        private InsertFlags(DataSource ds) {
            super(ds, INSERT_QUERY, new int[]{VARCHAR, INTEGER}, 100);
        }
    }

    // compare flags in DB with current
    private boolean flagsHaveChanged(Map<String, Set<ContentFlags.Flag>> map) {
        return !getExistingFlags().equals(map);
    }

    private Map<String, Set<ContentFlags.Flag>> getExistingFlags() {
        return jdbcTemplate.query(SELECT_QUERY,
                resultSet -> {
                    final Map<String, Set<ContentFlags.Flag>> result = new HashMap<>();
                    while (resultSet.next()) {
                        result.put(resultSet.getString("key"), getFlags(resultSet));
                    }
                    return result;
                }
        );
    }

    private Set<ContentFlags.Flag> getFlags(ResultSet rs) throws SQLException {
        EnumSet<ContentFlags.Flag> result = EnumSet.noneOf(ContentFlags.Flag.class);
        for (Map.Entry<String, ContentFlags.Flag> entry : COLUMN_TO_FLAG.entrySet()) {
            if (1 == rs.getInt(entry.getKey())) {
                result.add(entry.getValue());
            }
        }
        return result;
    }
}
