package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hummell
 */
public class AccessFndFeriPerformances implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Quote quote = (Quote) model.get("quote");
    final Iterator<DataWithInterval<BigDecimal>> fund =
        (Iterator<DataWithInterval<BigDecimal>>) model.get("fund");
    if (fund == null || !fund.hasNext()) {
      return Access.of(quote, AccessStatus.NO_DATA);
    } else {
      return Access.of(quote, AccessStatus.OK);
    }
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
