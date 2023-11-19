/*
 * StockAnalysisDaoDb.java
 *
 * Created on 10.08.2006 09:32:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.stockanalysis;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.SerializableCollator;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.data.StockAnalysisAimsImpl;
import de.marketmaker.istar.domainimpl.data.StockAnalysisImpl;
import de.marketmaker.istar.domainimpl.data.StockAnalysisSummaryImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockAnalysisDaoDb extends JdbcDaoSupport implements StockAnalysisDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private SelectAnalysts selectAnalysts;

    private SelectAnalystsWithAnalyses selectAnalystsWithAnalyses;

    private SelectSectors selectSectors;

    private SelectRegions selectRegions;

    private DeleteAnalysis deleteAnalysis;

    private DeleteRating deleteRating;

    private DeleteOldEntries deleteOldEntries;

    private InsertAnalysis insertAnalysis;

    private InsertAnalyst insertAnalyst;

    private SelectAnalysesForInstruments selectAnalysesForInstruments;

    private SelectAnalysisById selectAnalysisById;

    private SelectLatestRating selectLatestRating;

    private SelectAims selectAims;

    private SelectSummaryData selectSummaryData;

    private boolean keepOnlyLatestRating = false;

    private final Map<String, Long> analysts = new HashMap<>();

    private static final SerializableCollator<String> COLLATOR
            = new SerializableCollator<>(Locale.GERMAN);

    private final RowMapper analysisRowMapper = new RowMapper() {
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            return new StockAnalysisImpl(Long.toString(rs.getLong("id")),
                    new DateTime(rs.getTimestamp("analysisdate")),
                    rs.getString("analyst"),
                    rs.getString("headline"),
                    rs.getString("sector"),
                    rs.getString("analysistext"),
                    getInstrumentId(rs),
                    getRecommendation(rs));
        }

        private Long getInstrumentId(ResultSet rs) throws SQLException {
            final long instrumentid = rs.getLong("instrumentid");
            return rs.wasNull() ? null : instrumentid;
        }

        private StockAnalysis.Recommendation getRecommendation(ResultSet rs) throws SQLException {
            final Rating rating
                    = Rating.parse(rs.getInt("ratingid"));
            return (rating != null) ? rating.getRecommendation() : null;
        }
    };

    private static class Analyst {
        private final long id;

        private final String name;

        public Analyst(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class IntermediateResult {
        private final Rating rating;

        private final int count;

        private final DateTime date;

        public IntermediateResult(Rating rating, int count, DateTime date) {
            this.rating = rating;
            this.count = count;
            this.date = date;
        }

        public Rating getRating() {
            return rating;
        }

        public int getCount() {
            return count;
        }

        public DateTime getDate() {
            return date;
        }
    }

    private class SelectSummaryData extends MappingSqlQuery {
        public SelectSummaryData() {
            super(getDataSource(), "SELECT ratingid, count(ratingid) as count, max(analysisdate) as maxdate " +
                    "FROM analyses " +
                    "WHERE instrumentid = ? " +
                    "GROUP BY ratingid");
            declareParameter(new SqlParameter(Types.BIGINT));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            final Rating rating
                    = Rating.parse(rs.getInt("ratingid"));
            final int count = rs.getInt("count");
            final DateTime date = new DateTime(rs.getTimestamp("maxdate"));

            return new IntermediateResult(rating, count, date);
        }

        public List<IntermediateResult> getSummaryData(Instrument instrument) {
            //noinspection unchecked
            return (List<IntermediateResult>) execute(instrument.getId());
        }
    }

    private abstract class SelectAnalyses extends MappingSqlQuery {
        private static final String SELECT
                = "SELECT analyses.id as id, analysisdate, analysts.name as analyst, " +
                "instrumentid, headline, sector, analysistext, ratingid " +
                "FROM analyses,analysts ";

        protected SelectAnalyses(String where) {
            super(getDataSource(), SELECT + where);
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return analysisRowMapper.mapRow(rs, i);
        }
    }

    private class SelectAnalysesForInstruments extends SelectAnalyses {
        public SelectAnalysesForInstruments() {
            super("WHERE analyses.analystid = analysts.id " +
                    "AND instrumentid IN (?, ?, ?, ?, ?) " +
                    "AND ratingid <> 0 " +
                    "ORDER BY analysisdate DESC LIMIT ?");
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.INTEGER));
            compile();
        }

        protected List<StockAnalysis> getAnalyses(List<Instrument> instruments, int maxResults) {
            if (instruments.size() > 5) {
                throw new IllegalArgumentException("too many instruments, max. 5 allowed: " + instruments.size());
            }

            final Object[] args = new Object[6];
            Arrays.fill(args, -1);
            for (int i = 0; i < instruments.size(); i++) {
                args[i] = instruments.get(i).getId();
            }
            args[5] = maxResults;

            //noinspection unchecked
            return (List<StockAnalysis>) execute(args);
        }
    }

    private class SelectAnalysisById extends SelectAnalyses {
        public SelectAnalysisById() {
            super("WHERE analyses.analystid = analysts.id AND analyses.id = ?");
            declareParameter(new SqlParameter(Types.BIGINT));
            compile();
        }

        protected StockAnalysis getAnalysis(String id) {
            return (StockAnalysis) findObject(Long.parseLong(id));
        }
    }

    private class SelectLatestRating extends SelectAnalyses {
        public SelectLatestRating() {
            super("WHERE analyses.analystid = analysts.id" +
                    " AND analyses.analystid = ?" +
                    " AND analyses.instrumentid = ?" +
                    " AND analyses.ratingid <> 0" +
                    " ORDER BY analysisdate DESC LIMIT 1"
            );
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BIGINT));
            compile();
        }

        protected StockAnalysis getAnalysis(Long analystid, Long instrumentid) {
            try {
                return (StockAnalysis) findObject(new Object[]{analystid, instrumentid});
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
    }

    private class SelectAims extends MappingSqlQuery {
        public SelectAims() {
            // TODO: find solution for possibly different currencies
            super(getDataSource(), "SELECT min(aimmin) as minimum, max(aimmax) as maximum, " +
                    "min(currency) as currency " +
                    "FROM analyses WHERE instrumentid = ? GROUP BY instrumentid");
            declareParameter(new SqlParameter(Types.BIGINT));
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            final String currency = rs.getString("currency");
            final BigDecimal min = new BigDecimal(rs.getDouble("minimum"));
            final BigDecimal max = new BigDecimal(rs.getDouble("maximum"));
            return new StockAnalysisAimsImpl(currency, min, max);
        }

        protected StockAnalysisAims getAims(long instrumentid) {
            return (StockAnalysisAims) findObject(instrumentid);
        }
    }

    private class SelectAnalysts extends MappingSqlQuery {
        protected static final String SQL = "SELECT id, name FROM analysts";

        protected SelectAnalysts(String sql) {
            super(getDataSource(), sql);
            compile();
        }

        public SelectAnalysts() {
            this(SQL);
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return new Analyst(rs.getLong("id"), rs.getString("name"));
        }
    }

    private class SelectAnalystsWithAnalyses extends SelectAnalysts {
        public SelectAnalystsWithAnalyses() {
            super(SelectAnalysts.SQL + " WHERE id IN (SELECT DISTINCT analystid FROM analyses " +
                    "WHERE ratingid<>0)");
        }
    }

    private class SelectSectors extends MappingSqlQuery {
        public SelectSectors() {
            super(getDataSource(), "SELECT DISTINCT sector FROM analyses WHERE ratingid <> 0 ");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getString("sector");
        }
    }

    private class SelectRegions extends MappingSqlQuery {
        public SelectRegions() {
            super(getDataSource(), "SELECT DISTINCT region FROM analyses WHERE ratingid <> 0 ");
            compile();
        }

        protected Object mapRow(ResultSet rs, int i) throws SQLException {
            return rs.getString("region");
        }
    }

    private class DeleteAnalysis extends SqlUpdate {
        public DeleteAnalysis() {
            super(getDataSource(), "DELETE FROM analyses WHERE id=?");
            declareParameter(new SqlParameter(Types.BIGINT));
            compile();
        }
    }

    private class DeleteRating extends SqlUpdate {
        public DeleteRating() {
            super(getDataSource(), "DELETE FROM analyses WHERE analystid=? AND instrumentid=? and ratingid>0");
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BIGINT));
            compile();
        }
    }

    private class DeleteOldEntries extends SqlUpdate {
        public DeleteOldEntries() {
            super(getDataSource(), "DELETE FROM analyses WHERE updated < ? or analysisdate < ?");
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            compile();
        }
    }

    private class InsertAnalysis extends SqlUpdate {

        public InsertAnalysis() {
            super(getDataSource(), "INSERT INTO analyses (id," +
                    "analystid,instrumentid,instrumentname,sector,region,ratingid,analysisdate,category," +
                    "subcategory,headline,analysistext,aimmin,aimmax,currency,instrumentsymbols,updated)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())");
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.BIGINT));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.INTEGER));
            declareParameter(new SqlParameter(Types.TIMESTAMP));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.DECIMAL));
            declareParameter(new SqlParameter(Types.DECIMAL));
            declareParameter(new SqlParameter(Types.VARCHAR));
            declareParameter(new SqlParameter(Types.VARCHAR));
            compile();
        }


        public int insert(Long id, Long analystId, Instrument instrument,
                Rating rating, DateTime date, String category,
                String subcategory, String headline, String text, Double aimMin, Double aimMax,
                String currency) {
            return update(new Object[]{
                    id,
                    analystId,
                    instrument != null ? instrument.getId() : null,
                    instrument != null ? instrument.getName() : null,
                    getSector(instrument),
                    getCountry(instrument),
                    rating.getId(),
                    date.toDate(),
                    category,
                    subcategory,
                    headline,
                    text,
                    aimMin,
                    aimMax,
                    currency,
                    getInstrumentSymbolString(instrument)
            });
        }

        private String getInstrumentSymbolString(Instrument instrument) {
            if (instrument == null) {
                return null;
            }
            final StringBuilder stb = new StringBuilder();
            if (StringUtils.hasText(instrument.getSymbolIsin())) {
                stb.append(instrument.getSymbolIsin()).append(" ");
            }
            if (StringUtils.hasText(instrument.getSymbolWkn())) {
                stb.append(instrument.getSymbolWkn()).append(" ");
            }
            if (StringUtils.hasText(instrument.getSymbolTicker())) {
                stb.append(instrument.getSymbolTicker()).append(" ");
            }

            return stb.length() > 0 ? stb.toString().trim() : null;
        }

        private String getCountry(Instrument instrument) {
            if (instrument != null && instrument.getCountry() != null) {
                return instrument.getCountry().getName();
            }
            return null;
        }

        private String getSector(Instrument instrument) {
            if (instrument != null && instrument.getSector() != null
                    && !"Unknown".equals(instrument.getSector().getName())) {
                return instrument.getSector().getName();
            }
            return null;
        }
    }

    private class InsertAnalyst extends SqlUpdate {
        public InsertAnalyst() {
            super(getDataSource(), "INSERT INTO analysts (name) VALUES (?)");
            declareParameter(new SqlParameter(Types.VARCHAR));
            setReturnGeneratedKeys(true);
            compile();
        }

        public long insert(String name) {
            final KeyHolder key = new GeneratedKeyHolder();
            update(new Object[]{name}, key);
            return key.getKey().longValue();
        }
    }

    protected void initDao() throws Exception {
        super.initDao();
        this.selectAnalysts = new SelectAnalysts();
        this.selectAnalystsWithAnalyses = new SelectAnalystsWithAnalyses();
        this.selectSectors = new SelectSectors();
        this.selectRegions = new SelectRegions();
        this.deleteAnalysis = new DeleteAnalysis();
        this.deleteRating = new DeleteRating();
        this.deleteOldEntries = new DeleteOldEntries();
        this.insertAnalysis = new InsertAnalysis();
        this.insertAnalyst = new InsertAnalyst();
        this.selectAnalysesForInstruments = new SelectAnalysesForInstruments();
        this.selectAnalysisById = new SelectAnalysisById();
        this.selectLatestRating = new SelectLatestRating();
        this.selectAims = new SelectAims();
        this.selectSummaryData = new SelectSummaryData();
        readAnalysts();
    }

    public void setKeepOnlyLatestRating(boolean keepOnlyLatestRating) {
        this.keepOnlyLatestRating = keepOnlyLatestRating;
    }

    public Map<String, Long> getAnalysts() {
        return getAnalysts(this.selectAnalysts);
    }

    private Map<String, Long> getAnalysts(SelectAnalysts select) {
        // TODO: auto handling for uncommented section
//        try {
//            final JdbcTemplate jt = new JdbcTemplate(getDataSource());
//            final Scanner s = new Scanner(new File("e:/istar/merger/src/conf/mysql_aktiencheck_analysts.sql"));
//            while(s.hasNextLine()) {
//                final String str = s.nextLine();
//                jt.execute(str);
//            }
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        //noinspection unchecked
        final List<Analyst> analysts = select.execute();

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getAnalysts> #analysts: " + analysts.size());
        }

        final Map<String, Long> result = new TreeMap<>(COLLATOR);
        for (final Analyst analyst : analysts) {
            result.put(analyst.getName(), analyst.getId());
        }
        return result;
    }

    public int deleteAnalysis(long id) {
        return this.deleteAnalysis.update(id);
    }

    public int deleteAnalysisWithRating(long analystId, Instrument instrument) {
        return this.deleteRating.update(analystId, instrument.getId());
    }

    public void deleteOldEntries() {
        Date oldestDate = new DateTime().minusDays(90).toDate();
        final int count = this.deleteOldEntries.update(new Object[]{
                oldestDate, oldestDate
        });
        this.logger.info("<deleteOldEntries> successfully deleted " + count + " entries");
    }

    public int storeAnalysis(Long id, String analyst, Instrument instrument,
            Rating rating, DateTime date, String category,
            String subcategory,
            String headline, String text, Double aimMin, Double aimMax, String currency) {
        final Long analystId = getAnalystId(analyst);

        deleteAnalysis(id);
        if (this.keepOnlyLatestRating && rating != Rating.NO_RATING) {
            final StockAnalysis analysis
                    = this.selectLatestRating.getAnalysis(analystId, instrument.getId());
            if (analysis != null) {
                if (analysis.getDate().isAfter(date)) {
                    this.logger.info("<storeAnalysis> " + id + " " 
                            + date + " older than "
                            + analysis.getId() + " " + analysis.getDate() + ": ignored");
                    return -1;
                }
                // ours is later, remove existing
                deleteAnalysis(Long.parseLong(analysis.getId()));
            }
        }
        return this.insertAnalysis.insert(id, analystId, instrument, rating, date, category,
                subcategory, headline, text, aimMin, aimMax, currency);
    }

    public List<StockAnalysis> getAnalyses(List<Instrument> instruments, int maxResults) {
        return this.selectAnalysesForInstruments.getAnalyses(instruments, maxResults);
    }

    public StockAnalysisSummary getSummaryData(Instrument instrument) {
        final List<IntermediateResult> perRating = this.selectSummaryData.getSummaryData(instrument);
        if (perRating.isEmpty()) {
            return new StockAnalysisSummaryImpl(null, 0, 0, 0, 0, 0);
        }

        int numberOfBuys = 0;
        int numberOfStrongBuys = 0;
        int numberOfHolds = 0;
        int numberOfSells = 0;
        int numberOfStrongSells = 0;
        DateTime date = perRating.get(0).getDate();

        for (final IntermediateResult ir : perRating) {
            if (date.isBefore(ir.getDate())) {
                date = ir.getDate();
            }
            switch (ir.getRating()) {
                case BUY:
                    numberOfBuys = ir.getCount();
                    break;
                case STRONG_BUY:
                    numberOfStrongBuys = ir.getCount();
                    break;
                case HOLD:
                    numberOfHolds = ir.getCount();
                    break;
                case SELL:
                    numberOfSells = ir.getCount();
                    break;
                case STRONG_SELL:
                    numberOfStrongSells = ir.getCount();
                    break;
            }
        }

        return new StockAnalysisSummaryImpl(date, numberOfBuys, numberOfStrongBuys, numberOfHolds,
                numberOfSells, numberOfStrongSells);
    }

    public Map<String, Map<?, String>> getMetaData() {
        final Map<String, Map<?, String>> metadata = new HashMap<>();

        final Map<Rating, String> ratings
                = new LinkedHashMap<>();
        ratings.put(Rating.STRONG_BUY, "Kaufen (hoch)");
        ratings.put(Rating.BUY, "Kaufen");
        ratings.put(Rating.HOLD, "Halten");
        ratings.put(Rating.SELL, "Verkaufen");
        ratings.put(Rating.STRONG_SELL, "Verkaufen (hoch)");
        metadata.put("ratings", ratings);

        final Map<String, String> analystsMeta = new TreeMap<>(COLLATOR);
        final Map<String, Long> analysts = getAnalysts(this.selectAnalystsWithAnalyses);
        for (final String name : analysts.keySet()) {
            analystsMeta.put(name, name);
        }
        metadata.put("analysts", analystsMeta);

        //noinspection unchecked
        final List<String> sectorList = this.selectSectors.execute();
        final Map<String, String> sectors = new TreeMap<>(COLLATOR);
        for (final String sector : sectorList) {
            if (sector == null) {
                continue;
            }
//            if("NULL".equals(sector)) {
//                continue;
//            }
            sectors.put(sector, sector);
        }
        metadata.put("sectors", sectors);

        //noinspection unchecked
        final List<String> regionList = this.selectRegions.execute();
        final Map<String, String> regions = new TreeMap<>(COLLATOR);
        for (final String region : regionList) {
            if (region == null) {
                continue;
            }
//            if("NULL".equals(sector)) {
//                continue;
//            }
            regions.put(region, region);
        }
        metadata.put("regions", regions);

        return metadata;
    }

    @Override
    public StockAnalysisResponse getAnalyses(StockAnalysisRequest request) {
        if (request.isAggregatedResultType() && request.getInstrumentids().isEmpty()) {
            throw new IllegalArgumentException("aggregated results not allowed for empty instrument lists");
        }

        final StringBuilder stb = new StringBuilder(100)
                .append(" FROM analyses,analysts")
                .append(" WHERE analyses.analystid = analysts.id");

        if (!request.getRatings().isEmpty()) {
            stb.append(" AND ratingid IN (");
            for (final Rating rating : request.getRatings()) {
                if (stb.charAt(stb.length() - 1) != '(') {
                    stb.append(",");
                }
                stb.append(Integer.toString(rating.getId()));
            }
            stb.append(")");
        }
        else {
            stb.append(" AND ratingid <> 0");
        }

        if (!request.getInstrumentids().isEmpty()) {
            stb.append(" AND instrumentid IN (");
            for (final Long id : request.getInstrumentids()) {
                if (stb.charAt(stb.length() - 1) != '(') {
                    stb.append(",");
                }
                stb.append(id.toString());
            }
            stb.append(")");
        }

        final List<Object> argumentList = new ArrayList<>();

        if (StringUtils.hasText(request.getSource())) {
            stb.append(" AND analysts.name = ?");
            argumentList.add(request.getSource());
        }
        if (StringUtils.hasText(request.getRegion())) {
            stb.append(" AND region = ?");
            argumentList.add(request.getRegion());
        }
        if (StringUtils.hasText(request.getSector())) {
            stb.append(" AND sector = ?");
            argumentList.add(request.getSector());
        }
        if (StringUtils.hasText(request.getSearchtext())) {
            stb.append(" AND (headline LIKE ? OR analysistext LIKE ? OR instrumentsymbols LIKE ?)");
            argumentList.add("%" + request.getSearchtext() + "%");
            argumentList.add("%" + request.getSearchtext() + "%");
            argumentList.add("%" + request.getSearchtext() + "%");
        }
        if (request.getStart() != null) {
            stb.append(" AND analysisdate >= ?");
            argumentList.add(request.getStart().toDateTimeAtStartOfDay().toDate());
        }
        if (request.getEnd() != null) {
            stb.append(" AND analysisdate <= ?");
            argumentList.add(request.getEnd().plusDays(1).toDateTimeAtStartOfDay().toDate());
        }

        final Object[] args = argumentList.toArray();

        if (request.isAggregatedResultType()) {
            stb.append(" AND ratingid > 0 AND ratingid < 6 AND instrumentid IS NOT NULL")
                    .append(" GROUP BY instrumentid, ratingid");
            return createAggregatedResult(stb.toString(), args);
        }
        else {
            return createResult(stb.toString(), getSuffix(request), args);
        }
    }

    private String getSuffix(StockAnalysisRequest request) {
        final StringBuilder result = new StringBuilder(100);
        if ("ratingid".equals(request.getSortBy())) {
            result.append(" AND ratingid > 0 && ratingid < 6");
        }
        if ("instrumentname".equals(request.getSortBy())) {
            result.append(" AND instrumentname IS NOT NULL");
        }
        if ("sector".equals(request.getSortBy())) {
            result.append(" AND sector IS NOT NULL");
        }
        result.append(" ORDER BY ").append(request.getSortBy())
                .append(request.isAscending() ? " ASC" : " DESC");
        if (!"analysisdate".equals(request.getSortBy())) {
            result.append(", analysisdate DESC");            
        }
        return result.append(" LIMIT ").append(request.getAnzahl())
                .append(" OFFSET ").append(request.getOffset())
                .toString();
    }

    private StockAnalysisResponse createAggregatedResult(String stmt,
            Object[] args) {
        final String prefix = "SELECT instrumentid, ratingid, count(ratingid) as num";

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<createAggregatedResult> stmt = " + prefix
                    + stmt + ", args = " + Arrays.toString(args));
        }

        final Map<Long, Map<StockAnalysis.Recommendation, Integer>> result
                = new HashMap<>();

        //noinspection unchecked
        getJdbcTemplate().query(prefix + stmt, args, new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
                final long iid = rs.getLong("instrumentid");
                final Rating rating = Rating.parse(rs.getInt("ratingid"));
                final StockAnalysis.Recommendation recommendation = rating.getRecommendation();
                final int num = rs.getInt("num");

                Map<StockAnalysis.Recommendation, Integer> map = result.get(iid);
                if (map == null) {
                    map = new HashMap<>();
                    result.put(iid, map);
                }

                Integer count = map.get(recommendation);
                map.put(recommendation, count == null ? num : num + count);
            }
        });

        final StockAnalysisResponse response = new StockAnalysisResponse();
        response.setCountsByInstrumentid(result);
        return response;
    }

    private StockAnalysisResponse createResult(String stmt, String suffix,
            Object[] args) {
        final String selectPrefix
                = "SELECT analyses.id as id, analysisdate, analysts.name as analyst, instrumentid, " +
                "headline, sector, analysistext, ratingid";
        final String countPrefix = "SELECT count(*)";

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<createResult> stmt = " + countPrefix + stmt + ", args = "
                    + Arrays.toString(args));
        }
        final int totalCount = getJdbcTemplate()
                .queryForObject(countPrefix + stmt, Integer.class, args);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<createResult> stmt = " + selectPrefix + stmt + suffix
                    + ", args = " + Arrays.toString(args));
        }

        //noinspection unchecked
        final List<StockAnalysis> analyses = getJdbcTemplate().query(selectPrefix + stmt + suffix, args,
                this.analysisRowMapper);

        final StockAnalysisResponse response = new StockAnalysisResponse();
        response.setTotalCount(totalCount);
        response.setAnalyses(analyses);
        return response;
    }

    public StockAnalysisAims getAims(long instrumentid) {
        return this.selectAims.getAims(instrumentid);
    }

    public List<StockAnalysis> getAnalyses(List<String> analysisids) {
        final List<StockAnalysis> result = new ArrayList<>(analysisids.size());
        for (final String id : analysisids) {
            result.add(this.selectAnalysisById.getAnalysis(id));
        }
        return result;
    }

    private void readAnalysts() {
        final Map<String, Long> analysts = getAnalysts();
        synchronized (this.analysts) {
            this.analysts.clear();
            for (Map.Entry<String, Long> entry : analysts.entrySet()) {
                this.analysts.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        this.logger.info("<readAnalysts> read " + this.analysts.size() + " analysts");
    }

    private Long getAnalystId(String analyst) {
        synchronized (this.analysts) {
            final Long analystId = this.analysts.get(analyst.toLowerCase());
            if (analystId != null) {
                return analystId;
            }

            final long result = this.insertAnalyst.insert(analyst);
            this.analysts.put(analyst.toLowerCase(), result);
            this.logger.info("<getAnalystId> inserted '" + analyst + "', id=" + result);

            return result;
        }
    }

}
