package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessMscPriceDatas implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List<Quote>) model.get("quotes");
    final String[] symbols = getMajorSymbols(req);
    if (quotes == null || quotes.isEmpty()) {
      return noData(symbols);
    }

    final List<PriceRecord> prices = (List<PriceRecord>) model.get("prices");

    final ArrayList<Access> accesses = new ArrayList<>(quotes.size());
    for (int i = 0; i < quotes.size(); i++) {
      final Quote quote = quotes.get(i);
      if (quote == null) {
        continue;
      }
      final PriceRecord pr = prices.get(i);
      accesses.add(Access.of(quote, pr != null && pr.getPriceQuality() != PriceQuality.NONE
          ? AccessStatus.OK : AccessStatus.NO_DATA));
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
