/*
 * EconodayProviderImpl.java
 *
 * Created on 21.03.12 11:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;
import de.marketmaker.istar.merger.web.econoday.EdFinderTermVisitor;

/**
 * @author zzhao
 */
@Transactional
public class EconodayProviderImpl extends JdbcDaoSupport implements EconodayProvider {

    private static final String SQL_RELEASE_SELECT = "SELECT r.id, r.event_code, r.modify_date, " +
            "r.released_for, r.released_on, r.released_on_gmt, r.previous_date, r.attribute, " +
            "r.article_url, a.type, a.value_type, a.value " +
            "FROM (SELECT r.* FROM t_release r, t_event b WHERE r.event_code = b.code ";

    private static final String SQL_RELEASE_LEFT_JOIN_ASSESSMENT = ") r LEFT JOIN t_assessment a ON r.id = a.release_id";

    private static final String SQL_RELEASE_TOTAL = "SELECT COUNT(r.id)" +
            " FROM t_release r JOIN t_event b" +
            " ON r.event_code = b.code ";

    private String imageUrlPrefix = "http://dmxml-1/iview/econoday/images/";

    private EventFactory eventFactory;

    private AssessmentTypeFactory assessmentTypeFactory;

    private CountryNameProvider countryNameProvider;

    public void setCountryNameProvider(CountryNameProvider countryNameProvider) {
        this.countryNameProvider = countryNameProvider;
    }

    public void setImageUrlPrefix(String imageUrlPrefix) {
        this.imageUrlPrefix = imageUrlPrefix;
    }

    public void setAssessmentTypeFactory(AssessmentTypeFactory assessmentTypeFactory) {
        this.assessmentTypeFactory = assessmentTypeFactory;
    }

    public void setEventFactory(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAvailableEvents() {
        return this.eventFactory.values();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, ReleaseDetail> getReleaseDetails(int[] releaseIds) {
        final HashMap<Integer, ReleaseDetail> map = new HashMap<>();
        final String whereSql = getReleaseIdWhereSql(releaseIds);

        retrieveTexts(map, whereSql);
        retrieveAssessments(map, whereSql);
        retrieveImages(map, whereSql);

        return map;
    }

    private void retrieveImages(final Map<Integer, ReleaseDetail> map, String whereSql) {
        getJdbcTemplate().query("SELECT release_id, type, name, caption" +
                " FROM t_image " + whereSql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                final int releaseId = rs.getInt(1);
                final ReleaseDetail detail = getReleaseDetail(releaseId, map);
                final ImageTypeEnum type = ImageTypeEnum.fromValue(rs.getInt(2));
                final Image image = new Image(rs.getString(3), rs.getString(4),
                        getImageUrl(releaseId, type));
                switch (type) {
                    case Chart_1:
                        detail.setChart1(image);
                        break;
                    case Chart_2:
                        detail.setChart2(image);
                        break;
                    case Grid_1:
                        detail.setGrid1(image);
                        break;
                    default:
                        throw new UnsupportedOperationException("no support for: " + type);
                }
            }
        });
    }

    private String getImageUrl(int releaseId, ImageTypeEnum type) {
        return releaseId + "_" + type.getValue();
    }

