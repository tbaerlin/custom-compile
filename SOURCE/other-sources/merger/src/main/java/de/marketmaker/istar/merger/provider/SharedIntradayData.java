/*
 * SharedIntradayData.java
 *
 * Created on 15.09.2009 10:00:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.TickRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.PriceRecordFactory;

import static de.marketmaker.istar.domain.data.PriceQuality.END_OF_DAY;
import static de.marketmaker.istar.domain.data.PriceQuality.REALTIME;

/**
 * Intraday data for a certain quote that can be shared among different threads.
 * An instance of this class exsists for each symbol for which intraday data is requested
 * in a given {@link de.marketmaker.istar.merger.context.RequestContext}.
 * <p>
 * <b>Important:</b> This class is quite complicated, but for a reason. Do not modify it unless
 * you know exactly what you are doing.
 * @author Oliver Flege
 */
class SharedIntradayData {

    /**
     * We may get several tick requests with overlapping days. In order to be able to merge
     * them and to avoid requesting data for a day more than once, we have to use several
     * objects of this class to store what was already requested to adapt new requests accordingly.
     */
    private static class TickFuture {
        private final CompletableFuture<TickRecord> result;

        private final LocalDate from;

        private final LocalDate to;

        private TickFuture(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
            this.result = new CompletableFuture<>();
        }

        @Override
        public String toString() {
            return "TickFuture{" + from + ".." + to + ", " + this.result + '}';
        }

        /**
         * Someone needs ticks between aFrom and aTo. If that interval starts/ends within this
         * object's interval, the boundaries can be adapted to avoid requesting the ticks again.
         * @param aFrom a from value
         * @param aTo a to value
         * @return adapted from (in [0]) and to (in [1])
         */
        private LocalDate[] adapt(LocalDate aFrom, LocalDate aTo) {
            final LocalDate[] result = new LocalDate[]{aFrom, aTo};
            if (isBetweenFromAndTo(aFrom)) {
                result[0] = this.to.plusDays(1);
            }
            if (isBetweenFromAndTo(aTo)) {
                result[1] = this.from.minusDays(1);
            }
            return result;
        }

        private boolean isBetweenFromAndTo(LocalDate ld) {
            return !ld.isBefore(this.from) && !ld.isAfter(this.to);
        }

        public void set(TickRecord tickRecord) {
            this.result.complete(tickRecord);
        }

        public TickRecord get() throws InterruptedException, ExecutionException {
            return result.get();
        }

        public boolean cancel() {
            return result.cancel(false);
        }

        public void setException(Throwable t) {
            result.completeExceptionally(t);
        }
    }

    /**
     * Wraps a IntradayRequest.Item and additional information needed to decide which
     * FutureResult objects have to be set when the results are available, and which
     * FutureResult objects have to be queried to create an appropriate IntradayData.
     */
    class RequestInfo {
        private final IntradayRequest.Item item;

        private boolean withSnap;

        /**
         * If we need ticks requested by others, these futures will have to be queried for
         * their results to make sure ticks for all requested days.
         */
        private List<TickFuture> tickFutures;

        /**
         * If we request ticks, this will be set and used to store the result.
         */
        private TickFuture tickFuture;

        private RequestInfo(IntradayRequest.Item item) {
            this.item = item;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append("RequestInfo{");
            if (this.withSnap) {
                sb.append(this.item).append(", ");
            }
            if (this.tickFuture != null) {
                sb.append("tickFuture=").append(tickFuture).append(", ");
            }
            if (this.tickFutures != null) {
                sb.append("tickFutures=").append(tickFutures);
            }
            return sb.append('}').toString();
        }

        void setWithSnap() {
            this.withSnap = true;
        }

        private void needsResultFrom(TickFuture tf) {
            if (this.tickFutures == null) {
                this.tickFutures = new LinkedList<>();
            }
            this.tickFutures.add(tf);
        }

        private TickFuture setRetrieveTicks(LocalDate from, LocalDate to) {
            context.checkTickSize(); // throws Exception if we already exceeded the limit
            this.item.setRetrieveTicks(DateUtil.toYyyyMmDd(from), DateUtil.toYyyyMmDd(to));
            return this.tickFuture = new TickFuture(from, to);
        }

        IntradayRequest.Item getItem() {
            if (!this.withSnap && !this.item.isWithTicks()) {
                return null;
            }
            return this.item;
        }

        /**
         * Update relevant futures with results
         * @param responseItem contains results
         */
        void update(IntradayResponse.Item responseItem) {
            if (this.withSnap) {
                setPrice(responseItem.getVendorkey(), responseItem.getPriceSnapRecord());
            }
            if (this.item.isWithTicks()) {
                TickRecord tr = responseItem.getTickRecord();
                if (tr != null) {
                    context.incTickSize(tr.tickSize());
                }
                this.tickFuture.set(tr);
            }
        }

