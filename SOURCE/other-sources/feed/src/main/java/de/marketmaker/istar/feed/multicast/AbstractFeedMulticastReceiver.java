/*
 * AbstractFeedMulticastReceiver.java
 *
 * Created on 20.11.14 13:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.annotations.MonitorTags;
import com.netflix.servo.tag.BasicTagList;
import com.netflix.servo.tag.TagList;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import de.marketmaker.istar.common.nioframework.AbstractReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.CallbackErrorHandler;
import de.marketmaker.istar.common.nioframework.Connector;
import de.marketmaker.istar.common.nioframework.ConnectorListener;
import de.marketmaker.istar.common.nioframework.ReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.SelectorThread;
import de.marketmaker.istar.common.spring.MidnightEvent;
import de.marketmaker.istar.common.util.Mementos;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.feed.connect.FeedStats;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import static com.netflix.servo.annotations.DataSourceType.GAUGE;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * A single-threaded multicast receiver driven by a
 * {@link de.marketmaker.istar.common.nioframework.SelectorThread}. Registers read interest for
 * datagrams on the multicast socket so that {@link #handleRead()} will be invoked whenever
 * a datagram is readily available. Invokes {@link #publish()} whenever data is available for
 * further processing; subclasses can find the data in the {@link #mcBuffer} ByteBuffer and may
 * also reassign that field to another object in that method.
 * <p>
 * Whenever a packet is missed, a tcp connection to the server is established and a request is
 * sent to resend packets starting from the missed one. As soon as the tcp packets have catched
 * up with the multicast packets, the tcp connection is closed.
 * </p>
 *
 * @author oflege
 */
