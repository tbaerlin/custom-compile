/*
 * EstimatesProvider.java
 *
 * Created on 09.08.2006 07:56:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.StockRevenueSummary;
import de.marketmaker.istar.domain.data.HistoricEstimates;
import de.marketmaker.istar.domain.profile.Profile;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.estimates")
public interface EstimatesProvider {
    List<StockRevenueSummary> getEstimates(Profile profile, long instrumentid);

    List<Long> getEstimatesDirectory(Profile profile, DateTime refDate);

    HistoricEstimates getHistoricEstimates(Profile profile, long instrumentid);
}
