/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.instrument.Certificate;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CertificateDp2 extends DerivativeWithStrikeDp2 implements Certificate, Serializable {
    static final long serialVersionUID = -101L;

    public CertificateDp2() {
    }

    public CertificateDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.CER;
    }
}
