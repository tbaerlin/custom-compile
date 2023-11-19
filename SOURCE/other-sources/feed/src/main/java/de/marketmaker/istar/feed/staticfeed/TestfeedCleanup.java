/*
 * TestfeedCleanup.java
 *
 * Created on 27.01.14 08:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.staticfeed;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;

/**
 * The test static feed will only contain updates for certain markets. In order to cleanup data
 * for markets that are no longer active, this class reads a file with currently valid market names
 * (one name per line) and removes all data for markets not in that set from the feed data repository.
 *
 * @author oflege
 */
public class TestfeedCleanup {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedDataRepository repository;

    private File activeMarkets;

    public void setActiveMarkets(File activeMarkets) {
        this.activeMarkets = activeMarkets;
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Expected to be invoked by a scheduler
     */
    public void cleanup() {
        Set<String> marketNames = new TreeSet<>();
        try {
            marketNames.addAll(Files.readAllLines(this.activeMarkets.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            this.logger.error("<cleanup> failed to read " + activeMarkets.getAbsolutePath(), e);
            return;
        }

        this.logger.info("<cleanup> market names = " + marketNames);

        for (FeedMarket market : repository.getMarkets()) {
            final String marketName = market.getName().toString();
            if (!marketNames.contains(marketName)) {
                repository.removeMarket(marketName);
                this.logger.info("<cleanup> removed " + marketName);
            }
        }
    }
}
