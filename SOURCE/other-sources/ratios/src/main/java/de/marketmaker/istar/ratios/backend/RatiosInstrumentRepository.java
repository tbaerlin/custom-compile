/*
 * InstrumentRepository.java
 *
 * Created on 13.09.2005 13:06:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.InstrumentServerUpdateable;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.export.InstrumentDao;
import de.marketmaker.istar.instrument.search.InstrumentSearcher;
import de.marketmaker.istar.instrument.search.SuggestionSearcher;
import de.marketmaker.istar.ratios.BackendUpdateReceiver;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioInstrumentFilter;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosInstrumentRepository implements InstrumentServerUpdateable,
        InstrumentRepository {

    private static final Pattern EUR_FX_PATTERN = Pattern.compile("10\\.EUR.*\\.FX");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Set<InstrumentTypeEnum> allowedTypes = EnumSet.noneOf(InstrumentTypeEnum.class);

    private InstrumentDao instrumentDao;

    private ChicagoFileCreator chicagoFileCreator = new ChicagoFileCreator(); // null object

    private Long2LongMap qidToIid = new Long2LongOpenHashMap();

    private Long2LongMap nonStandardUnderlyingIidToQid = new Long2LongOpenHashMap();

    private Long2ObjectMap<LongList> iidToDerivativeQids = new Long2ObjectOpenHashMap<>();

    private Map<String, Long> currencyToQuoteid = new HashMap<>();

    private BackendUpdateReceiver frontend;

    private IndexCompositionProvider indexCompositionProvider;

    private int iidModForDevelopment = 0;

    private final Set<InstrumentTypeEnum> modForDevelopmentTypes = EnumSet.allOf(InstrumentTypeEnum.class);

    private boolean initialized = false;

    private StaticReader staticReader;

    public void setStaticReader(StaticReader staticReader) {
        this.staticReader = staticReader;
    }

    public void setTypes(String[] typeNames) {
        addAll(typeNames, this.allowedTypes);
    }

    public void setModForDevelopmentTypes(String typeNames) {
        if (StringUtils.hasText(typeNames)) {
            this.modForDevelopmentTypes.clear();
            addAll(typeNames.split(","), this.modForDevelopmentTypes);
        }
    }

    private void addAll(String[] typeNames, final Set<InstrumentTypeEnum> types) {
        for (String type : typeNames) {
            types.add(InstrumentTypeEnum.valueOf(type.trim()));
        }
    }

    public void setFrontend(BackendUpdateReceiver frontend) {
        this.frontend = frontend;
    }

    public void setIndexCompositionProvider(IndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setIidModForDevelopment(int iidModForDevelopment) {
        this.iidModForDevelopment = iidModForDevelopment;
    }

    public void setChicagoFileCreator(ChicagoFileCreator chicagoFileCreator) {
        this.chicagoFileCreator = chicagoFileCreator;
    }

    public void setInstrumentBackends(boolean update, InstrumentDao instrumentDao,
            InstrumentSearcher instrumentSearcher, SuggestionSearcher suggestionSearcher) {
        this.logger.info("<setInstrumentBackends> reset instrument dao ...");

        final Set<Long> underlyingIds = instrumentSearcher.getUnderlyingIds();
        if (underlyingIds == null) {
            this.logger.error("<setInstrumentBackends> no underlyingIds found?!");
            return;
        }
        this.instrumentDao = instrumentDao;
        if (!this.initialized) {
            initMaps(underlyingIds);
        }
        initInstruments(update, underlyingIds);
        this.initialized = true;

        if (this.staticReader != null) {
            this.staticReader.afterInstrumentUpdate(update);
        }
        this.logger.info("<setInstrumentBackends> ... finished");
    }

    public Collection<Long> getDerivativeQuoteids(long instrumentid) {
        this.lock.readLock().lock();

        try {
            return this.iidToDerivativeQids.get(instrumentid);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Long getInstrumentid(long quoteid) {
        this.lock.readLock().lock();

        try {
            return this.qidToIid.get(quoteid);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Instrument getInstrument(long instrumentid) {
        this.lock.readLock().lock();

        try {
            return this.instrumentDao.getInstrument(instrumentid);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Quote getQuote(long quoteid) {
        return getQuote(getInstrumentid(quoteid), quoteid);
    }

    private Quote getQuote(Long iid, long quoteid) {
        if (iid == null) {
            return null;
        }
        final Instrument instrument = getInstrument(iid);
        return (instrument != null) ? instrument.getQuote(quoteid) : null;
    }

    public Long getQuoteid(String currency) {
        this.lock.readLock().lock();

        try {
            return this.currencyToQuoteid.get(currency);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Quote getBenchmarkQuote(Instrument instrument) {
        final Long bqid = this.indexCompositionProvider.getBenchmarkId(instrument);
        return (bqid != null) ? getQuote(bqid) : null;
    }

    public Long getNonStandardUnderlyingQid(long iid) {
        this.lock.readLock().lock();

        try {
            return this.nonStandardUnderlyingIidToQid.get(iid);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    private void initMaps(Set<Long> underlyingIds) {
        this.logger.info("<initMaps>...");

        try {
            for (Iterator<Instrument> it = getInstrumentIterator(false); it.hasNext(); ) {
                final Instrument instrument = it.next();
                if (isToBeIgnored(instrument)) {
                    if (instrument.getInstrumentType() == InstrumentTypeEnum.IND) {
                        updateQidToIid(this.qidToIid, instrument);
                    }
                    continue;
                }

                if (instrument instanceof Derivative) {
                    addQidsToUnderlying(this.iidToDerivativeQids, (Derivative) instrument);
                }

                updateQidToIid(this.qidToIid, instrument);

                final boolean allowed = this.allowedTypes.contains(instrument.getInstrumentType());
                if (!allowed && underlyingIds.contains(instrument.getId())) {
                    final List<Quote> quotes = getValidUnderlyingQuotes(instrument);
                    if (quotes.size() == 1) {
                        final Quote quote = quotes.get(0);
                        // an underlying of a type we do not normally process
                        this.qidToIid.put(quote.getId(), instrument.getId());
                        this.nonStandardUnderlyingIidToQid.put(instrument.getId(), quote.getId());
                    }
                }
            }

            this.logger.info("<initMaps> #qidToIid = " + qidToIid.size());
            this.logger.info("<initMaps> #nonStandardUnderlyingsIid2Qid = " + nonStandardUnderlyingIidToQid.size());
            this.logger.info("<initMaps> #derivativequoteidsByInstrumentid = " + iidToDerivativeQids.size());
        } catch (Exception e) {
            throw new RuntimeException("<initMaps> failed", e);
        }
    }

    private void initInstruments(boolean update, Set<Long> underlyingIds) {
        this.logger.info("<initInstruments> regularInstrumentUpdate=" + this.initialized
                + ", update=" + update + ", #underlings=" + underlyingIds.size());


        final Long2LongMap tmpQidToIid = new Long2LongOpenHashMap();
        final Long2LongMap tmpNonStandardUnderlyingIidToQid = new Long2LongOpenHashMap();
        final Long2ObjectMap<LongList> tmpIidToDerivativeQids = new Long2ObjectOpenHashMap<>();
        final Map<String, Long> tmpCurrencyToQuoteid = new HashMap<>();

        boolean holdsLock = false;

        try {
            this.chicagoFileCreator.prepare(update);

            final RatiosEncoder encoder = new RatiosEncoder();

            for (Iterator<Instrument> it = getInstrumentIterator(update); it.hasNext(); ) {
                final Instrument instrument = it.next();
                if (isToBeIgnored(instrument)) {
                    if (instrument.getInstrumentType() == InstrumentTypeEnum.IND) {
                        updateQidToIid(tmpQidToIid, instrument);
                    }
                    continue;
                }

                final boolean isUnderlying = underlyingIds.contains(instrument.getId());

                if (instrument instanceof Derivative) {
                    addQidsToUnderlying(tmpIidToDerivativeQids, (Derivative) instrument);
                }

                updateQidToIid(tmpQidToIid, instrument);

                final boolean allowed = this.allowedTypes.contains(instrument.getInstrumentType());
                if (!allowed) {
                    if (isUnderlying) {
                        final List<Quote> quotes = getValidUnderlyingQuotes(instrument);
                        if (quotes.isEmpty()) {
                            continue;
                        }
                        if (quotes.size() > 1) {
                            this.logger.debug("<initInstruments> >1 quotes underlying: " + instrument
                                    + ", quotes=" + quotes);
                            continue;
                        }
                        final Quote quote = quotes.get(0);
                        // an underlying of a type we do not normally process
                        addToFile(InstrumentTypeEnum.STK, quote);
                        tmpQidToIid.put(quote.getId(), instrument.getId());
                        tmpNonStandardUnderlyingIidToQid.put(instrument.getId(), quote.getId());
                    }
                    continue;
                }

                addQuotes(instrument);

                if (isUnderlying || instrument.getInstrumentType() == InstrumentTypeEnum.CUR) {
                    for (final Quote quote : instrument.getQuotes()) {
                        final String vendorkey = quote.getSymbol(KeysystemEnum.VWDFEED);
                        if (vendorkey == null) {
                            continue;
                        }

                        final String currency = quote.getCurrency().getSymbol(KeysystemEnum.ISO);
                        if (instrument.getInstrumentType() == InstrumentTypeEnum.CUR
                                && EUR_FX_PATTERN.matcher(vendorkey).matches()) {
                            tmpCurrencyToQuoteid.put(currency, quote.getId());
                        }

                        if ("EUR".equals(currency) || isUnderlying) {
                            if (!RatioInstrumentFilter.isValid(quote)) {
                                // if true == isValid(..), addQuotes already added the quote
                                addToFile(instrument.getInstrumentType(), quote);
                            }
                        }
                    }
                }

                if (isWithBenchmark(instrument)) {
                    sendBenchmarkName(instrument, encoder);
                }
            }

            this.lock.writeLock().lock();
            holdsLock = true;

            if (update) {
                this.nonStandardUnderlyingIidToQid.putAll(tmpNonStandardUnderlyingIidToQid);
                this.qidToIid.putAll(tmpQidToIid);
                this.currencyToQuoteid.putAll(tmpCurrencyToQuoteid);
                for (Map.Entry<Long, LongList> e : tmpIidToDerivativeQids.entrySet()) {
                    long iid = e.getKey();
                    this.iidToDerivativeQids.put(iid,
                            merge(e.getValue(), iidToDerivativeQids.get(iid)));
                }
            }
            else {
                this.nonStandardUnderlyingIidToQid = tmpNonStandardUnderlyingIidToQid;
                this.qidToIid = tmpQidToIid;
                this.iidToDerivativeQids = tmpIidToDerivativeQids;
                this.currencyToQuoteid = tmpCurrencyToQuoteid;
            }

            this.logger.info("<initInstruments> #qidToIid = " + qidToIid.size());
            this.chicagoFileCreator.createFiles(update);

        } catch (Exception e) {
            this.logger.error("<initInstruments> failed", e);
            this.chicagoFileCreator.cancel();
        } finally {
            if (holdsLock) {
                this.lock.writeLock().unlock();
            }
        }
    }

    private boolean isWithBenchmark(Instrument instrument) {
        return (instrument.getInstrumentType() == InstrumentTypeEnum.STK
                || instrument.getInstrumentType() == InstrumentTypeEnum.BND);
    }

    private LongList merge(LongList l1, LongList l2) {
        if (l1 == null) {
            return l2;
        }
        if (l2 == null) {
            return l1;
        }
        final LongOpenHashSet set = new LongOpenHashSet(l1);
        set.addAll(l2);
        return new LongArrayList(set);
    }

    private void updateQidToIid(Long2LongMap map, Instrument instrument) {
        for (final Quote quote : instrument.getQuotes()) {
            map.put(quote.getId(), instrument.getId());
        }
    }

    private List<Quote> getValidUnderlyingQuotes(Instrument instrument) {
        final ArrayList<Quote> result = new ArrayList<>();
        final String eurexTicker = instrument.getSymbol(KeysystemEnum.EUREXTICKER);
        for (Quote quote : instrument.getQuotes()) {
            if (!StringUtils.hasText(quote.getSymbolVwdfeed())) {
                continue;
            }
            if ("DTB".equals(quote.getSymbolVwdfeedMarket())) {
                if (eurexTicker != null && !eurexTicker.equals(quote.getSymbolVwdsymbol())) {
                    continue;
                }
            }
            result.add(quote);
        }
        return result;
    }

    private void addToFile(InstrumentTypeEnum t, Quote q) {
        this.chicagoFileCreator.add(t, q);
    }

    private void addQuotes(Instrument instrument) {
        for (final Quote quote : instrument.getQuotes()) {
            if (RatioInstrumentFilter.isValid(quote)) {
                addToFile(instrument.getInstrumentType(), quote);
            }
        }
    }

    private boolean isToBeIgnored(Instrument instrument) {
        if (isModForDevelopment(instrument)
                && (instrument.getId() % this.iidModForDevelopment) != 0) {
            return true;
        }
        if (InstrumentUtil.isOpraInstrument(instrument)) {
            return true;
        }
        return false;
    }

    private boolean isModForDevelopment(Instrument instrument) {
        return this.iidModForDevelopment > 1
                && this.modForDevelopmentTypes.contains(instrument.getInstrumentType());
    }


    private Iterator<Instrument> getInstrumentIterator(boolean update) {
        return update ? this.instrumentDao.getUpdates() : this.instrumentDao.iterator();
    }

    private void sendBenchmarkName(final Instrument instrument, final RatiosEncoder encoder) {
        encoder.reset(instrument.getInstrumentType(), instrument.getId(), Long.MIN_VALUE);
        final Quote benchmarkQuote = getBenchmarkQuote(instrument);
        encoder.add(RatioFieldDescription.benchmarkName,
                (benchmarkQuote != null) ? benchmarkQuote.getInstrument().getName() : null);
        this.frontend.update(encoder.getData());
    }

    private void addQidsToUnderlying(Long2ObjectMap<LongList> map, Derivative derivative) {
        LongList qids = map.get(derivative.getUnderlyingId());
        if (qids == null) {
            map.put(derivative.getUnderlyingId(), qids = new LongArrayList());
        }
        for (final Quote quote : derivative.getQuotes()) {
            qids.add(quote.getId());
        }
    }
}
