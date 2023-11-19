/*
 * DelegatingMultiProvider.java
 *
 * Created on 02.11.11 15:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;
import java.util.Map;

import de.marketmaker.istar.merger.provider.ftadata.FTADataProvider;
import de.marketmaker.istar.merger.provider.ftadata.FTADataRequest;
import de.marketmaker.istar.merger.provider.ftadata.FTADataResponse;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingMetaDataKey;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingProvider;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingSearchRequest;
import de.marketmaker.istar.merger.provider.rating.IssuerRatingSearchResponse;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryProvider;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryRequest;
import de.marketmaker.istar.merger.provider.rating.history.RatingHistoryResponse;
import de.marketmaker.istar.merger.provider.report.ReportRequest;
import de.marketmaker.istar.merger.provider.report.ReportResponse;
import de.marketmaker.istar.merger.provider.report.ReportService;
import de.marketmaker.istar.merger.web.easytrade.FinderMetaItem;

/**
 * @author tkiesgen
 */
public class DelegatingMultiProviderImpl implements MultiProvider {
    private RatingDataProvider ratingDataProvider;

    private WMDataProvider wmDataProvider;

    private IssuerRatingProvider issuerRatingProvider;

    private ReportService reportService;

    private RatingHistoryProvider ratingHistoryProvider;

    private FTADataProvider ftaDataProvider;

    public void setRatingHistoryProvider(RatingHistoryProvider ratingHistoryProvider) {
        this.ratingHistoryProvider = ratingHistoryProvider;
    }

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }

    public void setRatingDataProvider(RatingDataProvider ratingDataProvider) {
        this.ratingDataProvider = ratingDataProvider;
    }

    public void setWmDataProvider(WMDataProvider wmDataProvider) {
        this.wmDataProvider = wmDataProvider;
    }

    public void setIssuerRatingProvider(IssuerRatingProvider issuerRatingProvider) {
        this.issuerRatingProvider = issuerRatingProvider;
    }

    public void setFtaDataProvider(FTADataProvider ftaDataProvider) {
        this.ftaDataProvider = ftaDataProvider;
    }

    @Override
    public RatingDataResponse getData(RatingDataRequest request) {
        return this.ratingDataProvider.getData(request);
    }

    @Override
    public WMDataResponse getData(WMDataRequest request) {
        return this.wmDataProvider.getData(request);
    }

    @Override
    public Map<IssuerRatingMetaDataKey, List<Object>> getMetaData(boolean withDetailedSymbol) {
        return this.issuerRatingProvider.getMetaData(withDetailedSymbol);
    }

    @Override
    public IssuerRatingSearchResponse search(IssuerRatingSearchRequest req) {
        return this.issuerRatingProvider.search(req);
    }

    @Override
    public ReportResponse getReports(ReportRequest req) {
        return this.reportService.getReports(req);
    }

    @Override
    public RatingHistoryResponse getRatingHistory(RatingHistoryRequest req) {
        return this.ratingHistoryProvider.getRatingHistory(req);
    }

    @Override
    public Map<String, List<FinderMetaItem>> getRatingHistoryMetaData() {
        return this.ratingHistoryProvider.getRatingHistoryMetaData();
    }

    @Override
    public FTADataResponse getFTAData(FTADataRequest request) {
        return ftaDataProvider.getFTAData(request);
    }
}
