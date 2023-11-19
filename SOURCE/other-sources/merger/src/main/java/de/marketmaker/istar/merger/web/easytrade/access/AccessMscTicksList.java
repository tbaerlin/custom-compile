package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessMscTicksList implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Collection<Map<String, Object>> models = (Collection<Map<String, Object>>) model.get("models");
    if (models == null || models.isEmpty()) {
      return CollectorSupport.toAccess(getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    final List<Access> accessList = new ArrayList<>(models.size());
    for (Map<String, Object> map : models) {
      final Quote quote = (Quote) map.get("quote");
      if (quote != null && !quote.isNullQuote()) {
        accessList.add(Access.of(quote, AccessStatus.OK));
      }
    }

    if (accessList.isEmpty()) {
      return CollectorSupport.toAccess(getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    return accessList;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getSymbol(req);
  }
}
