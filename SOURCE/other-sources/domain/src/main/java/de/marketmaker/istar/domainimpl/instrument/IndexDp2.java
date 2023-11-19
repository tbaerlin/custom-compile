/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;

import de.marketmaker.istar.domain.instrument.Index;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexDp2 extends InstrumentDp2 implements Index, Serializable {
    static final long serialVersionUID = -107L;

    public IndexDp2() {
    }

    public IndexDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.IND;
    }
}
