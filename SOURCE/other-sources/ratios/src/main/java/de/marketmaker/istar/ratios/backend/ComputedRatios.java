/*
 * ComputedRatios.java
 *
 * Created on 18.10.2005 11:27:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ComputedRatios {
    private final long quoteid;
    private final byte[] data;

    public ComputedRatios(long quoteid, byte[] data) {
        this.quoteid = quoteid;
        this.data=data;
    }

    public long getQuoteid() {
        return quoteid;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "client controls this data container")
    public byte[] getData() {
        return data;
    }
}
