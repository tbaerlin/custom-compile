package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import java.util.Map;

/**
 * Collects accesses for an atom controller.
 * <p>
 * There could be 0 to n requested symbols. The returned accesses must be 1 to m, where m is
 * strictly greater or equal to n.
 * </p>
 * <p>
 *   <table>
 *     <tr><th>number of requested symbol</th><th>expected accesses</th><th>explanation</th></tr>
 *     <tr><td>0</td><td>1 to m</td>
 *     <td>XxxFinders doesn't need symbol, MSC_FeedSnapshot can query market</td></tr>
 *     <tr><td>1</td><td>1 to m</td>
 *     <td>Most of time will be 1, can be greater than 1 if necessary</td></tr>
 *     <tr><td>n</td><td>m</td><td>This depends on the quality of response, but m must be always
 *     equal or greater than n, e.g. when no response can be found for a given request symbol, it
 *     report an {@value AccessStatus#NO_DATA}</td></tr>
 *   </table>
 * </p>
 * <p>
 *   Implementations of this interface has following two options:
 *   <ol>
 *     <li>If it is clear, only one symbol is supported in the command class of the associated
 *     {@link de.marketmaker.istar.merger.web.easytrade.block.AtomController}, then it is only
 *     necessary to implement {@link #collect(String, Map, String)}</li>
 *     <li>If is clear, multiple symbols are supported in the command class of the associated
 *     {@link de.marketmaker.istar.merger.web.easytrade.block.AtomController}, then it is necessary
 *     to implement both methods.</li>
 *   </ol>
 *   The method with the single symbol is an optimisation, since most atoms only support one symbol
 *   and this avoids creation of many single element lists.
 * </p>
 *
 * @author zzhao
 */
public interface AtomAccessCollector {

  /**
   * Collects accesses for an atom controller.
   *
   * @param req the atom request
   * @param model model returned by the atom controller
   * @return an {@link Access} or a collection of {@link Access}es
   */
  Object collect(AtomRequest req, Map<String, Object> model);

  /**
   * Extracts major symbols involved in the given {@link AtomRequest}.
   * <p>
   * Most of the time it will be symbol string. For a few blocks it can be a symbol list. For blocks
   * which don't support symbols, it can be a list id, or some other combination. This must be
   * decided by each block.
   * </p>
   *
   * @param req the atom request
   * @return an array of symbols, can be of length zero, but never null
   */
  String[] getMajorSymbols(AtomRequest req);

  default Object toAccess(Quote quote, String[] symbols, AccessStatus status) {
    if (quote != null) {
      return Access.of(quote, status);
    }

    if (symbols.length > 0) {
      return Access.of(symbols[0], status);
    }

    return Access.of(status);
  }

  default Access toAccess(Quote quote, String symbol, AccessStatus status) {
    if (quote != null) {
      return Access.of(quote, status);
    }

    return Access.of(symbol, status);
  }
}
