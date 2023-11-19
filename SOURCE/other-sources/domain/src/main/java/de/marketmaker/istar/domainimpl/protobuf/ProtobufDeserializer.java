/*
 * ProtobufDeserializer.java
 *
 * Created on 14.09.2010 14:18:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import com.google.protobuf.CodedInputStream;

/**
 * @author oflege
 */
public class ProtobufDeserializer<T> {

    public interface ObjectFactory<T> {
        T parseFrom(CodedInputStream cis) throws IOException;
    }

    private final int numObjects;

    private int numNext;

    private final CodedInputStream cis;

    private final ObjectFactory<T> factory;

    public ProtobufDeserializer(InputStream is, int numObjects, ObjectFactory<T> factory) {
        this.cis = CodedInputStream.newInstance(is);
        this.factory = factory;
        this.numObjects = numObjects;
        this.numNext = numObjects;
    }

    public ProtobufDeserializer(ByteBuffer bb, int numObjects, ObjectFactory<T> factory) {
        this.cis = CodedInputStream.newInstance(bb.array(), bb.position(), bb.remaining());
        this.factory = factory;
        this.numObjects = numObjects;
        this.numNext = numObjects;
    }

    public int size() {
        return this.numObjects;
    }

    public boolean hasNext() throws IOException {
        return this.numNext > 0;
    }

    public T next() throws IOException {
        if (this.numNext-- <= 0) {
            throw new NoSuchElementException();
        }

        final int length = this.cis.readRawVarint32();
        final int oldLimit = this.cis.pushLimit(length);
        final T result = factory.parseFrom(this.cis);
        this.cis.popLimit(oldLimit);
        return result;
    }
}
