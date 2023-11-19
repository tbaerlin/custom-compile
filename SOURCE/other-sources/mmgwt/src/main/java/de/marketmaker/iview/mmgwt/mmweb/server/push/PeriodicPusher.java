/*
 * PricePusher.java
 *
 * Created on 10.02.2010 07:30:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.Lifecycle;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.snap.SnapData;

/**
 * Periodically collects Registration objects for which feed data updates are available,
 * creates push updates and tells each registration to send the updates to the clients that
 * are registered with the registration object.
 * @author oflege
 */
public class PeriodicPusher implements Lifecycle, Runnable {
    private final Log logger = LogFactory.getLog(getClass());

    private Registrations realtimeRegistrations;

    private Registrations neartimeRegistrations;

    private FeedDataRepository realtimeRepository;

    private FeedDataRepository neartimeRepository;

    private PushServiceImpl pushService;

    private final ScheduledExecutorService es = Executors.newScheduledThreadPool(1,
            new ThreadFactory() {
                public Thread newThread(Runnable r) {
                    return new Thread(r, ClassUtils.getShortName(PeriodicPusher.this.getClass()));
                }
            });

    private volatile boolean stopped = false;

    private boolean running;

    public void setRealtimeRepository(FeedDataRepository realtimeRepository) {
        this.realtimeRepository = realtimeRepository;
    }

    public void setNeartimeRepository(FeedDataRepository neartimeRepository) {
        this.neartimeRepository = neartimeRepository;
    }

    public void setRealtimeRegistrations(Registrations realtimeRegistrations) {
        this.realtimeRegistrations = realtimeRegistrations;
    }

    public void setNeartimeRegistrations(Registrations neartimeRegistrations) {
        this.neartimeRegistrations = neartimeRegistrations;
    }

    public void setPushService(PushServiceImpl pushService) {
        this.pushService = pushService;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        this.es.schedule(this, 1, TimeUnit.SECONDS);
        this.running = true;
        this.logger.info("<start> done");
    }

    public void stop() {
        this.stopped = true;
        this.es.shutdown();
        this.logger.info("<stop> done");
    }

    public void run() {
        if (this.stopped) {
            return;
        }
        final long then = System.currentTimeMillis();
        long took = 0;
        try {
            final int numUpdates = push();
            if (numUpdates > 0) {
                flush();
            }
            took = System.currentTimeMillis() - then;

            if (numUpdates > 0 && this.logger.isDebugEnabled()) {
                this.logger.debug("<run> finished in " + took + "ms, #updates=" + numUpdates);
            }
        } catch (Throwable t) {
            this.logger.error("<run> failed", t);
        } finally {
            if (!this.stopped) {
                this.es.schedule(this, Math.max(100, 1000 - took), TimeUnit.MILLISECONDS);
            }
        }
    }

    private int push() {
        return push(this.realtimeRegistrations, this.realtimeRepository)
                + push(this.neartimeRegistrations, this.neartimeRepository);
    }

    private void flush() {
        for (AbstractClient client : this.pushService.getClientsWithData()) {
            client.flush();
        }
    }

    private int push(Registrations registrations, FeedDataRepository repository) {
        final List<Registration> withUpdates = registrations.getWithUpdates();
        if (withUpdates.isEmpty()) {
            return 0;
        }
        for (final Registration r : withUpdates) {
            r.clearUpdated();
            final FeedData data = repository.get(r.getVwdcode());
            final SnapRecord record;
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (data) {
                if (!data.isReadyForPush()) {
                    // registered, but full snap not available yet: mark for next iteration & continue
                    // if no update received so far:
                    if (r.isFreshUpdate()) {
                        registrations.ackUpdateFor(r);
                    }
                    continue;
                }

                final SnapData snapData = data.getSnapData(true);
                record = snapData.toSnapRecord(0);
            }
            r.pushUpdate(record);
        }
        return withUpdates.size();
    }


}
