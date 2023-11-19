/*
 * DelegatingMultiProvider.java
 *
 * Created on 02.11.11 15:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.merger.provider.ftadata.FTADataProvider;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingProvider;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryProvider;
import de.marketmaker.istar.merger.provider.report.ReportService;

/**
 * @author tkiesgen
 */
@AmqpAddress(queue = "istar.provider.multiProvider")
public interface MultiProvider extends WMDataProvider, RatingDataProvider,
        IssuerRatingProvider, ReportService, RatingHistoryProvider, FTADataProvider {
}
