/*
 * Derivative.java
 *
 * Created on 28.02.2005 18:45:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Derivative extends Instrument {
    long getUnderlyingId();
    BigDecimal getSubscriptionRatio();
}
