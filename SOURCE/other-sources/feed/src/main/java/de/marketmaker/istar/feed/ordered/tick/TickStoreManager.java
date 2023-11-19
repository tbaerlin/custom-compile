/*
 * TickStoreManager.java
 *
 * Created on 20.11.12 10:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.IOException;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.support.CronSequenceGenerator;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.Mementos;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.DateTimeProviderImpl;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;

import static de.marketmaker.istar.common.util.NumberUtil.humanReadableByteCount;
import static org.joda.time.DateTimeConstants.SECONDS_PER_HOUR;

/**
 * Manages recurring tasks that need to be executed and makes sure all files are written with
 * an index and closed on shutdown.
 * @author oflege
 */
@ManagedResource
public class TickStoreManager implements DisposableBean, Lifecycle {

    private static final int IDLE_TIMEOUT = SECONDS_PER_HOUR / 2; // 30min

    private volatile boolean running;

    protected final class RecurringJob implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private final Runnable delegate;

        private final CronSequenceGenerator cron;

        protected RecurringJob(String ce, Runnable delegate) {
            this.cron = new CronSequenceGenerator(ce);
            this.delegate = delegate;
        }

        @Override
        public void run() {
            doRun(true);
        }

        private void doRun(boolean resubmit) {
            try {
                this.delegate.run();
            } catch (Throwable t) {
                this.logger.error("<run> failed", t);
            } finally {
                if (resubmit) {
                    submit();
                }
            }
        }

        protected ScheduledFuture<?> submit() {
            final ScheduledThreadPoolExecutor es
                    = (this.delegate == logStatsRunnable) ? logExecutor : executor;
            return es.schedule(this, getDelay(), TimeUnit.MILLISECONDS);
        }

