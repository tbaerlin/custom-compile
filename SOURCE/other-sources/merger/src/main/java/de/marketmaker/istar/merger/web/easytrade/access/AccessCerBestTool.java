package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.block.CerBestTool.BestElement;
import de.marketmaker.istar.ratios.frontend.BestToolRatioSearchResponse.BestToolElement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessCerBestTool implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final String reqSymbol = getRequestSymbol(req);
    if (hasValue(model) || hasQuote(model)) {
      return Access.of(reqSymbol, AccessStatus.OK);
    }

    return Access.of(reqSymbol, AccessStatus.NO_DATA);
  }

  private boolean hasQuote(Map<String, Object> model) {
    final Object quotes = model.get("quotes");
    if (quotes instanceof Collection) {
      return !((Collection<?>) quotes).isEmpty();
    }
    return false;
  }

  private boolean hasValue(Map<String, Object> model) {
    BestElement current;
    final Iterator<Object> iterator = model.values().iterator();
    while (iterator.hasNext() && (current = getBestElement(iterator)) != null) {
      if (current.getElements() != null && !current.getElements().isEmpty()) {
        final BestToolElement bte = current.getElements().get(0);
        if (bte.getSourceValue() > 0) {
          return true;
        }
      }
    }
    return false;
  }

  private BestElement getBestElement(Iterator<Object> iterator) {
    final Object next;
    if ((next = iterator.next()) instanceof Collection) {
      final Collection<?> nextList = (Collection<?>) next;
      if (!nextList.isEmpty()) {
        final Object element;
        final Iterator<?> innerIterator = nextList.iterator();
        if (innerIterator.hasNext() && (element = innerIterator.next()) instanceof BestElement) {
          return (BestElement) element;
        }
      }
    }
    return null;
  }

  private String getRequestSymbol(AtomRequest req) {
    final String[] majorSymbols = getMajorSymbols(req);
    return (majorSymbols.length > 0) ? majorSymbols[0] : Access.SYMBOL_DEFAULT;
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return CollectorSupport.getListId(req);
  }
}
