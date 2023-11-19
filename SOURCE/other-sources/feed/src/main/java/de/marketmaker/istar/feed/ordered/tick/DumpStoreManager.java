/*
 * DumpStoreManager.java
 *
 * Created on 23.01.15 20:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.tick.TickProvider;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.DUMP3;

/**
 * Subclass of {@link de.marketmaker.istar.feed.ordered.tick.TickStoreManager} that implements a
 * different evictIdle strategy:
 * @author oflege
 */
@ManagedResource
public class DumpStoreManager extends TickStoreManager {
    private static final int EVICT_IDLE_SLEEP_EVERY = 262144;


    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vwdcode", description = "vwdcode"),
            @ManagedOperationParameter(name = "filename", description = "output filename")
    })
    public String exportDump(String vwdcode, String filename) {
        FeedData fd = this.repository.get(new ByteString(vwdcode.trim()));
        if (fd == null) {
            return "no such symbol";
        }
        TickProvider.Result r;
        synchronized (fd) {
            r = this.memoryTickStore.getTicks(fd, DateUtil.dateToYyyyMmDd(), null);
        }
        if (r == null) {
            return "no data";
        }
        byte[] data = r.getTicks();
        File f = new File(filename.trim());
        DumpCli.LineBuilder lb = new DumpCli.LineBuilder();
        try (PrintWriter pw = new PrintWriter(f, "utf8")) {
            pw.println("# " + vwdcode);
            for (DumpDecompressor.Element e : new DumpDecompressor(data, DUMP3)) {
                pw.println(lb.build(e));
            }
        } catch (Exception e) {
            this.logger.warn("<exportDump> failed", e);
            return e.getMessage();
        }
        return "wrote " + f.getAbsolutePath();
    }

    @Override
    protected void submitExtraTasks() {
        submit("59 0 0 * * ?", () -> {
            closeYesterday();
            releaseYesterday();
        });
        submit("12 1/2 * * * ?", this::evictIdle);
    }

    private void evictIdle() {
        final TimeTaker tt = new TimeTaker();

        DateTime now = DateTime.now();

        // for markets w/o a tick in the past 5 minutes, we want to evict data that
        // has been idle for more than a minute
        final DateTimeProvider.Timestamp ts1
                = new DateTimeProvider.Timestamp(now.minusMinutes(1));

        // for the others, we want to evict data that has been idle for at least 5mins
        final DateTimeProvider.Timestamp ts5
                = new DateTimeProvider.Timestamp(now.minusMinutes(5));

        int sleepThreshold = EVICT_IDLE_SLEEP_EVERY;
        int numChecked = 0;
        int numEvicted = 0;
        for (FeedMarket market : repository.getMarkets()) {
            final DateTimeProvider.Timestamp ts =
                market.getTickFeedTimestamp() > ts5.feedTimestamp ? ts5 : ts1;

            int i = doEvictIdle(market, ts.yyyyMmDd, ts.feedTimestamp);
            numEvicted += Math.abs(i);

            numChecked += market.size();
            sleepThreshold = sleepIfExceeds(numChecked, sleepThreshold);
            if (this.stopped) {
                return;
            }
        }

        this.logger.info("<evictIdle> for " + numEvicted + "/" + numChecked + ", took " + tt);

        flushIdleTasks();
    }

    private int sleepIfExceeds(int numChecked, int threshold) {
        if (numChecked < threshold) {
            return threshold;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return threshold + EVICT_IDLE_SLEEP_EVERY;
    }
}
