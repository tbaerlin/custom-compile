package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
public class AccessImgRatioUniverse implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // following fields must be present, see ImgRatioUniverseCommand
    final String typeName = getParameter(req, "type")[0];
    final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(typeName);
    final String field = getParameter(req, "field")[0];

    final Map<Object, Integer> counts = (Map<Object, Integer>) model.get("counts");
    final String request = (String) model.get("request");

    // counts cannot be null
    if (!counts.isEmpty() && StringUtils.isNotBlank(request)) {
      return Access.of(field, typeName, type, AccessStatus.OK);
    } else { // actually won't happen
      return Access.of(field, typeName, type, AccessStatus.NO_DATA);
    }
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, "type");
  }
}
