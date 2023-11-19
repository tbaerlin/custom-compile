/*
 * ImgChartAnalyseMethod.java
 *
 * Created on 17.12.2007 13:47:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.MessageSource;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import de.marketmaker.istar.analyses.frontend.AnalysesRequest;
import de.marketmaker.istar.analyses.frontend.AnalysesSummaryResponse;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.component.datadrawstyle.CandleDrawStyle;
import de.marketmaker.istar.chart.component.datadrawstyle.HighLowDrawStyle;
import de.marketmaker.istar.chart.component.datadrawstyle.LineDrawStyle;
import de.marketmaker.istar.chart.component.datadrawstyle.OhlcDrawStyle;
import de.marketmaker.istar.chart.component.datadrawstyle.VolumeDrawStyle;
import de.marketmaker.istar.chart.data.TimeSeriesCollection;
import de.marketmaker.istar.common.spring.MessageSourceFactory;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.StockAnalysisAims;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayData;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.ProviderPreference;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.web.easytrade.block.CurrencyConversionMethod;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.analyses.RscCommand;

import static de.marketmaker.istar.merger.web.easytrade.block.ImgChartAnalyse.getCacheKey;

/**
 * Method object to create the model for a single ImgChartAnalyse invocation. In contrast to the
 * ImgChartAnalyse object itself, which is used for all charts, this object can maintain various
 * data objects in instance fields, which allows to create a number of small, readable
 * methods.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class ImgChartAnalyseMethod {
    static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    static final DateTimeFormatter DTF_US = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final Pattern YIELD_SYMBOL = Pattern.compile("REXP.*\\.FFM");

    final static MessageSource MESSAGES
            = MessageSourceFactory.create(InstrumentTypeEnum.class);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ImgChartAnalyse controller;

    protected final BindException errors;

    protected final ImgChartAnalyseCommand cmd;

    protected final Map<String, Object> model;

    protected final ChartModelAndView result;

    protected final Locale locale;

    protected Quote[] benchmarks;

    protected Quote quote;

    protected final QuoteNameStrategy qns
            = RequestContextHolder.getRequestContext().getQuoteNameStrategy();

    /**
     * if defined, this is the "overlying" for the underlying {@link #quote}
     */
    protected Instrument derivative;

    protected boolean isFund;

    protected boolean isIgnoreVolumeType;

    protected List<BigDecimal> shares;

    protected PriceRecord intradayPrice;

    protected static final String COLOR_SUFFIX = ".color";

    protected static final String LABEL_SUFFIX = ".label";

    protected static final String LAST_PRICE = "lastprice";

    private final HistoricDataProfiler historicDataProfiler = new HistoricDataProfiler();

    public static ImgChartAnalyseMethod create(ImgChartAnalyse controller, BindException errors,
                                               ImgChartAnalyseCommand cmd) {
        return cmd.isIntraday()
                ? new ImgChartAnalyseIntradayMethod(controller, errors, cmd)
                : new ImgChartAnalyseHistoricMethod(controller, errors, cmd, controller.getHistoricConfigurationMBean());
    }

    private void restrictInterval() {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Entitlement entitlement = this.historicDataProfiler.getEntitlement(profile, this.quote);

        if (!entitlement.isRestricted()) {
            return;
        }

        entitlement.getStart().ifPresent(start -> {
            if (this.cmd.getFrom() != null) {
                LocalDate oldFromDate = DTF.parseLocalDate(this.cmd.getFrom());
                if (oldFromDate.isBefore(start.toLocalDate())) {
                    this.cmd.setFrom(start.toLocalDate().toString());
                }
            } else {
                this.cmd.setFrom(start.toLocalDate().toString());
            }
        });

        entitlement.getEnd().ifPresent(end -> {
            if (this.cmd.getTo() != null) {
                LocalDate oldToDate = DTF.parseLocalDate(this.cmd.getTo());
                if (oldToDate.isAfter(end.toLocalDate())) {
                    this.cmd.setTo(end.toLocalDate().toString());
                }
            } else {
                this.cmd.setFrom(end.toLocalDate().toString());
            }
        });
    }

    protected ImgChartAnalyseMethod(ImgChartAnalyse controller, BindException errors,
            ImgChartAnalyseCommand cmd) {
        this.controller = controller;
        this.errors = errors;
        this.cmd = cmd;
        this.locale = (this.cmd.getLocale() == null)
                ? RequestContextHolder.getRequestContext().getLocale()
                : new Locale(this.cmd.getLocale());
        this.result = controller.createResult(cmd);
        this.model = result.getModel();
        this.model.put("hilo", cmd.isHilo());
        this.quote = getInstrumentProvider().getQuote(this.cmd);

        restrictInterval();

        if (cmd.isLogScales()) {
            this.result.withLogScales();
        }
        if (this.quote != null) {
            this.result.addObject("qid", this.quote.getId());
            if (StringUtils.hasText(this.cmd.getCurrency())) {
                this.result.addObject("currency", this.cmd.getCurrency());
            }
            else if (StringUtils.hasText(this.quote.getCurrency().getSymbolIso())) {
                this.result.addObject("currency", this.quote.getCurrency().getSymbolIso());
            }
        }
    }

    protected boolean isWithOpenHighLow(String aStyleKey) {
        //noinspection StringEquality
        return aStyleKey == OhlcDrawStyle.STYLE_KEY
                || aStyleKey == CandleDrawStyle.STYLE_KEY
                || aStyleKey == HighLowDrawStyle.STYLE_KEY;
    }


    protected String getStyleName() {
        final String type = this.cmd.getType();
        if (type.indexOf('.') < 0) {
            return getStyleKey();
        }
        return type.substring(type.indexOf('.') + 1);
    }

    protected String getStyleKey() {
        String type = this.cmd.getType();
        if (type.indexOf('.') != -1) {
            type = type.substring(0, type.indexOf('.'));
        }
        if ("line".equals(type)) {
            return LineDrawStyle.STYLE_KEY;
        }
        if ("bar".equals(type)) {
            return VolumeDrawStyle.STYLE_KEY;
        }
        if ("ohlc".equals(type)) {
            return OhlcDrawStyle.STYLE_KEY;
        }
        if ("candle".equals(type)) {
            return CandleDrawStyle.STYLE_KEY;
        }
        if ("hilo".equals(type)) {
            return HighLowDrawStyle.STYLE_KEY;
        }
        return LineDrawStyle.STYLE_KEY;
    }

    protected Interval getInterval() {
        return getInterval(this.cmd, this.quote);
    }

    public static Interval getInterval(ImgChartAnalyseCommand cmd, Quote q) {
        final LocalDate today = new LocalDate();
        LocalDate from = parseDate(cmd.getFrom(), q);
        LocalDate to = parseDate(cmd.getTo(), q);
        if (to != null && to.isAfter(today)) {
            to = today;
        }
        if (from == null || to == null || from.isAfter(to) || !from.isBefore(today)) {
            return AbstractImgChart.getInterval(cmd.getPeriod());
        }
        return new Interval(from.toDateTimeAtStartOfDay(), to.toDateTimeAtStartOfDay());
    }

    private static LocalDate parseDate(String s, Quote q) {
        if (!StringUtils.hasText(s)) {
            return null;
        }

        if ("start".equals(s)) {
            return DateUtil.yyyyMmDdToLocalDate(q.getFirstHistoricPriceYyyymmdd());
        }
        if ("today".equals(s)) {
            return new LocalDate();
        }

        final DateTimeFormatter formatter = s.indexOf("-") > 0 ? DTF_US : DTF;

        try {
            final LocalDate result = formatter.parseDateTime(s).toLocalDate();
            if (result.isBefore(Constants.EARLIEST_CHART_DAY)) {
                return null;
            }
            return result;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected void identifyBenchmarks() {
        final ChartBenchmarkSupport cbs = new ChartBenchmarkSupport(this.cmd, this.quote,
                getInstrumentProvider(), controller.getFundDataProvider());
        this.benchmarks = cbs.identifyBenchmarks();
        this.shares = cbs.getShares();
    }

    private EasytradeInstrumentProvider getInstrumentProvider() {
        return this.controller.getInstrumentProvider();
    }

    protected void addTargetPrices() {
        if (isPercent() || !this.cmd.isKursziele()) {
            return;
        }
        final StockAnalysisAims aims = getAims();
        if (aims == null) {
            return;
        }
        if (aims.getMaximum().equals(aims.getMinimum())) {
            if (aims.getMaximum().compareTo(BigDecimal.ZERO) > 0) {
                this.model.put("kursziel", aims.getMaximum());
            }
        }
        else {
            this.model.put("maxKursziel", aims.getMaximum());
            if (aims.getMinimum().compareTo(BigDecimal.ZERO) > 0) {
                this.model.put("minKursziel", aims.getMinimum());
            }
        }
    }

    private StockAnalysisAims getAims() {
        if (this.controller.getStockAnalysisProvider() != null) {
            return this.controller.getStockAnalysisProvider().getAims(quote.getInstrument().getId());
        }
        if (this.controller.getAnalysesServer() == null) {
            return null;
        }
        try {
            final AnalysesRequest request = new AnalysesRequest(RscCommand.getSelector(null));
            request.setInstrumentIds(Collections.singleton(quote.getInstrument().getId()));
            final AnalysesSummaryResponse response
                    = this.controller.getAnalysesServer().getSummary(request);
            return response.getAims(quote.getInstrument().getId());
        } catch (NoDataException e) {
            return null;
        }
    }

    protected boolean isPercent() {
        if (this.cmd.isPercent() != null) {
            return this.cmd.isPercent();
        }
        if (hasBenchmarks()) {
            return true;
        }
        return this.isFund && this.cmd.isBviPerformanceForFunds();
    }

    protected boolean hasBenchmarks() {
        for (Quote benchmark : this.benchmarks) {
            if (benchmark != null) {
                return true;
            }
        }
        return false;
    }

    protected final ChartModelAndView invoke() {
        if (this.quote == null) {
            return null;
        }

        this.isFund = isFund(this.quote);
        this.isIgnoreVolumeType = isIgnoreVolumeType();

        identifyBenchmarks();
        addTargetPrices();
        addStaticData();
        fillModel();

        return isValidModel() ? result : null;
    }

    private void addStaticData() {
        if (!StringUtils.hasText(this.cmd.getDerivative())) {
            return;
        }
        try {
            this.derivative = getInstrumentProvider().identifyInstrument(this.cmd.getDerivative(), null);
        } catch (UnknownSymbolException e) {
            this.logger.warn("<addStaticData> no such derivative: '" + this.cmd.getDerivative() + "'");
            return;
        }
        if (this.derivative != null
                && this.derivative.getInstrumentType() == InstrumentTypeEnum.CER
                && (this.cmd.isBarrier() || this.cmd.isBonus())) {
            addCerStaticData();
        }
    }

    private void addCerStaticData() {
        final CertificateDataProvider dataProvider = this.controller.getCertificateDataProvider();
        if (dataProvider == null) {
            return;
        }
        final MasterDataCertificate data = dataProvider.getMasterData(this.derivative.getId(), ProviderPreference.VWD);
        if (this.cmd.isBonus()) {
            putCerValue(data.getBonuslevel(), "bonus");
        }
        if (this.cmd.isCap()) {
            putCerValue(data.getCap(), "cap");
        }
        if (this.cmd.isBarrier()) {
            putCerValue(data.getBarrier(), "barrier");
            if (data.getKnockoutdate() != null) {
                this.model.put("cer_barrier_reached", data.getKnockoutdate());
            }
        }
    }

    private void putCerValue(BigDecimal value, String name) {
        if (value != null) {
            this.model.put("cer_" + name, value);
        }
    }

    protected boolean isValidModel() {
        return this.model.containsKey("main");
    }

    protected boolean isFund(final Quote q) {
        return InstrumentUtil.isVwdFund(q);
    }

    protected abstract void fillModel();

    protected void addIntradayData() {
        initIntradayData();
        if (this.intradayPrice != null) {
            final Price price = this.intradayPrice.getPrice();
            // lastprice is usually not displayed, but used to compute a sensible axis scale
            // even if no intraday data is available.
            this.model.put(LAST_PRICE, isPercent() ? Constants.ONE_HUNDRED : price.getValue());
            this.model.put("lastdate", price.getDate());
            this.model.put("changenet", this.intradayPrice.getChangeNet());
            this.model.put("changepct", this.intradayPrice.getChangePercent());
        }
    }

    protected void initIntradayData() {
        if (this.intradayPrice != null) {
            return;
        }
        final Ehcache cache = this.controller.getChartPriceCache();
        if (cache != null) {
            final Element element = cache.get(getCacheKey(this.quote, this.cmd.getCurrency()));
            if (element != null) {
                this.intradayPrice = (PriceRecord) element.getObjectValue();
            }
        }
        if (this.intradayPrice == null) {
            final IntradayProvider ip = this.controller.getIntradayProvider();
            final IntradayData data = ip.getIntradayData(quote, null);
            this.intradayPrice = toPriceRecord(data, this.quote);
        }
    }

    protected List<PriceRecord> getPrices(final List<Quote> quotes) {
        if (!this.cmd.isWithPrice()) {
            return Collections.nCopies(quotes.size(), null);
        }

        final List<IntradayData> datas
                = this.controller.getIntradayProvider().getIntradayData(quotes, null);

        final List<PriceRecord> result = new ArrayList<>(datas.size());
        for (int i = 0; i < quotes.size(); i++) {
            result.add(toPriceRecord(datas.get(i), quotes.get(i)));
        }
        return result;
    }

    private PriceRecord toPriceRecord(IntradayData data, final Quote q) {
        if (data == null) {
            return null;
        }
        final PriceRecord result = data.getPrice();
        if (StringUtils.hasText(this.cmd.getCurrency())) {
            return new CurrencyConversionMethod(this.controller.getIsoCurrencyConversionProvider(),
                    this.cmd.getCurrency()).invoke(q, result);
        }
        return result;
    }


    private boolean isIgnoreVolumeType() {
        if (cmd.getIgnoreVolumeTypes() != null) {
            for (final String s : cmd.getIgnoreVolumeTypes()) {
                final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(s);
                if (this.quote.getInstrument().getInstrumentType() == type) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void addTimeseriesCollection(TimeSeriesCollection c, String name) {
        this.model.put(name, c);
    }

    protected void addLabel(Quote q, String name) {
        final String label = getLabel(q, name);
        if (StringUtils.hasText(label)) {
            this.model.put(name + LABEL_SUFFIX, label);
        }
    }

    protected void addColor(Quote q, String name) {
        final String color = getColor(q, name);
        if (StringUtils.hasText(color)) {
            this.model.put(name + COLOR_SUFFIX, color);
        }
    }

    protected String getLabel(Quote q, String name) {
        if (q == this.quote) {
            return "main".equals(name) ? cmd.getLabel() : null;
        }
        for (int i = 0; i < benchmarks.length; i++) {
            if (q == benchmarks[i]) {
                return cmd.getBenchmarkLabel(i);
            }
        }
        return null;
    }

    private String getColor(Quote q, String name) {
        if (q == this.quote) {
            return "main".equals(name) ? cmd.getColor() : null;
        }
        for (int i = 0; i < benchmarks.length; i++) {
            if (q == benchmarks[i]) {
                return cmd.getBenchmarkColor(i);
            }
        }
        return null;
    }

    protected boolean isYieldBased(Quote q) {
        return YIELD_SYMBOL.matcher(q.getSymbolVwdcode()).matches();
    }
}
