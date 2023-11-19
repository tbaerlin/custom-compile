/*
 * MscCompanyDateFinder.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.CompanyDate;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteOrder;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateProvider;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateRequest;
import de.marketmaker.istar.merger.provider.companydate.CompanyDateResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.Terms;

/**
 * This block shows company dates (e.g. general meeting) of the company related to its share, which is defined by the given symbol.
 */
public class MscCompanyDateFinder extends EasytradeCommandController {

    private static class Parameters {
        private Set<String> events;

        private Set<Long> iids;

        private LocalDate from;

        private LocalDate to;

        private boolean needNonEvents = false;

        public Set<String> getNonEvents() {
            return this.needNonEvents ? new HashSet<>(EVENT_MAPPING.values()) : null;
        }

        public Set<String> getEvents() {
            return this.events;
        }

        public void setEvents(Set<String> events) {
            this.needNonEvents = events.remove("x0");

            this.events = new HashSet<>();
            for (String event : events) {
                final String mapped = EVENT_MAPPING.get(event);
                this.events.add(mapped != null ? mapped : Pattern.quote(event));
            }
        }

        public Set<Long> getIids() {
            return iids;
        }

        public void setIids(Set<Long> iids) {
            this.iids = iids;
        }

        public LocalDate getFrom() {
            return from;
        }

        public void setFrom(LocalDate from) {
            this.from = from;
        }

        public LocalDate getTo() {
            return to;
        }

        public void setTo(LocalDate to) {
            this.to = to;
        }
    }

    public static class CompanyDateWithInstrument implements CompanyDate {
        private final CompanyDate delegate;

        private final Quote quote;

        public CompanyDateWithInstrument(CompanyDate delegate, Quote quote) {
            this.delegate = delegate;
            this.quote = quote;
        }

        public Quote getQuote() {
            return quote;
        }

        public YearMonthDay getDate() {
            return delegate.getDate();
        }

        public LocalizedString getEvent() {
            return delegate.getEvent();
        }

        public Long getInstrumentid() {
            return delegate.getInstrumentid();
        }
    }

    public static class Command extends ListCommand {

        private String query;

        private SymbolStrategyEnum symbolStrategy;

        /**
         * @return defines the search parameters
         * @sample symbol==&quot;710000.ETR&quot; &amp;&amp; event==x4
         */
        @NotNull
        public String getQuery() {
            return query;
        }

        @Override
        @Range(min = 1, max = 200)
        public int getCount() {
            return super.getCount();
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }
    }

    private final static DateTimeFormatter DTF_US = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final static DateTimeFormatter DTF_DE = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final static Map<String, String> EVENT_MAPPING = new HashMap<>();

    static {
        EVENT_MAPPING.put("x1", "konferenz");
        EVENT_MAPPING.put("x2", "dividende");
        EVENT_MAPPING.put("x3", "hauptversammlung");
        EVENT_MAPPING.put("x4", "(bericht|ergebnis|abschlu)");
        EVENT_MAPPING.put("x5", "aufsicht");
        EVENT_MAPPING.put("x6", "kapitalmaßnahme");
        EVENT_MAPPING.put("x7", "Anleihe");
    }

    private static final String DEFAULT_SORTBY = "relevance";

    private static final String FIELDNAME_DATE = "date";

    private static final String FIELDNAME_EVENT = "event";

    private static final String FIELDNAME_SYMBOL = "symbol";

    private static final String FIELDNAME_INDEX_SYMBOL = "indexSymbol";

    private final MarketStrategy strategy = new MarketStrategy.Builder()
            .withFilter(QuoteFilters.WITH_PRICES)
            .withSelectors(Arrays.asList(
                    new QuoteSelectors.ByOrder(QuoteOrder.VOLUME),
                    QuoteSelectors.HOME_EXCHANGE,
                    QuoteSelectors.SYMBOL_RELEVANCE
            )).build();

    public MscCompanyDateFinder() {
        super(Command.class);
    }

    private CompanyDateProvider companyDateProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private IndexCompositionProvider indexCompositionProvider;

    public void setIndexCompositionProvider(
            IndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCompanyDateProvider(CompanyDateProvider companyDateProvider) {
        this.companyDateProvider = companyDateProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final Parameters parameters = parseQuery(cmd.getQuery(), cmd.getSymbolStrategy());
        final CompanyDateRequest cdr = createRequest(cmd, parameters);
        final CompanyDateResponse r = this.companyDateProvider.getCompanyDates(cdr);

        final ListResult listResult
                = ListResult.create(cmd, CompanyDateRequest.SORTFIELDS, DEFAULT_SORTBY, r.getTotalCount());

        final List<CompanyDateWithInstrument> result = createResult(r);
        listResult.setCount(result.size());

        final Map<String, Object> model = new HashMap<>();
        model.put("companyDates", result);
        model.put("listinfo", listResult);
        model.put("language", listResult);
        return new ModelAndView("msccompanydatefinder", model);
    }

    private List<CompanyDateWithInstrument> createResult(CompanyDateResponse r) throws Exception {
        final List<CompanyDate> dates = r.getDates();

        final List<Long> iids =
                dates.stream()
                        .map(CompanyDate::getInstrumentid)
                        .collect(Collectors.toList());
        final List<Instrument> instruments = this.instrumentProvider.identifyInstruments(iids);

        final List<CompanyDateWithInstrument> result = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            final Instrument instrument = instruments.get(i);
            if (instrument == null) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<createResult> no instrument for " + iids.get(i));
                }
                continue;
            }

