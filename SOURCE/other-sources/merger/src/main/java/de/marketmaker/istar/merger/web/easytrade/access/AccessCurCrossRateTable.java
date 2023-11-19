package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.block.CurCrossRateTable.CrossRateCell;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zzhao
 */
public class AccessCurCrossRateTable implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<CrossRateCell> cells = (List<CrossRateCell>) model.get("cells");
    if (cells == null || cells.isEmpty()) {
      return noData(getMajorSymbols(req));
    }

    return cells.stream()
        .map(c -> {
          final PriceRecord pr = c.getPriceRecord();
          return Access.of(c.getQuote(), pr != null && pr.getPriceQuality() != PriceQuality.NONE
              ? AccessStatus.OK : AccessStatus.NO_DATA);
        })
        .collect(Collectors.toList());
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    final String[] isoCodes = getParameter(req, "isocode");
    return isoCodes.length > 0 ? isoCodes : getParameter(req, "isocodeSourceTarget");
  }
}
