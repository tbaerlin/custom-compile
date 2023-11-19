package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.HistoricEstimates;
import de.marketmaker.istar.domainimpl.data.NullHistoricEstimates;

/**
 * @author zzhao
 */
public class AccessStkHistoricEstimates extends AccessSymbolAndPredicate {

  public AccessStkHistoricEstimates() {
    super("referencequote", "historicEstimates", obj -> {
      final HistoricEstimates estimates = (HistoricEstimates) obj;
      return !(estimates instanceof NullHistoricEstimates);
    });
  }
}
