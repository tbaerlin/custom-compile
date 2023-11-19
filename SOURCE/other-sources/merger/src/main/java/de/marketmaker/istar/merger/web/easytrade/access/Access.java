package de.marketmaker.istar.merger.web.easytrade.access;

import static de.marketmaker.istar.merger.web.easytrade.access.CollectorSupport.EMPTY_SUFFIX;

import de.marketmaker.istar.domain.ItemWithSymbols;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProviderImpl.MyEntitlementQuote;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents access made in an {@link de.marketmaker.istar.merger.web.easytrade.block.AtomController}
 * in dmxml by instrument (and its type id), market and response status. There can be multiple such
 * accesses during processing of one {@link de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.AtomRequest}.
 * <p>
 * Not all dmxml queries have symbols, e.g. XxxFinder and blocks concerning exchange index, etc.
 * </p>
 *
 * @author zzhao
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Access {

  public static final String SYMBOL_DEFAULT = "-";

  private static final List<KeysystemEnum> KS_INSTRUMENT = Arrays.asList(
      KeysystemEnum.ISIN,
      KeysystemEnum.WKN
  );

  private static final List<KeysystemEnum> KS_MARKET = Arrays.asList(
      KeysystemEnum.VWDFEED,
      KeysystemEnum.ISO
  );

  private final String instrument;

  private final String market;

  private final int type;

  private final AccessStatus status;

  public static Access of(AccessStatus status) {
    return new Access(SYMBOL_DEFAULT, null, 0, status);
  }

  public static Access of(String symbol, AccessStatus status) {
    return new Access(symbol, null, 0, status);
  }

  public static Access of(String info1, String info2, AccessStatus status) {
    return new Access(info1, info2, 0, status);
  }

  public static Access of(String instrument, String market, InstrumentTypeEnum type,
      AccessStatus status) {
    return new Access(instrument, market, type == null ? 0 : type.getId(), status);
  }

  public static Access of(String instrument, InstrumentTypeEnum type, AccessStatus status) {
    return of(instrument, null, type, status);
  }

  public static Access of(Instrument instrument, AccessStatus status) {
    return of(instrument, status, EMPTY_SUFFIX);
  }

  public static Access of(Instrument instrument, AccessStatus status, Supplier<String> suffix) {
    if (instrument == null) {
      return of(status);
    }
    return of(getInstrumentSymbol(instrument, suffix), instrument.getInstrumentType(), status);
  }

  public static Access of(Quote quote, AccessStatus status) {
    return of(quote, status, EMPTY_SUFFIX);
  }

  public static Access of(Quote quote, AccessStatus status, Supplier<String> suffix) {
    if (quote == null) {
      return of(status);
    } else if (quote instanceof QuoteDp2) {
      return of(getInstrumentSymbol(quote.getInstrument(), suffix),
          getMarketSymbol(quote.getMarket()),
          quote.getInstrument().getInstrumentType(), status);
    } else if (quote instanceof NullQuote) {
      return of(quote.getInstrument(), status);
    } else if (quote instanceof MyEntitlementQuote) {
      return of(quote.getSymbolVwdcode(), quote.getInstrument().getInstrumentType(), status);
    } else {
      log.error("<of> unhandled quote type {}", quote.getClass().getName());
      return of(status);
    }
  }

  private static String getMarketSymbol(Market market) {
    return getSymbol(market, KS_MARKET, market::getName);
  }

  private static String getInstrumentSymbol(Instrument ins, Supplier<String> suffix) {
    return getSymbol(ins, KS_INSTRUMENT, () -> {
      final String suf = suffix.get();
      return StringUtils.isNotBlank(suf) ? ins.getName() + " " + suf : ins.getName();
    });
  }

  private static String getSymbol(ItemWithSymbols item, List<KeysystemEnum> systems,
      Supplier<String> fallback) {
    for (KeysystemEnum ks : systems) {
      final String symbol = item.getSymbol(ks);
      if (StringUtils.isNotBlank(symbol)) {
        return symbol;
      }
    }

    final String ret = fallback.get();
    return StringUtils.isNotBlank(ret) ? ret : SYMBOL_DEFAULT;
  }
}
