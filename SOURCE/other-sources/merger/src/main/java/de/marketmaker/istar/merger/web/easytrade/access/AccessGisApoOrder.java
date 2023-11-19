package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AccessGisApoOrder implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Quote> quotes = (List<Quote>) model.get("quotes");

    if (quotes.isEmpty()) {
      return noData(req, model);
    }

    int index = 0;
    final List<PriceRecord> priceRecords = (List<PriceRecord>) model.get("priceRecords");
    final List<Access> accessList = new ArrayList<>();
    for (Quote quote : quotes) {
      if (quote != null && !quote.isNullQuote()) {
        final PriceRecord priceRecord = priceRecords.get(index);
        final boolean hasPriceData = priceRecord != null &&
            priceRecord != NullPriceRecord.INSTANCE &&
            !priceRecord.getPriceQuality().equals(PriceQuality.NONE);
        accessList.add(Access.of(quote, hasPriceData ? AccessStatus.OK : AccessStatus.NO_DATA));
      }
      index++;
    }

    // also collect unknown symbols if reported
    accessList.addAll(collectUnknownSymbols(model));

    return accessList.isEmpty() ? noData(req, model) : accessList;
  }

  private List<Access> collectUnknownSymbols(Map<String, Object> model) {
    final List<Access> accessList = new ArrayList<>();
    final List<String> unknownSymbols = (List<String>) model.get("errorSymbols");
    for (String symbol : unknownSymbols) {
      accessList.add(Access.of(symbol, AccessStatus.UNKNOWN_SYMBOL));
    }

    return accessList;
  }

  private List<Access> noData(AtomRequest request, Map<String, Object> model) {
    final String[] isins = getIsins(request);
    final String[] wkns = getWkns(request);
    final String[] names = getNames(request);

    final List<Access> accessList = new ArrayList<>(isins.length + wkns.length + names.length);
    for (String isin : isins) {
      accessList.add(Access.of(isin, AccessStatus.NO_DATA));
    }
    for (String wkn : wkns) {
      accessList.add(Access.of(wkn, AccessStatus.NO_DATA));
    }
    for (String name : names) {
      accessList.add(Access.of(name, AccessStatus.NO_DATA));
    }

    // collect unknown symbols if reported
    // also remove duplicates
    final List<Access> unknownSymbolsAccesses = collectUnknownSymbols(model);
    final List<String> symbols = unknownSymbolsAccesses.stream().map(Access::getInstrument)
            .collect(Collectors.toList());
    accessList.removeIf(access -> symbols.contains(access.getInstrument()));
    accessList.addAll(unknownSymbolsAccesses);

    if (accessList.isEmpty()) {
      accessList.add(Access.of(AccessStatus.NO_DATA));
    }

    return accessList;
  }

  private String[] getIsins(AtomRequest request) {
    return getParameter(request, "isin");
  }

  private String[] getWkns(AtomRequest request) {
    return getParameter(request, "wkn");
  }

  private String[] getNames(AtomRequest request) {
    return getParameter(request, "name");
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return EMPTY_STRING_ARRAY;
  }
}
