package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessMscQuotesList implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Map<String, Map<String, Object>> result =
        (Map<String, Map<String, Object>>) model.get("result");
    final String[] symbols = getMajorSymbols(req);
    if (result == null || result.isEmpty()) {
      return noData(symbols);
    }

    final ArrayList<Access> accesses = new ArrayList<>(symbols.length);
    for (String symbol : symbols) {
      final Map<String, Object> map = result.get(symbol);
      // map will never be null or empty
      final Quote quote = (Quote) map.get("quote");
      final List<Quote> quotes = (List<Quote>) map.get("quotes");
      accesses.add(toAccess(quote, symbol,
          quotes == null || quotes.isEmpty() ? AccessStatus.NO_DATA : AccessStatus.OK));
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
