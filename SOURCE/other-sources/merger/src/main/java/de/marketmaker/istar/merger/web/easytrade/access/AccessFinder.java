package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_STRING_ARRAY;
import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.getParameter;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.ListInfo;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.List;
import java.util.Map;

/**
 * Integrates into DefaultAtomAccessCollector for all blocks extending {@link
 * de.marketmaker.istar.merger.web.easytrade.block.AbstractFindersuchergebnis}
 *
 * @author zzhao
 */
public class AccessFinder implements AtomAccessCollector {

  @Override
  public Object collect(AtomRequest req, Map<String, Object> model) {
    // no requested symbol
    final int count = getResultCount(req.getName(), model);
    final String[] majorSymbols = getMajorSymbols(req);
    final AccessStatus status = count == 0 ? AccessStatus.NO_DATA : AccessStatus.OK;
    return majorSymbols.length == 0 ? Access.of(status) : Access.of(majorSymbols[0], status);
  }

  private int getResultCount(String atomName, Map<String, Object> model) {
    if ("FUT_SymbolFinder".equals(atomName)) {
      final Map<String, List<Quote>> futureSymbols =
          (Map<String, List<Quote>>) model.get("symbols");
      if (futureSymbols == null || futureSymbols.isEmpty()) {
        return 0;
      }
      for (List<Quote> quotes : futureSymbols.values()) {
        if (quotes != null && !quotes.isEmpty()) {
          return 1;
        }
      }
      return 0;
    } else {
      final Object listResult = model.get("listinfo");
      if (listResult instanceof ListResult) {
        return ((ListResult) listResult).getCount();
      } else if (listResult instanceof ListInfo) {
        return ((ListInfo) listResult).getCount();
      }

      throw new IllegalStateException("check finder implementation");
    }
  }

  @Override
  public String[] getMajorSymbols(AtomRequest req) {
    switch (req.getName()) {
      case "FUT_SymbolFinder":
        return getParameter(req, "symbolPrefix");
      case "MSC_Lexicon_Entries":
        return getParameter(req, "initial");
      case "MSC_List_Constituents":
        return getParameter(req, "listid");
      default:
        return EMPTY_STRING_ARRAY;
    }
  }
}
