/*
 * InstrumentServerImpl.java
 *
 * Created on 22.12.2004 14:06:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import de.marketmaker.istar.domainimpl.LegacyAppKeySystemProcessor;
import de.marketmaker.istar.domainimpl.instrument.RateDp2;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.CurrencyCrossrateDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeDp2;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.export.InstrumentDao;
import de.marketmaker.istar.instrument.search.InstrumentSearcher;
import de.marketmaker.istar.instrument.search.SearchMetaRequest;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.instrument.search.SearchRequest;
import de.marketmaker.istar.instrument.search.SearchRequestResultType;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.instrument.search.SuggestRequest;
import de.marketmaker.istar.instrument.search.SuggestResponse;
import de.marketmaker.istar.instrument.search.SuggestionSearcher;
import de.marketmaker.istar.instrument.search.ValidationRequest;
import de.marketmaker.istar.instrument.search.ValidationResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class InstrumentServerImpl implements InstrumentServer, InstrumentServerUpdateable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private InstrumentSearcher instrumentSearcher;

    private InstrumentDao instrumentDao;

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private static final long SLOW_SEARCH_TIME = 2000;

    private SuggestionSearcher suggestionSearcher;

    public InstrumentServerImpl() {
    }

    public void setInstrumentBackends(boolean update, InstrumentDao instrumentDao,
            InstrumentSearcher instrumentSearcher, SuggestionSearcher suggestionSearcher) {
        this.lock.writeLock().lock();
        try {
            this.instrumentDao = instrumentDao;
            this.instrumentSearcher = instrumentSearcher;
            if (null != suggestionSearcher) {
                this.suggestionSearcher = suggestionSearcher;
            }
        }
        finally {
            this.lock.writeLock().unlock();
        }
        this.logger.info("<setInstrumentBackends> finished");
    }

    @Override
    public InstrumentResponse identifyNew(InstrumentRequest ir) {
        final TimeTaker tt = new TimeTaker();
        final List<Instrument> result = new ArrayList<>(ir.getItems().size());
        final Map<Long, Instrument> underlyings;

        this.lock.readLock().lock();
        try {
            for (InstrumentRequest.Item item : ir.getItems()) {
                result.add(identify(item));
            }
            underlyings = identifyUnderlyings(result);
        }
        finally {
            this.lock.readLock().unlock();
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<identify> #" + result.size() + " in " + tt);
        }
        final InstrumentResponse response = new InstrumentResponse();
        response.setInstruments(result);
        response.setUnderlyings(underlyings);
        return response;
    }

    @Override
    public InstrumentResponse identify(InstrumentRequest ir) {
        InstrumentResponse response = this.identifyNew(ir);
        applyLegacyConsumer(response.getInstruments());
        applyLegacyConsumer(response.getUnderlyings().values());
        return response;
    }

    private void applyLegacyConsumer(Collection<Instrument> instruments) {
        if (Objects.nonNull(instruments)) {
            instruments.stream()
                    .filter(Objects::nonNull)
                    .map(Instrument::getQuotes)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .forEach(new LegacyAppKeySystemProcessor());
        }
    }

    private Instrument identify(InstrumentRequest.Item item) {
        if (item.getKeyType() == InstrumentRequest.KeyType.IID) {
            return this.instrumentDao.getInstrument(Long.parseLong(item.getKey()));
        }
        final SearchResponse response = this.instrumentSearcher.search(item.toSearchRequest());
        return (response.getInstruments().isEmpty())
                ? null : response.getInstruments().get(0);
    }

    private Map<Long, Instrument> identifyUnderlyings(List<Instrument> instruments) {
        final Map<Long, Instrument> result = new HashMap<>();
        for (Instrument instrument : instruments) {
            if (!(instrument instanceof Derivative)) {
                continue;
            }
            final Derivative d = (Derivative) instrument;
            if (result.containsKey(d.getUnderlyingId())) {
                continue;
            }
            final Instrument underlying = this.instrumentDao.getInstrument(d.getUnderlyingId());
            if (underlying != null) {
                result.put(d.getUnderlyingId(), underlying);
            }
        }
        return result;
    }

    public SearchResponse searchNew(SearchRequest sr) {
        final TimeTaker tt = new TimeTaker();
        this.lock.readLock().lock();
        try {
            final SearchResponse result = this.instrumentSearcher.search(sr);
            tt.stop();
            if (tt.getElapsedMs() > SLOW_SEARCH_TIME) {
                this.logger.warn("<search> slow: " + sr + ", took " + tt);
            }
            return result;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public SearchResponse search(SearchRequest ir) {
        SearchResponse response = this.searchNew(ir);
        response.getQuotes().forEach(new LegacyAppKeySystemProcessor());
        applyLegacyConsumer(response.getInstruments());
        return response;
    }

    public SearchResponse simpleSearchNew(SearchRequest sr) {
        final TimeTaker tt = new TimeTaker();
        this.lock.readLock().lock();
        try {
            final SearchResponse result = this.instrumentSearcher.simpleSearch(sr);
            tt.stop();
            if (tt.getElapsedMs() > SLOW_SEARCH_TIME) {
                this.logger.warn("<simpleSearch> slow: " + sr + ", took " + tt);
            }
            return result;
        }
        catch (Exception e) {
            this.logger.warn("<simpleSearch> failed for " + sr, e);
            return SearchResponse.getInvalid();
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public SearchResponse simpleSearch(SearchRequest ir) {
        SearchResponse response = this.simpleSearchNew(ir);
        response.getQuotes().forEach(new LegacyAppKeySystemProcessor());
        applyLegacyConsumer(response.getInstruments());
        return response;
    }

    public SearchMetaResponse getMetaData(SearchMetaRequest smr) {
        this.lock.readLock().lock();
        try {
            return this.instrumentSearcher.getMetaData(smr);
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public SuggestResponse getSuggestions(SuggestRequest request) {
        this.lock.readLock().lock();
        try {
            return new SuggestResponse().withSuggestions(this.suggestionSearcher.query(request));
        } catch (Exception e) {
            this.logger.warn("<getSuggestions> failed", e);
            return SuggestResponse.getInvalid();
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public ValidationResponse validate(ValidationRequest request) {
        this.lock.readLock().lock();
        try {
            final ValidationResponse result = new ValidationResponse();
            result.setInvalidIids(this.instrumentSearcher.validate(request.getIids()));
            return result;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public static void main(String[] args) throws Exception {
        EnumSet<InstrumentTypeEnum> COUNT_TYPES = EnumSet.of(
                InstrumentTypeEnum.STK,
                InstrumentTypeEnum.BND,
                InstrumentTypeEnum.FND,
                InstrumentTypeEnum.CER,
                InstrumentTypeEnum.WNT
        );

        final List<String> DEFAULT_FIELDS = Arrays.asList("name",
                KeysystemEnum.WM_WP_NAME_KURZ.name().toLowerCase(),
                KeysystemEnum.WM_WP_NAME.name().toLowerCase(),
                KeysystemEnum.ISIN.name().toLowerCase(),
                KeysystemEnum.WKN.name().toLowerCase(),
                KeysystemEnum.WM_TICKER.name().toLowerCase());

        final InstrumentServerImpl impl = new InstrumentServerImpl();
        Controller c = new Controller();
        c.setBaseDir(new File("d:/produktion/var/data/instrument"));
        c.setInstrumentServer(impl);
        c.initialize();

        final SearchRequestStringBased req = new SearchRequestStringBased();

        System.out.println("Ready to search");

        req.setProfile(ProfileFactory.valueOf(true));
        req.setCountTypes(COUNT_TYPES);
        req.setCountInstrumentResults(true);
        req.setDefaultFields(DEFAULT_FIELDS);
        req.setMaxNumResults(500);
        req.setUsePaging(true);
        req.setPagingOffset(0);
        req.setPagingCount(50);
        req.setSearchExpression("BP AG inc");

        TimeTaker tt = new TimeTaker();
        final SearchResponse resp = impl.simpleSearchNew(req);

        System.out.println("took: " + tt);
        System.out.println(resp);
    }

    @ManagedOperation(description = "invoke identify and return formatted String")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "instrument id", description = "instrument id")
    })
    public String identifyInstrumentStr(long iid) {
        return showInstrument(0, identifyInstrument(iid)).toString();
    }

    @ManagedOperation(description = "invoke identify")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "instrument id", description = "instrument id")
    })
    public Instrument identifyInstrument(long iid) {
        final InstrumentRequest req = new InstrumentRequest();
        req.addItem("" + iid, InstrumentRequest.KeyType.IID);

        InstrumentResponse resp = identifyNew(req);

        return resp.getInstruments().get(0);
    }

    @ManagedOperation(description = "invoking search method")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "name:sap", description = "lucene query like name:sap"),
            @ManagedOperationParameter(name = "type:STK", description = "constraint like type:STK")
    })
    public String doSearch(String query, String constraint) throws Exception {
        final SearchRequestStringBased req = buildRequest(query, constraint);

        final SearchResponse resp = searchNew(req);
        return handleResponse(resp);
    }

    private SearchRequestStringBased buildRequest(String query, String constraint) {
        final SearchRequestStringBased req = new SearchRequestStringBased();
        req.setSearchExpression(query);
        req.setSearchConstraints(constraint);
        req.setCountInstrumentResults(true);
        req.setResultType(SearchRequestResultType.QUOTE_ANY);
        req.setUsePaging(true);
        req.setProfile(ProfileFactory.valueOf(true));
        req.setMaxNumResults(100);
        req.setPagingCount(10);
        req.setPagingOffset(0);
        req.setDefaultFields(Arrays.asList("name",
                KeysystemEnum.WM_WP_NAME_KURZ.name().toLowerCase(),
                KeysystemEnum.WM_WP_NAME.name().toLowerCase(),
                KeysystemEnum.ISIN.name().toLowerCase(),
                KeysystemEnum.WKN.name().toLowerCase(),
                KeysystemEnum.VALOR.name().toLowerCase(),
                KeysystemEnum.VALORSYMBOL.name().toLowerCase(),
                KeysystemEnum.WM_TICKER.name().toLowerCase(),
                KeysystemEnum.VWDCODE.name().toLowerCase(),
                KeysystemEnum.VWDFEED.name().toLowerCase()
        ));

        return req;
    }

    @ManagedOperation(description = "invoking simple search method")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "mircosaft", description = "arbitrary string to search"),
            @ManagedOperationParameter(name = "type:STK", description = "constraint like type:STK")
    })
    public String doSimpleSearch(String expr, String constraint) throws Exception {
        final SearchRequestStringBased req = buildRequest(expr, constraint);

        final SearchResponse resp = simpleSearchNew(req);
        return handleResponse(resp);
    }

    private String handleResponse(SearchResponse resp) {
        if (!resp.isValid()) {
            return "Invalid Response";
        }

        StringBuilder sb = new StringBuilder();
        int hits = resp.getNumTotalHits();
        sb.append("Total Hits: ").append(hits).append(SystemUtils.LINE_SEPARATOR);

        if (hits > 0) {
            int count = 1;
            for (final Instrument instrument : resp.getInstruments()) {
                sb.append(showInstrument(count, instrument));
                count++;
            }
        }

        return sb.toString();
    }

    @ManagedOperation(description = "invoking suggestion method")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "dws", description = "arbitrary string"),
            @ManagedOperationParameter(name = "de", description = "strategy: de,us,ch,nl,it")
    })
    public String getSuggestion(String expr, String strategy) throws Exception {
        SuggestRequest req = new SuggestRequest(ProfileFactory.valueOf(true), InstrumentNameStrategies.DEFAULT);
        req.setQuery(expr);
        req.setStrategy(strategy);
        req.setLimit(10);

        SuggestResponse resp = getSuggestions(req);
        if (!resp.isValid()) {
            return "invalid response";
        }
        List<SuggestedInstrument> suggestions = resp.getSuggestions();
        if (CollectionUtils.isEmpty(suggestions)) {
            return "NO Suggestion";
        }
        StringBuilder sb = new StringBuilder();
        for (SuggestedInstrument sug : suggestions) {
            sb.append(sug.getName()).append(" ").append(sug.getInstrumentType());
            sb.append(" ").append(sug.getSymbolIsin()).append(" ").append(sug.getSymbolWkn());
            sb.append(SystemUtils.LINE_SEPARATOR);
        }

        return sb.toString();
    }

    private StringBuffer showInstrument(int count, Instrument instrument) {
        StringBuffer sb = new StringBuffer();
        sb.append("\n" + count + ": " + instrument.getId()
                + "\n" + instrument.getInstrumentType() + " SecID: " + instrument.getId()
                + "\nGatrixx Name: " + instrument.getSymbol(KeysystemEnum.GATRIXX)
                + "\n" + instrument.getSymbol(KeysystemEnum.ISIN)
                + " #quotes: " + instrument.getQuotes().size()
        );
        sb.append("\n Sector=" + instrument.getSector());

        if (instrument instanceof DerivativeDp2) {
            sb.append("\n SubscriptionRatio " + ((DerivativeDp2) instrument).getSubscriptionRatio());
        }

        if (instrument instanceof CurrencyCrossrateDp2) {
            final CurrencyCrossrateDp2 crossrate = (CurrencyCrossrateDp2) instrument;
            sb.append("\n CurrencyCrossrateDp2 source: " + crossrate.getSourceCurrency());
            sb.append("\n CurrencyCrossrateDp2 target: " + crossrate.getTargetCurrency());
            sb.append("\n CurrencyCrossrateDp2 factor: " + crossrate.getSourceToTargetFactor());
        }
        else if (instrument instanceof RateDp2) {
            final RateDp2 rate = (RateDp2) instrument;
            sb.append("\n RateDp2 source: " + rate.getSourceCurrency());
            sb.append("\n RateDp2 target: " + rate.getTargetCurrency());
            sb.append("\n RateDp2 factor: " + rate.getSourceToTargetFactor());
        }
        for (final Quote quote : instrument.getQuotes()) {
            sb.append("\n");
            sb.append("\n            " + quote.getId() + " -> "
                    + quote.getSymbolWmWpNameKurz() + " - " + quote.getSymbol(KeysystemEnum.VWDFEED)
                    + ", " + quote.getMarket().getSymbolVwdfeed());
            sb.append("\n" + Arrays.asList(quote.getEntitlement().getEntitlements(KeysystemEnum.VWDFEED)));
            sb.append("\n                " + quote.getCurrency());
            sb.append("\n"+" " + quote.getMinimumQuotationSize());
        }
        return sb;
    }


}
