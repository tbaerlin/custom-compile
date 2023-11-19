/*
 * TcpResender.java
 *
 * Created on 29.08.14 09:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.multicast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.nioframework.AbstractReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.Acceptor;
import de.marketmaker.istar.common.nioframework.AcceptorListener;
import de.marketmaker.istar.common.nioframework.ReadWriteSelectorHandler;
import de.marketmaker.istar.common.nioframework.SelectorThread;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * Tcp based server that uses a {@link FeedMulticastStore} to
 * obtain data for packets that clients somehow missed and sends that data to clients.<p>
 * Driven by a {@link de.marketmaker.istar.common.nioframework.SelectorThread}, so everything
 * is single-threaded and no synchronization is needed.
 *
 * @author oflege
 */
@ManagedResource
public class FeedTcpResender implements AcceptorListener, InitializingBean {

    static final int END_OF_STREAM = 0;

    /**
     * A client is expected to first send two <code>long</code> values that represent the first
     * and last packet sequence number it needs to be retransmitted.
     * This component will then try to send those packets to the client.
     */
    private class Client extends AbstractReadWriteSelectorHandler {
        private final long connectedAtMillis = System.currentTimeMillis();

        private ByteBuffer writeBuffer = ByteBuffer.allocate(1 << 19);

        private ByteBuffer request = ByteBuffer.allocate(16);

        private long seqFrom;

        private long seqTo;

        private int numBytesSent;

        private int numPacketsSent;

        private int numPacketsSentTmp;

        private Client(Acceptor acceptor, SocketChannel sc) throws IOException {
            super(acceptor, sc);
            // register read interest as we have to read the start sequence id
            registerChannel(true, false);
        }

        @Override
        public String toString() {
            return String.valueOf(getRemoteAddress());
        }

        @Override
        protected boolean doRead(SocketChannel sc) throws IOException {
            sc.read(this.request);
            if (this.request.hasRemaining()) {
                return true;
            }
            this.request.flip();
            this.seqFrom = request.getLong();
            this.seqTo = request.getLong();

            this.logger.info("<doRead> " + this + " requests data from seq=" + seqFrom + ".." + seqTo);
            fillWriteBuffer();

            return false;
        }

        private void fillWriteBuffer() throws IOException {
            this.writeBuffer.clear();
            while (true) {
                long lastSeq = seqFrom;
                this.seqFrom = store.copyFrom(this.seqFrom, this.seqTo, this.writeBuffer);
                if (writeBuffer.hasRemaining()) {
//                    this.logger.info("<fillWriteBuffer> " + this + " seq=" + seq + " " + writeBuffer);
                    this.numPacketsSentTmp = numPacketsSent + (int)(seqFrom - lastSeq);
                    enableWritingNow();
                    return;
                }
                if (seqFrom == lastSeq) { // no new data
//                    this.logger.info("<fillWriteBuffer> " + this + " no new data, seq=" + seq);
                    return;
                }
                if (this.seqFrom > this.seqTo) {
                    this.logger.warn("<fillWriteBuffer> " + this + " missed " + lastSeq + ".." + seqTo);
                    this.writeBuffer.clear();
                    this.writeBuffer.putShort((short) END_OF_STREAM).flip();
                    enableWritingNow();
                    return;
                }
                this.logger.warn("<fillWriteBuffer> " + this + " missed " + lastSeq + ".." + (seqFrom - 1));
            }
        }

        @Override
        protected boolean doWrite(SocketChannel sc) throws IOException {
            try {
                this.numBytesSent += sc.write(this.writeBuffer);
            } catch (IOException e) {
                handleDisconnect(e);
                return false;
            }
            if (this.writeBuffer.hasRemaining()) {
                return true;
            }
            this.numPacketsSent = this.numPacketsSentTmp;
            if (this.seqFrom > this.seqTo) {
                this.logger.info("<doWrite> finished for " + this);
                dispose();
                return false;
            }
            fillWriteBuffer();
            return false; // fillWriteBuffer would have re-enabled write interest, so false is ok.
        }

        private void handleDisconnect(IOException ex) {
            this.logger.warn("<handleDisonnect> " + this + ": " + ex.getMessage());
            dispose();
        }

        @Override
        protected void onError(IOException ex) {
            this.logger.error("<onError> " + this, ex);
            dispose();
        }

        private void dispose() {
            removeClient(this);
            close();
        }

        @Override
        protected void close() {
            super.close();
            this.logger.info("<close> " + getRemoteAddress()
                    + ", #bytes=" + this.numBytesSent
                    + ", #packets=" + this.numPacketsSent
                + ", connected for " + (System.currentTimeMillis() - this.connectedAtMillis) + "ms");
        }

        public boolean onMoreData(long id) {
            if (!this.writeBuffer.hasRemaining() && this.seqFrom <= id) {
                try {
                    fillWriteBuffer();
                } catch (IOException e) {
                    onError(e);
                    return false;
                }
            }
            return true;
        }
    }

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private final ByteBuffer seqBuffer = ByteBuffer.allocateDirect(1 << 12).order(LITTLE_ENDIAN);

    private List<Client> clients = new ArrayList<>();

    private SelectorThread selectorThread;

    private FeedMulticastStore store;

    /**
     * source end of a pipe written to by <code>store</code>, by which we are informed about
     * new packets that can be sent to connected clients.
     */
    private Pipe.SourceChannel source;

    public void setSelectorThread(SelectorThread selectorThread) {
        this.selectorThread = selectorThread;
    }

    public void setStore(FeedMulticastStore store) {
        this.store = store;
    }

    private boolean isExceeded(int limit, int value) {
        return limit > 0 && value > limit;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.source = this.store.getSource();

        this.selectorThread.registerChannelNow(this.source, this.source.validOps(),
                new ReadWriteSelectorHandler() {
                    @Override
                    public void handleRead() {
                        readNewSequence();
                    }

                    @Override
                    public void handleWrite() {
                        // write is not a valid op of source, so we never get here
                    }
                });
    }

    @Override
    public void socketConnected(Acceptor acceptor, SocketChannel sc) throws IOException {
        addClient(new Client(acceptor, sc));
    }

    private void addClient(Client c) {
        if (this.clients.add(c)) {
            this.store.setDoNotifySink(true);
        }
    }

    private void removeClient(Client c) {
        this.clients.remove(c);
        if (this.clients.isEmpty()) {
            this.store.setDoNotifySink(false);
        }
    }

    private void readNewSequence() {
        try {
            this.selectorThread.addChannelInterestNow(this.source, this.source.validOps());
            this.source.read(this.seqBuffer);
        } catch (IOException e) {
            this.logger.error("<readNewSequence> failed", e);
            return;
        }

        this.seqBuffer.flip();
        if (this.seqBuffer.remaining() >= 8) {
            onMoreData(readMaxSeqId());
        }
        this.seqBuffer.compact();
    }

    private long readMaxSeqId() {
        // we may have read more than one id but are only interested in the last:
        final int numIds = this.seqBuffer.remaining() >> 3;
        this.seqBuffer.position((numIds - 1) << 3);
        return this.seqBuffer.getLong();
    }

    private void onMoreData(long id) {
        if (this.clients.isEmpty()) {
            return;
        }
        for (int i = 0; i < clients.size(); ) {
            if (clients.get(i).onMoreData(id)) {
                i++;
            }
        }

    }

}
