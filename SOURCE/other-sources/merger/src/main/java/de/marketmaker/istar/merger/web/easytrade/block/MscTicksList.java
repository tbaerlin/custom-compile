package de.marketmaker.istar.merger.web.easytrade.block;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Quote;

/**
 * Returns the same data as MSC_Ticks but for all quotes associated to the provided symbol.
 */
public class MscTicksList extends MscTicks {

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws IOException {

        final Command cmd = (Command) o;
        validateCommand(cmd);
        final Quote quote = getQuote(cmd);
        checkAuthorization(cmd, quote);

        List<Map<String, Object>> models =
                quote.getInstrument().getQuotes()
                        .stream()
                        .map(q -> {
                            try {
                                return this.createModel(cmd, q);
                            } catch (Exception ignored) {
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        return new ModelAndView("msctickslist", Collections.<String, Object>singletonMap("models", models));
    }
}
