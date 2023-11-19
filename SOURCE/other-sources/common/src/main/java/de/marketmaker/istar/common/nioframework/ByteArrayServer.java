/*
 * AbstractByteArrayServer.java
 *
 * Created on 07.03.2006 08:24:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteBufferUtils;

/**
 * A simple server that sends byte-arrays to a number of clients.
 * Sending the byte arrays happens based on non-blocking IO and will run
 * in a {@link SelectorThread}'s thread. Subclasses are expected to call {@link #send(byte[])}
 * for all arrays to be sent. If writing data to a client fails, the client will be removed.
 * The server will make sure that a client will receive only complete byte arrays as
 * they have been submitted to the send method.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class ByteArrayServer implements DisposableBean, AcceptorListener {
    private static final boolean DEFAULT_COPY_DATA = true;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Before data is sent to a client, it will be stored in the client's write queue. If the
     * client's queue is full when new data is added, the new record will be discarded. Thus, this
     * queue should be large enough to buffer all incoming records between socket writes.
     */
    private int clientWriteQueueCapacity = 1024 * 8;

    /** state for all connected clients. */
    private List<Client> clients = new CopyOnWriteArrayList<>();

    private int acceptableConsecutiveDiscards = 10000;

    private boolean copyData = DEFAULT_COPY_DATA;

    public class Client extends AbstractReadWriteSelectorHandler implements ClientConnectionInfo {
        protected final DateTime connectedSince = new DateTime();

        /** number of byte arrays not sent because toWrite queue was full */
        private final AtomicLong numDiscarded = new AtomicLong();

        /** number of byte arrays sent */
        private final AtomicLong numSent = new AtomicLong();

        /** whether we have something to write */
        private final AtomicBoolean writeable = new AtomicBoolean(false);

        /** buffers incoming data between sends */
        private final Queue<ByteBuffer> toWrite =
                new ArrayBlockingQueue<>(clientWriteQueueCapacity);

        private final AtomicInteger numConsecutiveDiscarded = new AtomicInteger(0);

        /**
         * Wraps the byte[] that is currently being sent. Since we use non-blocking writes, only
         * a part of the data may be written, so we use a field to be able to continue sending
         * when doWrite is invoked the next time.<br>
         * No need to synchronize, as it will only be used from the {@link SelectorThread}.
         */
        private ByteBuffer current = null;

        protected Client(Acceptor acceptor, SocketChannel sc) {
            super(acceptor, sc);
        }

        protected Client(SelectorThread st, SocketChannel sc) {
            super(st, sc);
        }

        private void resetNumSent() {
            this.numSent.set(0);
        }

        private void resetNumDiscarded() {
            this.numDiscarded.set(0);
        }

        private void add(ByteBuffer bb) {
            if (this.toWrite.offer(bb)) {
                this.numConsecutiveDiscarded.set(0);
                if (writeable.compareAndSet(false, true)) {
                    // we have s.th. to write again, signal write interest
                    this.enableWriting();
                }
            }
            else {
                this.numDiscarded.addAndGet(bb.remaining());
                final int n = this.numConsecutiveDiscarded.incrementAndGet();
                if (n > ByteArrayServer.this.acceptableConsecutiveDiscards) {
                    this.logger.warn("<add> #" + ByteArrayServer.this.acceptableConsecutiveDiscards
                            + " missed in a row, closing " + this);
                    removeClient(this);
                    close();
                }
            }
        }

        protected boolean doRead(SocketChannel sc) throws IOException {
            throw new UnsupportedOperationException("should never be called");
        }

        private void updateCurrent() {
            if (current == null) {
                this.current = this.toWrite.poll();
            }
        }

        protected boolean doWrite(SocketChannel sc) throws IOException {
            final boolean moreToWrite = write(sc);
            this.writeable.set(moreToWrite);
            return moreToWrite;
        }

        private boolean write(SocketChannel sc) {
            updateCurrent();
            if (this.current == null) {
                // this should never be called when we have nothing to write, but nevermind...
                return false;
            }

            while (this.current != null) {
                final int numWritten;
                try {
                    // we do not use a gathering write with multiple ByteBuffers, as that
                    // causes a memory leak on solaris platforms (at least for jdk1.5.0)
                    numWritten = sc.write(this.current);
                } catch (IOException e) {
                    handleDisconnect(e);
                    return false;
                }

                this.numSent.addAndGet(numWritten);

                if (this.current.hasRemaining()) {
                    return true;
                }

                this.current = null;
                updateCurrent();
            }

            return false;
        }

        private void handleDisconnect(IOException ex) {
            this.logger.warn("<handleDisconnect> probably client disconnected: " + ex.getMessage());
            removeClient(this);
            close();
        }

        protected void onError(IOException ex) {
            removeClient(this);
            this.logger.info("<onError> removed " + this);
        }

        public String toString() {
            return "Client[" + getRemoteAddress()
                + ", connectedSince=" + ISODateTimeFormat.dateTimeNoMillis().print(this.connectedSince)
                + ", #bytes sent=" + this.numSent
                + ", #bytes dropped=" + this.numDiscarded
                + ", #pending=" + this.toWrite.size()
                + "]";
        }

        public DateTime getConnectedSince() {
            return this.connectedSince;
        }

        public long getNumDiscarded() {
            return this.numDiscarded.get();
        }

        public long getNumSent() {
            return this.numSent.get();
        }
    }

    public void destroy() throws Exception {
        for (Client client : this.clients) {
            client.close();
        }
    }

    /**
     * Set whether data submitted for sending will be copied first; copying has to be used
     * if the underlying data might change.
     * <p>Default is {@value #DEFAULT_COPY_DATA}
     * @param copyData whether data to be sent should be copied first
     */
    public void setCopyData(boolean copyData) {
        this.copyData = copyData;
        this.logger.info("<setCopyData> " + this.copyData);
    }


    public void setClientWriteQueueCapacity(int clientWriteQueueCapacity) {
        this.clientWriteQueueCapacity = clientWriteQueueCapacity;
        this.logger.info("<setClientWriteQueueCapacity> to " + this.clientWriteQueueCapacity);
    }

    @ManagedOperation
    public List<String> getClientInfoAsString() {
        final List<String> result = new ArrayList<>(this.clients.size());
        for (Client client : this.clients) {
            result.add(client.toString());
        }
        return result;
    }

    public List<? extends ClientConnectionInfo> getClientInfo() {
        return this.clients;
    }

    private void doSend(byte[] bytes) {
        for (Client client : this.clients) {
            client.add(ByteBuffer.wrap(bytes));
        }
    }

    /**
     * to be called by clients; enqueues <code>data</code> so that it will be sent to all
     * currently connected clients. If a client's write queue is full, the data will be
     * discarded for that client.
     * @param data to be sent to connected clients.
     */
    public void send(byte[] data) {
        if (this.clients.isEmpty()) {
            return;
        }
        doSend(this.copyData ? Arrays.copyOf(data, data.length) : data);
    }

    /**
     * to be called by clients; enqueues <code>bb</code> so that its remaining bytes will
     * be sent to all currently connected clients. If a client's write queue is full,
     * the data will be discarded for that client.
     * @param bb contains data to be sent to connected clients.
     */
    public void send(ByteBuffer bb) {
        if (this.clients.isEmpty()) {
            return;
        }
        if (this.copyData) {
            doSend(ByteBufferUtils.toByteArray(bb));
            return;
        }
        for (Client client : this.clients) {
            // do _not_ use bb directly, we need independent positions etc.
            client.add(bb.duplicate());
        }
    }

    /**
     * Adds new client based on a connection to some external server. To figure out whether the
     * external server closed the connection and a reconnect is required, the returned Client
     * object can be inspected using {@link Client#isClosed}
     */
    public Client addClient(SelectorThread st, SocketChannel sc) throws IOException {
        return addClient(new Client(st, sc));
    }

    /**
     * Creates a new client based on an accepted server connection, i.e., the client connected
     * to this server.
     */
    public Client addClient(Acceptor acceptor, SocketChannel sc) throws IOException {
        return addClient(new Client(acceptor, sc));
    }

    private Client addClient(Client cs) throws IOException {
        cs.registerChannel(false, false);
        this.clients.add(cs);
        this.logger.info("<addClient> number of clients: " + this.clients.size());
        return cs;
    }

    public void setAcceptableConsecutiveDiscards(int acceptableConsecutiveDiscards) {
        this.acceptableConsecutiveDiscards = acceptableConsecutiveDiscards;
        this.logger.info("<setAcceptableConsecutiveDiscards> " + this.acceptableConsecutiveDiscards);
    }

    private void removeClient(Client cs) {
        this.clients.remove(cs);
        this.logger.info("<removeClient> number of clients: " + this.clients.size());
    }

    public void socketConnected(Acceptor acceptor, SocketChannel sc) throws IOException {
        addClient(acceptor, sc);
    }
}
