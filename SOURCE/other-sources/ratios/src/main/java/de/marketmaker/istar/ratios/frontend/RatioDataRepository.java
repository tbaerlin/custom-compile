/*
 * RatioDataRepository.java
 *
 * Created on 26.10.2005 09:28:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.lifecycle.Disposable;
import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.monitor.Resource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.InstrumentServerUpdateable;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.export.InstrumentDao;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.instrument.search.InstrumentSearcher;
import de.marketmaker.istar.instrument.search.SuggestionSearcher;
import de.marketmaker.istar.ratios.Partition;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioInstrumentFilter;
import de.marketmaker.istar.ratios.RatioUpdateable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class RatioDataRepository implements InstrumentServerUpdateable, Initializable,
        Lifecycle, Disposable, RatioUpdateable {

    // all the data we need to store for several thousand underlyings, use far less memory
    // than if we were storing Instrument instances directly.
    static class Underlying {
        final String name;

        final String wkn;

        final String isin;

        final String eurexTicker;

        private Underlying(String name, String wkn, String isin, String eurexTicker) {
            this.name = name;
            this.wkn = wkn;
            this.isin = isin;
            this.eurexTicker = eurexTicker;
        }
    }

    private static final int DEFAULT_SYNC_SLEEP_INTERVAL = 10000;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<InstrumentTypeEnum> types = EnumSet.noneOf(InstrumentTypeEnum.class);

    private final Map<InstrumentTypeEnum, TypeData> dataByType = new EnumMap<>(InstrumentTypeEnum.class);

    private RatioDataStore store;

    private boolean storeOnDispose = true;

    private int sleepAfter = Integer.MAX_VALUE;

    private File restrictedIsinsFile;

    private int iidModForDevelopment;

    private final Set<InstrumentTypeEnum> modForDevelopmentTypes = EnumSet.allOf(InstrumentTypeEnum.class);

    private AtomicReference<Set<String>> restrictedIsins = new AtomicReference<>();

    private final ActiveMonitor monitor = new ActiveMonitor();

    private int syncSleepInterval = DEFAULT_SYNC_SLEEP_INTERVAL;

    private final Object multiTypeDataSearchMutex = new Object();

    /**
     * supports parallel search
     */
    private ForkJoinPool pool;

    public void setPool(ForkJoinPool pool) {
        this.pool = pool;
    }

    /**
     * When sync'ing on a new instrument repository, sleep one second after this many
     * updates. Default is {@value #DEFAULT_SYNC_SLEEP_INTERVAL}. Useful to reduce the
     * load as the sync process keeps the cpu busy for a minute or so.
     *
     * @param syncSleepInterval interval between updates used to sleep (yield cpu)
     */
    public void setSyncSleepInterval(int syncSleepInterval) {
        this.syncSleepInterval = syncSleepInterval;
    }

    public void setRestrictedIsinsFile(File restrictedIsinsFile) {
        this.restrictedIsinsFile = restrictedIsinsFile;
    }

    public void setStoreOnDispose(boolean storeOnDispose) {
        this.storeOnDispose = storeOnDispose;
    }

    @ManagedAttribute
    public int getNumMissedUpdates() {
        return TypeData.NUM_MISSED_UPDATES.get();
    }

    public void setModForDevelopmentTypes(String typeNames) {
        if (StringUtils.hasText(typeNames)) {
            this.modForDevelopmentTypes.clear();
            addAll(typeNames.split(","), this.modForDevelopmentTypes);
        }
    }

    public void setTypes(String[] typeNames) {
        addAll(typeNames, this.types);
        this.logger.info("<setTypes> " + this.types);
    }

    private void addAll(String[] typeNames, final Set<InstrumentTypeEnum> types) {
        for (final String typeStr : typeNames) {
            types.add(InstrumentTypeEnum.valueOf(typeStr.trim()));
        }
    }

    public void setStore(RatioDataStore store) {
        this.store = store;
    }

    public void initialize() throws Exception {
        if (this.pool == null) {
            this.pool = new ForkJoinPool();
            this.logger.info("<initialize> #created pool, #search threads = " + this.pool.getParallelism());
        }
        readIsins();
        restore();
        this.sleepAfter = this.syncSleepInterval;
    }

    @Override
    public String toString() {
        return "RatioDataRepository[" + this.types + ", #missedUpdates=" + getNumMissedUpdates() + "]";
    }

    public void setIidModForDevelopment(int iidModForDevelopment) {
        this.iidModForDevelopment = iidModForDevelopment;
    }

    @Override
    public boolean isRunning() {
        return this.monitor.isRunning();
    }

    @Override
    public void start() {
        if (this.restrictedIsinsFile == null) {
            return;
        }

        this.monitor.setFrequency(30 * 1000);

        final FileResource resource = new FileResource(this.restrictedIsinsFile);
        resource.addPropertyChangeListener(evt -> readIsins());

        this.monitor.setResources(new Resource[]{resource});
        this.monitor.start();
    }

    public void stop() {
        if (this.restrictedIsinsFile == null) {
            return;
        }

        this.monitor.stop();
    }

    public void dispose() throws Exception {
        if (this.storeOnDispose) {
            store();
        }
    }

    private void readIsins() {
        if (this.restrictedIsinsFile == null) {
            return;
        }

        try {
            final Set<String> isins = new HashSet<>();

            final Scanner s = new Scanner(this.restrictedIsinsFile);

            while (s.hasNextLine()) {
                final String isin = s.nextLine();
                if (IsinUtil.isIsin(isin)) {
                    isins.add(isin);
                }
                else {
                    this.logger.info("<readIsins> invalid isin: " + isin);
                }
            }

            s.close();

            this.restrictedIsins.set(isins);

            this.logger.info("<readIsins> read new restricted isins file, #isins: " + isins.size());
        } catch (Exception e) {
            this.logger.error("<readIsins> failed", e);
        }
    }

    @ManagedOperation(description = "stores ratio data for a particular type")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "typeName", description = "instrument type (e.g., CER)")
    })
    public String storeType(String typeName) {
        if (this.store == null) {
            return "no store available";
        }
        final InstrumentTypeEnum type;
        try {
            type = InstrumentTypeEnum.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            return "Invalid type: " + typeName;
        }
        final TypeData typeData = this.dataByType.get(type);
        if (typeData == null) {
            return "Data for type " + typeName + " not available";
        }
        if (store(typeData)) {
            return "stored " + typeName;
        }
        else {
            return "store failed, see log for details";
        }
    }

    @ManagedOperation(description = "stores ratio data for all types")
    public String storeTypes() {
        final StringBuilder sb = new StringBuilder();
        for (final InstrumentTypeEnum type : this.types) {
            sb.append(", ").append(storeType(type.name()));
        }
        return sb.toString().substring(2);
    }

    private void store() throws IOException {
        if (this.store == null) {
            return;
        }
        for (final Map.Entry<InstrumentTypeEnum, TypeData> entry : this.dataByType.entrySet()) {
            store(entry.getValue());
        }
    }

    private boolean store(TypeData typeData) {
        try {
            this.store.store(typeData);
            return true;
        } catch (Throwable t) {
            this.logger.error("<store> failed for " + typeData, t);
            return false;
        }
    }

    @ManagedOperation(description = "get ratio data for instrument id")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "iid", description = "instrument id")
    })
    public String getRatioData(long iid) {
        for (TypeData td : dataByType.values()) {
            final String result = td.toDebugString(iid);
            if (result != null) {
                return result;
            }
        }
        return "not available";
    }


    private void restore() throws Exception {
        for (InstrumentTypeEnum type : this.types) {
            final TypeData typeData = restore(type);
            if (typeData != null) {
                this.dataByType.put(type, typeData);
            }
            else {
                this.logger.info("<restore> creating new TypeData for " + type.name());
                this.dataByType.put(type, new TypeData(type));
            }
        }
    }

    private TypeData restore(InstrumentTypeEnum type) throws Exception {
        if (this.store == null) {
            return null;
        }
        final int mod = this.modForDevelopmentTypes.contains(type) ? this.iidModForDevelopment : 0;
        final TypeData data = new TypeData(type, mod);
        this.store.restore(type, data);
        return data;
    }

    /**
     * Switch to use a newly available index.
     * This method is called by the Controller, when a new index is available.
     */
    @Override
    public void setInstrumentBackends(boolean update, InstrumentDao instrumentDao,
                                      InstrumentSearcher instrumentSearcher, SuggestionSearcher suggestionSearcher) {
        final Set<Long> underlyingIds = instrumentSearcher.getUnderlyingIds();
        if (underlyingIds == null) {
            this.logger.error("<setInstrumentBackends> no underlyingIds found?!");
            return;
        }

        syncDataWithInstruments(update, underlyingIds, instrumentDao);
    }

    /**
     * This method is called by TypeData.main() - simple way to locally simulate ratio searched
     */
    public void setSimpleInstrumentBackends(boolean update, InstrumentDao instrumentDao,
                                      InstrumentSearcher instrumentSearcher, SuggestionSearcher suggestionSearcher) {
//        final Set<Long> underlyingIds = instrumentSearcher.getUnderlyingIds();
//        if (underlyingIds == null) {
//            this.logger.error("<setInstrumentBackends> no underlyingIds found?!");
//            return;
//        }
        this.sleepAfter = 10000000; // full power since we are on a local machine
        syncDataWithInstruments(update, Collections.emptySet(), instrumentDao);
    }

    private void syncDataWithInstruments(boolean update, Set<Long> underlyingIds, InstrumentDao dao) {
        final Map<Long, Underlying> underlyings = readUnderlyings(underlyingIds, dao);

        this.logger.info("<syncDataWithInstruments> started, sleep 1s after every " + sleepAfter + " updates");
        final TimeTaker tt = new TimeTaker();

        for (TypeData data : this.dataByType.values()) {
            data.beforeSyncWithInstruments(update);
        }

        int numUpdated = 0;

        final Iterator<Instrument> it = update ? dao.getUpdates() : dao.iterator();

        while (it.hasNext()) {
            Instrument instrument = it.next();

            if (isToBeIgnored(instrument)) {
                continue;
            }

            final TypeData data = getType(instrument.getInstrumentType());

            data.update(instrument, getUnderlying(instrument, underlyings));

            if (++numUpdated % this.sleepAfter == 0) {
                this.logger.info("<syncDataWithInstruments> numUpdated " + numUpdated);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    this.logger.warn("<syncDataWithInstruments> interrupted?!");
                }
            }
        }

        for (TypeData data : this.dataByType.values()) {
            data.afterSyncWithInstruments(update);
        }

        this.logger.info("<syncDataWithInstruments> updated " + numUpdated + ", took " + tt);
    }

    private boolean isToBeIgnored(Instrument instrument) {
        if (this.iidModForDevelopment > 1
                && this.modForDevelopmentTypes.contains(instrument.getInstrumentType())
                && (instrument.getId() % this.iidModForDevelopment) != 0) {
            return true;
        }
        if (!this.types.contains(instrument.getInstrumentType())
                || !RatioInstrumentFilter.isValid(instrument)) {
            return true;
        }
        if (InstrumentUtil.isOpraInstrument(instrument)) {
            return true;
        }
        // ISTAR-758 (T-44758) (git: 9443c352b48314ff85dfbf38b0d1a9a394d966fb)
        // Explicitely exclude ITRAXX indexes until required.
        // Before removing this code implement permission check regading data visibility (all, 6 months, nothing)
        // to backend service(s).
        if (instrument.getQuotes().stream().anyMatch(q -> "ITRAXX".equalsIgnoreCase(q.getMarket().getName()))) {
            return true;
        }
        final Set<String> isins = this.restrictedIsins.get();
        if (isins != null && instrument.getSymbolIsin() != null
                && !isins.contains(instrument.getSymbolIsin())) {
            return true;
        }
        return false;
    }

    private Map<Long, Underlying> readUnderlyings(final Set<Long> iids, InstrumentDao dao) {
        final Map<Long, Underlying> result = new HashMap<>();
        for (Long underlyingId : iids) {
            final Instrument underlying = dao.getInstrument(underlyingId);
            if (underlying != null) {
                result.put(underlyingId, new Underlying(underlying.getName(),
                        underlying.getSymbol(KeysystemEnum.WKN),
                        underlying.getSymbol(KeysystemEnum.ISIN),
                        underlying.getSymbol(KeysystemEnum.EUREXTICKER)));
            }
        }
        this.logger.info("<readUnderlyings> read #" + result.size());
        return result;
    }

    private Underlying getUnderlying(Instrument instrument, Map<Long, Underlying> underlyings) {
        return (instrument instanceof Derivative)
                ? underlyings.get(((Derivative) instrument).getUnderlyingId())
                : null;
    }

    public RatioSearchResponse search(SearchParameterParser spp) {
        final TypeData td = getType(spp.getType());
        if (td == null) {
            return null;
        }

        try {
            return doSearch(spp, td);
        } catch (Exception e) {
            this.logger.error("<search> failed for " + spp, e);
            return createEmptyResponse(false);
        }
    }

    /**
     * Performs a search in td and possibly additional types. In the "easy" case (no
     * additional types), we can just invoke one of td's search methods, which will
     * use a lock internally to guard the search and the evaluation of the visitor object.
     * The complicated case (with additional types) is handled by {@link
     * #doSearch(SearchParameterParser, TypeData, Iterator)}.
     *
     * @param spp search parameters
     * @param td primary search type
     * @return search result
     */
    private RatioSearchResponse doSearch(final SearchParameterParser spp, final TypeData td) throws Exception {
        if (spp.getId() > 0) {
            return td.getForId(spp);
        }

        final Iterator<TypeData> others = getAdditionalTypes(spp);

        if (!others.hasNext()) {
            return td.searchAndVisit(spp, this.pool);
        }

        // the called method is going to acquire locks on td and all TypeDatas in others; we
        // use synchronized to prevent deadlocks that could occur if another thread were to lock
        // those TypeDatas in a different order
        synchronized (this.multiTypeDataSearchMutex) {
            return doSearch(spp, td, others);
        }
    }

    /**
     * Performs a search in <code>td</code> and <code>others</code>. The important aspect here
     * is that we need to hold a lock on all TypeDatas that we searched until the visitor's
     * {@link de.marketmaker.istar.ratios.frontend.SearchEngineVisitor#getResponse()} method
     * has been evaluated.
     *
     * @param spp search specification
     * @param td primary search type
     * @param others additional types in which to search
     * @return search result
     * @throws Exception when search fails
     */
    private RatioSearchResponse doSearch(final SearchParameterParser spp, final TypeData td,
                                         final Iterator<TypeData> others) throws Exception {
        return td.withLock(new Callable<RatioSearchResponse>() {
            private SearchEngineVisitor v;

            private TypeData current;

            @Override
            public RatioSearchResponse call() throws Exception {
                if (this.v == null) {
                    this.v = td.search(spp, pool);
                    if (!(v instanceof MergeableSearchEngineVisitor)) {
                        throw new IllegalStateException("Cannot use additional types with visitor class "
                                + v.getClass());
                    }
                }
                else {
                    final SearchEngineVisitor v2 = current.search(spp, pool);
                    //noinspection unchecked
                    ((MergeableSearchEngineVisitor) v).merge((MergeableSearchEngineVisitor) v2);
                }

                if (others.hasNext()) {
                    this.current = others.next();
                    // indirect recursion while holding locks on all TypeDatas encountered so far
                    return this.current.withLock(this);
                }
                else {
                    return this.v.getResponse();
                }
            }
        });
    }

    private Iterator<TypeData> getAdditionalTypes(SearchParameterParser spp) {
        if (spp.getAdditionalTypes() == null) {
            return Collections.emptyIterator();
        }
        final List<TypeData> others = new ArrayList<>();
        for (InstrumentTypeEnum t : spp.getAdditionalTypes()) {
            final TypeData other = getType(t);
            if (other != null) {
                others.add(other);
            }
        }
        return others.iterator();
    }

    private static RatioSearchResponse createEmptyResponse(boolean valid) {
        final DefaultRatioSearchResponse result = new DefaultRatioSearchResponse();
        result.setElements(Collections.emptyList());
        result.setNumTotal(0);
        if (!valid) {
            result.setInvalid();
        }
        return result;
    }

    public RatioSearchMetaResponse getMetaData(RatioSearchMetaRequest req) {
        final TypeData data = getType(req.getType());
        if (data == null) {
            return null;
        }

        return data.getMetaData(req);
    }

    public void update(byte[] bytes) {
        update(ByteBuffer.wrap(bytes));
    }

    /**
     * Update ratio data of one instrument or quote.
     */
    @Override
    public void update(ByteBuffer bb) {
        try {
            final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(bb.getInt());
            final TypeData data = getType(type);
            if (data != null) {
                data.updateDynamicData(bb);
            }
        } catch (Exception e) {
            this.logger.warn("<update> failed", e);
        }
    }

    TypeData getType(InstrumentTypeEnum type) {
        return this.dataByType.get(type);
    }

    /**
     * To be invoked by external scheduler; may take some seconds as it has to acquire a write
     * lock for all maintained TypeData elements. Helps to make sure that no updates are pending for
     * significantly longer than the interval used to invoke this method.
     */
    public void flushPendingUpdates() {
        this.dataByType.values().forEach(TypeData::flushPendingUpdates);
    }

    public static void main(String[] args) throws Exception {
        final String query, offset, count, sort, sortOrder;
        if (args.length < 5) {
            System.err.println("Provider 5 arguments: isin, start, offset, sort, sort_order");
            query = "country==Deutschland";
            offset = "0";
            count = "1";
            sort = "name";
            sortOrder = "false";
        } else {
            query = args[0];
            offset = args[1];
            count = args[2];
            sort = args[3];
            sortOrder = args[4];
        }
        final FileRatioDataStore store = new FileRatioDataStore();
        store.setBaseDir(LocalConfigProvider.getProductionDir("/var/data/ratios/"));

        RatioDataRepository r = new RatioDataRepository();
        r.setTypes(new String[]{"BND"});
        r.setStore(store);
        r.initialize();
        r.sleepAfter = Integer.MAX_VALUE;
        r.setIidModForDevelopment(32);


        InstrumentDirDao dao = new InstrumentDirDao(LocalConfigProvider
                .getProductionDir("/var/data/instrument/work0/data/instruments"));
        r.syncDataWithInstruments(false, Collections.emptySet(), dao);

        final RatioSearchRequest request = new RatioSearchRequest(ProfileFactory.valueOf(true));

        {
            final InstrumentTypeEnum type = InstrumentTypeEnum.BND;
            final InstrumentTypeEnum additionalType = null;

            request.setInstrumentIds(Arrays.asList(170332349L, 119733398L));
            request.setType(type);
            request.setAdditionalTypes(new InstrumentTypeEnum[]{additionalType});
            request.setVisitorClass(PagedResultVisitor.class);
            request.addParameter("i", offset);
            request.addParameter("n", count);
            request.addParameter("sort1", sort);
            request.addParameter("sort1:D", sortOrder);
            request.setMetadataFieldids(Collections.singletonList(RatioFieldDescription.wmNotActive.id()));
        }

        final DefaultRatioSearchResponse searchResponse =
                (DefaultRatioSearchResponse) r.search(new SearchParameterParser(request, null));

        System.out.println(searchResponse.getElements().size());
        List<Partition> p = searchResponse.getPartition();
        if (p == null) {
            System.out.println("Field indexes is null");
        } else {
            System.out.println(Arrays.deepToString(searchResponse.getPartition().toArray()));
        }
    }
}
