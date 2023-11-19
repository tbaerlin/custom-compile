/*
 * PushRegistry.java
 *
 * Created on 11.02.2010 12:51:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.push;

/**
 * Low-level interface to register vendorkeys in a push server. A push server will only parse data
 * for keys registered by using this interface. If realtime and delayed push is required, two
 * components of this type will be needed.
 * @author oflege
 */
public interface PushRegistry {
    /**
     * Register vendorkey so that forthcoming feed updates will be parsed and can be added to the
     * pushed feed.
     * @param vendorkey feed symbol
     * @return true iff vendorkey is now registered; false iff vendorkey is not a valid feed symbol
     */
    boolean register(String vendorkey);

    /**
     * Calling {@link #register(String)} does <em>not</em> mean that the snap data can already be
     * used as it will be incomplete: only those fields will be available that are present in the
     * feed after register has been called. To complete registrations, the data has to be merged
     * with complete snap records obtained from a "real" chicago instance. Call this method to
     * request complete snap data for your registrations. Calling it for every key is very
     * inefficient. It should be called after all needed keys have been registered. This method
     * may be implemented by creating an asynchronous task that fetches that data. To figure out
     * whether feed data is complete use {@link de.marketmaker.istar.feed.FeedData#isReadyForPush()}.
     */
    void completeRegistrations();

    /**
     * Unregister vendorkey so that forthcoming feed updates will no longer be parsed
     * @param vendorkey feed symbol
     */
    void unregister(String vendorkey);
}
