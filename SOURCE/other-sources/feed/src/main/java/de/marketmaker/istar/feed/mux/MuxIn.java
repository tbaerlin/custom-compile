/*
 * MuxIn.java
 *
 * Created on 08.10.12 10:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.log.JmxLog;
import de.marketmaker.istar.common.nioframework.AbstractReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.CallbackErrorHandler;
import de.marketmaker.istar.common.nioframework.ConnectionHandler;
import de.marketmaker.istar.common.nioframework.Connector;
import de.marketmaker.istar.common.nioframework.DefaultConnectorListener;
import de.marketmaker.istar.common.nioframework.SelectorThread;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.feed.connect.FeedConnectionEvent;
import de.marketmaker.istar.feed.connect.FeedStats;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;

/**
 * Input side of the mux, reads data from a primary socket, or, if that is not available, from
 * a backup socket. Switching between those sockets will only happen if the current input socket
 * is closed or when {@link MuxControl#connectToPrimary()} or {@link MuxControl#connectToBackup()}
 * are called via jmx.
 * <p>
 * Uses a {@link MuxProtocolHandler} to ensure that only complete messages are forwarded to the
 * {@link MuxOut} delegate. 
 * </p>
 * <p>
 * Except for connecting to an input socket, all actions run in a single {@link SelectorThread}, so
 * there is no need to worry about synchronizing data reading and forwarding it to clients. Using
 * the <code>SelectorThread</code> also ensures that all I/O-opertations are non-blocking.
 * </p>
 * @author oflege
 */
