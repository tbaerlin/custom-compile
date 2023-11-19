/*
 * ScreenerDataRepository.java
 *
 * Created on 03.04.2007 18:50:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.data.ScreenerUpDownData;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.screener")
public interface ScreenerDataRepository {
    boolean hasScreenerData(long instrumentid);

    ScreenerData getScreenerData(long instrumentid, String language);

    List<String> getIsinsWithPdf();

    ScreenerUpDownData getUpDownData(String region);
}
