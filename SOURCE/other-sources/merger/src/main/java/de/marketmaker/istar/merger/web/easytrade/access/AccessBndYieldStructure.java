package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.noData;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.block.BndYieldStructure.YieldElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author zzhao
 */
public class AccessBndYieldStructure implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    final Map<String, List<YieldElement>> elements =
        (Map<String, List<YieldElement>>) model.get("elements");
    if (elements == null || elements.isEmpty()) {
      return noData(getMajorSymbols(req));
    }

    final AtomicInteger count = new AtomicInteger(0);
    iterateYieldElements(elements, ye -> count.incrementAndGet());

    final ArrayList<Access> accesses = new ArrayList<>(count.get());
    iterateYieldElements(elements,
        ye -> accesses.add(Access.of(ye.getQuote(),
            ye.getValue() == null ? AccessStatus.NO_DATA : AccessStatus.OK)));

    return accesses;
  }

  private void iterateYieldElements(Map<String, List<YieldElement>> elements,
      Consumer<YieldElement> consumer) {
    for (List<YieldElement> yieldElements : elements.values()) {
      if (yieldElements != null && !yieldElements.isEmpty()) {
        for (YieldElement yieldElement : yieldElements) {
          if (yieldElement != null && yieldElement.getQuote() != null) {
            consumer.accept(yieldElement);
          }
        }
      }
    }
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    return getParameter(req, "countryCode");
  }
}