        private long getDelay() {
            final Date now = new Date();
            return this.cron.next(now).getTime() - now.getTime();
        }
    }

    private final Runnable logStatsRunnable = new Runnable() {
        private final Mementos.Long lastNumFailedAdds = new Mementos.Long();

        private final Mementos.Int lastNumReads = new Mementos.Int();

        private final Mementos.Int lastNumWrites = new Mementos.Int();

        private final Mementos.Long lastNumBytesIn = new Mementos.Long();

        private final Mementos.Long lastNumBytesOut = new Mementos.Long();

        @Override
        public void run() {
            logStats(lastNumFailedAdds.diffAndSet(memoryTickStore.getNumFailedAdds()),
                    lastNumReads.diffAndSet(fileTickStore.getNumReads()),
                    lastNumWrites.diffAndSet(writer.getNumWrites()),
                    humanReadableByteCount(lastNumBytesIn.diffAndSet(writer.getNumBytesIn())),
                    humanReadableByteCount(lastNumBytesOut.diffAndSet(writer.getNumBytesOut()))
            );
        }
    };

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private DateTimeProvider dateTimeProvider = DateTimeProviderImpl.INSTANCE;

    protected FeedDataRepository repository;

    private FileTickStore fileTickStore;

    MemoryTickStore memoryTickStore;

    private TickWriter writer;

    protected volatile boolean stopped = false;

    private boolean needToCloseYesterday;

    private long evictIdleFlushPauseMs = 0;

    private final Map<FeedMarket, Integer> idleMarkets = new IdentityHashMap<>();

    private final ScheduledThreadPoolExecutor executor
            = new ScheduledThreadPoolExecutor(1, r -> {
                return new Thread(r, "TSM-E");
            });

    // some tasks take longer than the default log interval; to ensure that the status is logged
    // every 10s, use a dedicated ScheduledThreadPoolExecutor
    private final ScheduledThreadPoolExecutor logExecutor
            = new ScheduledThreadPoolExecutor(1, r -> {
                return new Thread(r, "TSM-L");
            });

    public TickStoreManager() {
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    public void setDateTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    public void setMemoryTickStore(MemoryTickStore memoryTickStore) {
        this.memoryTickStore = memoryTickStore;
    }

    public void setFileTickStore(FileTickStore fileTickStore) {
        this.fileTickStore = fileTickStore;
    }

    public void setWriter(TickWriter writer) {
        this.writer = writer;
    }

    public void setEvictIdleFlushPauseMs(long evictIdleFlushPauseMs) {
        this.evictIdleFlushPauseMs = evictIdleFlushPauseMs;
    }

    private void midnight() {
        this.needToCloseYesterday = true;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void start() {
        submit("0 0 0 * * ?", this::midnight);
        submit("5/10 * * * * ?", logStatsRunnable);
        submitExtraTasks();
        this.running = true;
    }

    protected void submitExtraTasks() {
        submit("0 15 0 * * ?", this::closeYesterday);
        submit("0 45 0 * * ?", this::releaseYesterday);
        submit("22 3/10 1-23 * * ?", this::evictIdle);
        submit("42 * * * * ?", this::flushIdleTasks);
    }

    protected void flushIdleTasks() {
        writer.flushIdleTasks();
    }

    protected void submit(String cron, Runnable r) {
        new RecurringJob(cron, r).submit();
    }

    @Override
    public void stop() {
        this.stopped = true;
        shutdown(this.executor);
        shutdown(this.logExecutor);
    }

    private void shutdown(final ScheduledThreadPoolExecutor ex) {
        ex.shutdown();
        try {
            if (!ex.awaitTermination(60, TimeUnit.SECONDS)) {
                this.logger.error("<shutdown> executor did not terminate within 60s");
                ex.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.logger.error("<shutdown> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (this.needToCloseYesterday) {
            closeYesterday();
        }
        closeToday();
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vwdcode", description = "vwdcode")
    })
    public String explainTickStorage(String vwdcode) {
        FeedData fd = this.repository.get(new ByteString(vwdcode));
        if (fd == null) {
            return "no data for " + vwdcode;
        }
        return explain(fd, this.memoryTickStore, this.fileTickStore);
    }

    static String explain(FeedData fd, MemoryTickStore ms, FileTickStore fs) {
        StringBuilder sb = new StringBuilder(128);
        int day;
        long addr;
        synchronized (fd) {
            OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
            day = td.getDate();
            sb.append(td);
            addr = ms.explain(td.getStoreAddress(), sb);
        }
        if (addr != 0 && addr != MemoryTickStore.NOT_AN_ADDRESS) {
            fs.explain(fd, day, addr, sb);
        }
        return sb.toString();
    }

    private void logStats(long numFailedAdds, int numReadsDiff, int numWritesDiff, String numInDiff,
            String numOutDiff) {
        if (numFailedAdds == 0L) {
            this.logger.info(String.format("M b=%3d%% c=%3d%% %s |F r=%3d w=%3d i=%s o=%s q=%d/%d",
                    this.memoryTickStore.getNumBytesFreePct(), this.memoryTickStore.getNumChunksFreePct(),
                    this.memoryTickStore.status(), numReadsDiff, numWritesDiff, numInDiff, numOutDiff,
                    this.writer.getCurrentWorkQueueLength(), this.writer.getMaxWorkQueueLength()));
        }
        else {
            this.logger.error(String.format("M X=%d b=%3d%% c=%3d%% %s |F r=%3d w=%3d i=%s o=%s q=%d/%d",
                    numFailedAdds,
                    this.memoryTickStore.getNumBytesFreePct(), this.memoryTickStore.getNumChunksFreePct(),
                    this.memoryTickStore.status(), numReadsDiff, numWritesDiff, numInDiff, numOutDiff,
                    this.writer.getCurrentWorkQueueLength(), this.writer.getMaxWorkQueueLength()));
        }
    }

    private int getToday() {
        return this.dateTimeProvider.dayAsYyyyMmDd();
    }

    private void evictIdle() {
        final int day = getToday();
        final int ts = this.dateTimeProvider.current().feedTimestamp;

        for (FeedMarket market : repository.getMarkets()) {
            evictIdle(market, day, ts);
            if (this.stopped) {
                return;
            }
        }
    }

    private void evictIdle(FeedMarket market, int day, int ts) {
        final int mts = market.getTickFeedTimestamp();
        if (mts == 0) {
            return;
        }
        final Integer flushMts = this.idleMarkets.get(market);
        if (flushMts != null && flushMts == mts) {
            return; // already flushed
        }
        if (DateTimeProvider.Timestamp.decodeDate(mts) == day && (ts - mts > IDLE_TIMEOUT)) {
            this.idleMarkets.put(market, mts);
            this.logger.info("<evictIdle> " + market.getName() + "-" + day + ", idle since "
                    + TimeFormatter.formatSecondsInDay(DateTimeProvider.Timestamp.decodeTime(mts)));
            evictIdle(market, day);
        }
        else if (flushMts != null) {
            this.idleMarkets.remove(market);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<evictIdle> no longer idle: " + market.getName());
            }
        }
    }

    void evictIdle(FeedMarket market, int day) {
        final TimeTaker tt = new TimeTaker();
        int num = doEvictIdle(market, day, 0);
        if (num != 0) {
            this.logger.info("<evictIdle> " + market.getName() + ", #=" + num + ", took " + tt);
        }
    }

    protected int doEvictIdle(FeedMarket market, int day, int timestamp) {
        final MarketDay md = MarketDay.create(market.getName(), day);

        int num = 0;
        final List<FeedData> elements = market.getElements(false);
        for (FeedData fd : elements) {
            OrderedFeedData ofd = (OrderedFeedData) fd;
            final OrderedTickData td = ofd.getOrderedTickData();
            final Future<?> f;
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (fd) {
                if (!td.canBeEvicted(day)) {
                    continue;
                }
                if (timestamp != 0 && ofd.getSnapData(true).getLastUpdateTimestamp() > timestamp) {
                    continue;
                }
                num++;
                f = this.writer.addIdle(fd, td, td.getStoreAddress(), md);
                td.setEvictionPending();
            }
            // elements.size() may well exceed 1m (euwax, ffmst); waiting for the current task
            // to finish ensures that we don't flood writer's workQueue
            if (f != null && !waitFor(f, num)) {
                return -num;
            }
        }

        final Future<?> f = this.writer.flushTask(md);
        if (f != null) {
            waitFor(f, num);
        }
        return num;
    }

    private boolean waitFor(Future<?> f, int num) {
        try {
            f.get();
            TimeUnit.MILLISECONDS.sleep(this.evictIdleFlushPauseMs);
            return true;
        } catch (InterruptedException e1) {
            this.logger.warn("<evictIdle> interrupted?!");
            Thread.currentThread().interrupt();
        } catch (ExecutionException e1) {
            this.logger.error("<evictIdle> failed after " + num, e1);
        }
        return false;
    }

    protected void releaseYesterday() {
        for (FeedMarket market : repository.getMarkets()) {
            releaseYesterday(market);
        }
    }

    private void releaseYesterday(FeedMarket market) {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<releaseYesterday> " + market.getName());
        }
        market.applyToElements(fd -> ((OrderedFeedData) fd).getOrderedTickData().releaseYesterday());
    }

    private void closeToday() {
        closeMarkets(getToday());
    }

    private void closeMarkets(int day) {
        for (FeedMarket market : repository.getMarkets()) {
            close(market, day);
        }
    }

    protected void closeYesterday() {
        this.needToCloseYesterday = false;

        final int today = getToday();
        final int yesterday = DateUtil.getDate(-1);
        final int idleBits = this.writer.getIdleBits();

        for (FeedMarket market : repository.getMarkets()) {
            market.applyToElements(fd -> {
                OrderedTickData otd = ((OrderedFeedData) fd).getOrderedTickData();
                otd.setDate(today);
                if (otd.getLength() == 0) {
                    otd.setIdleBits(idleBits);
                }
            });
            close(market, yesterday);
            market.setTickFeedTimestamp(0);
        }

        this.idleMarkets.clear();
    }

    private void close(FeedMarket market, int day) {
        final MarketDay md = MarketDay.create(market.getName(), day);
        final TimeTaker tt = new TimeTaker();
        try (TickFile tf = this.fileTickStore.getOrCreateTickFile(md)) {
            final List<FeedData> feedDatas = write(market, day);
            if (feedDatas == null) {
                return;
            }
            if (!tf.isOpen()) {
                this.logger.info("<close> not open " + md);
                return;
            }
            final long indexPosition = tf.position();
            new TickFileIndexWriter(tf, indexPosition).append(feedDatas, day);
            tf.setIndexPosition(indexPosition);
            this.logger.info("<close> for " + md + " took " + tt);
        } catch (IOException e) {
            this.logger.error("<close> failed for " + md, e);
        } finally {
            this.fileTickStore.removeTickFile(md);
        }
    }

    private List<FeedData> write(FeedMarket market, int day) {
        try {
            return this.writer.write(market, day).get();
        } catch (InterruptedException e) {
            this.logger.warn("<write> interrupted when writing " + market + "-" + day);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            this.logger.error("<write> failed for " + market + "-" + day, e);
        }
        return null;
    }
}
