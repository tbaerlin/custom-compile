package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
public class AccessMscListOverview implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // only one requested symbol
    final String xml = (String) model.get("xml");

    return toAccess(null, getMajorSymbols(req),
        StringUtils.isNoneBlank(xml) ? AccessStatus.OK : AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, "id");
  }
}
