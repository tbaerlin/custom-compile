package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbolStrategy;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.inCandidates;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author hummell
 */
public class AccessFndReportsAvailability implements AtomAccessCollector {

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    List<String> symbols = toArrayList(getMajorSymbols(req));
    final List<Object> candidates = (List<Object>) model.get("quotes");
    CollectionUtils.removeNulls(candidates);
    if (candidates.isEmpty()) {
      final ArrayList<Access> accesses = new ArrayList<>(symbols.size());
      for (String symbol : symbols) {
        accesses.add(Access.of(symbol, AccessStatus.NO_DATA));
      }
      return accesses;
    }

    symbols.removeIf(symbol -> inCandidates(candidates, symbol, getSymbolStrategy(req)));

    final ArrayList<Access> accesses = new ArrayList<>(candidates.size() + symbols.size());
    // for all candidates report OK
    for (Object candidate : candidates) {
      addAccess(accesses, candidate, AccessStatus.OK);
    }
    // for all remaining symbols report no.data
    for (String symbol : symbols) {
      addAccess(accesses, symbol, AccessStatus.NO_DATA);
    }

    return accesses;
  }

  private void addAccess(List<Access> accesses, Object candidate, AccessStatus status) {
    if (candidate instanceof Quote) {
      accesses.add(Access.of((Quote) candidate, status));
      return;
    } else if (candidate instanceof HasQuote) {
      accesses.add(Access.of(((HasQuote) candidate).getQuote(), status));
      return;
    } else if (candidate instanceof String) {
      accesses.add(Access.of((String) candidate, status));
      return;
    }

    throw new IllegalStateException("should not happen, given quote: " + candidate);
  }

  private List<String> toArrayList(String[] symbols) {
    // Arrays.asList does not allow remove operation from iterator
    final ArrayList<String> list = new ArrayList<>(symbols.length);
    Collections.addAll(list, symbols);
    return list;
  }

}
