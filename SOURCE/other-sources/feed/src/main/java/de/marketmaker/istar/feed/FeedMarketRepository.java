/*
 * FeedMarketRegistry.java
 *
 * Created on 28.01.15 15:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import net.jcip.annotations.GuardedBy;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.feed.vwd.TickTypeCheckerVwd;

import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 * Central registry for {@link de.marketmaker.istar.feed.FeedMarket} objects.
 * Can be
 * configured to read a file with time zone information for certain markets, each line
 * in the file has to be defined as <tt>marketname=timeZoneId</tt>; this information can be
 * used to convert times in the market's home time zone into our local time zone.
 * <p>
 * The <tt>marketTickTypeProperties</tt> property allows to configure a resource that
 * defines <tt>TickTypeChecker</tt>s for the various markets. If undefined,
 * {@link TickTypeCheckerVwd#DEFAULT} will be used for all markets.
 * <p>
 *
 * @author oflege
 */
@ManagedResource
public class FeedMarketRepository implements InitializingBean {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    @GuardedBy("this.rwl")
    private final Map<ByteString, FeedMarket> markets = new HashMap<>(1024);

    private File marketTimeOffsetsFile;

    private File marketTickTypePropertiesFile = null;

    private volatile TickTypeChecker defaultTickTypeChecker = TickTypeCheckerVwd.DEFAULT;

    private ActiveMonitor activeMonitor;

    private volatile Map<String, TickTypeChecker> marketTickTypes;

    private volatile Map<ByteString, DateTimeZone> marketTimeOffsets;

    private final boolean withMarketElements;

    private final boolean withPartitions;

    private final CopyOnWriteArrayList<FeedMarketChangeListener> feedMarketChangeListeners = new CopyOnWriteArrayList<>();

    public synchronized void addChangeListener(FeedMarketChangeListener feedMarketChangeListener) {
        this.feedMarketChangeListeners.addIfAbsent(feedMarketChangeListener);
    }

    public void removeChangeListener(FeedMarketChangeListener feedMarketChangeListener) {
        this.feedMarketChangeListeners.remove(feedMarketChangeListener);
    }

    private void notifyListeners(FeedMarket data, final FeedMarketChangeListener.ChangeType type) {
        this.feedMarketChangeListeners.forEach(cl -> cl.onChange(data, type));
    }

    /**
     * If a market for a vendorkey is requested and that market is currently unknown, a FeedMarket
     * object will be created iff this field's value is true.
     */
    private boolean addMarketForVendorkey = true;

    public FeedMarketRepository(boolean withMarketElements) {
        this.withMarketElements = withMarketElements;
        this.withPartitions = false;
    }

    /**
     * Create repository with the given partitioned markets. Partitioning is useful if data is
     * stored per market (e.g., ticks, feeddump) and the size of the market's file exceeds a
     * certain limit and is therefore difficult to handle (i.e., cannot be mapped into memory, etc).
     * @param partitions names of partitioned markets as keys, number of partitions as values; the
     * values have to be multiples of 2
     */
    public FeedMarketRepository(Map<String, Integer> partitions) {
        this.withMarketElements = true;
        this.withPartitions = !partitions.isEmpty();
        for (Map.Entry<String, Integer> e : partitions.entrySet()) {
            int v = e.getValue();
            if (v < 2) {
                throw new IllegalArgumentException("Invalid partition for "
                        + e.getKey() + ": not at least 2: " + v);
            }
            if (Integer.bitCount(v) != 1) {
                throw new IllegalArgumentException("Invalid partition for "
                        + e.getKey() + ": not a multiple of 2: " + v);
            }
            final ByteString name = new ByteString(e.getKey());
            this.markets.put(name, new FeedMarket(name, v));
        }
    }

    public boolean isWithMarketElements() {
        return this.withMarketElements;
    }

    public synchronized void setAddMarketForVendorkey(boolean addMarketForVendorkey) {
        this.addMarketForVendorkey = addMarketForVendorkey;
    }

