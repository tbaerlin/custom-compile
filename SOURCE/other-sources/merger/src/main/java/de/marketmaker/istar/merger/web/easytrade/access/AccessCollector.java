package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Collects accesses via {@link AtomAccessCollector}.
 *
 * @author zzhao
 */
@RequiredArgsConstructor
@Slf4j
public class AccessCollector {

  /**
   * See accessContext.xml
   */
  private final Map<String, AtomAccessCollector> collectorByAtomName;

  private final AtomAccessCollector defaultAtomAccessCollector;

  private final AtomAccessCollector collectorForFinder;

  private final AtomAccessCollector collectorForFinderMeta;

  private final AtomAccessCollector collectorForPriceData;

  private final AtomAccessCollector collectorForRatioData;

  private final AtomAccessCollector collectorForStaticData;

  /**
   * Collects all quotes and states that should be reported during processing of the given atom.
   *
   * @param req the associated atom request
   * @param model model map after processing the given atom
   * @return an {@link Access} or a collection of {@link Access}es, never null
   */
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String atomName = req.getName();
    // handle special cases based on atomName
    // fallback to general approach to just collect main quote or quotes (not benchmark quotes etc.)
    return getAtomAccessCollector(atomName).collect(req, model);
  }

  /**
   * @param req atom request
   * @return an array of symbols, can be of length 0, but never null
   */
  public String[] getMajorSymbols(AtomRequest req) {
    return getAtomAccessCollector(req.getName()).getMajorSymbols(req);
  }

  private AtomAccessCollector getAtomAccessCollector(String atomName) {
    AtomAccessCollector atomAccessCollector =
        this.collectorByAtomName == null ? null : this.collectorByAtomName.get(atomName);
    if (atomAccessCollector != null) {
      return atomAccessCollector;
    }

    if (atomName.endsWith("_PriceData")) {
      return this.collectorForPriceData;
    }

    if (atomName.endsWith("_RatioData")) {
      return this.collectorForRatioData;
    }

    if (atomName.endsWith("StaticData") || atomName.endsWith("StaticDataExtension")) {
      return this.collectorForStaticData;
    }

    if (atomName.endsWith("FinderMetadata") || atomName.endsWith("FinderMetaData")) {
      return this.collectorForFinderMeta;
    }

    if (atomName.endsWith("Finder")) {
      return this.collectorForFinder;
    }

    return this.defaultAtomAccessCollector;
  }
}
