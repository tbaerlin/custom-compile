package de.marketmaker.istar.analyses.analyzer;

import de.marketmaker.istar.analyses.backend.Protos;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;

/**
 * process incoming analyses
 */
public interface AnalysesCollector {

    AnalysesCollector NULL = (provider, analysis) -> {
        // do nothing
    };

    // to signal empty values
    int NULL_DATE = Integer.MIN_VALUE;
    int NULL_QID = Integer.MIN_VALUE;
    String NULL_VWDCODE = "";
    String NULL_CURRENCY = "";
    String NULL_RATIO_STRING = "";

    void addAnalysis(Provider provider, Protos.Analysis analysis);

}
