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
import de.marketmaker.istar.domain.instrument.Underlying;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnderlyingDp2 extends InstrumentDp2 implements Underlying, Serializable {
    static final long serialVersionUID = -114L;

    public UnderlyingDp2() {
    }

    public UnderlyingDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.UND;
    }
}
