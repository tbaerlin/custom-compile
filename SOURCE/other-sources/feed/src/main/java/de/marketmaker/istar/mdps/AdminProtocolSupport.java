/*
 * AdmServerProtocolSupport.java
 *
 * Created on 18.06.2010 12:43:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.HexDump;

/**
 * @author oflege
 */
class AdminProtocolSupport {
    private static final int PROTOCOL_VERSION = Integer.getInteger("adminMdpsProtocolVersion", 3);

    private static final ByteOrder BYTE_ORDER = PROTOCOL_VERSION == 3
            ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;

    private static final Charset ASCII = Charset.forName("US-ASCII");

    private static final int MSG_HEADER_LEN = 4;

    private static final int NAME_LENGTH = 30;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private byte[] mdpsName;

    private byte[] localAddress;

    private byte[] localHost;

    AdminProtocolSupport(String mdpsName) throws UnknownHostException {
        this.mdpsName = toAscii(mdpsName, NAME_LENGTH);
        final String host = getHost();
        this.localAddress = toAscii(InetAddress.getByName(host).getHostAddress(), 16);
        this.localHost = toAscii(host, 32);
    }

    int getMessageId(ByteBuffer message) {
        return (int) message.get(3);
    }

    AdminRequestContext getAdmRequestContext(ByteBuffer message) {
        message.position(MSG_HEADER_LEN);
        final int userData = message.getInt();
        message.position(message.position() + NAME_LENGTH); // skip process name
        final String cmd = getString(message, 32);
        return new AdminRequestContext(userData, cmd, getCommandArgs(message));
    }

    private String[] getCommandArgs(ByteBuffer message) {
        final int paramLength = message.getShort() & 0xFFFF;
        if (paramLength > 0 && paramLength == message.remaining()) {
            return getString(message, paramLength).split("\\s+");
        }
        return new String[0];
    }

    int getMdpsProcessId(ByteBuffer message) {
        message.position(MSG_HEADER_LEN);
        final char ackFlag = (char) message.get();
        if (ackFlag == '2') {
            throw new IllegalStateException("Failed to login on AdmServer");
        }
        if (ackFlag != '1') {
            throw new IllegalStateException("LoginAck from AdmServer with unknown flag " + ackFlag);
        }
        if (PROTOCOL_VERSION == 3) {
            return message.getShort() & 0xFFFF;
        }
        else {
            return message.get() & 0xFF;
        }
    }

    ByteBuffer createLoginMessage() {
        final ByteBuffer result = createMessageBuffer(1, 88);
        result.put(this.mdpsName);
        result.put(this.localAddress);
        result.put(toAscii(getPort(), 8));
        result.put(this.localHost);
        result.putShort((short) getPid());
        result.flip();
        return result;
    }

    ByteBuffer createProfileRequest() {
        final ByteBuffer result = createMessageBuffer(6, 30);
        result.put(this.mdpsName);
        result.flip();
        return result;
    }

    ByteBuffer createProcessReadyMessage() {
        final ByteBuffer result = createMessageBuffer(3, 30);
        result.put(this.mdpsName);
        result.flip();
        return result;
    }

    ByteBuffer createAdminResponseMessage(AdminRequestContext context) {
        if (context.isCancelResponse()) {
            return null;
        }
        final byte[] data = toAscii(context.getResult());
        final ByteBuffer result = createMessageBuffer(5, 8 + data.length);
        result.put((byte) ( context.isFailed() ?  '0'  : '1' ));
        result.putInt(context.getUserData());
        result.put((byte) '0'); // '0' response completed ('1' = more-to-follow)
        result.putShort((short) data.length);
        result.put(data);
        result.flip();
        return result;
    }

    private String getString(ByteBuffer buffer, int len) {
        final byte[] cmdBuffer = new byte[len];
        buffer.get(cmdBuffer);
        return new String(cmdBuffer, ASCII).trim();
    }

    private ByteBuffer createMessageBuffer(int msgId, int length) {
        final int lengthWithHeader = length + MSG_HEADER_LEN;
        final ByteBuffer result = createBuffer(lengthWithHeader);
        result.putShort((short) lengthWithHeader);
        result.put((byte) PROTOCOL_VERSION);
        result.put((byte) msgId);
        return result;
    }

    ByteBuffer createBuffer(int length) {
        return ByteBuffer.allocate(length).order(BYTE_ORDER);
    }

    private int getPid() {
        final String s = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(s.substring(0, s.indexOf('@')));
    }

    private String getHost() {
        final String s = ManagementFactory.getRuntimeMXBean().getName();
        int start = s.indexOf('@') + 1;
        int end = s.indexOf('.', start);
        return s.substring(start, end > 0 ? end : s.length());
    }

    private String getPort() {
        return System.getProperty("jmxremote.registry.port", "xxxx");
    }

    private byte[] toAscii(String s) {
        return s.getBytes(ASCII);
    }

    private byte[] toAscii(String s, int length) {
        final StringBuilder sb = new StringBuilder(length).append(s);
        sb.setLength(length);
        for (int i = s.length(); i < length; i++) {
            sb.setCharAt(i, ' ');
        }
        return toAscii(sb.toString());
    }

    public boolean isMessageComplete(ByteBuffer buffer) {
        return buffer.position() >= 2 && buffer.getShort(0) <= buffer.position();
    }

    public Properties getProfileProperties(ByteBuffer bb) {
        bb.position(MSG_HEADER_LEN);
        final char status = (char) bb.get();
        if (status != '1') {
            throw new IllegalStateException("Error in ProfileAnswer: " + status);
        }
        final Properties result = new Properties();

        final int numParams = bb.getShort() & 0xFFFF;
        bb.position(bb.position() + 2); // skip info length
        for (int i = 0; i < numParams; i++) {
            result.setProperty(getString(bb, bb.getShort()), getString(bb, bb.getShort()));
        }
        return result;
    }

    public static void main(String[] args) throws UnknownHostException {
        AdminProtocolSupport s = new AdminProtocolSupport("MDPS_delay");
        System.out.println(HexDump.toHex(s.createLoginMessage().array()));
        System.out.println(HexDump.toHex(s.createProfileRequest().array()));
    }
}
