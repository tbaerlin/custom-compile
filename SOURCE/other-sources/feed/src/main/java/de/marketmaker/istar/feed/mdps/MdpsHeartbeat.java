/*
 * MdpsHeartbeat.java
 *
 * Created on 01.12.2008 15:29:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;
import org.springframework.util.ClassUtils;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.nioframework.ByteArrayServer;
import de.marketmaker.istar.common.util.ByteUtil;
import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.HEADER_LENGTH;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Sends an mdps record at regular intervals. Heartbeat is useful to detect stale clients of a
 * ByteArrayServer. When a client disconnects, that server will only notice that when it tries to
 * send something to that client.
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsHeartbeat implements Lifecycle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ByteArrayServer server;

    private long heartbeatIntervalSeconds = 5;

    private byte[] key = ByteUtil.toBytes("HUHU.VWD,E");

    private int messageType = MdpsMessageTypes.UPDATE;

    private Timer t;

    private int protocolVersion = 1;

    public void setProtocolVersion(int protocolVersion) {
        if (protocolVersion != 1 && protocolVersion != 3) {
            throw new IllegalArgumentException("unsupported " + protocolVersion);
        }
        this.protocolVersion = protocolVersion;
        this.logger.info("<setProtocolVersion> = " + this.protocolVersion);
    }

    public void setHeartbeatIntervalSeconds(long heartbeatIntervalSeconds) {
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    public void setServer(ByteArrayServer server) {
        this.server = server;
    }

    public void setKey(String key) {
        this.key = ByteUtil.toBytes(key);
    }

    @Override
    public boolean isRunning() {
        return this.t != null;
    }

    public void start() {
        this.t = new Timer(ClassUtils.getShortName(getClass()), true);

        final long millis = TimeUnit.MILLISECONDS.convert(this.heartbeatIntervalSeconds, TimeUnit.SECONDS);

        this.t.schedule(new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        }, millis, millis);
    }

    public void stop() {
        this.t.cancel();
    }

    private void sendHeartbeat() {
        final ByteBuffer bb = ByteBuffer.wrap(new byte[100]).order(getByteOrder());
        bb.position(HEADER_LENGTH);
        bb.putShort((short) MdpsMessageConstants.MDPS_KEY_FID);
        bb.put((byte) (this.key.length));
        bb.put(this.key);

        final DateTime dt = new DateTime();

        bb.putShort((short) VwdFieldDescription.ADF_Datum.id());
        bb.putInt(MdpsFeedUtils.encodeDate(dt));

        bb.putShort((short) VwdFieldDescription.ADF_Zeit.id());
        bb.putInt(MdpsFeedUtils.encodeTime(dt.getSecondOfDay()));

        final int msgLength = bb.position();
        bb.putShort(0, (short) msgLength); // mdps length
        bb.put(2, (byte) this.messageType); // msg type
        bb.putShort(4, (short) (msgLength - HEADER_LENGTH)); // body length
        bb.flip();

        this.server.send(bb);
    }

    private ByteOrder getByteOrder() {
        return MdpsFeedUtils.getByteOrder(this.protocolVersion);
    }
}