    private void retrieveAssessments(final Map<Integer, ReleaseDetail> map, String whereSql) {
        getJdbcTemplate().query("SELECT release_id, type, value_type, value" +
                " FROM t_assessment " + whereSql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                final ReleaseDetail detail = getReleaseDetail(rs.getInt(1), map);
                final AssessmentType assessmentType = assessmentTypeFactory.get((long) rs.getInt(2));
                if (!detail.contains(assessmentType)) {
                    detail.addAssessment(new Assessment(assessmentType));
                }
                final Assessment assessment = detail.getAssessment(assessmentType);
                assessment.addValue(ValueTypeEnum.fromValue(rs.getInt(3)), rs.getString(4));
            }
        });
    }

    private void retrieveTexts(final Map<Integer, ReleaseDetail> map, String whereSql) {
        getJdbcTemplate().query("SELECT release_id, type, UNCOMPRESS(content)" +
                        " FROM t_text" + whereSql,
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        final ReleaseDetail detail = getReleaseDetail(rs.getInt(1), map);
                        final TextTypeEnum type = TextTypeEnum.fromValue(rs.getInt(2));
                        switch (type) {
                            case Highlights:
                                detail.setHighlights(rs.getString(3));
                                break;
                            case ConsensusNotes:
                                detail.setConsensusNotes(rs.getString(3));
                                break;
                            default:
                                throw new UnsupportedOperationException("no support for: " + type);
                        }
                    }
                });
    }

    private ReleaseDetail getReleaseDetail(int releaseId, Map<Integer, ReleaseDetail> map) {
        if (!map.containsKey(releaseId)) {
            map.put(releaseId, new ReleaseDetail(releaseId));
        }
        return map.get(releaseId);
    }

    private String getReleaseIdWhereSql(int[] releaseIds) {
        final StringBuilder sb = new StringBuilder();
        sb.append(" WHERE release_id IN (");
        for (int releaseId : releaseIds) {
            sb.append(releaseId).append(",");
        }

        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public EconodaySearchResponse getReleases(EconodaySearchRequest req) {
        final String restClause = getRestClause(req.getOffset(), req.getCount(),
                req.getSortBy(), req.isAscending());
        if (StringUtils.isBlank(req.getQuery())) {
            return doQuery(getReleaseWhereClause(req.getCountries(), req.getEventCodes(),
                    req.getFrom(), req.getTo()), restClause);
        }
        else {
            return doQuery(" AND " + req.getQuery(), restClause);
        }
    }

    @Override
    public byte[] getImage(final int releaseId, final int imageType) {
        return (byte[]) getJdbcTemplate().queryForObject("SELECT UNCOMPRESS(data) FROM t_image" +
                        " WHERE release_id = ? AND type = ?", new Object[]{releaseId, imageType},
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return Base64.decodeBase64(rs.getString(1));
                    }
                });
    }

    private String getRestClause(int offset, int count, String sortBy, boolean ascending) {
        return " ORDER BY " + sortBy + getOrder(ascending) + ", r.id ASC LIMIT " + offset + ", " + count;
    }

    private EconodaySearchResponse doQuery(String whereClause, String restClause) {
        final List<Release> releases = getReleases(whereClause, restClause);
        final int totalCount = getTotalCount(whereClause);
        final TreeMap<EconodayMetaDataKey, List<FinderMetaItem>> metaData =
                new TreeMap<>();
        for (EconodayMetaDataEnum md : EconodayMetaDataEnum.values()) {
            final List<FinderMetaItem> ml = getMetaData(md, whereClause);
            if (!ml.isEmpty()) {
                metaData.put(EconodayMetaDataKey.fromEnum(md), ml);
            }
        }

        return new EconodaySearchResponse(totalCount, releases, metaData);
    }

    private List<FinderMetaItem> getMetaData(final EconodayMetaDataEnum md, String whereClause) {
        final List<FinderMetaItem> ret = new ArrayList<>();
        final String sql = getMetaDataSql(md, whereClause);
        this.logger.info("<getMetaData> " + sql);
        getJdbcTemplate().query(sql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                final int count = rs.getInt(2);
                if (count > 0) {
                    switch (md) {
                        case Country:
                            final String symbol = rs.getString(1);
                            ret.add(new FinderMetaItem(symbol,
                                    countryNameProvider.getCountryName(symbol, Language.en), count));
                            break;
                        case Event:
                            final String code = rs.getString(1);
                            final Event event = eventFactory.get(code);
                            ret.add(new FinderMetaItem(code, event.getName(), count));
                            break;
                        case Frequency:
                            final int anInt = rs.getInt(1);
                            final FrequencyEnum anEnum = FrequencyEnum.fromValue(anInt);
                            ret.add(new FinderMetaItem(String.valueOf(anInt), anEnum.getInfo(), count));
                            break;
                        default:
                            throw new UnsupportedOperationException("no support for: " + md);
                    }
                }
            }
        });

        return ret;
    }

    private String getMetaDataSql(EconodayMetaDataEnum md, String whereClause) {
        return "SELECT " + md.getColumnName() + ", COUNT(" + md.getColumnName() + ")" +
                " FROM t_release r JOIN t_event b" +
                " ON r.event_code = b.code" + whereClause +
                " GROUP BY " + md.getColumnName();
    }

    private int getTotalCount(String whereClause) {
        return getJdbcTemplate().queryForObject(SQL_RELEASE_TOTAL + whereClause, Integer.class);
    }

    private List<Release> getReleases(String whereClause, String restClause) {
        final List<Release> releases = new ArrayList<>();
        final String sql = SQL_RELEASE_SELECT + whereClause + restClause + SQL_RELEASE_LEFT_JOIN_ASSESSMENT;
        this.logger.info("<doQuery> " + sql);
        getJdbcTemplate().query(sql,
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        final int uid = rs.getInt(1);

                        Release release = releases.isEmpty() ? null : releases.get(releases.size() - 1);
                        if (release == null || release.getUid() != uid) {
                            release = new Release(eventFactory.get(rs.getString(2)), uid);
                            release.setModifyDate(new DateTime(rs.getTimestamp(3)));
                            release.setReleasedFor(rs.getString(4));
                            release.setReleasedOn(new DateTime(rs.getTimestamp(5)));
                            release.setReleasedOnGmt(new DateTime(rs.getTimestamp(6)));
                            release.setPreviousDate(new DateTime(rs.getTimestamp(7)));
                            release.setAttributes(Attribute.fromValue(rs.getInt(8)));
                            release.setArticleUrl(rs.getString(9));
                            releases.add(release);
                        }

                        final AssessmentType assessmentType = assessmentTypeFactory.get((long) rs.getInt(10));
                        if (assessmentType == null) {
                            return;
                        }

                        if (!release.contains(assessmentType)) {
                            release.addAssessment(new Assessment(assessmentType));
                        }
                        final Assessment assessment = release.getAssessment(assessmentType);
                        assessment.addValue(ValueTypeEnum.fromValue(rs.getInt(11)), rs.getString(12));
                    }
                });
        return releases;
    }

    private String getReleaseWhereClause(String[] countries, String[] eventCodes,
            String from, String to) {
        final String countryClause = getWhereInClause("b.country", countries);
        final String eventCodeClause = getWhereInClause("r.event_code", eventCodes);
        final String releaseDateClause = getWhereClause4ReleaseDate(from, to);

        final String clause = join(countryClause, eventCodeClause, releaseDateClause);
        return StringUtils.isBlank(clause) ? StringUtils.EMPTY : " AND " + clause;
    }

    private String join(String... clauses) {
        if (null == clauses) {
            return StringUtils.EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        for (String clause : clauses) {
            if (sb.length() > 0) {
                sb.append(" AND ");
            }
            sb.append(clause);
        }

        return sb.toString();
    }

    private String getOrder(boolean ascending) {
        return ascending ? " ASC" : " DESC";
    }

    private String getWhereInClause(String columnName, String[] array) {
        if (null == array || array.length == 0) {
            return StringUtils.EMPTY;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(columnName).append(" IN (");
        for (String country : array) {
            sb.append("'").append(country).append("',");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }

    private String getWhereClause4ReleaseDate(String from, String to) {
        final StringBuilder sb = new StringBuilder();
        appendConditionIfNotBlank(sb, from, "r.released_on", " >= ");
        appendConditionIfNotBlank(sb, to, "r.released_on", " < ");

        return sb.toString();
    }

    private void appendConditionIfNotBlank(StringBuilder sb, String str, String colName,
            String op) {
        String val = StringUtils.trim(str);
        if (!StringUtils.isBlank(val)) {
            if (sb.length() > 0) {
                sb.append(" AND ");
            }
            sb.append(EdFinderTermVisitor.getLeftOperand(colName, val))
                    .append(op).append("'").append(val).append("'");
        }
    }
}
