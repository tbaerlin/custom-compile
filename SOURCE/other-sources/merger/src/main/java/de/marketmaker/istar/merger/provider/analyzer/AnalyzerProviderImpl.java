package de.marketmaker.istar.merger.provider.analyzer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.analyses.analyzer.ReportContext;
import de.marketmaker.istar.analyses.analyzer.ReportViewFactory;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.domain.profile.Profile;


/**
 * generate a report for a set of analysis data
 */
@ManagedResource
public class AnalyzerProviderImpl implements AnalyzerProvider, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // the analysis data
    private ReportContextStore analysesContext;

    public void setAnalysesContext(ReportContextStore analysesContext) {
        this.analysesContext = analysesContext;
    }

    @Override
    public AnalyzerResponse getAnalytics(AnalyzerRequest analyzerRequest) {
        final Profile profile = analyzerRequest.getProfile();
        final String providerId = analyzerRequest.getProviderId();
        Provider provider;
        if (providerId == null || (provider = Provider.valueOf(providerId.toUpperCase())) == null) {
            logger.warn("<getAnalytics> empty or unknown providerId: '" +  providerId + "'");
            AnalyzerResponse response = new AnalyzerResponse(
                    "empty or unknown providerId: '" +  providerId + "'",
                    analyzerRequest.getQuery());
            response.setInvalid();
            return response;
        }
        final ReportViewFactory fab = new ReportViewFactory();
        boolean canParse = fab.setup(analyzerRequest.getQuery());
        if (!canParse) {
            logger.warn("<getAnalytics> can't parse query: '" +  analyzerRequest.getQuery() + "'");
            AnalyzerResponse response = new AnalyzerResponse(
                    "can't parse query ",
                    analyzerRequest.getQuery());
            response.setInvalid();
            return response;
        }
        return new AnalyzerResponse(
                analyzerRequest.getQuery(),
                fab.generateView(analysesContext.getReportContext(provider)));
    }

    @Override
    public void afterPropertiesSet() {

    }

    @ManagedOperation
    public String getStatus() {
        StringBuilder stringBuilder = new StringBuilder();
        analysesContext.getSources().forEach(provider -> {
            stringBuilder.append("\n---- " + provider + " ---- \n");
            final ReportContext content = analysesContext.getReportContext(provider);
            stringBuilder.append(" agencies count: " + content.getAgencies().size() + "\n");
            stringBuilder.append(" analyses count: " + content.getAnalyses().size() + "\n");
            stringBuilder.append(" indices count: " + content.getIndices().size() + "\n");
            stringBuilder.append(" securities count: " + content.getSecurities().size() + "\n");
        });
        return stringBuilder.toString();
    }

}
