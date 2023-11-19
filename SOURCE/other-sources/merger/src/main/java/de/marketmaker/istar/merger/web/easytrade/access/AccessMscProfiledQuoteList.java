package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbolStrategy;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.inCandidates;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.toArrayList;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessMscProfiledQuoteList implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List<Quote>) model.get("quotes");
    final String[] symbols = getMajorSymbols(req);
    if (quotes == null || quotes.isEmpty()) {
      return noData(symbols);
    }

    final List<String> symbolList = toArrayList(symbols);
    final SymbolStrategyEnum symbolStrategy = getSymbolStrategy(req);
    symbolList.removeIf(s -> inCandidates(quotes, s, symbolStrategy));

    final ArrayList<Access> accesses = new ArrayList<>(symbols.length);
    for (int i = 0; i < quotes.size(); i++) {
      final Quote quote = quotes.get(i);
      if (quote == null) {
        continue;
      }
      accesses.add(Access.of(quote, AccessStatus.OK));
    }

    symbolList.forEach(s -> accesses.add(Access.of(s, AccessStatus.NO_DATA)));

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
