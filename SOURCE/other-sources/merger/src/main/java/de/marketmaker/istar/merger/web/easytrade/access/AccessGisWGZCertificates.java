package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.WGZCertificateData;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessGisWGZCertificates implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Object data = model.get("data");
    if (data instanceof Collection && !((Collection<?>) data).isEmpty()) {
      final Object firstElement = ((Collection<?>) data).iterator().next();
      if (firstElement instanceof WGZCertificateData) {
        return Access.of(AccessStatus.OK);
      }
    }

    return Access.of(AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.EMPTY_STRING_ARRAY;
  }
}
