/*
 * WlHandler.java
 *
 * Created on 31.07.2006 12:07:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.misc;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.web.ProfileResolver;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;

/**
 * Backend for ticker data in gwt frontend.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisTickerData extends EasytradeCommandController {
    public static class Command {
        private String symbol;
        private boolean html=false;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public boolean isHtml() {
            return html;
        }

        public void setHtml(boolean html) {
            this.html = html;
        }
    }

    private final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
    private static final DecimalFormat PRICE_DF = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
    private static final DecimalFormat PERCENT_DF = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);

    static {
        PRICE_DF.applyLocalizedPattern("0,00#");
        PERCENT_DF.applyLocalizedPattern("0,00");
    }

    private EasytradeInstrumentProvider instrumentProvider;
    private IntradayProvider intradayProvider;
    private static final String N_A = "n/a";

    public GisTickerData() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        if (cmd.isHtml()) {
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
            writer.write(getPage(cmd.getSymbol(), getAttribute(request, ProfileResolver.AUTHENTICATION_KEY), getAttribute(request, ProfileResolver.AUTHENTICATION_TYPE_KEY)));
            writer.close();
            return null;
        }

        final Quote indexQuote = this.instrumentProvider.identifyQuote(cmd.getSymbol(), null, null, null);
        final List<Quote> quotes = this.instrumentProvider.getIndexQuotes(qidSymbol(indexQuote.getId()));

        quotes.sort(QuoteComparator.byName(RequestContextHolder.getRequestContext().getQuoteNameStrategy()));

        final List<Quote> all = new ArrayList<>();
        all.add(indexQuote);
        all.addAll(quotes);
        final List<IntradayData> ids = this.intradayProvider.getIntradayData(all, null);

        response.setContentType("text/plain;charset=ISO-8859-1");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "0");
        response.setStatus(HttpServletResponse.SC_OK);
        final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "ISO-8859-1");

        writer.write("@ vwd ticker prices\n");
        writer.write("#2 +++ " + DTF.print(new DateTime()) + " +++\n");

        for (int i = 0; i < all.size(); i++) {
            final Quote quote = all.get(i);
            final PriceRecord pr = ids.get(i).getPrice();

            writer.write("#1  " + quote.getInstrument().getName() + "\n");
            writer.write("#2  " + getPriceStr(pr.getPrice()) + "\n");
            writer.write(pr.getChangeNet() != null && pr.getChangeNet().compareTo(BigDecimal.ZERO) >= 0
                    ? "#3  "
                    : "#4  ");
            writer.write(getPercentStr(pr.getChangePercent()) + "\n");
        }

        writer.close();

        return null;
    }

    private String getAttribute(HttpServletRequest request, final String key) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final String result = (String) session.getAttribute(key);
            if (StringUtils.hasText(result)) {
                return result;
            }
        }
        final String result = request.getParameter(key);
        return (result != null) ? result : (String) request.getAttribute(key);
    }

    private static String getPriceStr(Price p) {
        if (isInvalid(p)) {
            return N_A;
        }
        synchronized (PRICE_DF) {
            return PRICE_DF.format(p.getValue());
        }
    }

    private static String getPercentStr(BigDecimal value) {
        if (value == null) {
            return N_A;
        }
        synchronized (PERCENT_DF) {
            return PERCENT_DF.format(value.movePointRight(2)) + "%";
        }
    }

    private static boolean isInvalid(Price price) {
        return (price == null) || (price == NullPrice.INSTANCE) || (price.getValue() == null);
    }

    private String getPage(String symbol, String authentication, String type) {
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Ticker: TecDAX</title>\n" +
                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n" +
                "  </head>\n" +
                "\n" +
                "  <body>\n" +
                "    <object width=\"658\" height=\"22\" classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"\n" +
                "            codebase=\"http://java.sun.com/products/plugin/autodl/jinstall-1_4-windows-i586.cab#Version=1,4,0,0\">\n" +
                "        <param name=\"button\" value=\"no\" />\n" +
                "        <param name=\"bgcolor\" value=\"#CCD6EB\" />\n" +
                "        <param name=\"color1\" value=\"#000000\" />\n" +
                "        <param name=\"color2\" value=\"#003399\" />\n" +
                "        <param name=\"color3\" value=\"#009933\" />\n" +
                "        <param name=\"color4\" value=\"#ff6200\" />\n" +
                "        <param name=\"delay\" value=\"20\" />\n" +
                "        <param name=\"dx\" value=\"1\" />\n" +
                "        <param name=\"fontsize\" value=\"11\" />\n" +
                "        <param name=\"fontstyle\" value=\"bold\" />\n" +
                "        <param name=\"datafile\" value=\"/gis-ticker.html?symbol="+symbol+"&authentication=" + authentication + "&authenticationType=" + type + "\"/>\n" +
                "        <param name=\"code\" value=\"de.gis.javaticker.QuotesTicker\"/>\n" +
                "        <param name=\"archive\" value=\"/web/download/QuotesTicker.jar\"/>\n" +
                "\n" +
                "        <!--[if !IE]> -->\n" +
                "        <object width=\"658\" height=\"22\" type=\"application/x-java-applet\">\n" +
                "            <param name=\"button\" value=\"no\"/>\n" +
                "            <param name=\"bgcolor\" value=\"#CCD6EB\"/>\n" +
                "            <param name=\"color1\" value=\"#000000\"/>\n" +
                "            <param name=\"color2\" value=\"#003399\"/>\n" +
                "            <param name=\"color3\" value=\"#009933\"/>\n" +
                "            <param name=\"color4\" value=\"#ff6200\"/>\n" +
                "            <param name=\"delay\" value=\"20\"/>\n" +
                "            <param name=\"dx\" value=\"1\"/>\n" +
                "            <param name=\"fontsize\" value=\"11\"/>\n" +
                "            <param name=\"fontstyle\" value=\"bold\"/>\n" +
                "            <param name=\"datafile\" value=\"/gis-ticker.html?symbol="+symbol+"&authentication=" + authentication + "&authenticationType=" + type + "\"/>\n" +
                "            <param name=\"code\" value=\"de.gis.javaticker.QuotesTicker\"/>\n" +
                "            <param name=\"archive\" value=\"web/download/QuotesTicker.jar\"/>\n" +
                "        </object>\n" +
                "        <!-- <![endif]-->\n" +
                "    </object>\n" +
                "  </body>\n" +
                "</html>";
    }
}
