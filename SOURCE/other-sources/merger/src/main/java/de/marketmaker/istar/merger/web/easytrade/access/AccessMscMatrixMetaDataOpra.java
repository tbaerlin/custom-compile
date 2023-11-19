package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.AccessMscMatrixMetaData.hasCounts;

import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;

/**
 * @author zzhao
 */
public class AccessMscMatrixMetaDataOpra extends AccessSymbolAndPredicate {

  public AccessMscMatrixMetaDataOpra() {
    super("result", obj -> hasCounts((MatrixMetadataRatioSearchResponse.Node) obj));
  }
}