        void cancel() {
            if (this.withSnap) {
                snapFuture.cancel(false);
            }
            if (this.tickFuture != null) {
                this.tickFuture.cancel();
            }
        }

        void setException(Throwable t) {
            if (this.withSnap) {
                snapFuture.completeExceptionally(t);
            }
            if (this.tickFuture != null) {
                this.tickFuture.setException(t);
            }
        }

        public IntradayData getIntradayData() {
            try {
                final SnapRecord srPrice = snapFuture.get();

                final PriceRecord pr = PriceRecordFactory.create(q, srPrice, priceQuality, pushAllowed);
                return new IntradayData(q.getId(), pr, srPrice, getTickRecord());
            } catch (MergerException e) {
                throw e;
            } catch (Exception e) {
                return null;
            }
        }

        private TickRecord getTickRecord() throws ExecutionException, InterruptedException {
            TickRecord result = null;
            if (this.tickFutures != null) {
                for (TickFuture future : this.tickFutures) {
                    result = merge(result, future.get());
                }
            }
            if (this.tickFuture != null) {
                result = merge(result, this.tickFuture.get());
            }
            return result;
        }

        private TickRecord merge(TickRecord result, TickRecord source) {
            if (result == null) {
                return source;
            }
            return result.merge(source);
        }
    }

    private CompletableFuture<SnapRecord> snapFuture;

    private final List<TickFuture> tickFutures = new LinkedList<>();

    private final Quote q;

    private final PriceQuality priceQuality;

    private final boolean pushAllowed;

    private final boolean eodTicksAllowed;

    private final ProfiledSnapRecordFactory factory;

    private final SharedIntradayContext context;

    SharedIntradayData(Quote q, PriceQuality priceQuality, Profile profile,
            ProfiledSnapRecordFactory factory, SharedIntradayContext context) {
        this(q, priceQuality, profile.getPushPriceQuality(q, null) != PriceQuality.NONE,
                (priceQuality == PriceQuality.END_OF_DAY) &&
                        profile.isAllowed(Selector.DATA_MANAGER_TREASURY),
                factory, context);
    }

    SharedIntradayData(Quote q, PriceQuality priceQuality, boolean pushAllowed, boolean eodTicksAllowed,
            ProfiledSnapRecordFactory factory, SharedIntradayContext context) {
        this.q = q;
        this.priceQuality = priceQuality;
        this.pushAllowed = pushAllowed;
        this.eodTicksAllowed = eodTicksAllowed;
        this.factory = factory;
        this.context = context;
    }

    private void setPrice(String vendorkey, SnapRecord sr) {
        this.snapFuture.complete(this.factory.applyProfileTo(vendorkey, sr));
    }

    @Override
    public String toString() {
        return "SharedIntradayData{" + this.q +
                ", priceQuality=" + priceQuality +
                ", pushAllowed=" + pushAllowed +
                ", eodTicksAllowed=" + eodTicksAllowed +
                ", snapFuture=" + snapFuture +
                '}';
    }

    /**
     * Add a request for this quote. This method has to be synchronized, so that the decision
     * whether the current thread will request snap, static, or ticks will be unambiguous (i.e.,
     * no other thread requests the data as well).
     *
     * @param from if != null, first day for which to retrieve ticks
     * @param to if != null, last day for which to retrieve ticks, must be != null iff from != null
     * @return RequestInfo object
     */
    synchronized RequestInfo addRequest(LocalDate from, LocalDate to) {
        final IntradayRequest.Item item
                = new IntradayRequest.Item(q.getSymbolVwdfeed(), isNotDelay());
        final RequestInfo result = new RequestInfo(item);

        if (this.snapFuture == null) {
            this.snapFuture = new CompletableFuture<>();
            result.setWithSnap();
        }

        if (from != null && (this.priceQuality != PriceQuality.END_OF_DAY || this.eodTicksAllowed)) {
            LocalDate requestFrom = from;
            LocalDate requestTo = to;
            for (TickFuture tickFuture : this.tickFutures) {
                final LocalDate[] adapted = tickFuture.adapt(requestFrom, requestTo);
                if (adapted[0] != requestFrom || adapted[1] != requestTo) {
                    requestFrom = adapted[0];
                    requestTo = adapted[1];
                    result.needsResultFrom(tickFuture);
                    if (requestTo.isBefore(requestFrom)) {
                        return result; // nothing to request
                    }
                }
                else if (requestFrom.isBefore(tickFuture.from) && requestTo.isAfter(tickFuture.to)) {
                    // we probably request more than the thread that created tickFuture, but
                    // we still need to incorporate its results to be sure all threads
                    // collect the same data
                    result.needsResultFrom(tickFuture);
                }
                else {
                    break;
                }
            }
            this.tickFutures.add(result.setRetrieveTicks(requestFrom, requestTo));
        }

        return result;
    }

    private boolean isNotDelay() {
        return this.priceQuality == REALTIME || this.priceQuality == END_OF_DAY;
    }
}
