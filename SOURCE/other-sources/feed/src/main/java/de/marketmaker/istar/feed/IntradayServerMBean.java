/*
 * IntradayServerMBean.java
 *
 * Created on 23.04.2005 16:23:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.tick.TickServerMBean;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IntradayServerMBean extends TickServerMBean {
    IntradayResponse getIntradayDataJmx(IntradayRequest request);

    /**
     * Returns a vendorkey with type for an untyped one.
     * @param vkeyWithoutType
     * @return typed vendorkey.
     */
    String getTypedVendorkey(String vkeyWithoutType);

    byte[] getRawSnap(String vwdcode, boolean realtime);
}
