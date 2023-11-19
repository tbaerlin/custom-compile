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
import de.marketmaker.istar.domain.instrument.Option;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OptionDp2 extends DerivativeWithStrikeDp2 implements Option, Serializable {
    static final long serialVersionUID = -110L;

    public OptionDp2() {
    }

    public OptionDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.OPT;
    }
}
