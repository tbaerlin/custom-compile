package de.marketmaker.istar.merger.web.easytrade.block;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.domain.profile.Selector;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchProvider;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchRequest;
import de.marketmaker.istar.merger.provider.gisresearch.GisResearchResponse;


/**
 * <p>
 * Retrieves meta data related to GIS_ResearchFinder.
 * </p>
 * <p>
 * Returns fields that are relevant for categorizing, querying and sorting research items.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisResearchFinderMetadata implements AtomController {

    private GisResearchProvider gisResearchProvider;

    public void setGisResearchProvider(GisResearchProvider gisResearchProvider) {
        this.gisResearchProvider = gisResearchProvider;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        EasytradeCommandController.checkPermission(Selector.DZ_RESEARCH);

        GisResearchRequest r = new GisResearchRequest();
        final GisResearchResponse gisResponse = this.gisResearchProvider.search(r);

        final Map<String, Object> model = new HashMap<>();
        FacetedSearchResult fsr = gisResponse.getFacetedSearchResult();
        for (FacetedSearchResult.Facet f : fsr.withFacetValuesSortedByName(Locale.GERMAN).getFacets()) {
            model.put(f.getId(), f);
        }
        return new ModelAndView("gisresearchfindermetadata", model);
    }
}
