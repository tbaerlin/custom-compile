/*
* CheckDaxAgeJmx.java
*
* Created on 06.02.2007 09:10:39
*
* Copyright (c) market maker Software AG. All Rights Reserved.
*/

package de.marketmaker.istar.feed.admin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.log.JmxLog;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.OrderedSnapData;
import de.marketmaker.istar.feed.ordered.tick.TickDecompressor;
import de.marketmaker.istar.feed.tick.TickProvider;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.common.util.DateUtil.yyyyMmDdToLocalDate;
import static de.marketmaker.istar.common.util.TimeFormatter.formatSecondsInDay;
import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.decodeLocalTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Checks data for feed symbol CLOCK.VWD, which is expected to be received once every second.
 *
 * @author Oliver Flege
 */
@ManagedResource
public class CheckClockAge implements Lifecycle {

    private static String ok(String s) {
        return "OK - " + s;
    }

    private static String critical(String s) {
        return "CRITICAL - " + s;
    }
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedDataRegistry repository;

    private TickProvider tickProvider;

    private final AtomicBoolean lastTimeCheckFailed = new AtomicBoolean();

    private final AtomicBoolean lastTickCheckFailed = new AtomicBoolean();

    private volatile int maxAllowedRtLagSecs = 10;

    private volatile int maxAllowedNtLagSecs = 15 * DateTimeConstants.SECONDS_PER_MINUTE + 10;

    private volatile int numTicks = 100;

    private final VendorkeyVwd clock = VendorkeyVwd.getInstance("1.CLOCK.VWD");

    private volatile DateTime firstCheckAt;

    @Override
    public void start() {
        this.firstCheckAt = new DateTime().plusSeconds(10);
    }

    @Override
    public void stop() {
        this.firstCheckAt = null;
    }

    @Override
    public boolean isRunning() {
        return this.firstCheckAt != null;
    }

    @ManagedAttribute
    public void setMaxAllowedRtLagSecs(int maxAllowedRtLagSecs) {
        this.maxAllowedRtLagSecs = maxAllowedRtLagSecs;
    }

    @ManagedAttribute
    public int getMaxAllowedRtLagSecs() {
        return maxAllowedRtLagSecs;
    }

    @ManagedAttribute
    public void setMaxAllowedNtLagSecs(int maxAllowedNtLagSecs) {
        this.maxAllowedNtLagSecs = maxAllowedNtLagSecs;
    }

    @ManagedAttribute
    public int getMaxAllowedNtLagSecs() {
        return maxAllowedNtLagSecs;
    }

    @ManagedAttribute
    public void setNumTicks(int numTicks) {
        this.numTicks = numTicks;
    }

    @ManagedAttribute
    public int getNumTicks() {
        return numTicks;
    }

    public void setRepository(FeedDataRegistry repository) {
        this.repository = repository;
    }

    public void setTickProvider(TickProvider tickProvider) {
        this.tickProvider = tickProvider;
    }

    @ManagedOperation
    public String checkTime() {
        final String result = check(false);
        if (result.startsWith("CRITICAL")) {
            this.logger.error("<checkTime> " + result);
            this.lastTimeCheckFailed.set(true);
        }
        else if (this.lastTimeCheckFailed.compareAndSet(true, false)) {
            this.logger.info("<checkTime> " + result + " " + JmxLog.OK_DEFAULT);
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<checkTime> " + result);
        }
        return result;
    }

    @ManagedOperation
    public String check() {
        final String result = check(true);
        if (result.startsWith("CRITICAL")) {
            this.logger.error("<checkTicks> " + result);
            this.lastTickCheckFailed.set(true);
        }
        else if (this.lastTickCheckFailed.compareAndSet(true, false)) {
            this.logger.info("<checkTicks> " + result + " " + JmxLog.OK_DEFAULT);
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<checkTicks> " + result);
        }
        return result;
    }

