/*
 * IOL.java
 *
 * Created on 27.04.2010 14:55:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

/**
 * An IOL is identified by its instrument id and contains offset and length values into an instrument
 * data file created by {@link InstrumentDirWriter}.
 *
 * @author zzhao
 * @since 1.2
 */
class IOL implements Comparable<IOL> {

    static final int SIZE = Long.BYTES + Long.BYTES + Integer.BYTES;

    final long iid;

    long offset;

    final int length;

    IOL(long iid, long offset, int length) {
        this.iid = iid;
        this.offset = offset;
        this.length = length;
    }

    IOL withOffsetIncrementedBy(long inc) {
        this.offset += inc;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IOL block = (IOL) o;
        return iid == block.iid;
    }

    public long getIid() {
        return iid;
    }

    public long getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public int hashCode() {
        return (int) (iid ^ (iid >>> 32));
    }

    public int compareTo(IOL o) {
        return (this.iid < o.iid) ? -1 : (this.iid > o.iid) ? 1 : 0;
    }

    public String toString() {
        return "IOL[" + this.iid + ":" + this.offset + ":" + this.length + "]";
    }
}
