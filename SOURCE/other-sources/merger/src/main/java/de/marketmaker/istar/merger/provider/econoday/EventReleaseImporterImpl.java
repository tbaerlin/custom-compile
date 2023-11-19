/*
 * EventExtractor.java
 *
 * Created on 16.03.12 12:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author zzhao
 */
@Transactional
public class EventReleaseImporterImpl implements InitializingBean, EventReleaseImporter {
    private static final String EVENT_CODE_2_IGNORE = "DLSAVING";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static SimpleDateFormat createDTF(String pattern, TimeZone tz) {
        final SimpleDateFormat ret = new SimpleDateFormat(pattern, Locale.ENGLISH);
        ret.setTimeZone(tz);
        return ret;
    }

    private static SimpleDateFormat createDTF(String pattern) {
        return createDTF(pattern, TimeZone.getDefault());
    }

    static final SimpleDateFormat DTF_MODIFY_DATE = createDTF("MM/dd/yyyy hh:mm:ss a",
            TimeZone.getTimeZone("US/Eastern"));

    static final SimpleDateFormat DTF_RELEASE = createDTF("MMMM dd, yyyy HH:mm",
            TimeZone.getTimeZone("US/Eastern"));

    static final SimpleDateFormat DTF_RELEASE_ON_GMT = createDTF("MMMM dd, yyyy HH:mm",
            TimeZone.getTimeZone("GMT"));

