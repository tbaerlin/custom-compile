/*
 * TypeData.java
 *
 * Created on 15.11.2005 15:38:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.PermissionProvider;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;

/**
 * Store the data of one instrument type.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TypeData implements Consumer<RatioData> {

    private static final int NULL_INDEX = -1;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * position (i.e., index) of each RatioData in <code>ratiosDatas</code>, keyed by <code>iid</code>
     * <br>use int instead of long to save memory, current iids use only 5% of the available int range.
     */
    @GuardedBy("lock")
    private final Int2IntMap indexById = new Int2IntOpenHashMap();

    @GuardedBy("lock")
    private final List<RatioData> ratioDatas = new ArrayList<>(10000);

    private final ReentrantLock lock = new ReentrantLock();

    private final InstrumentTypeEnum type;

    /**
     * for every instrument update, this map will store qids that are newly assigned. This enables
     * us to detect quotes that are reassigned to a different instrument.
     */
    private Int2IntMap newQidToIid = null;

    /**
     * items will be added when no write lock could be acquired, so use thread-safe queue.
     */
    private final Queue<byte[]> pendingUpdates = new LinkedBlockingQueue<>();

    private static final int MAX_NUM_PENDING_UPDATES = Integer.getInteger("typeData.maxNumPendingUpdates", 77777);

    private static final AtomicInteger NUM_PENDING_UPDATES = new AtomicInteger();

    static final AtomicInteger NUM_MISSED_UPDATES = new AtomicInteger();

    private int iidModForDevelopment;

    public TypeData(InstrumentTypeEnum type, int iidModForDevelopment) {
        this.type = type;
        this.iidModForDevelopment = iidModForDevelopment;
        this.indexById.defaultReturnValue(NULL_INDEX);
    }

    public TypeData(InstrumentTypeEnum type) {
        this(type, 0);
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    <V> V withLock(Callable<V> c) throws Exception {
        final long then = System.nanoTime();
        this.lock.lock();
        final long lockWaitTime = TimeUnit.MILLISECONDS.convert(System.nanoTime() - then, TimeUnit.NANOSECONDS);

        try {
            return c.call();
        } finally {
            this.lock.unlock();
            if (lockWaitTime > 1000) {
                this.logger.info("<search> waited " + lockWaitTime
                        + "ms for read lock in " + getType().name());
            }
        }
    }

    /**
     * Update the static data of an instrument.
     * @param instrument contains static data
     * @param underlying information about instrument's underlying or null if not available
     */
    void update(Instrument instrument, RatioDataRepository.Underlying underlying) {
        int[] newQids = null;
        this.lock.lock();
        try {
            final RatioData ratioData = getOrCreateFor(instrument);
            if (ratioData != null) {
                newQids = ratioData.update(instrument, underlying);
            }
        } finally {
            this.lock.unlock();
        }
        if (this.newQidToIid != null && newQids != null) {
            for (int updatedQid : newQids) {
                this.newQidToIid.put(updatedQid, (int) instrument.getId());
            }
        }
    }

    private RatioData getOrCreateFor(Instrument instrument) {
        assert this.lock.getHoldCount() > 0;

        final int index = this.indexById.get((int) instrument.getId());
        if (index >= 0) {
            return this.ratioDatas.get(index);
        }
        final RatioData result = createFor(instrument.getId());
        if (result != null) {
            doAdd(result);
        }
        return result;
    }

    private RatioData createFor(final long iid) {
        if (isToBeIgnored(iid)) {
            return null;
        }
        return RatioData.create(this.type, iid);
    }

    void beforeSyncWithInstruments(boolean update) {
        if (update) {
            this.newQidToIid = new Int2IntOpenHashMap();
            return;
        }
        this.lock.lock();
        try {
            markAllAsGarbage();
        } finally {
            this.lock.unlock();
        }
    }

    private void markAllAsGarbage() {
        this.ratioDatas.parallelStream().forEach(RatioData::markAsGarbage);
    }

    void afterSyncWithInstruments(boolean update) {
        if (update) {
            final boolean withGarbage = removeUpdatedQids();
            this.newQidToIid = null;
            if (!withGarbage) {
                return;
            }
        }
        this.lock.lock();
        try {
            removeGarbage();
        } finally {
            this.lock.unlock();
        }
    }

    private boolean removeUpdatedQids() {
        if (this.newQidToIid.isEmpty()) {
            this.logger.info("<removeUpdatedQids> " + this.type + " no new qids");
            return false;
        }
        TimeTaker tt = new TimeTaker();
        int numGarbage = 0;
        this.lock.lock();
        try {
            for (RatioData rd : this.ratioDatas) {
                if (rd.removeUpdatedQids(this.newQidToIid)) {
                    rd.markAsGarbage();
                    numGarbage++;
                }
            }
        } finally {
            this.lock.unlock();
        }
        this.logger.info("<removeUpdatedQids> " + this.type + " with "
                + this.newQidToIid.size() + " new qids took " + tt);
        return numGarbage > 0;
    }

    private void removeGarbage() {
        final TimeTaker tt = new TimeTaker();

        int numRemoved = 0;
        int p = 0;
        for (int i = 0; i < this.ratioDatas.size(); i++) {
            final RatioData rd = this.ratioDatas.get(i);
            int id = (int) rd.getInstrumentRatios().getId();
            if (rd.isGarbage()) {
                this.indexById.remove(id);
                numRemoved++;
            }
            else {
                rd.setIndex(p);
                this.ratioDatas.set(p, rd);
                this.indexById.put(id, p++);
            }
        }

        if (numRemoved > 0) {
            this.ratioDatas.subList(p, this.ratioDatas.size()).clear();
        }

        this.logger.info("<removeGarbage> " + this.type + ": #size=" + p
                + ", #removed=" + numRemoved + ", took=" + tt);
    }

    public List<RatioData> getRatioDatas() {
        assert this.lock.getHoldCount() > 0;
        return Collections.unmodifiableList(this.ratioDatas);
    }

    public List<RatioData> getRatioDatasCopy() {
        assert this.lock.getHoldCount() > 0;
        return new ArrayList<>(this.ratioDatas);
    }

    int size() {
        this.lock.lock();
        try {
            return this.ratioDatas.size();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void accept(RatioData ratioData) {
        add(ratioData);
    }

    boolean add(RatioData rd) {
        if (isToBeIgnored(rd)) {
            return false;
        }

        this.lock.lock();
        try {
            doAdd(rd);
            return true;
        } finally {
            this.lock.unlock();
        }
    }

    private void doAdd(RatioData rd) {
        assert this.lock.getHoldCount() > 0;

        final int n = this.ratioDatas.size();
        rd.setIndex(n);
        this.indexById.put((int) rd.getInstrumentRatios().getId(), n);
        this.ratioDatas.add(rd);
    }

    private boolean isToBeIgnored(RatioData rd) {
        return isToBeIgnored(rd.getInstrumentRatios().getId());
    }

    private boolean isToBeIgnored(long iid) {
        return this.iidModForDevelopment != 0 && (iid % this.iidModForDevelopment) != 0;
    }

    void updateDynamicData(ByteBuffer bb) {
        if (bb.remaining() < 16) { // need at leas iid and qid
            this.logger.warn("<updateDynamicData> invalid data");
            return;
        }

        if (!this.lock.tryLock()) {
            if (NUM_PENDING_UPDATES.incrementAndGet() > MAX_NUM_PENDING_UPDATES) {
                NUM_PENDING_UPDATES.decrementAndGet();
                NUM_MISSED_UPDATES.incrementAndGet();
                return;
            }
            final byte[] tmp = new byte[bb.remaining()];
            bb.get(tmp);
            this.pendingUpdates.add(tmp);
            return;
        }

        try {
            doPendingUpdates();
            doUpdate(bb);
        } finally {
            this.lock.unlock();
        }
    }

    void flushPendingUpdates() {
        this.lock.lock();
        try {
            doPendingUpdates();
        } finally {
            this.lock.unlock();
        }
    }

    private void doPendingUpdates() {
        byte[] data;
        while ((data = this.pendingUpdates.poll()) != null) {
            NUM_PENDING_UPDATES.decrementAndGet();
            doUpdate(ByteBuffer.wrap(data));
        }
    }

    private void doUpdate(ByteBuffer bb) {
        final long instrumentid = bb.getLong();
        final long quoteid = bb.getLong();

        RatioData rd = getRatioData(instrumentid);
        if (rd == null) {
            return;
        }

        try {
            if (quoteid <= 0) {
                rd.update(bb);
            }
            else {
                rd.update(quoteid, bb);
            }
        } catch (Exception e) {
            this.logger.warn("<doUpdate> failed for " + instrumentid + ".iid and " + quoteid + ".qid", e);
        }
    }

    String toDebugString(long iid) {
        this.lock.lock();
        try {
            final RatioData rd = getRatioData(iid);
            return (rd != null) ? RatioDataUtil.toDebugString(this, rd) : null;
        } finally {
            this.lock.unlock();
        }
    }


    void withRatioData(long instrumentid, Consumer<RatioData> c) {
        this.lock.lock();
        try {
            c.accept(getRatioData(instrumentid));
        } finally {
            this.lock.unlock();
        }
    }

    RatioData getRatioData(long instrumentid) {
        assert this.lock.getHoldCount() > 0;

        final int index = this.indexById.get((int) instrumentid);
        return index >= 0 ? this.ratioDatas.get(index) : null;
    }

    /**
     * @return ratios for a single instrument
     */
    RatioSearchResponse getForId(final SearchParameterParser spp) {
        this.lock.lock();
        try {
            final RatioData ratioData = getRatioData(spp.getId());

            final DefaultRatioSearchResponse result = new DefaultRatioSearchResponse();
            final List<RatioDataResult> l = getResult(ratioData, spp);
            result.setNumTotal(l.size());
            result.setOffset(0);
            result.setLength(l.size());
            result.setElements(l);
            return result;
        } finally {
            this.lock.unlock();
        }
    }

    private List<RatioDataResult> getResult(RatioData ratioData, SearchParameterParser spp) {
        return (ratioData != null && ratioData.select(spp))
                ? Collections.singletonList(ratioData.createResult())
                : Collections.<RatioDataResult>emptyList();
    }

    /**
     * Searches this object according to <code>spp</code> and returns a visitor that can be used
     * to obtain the search result. <p><b>Important</b></p> This method must only be called from
     * a Callable passed to {@link #withLock(java.util.concurrent.Callable)}, as only that method
     * will ensure adequate locking. Furthermore, the visitor's
     * {@link de.marketmaker.istar.ratios.frontend.SearchEngineVisitor#getResponse()} method needs to be
     * called from within that Callable as well (i.e., while still holding the lock).
     * @param spp defines search.
     * @param pool used for parallel searching
     * @return visitor
     */
    SearchEngineVisitor search(final SearchParameterParser spp, ForkJoinPool pool) {
        return new TypeDataSearcher(this, spp, pool).search();
    }

    public RatioSearchResponse searchAndVisit(final SearchParameterParser spp, ForkJoinPool pool) {
        final long then = System.currentTimeMillis();
        this.lock.lock();
        final long lockWaitTime = System.currentTimeMillis() - then;

        try {
            return new TypeDataSearcher(this, spp, pool).search().getResponse();
        } finally {
            this.lock.unlock();
            if (lockWaitTime > 1000) {
                this.logger.info("<search> waited " + lockWaitTime
                        + "ms for read lock in " + getType().name());
            }
        }
    }

    public RatioSearchMetaResponse getMetaData(RatioSearchMetaRequest req) {
        final long then = System.currentTimeMillis();
        this.lock.lock();
        final long lockWaitTime = System.currentTimeMillis() - then;

        try {
            return new TypeDataMetaSearchMethod(this, req).invoke();
        } catch (Exception e) {
            this.logger.warn("<getMetaData> failed", e);
            final RatioSearchMetaResponse result = new RatioSearchMetaResponse();
            result.setInvalid();
            return result;
        } finally {
            this.lock.unlock();
            final long time = System.currentTimeMillis() - then;
            if (time > 1000) {
                this.logger.info("<getMetaData> took " + time + "ms, " + lockWaitTime
                        + "ms for read lock in " + getType().name());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final FileRatioDataStore store = new FileRatioDataStore();
        store.setBaseDir(LocalConfigProvider.getProductionDir("var/data/ratios"));

        InstrumentDirDao dao = new InstrumentDirDao(LocalConfigProvider.getProductionDir("/var/data/instrument/work1/data/instruments"));


        RatioDataRepository rdr = new RatioDataRepository();
        rdr.setStore(store);
        rdr.setTypes(new String[]{"OPT"});
        rdr.initialize();
        rdr.setSimpleInstrumentBackends(false, dao, null, null);

        final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf("OPT");

        final PermissionProvider pp = ResourcePermissionProvider.getInstance("mm-xml");
        final Profile p = ProfileFactory.createInstance(pp);

        final RatioSearchRequest request = new RatioSearchRequest(p);
        request.setType(type);
        request.addParameter("bisKey", "1_965239_");
//        request.addParameter("i", "0");
        request.addParameter("n", "5000");
//        request.addParameter("msMarketAdmission", "CH");
//        request.addParameter("performance1m:L", "0");
//        request.addParameter("performance1m:U", "140");
//        request.addParameter("issueVolume:L", "140");
//        request.addParameter("sort1", "performance3m");
//        request.addParameter("sort1:D", "true");
//        request.setDataRecordStrategyClass(PreferIssuerQuoteStrategy.class);

        for (int i = 0; i < 1; i++) {
            final SearchParameterParser spp = new SearchParameterParser(request, null);
            final long then = System.nanoTime();

            final DefaultRatioSearchResponse response = (DefaultRatioSearchResponse) rdr.search(spp);
            final long now = System.nanoTime();
            for (final RatioDataResult ratioDataResult : response.getElements()) {
//                System.out.println(ratioDataResult.getInstrumentRatios().getString(583));
//                System.out.println(ratioDataResult.getQuoteData().getSymbolVwdfeedMarket());
                System.out.println(ratioDataResult.getQuoteData().getString(155));
            }
            System.out.println(response.getInstrumentIds() + ": took " + (now - then));
        }
    }
}
