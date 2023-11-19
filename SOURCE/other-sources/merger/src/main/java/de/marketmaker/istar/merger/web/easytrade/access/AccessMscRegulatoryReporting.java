package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.data.NullRegulatoryReportingRecord;
import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

public class AccessMscRegulatoryReporting implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final RegulatoryReportingRecord record = (RegulatoryReportingRecord) model.get("record");
    if (isNotNull(record)) {
      final Quote quote = (Quote) model.get("quote");
      return Access.of(quote, AccessStatus.OK);
    }

    return noData(getMajorSymbols(req));
  }

  private boolean isNotNull(RegulatoryReportingRecord record) {
    return record != null && record != NullRegulatoryReportingRecord.INSTANCE;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }


}
