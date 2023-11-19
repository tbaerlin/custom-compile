/*
 * ImgStartTable.java
 *
 * Created on 06.11.2006 17:59:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.view.stringtemplate.Renderer;
import de.marketmaker.istar.chart.ChartModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImgStartTable extends AbstractImgChart {

    private static DecimalFormat create(Locale locale, String pattern) {
        DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
        df.applyLocalizedPattern(pattern);
        return df;
    }

    protected IntradayProvider intradayProvider;

    protected EasytradeInstrumentProvider instrumentProvider;

    private final DecimalFormat renderer = create(Locale.US, "0.00");

    private final DecimalFormat diffRenderer = create(Locale.US, "+0.00;-0.00");

    private List<Long> qids = new ArrayList<>();

    private List<String> names = new ArrayList<>();

    public ImgStartTable() {
        super(BaseImgCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setElements(List<String> elements) {
        this.qids.clear();
        this.names.clear();
        for (String element : elements) {
            final int p = element.indexOf(';');
            this.qids.add(Long.parseLong(element.substring(0, p)));
            this.names.add(element.substring(p + 1));
        }
        this.logger.info("<setElements> names = " + this.names);
        this.logger.info("<setElements> qids = " + this.qids);
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {

        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(this.qids);
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);

        final String[][] table = new String[this.qids.size()][4];

        for (int i = 0; i < this.qids.size(); i++) {
            table[i][0] = this.names.get(i);
            final PriceRecord record = priceRecords.get(i);
            if (record.getPrice().getValue() != null) {
                synchronized (this.renderer) {
                    table[i][1] = this.renderer.format(record.getPrice().getValue());
                }
            }
            else {
                table[i][1] = "";
            }

            if (record.getChangePercent() != null) {
                table[i][2] = record.getChangePercent().compareTo(BigDecimal.ZERO) < 0 ? "-" : "+";
            }
            else {
                table[i][2] = "";
            }

            if (record.getChangeNet() != null) {
                synchronized (this.diffRenderer) {
                    table[i][3] = this.diffRenderer.format(record.getChangeNet());
                }
            }
            else {
                table[i][3] = "";
            }
        }

        final ChartModelAndView result = createChartModelAndView((BaseImgCommand) object);
        final Map<String, Object> model = result.getModel();
        model.put("table", table);
        return result;
    }
}
