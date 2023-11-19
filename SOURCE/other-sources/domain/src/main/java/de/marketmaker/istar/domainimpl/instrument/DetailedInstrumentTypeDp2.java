/*
 * DetailedInstrumentTypeDp2.java
 *
 * Created on 02.09.2005 12:58:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.DetailedInstrumentType;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DetailedInstrumentTypeDp2 extends ItemWithSymbolsDp2 implements DetailedInstrumentType {
    static final long serialVersionUID = -5769884887217968501L;

    @Override
    public String getSymbolWmGd198bId() {
        return getSymbol(KeysystemEnum.WM_GD198B_ID);
    }

    @Override
    public String getSymbolWmGd198cId() {
        return getSymbol(KeysystemEnum.WM_GD198C_ID);
    }
}
