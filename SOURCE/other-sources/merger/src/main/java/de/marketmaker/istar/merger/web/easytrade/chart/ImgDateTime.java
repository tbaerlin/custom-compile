/*
 * ImgDateTime.java
 *
 * Created on 06.11.2006 15:55:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.chart.ChartModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgDateTime extends AbstractImgChart {
    private final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");

    private final DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");

    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public ImgDateTime() {
        super(BaseImgSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {

        final BaseImgSymbolCommand cmd =  (BaseImgSymbolCommand) object;
        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final List<PriceRecord> priceRecords =
                this.intradayProvider.getPriceRecords(Arrays.asList(quote));

        final DateTime dt = getPriceDateOrNow(priceRecords);

        final ChartModelAndView result = createChartModelAndView(cmd);
        final Map<String, Object> model = result.getModel();
        model.put("time", this.timeFormatter.print(dt));
        model.put("date", this.dateFormatter.print(dt));

        return result;
    }

    private DateTime getPriceDateOrNow(List<PriceRecord> priceRecords) {
        if (priceRecords.isEmpty()) {
            return new DateTime();
        }
        final PriceRecord pr = priceRecords.get(0);
        final Price price = pr.getPrice();
        final DateTime priceDate = price.getDate();
        return priceDate != null ? priceDate : new DateTime();
    }
}
