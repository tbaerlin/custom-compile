/*
 * InstrumentAssociation.java
 *
 * Created on 12.07.2006 22:20:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentAssociation {

    BigDecimal getFraction();

    long getInstrumentId();
}
