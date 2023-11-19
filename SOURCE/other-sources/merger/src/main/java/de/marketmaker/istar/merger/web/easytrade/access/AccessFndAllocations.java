package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.List;
import java.util.Map;

/**
 * @author hummell
 */
public class AccessFndAllocations implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Quote quote = (Quote) model.get("quote");
    return Access.of(quote, hasAllocations(model) ? AccessStatus.OK : AccessStatus.NO_DATA);
  }

  private boolean hasAllocations(Map<String, Object> model) {
    if(model.get("allocations") == null) {
      return false;
    }
    final List<List<InstrumentAllocation>> allocations =
        (List<List<InstrumentAllocation>>) model.get("allocations");
    for (List<InstrumentAllocation> allocation : allocations) {
      if (!allocation.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
