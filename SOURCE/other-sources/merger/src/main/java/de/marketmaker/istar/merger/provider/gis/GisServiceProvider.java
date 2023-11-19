/*
 * DynamicPibService.java
 *
 * Created on 04.04.14 09:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gis;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author oflege
 */
@AmqpAddress(queue = "istar.gis.service")
public interface GisServiceProvider {
    DynamicGisReportListResponse fetchReportList(DynamicGisReportListRequest dynamicPibRequest);
    DynamicGisReportResponse fetchReport(DynamicGisReportRequest dynamicGisReportRequest);
}
