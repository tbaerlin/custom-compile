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
public class AccessMscInstruments implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String[] majorSymbols = getMajorSymbols(req);
    final List<Quote> quotes = (List<Quote>) model.get("quotes");
    if (quotes == null || quotes.isEmpty()) {
      return noData(majorSymbols);
    }

    final List<List<Quote>> quoteses = (List<List<Quote>>) model.get("quoteses");
    if (majorSymbols.length == 1) { // optimisation: most of the time just one symbol
      return toAccess(quotes.get(0), majorSymbols[0],
          quoteses.get(0).isEmpty() ? AccessStatus.NO_DATA : AccessStatus.OK);
    }

    final ArrayList<Access> accesses = new ArrayList<>(majorSymbols.length);
    for (int i = 0; i < majorSymbols.length; i++) {
      final String symbol = majorSymbols[i];
      final AccessStatus status =
          quoteses.get(i).isEmpty() ? AccessStatus.NO_DATA : AccessStatus.OK;
      accesses.add(
          quotes.get(i) == null ? Access.of(symbol, status) : Access.of(quotes.get(i), status));
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
