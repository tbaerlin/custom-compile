/*
 * BndRenditekennzahlen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Queries yield related data for a given bond.
 * <p>
 * Yield describes the amount in cash that returns to the owners of an instrument and serves as a
 * major factor in bond valuation. The returned yield related data includes:
 * <ul>
 * <li>yieldRelativePerYear</li>
 * <li>brokenPeriodInterest</li>
 * <li>duration</li>
 * <li>modifiedDuration</li>
 * <li>convexity</li>
 * <li>interestRateElasticity</li>
 * <li>basePointValue</li>
 * </ul>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @sample symbol 1635358.qid
 */
public class BndRenditekennzahlen extends EasytradeCommandController {
    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    protected RatiosProvider ratiosProvider;

    public BndRenditekennzahlen() {
        super(DefaultSymbolCommand.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(Collections.singletonList(quote));

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.BND);

        final RatioDataRecord ratios = this.ratiosProvider.getRatioData(quote, fields);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("ratios", ratios);
        model.put("price", prices.get(0));
        return new ModelAndView("bndrenditekennzahlen", model);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

}
