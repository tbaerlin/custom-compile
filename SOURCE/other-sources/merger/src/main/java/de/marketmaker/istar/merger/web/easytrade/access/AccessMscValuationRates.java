package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessMscValuationRates implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List) model.getOrDefault("quotes", Collections.emptyList());

    if (quotes.isEmpty()) {
      return CollectorSupport.toAccess(getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    final ArrayList<PriceRecord> prices =
        (ArrayList<PriceRecord>) model.getOrDefault("prices", Collections.emptyList());

    int index = 0;
    final List<Access> accesses = new ArrayList<>();
    for (Quote quote : quotes) {
      final boolean hasQuote = quote != null && !quote.isNullQuote();
      final boolean hasPriceData =
          prices.size() > index && !prices.get(index).getPriceQuality().equals(PriceQuality.NONE);
      accesses.add(Access.of(quote, (hasQuote && hasPriceData) ? AccessStatus.OK : AccessStatus.NO_DATA));
      index++;
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getSymbol(req);
  }
}
