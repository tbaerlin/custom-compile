/*
 * RscFinder.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchProvider;
import de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchRequest;
import de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchResponse;
import de.marketmaker.istar.merger.provider.lbbwresearch.ResultDocument;
import de.marketmaker.istar.merger.provider.lbbwresearch.ResultDocument.CompanyInfo;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;

import static de.marketmaker.istar.merger.provider.lbbwresearch.BasicDocument.*;
import static de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchIndexConstants.FIELD_ISIN;

/**
 * Finder for LBBW Research documents.
 * @author mcoenen
 */
public class LbbwResearchFinder extends EasytradeCommandController {

    public static class Command extends SymbolListCommand {

        private String query;

        private String urlPrefix;

        /**
         * A query expression ...
         * @sample text='daimler' and date > '2012-04-01'
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public void setUrlPrefix(String urlPrefix) {
            this.urlPrefix = urlPrefix;
        }

        @Override
        // override to deactivate @NotNull validator
        public String getSymbol() {
            return super.getSymbol();
        }
    }

    private static final List<String> SORT_FIELDS = Arrays.asList(
            SORT_BY_TITLE,
            SORT_BY_PUBLICATION_DATE,
            SORT_BY_CATEGORY,
            SORT_BY_RATING,
            SORT_BY_TARGET_PRICE
    );

    private LbbwResearchProvider lbbwResearchProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private String urlPrefix = "";

    public LbbwResearchFinder() {
        super(Command.class);
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public void setLbbwResearchProvider(LbbwResearchProvider lbbwResearchProvider) {
        this.lbbwResearchProvider = lbbwResearchProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        checkPermission(Selector.LBBW_RESEARCH);

        final Command cmd = (Command) o;

        final ListResult listResult = createListResult(cmd);
        final LbbwResearchRequest r = StringUtils.hasText(cmd.getSymbol())
                ? createRequestForInstrument(cmd, listResult)
                : createRequestFromQuery(cmd, listResult);
        final LbbwResearchResponse lbbwResearchResponse = this.lbbwResearchProvider.search(r);
        final String urlPrefix = StringUtils.hasText(cmd.urlPrefix) ? cmd.urlPrefix : this.urlPrefix;
        return createResult(lbbwResearchResponse, listResult, request, urlPrefix);
    }

    private LbbwResearchRequest createRequestFromQuery(Command cmd, ListResult listResult) {
        LbbwResearchRequest result = createRequest(listResult);
        result.setQuery(createQuery(cmd));
        return result;
    }

    private LbbwResearchRequest createRequestForInstrument(Command cmd, ListResult listResult) {
        final Query query = getSymbolQuery(cmd);

        LbbwResearchRequest result = createRequest(listResult);
        result.setQuery(query);
        return result;
    }

    Query getSymbolQuery(Command cmd) {
        Instrument instrument = this.instrumentProvider.identifyInstrument(cmd);
        if (instrument == null) {
            throw new UnknownSymbolException(cmd.getSymbol());
        }

        final String isin = instrument.getSymbolIsin();
        return new TermQuery(new org.apache.lucene.index.Term(FIELD_ISIN, isin));
    }

    private Query createQuery(Command cmd) {
        String rawQuery = cmd.getQuery();
        if (!StringUtils.hasText(rawQuery)) {
            return null;
        }
        try {
            final Term term = Query2Term.toTerm(rawQuery);
            final LbbwResearchFinderTermVisitor visitor
                    = new LbbwResearchFinderTermVisitor(this, cmd);
            term.accept(visitor);
            return visitor.getResult();
        } catch (BadRequestException e) {
            throw new BadRequestException("invalid query '" + rawQuery + "': " + e.getMessage());
        } catch (Exception e) {
            this.logger.warn("<createQuery> failed to parse '" + rawQuery + "'", e);
            throw new BadRequestException("invalid query '" + rawQuery + "'");
        }
    }

    private LbbwResearchRequest createRequest(ListResult listResult) {
        final LbbwResearchRequest result = new LbbwResearchRequest();
        result.setOffset(listResult.getOffset());
        result.setCount(listResult.getRequestedCount());
        result.setSortBy(listResult.getSortedBy());
        result.setAscending(listResult.isAscending());
        return result;
    }

    private ModelAndView createResult(LbbwResearchResponse result, ListResult listResult,
            HttpServletRequest request, String urlPrefix) {
        List<ResultDocument> resultDocuments = result.getResultDocuments();

        // HACK for market manager that can handle only one country atm
        final List<List<String>> countryNames = new ArrayList<>(resultDocuments.size());
        for (final ResultDocument resultDocument : resultDocuments) {
            final List<String> names =
                    resultDocument.getCompanyInfos().stream()
                            .map(CompanyInfo::getCountry)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
            if (!"marketmanager".equals(getZoneName(request)) || names.size() <= 1) {
                countryNames.add(names);
                continue;
            }
            final String s = names.toString();
            countryNames.add(Collections.singletonList(s.substring(1, s.length() - 1)));
        }
        // END OF HACK

        listResult.setCount(resultDocuments.size());
        listResult.setTotalCount(result.getTotalCount());


        final Map<String, Object> model = new HashMap<>();
        model.put("resultDocuments", resultDocuments);
        model.put("countryNames", countryNames);
        model.put("urlPrefix", urlPrefix);
        model.put("facets", result.getFacetedSearchResult()
                .withFacetValuesSortedByName(Locale.GERMAN).getFacets());
        model.put("quotes", getQuotes(resultDocuments));
        model.put("listinfo", listResult);
        return new ModelAndView("lbbwresearchfinder", model);
    }

    private Map<String, Quote> getQuotes(List<ResultDocument> analyses) {
        final List<String> isins =
                analyses.stream()
                        .flatMap(bd -> bd.getCompanyInfos().stream())
                        .map(CompanyInfo::getIsin)
                        .distinct()
                        .collect(Collectors.toList());

        final List<Quote> quotes =
                this.instrumentProvider.identifyQuotes(isins, SymbolStrategyEnum.ISIN, null, null);

        return quotes.stream()
                .filter(quote -> quote != null)
                .collect(Collectors.toMap(quote -> quote.getInstrument().getSymbolIsin(), Function.identity()));
    }

    ListResult createListResult(ListCommand cmd) {
        return ListResult.create(cmd, SORT_FIELDS, SORT_BY_PUBLICATION_DATE, 0);
    }
}
