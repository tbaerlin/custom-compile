package de.marketmaker.istar.merger.provider.estimates;

import de.marketmaker.istar.domain.data.StockRevenueSummary;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Estimate data multiplexer with a priority-sorted list of inputs.
 */
public class EstimatesProviderFactsetMux implements EstimatesProviderFactset {

    private boolean ignoreSelector = false;

    private EstimatesProviderFactset[] estimatesProviderFactsets;

    private Selector[] selectors;

    public void setIgnoreSelector(boolean ignoreSelector) {
        this.ignoreSelector = ignoreSelector;
    }

    public void setEstimatesProviderFactsets(EstimatesProviderFactset... estimatesProviderFactsets) {
        this.estimatesProviderFactsets = estimatesProviderFactsets;
    }

    public void setSelectors(Selector... selectors) {
        this.selectors = selectors;
    }

    @Override
    public List<StockRevenueSummary> getEstimates(Profile profile, long instrumentid) {
        return IntStream.range(0, this.estimatesProviderFactsets.length).mapToObj(i ->
                this.getEstimates(this.estimatesProviderFactsets[i], this.selectors[i], profile, instrumentid))
                .filter(summaries -> !summaries.isEmpty())
                .findFirst().orElse(Collections.emptyList());
    }

    private List<StockRevenueSummary> getEstimates(EstimatesProviderFactset provider, Selector selector, Profile profile, long instrumentid) {
        if (this.ignoreSelector || profile.isAllowed(selector)) {
            return provider.getEstimates(profile, instrumentid);
        }

        return Collections.emptyList();
    }

    @Override
    public List<Long> getEstimatesDirectory(Profile profile, DateTime refDate) {
        return IntStream.range(0, this.estimatesProviderFactsets.length).mapToObj(i ->
                this.getEstimatesDirectory(this.estimatesProviderFactsets[i], this.selectors[i], profile, refDate))
                .filter(summaries -> !summaries.isEmpty())
                .findFirst().orElse(Collections.emptyList());
    }

    private List<Long> getEstimatesDirectory(EstimatesProviderFactset provider, Selector selector, Profile profile, DateTime refDate) {
        if (this.ignoreSelector || profile.isAllowed(selector)) {
            return provider.getEstimatesDirectory(profile, refDate);
        }

        return Collections.emptyList();
    }
}
