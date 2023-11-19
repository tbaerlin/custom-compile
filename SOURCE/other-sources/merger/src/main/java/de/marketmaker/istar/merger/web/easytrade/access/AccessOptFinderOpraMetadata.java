package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.MapUtils;

@RequiredArgsConstructor
public class AccessOptFinderOpraMetadata implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Map underlyingSymbol = (Map) model.get("underlyingSymbol");
    final Map symbol = (Map) model.get("symbol");
    final Map optionType = (Map) model.get("optionType");
    final Map market = (Map) model.get("market");

    final boolean hasNoData = MapUtils.isEmpty(underlyingSymbol) &&
        MapUtils.isEmpty(symbol) && MapUtils.isEmpty(optionType) && MapUtils.isEmpty(market);

    if (hasNoData) {
      return Access.of(AccessStatus.NO_DATA);
    }

    return Access.of(AccessStatus.OK);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
