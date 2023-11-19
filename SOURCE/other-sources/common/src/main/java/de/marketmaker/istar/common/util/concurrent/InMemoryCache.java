/*
 * InMemorySoftReferenceCache.java
 *
 * Created on 11.03.2010 14:19:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
@ThreadSafe
public final class InMemoryCache<K, V> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ConcurrentMap<K, Future<V>> cache;

    private final CacheFeeder<K, V> cacheFeeder;

    public InMemoryCache(CacheFeeder<K, V> cacheFeeder) {
        this.cache = new ConcurrentHashMap<>();
        this.cacheFeeder = cacheFeeder;
    }

    public V get(final K key) {
        Future<V> fv = cache.get(key);

        if (null == fv) {
            FutureTask<V> ftv = new FutureTask<>(new Callable<V>() {
                public V call() throws Exception {
                    return cacheFeeder.feed(key);
                }
            });
            fv = cache.putIfAbsent(key, ftv);
            if (null == fv) {
                fv = ftv;
                ftv.run();
            }
        }

        V ret = null;
        try {
            ret = fv.get();
        } catch (InterruptedException e) {
            this.logger.error("<get> cannot retrieve value for: " + key, e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            this.logger.error("<get> cannot retrieve value for: " + key, e);
            throw new IllegalStateException("cannot retrieve value for: " + key, e);
        }

        return ret;
    }

    public void clear() {
        cache.clear();
    }

    public Future<V> remove(K key) {
        return cache.remove(key);
    }

    public static interface CacheFeeder<K, V> {
        V feed(K key) throws Exception;
    }
}
