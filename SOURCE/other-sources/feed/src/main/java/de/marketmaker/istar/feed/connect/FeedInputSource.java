/*
 * FeedInputSource.java
 *
 * Created on 13.12.2004 10:53:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.net.InetSocketAddress;
import java.util.Comparator;

/**
 * Input address for a specific data feed. Each input address has a name, a priority
 * that allows to use the address with the highest priority that is available, and
 * one or more sockets from which feed data can be read.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeedInputSource {
    @SuppressWarnings("UseCompareMethod")
    public final static Comparator<FeedInputSource> COMPARATOR_BY_PRIO =
        (o1, o2) -> o1.priority < o2.priority ? -1 : (o1.priority == o2.priority ? 0 : 1);

    /** name of is */
    private final String name;

    /** priority */
    private final int priority;

    /** InetSocketAddress objects representing this feed's input sockets */
    private final InetSocketAddress address;

    /**
     * Constructor
     * @param name this address's name
     * @param priority this address's priority
     * @param hostname this address's hostname
     * @param port this address's port
     */
    public FeedInputSource(String name, int priority, String hostname, int port) {
        this.name = name;
        this.priority = priority;
        this.address = new InetSocketAddress(hostname, port);
    }

    public String getName() {
        return this.name;
    }

    public int getPriority() {
        return this.priority;
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public String toString() {
        return "FeedInputSource[\"" + name + "\" Prio=" + priority + " "
                + this.address + "]";
    }
}
