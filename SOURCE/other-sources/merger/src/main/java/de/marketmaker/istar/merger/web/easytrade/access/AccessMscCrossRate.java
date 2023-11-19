package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.util.QuoteRef;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * @author hummell
 */
public class AccessMscCrossRate implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    QuoteRef quoteRef = (QuoteRef) model.get("quoteRef");
    if (quoteRef == null || quoteRef.getQuote() == null) {
      return Access.of(AccessStatus.OK);
    }
    return Access.of(quoteRef.getQuote(), AccessStatus.OK);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return new String[]{Arrays.toString(getParameter(req, "isocodeFrom")) + "-" +
        getParameter(req, "isocodeTo")};
  }
}
