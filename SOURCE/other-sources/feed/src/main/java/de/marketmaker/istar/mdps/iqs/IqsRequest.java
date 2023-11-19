/*
 * IqsMessage.java
 *
 * Created on 19.09.13 10:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.util.Arrays;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author oflege
 */
class IqsRequest {

    static class Builder implements StreamingMessageParser.Handler {
        private final long[] fields = new long[64];

        private int numFields = 0;

        private byte[] msg;

        private boolean failed;

        private boolean addFields;

        @Override
        public void fail() {
            this.failed = true;
        }

        @Override
        public void start(byte[] msg) {
            this.msg = msg;
            this.numFields = 0;
            this.failed = false;
            this.addFields = true;
        }

        @Override
        public void startBody() {
            this.addFields = false;
        }

        @Override
        public void startEnvelope() {
            this.addFields = false;
        }

        @Override
        public void end() {
        }

        @Override
        public void field(int fid, int start, int end) {
            if (this.addFields) {
                this.fields[this.numFields] = fid;
                this.fields[this.numFields] <<= 16;
                this.fields[this.numFields] += start;
                this.fields[this.numFields] <<= 16;
                this.fields[this.numFields] += end;
                this.numFields++;
            }
        }

        IqsRequest build() {
            if (failed) {
                return null;
            }
            Arrays.sort(this.fields, 0, this.numFields);
            return new IqsRequest(this.msg, Arrays.copyOf(this.fields, this.numFields));
        }
    }

    private final byte[] msg;

    /**
     * each value encodes meta data about a single field in the header of the message
     * the three values are interpreted as unsigned short values and are stored as follows:
     * <pre>
     * | not used |    fid   |startoffs.| endoffset|
     * |____7|___6|____5|___4|____3|___2|____1|___0|
     * </pre>
     * the values are sorted by ascending fid.
     */
    private final long[] fieldDescriptors;

    private int messageType;

    private IqsRequest(byte[] msg, long[] fieldDescriptors) {
        this.msg = msg;
        this.fieldDescriptors = fieldDescriptors;
        this.messageType = getInt(Constants.FID_MESSAGE_TYPE);
    }

    int getMessageType() {
        return messageType;
    }

    byte[] getMessageBytes() {
        return this.msg;
    }

    boolean hasField(int fieldId) {
        return findField(fieldId) >= 0;
    }

    byte getByte(int fid) {
        final int i = findField(fid);
        return (i >= 0) ? msg[getStart(i)] : 0;
    }

    int getInt(int fid) {
        final int i = findField(fid);
        return (i >= 0) ? doGetInt(i) : Integer.MIN_VALUE;
    }

    private int doGetInt(int i) {
        int result = 0;
        for (int k = getStart(i), n = getEnd(i); k < n; k++) {
            result = 10 * result + (msg[k] - '0');
        }
        return result;
    }

    ByteString getString(int fid) {
        final int i = findField(fid);
        return (i >= 0) ? doGetString(i) : null;
    }

    byte[] getBytes(int fid) {
        final int i = findField(fid);
        return (i >= 0) ? doGetBytes(i) : null;
    }

    private int findField(int fid) {
        for (int i = 0, n = this.fieldDescriptors.length; i < n; i++) {
            switch (Integer.compare(getFid(i), fid)) {
                case 0:
                    return i;
                case 1: // ids are sorted, we found id > fid
                    return -1;
            }
        }
        return -1;
    }

    private ByteString doGetString(int i) {
        final int from = getStart(i);
        return new ByteString(this.msg, from, getEnd(i) - from);
    }

    private byte[] doGetBytes(int i) {
        final int from = getStart(i);
        return Arrays.copyOfRange(this.msg, from, getEnd(i));
    }

    private int getFid(int i) {
        return (int) (this.fieldDescriptors[i] >> 32);
    }

    private int getEnd(int i) {
        return ((int)(this.fieldDescriptors[i])) & 0xFFFF;
    }

    private int getStart(int i) {
        return ((int)(this.fieldDescriptors[i] >> 16)) & 0xFFFF;
    }
}
