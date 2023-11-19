package de.marketmaker.istar.merger.web.easytrade.block;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;
import de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchProvider;
import de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchRequest;
import de.marketmaker.istar.merger.provider.lbbwresearch.LbbwResearchResponse;


/**
 * <p>
 * Retrieves meta data related to LBBW_ResearchFinder.
 * </p>
 * <p>
 * Returns fields that are relevant for categorizing, querying and sorting research items.
 * </p>
 * @author mcoenen
 */
public class LbbwResearchFinderMetadata implements AtomController {

    private LbbwResearchProvider lbbwResearchProvider;

    public void setLbbwResearchProvider(LbbwResearchProvider lbbwResearchProvider) {
        this.lbbwResearchProvider = lbbwResearchProvider;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        EasytradeCommandController.checkPermission(Selector.LBBW_RESEARCH);

        LbbwResearchRequest r = new LbbwResearchRequest();
        final LbbwResearchResponse lbbwResponse = this.lbbwResearchProvider.search(r);

        final Map<String, Object> model = new HashMap<>();
        FacetedSearchResult fsr = lbbwResponse.getFacetedSearchResult();
        for (FacetedSearchResult.Facet f : fsr.withFacetValuesSortedByName(Locale.GERMAN).getFacets()) {
            model.put(f.getId(), f);
        }
        return new ModelAndView("lbbwresearchfindermetadata", model);
    }
}
