/*
 * OrderedFeedDataGC.java
 *
 * Created on 28.11.12 14:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.DateTimeProviderStatic;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;

/**
 * @author oflege
 */
@ManagedResource
public class OrderedFeedDataGC {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedDataRepository repository;

    private VendorkeyFilterFactory filterFactory;

    private volatile boolean enabled = true;

    private int minDaysIdle = 8;

    public void setFilterFactory(VendorkeyFilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    @ManagedAttribute
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ManagedAttribute
    public boolean isEnabled() {
        return enabled;
    }

    public void setMinDaysIdle(int minDaysIdle) {
        this.minDaysIdle = minDaysIdle;
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "minDaysIdle",
                    description = "gc symbols w/o update for this many days, > 1 required"),
            @ManagedOperationParameter(name = "filename", description = "if defined, write symbols to file, dry-run")
    })
    public String gc(int minDaysIdle, String filename) throws IOException {
        if (minDaysIdle <= 1) {
            return minDaysIdle + " must be > 1";
        }
        if (StringUtils.hasText(filename) && !"String".equals(filename)) {
            final File f = new File(filename);
            dumpIdleSymbols(minDaysIdle, f);
            return "wrote symbols to " + f.getAbsolutePath();
        }
        else {
            gc(minDaysIdle);
            return "invoked gc";
        }
    }

    private void dumpIdleSymbols(int daysIdle, File f) throws IOException {
        final DateTime dt = new DateTime().minusDays(daysIdle).withTimeAtStartOfDay();
        final int minTimestamp = new DateTimeProviderStatic(dt).current().feedTimestamp;

        try (PrintWriter pw = new PrintWriter(new FileOutputStream(f))) {
            pw.println("# Symbols without update since " + dt);
            for (FeedMarket market : this.repository.getMarkets()) {
                market.applyToElements(feedData -> {
                    final OrderedSnapData snapData = (OrderedSnapData) feedData.getSnapData(true);
                    if (snapData.getLastUpdateTimestamp() < minTimestamp) {
                        pw.println(feedData.getVwdcode());
                    }
                });
            }
        }
    }

    public void gc() {
        if (!isEnabled()) {
            this.logger.info("<gc> disabled");
            return;
        }
        gc(this.minDaysIdle);
    }

    private void gc(int daysIdle) {
        final DateTime dt = new DateTime().minusDays(daysIdle).withTimeAtStartOfDay();
        final int minTimestamp = new DateTimeProviderStatic(dt).current().feedTimestamp;
        final VendorkeyFilter filter = getFilter();
        this.logger.info("<gc> data older than " + dt
                + ((filter != null) ? (" and filter=" + filter) : ""));

        final int num[] = new int[1];

        for (FeedMarket market : this.repository.getMarkets()) {
            market.applyToElements(fd -> !fd.isDeleted(), fd -> {
                if (isGarbage(fd, minTimestamp, filter)) {
                    fd.setState(FeedData.STATE_GARBAGE);
                    num[0]++;
                }
            });
        }

        this.logger.info("<gc> marked " + num[0] + " objects as garbage");
        this.repository.gc();
    }

    private boolean isGarbage(FeedData fd, int minTimestamp, VendorkeyFilter f) {
        if (f != null && !f.test(fd.getVendorkey())) {
            return true;
        }
        final OrderedSnapData snapData = (OrderedSnapData) fd.getSnapData(true);
        return snapData.getLastUpdateTimestamp() < minTimestamp;
    }

    private VendorkeyFilter getFilter() {
        // returning a filter is only sensible if it is not a singleton, as in that case the
        // parser, which happens to use the same filter, has never permitted any vendorkey
        // not accepted by the filter.
        return (this.filterFactory != null && !this.filterFactory.isSingleton())
                ? this.filterFactory.getObject() : null;
    }
}