    private String check(boolean withTicks) {
        final DateTime now = new DateTime();
        if (now.isBefore(this.firstCheckAt)) {
            return ok("1st check at " + this.firstCheckAt);
        }

        final FeedData fd = repository.get(this.clock);
        if (fd == null) {
            return critical("no data for CLOCK.VWD");
        }

        final TickProvider.Result ticks;

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (fd) {
            final OrderedSnapData rsd = (OrderedSnapData) fd.getSnapData(true);
            if (rsd == null || !rsd.isInitialized()) {
                return critical("no snap data for CLOCK.VWD");
            }
            final DateTime rtUp = DateTimeProvider.Timestamp.toDateTime(rsd.getLastUpdateTimestamp());
            if (rtUp.plusSeconds(this.maxAllowedRtLagSecs).isBefore(now)) {
                return critical("RT CLOCK.VWD last update was " + getSecsBetween(rtUp, now) + " seconds ago");
            }

            final DateTime rt = getDatumZeit(rsd.getData(false));
            if (rt == null) {
                return critical("RT CLOCK.VWD without valid Datum/Zeit");
            }
            if (rt.plusSeconds(this.maxAllowedRtLagSecs).isBefore(now)) {
                return critical("RT CLOCK.VWD Datum/Zeit gap " + getSecsBetween(rt, now) + "s");
            }

            final OrderedSnapData nsd = (OrderedSnapData) fd.getSnapData(false);
            if (nsd != null && nsd.isInitialized()) {
                final DateTime ntUp = DateTimeProvider.Timestamp.toDateTime(rsd.getLastUpdateTimestamp());
                if (ntUp.plusSeconds(this.maxAllowedNtLagSecs).isBefore(now)) {
                    return critical("NT CLOCK.VWD last update was " + getSecsBetween(ntUp, now) + " seconds ago");
                }
                final DateTime nt = getDatumZeit(rsd.getData(false));
                if (nt == null) {
                    return critical("NT CLOCK.VWD without valid Datum/Zeit");
                }
                if (nt.plusSeconds(this.maxAllowedRtLagSecs).isBefore(now)) {
                    return critical("NT CLOCK.VWD Datum/Zeit gap " + getSecsBetween(nt, now) + "s");
                }
            }

            if (!withTicks || this.tickProvider == null || isWithoutTickCheck(now)) {
                return ok("CLOCK.VWD time is up to date " + rt.toLocalTime());
            }
            ticks = this.tickProvider.getTicks(fd, DateUtil.toYyyyMmDd(now), null);
        }

        return doCheckTicks(now, new TickDecompressor(ticks.getTicks(), ticks.getEncoding()));
    }

    private boolean isWithoutTickCheck(DateTime now) {
        return !this.firstCheckAt.plusSeconds(this.numTicks).isBefore(now)
                || now.getSecondOfDay() <= this.numTicks;
    }

    private String doCheckTicks(DateTime now, final TickDecompressor dc) {
        final int n = this.numTicks;
        final int min = Math.max(0, now.getSecondOfDay() - n);

        final int[] tickTimes = StreamSupport.stream(dc.spliterator(), false)
                .mapToInt(e -> MdpsFeedUtils.decodeTime(e.getData().getInt()))
                .filter(t -> t > min)
                .toArray();

        for (int i = 1; i < tickTimes.length; i++) {
            if (tickTimes[i - 1] + 1 < tickTimes[i]) {
                return critical("CLOCK.VWD tick gap at " + formatGap(tickTimes[i - 1] + 1, tickTimes[i]));
            }
        }

        return ok("CLOCK.VWD ticks are up to date");
    }

    private String formatGap(int from, int to) {
        if (from + 1 == to) {
            return formatSecondsInDay(from);
        }
        if (from + 2 == to) {
            return formatSecondsInDay(from) + ", " + formatSecondsInDay(from + 1);
        }
        return formatSecondsInDay(from) + " - "  + formatSecondsInDay(to - 1);
    }

    private DateTime getDatumZeit(byte[] snap) {
        final BufferFieldData fd = new BufferFieldData(snap);
        int time = -1;
        for (int oid = fd.readNext(); oid > 0 && oid <= VwdFieldOrder.ORDER_ADF_DATUM; oid = fd.readNext()) {
            switch (oid) {
                case VwdFieldOrder.ORDER_ADF_ZEIT:
                    time = fd.getInt();
                    break;
                case VwdFieldOrder.ORDER_ADF_DATUM:
                    return (time != -1)
                            ? yyyyMmDdToLocalDate(fd.getInt()).toDateTime(decodeLocalTime(time))
                            : null;
                default:
                    fd.skipCurrent();
            }
        }
        return null;
    }

    protected long getSecsBetween(DateTime then, DateTime now) {
        return MILLISECONDS.toSeconds(now.getMillis() - then.getMillis());
    }
}



