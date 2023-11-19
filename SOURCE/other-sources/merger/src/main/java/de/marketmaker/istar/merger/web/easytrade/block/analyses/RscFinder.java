/*
 * RscFinder.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.Query;
import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.analyses.frontend.AnalysesRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesServer;
import de.marketmaker.istar.analyses.frontend.AnalysesSummaryResponse;
import de.marketmaker.istar.analyses.frontend.AnalysisResponse;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.data.StockAnalysisSummary;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.FinderQueryParserSupport;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategyFactory;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;

import static java.util.stream.Collectors.toList;

/**
 * Finds analyses distributed by a specific analyses provider (such as awp, dpa-AFX, Smarthouse Media, etc).
 * An analysis evaluates a specific company and most often contains a recommendation (e.g., sell/hold/buy).
 * for the company's stock. For each company and analyst, only the most recent analysis (in
 * the requested time period) will be returned.
 * <p>
 * Note that it is not possible to request analyses of different providers in a single request.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscFinder extends EasytradeCommandController {

    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    public static final String DEFAULT_TEMPLATE = "rscfinder";

    public static final String AGGREGATED_TEMPLATE = "rscaggregatedfinder";

    public static final String DETAILED_TEMPLATE = "rscdetailedfinder";


    public static class Command extends RscListCommand {

        private String query;

        private String recommendation;

        private String analyst;

        private String sector;

        private String region;

        private String index;

        private String searchstring;

        private LocalDate start;

        private LocalDate end;

        private boolean aggregated = false;

        private boolean detailed = false;

        private boolean useLongId = true;

        private boolean useShortId = false;

        /**
         * @param resultType use "aggregated" to request aggregated results, anything else for default results
         * @deprecated
         */
        public void setResultType(String resultType) {
            this.aggregated = "aggregated".equals(resultType);
        }

        public void setAggregated(boolean aggregated) {
            this.aggregated = aggregated;
        }

        /**
         * A default result will contain the various analyses depending on the query and various
         * parameters, whereas an aggregated result will summarize the different recommenations
         * in the default result per instrument.
         */
        public boolean isAggregated() {
            return aggregated;
        }

        public void setDetailed(boolean detailed) {
            this.detailed = detailed;
        }

        /**
         * set this to true if you need additional data like previousRecommendation, previousTarget
         * etc. in the result, only works with non-aggregated queries
         */
        @MmInternal
        public boolean isDetailed() {
            return detailed;
        }

        /**
         * A query expression that specifies which analyses are requested. Query fields may vary
         * depending on the analyses provider, contact us for details. If a query is specified,
         * all other parameters except <tt>providerId</tt> and <tt>ignoreAnalysesWithoutRating</tt>
         * are ignored.
         * @sample symbol='710000.ETR' and searchstring='bmw' and date > '2012-04-01'
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * Search for analyses issued by this analyst (i.e, <em>not</em> the author, but rather
         * the issuer, usually a bank or a financial research company). The available analysts are returned
         * by {@see RscFinderMetadata} (if supported by the analyses provider).
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>analyst = '...'</tt> in query</em>
         */
        public String getAnalyst() {
            return analyst;
        }

        public void setAnalyst(String analyst) {
            this.analyst = analyst;
        }

        /**
         * Search for analyses of companies in the given sector. The available sectors are returned
         * by {@see RscFinderMetadata} (if supported by the analyses provider).
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>sector = '...'</tt> in query</em>
         */
        public String getSector() {
            return sector;
        }

        public void setSector(String sector) {
            this.sector = sector;
        }


        /**
         * Search for analyses with this recommendation. The available recommendations are returned
         * by {@see RscFinderMetadata} (if supported by the analyses provider).
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>recommendation = '...'</tt> in query</em>
         */
        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        /**
         * Search for analyses of companies in the given index.
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>index='...'</tt> in query</em>
         * @sample 846900.ETR
         */
        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        /**
         * Search for analyses of companies in the given region. The available regions are returned
         * by {@see RscFinderMetadata} (if supported by the analyses provider)
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>sector='...'</tt> in query</em>
         */
        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        /**
         * Search for analyses that contain this string in the analysis headline or body text.
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>searchstring='...'</tt> in query</em>
         */
        public String getSearchstring() {
            return searchstring;
        }

        public void setSearchstring(String searchstring) {
            this.searchstring = searchstring;
        }

        /**
         * Request analyses that are not older than this.
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>start='...'</tt> in query</em>
         * @sample 2012-01-01
         */
        public LocalDate getStart() {
            return start;
        }

        public void setStart(LocalDate start) {
            this.start = start;
        }

        /**
         * Request analyses that are not more recent than this.
         * <p><b>Ignored</b> if <tt>query</tt> is specified, use <tt>end='...'</tt> in query</em>
         * @sample 2012-01-01
         */
        public LocalDate getEnd() {
            return end;
        }

        public void setEnd(LocalDate end) {
            this.end = end;
        }

        /*
        OLD postbank style setter
         */
        public void setAnalystenhaus(String analystenhaus) {
            this.analyst = analystenhaus;
        }

        public void setBranche(String branche) {
            this.sector = branche;
        }

        public void setEmpfehlung(String empfehlung) {
            this.recommendation = empfehlung;
        }

        public void setStichwort_kuerzel(String stichwort_kuerzel) {
            this.searchstring = stichwort_kuerzel;
        }

        @MmInternal
        public boolean isUseLongId() {
            return useLongId;
        }

        public void setUseLongId(boolean useLongId) {
            this.useLongId = useLongId;
        }
    }

    private static final String INSTRUMENTNAME = "instrumentname";

    static final Map<String, String> SORTFIELDS = new HashMap<>();

    private static final Map<String, String> SORTFIELDS_QUERY = new HashMap<>();

    private static final Map<String, String> SORTFIELDS_AGGREGATED
            = Collections.singletonMap(INSTRUMENTNAME, INSTRUMENTNAME);

    private static final Map<String, String> SORTFIELDS_WEBSIM
            = Collections.singletonMap("date", "analysisdate");

    static {
        SORTFIELDS.put("branche", "sector");
        SORTFIELDS.put("quelle", "source");
        SORTFIELDS.put("datum", "analysisdate");
        SORTFIELDS.put("recommendation", "ratingid");
        SORTFIELDS.put(INSTRUMENTNAME, INSTRUMENTNAME);

        SORTFIELDS_QUERY.put("sector", "sector");
        SORTFIELDS_QUERY.put("analyst", "source");
        SORTFIELDS_QUERY.put("recommendation", "ratingid");
        SORTFIELDS_QUERY.put(INSTRUMENTNAME, INSTRUMENTNAME);
        SORTFIELDS_QUERY.putAll(SORTFIELDS_WEBSIM);
    }

    static final String DEFAULT_SORT_BY = "datum";

    private static final String DEFAULT_SORT_BY_QUERY = "date";

    private AnalysesServer analysesServer;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setAnalysesServer(AnalysesServer analysesServer) {
        this.analysesServer = analysesServer;
    }

    public RscFinder() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        cmd.useShortId = !cmd.isUseLongId() && "marketmanager".equals(getZoneName(request));

        if (cmd.getQuery() != null || cmd.isAggregated()) {
            return doHandleQueryStyle(cmd);
        }
        else {
            return doHandleParameterStyle(cmd);
        }
    }

    protected ModelAndView doHandleQueryStyle(Command cmd) {
        if (cmd.isAggregated()) {
            final ListResult listResult
                    = createListResult(cmd, SORTFIELDS_AGGREGATED, INSTRUMENTNAME);
            final AnalysesRequest asr = createRequestFromQuery(cmd, listResult);
            final AnalysesSummaryResponse summary = this.analysesServer.getSummary(asr);
            return createAggregatedResult(cmd, summary, listResult);
        }
        else {
            final ListResult listResult
                    = createListResult(cmd, getSortfields(cmd), DEFAULT_SORT_BY_QUERY);
            final AnalysesRequest ar = createRequestFromQuery(cmd, listResult);
            final AnalysisResponse analyses = getAnalyses(ar);
            return createResult(analyses, listResult, cmd);
        }
    }


    private List<String> sortAndPageAnalysesByInstrumentName(AnalysisResponse analyses,
            AnalysesRequest request) {
        Map<String, Long> analysisIdsToIids = analyses.getAnalysesToIids();

        List<Long> iids = analysisIdsToIids.values().stream().distinct().collect(toList());
        List<Instrument> instruments = getInstruments(iids);

        RequestContext ctx = RequestContextHolder.getRequestContext();
        InstrumentNameStrategy nameStrategy = ctx.getInstrumentNameStrategy();

        Comparator comp = Comparator.comparing(nameStrategy::getName);
        if (!request.isAscending()) {
            comp = comp.reversed();
        }

        instruments.sort(comp);

        Map<Long, List<String>> iidsToAnalysesIds = analysisIdsToIids.entrySet().stream().collect(Collectors.groupingBy(
                Map.Entry::getValue,
                Collectors.mapping(Map.Entry::getKey, Collectors.toList())
        ));

        return instruments.stream().
                map(Instrument::getId).
                map(iidsToAnalysesIds::get).
                flatMap(List::stream).
                skip(request.getOffset()).
                limit(request.getCount()).
                collect(toList());
    }

    private Map<String, String> getSortfields(Command cmd) {
        return cmd.getSelector() == Selector.WEB_SIM_ANALYSES ? SORTFIELDS_WEBSIM : SORTFIELDS_QUERY;
    }

    private AnalysesRequest createRequestFromQuery(Command cmd, ListResult listResult) {
        final AnalysesRequest result = createRequest(cmd.getSelector(),
                cmd.isIgnoreAnalysesWithoutRating(), listResult, SORTFIELDS_QUERY);
        result.setQuery(createQuery(cmd.getSelector(), cmd.getQuery()));
        return result;
    }

    private Query createQuery(Selector selector, String rawQuery) {
        // HACK until all clients have adapted to new query syntax
        final String query = FinderQueryParserSupport.ensureQuotedValues(rawQuery);
        try {
            final Term term = Query2Term.toTerm(query);
            final RscFinderTermVisitor visitor
                    = new RscFinderTermVisitor(selector, this.instrumentProvider);
            term.accept(visitor);
            return visitor.getResult();
        } catch (Exception ex) {
            this.logger.warn("<createQuery> failed to parse '" + rawQuery + "'", ex);
            throw new BadRequestException("invalid query '" + rawQuery + "', reason: " + ex.getMessage());
        }
    }

    private String asRecommendation(String value) {
        return StringUtils.hasText(value)
                ? StockAnalysis.Recommendation.valueOf(value.toUpperCase()).name() : null;
    }

    private void appendTerm(StringBuilder sb, String key, String value) {
        if (StringUtils.hasText(value)) {
            if (sb.length() > 0) {
                sb.append(" and ");
            }
            sb.append(key).append("='").append(value).append("'");
        }
    }

    protected ModelAndView doHandleParameterStyle(Command cmd) {
        final ListResult listResult = createListResult(cmd, SORTFIELDS, DEFAULT_SORT_BY);
        final AnalysesRequest sr = createRequest(cmd.getSelector(),
                cmd.isIgnoreAnalysesWithoutRating(), listResult, SORTFIELDS);
        final StringBuilder sb = new StringBuilder(100);
        appendTerm(sb, "index", cmd.getIndex());
        appendTerm(sb, "sector", cmd.getSector());
        appendTerm(sb, "region", cmd.getRegion());
        appendTerm(sb, "source", cmd.getAnalyst());
        appendTerm(sb, "recommendation", asRecommendation(cmd.getRecommendation()));
        appendTerm(sb, "searchstring", cmd.getSearchstring());
        if (cmd.getStart() != null) {
            appendTerm(sb, "start", cmd.getStart().toString());
        }
        if (cmd.getEnd() != null) {
            appendTerm(sb, "end", cmd.getEnd().toString());
        }

        if (sb.length() > 0) {
            sr.setQuery(createQuery(cmd.getSelector(), sb.toString()));
        }

        final AnalysisResponse result = getAnalyses(sr);
        return createResult(result, listResult, cmd);
    }

    AnalysesRequest createRequest(Selector s, boolean ignoreAnalysesWithoutRating,
            ListResult listResult, Map<String, String> sortfields) {
        final AnalysesRequest sr = new AnalysesRequest(s);
        sr.setIgnoreAnalysesWithoutRating(ignoreAnalysesWithoutRating);
        sr.setOffset(listResult.getOffset());
        sr.setCount(listResult.getRequestedCount());
        sr.setSortBy(sortfields.get(listResult.getSortedBy()));
        sr.setAscending(listResult.isAscending());
        return sr;
    }

    ModelAndView createAggregatedResult(Command cmd, AnalysesSummaryResponse result,
            ListResult listResult) {
        final List<Long> iids = new ArrayList<>(result.getInstrumentIds());
        final MarketStrategy strategy = MarketStrategyFactory.defaultStrategy();
        final List<Instrument> instruments = getInstruments(cmd, iids, strategy);

        listResult.setTotalCount(instruments.size());
        ListHelper.clipPage(cmd, instruments);
        listResult.setCount(instruments.size());

        final List<Map<StockAnalysis.Recommendation, Integer>> counts = new ArrayList<>(iids.size());
        final ArrayList<String> sectors = new ArrayList<>(instruments.size());
        final List<Quote> quotes = new ArrayList<>(instruments.size());

        for (Instrument instrument : instruments) {
            counts.add(toRecommendationMap(result.getSummary(instrument.getId())));
            sectors.add(result.getSector(instrument.getId()));
            quotes.add(getQuote(instrument, strategy));
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("counts", counts);
        model.put("sectors", sectors);
        model.put("quotes", quotes);
        model.put("listinfo", listResult);
        return new ModelAndView(RscFinder.AGGREGATED_TEMPLATE, model);
    }

    /**
     * Returns instruments that have valid quotes.
     * To be used within {@see #createAggregatedResult}
     */
    private List<Instrument> getInstruments(Command cmd, List<Long> iids, MarketStrategy strategy) {
        final List<Instrument> result = this.instrumentProvider.identifyInstruments(iids);
        final InstrumentNameStrategy ins
                = RequestContextHolder.getRequestContext().getInstrumentNameStrategy();

        //filter instruments without quotes.
        ListIterator<Instrument> li = result.listIterator();
        while (li.hasNext()) {
            Instrument instrument = li.next();
            if (getQuote(instrument, strategy) == null) {
                li.remove();
            }
        }

        final MultiListSorter sorter = new MultiListSorter(new Comparator<Instrument>() {
            public int compare(Instrument o1, Instrument o2) {
                if (o1 == null || o2 == null) {
                    return -1;
                }
                return GERMAN_COLLATOR.compare(ins.getName(o1), ins.getName(o2));
            }
        }, !cmd.isAscending());
        sorter.sort(result);
        return result;
    }

    private Map<StockAnalysis.Recommendation, Integer> toRecommendationMap(StockAnalysisSummary s) {
        // cannot use EnumMap as its iterator is incompatible with stringtemplate in jdk1.6
        final Map<StockAnalysis.Recommendation, Integer> result = new HashMap<>();
        add(result, s.getNumberOfStrongSells(), StockAnalysis.Recommendation.STRONG_SELL);
        add(result, s.getNumberOfSells(), StockAnalysis.Recommendation.SELL);
        add(result, s.getNumberOfHolds(), StockAnalysis.Recommendation.HOLD);
        add(result, s.getNumberOfBuys(), StockAnalysis.Recommendation.BUY);
        add(result, s.getNumberOfStrongBuys(), StockAnalysis.Recommendation.STRONG_BUY);
        return result;
    }

    private void add(Map<StockAnalysis.Recommendation, Integer> result, int number,
            StockAnalysis.Recommendation r) {
        if (number > 0) {
            result.put(r, number);
        }
    }

    // used from RscListeAnalysen
    ModelAndView createResult(AnalysisResponse result, ListResult listResult, Selector s) {
        final List<StockAnalysis> analyses = result.getAnalyses();

        listResult.setCount(analyses.size());
        listResult.setTotalCount(result.getTotalCount());

        final List<Long> iids = analyses.stream()
                .map(StockAnalysis::getInstrumentid)
                .map(v -> (v != null) ? v : Long.valueOf(0))
                .collect(Collectors.toList());
        final List<Instrument> instruments = getInstruments(iids);

        final Map<String, Object> model = new HashMap<>();
        model.put("analyses", analyses);
        model.put("quotes", getQuotes(instruments));
        model.put("listinfo", listResult);
        model.put(RscCommand.getProviderId(s), Boolean.TRUE);
        return new ModelAndView(RscFinder.DEFAULT_TEMPLATE, model);
    }

    private ModelAndView createResult(AnalysisResponse response, ListResult listResult,
            Command cmd) {
        final ModelAndView result = createResult(response, listResult, cmd.getSelector());
        result.addObject("useShortId", cmd.useShortId);
        if (cmd.isDetailed()) {
            result.setViewName(RscFinder.DETAILED_TEMPLATE);
        }
        return result;
    }

    private List<Instrument> getInstruments(List<Long> iids) {
        return this.instrumentProvider.identifyInstruments(iids);
    }

    private List<Quote> getQuotes(List<Instrument> instruments) {
        final List<Quote> result = new ArrayList<>(instruments.size());
        final MarketStrategy strategy = MarketStrategyFactory.defaultStrategy();
        for (Instrument instrument : instruments) {
            try {
                result.add(instrument != null ? strategy.getQuote(instrument) : null);
            } catch (Exception e) {
                result.add(getFirstQuoteWithVwdFeedSymbol(instrument));
            }
        }
        return result;
    }

    private Quote getQuote(Instrument instrument, MarketStrategy strategy) {
        try {
            return instrument != null ? strategy.getQuote(instrument) : null;
        } catch (Exception e) {
            return getFirstQuoteWithVwdFeedSymbol(instrument);
        }
    }

    private Quote getFirstQuoteWithVwdFeedSymbol(Instrument instrument) {
        for (Quote quote : instrument.getQuotes()) {
            if (quote.getSymbolVwdfeed() != null) {
                return quote;
            }
        }
        return null;
    }

    AnalysisResponse getAnalyses(AnalysesRequest sr) {
        AnalysisResponse analyses = this.analysesServer.getAnalyses(sr);

        if (INSTRUMENTNAME.equals(sr.getSortBy())) {
            int totalCount = analyses.getTotalCount();
            List<String> analysesIds = sortAndPageAnalysesByInstrumentName(analyses, sr);
            sr.setAnalysisIds(analysesIds);
            analyses = this.analysesServer.getAnalyses(sr);
            analyses.setTotalCount(totalCount);
        }

        return analyses;
    }

    AnalysesSummaryResponse getSummaries(AnalysesRequest sr) {
        return this.analysesServer.getSummary(sr);
    }

    ListResult createListResult(ListCommand cmd, Map<String, String> sortfields,
            String defaultSortBy) {
        return ListResult.create(cmd, new ArrayList<>(sortfields.keySet()), defaultSortBy, 0);
    }
}