    List<FeedMarket> getMarkets() {
        this.rwl.readLock().lock();
        try {
            return new ArrayList<>(this.markets.values());
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    public FeedMarket getMarket(Vendorkey v) {
        ByteString name = v.getMarketName();
        FeedMarket fm = getExistingMarket(name);
        if (fm == null) {
            if (!this.addMarketForVendorkey) {
                return null;
            }
            return getMarket(name);
        }
        if (!fm.isPartitioned()) {
            return fm;
        }
        return getMarket(fm.getPartitionName(v.toVwdcode()));
    }

    public FeedMarket remove(ByteString name) {
        this.rwl.writeLock().lock();
        try {
            FeedMarket removed = this.markets.remove(name);
            if (removed != null) {
                this.notifyListeners(removed, FeedMarketChangeListener.ChangeType.REMOVED);
            }
            return removed;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    private FeedMarket getExistingMarket(ByteString name) {
        this.rwl.readLock().lock();
        try {
            return this.markets.get(name);
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    public FeedMarket getMarket(ByteString name) {
        this.rwl.readLock().lock();
        try {
            FeedMarket fm = this.markets.get(name);
            if (fm != null) {
                return fm;
            }

            // Must release read lock before acquiring write lock
            this.rwl.readLock().unlock();
            this.rwl.writeLock().lock();
            try {
                // We need to get the read lock again to not have strange things happen with the outer finally
                this.rwl.readLock().lock();

                // Use computeIfAbsent to cover a case where another writer got here first
                return this.markets.computeIfAbsent(name, this::newMarket);
            } finally {
                this.rwl.writeLock().unlock();
            }
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    private FeedMarket newMarket(ByteString name) {
        final FeedMarket fm = new FeedMarket(name, getTickTypeChecker(name), this.withMarketElements);
        doUpdateBoersenzeitOffset(fm, this.marketTimeOffsets);
        this.notifyListeners(fm, FeedMarketChangeListener.ChangeType.CREATED);
        return fm;
    }

    private TickTypeChecker getTickTypeChecker(ByteString name) {
        final String key = name.toString();
        return getTickTypeChecker(key, this.marketTickTypes);
    }

    private TickTypeChecker getTickTypeChecker(String key, final Map<String, TickTypeChecker> map) {
        if (map != null) {
            TickTypeChecker ttc = map.get(key);
            if (ttc != null) {
                return ttc;
            }
            TickTypeChecker defaultTtc = map.get("default");
            return defaultTtc.forMarket(key);
        }
        return this.defaultTickTypeChecker.forMarket(key);
    }

    public synchronized void setDefaultTickTypeChecker(TickTypeChecker defaultTickTypeChecker) {
        this.defaultTickTypeChecker = defaultTickTypeChecker;
        if (this.marketTickTypes == null) {
            doUpdateMarketTickTypes(null);
        }
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setMarketTimeOffsetsFile(File marketTimeOffsetsFile) {
        this.marketTimeOffsetsFile = marketTimeOffsetsFile;
    }

    public void setMarketTickTypePropertiesFile(File marketTickTypePropertiesFile) {
        this.marketTickTypePropertiesFile = marketTickTypePropertiesFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.marketTickTypes = readMarketTickTypes();
        this.marketTimeOffsets = readMarketTimeOffsets();

        if (this.activeMonitor == null) {
            return;
        }

        if (this.marketTimeOffsetsFile != null) {
            final FileResource fileResource = new FileResource(this.marketTimeOffsetsFile);
            this.activeMonitor.addResource(fileResource);
            fileResource.addPropertyChangeListener(evt -> reloadBoersenzeitOffsets());
        }
        if (this.marketTickTypePropertiesFile != null) {
            final FileResource fileResource = new FileResource(this.marketTickTypePropertiesFile);
            this.activeMonitor.addResource(fileResource);
            fileResource.addPropertyChangeListener(evt -> reloadMarketTickTypes());
        }
    }

    @ManagedOperation
    public void updateBoersenzeitOffsets() {
        if (this.marketTimeOffsets != null) {
            this.logger.info("<updateBoersenzeitOffsets>");
            doUpdateBoersenzeitOffsets(this.marketTimeOffsets);
        }
    }

    @ManagedOperation
    public void reloadBoersenzeitOffsets() {
        try {
            Map<ByteString, DateTimeZone> offsets = readMarketTimeOffsets();
            if (!Objects.equals(offsets, this.marketTimeOffsets)) {
                doUpdateBoersenzeitOffsets(offsets);
            }
            else {
                this.logger.info("<reloadBoersenzeitOffsets> no changes found");
            }
        } catch (IOException e) {
            this.logger.error("<updateBoersenzeitOffsets> failed", e);
        }
    }

    private void doUpdateBoersenzeitOffsets(Map<ByteString, DateTimeZone> offsets) {
        for (FeedMarket feedMarket : getMarkets()) {
            doUpdateBoersenzeitOffset(feedMarket, offsets);
        }
        this.marketTimeOffsets = offsets;
    }

    private void doUpdateBoersenzeitOffset(FeedMarket m, Map<ByteString, DateTimeZone> offsets) {
        final int oldOffset = m.getBoersenzeitOffset();
        final DateTimeZone dtz = (offsets != null) ? offsets.get(m.getBaseName()) : null;
        if (dtz == null) {
            if (oldOffset != 0) {
                m.setBoersenzeitOffset(0);
                this.logger.info("<doUpdateBoersenzeitOffset> for " + m.getName()
                        + " from " + oldOffset + " to 0");
            }
            return;
        }
        final int offset = getTimezoneOffset(dtz);
        m.setBoersenzeitOffset(offset);
        if (offset != oldOffset) {
            this.logger.info("<doUpdateBoersenzeitOffset> for " + m.getName()
                + " in " + dtz + " from " + oldOffset + " to " + offset);
        }
    }

    private int getTimezoneOffset(DateTimeZone dtz) {
        if (dtz == null) {
            return 0;
        }
        final long now = System.currentTimeMillis();
        final int otherOffset = dtz.getOffset(now);
        final int homeOffset = DateUtil.DTZ_BERLIN.getOffset(now);
        return (homeOffset - otherOffset) / MILLIS_PER_SECOND;
    }

    @ManagedOperation
    public void reloadMarketTickTypes() {
        try {
            Map<String, TickTypeChecker> mtt = readMarketTickTypes();
            doUpdateMarketTickTypes(mtt);
        } catch (IOException e) {
            this.logger.error("<updateMarketTickTypes> failed", e);
        }
    }

    private void doUpdateMarketTickTypes(Map<String, TickTypeChecker> mtt) {
        for (FeedMarket feedMarket : getMarkets()) {
            feedMarket.setTickTypeChecker(getTickTypeChecker(feedMarket.getBaseName().toString(), mtt));
        }
        this.marketTickTypes = mtt;
    }

    private Map<String, TickTypeChecker> readMarketTickTypes() throws IOException {
        if (this.marketTickTypePropertiesFile == null) {
            return null;
        }
        final Properties p = PropertiesLoader.load(this.marketTickTypePropertiesFile);
        this.logger.info("<readMarketTickTypes> " + new TreeMap<>(p));
        return TickTypeCheckerVwd.create(p);
    }

    private Map<ByteString, DateTimeZone> readMarketTimeOffsets() throws IOException {
        if (this.marketTimeOffsetsFile == null) {
            return null;
        }

        final HashMap<ByteString, DateTimeZone> result = new HashMap<>();
        try (Scanner s = new Scanner(this.marketTimeOffsetsFile)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                if (!StringUtils.hasText(line) || line.startsWith("#")) {
                    continue;
                }
                final String[] tokens = line.split("=");
                if (tokens.length != 2) {
                    this.logger.warn("<readMarketTimeOffsets> invalid line: " + line);
                    continue;
                }
                try {
                    result.put(new ByteString(tokens[0]), DateTimeZone.forID(tokens[1]));
                } catch (IllegalArgumentException e) {
                    this.logger.error("<readMarketTimeOffsets> invalid timezone '" + tokens[1] + "'");
                }
            }
        }
        this.logger.info("<readMarketTimeOffsets> " + result);
        return result;
    }

    int size() {
        if (this.withPartitions) {
            // count only "real" markets, ignore partitions
            return (int) getMarkets().stream().filter((m) -> !m.isPartition()).count();
        }
        this.rwl.readLock().lock();
        try {
            return this.markets.size();
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    int gc(Consumer<FeedData> gcCallback) {
        int result = 0;
        for (FeedMarket market : getMarkets()) {
            final int numRemoved = market.gc(gcCallback);
            if (numRemoved > 0) {
                result += numRemoved;
            }
            if (market.size() == 0) {
                remove(market.getName());
                this.logger.info("<gc> removed market " + market.getName());
            } else if (numRemoved > 0) {
                this.logger.info("<gc> removed " + numRemoved + " from " + market.getName()
                    + ", " + market.size() + " remaining");
            }
        }
        return result;
    }

    public Iterable<FeedData> getElements() {
        if (!this.withMarketElements) {
            return Collections.emptyList();
        }
        return () -> new Iterator<FeedData>() {
            final Iterator<FeedMarket> mit = getMarkets().iterator();
            Iterator<FeedData> it = mit.hasNext()
                    ? this.mit.next().getElements(false).iterator()
                    : Collections.emptyIterator();
            FeedData next = advance();

            FeedData advance() {
                while (this.it.hasNext() || this.mit.hasNext()) {
                    while (this.it.hasNext()) {
                        final FeedData fd = this.it.next();
                        if (fd.isDeleted()) {
                            continue;
                        }
                        return fd;
                    }
                    this.it = this.mit.next().getElements(false).iterator();
                }
                return null;
            }

            @Override
            public boolean hasNext() {
                return this.next != null;
            }

            @Override
            public FeedData next() {
                final FeedData result = this.next;
                this.next = advance();
                return result;
            }
        };
    }
}
