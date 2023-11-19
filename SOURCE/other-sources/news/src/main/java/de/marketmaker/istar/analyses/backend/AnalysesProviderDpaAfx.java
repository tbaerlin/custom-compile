package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.analyses.backend.Protos.Analysis.Provider;
import de.marketmaker.istar.analyses.frontend.AnalysesMetaRequest;
import de.marketmaker.istar.analyses.frontend.AnalysisImpl;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.data.StockAnalysis.Recommendation;
import de.marketmaker.istar.domain.profile.Selector;

/**
 *
 *
 * @author oflege
 */
@ManagedResource
public class AnalysesProviderDpaAfx extends PushedAnalysesProvider {

    private Properties countryMappings;

    private Properties sectorMappings;

    @ManagedOperation
    public void dumpState(String filename) throws IOException {
        dumpState(new File(filename));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.countryMappings = loadProperties("dpaafx-countries.properties");
        this.sectorMappings = loadProperties("dpaafx-sectors.properties");
    }

    private Properties loadProperties(final String path) throws IOException {
        return PropertiesLoader.load(new ClassPathResource(path, getClass()));
    }

    @Override
    protected String getId() {
        return "dpaafx";
    }

    @SuppressWarnings("Duplicates")
    @Override
    Map<String, Map<String, String>> doGetMetaData(AnalysesMetaRequest request) {
        final Map<String, Map<String, String>> result = new HashMap<>();
        result.put("ratings", createRatingsMetaMap());
        result.put("analysts", getSources(request.isIgnoreAnalysesWithoutRating()));
        result.put("sectors", getSectors(request.isIgnoreAnalysesWithoutRating()));
        result.put("regions", getRegions(request.isIgnoreAnalysesWithoutRating()));
        return result;
    }

    @SuppressWarnings("Duplicates")
    private Map<String, String> createRatingsMetaMap() {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put(Recommendation.BUY.name(), "Kaufen");
        result.put(Recommendation.HOLD.name(), "Halten");
        result.put(Recommendation.SELL.name(), "Verkaufen");
        return result;
    }

    private LinkedHashMap<String, String> getRegions(boolean ignoreAnalysesWithoutRating) {
        final Set<String> tmpRegions = new HashSet<>();
        addRegions(tmpRegions, ignoreAnalysesWithoutRating);
        return toMap(tmpRegions, this.countryMappings);
    }

    private LinkedHashMap<String, String> getSectors(boolean ignoreAnalysesWithoutRating) {
        final Set<String> tmpSectors = new HashSet<>();
        addBranches(tmpSectors, ignoreAnalysesWithoutRating);
        return toMap(tmpSectors, this.sectorMappings);
    }

    private LinkedHashMap<String, String> toMap(Set<String> items, Properties mappings) {
        final Map<String, String> ordered = new TreeMap<>(GERMAN_COLLATOR);
        for (String item : items) {
            final String mapped = mappings.getProperty(item);
            if (mapped != null) {
                ordered.put(mapped, item);
            }
        }
        final LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Entry<String, String> entry : ordered.entrySet()) {
            result.put(entry.getValue(), entry.getKey());
        }
        return result;
    }

    @Override
    public Selector getSelector() {
        return Selector.DPA_AFX_ANALYSES;
    }

    @Override
    public Provider getProvider() {
        return Provider.DPAAFX;
    }

    @Override
    protected String getSectorName(String sector) {
        if (sector == null) {
            return null;
        }
        final String name = this.sectorMappings.getProperty(sector);
        return (name != null) ? name : super.getSectorName(sector);
    }

    @Override
    public List<AnalysisImpl> getAnalyses(List<Long> ids) {
        final List<AnalysisImpl> result = super.getAnalyses(ids);
        for (AnalysisImpl analysis : result) {
            if (analysis != null && analysis.getSector() != null) {
                analysis.setSector(this.sectorMappings.getProperty(analysis.getSector()));
            }
        }
        return result;
    }

}