    static final DecimalFormat DF_VALUE = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);

    static {
        DF_VALUE.applyPattern("#,###,###,##0.#####");
        DF_VALUE.setParseBigDecimal(true);
    }

    private static final XMLInputFactory XIF = XMLInputFactory.newInstance();

    private static final Pattern ET = Pattern.compile("(ET)$");

    private JdbcTemplate jt;

    private final StringBuilder buffer = new StringBuilder();

    private EventHolder event = new EventHolder();

    private TextPool textPool;

    private AssessmentPool assessmentPool;

    private AssessmentTypePool assessmentTypePool;

    private ImagePool imagePool;

    public void setDataSource(DataSource dataSource) {
        this.jt = new JdbcTemplate(dataSource);
    }

    private JdbcTemplate getJdbcTemplate() {
        return jt;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.textPool = new TextPool(2, TextHolder::new);
        this.assessmentPool = new AssessmentPool(6, AssessmentHolder::new);
        this.assessmentTypePool = new AssessmentTypePool(6, AssessmentTypeHolder::new);
        this.imagePool = new ImagePool(3, ImageHolder::new);
    }

    @Override
    public int execute(InputStream is) throws Exception {
        XMLStreamReader reader = null;
        try {
            reader = XIF.createXMLStreamReader(is);
            String ln;
            int event;
            int count = 0;
            TextHolder textHolder = null;
            AssessmentTypeHolder assessmentTypeHolder = null;
            ImageHolder imageHolder = null;

            do {
                event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        ln = reader.getLocalName();
                        if ("HIGHLIGHTS".equalsIgnoreCase(ln)) {
                            textHolder = this.textPool.getTextHolder(this.event.uid,
                                    TextTypeEnum.Highlights);
                        }
                        else if ("CONSENSUSNOTES".equalsIgnoreCase(ln)) {
                            textHolder = this.textPool.getTextHolder(this.event.uid,
                                    TextTypeEnum.ConsensusNotes);
                        }
                        else if ("VALUE".equalsIgnoreCase(ln)) {
                            assessmentTypeHolder = this.assessmentTypePool.getHolder();
                            assessmentTypeHolder.from = this.assessmentPool.getSize();
                        }
                        else if ("CHART_1".equalsIgnoreCase(ln)) {
                            imageHolder = this.imagePool.getImageHolder(this.event.uid, ImageTypeEnum.Chart_1);
                        }
                        else if ("CHART_2".equalsIgnoreCase(ln)) {
                            imageHolder = this.imagePool.getImageHolder(this.event.uid, ImageTypeEnum.Chart_2);
                        }
                        else if ("GRID_1".equalsIgnoreCase(ln)) {
                            imageHolder = this.imagePool.getImageHolder(this.event.uid, ImageTypeEnum.Grid_1);
                        }
                        break;
                    case XMLStreamConstants.CDATA:
                    case XMLStreamConstants.CHARACTERS:
                        this.buffer.append(reader.getText());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        ln = reader.getLocalName();
                        if ("EVENT".equalsIgnoreCase(ln)) {
                            count += storeEvent();
                            if (count % 50 == 0) {
                                this.logger.info("<execute> imported " + count + " event release(s)");
                            }
                        }
                        else if ("NAME".equalsIgnoreCase(ln)) {
                            this.event.name = getCurrentString();
                        }
                        else if ("MODIFYDATE".equalsIgnoreCase(ln)) {
                            this.event.modifyDate = getCurrentDateTime(DTF_MODIFY_DATE, true);
                        }
                        else if ("COUNTRY".equalsIgnoreCase(ln)) {
                            this.event.country = getCurrentString();
                        }
                        else if ("UID".equalsIgnoreCase(ln)) {
                            this.event.uid = getCurrentInt();
                        }
                        else if ("EVENT_CODE".equalsIgnoreCase(ln)) {
                            this.event.code = getCurrentString();
                        }
                        else if ("DEFINITION".equalsIgnoreCase(ln)) {
                            this.event.definition = getCurrentString();
                        }
                        else if ("DESCRIPTION".equalsIgnoreCase(ln)) {
                            this.event.description = getCurrentString();
                        }
//                    else if ("EVENT_ID".equalsIgnoreCase(ln)) {
//                        // ignore event id since it can be obtained by combining event code and uid
//                        sb.setLength(0);
//                    }
                        else if ("RELEASED_FOR".equalsIgnoreCase(ln)) {
                            this.event.releasedFor = getCurrentString();
                        }
                        else if ("RELEASED_ON".equalsIgnoreCase(ln)) {
                            this.event.releasedOn = getCurrentDateTime(DTF_RELEASE, true);
                        }
                        else if ("RELEASED_ON_GMT".equalsIgnoreCase(ln)) {
                            this.event.releasedOnGmt = getCurrentDateTime(DTF_RELEASE_ON_GMT, false);
                        }
                        else if ("PREVIOUS_DATE".equalsIgnoreCase(ln)) {
                            this.event.previousDate = getCurrentDateTime(DTF_RELEASE, true);
                        }
                        else if ("FREQUENCY".equalsIgnoreCase(ln)) {
                            this.event.frequency = getCurrentInt();
                        }
                        else if ("ATTRIBUTE".equalsIgnoreCase(ln)) {
                            this.event.attribute = getCurrentInt();
                        }
                        else if ("ARTICLE_URL".equalsIgnoreCase(ln)) {
                            this.event.articleUrl = getCurrentString();
                        }
                        else if ("HIGHLIGHTS".equalsIgnoreCase(ln)
                                || "CONSENSUSNOTES".equalsIgnoreCase(ln)) {
                            textHolder.content = getCurrentString();
                        }
                        else if ("VALUE_NAME".equalsIgnoreCase(ln)) {
                            assessmentTypeHolder.name = getCurrentString();
                        }
                        else if ("PREFIX".equalsIgnoreCase(ln)) {
                            assessmentTypeHolder.prefix = getCurrentString();
                        }
                        else if ("SUFFIX".equalsIgnoreCase(ln)) {
                            assessmentTypeHolder.suffix = getCurrentString();
                        }
                        else if ("CONSENSUS".equalsIgnoreCase(ln)) {
                            this.assessmentPool.getValueHolder(this.event.uid, ValueTypeEnum.Consensus,
                                    getCurrentString());
                        }
                        else if ("ACTUAL".equalsIgnoreCase(ln)) {
                            this.assessmentPool.getValueHolder(this.event.uid, ValueTypeEnum.Actual,
                                    getCurrentString());
                        }
                        else if ("REVISED".equalsIgnoreCase(ln)) {
                            this.assessmentPool.getValueHolder(this.event.uid, ValueTypeEnum.Revised,
                                    getCurrentString());
                        }
                        else if ("PREVIOUS".equalsIgnoreCase(ln)) {
                            this.assessmentPool.getValueHolder(this.event.uid, ValueTypeEnum.Previous,
                                    getCurrentString());
                        }
                        else if ("CONSENSUSRANGEFROM".equalsIgnoreCase(ln)) {
                            this.assessmentPool.getValueHolder(this.event.uid, ValueTypeEnum.ConsensusRangeFrom,
                                    getCurrentString());
                        }
                        else if ("CONSENSUSRANGETO".equalsIgnoreCase(ln)) {
                            this.assessmentPool.getValueHolder(this.event.uid, ValueTypeEnum.ConsensusRangeTo,
                                    getCurrentString());
                        }
                        else if ("VALUE".equalsIgnoreCase(ln)) {
                            assessmentTypeHolder.to = this.assessmentPool.getSize();
                        }
                        else if ("CHART_NAME".equalsIgnoreCase(ln)) {
                            imageHolder.name = getCurrentString();
                        }
                        else if ("CAPTION".equalsIgnoreCase(ln)) {
                            imageHolder.caption = getCurrentString();
                        }
                        else if ("CHART".equalsIgnoreCase(ln)
                                || "GRID".equalsIgnoreCase(ln)) {
                            imageHolder.data = getCurrentString();
                        }

                        buffer.setLength(0);
                        break;
                }

            } while (event != XMLStreamConstants.END_DOCUMENT);

            this.logger.info("<execute> imported " + count + " event release(s)");
            return count;
        } finally {
            this.buffer.setLength(0);
            if (reader != null) {
                reader.close();
            }
        }
    }

    private BigDecimal getCurrentDecimal() {
        final String curStr = getCurrentString();
        try {
            return (BigDecimal) DF_VALUE.parse(curStr);
        } catch (ParseException e) {
            this.logger.error("<getCurrentDecimal> cannot parse decimal: '" + curStr + "'");
            return null;
        }
    }

    private int getCurrentInt() {
        return Integer.parseInt(getCurrentString());
    }

    private DateTime getCurrentDateTime(SimpleDateFormat dtf, boolean withTimeZoneMap) {
        final String curStr = getCurrentString();
        try {
            return new DateTime(dtf.parse(fixDateTime(curStr, withTimeZoneMap)));
        } catch (ParseException e) {
            throw new IllegalStateException("cannot parse date time: '" + curStr + "'");
        }
    }

    private String fixDateTime(String curStr, boolean withTimeZoneMap) {
        String tmp = curStr;
        if (!curStr.contains(":")) {
            tmp += " 00:00";
        }
        if (withTimeZoneMap) {
            final Matcher matcher = ET.matcher(tmp);
            if (matcher.find()) {
                return matcher.replaceAll(""); // just cut off "ET" - should only this one
            }
            else {
                return tmp;
            }
        }
        else {
            return tmp;
        }
    }

    private String getCurrentString() {
        return StringUtils.normalizeSpace(this.buffer.toString());
    }

    @Transactional
    public int storeEvent() {
        try {
            if (!shouldStore()) {
                return 0;
            }
            addEvent();
            addRelease();
            addText();
            addAssessmentType();
            addAssessment();
            addImage();
            return 1;
        } catch (Exception e) {
            this.logger.error("<storeEvent> cannot persist event: "
                    + this.event.code + ", " + this.event.uid
                    + ", " + this.event.name, e);
            return 0;
        } finally {
            this.event.reset();
            this.textPool.reset();
            this.assessmentTypePool.reset();
            this.assessmentPool.reset();
            this.imagePool.reset();
        }
    }

    private int removeOlderRelease(int uid) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<removeOlderRelease> remove older release " + uid);
        }
        return getJdbcTemplate().update("DELETE FROM t_release WHERE id = ?", new Object[]{uid});
    }

    private boolean shouldStore() {
        if (this.event.uid < 0 || EVENT_CODE_2_IGNORE.equals(this.event.code)) {
            return false;
        }

        if (null == this.event.modifyDate) {
            this.logger.error("<shouldStore> cannot store event: " + this.event.uid
                    + " without modify date");
            return false;
        }

        final DateTime lastModifyDate = getLastModifyDate(this.event.uid);
        if (null != lastModifyDate && lastModifyDate.isAfter(this.event.modifyDate)) {
            this.logger.warn("<shouldStore> older event release: " + this.event.uid);
            return false;
        }

        return null == lastModifyDate || 1 == removeOlderRelease(this.event.uid);
    }

    private DateTime getLastModifyDate(int uid) {
        try {
            return new DateTime(getJdbcTemplate().queryForObject(
                    "SELECT modify_date FROM t_release WHERE id = ?",
                    new Object[]{uid}
                    , Timestamp.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void addImage() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addImage> " + this.imagePool.getSize() +
                    " image(s) for release: " + this.event.uid);
        }
        if (this.imagePool.getSize() == 0) {
            return;
        }
        getJdbcTemplate().batchUpdate("INSERT INTO t_image(release_id, type, name, caption, data)" +
                " VALUES(?, ?, ?, ?, COMPRESS(?))",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        final ImageHolder holder = imagePool.getHolder(i);
                        ps.setInt(1, holder.releaseId);
                        ps.setInt(2, holder.type.getValue());
                        ps.setString(3, holder.name);
                        ps.setString(4, holder.caption);
                        ps.setString(5, holder.data);
                    }

                    @Override
                    public int getBatchSize() {
                        return imagePool.getSize();
                    }
                });
    }

    private void addAssessmentType() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addAssessmentType> " + this.assessmentTypePool.getSize() +
                    " assessment type(s) for release: " + this.event.uid);
        }

        if (this.assessmentTypePool.getSize() == 0) {
            return;
        }

        for (int i = 0; i < this.assessmentTypePool.getSize(); i++) {
            final AssessmentTypeHolder assessmentTypeHolder = this.assessmentTypePool.getHolder(i);
            assessmentTypeHolder.retrieveId(getJdbcTemplate());
            for (int j = assessmentTypeHolder.from; j < assessmentTypeHolder.to; j++) {
                this.assessmentPool.getHolder(j).type = assessmentTypeHolder.id;
            }
        }
    }

    private void addAssessment() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addAssessment> " + this.assessmentPool.getSize() +
                    " assessment(s) for release: " + this.event.uid);
        }

        if (this.assessmentPool.getSize() == 0) {
            return;
        }

        List<AssessmentHolder> holders = getUniqueAssessments();

        getJdbcTemplate().batchUpdate("INSERT INTO t_assessment(release_id, type, value_type, value)" +
                " VALUES(?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        final AssessmentHolder holder = holders.get(i);
                        ps.setInt(1, holder.releaseId);
                        ps.setInt(2, holder.type);
                        ps.setInt(3, holder.valueType.getValue());
                        ps.setString(4, holder.value);
                    }

                    @Override
                    public int getBatchSize() {
                        return holders.size();
                    }
                });
    }

    private List<AssessmentHolder> getUniqueAssessments() {
        Map<String, AssessmentHolder> map = new HashMap<>();
        for (int i = 0; i < this.assessmentPool.getSize(); i++) {
            final AssessmentHolder h = this.assessmentPool.getHolder(i);
            final String key = h.releaseId + "-" + h.type + "-" + h.valueType.getValue();
            final AssessmentHolder existing = map.put(key, h);
            if (existing != null) {
                this.logger.warn("<getUniqueAssessments> duplicate entry for " + h);
            }
        }
        return new ArrayList<>(map.values());
    }

    private void addText() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addText> " + this.textPool.getSize() +
                    " text(s) for release: " + this.event.uid);
        }
        if (this.textPool.getSize() == 0) {
            return;
        }
        getJdbcTemplate().batchUpdate("INSERT INTO t_text(release_id, type, content)" +
                " VALUES(?, ?, COMPRESS(?))",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        final TextHolder holder = textPool.getHolder(i);
                        ps.setInt(1, holder.releaseId);
                        ps.setInt(2, holder.type.getValue());
                        ps.setString(3, holder.content);
                    }

                    @Override
                    public int getBatchSize() {
                        return textPool.getSize();
                    }
                });
    }

    private void addRelease() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addRelease> " + this.event.uid);
        }
        getJdbcTemplate().update("INSERT INTO t_release(id, event_code, modify_date, released_for, " +
                " released_on, released_on_gmt, previous_date, attribute, article_url)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        final EventHolder holder = event;
                        ps.setInt(1, holder.uid);
                        ps.setString(2, holder.code);
                        ps.setTimestamp(3, getTimestamp(holder.modifyDate));
                        ps.setString(4, holder.releasedFor);
                        ps.setTimestamp(5, getTimestamp(holder.releasedOn));
                        ps.setTimestamp(6, getTimestamp(holder.releasedOnGmt));
                        ps.setTimestamp(7, getTimestamp(holder.previousDate));
                        ps.setInt(8, holder.attribute);
                        ps.setString(9, holder.articleUrl);
                    }
                });
    }

    private Timestamp getTimestamp(DateTime dt) {
        return null == dt ? null : new Timestamp(dt.getMillis());
    }

    private int addEvent() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<addEvent> " + this.event.code + ", " + this.event.uid + ", " + this.event.name);
        }
        return getJdbcTemplate().update("INSERT INTO t_event(code, name, country, frequency," +
                " definition, description)" +
                " VALUES(?, ?, ?, ?, COMPRESS(?), COMPRESS(?))" +
                " ON DUPLICATE KEY UPDATE" +
                " name = ?, country = ?, frequency = ?," +
                " definition = COMPRESS(?), description = COMPRESS(?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, event.code);
                        ps.setString(2, event.name);
                        ps.setString(3, event.country);
                        ps.setInt(4, event.frequency);
                        ps.setString(5, event.definition);
                        ps.setString(6, event.description);
                        // on duplicate key update
                        ps.setString(2 + 5, event.name);
                        ps.setString(3 + 5, event.country);
                        ps.setInt(4 + 5, event.frequency);
                        ps.setString(5 + 5, event.definition);
                        ps.setString(6 + 5, event.description);
                    }
                });
    }

    private static class HolderPool<T extends Holder> {
        private final List<T> holders;

        private int size;

        private HolderFactory<T> factory;

        private HolderPool(int size, HolderFactory<T> fac) {
            this.holders = new ArrayList<>(size);
            this.factory = fac;
            for (int i = 0; i < size; i++) {
                this.holders.add(this.factory.createHolder());
            }
            this.size = 0;
        }

        private boolean isFull() {
            return this.size == this.holders.size();
        }

        private boolean isEmpty() {
            return this.size == 0;
        }

        int getSize() {
            return this.size;
        }

        protected T getHolder() {
            if (this.size == this.holders.size()) {
                this.holders.add(this.factory.createHolder());
            }

            return this.holders.get(this.size++);
        }

        protected T getHolder(int i) {
            return this.holders.get(i);
        }

        void reset() {
            for (T holder : holders) {
                holder.reset();
            }
            this.size = 0;
        }

        public void dropLast() {
            this.holders.remove(--this.size);
        }
    }

    private interface HolderFactory<T extends Holder> {
        T createHolder();
    }

    private interface Holder {
        void reset();
    }

    private static class EventHolder implements Holder {
        private String name;

        private DateTime modifyDate;

        private String country;

        private String code;

        private int frequency;

        private String definition;

        private String description;

        private int uid;

        private String releasedFor;

        private DateTime releasedOn;

        private DateTime releasedOnGmt;

        private DateTime previousDate;

        private int attribute;

        private String articleUrl;

        private EventHolder() {
        }

        @Override
        public void reset() {
            this.name = null;
            this.modifyDate = null;
            this.country = null;
            this.code = null;
            this.frequency = -1;
            this.definition = null;
            this.description = null;
            this.uid = -1;
            this.releasedFor = null;
            this.releasedOn = null;
            this.releasedOnGmt = null;
            this.previousDate = null;
            this.attribute = -1;
            this.articleUrl = null;
        }
    }

    private static class TextPool extends HolderPool<TextHolder> {

        private TextPool(int size, HolderFactory<TextHolder> fac) {
            super(size, fac);
        }

        TextHolder getTextHolder(int releaseId, TextTypeEnum type) {
            final TextHolder holder = getHolder();
            holder.releaseId = releaseId;
            holder.type = type;
            return holder;
        }
    }

    private static class TextHolder implements Holder {
        private int releaseId;

        private TextTypeEnum type;

        private String content;


        @Override
        public void reset() {
            this.releaseId = -1;
            this.type = null;
            this.content = null;
        }
    }

    private static class AssessmentHolder implements Holder {
        private int releaseId;

        private int type;

        private ValueTypeEnum valueType;

        private String value;

        @Override
        public void reset() {
            this.releaseId = -1;
            this.type = -1;
            this.valueType = null;
            this.value = null;
        }

        @Override
        public String toString() {
            return "AssessmentHolder{" +
                    this.releaseId +
                    ", type=" + type +
                    ", valueType=" + valueType +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    private static class AssessmentPool extends HolderPool<AssessmentHolder> {

        private AssessmentPool(int size, HolderFactory<AssessmentHolder> fac) {
            super(size, fac);
        }

        AssessmentHolder getValueHolder(int releaseId, ValueTypeEnum valueType, String value) {
            final AssessmentHolder holder = getHolder();
            holder.releaseId = releaseId;
            holder.valueType = valueType;
            holder.value = value;
            return holder;
        }
    }

    private static class AssessmentTypePool extends HolderPool<AssessmentTypeHolder> {

        private AssessmentTypePool(int size, HolderFactory<AssessmentTypeHolder> fac) {
            super(size, fac);
        }
    }

    static class AssessmentTypeHolder implements Holder {
        private int id;

        private String name;

        private String prefix;

        private String suffix;

        private int from;

        private int to;

        @Override
        public void reset() {
            this.id = -1;
            this.name = null;
            this.prefix = null;
            this.suffix = null;
            this.from = -1;
            this.to = -1;
        }

        private static final String[] SQL = {
                "SELECT id FROM t_assessment_type WHERE name = ? AND prefix IS NULL AND suffix IS NULL",
                "SELECT id FROM t_assessment_type WHERE name = ? AND prefix IS NULL AND suffix = ?",
                "SELECT id FROM t_assessment_type WHERE name = ? AND prefix = ? AND suffix IS NULL",
                "SELECT id FROM t_assessment_type WHERE name = ? AND prefix = ? AND suffix = ?"
        };

        static String getSql(String prefix, String suffix) {
            int idx = 0;
            if (null != prefix) {
                idx |= 0x2;
            }
            if (null != suffix) {
                idx |= 0x1;
            }

            return SQL[idx];
        }

        private static final Object[] PARAM_3 = new Object[]{null, null, null};

        private static final Object[] PARAM_2 = new Object[]{null, null};

        private static final Object[] PARAM_1 = new Object[]{null};

        static Object[] getSqlParam(String name, String prefix, String suffix) {
            if (null != prefix && null != suffix) {
                PARAM_3[0] = name;
                PARAM_3[1] = prefix;
                PARAM_3[2] = suffix;
                return PARAM_3;
            }
            else if (null != prefix) {
                PARAM_2[0] = name;
                PARAM_2[1] = prefix;
                return PARAM_2;
            }
            else if (null != suffix) {
                PARAM_2[0] = name;
                PARAM_2[1] = suffix;
                return PARAM_2;
            }
            else {
                PARAM_1[0] = name;
                return PARAM_1;
            }
        }

        public void retrieveId(JdbcTemplate jt) {
            final Object theId = jt.query(getSql(this.prefix, this.suffix),
                    getSqlParam(this.name, this.prefix, this.suffix),
                    new ResultSetExtractor() {
                        @Override
                        public Object extractData(ResultSet rs)
                                throws SQLException, DataAccessException {
                            if (rs.next()) {
                                return rs.getInt(1);
                            }

                            return null;
                        }
                    });
            if (null != theId) {
                id = (Integer) theId;
            }
            else {
                final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
                jt.update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(
                            Connection con) throws SQLException {
                        final PreparedStatement ps = con.prepareStatement(
                                "INSERT INTO t_assessment_type(name, prefix, suffix) VALUES(?, ?, ?)",
                                PreparedStatement.RETURN_GENERATED_KEYS);
                        ps.setString(1, name);
                        ps.setString(2, prefix);
                        ps.setString(3, suffix);
                        return ps;
                    }
                }, keyHolder);

                id = keyHolder.getKey().intValue();
            }
        }
    }

    private static class ImagePool extends HolderPool<ImageHolder> {

        private ImagePool(int size, HolderFactory<ImageHolder> fac) {
            super(size, fac);
        }

        ImageHolder getImageHolder(int releaseId, ImageTypeEnum type) {
            final ImageHolder holder = getHolder();
            holder.releaseId = releaseId;
            holder.type = type;
            return holder;
        }
    }

    private static class ImageHolder implements Holder {
        private int releaseId;

        private ImageTypeEnum type;

        private String name;

        private String caption;

        private String data;

        @Override
        public void reset() {
            this.releaseId = -1;
            this.type = null;
            this.name = null;
            this.caption = null;
            this.data = null;
        }

        @Override
        public String toString() {
            return "ImageHolder{" +
                    "releaseId=" + releaseId +
                    ", type=" + type +
                    '}';
        }
    }
}
