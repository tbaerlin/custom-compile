/*
 * PushRegistry.java
 *
 * Created on 08.02.2010 09:01:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.api.FeedConnector;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.push.PushRegistry;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * Takes care of registering keys to be pushed in the FeedDataRepository (so that the parser
 * no longer discards them), requests the full snap data for registered keys from another service
 * and integrates that data with data in the repository.
 * <p>
 * Can also be used to start/stop a specified parser whenever the first key is registered or
 * the last key is removed, respectively.
 * 
 * @author oflege
 */
public class OrderedPushRegistry implements Lifecycle, PushRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<String> keys = new HashSet<>();

    private FeedDataRepository repository;

    private FeedConnector feedConnector;

    private boolean realtime;

    private AtomicInteger numRegistered = new AtomicInteger(0);

    private MulticastFeedParser parser;

    private final ExecutorService es = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(400),
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(r, "PushRegistry" + (realtime ? "-rt" : "-nt"));
                }
            });

    private boolean running;


    public void setParser(MulticastFeedParser parser) {
        this.parser = parser;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        this.running = true;
    }

    public void stop() {
        this.es.shutdown();
    }

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    public void setFeedConnector(FeedConnector feedConnector) {
        this.feedConnector = feedConnector;
    }

    public boolean register(String vendorkey) {
        final VendorkeyVwd vkey = VendorkeyVwd.getInstance(vendorkey);
        if (vkey == VendorkeyVwd.ERROR) {
            this.logger.warn("<register> ignoring invalid: " + vendorkey);
            return false;
        }
        final FeedData data = this.repository.get(vkey);
        if (data == null) {
            this.repository.register(vkey);
            synchronized (this) {
                this.keys.add(vendorkey);
            }
            resumeOnFirstRegistration();
        }
        return true;
    }

    public void completeRegistrations() {
        synchronized (this) {
            if (this.keys.isEmpty()) {
                return;
            }
            final RegistrationTask task = new RegistrationTask(this.keys);
            try {
                this.es.submit(task);
            } catch (RejectedExecutionException e) {
                this.logger.error("<register> rejected " + task);
            }
            this.keys = new HashSet<>();
        }
    }

    public void unregister(String vendorkey) {
        if (this.repository.removeFeedData(vendorkey)) {
            suspendAfterLastRegistrationRemoved();
        }
    }

    private void resumeOnFirstRegistration() {
        if (this.numRegistered.getAndIncrement() == 0) {
            this.logger.info("<resumeOnFirstRegistration>");
            this.parser.setDisabled(false);
        }
    }

    private void suspendAfterLastRegistrationRemoved() {
        if (this.numRegistered.decrementAndGet() == 0) {
            this.logger.info("<suspendAfterLastRegistrationRemoved>");
            this.parser.setDisabled(true);
        }
    }

    private class RegistrationTask implements Runnable {
        private final Set<String> vendorkeys;

        public RegistrationTask(Set<String> vendorkeys) {
            this.vendorkeys = vendorkeys;
        }

        @Override
        public String toString() {
            return this.vendorkeys + "/rt=" + realtime;
        }

        public void run() {
            try {
                complete();
            } catch (Throwable t) {
                logger.error("<complete> failed", t);
            }
        }

        private void complete() {
            final IntradayResponse response = requestSnaps();
            final FeedDataRepository repository = OrderedPushRegistry.this.repository;
            for (String vendorkey : this.vendorkeys) {
                final ByteString vwdcode
                        = new ByteString(vendorkey.substring(vendorkey.indexOf('.') + 1));
                final FeedData data = repository.get(vwdcode);
                final IntradayResponse.Item item = response.getItem(vendorkey);
                if (item == null) {
                    logger.warn("<getSnap> no snap for " + vendorkey);
                    continue;
                }
                final SnapRecord sr = item.getPriceSnapRecord();
                if (!(sr instanceof OrderedSnapRecord)) {
                    logger.warn("<invoke> not a OrderedSnapRecord: " + sr.getClass().getName());
                    continue;
                }
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (data) {
                    ((OrderedSnapData) data.getSnapData(true)).init((OrderedSnapRecord) sr);
                    data.setReadyForPush(true);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("<complete> " + this);
            }
        }

        private IntradayResponse requestSnaps() {
            final IntradayRequest request = new IntradayRequest();
            for (String vendorkey : this.vendorkeys) {
                request.add(new IntradayRequest.Item(vendorkey, realtime));
            }
            return feedConnector.getIntradayData(request);
        }
    }
}
