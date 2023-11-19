/*
 * LbbwValuationRates.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns the current valuation price for a set of quotes.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LbbwValuationRates extends EasytradeCommandController {

  public static class Command extends BaseMultiSymbolCommand {

    /**
     * @return Identifiers for instruments or quotes that will be interpreted according to the
     * specified <tt>symbol</tt>.
     * @sample 25548.qid
     */
    @NotNull
    @Size(min = 1, max = 1000)
    public String[] getSymbol() {
      return super.getSymbol();
    }
  }

  private IntradayProvider intradayProvider;

  private EasytradeInstrumentProvider instrumentProvider;

  public LbbwValuationRates() {
    super(Command.class);
  }

  public void setIntradayProvider(IntradayProvider intradayProvider) {
    this.intradayProvider = intradayProvider;
  }

  public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
    this.instrumentProvider = instrumentProvider;
  }

  protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
      Object o, BindException errors) {
    final Command cmd = (Command) o;
    final List<Quote> quotes
        = this.instrumentProvider.identifyQuotes(Arrays.asList(cmd.getSymbol()),
        cmd.getSymbolStrategy(), new MarketStrategies(cmd));
    CollectionUtils.removeNulls(quotes);
    final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);
    final Map<String, Object> model = new HashMap<>();
    model.put("quotes", quotes);
    model.put("prices", prices);
    return new ModelAndView("lbbwvaluationrates", model);
  }
}