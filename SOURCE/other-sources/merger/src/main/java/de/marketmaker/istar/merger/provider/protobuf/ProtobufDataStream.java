/*
 * ProtobufDataStream.java
 *
 * Created on 09.12.11 07:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.protobuf;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessage;
import org.apache.commons.io.IOUtils;

/**
 * Deserializes data from a data file created by {@link ProtobufDataWriter} without an index.
 *
 * @author oflege
 */
public class ProtobufDataStream implements Closeable {

    private final FileInputStream fis;

    private final CodedInputStream cis;

    private int len;

    private boolean failed = false;

    public ProtobufDataStream(File file) throws IOException {
        fis = new FileInputStream(file);
        final DataInputStream dis = file.getName().toLowerCase().endsWith(".gz")
                ? new DataInputStream(new GZIPInputStream(fis))
                : new DataInputStream(fis);

        /* ignore message class name */
        dis.readUTF();
        this.cis = CodedInputStream.newInstance(dis);
        this.cis.setSizeLimit(Integer.MAX_VALUE);
        this.len = readLength();
    }

    public boolean hasNext() {
        return !this.failed && this.len > 0;
    }

    public <V extends GeneratedMessage.Builder<V>> V mergeNext(V builder) throws IOException {
        if (!hasNext()) {
            throw new IllegalStateException();
        }
        final int oldLimit = this.cis.pushLimit(this.len);
        try {
            builder.mergeFrom(this.cis);
            return builder;
        } catch (IOException e) {
            this.failed = true;
            throw e;
        } finally {
            this.cis.resetSizeCounter();
            this.cis.popLimit(oldLimit);
            this.len = readLength();
        }
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(this.fis);
    }

    private int readLength() throws IOException {
        return this.cis.readInt32();
    }
}
