package de.marketmaker.istar.merger.provider.analyzer;

import de.marketmaker.istar.analyses.analyzer.ReportView;
import de.marketmaker.istar.common.request.AbstractIstarResponse;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class AnalyzerResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final String query;

    private final ReportView reportView;

    AnalyzerResponse(String serverInfo, String query) {
        super(serverInfo);
        this.query = query;
        this.reportView = new ReportView();
    }


    public AnalyzerResponse(String query, ReportView reportView) {
        super(); // default serverinfo
        this.query = query;
        this.reportView = reportView;
    }

    public String getQuery() {
        return query;
    }

    public ReportView getReportView() {
        return reportView;
    }

}
