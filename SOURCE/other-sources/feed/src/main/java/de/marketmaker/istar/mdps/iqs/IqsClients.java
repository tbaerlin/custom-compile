/*
 * IqsClients.java
 *
 * Created on 16.10.13 13:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import de.marketmaker.istar.common.nioframework.AbstractReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.Acceptor;
import de.marketmaker.istar.common.nioframework.AcceptorListener;
import de.marketmaker.istar.common.util.ByteString;

/**
 * @author oflege
 */
public class IqsClients implements AcceptorListener, DisposableBean {

    private static final int EVICTION_TIMEOUT_SECS = 10;

    private static int nextPowerOf2(int i) {
        if (Integer.bitCount(i) == 1) {
            return i;
        }
        return Integer.highestOneBit(i) << 1;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReferenceArray<Client> clients;

    private final int[] counts;

    /**
     * we use direct buffers for large rings; if clients would frequently connect and disconnect
     * again, we might run out of space to allocate more buffers. To avoid that, buffers that are
     * no longer used are cached in this Deque and reused when the next client connects.
     */
    private final Deque<WeakReference<ByteBuffer>> bufferCache = new ConcurrentLinkedDeque<>();

    private final int mask;

    private final int shift;

    private int clientBufferSize = 1 << 24;

    private final IqsMessageParser parser = new IqsMessageParser();

    private Map<ByteString, ByteString> clientCredentials = new HashMap<>();

    private final Timer evictionTimer = new Timer("IqsClients.evictionTimer", true);

    public IqsClients(int maxNumClients, IqsMessageProcessor processor) {
        if (maxNumClients <= 0) {
            throw new IllegalArgumentException(maxNumClients + "<= 0");
        }
        final int numClients = nextPowerOf2(maxNumClients);

        this.clients = new AtomicReferenceArray<>(numClients);
        this.counts = new int[numClients];
        this.counts[0] = 1;
        this.mask = numClients - 1;
        this.shift = 31 - Integer.numberOfLeadingZeros(numClients);

        this.parser.setProcessor(processor);
    }

    public void setClientCredentials(Map<String, String> clientCredentials) {
        for (Map.Entry<String, String> e : clientCredentials.entrySet()) {
            this.clientCredentials.put(new ByteString(e.getKey()), new ByteString(e.getValue()));
        }
        this.logger.info("<setClientCredentials> " + this.clientCredentials);
    }

    public void setClientBufferSize(int clientBufferSize) {
        if (clientBufferSize < 1 << 16 || clientBufferSize > 1 << 27) {
            throw new IllegalArgumentException();
        }
        this.clientBufferSize = nextPowerOf2(clientBufferSize);
        this.logger.info("<setClientBufferSize> " + this.clientBufferSize);
    }

    @Override
    public void socketConnected(Acceptor acceptor, SocketChannel sc) throws IOException {
        int idx = 0;
        int minCount = Integer.MAX_VALUE;
        for (int i = 0; i < counts.length; i++) {
            if (clients.get(i) == null && counts[i] < minCount) {
                minCount = counts[i];
                idx = i;
            }
        }
        final int id = ((++counts[idx]) << shift) + idx;

        final Client c = new Client(acceptor, sc, getOrCreateBuffer(), id);
        this.clients.set(c.getId() & mask, c);
        c.registerChannel(true, false);
        this.logger.info("<socketConnected> added " + c);

        this.evictionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                evictClientIfNotLoggedIn(id);
            }
        }, TimeUnit.SECONDS.toMillis(EVICTION_TIMEOUT_SECS));
    }

    private void evictClientIfNotLoggedIn(int id) {
        final Client client = getClient(id);
        if (client != null && !client.isLoggedIn()) {
            this.logger.warn("<evictClientIfNotLoggedIn> evicting client " + id);
            client.close();
        }
    }

    private ByteBuffer getOrCreateBuffer() {
        WeakReference<ByteBuffer> wr;
        while ((wr = bufferCache.poll()) != null) {
            final ByteBuffer result = wr.get();
            if (result != null) {
                return result;
            }
        }
        this.logger.info("<getOrCreateBuffer> create new buffer " + this.clientBufferSize);
        return ByteBuffer.allocateDirect(this.clientBufferSize);
    }

    private void destroy(Client c) {
        if (!this.clients.compareAndSet(c.getId() & mask, c, null)) {
            return;
        }
        this.bufferCache.add(new WeakReference<>(c.ring));
    }

    @Override
    public void destroy() throws Exception {
    }

    public Client getClient(int id) {
        final Client client = clients.get(id & mask);
        return (client != null && client.getId() == id) ? client : null;
    }

    /**
     * @author oflege
     */
    class Client extends AbstractReadWriteSelectorHandler {

        private volatile String login;

        private ByteBuffer ring;

        private final ByteBuffer in = ByteBuffer.allocate(1 << 16);

        private final ByteBuffer out = ByteBuffer.allocate(1 << 16);

        private final int id;

        private final long positionMask;

        private long readPos;

        private long writePos;

        private final Object outputMutex = new Object();

        public Client(Acceptor acceptor, SocketChannel sc, ByteBuffer ring, int id) {
            super(acceptor, sc);
            this.ring = ring;
            this.positionMask = ring.capacity() - 1;
            this.id = id;
        }

        boolean isLoggedIn() {
            return this.login != null;
        }

        @Override
        protected void close() {
            super.close();
            destroy(this);
        }

        @Override
        protected boolean doRead(SocketChannel sc) throws IOException {
            final int num = sc.read(this.in);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<doRead> read " + num + " bytes " + this.id);
            }
            if (num < 0) {
                close();
                return false;
            }
            if (num > 0) {
                this.in.flip();
                parser.process(this, this.in);
            }
            return true;
        }

        @Override
        protected boolean doWrite(SocketChannel sc) throws IOException {
            synchronized (this.outputMutex) {
                int from = asPosition(this.readPos);
                int to = asPosition(this.writePos);
                ring.clear().position(from).limit(to > from ? to : ring.capacity());
                final int num = sc.write(ring);
                this.readPos += num;
                return this.readPos < this.writePos;
            }
        }

        boolean appendMessage(ByteBuffer bb) {
            return appendMessage(new ByteBufferResponse(bb));
        }

        boolean appendMessage(Response response) {
            boolean mustEnableWriting;
            synchronized (outputMutex) {
                mustEnableWriting = this.readPos == this.writePos;
                final long newWritePos = this.writePos + response.size();
                if (newWritePos - this.readPos >= this.ring.capacity()) {
                    // TODO: record buffer overflow, missed update
                    return false;
                }
                int from = asPosition(this.writePos);
                int to = asPosition(newWritePos);
                ring.clear().position(from);
                if (to < from) {
                    appendByWrapAround(response);
                }
                else {
                    response.appendTo(this.ring);
                }
                assert this.ring.position() == to;
                this.writePos = newWritePos;
            }
            if (mustEnableWriting) {
                super.enableWriting();
            }
            return true;
        }

        private void appendByWrapAround(Response response) {
            this.out.clear();
            response.appendTo(this.out);
            this.out.flip();
            int limit = out.limit();
            this.out.limit(this.ring.remaining());
            this.ring.put(this.out);
            this.out.limit(limit);
            this.ring.position(0);
            this.ring.put(out);
        }

        private int asPosition(long p) {
            return (int) (p & positionMask);
        }

        boolean checkAuthentication(ByteString user, ByteString password) {
            if (password.equals(clientCredentials.get(user))) {
                this.login = user.toString();
                return true;
            }
            return false;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "Client[" + this.id + ", login='" + this.login + "']";
        }
    }

    public static void main(String[] args) {
        final IqsClients cs = new IqsClients(16, null);
        cs.setClientBufferSize(7089790);
    }
}
