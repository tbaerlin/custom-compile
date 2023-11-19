package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand.ElementDataType;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand.Format;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zzhao
 */
@Slf4j
public class AccessMscHistoricData implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Quote quote = (Quote) model.get("quote");

    final TickDataCommand.Format format = getFormat(req);
    switch (format) {
      case XML:
        final Collection trades = (Collection) model.get(getKey(req));
        return toAccess(quote, getMajorSymbols(req),
            trades != null && !trades.isEmpty() ? AccessStatus.OK : AccessStatus.NO_DATA);
      case PROTOBUF:
        final int numObjs = (int) model.get("numProtobufObjects");
        return toAccess(quote, getMajorSymbols(req),
            numObjs > 0 ? AccessStatus.OK : AccessStatus.NO_DATA);
    }

    throw new UnsupportedOperationException("no support for TickDataCommand.Format: " + format);
  }

  private String getKey(AtomRequest req) {
    switch (getElementDataType(req)) {
      case VOLUME_AGGREGATION:
        return "volumes";
      case FUND:
        return "fundTs";
      default:
        return "trades";
    }
  }

  private ElementDataType getElementDataType(AtomRequest req) {
    final String[] types = getParameter(req, "type");
    if (types != null && types.length > 0 && StringUtils.isNotBlank(types[0])) {
      try {
        return ElementDataType.valueOf(types[0].toUpperCase());
      } catch (Exception e) {
        if (log.isDebugEnabled()) {
          log.debug("<getElementDataType> cannot resolve {} into ElementDataType", types[0], e);
        }
      }
    }

    return ElementDataType.CLOSE;
  }

  private Format getFormat(AtomRequest req) {
    final String[] formats = getParameter(req, "format");
    if (formats != null && formats.length > 0) {
      return TickDataCommand.Format.valueOf(formats[0]);
    }
    return Format.XML;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
