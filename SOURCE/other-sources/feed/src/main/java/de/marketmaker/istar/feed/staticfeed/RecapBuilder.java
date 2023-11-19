/*
 * RecapBuilder.java
 *
 * Created on 19.04.13 14:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.staticfeed.DiffFormatter.EXPIRED;
import static de.marketmaker.istar.feed.staticfeed.DiffFormatter.RECAP;
import static de.marketmaker.istar.feed.staticfeed.DiffFormatter.UPDATE;

/**
 * @author oflege
 */
@ManagedResource
public class RecapBuilder extends StaticBuilder implements DisposableBean {
    private FeedDataRepository repository;

    private int maxNumRecapsPerSecond = 10000;

    private int numDaysWithoutUpdateTriggersGc = 30;

    private ExecutorService es = Executors.newSingleThreadExecutor(
            r -> new Thread(r, RecapBuilder.class.getSimpleName())
    );

    public void setNumDaysWithoutUpdateTriggersGc(int numDaysWithoutUpdateTriggersGc) {
        this.numDaysWithoutUpdateTriggersGc = numDaysWithoutUpdateTriggersGc;
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    public void setMaxNumRecapsPerSecond(int maxNumRecapsPerSecond) {
        this.maxNumRecapsPerSecond = maxNumRecapsPerSecond;
    }

    @Override
    public void destroy() throws Exception {
        es.shutdown();
    }

    @ManagedOperation
    public void startGC() throws Exception {
        startGC(this.numDaysWithoutUpdateTriggersGc);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "numDays",
                    description = "update in last numDays or gc")
    })
    public void startGC(int numDays) throws Exception {
        DateTime dt = new DateTime().minusDays(numDays);
        this.es.submit(() -> gc(repository.getMarkets(), dt));
    }

    @ManagedOperation
    public void startWriteRecaps() {
        this.es.submit(() -> writeRecaps(repository.getMarkets(), null));
    }

    @ManagedOperation
    public String startWriteRecapsForMarket(String market) {
        final FeedMarket m = repository.getMarket(new ByteString(market));
        if (m == null) {
            return "unknown market: '" + market + "'";
        }
        this.es.submit(() -> writeRecaps(Collections.singletonList(m), null));
        return "submitted task to write recaps for market " + market;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "symbols", description = "comma separated feed symbols without type")
    })
    public String startWriteRecapsForSymbols(String symbolsStr) {
        String[] symbols = symbolsStr.split(",");
        final Set<String> unknownSymbols = new HashSet<>();
        final List<FeedData> elements = new ArrayList<>(symbols.length);
        for (String symbol : symbols) {
            FeedData data = repository.get(new ByteString(symbol.trim()));
            if (data == null || data.isDeleted()) {
                unknownSymbols.add(symbol);
                continue;
            }
            elements.add(data);
        }
        this.es.submit(() -> writeRecaps(elements, createLimiter(), null));
        if (!unknownSymbols.isEmpty()) {
            return "submitted task to write recaps for " + elements.size()
                    + " symbols; unknown symbols: " + unknownSymbols;
        }
        return "submitted task to write recaps for " + elements.size() + " symbols";
    }

    /**
     * Write updates for all symbols that contain at least one of certain fields; each update record
     * is restricted to the specified fields. Useful if fields had not been enabled in the mdp but
     * have now become enabled, so that the current value of those fields is considered "new".
     * @param fieldsStr field names/ids separated by comma
     */
    @ManagedOperation(description = "writes updates for all symbols containing at least one of the given fields")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "fields", description = "comma separated list of field names or ids")
    })
    public void startWriteUpdates(String fieldsStr) {
        final int[] orders = parseFields(fieldsStr);
        this.es.submit(() -> writeRecaps(repository.getMarkets(), orders));
    }

    private int[] parseFields(String fieldsStr) {
        final String[] fields = fieldsStr.split(",");
        final int[] orders = new int[fields.length];
        for (int i = 0; i < fields.length; i++) {
            final VwdFieldDescription.Field f = getField(fields[i].trim());
            if (f == null) {
                throw new IllegalArgumentException(fields[i]);
            }
            orders[i] = VwdFieldOrder.getOrder(f.id());
        }
        Arrays.sort(orders);
        return orders;
    }

    private VwdFieldDescription.Field getField(String field) {
        if (field.matches("\\d+")) {
            return VwdFieldDescription.getField(Integer.valueOf(field));
        }
        return VwdFieldDescription.getFieldByName(field.startsWith("ADF_") ? field : ("ADF_" + field));
    }


    private void gc(final List<FeedMarket> markets, DateTime dt) {
        try {
            doGc(markets, dt);
        } catch (Throwable t) {
            this.logger.error("<gc> failed", t);
        }
    }

    private void doGc(final List<FeedMarket> markets, final DateTime dt) {
        int totalNum = 0;
        int updateThreshold = DateTimeProvider.Timestamp.encodeTimestamp(dt.getMillis());
        final TimeTaker tt = new TimeTaker();
        final ThroughputLimiter limiter = createLimiter();
        for (FeedMarket market : markets) {
            final int num = doGc(market.getElements(false), limiter, updateThreshold);
            totalNum += num;
            if (num > 0) {
                this.logger.info("<doGc> " + num + " for " + market.getName());
            }
        }
        if (totalNum > 0) {
            this.repository.gc();
        }
        this.logger.info("<doGc> w/o update after " + dt
                + ", removed " + totalNum + ", took " + tt);
    }

    private int doGc(List<FeedData> elements, ThroughputLimiter limiter, int updateThreshold) {
        int n = 0;
        for (FeedData data : elements) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (data) {
                final OrderedSnapData sd = (OrderedSnapData) data.getSnapData(true);
                if (sd.getLastUpdateTimestamp() > updateThreshold) {
                    continue;
                }
                try {
                    data.setState(FeedData.STATE_GARBAGE);
                    writeRecap((StaticFeedData) data, null);
                } catch (Exception e) {
                    this.logger.error("<doGc> failed for " + data.getVwdcode(), e);
                }
            }

            limiter.ackAction();
            n++;
        }
        return n;
    }

    private void writeRecaps(final List<FeedMarket> markets, int[] orders) {
        try {
            doWriteRecaps(markets, orders);
        } catch (Throwable t) {
            this.logger.error("<writeRecaps> failed, orders=" + Arrays.toString(orders), t);
        }
    }

    private void doWriteRecaps(final List<FeedMarket> markets, int[] orders) {
        int totalNum = 0;
        final TimeTaker tt = new TimeTaker();
        final ThroughputLimiter limiter = createLimiter();
        for (FeedMarket market : markets) {
            final int num = writeRecaps(market, limiter, orders);
            totalNum += num;
            this.logger.info("<doWriteRecaps> " + num + " for " + market.getName());
        }
        this.logger.info("<doWriteRecaps> " + totalNum + ", took " + tt);
    }


    private ThroughputLimiter createLimiter() {
        return new ThroughputLimiter(this.maxNumRecapsPerSecond);
    }

    private int writeRecaps(FeedMarket market, ThroughputLimiter limiter, int[] orders) {
        final List<FeedData> elements = market.getElements(false);
        return writeRecaps(elements, limiter, orders);
    }

    private int writeRecaps(List<FeedData> elements, ThroughputLimiter limiter, int[] orders) {
        int n = 0;
        for (FeedData data : elements) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (data) {
                if (data.isDeleted()) {
                    continue;
                }
                final SnapData sd = data.getSnapData(true);

                try {
                    writeRecap((StaticFeedData) data, orders);
                } catch (Exception e) {
                    this.logger.error("<writeRecaps> failed for " + data.getVwdcode(), e);
                    sd.init(null, null);
                }
            }

            limiter.ackAction();
            n++;
        }
        return n;
    }

    private void writeRecap(StaticFeedData data, int[] orders) {
        SnapData sd = data.getSnapData(true);
        if (orders == null) {
            this.formatter.init(data.isGarbage() ? EXPIRED : RECAP, data.getVwdcode(), data.getVendorkeyType(), -1);
            if (sd.isInitialized()) {
                appendAll(new BufferFieldData(sd.getData(false)));
            }
            this.formatter.finish(true);
        }
        else {
            if (!sd.isInitialized()) {
                return;
            }
            this.formatter.init(UPDATE, data.getVwdcode(), data.getVendorkeyType(), -1);
            if (sd.isInitialized()) {
                appendSome(new BufferFieldData(sd.getData(false)), orders);
            }
            this.formatter.finish(false);
        }
    }
}
