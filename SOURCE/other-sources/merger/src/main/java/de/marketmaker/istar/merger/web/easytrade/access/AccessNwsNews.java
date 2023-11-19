package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessNwsNews implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Object data = model.get("item");
    final String[] symbols = getMajorSymbols(req);
    if (data == null) {
      return CollectorSupport.toAccess(symbols, AccessStatus.NO_DATA);
    }

    return CollectorSupport.toAccess(symbols, AccessStatus.OK);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getParameter(req, "newsid");
  }
}
