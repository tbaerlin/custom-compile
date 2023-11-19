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
public class AccessStaticDataList implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List<Quote>) model.get("quotes");
    final String[] majorSymbols = getMajorSymbols(req);
    if (quotes == null || quotes.isEmpty()) {
      return noData(majorSymbols);
    }

    final List<Quote> quoteBenchmarks = (List<Quote>) model.get("quoteBenchmarks");
    final List<List<Quote>> underlyings = (List<List<Quote>>) model.get("underlyings");
    final List<String> sectors = (List<String>) model.get("sectors");

    if (majorSymbols.length == 1) {
      return Access.of(quotes.get(0), getAccessStatus(0, quoteBenchmarks, underlyings, sectors));
    }

    // request and response align with positions in list
    final ArrayList<Access> accesses = new ArrayList<>(majorSymbols.length);
    for (int i = 0; i < majorSymbols.length; i++) {
      final Quote quote = quotes.get(i);
      if (quote == null) {
        accesses.add(Access.of(majorSymbols[i], AccessStatus.NO_DATA));
      } else {
        accesses.add(Access.of(quote, getAccessStatus(i, quoteBenchmarks, underlyings, sectors)));
      }
    }

    return accesses;
  }

  private AccessStatus getAccessStatus(int idx, List<Quote> quoteBenchmarks,
      List<List<Quote>> underlyings, List<String> sectors) {
    if (quoteBenchmarks.get(idx) != null
        || sectors.get(idx) != null
        || (underlyings.get(idx) != null && !underlyings.get(idx).isEmpty())
    ) {
      return AccessStatus.OK;
    }
    return AccessStatus.NO_DATA;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
