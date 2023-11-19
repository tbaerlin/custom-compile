/*
 * SuggestedInstrumentImpl.java
 *
 * Created on 17.06.2009 11:38:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SuggestedInstrumentImpl
        implements Serializable, SuggestedInstrument<SuggestedInstrumentImpl> {
    protected static final long serialVersionUID = 1L;

    private final int order;
    private final long id;
    private final InstrumentTypeEnum type;
    private final String name;
    private final String isin;
    private final String wkn;

    public SuggestedInstrumentImpl(int order, long id, InstrumentTypeEnum type, String name,
            String isin, String wkn) {
        this.order = order;
        this.id = id;
        this.type = type;
        this.name = name;
        this.isin = isin;
        this.wkn = wkn;
    }

    @Override
    public String toString() {
        return name + "(" + id + ".iid) " + this.type + ", " + this.isin + ", " + this.wkn  + " / " + this.order;
    }

    public int compareTo(SuggestedInstrumentImpl o) {
        if (this.order < o.order) return -1;
        if (this.order > o.order) return 1;
        return this.name.compareToIgnoreCase(o.name);
    }

    public int getOrder() {
        return this.order;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public InstrumentTypeEnum getInstrumentType() {
        return this.type;
    }

    public String getSymbolIsin() {
        return this.isin;
    }

    public String getSymbolWkn() {
        return this.wkn;
    }

}
