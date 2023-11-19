package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.news.frontend.NewsRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessInsNews implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<NewsRecord> records =
        (List<NewsRecord>) model.getOrDefault("records", Collections.emptyList());
    final String[] symbols = getMajorSymbols(req);
    if (records.isEmpty()) {
      return CollectorSupport.toAccess(symbols, AccessStatus.NO_DATA);
    }

    int index = 0;
    final List<Access> accesses = new ArrayList<>();
    for (String symbol : symbols) {
      final boolean hasNewsRecord = records.size() > index++;
      accesses.add(Access.of(symbol, hasNewsRecord ? AccessStatus.OK : AccessStatus.NO_DATA));
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getParameter(req, "newsid");
  }
}
