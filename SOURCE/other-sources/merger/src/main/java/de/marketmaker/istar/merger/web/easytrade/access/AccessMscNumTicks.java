package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.AggregatedTickEvent;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand.Format;
import java.util.Map;

/**
 * @author zzhao
 */
public class AccessMscNumTicks implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Quote quote = (Quote) model.get("quote");

    final Format format = getFormat(req);
    switch (format) {
      case XML:
        final Iterable<AggregatedTickEvent> ticks =
            (Iterable<AggregatedTickEvent>) model.get("trades");
        return toAccess(quote, getMajorSymbols(req),
            ticks != null && ticks.iterator().hasNext() ? AccessStatus.OK : AccessStatus.NO_DATA);
      case PROTOBUF:
        final int numObjs = (int) model.get("numProtobufObjects");
        return toAccess(quote, getMajorSymbols(req),
            numObjs > 0 ? AccessStatus.OK : AccessStatus.NO_DATA);
    }

    throw new UnsupportedOperationException("no support for TickDataCommand.Format: " + format);
  }

  private Format getFormat(AtomRequest req) {
    final String[] formats = getParameter(req, "format");
    if (formats != null && formats.length > 0) {
      return Format.valueOf(formats[0]);
    }
    return Format.XML;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }
}
