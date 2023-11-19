/*
 * IqsSymbolLookup.java
 *
 * Created on 21.10.13 13:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import de.marketmaker.istar.common.util.ByteString;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @author oflege
 */
@ManagedResource
public class IqsSymbolLookupClient implements DisposableBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String url;

    private final HttpClient client;

    private Map<ByteString, ByteString> cache = new LinkedHashMap<ByteString, ByteString>(1 << 10) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<ByteString, ByteString> eldest) {
            return size() > 512;
        }
    };

    private final ExecutorService es;

    private AtomicInteger numHits = new AtomicInteger();

    private AtomicInteger numMisses = new AtomicInteger();

    private AtomicInteger numRejected = new AtomicInteger();

    @ManagedAttribute
    public int getNumHits() {
        return numHits.get();
    }

    @ManagedAttribute
    public int getNumMisses() {
        return numMisses.get();
    }

    @ManagedAttribute
    public int getNumRejected() {
        return numRejected.get();
    }

    public IqsSymbolLookupClient(String urlStr, int backlog) throws MalformedURLException {
        this.url = urlStr;

        this.client = HttpClientBuilder.create()
            .setDefaultRequestConfig(RequestConfig.custom()
                .setConnectTimeout((int) TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS))
                .build())
            .build();

        this.es = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(backlog),
            new ThreadFactory() {
                private int i = 0;

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, IqsSymbolLookupClient.class.getSimpleName() + "-" + ++i);
                }
            });
    }

    @Override
    public void destroy() throws Exception {
        es.shutdown();
        if (!es.awaitTermination(10, TimeUnit.SECONDS)) {
            this.logger.error("<destroy> ExecutorService failed to stop");
        }
    }

    ByteString lookup(ByteString symbol) {
        ByteString result = this.cache.get(symbol);
        if (result != null) {
            this.numHits.incrementAndGet();
        }
        else {
            this.numMisses.incrementAndGet();
            result = doLookup(symbol);
            if (result != ByteString.NULL) {
                this.cache.put(symbol, result);
            }
        }
        return isDefined(result) ? result : null;
    }

    private boolean isDefined(ByteString result) {
        return (result != ByteString.EMPTY) && (result != ByteString.NULL);
    }


    private ByteString doLookup(ByteString symbol) {
        try {
            final HttpUriRequest getMethod = RequestBuilder.get(this.url)
                .addParameter("symbol", symbol.toString())
                .build();
            final HttpResponse resp = this.client.execute(getMethod);
            final int statusCode = resp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return new ByteString(EntityUtils.toString(resp.getEntity()));
            }
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                this.logger.info("<doLookup> no result for '" + symbol + "'");
                return ByteString.EMPTY;
            }
            this.logger.error("<doLookup> returned status " + statusCode + " for '" + symbol + "'");
        } catch (ConnectTimeoutException e) {
            this.logger.error("<doLookup> timeout for '" + symbol + "'");
        } catch (IOException e) {
            this.logger.error("<doLookup> failed for '" + symbol + "'", e);
        }
        return ByteString.NULL;
    }

    boolean submit(Runnable runnable) {
        try {
            es.submit(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            this.numRejected.incrementAndGet();
            return false;
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        final IqsSymbolLookupClient c = new IqsSymbolLookupClient("http://gis-test.vwd.com/dmxml-1/marketmanager/lookup.txt", 16);
        System.out.println(c.lookup(new ByteString("840400")));
        System.out.println(c.lookup(new ByteString("840400")));
    }
}
