package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.provider.gisresearch.GisResearchDoc;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.runtime.RecognitionException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domain.special.DzBankRecord;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.DzBankRecordProvider;
import de.marketmaker.istar.merger.provider.DzBankRecordSearchResponse;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.IstarQueryListRequest;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.query.DistinctValueCounter;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListInfo;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.finder.Query2Term;
import de.marketmaker.istar.merger.web.finder.Term;
import de.marketmaker.istar.merger.web.finder.TermVisitor;
import de.marketmaker.istar.merger.web.finder.Terms;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * <p>
 * Queries DzBankRecord objects by applying a search term to the list of known DzBankRecords.
 * </p>
 * <p>
 * The search term is specified by the <code>query</code> parameter and can consist of multiple
 * expressions.<br/>
 * Examples:
 * <ul>
 * <li>java-style regular expression matching: <pre>sonderheit =~ '.*Risiko.*'</pre></li>
 * <li>exact match: <pre>risikoklasse = '4'</pre></li>
 * <li>range match: <pre>bonifikation >= '1'</pre></li>
 * <li>combined expressions: <pre>(bonifikation >= '1') AND (risikoklasse = '4')</pre></li>
 * </ul>
 * </p>
 * Sorting can be done by providing one or more  fieldnames, followed by &quot;asc&quot; or &quot;desc&quot;
 * depending on the required sort order.
 * <p>
 * Allowed search and sort fields can be found in the sort field lists in the response
 * or in the GIS_FinderMetadate block.
 * </p>
 * @author Michael Wohlfart
 * @sample query bonifikation = '1'
 */
public class GisFinder extends EasytradeCommandController {

    public static class Command extends ListCommand {
        public static final String DEFAULT_SORT_BY = "indexPosition";

        public static final boolean DEFAULT_ASCENDING = true;

        private String query = NULL_QUERY;

        private String zone;

        public Command() {
            setSortBy(DEFAULT_SORT_BY);
            setAscending(DEFAULT_ASCENDING);
        }

        /**
         * Search terms as they are provided by {@link GisResearchDoc}.
         * For example 'text', 'type', 'documentType', 'sector', 'issuer', 'date', 'country' and 'symbol'.
         * For possible search term values see {@link GisFinderMetadata}.
         */
        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        @Override
        public boolean equals(Object o) {
            if (!super.equals(o)) return false;

            Command other = (Command) o;
            if (!this.zone.equals(other.zone)) return false;
            if (!query.equals(other.query)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (31 * (31 * super.hashCode()) + query.hashCode()) + zone.hashCode();
        }
    }

    private class Result {
        DzBankRecordSearchResponse response;

        List<Quote> underlyingQuotes;

        List<Quote> quotes;

        List<PriceQuality> priceQualities;

        List<PriceRecord> prices;

        List<HighLow> highLows;

        List<RatioDataRecord> ratioData;

        public Result(DzBankRecordSearchResponse response) {
            if (response != null && response.isValid()) {
                this.response = response;
                final List<DzBankRecord> records = response.getDzBankRecords();
                this.underlyingQuotes = resolveUnderlyingQuotes(records);
                this.quotes = resolveQuotes(records);
                this.priceQualities = getPriceQualities(this.quotes);
                this.prices = intradayProvider.getPriceRecords(quotes);
                this.highLows = highLowProvider.getHighLows52W(quotes, prices);
                final Map<Long, RatioDataRecord> ratioDataHash = ratiosProvider.getRatioDatas(quotes,
                        RatioFieldDescription.FIELDNAMES.get(InstrumentTypeEnum.CER));
                this.ratioData = new ArrayList<>();
                for (Quote quote : quotes) {
                    if (quote != null) {
                        ratioData.add(ratioDataHash.get(quote.getId()));
                    }
                }
            }
        }
    }

    // to be used as query when an empty search result should be returned.
    private static final String NULL_QUERY = String.valueOf((Object) null);

    private static final String IID = "iid";

    private static final String VWDCODE = "vwdcode";

    private static final String UNDERLYING_IID = "underlyingIid";

    private static final String RENDITE = "rendite";

    private static final String COUPON = "coupon";

    private static final String UNDERLYING_VWDCODE = "underlyingVwdcode";

    private static final HashSet UNDERLYING_FIELDS
            = new HashSet<>(Arrays.asList(UNDERLYING_VWDCODE, UNDERLYING_IID));

    private static final HashSet PERCENT_FIELDS
            = new HashSet<>(Arrays.asList(RENDITE, COUPON));

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private IntradayProvider intradayProvider;

    private DzBankRecordProvider dzProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private UnderlyingShadowProvider underlyingShadowProvider;

    private RatiosProvider ratiosProvider;

    private HighLowProvider highLowProvider;

