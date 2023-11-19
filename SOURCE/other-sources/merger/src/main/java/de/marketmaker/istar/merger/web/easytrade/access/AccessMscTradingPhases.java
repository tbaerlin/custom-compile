package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.TradingPhase;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessMscTradingPhases implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List) model.get("quotes");
    final List<List<TradingPhase>> tps = (List) model.get("tps");
    final List<List<TradingPhase>> lastSignals = (List) model.get("lastSignals");

    if (quotes.size() == 0) {
      return CollectorSupport.toAccess(getMajorSymbols(req), AccessStatus.NO_DATA);
    }
    final boolean noTpsData =  tps.isEmpty() || tps.stream().allMatch(List::isEmpty);
    final boolean noSignalData =  lastSignals.isEmpty() || lastSignals.stream().allMatch(List::isEmpty);
    if (noTpsData && noSignalData) {
      return CollectorSupport.toAccess(getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    int index = 0;
    final List<Access> accesses = new ArrayList<>();
    for (Quote quote : quotes) {
      final boolean hasDataForQuote = !tps.get(index).isEmpty() || !lastSignals.get(index).isEmpty();
      accesses.add(Access.of(quote, hasDataForQuote ? AccessStatus.OK : AccessStatus.NO_DATA));
      index++;
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getSymbol(req);
  }
}
