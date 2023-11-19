/*
 * Bestseller.java
 *
 * Created on 19.07.2006 22:38:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.provider;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Bestseller implements Serializable  {
    protected static final long serialVersionUID = 1L;

    private final DateTime importDate;
    private final InstrumentTypeEnum type;
    private final List<Long> instrumentids = new ArrayList<>();

    public Bestseller(InstrumentTypeEnum type, DateTime importDate) {
        this.importDate = importDate;
        this.type= type;
    }

    void add(long iid) {
        this.instrumentids.add(iid);
    }

    public DateTime getImportDate() {
        return importDate;
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    public List<Long> getInstruments() {
        return instrumentids;
    }

    public String toString() {
        return "Bestseller["+importDate
                +", type="+type.name()
                +", iids="+instrumentids
                +"]";
    }
}
