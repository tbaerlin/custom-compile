/*
 * CachingFeedConnector.java
 *
 * Created on 14.10.11 10:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsReq;
import dev.infrontfinance.dm.proto.DmChicago.SnapFieldsResp;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;

import de.marketmaker.istar.common.amqp.ServiceProviderSelection;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.api.PageRequest;
import de.marketmaker.istar.feed.api.PageResponse;
import de.marketmaker.istar.feed.api.SymbolSortRequest;
import de.marketmaker.istar.feed.api.SymbolSortResponse;
import de.marketmaker.istar.feed.api.TypedVendorkeysRequest;
import de.marketmaker.istar.feed.api.TypedVendorkeysResponse;
import de.marketmaker.istar.feed.api.VendorkeyListRequest;
import de.marketmaker.istar.feed.api.VendorkeyListResponse;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickRecordImpl;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.TICK3;

/**
 * A wrapper for a delegate FeedConnector that caches tick data for the current day. The cached
 * data will never be used to avoid calls of the delegate. Instead, if cached data is available,
 * the call will be modified to request ticks data with an offset. That offset is the amount
 * of tick bytes that have been received earlier.<p>
 * Since different servers may encode ticks differently (e.g., ticks chunks are released after
 * the contents have been written to disk which takes arbitrary times, so feed data objects for
 * the same symbol on different machines may be assigned tick chunks of different sizes which
 * results in different storage patterns, although the ticks themselves will be the same), it
 * is important to ensure that cached data is only updated with data received from the same
 * server.
 * </p>
 * @author oflege
 */
@ManagedResource
public class CachingFeedConnector implements FeedConnector, InitializingBean, DisposableBean {
    /**
     * result of requesting new ticks, can return either realtime or delayed tick data
     */
    private static class Response {
        private IntradayResponse rtResponse;

        private IntradayResponse ntResponse;

        Response(IntradayResponse r, String key) {
            if (r.isValid()) {
                rtResponse = new IntradayResponse();
                rtResponse.add(r.getItem(key));
            }
            else {
                rtResponse = r;
            }
        }

        IntradayResponse getResponse(boolean realtime) {
            return realtime ? this.rtResponse : getNtResponse();
        }

        private IntradayResponse getNtResponse() {
            if (this.ntResponse == null) {
                this.ntResponse = createNtResponse();
            }
            return this.ntResponse;
        }

        private IntradayResponse.Item toNtItem(IntradayResponse.Item item) {
            IntradayResponse.Item result = new IntradayResponse.Item(item.getVendorkey(), false);
            result.setPriceSnapRecord(item.getDelaySnapRecord());
            SnapRecord dsr = item.getRawDelaySnapRecord();
            TickRecordImpl tr = (TickRecordImpl) new TickRecordImpl().merge(item.getTickRecord());
            result.setTickRecord(tr);
            final int doa = SnapRecordUtils.getDateOfArrival(dsr);
            if (doa != 0) {
                // mdps-delayed data's TOA is the time when the delayed data is received,
                // so we cannot use getTimeOfArrival here:
                final int toa = SnapRecordUtils.getTime(dsr);
                tr.setLast(DateUtil.toDateTime(doa, toa), dsr);
            }
            tr.setLast(null, dsr);
            return result;
        }

        private IntradayResponse createNtResponse() {
            if (!rtResponse.isValid()) {
                return this.rtResponse;
            }
            IntradayResponse.Item item = this.rtResponse.iterator().next();
            if (item.getRawDelaySnapRecord() == null) {
                return this.rtResponse;
            }
            IntradayResponse result = new IntradayResponse();
            result.add(toNtItem(item));
            return result;
        }
    }


    /**
     * There is one Element for each vendorkey in the cache. Ecapsulates single threaded
     * (i.e., sequential) access of a
     * {@link de.marketmaker.istar.merger.provider.CachedTickData} instance.
     */
    private class Element {

        private final String key;

        private final int day;

        private CachedTickData data;

        private final AtomicInteger numRequests = new AtomicInteger();

        private final AtomicInteger numPiggyback = new AtomicInteger();

        // makes sure only a single thread is ever updating data
        private final AtomicReference<Future<Response>> f = new AtomicReference<>();

