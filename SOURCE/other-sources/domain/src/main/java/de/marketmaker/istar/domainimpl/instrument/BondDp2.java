/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.instrument.Bond;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BondDp2 extends InstrumentDp2 implements Bond, Serializable {
    static final long serialVersionUID = -100L;

    public BondDp2() {
    }

    public BondDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.BND;
    }
}
