/*
 * SequenceWriter.java
 *
 * Created on 09.02.2005 12:00:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.mcast.MulticastSender;

import static de.marketmaker.istar.common.mcast.MulticastSenderImpl.MULTICAST_PACKET_SIZE;

/**
 * A BufferWriter that writes data it receives into a private buffer before it forwards it to
 * either another BufferWriter or a MulticastSender. In the former case, the data starts with a
 * short message length field followed by a long sequence number, in the latter case just the
 * long sequence number is used. The sequence number can be used by a client to detect whether
 * the data it receives contains any gaps.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SequenceWriter implements BufferWriter, InitializingBean {

    private static final int MAX_RECORD_LENGTH = (1 << 16) - 1;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // max record payload length, i.e., w/o length and/or sequence number
    private int netRecordLength;

    private int recordLength = MULTICAST_PACKET_SIZE;

    private ByteBuffer buffer;

    private final Object bufferMutex = new Object();

    private AtomicLong id = new AtomicLong();

    private boolean sendOnEveryWrite = false;

    private BufferWriter writer;

    private MulticastSender sender;

    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

    public void setLittleEndian(boolean littleEndian) {
        this.byteOrder = littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
    }

    public void setRecordLength(int recordLength) {
        this.recordLength = recordLength;
    }

    public void setWriter(BufferWriter writer) {
        this.writer = writer;
    }

    public void setSender(MulticastSender sender) {
        this.sender = sender;
    }

    public void setSendOnEveryWrite(boolean sendOnEveryWrite) {
        this.sendOnEveryWrite = sendOnEveryWrite;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.sender == null && this.writer == null) {
            throw new Exception("sender and writer are null");
        }
        if (this.sender != null && this.writer != null) {
            throw new Exception("sender and writer are defined");
        }
        if (this.writer != null && this.recordLength > MAX_RECORD_LENGTH) {
            throw new IllegalStateException(this.recordLength + " > max: " + MAX_RECORD_LENGTH);
        }

        this.netRecordLength = this.recordLength - (this.writer != null ? 10 : 8);
        this.buffer = ByteBuffer.allocate(this.recordLength).order(this.byteOrder);

        this.logger.info("<initialize> recordLength = " + this.recordLength);
        resetBuffer();
    }

    public void write(ByteBuffer toSend) throws IOException {
        if (toSend.remaining() > this.netRecordLength) {
            throw new IOException("too large to send: " + toSend.remaining());
        }

        synchronized (this.bufferMutex) {
            if (this.buffer.remaining() < toSend.remaining()) {
                send();
            }
            this.buffer.put(toSend);

            if (this.sendOnEveryWrite) {
                send();
            }
        }
    }

    void send() throws IOException {
        this.buffer.flip();

        if (this.sender != null) {
            this.sender.sendPacket(this.buffer.array(), 0, this.buffer.remaining());
        }
        else {
            this.buffer.putShort(0, (short) this.buffer.remaining());
            this.writer.write(this.buffer);
        }

        resetBuffer();
    }

    private void resetBuffer() {
        this.buffer.clear();
        if (this.writer != null) {
            this.buffer.position(2);
        }
        this.buffer.putLong(this.id.incrementAndGet());
    }
}
