/*
 * VolatileFeedDataRegistry.java
 *
 * Created on 24.09.2008 15:17:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.Collections;
import java.util.List;

import io.netty.util.AsciiString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;

/**
 * A FeedDataRegistry that creates a new FeedData object for each requested vendorkey,
 * no feed data objects will be created if no vendorKey Object is used in the request
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VolatileFeedDataRegistry implements FeedDataRegistry {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final FeedMarketRepository marketRepository;

    private FeedDataFactory dataFactory = FeedDataVkeyOnly.FACTORY;

    public VolatileFeedDataRegistry() {
        this(new FeedMarketRepository(false));
    }

    public VolatileFeedDataRegistry(FeedMarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Override
    public void setDataFactory(FeedDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    @Override
    public FeedData create(Vendorkey vkey) {
        return register(vkey);
    }

    @Override
    public void register(FeedData data) {
        // empty
    }

    @Override
    public FeedData register(Vendorkey vkey) {
        FeedMarket m = getMarket(vkey);
        if (m == null) {
            return null;
        }
        return dataFactory.create(vkey, m);
    }

    protected FeedMarket getMarket(Vendorkey vkey) {
        return this.marketRepository.getMarket(vkey);
    }

    @Override
    public FeedData get(Vendorkey key) {
        return register(key);
    }

    @Override
    public FeedData get(ByteString vwdcode) {
        return null; // we need real vendorkeys...
    }

    @Override
    public FeedData get(AsciiString vwdcode) {
        return null; // we need real vendorkeys...
    }

    @Override
    public boolean unregister(Vendorkey vkey) {
        return false;
    }

    @Override
    public List<FeedData> getElements() {
        return Collections.emptyList();
    }

    @Override
    public FeedMarketRepository getFeedMarketRepository() {
        return this.marketRepository;
    }
}