@ManagedResource
public class MuxIn implements InitializingBean, DisposableBean, SmartLifecycle, ConnectionHandler,
        CallbackErrorHandler, SelectorThread.NopListener, FeedStats.ByteSink,
        ApplicationContextAware {

    private ApplicationContext applicationContext;

    private class Source extends AbstractReadWriteSelectorHandler {

        private final long connectedSince = System.currentTimeMillis();

        private final SocketAddress remoteAddress;

        private volatile long lastReadAt = 0;

        protected Source(SelectorThread selectorThread, SocketChannel sc) throws IOException {
            super(selectorThread, sc);
            this.remoteAddress = sc.getRemoteAddress();
            registerChannel(true, false); // we only want to read
        }

        @Override
        public String toString() {
            return "Source[" + this.remoteAddress + "]";
        }

        @Override
        protected boolean doRead(SocketChannel sc) throws IOException {
            final int read = sc.read(bb);
            if (read == -1) {
                logger.info("<doRead> channel reached end-of-stream, probably closed");
                closeAndScheduleReconnect(true);
                return false;
            }

            toOut();

            numBytesReceived.addAndGet(read);
            this.lastReadAt = System.currentTimeMillis();

            if (bb.remaining() == 0) {
                this.logger.error("<doRead> no space left in buffer");
                closeAndScheduleReconnect(true);
                return false;
            }

            return true;
        }

        @Override
        protected void onError(IOException ex) {
            this.logger.error("<onError> ", ex);
            connectPrimary = true;
            scheduleConnect(1);
        }

        private void closeAndScheduleReconnect(final boolean primary) {
            close();
            connectPrimary = primary;
            scheduleConnect(1);
        }

        @Override
        protected void close() {
            super.close();
            onInClosed();
        }

        @Override
        protected boolean doWrite(SocketChannel sc) throws IOException {
            throw new UnsupportedOperationException();
        }

        public void appendStatusTo(PrintWriter pw) {
            long at = this.lastReadAt;
            final long bytesRead = numBytesReceived.get();
            pw.printf("Connected to %s (%s)%n", this.remoteAddress,
                    isConnectedToPrimarySource() ? "Primary" : "Backup");
            pw.printf("since        %s%n", new DateTime(this.connectedSince));
            pw.printf("#bytes read  %d (%s)%n", bytesRead, NumberUtil.prettyPrint(bytesRead));
            pw.printf("last read at %s%n", (at > 0 ? new DateTime(at).toString() : "--"));
        }

        protected boolean isConnectedToPrimarySource() {
            return this.remoteAddress.equals(primarySourceAddress);
        }
    }

    private static final long DEFAULT_IDLE_TIMEOUT_MS = 120000L;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final SelectorThread selectorThread;

    /**
     * If no bytes have been read for this many milliseconds, the current connection will be
     * closed and a new connection to the primary source will be established. Default is
     * {@value #DEFAULT_IDLE_TIMEOUT_MS}.
     */
    private volatile long idleTimeoutMs = DEFAULT_IDLE_TIMEOUT_MS;

    /**
     * Provides a thread to which tasks are submitted that try to establish a connection
     * with an input socket. A task will be submitted whenever this service is not connected,
     * and a ScheduledExecutorService is used to easily allow for pauses between connection attempts.
     */
    private ScheduledExecutorService ses
            = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Mux-Connect"));

    private int srcReceiveBufferSize = 1 << 20;

    /**
     * size of the receiving buffer
     */
    private int messageBufferSize = 1 << 17;

    private InetSocketAddress primarySourceAddress;

    private InetSocketAddress backupSourceAddress;

    private volatile InetSocketAddress currentAddress = null;

    private volatile boolean connectPrimary = true;

    private volatile Source source;

    private MuxOutput out;

    @Monitor(type = COUNTER)
    private AtomicLong numBytesReceived = new AtomicLong();

    /**
     * for receiving data from the input socket
     */
    private ByteBuffer bb;

    private MuxProtocolHandler protocolHandler;

    private volatile boolean started;

    private boolean logOkOnSuccessfulWrite;

    private int waitForInitialConnectSecs = 0;

    private int phase = 10;

    private final Lock lock = new ReentrantLock();

    private final Condition connected = this.lock.newCondition();

    public MuxIn(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public void setPhase(int phase) {
        // the SelectorThread (as Startable) will be started in phase 0 and we require it to be
        // already running when we start, so a positive (i.e., later) phase is required
        if (phase <= 0) {
            throw new IllegalStateException("phase <= 0 :" + phase);
        }
        this.phase = phase;
    }

    public void setWaitForInitialConnectSecs(int waitForInitialConnectSecs) {
        this.waitForInitialConnectSecs = waitForInitialConnectSecs;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @ManagedAttribute
    public void setIdleTimeoutMs(long idleTimeoutMs) {
        this.idleTimeoutMs = idleTimeoutMs;
    }

    @ManagedAttribute
    public long getIdleTimeoutMs() {
        return idleTimeoutMs;
    }

    @ManagedOperation
    public void connectToPrimary() {
        connectTo(true);
    }

    @ManagedOperation
    public void connectToBackup() {
        connectTo(false);
    }

    private void publishEvent(final boolean connected) {
        if (this.applicationContext != null) {
            this.applicationContext.publishEvent(new FeedConnectionEvent(this, connected));
        }
    }

    private void connectTo(boolean primary) {
        final Source tmp = this.source;
        if (tmp == null) {
            this.logger.info("<connectTo> not connected");
            return;
        }
        InetSocketAddress address = getAddress(primary);
        if (address == null) {
            this.logger.warn("<connectTo> no address defined with primary=" + primary);
            return;
        }
        if (address == currentAddress) {
            this.logger.info("<connectTo> already connected to " + address);
            return;
        }
        tmp.closeAndScheduleReconnect(primary);
    }

    private InetSocketAddress getAddress(boolean primary) {
        return (primary || this.backupSourceAddress == null)
                ? this.primarySourceAddress : this.backupSourceAddress;
    }

    private void toOut() throws IOException {
        this.bb.flip();

        if (this.out.isAppendOnlyCompleteRecords()) {
            appendCompleteRecords();
        }
        else {
            this.out.append(this.bb);
        }
        if (this.logOkOnSuccessfulWrite && this.bb.position() > 0) {
            this.logOkOnSuccessfulWrite = false;
            // tell nagios that previous connection errors can be ignored now that we are connected (again)
            this.logger.info(JmxLog.OK_DEFAULT);
        }

        this.bb.compact();
    }

    private void appendCompleteRecords() throws IOException {
        final int endOfLastCompleteRecord
                = this.protocolHandler.getEndOfLastCompleteRecord(this.bb);
        if (endOfLastCompleteRecord > 0) {
            final int limit = this.bb.limit();
            this.bb.limit(endOfLastCompleteRecord);
            this.out.append(this.bb);
            this.bb.position(endOfLastCompleteRecord).limit(limit);
        }
    }

    @Required
    public void setOut(MuxOutput out) {
        this.out = out;
    }

    public void setMessageBufferSize(int messageBufferSize) {
        this.messageBufferSize = messageBufferSize;
    }

    public void setSrcReceiveBufferSize(int srcReceiveBufferSize) {
        this.srcReceiveBufferSize = srcReceiveBufferSize;
    }

    @Required
    public void setProtocolHandler(MuxProtocolHandler protocolHandler) {
        this.protocolHandler = protocolHandler;
    }

    @Required
    public void setPrimarySourceAddress(String primarySourceAddress) {
        this.primarySourceAddress = asAddress(primarySourceAddress);
    }

    public void setBackupSourceAddress(String backupSourceAddress) {
        this.backupSourceAddress = asAddress(backupSourceAddress);
    }

    private InetSocketAddress asAddress(String s) {
        final String[] terms = s.split(":");
        return new InetSocketAddress(terms[0], Integer.parseInt(terms[1]));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.selectorThread.addListener(this);
        this.bb = ByteBuffer.allocate(this.messageBufferSize);
        this.bb.order(this.protocolHandler.getByteOrder());
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void stop() {
        this.started = false;
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public void start() {
        scheduleConnect(0);
        this.started = true;
        if (this.waitForInitialConnectSecs > 0) {
            try {
                waitForConnect(this.waitForInitialConnectSecs, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void waitForConnect(long time, TimeUnit timeUnit) throws TimeoutException, InterruptedException {
        final long deadline = System.currentTimeMillis() + timeUnit.toMillis(time);
        this.lock.lock();
        try {
            while (this.source == null) {
                final long ms = deadline - System.currentTimeMillis();
                if (ms <= 0 || !this.connected.await(ms, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException("no connect within " + time + " " + timeUnit);
                }
            }
        } finally {
            this.lock.unlock();
        }
    }


    @Override
    public boolean isRunning() {
        return this.started;
    }

    @Override
    public void destroy() throws Exception {
        this.ses.shutdownNow();
        this.selectorThread.removeListener(this);
    }

    @Override
    public void ackNop() {
        final Source tmp = this.source;
        if (tmp != null) {
            long then = tmp.lastReadAt;
            if (then == 0) {
                then = tmp.connectedSince;
            }
            final long idleMs = System.currentTimeMillis() - then;
            if (idleMs > this.idleTimeoutMs) {
                this.logger.error("<ackNop> idle for " + idleMs + "ms!? Switching source...");
                tmp.closeAndScheduleReconnect(!tmp.isConnectedToPrimarySource());
            }
        }
    }

    protected void onInClosed() {
        this.source = null;
        this.currentAddress = null;
        this.out.onInClosed();
        this.numBytesReceived.set(0);
        publishEvent(false);
    }

    private void scheduleConnect(final int delay) {
        this.ses.schedule(this::connect, delay, TimeUnit.SECONDS);
    }

    private void connect() {
        final InetSocketAddress isa = getAddress(this.connectPrimary);

        final DefaultConnectorListener dcl = new DefaultConnectorListener(this);

        final Connector c = new Connector(this.selectorThread, isa, dcl);
        if (this.srcReceiveBufferSize > 0) {
            c.setReceiveBufferSize(this.srcReceiveBufferSize);
        }

        try {
            c.connect();
            if (dcl.isSuccessfulConnect(5, TimeUnit.SECONDS)) {
                this.currentAddress = isa;
                publishEvent(true);
            }
            else {
                this.logger.info("<connect> no success for " + isa);
            }
        } catch (TimeoutException e) {
            c.cancel();
            this.logger.warn("<connect> timeout for " + isa);
        } catch (Throwable t) {
            this.logger.warn("<connect> failed for " + isa, t);
        } finally {
            if (this.currentAddress == null && selectorThread.isRunning()) {
                this.connectPrimary = !this.connectPrimary;
                scheduleConnect(1);
            }
        }
    }

    @Override
    public void handleError(String message, Exception ex) {
        this.logger.error("<handleError> " + message, ex);
        this.logger.error("<handleError> selectorThread.isRunning(): " + selectorThread.isRunning());
    }

    @Override
    public void handleConnection(SocketChannel sc) throws IOException {
        this.logger.info("<handleConnection> " + sc);
        this.logOkOnSuccessfulWrite = true;
        this.bb.clear();
        this.out.reset();

        this.lock.lock();
        try {
            this.source = new Source(getSelectorThread(), sc);
            this.connected.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public SelectorThread getSelectorThread() {
        return this.selectorThread;
    }

    @Override
    public long numBytesReceived() {
        return this.numBytesReceived.get();
    }

    public void appendStatusTo(PrintWriter pw) {
        pw.println("--IN--------");
        pw.append("Primary: ").println(this.primarySourceAddress);
        if (this.backupSourceAddress != null) {
            pw.append("Backup : ").println(this.backupSourceAddress);
        }
        pw.println();
        Source tmp = this.source;
        if (tmp != null) {
            tmp.appendStatusTo(pw);
        }
        else {
            pw.println("NOT CONNECTED");
        }
    }
}
