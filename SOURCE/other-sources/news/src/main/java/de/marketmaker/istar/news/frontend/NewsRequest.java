/*
 * NewsRequest.java
 *
 * Created on 15.03.2007 12:17:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import java.util.List;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsRequest extends AbstractIstarRequest implements NewsRequestBase {
    protected static final long serialVersionUID = 1L;

    public static final String OR_CONSTRAINTS_SEPARATOR = "_";

    private static final DateTime FIRST_TIMESTAMP = new DateTime(2000, 1, 1, 0, 0, 0, 0);

    private static final DateTime LAST_TIMESTAMP = new DateTime(2038, 1, 18, 23, 59, 59, 0);

    private static final String SHORT_ID_PREFIX = "s:";

    public static String iidQuery(Set<Long> iids) {
        final StringBuilder sb = new StringBuilder(iids.size() * 8 + 10);
        sb.append("+").append(NewsIndexConstants.FIELD_IID).append(":(");
        for (Long iid : iids) {
            sb.append(iid).append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

    private int count = 10;

    private EntitlementsVwd entitlements;

    private DateTime from;

    private Query luceneQuery;

    private List<String> newsids;

    private int offset;

    private String offsetId = null;

    private Profile profile;

    /**
     * keep for now, clients may still set this value if they use an older version of this class
     */
    private String query;

    private transient boolean silent = false;

    private boolean sortByDate = true;

    private DateTime to;

    private boolean withAds = true;

    private boolean withHitCount = false;

    private boolean withText = true;

    private boolean withRawText;

    private boolean withGallery = false;

    public NewsRequest() {
    }

    public int getCount() {
        return this.count;
    }

    /**
     * @deprecated use {@link #getProfile()} instead
     */
    public EntitlementsVwd getEntitlements() {
        return entitlements;
    }

    @Override
    public DateTime getFrom() {
        return this.from;
    }

    public Query getLuceneQuery() {
        return this.luceneQuery;
    }

    public List<String> getNewsids() {
        return newsids;
    }

    public int getOffset() {
        return this.offset;
    }

    /**
     * If not null, the query returns results starting with the document that has this id
     * rather than starting at offset.
     * @return id of the first news to be returned, may be null
     */
    public String getOffsetId() {
        return isShortOffsetId() ? this.offsetId.substring(SHORT_ID_PREFIX.length()) : this.offsetId;
    }

    @Override
    public Profile getProfile() {
        return this.profile;
    }

    public String getQuery() {
        return this.query;
    }

    @Override
    public DateTime getTo() {
        return this.to;
    }

    public boolean isSilent() {
        return this.silent;
    }

    public boolean isSortByDate() {
        return this.sortByDate;
    }

    @Override
    public boolean isWithAds() {
        return withAds;
    }

    public boolean isWithHitCount() {
        return withHitCount;
    }

    @Override
    public boolean isWithText() {
        return withText;
    }

    public boolean isWithRawText() {
        return withRawText;
    }

    @Override
    public boolean isWithGallery() {
        return withGallery;
    }

    public void setCount(int count) {
        if (count > 1000) {
            throw new IllegalArgumentException("too many: " + count + ", max is 1000");
        }
        this.count = count;
    }

    /**
     * @deprecated set a Profile instead
     */
    public void setEntitlements(EntitlementsVwd entitlements) {
        this.entitlements = entitlements;
    }

    /**
     * @param from specify earliest requested news date
     * @throws IllegalArgumentException if from before 2000-Jan-01 or after this.to
     */
    public void setFrom(DateTime from) {
        this.from = (from != null && from.isBefore(FIRST_TIMESTAMP)) ? FIRST_TIMESTAMP : from;
    }

    public void setTo(DateTime to) {
        this.to = (to != null && to.isAfter(LAST_TIMESTAMP)) ? LAST_TIMESTAMP : to;
    }

    public void setLuceneQuery(Query luceneQuery) {
        this.luceneQuery = luceneQuery;
    }

    public void setNewsids(List<String> newsids) {
        this.newsids = newsids;
    }

    public void setOffset(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("negative startAt: " + offset);
        }
        this.offset = offset;
    }

    public void setOffsetId(String offsetId) {
        this.offsetId = offsetId;
    }

    public void setShortOffsetId(String offsetId) {
        this.offsetId = SHORT_ID_PREFIX + offsetId;
    }

    public boolean isShortOffsetId() {
        return this.offsetId != null && this.offsetId.startsWith(SHORT_ID_PREFIX);
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public void setQuery(String query) throws ParseException {
        setLuceneQuery(NewsQueryParserUtil.parse(query));
    }

    public void setSilent() {
        this.silent = true;
    }

    public void setSortByDate(boolean sortByDate) {
        this.sortByDate = sortByDate;
    }

    public void setWithAds(boolean withAds) {
        this.withAds = withAds;
    }

    /**
     * Define whether or not the response's
     * {@link de.marketmaker.istar.news.frontend.NewsResponse#getHitCount()}
     * should return the actual total number of hits for this query. If the hit count is not required,
     * it should not be requested as it will take somewhat longer to compute that value.
     * @param withHitCount whether hits should be counted
     */
    public void setWithHitCount(boolean withHitCount) {
        this.withHitCount = withHitCount;
    }

    public void setWithText(boolean withText) {
        this.withText = withText;
    }

    public void setWithRawText(boolean withRawText) {
        this.withRawText = withRawText;
    }

    public void setWithGallery(boolean withGallery) {
        this.withGallery = withGallery;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", profile=").append(this.profile != null ? this.profile.getName() : this.entitlements)
                .append(", from=").append(this.from)
                .append(", to=").append(this.to)
                .append(", offset=").append(this.offset)
                .append(", offsetId=").append(this.offsetId)
                .append(", count=").append(this.count)
                .append(this.withHitCount ? ", withHitCount=" : "")
                .append(withText ? ", withText" : "")
                .append(withRawText ? ", withRawText" : "")
                .append(withGallery ? ", withGallery" : "")
                .append(", query=").append(this.query != null ? this.query : this.luceneQuery)
                .append(", newsids=").append(this.newsids)
                .append(", sortByDate=").append(this.sortByDate)
        ;
    }
}
