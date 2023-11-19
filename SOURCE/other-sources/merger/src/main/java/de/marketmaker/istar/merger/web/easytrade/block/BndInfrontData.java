package de.marketmaker.istar.merger.web.easytrade.block;

import static java.util.stream.Collectors.toList;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.edi.EdiDataProvider;
import de.marketmaker.istar.merger.provider.edi.EdiDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.SymbolArrayOnlyCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import io.grpc.Status;
import io.grpc.Status.Code;
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

/** Queries Infront BOND data for a single symbol. */
public class BndInfrontData extends EasytradeCommandController {

  private final EasytradeInstrumentProvider instrumentProvider;
  private final EdiDataProvider ediDataProvider;

  public BndInfrontData(
      EasytradeInstrumentProvider instrumentProvider, EdiDataProvider ediDataProvider) {
    super(SymbolArrayOnlyCommand.class);
    this.instrumentProvider = instrumentProvider;
    this.ediDataProvider = ediDataProvider;
  }

  private static Locale getLocale() {
    final RequestContext rc = RequestContextHolder.getRequestContext();
    return (rc != null) ? rc.getLocale() : RequestContext.DEFAULT_LOCALE;
  }

  @Override
  protected ModelAndView doHandle(
      HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) {
    checkPermission(Selector.EDI_BOND_REFERENCE_DATA);

    final Locale locale = getLocale();
    final String language = locale.getLanguage();

    final SymbolArrayOnlyCommand command = (SymbolArrayOnlyCommand) o;
    final String[] symbols = command.getSymbol();

    if (symbols.length > 100) {
      // CDAPI supports 100 ISINs max. at one shot. So, we limit it too.
      throw new BadRequestException("maximum 100 symbols are supported");
    }

    final MarketStrategies marketStrategies =
        new MarketStrategies(MarketStrategyFactory.getDefaultMarketStrategy(), null);
    final List<Quote> quotes = getBySymbol(marketStrategies, symbols);
    final List<String> isins = quotes.stream()
        .map(quote -> quote.getInstrument().getSymbolIsin())
        .collect(toList());

    if (isins.size() == 0) {
      throw new UnknownSymbolException("symbols: " + Arrays.toString(symbols));
    }

    final Map<String, Object> model = new HashMap<>();
    final List<EdiDataResponse> dataList = this.ediDataProvider.fetchEdiData(language, isins);

    /*
    if (dataList.size() == 1 && dataList.get(0).getResult().hasError()) {
      final Status errorStatus = dataList.get(0).getErrorStatus();
      // Only expose known errors
      if (errorStatus.getCode() == Code.INVALID_ARGUMENT) {
        throw new BadRequestException(errorStatus.getDescription());
      }
      // Do not expose error details to UI. It's already logged before
      throw new InternalFailure("cannot fetch data from CDAPI");
    }
     */

    // collect all fields
    final List<Map<String, Object>> allFields = dataList.stream()
        .map(EdiDataResponse::getFields).collect(toList());

    model.put("quotes", quotes);
    model.put("bonds", allFields);
    return new ModelAndView("bndinfrontdata", model);
  }

  private List<Quote> getBySymbol(MarketStrategies marketStrategies, String[] symbol) {
    final Map<String, Instrument> instrumentsBySymbol =
        this.instrumentProvider.identifyInstrument(Arrays.asList(symbol), SymbolStrategyEnum.AUTO);

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
