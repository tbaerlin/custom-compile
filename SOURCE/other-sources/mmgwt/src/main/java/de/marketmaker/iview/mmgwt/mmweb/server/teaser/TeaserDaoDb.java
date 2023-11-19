package de.marketmaker.iview.mmgwt.mmweb.server.teaser;

import de.marketmaker.istar.merger.web.Zone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TeaserDaoDb extends JdbcDaoSupport {
    public static final String NEXT_VERSION = "next";
    public static final String CURRENT_VERSION = "current";

    protected Log logger = LogFactory.getLog(getClass());

    private static final String COUNT = "select count(*) from teaser where module_name = ? and version = ?";

    private static final String INSERT = "insert into teaser"
            + " (module_name, version, teaser_enabled, link_enabled, size, content_type, link_url, filename, width, height, link_target, image_data)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE = "update teaser set "
            + " teaser_enabled = ?, link_enabled = ?, size = ?, content_type = ?, link_url = ?, filename = ?, "
            + " width = ?, height = ?, link_target = ?, image_data = ?"
            + " where module_name = ? and version = ?";

    private static final String SELECT = ""
            + "select * from teaser where module_name = ? and version = ?";


    public void storeRecord(Zone zone, String version, final TeaserRecordImpl record) {
        try {
            // not using a transaction here :-/
            int count = getJdbcTemplate().queryForObject(COUNT, Integer.class, zone.getName(), version);
            if (count == 0) {
                getJdbcTemplate().update(INSERT,
                        new PreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps) throws SQLException {
                                int i = 0;
                                ps.setString(++i, record.getModuleName());
                                ps.setString(++i, record.getVersion());
                                ps.setBoolean(++i, record.getTeaserEnabled());
                                ps.setBoolean(++i, record.getLinkEnabled());
                                ps.setInt(++i, record.getSize());
                                ps.setString(++i, record.getContentType());
                                ps.setString(++i, record.getLinkUrl());
                                ps.setString(++i, record.getFilename());
                                ps.setInt(++i, record.getWidth());
                                ps.setInt(++i, record.getHeight());
                                ps.setString(++i, record.getLinkTarget());
                                ps.setBytes(++i, record.getImageData());
                            }
                        });
            }
            else {
                getJdbcTemplate().update(UPDATE,
                        new PreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps) throws SQLException {
                                int i = 0;
                                ps.setBoolean(++i, record.getTeaserEnabled());
                                ps.setBoolean(++i, record.getLinkEnabled());
                                ps.setInt(++i, record.getSize());
                                ps.setString(++i, record.getContentType());
                                ps.setString(++i, record.getLinkUrl());
                                ps.setString(++i, record.getFilename());
                                ps.setInt(++i, record.getWidth());
                                ps.setInt(++i, record.getHeight());
                                ps.setString(++i, record.getLinkTarget());
                                ps.setBytes(++i, record.getImageData());
                                // primary key / where clause:
                                ps.setString(++i, record.getModuleName());
                                ps.setString(++i, record.getVersion());
                            }
                        });
            }
        } catch (DataAccessException ex) {
            logger.error("<storeRecord> can't store record, zone is '" + zone + "' version is: '" + version + "'", ex);
        }
    }

    public TeaserRecordImpl findRecord(Zone zone, String version) {
        TeaserRecordImpl result = null;
        try {
            SqlRowSet rowSet = getJdbcTemplate().queryForRowSet(SELECT, zone.getName(), version);
            if (rowSet.first()) { // there can be only one!
                result = new TeaserRecordImpl();
                result.setModuleName(rowSet.getString("module_name"));
                result.setVersion(rowSet.getString("version"));
                result.setTeaserEnabled(rowSet.getBoolean("teaser_enabled"));
                result.setLinkEnabled(rowSet.getBoolean("link_enabled"));
                result.setSize(rowSet.getInt("size"));
                result.setContentType(rowSet.getString("content_type"));
                result.setLinkUrl(rowSet.getString("link_url"));
                result.setFilename(rowSet.getString("filename"));
                result.setWidth(rowSet.getInt("width"));
                result.setHeight(rowSet.getInt("height"));
                result.setLinkTarget(rowSet.getString("link_target"));
                result.setImageData(((ResultSetWrappingSqlRowSet) rowSet).getResultSet().getBytes("image_data"));
            }
        } catch (SQLException ex) {
            logger.error("<findRecord> can't find record, module_name is '" + zone + "' version is: '" + version + "',"
                    + " returning null", ex);
        }
        return result;
    }

}
