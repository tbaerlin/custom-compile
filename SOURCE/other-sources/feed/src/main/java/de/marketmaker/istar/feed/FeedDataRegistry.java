/*
 * FeedDataRegistry.java
 *
 * Created on 24.09.2008 15:09:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.List;

import io.netty.util.AsciiString;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FeedDataRegistry {
    /**
     * Creates a new FeedData object but does <em>not</em> register it
     * @param vkey feed symbol
     * @return FeedData object (or null if vkey is not a valid key)
     */
    FeedData create(Vendorkey vkey);

    /**
     * Registers a FeedData object (e.g., created earlier with {@link #create(Vendorkey)}).
     * @param data to be registered
     */
    void register(FeedData data);

    /**
     * Lookup the FeedData associated with vkey and return it. If no data is currently associated,
     * it will be created and returned.
     * @param vkey feed symbol
     * @return FeedData object (or null if vkey is not a valid key)
     */
    FeedData register(Vendorkey vkey);

    /**
     * Lookup the FeedData associated with vkey and return it. If no data is currently associated,
     * return null.
     * @param vkey feed symbol
     * @return FeedData object (or null if vkey is invalid or no data is associated)
     */
    FeedData get(Vendorkey vkey);

    /**
     * Lookup the FeedData associated with vwdcode and return it. If no data is currently associated,
     * return null.
     * @param vwdcode feed symbol
     * @return FeedData object (or null if vwdcode is invalid or no data is associated)
     */
    FeedData get(ByteString vwdcode);

    /**
     * Lookup the FeedData associated with vwdcode and return it. If no data is currently associated,
     * return null.
     * @param vwdcode array containing the feed symbol
     * @return FeedData object (or null if vwdcode is invalid or no data is associated)
     */
    FeedData get(AsciiString vwdcode);

    /**
     * sets the factory used to create FeedData instances
     * @param dataFactory
     */
    void setDataFactory(FeedDataFactory dataFactory);

    /**
     * Remove the data associated with the key from the registry
     * @param vkey to be removed
     * @return whether any data was removed
     */
    boolean unregister(Vendorkey vkey);

    /**
     * @return the currently registered elements
     */
    List<FeedData> getElements();

    FeedMarketRepository getFeedMarketRepository();

}
