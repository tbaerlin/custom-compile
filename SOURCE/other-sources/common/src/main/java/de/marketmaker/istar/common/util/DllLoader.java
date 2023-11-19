/*
 * DllLoader.java
 *
 * Created on 17.03.2005 08:16:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads a dll using a daemon thread and keeps this thread alive forever (i.e.,
 * until the VM exits). If the thread would terminate before, COM would detect
 * that and inform the server (which is used by the client in the dll) about
 * the presumably no longer existent client. The server would then silently remove
 * the client, and as soon as another thread of the client would subsequently try
 * to use the server, the COM call would fail.
 * <b>Important<b> A single thread is used to load all dlls. If it fails to load one dll,
 * the thread will die and thus using any previously loaded dll is likely to fail.
 * @author Oliver Flege
 */
final public class DllLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static DllLoader INSTANCE = new DllLoader();

    private final AtomicInteger threadCount = new AtomicInteger();

    private ExecutorService loader = Executors.newSingleThreadExecutor(r -> {
        final Thread t = new Thread(r, "DllLoader-" + threadCount.incrementAndGet());
        t.setDaemon(true);
        return t;
    });

    private Set<String> loadedDlls = new HashSet<>();

    private DllLoader() {
    }

    /**
     * Loads the given dll (if not already loaded) in a separate thread that is guaranteed to
     * be alive forever as long is it is able to load any dll specified.
     * @param dllName dll to load
     * @throws Throwable thrown if loading the dll fails.
     */
    public static synchronized void load(final String dllName) throws Throwable {
        INSTANCE.doLoad(dllName);
    }

    private void doLoad(final String dllName) throws Throwable {
        if (this.loadedDlls.contains(dllName)) {
            return;
        }
        this.loadedDlls.add(dllName);

        final Future<?> result = this.loader.submit(() -> System.loadLibrary(dllName));
        try {
            result.get();
        } catch (ExecutionException e) {
            this.logger.error("<doLoad> failed for " + dllName, e);
            throw e.getCause();
        }
    }
}
