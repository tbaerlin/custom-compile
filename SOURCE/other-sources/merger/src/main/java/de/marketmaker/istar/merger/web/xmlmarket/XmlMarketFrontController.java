package de.marketmaker.istar.merger.web.xmlmarket;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.AggregatedTickImpl;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.MasterDataBond;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.data.MasterDataWarrant;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.ProviderPreference;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.bonddata.BondDataProvider;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.provider.certificatedata.WarrantDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataRequest;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand;


public class XmlMarketFrontController extends MultiActionController implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String WKN = "wkn";

    private static final String ARBITRAGE_ENDPOINT = "arbitrage.xml?ukey={1}&uname={0}&wkn={2}";

    private MessageFormat arbitrageEndpointOld;

    private MessageFormat arbitrageEndpointNew;

    private static final String SEARCH_ENDPOINT = "search.xml?ukey={1}&uname={0}&searchFor={2}&wpart={3}";

    private MessageFormat searchEndpointOld;

    private MessageFormat searchEndpointNew;

    private static final String INTRADAY_ENDPOINT = "intraday.xml?ukey={1}&uname={0}&wkn={2}&platz={3}&numResults={4}&thininterval={5}";

    private MessageFormat intradayEndpointOld;

    private MessageFormat intradayEndpointNew;

    private static final String PRICESEARCH_ENDPOINT = "pricesearch.xml?ukey={1}&uname={0}&searchFor={2}&wmtype={3}&startAt=0";

    private MessageFormat pricesearchEndpointOld;

    private MessageFormat pricesearchEndpointNew;

    private static final String DETAILS_ENDPOINT = "details.xml?ukey={1}&uname={0}&wkn={2}&platz={3}";

    private MessageFormat detailsEndpointOld;

    private MessageFormat detailsEndpointNew;


    private UserRegistry authenticator;


    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider easytradeInstrumentProvider;

    private HighLowProvider highLowProvider;

    private RatiosProvider ratiosProvider;

    // some master data provider:
    private StockDataProvider stockDataProvider;

    private BondDataProvider bondDataProvider;

    private CertificateDataProvider certificateDataProvider;

    private FundDataProvider fundDataProvider;

    private WarrantDataProvider warrantDataProvider;

    private String localBaseUrl;

    public void setUserRegistry(UserRegistry authenticator) {
        this.authenticator = authenticator;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setEasytradeInstrumentProvider(
            EasytradeInstrumentProvider easytradeInstrumentProvider) {
        this.easytradeInstrumentProvider = easytradeInstrumentProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setLocalBaseUrl(String localBaseUrl) {
        this.localBaseUrl = localBaseUrl;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    public void setBondDataProvider(BondDataProvider bondDataProvider) {
        this.bondDataProvider = bondDataProvider;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setWarrantDataProvider(WarrantDataProvider warrantDataProvider) {
        this.warrantDataProvider = warrantDataProvider;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        String oldPrefix = "http://xml.market-maker.de/data/";
        String newPrefix = this.localBaseUrl + "/dmxml-1/xmlmarket/";

        arbitrageEndpointNew = new MessageFormat(newPrefix + ARBITRAGE_ENDPOINT);
        arbitrageEndpointOld = new MessageFormat(oldPrefix + ARBITRAGE_ENDPOINT);

        searchEndpointNew = new MessageFormat(newPrefix + SEARCH_ENDPOINT);
        searchEndpointOld = new MessageFormat(oldPrefix + SEARCH_ENDPOINT);

        intradayEndpointNew = new MessageFormat(newPrefix + INTRADAY_ENDPOINT);
        intradayEndpointOld = new MessageFormat(oldPrefix + INTRADAY_ENDPOINT);

        pricesearchEndpointNew = new MessageFormat(newPrefix + PRICESEARCH_ENDPOINT);
        pricesearchEndpointOld = new MessageFormat(oldPrefix + PRICESEARCH_ENDPOINT);

        detailsEndpointNew = new MessageFormat(newPrefix + DETAILS_ENDPOINT);
        detailsEndpointOld = new MessageFormat(oldPrefix + DETAILS_ENDPOINT);
    }

    public void fallback(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        final PrintWriter writer = httpResponse.getWriter();
        setResponseHeaders("text/html; charset=ISO-8859-1", httpResponse);
        new FallbackResponse().render(writer);
    }

    public void arbitrage(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        try {
            setupRequestContext(httpRequest);
            setResponseHeaders("text/xml; charset=ISO-8859-2", httpResponse);

            final String wkn = httpRequest.getParameter(WKN);

            // the wkn parameter can also contain ISINs oder vwdCodes, need AUTO strategy
            final Instrument instrument = easytradeInstrumentProvider.identifyInstrument(wkn, SymbolStrategyEnum.AUTO);
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);

            if (instrument.getInstrumentType() == InstrumentTypeEnum.STK
                    || instrument.getInstrumentType() == InstrumentTypeEnum.FND
                    || instrument.getInstrumentType() == InstrumentTypeEnum.BND) {
                quotes.sort(QuoteSorter.MARKET_SORTER);
            }
            else {
                quotes.sort(QuoteSorter.DEFAULT_SORTER);
            }

            final List<PriceRecord> prices = intradayProvider.getPriceRecords(quotes);
            final List<HighLow> highLows = highLowProvider.getHighLows52W(quotes, prices);

            final PrintWriter writer = httpResponse.getWriter();
            new ArbitrageResponse(instrument, quotes, prices, highLows).render(writer);

        } catch (UnknownSymbolException ex) {
            PrintWriter writer = httpResponse.getWriter();
            new UnknownSymbolDocument(httpRequest.getParameter(WKN)).render(writer);
        } catch (Exception ex) {
            logger.error("error in frontcontroller, returning error page", ex);
            PrintWriter writer = httpResponse.getWriter();
            new ErrorDocument("9000", ex.getClass().toString(), ex.getMessage(), null, ex.getStackTrace()).render(writer);
        } finally {
            cleanupRequestContext();
        }
    }

    // return one entry per security and only static data
    public void search(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        int MAX_RESULT = 200;
        try {
            setupRequestContext(httpRequest);
            setResponseHeaders("text/xml; charset=ISO-8859-2", httpResponse);

            final String searchFor = httpRequest.getParameter("searchFor");
            // "aktie|rente|future|index|fonds|devise|os"
            final String wpart = httpRequest.getParameter("wpart");

            final SimpleSearchCommand searchCommand;
            if (wpart == null || wpart.trim().length() == 0) {
                searchCommand = new SimpleSearchCommand(searchFor, 0, MAX_RESULT, MAX_RESULT, true);
            }
            else {
                EnumSet<InstrumentTypeEnum> types = resolveTypes(wpart);
                searchCommand = new SimpleSearchCommand(searchFor, types, 0, MAX_RESULT, MAX_RESULT, true);
            }
            SearchResponse result = easytradeInstrumentProvider.simpleSearch(searchCommand);

            Map<String, String> parameter = new LinkedHashMap<String, String>() {{
                put("searchFor", searchFor);
                put("securityType", wpart == null ? " -- Alle --" : wpart);
            }};

            final PrintWriter writer = httpResponse.getWriter();
            new SearchResultResponse(parameter, result.getInstruments()).render(writer);
        } catch (UnknownSymbolException ex) {
            PrintWriter writer = httpResponse.getWriter();
            new UnknownSymbolDocument(httpRequest.getParameter(WKN)).render(writer);
        } catch (Exception ex) {
            logger.error("error in frontcontroller, returning error page", ex);
            PrintWriter writer = httpResponse.getWriter();
            new ErrorDocument("9000", ex.getClass().toString(), ex.getMessage(), null, ex.getStackTrace()).render(writer);
        } finally {
            cleanupRequestContext();
        }
    }

    public void intraday(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        try {
            setupRequestContext(httpRequest);
            setResponseHeaders("text/xml; charset=ISO-8859-2", httpResponse);
            final String wkn = httpRequest.getParameter(WKN);
            final String platz = httpRequest.getParameter("platz");

            final Instrument instrument = easytradeInstrumentProvider.identifyInstrument(wkn, SymbolStrategyEnum.AUTO);
            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);

            final int numTicks;
            final String numResults = httpRequest.getParameter("numResults");
            if (!StringUtils.isEmpty(numResults)) {
                numTicks = Integer.valueOf(numResults);
            }
            else {
                numTicks = 10; // a default found in the old code
            }

            final Duration duration;
            final String thininterval = httpRequest.getParameter("thininterval");
            if ("null".equals(thininterval) || StringUtils.isEmpty(thininterval)) {
                duration = Duration.ZERO;
            }
            else {
                duration = Duration.standardSeconds(Long.parseLong(thininterval));
            }

            final Quote quote = findQuoteForPlatz(instrument, quotes, platz);

            final PriceRecord defaultPrice = intradayProvider.getPriceRecords(Collections.singletonList(quote)).get(0);
            final PrintWriter writer = httpResponse.getWriter();

            if (duration != Duration.ZERO) {
                final List<AggregatedTickImpl> limitedList = getAggregatedTicks(numTicks, duration, quote);
                IntradayResponse response = new IntradayResponse(instrument, quote, quotes, defaultPrice, limitedList, numTicks);
                response.render(writer);
            }
            else {
                final Timeseries<TickEvent> timeseries = getDataWithIntervals(quote);
                IntradayResponse response = new IntradayResponse(instrument, quote, quotes, defaultPrice, timeseries, numTicks);
                addTypeSpecificMasterData(instrument, response);
                response.render(writer);
            }
        } catch (UnknownSymbolException ex) {
            PrintWriter writer = httpResponse.getWriter();
            new UnknownSymbolDocument(httpRequest.getParameter(WKN)).render(writer);
        } catch (Exception ex) {
            logger.error("error in frontcontroller, returning error page", ex);
            PrintWriter writer = httpResponse.getWriter();
            new ErrorDocument("9000", ex.getClass().toString(), ex.getMessage(), null, ex.getStackTrace()).render(writer);
        } finally {
            cleanupRequestContext();
        }
    }

    private Timeseries<TickEvent> getDataWithIntervals(Quote defaultQuote) {
        final Interval interval = new LocalDate().toInterval();
        final List<IntradayData> datas = this.intradayProvider
                .getIntradayData(Collections.singletonList(defaultQuote), interval, 10); // with snap, with static, 10 sec cache
        final TickRecord tickRecord = datas.get(0).getTicks();
        return tickRecord == null ? null : tickRecord.getTimeseries(tickRecord.getInterval());
    }

    private List<AggregatedTickImpl> getAggregatedTicks(int numTicks, Duration duration,
            Quote defaultQuote) {
        Interval interval = new LocalDate().toInterval();
        // FIXME: is blindly extending the interval the only way to get the amount of ticks we need?
        interval = interval.withStart(interval.getStart().minusDays(1));
        List<AggregatedTickImpl> tickList = this.intradayProvider.getAggregatedTrades(defaultQuote, interval, duration, TickType.TRADE, numTicks, true); // numTicks = min
        return limitAndReverseList(tickList, numTicks);
    }

    private List<AggregatedTickImpl> limitAndReverseList(List<AggregatedTickImpl> tickList,
            int limit) {
        List<AggregatedTickImpl> result = new ArrayList<>();
        for (int j = tickList.size(); j-- > 0; ) {
            result.add(tickList.get(j));
            if (result.size() >= limit) {
                return result;
            }
        }
        return result;
    }

    // arbitrage seems to use MSC_PriceDatas and MSC_PriceDataExtended
    public void pricesearch(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        try {
            setupRequestContext(httpRequest);
            setResponseHeaders("text/xml; charset=ISO-8859-2", httpResponse);

            final int offset = parseInt(httpRequest.getParameter("startAt"));

            final String searchFor = httpRequest.getParameter("searchFor");
            // "aktie|rente|future|index|fonds|devise|os"
            final String wpart = httpRequest.getParameter("wpart");

            final SimpleSearchCommand searchCommand;
            if (wpart == null || wpart.trim().length() == 0) {
                searchCommand = new SimpleSearchCommand(searchFor, offset, 10, 10, true); // 10 elems returned in the orig
            }
            else {
                EnumSet<InstrumentTypeEnum> types = resolveTypes(wpart);
                searchCommand = new SimpleSearchCommand(searchFor, types, offset, 10, 10, true);
            }
            SearchResponse result = easytradeInstrumentProvider.simpleSearch(searchCommand);
            List<Instrument> instruments = result.getInstruments();

            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            final List<Quote> quotes;
            final List<PriceRecord> prices;
            final Instrument instrument;
            if (instruments.size() > 0) {
                instrument = instruments.get(0);
                quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);
                prices = intradayProvider.getPriceRecords(quotes);
            }
            else {
                instrument = null;
                quotes = Collections.emptyList();
                prices = Collections.emptyList();
            }

            Map<String, String> parameter = new LinkedHashMap<String, String>() {{
                put("searchFor", searchFor);
                put("securityType", wpart == null ? " - " : wpart);
            }};

            final PrintWriter writer = httpResponse.getWriter();
            new PriceSearchResponse(parameter, instrument, quotes, prices).render(writer);
        } catch (UnknownSymbolException ex) {
            PrintWriter writer = httpResponse.getWriter();
            new UnknownSymbolDocument(httpRequest.getParameter(WKN)).render(writer);
        } catch (Exception ex) {
            logger.error("error in frontcontroller, returning error page", ex);
            PrintWriter writer = httpResponse.getWriter();
            new ErrorDocument("9000", ex.getClass().toString(), ex.getMessage(), null, ex.getStackTrace()).render(writer);
        } finally {
            cleanupRequestContext();
        }
    }
    //http://localhost:8181/dmxml-1/xmlmarket/details.xml?ukey=2179620d6d76db77e5c27d3aebbe68dd1bba5f2b0e44e7029b919afcb12680d3&uname=aab-nt&wkn=DE0009848119&platz=
    public void details(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        try {
            setupRequestContext(httpRequest);
            setResponseHeaders("text/xml; charset=ISO-8859-2", httpResponse);
            final String wkn = StringUtils.trimWhitespace(httpRequest.getParameter(WKN));
            final String platz = StringUtils.trimWhitespace(httpRequest.getParameter("platz"));

            final Instrument instrument = easytradeInstrumentProvider.identifyInstrument(wkn, SymbolStrategyEnum.AUTO);

            final Profile profile = RequestContextHolder.getRequestContext().getProfile();
            final List<Quote> quotes = ProfiledInstrument.quotesWithPrices(instrument, profile);

            final Quote selectedQuote = findQuoteForPlatz(instrument, quotes, platz);

            final List<PriceRecord> prices = intradayProvider.getPriceRecords(quotes);
            final PriceRecord selectedPrice = intradayProvider.getPriceRecords(Collections.singletonList(selectedQuote)).get(0);

            final PrintWriter writer = httpResponse.getWriter();
            DetailsResponse response = new DetailsResponse(instrument, selectedQuote, quotes, selectedPrice, prices);
            addTypeSpecificMasterData(instrument, response);
            response.render(writer);
        } catch (UnknownSymbolException ex) {
            PrintWriter writer = httpResponse.getWriter();
            new UnknownSymbolDocument(httpRequest.getParameter(WKN)).render(writer);
        } catch (Exception ex) {
            logger.error("error in frontcontroller, returning error page", ex);
            PrintWriter writer = httpResponse.getWriter();
            new ErrorDocument("9000", ex.getClass().toString(), ex.getMessage(), null, ex.getStackTrace()).render(writer);
        } finally {
            cleanupRequestContext();
        }
    }

    public void diff(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {
        final PrintWriter writer = httpResponse.getWriter();

        final String name = httpRequest.getParameter(UserRegistry.NAME);
        final String token = httpRequest.getParameter(UserRegistry.TOKEN);

        final String wkn = httpRequest.getParameter(WKN);

        logger.info("[" + wkn + "] ");

        String searchFor;
        String platz;

        String left;
        String right;
        final String ep = httpRequest.getParameter("ep");
        switch (ep) {
            case "arbitrage":
                left = doRequest(arbitrageEndpointOld.format(new Object[]{name, token, wkn}));
                right = doRequest(arbitrageEndpointNew.format(new Object[]{name, token, wkn}));
                break;
            case "search":
                searchFor = httpRequest.getParameter("searchFor");
                searchFor = searchFor == null ? "" : searchFor;
                String wpart = httpRequest.getParameter("wpart");
                wpart = wpart == null ? "" : wpart;
                left = doRequest(searchEndpointOld.format(new Object[]{name, token, searchFor, wpart}));
                right = doRequest(searchEndpointNew.format(new Object[]{name, token, searchFor, wpart}));
                break;
            case "intraday":
                platz = httpRequest.getParameter("platz");
                String numResults = httpRequest.getParameter("numResults");
                String thininterval = httpRequest.getParameter("thininterval");
                if (thininterval == null || thininterval.trim().length() == 0) {
                    thininterval = "null";
                }
                left = doRequest(intradayEndpointOld.format(new Object[]{name, token, wkn, platz, numResults, thininterval}));
                right = doRequest(intradayEndpointNew.format(new Object[]{name, token, wkn, platz, numResults, thininterval}));
                break;
            case "pricesearch":
                searchFor = httpRequest.getParameter("searchFor");
                String wmtype = httpRequest.getParameter("wmtype");
                left = doRequest(pricesearchEndpointOld.format(new Object[]{name, token, searchFor, wmtype}));
                right = doRequest(pricesearchEndpointNew.format(new Object[]{name, token, searchFor, wmtype}));
                break;
            case "details":
                platz = httpRequest.getParameter("platz");
                left = doRequest(detailsEndpointOld.format(new Object[]{name, token, wkn, platz}));
                right = doRequest(detailsEndpointNew.format(new Object[]{name, token, wkn, platz}));
                break;
            default:
                throw new IllegalStateException("unknown endpoint: '" + ep + "'");
        }

        setResponseHeaders("text/html; charset=ISO-8859-1", httpResponse);
        new DiffResponse(left, right).render(writer);
    }

    private void addTypeSpecificMasterData(Instrument instrument, MasterDataResponse response) {
        long uiid;
        // we have additional data depending on the instrument type:
        switch (instrument.getInstrumentType()) {
            case STK:
                MasterDataStock masterDataStock =
                    stockDataProvider.getMasterData(instrument.getId(),
                        RequestContextHolder.getRequestContext().getProfile());
                response.put(masterDataStock);
                break;
            case BND:
                MasterDataBond masterDataBond = bondDataProvider.getMasterData(instrument.getId());
                response.put(masterDataBond);
                break;
            case CER:
                MasterDataCertificate masterDataCertificate = certificateDataProvider.getMasterData(instrument.getId(), ProviderPreference.VWD);
                response.put(masterDataCertificate);
                uiid = ((Derivative) instrument).getUnderlyingId();
                handleUnderlying(response, uiid);
                break;
            case FND:
                FundDataRequest request = new FundDataRequest(instrument);
                FundDataResponse fundResponse = fundDataProvider.getFundData(request);
                MasterDataFund masterDataFund = fundResponse.getMasterDataFunds().get(0);
                response.put(masterDataFund);
                break;
            case WNT:
                MasterDataWarrant masterDataWarrant = warrantDataProvider.getMasterData(instrument.getId());
                response.put(masterDataWarrant);
                uiid = ((Derivative) instrument).getUnderlyingId();
                handleUnderlying(response, uiid);
                break;
            case GNS:
            case IND:
            case MER:
                break;
            default:
                logger.info("no master data for instrument type '" + instrument.getInstrumentType() + "'");
        }
    }

    private void handleUnderlying(MasterDataResponse response, long uiid) {
        Instrument underlying;
        if (uiid > 0) {
            response.setUnderlyingId(EasytradeInstrumentProvider.iidSymbol(uiid));
            try {
                underlying = easytradeInstrumentProvider.identifyInstrument(uiid + ".iid", SymbolStrategyEnum.AUTO);
                response.setUnderlyingType(underlying == null ? "n/a" : underlying.getInstrumentType().getName(Language.de));
            } catch (Exception ex) {
                // no underlying found, ignore
            }
        }
    }

    private String doRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        Scanner s = new Scanner(url.openStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void setupRequestContext(HttpServletRequest httpRequest) throws Exception {
        Profile profile = authenticator.resolveProfile(httpRequest);
        // this uses the default locale which is german...
        RequestContext requestContext = new RequestContext(profile, LbbwMarketStrategy.INSTANCE);
        RequestContextHolder.setRequestContext(requestContext);
    }

    private void cleanupRequestContext() {
        RequestContextHolder.setRequestContext(null);
    }

    private void setResponseHeaders(String contentType, HttpServletResponse httpResponse) {
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.setContentType(contentType);
        httpResponse.setDateHeader("Date", new Date().getTime());
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);
    }

    private EnumSet<InstrumentTypeEnum> resolveTypes(String wpart) {
        EnumSet<InstrumentTypeEnum> result = EnumSet.noneOf(InstrumentTypeEnum.class);
        String[] elems = wpart.split(",");
        for (String elem : elems) {
            result.add(getTypeForString(elem));
        }
        return result;
    }

    private InstrumentTypeEnum getTypeForString(String string) {
        switch (string) {
            case "aktie":
                return InstrumentTypeEnum.STK;
            case "rente":
                return InstrumentTypeEnum.BND;
            case "future":
                return InstrumentTypeEnum.FUT;
            case "index":
                return InstrumentTypeEnum.IND;
            case "fonds":
                return InstrumentTypeEnum.FND;
            case "devise":
                return InstrumentTypeEnum.CUR;
            case "os":
                return InstrumentTypeEnum.OPT;
            default:
                return InstrumentTypeEnum.NON;
        }
    }

    private Quote findQuoteForPlatz(Instrument instrument, List<Quote> quotes, String platz) {
        if (quotes.size() == 0) {
            return NullQuote.create(instrument);
        }

        if (!StringUtils.isEmpty(platz)) {
            // next two loops try to get quote matching to requested platz
            // see R-71034, R-72379, wkn=A0V9X3 gives us DE000A0V9X33.FONDS.EUR

            for (Quote quote : quotes) {
                if (platz.equalsIgnoreCase(quote.getSymbolVwdfeedMarket())) { // direct match
                    return quote;
                }
            }

            for (Quote quote : quotes) {
                if (quote.getSymbolVwdfeed().endsWith(platz)) { // typical case: platz=FONDS.EUR
                    return quote;
                }
            }
        }

        // more R-71034, fallback to home exchange, then fallback to strategy
        // (e.g. when requested with "FONDS.EUR" and there is no such market place for the instrument)
        final String fallbackPlatz = instrument.getHomeExchange().getSymbolVwdfeed();
        if (!StringUtils.isEmpty(fallbackPlatz)) {
            for (Quote quote : quotes) {
                if (fallbackPlatz.equalsIgnoreCase(quote.getSymbolVwdfeedMarket())) {
                    return quote;
                }
            }
        }

        // default
        Quote quote = MarketStrategy.MERGER_OLD_STRATEGY.getQuote(instrument);
        return (quote != null) ? quote : NullQuote.create(instrument);
    }

    public static class RequestToMethodMapper implements MethodNameResolver {
        private static final String FALLBACK_METHOD_NAME = "fallback";

        private static final HashMap<String, String> pathInfo2methodMap = new HashMap<String, String>() {{
            put("/arbitrage.xml", "arbitrage");
            put("/intraday.xml", "intraday");
            put("/search.xml", "search");
            put("/pricesearch.xml", "pricesearch");
            put("/details.xml", "details");
            put("/diff.html", "diff");
        }};

        @Override
        public String getHandlerMethodName(
                HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
            final String pathInfo = request.getPathInfo();
            final String methodName = pathInfo2methodMap.get(pathInfo);
            if (methodName == null) {
                return FALLBACK_METHOD_NAME;
            }
            else {
                return methodName;
            }
        }

    }

    private int parseInt(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

}
