package de.marketmaker.istar.merger.provider.analyzer;

import java.util.stream.Stream;

import de.marketmaker.istar.analyses.analyzer.ReportContext;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;


/**
 * data container for multiple analyses provider
 */
public interface ReportContextStore {

    // retrieve a provider specific data container
    ReportContext getReportContext(Provider provider);

    // return a list of available providers
    Stream<Provider> getSources();

}