            final Quote quote;
            try {
                quote = this.instrumentProvider.getQuote(instrument, this.strategy);
            } catch (Exception ignore) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<createResult> no quote allowed for " + iids.get(i));
                }
                continue;
            }

            result.add(new CompanyDateWithInstrument(dates.get(i), quote));
        }

        return result;
    }

    private CompanyDateRequest createRequest(Command cmd, Parameters parameters) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        final List<Locale> locales = requestContext.getLocales();
        final Profile profile = requestContext.getProfile();

        final CompanyDateRequest request = new CompanyDateRequest();
        request.setEvents(parameters.getEvents());
        request.setNonEvents(parameters.getNonEvents());
        request.setFrom(parameters.getFrom());
        request.setTo(parameters.getTo());
        request.setIids(parameters.getIids());

        request.setOffset(cmd.getOffset());
        request.setCount(cmd.getCount());
        request.setSortBy(cmd.getSortBy());
        request.setAscending(cmd.isAscending());
        request.setLocales(locales);
        request.setLanguage(Language.valueOf(locales.get(0)));
        request.setQuoteNameStrategyName(requestContext.getQuoteNameStrategy().getStrategyName());
        request.setProfile(profile);
        QuoteFilter baseQuoteFilter = requestContext.getBaseQuoteFilter();
        request.setBaseQuoteFilter(baseQuoteFilter == QuoteFilters.FILTER_SPECIAL_MARKETS ? null : baseQuoteFilter);

        request.setWmAllowed(profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE));
        request.setConvensysAllowed(profile.isAllowed(Selector.CONVENSYS_I) && isLanguageAvailableForConvensys(locales));

        return request;
    }

    private boolean isLanguageAvailableForConvensys(List<Locale> locales) {
        return locales.stream().map(Language::valueOf)
                .anyMatch(l -> l == Language.de || l == Language.en);
    }

    private Parameters parseQuery(String query, SymbolStrategyEnum symbolStrategy) {
        if (!StringUtils.hasText(query)) {
            return new Parameters();
        }

        // HACK until all clients have adapted to new query syntax
        return doParseQuery(FinderQueryParserSupport.ensureQuotedValues(query), symbolStrategy);
    }

    private List<Term> getQueryTerms(String query) {
        final Term queryTerm;
        try {
            queryTerm = Query2Term.toTerm(query);
        } catch (Exception e) {
            logger.warn("<getQueryTerms> failed for '" + query + "':" + e.getMessage());
            return Collections.emptyList();
        }

        return queryTerm instanceof Terms.AndOp
                ? ((Terms.AndOp) queryTerm).getTerms()
                : Collections.singletonList(queryTerm);
    }

    private Parameters doParseQuery(String query, SymbolStrategyEnum symbolStrategy) {
        final List<Term> terms = getQueryTerms(query);

        final Parameters result = new Parameters();

        for (final Term term : terms) {
            if (!(term instanceof Terms.Relation)) {
                throw new BadRequestException("query too complex: " + query);
            }
            final Terms.Relation relation = (Terms.Relation) term;
            final String fieldname = relation.getIdentifier();
            final String value = relation.getValue();

            switch (fieldname) {
                case FIELDNAME_DATE:
                    final LocalDate date = parseDate(value);
                    switch (relation.getOp()) {
                        case EQ:
                            result.setFrom(date);
                            result.setTo(date);
                            break;
                        case GT:
                            result.setFrom(date.plusDays(1));
                            break;
                        case GTE:
                            result.setFrom(date);
                            break;
                        case LT:
                            result.setTo(date.minusDays(1));
                            break;
                        case LTE:
                            result.setTo(date);
                            break;
                    }
                    break;
                case FIELDNAME_EVENT:
                    result.setEvents(new HashSet<>(Arrays.asList(value.split("@"))));
                    break;
                case FIELDNAME_SYMBOL:
                    result.setIids(getIids(value, symbolStrategy));
                    break;
                case FIELDNAME_INDEX_SYMBOL:
                    result.setIids(getIidsInIndex(value, symbolStrategy));
                    break;
            }
        }

        return result;
    }

    private Set<Long> getIids(String value, SymbolStrategyEnum symbolStrategy) {
        final String[] symbols = value.split("@");
        return new HashSet<>(this.instrumentProvider.identifyInstrumentIds(symbols, symbolStrategy));
    }

    private Set<Long> getIidsInIndex(String indexSymbol, SymbolStrategyEnum symbolStrategy) {
        final Quote index = this.instrumentProvider.identifyQuote(indexSymbol, symbolStrategy, this.strategy);
        if (index != null) {
            IndexCompositionRequest request = new IndexCompositionRequest(index.getId());
            IndexCompositionResponse indexCompositionByQid = this.indexCompositionProvider.getIndexComposition(request);
            if (!indexCompositionByQid.getIndexComposition().getIids().isEmpty()) {
                return new HashSet<>(indexCompositionByQid.getIndexComposition().getIids());
            }
        }
        return null;
    }

    private LocalDate parseDate(String s) {
        return (s.contains("-") ? DTF_US : DTF_DE).parseDateTime(s).toLocalDate();
    }
}
