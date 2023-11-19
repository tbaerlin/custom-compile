package de.marketmaker.istar.analyses.analyzer;

import java.util.Collection;

import de.marketmaker.istar.analyses.backend.Protos;

/**
 * a data container for a single provider this interface is used for during report generation
 */
public interface ReportContext {

    void addAnalyses(Protos.Analysis analysis);

    PriceCache getPriceCache();

    Collection<Security> getSecurities();

    Collection<Agency> getAgencies();

    Collection<Analysis> getAnalyses();

    Collection<Index> getIndices();

}
