/*
 * Partition.java
 *
 * Created on 3/11/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
public class Partition implements Serializable {

    static final long serialVersionUID = 5692503327828580166L;

    private final int offset;

    private final String start;

    private String end;

    public Partition(int offset, String start, String end) {
        this.offset = offset;
        this.start = start;
        this.end = end;
    }

    public int getOffset() {
        return offset;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getName() {
        final char firstChar = start.toUpperCase().charAt(0);
        final char lastChar = end.toUpperCase().charAt(0);
        if (firstChar == lastChar) {
            return String.valueOf(firstChar);
        }
        return start.toUpperCase().charAt(0) + "..." + end.toUpperCase().charAt(0);
    }

    public Partition merge(Partition partition) {
        this.end = partition.end;
        return this;
    }

    @Override
    public String toString() {
        return "Partition{" +
                "name='" + getName() + '\'' +
                ", offset=" + offset +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                '}';
    }
}
