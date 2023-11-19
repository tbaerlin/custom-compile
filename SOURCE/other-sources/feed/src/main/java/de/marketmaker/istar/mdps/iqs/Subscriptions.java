/*
 * Subscriptions.java
 *
 * Created on 21.10.13 08:54
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * @author oflege
 */
class Subscriptions {
    private static final byte[] NULL = new byte[2];

    private static final byte[] REQUEST_ID_FIELD;

    private static final byte[] USERDATA_FIELD;

    static {
        final IqsMessageBuilder b = new IqsMessageBuilder(3);
        REQUEST_ID_FIELD = b.clear().header(Constants.FID_REQUEST_ID).asBytes();
        USERDATA_FIELD = b.clear().header(Constants.FID_USERDATA).asBytes();
    }

    private static int lengthAsField(byte[] data) {
        return (data != null) ? (3 + data.length): 0;
    }

    private byte[] data;

    private int pos;

    private int clientId;

    private int nextPos;


    Subscriptions reset(byte[] requestId, byte[] userData) {
        this.data = createReplyFields(requestId, userData);
        this.pos = 0;
        this.nextPos = this.data.length;
        this.clientId = 0;
        return this;
    }

    Subscriptions reset(byte[] data) {
        this.data = data != null ? data : NULL;
        this.nextPos = 0;
        next();
        return this;
    }

    boolean hasData() {
        return this.clientId != 0;
    }

    int getClientId() {
        return clientId;
    }

    int getReplyFieldsLength() {
        return this.nextPos - this.pos;
    }

    void appendReplyFieldsTo(ByteBuffer bb) {
        bb.put(this.data, this.pos, this.nextPos - this.pos);
    }

    int next() {
        if (this.nextPos < 0) {
            throw new NoSuchElementException();
        }
        this.pos = nextPos;
        this.clientId = getShort();
        this.nextPos = (clientId != 0) ? (this.pos + 1 + getByte()) : -1;
        return this.clientId;
    }

    private int getShort() {
        return ((getByte()) << 8) + getByte();
    }

    private void putShort(int s) {
        this.data[pos++] = (byte) (s >> 8);
        this.data[pos++] = (byte) s;
    }

    private int getByte() {
        return data[pos++] & 0xFF;
    }

    /**
     * Adds a new subscription and ensures that the internal state points to that new subscription.
     * @param clientId identifier for client
     * @param requestId identifies request, must not be null
     * @param userData optional userdata, may be null
     */
    byte[]  addSubscription(int clientId, byte[] requestId, byte[] userData) {
        final byte[] replyFields = createReplyFields(requestId, userData);
        while (hasData()) {
            next();
        }

        final int requiredLength = asMultipleOf32(this.pos + 3 + replyFields.length);
        if (this.data.length < requiredLength) {
            this.data = Arrays.copyOf(this.data, requiredLength);
        }

        this.pos -= 2; // just before final 00
        appendSubscription(clientId, replyFields);
        return this.data;
    }

    private void appendSubscription(int clientId, byte[] replyFields) {
        putShort(clientId);
        this.data[pos++] = (byte) replyFields.length;
        System.arraycopy(replyFields, 0, this.data, pos, replyFields.length);

        this.nextPos = pos + replyFields.length;
        this.data[nextPos + 1] = 0;
        this.data[nextPos + 2] = 0;
        this.clientId = clientId;
    }

    private int asMultipleOf32(int tmp) {
        return ((tmp + 32) >> 5) << 5;
    }

    private byte[] createReplyFields(byte[] requestId, byte[] userdata) {
        final byte[] result = Arrays.copyOf(REQUEST_ID_FIELD, lengthAsField(requestId) + lengthAsField(userdata));
        int n = copyInto(requestId, result, REQUEST_ID_FIELD.length);
        if (userdata != null) {
            n = copyInto(USERDATA_FIELD, result, n);
            copyInto(userdata, result, n);
        }
        return result;
    }

    private int copyInto(byte[] src, byte[] dst, int pos) {
        System.arraycopy(src, 0, dst, pos, src.length);
        return pos + src.length;
    }

    boolean hasSubscriptionFor(int clientId) {
        while (hasData()) {
            if (clientId == this.clientId) {
                return true;
            }
            next();
        }
        return false;
    }

    void remove() {
        assert clientId != 0;
        System.arraycopy(this.data, nextPos, this.data, this.pos - 3, this.data.length - nextPos);
        this.nextPos = pos - 3;
        next();
    }

    boolean remove(int id) {
        while (hasData()) {
            if (this.clientId == id) {
                remove();
                return true;
            }
            next();
        }
        return false;
    }
}
