package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

public class AccessGisEbrokeragePrices implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Quote quote = (Quote) model.get("quote");

    if (quote == null || quote.isNullQuote()) {
      return CollectorSupport.toAccess(getMajorSymbols(req), AccessStatus.NO_DATA);
    }

    final PriceRecord priceRecord = (PriceRecord) model.get("price");
    final boolean hasPriceData = priceRecord != null &&
        priceRecord != NullPriceRecord.INSTANCE &&
        !priceRecord.getPriceQuality().equals(PriceQuality.NONE);

    return Access.of(quote, hasPriceData ? AccessStatus.OK : AccessStatus.NO_DATA);
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, "wkn");
  }
}
