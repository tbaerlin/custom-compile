/*
 * EndOfDayScheduler.java
 *
 * Created on 09.04.15 09:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.Lifecycle;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataChangeListener;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.FeedMarketRepository;
import de.marketmaker.istar.feed.mdps.MdpsEodWriter;
import de.marketmaker.istar.feed.mdps.MdpsMessageTypes;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

import static de.marketmaker.istar.feed.FeedDataChangeListener.ChangeType.CREATED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Schedules events based on <code>EndOfDay*</code> entries in <tt>exchange_time_schedule.cfg</tt>
 * @author oflege
 */
public class EndOfDayScheduler implements
        InitializingBean, Lifecycle, ApplicationListener<EndOfDayRulesChangedEvent> {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("E,yyyy-MM-dd'T'HH:mm");

    private enum JobType { END_OF_DAY, END_OF_DAY_START, END_OF_DAY_END }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final class Job implements Runnable {
        private final EndOfDayProviderImpl.Item item;

        private final JobType type;

        protected Job(EndOfDayProviderImpl.Item item, JobType type) {
            this.item = item;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Job{" + this.type + ", " + this.item + '}';
        }

        @Override
        public void run() {
            EndOfDayScheduler.this.run(this);
        }
    }


    private final ScheduledThreadPoolExecutor executor
            = new ScheduledThreadPoolExecutor(1, r -> {
        return new Thread(r, "EoD-Scheduler");
    });

    private EndOfDayProviderImpl endOfDayProvider;

    private OrderedEntitlementProvider entitlementProvider;

    private FeedMarketRepository marketRepository;

    private FeedDataRepository feedDataRepository;

    private Set<ByteString> marketNames = new HashSet<>();

    private MdpsEodWriter eodWriter;

    private ThroughputLimiter throughputLimiter = ThroughputLimiter.UNLIMITED;

    private boolean started = false;

    public EndOfDayScheduler() {
        this.executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    public void setFeedDataRepository(FeedDataRepository feedDataRepository) {
        this.feedDataRepository = feedDataRepository;
    }

    public void setMarketRepository(FeedMarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    public void setEntitlementProvider(OrderedEntitlementProvider entitlementProvider) {
        this.entitlementProvider = entitlementProvider;
    }

    public void setEndOfDayProvider(EndOfDayProviderImpl endOfDayProvider) {
        this.endOfDayProvider = endOfDayProvider;
    }

    public void setEodWriter(MdpsEodWriter eodWriter) {
        this.eodWriter = eodWriter;
    }

    public void setMaxNumSendPerSec(int num) {
        this.throughputLimiter = new ThroughputLimiter(num);
    }

    @Override
    public void onApplicationEvent(EndOfDayRulesChangedEvent event) {
        if (this.started) {
            this.executor.schedule(this::onUpdate, 100, MILLISECONDS);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // FeedDataRepo is configured not to create markets for vendorkey lookups; we
        // need to pre-register all markets with eod rules, so that their vendorkeys can be
        // restored from snap file
        this.endOfDayProvider.getMappings().keySet().stream()
                .filter(this.entitlementProvider::hasEntitlementsForMarket)
                .forEach(this.marketRepository::getMarket);
    }

    @Override
    public void start() {
        ScheduledFuture<?> f = this.executor.schedule(this::onUpdate, 0, SECONDS);
        try {
            f.get();
        } catch (InterruptedException e) {
            this.logger.warn("<start> interrupted?!");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        this.started = true;
        this.feedDataRepository.setChangeListener(this::onChange);
    }

    @Override
    public void stop() {
        cancelPending();
        this.executor.shutdown();
        this.logger.info("<stop> done");
    }

    @Override
    public boolean isRunning() {
        return this.started;
    }

    private void onChange(FeedData data, FeedDataChangeListener.ChangeType type) {
        if (type == CREATED) {
            EndOfDayProviderImpl.Item item = this.endOfDayProvider.getItem(data.getVendorkey());
            if (item != null && item.betweenStartAndEnd == Boolean.TRUE) {
                data.setReadyForPush(true);
            }
        }
    }

    protected void onUpdate() {
        this.logger.info("<onUpdate> ...");
        Map<ByteString, EndOfDayProviderImpl.MarketItems> mappings = this.endOfDayProvider.getMappings();
        Set<ByteString> tmp = new HashSet<>(mappings.keySet());

        HashSet<ByteString> removed = new HashSet<>(this.marketNames);
        removed.removeAll(tmp);
        for (ByteString name : removed) {
            this.feedDataRepository.removeMarket(name.toString());
        }
        this.marketNames.removeAll(removed);

        if (this.started) {
            cancelPending();
        }

        tmp.removeAll(this.marketNames);

        for (ByteString name : tmp) {
            if (this.entitlementProvider.hasEntitlementsForMarket(name)) {
                this.marketRepository.getMarket(name);
                this.marketNames.add(name);
            }
            else {
                this.logger.warn("<onUpdate> ignoring " + name + ", is w/o entitlements");
            }
        }

        mappings.values().forEach(this::schedule);
        this.logger.info("<onUpdate> finished");
    }

    protected void cancelPending() {
        BlockingQueue<Runnable> queue = this.executor.getQueue();
        int size = queue.size();
        queue.clear();
        this.logger.info("<afterPropertiesSet> cancelled " + size + " pending tasks");
    }

    private void schedule(EndOfDayProviderImpl.Item item, JobType type,
            DateTime now, DateTime when) {
        Job j = new Job(item, type);
        this.executor.schedule(j, when.getMillis() - now.getMillis(), MILLISECONDS);
        this.logger.info("<schedule> " + j + " to run at " + DTF.print(when));
    }

    private void schedule(EndOfDayProviderImpl.MarketItems items) {
        final DateTime now = new DateTime();

        for (EndOfDayProviderImpl.Item item : items.getItems()) {
            if (this.marketNames.contains(item.getMarketName())) {
                schedule(now, item);
            }
        }
    }

    private void schedule(DateTime now, EndOfDayProviderImpl.Item item) {
        DateTime start = getNext(item, item.eodStart, now);
        if (item.eodEnd == null) {
            schedule(item, JobType.END_OF_DAY, now, start);
        }
        else if (item.eodStart == item.eodEnd) {
            schedule(item, JobType.END_OF_DAY_START, now, start);
            if (!this.started) {
                setReadyForPush(item, true);
            }
        }
        else {
            DateTime end = getNext(item, item.eodEnd, now);
            if (start.isBefore(end)) {
                schedule(item, JobType.END_OF_DAY_START, now, start);
            }
            else {
                schedule(item, JobType.END_OF_DAY_END, now, end);
                if (!this.started) {
                    setReadyForPush(item, true);
                }
            }
        }
    }

    private void setReadyForPush(EndOfDayProviderImpl.Item item, Boolean value) {
        if (item.betweenStartAndEnd == value) {
            return;
        }
        boolean b = value;
        int[] n = new int[1];
        FeedMarket m = this.marketRepository.getMarket(item.getMarketName());
        m.applyToElements((fd) -> {
            if (item.isAcceptable(fd.getVendorkey())) {
                fd.setReadyForPush(b);
                n[0]++;
            }
        });
        item.betweenStartAndEnd = value;
        this.logger.info("<setReadyForPush> for #" + n[0] + " in " + item);
    }

    /**
     * @return DateTime in local timezone that corresponds to <code>lt</code> in <code>zone</code>
     * and is after <code>now</code> and on a day for which events are scheduled in <code>item</code>
     */
    private static DateTime getNext(EndOfDayProviderImpl.Item item, LocalTime lt, DateTime now) {
        DateTime ldt = lt.toDateTimeToday(item.getZone());
        DateTime result;
        while ((result = ldt.toDateTime(DateUtil.DTZ_BERLIN)).isBefore(now)
                || !item.isScheduledForDay(ldt.getDayOfWeek())) {
            ldt = ldt.plusDays(1);
        }
        return result;
    }

    private void run(Job job) {
        this.logger.info("<run> " + job + "...");

        try {
            if (job.type == JobType.END_OF_DAY_END) {
                setReadyForPush(job.item, false);
            } else {
                send(job.item, job.type);
            }
        } catch (Exception e) {
            this.logger.error("<run> failed for " + job, e);
        } finally {
            schedule(new DateTime(), job.item);
        }
    }

    private void send(EndOfDayProviderImpl.Item item, JobType type) {
        TimeTaker tt = new TimeTaker();
        final boolean readyForPush = (type == JobType.END_OF_DAY_START);
        int n = 0;
        FeedMarket market = this.marketRepository.getMarket(item.getMarketName());
        for (FeedData fd : market.getElements(false)) {
            if (!item.isAcceptable(fd.getVendorkey())) {
                continue;
            }
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (fd) {
                if (fd.isDeleted()) {
                    continue;
                }
                OrderedFeedData ofd = (OrderedFeedData) fd;
                OrderedSnapData sd = ofd.getSnapData(true);
                if (!sd.isInitialized()) {
                    continue;
                }
                this.eodWriter.append(ofd, new BufferFieldData(sd.getData(false)), MdpsMessageTypes.RECAP);
                fd.setReadyForPush(readyForPush);
            }
            n++;
            this.throughputLimiter.ackAction();
        }
        item.betweenStartAndEnd = (type == JobType.END_OF_DAY_START) ? Boolean.TRUE : null;
        this.logger.info("<send> for #" + n + " in " + item + ", took " + tt);
    }
}
