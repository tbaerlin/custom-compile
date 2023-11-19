package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessMscTopFlop implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Integer n1 = (Integer) model.getOrDefault("numUp", 0);
    final Integer n2 = (Integer) model.getOrDefault("numDown", 0);
    final Integer n3 = (Integer) model.getOrDefault("numUnchanged", 0);

    final String[] symbols = getMajorSymbols(req);
    if (n1 > 0 || n2 > 0 || n3 > 0) {
      // check price data deeply since sometimes we get no price data
      // even if the numbers are greater than zero
      final ArrayList<PriceRecord> prices =
          (ArrayList<PriceRecord>) model.getOrDefault("prices", Collections.emptyList());
      final boolean hasPriceData =
          prices.stream().anyMatch(p -> !p.getPriceQuality().equals(PriceQuality.NONE));
      final AccessStatus status = hasPriceData ? AccessStatus.OK : AccessStatus.NO_DATA;

      final Quote indexQuote = (Quote) model.get("indexQuote");
      if (indexQuote != null && !indexQuote.isNullQuote()) {
        return Access.of(indexQuote, status);
      }
      if (symbols.length > 0) {
        // find quotes and eleminates duplications
        final Set<Quote> quotes = new HashSet((Collection) model.get("quotes"));
        final List<Access> accesses = new ArrayList<>(quotes.size());
        for (Quote q : quotes) {
          accesses.add(Access.of(q, status));
        }
        return accesses;
      }
      return CollectorSupport.toAccess(symbols, status);
    }

    return CollectorSupport.toAccess(symbols, AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    final String[] listId = CollectorSupport.getListId(req);
    if (listId.length == 0) {
      return CollectorSupport.getSymbol(req);
    }
    return listId;
  }
}
