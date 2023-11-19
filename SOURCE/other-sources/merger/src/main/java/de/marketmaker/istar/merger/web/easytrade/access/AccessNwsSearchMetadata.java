package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Collection;
import java.util.Map;

public class AccessNwsSearchMetadata implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {

    final Object topics = model.get("themengebiete");
    final Object sectors = model.get("branchen");
    final Object regions = model.get("regionen");

    if ((topics instanceof Collection && !((Collection<?>) topics).isEmpty())
        || (sectors instanceof Collection && !((Collection<?>) sectors).isEmpty())
        || (regions instanceof Collection && !((Collection<?>) regions).isEmpty())) {
      return Access.of(AccessStatus.OK);
    }

    return Access.of(AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