        private final Callable<Response> c = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                IntradayResponse ir = doCall();
                return new Response(ir, key);
            }
        };

        private final Callable<Response> d = () -> {
            disposeData();
            return null;
        };

        private Element(String key, int day) {
            this.key = key;
            this.day = day;
        }

        @Override
        public String toString() {
            return this.key + "/" + numRequests + "/" + numPiggyback + "/" + String.valueOf(this.data);
        }

        /**
         * runs in the thread that succeeded in setting <code>f</code>, requests the latest ticks
         * and updates the cache.
         */
        IntradayResponse doCall() {
            IntradayRequest request = new IntradayRequest();
            IntradayRequest.Item item = new IntradayRequest.Item(key);
            item.setRetrieveTicks(this.day);
            if (this.data != null) {
                final String serverId = this.data.getServerId();
                item.setTickStorageInfo(serverId, this.data.getStorageInfo());
                ServiceProviderSelection.ID_FOR_NEXT_SEND.set(serverId);
            }
            request.add(item);
            IntradayResponse response = doGetResponse(request);
            // for testing, as amqp does this anyway:
            ServiceProviderSelection.ID_FOR_NEXT_SEND.remove();

            if (!response.isValid()) {
                return response;
            }

            IntradayResponse.Item responseItem = response.getItem(item.getVendorkey());
            if (responseItem == null) {
                return response;
            }

            final TickRecord tr = responseItem.getTickRecord();
            if (!(tr instanceof AbstractTickRecord)) {
                return response;
            }
            final AbstractTickRecord tickRecord = (AbstractTickRecord) tr;

            final AbstractTickRecord.TickItem tickItem = tickRecord.getItem(item.getTicksFrom());
            if (tickItem != null && tickItem.getEncoding() != TICK3) {
                return response;
            }

            updateCache(response, responseItem);
            return response;
        }

        private void updateCache(IntradayResponse response, IntradayResponse.Item item) {
            final AbstractTickRecord tr = (AbstractTickRecord) item.getTickRecord();
            AbstractTickRecord.TickItem tickItem = tr.getItem(day);

            final byte[] ticks = (tickItem != null) ? tickItem.getData() : null;
            final String serverId = response.getServerId();

            if (this.data != null) {
                if (this.data.isResultFromSameServer(response)) {
                    if (this.data.isCacheComplete(item)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("same-no-new " + key + " " + serverId);
                        }
                        tr.add(day, this.data.getCachedTickBytes(), TICK3);
                    }
                    else {
                        final int numBytesCachedOld = data.getNumBytesCached();
                        byte[] bytes = this.data.extendWith(ticks, item.getTickStorageInfo());
                        if (logger.isDebugEnabled()) {
                            logger.debug("same-extended " + key + " " + this.data);
                        }
                        numBytesCached.addAndGet(this.data.getNumBytesCached() - numBytesCachedOld);
                        tr.add(day, bytes, TICK3);
                    }
                }
                else if (ticks != null) { // complete result from other server
                    disposeData();

                    this.data = CachedTickData.create(serverId, ticks, item.getTickStorageInfo());
                    numBytesCached.addAndGet(this.data.getNumBytesCached());
                    if (logger.isDebugEnabled()) {
                        logger.debug("other-complete " + key + " " + serverId + " " + this.data);
                    }
                }
            }
            else if (ticks != null) {
                this.data = CachedTickData.create(serverId, ticks, item.getTickStorageInfo());
                numBytesCached.addAndGet(this.data.getNumBytesCached());
                if (logger.isDebugEnabled()) {
                    logger.debug("new-complete " + this.key + " " + serverId + " " + this.data);
                }
            }
        }

        private Future<Response> getF(boolean dispose) {
            for (; ; ) {
                Future<Response> existing = this.f.get();
                if (existing != null) {
                    this.numPiggyback.incrementAndGet();
                    return existing;
                }
                FutureTask<Response> r = new FutureTask<>(dispose ? d : c);
                if (this.f.compareAndSet(null, r)) {
                    this.numRequests.incrementAndGet();
                    try {
                        r.run();
                        return r;
                    } finally {
                        if (!dispose) {
                            disposeFuture();
                        }
                    }
                }
            }
        }

        private void disposeFuture() {
            final int ms = cacheDataForMillis;
            if (ms <= 0) {
                this.f.set(null);
                return;
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    f.set(null);
                }
            }, ms);
        }

        private void disposeData() {
            if (this.data != null) {
                numBytesCached.addAndGet(-this.data.getNumBytesCached());
                this.data.freeMemory();
                this.data = null;
            }
        }

        private IntradayResponse getResponse(boolean realtime) {
            try {
                final Response response = getF(false).get();
                return (response != null) ? response.getResponse(realtime) : null;
            } catch (InterruptedException | ExecutionException e) {
                disposeData();
                return null;
            }
        }
    }


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AtomicStampedReference<ConcurrentMap<String, Element>> elementsRef
            = new AtomicStampedReference<>(null, 0);

    private final Timer timer = new Timer(CachingFeedConnector.class.getSimpleName() + "-expire", true);

    /**
     * file with vwdcodes for which data should be cached; if null, data for all requested keys
     * will be cached; in that case, it is strongly recommended to set {@link #maxNumBytesCached}
     * to avoid memory exhaustion
     */
    private File vendorkeyFile;

    private int maxNumElements = Integer.MAX_VALUE;

    private FeedConnector delegate;

    private volatile int cacheDataForMillis = 0;

    private final AtomicLong numBytesCached = new AtomicLong();

    private long maxNumBytesCached = 0;

    private final AtomicInteger numHits = new AtomicInteger();

    private final AtomicInteger numMisses = new AtomicInteger();

    public void setVendorkeyFile(File vendorkeyFile) {
        this.vendorkeyFile = vendorkeyFile;
    }

    public void setMaxNumElements(int maxNumElements) {
        this.maxNumElements = maxNumElements;
    }

    /**
     * @param maxNumBytesCached maximum number of bytes to be cached, a value &lt; 0 disables the
     * cache entirely, a value of 0 allows unlimited caching
     */
    public void setMaxNumBytesCached(long maxNumBytesCached) {
        this.maxNumBytesCached = maxNumBytesCached;
    }

    private IntradayResponse doGetResponse(IntradayRequest request) {
        return this.delegate.getIntradayData(request);
    }

    public int getNumHits() {
        return numHits.get();
    }

    public int getNumMisses() {
        return numMisses.get();
    }

    public double getHitRatio() {
        int num = getNumHits() + getNumMisses();
        return num > 0 ? (numHits.get() / (double) num) : 1d;
    }

    @ManagedAttribute
    public int getCacheDataForMillis() {
        return cacheDataForMillis;
    }

    @ManagedAttribute
    public void setCacheDataForMillis(int cacheDataForMillis) {
        this.cacheDataForMillis = cacheDataForMillis;
    }

    @ManagedAttribute
    public long getNumBytesCached() {
        return this.numBytesCached.get();
    }

    @ManagedAttribute
    public boolean isCacheEnabled() {
        final Map<String, Element> map = this.elementsRef.getReference();
        return map != null && !map.isEmpty();
    }

    @ManagedOperation
    public void disableCache() {
        updateElements(null, 0);
    }

    @Scheduled(cron = "0 30 0 * * *") // reload daily at 00:30:00
    @ManagedOperation
    public void reloadElements() throws IOException {
        if (this.maxNumBytesCached < 0) {
            return;
        }
        final int day = DateUtil.dateToYyyyMmDd();
        if (this.vendorkeyFile == null) {
            this.logger.info("<reloadElements> remove all");
            updateElements(new ConcurrentHashMap<>(), day);
            return;
        }
        if (!this.vendorkeyFile.canRead()) {
            this.logger.warn("<reloadElements> cannot read input file " + this.vendorkeyFile.getAbsolutePath());
            updateElements(null, 0);
            return;
        }
        final ConcurrentMap<String, Element> tmp = new ConcurrentHashMap<>();
        List<String> keys = Files.readAllLines(this.vendorkeyFile.toPath(), StandardCharsets.UTF_8);
        for (String key : keys.subList(0, Math.min(keys.size(), this.maxNumElements))) {
            tmp.put(key, new Element(key, day));
        }
        updateElements(tmp, day);
        this.logger.info("<reloadElements> succeeded for " + tmp.size() + " elements");
    }

    private void updateElements(ConcurrentMap<String, Element> m, int day) {
        ConcurrentMap<String, Element> old = this.elementsRef.getReference();
        this.elementsRef.set(m, day);
        if (old != null) {
            dispose(old.values());
        }
    }

    private void dispose(final Collection<Element> values) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                doDispose(values);
            }
        }, 1000);
    }

    private void doDispose(final Collection<Element> values) {
        if (this.numBytesCached.get() == 0L) {
            return;
        }
        this.logger.info("<doDispose> before: " + this.numBytesCached);
        for (Element value : values) {
            doDispose(value);
        }
        this.logger.info("<doDispose> after: " + this.numBytesCached);
    }

    private void doDispose(Element element) {
        this.logger.info("<doDispose> " + element);
        try {
            Future<Response> r = element.getF(true);
            while (r.get() != null) {
                TimeUnit.SECONDS.sleep(1);
                r = element.getF(true);
            }
        } catch (Exception e) {
            this.logger.error("<doDispose> failed for " + element, e);
        }
    }

    public void setDelegate(FeedConnector delegate) {
        this.delegate = delegate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.maxNumBytesCached < 0) {
            this.logger.info("<afterPropertiesSet> cache is disabled");
            return;
        }
        if (this.delegate == null) {
            throw new IllegalStateException("delegate is null");
        }
        reloadElements();
    }

    @Scheduled(cron = "30 * * * * *")
    public void logStats() {
        Map<String, Element> m = this.elementsRef.getReference();
        if (m != null) {
            this.logger.info(String.format(Locale.US, "<logStats> #h=%d, #m=%d, h/m=%4.2f, mem=%d"
                    , this.numHits.get(), this.numMisses.get(), getHitRatio(), getNumBytesCached()));
        }
    }

    @Override
    public void destroy() throws Exception {
        this.timer.cancel();
        final Map<String, Element> map = this.elementsRef.getReference();
        if (map != null) {
            doDispose(map.values());
        }
        this.elementsRef.set(null, 0);
    }

    private IntradayRequest.Item getTickRequestItem(IntradayRequest request) {
        final List<IntradayRequest.Item> items = request.getItems();
        if (items.size() != 1) {
            return null;
        }
        final IntradayRequest.Item item = items.get(0);
        if (!item.isWithTicks()) {
            return null;
        }
        return (item.getTicksFrom() == DateUtil.dateToYyyyMmDd()) ? item : null;
    }

    @Override
    public IntradayResponse getIntradayData(IntradayRequest request) {
        if (this.maxNumBytesCached < 0 || ServiceProviderSelection.ID_FOR_NEXT_SEND.get() != null) {
            return this.delegate.getIntradayData(request);
        }

        final IntradayRequest.Item item = getTickRequestItem(request);
        if (item == null) {
            return this.delegate.getIntradayData(request);
        }

        final String key = item.getVendorkey();

        final int[] stampHolder = new int[1];
        final Map<String, Element> map = this.elementsRef.get(stampHolder);
        if (stampHolder[0] != item.getTicksFrom() || map == null) {
            return this.delegate.getIntradayData(request);
        }

        Element e = map.get(key);
        if (e == null) {
            this.numMisses.incrementAndGet();
            if (this.vendorkeyFile != null || isCacheFull()) {
                return this.delegate.getIntradayData(request);
            }

            final Element existing = map.putIfAbsent(key, e = new Element(key, stampHolder[0]));
            if (existing != null) {
                e = existing;
            }
        }

        IntradayResponse response = e.getResponse(item.isRealtime());
        if (response != null) {
            this.numHits.incrementAndGet();
            if (isCacheFull()) {
                final Element removed = map.remove(key);
                if (removed != null) {
                    dispose(Collections.singleton(removed));
                }
            }
            return response;
        }

        this.numMisses.incrementAndGet();
        return this.delegate.getIntradayData(request);
    }

    private boolean isCacheFull() {
        return this.maxNumBytesCached > 0 && this.numBytesCached.get() > this.maxNumBytesCached;
    }

    /*
    ------------ FeedConnector methods that are not subject to caching -------------------
     */

    @Override
    public SymbolSortResponse getSortedSymbols(SymbolSortRequest request) {
        return this.delegate.getSortedSymbols(request);
    }

    @Override
    public TypedVendorkeysResponse getTypesForVwdcodes(TypedVendorkeysRequest request) {
        return this.delegate.getTypesForVwdcodes(request);
    }

    @Override
    public PageResponse getPage(PageRequest request) {
        return this.delegate.getPage(request);
    }

    @Override
    public VendorkeyListResponse getVendorkeys(VendorkeyListRequest request) {
        return this.delegate.getVendorkeys(request);
    }

    @Override
    public SnapFieldsResp getSnapFields(SnapFieldsReq req) {
        return this.delegate.getSnapFields(req);
    }
}
