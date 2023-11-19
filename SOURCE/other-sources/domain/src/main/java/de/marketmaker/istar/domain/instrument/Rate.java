/*
 * Bond.java
 *
 * Created on 17.12.2004 11:48:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import de.marketmaker.istar.domain.Currency;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: Index.java,v 1.1 2004/12/17 17:51:56 tkiesgen Exp $
 */
public interface Rate extends Instrument {

    Currency getSourceCurrency();

    Currency getTargetCurrency();

    double getSourceToTargetFactor();
}
