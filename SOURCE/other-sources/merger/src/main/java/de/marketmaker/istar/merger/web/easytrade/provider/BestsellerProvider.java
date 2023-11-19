/*
 * BestsellerProvider.java
 *
 * Created on 19.07.2006 22:14:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.provider;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.merger.bestseller")
public interface BestsellerProvider {
    Bestseller getBestseller(InstrumentTypeEnum type);

    Bestseller getSavingplans();
}
