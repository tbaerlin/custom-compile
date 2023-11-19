/*
 * TickServerMBean.java
 *
 * Created on 23.04.2005 16:17:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TickServerMBean {
    /**
     * @deprecated use {@link #getTickItem(String, int)} as you will need an Encoding to tell
     * how to decode the ticks.
     */
    byte[] getTicks(String vendorkey, int date);

    AbstractTickRecord.TickItem getTickItem(String vendorkey, int date);
}
