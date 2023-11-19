package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;

/**
 * @author zzhao
 */
public class AccessMscMatrixMetaData extends AccessSymbolAndPredicate {

  public AccessMscMatrixMetaData() {
    super("underlyingQuote", "result",
        obj -> hasCounts((MatrixMetadataRatioSearchResponse.Node) obj));
  }

  static boolean hasCounts(MatrixMetadataRatioSearchResponse.Node node) {
    if (node == null) {
      return false;
    }

    if (node.isLeaf() && !node.getCounts().isEmpty()) {
      return true;
    }

    while (node.getChildren() != null) {
      for (MatrixMetadataRatioSearchResponse.Node child : node.getChildren().values()) {
        if (hasCounts(child)) {
          return true;
        }
      }
    }

    return false;
  }
}
