/*
 * AdminSession.java
 *
 * Created on 24.06.2010 16:20:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.HexDump;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.NioUtils;

/**
 * A session that connects to the admin server, logs in, and then runs a command/response loop.
 * @author oflege
 */
class AdminSession implements Callable<Void> {
    interface Callback {
        void setMdpsProcessId(int id);
        void execute(AdminRequestContext context);
        void awaitReady() throws InterruptedException;
        boolean isStopped();
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Callback callback;

    private AtomicInteger expectedMessage = new AtomicInteger(2);

    private SocketChannel sc;

    private final InetSocketAddress address;

    private final AdminProtocolSupport protocolSupport;

    public AdminSession(InetSocketAddress address, AdminProtocolSupport protocolSupport,
            Callback callback) {
        this.address = address;
        this.protocolSupport = protocolSupport;
        this.callback = callback;
    }

    public Void call() throws Exception {
        try {
            this.logger.info("<call> connecting to " + this.address + "...");
            this.sc = NioUtils.createSocketChannel(this.address, 0);
            this.logger.info("<call> connected");
        } catch (IOException e) {
            this.logger.error("<call> failed", e);
            throw e;
        }


        try {
            write(this.protocolSupport.createLoginMessage());
            this.logger.info("<call> sent login");

            this.expectedMessage.set(2);
            handleMessage();

            this.expectedMessage.set(4);

            while (!this.callback.isStopped() && handleMessage()) {
                // empty
            }
        } finally {
            if (IoUtils.close(this.sc)) {
                this.logger.info("<call> closed admin socket");
            }
        }

        this.logger.info("<call> finished");
        return null;
    }

    private void write(ByteBuffer message) throws IOException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<write> " + "\n" + HexDump.toHex(toBytes(message)));
        }
        while (message.hasRemaining()) {
            this.sc.write(message);
        }
    }

    private boolean handleMessage() throws IOException {
        final ByteBuffer bb = this.protocolSupport.createBuffer(128);
        try {
            do {
                final int n = this.sc.read(bb);
                if (n == -1) {
                    this.logger.info("<handleMessage> channel was closed");
                    return false;
                }
            } while (!this.protocolSupport.isMessageComplete(bb));
            bb.flip();
            final ByteBuffer result = onMessage(bb);
            if (result != null) {
                write(result);
            }
            return true;
        } catch (ClosedByInterruptException e) {
            this.logger.info("<handleMessage> closed by interrupt, stopping");
            return false;
        } catch (IOException e) {
            this.logger.warn("<handleMessage> failed", e);
            return false;
        }
    }

    private ByteBuffer onMessage(ByteBuffer message) {
        final int mid = this.protocolSupport.getMessageId(message);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<onMessage> " + "\n" + HexDump.toHex(toBytes(message)));
        }

        if (this.expectedMessage.get() != mid) {
            this.logger.warn("<onMessage> got message type " + mid + ", expected "
                    + this.expectedMessage.get());
            return null;
        }
        switch (mid) {
            case 2:
                return onLoginAck(message);
            case 4:
                return onAdminRequest(message);
            default:
                this.logger.warn("<onMessage> unknown message id " + mid);
                return null;
        }
    }

    private byte[] toBytes(ByteBuffer message) {
        final byte[] data = new byte[message.remaining()];
        System.arraycopy(message.array(), 0, data, 0, message.remaining());
        return data;
    }

    private ByteBuffer onAdminRequest(ByteBuffer message) {
        final AdminRequestContext context = this.protocolSupport.getAdmRequestContext(message);
        this.callback.execute(context);
        return this.protocolSupport.createAdminResponseMessage(context);
    }

    private ByteBuffer onLoginAck(ByteBuffer message) {
        int id = this.protocolSupport.getMdpsProcessId(message);
        this.logger.info("<onLoginAck> id = " + id);
        this.callback.setMdpsProcessId(id);

        try {
            this.callback.awaitReady();
        } catch (InterruptedException e) {
            // should never happen
            this.logger.warn("<onLoginAck> Interrupted?!");
            Thread.currentThread().interrupt();
            return null;
        }
        this.logger.info("<onLoginAck> continue with process ready");
        return this.protocolSupport.createProcessReadyMessage();
    }

}
