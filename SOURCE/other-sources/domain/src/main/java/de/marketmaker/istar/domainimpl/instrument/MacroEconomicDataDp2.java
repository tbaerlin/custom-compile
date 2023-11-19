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
import de.marketmaker.istar.domain.instrument.MacroEconomicData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MacroEconomicDataDp2 extends InstrumentDp2 implements MacroEconomicData, Serializable {
    static final long serialVersionUID = -109L;

    public MacroEconomicDataDp2() {
    }

    public MacroEconomicDataDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.MK;
    }
}
