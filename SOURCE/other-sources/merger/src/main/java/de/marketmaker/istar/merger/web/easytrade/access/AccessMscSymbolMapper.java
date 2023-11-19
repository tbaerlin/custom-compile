package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.data.NullRegulatoryReportingRecord;
import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccessMscSymbolMapper implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String[] symbols = getMajorSymbols(req);
    final List<Quote> quotes = (List<Quote>) model.get("quotes");
    if (quotes != null && !quotes.isEmpty()) {
      final List<Access> accesses = new ArrayList<>();
      int index = 0;
      for (Quote quote : quotes) {
        if (quote != null) {
          accesses.add(Access.of(quote, AccessStatus.OK));
        } else {
          accesses.add(Access.of(symbols[index], AccessStatus.NO_DATA));
        }
        index++;
      }

      return accesses;
    }

    return noData(symbols);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }


}