    public GisFinder() {
        super(Command.class);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    @Override
    protected void onBind(HttpServletRequest request, Object o) throws Exception {
        final Command c = (Command) o;
        final Zone z = (Zone) request.getAttribute(ZoneDispatcherServlet.ZONE_ATTRIBUTE);
        c.zone = (z != null) ? z.getName() : "";

        if (c.getQuery() != NULL_QUERY) {
            return;
        }
        handleDependsOnNwsNachricht(request, c);
    }

    /**
     * Special case: if this block depends on an NwsNachricht-Block, that block's model already
     * contains the quotes for which we need to find gis records. So instead evaluating the
     * news block's response in the client and issuing another GisFinder request, the client
     * can add a GisFinder request w/o a query that depends on the news block.
     */
    private void handleDependsOnNwsNachricht(HttpServletRequest request, Command c) {
        final Map<String, Object> model = getDependencyModel(request);
        if (model == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        final List<Quote> quotes = (List<Quote>) model.get(NwsNachricht.QUOTES_KEY);

        if (quotes == null) {
            c.setQuery(NULL_QUERY);
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (Quote quote : quotes) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(quote.getInstrument().getId());
        }
        final String iids = sb.toString();
        c.setQuery(IID + " IN (" + iids + ") or " + UNDERLYING_IID + " IN (" + iids + ")");
        c.setCount(1000); // effectively disable paging
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object cmdObject, BindException errors) throws Exception {

        checkPermission(Selector.DZ_KAPITALKMARKT);

        final HashMap<String, Object> model = new HashMap<>();
        final Command cmd = (Command) cmdObject;
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final Profile profile = requestContext.getProfile();

        final Term term = getTerm(cmd);
        final IstarQueryListRequest req = new IstarQueryListRequest(
                cmd.getOffset(), cmd.getCount(), cmd.getSortBy(), cmd.isAscending(), term, profile);

        if (cmd.getQuery() == NULL_QUERY) {
            model.put("listinfo", ListInfo.createEmptyInfo(req));
            model.put("elements", Collections.emptyList());
            return new ModelAndView("gisfinder", model);
        }

        Result r = null;

        try {
            final DzBankRecordSearchResponse dzBankRecords = this.dzProvider.searchDzBankRecords(req);
            r = new Result(dzBankRecords);
        } catch (Throwable t){
            this.logger.warn("<doHandle> searchDzBankRecords failed", t);
        }

        if (r == null || r.response == null) {
            errors.reject("search.failure");
            return null;
        }

        final List<DzBankRecord> records = r.response.getDzBankRecords();
        final List<Quote> underlyingQuotes = r.underlyingQuotes;
        final List<Quote> quotes = r.quotes;
        final List<RatioDataRecord> ratioData = r.ratioData;

        final List<PriceRecord> prices = intradayProvider.getPriceRecords(quotes);
        final List<HighLow> highLows = highLowProvider.getHighLows52W(quotes, prices);

        final Set<? extends DistinctValueCounter> metadataSet = renameUnderlyings(r.response.getMetadataSet(), cmd);
        model.put("listinfo", r.response.getListInfo());
        model.put("elements", records);
        model.put("withPib", profile.isAllowed(Selector.PRODUCT_WITH_PIB));
        model.put("quotes", quotes);
        model.put("prices", prices);
        model.put("highLows", highLows);
        model.put("ratioData", ratioData);
        model.put("underlyingQuotes", underlyingQuotes);
        model.put("metadata", metadataSet);
        return new ModelAndView("gisfinder", model);
    }

    private Set<? extends DistinctValueCounter> renameUnderlyings(
            Set<? extends DistinctValueCounter> metadata, Command cmd) {
        return GisFinderMetadata.rewriteUnderlyings(metadata, instrumentProvider, "marketmanager".equals(cmd.zone));
    }

    private List<Quote> resolveUnderlyingQuotes(List<DzBankRecord> records) {
        return resolveQuotes(records, true);
    }

    private List<Quote> resolveQuotes(List<DzBankRecord> records) {
        return resolveQuotes(records, false);
    }

    private List<String> getIids(List<DzBankRecord> records, boolean underlying) {
        final List<String> result = new ArrayList<>(records.size());
        for (DzBankRecord record : records) {
            Long iid = underlying ? record.getUnderlyingIid() : record.getIid();
            result.add(iid != null ? iid.toString() : "0");
        }
        return result;
    }

    private List<Quote> resolveQuotes(List<DzBankRecord> records, boolean underlying) {
        final List<String> iids = getIids(records, underlying);
        return this.instrumentProvider.identifyQuotes(iids, SymbolStrategyEnum.IID, null, null);
    }

    private List<PriceQuality> getPriceQualities(List<Quote> quotes) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final ArrayList<PriceQuality> result = new ArrayList<>(quotes.size());
        for (Quote quote : quotes) {
            result.add(quote != null ? profile.getPriceQuality(quote) : PriceQuality.NONE);
        }
        return result;
    }

    protected Term getTerm(Command cmd) {
        if (!StringUtils.hasText(cmd.getQuery()) || cmd.getQuery() == NULL_QUERY) {
            return null;
        }
        try {
            return rewrite(Query2Term.toTerm(cmd.getQuery()));
        } catch (RecognitionException e) {
            throw new BadRequestException("invalid query");
        }
    }

    private Term rewrite(Term term) {
        final LinkedList<Term> termStack = new LinkedList<>();
        term.accept(new TermVisitor() {
            @Override
            public void visit(Terms.AndOp term) {
                final ArrayList<Term> terms = new ArrayList<>(term.getTerms().size());
                for (Term t : term.getTerms()) {
                    t.accept(this);
                    terms.add(termStack.pop());
                }
                termStack.push(new Terms.AndOp(terms));
            }

            @Override
            public void visit(Terms.OrOp term) {
                final ArrayList<Term> terms = new ArrayList<>(term.getTerms().size());
                for (Term t : term.getTerms()) {
                    t.accept(this);
                    terms.add(termStack.pop());
                }
                termStack.push(new Terms.OrOp(terms));
            }

            @Override
            public void visit(Terms.NotOp term) {
                term.getTerm().accept(this);
                termStack.push(new Terms.NotOp(termStack.pop()));
            }

            @Override
            public void visit(Terms.In term) {
                if (UNDERLYING_FIELDS.contains(term.getIdentifier())) {
                    final Set<String> uiids = getUnderlyingIids(term.getValues(), term.getIdentifier());
                    termStack.push(toUnderlyingIidTerm(uiids));
                }
                else if (VWDCODE.equals(term.getIdentifier())) {
                    final Set<String> uiids = getIidsForVwdCodes(term.getValues());
                    termStack.push(toIidTerm(uiids));
                }
                else {
                    termStack.push(term);
                }
            }

            @Override
            public void visit(Terms.Relation term) {
                if (term.getOp() == Terms.Relation.Op.EQ && UNDERLYING_FIELDS.contains(term.getIdentifier())) {
                    final Set<String> values = new HashSet<>(Arrays.asList(term.getValue().split("@")));
                    final Set<String> uiids = getUnderlyingIids(values, term.getIdentifier());
                    termStack.push(toUnderlyingIidTerm(uiids));
                }
                else if (term.getOp() == Terms.Relation.Op.EQ && VWDCODE.equals(term.getIdentifier())) {
                    final Set<String> values = new HashSet<>(Arrays.asList(term.getValue().split("@")));
                    final Set<String> uiids = getIidsForVwdCodes(values);
                    termStack.push(toIidTerm(uiids));
                }
                else if (PERCENT_FIELDS.contains(term.getIdentifier())) {
                    termStack.push(toPercent(term));
                }
                else if (term.getOp() == Terms.Relation.Op.EQ && term.getValue().startsWith("+")) {
                    termStack.push(removePlus(term));
                }
                else {
                    termStack.push(term);
                }
            }
        });
        return termStack.pop();
    }

    private Term removePlus(Terms.Relation term) {
        return new Terms.Relation(term.getIdentifier(), term.getOp(),
                term.getValue().substring("+".length()));
    }

    private Term toPercent(Terms.Relation term) {
        return new Terms.Relation(term.getIdentifier(), term.getOp(),
                new BigDecimal(term.getValue().trim()).divide(HUNDRED).toPlainString());
    }

    private Term toIidTerm(Set<String> uiids) {
        if (uiids.size() == 1) {
            return new Terms.Relation(IID, Terms.Relation.Op.EQ, uiids.iterator().next());
        }
        return new Terms.In(IID, uiids);
    }

    private Term toUnderlyingIidTerm(Set<String> uiids) {
        if (uiids.size() == 1) {
            return new Terms.Relation(UNDERLYING_IID, Terms.Relation.Op.EQ, uiids.iterator().next());
        }
        return new Terms.In(UNDERLYING_IID, uiids);
    }

    public void setDzProvider(DzBankRecordProvider dzProvider) {
        this.dzProvider = dzProvider;
    }

    protected Set<String> getIidsForVwdCodes(Set<String> vwdCodes) {
        final Set<String> result = new HashSet<>();
        for (String vwdCode : vwdCodes) {
            final Instrument instrument = instrumentProvider.identifyInstrument(vwdCode, SymbolStrategyEnum.VWDCODE);
            if (instrument != null) {
                result.add(Long.toString(instrument.getId()));
            }
        }
        return result;
    }

    protected Set<String> getUnderlyingIids(Set<String> symbols, String identifier) {
        final UnderlyingResolver underlyingResolver
                = new UnderlyingResolver(this.instrumentProvider, this.underlyingShadowProvider);
        final SymbolStrategyEnum strategyEnum = UNDERLYING_IID.equals(identifier)
                ? SymbolStrategyEnum.IID : SymbolStrategyEnum.VWDCODE;
        for (String symbol : symbols) {
            underlyingResolver.addSymbol(symbol, strategyEnum);
        }
        final Set<Long> uiids = underlyingResolver.getUnderlyingIids(false);
        return uiids.isEmpty() ? symbols2StringSet(symbols) : asStringSet(uiids);
    }

    private Set<String> symbols2StringSet(Set<String> symbols) {
        final HashSet<String> result = new HashSet<>();
        for (String symbol : symbols) {
            result.add(EasytradeInstrumentProvider.idString(symbol));
        }
        return result;
    }

    private Set<String> asStringSet(Set<Long> uiids) {
        final HashSet<String> result = new HashSet<>();
        for (Long uiid : uiids) {
            result.add(Long.toString(uiid));
        }
        return result;
    }

}
