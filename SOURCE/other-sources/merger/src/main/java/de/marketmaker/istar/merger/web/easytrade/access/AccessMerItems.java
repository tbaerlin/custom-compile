package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AccessMerItems implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {

    final Map<String, Quote> quotes =
        (Map<String, Quote>) model.getOrDefault("quoteByQid", Collections.emptyMap());
    final Map<String, PriceRecord> prices =
        (Map<String, PriceRecord>) model.getOrDefault("priceByQid", Collections.emptyMap());

    if (quotes.isEmpty() || prices.isEmpty()) {
      return noDataAccesses(req);
    }

    final List<Access> accesses = new ArrayList<>();
    for (String quoteId : quotes.keySet()) {
      final Quote quote = quotes.get(quoteId);
      final PriceRecord priceRecord = prices.get(quoteId);
      AccessStatus status = AccessStatus.NO_DATA;
      final boolean hasPriceData = priceRecord != null
          && priceRecord.getPrice() != null
          && !priceRecord.getPriceQuality().equals(PriceQuality.NONE)
          && !(priceRecord.getPrice() instanceof NullPrice);
      if (hasPriceData) {
        status = AccessStatus.OK;
      }
      accesses.add(Access.of(quote, status));
    }

    return accesses;
  }

  private List<Access> noDataAccesses(AtomRequest req) {
    final String[] countries = getParameter(req, "country");
    final String[] types = getParameter(req, "type");
    final List<Access> accesses = new ArrayList<>();
    for (String country : countries) {
      for (String type : types) {
        accesses.add(Access.of(country, type, AccessStatus.NO_DATA));
      }
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }


}
