/*
 * IOLWriter.java
 *
 * Created on 29.07.11 12:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.common.io.DataFile;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Used to create a file with Iid/Offset/Length information,
 * @author oflege
 */
class IOLWriter implements AutoCloseable {
    private final ByteBuffer bb = ByteBuffer.allocate(IOL.SIZE * 1024);

    private final DataFile df;

    private int numAppended = 0;

    private long lastIid = Long.MIN_VALUE;

    IOLWriter(File f) throws IOException {
        InstrumentSystemUtil.deleteIfExistsFailException(f);
        this.df = new DataFile(f, false);
    }

    /**
     * add iol's data to the file
     * @param iol to be appended
     * @throws IOException on io failure
     * @throws IllegalArgumentException if iol's iid is not greater than that of the previousls
     * appended iol.
     */
    void append(IOL iol) throws IOException {
        if (iol.iid <= this.lastIid) {
            throw new IllegalArgumentException("trying to append " + iol.iid + " after " + this.lastIid);
        }
        this.lastIid = iol.iid;

        if (!this.bb.hasRemaining()) {
            this.bb.flip();
            this.df.write(this.bb);
            this.bb.clear();
        }
        this.bb.putLong(iol.iid);
        this.bb.putLong(iol.offset);
        this.bb.putInt(iol.length);
        this.numAppended++;
    }

    int getNumAppended() {
        return numAppended;
    }

    public void close() throws IOException {
        this.bb.flip();
        if (this.bb.hasRemaining()) {
            this.df.write(this.bb);
        }
        this.df.close();
    }
}
