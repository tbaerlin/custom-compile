/*
 * AbstractProtobufSerializer.java
 *
 * Created on 14.09.2010 14:18:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.GeneratedMessage;

import de.marketmaker.istar.common.io.ByteBufferOutputStream;

/**
 * @author oflege
 */
abstract class AbstractProtobufSerializer {

    private final ByteBufferOutputStream baos;

    private final CodedOutputStream cos;

    private int numObjects = 0;

    protected AbstractProtobufSerializer() {
        this(16384);
    }

    protected AbstractProtobufSerializer(int size) {
        this.baos = new ByteBufferOutputStream(size);
        this.cos = CodedOutputStream.newInstance(baos);
    }

    public int getNumObjects() {
        return this.numObjects;
    }

    protected void addObject(GeneratedMessage.Builder b) throws IOException {
        this.numObjects++;
        final byte[] bytes = b.build().toByteArray();
        this.cos.writeRawVarint32(bytes.length);
        this.cos.writeRawBytes(bytes);
    }

    protected ByteBuffer getResult() throws IOException {
        if (this.numObjects == 0) {
            return ByteBuffer.wrap(new byte[0]);
        }
        this.cos.flush();
        return this.baos.toBuffer();
    }

    protected BigDecimal toBigDecimal(Long val) {
        return null == val ? null : new BigDecimal(val);
    }

}
