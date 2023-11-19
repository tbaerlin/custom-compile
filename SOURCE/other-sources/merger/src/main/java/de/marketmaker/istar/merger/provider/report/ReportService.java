/*
 * ReportService.java
 *
 * Created on 16.05.12 16:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.report;

/**
 * @author zzhao
 */
public interface ReportService {

    ReportResponse getReports(ReportRequest req);
}
