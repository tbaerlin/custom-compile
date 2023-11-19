/*
 * Market.java
 *
 * Created on 12.04.2006 09:36:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.jcip.annotations.GuardedBy;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.vwd.MarketTickFields;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeedMarket implements Comparable<FeedMarket> {
    /**
     * Separates a market's baseName and its partition number
     */
    public static final String PARTITION_SEPARATOR = "^";

    /**
     * markets with names ending on this string are supposed to contain market depth symbols
     */
    public static ByteString MARKET_DEPTH_SUFFIX = new ByteString("MT");

    private final ByteString name;

    private final ByteString baseName;

    private final boolean marketDepth;

    private final BitSet tickOrderIds;

    /**
     * If a market is split into <code>n</code> partitions, this value will be <code>(n - 1)</code>,
     * so it can be used as a mask for a FeedData's hashCode to compute the data's partition. For
     * an unpartitioned market (or the object that represents a partition instance)
     * this value will be 0.
     */
    private final int partition;

    /**
     * If a market FOO is split into <code>n</code> partitions, this array contains the names of the
     * partition instances, i.e., <code>FOO^0 .. FOO^m</code> with <code>m = n - 1</code>
     */
    private final ByteString[] partitionNames;

    // if we store elements per market

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    @GuardedBy("this.rwl")
    private final ArrayList<FeedData> elements;

    @GuardedBy("this.rwl")
    private boolean elementsSorted = true;

    // all other non-final fields are volatile

    private volatile int boersenzeitOffset;

    private volatile TickTypeChecker tickTypeChecker;

    private volatile int tickFeedTimestamp = 0;

    public FeedMarket(ByteString name, int partition) {
        this(name, null, false, partition);
    }

    FeedMarket(ByteString name, TickTypeChecker tickTypeChecker, boolean withMarketElements) {
        this(name, tickTypeChecker, withMarketElements, 0);
    }

    private FeedMarket(ByteString name, TickTypeChecker tickTypeChecker, boolean withMarketElements,
            int partition) {
        this.name = name;
        int p = name.indexOf('^');
        this.baseName = p < 0 ? name : name.substring(0, p);
        this.tickTypeChecker = tickTypeChecker;
        this.tickOrderIds = MarketTickFields.getTickOrderIds(this.baseName);
        this.marketDepth = this.baseName.endsWith(MARKET_DEPTH_SUFFIX);
        this.elements = withMarketElements ? new ArrayList<>(128) : null;

        if (partition > 0) {
            this.partition = partition - 1;
            this.partitionNames = new ByteString[partition];
            for (int i = 0; i < this.partitionNames.length; i++) {
                this.partitionNames[i] = this.name.append(new ByteString(PARTITION_SEPARATOR + i));
            }
        }
        else {
            this.partition = 0;
            this.partitionNames = null;
        }
    }

    public String toString() {
        return "FeedMarket[" + this.name + "/#" + size() + "]";
    }

    /**
     * HACK to adjust field ADF_Boersenzeit so that it is ME(S)Z: For some exchanges, vwd submits
     * ADF_Boersenzeit in a different timezone. Since we need this timestamp to assign a time to
     * ticks, we are required to have an ME(S)Z time available.
     * @return number of seconds to be added to ADF_Boersenzeit to obtain a valid ME(S)Z time.
     */
    public int getBoersenzeitOffset() {
        return this.boersenzeitOffset;
    }

    void setBoersenzeitOffset(int boersenzeitOffset) {
        this.boersenzeitOffset = boersenzeitOffset;
    }

    public int getTickFeedTimestamp() {
        return this.tickFeedTimestamp;
    }

    public void setTickFeedTimestamp(int tickFeedTimestamp) {
        this.tickFeedTimestamp = tickFeedTimestamp;
    }

    boolean isPartition() {
        return this.name != this.baseName;
    }

    boolean isPartitioned() {
        return this.partition != 0;
    }

    ByteString getPartitionName(ByteString vwdcode) {
        if (this.partition == 0) {
            return null;
        }
        return this.partitionNames[vwdcode.hashCode() & this.partition];
    }

    void setTickTypeChecker(TickTypeChecker tickTypeChecker) {
        this.tickTypeChecker = tickTypeChecker;
    }

    public TickTypeChecker getTickTypeChecker() {
        return this.tickTypeChecker;
    }

    /**
     * order ids of fields that should be captured as <em>additional</em> tick fields for this market.
     * Additional tick fields have an order id &gt;= {@value de.marketmaker.istar.feed.vwd.VwdFieldOrder#FIRST_NON_TICK},
     * normal tick fields will be captured regardless of what this method returns.
     * @return tick field order ids, may be null
     */
    public BitSet getTickOrderIds() {
        return this.tickOrderIds;
    }

    /**
     * This market's name, e.g., "ETR" for Xetra.
     * @return
     */
    public ByteString getName() {
        return this.name;
    }

    public ByteString getBaseName() {
        return this.baseName;
    }

    public boolean isMarketDepth() {
        return this.marketDepth;
    }

    public int size() {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return -1;
        }
        this.rwl.readLock().lock();
        try {
            return this.elements.size();
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    public void add(FeedData data) {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return;
        }
        this.rwl.writeLock().lock();
        try {
            this.elements.add(data);
            this.elementsSorted = false;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    public int compareTo(FeedMarket market) {
        return this.name.compareTo(market.getName());
    }

    /**
     * Returns all elements of this market. The returned list
     * is a snapshot of the elements currently associated with the market, modifications of
     * the list will not modify the market data. If the operation to be invoked on each
     * element completes fast, consider using {@link #applyToElements(Consumer)}
     * @param sorted iff true, elements will be sorted by vwdcode
     * @return elements.
     */
    public List<FeedData> getElements(boolean sorted) {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return Collections.emptyList();
        }
        this.rwl.readLock().lock();
        try {
            if (sorted && !this.elementsSorted) {
                // Must release read lock before acquiring write lock
                this.rwl.readLock().unlock();
                this.rwl.writeLock().lock();
                try {
                    if (!this.elementsSorted) {
                        this.elements.sort(FeedData.COMPARATOR_BY_VWDCODE);
                        this.elementsSorted = true;
                    }

                    // Downgrade to readLock again
                    this.rwl.readLock().lock();
                } finally {
                    // Unlock write, still hold read
                    this.rwl.writeLock().unlock();
                }
            }
            // this is the fastest way to clone this.elements, as it reuses a copy of the
            // backing array which would even be created if we supplied a List to be filled
            // with this.elements by calling addAll
            return new ArrayList<>(this.elements);
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    void remove(FeedData data) {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return;
        }
        this.rwl.writeLock().lock();
        try {
            this.elements.remove(data);
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Perform some operation defined by closure on all elements. This should only be called
     * if the time to perform this method is not significantly larger than calling
     * {@link #getElements(boolean)}, because while iterating over elements
     * <code>this.elements</code> is locked.
     * @param c action
     */
    public void applyToElements(Consumer<FeedData> c) {
        applyToElements(fd -> true, c);
    }

    public void applyToElements(Predicate<? super FeedData> p, Consumer<FeedData> c) {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return;
        }
        this.rwl.readLock().lock();
        try {
            for (FeedData fd : this.elements) {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (fd) {
                    if (p.test(fd)) {
                        c.accept(fd);
                    }
                }
            }
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    public List<FeedData> collect(Predicate<? super FeedData> p) {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return Collections.emptyList();
        }
        this.rwl.readLock().lock();
        try {
            return this.elements.stream().filter(p).collect(Collectors.toList());
        } finally {
            this.rwl.readLock().unlock();
        }
    }


    /**
     * Removes all FeedData objects from this market for which {@link FeedData#isToBeDisposed()}
     * returns true
     * @param gcCallback its {@link Consumer#accept(Object)} method will be invoked for each
     * removed element.
     * @return number of removed Elements
     */
    int gc(Consumer<FeedData> gcCallback) {
        //noinspection FieldAccessNotGuarded
        if (this.elements == null) {
            return -1;
        }

        // Since there are iterative changes that rely on this being the only writer
        // we acquire the writeLock here instead of up- and downgrading between
        // read and write lock
        this.rwl.writeLock().lock();
        try {
            int k = 0;
            int numDeleted = 0;
            for (int i = 0; i < this.elements.size(); i++) {
                FeedData fd = this.elements.get(i);
                if (isToBeDeleted(fd)) {
                    numDeleted++;
                    gcCallback.accept(fd);
                }
                else {
                    this.elements.set(k++, fd);
                }
            }
            if (numDeleted > 0) {
                this.elements.subList(k, this.elements.size()).clear();
            }
            return numDeleted;
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    private boolean isToBeDeleted(FeedData fd) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (fd) {
            return fd.isToBeDisposed();
        }
    }
}
