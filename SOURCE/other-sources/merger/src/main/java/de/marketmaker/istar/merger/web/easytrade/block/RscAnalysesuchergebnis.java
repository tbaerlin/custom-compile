/*
 * RscAnalysesuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.StockAnalysis;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategy;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.provider.StockAnalysisProvider;
import de.marketmaker.istar.merger.stockanalysis.Rating;
import de.marketmaker.istar.merger.stockanalysis.StockAnalysisRequest;
import de.marketmaker.istar.merger.stockanalysis.StockAnalysisResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.analyses.RscFinder;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.Terms;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.IID_SUFFIX;
import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.QID_SUFFIX;

/**
 * Search for analyses documents based on various criteria.
 *
 * @see de.marketmaker.istar.merger.web.easytrade.block.analyses.RscFinder
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RscAnalysesuchergebnis extends EasytradeCommandController {
    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    private static final EnumSet<InstrumentTypeEnum> COUNT_TYPES = EnumSet.of(InstrumentTypeEnum.STK);

    public static class Command extends ListCommand {
        private String query;

        private String recommendation;

        private String analyst;

        private String sector;

        private String region;

        private String index;

        private String searchstring;

        private LocalDate start;

        private LocalDate end;

        private String resultType = "default";

        @RestrictedSet(value = "default,aggregated")
        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getAnalyst() {
            return analyst;
        }

        public void setAnalyst(String analyst) {
            this.analyst = analyst;
        }

        public String getSector() {
            return sector;
        }

        public void setSector(String sector) {
            this.sector = sector;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getSearchstring() {
            return searchstring;
        }

        public void setSearchstring(String searchstring) {
            this.searchstring = searchstring;
        }

        public LocalDate getStart() {
            return start;
        }

        public void setStart(LocalDate start) {
            this.start = start;
        }

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
    }

    static final Map<String, String> SORTFIELDS = new HashMap<>();

    private static final Map<String, String> SORTFIELDS_QUERY = new HashMap<>();

    private static final String INSTRUMENTNAME = "instrumentname";

    static {
        SORTFIELDS.put("branche", "sector");
        SORTFIELDS.put("header", "headline");
        SORTFIELDS.put("quelle", "analysts.name");
        SORTFIELDS.put("datum", "analysisdate");
        SORTFIELDS.put(INSTRUMENTNAME, INSTRUMENTNAME);
        SORTFIELDS.put("recommendation", "ratingid");

        SORTFIELDS_QUERY.put("sector", "sector");
        SORTFIELDS_QUERY.put("headline", "headline");
        SORTFIELDS_QUERY.put("analyst", "analysts.name");
        SORTFIELDS_QUERY.put("date", "analysisdate");
        SORTFIELDS_QUERY.put("recommendation", "ratingid");
        SORTFIELDS_QUERY.put(INSTRUMENTNAME, INSTRUMENTNAME);
    }

    static final String DEFAULT_SORT_BY = "datum";

    private static final String DEFAULT_SORT_BY_QUERY = "date";

    private StockAnalysisProvider stockAnalysisProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setStockAnalysisProvider(StockAnalysisProvider stockAnalysisProvider) {
        this.stockAnalysisProvider = stockAnalysisProvider;
    }

    public RscAnalysesuchergebnis() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final boolean queryStyle = cmd.getQuery() != null || isAggregated(cmd);
        return queryStyle ? doHandleQueryStyle(cmd) : doHandleParameterStyle(cmd);
    }

    protected ModelAndView doHandleQueryStyle(Command cmd) {
        final ListResult listResult = createListResult(cmd, SORTFIELDS_QUERY, DEFAULT_SORT_BY_QUERY);
        final StockAnalysisRequest sr = createRequestFromQuery(cmd, listResult);

        if (isAggregated(cmd)) {
            sr.setAggregatedResultType();
        }

        final StockAnalysisResponse result = getAnalyses(sr);
        return sr.isAggregatedResultType()
                ? createAggregatedResult(cmd, result, listResult)
                : createResult(result, listResult);
    }

    private boolean isAggregated(Command cmd) {
        return "aggregated".equals(cmd.getResultType());
    }

    private StockAnalysisRequest createRequestFromQuery(Command cmd, ListResult listResult) {
        final StockAnalysisRequest sr = createRequest(listResult, SORTFIELDS_QUERY);

        // HACK until all clients have adapted to new query syntax
        final String query = FinderQueryParserSupport.ensureQuotedValues(cmd.getQuery());

        final Term queryTerm;
        try {
            queryTerm = Query2Term.toTerm(query);
        } catch (Exception e) {
            logger.warn("<parseQuery> failed for '" + query + "':" + e.getMessage());
            sr.setInstrumentids(Collections.<Long>emptyList());
            sr.setRatings(Collections.<Rating>emptyList());
            return sr;
        }

        List<Term> terms = queryTerm instanceof Terms.AndOp
                ? ((Terms.AndOp) queryTerm).getTerms()
                : Collections.singletonList(queryTerm);

        List<Long> iids = null;
        List<Rating> ratings = null;

        for (final Term term : terms) {
            if (!(term instanceof Terms.Relation)) {
                throw new BadRequestException("query too complex: " + query);
            }
            final Terms.Relation relation = (Terms.Relation) term;
            final String fieldname = relation.getIdentifier();
            final String value = relation.getValue();

            if ("analyst".equals(fieldname)) {
                sr.setSource(value);
            }
            else if ("sector".equals(fieldname)) {
                sr.setSector(value);
            }
            else if ("recommendation".equals(fieldname)) {
                ratings = getRatings(value);
            }
            // KEEP ORDER OF SYMBOL AND INDEX! /////////
            else if ("symbol".equals(fieldname)) {
                iids = iidsForSymbol(value);
            }
            else if ("index".equals(fieldname)) {
                iids = getInstrumentids(value, iids);
            }
            /////////////////////////////////////////////
            else if ("region".equals(fieldname)) {
                sr.setRegion(value);
            }
            else if ("searchstring".equals(fieldname)) {
                sr.setSearchtext(value);
            }
            else if ("start".equals(fieldname)) {
                sr.setStart(DateUtil.parseDate(value).toLocalDate());
            }
            else if ("end".equals(fieldname)) {
                sr.setEnd(DateUtil.parseDate(value).toLocalDate());
            }
            else if ("date".equals(fieldname)) {
                if (relation.getOp() == Terms.Relation.Op.EQ || relation.getOp() == Terms.Relation.Op.GTE) {
                    sr.setStart(DateUtil.parseDate(value).toLocalDate());
                }
                if (relation.getOp() == Terms.Relation.Op.EQ || relation.getOp() == Terms.Relation.Op.LTE) {
                    sr.setEnd(DateUtil.parseDate(value).toLocalDate());
                }
            }
        }
        sr.setInstrumentids(iids == null ? Collections.<Long>emptyList() : iids);
        sr.setRatings(ratings == null ? Collections.<Rating>emptyList() : ratings);
        return sr;
    }

    private List<Long> iidsForSymbol(String value) {
        if (value.endsWith(IID_SUFFIX)) {
            return Collections.singletonList(EasytradeInstrumentProvider.id(value));
        }

        if (value.endsWith(QID_SUFFIX)) {
            return Collections.singletonList(this.instrumentProvider.identifyInstrument(value, SymbolStrategyEnum.QID).getId());
        }

        final SimpleSearchCommand command = new SimpleSearchCommand(value, null, null,
                InstrumentProvider.StrategyEnum.DEFAULT, COUNT_TYPES, COUNT_TYPES,
                null, null, 0, 40, 40, true);
        final SearchResponse sr = this.instrumentProvider.simpleSearch(command);

        final ArrayList<Long> result = new ArrayList<>();
        for (Instrument instrument : sr.getInstruments()) {
            result.add(instrument.getId());
        }

        return result;
    }

    protected ModelAndView doHandleParameterStyle(Command cmd) {
        final List<Long> instrumentids = getInstrumentids(cmd.getIndex(), null);
        final List<Rating> ratings = getRatings(cmd.getRecommendation());

        final ListResult listResult = createListResult(cmd, SORTFIELDS, DEFAULT_SORT_BY);

        final StockAnalysisRequest sr = createRequest(listResult, SORTFIELDS);
        sr.setRatings(ratings);
        sr.setSource(cmd.getAnalyst());
        sr.setInstrumentids(instrumentids);
        sr.setSector(cmd.getSector());
        sr.setRegion(cmd.getRegion());
        sr.setSearchtext(cmd.getSearchstring());

        final StockAnalysisResponse result = getAnalyses(sr);

        return createResult(result, listResult);
    }

    StockAnalysisRequest createRequest(ListResult listResult, Map<String, String> sortfields) {
        final StockAnalysisRequest sr = new StockAnalysisRequest();
        sr.setOffset(listResult.getOffset());
        sr.setAnzahl(listResult.getRequestedCount());
        sr.setSortBy(sortfields.get(listResult.getSortedBy()));
        sr.setAscending(listResult.isAscending());
        return sr;
    }

    ModelAndView createAggregatedResult(Command cmd, StockAnalysisResponse result,
            ListResult listResult) {
        final Map<Long, Map<StockAnalysis.Recommendation, Integer>> map = result.getCountsByInstrumentid();

        final List<Long> iids = new ArrayList<>(map.size());
        final List<Map<StockAnalysis.Recommendation, Integer>> counts = new ArrayList<>(map.size());

        listResult.setTotalCount(map.size());

        for (final Map.Entry<Long, Map<StockAnalysis.Recommendation, Integer>> entry : map.entrySet()) {
            iids.add(entry.getKey());
            counts.add(entry.getValue());
        }
        final List<Instrument> instruments =
                this.instrumentProvider.identifyInstruments(iids);

        final InstrumentNameStrategy ins 
                = RequestContextHolder.getRequestContext().getInstrumentNameStrategy();

        final MultiListSorter sorter = new MultiListSorter(new Comparator<Instrument>() {
            public int compare(Instrument o1, Instrument o2) {
                if (o1 == null || o2 == null) {
                    return -1;
                }
                return GERMAN_COLLATOR.compare(ins.getName(o1), ins.getName(o2));
            }
        }, !cmd.isAscending());
        if (INSTRUMENTNAME.equals(listResult.getSortedBy())) {
            sorter.sort(instruments, counts);
        }

        ListHelper.clipPage(cmd, instruments, counts);
        listResult.setCount(instruments.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("counts", counts);
        model.put("quotes", getQuotes(instruments));
        model.put("listinfo", listResult);
        return new ModelAndView(RscFinder.AGGREGATED_TEMPLATE, model);
    }

    private ModelAndView createResult(StockAnalysisResponse result, ListResult listResult) {
        final List<StockAnalysis> analyses = result.getAnalyses();

        listResult.setCount(analyses.size());
        listResult.setTotalCount(result.getTotalCount());

        final List<Instrument> instruments = getInstruments(analyses);

        final Map<String, Object> model = new HashMap<>();
        model.put("analyses", analyses);
        model.put("quotes", getQuotes(instruments));
        model.put("listinfo", listResult);
        return new ModelAndView(RscFinder.DEFAULT_TEMPLATE, model);
    }

    private List<Instrument> getInstruments(List<StockAnalysis> analyses) {
        final List<Long> ids = new ArrayList<>(analyses.size());
        for (final StockAnalysis analysis : analyses) {
            ids.add(analysis.getInstrumentid() != null ? analysis.getInstrumentid() : 0L);
        }
        return this.instrumentProvider.identifyInstruments(ids);
    }

    private List<Quote> getQuotes(List<Instrument> instruments) {
        final List<Quote> result = new ArrayList<>(instruments.size());
        final MarketStrategy strategy = MarketStrategyFactory.defaultStrategy();
        for (Instrument instrument : instruments) {
            try {
                result.add(instrument != null ? strategy.getQuote(instrument) : null);
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getQuotes> failed for " + instrument + ", ignore");
                }
                result.add(null);
            }
        }
        return result;
    }

    StockAnalysisResponse getAnalyses(StockAnalysisRequest sr) {
        return this.stockAnalysisProvider.getAnalyses(sr);
    }

    ListResult createListResult(ListCommand cmd, Map<String, String> sortfields,
            String defaultSortBy) {
        return ListResult.create(cmd, new ArrayList<>(sortfields.keySet()), defaultSortBy, 0);
    }

    private List<Rating> getRatings(String recommendation) {
        final Rating rating = getRating(recommendation);
        if (rating == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(rating);
    }

    static Rating getRating(String recommendation) {
        if (StringUtils.hasText(recommendation)) {
            try {
                return Rating.parse(Integer.parseInt(recommendation));
            } catch (NumberFormatException e) {
                return Rating.valueOf(recommendation);
            }
        }

        return null;
    }

    private List<Long> getInstrumentids(String index, List<Long> referenceIids) {
        if (!StringUtils.hasText(index) || !index.endsWith(".qid")) {
            return Collections.emptyList();
        }
        final List<Quote> quotes = this.instrumentProvider.getIndexQuotes(index);
        final List<Long> instrumentids = new ArrayList<>(quotes.size());
        for (final Quote quote : quotes) {
            if (quote != null) {
                if (referenceIids != null && !referenceIids.contains(quote.getInstrument().getId())) {
                    continue;
                }
                instrumentids.add(quote.getInstrument().getId());
            }
        }
        return instrumentids;
    }
}
