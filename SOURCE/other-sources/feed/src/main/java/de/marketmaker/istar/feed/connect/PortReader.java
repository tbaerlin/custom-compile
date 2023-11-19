/*
 * PortReader.java
 *
 * Created on 13.12.2004 10:50:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.log.JmxLog;
import de.marketmaker.istar.common.util.NioUtils;

/**
 * Reads data from an input source (socket) and forwards the result to a {@link BufferWriter}.
 * Reading is non-blocking.
 * @author Oliver Flege
 */
class PortReader {
    private static final int DEFAULT_BUFFERSIZE = 96 * 1024;

    /**
     * After this many seconds without returning any data an input source is assumed
     * to be dead
     */
    private final int assumeDeadAfterSeconds;

    private final ByteBuffer bb = ByteBuffer.allocate(DEFAULT_BUFFERSIZE);

    /**
     * whether this reader is currently connected to any input source
     */
    private boolean connected;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Object mutex = new Object();

    /**
     * statistic
     */
    private final AtomicLong numBytesProcessed = new AtomicLong();

    /**
     * size of the receive buffer of the socket we are reading from
     */
    private final int receiveBufferSize;

    /**
     * SocketChannel for connected input source
     */
    private SocketChannel sChannel;

    /**
     * for how many seconds nothing has been read from the channel
     */
    private final AtomicInteger secsNoRead = new AtomicInteger(0);

    /**
     * SelectionKey objects for connected input source
     */
    private SelectionKey selectionKey;

    private volatile boolean shouldDisconnect;

    /**
     * destination for data read
     */
    private final BufferWriter writeTo;

    private boolean logOkOnSuccessfulWrite;

    /**
     * Constructor.
     * @param writeTo destination for data read
     * @param assumeDeadAfterSeconds timeout for input sources without data
     * @param receiveBufferSize size of tcp receive buffer is bytes
     */
    PortReader(BufferWriter writeTo, int assumeDeadAfterSeconds, int receiveBufferSize) {
        this.writeTo = writeTo;
        this.assumeDeadAfterSeconds = assumeDeadAfterSeconds;
        this.receiveBufferSize = receiveBufferSize;
    }

    PortReader withByteOrder(ByteOrder byteOrder) {
        this.bb.order(byteOrder);
        return this;
    }
    
    /**
     * Connect to all the physical input sources defined by the parameter.
     * @param fis defines input sources for the reader.
     * @throws java.io.IOException if connecting fails.
     * @throws IllegalStateException if reader is still connected.
     */
    void connect(FeedInputSource fis) throws IOException {
        synchronized (this.mutex) {
            if (this.connected) {
                throw new IllegalStateException("connect while still connected");
            }

            this.numBytesProcessed.set(0);

            this.sChannel = NioUtils.createNBSocketChannel(fis.getAddress(), this.receiveBufferSize);
            if (this.receiveBufferSize > 0
                    && this.sChannel.socket().getReceiveBufferSize() != this.receiveBufferSize) {
                this.logger.warn("<connect> receiveBufferSize: requested " + this.receiveBufferSize
                    + ", actual " + this.sChannel.socket().getReceiveBufferSize());
            }

            this.logger.info("<connect> succeeded for " + fis.getAddress());
            this.logOkOnSuccessfulWrite = true;
            this.connected = true;
            this.shouldDisconnect = false;
            this.secsNoRead.set(0);
            this.bb.clear();
        }
    }

    /**
     * Asks to disconnect from the input source and waits until that has happened
     * @throws InterruptedException if waiting for disconnect is interrupted
     */
    void disconnect() throws InterruptedException {
        synchronized (this.mutex) {
            if (!this.connected) {
                return;
            }
            this.shouldDisconnect = true;
            this.logger.info("<disconnect> set connected to false, waiting...");
            final SocketChannel channelToDisconnect = this.sChannel;
            // the reconnect might happen before this thread runs again after being notified,
            // so testing for while(this.connected) could cause this thread to hang forever
            while (channelToDisconnect == this.sChannel) {
                this.mutex.wait(1000);
            }
            this.logger.info("<disconnect> finished");
        }
    }

    long getNumBytesProcessed() {
        return this.numBytesProcessed.get();
    }

    void resetNumBytesProcessed() {
        this.numBytesProcessed.set(0);
    }

    /**
     * Returns whether the input source of this reader is assumed to be dead.
     * @return true if input source is dead
     */
    boolean isAssumedDead() {
        return this.secsNoRead.get() > this.assumeDeadAfterSeconds;
    }

    /**
     * Read from connected input sources and forward data read to the writer until
     * either {@link #disconnect} is called or an error occurs.
     * @throws IOException if an error occurs.
     */
    void run() throws IOException {
        this.logger.info("<run> with " + Thread.currentThread().getName());

        Selector s = null;
        try {
            s = Selector.open();
            this.selectionKey = this.sChannel.register(s, SelectionKey.OP_READ);

            while (!this.shouldDisconnect) {
                s.select(1000);

                if (s.selectedKeys().contains(this.selectionKey) && this.selectionKey.isReadable()) {
                    readAndWrite();
//                        if (this.logger.isDebugEnabled()) {
//                            this.logger.debug("<run> read and sent.");
//                        }
                }
                else {
                    if (this.secsNoRead.incrementAndGet() > this.assumeDeadAfterSeconds) {
                        throw new IOException("record reader assumed dead");
                    }
                }
                // s.select does not clear the set so we do that here.
                s.selectedKeys().clear();
            }
        }
        finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    this.logger.warn("<run> failed to close selector", e);
                }
            }
            // handle exceptional disconnect
            closeSocketChannel();
            this.logger.info("<run> finished.");
            synchronized (this.mutex) {
                this.connected = false;
                this.mutex.notifyAll();
            }
        }
    }

    private void closeSocketChannel() {
        try {
            final String s = String.valueOf(this.sChannel.socket()); // null after close
            NioUtils.close(this.sChannel);
            this.logger.info("<close> " + s);
        }
        catch (IOException e) {
            this.logger.warn("<close> failed for " + this.sChannel.socket(), e);
        }
        finally {
            this.sChannel = null;
            this.selectionKey = null;
        }
    }

    /**
     * Reads bytes from a channel and forwards all of them for writing.
     * @throws IOException if reading fails
     */
    private void readAndWrite() throws IOException {
        final int numRead = this.sChannel.read(this.bb);
        if (numRead == 0) {
            return;
        }
        if (numRead == -1) {
            throw new FeedClosedException("no stream");
        }

        write();

        this.secsNoRead.set(0);
        this.numBytesProcessed.addAndGet(numRead);
    }

    private void write() throws IOException {
        this.bb.flip();
        if (this.writeTo != null) {
            int oldLimit = this.bb.limit();

            try {
                this.writeTo.write(this.bb);
                if (this.logOkOnSuccessfulWrite && this.bb.position() > 0) {
                    this.logger.info(JmxLog.OK_DEFAULT);
                    this.logOkOnSuccessfulWrite = false;
                }
            } catch (IOException | RuntimeException e) {
                // we don't know whether the data in bb caused the exception or s.th. else, and if
                // we read s.th. like the mdps-feed, chances are that we are in-between records
                // so the only chance to get going again is to throw the exception and re-establish
                // the connection to start over.
                this.bb.clear();
                throw e;
            }

            if (oldLimit == bb.position()) {
                this.bb.clear();
            }
            else {
                this.bb.limit(oldLimit);
                this.bb.compact();
            }
        }
    }

}
