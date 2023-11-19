/*
 * BondRatiosProvider.java
 *
 * Created on 28.07.2006 08:12:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.BondRatios;
import de.marketmaker.istar.domain.instrument.Quote;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pm5.bondRatios")
public interface BondRatiosProvider {
    /**
     * @deprecated use {@link de.marketmaker.istar.merger.provider.BondRatiosProvider#getBondRatios(SymbolQuote, java.math.BigDecimal)}
     */
    BondRatios getBondRatios(Quote quote, BigDecimal marketRate);

    BondRatios getBondRatios(SymbolQuote quote, BigDecimal marketRate);
}