public abstract class AbstractFeedMulticastReceiver implements InitializingBean, Lifecycle,
        CallbackErrorHandler, ReadWriteSelectorHandler, ConnectorListener,
        FeedStats.PacketSink, FeedStats.ByteSink, ApplicationListener<MidnightEvent> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Size of a packet that can be sent without fragmentation
     */
    protected final int multicastPacketSize;

    // tracks tcp connection times to be able to limit number of connection attempts to avoid thrashing
    private final long[] lastTcpConnectsAt = new long[16];

    /**
     * Buffer used for receiving tcp data; since we will have at most one <code>TcpSource</code>
     * running, a single buffer is all we need.
     */
    private final ByteBuffer tcpBuffer
            = ByteBuffer.allocateDirect(1 << 18).order(LITTLE_ENDIAN);

    private SelectorThread selectorThread;

    private DatagramChannel channel;

    private NetworkInterface ni;

    private int port;

    private Integer retransmitPort = null;

    private InetAddress group;

    protected int receiveBufferSize = 0;

    @Monitor(type = COUNTER)
    private final AtomicLong numBytesReceived = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicLong numPacketsReceived = new AtomicLong();

    @Monitor(type = COUNTER)
    private final AtomicLong numPacketsMissed = new AtomicLong();

    @Monitor(type = GAUGE)
    private final AtomicInteger numPacketsMissedToday = new AtomicInteger();

    @Monitor(type = GAUGE)
    private final AtomicInteger numPacketsMissedYesterday = new AtomicInteger();

    private final Mementos.Long lastNumBytesReceived = new Mementos.Long();

    private final AtomicLong lastMissAt = new AtomicLong();

    private final AtomicLong numMulticastReads = new AtomicLong();

    private final AtomicInteger numTcpConnects = new AtomicInteger();

    private SocketAddress sourceAddress;

    private TcpSource tcpSource;

    private volatile int maxNumPacketsToRequestByTcp = 0;

    private volatile boolean joined;

    private int lastTcpConnectIdx = 0;

    /**
     * Whenever this is true, received multicast packets will be ignored except for their sequence id
     * which will be used to determine when the tcp data catched up with the multicasted data.
     */
    private boolean readFromTcp;

    /**
     * buffer obtained from the ring buffer for the current <code>seq</code>, used to receive
     * multicast packets. Needs to be initialized by a subclass before {@link #start()} is called.
     */
    protected ByteBuffer mcBuffer;

    /**
     * expected id for next packet received by multicast
     */
    private long expectedPacketId = 0;

    /**
     * expected id for next packet received over tcp
     */
    private long tcpPacketId;

    /**
     * we should never receive a lower package id from the same source from which we already
     * received a higher id; if it does happen for whatever reason, we need to log the fact, but
     * have to limit the number of log statements
     */
    private int numTimesLoggedReceivedLowerThanExpected;

    public AbstractFeedMulticastReceiver(int multicastPacketSize) {
        this.multicastPacketSize = multicastPacketSize;
    }

    @MonitorTags
    private TagList tags() {
        return BasicTagList.of("port", Integer.toString(this.port));
    }

    @Override
    public void onApplicationEvent(MidnightEvent event) {
        this.numPacketsMissedYesterday.set(this.numPacketsMissedToday.getAndSet(0));
    }

    protected ByteBuffer createBuffer(boolean direct) {
        final ByteBuffer result = direct
                ? ByteBuffer.allocateDirect(this.multicastPacketSize)
                : ByteBuffer.allocate(this.multicastPacketSize);
        result.order(LITTLE_ENDIAN);
        return result;
    }

    public void setNetworkInterface(String name) throws SocketException {
        this.ni = NetworkInterface.getByName(name);
        if (this.ni == null) {
            throw new IllegalArgumentException("no such interface '" + name + "'");
        }
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRetransmitPort(int port) {
        this.retransmitPort = port;
    }

    public void setGroup(String groupName) throws UnknownHostException {
        this.group = InetAddress.getByName(groupName);
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    @ManagedOperation
    public int getMaxNumPacketsToRequestByTcp() {
        return maxNumPacketsToRequestByTcp;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "num", description = "max num packets to re-request")
    })
    public void setMaxNumPacketsToRequestByTcp(int num) {
        this.maxNumPacketsToRequestByTcp = Math.max(0, num);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initNetwork();
    }

    void initNetwork() throws IOException {
        if (this.group == null) {
            throw new IllegalStateException("no group set");
        }
        if (this.port < 1 || this.port > 0xFFFF) {
            throw new IllegalStateException("invalid port: " + this.port);
        }
        if (this.retransmitPort == null) {
            this.retransmitPort = this.port + 1;
            this.logger.info("<initNetwork> setting retransmitPort to default " + this.retransmitPort);
        }
        this.channel = DatagramChannel.open(StandardProtocolFamily.INET);
        this.channel.configureBlocking(false);
        this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni)
                .setOption(StandardSocketOptions.SO_RCVBUF, this.receiveBufferSize)
                .bind(new InetSocketAddress(this.port));
    }

    @Override
    public void start() {
        if (this.mcBuffer == null) {
            throw new IllegalStateException("mcBuffer is null");
        }
        try {
            this.selectorThread.invokeAndWait(this::join);
        } catch (InterruptedException e) {
            throw new IllegalStateException("join failed");
        }
    }

    @Override
    public void stop() {
        // empty
    }

    @Override
    public boolean isRunning() {
        return this.joined;
    }

    protected void join() {
        try {
            this.logger.info("<join> group = " + group + ":" + this.port + ", ni = " + ni);

            this.channel.join(group, ni);
            ackReceiveBufferSize(this.channel.socket().getReceiveBufferSize());
            selectorThread.registerChannelNow(channel, SelectionKey.OP_READ, this);
            this.joined = true;
        } catch (Exception e) {
            this.logger.error("<join> failed", e);
        }
    }

    private void ackReceiveBufferSize(int size) {
        if (size < this.receiveBufferSize) {
            this.logger.warn("<ackReceiveBufferSize> requested size " + this.receiveBufferSize
                    + " < actual size: " + size);
        }
    }

    private boolean tcpRead() {
        this.numBytesReceived.addAndGet(this.tcpBuffer.remaining());

        int limit = tcpBuffer.limit();
        this.tcpBuffer.mark();
        while (tcpBuffer.remaining() > 1) {
            int length = tcpBuffer.getShort() & 0xFFFF;
            if (length == FeedTcpResender.END_OF_STREAM) { // server asks to close the connection
                ackMissed(this.tcpPacketId, this.expectedPacketId);
                return true;
            }
            int packetLength = length - 2;
            if (this.tcpBuffer.remaining() < packetLength) {
                this.tcpBuffer.reset();
                break;
            }
            int packetEnd = this.tcpBuffer.position() + packetLength;
            this.tcpBuffer.limit(packetEnd);
            mcBuffer.clear();
            mcBuffer.put(this.tcpBuffer).flip();
            long seqId = mcBuffer.getLong();
            if (seqId != tcpPacketId) {
                // the server was not able to resend everything we missed
                ackMissed(tcpPacketId, seqId);
            }
            this.tcpPacketId = seqId + 1;
            this.numPacketsReceived.incrementAndGet();
            publish();
            this.tcpBuffer.limit(limit).position(packetEnd).mark();
        }

        if (this.tcpBuffer.hasRemaining()) {
            this.tcpBuffer.compact();
        }
        else {
            this.tcpBuffer.clear();
        }

//        this.logger.info("<tcpRead> " + tcpPacketId + " vs " + this.expectedPacketId);

        // return true if the last packet received by tcp and multicast had the same id
        // so we can switch back to multicast processing
        return this.tcpPacketId == this.expectedPacketId;
    }

    public void handleRead() {
        this.numMulticastReads.incrementAndGet();
        try {
            doRead();
        } catch (IOException e) {
            logger.error("<handleRead> failed", e);
        } finally {
            renewMulticastReadInterest();
        }
    }

    void renewMulticastReadInterest() {
        try {
            if (!this.readFromTcp) {
                this.selectorThread.addChannelInterestNow(channel, SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            logger.error("<renewReadInterest> failed", e);
        }
    }

    private void doRead() throws IOException {
        SocketAddress from;
        while ((from = receive(this.mcBuffer)) != null) {
            this.mcBuffer.flip();
            this.numBytesReceived.addAndGet(this.mcBuffer.remaining());
            long pid = mcBuffer.getLong();

            if (pid != this.expectedPacketId && !handleUnexpected(pid, from)) {
                break;
            }

            this.numPacketsReceived.incrementAndGet();
            this.expectedPacketId = pid + 1;
            publish();
        }
    }

    private void closeTcpSource() {
        this.logger.info("<closeTcpSource> tcp=" + this.tcpPacketId + ", mc=" + this.expectedPacketId);
        this.readFromTcp = false;
        if (this.tcpSource != null) {
            this.tcpSource.close();
            this.tcpSource = null;
        }
        this.tcpPacketId = 0;
        renewMulticastReadInterest();
    }

    SocketAddress receive(ByteBuffer buffer) throws IOException {
        buffer.clear();
        return channel.receive(buffer);
    }

    private boolean handleUnexpected(long pid, SocketAddress address) {
        if (!Objects.equals(this.sourceAddress, address)) {
            onSourceChange(pid, address);
            return true;
        }

        if (pid < this.expectedPacketId) {
            if (this.numTimesLoggedReceivedLowerThanExpected++ < 100) {
                this.logger.error("<handleUnexpected> " + pid
                        + " < " + this.expectedPacketId + " from same source!?");
            }
            return false;
        }

        // pid > this.expectedPacketId
        if (!isGapRecoverableByTcp(pid)) {
            ackMissed(this.expectedPacketId, pid);
            this.expectedPacketId = pid + 1;
            return true;
        }

        this.logger.warn("<handleUnexpected> " + pid + " > " + this.expectedPacketId
            + " (" + (pid - this.expectedPacketId) + ")");
        this.readFromTcp = true;
        this.tcpPacketId = expectedPacketId;
        this.expectedPacketId = pid + 1;
        tcpConnect();
        return false;
    }

    private boolean isGapRecoverableByTcp(long pid) {
        if ((pid - this.expectedPacketId) > this.maxNumPacketsToRequestByTcp) {
            if (this.maxNumPacketsToRequestByTcp > 0) {
                this.logger.warn("<handleUnexpected> gap not recoverable: "
                        + (pid - this.expectedPacketId) + " > " + this.maxNumPacketsToRequestByTcp);
            }
            return false;
        }
        long now = System.currentTimeMillis();
        long latest = this.lastTcpConnectsAt[nextTcpConnectIdxBy()];
        if (now - latest < 10_000L) {
            this.logger.warn("<handleUnexpected> gap not recoverable: 16 tcp connects in last 10s");
            return false;
        }
        this.lastTcpConnectsAt[this.lastTcpConnectIdx] = now;
        this.lastTcpConnectIdx = nextTcpConnectIdxBy();
        this.logger.info("<isGapRecoverableByTcp> " + Arrays.toString(this.lastTcpConnectsAt));
        return true;
    }

    private int nextTcpConnectIdxBy() {
        return (this.lastTcpConnectIdx + 1) & 0xF;
    }

    private void onSourceChange(long pid, SocketAddress address) {
        if (this.sourceAddress != null) {
            this.logger.info("<onSourceChange> now receiving from " + address + " at " + pid);
        }
        else {
            this.logger.info("<onSourceChange> picking up from " + address + " at " + pid);
        }
        this.sourceAddress = address;
        this.expectedPacketId = pid;
    }

    /**
     * called whenever another multicast packet has been read and is available for publishing.
     * The data can be found in the <tt>mcBuffer</tt> field, its position will be 8 (just after
     * the sequence id) and its limit points to the end of the last available record.
     * Subclasses may reassign the mcBuffer variable in this method, as the original buffer
     * contents will not be accessed any more byt this class.
     */
    protected abstract void publish();

    @Override
    public void handleWrite() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleError(String message, Exception ex) {
        this.logger.error("<handleError> " + message, ex);
    }

    private void ackMissed(long from, long to) {
        long num = to - from;
        this.numPacketsMissed.addAndGet(num);
        this.lastMissAt.set(System.currentTimeMillis());
        if (num > 1) {
            this.logger.warn("<ackMissed> missed " + from + ".." + (to - 1) + " (" + num + ")");
        }
        else {
            this.logger.warn("<ackMissed> missed " + from + " (1)");
        }
    }

    @ManagedAttribute
    public long getNumMulticastReads() {
        return this.numMulticastReads.get();
    }

    @Override
    public long numPacketsReceived() {
        return this.numPacketsReceived.get();
    }

    @ManagedAttribute
    public int getNumPacketsMissedToday() {
        return numPacketsMissedToday.get();
    }

    @ManagedAttribute
    public int getNumPacketsMissedYesterday() {
        return numPacketsMissedYesterday.get();
    }

    @ManagedAttribute
    public long getNumPacketsReceived() {
        return this.numPacketsReceived.get();
    }

    @ManagedAttribute
    public long getNumPacketsMissed() {
        return this.numPacketsMissed.get();
    }

    @ManagedAttribute
    @Monitor(name = "lastMissAt")
    public String getLastMissAt() {
        long ms = this.lastMissAt.get();
        return (ms == 0) ? null : new DateTime(ms).toString();
    }

    @ManagedAttribute
    public int getNumTcpConnects() {
        return numTcpConnects.get();
    }

    @Override
    public long numBytesReceived() {
        return this.numBytesReceived.get();
    }

    @Override
    public void connectionEstablished(SocketChannel sc) throws IOException {
        this.tcpSource = new TcpSource(sc);
        this.logger.info("<handleConnection> " + sc.getLocalAddress() + " => " + sc.getRemoteAddress());
    }

    @Override
    public void connectionFailed(InetSocketAddress remoteAddress, IOException ioe) {
        if (ioe instanceof ConnectException) {
            this.logger.error("<connectionFailed> for " + remoteAddress + ": " + ioe.getMessage());
        }
        else {
            this.logger.error("<connectionFailed> for " + remoteAddress, ioe);
        }
        onTcpError();
    }

    void onTcpError() {
        ackMissed(this.tcpPacketId, this.expectedPacketId);
        closeTcpSource();
    }

    private void tcpConnect() {
        this.numTcpConnects.incrementAndGet();

        InetAddress address = ((InetSocketAddress) this.sourceAddress).getAddress();
        final InetSocketAddress isa = new InetSocketAddress(address, this.retransmitPort);
        this.logger.info("<tcpConnect> to " + isa);

        final Connector c = new Connector(this.selectorThread, isa, this);
        c.setReceiveBufferSize(1 << 21);
        try {
            c.connect();
        } catch (IOException e) {
            connectionFailed(isa, e);
        }
    }

    public void logStatus() {
        long received = getNumBytesReceivedSinceLastCall();
        this.logger.info("<status> " + this.group + ":" + this.port
                + " on " + this.ni.getDisplayName()
                + ": #r=" + NumberUtil.humanReadableByteCount(received));
    }

    private synchronized long getNumBytesReceivedSinceLastCall() {
        return this.lastNumBytesReceived.diffAndSet(this.numBytesReceived.get());
    }

    private class TcpSource extends AbstractReadWriteSelectorHandler {
        private final long createdMillis = System.currentTimeMillis();

        private final long from;

        private final ByteBuffer writeRequest;

        private int numBytesRead = 0;

        private TcpSource(SocketChannel sc) throws IOException {
            super(selectorThread, sc);
            this.from = tcpPacketId;
            this.writeRequest = (ByteBuffer) ByteBuffer.allocate(16)
                    .putLong(tcpPacketId)
                    .putLong(expectedPacketId - 1)
                    .flip();
            tcpBuffer.clear();
            registerChannel(true, true);
        }

        @Override
        public String toString() {
            return "TcpSource{age=" + (System.currentTimeMillis() - createdMillis) +
                    "ms, #packets=" + (tcpPacketId - this.from) +
                    ", #bytes=" + numBytesRead +
                    '}';
        }

        @Override
        protected boolean doRead(SocketChannel sc) throws IOException {
            sc.read(tcpBuffer);
            tcpBuffer.flip();
            this.numBytesRead += tcpBuffer.remaining();

            if (tcpRead()) {
                // we have catched up, close the tcp connection
                closeTcpSource();
                return false;
            }
            return true;
        }

        @Override
        protected boolean doWrite(SocketChannel sc) throws IOException {
            sc.write(this.writeRequest);
            return this.writeRequest.hasRemaining();
        }

        @Override
        protected void onError(IOException ex) {
            this.logger.error("<onError> tcp connection will be closed", ex);
            onTcpError();
        }

        @Override
        protected void close() {
            super.close();
        }
    }
}
