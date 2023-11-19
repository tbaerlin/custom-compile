package de.marketmaker.istar.merger.provider.estimates;

import de.marketmaker.istar.domain.data.StockRevenueSummary;
import de.marketmaker.istar.domain.profile.Profile;
import org.joda.time.DateTime;

import java.util.List;

public interface EstimatesProviderFactset {
    List<StockRevenueSummary> getEstimates(Profile profile, long instrumentid);

    List<Long> getEstimatesDirectory(Profile profile, DateTime refDate);
}
