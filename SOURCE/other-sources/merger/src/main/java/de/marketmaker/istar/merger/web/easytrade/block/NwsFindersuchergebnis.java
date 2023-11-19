/*
 * NwsFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryParser.ParseException;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.news.NewsProvider;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.NewsQueryException;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.istar.news.frontend.NewsResponseImpl;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.QID_SUFFIX;

/**
 * Finds news based on various criteria.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NwsFindersuchergebnis extends EasytradeCommandController {
    public static final char OR_CONSTRAINTS_SEPARATOR = '_';

    private static final boolean[] ILLEGAL_CHARS = new boolean[128];

    private static final int MAX_SEARCHSTRING_LENGTH = 200;

    // used to migrate wrong LBBW requests via sector parameter for topic attributes
    private static final Pattern TOPIC_SEARCH_PATTERN = Pattern.compile("\\d+\\w");

    static {
        for (char c : "*?~:^[]{}()\"'".toCharArray()) {
            ILLEGAL_CHARS[c] = true;
        }
        Arrays.fill(ILLEGAL_CHARS, 0, ' ', true);
    }

    public NwsFindersuchergebnis() {
        super(Command.class);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Command extends ListCommand {
        private String realm = NwsFindersuchkriterien.DEFAULT_REALM;

        private String[] topic;

        private String sector;

        private String region;

        private String index;

        private String query;

        // todo: why multiple string?
        private String[] searchstring;

        private String[] symbol;

        private DateTime start;

        private DateTime end;

        private Period period;

        private boolean withText;

        private boolean withGallery = false;

        private boolean withRawText;

        private boolean withHitCount = true;

        private String offsetId;

        private boolean useShortId = false;

        @Override
        @Range(min = 1, max = 300)
        public int getCount() {
            return super.getCount();
        }

        /**
         * Always <tt>date</tt>.
         */
        @Override
        public String getSortBy() {
            return super.getSortBy();
        }

        /**
         * Always <tt>false</tt>, as the latest news will always be returned first.
         */
        @Override
        public boolean isAscending() {
            return false;
        }

        /**
         * Instrument symbols; if any are given, only those news will be included in the result
         * that are associated with at least one of the identified instruments. Valid symbol types
         * are iid, qid, ISIN, vwd-Code. <em>Only used if <tt>query</tt> is undefined</em>.
         */
        public String[] getSymbol() {
            return ArraysUtil.copyOf(symbol);
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        /**
         * key of a sector, only news for that sector will be returned
         * <em>Only used if <tt>query</tt> is undefined</em>.
         */
        public String getSector() {
            return sector;
        }

        public void setSector(String sector) {
            this.sector = sector;
        }

        public void setBranche(String branche) {
            setSector(branche);
        }

        /**
         * key of a region, only news for that region will be returned
         * <em>Only used if <tt>query</tt> is undefined</em>.
         */
        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        /**
         * Find news that contain these texts in headline, text, or as associated symbol. If
         * multiple values are given, each of them must occur in the matching news.
         * <em>Only used if <tt>query</tt> is undefined</em>.
         */
        public String[] getSearchstring() {
            return ArraysUtil.copyOf(searchstring);
        }

        public void setSearchstring(String[] searchstring) {
            this.searchstring = ArraysUtil.copyOf(searchstring);
        }

        public void setStichwort_kuerzel(String[] stichwort_kuerzel) {
            setSearchstring(stichwort_kuerzel);
        }

        /**
         * keys for topics, only news that match at least one of those topics will be returned
         * <em>Only used if <tt>query</tt> is undefined</em>.
         */
        public String[] getTopic() {
            return topic;
        }

        public void setTopic(String[] topic) {
            this.topic = topic;
        }

        public void setThemengebiet(String themengebiet) {
            setTopic(new String[]{themengebiet});
        }

        /**
         * the symbol (iid, qid, isin, vwdCode) of an index; restricts result to news associated
         * with either the index instrument itself or with any of the index constituents.
         * <em>Only used if <tt>query</tt> is undefined</em>.
         */
        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        /**
         * Specifies a complex query; whereas the other search related parameters are joined
         * in a simple <tt>and</tt> query that combines all terms on the top level, this query
         * may contain nested structures of <tt>and</tt>ed and <tt>or</tt>ed subqueries.
         * <p>The query has to be specified using the standard finder query syntax.
         * <table border=1>
         *     <tr><th>field</th><th>supported relation(s)</th><th>examples</th><th>note</th></tr>
         *     <tr><td>agency</td><td>=, IN</td><td>agency='djn'</td><td>&nbsp;</td></tr>
         *     <tr><td>country</td><td>=, IN</td><td>country='de'</td><td>values depend on agency</td></tr>
         *     <tr><td>date</td><td>&lt;, &lt;=, =, &gt;=, &gt;</td><td>date &gt; '2012-03-12'</td>
         *     <td>Supported Formats:<br>yyyy-MM-dd</br><br>yyyy-MM-dd'T'HH:mm:ss</br></td></tr>
         *     <tr><td>headline</td><td>=</td><td>headline='cornflakes'</td><td>&nbsp;</td></tr>
         *     <tr><td>language</td><td>=, IN</td><td>language='nl'</td><td>&nbsp;</td></tr>
         *     <tr><td>region</td><td>=, IN</td><td>region='europe'</td><td>values depend on agency</td></tr>
         *     <tr><td>symbol (or iid)</td><td>=, IN</td><td>symbol='de0007100000'<br>iid IN ('1', '42')</br></td>
         *     <td>&nbsp;</td></tr>
         *     <tr><td>sector</td><td>=, IN</td><td>sector='xyz'</b></td><td>values depend on agency</td></tr>
         *     <tr><td>selector</td><td>=, IN</td><td>selector='1A'</b></td><td>&nbsp;</td></tr>
         *     <tr><td>text</td><td>=</td><td>text='cornflakes'<br>text='peanut butter'</b></td>
         *     <td>searches headline and text</td></tr>
         *     <tr><td>topic</td><td>=, IN</td><td>topic='asia'</b></td><td>&nbsp;</td></tr>
         * </table>
         * </p>
         * Complex query example: <tt>text='daimler' and (text='bmw' or headline='auto')</tt>
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * search only news with a timestamp equal or later than this
         */
        public DateTime getStart() {
            return this.start;
        }

        public void setStart(DateTime start) {
            this.start = start;
        }

        /**
         * search only news with a timestamp earlier than this
         */
        public DateTime getEnd() {
            return end;
        }

        public void setEnd(DateTime end) {
            this.end = end;
        }

        /**
         * Specifies an interval for news. The interval is defined as follows:
         * <dl>
         * <dt><tt>start</tt> and <tt>end</tt> are undefined:</dt>
         * <dd><tt>now - period ... now</tt></dd>
         * <dt><tt>start</tt> is defined and <tt>end</tt> is undefined:</dt>
         * <dd><tt>start ... start + period</tt></dd>
         * <dt><tt>start</tt> is undefined and <tt>end</tt> is defined:</dt>
         * <dd><tt>end - period ... end</tt></dd>
         * <dt><tt>start</tt> and <tt>end</tt> are defined:</dt>
         * <dd><tt>start ... end</tt>, period is ignored</dd>
         * </dl>
         * If the period is specified without hours, minutes, seconds and millis, the computed
         * start / end of the interval will be aligned with midnight (i.e.,a request with
         * period <tt>P1d</tt> and no start/end defined returns news starting from
         * yesterday (= now - P1d) at midnight.
         */
        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        /**
         * Whether the search results should contain the news text.
         */
        public boolean isWithText() {
            return withText;
        }

        public void setWithText(boolean withText) {
            this.withText = withText;
        }

        @MmInternal
        public boolean isWithRawText() {
            return withRawText;
        }

        public void setWithRawText(boolean withRawText) {
            this.withRawText = withRawText;
        }

        /**
         * Whether the search results should contain the gallery (if the news item contains a gallery).
         */
        public boolean isWithGallery() {
            return withGallery;
        }

        public void setWithGallery(boolean withGallery) {
            this.withGallery = withGallery;
        }

        /**
         * Whether the response should contain a total hit count. Should only be requested if
         * needed as searches are a lot faster if no hit count is computed.
         */
        public boolean isWithHitCount() {
            return withHitCount;
        }

        public void setWithHitCount(boolean withHitCount) {
            this.withHitCount = withHitCount;
        }

        /**
         * news-id of a news that defines the first news to be returned; if defined, this
         * property overrides <tt>offset</tt>.
         */
        public String getOffsetId() {
            return offsetId;
        }

        public void setOffsetId(String offsetId) {
            this.offsetId = offsetId;
        }

        @MmInternal
        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        @MmInternal
        public boolean isUseShortId() {
            return useShortId;
        }

        public void setUseShortId(boolean useShortId) {
            this.useShortId = useShortId;
        }
    }

    private NewsProvider newsProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private NwsFindersuchkriterien nwsFindersuchkriterien;

    private static final List<String> SORT_FIELDS = Collections.emptyList();

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setNewsProvider(NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    public void setNwsFindersuchkriterien(NwsFindersuchkriterien nwsFindersuchkriterien) {
        this.nwsFindersuchkriterien = nwsFindersuchkriterien;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final NewsRequest nr = createRequest(cmd);
        ModelAndView result = findNews(cmd, nr, cmd.getRealm());
        result.addObject("useShortId", cmd.isUseShortId());
        return result;
    }

    ModelAndView findNews(ListCommand cmd, NewsRequest nr, String realm) {
        final NewsResponse news = (nr != null)
                ? this.newsProvider.getNews(nr, false)
                : new NewsResponseImpl();

        final Map<String, Object> model = new HashMap<>();
        model.put("listinfo", createListResult(cmd, news, "datum"));
        model.put("dmxmllistinfo", createListResult(cmd, news, "date"));
        if (nr != null) {
            model.put("withText", nr.isWithText());
            model.put("withRawText", nr.isWithRawText());
            model.put("withGallery", nr.isWithGallery());
            model.put("blockAds", !nr.isWithAds());
        }
        model.put("news", news);
        addMetadata(model, news.getRecords(), realm);
        return new ModelAndView("nwsfindersuchergebnis", model);
    }

    void addMetadata(Map<String, Object> model, final List<NewsRecord> records, String realmName) {
        final NwsFindersuchkriterien.Realm realm = this.nwsFindersuchkriterien.getRealm(realmName);
        Language lang = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());
        model.put("branchen", getMetadata(records, realm.getBranchenById(), lang));
        model.put("subjects", getMetadata(records, realm.getSubjectsById(), lang));
    }

    private ListResult createListResult(ListCommand cmd, NewsResponse news,
            final String defaultSortBy) {
        final ListResult result
                = ListResult.create(cmd, SORT_FIELDS, defaultSortBy, news.getHitCount());
        result.setCount(news.getRecords().size());
        return result;
    }

    private NewsRequest createRequest(Command cmd) {
        final NewsRequest result = new NewsRequest();
        result.setCount(cmd.getAnzahl());
        result.setOffset(cmd.getOffset());
        result.setWithText(cmd.isWithText());
        result.setWithRawText(cmd.isWithRawText());
        result.setWithGallery(cmd.isWithGallery());
        result.setWithHitCount(cmd.isWithHitCount());

        if (cmd.getOffsetId() != null) {
            if (cmd.isUseShortId()) {
                result.setShortOffsetId(cmd.getOffsetId());
            }
            else {
                result.setOffsetId(cmd.getOffsetId());
            }
        }

        if (cmd.getQuery() == null) {
            final StringBuilder query = new StringBuilder(MAX_SEARCHSTRING_LENGTH);
            if (!appendSymbols(cmd, query)) {
                return null;
            }
            appendSearchstrings(cmd, query);
            appendIndex(cmd, query);
            appendConstraints(cmd, query);
            if (query.length() > 0) {
                try {
                    result.setQuery(query.toString());
                } catch (ParseException e) {
                    this.logger.warn("<createRequest> setQuery failed: " + e.getMessage());
                    throw new NewsQueryException(e);
                }
            }
        }
        else {
            evaluateQueryExpression(cmd, result);
        }


        if (cmd.getStart() != null) {
            result.setFrom(cmd.getStart());
        }
        if (cmd.getEnd() != null) {
            result.setTo(cmd.getEnd());
        }
        if (cmd.getPeriod() != null) {
            if (result.getFrom() == null && result.getTo() != null) {
                result.setFrom(minus(result.getTo(), cmd.getPeriod()));
            }
            else if (result.getTo() == null && result.getFrom() != null) {
                result.setTo(plus(result.getFrom(), cmd.getPeriod()));
            }
            else if (result.getTo() == null && result.getFrom() == null) {
                result.setFrom(minus(new DateTime(), cmd.getPeriod()));
            }
        }

        return result;
    }

    private DateTime plus(DateTime start, Period p) {
        DateTime dt = start.plus(p);
        return (isDaily(p)) ? dt.withTime(0, 0, 0, 0).plusDays(1) : dt;
    }

    private DateTime minus(DateTime end, Period p) {
        DateTime dt = end.minus(p);
        return (isDaily(p)) ? dt.withTime(0, 0, 0, 0) : dt;
    }

    private boolean isDaily(Period p) {
        return p.getHours() == 0 && p.getMinutes() == 0 && p.getSeconds() == 0 && p.getMillis() == 0;
    }

    private void evaluateQueryExpression(Command cmd, NewsRequest result) {
        final Term queryTerm;
        try {
            queryTerm = Query2Term.toTerm(cmd.getQuery());
        } catch (Exception e) {
            logger.warn("<evaluateQueryExpression> failed for '" + cmd.getQuery() + "': " + e.getMessage());
            throw new BadRequestException("cannot evaluate: '" + cmd.getQuery() + "'");
        }

        final NwsFinderTermVisitor visitor = new NwsFinderTermVisitor(this.instrumentProvider, result);
        queryTerm.accept(visitor);
        result.setLuceneQuery(visitor.getResult());
    }

    private boolean appendSymbols(Command cmd, StringBuilder query) {
        if (cmd.getSymbol() == null || cmd.getSymbol().length == 0) {
            return true;
        }
        final Set<Long> iids = collectIids(cmd);
        if (!iids.isEmpty()) {
            query.append(NewsRequest.iidQuery(iids)).append(" ");
            return true;
        }
        // make sure we won't find anything if query is with symbols but none can be identified
        return false;
    }

    private Set<Long> collectIids(Command cmd) {
        final Set<Long> result = new HashSet<>();
        for (String s : cmd.getSymbol()) {
            final Long iid = getIidForSymbol(s);
            if (iid != null) {
                result.add(iid);
            }
        }
        return result;
    }

    private Long getIidForSymbol(String symbol) {
        if (symbol.endsWith(EasytradeInstrumentProvider.IID_SUFFIX)) {
            return EasytradeInstrumentProvider.id(symbol);
        }
        final Instrument instrument = this.instrumentProvider.identifyInstrument(symbol, null);
        if (instrument != null) {
            return instrument.getId();
        }
        return null;
    }

    private void appendSearchstrings(Command cmd, StringBuilder query) {
        if (cmd.getSearchstring() == null || cmd.getSearchstring().length == 0) {
            return;
        }

        final String searchstring = getSearchstring(cmd);
        if (searchstring.length() > MAX_SEARCHSTRING_LENGTH) {
            throw new BadRequestException("too long (>" + MAX_SEARCHSTRING_LENGTH + "): " + searchstring);
        }
        appendQuery(query, searchstring, false);
    }

    private void appendQuery(StringBuilder query, String searchstring, boolean headlineOnly) {
        final String term = toValidTerm(searchstring);

        if (term == null) {
            return;
        }

        if (headlineOnly) {
            query.append("+").append(NewsIndexConstants.FIELD_HEADLINE)
                    .append(":(").append(term).append(") ");
            return;
        }

        query.append("+(");
        query.append(NewsIndexConstants.FIELD_TEXT).append(":(").append(term).append(") ");
        query.append(NewsIndexConstants.FIELD_SYMBOL).append(":(").append(term).append(") ");
        query.append(") ");
    }

    private String getSearchstring(Command cmd) {
        if (cmd.getSearchstring().length == 1) {
            return cmd.getSearchstring()[0];
        }
        this.logger.info("<getSearchstring> multiple searchstrings: " + Arrays.toString(cmd.getSearchstring()));
        final StringBuilder sb = new StringBuilder(40);
        for (String s : cmd.getSearchstring()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    static String toValidTerm(String s) {
        final StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c < ILLEGAL_CHARS.length && ILLEGAL_CHARS[c]) {
                continue;
            }
            sb.append(c);
        }
        final String tmp = sb.toString().trim();
        return StringUtils.hasText(tmp) ? tmp : null;
    }

    private void appendIndex(Command cmd, StringBuilder query) {
        if (!StringUtils.hasText(cmd.getIndex())) {
            return;
        }
        final Quote quote = this.instrumentProvider.identifyQuote(cmd.getIndex(), null, null, null);
        if (quote == null) {
            return;
        }
        query.append("+").append(NewsIndexConstants.FIELD_IID).append(":(");
        query.append(quote.getInstrument().getId()).append(" ");
        final List<Quote> quotes =
                this.instrumentProvider.getIndexQuotes(quote.getId() + QID_SUFFIX);
        for (final Quote q : quotes) {
            query.append(q.getInstrument().getId()).append(" ");
        }
        query.append(") ");
    }

    private void appendConstraints(Command cmd, StringBuilder query) {
        appendConstraints(query, NewsIndexConstants.FIELD_TOPIC, cmd.getTopic());
        appendConstraints(query, NewsAttributeEnum.COUNTRY.name().toLowerCase(), cmd.getRegion());
        appendConstraints(query, NewsAttributeEnum.SECTOR.name().toLowerCase(), cmd.getSector());
    }

    private void appendConstraints(StringBuilder sb, String attribute, String... strs) {
        if (strs == null) {
            return;
        }

        final StringBuilder value = new StringBuilder();
        for (final String s : strs) {
            if (StringUtils.hasText(s)) {
                value.append(s.replace(OR_CONSTRAINTS_SEPARATOR, ' ')).append(" ");
            }
        }

        if (value.length() == 0) {
            return;
        }

        final String attributes = value.toString().trim();

        // TODO: remove when LBBW changed production requests to use topic instead of sector for topic requests
        final String fieldname = isTopicRequest(attributes)
                ? NewsIndexConstants.FIELD_TOPIC
                : attribute;

        sb.append("+").append(fieldname).append(":(")
                .append(attributes).append(")");
    }

    private boolean isTopicRequest(String attributes) {
        final Matcher matcher = TOPIC_SEARCH_PATTERN.matcher("");
        final String[] strings = attributes.split(" ");
        for (final String string : strings) {
            if (matcher.reset(string).matches()) {
                return true;
            }
        }
        return false;
    }

    private List<List<String>> getMetadata(final List<NewsRecord> records,
            Map<String, LocalizedString> map, Language lang) {
        if (map == null) {
            return Collections.nCopies(records.size(), Collections.<String>emptyList());
        }

        final List<List<String>> result = new ArrayList<>(records.size());
        for (NewsRecord item : records) {
            result.add(getMetadata(item, map, lang));
        }
        return result;
    }

    private List<String> getMetadata(NewsRecord item, Map<String, LocalizedString> map,
            Language lang) {
        final Set<String> sectors = item.getTopics();
        if (sectors == null) {
            return Collections.emptyList();
        }
        final Set<String> result = new TreeSet<>();
        for (String sector : sectors) {
            final String normalized = normalize(sector);
            final LocalizedString s = map.get(normalized);
            if (s != null) {
                result.add(getLocalized(lang, s));
            }
        }
        return result.isEmpty() ? Collections.<String>emptyList() : new ArrayList<>(result);
    }

    private String getLocalized(Language lang, LocalizedString s) {
        String result = s.getLocalized(lang);
        if (result == null && lang != Language.en) {
            result = s.getLocalized(Language.en);
        }
        return (result != null) ? result : s.getDefault();
    }

    private String normalize(String sector) {
        if (Character.isDigit(sector.charAt(0))) {
            return EntitlementsVwd.normalize(sector);
        }
        return sector;
    }

}
