/*
 * InstrumentServerWarmup.java
 *
 * Created on 13.04.2010 15:36:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.instrument.search.InstrumentSearcher;
import de.marketmaker.istar.instrument.search.SearchRequest;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;

/**
 * An instrument searcher warm-up executes a series of configured queries against a given instrument
 * searcher to warm-up the background search engine.
 *
 * <p>
 * If a warm-up is required during instrument searcher update/switch, it is necessary to pause a while
 * between the warm-up queries, in order to let productive queries get a chance to be processed fairly,
 * since they consume the same set of processing resources.
 *
 * @author zzhao
 * @since 1.2
 */
public class InstrumentSearcherWarmUp {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private List<SearchRequest> requests;

    private boolean withPause = true;

    private int queryNumForPause = 50;

    private long pauseMs = 1000;

    /**
     * Sets pause milli-seconds.
     *
     * @param pauseMs milli-seconds. Default 1000.
     */
    public void setPauseMs(long pauseMs) {
        this.pauseMs = pauseMs;
    }

    /**
     * Sets this warm-up in fair mode, i.e. there would be a pause between queries.
     *
     * @param withPause true with pause(fair mode), false without pause. Default true.
     */
    public void setWithPause(boolean withPause) {
        this.withPause = withPause;
    }

    /**
     * Sets the number of queries executed before a pause.
     *
     * @param queryNumForPause number of queries before a pause. Default 50.
     */
    public void setQueryNumForPause(int queryNumForPause) {
        this.queryNumForPause = queryNumForPause;
    }

    /**
     * Loads queries from the given query file.
     *
     * @param queryFile a file contains queries against an
     * {@link de.marketmaker.istar.instrument.search.InstrumentSearcher}
     * @throws java.io.IOException if IOException occurred during loading.
     */
    public void setQueryFile(File queryFile) throws IOException {
        final Properties prop = PropertiesLoader.load(queryFile);

        this.requests = new ArrayList<>();
        for (String key : prop.stringPropertyNames()) {
            for (String str : prop.getProperty(key).split(",")) {
                this.requests.add(createRequest(key + ":" + str));
            }
        }

        this.logger.info("<setQueryFile> created " + this.requests.size() + " requests");
    }

    private SearchRequestStringBased createRequest(String searchExpr) {
        final SearchRequestStringBased sr = new SearchRequestStringBased();
        sr.setCountInstrumentResults(true);
        sr.setUsePaging(true);
        sr.setPagingCount(10);
        sr.setPagingOffset(0);
        sr.setMaxNumResults(100);
        sr.setSearchExpression(searchExpr);
        return sr;
    }

    /**
     * Executes the series of configured queries on the given
     * {@link de.marketmaker.istar.instrument.search.InstrumentSearcher} to warm up its search engine.
     *
     * <p>
     * If this warm-up is in fair mode({@link #withPause} is true), there would be a pause of {@link #pauseMs}
     * milli-seconds between every {@link #queryNumForPause} queries.
     *
     * @param searcher an {@link de.marketmaker.istar.instrument.search.InstrumentSearcher}
     */
    public void warmUp(InstrumentSearcher searcher) {
        if (null == searcher) {
            throw new IllegalArgumentException("an instance of instrument searcher is required");
        }
        this.logger.info("<warmUp> starting instrument searcher warm up ...");
        TimeTaker tt = new TimeTaker();
        int n = 0;
        for (SearchRequest req : this.requests) {
            try {
                searcher.search(req);
            } catch (Exception e) {
                this.logger.error("<warmUp> search failed: " + req);
            }
            if (withPause) {
                if (++n == queryNumForPause) {
                    n = 0;
                    try {
                        Thread.sleep(pauseMs);
                    } catch (InterruptedException e) {
                        this.logger.error("<warmUp> warming up instrument searcher interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        this.logger.info("<warmUp> instrument searcher warm up finished in: " + tt);
    }

    public void warmUp(InstrumentServer server) {
        if (null == server) {
            throw new IllegalArgumentException("an instance of instrument server is required");
        }
        this.logger.info("<warmUp> starting instrument server warm up ...");
        TimeTaker tt = new TimeTaker();

        int n = 0;
        for (SearchRequest req : this.requests) {
            final long then = System.currentTimeMillis();
            try {
                server.searchNew(req);
            } catch (Exception e) {
                this.logger.error("<warmUp> search failed: " + req);
            }
            final long now = System.currentTimeMillis();
            if (this.withPause) {
                if (this.queryNumForPause == 0) {
                    sleep(Math.min(now - then, this.pauseMs));
                }
                else if (++n == this.queryNumForPause) {
                    n = 0;
                    sleep(this.pauseMs);
                }
            }
        }
        this.logger.info("<warmUp> instrument server warm up finished in: " + tt);
    }

    private void sleep(long sleepMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepMs);
        } catch (InterruptedException e) {
            this.logger.info("<sleep> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }
}
