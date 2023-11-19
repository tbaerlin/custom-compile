package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class AccessMscTopProducts implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List<Quote>) model.get("underlyingQuotes");
    final String[] symbols = getMajorSymbols(req);
    if (quotes == null || quotes.isEmpty()) {
      if (symbols.length == 0) {
        return Access.of(getIssuername(req), getProductType(req), AccessStatus.NO_DATA);
      }
      return noData(symbols);
    }

    final Map<String, Object> table = (Map<String, Object>) model.get("table");
    final ArrayList<Access> accesses = new ArrayList<>(quotes.size());
    for (final Quote quote : quotes) {
      if (quote == null || quote.isNullQuote()) {
        continue;
      }
      final List bestToolElementsForQuote = (List) table.get(String.valueOf(quote.getInstrument().getId()));
      accesses.add(Access.of(quote, bestToolElementsForQuote != null && bestToolElementsForQuote.size() > 0
              ? AccessStatus.OK : AccessStatus.NO_DATA));
    }

    return accesses;
  }

  private String getIssuername(AtomRequest req) {
    final String issuername = getParameter(req, "issuername")[0];
    return StringUtils.getIfBlank(issuername, () -> Access.SYMBOL_DEFAULT);
  }

  private String getProductType(AtomRequest req) {
    return getParameter(req, "productType")[0];
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }


}
