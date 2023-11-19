package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ProfiledInstrument;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides a list of instrument quotes list; only those quotes will be included for which the user has at
 * least the permission to view end-of-day prices.<br>
 * If no quoutes are visible an empty block will be returned.
 * @author mcoenen
 */
public class MscQuotesList extends EasytradeCommandController {

    protected EasytradeInstrumentProvider instrumentProvider;

    protected MscQuotesList() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        Map<String, Map<String, Object>> result =
                Stream.of(cmd.getSymbol())
                        .collect(Collectors.toMap(Function.identity(),
                                symbol -> {
                                    Map<String, Object> model = new HashMap<>();
                                    model.put("correlationId", symbol);

                                    try {
                                        Quote quote = this.instrumentProvider.identifyQuote(symbol,
                                                cmd.getSymbolStrategy(),
                                                cmd.getMarket(),
                                                cmd.getMarketStrategy());
                                        if (quote != null) {
                                            Instrument instrument = quote.getInstrument();
                                            List<Quote> quotes =
                                                    ProfiledInstrument.quotesWithPrices(instrument, profile);
                                            if (!quotes.isEmpty()) {
                                                model.putAll(MscQuotes.getModel(new ListCommand(quotes.size()),
                                                        quote, instrument, quotes));
                                            } else {
                                                model.putAll(
                                                    MscQuotes.getModel(new ListCommand(0), quote,
                                                        instrument, Collections.emptyList()));
                                            }
                                        } else {
                                            model.putAll(
                                                MscQuotes.getModel(new ListCommand(0), null,
                                                    null, Collections.emptyList()));
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    return model;
                                },
                                (u, v) -> u,
                                LinkedHashMap::new));

        return new ModelAndView("mscquoteslist", Collections.<String, Object>singletonMap("result", result));
    }

    public static class Command extends BaseMultiSymbolCommand {
        @NotNull
        @Size(min = 1, max = 100)
        public String[] getSymbol() {
            return super.getSymbol();
        }
    }
}
