package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProviderImpl.MyEntitlementQuote;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.block.MscFeedSnapshot.Item;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hummell
 */
@Slf4j
@RequiredArgsConstructor
public class AccessMscFeedSnapshot implements AtomAccessCollector {

  private final InstrumentProvider instrumentProvider;

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final List<Item> items = (List<Item>) model.get("items");
    if (items == null || items.isEmpty()) {
      return noData(getMajorSymbols(req));
    }

    final List<Access> accesses = new ArrayList<>(items.size());
    for (Item item : items) { // priceQuality NONE results in no item
      final Quote quote = item.getQuote();
      if (quote instanceof MyEntitlementQuote) {
        try {
          final Quote realQuote =
              this.instrumentProvider.identifyByVwdfeed(quote.getSymbolVwdfeed());
          accesses.add(Access.of(realQuote, AccessStatus.OK));
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("<collect> failed {}", quote.getSymbolVwdfeed(), e);
          }
          accesses.add(Access.of(quote, AccessStatus.OK));
        }
      } else {
        accesses.add(Access.of(quote, AccessStatus.OK));
      }
    }

    return accesses;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    final String[] markets = getParameter(req, "market");
    if (markets != null && markets.length > 0) {
      return markets;
    }

    final String[] symbols = getParameter(req, "symbol");
    if (symbols != null && symbols.length > 0) {
      return symbols;
    }

    return getParameter(req, "listid");
  }
}
