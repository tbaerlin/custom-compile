package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
public class AccessMscMostActive implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Set<String> vendors = (Set<String>) model.get("vendors");
    if (vendors == null) {
      return Access.of(AccessStatus.NO_DATA);
    }

    for (String vendor : vendors) {
      final List<Quote> quotes = (List<Quote>) model.get(vendor);
      if (quotes != null && !quotes.isEmpty()) {
        return Access.of(AccessStatus.OK);
      }
    }

    return Access.of(AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
