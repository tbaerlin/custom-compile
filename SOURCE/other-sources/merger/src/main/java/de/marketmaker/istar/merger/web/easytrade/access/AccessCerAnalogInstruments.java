package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessCerAnalogInstruments implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String reqSymbol = getRequestSymbol(req);
    final Object quotes = model.get("quotes");
    if (quotes instanceof Collection && !((Collection<?>) quotes).isEmpty()) {
      return Access.of(reqSymbol, AccessStatus.OK);
    }

    return Access.of(reqSymbol, AccessStatus.NO_DATA);
  }

  private String getRequestSymbol(AtomRequest req) {
    final String[] majorSymbols = getMajorSymbols(req);
    return (majorSymbols.length > 0) ? majorSymbols[0] : Access.SYMBOL_DEFAULT;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getSymbol(req);
  }



}
