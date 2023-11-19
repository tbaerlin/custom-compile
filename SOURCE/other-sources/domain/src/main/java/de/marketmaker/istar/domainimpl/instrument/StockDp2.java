/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Stock;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockDp2 extends InstrumentDp2 implements Stock, Serializable {
    static final long serialVersionUID = -113L;

    private int generalMeetingDate;

    public StockDp2() {
    }

    public StockDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.STK;
    }

    public int getGeneralMeetingDate() {
        return generalMeetingDate;
    }

    public void setGeneralMeetingDate(int generalMeetingDate) {
        this.generalMeetingDate = generalMeetingDate;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        StockDp2 stockDp2 = (StockDp2) o;
        if (generalMeetingDate != stockDp2.generalMeetingDate) return false;
        return true;
    }
}
