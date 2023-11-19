/*
 * StockDp2.java
 *
 * Created on 20.12.2004 13:14:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.ParticipationCertificate;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ParticipationCertificateDp2 extends InstrumentDp2 implements ParticipationCertificate, Serializable {
    static final long serialVersionUID = -111L;

    public ParticipationCertificateDp2() {
    }

    public ParticipationCertificateDp2(long id) {
        super(id);
    }

    public InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.GNS;
    }
}
