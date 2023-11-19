/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;

import de.marketmaker.istar.domain.instrument.Fund;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDp2 extends InstrumentDp2 implements Fund, Serializable {
    static final long serialVersionUID = -105L;

    public FundDp2() {
    }

    public FundDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.FND;
    }
}
