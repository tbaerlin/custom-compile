package de.marketmaker.istar.merger.web.easytrade.block;

import static java.util.stream.Collectors.toList;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.sfdr.SfdrDataProvider;
import de.marketmaker.istar.merger.provider.sfdr.SfdrDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.SymbolArrayOnlyCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Queries SFDR (Sustainable Finance Disclosure Regulation) data for multiple symbols.
 */
public class MscSFDRClassification extends EasytradeCommandController {

  private final EasytradeInstrumentProvider instrumentProvider;
  private final SfdrDataProvider sfdrDataProvider;

  public MscSFDRClassification(EasytradeInstrumentProvider instrumentProvider, SfdrDataProvider sfdrDataProvider) {
    super(SymbolArrayOnlyCommand.class);
    this.instrumentProvider = instrumentProvider;
    this.sfdrDataProvider = sfdrDataProvider;
  }

  private static Locale getLocale() {
    final RequestContext rc = RequestContextHolder.getRequestContext();
    return (rc != null) ? rc.getLocale() : RequestContext.DEFAULT_LOCALE;
  }

  @Override
  protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
      Object o, BindException errors) {

    checkPermission(Selector.SFDR_CLASSIFICATION_DATA);

    final Locale locale = getLocale();
    final String language = locale.getLanguage();

    final SymbolArrayOnlyCommand cmd = (SymbolArrayOnlyCommand) o;
    final String[] symbols = cmd.getSymbol();

    if (symbols.length > 100) {
      // CDAPI supports 100 ISINs max. at one shot. So, we limit it too.
      throw new BadRequestException("maximum 100 symbols are supported");
    }

    final MarketStrategies marketStrategies =
        new MarketStrategies(MarketStrategyFactory.getDefaultMarketStrategy(), null);
    final List<Quote> quotes = getBySymbol(marketStrategies, symbols);
    final List<String> isins = quotes.stream()
        //.filter(Objects::nonNull)
        .map(quote -> quote.getInstrument().getSymbolIsin())
        .collect(toList());

    if (isins.size() == 0) {
      throw new UnknownSymbolException("symbols: " + Arrays.toString(symbols));
    }

    final Map<String, Object> model = new HashMap<>();
    final List<SfdrDataResponse> dataList = this.sfdrDataProvider
        .fetchSfdrData(language, isins);

    // pre-traverse to build a cache of isin -> response map to avoid o(n^2) when sorting
    final Map<String, SfdrDataResponse> responseMap = dataList.stream()
        .collect(Collectors.toMap(SfdrDataResponse::getIsin, v -> v));
    // sort responses by our Quote list via matching isins
    final List<Map<String, Object>> spiFieldList = new ArrayList<>();
    final List<Map<String, Object>> sfdrFieldList = new ArrayList<>();
    for (final Quote quote : quotes) {
      final SfdrDataResponse sfdrDataResponse =
          responseMap.get(quote.getInstrument().getSymbolIsin());
      if (sfdrDataResponse != null) {
        spiFieldList.add(sfdrDataResponse.getSpiFields("id", "source"));
        sfdrFieldList.add(sfdrDataResponse.getSfdrFields("id", "source"));
      } else {
        spiFieldList.add(Collections.EMPTY_MAP);
        sfdrFieldList.add(Collections.EMPTY_MAP);
      }
    }

    model.put("quotes", quotes);
    model.put("spiFields", spiFieldList);
    model.put("sfdrFields", sfdrFieldList);
    return new ModelAndView("mscsfdrclassification", model);
  }

  private List<Quote> getBySymbol(MarketStrategies marketStrategies, String[] symbol) {
    final Map<String, Instrument> instrumentsBySymbol
        = this.instrumentProvider.identifyInstrument(Arrays.asList(symbol), SymbolStrategyEnum.AUTO);

    final List<Quote> result = new ArrayList<>();

    for (final String s : symbol) {
      final Instrument instrument = instrumentsBySymbol.get(s);

      if (instrument != null) {
        try {
          if (EasytradeInstrumentProvider.usesUnderlyingFunction(s)) {
            result.add(
                marketStrategies.getQuote(
                    EasytradeInstrumentProvider.iidSymbol(instrument.getId()), instrument, null));
          } else {
            result.add(marketStrategies.getQuote(s, instrument, null));
          }
        } catch (Exception ignore) {
        }
      }
    }
    return result;
  }

}
