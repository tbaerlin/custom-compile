package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessIntEntitlement implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {

    final List<Quote> quotes = (List<Quote>) model.get("defaultQuotes");
    final String[] symbols = getMajorSymbols(req);

    // request and response align with positions in list
    final List<Access> accesses = new ArrayList<>(symbols.length);
    for (int i = 0; i < symbols.length; i++) {
      final Quote quote = quotes.get(i);
      if (quote == null) {
        accesses.add(Access.of(symbols[i], AccessStatus.NO_DATA));
      } else {
        accesses.add(Access.of(quote, AccessStatus.OK));
      }
    }

    return accesses.size() > 0 ? accesses : Access.of(AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getSymbol(req);
  }
}
