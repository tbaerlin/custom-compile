package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbol;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getSymbolStrategy;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.inCandidates;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.toArrayList;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.util.QuoteRef;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link AtomAccessCollector}. It only collects quote from model and
 * compare with requested symbol(s). For each found quote a {@value AccessStatus#OK} and each
 * not-found symbol a {@value AccessStatus#NO_DATA} are returned.
 *
 * @author zzhao
 */
@Slf4j
public class DefaultAtomAccessCollector implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String[] symbols = getMajorSymbols(req);
    if (symbols.length > 1) {
      return collect(model, toArrayList(symbols), getSymbolStrategy(req));
    } else {
      return collect(model, symbols.length == 0 ? null : symbols[0]); // no strategy necessary
    }
  }

  private Object collect(Map<String, Object> model, String symbol) {
    final Object candidate = findTheCandidate(model);
    // just one symbol in request, not really necessary to compare
    // special cases must be handled in specific collector
    if (candidate instanceof Quote) {
      return Access.of((Quote) candidate, AccessStatus.OK);
    } else if (candidate instanceof QuoteRef) {
      QuoteRef quoteRef = (QuoteRef) candidate;
      if (quoteRef.getNext() == null) {
        return Access.of(quoteRef.getQuote(), AccessStatus.OK);
      } else {
        final ArrayList<Access> list = new ArrayList<>(5);
        // max. reference depth 5
        for (int i = 0; i < 5 && quoteRef.getNext() != null; i++) {
          list.add(Access.of(quoteRef.getQuote(), AccessStatus.OK));
          quoteRef = quoteRef.getNext();
        }
        return list;
      }
    } else if (candidate instanceof Instrument) {
      return Access.of((Instrument) candidate, AccessStatus.OK);
    } else {
      return Access.of(symbol, AccessStatus.NO_DATA);
    }
  }

  /**
   * An atom request can have:
   * <ul>
   *   <li>0 symbols, e.g. XxxFinder</li>
   *   <li>0 symbols, but a market symbol, e.g. MSC_FeedSnapshot</li>
   *   <li>1 symbol, the majority case</li>
   *   <li>n symbol, for a few atoms</li>
   * </ul>
   *
   * @return a symbol or a list of symbols, or null if no symbol can be extracted
   */
  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getSymbol(req);
  }

  private Object collect(Map<String, Object> model, List<String> reqSymbols,
      SymbolStrategyEnum symbolStrategy) {
    final List<Object> candidates = findTheCandidates(model);
    if (candidates.isEmpty()) {
      // avoid stream
      final ArrayList<Access> accesses = new ArrayList<>(reqSymbols.size());
      for (String symbol : reqSymbols) {
        accesses.add(Access.of(symbol, AccessStatus.NO_DATA));
      }
      return accesses;
    }

    reqSymbols.removeIf(symbol -> inCandidates(candidates, symbol, symbolStrategy));

    final ArrayList<Access> accesses = new ArrayList<>(candidates.size() + reqSymbols.size());
    // for all candidates report OK
    for (Object candidate : candidates) {
      addAccess(accesses, candidate, AccessStatus.OK);
    }
    // for all remaining symbols report no.data
    for (String symbol : reqSymbols) {
      addAccess(accesses, symbol, AccessStatus.NO_DATA);
    }

    return accesses;
  }

  private void addAccess(List<Access> accesses, Object candidate, AccessStatus status) {
    if (candidate instanceof Quote) {
      accesses.add(Access.of((Quote) candidate, status));
      return;
    } else if (candidate instanceof HasQuote) {
      accesses.add(Access.of(((HasQuote) candidate).getQuote(), status));
      return;
    } else if (candidate instanceof String) {
      accesses.add(Access.of((String) candidate, status));
      return;
    }

    throw new IllegalStateException("should not happen, given quote: " + candidate);
  }

  private List<Object> findTheCandidates(Map<String, Object> model) {
    // could there be duplicated quotes?
    Object candidate = model.get("quotes");
    if (candidate instanceof List) {
      return (List<Object>) candidate;
    }

    for (Object val : model.values()) {
      if (val instanceof List) {
        final List<Object> list = (List<Object>) val;
        if (!list.isEmpty()) {
          final Object obj = list.get(0);
          if (obj instanceof HasQuote) {
            return list;
          } // any blocks using instrument list in model?
        }
      }
    }

    return Collections.emptyList();
  }

  private Object findTheCandidate(Map<String, Object> model) {
    Object candidate = model.get("quote");
    if (candidate instanceof Quote) {
      return candidate;
    }

    candidate = model.get("quoteRef");
    if (candidate instanceof QuoteRef) {
      return candidate;
    }

    // multiple quotes case
    final Object candidates = model.get("quotes");
    if (candidates instanceof Collection) {
      final Optional<?> hasQuote = ((Collection<?>) candidates).stream()
          .filter(o -> o instanceof Quote).findFirst();
      if (hasQuote.isPresent()) {
        return hasQuote.get();
      }
    }

    return findCandidate(model);
  }

  private Object findCandidate(Map<String, Object> model) {
    for (Object val : model.values()) {
      if (val instanceof Quote) {
        return val;
      }
    }

    // some blocks only use instrument
    for (Object val : model.values()) {
      if (val instanceof Instrument) {
        return val;
      }
    }

    return null;
  }
}
