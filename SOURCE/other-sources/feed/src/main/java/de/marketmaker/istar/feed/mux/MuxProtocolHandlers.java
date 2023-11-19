/*
 * MuxProtocolHandlers.java
 *
 * Created on 08.10.12 14:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

/**
 * @author oflege
 */
public class MuxProtocolHandlers {

    public static final MuxProtocolHandler MDPS_V1 = new MdpsProtocolHandler(BIG_ENDIAN, false);

    public static final MuxProtocolHandler MDPS_V3 = new MdpsProtocolHandler(LITTLE_ENDIAN, false);

    public static final MuxProtocolHandler MDPS_ZIP = new MdpsProtocolHandler(BIG_ENDIAN, true);

    public static final MuxProtocolHandler MDPS_ZIP_V3 = new MdpsProtocolHandler(LITTLE_ENDIAN, true);

    private static class MdpsProtocolHandler implements MuxProtocolHandler {
        private final boolean zipped;

        private final ByteOrder order;

        private final AtomicLong numMessages = new AtomicLong();

        private MdpsProtocolHandler(ByteOrder order, boolean zipped) {
            this.order = order;
            this.zipped = zipped;
        }

        @Override
        public long numMessagesReceived() {
            return this.numMessages.get();
        }

        @Override
        public ByteOrder getByteOrder() {
            return this.order;
        }

        @Override
        public int getEndOfLastCompleteRecord(ByteBuffer in) throws IOException {
            int result = 0;
            int length;
            while ((length = getLengthOfRecordAt(in, result)) > 0) {
                if (length > (in.remaining() - result)) {
                    break;
                }
                result += length;
                this.numMessages.incrementAndGet();
            }
            return result;
        }

        @Override
        public int getLengthOfRecordAt(ByteBuffer in, int position) throws IOException {
            if (this.zipped) {
                if (position + 4 > in.limit()) {
                    return 0;
                }
                final int result = in.getInt(position);
                // sanity check: the zipping MX on the other end has a tendency to start
                // sending in the middle of a record if it is overwhelmed by feed...
                if (result < 0 || result > 0xFFFFFF) {
                    throw new IOException("invalid length: " + result);
                }
                return result;
            }
            else {
                return (position + 2 > in.limit()) ? 0 : (in.getShort(position) & 0xFFFF);
            }
        }
    }
}
