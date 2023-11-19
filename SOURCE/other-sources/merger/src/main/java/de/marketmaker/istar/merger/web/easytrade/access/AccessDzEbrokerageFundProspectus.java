package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.Access.SYMBOL_DEFAULT;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class AccessDzEbrokerageFundProspectus implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List<Quote>) model.get("quotes");
    final List<List<DownloadableItem>> reports = (List<List<DownloadableItem>>) model.get("reports");

    if (quotes.isEmpty()) {
      return noData(req);
    }

    int index = 0;
    final List<Access> accessList = new ArrayList<>();
    for (Quote quote : quotes) {
      if (quote != null && !quote.isNullQuote()) {
        final List<DownloadableItem> items = reports.get(index);
        final boolean hasReport = items != null && !items.isEmpty() && items.stream().anyMatch(d -> StringUtils.isNotBlank(d.getUrl()));
        accessList.add(Access.of(quote.getInstrument(), hasReport ? AccessStatus.OK : AccessStatus.NO_DATA));
      }
      index++;
    }

    return accessList.isEmpty() ? noData(req) : accessList;
  }

  private Access noData(AtomRequest request) {
    return Access.of(getIssuerName(request), getName(request), AccessStatus.NO_DATA);
  }

  private String getIssuerName(AtomRequest request) {
    final String[] issuername = getParameter(request, "issuername");
    return (issuername.length > 0) ? issuername[0] : SYMBOL_DEFAULT;
  }

  private String getName(AtomRequest request) {
    final String[] name = getParameter(request, "name");
    return (name.length > 0) ? name[0] : SYMBOL_DEFAULT;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
