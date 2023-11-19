/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OptMatrix extends DerivativeMatrix {
    public static class Command extends DerivativeMatrix.Command {
        private boolean eurexOnly = true;

        public boolean isEurexOnly() {
            return eurexOnly;
        }

        public void setEurexOnly(boolean eurexOnly) {
            this.eurexOnly = eurexOnly;
        }
    }

    public OptMatrix() {
        super(Command.class);
    }

    protected InstrumentTypeEnum getInstrumentType() {
        return InstrumentTypeEnum.OPT;
    }

    protected String getTemplate() {
        return "optmatrix";
    }

    @Override
    protected void beforeSearch(DerivativeMatrix.Command cmd, RatioSearchRequest rsr) {
        if (((Command)cmd).isEurexOnly()) {
            rsr.addParameter("vwdMarket", "DTB");
        }
    }
}