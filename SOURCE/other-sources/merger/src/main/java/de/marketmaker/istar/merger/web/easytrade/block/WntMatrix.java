/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WntMatrix extends DerivativeMatrix {
    public WntMatrix() {
        super(DerivativeMatrix.Command.class);
    }

    protected InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.WNT;
    }

    protected String getTemplate() {
        return "wntmatrix";
    }
}