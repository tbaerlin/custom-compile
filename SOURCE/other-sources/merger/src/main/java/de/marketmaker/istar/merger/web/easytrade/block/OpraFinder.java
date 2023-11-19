/*
 * OpraFinder.java
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.instrument.OptionDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.opra.OpraItem;
import de.marketmaker.istar.ratios.opra.OpraRatioSearchResponse;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ADF_Symbol;
import static de.marketmaker.istar.ratios.opra.OpraSymbolProviderImpl.FIELDS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OpraFinder extends AbstractFindersuchergebnis {

    private static final Set<String> MARKETS
            = new HashSet<>(Arrays.asList("A", "N", "Q", "SP", "IQ", "DJ", "CBOEX", "RUSSEL"));

    private static final Map<String, MarketDp2> EXCHANGES
            = Collections.synchronizedMap(new HashMap<String, MarketDp2>());

    private static final Map<String, String> MAPPINGS = new HashMap<>();

    static {
        MAPPINGS.put("DJI", "DJX");
        MAPPINGS.put("VIX", "VIXY'@'VIXM"); // VIXY = short-term, VIMX = mid-term
        MAPPINGS.put("NDX_X", "NDX'@'MNX");
        MAPPINGS.put("BRK-B", "BRKB");
        MAPPINGS.put("BF-B", "BFB");
    }

    // hard-coded acc. to email by Domingo Santos on input by Erich Scheuber
    // that each Opra Item is in USD
    // 66 is (currently) the id in the MDP for USD
    private static final CurrencyDp2 USD = new CurrencyDp2(66, "US Dollar") {
        {
            setSymbol(KeysystemEnum.ISO, "USD");
        }
    };

    @SuppressWarnings("UnusedDeclaration")
    public OpraFinder() {
        this(Command.class);
    }

    protected OpraFinder(Class cmdClass) {
        super(cmdClass);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.OPT, cmd, null);
        final Map<String, String> parameters = parseQuery(cmd.getQuery(), FIELDS);

        OpraRatioSearchResponse sr = null;

        if (!replaceUnderlyingVwdcode(parameters)) {
            // invalid
            sr = new OpraRatioSearchResponse();
        }

        rsr.addParameters(parameters);

        final List<String> sortfields = asSortfields(FIELDS);
        final ListResult listResult = ListResult.create(cmd, sortfields, "name", 0);
        RatioDataRecord.Field sortBy = RatioDataRecord.Field.valueOf(listResult.getSortedBy());
        if (!FIELDS.containsKey(sortBy)) {
            errors.reject("ratios.searchfailed", "unknown sortfield: '" + listResult.getSortedBy()
            + "' possible values are: " + Arrays.toString(FIELDS.keySet().toArray()));
            return null;
        }
        rsr.addParameter("sort1", FIELDS.get(sortBy).name());
        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));

        if (sr == null) {
            sr = this.ratiosProvider.getOpraItems(rsr);
        }

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        return new ModelAndView("optfinderopra", createResultModel(sr, listResult));
    }

    protected String getUnderlyingSymbol(SymbolCommand cmd) {
        final Quote underlyingQuote = this.instrumentProvider.getQuote(cmd);
        for (final Quote q : underlyingQuote.getInstrument().getQuotes()) {
            if (MARKETS.contains(q.getSymbolVwdfeedMarket())) {
                return getUnderlyingSymbol(q);
            }
        }
        return null;
    }

    private String getUnderlyingSymbol(Quote quote) {
        final IntradayData id = this.intradayProvider.getIntradayData(quote, null);
        return getUnderlyingSymbol(quote, id);
    }

    private String getUnderlyingSymbol(Quote quote, IntradayData data) {
        final SnapField field = data.getSnap().getField(ADF_Symbol.id());
        if (!field.isDefined()) {
            return null;
        }
        final String symbol = field.getValue().toString();
        final String stripped = quote.getSymbolVwdfeed().startsWith("6.")
                && (symbol.endsWith(".X") || symbol.endsWith("_X"))
                ? symbol.substring(0, symbol.length() - 2)
                : symbol;
        return MAPPINGS.containsKey(stripped) ? MAPPINGS.get(stripped) : stripped;
    }


    protected boolean replaceUnderlyingVwdcode(Map<String, String> parameters) {
        if (parameters.containsKey(RatioDataRecord.Field.underlyingSymbol.name())) {
            return true;
        }
        final String vwdcode = parameters.remove(RatioDataRecord.Field.underlyingVwdcode.name());
        if (vwdcode == null) {
            return true;
        }

        final List<Quote> quotes = getQuotes(vwdcode);
        final List<IntradayData> ids = this.intradayProvider.getIntradayData(quotes, null);
        final StringBuilder sb = new StringBuilder(ids.size() * 10);
        for (int i = 0; i < quotes.size(); i++) {
            Quote quote = quotes.get(i);
            IntradayData intradayData = ids.get(i);
            String underlyingSymbol = getUnderlyingSymbol(quote, intradayData);
            if (!StringUtils.hasText(underlyingSymbol)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("@");
            }
            sb.append("'").append(underlyingSymbol).append("'");
        }
        if (sb.length() == 0) {
            // no valid underlying symbols found, so underlyingVwdcode matches nothing, return false
            this.logger.warn("<replaceUnderlyingVwdcode> no result for '" + vwdcode + "'");
            return false;
        }
        parameters.put(RatioFieldDescription.underlyingWkn.name(), sb.toString());
        return true;
    }

    private List<Quote> getQuotes(String vwdcode) {
        final List<Quote> result = new ArrayList<>();
        final String[] tokens = vwdcode.split("@");
        for (final String token : tokens) {
            final Quote quote = this.instrumentProvider.identifyQuoteByVwdcode(token);
            for (final Quote q : quote.getInstrument().getQuotes()) {
                if (MARKETS.contains(q.getSymbolVwdfeedMarket())) {
                    result.add(q);
                }
            }
        }
        return result;
    }

    protected Map<String, Object> createResultModel(OpraRatioSearchResponse sr,
                                                    ListResult listResult) {
        final Map<String, Object> model = new HashMap<>();

        final List<OpraItem> items = sr.getItems();

        listResult.setCount(items.size());
        listResult.setOffset(sr.getOffset());
        listResult.setTotalCount(sr.getNumTotal());

        model.put("items", items);
        model.put("quotes", createQuotes(items));
        model.put("listinfo", listResult);
        return model;
    }

    private List<Quote> createQuotes(List<OpraItem> items) {
        final List<Quote> quotes = new ArrayList<>(items.size());
        for (final OpraItem item : items) {
            quotes.add(createQuote(item));
        }
        return quotes;
    }

    private QuoteDp2 createQuote(OpraItem item) {
        final QuoteDp2 quote = new QuoteDp2(0);
        quote.setSymbol(KeysystemEnum.VWDCODE, item.getVwdcode());
        quote.setInstrument(createInstrument(item));
        quote.setMarket(createMarket(item.getExchange()));
        quote.setCurrency(USD);
        return quote;
    }

    private MarketDp2 createMarket(final String exchange) {
        final MarketDp2 existing = EXCHANGES.get(exchange);
        if (existing != null) {
            return existing;
        }
        final MarketDp2 market = new MarketDp2(0, exchange);
        market.setSymbol(KeysystemEnum.VWDFEED, exchange);
        EXCHANGES.put(exchange, market);
        return market;
    }

    private OptionDp2 createInstrument(OpraItem item) {
        final OptionDp2 instrument = new OptionDp2(0);
        instrument.setName(item.getVwdcode());
        return instrument;
    }
}