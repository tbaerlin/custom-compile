/*
 * IndexAndOffset.java
 *
 * Created on 28.10.2004 08:53:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.snap;

import java.util.Arrays;

import de.marketmaker.istar.common.util.SimpleBitSet;

/**
 * Encapsulates a two dimensional array with following structure:
 * <dl>
 * <dt><tt>index[0]</tt></dt><dd>an integer array which contains field ids as defined in
 * {@link de.marketmaker.istar.feed.vwd.VwdFieldDescription.Field}, sorted in ascending, with one
 * additional id - <tt>Integer.MAX_VALUE</tt>. Only those for the associated FeedData available field
 * ids are contained.</dd>
 * <dt><tt>index[1]</tt></dt><dd>an integer array which contains offsets into a byte array that
 * contains the field values, with one additional integer equals the last offset plus its length.</dd>
 * </dl>
 * and provides convenient methods to access those information.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexAndOffset {
    private final IndexAndOffsetFactory factory;

    private final int[][] index;

    private final int hashCode;

    private final int size;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    IndexAndOffset(IndexAndOffsetFactory factory, int[][] index) {
        this.factory = factory;
        this.index = index;
        this.hashCode = computeHashCode();
        this.size = this.index[1][this.index[1].length - 1];
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append("IndexAndOffset[");
        sb.append("fields=");
        sb.append(Arrays.toString(this.index[0]));
        sb.append(", offsets=");
        sb.append(Arrays.toString(this.index[1]));
        sb.append("]");
        return sb.toString();
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public IndexAndOffset(int[][] index) {
        this.factory = null;
        this.index = index;
        this.hashCode = computeHashCode();
        this.size = this.index[1][this.index[1].length - 1];
    }

    private int computeHashCode() {
        int n = 17;
        for (int i = 0; i < this.index[0].length; i++) {
            n = n * 37 + this.index[0][i];
        }
        return n;
    }

    public int getIndex(int fieldid) {
        return Arrays.binarySearch(this.index[0], fieldid);
    }

    public int getFieldid(int index) {
        return this.index[0][index];
    }

    public int getOffsetByIndex(int index) {
        return this.index[1][index];
    }

    public int getOffset(final int fieldid) {
        final int fieldindex = getIndex(fieldid);

        if (fieldindex < 0) {
            return -1;
        }

        return this.index[1][fieldindex];
    }

    public IndexAndOffset expand(int fieldid) {
        final int fieldindex = getIndex(fieldid);
        if (fieldindex >= 0) {
            return this;
        }

        return this.factory.getExpandedIndexAndOffset(this, fieldid);
    }

    public IndexAndOffset shrink(SimpleBitSet fieldIdsToKeep) {
        return this.factory.getShrunkIndexAndOffset(this, fieldIdsToKeep);
    }

    public int getArrayLength() {
        return this.index[0].length;
    }

    public int getNumFields() {
        return this.index[0].length - 1;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public int[] getIndexArray() {
        return this.index[0];
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public int[] getOffsetArray() {
        return this.index[1];
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexAndOffset)
                || this.hashCode != o.hashCode()) {
            return false;
        }

        return Arrays.equals(this.index[0], ((IndexAndOffset) o).index[0]);
    }

    public int hashCode() {
        return this.hashCode;
    }

    public int getLength(int fieldid) {
        final int index = getIndex(fieldid);
        return index < 0 ? -1 : getLengthByIndex(index);
    }

    public int getLengthByIndex(int n) {
        return this.index[1][n + 1] - this.index[1][n];
    }

    public int getSize() {
        return this.size;
    }
}
