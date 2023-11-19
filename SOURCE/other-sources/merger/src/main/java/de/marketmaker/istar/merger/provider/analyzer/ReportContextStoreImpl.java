package de.marketmaker.istar.merger.provider.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.analyses.analyzer.PriceCache;
import de.marketmaker.istar.analyses.analyzer.ReportContext;
import de.marketmaker.istar.analyses.analyzer.ReportContextImpl;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;

/**
 *  container for report relevant analyses data from multiple providers also contains dynamic
 *  price data needed to calculate success and potential values
 */
public class ReportContextStoreImpl implements ReportContextStore, InitializingBean {

    // DPA-AFX, AWP
    private final ArrayList<Provider> sources = new ArrayList<>(Arrays.asList(Provider.DPAAFX));

    // storing the analysis specific (static-)data for each provider
    private final Map<Provider, ReportContextImpl> analysesData = new HashMap<>();

    // shared by all providers
    private PriceCache priceCache;

    public void setPriceCache(PriceCache priceCache) {
        this.priceCache = priceCache;
    }

    public void setSources(List<Provider> sources) {
        this.sources.clear();
        this.sources.addAll(sources);
    }

    @Override
    public Stream<Provider> getSources() {
        return sources.stream();
    }

    @Override
    public ReportContext getReportContext(Provider provider) {
        return analysesData.get(provider);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (priceCache == null) {
            throw new IllegalStateException("<afterPropertiesSet> PriceCache must not be null");
        }
        this.analysesData.clear();
        // setup a separate data container for each provider
        this.sources.forEach(provider -> {
            analysesData.put(provider, new ReportContextImpl(priceCache));
        });
    }

}
