/*
 * AbstractDde.java
 *
 * Created on 27.10.2008 17:56:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.webxl;

import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.XmlUtil;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.NullPriceRecord;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.web.DataBinderUtils;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.UserCommandImpl;
import de.marketmaker.istar.merger.web.easytrade.block.UserHandler;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract public class AbstractDde extends UserHandler {
    private static final String[] PERIOD_NAMES = new String[]{
            "P1W", "P1M", "P3M", "P6M", "P1Y", "P3Y", "P5Y", "P7Y", "P10Y"
    };

    private static final List<Period> PERIODS = new ArrayList<>();

    static {
        for (final String period : PERIOD_NAMES) {
            PERIODS.add(DateUtil.getPeriod(period));
        }
    }

    public static class Command {

        private String xun;

        @NotNull
        public String getXun() {
            return xun;
        }

        public void setXun(String xun) {
            this.xun = xun;
        }
    }

    /**
     * The original parameter names are incompatible wrt java beans property naming standards; in
     * order to be able to use automatic binding, we have to translate the names (at least I could
     * not come up with anything better (of)).
     *
     * @return key/value pairs for parameter mappings
     */
    protected abstract DataBinderUtils.Mapping getParameterMapping();

    protected Long companyid;
    protected EasytradeInstrumentProvider instrumentProvider;
    protected IntradayProvider intradayProvider;
    protected HistoricRatiosProvider historicRatiosProvider;

    protected AbstractDde(Class command) {
        super(command);
    }

    public void setHistoricRatiosProvider(HistoricRatiosProvider historicRatiosProvider) {
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setCompanyid(Long companyid) {
        this.companyid = companyid;
    }

    protected ServletRequestDataBinder createBinder(HttpServletRequest request,
                                                    Object object) throws Exception {
        return DataBinderUtils.createBinder(object, getParameterMapping());
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (!profile.isAllowed(Selector.WEB_XL)) {
            response.sendError(HttpServletResponse.SC_NO_CONTENT, Selector.WEB_XL.getId() + " not in " + profile.getName());
            return null;
        }

        response.setContentType("text/xml;charset=UTF-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "0");
        response.setStatus(HttpServletResponse.SC_OK);
        final OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        writer.write(getContent(o));
        writer.close();

        return null;
    }

    abstract protected String getContent(Object o);

    protected String getQuoteContent(String attributes, Map<String, Quote> symbolToQuote) {
        final String[] attribs = attributes.split(",");
        final List<Field> fields = new ArrayList<>(attribs.length);
        for (final String attrib : attribs) {
            fields.add(Field.valueOf(attrib));
        }

        final List<Quote> quotes = new ArrayList<>(symbolToQuote.values());
        CollectionUtils.removeNulls(quotes);
        final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);
        final Map<String, PriceRecord> vwdsymbolToPriceRecord = new HashMap<>(prices.size());
        final Map<String, Map<String, BasicHistoricRatios>> vwdsymbolToRatios = new HashMap<>(prices.size());
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord pr = prices.get(i);
            vwdsymbolToPriceRecord.put(quote.getSymbolVwdfeed(), pr);

            final List<BasicHistoricRatios> ratios =
                    this.historicRatiosProvider.getBasicHistoricRatios(SymbolQuote.create(quote), null, getIntervals());
            vwdsymbolToRatios.put(quote.getSymbolVwdfeed(), toRatiosByPeriod(pr, ratios));
        }

        final StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<kurse>");

        for (final Map.Entry<String, Quote> entry : symbolToQuote.entrySet()) {
            if (entry.getValue() == null) {
                sb.append("<quote_").append(entry.getKey()).append("/>");
                continue;
            }

            final PriceRecord pr = vwdsymbolToPriceRecord.get(entry.getValue().getSymbolVwdfeed());
            final Map<String, BasicHistoricRatios> ratios = vwdsymbolToRatios.get(entry.getValue().getSymbolVwdfeed());
            sb.append("<quote_").append(entry.getKey()).append(">");
            for (final Field field : fields) {
                sb.append(field.getFormatter().format(field.name(), entry.getValue(), pr, ratios));
            }
            sb.append("</quote_").append(entry.getKey()).append(">");
        }

        sb.append("</kurse>");

        return sb.toString();
    }

    private Map<String, BasicHistoricRatios> toRatiosByPeriod(PriceRecord pr,
                                                              List<BasicHistoricRatios> ratios) {
        final Map<String, BasicHistoricRatios> result = new HashMap<>();
        for (int j = 0; j < PERIOD_NAMES.length; j++) {
            result.put(PERIOD_NAMES[j], ratios.get(j).copy(pr, NullPriceRecord.INSTANCE));
        }
        return result;
    }

    abstract private static class AbstractFieldFormatter {
        abstract String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios);

        String format(String tag, Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
            final String s = doFormat(quote, pr, ratios);
            if (s == null) {
                return "<" + tag + ">-</" + tag + ">";
            }
            return "<" + tag + ">" + XmlUtil.encode(s) + "</" + tag + ">";
        }
    }

    private enum Field {
        STD_Ask(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getAsk());
            }
        }),
        STD_AskTimeDate(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getDateStr(pr.getAsk().getDate());
            }
        }),
        STD_AskVolume(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getNumberStr(pr.getAsk().getVolume());
            }
        }),
        STD_Bid(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getBid());
            }
        }),
        STD_BidTimeDate(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getDateStr(pr.getBid().getDate());
            }
        }),
        STD_BidVolume(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getNumberStr(pr.getBid().getVolume());
            }
        }),
        STD_Category(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getInstrument().getInstrumentType().getDescription();
            }
        }),
        STD_Close(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getPreviousClose());
            }
        }),
        STD_CurrencyIso(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getCurrency().getSymbolIso();
            }
        }),
        STD_DiffA(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getChangeNet());
            }
        }),
        STD_DiffP(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getChangePercent(), true);
            }
        }),
        STD_High(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getHighDay());
            }
        }),
        STD_Isin(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getInstrument().getSymbolIsin();
            }
        }),
        STD_Low(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getLowDay());
            }
        }),
        STD_Market(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getMarket().getName();
            }
        }),
        WpName(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getInstrument().getName();
            }
        }),
        STD_Name(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getInstrument().getName();
            }
        }),
        STD_NameShort(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return quote.getInstrument().getName();
            }
        }),
        STD_Open(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getPrice());
            }
        }),
        STD_Price(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getPrice());
            }
        }),
        STD_PriceTimeDateMedium(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getDateStr(pr.getPrice().getDate());
            }
        }),
        STD_Sector(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return null;
            }
        }),
        STD_Volume(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getNumberStr(pr.getVolumeDay());
            }
        }),
        STD_Performance1D(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(pr.getChangePercent(), true);
            }
        }),
        STD_Performance1W(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P1W").getPerformance(), true);
            }
        }),
        STD_Performance1M(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P1M").getPerformance(), true);
            }
        }),
        STD_Performance3M(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P3M").getPerformance(), true);
            }
        }),
        STD_Performance6M(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P6M").getPerformance(), true);
            }
        }),
        STD_Performance1Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P1Y").getPerformance(), true);
            }
        }),
        STD_Performance3Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P3Y").getPerformance(), true);
            }
        }),
        STD_Performance5Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P5Y").getPerformance(), true);
            }
        }),
        STD_Performance7Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P7Y").getPerformance(), true);
            }
        }),
        STD_Performance10Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P10Y").getPerformance(), true);
            }
        }),
        STD_Volatility1W(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P1W").getVolatility(), true);
            }
        }),
        STD_Volatility1M(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P1M").getVolatility(), true);
            }
        }),
        STD_Volatility3M(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P3M").getVolatility(), true);
            }
        }),
        STD_Volatility6M(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P6M").getVolatility(), true);
            }
        }),
        STD_Volatility1Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P1Y").getVolatility(), true);
            }
        }),
        STD_Volatility3Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P3Y").getVolatility(), true);
            }
        }),
        STD_Volatility5Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P5Y").getVolatility(), true);
            }
        }),
        STD_Volatility7Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P7Y").getVolatility(), true);
            }
        }),
        STD_Volatility10Y(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return getPriceStr(ratios.get("P10Y").getVolatility(), true);
            }
        }),
        STD_Volatility5TD(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return null;
            }
        }),
        STD_Volatility30TD(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return null;
            }
        }),
        STD_Volatility100TD(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return null;
            }
        }),
        STD_Volatility250TD(new AbstractFieldFormatter() {
            public String doFormat(Quote quote, PriceRecord pr, Map<String, BasicHistoricRatios> ratios) {
                return null;
            }
        });

        private final AbstractFieldFormatter formatter;

        private Field(AbstractFieldFormatter formatter) {
            this.formatter = formatter;
        }

        public AbstractFieldFormatter getFormatter() {
            return formatter;
        }
    }

    private static final DecimalFormat PRICE_DF = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("dd.MM.yy HH:mm");

    static {
        PRICE_DF.applyLocalizedPattern("0,00####");
    }

    private static String getPriceStr(Price p) {
        if (isInvalid(p)) {
            return null;
        }
        synchronized (PRICE_DF) {
            return PRICE_DF.format(p.getValue());
        }
    }

    private static String getPriceStr(BigDecimal p) {
        return getPriceStr(p, false);
    }

    private static String getPriceStr(BigDecimal p, boolean percent) {
        if (p == null) {
            return null;
        }
        synchronized (PRICE_DF) {
            return PRICE_DF.format(percent ? p.movePointRight(2) : p) + "%";
        }
    }

    private static String getDateStr(DateTime date) {
        if (date == null) {
            return null;
        }
        return DATE_FORMAT.print(date);
    }

    private static String getNumberStr(Number n) {
        if (n == null) {
            return null;
        }
        return Long.toString(n.longValue());
    }

    private static boolean isInvalid(Price price) {
        return (price == null) || (price == NullPrice.INSTANCE) || (price.getValue() == null);
    }

    protected UserCommandImpl getUserCommand() {
        final UserCommandImpl uc = new UserCommandImpl();
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile == null || !(profile instanceof VwdProfile)) {
            return null;
        }
        uc.setUserid(((VwdProfile) profile).getVwdId());
        uc.setCompanyid(this.companyid);
        return uc;
    }

    protected List<Interval> getIntervals() {
        final List<Interval> result = new ArrayList<>();
        final DateTime now = new DateTime();
        for (final Period p : PERIODS) {
            result.add(new Interval(p, now));
        }
        return result;
    }

}
