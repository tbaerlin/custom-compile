package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author zzhao
 */
public final class CollectorSupport {

  public static final Supplier<String> EMPTY_SUFFIX = () -> "";

  public static final String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * Cannot handle underlying function in symbol so that such symbol will be considered in
   * candidates.
   */
  public static boolean inCandidates(List<?> candidates, String symbol,
      SymbolStrategyEnum symbolStrategy) {
    if (SymbolUtil.usesUnderlyingFunction(symbol)) {
      return true;
    }

    final SymbolStrategyEnum strategy =
        SymbolStrategyEnum.AUTO == symbolStrategy ? SymbolUtil.guessStrategy(symbol)
            : symbolStrategy;

    final String symbolToCompare = getSymbolToCompare(symbol, strategy);
    for (Object candidate : candidates) {
      if (candidate == null) {
        continue;
      }

      switch (strategy) {
        case IID:
          if (Long.parseLong(symbolToCompare) == getIid(candidate)) {
            return true;
          }
          break;
        case QID:
          if (Long.parseLong(symbolToCompare) == getQid(candidate)) {
            return true;
          }
          break;
        case VWDCODE_PREFIX:
          final String vwdCode = getQuote(candidate).getSymbolVwdcode();
          if (vwdCode != null && vwdCode.startsWith(symbolToCompare)) {
            return true;
          }
          break;
        case ISIN:
          final String isin = getIsin(candidate);
          if (isin != null && isin.equalsIgnoreCase(symbolToCompare)) {
            return true;
          }
          break;
        case WKN:
          final String wkn = getWkn(candidate);
          if (wkn != null && wkn.equalsIgnoreCase(symbolToCompare)) {
            return true;
          }
          break;
        case VWDCODE:
          final String vwdcode = getQuote(candidate).getSymbolVwdcode();
          if (vwdcode != null && vwdcode.equalsIgnoreCase(symbolToCompare)) {
            return true;
          }
          break;
        case INFRONT_ID:
          final String infrontId = getQuote(candidate).getSymbolInfrontId();
          if (infrontId != null && infrontId.equalsIgnoreCase(symbolToCompare)) {
            return true;
          }
          break;
        case BIS_KEY:
          final String bisKey = getQuote(candidate).getSymbolBisKey();
          if (bisKey != null && bisKey.equalsIgnoreCase(symbolToCompare)) {
            return true;
          }
          break;
        default:
          // check MMWKN at last
          final String mmwkn = getQuote(candidate).getSymbolMmwkn();
          if (mmwkn != null && mmwkn.equalsIgnoreCase(symbolToCompare)) {
            return true;
          }
          break;
      }
    }

    return false;
  }

  public static String getWkn(Object candidate) {
    final Quote q = getQuote(candidate);
    final Instrument ins = q.getInstrument();
    return ins == null ? null : ins.getSymbolWkn();
  }

  public static String getIsin(Object candidate) {
    final Quote q = getQuote(candidate);
    final Instrument ins = q.getInstrument();
    return ins == null ? null : ins.getSymbolIsin();
  }

  public static Long getQid(Object candidate) {
    final Quote q = getQuote(candidate);
    return q.getId();
  }

  public static long getIid(Object candidate) {
    final Quote q = getQuote(candidate);
    final Instrument ins = q.getInstrument();
    return ins == null ? 0 : ins.getId();
  }

  public static Quote getQuote(Object candidate) {
    if (candidate instanceof Quote) {
      return (Quote) candidate;
    } else if (candidate instanceof HasQuote) {
      return ((HasQuote) candidate).getQuote();
    }

    throw new IllegalStateException("should not happen");
  }

  public static String getSymbolToCompare(String symbol, SymbolStrategyEnum strategy) {
    switch (strategy) {
      case IID:
      case QID:
        return symbol.substring(0, symbol.length() - 4);
      case VWDCODE_PREFIX:
        return symbol.substring(0, symbol.length() - 1);
      default:
        return symbol;
    }
  }

  public static SymbolStrategyEnum getSymbolStrategy(AtomRequest req) {
    final Map<String, String[]> parameterMap = req.getParameterMap();
    if (parameterMap == null || parameterMap.isEmpty()) {
      return SymbolStrategyEnum.AUTO;
    }

    final String[] symbolStrategies = parameterMap.get("symbolStrategy");
    if (symbolStrategies == null || symbolStrategies.length == 0) {
      return SymbolStrategyEnum.AUTO;
    }

    // we only support one strategy at most
    return SymbolStrategyEnum.valueOf(symbolStrategies[0].toUpperCase());
  }

  public static String[] getListId(AtomRequest req) {
    return getParameter(req, "listid");
  }

  public static String[] getSymbol(AtomRequest req) {
    return getParameter(req, "symbol");
  }

  public static String[] getParameter(AtomRequest req, String paramName) {
    final Map<String, String[]> paramMap = req.getParameterMap();
    if (paramMap == null || paramMap.isEmpty()) {
      return EMPTY_STRING_ARRAY;
    }

    final String[] params = paramMap.get(paramName);
    return params == null ? EMPTY_STRING_ARRAY : params;
  }

  public static Object noData(String[] symbols) {
    return toAccess(symbols, AccessStatus.NO_DATA);
  }

  public static Object ok(String[] symbols) {
    return toAccess(symbols, AccessStatus.OK);
  }

  public static Object toAccess(String[] symbols, AccessStatus status) {
    if (symbols.length == 0) {
      return Access.of(status);
    } else if (symbols.length == 1) {
      return Access.of(symbols[0], status);
    } else {
      final ArrayList<Access> accesses = new ArrayList<>(symbols.length);
      for (String majorSymbol : symbols) {
        accesses.add(Access.of(majorSymbol, status));
      }
      return accesses;
    }
  }

  public static List<String> toArrayList(String[] symbols) {
    // Arrays.asList does not allow remove operation from iterator
    final ArrayList<String> list = new ArrayList<>(symbols.length);
    Collections.addAll(list, symbols);
    return list;
  }
}
