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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.WMDataProvider;
import de.marketmaker.istar.merger.provider.WMDataRequest;
import de.marketmaker.istar.merger.provider.WMDataResponse;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchDoc;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchProvider;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchRequest;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolListCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;

import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexConstants.FIELD_ISSUER;

/**
 * Finder for DZ Research documents.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisResearchFinder extends EasytradeCommandController {
    private static final String[] ISSUER_NUMBER_FIELDS = new String[]{"GD245", "GD240"};

    private static final String UNDEFINED = "0";

    public static class Command extends SymbolListCommand {

        private String query;

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

        @Override
        // override to deactivate @NotNull validator
        public String getSymbol() {
            return super.getSymbol();
        }
    }

    public static final String SORT_BY_DATE = "date";

    public static final String SORT_BY_SECTOR = "sector";

    public static final String SORT_BY_TITLE = "title";

    public static final String SORT_BY_ASSET_CLASS = "assetClass";

    public static final String SORT_BY_RECOMMENDATION = "recommendation";

    private static final List<String> SORT_FIELDS = Arrays.asList(
            SORT_BY_DATE,
            SORT_BY_TITLE,
            SORT_BY_ASSET_CLASS,
            SORT_BY_RECOMMENDATION,
            SORT_BY_SECTOR
    );

    private GisResearchProvider gisResearchProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private WMDataProvider wmDataProvider;

    private String urlPrefix = "";

    public GisResearchFinder() {
        super(Command.class);
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public void setWmDataProvider(WMDataProvider wmDataProvider) {
        this.wmDataProvider = wmDataProvider;
    }

    public void setGisResearchProvider(GisResearchProvider gisResearchProvider) {
        this.gisResearchProvider = gisResearchProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {

        checkPermission(Selector.DZ_RESEARCH);

        final Command cmd = (Command) o;

        final ListResult listResult = createListResult(cmd);
        final GisResearchRequest r = StringUtils.hasText(cmd.getSymbol())
                ? createRequestForInstrument(cmd, listResult)
                : createRequestFromQuery(cmd, listResult);
        final GisResearchResponse gisResearchResponse = this.gisResearchProvider.search(r);
        return createResult(gisResearchResponse, listResult, request);
    }

    private GisResearchRequest createRequestFromQuery(Command cmd, ListResult listResult) {
        GisResearchRequest result = createRequest(listResult);
        result.setQuery(createQuery(cmd));
        return result;
    }

    private GisResearchRequest createRequestForInstrument(Command cmd, ListResult listResult) {
        final Query query = getIssuerQuery(cmd);

        GisResearchRequest result = createRequest(listResult);
        result.setQuery(query);
        return result;
    }

    Query getIssuerQuery(Command cmd) {
        Instrument instrument = this.instrumentProvider.identifyInstrument(cmd);
        if (instrument == null) {
            throw new UnknownSymbolException(cmd.getSymbol());
        }

        final String issuerNumber = getWmIssuerNumber(instrument);
        return new TermQuery(new org.apache.lucene.index.Term(FIELD_ISSUER, issuerNumber));
    }

    private String getWmIssuerNumber(Instrument instrument) {
        final WMDataRequest request =
            new WMDataRequest(RequestContextHolder.getRequestContext().getProfile(),
                instrument.getId());
        WMDataResponse wm = this.wmDataProvider.getData(request);
        WMData data = wm.getData(instrument.getId());
        if (data == null) {
            return UNDEFINED;
        }
        for (String fid : ISSUER_NUMBER_FIELDS) {
            WMData.Field f = data.getField(fid);
            if (f != null) {
                return String.valueOf(f.getValue());
            }
        }
        return UNDEFINED;
    }

    private Query createQuery(Command cmd) {
        String rawQuery = cmd.getQuery();
        if (!StringUtils.hasText(rawQuery)) {
            return null;
        }
        try {
            final Term term = Query2Term.toTerm(rawQuery);
            final GisResearchFinderTermVisitor visitor
                    = new GisResearchFinderTermVisitor(this, cmd);
            term.accept(visitor);
            return visitor.getResult();
        } catch (BadRequestException e) {
            throw new BadRequestException("invalid query '" + rawQuery + "': " + e.getMessage());
        } catch (Exception e) {
            this.logger.warn("<createQuery> failed to parse '" + rawQuery + "'", e);
            throw new BadRequestException("invalid query '" + rawQuery + "'");
        }
    }

    private GisResearchRequest createRequest(ListResult listResult) {
        final GisResearchRequest result = new GisResearchRequest();
        result.setOffset(listResult.getOffset());
        result.setCount(listResult.getRequestedCount());
        result.setSortBy(listResult.getSortedBy());
        result.setAscending(listResult.isAscending());
        return result;
    }

    private ModelAndView createResult(GisResearchResponse result, ListResult listResult,
            HttpServletRequest request) {
        RequestContext requestContext = RequestContextHolder.getRequestContext();
        List<GisResearchDoc> docs = result.getDocs();

        // HACK for market manager that can handle only one country atm
        final List<List<String>> countryNames = new ArrayList<>(docs.size());
        for (final GisResearchDoc doc : docs) {
            final List<String> names = doc.getCountryNames(requestContext.getLocale());
            if (!"marketmanager".equals(getZoneName(request)) || names.size() <= 1) {
                countryNames.add(names);
                continue;
            }
            final String s = names.toString();
            countryNames.add(Collections.singletonList(s.substring(1, s.length() - 1)));
        }
        // END OF HACK

        listResult.setCount(docs.size());
        listResult.setTotalCount(result.getTotalCount());


        final Map<String, Object> model = new HashMap<>();
        model.put("docs", docs);
        model.put("countryNames", countryNames);
        model.put("urlPrefix", this.urlPrefix);
        model.put("facets", result.getFacetedSearchResult()
                .withFacetValuesSortedByName(requestContext.getLocale()).getFacets());
        model.put("quotes", getQuotes(docs));
        model.put("listinfo", listResult);
        return new ModelAndView("gisresearchfinder", model);
    }

    private Map<String, Quote> getQuotes(List<GisResearchDoc> analyses) {
        final Set<String> isins = new HashSet<>();
        for (final GisResearchDoc item : analyses) {
            isins.addAll(item.getIsins());
        }

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(new ArrayList<>(isins),
                SymbolStrategyEnum.ISIN, null, null);

        final Map<String, Quote> result = new HashMap<>();
        for (Quote quote : quotes) {
            if (quote != null) {
                result.put(quote.getInstrument().getSymbolIsin(), quote);
            }
        }
        return result;
    }

    ListResult createListResult(ListCommand cmd) {
        return ListResult.create(cmd, SORT_FIELDS, SORT_BY_DATE, 0);
    }
}
