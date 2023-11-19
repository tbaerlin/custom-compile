package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;

/**
 * @author zzhao
 */
public class AccessMscLexikonEntry extends AccessSymbolAndPredicate {

  public AccessMscLexikonEntry() {
    super("element");
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    final String[] ids = getParameter(req, "id");
    return ids != null && ids.length > 0 ? ids : getParameter(req, "initial");
  }
}
