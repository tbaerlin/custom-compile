/*
 * DefaultFeedConnector.java
 *
 * Created on 13.12.2004 11:13:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.netflix.servo.annotations.Monitor;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;
import org.springframework.core.Ordered;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import de.marketmaker.istar.common.statistics.HasStatistics;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * Connects to a tcp socket and forwards the contents (the feed) to a BufferWriter.
 * Two feed sources can be configured (primary and backup). As long as at least on source is
 * available, a connection to that source will be established and the content will be read.
 * Otherwise, this component sleeps some configurable time before retrying.<p>
 * Does <b>not</b> switch back to primary source automatically if connected to backup source and
 * primary source becomes available again. Administrator can switch input source using the
 * {@link FeedConnector#switchInputSource()} method.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class FeedConnector
        implements InitializingBean, Ordered, ApplicationContextAware, BeanNameAware,
        Lifecycle, Runnable, HasStatistics, FeedStats.ByteSink {
    /**
     * default time after which the input feed is considered dead when nothing was received
     */
    private static final int ASSUME_DEAD = 300;

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    private static final String NOT_AVAILABLE = "n/a";
    private static final String GAUGE = "feed_connector_gauge";

    /**
     * default time in sec to wait before trying to reconnect to a
     * previously unavailable feed.
     */
    private static final int RECONNECT_INTERVAL = 30;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * time to consider a stream dead
     */
    private int assumeDeadAfterSeconds = ASSUME_DEAD;

    private FeedInputSource backupInputSource;

    /**
     * Name of this bean, also used as name for this object's main thread.
     */
    private String beanName = ClassUtils.getShortName(getClass());

    /**
     * used instance to write received bytes to
     */
    private BufferWriter bufferWriter = null;

    /**
     * reference to the currently used input source
     */
    @GuardedBy("this")
    private FeedInputSource currentInputSource;

    /**
     * nio connector to input feed
     */
    private PortReader portReader;

    private FeedInputSource primaryInputSource;

    /**
     * size in bytes of the receive buffer of the socket receiving data
     */
    private int receiveBufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * time before trying to reconnect (again)
     */
    private volatile int reconnectIntervalSeconds = RECONNECT_INTERVAL;

    /**
     * flag to signal that this object has been stopped
     */
    private volatile boolean stopped = false;

    private volatile DateTime connectedSince = null;    

    private ScheduledExecutorService ses;

    private int order = Ordered.LOWEST_PRECEDENCE;

    private ApplicationContext applicationContext;

    private volatile boolean running;

    private MeterRegistry meterRegistry;

    /**
     * empty default constructor
     */
    public FeedConnector() {
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void afterPropertiesSet() throws Exception {
        this.portReader = new PortReader(this.bufferWriter, this.assumeDeadAfterSeconds,
                this.receiveBufferSize).withByteOrder(getByteOrder());

        this.ses = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, this.beanName));

        if (this.meterRegistry != null) {
            Gauge.builder(GAUGE, () -> this.portReader.getNumBytesProcessed())
                .tags(Tags.of("t", "num_bytes_processed")).register(this.meterRegistry);
        }
    }

    @ManagedAttribute
    public synchronized String getBackupSource() {
        return this.backupInputSource == null ? NOT_AVAILABLE : this.backupInputSource.toString();
    }

    @ManagedAttribute
    public synchronized String getCurrentSource() {
        return this.currentInputSource == null ? NOT_AVAILABLE : this.currentInputSource.toString();
    }

    @ManagedAttribute
    public synchronized String getPrimarySource() {
        return this.primaryInputSource.toString();
    }

    @ManagedOperation
    public void switchInputSource() {
        //noinspection StringEquality
        if (getBackupSource() == NOT_AVAILABLE) {
            return;
        }
        // on disconnect, the runMain method will try the next input source
        disconnectReader();
    }

    @ManagedAttribute
    public long getNumBytesProcessed() {
        return this.portReader.getNumBytesProcessed();
    }

    @Override
    @Monitor(type = COUNTER)
    public long numBytesReceived() {
        return this.portReader.getNumBytesProcessed();
    }

    public void resetStatistics() {
        this.portReader.resetNumBytesProcessed();
    }

    public void run() {
        runMain();
    }

    public void setAssumeDeadAfterSeconds(int assumeDeadAfterSeconds) {
        this.assumeDeadAfterSeconds = assumeDeadAfterSeconds;
    }

    public void setBackupInputSource(FeedInputSource backupInputSource) {
        this.backupInputSource = backupInputSource;
        this.logger.info("<setBackupInputSource> " + backupInputSource);
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setBufferWriter(BufferWriter bufferWriter) {
        this.bufferWriter = bufferWriter;
    }

    public void setPrimaryInputSource(FeedInputSource primaryInputSource) {
        this.primaryInputSource = primaryInputSource;
        this.logger.info("<setPrimaryInputSource> " + primaryInputSource);
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public void setReconnectIntervalSeconds(int reconnectIntervalSeconds) {
        this.reconnectIntervalSeconds = reconnectIntervalSeconds;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        this.ses.schedule(this, 0, TimeUnit.SECONDS);

        this.logger.info("<start> created reader thread, finished start");
        this.running = true;
    }

    public void stop() {
        this.stopped = true;

        disconnectReader();
        this.ses.shutdownNow();
        this.logger.info("<stop>");
    }

    protected ByteOrder getByteOrder() {
        return ByteOrder.BIG_ENDIAN;
    }

    private void disconnectReader() {
        try {
            this.portReader.disconnect();
        } catch (InterruptedException e) {
            this.logger.error("<disconnectReader> interrupted?!");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * start reading from portreader and send switch sequence before sending first byte.
     * @param is the input source to read from
     */
    private void inToOut(FeedInputSource is) {
        publishEvent(true);
        try {
            this.portReader.run();
        } catch (FeedClosedException e) {
            this.logger.warn("<inToOut> stream was closed for " + is.toString());
        } catch (IOException e) {
            this.logger.warn("<inToOut> io exception while reading from stream " + is.toString(), e);
        } finally {
            publishEvent(false);
        }
    }

    private void publishEvent(final boolean connected) {
        if (this.applicationContext != null) {
            this.applicationContext.publishEvent(new FeedConnectionEvent(this, connected));
        }
    }

    private void runMain() {
        final int numSources = this.backupInputSource != null ? 2 : 1;
        int numFailedConnects = 0;
        boolean primary = true;

        while (!this.stopped && numFailedConnects < numSources) {
            final FeedInputSource is = primary ? this.primaryInputSource : this.backupInputSource;
            try {
                this.portReader.connect(is);
                setCurrentInputSource(is);
                this.connectedSince = new DateTime();
                inToOut(is);
            }
            catch (IOException e) {
                // this only happens if connect fails, inToOut does not throw anything
                this.logger.warn("<runMain> failed to connect to " + is, e);
            }
            catch (Throwable t) {
                this.logger.error("<runMain> unexpected failure", t);
                if (this.portReader.getNumBytesProcessed() == 0L && numSources == 1) {
                    break;
                }
            }
            finally {
                setCurrentInputSource(null);
                this.connectedSince = null;
                if (this.portReader.getNumBytesProcessed() == 0L) {
                    numFailedConnects++;
                }
                else {
                    numFailedConnects = 0;
                }
            }
            primary = (numSources == 1) || !primary;
        }

        if (!this.stopped) {
            // we get here if numFailedConnects == numSources, so we have to wait before retrying
            this.logger.error("no feeds available => sleeping "
                    + this.reconnectIntervalSeconds + " seconds before retrying");
            this.ses.schedule(this, this.reconnectIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * set the input source to use
     * @param currentInputSource is to use
     */
    private synchronized void setCurrentInputSource(FeedInputSource currentInputSource) {
        this.currentInputSource = currentInputSource;
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<setCurrentInputSource> value: " + currentInputSource);
        }
    }

    public synchronized FeedInputSource getCurrentInputSource() {
        return this.currentInputSource;
    }

    public DateTime getConnectedSince() {
        return this.connectedSince;
    }

    public FeedInputSource getBackupInputSource() {
        return this.backupInputSource;
    }

    public FeedInputSource getPrimaryInputSource() {
        return this.primaryInputSource;
    }
}
