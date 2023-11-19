/*
 * ImgRenditestruktur.java
 *
 * Created on 28.08.2006 16:29:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;

import de.marketmaker.istar.common.validator.DateFormat;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Pattern;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.chart.ChartModelAndView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EdgChart extends AbstractImgChart {

    public final static String DEFAULT_LOCALE = "de";

    public static class Command extends BaseImgCommand {
        private String symbol;

        private String date;

        private int rating;

        private int risk;

        public Command() {
            setLocale(DEFAULT_LOCALE);
        }

        @NotNull
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        @NotNull
        @Pattern(regex = "\\d{4}-\\d{2}-\\d{2}")
        @DateFormat(format = "yyyy-MM-dd")
        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @Range(min = 1, max = 5)
        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        @Range(min = 1, max = 5)
        public int getRisk() {
            return risk;
        }

        public void setRisk(int risk) {
            this.risk = risk;
        }

        @Override
        protected void validate(BindException bindException) {
            // do not call super.validate(bindException);, style and layout are supposed to be null
        }

        @Override
        @NotNull
        @Size(min=2, max=2)
        public String getLocale() {
            return super.getLocale();
        }
    }

    public EdgChart() {
        super(Command.class);
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
            HttpServletResponse response, Object object,
            BindException bindException) throws Exception {
        final Command cmd = (Command) object;
        final ChartModelAndView result = createChartModelAndView(cmd);

        final Map<String, Object> model = result.getModel();
        model.put("isin", cmd.getSymbol());
        model.put("date", cmd.getDate());
        model.put("locale", cmd.getLocale());
        model.put("risk", cmd.getRisk());
        model.put("rating", cmd.getRating());
        model.put("renderer", "edg");

        return result;
    }
}