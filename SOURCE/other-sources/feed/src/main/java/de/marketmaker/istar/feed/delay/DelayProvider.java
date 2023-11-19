/*
 * DelayProvider.java
 *
 * Created on 06.03.2006 16:31:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.FeedData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DelayProvider {

    /**
     * convenience method
     * @param data provides vendorkey
     * @return result of {@link #getDelayInSeconds(de.marketmaker.istar.feed.Vendorkey)}, invoked
     * with <tt>data.getVendorkey()</tt>
     */
    int getDelayInSeconds(FeedData data);

    /**
     * @param key
     * @return delay for the specified key or a default delay that is used if no rule
     * that applies to the key is available
     */
    int getDelayInSeconds(Vendorkey key);
}
