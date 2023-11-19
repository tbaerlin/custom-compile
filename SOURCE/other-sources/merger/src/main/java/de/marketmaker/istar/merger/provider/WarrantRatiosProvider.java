/*
 * BondRatiosProvider.java
 *
 * Created on 28.07.2006 08:12:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.WarrantRatios;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.pm5.warrantRatios")
public interface WarrantRatiosProvider {
    /**
     * @deprecated use {@link de.marketmaker.istar.merger.provider.WarrantRatiosProvider#getWarrantRatios(SymbolQuote, de.marketmaker.istar.domain.data.PriceRecord)}
     */
    WarrantRatios getWarrantRatios(Quote quote, PriceRecord pr);

    WarrantRatios getWarrantRatios(SymbolQuote quote, PriceRecord pr);
}
