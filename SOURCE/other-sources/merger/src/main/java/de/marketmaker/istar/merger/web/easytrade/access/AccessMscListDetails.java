package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbolStrategy;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.inCandidates;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.toArrayList;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessMscListDetails implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final ListResult listResult = (ListResult) model.get("listinfo");
    final String[] listids = getParameter(req, "listid");
    if (listids != null && listids.length > 0) {
      return Access.of(listids[0], getCount(listResult) > 0 && anyPriceQuality(model)
          ? AccessStatus.OK : AccessStatus.NO_DATA);
    } else {
      final String[] symbols = getSymbol(req);
      if (getCount(listResult) == 0) {
        return CollectorSupport.toAccess(symbols, AccessStatus.NO_DATA);
      }

      final List<Quote> quotes = (List<Quote>) model.get("quotes");
      final List<PriceRecord> prices = (List<PriceRecord>) model.get("prices");
      if (symbols.length == 1) {
        return Access.of(quotes.get(0), prices.get(0).getPriceQuality() == PriceQuality.NONE
            ? AccessStatus.NO_DATA : AccessStatus.OK);
      }

      final List<String> symbolList = toArrayList(symbols);
      final SymbolStrategyEnum symbolStrategy = getSymbolStrategy(req);
      symbolList.removeIf(s -> inCandidates(quotes, s, symbolStrategy));

      final ArrayList<Access> accesses = new ArrayList<>(symbols.length);
      for (int i = 0; i < quotes.size(); i++) {
        accesses.add(Access.of(quotes.get(i), prices.get(i).getPriceQuality() == PriceQuality.NONE
            ? AccessStatus.NO_DATA : AccessStatus.OK));
      }
      symbolList.forEach(s -> accesses.add(Access.of(s, AccessStatus.NO_DATA)));

      return accesses;
    }
  }

  private boolean anyPriceQuality(Map<String, Object> model) {
    final List<PriceRecord> prices = (List<PriceRecord>) model.get("prices");
    if (prices == null) {
      return false;
    }
    for (PriceRecord price : prices) {
      if (price.getPriceQuality() != PriceQuality.NONE) {
        return true;
      }
    }
    return false;
  }

  private int getCount(ListResult listResult) {
    return listResult != null ? listResult.getCount() : 0;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    final String[] listids = getParameter(req, "listid");
    return listids != null && listids.length > 0 ? listids : getSymbol(req);
  }
}
