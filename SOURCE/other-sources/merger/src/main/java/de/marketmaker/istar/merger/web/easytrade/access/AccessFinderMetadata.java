package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;

import de.marketmaker.istar.domainimpl.data.FacetedSearchResult;
import de.marketmaker.istar.merger.query.DistinctValueCounter;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Integrates into DefaultAtomAccessCollector for all blocks extending {@link
 * de.marketmaker.istar.merger.web.easytrade.block.AbstractFinderMetadata}
 *
 * @author zzhao
 */
public class AccessFinderMetadata implements AtomAccessCollector {

  private static final Map<String, Set<String>> MODELKEYSTOIGNORE_BY_ATOMNAME;

  static {
    MODELKEYSTOIGNORE_BY_ATOMNAME = new HashMap<>();

    MODELKEYSTOIGNORE_BY_ATOMNAME.put("STK_FinderMetadata", new HashSet<>(Arrays.asList(
        "byYears",
        "noYears",
        "years"
    )));
  }

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String atomName = req.getName();

    for (Entry<String, Object> entry : model.entrySet()) {
      if (shouldIgnore(entry.getKey(), atomName)) {
        continue;
      }

      final Object value = entry.getValue();
      if (value instanceof Collection && !((Collection) value).isEmpty()) {
        return Access.of(AccessStatus.OK);
      } else if (value instanceof Map && !((Map) value).isEmpty()) {
        return Access.of(AccessStatus.OK);
      } else if (value instanceof DistinctValueCounter) {
        final DistinctValueCounter counter = (DistinctValueCounter) value;
        if (counter.getValues() != null && !counter.getValues().isEmpty()) {
          return Access.of(AccessStatus.OK);
        }
      } else if (value instanceof FacetedSearchResult.Facet) {
        final FacetedSearchResult.Facet facet = (FacetedSearchResult.Facet) value;
        if (facet.getValues() != null && !facet.getValues().isEmpty()) {
          return Access.of(AccessStatus.OK);
        }
      }
    }

    return Access.of(AccessStatus.NO_DATA);
  }

  private boolean shouldIgnore(String key, String atomName) {
    return MODELKEYSTOIGNORE_BY_ATOMNAME.containsKey(atomName)
        && MODELKEYSTOIGNORE_BY_ATOMNAME.get(atomName).contains(key);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
