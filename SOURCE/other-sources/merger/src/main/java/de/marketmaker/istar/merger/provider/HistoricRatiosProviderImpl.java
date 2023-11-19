/*
 * HistoricRatiosProvider.java
 *
 * Created on 17.07.2006 17:13:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.mm.MMKeyType;
import de.marketmaker.istar.common.mm.MMService;
import de.marketmaker.istar.common.mm.MMServiceResponse;
import de.marketmaker.istar.common.mm.MMTalkException;
import de.marketmaker.istar.common.mm.MMTalkTableRequest;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.BondRatios;
import de.marketmaker.istar.domain.data.CorporateAction;
import de.marketmaker.istar.domain.data.ExtendedHistoricRatios;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.IntervalPerformance;
import de.marketmaker.istar.domain.data.NullBondRatios;
import de.marketmaker.istar.domain.data.NullWarrantRatios;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.QuarterlyYield;
import de.marketmaker.istar.domain.data.TradingPhase;
import de.marketmaker.istar.domain.data.WarrantRatios;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.data.AbstractCorporateAction;
import de.marketmaker.istar.domainimpl.data.BasicHistoricRatiosImpl;
import de.marketmaker.istar.domainimpl.data.BondRatiosImpl;
import de.marketmaker.istar.domainimpl.data.ExtendedHistoricRatiosImpl;
import de.marketmaker.istar.domainimpl.data.HighLowImpl;
import de.marketmaker.istar.domainimpl.data.IntervalUnit;
import de.marketmaker.istar.domainimpl.data.NullBasicHistoricRatios;
import de.marketmaker.istar.domainimpl.data.NullExtendedHistoricRatios;
import de.marketmaker.istar.domainimpl.data.NullHighLow;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.data.QuarterlyYieldImpl;
import de.marketmaker.istar.domainimpl.data.TradingPhaseImpl;
import de.marketmaker.istar.domainimpl.data.WarrantRatiosImpl;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseriesUtils;
import de.marketmaker.istar.ratios.backend.FormulaAdaptor;
import de.marketmaker.istar.ratios.backend.QuoteCategorizer;
import de.marketmaker.istar.ratios.backend.QuoteCategorizerImpl;

import static de.marketmaker.istar.domain.data.PriceQuality.END_OF_DAY;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricRatiosProviderImpl implements InitializingBean, WarrantRatiosProvider,
        BondRatiosProvider, HistoricRatiosProvider, HighLowProvider, TradingPhaseProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static Double NOT_AVAILABLE = Double.valueOf("1.0E+307");

    private final static DateTime ALLTIME = new DateTime().minusYears(100).plusDays(1);

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final static Map<String, String> PM_CURRENCY_TO_ISO = new HashMap<>();

    private final static DecimalFormat MM_DF = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);

    static {
        PM_CURRENCY_TO_ISO.put("DM", "DEM");
        MM_DF.applyLocalizedPattern("0,####");
    }

    private static final List<String> SIGNAL_FORMULAS = Arrays.asList(
            "ShortPhase[]",
            "KaufDatum[]",
            "VerkaufsDatum[]",
            "Gewinn[]"
    );

    private static final List<String> CA_STANDARD = Arrays.asList(
            "#datum.map[factorlist]",
            "#number.map[factorlist]",
            "#datum.map[yieldlist]",
            "#number.map[yieldlist]",
            "#währung.map[yieldlist]",
            "$wp := object; $formula := #[]( $yield := object; $closeEx := $wp.Close[$yield.Währung; _; _; 1; $yield.datum].At[$yield.datum-2; 30]; $yieldPercent := ($yield.number / ($closeEx+$yield.number)); ((1 / (1-$yieldPercent))-1) ); $formula.map[yieldlist]"
    );

    private static final List<String> CA_FACTORIZED_DIVS = Arrays.asList(
            "#datum.map[factorlist]",
            "#number.map[factorlist]",
            "#datum.map[yieldlist]",
            "#number.map[yieldlist[_;\"true\"]]",
            "#währung.map[yieldlist]",
            "$wp := object; $formula := #[]( $yield := object; $closeEx := $wp.Close[$yield.Währung; _; _; 1; $yield.datum].At[$yield.datum-2; 30]; $yieldPercent := ($yield.number / ($closeEx+$yield.number)); ((1 / (1-$yieldPercent))-1) ); $formula.map[yieldlist[_;\"true\"]]"
    );

    private MMService pm;

    private QuoteCategorizer categorizer;

    private File signalSystemsFile;

    private final Map<SignalSystem.Key, SignalSystem> signalSystems = new HashMap<>();

    public void setCategorizer(QuoteCategorizer categorizer) {
        this.categorizer = categorizer;
    }

    public void setPm(MMService pm) {
        this.pm = pm;
    }

    public void setSignalSystemsFile(File signalSystemsFile) {
        this.signalSystemsFile = signalSystemsFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.signalSystemsFile == null) {
            this.logger.warn("<afterPropertiesSet> no signalSystemsFile set => no trading phases will be available");
            return;
        }

        final SAXBuilder builder = new SAXBuilder();
        final Document configuration = builder.build(this.signalSystemsFile);

        @SuppressWarnings("unchecked")
        final List<Element> elements = configuration.getRootElement().getChild("systems").getChildren("system");
        for (Element element : elements) {
            final TradingPhase.SignalSystem name = TradingPhase.SignalSystem.valueOf(element.getChildTextTrim("name"));
            final String preFormula = element.getChildTextTrim("preformula");
            final TradingPhase.SignalSystem.Strategy strategy =
                    TradingPhase.SignalSystem.Strategy.valueOf(element.getChildTextTrim("strategy"));
            final SignalSystem system = new SignalSystem(name, strategy, preFormula);
            this.signalSystems.put(system.getKey(), system);
        }
        this.logger.info("<afterPropertiesSet> signal systems: " + this.signalSystems);
    }

    public TradingPhaseResponse getTradingPhases(TradingPhaseRequest request) {
        try {
            return doGetTradingPhases(request);
        } catch (Exception e) {
            this.logger.error("<getTradingPhases> failed", e);
            final TradingPhaseResponse invalid = new TradingPhaseResponse();
            invalid.setInvalid();
            return invalid;
        }
    }

    private TradingPhaseResponse doGetTradingPhases(TradingPhaseRequest request) {
        final TradingPhaseResponse response = new TradingPhaseResponse();

        if (request.getSystems() == null || request.getStrategies() == null) {
            response.setInvalid();
            return response;
        }

        final List<TradingPhase.SignalSystem> systems = Arrays.asList(request.getSystems());
        final List<TradingPhase.SignalSystem.Strategy> strategies = Arrays.asList(request.getStrategies());

        final List<SignalSystem> toCalc = new ArrayList<>();
        for (final TradingPhase.SignalSystem system : systems) {
            for (final TradingPhase.SignalSystem.Strategy strategy : strategies) {
                final SignalSystem.Key key = new SignalSystem.Key(system, strategy);
                toCalc.add(this.signalSystems.get(key));
            }
        }

        for (final SymbolQuote quote : request.getQuotes()) {
            if (quote == null) {
                continue;
            }
            response.add(quote, getTradingPhases(quote, toCalc, request.getInterval(), request.getShortPhases()));
        }

        return response;
    }

    private List<TradingPhase> getTradingPhases(SymbolQuote quote, List<SignalSystem> systems,
            Interval interval, Boolean shortPhases) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getTradingSignals> no mmwkn for quote: " + quote.getId());
            return Collections.emptyList();
        }

        // need some time before core interval
        final LocalDate ymd = interval.getStart().minusMonths(6).toLocalDate();
        final String startDay = getStartDay(ymd);

        final List<TradingPhase> result = new ArrayList<>();

        for (final SignalSystem ss : systems) {
            final String ts = ss.getPreFormula();
            final String preformula = ts.replace("$date", startDay);

            result.addAll(getTradingPhases(quote.getSymbolMmwkn(), preformula, interval,
                    shortPhases, ss.getKey().getSystem(), ss.getKey().getStrategy()));
        }

        return result;
    }

    public List<TradingPhase> getTradingPhases(String mmwkn, String preformula,
            Interval interval, Boolean shortPhases, TradingPhase.SignalSystem system,
            TradingPhase.SignalSystem.Strategy strategy) {

        try {
            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withPreFormula(preformula)
                    .withFormulas(SIGNAL_FORMULAS)
                    .withKey(mmwkn);

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            if (mmtt == null) {
                return Collections.emptyList();
            }

            final int numSignals = mmtt.length / SIGNAL_FORMULAS.size();

            final List<TradingPhase> result = new ArrayList<>(numSignals);

            for (int i = 0; i < numSignals; i++) {
                final LocalDate startdate = getLocalDate(mmtt[i + numSignals]);
                if (startdate == null) {
                    this.logger.warn("<getTradingPhases> no startdate for " + mmwkn
                            + ", preformula=" + preformula
                            + ", interval=" + interval
                            + ", shortPhases=" + shortPhases
                            + ", system=" + system
                            + ", strategy=" + strategy
                            + ", i=" + i
                            + ", value=" + String.valueOf(mmtt[i + numSignals]));
                    continue;
                }
                if (startdate.isAfter(interval.getEnd().toLocalDate())) {
                    continue;
                }
                final LocalDate enddate = getLocalDate(mmtt[i + 2 * numSignals]);
                if (enddate != null && enddate.isBefore(interval.getStart().toLocalDate())) {
                    continue;
                }
                final BigDecimal changePercentRaw = getValue(mmtt[i + 3 * numSignals]);
                final BigDecimal changePercent = changePercentRaw != null ? changePercentRaw.setScale(5, BigDecimal.ROUND_HALF_UP) : null;

                final boolean isShort = getBoolean(mmtt[i]);
                if (shortPhases == null || isShort == shortPhases) {
                    result.add(new TradingPhaseImpl(system, strategy, startdate, enddate, changePercent, isShort));
                }
            }

            return result;
        } catch (MMTalkException e) {
            this.logger.warn("<getTradingSignals> failed for preformula: " + preformula + ", mmwkn " + mmwkn);
        }
        return Collections.emptyList();
    }

    private String getStartDay(LocalDate ymd) {
        return ymd.getDayOfMonth() + "." + ymd.getMonthOfYear() + "." + ymd.getYear();
    }

    public WarrantRatios getWarrantRatios(SymbolQuote quote, PriceRecord pr) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getWarrantRatios> no mmwkn for quote: " + quote.getId());
            return NullWarrantRatios.INSTANCE;
        }

        try {
            final List<String> formulas = new ArrayList<>();
            formulas.add(getLeverageFormula()); // Hebel
            formulas.add(getContangoFormula()); // Agio/Prämie
            formulas.add(getContangoPerYearFormula()); // Agio/Prämie p.a.
            formulas.add(getIntrinsicValueFormula()); // Innerer Wert
            formulas.add(getExtrinsicValueFormula()); // Zeitwert
            formulas.add(getBreakevenFormula());  // Breakeven
            formulas.add(getImpliedVolatilityFormula()); // Implizite Volatilität
            formulas.add(getDeltaFormula()); // Delta
            formulas.add(getFairPriceFormula()); // Fairer Preis
            formulas.add(getParityFormula()); // Parität
            formulas.add(getOmegaFormula()); // Omega
            formulas.add(getGammaFormula()); // Gamma
            formulas.add(getVegaFormula()); // Vega
            formulas.add(getRhoFormula()); // Rho
            formulas.add(getMoneynessFormula()); // Moneyness
            formulas.add(getMoneynessRelativeFormula()); // Moneyness (rel.)
            formulas.add(getThetaFormula()); // Theta
            formulas.add(getThetaRelativeFormula()); // Theta (rel.)
            formulas.add(getTheta1wFormula()); // Wochen-Theta
            formulas.add(getTheta1wRelativeFormula()); // Wochen-Theta (rel.)
            formulas.add(getTheta1mFormula()); // Theta-Monat
            formulas.add(getTheta1mRelativeFormula()); // Theta-Monat (rel.)


            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(formulas)
                    .withKey(quote.getSymbolMmwkn());

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            int index = 0;

            final BigDecimal leverage = getValue(mmtt[index++]);
            final BigDecimal contango = getValue(mmtt[index++]);
            final BigDecimal contangoPerYear = getValue(mmtt[index++]);
            final BigDecimal intrinsicValue = getValue(mmtt[index++]);
            final BigDecimal extrinsicValue = getValue(mmtt[index++]);
            final BigDecimal breakeven = getValue(mmtt[index++]);
            final BigDecimal impliedVolatility = getValue(mmtt[index++]);
            final BigDecimal delta = getValue(mmtt[index++]);
            final BigDecimal fairPrice = getValue(mmtt[index++]);
            final BigDecimal parity = getValue(mmtt[index++]);
            final BigDecimal omega = getValue(mmtt[index++]);
            final BigDecimal gamma = getValue(mmtt[index++]);
            final BigDecimal vega = getValue(mmtt[index++]);
            final BigDecimal rho = getValue(mmtt[index++]);
            final BigDecimal moneyness = getValue(mmtt[index++]);
            final BigDecimal moneynessRelative = getValue(mmtt[index++]);
            final BigDecimal theta = getValue(mmtt[index++]);
            final BigDecimal thetaRelative = getValue(mmtt[index++]);
            final BigDecimal theta1w = getValue(mmtt[index++]);
            final BigDecimal theta1wRelative = getValue(mmtt[index++]);
            final BigDecimal theta1m = getValue(mmtt[index++]);
            final BigDecimal theta1mRelative = getValue(mmtt[index]); // last element, ADD ++ if fields are added

            return new WarrantRatiosImpl(leverage, contango, contangoPerYear, intrinsicValue, extrinsicValue, breakeven,
                    impliedVolatility, delta, fairPrice, parity, omega, gamma, vega, rho,
                    moneyness, moneynessRelative, theta, thetaRelative, theta1w, theta1wRelative, theta1m, theta1mRelative);
        } catch (MMTalkException e) {
            this.logger.warn("<getWarrantRatios> failed");
        }
        return NullWarrantRatios.INSTANCE;
    }

    public WarrantRatios getWarrantRatios(Quote quote, PriceRecord pr) {
        return getWarrantRatios(SymbolQuote.create(quote), pr);
    }

    public BondRatios getBondRatios(SymbolQuote quote, BigDecimal marketRate) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getBondRatios> no mmwkn for quote: " + quote.getId());
            return NullBondRatios.INSTANCE;
        }

        try {
            final List<String> formulas = new ArrayList<>();
            formulas.add(getYieldFormula());
            formulas.add(getBrokenPeriodInterestFormula());
            formulas.add(getDurationFormula(marketRate));
            formulas.add(getConvexityFormula(marketRate));

            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(formulas)
                    .withKey(quote.getSymbolMmwkn());

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            int index = 0;

            final BigDecimal yield = getValue(mmtt[index++]);
            final BigDecimal brokenPeriodInterest = getValue(mmtt[index++]);
            final BigDecimal duration = getValue(mmtt[index++]);
            final BigDecimal convexity = getValue(mmtt[index]); // NOTE: index w/o increment

            return new BondRatiosImpl(marketRate, yield, brokenPeriodInterest, duration, convexity, null, null);
        } catch (MMTalkException e) {
            this.logger.warn("<getBondRatios> failed");
        }
        return NullBondRatios.INSTANCE;
    }

    public BondRatios getBondRatios(Quote quote, BigDecimal marketRate) {
        return getBondRatios(SymbolQuote.create(quote), marketRate);
    }

    public List<List<HighLow>> getHighLows(List<SymbolQuote> quotes, List<Interval> intervals) {
        final List<List<HighLow>> result = new ArrayList<>(quotes.size());
        for (final SymbolQuote quote : quotes) {
            result.add(getHighLow(quote, intervals));
        }
        return result;
    }

    /**
     * Always throws an exception. Wrap this instance using a
     * {@link de.marketmaker.istar.merger.provider.CachingHighLowProvider} when you need to call
     * this method
     * @param quote ignored
     * @param pr ignored
     * @throws UnsupportedOperationException
     */
    public HighLow getHighLow52W(Quote quote, PriceRecord pr) {
        throw new UnsupportedOperationException();
    }

    /**
     * Always throws an exception. Wrap this instance using a
     * {@link de.marketmaker.istar.merger.provider.CachingHighLowProvider} when you need to call
     * this method
     * @param quotes ignored
     * @param prs ignored
     * @throws UnsupportedOperationException
     */
    public List<HighLow> getHighLows52W(List<Quote> quotes, List<PriceRecord> prs) {
        throw new UnsupportedOperationException();
    }

    public List<HighLow> getHighLow(SymbolQuote quote, List<Interval> intervals) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getHighLow> no mmwkn for quote: " + quote.getId());
            return getNullResult(intervals.size());
        }

        final BigDecimal factor = HistoricTimeseriesUtils.getFactor(quote);

        try {
            final int numFormulas = intervals.size() * 2;
            final List<String> formulas = new ArrayList<>(numFormulas);
            for (final Interval interval : intervals) {
                formulas.add(getHighFormula(interval, factor, "_"));
                formulas.add(getHighDateFormula(interval, "_"));
                formulas.add(getLowFormula(interval, factor, "_"));
                formulas.add(getLowDateFormula(interval, "_"));
            }

            List<String> adaptedFormulas = adaptFormulas(quote, formulas);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getHighLow> " + adaptedFormulas);
            }

            final List<HighLow> result = new ArrayList<>(numFormulas);

            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(adaptedFormulas)
                    .withKey(quote.getSymbolMmwkn());

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getHighLow> " + Arrays.deepToString(mmtt));
            }
            int index = 0;
            for (final Interval interval : intervals) {
                final BigDecimal high = getValue(mmtt[index++]);
                final BigDecimal highDate = getValue(mmtt[index++]);
                final PriceImpl highPrice = toPrice(high, highDate);

                final BigDecimal low = getValue(mmtt[index++]);
                final BigDecimal lowDate = getValue(mmtt[index++]);
                final PriceImpl lowPrice = toPrice(low, lowDate);

                final HighLow hl = new HighLowImpl(interval, highPrice, lowPrice);
                result.add(hl);
            }

            return result;

        } catch (MMTalkException e) {
            this.logger.warn("<getHighLow> failed: " + e.getMessage());
        } catch (Throwable t) {
            this.logger.error("<getHighLow> failed", t);
        }
        return getNullResult(intervals.size());
    }

    private List<String> adaptFormulas(SymbolQuote quote, List<String> formulas) {
        return FormulaAdaptor.adapt(categorizer.categorize(quote.getSymbolVwdfeed()), formulas);
    }

    private PriceImpl toPrice(BigDecimal bd, BigDecimal date) {
        final DateTime dateTime = (date != null && date.doubleValue() > 0)
                ? DateUtil.comDateToDateTime(date.doubleValue()) : null;
        return new PriceImpl(bd, null, null, dateTime, END_OF_DAY);
    }

    private List<HighLow> getNullResult(int num) {
        return Collections.nCopies(num, NullHighLow.INSTANCE);
    }

    @Override
    public List<IntervalPerformance> getIntervalPerformances(SymbolQuote quote, IntervalUnit unit,
            final int intervals) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getIntervalPerformances> no mmwkn for quote: " + quote.getId());
            return Collections.emptyList();
        }

        if (intervals < 0) {
            this.logger.warn("<getIntervalPerformances> has negative year value: " + intervals);
            return Collections.emptyList();
        }

        try {
            final String formula = "$security := object;" +
                    "Map(#[$to](" +
                    "$from := object;" +
                    "$Erster_Kurs := $security.Close.FromTo[Heute; 1900.Date].Nth[0].Datum;" +
                    "If($From < $Erster_Kurs;" +
                    "List($Erster_Kurs;$to;$Security.BVIPerformance[$Erster_Kurs; $to]);" +
                    "List($from;$to;$security.BVIPerformance[$from; $to])));" +
                    "Map(#[]($security.close_kurs.datum." + unit.toMMFunction("- object - 1") + "+1);5.MakeList);" +
                    "Map(#[]($security.close_kurs.datum." + unit.toMMFunction("- object") + "); 5.MakeList))";

            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormula(formula)
                    .withKey(quote.getSymbolMmwkn());

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);

            final Object[] values = (Object[]) response.getData()[0];
            if (values == null) {
                this.logger.info("<getIntervalPerformances> no data for " + quote);
                return Collections.emptyList();
            }

            final List<IntervalPerformance> result = new ArrayList<>();
            for (Object value : values) {
                int index = 0;
                Object[] list = ((Object[]) value);

                BigDecimal from = getValue(list[index++]);
                BigDecimal to = getValue(list[index++]);
                BigDecimal performance = getValue(list[index]);
                DateTime fromDate = DateUtil.comDateToDateTime(from.doubleValue());
                DateTime toDate = DateUtil.comDateToDateTime(to.doubleValue());

                if (fromDate.isBefore(toDate)) {
                    result.add(new IntervalPerformance(new Interval(fromDate, toDate), performance));
                }
            }

            return result;
        } catch (MMTalkException e) {
            this.logger.warn("<getIntervalPerformances> failed: " + e.getMessage());
        } catch (Throwable t) {
            this.logger.error("<getIntervalPerformances> failed", t);
        }
        return Collections.emptyList();
    }

    public List<QuarterlyYield> getQuarterlyYields(SymbolQuote quote) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getQuarterlyYields> no mmwkn for quote: " + quote.getId());
            return Collections.emptyList();
        }

        try {
            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormula("$fonds:= object;" +
                            "$date:= heute;" +
                            "Map(" +
                            "#[](" +
                            "$from:=$date.Quartalsanfang - 1;" +
                            "$perf:= List($from+1; $date; $fonds.BVIPerformance[$from; $date]);" +
                            "$date:= $from;" +
                            "$perf);" +
                            "21.MakeList)")
                    .withKey(quote.getSymbolMmwkn());
            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);

            final Object[] values = (Object[]) response.getData()[0];
            if (values == null) {
                this.logger.info("<getQuarterlyYields> no data for " + quote);
                return Collections.emptyList();
            }

            final List<QuarterlyYield> result = new ArrayList<>();
            for (Object value : values) {
                int index = 0;
                Object[] list = ((Object[]) value);
                BigDecimal from = getValue(list[index++]);
                BigDecimal to = getValue(list[index++]);
                BigDecimal yield = getValue(list[index]);
                Interval interval = new Interval(DateUtil.comDateToDateTime(from.doubleValue()),
                        DateUtil.comDateToDateTime(to.doubleValue()));
                result.add(new QuarterlyYieldImpl(interval, yield));
            }
            return result;
        } catch (MMTalkException e) {
            this.logger.warn("<getQuarterlyYields> failed: " + e.getMessage());
        } catch (Throwable t) {
            this.logger.error("<getQuarterlyYields> failed", t);
        }
        return Collections.emptyList();
    }

    @Override
    public List<BasicHistoricRatios> getPortfolioRatios(PortfolioRatiosRequest request) {
        final String aggregation = request.getAggregation() == null ? "_" : Double.toString(request.getAggregation());
        final List<Interval> intervals = request.getIntervals();

        try {
            final String preFormula = getPortfolioFormula(request);

            final int numFormulas = intervals.size() * 3;
            final List<String> formulas = new ArrayList<>(numFormulas);
            for (final Interval interval : intervals) {
                formulas.add(getPortfolioVolatilityFormula(interval));
                formulas.add(getPortfolioFirstPriceFormula(interval));
                formulas.add(getPortfolioCurrentPriceFormula(interval));
            }

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getPortfolioRatios> preFormula: " + preFormula);
                this.logger.debug("<getPortfolioRatios> formulas: " + formulas);
            }

            final List<BasicHistoricRatios> result = new ArrayList<>(numFormulas);

            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(formulas)
                    .withPreFormula(preFormula)
                    .withKey("I846900"); // dummy to trigger calc

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getPortfolioRatios> " + Arrays.deepToString(mmtt));
            }

            int index = 0;
            for (final Interval interval : intervals) {
                final BasicHistoricRatiosImpl bhr = new BasicHistoricRatiosImpl(interval,
                        null, null, null, null, null,
                        getValue(mmtt[index++]),
                        null,
                        getValue(mmtt[index++]),
                        getValue(mmtt[index++]),
                        null, null, null, null, null, null, null, null);

                result.add(bhr);
            }

            return result;

        } catch (MMTalkException e) {
            this.logger.warn("<getPortfolioRatios> failed", e);
        }
        return getNullRecords(intervals.size());
    }

    public static String getPortfolioFormula(PortfolioRatiosRequest request) {
        final String currency = request.getCurrency() == null ? "EUR" : request.getCurrency();

        final StringBuilder preFormula = new StringBuilder();
        preFormula.append("$WPList := List(");

        preFormula.append(request.getPositions().stream().map(position ->
                        "\"" + position.getQuote().getSymbolMmwkn() + "\".findWP[\"MMWKN\"].nth[0].MakeCollection.Add[\"Anzahl\"; " + position.getQuantity().intValue() + "]"
        ).collect(Collectors.joining(";")));

        preFormula.append(");");

        preFormula.append("map(#[](" +
                "  Value.Price[\"" + currency + "\"; true; true].FillGaps[true;\"Left\"] * Value[\"Anzahl\"]" +
//                "  Value.Close[\"" + currency + "\"; true; true] * Value[\"Anzahl\"]" +
                ");$WPList).fold[#PLUS;0]");
        return preFormula.toString();
    }

    private String getPortfolioVolatilityFormula(Interval interval) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$length := fromto[" + end + ";" + start + ";0].length;"
                + "volatilität[$length-1;" + end + "]";
    }


    private String getPortfolioFirstPriceFormula(Interval interval) {
        return "$first:=at[" + format(interval.getStart()) + "; 10];"
                + "if($first <= 0,01; na; $first)";
    }

    private String getPortfolioCurrentPriceFormula(Interval interval) {
        return "$last:=at[" + format(interval.getEnd()) + "; 10];"
                + "if($last <= 0,01; na; $last)";
    }


    public List<BasicHistoricRatios> getBasicHistoricRatios(SymbolQuote quote,
            SymbolQuote benchmarkQuote, List<Interval> intervals) {
        return getBasicHistoricRatios(quote, benchmarkQuote, intervals, null);
    }

    public List<BasicHistoricRatios> getBasicHistoricRatios(SymbolQuote quote,
            SymbolQuote benchmarkQuote, List<Interval> intervals, Double agg) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getBasicHistoricRatios> no mmwkn for quote: " + quote.getId());
            return getNullRecords(intervals.size());
        }
        final BigDecimal factor = HistoricTimeseriesUtils.getFactor(quote);

        final String aggregation = agg == null ? "_" : Double.toString(agg);

        try {
            final boolean withBenchmark = benchmarkQuote != null
                    && StringUtils.hasText(benchmarkQuote.getSymbolMmwkn());
            final String contextMmwkn = withBenchmark ? benchmarkQuote.getSymbolMmwkn() : null;

            final int numFormulas = intervals.size() * 13;
            final List<String> formulas = new ArrayList<>(numFormulas);
            for (final Interval interval : intervals) {
                formulas.add(getHighFormula(interval, factor, aggregation));
                formulas.add(getHighDateFormula(interval, aggregation));
                formulas.add(getLowFormula(interval, factor, aggregation));
                formulas.add(getLowDateFormula(interval, aggregation));
                formulas.add(getPriceLengthFormula(interval, aggregation));
                formulas.add(getPriceSumFormula(interval, factor, aggregation));
                formulas.add(getVolumeLengthFormula(interval));
                formulas.add(getVolumeSumFormula(interval));
                formulas.add(getSharpeRatioFormula(interval));
                if (agg != null && agg == 30d) {
                    formulas.add(getVolatilityLikeDeutscheBoerseWithMonthlyAggregationFormula(interval));
                }
                else {
                    formulas.add(getVolatilityFormula(interval, aggregation));
                }
                formulas.add(getMaximumLossPercentFormula(interval));
                formulas.add(getFirstPriceFormula(interval, quote, factor, aggregation));
                formulas.add(getCurrentPriceFormula(interval, quote, factor, aggregation));
                if (withBenchmark) {
                    formulas.add(getFirstPriceBenchmarkFormula(contextMmwkn, interval));
                    formulas.add(getCurrentPriceBenchmarkFormula(contextMmwkn, interval));
                    formulas.add(getAlphaFaktorFormula(contextMmwkn, interval));
                    formulas.add(getBetaFaktorFormula(contextMmwkn, interval));
                    formulas.add(getCorrelationFormula(contextMmwkn, interval));
                    formulas.add(getTrackingErrorFormula(contextMmwkn, interval));
                }
            }

            List<String> adaptedFormulas = adaptFormulas(quote, formulas);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getBasicHistoricRatios> " + adaptedFormulas);
            }

            final List<BasicHistoricRatios> result = new ArrayList<>(numFormulas);

            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(adaptedFormulas)
                    .withKey(quote.getSymbolMmwkn())
                    .withContextHandle(contextMmwkn);

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getBasicHistoricRatios> " + Arrays.deepToString(mmtt));
            }

            int index = 0;
            for (final Interval interval : intervals) {
                final BasicHistoricRatiosImpl bhr;

                final BigDecimal high = getValue(mmtt[index++]);
                final BigDecimal highDate = getValue(mmtt[index++]);
                final PriceImpl highPrice = toPrice(high, highDate);
                final BigDecimal low = getValue(mmtt[index++]);
                final BigDecimal lowDate = getValue(mmtt[index++]);
                final PriceImpl lowPrice = toPrice(low, lowDate);

                if (withBenchmark) {
                    bhr = new BasicHistoricRatiosImpl(interval,
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            highPrice, lowPrice);
                }
                else {
                    bhr = new BasicHistoricRatiosImpl(interval,
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            getValue(mmtt[index++]),
                            null,null, null, null, null, null,
                            highPrice, lowPrice);
                }

                result.add(bhr);
            }

            return result;

        } catch (MMTalkException e) {
            this.logger.warn("<getBasicHistoricRatios> failed");
        }
        return getNullRecords(intervals.size());
    }

    public List<ExtendedHistoricRatios> getExtendedHistoricRatios(SymbolQuote quote,
            SymbolQuote benchmarkQuote, List<Interval> intervals) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getExtendedHistoricRatios> no mmwkn for quote: " + quote.getId());
            return getExtendedNullRecords(intervals.size());
        }

        try {
            final boolean withBenchmark = benchmarkQuote != null
                    && StringUtils.hasText(benchmarkQuote.getSymbolMmwkn());
            final String contextMmwkn = withBenchmark ? benchmarkQuote.getSymbolMmwkn() : null;

            final int numFormulas = intervals.size() * 12;
            final List<String> formulas = new ArrayList<>(numFormulas);
            for (final Interval interval : intervals) {
                formulas.add(getLongestContinuousNegativeReturnPeriodFormula(interval));
                formulas.add(getMaximumLossPercentFormula(interval));
                formulas.add(getSharpeRatioFormula(interval));
            }

            final List<ExtendedHistoricRatios> result = new ArrayList<>(numFormulas);

            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(formulas)
                    .withKey(quote.getSymbolMmwkn())
                    .withContextHandle(contextMmwkn);

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            int index = 0;
            for (final Interval interval : intervals) {
                final BigDecimal months = getValue(mmtt[index++]);

                final ExtendedHistoricRatios bhr = new ExtendedHistoricRatiosImpl(interval,
                        months == null ? null : months.intValue(),
                        getValue(mmtt[index++]),
                        getValue(mmtt[index++]));

                result.add(bhr);
            }

            return result;

        } catch (MMTalkException e) {
            this.logger.warn("<getBasicHistoricRatios> failed");
        }
        return getExtendedNullRecords(intervals.size());
    }

    private List<BasicHistoricRatios> getNullRecords(int size) {
        final List<BasicHistoricRatios> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(NullBasicHistoricRatios.INSTANCE);
        }
        return result;
    }

    private List<ExtendedHistoricRatios> getExtendedNullRecords(int size) {
        final List<ExtendedHistoricRatios> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(NullExtendedHistoricRatios.INSTANCE);
        }
        return result;
    }

    public List<CorporateAction> getCorporateActions(SymbolQuote quote, Interval interval,
            boolean withFactorizedDividends) {
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            this.logger.warn("<getCorporateActions> no mmwkn for quote: " + quote.getId());
            return Collections.emptyList();
        }

        try {
            final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withFormulas(withFactorizedDividends ? CA_FACTORIZED_DIVS : CA_STANDARD)
                    .withKey(quote.getSymbolMmwkn());

            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] mmtt = response.getData();

            final Object[] factorDates = (Object[]) mmtt[0];
            if (factorDates == null) {
                return Collections.emptyList();
            }

            final Object[] factorNumbers = (Object[]) mmtt[1];
            final Object[] dividendDates = (Object[]) mmtt[2];
            final Object[] dividendNumbers = (Object[]) mmtt[3];
            final Object[] dividendCurrencies = (Object[]) mmtt[4];
            final Object[] dividendYield = (Object[]) mmtt[5];

            final List<CorporateAction> result = new ArrayList<>();

            for (int i = 0; i < factorDates.length; i++) {
                final DateTime date = DateUtil.comDateToDateTime((Double) factorDates[i]);
                if (interval.contains(date)) {
                    result.add(new AbstractCorporateAction.Factor(date, getValue(factorNumbers[i])));
                }
            }
            for (int i = 0; i < dividendDates.length; i++) {
                final DateTime date = DateUtil.comDateToDateTime((Double) dividendDates[i]);
                if (!interval.contains(date)) {
                    continue;
                }

                final AbstractCorporateAction.Dividend dividend
                        = new AbstractCorporateAction.Dividend(date,
                        getValue(dividendNumbers[i]), (String) dividendCurrencies[i],
                        getValue(dividendYield[i]));
                result.add(dividend);
            }

            result.sort(null);
            return result;
        } catch (MMTalkException e) {
            this.logger.warn("<getCorporateActions> failed");
        }
        return Collections.emptyList();
    }

    public PortfolioVaRLightResponse getPortfolioVaRLight(PortfolioVaRLightRequest request) {
        final StringBuilder sb = new StringBuilder();

        if (request.getDate() != null) {
            sb.append("$Auswertungsdatum:=\"")
                    .append(request.getDate().toString("dd.MM.yyyy")).append("\";");
        }
        else {
            sb.append("$Auswertungsdatum:=heute;");
        }
        if (StringUtils.hasText(request.getCurrency())) {
            sb.append("$Währung:=\"").append(request.getCurrency()).append("\";");
        }
        else {
            sb.append("$Währung:=\"EUR\";");
        }

        sb.append("$input:=EmptyList");

        String sampleMmwkn = null;
        for (final Position position : request.getPositions()) {
            if (position.getQuote() != null) {
                if (sampleMmwkn == null) {
                    sampleMmwkn = position.getQuote().getSymbolMmwkn();
                }

                sb.append(".AddLast[MakeCollection(\"a")
                        .append(position.getQuote().getSymbolMmwkn())
                        .append("\".StringSub[2]).Add[\"Quantity\";")
                        .append(toMmtalkDouble(position.getQuantity()))
                        .append("].Add[\"EntryPrice\";")
                        .append(position.getPurchasePrice() != null ? toMmtalkDouble(position.getPurchasePrice()) : "na")
                        .append("]]");
            }
        }
        sb.append(";\n" +
                "\n" +
                "{##############################################################################################################################################}\n" +
                "\n" +
                "$positions := Map(\n" +
                "  #[](\n" +
                "    $wps := Value.FindWP[\"MMWKN\"];\n" +
                "    $found := ($wps.Length > 0);\n" +
                "    $ok := $found and with[$wps.Nth[0]] (Is[\"Future\"].not and not (Is[\"Anleihe\"] and As[\"Anleihe\"].AnleiheTyp = \"Indexiert\"));\n" +
                "    if($ok; Add[\"position\"; ValueAtRiskPosition($wps.Nth[0]; Value[\"Quantity\"]; Value[\"EntryPrice\"])];\n" +
                "            Add[\"error\"; if($found; \"NoModel\"; \"NotFound\")]));\n" +
                "  $input);\n" +
                "\n" +
                "$positionsnotfound := $positions.DeleteIf[#[](Value[\"position\"].Given)];\n" +
                "$positionsfound := Map(#[](Value[\"position\"]); $positions).DeleteIfNot[#Given];\n" +
                "\n" +
                "$Zeitreihenanalysezeitraum:=_.Default[250];\n" +
                "$Prognosezeitraum:=_.Default[20];\n" +
                "$Konfidenz:=_.Default[0,95];\n" +
                "\n" +
                "\n" +
                "$PriceSeries := #[](with[Instrument](Close[$Währung; true; true; 1]));\n" +
                "$PriceSeriesOk := #[]((FromTo[$Auswertungsdatum; $Auswertungsdatum.AddWorkingDays[- $Zeitreihenanalysezeitraum]]\n" +
                "  .Length >= $Zeitreihenanalysezeitraum * 0,80) and At[$Auswertungsdatum;3].Given);\n" +
                "\n" +
                "$positionanalysis := Map(\n" +
                "  #[](\n" +
                "    $series := $PriceSeries.Apply[object];\n" +
                "    MakeCollection\n" +
                "    .Add[\"Series\"; $Series]\n" +
                "    .Add[\"SeriesOk\"; $PriceSeriesOk.Apply[$Series]]\n" +
                "    );\n" +
                "  $positionsfound);\n" +
                "  \n" +
                "$positionsNotOK := $positionanalysis.DeleteIf[#[](Value[\"SeriesOk\"])];\n" +
                "$positionsOK := $positionanalysis.DeleteIfNot[#[](Value[\"SeriesOk\"])];\n" +
                "$NormInv := #[](if(object = 0,95;1,65; if(object = 0,99; 2,33; na)));\n" +
                "\n" +
                "$VaR := #[](\n" +
                "  $totalseries:= map(#[](Value[\"Series\"].FillGaps[true; \"Interpolate\"] / Value.Instrument.Kursfaktor * Value.Bestand);object).Fold[#plus;0];\n" +
                "  $returns := Ln($totalseries / $totalseries.before[1]);\n" +
                "  $VaR := $returns.StdNormal[$Zeitreihenanalysezeitraum].At[$Auswertungsdatum]\n" +
                "          * $NormInv.Apply[$Konfidenz] * Sqrt($Prognosezeitraum) * 100;\n" +
                "  $VaR := $VaR.RoundN[2].Default[na];\n" +
                "  $value := $totalseries.At[$Auswertungsdatum];\n" +
                "  $RK  := if($VaR.Given.Not; na; if($VaR <= 2,5; 1 ; if($VaR <= 7,5; 2; if($VaR <= 12,5; 3; if($VaR <= 17,5; 4; 5)))));\n" +
                "  _.MakeCollection\n" +
                "  .Add[\"VaR\"; $VaR]\n" +
                "  .Add[\"RK\"; $RK]\n" +
                "  .Add[\"Value\"; $value]\n" +
                "  .Add[\"VaRabs\"; $value * $VaR / 100]\n" +
                ");\n" +
                "\n" +
                "$VaRResult := $VaR.Apply[$positionsOK];\n" +
                "\n" +
                "List(\n" +
                "List(\"VaR\"; $VaRResult.Value[\"VaR\"]);\n" +
                "List(\"RK\"; $VaRResult.Value[\"RK\"]);\n" +
                "List(\"Value\"; $VaRResult.Value[\"Value\"]);\n" +
                "List(\"VaRabsolute\"; $VaRResult.Value[\"VaRabs\"]);\n" +
                "List(\"Currency\"; $Währung);\n" +
                "List(\"EvaluationDate\"; $Auswertungsdatum)\n" +
                ")\n" +
                ".Append[Map(#[](List(\"Error.\" + Value[\"error\"]; Value)); $positionsnotfound)]\n" +
                ".Append[Map(#[](List(\"Error.InsuffientData\"; Value.Instrument.WKN)); $positionsNotOK)]");

        final MMTalkTableRequest tableRequest = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                .appendKey(sampleMmwkn)
                .appendFormula("nth[0]")
                .appendFormula("nth[1]")
                .withPreFormula(sb.toString());


        try {
            final MMServiceResponse response = this.pm.getMMTalkTable(tableRequest);
            final Object[] data = response.getData();

            final int numErrors = (data.length - 12) / 2;
            final BigDecimal rawVar = getValue(data[6 + numErrors]);
            final BigDecimal var = rawVar != null ? rawVar.movePointLeft(2) : null;
            final BigDecimal riskclass = getValue(data[7 + numErrors]);
            final BigDecimal portfolioValue = getValue(data[8 + numErrors]);
            final BigDecimal varAbsolute = getValue(data[9 + numErrors]);
            final String currency = (String) data[10 + numErrors];
            final DateTime date = getDate(data[11 + numErrors]);

            final String[] errorsAsMmwkns = new String[numErrors];
            for (int i = 0; i < numErrors; i++) {
                errorsAsMmwkns[i] = (String) data[12 + numErrors + i];
            }

            final Map<String, SymbolQuote> failedQuotes = getFailedQuotes(request.getPositions(), errorsAsMmwkns);
            return new PortfolioVaRLightResponse(var, riskclass.intValue(), portfolioValue, varAbsolute, currency, date, failedQuotes);
        } catch (MMTalkException e) {
            this.logger.error("<getPortfolioVaRLight> failed", e);
        }

        return null;
    }

    public List<List<Price>> getHistoricPrices(List<SymbolQuote> quotes, List<LocalDate> dates) {
        if (quotes == null || dates == null || quotes.isEmpty() || dates.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<List<Price>> result = new ArrayList<>(quotes.size());
        for (SymbolQuote quote : quotes) {
            result.add(getHistoricPrices(quote, dates));
        }
        return result;
    }

    public List<Price> getHistoricPrices(SymbolQuote quote, List<LocalDate> dates) {
        if (quote == null || quote.getSymbolMmwkn() == null || dates == null || dates.isEmpty()) {
            return Collections.nCopies(dates != null ? Math.max(dates.size(), 1) : 1, null);
        }

        final MMTalkTableRequest request = new MMTalkTableRequest(MMKeyType.SECURITY_WKN);
        request.appendKey(quote.getSymbolMmwkn());
        request.appendFormula("Währung");
        for (final LocalDate date : dates) {
            final String dateStr = DTF.print(date);
            request.appendFormula("close.at[\"" + dateStr + "\";30]");
            request.appendFormula("close.at[\"" + dateStr + "\";30].datum");
            request.appendFormula("volume.at[\"" + dateStr + "\";30]");
            request.appendFormula("volume.at[\"" + dateStr + "\";30].datum");
        }

        final MMServiceResponse response;
        try {
            response = this.pm.getMMTalkTable(request);
        } catch (MMTalkException e) {
            this.logger.error("<getHistoricPrices> failed", e);
            return Collections.nCopies(dates.size(), null);
        }

        final Object[] data = response.getData();
        int i = 0;
        final String currency = String.valueOf(data[i++]);
        BigDecimal factor = getCurrencyFactor(quote, currency);

        final List<Price> result = new ArrayList<>(dates.size());
        while (i < data.length) {
            final BigDecimal value = multiply(factor, getValue(data[i++]));
            final DateTime date = getDate(data[i++]);
            final BigDecimal volumeRaw = getValue(data[i++]);
            final DateTime volumeDate = getDate(data[i++]);

            final Long volume = date != null && volumeDate != null && volumeRaw != null && date.equals(volumeDate)
                    ? volumeRaw.longValue()
                    : null;

            final Price p = value != null && date != null
                    ? new PriceImpl(value, volume, null, date, PriceQuality.END_OF_DAY)
                    : NullPrice.INSTANCE;
            result.add(p);
        }
        return result;
    }

    private BigDecimal multiply(BigDecimal factor, BigDecimal value) {
        return (factor != null && value != null) ? factor.multiply(value) : null;
    }

    private BigDecimal getCurrencyFactor(SymbolQuote quote, String pmCurrency) {
        final String iso = quote.getCurrencyIso();
        if (!StringUtils.hasText(iso)) {
            this.logger.warn("<getCurrencyFactor> no currency for " + quote);
            return null;
        }
        if (!StringUtils.hasText(pmCurrency)) {
            this.logger.warn("<getCurrencyFactor> no pm currency for " + quote);
            return null;
        }
        if (iso.equals(pmCurrency) || iso.equals(PM_CURRENCY_TO_ISO.get(pmCurrency))) {
            return BigDecimal.ONE;
        }
        if (pmCurrency.equals(CurrencyDp2.getBaseCurrencyIso(iso))) {
            return Constants.ONE_HUNDRED;
        }
        this.logger.warn("<getCurrencyFactor> from '" + pmCurrency + "' for " + quote + " is undefined");
        return null;
    }

    private Map<String, SymbolQuote> getFailedQuotes(
            List<Position> positions, String[] mmwkns) {

        final Map<String, SymbolQuote> quotes = new HashMap<>();
        NEXT:
        for (final String mmwkn : mmwkns) {
            for (final Position position : positions) {
                final SymbolQuote quote = position.getQuote();
                if (quote != null && mmwkn.equals(quote.getSymbolMmwkn())) {
                    quotes.put(position.getSymbol(), quote);
                    continue NEXT;
                }
            }
        }

        for (final Position position : positions) {
            if (position.getQuote() == null) {
                quotes.put(position.getSymbol(), null);
            }
        }

        return quotes;
    }

    private String toMmtalkDouble(BigDecimal value) {
        return value.toPlainString().replace('.', ',');
    }

    public static BigDecimal getValue(Object o) {
        if (o == null || !(o instanceof Double)) {
            return null;
        }
        final Double val = (Double) o;
        if (val >= NOT_AVAILABLE) {
            return null;
        }

        return BigDecimal.valueOf(val);
    }

    private boolean getBoolean(Object o) {
        return !((o == null) || !(o instanceof Boolean)) && (Boolean) o;
    }

    private DateTime getDate(Object o) {
        if ((o == null) || !(o instanceof Double)) {
            return null;
        }

        final double dbl = (Double) o;
        return (dbl > 0d) ? new DateTime(DateUtil.comDateToDate(dbl)) : null;
    }

    public static LocalDate getLocalDate(Object o) {
        if ((o == null) || !(o instanceof Double)) {
            return null;
        }

        final double dbl = (Double) o;
        return (dbl > 0d) ? new LocalDate(DateUtil.comDateToDate(dbl)) : null;
    }

    private String format(DateTime dt) {
        return dt.isAfter(ALLTIME)
                ? "\"" + DTF.print(dt) + "\""
                : "erster_kurs.datum";
    }

    private String getYieldFormula() {
        return "rendite";
    }

    private String getLeverageFormula() {
        return "hebel";
    }

    private String getContangoFormula() {
        return "prämie";
    }

    private String getContangoPerYearFormula() {
        return "prämiepa";
    }

    private String getIntrinsicValueFormula() {
        return "InnererWert";
    }

    private String getExtrinsicValueFormula() {
        return "Zeitwert";
    }

    private String getBreakevenFormula() {
        return "Breakeven";
    }

    private String getDeltaFormula() {
        return "Delta";
    }

    private String getFairPriceFormula() {
        return "FairerPreis";
    }

    private String getImpliedVolatilityFormula() {
        return "ImpliziteVolatilität";
    }

    private String getParityFormula() {
        return "$k:=BasisPapier.Aktuell[_;$Datum];  if(Optionstyp=\"call\";$k - strike; strike - $k)*Aktien/Scheine";
    }

    private String getOmegaFormula() {
        return "Omega";
    }

    private String getGammaFormula() {
        return "Gamma";
    }

    private String getVegaFormula() {
        return "Vega";
    }

    private String getRhoFormula() {
        return "Rho";
    }

    private String getMoneynessFormula() {
        return "$k:=BasisPapier.Aktuell[_;$Datum];  if(Optionstyp=\"call\";$k - strike; strike - $k)";
    }

    private String getMoneynessRelativeFormula() {
        return "$k:=BasisPapier.Aktuell[_;$Datum];  if(Optionstyp=\"call\";$k/strike-1;1-$k/strike)";
    }

    private String getThetaFormula() {
        return "Theta";
    }

    private String getThetaRelativeFormula() {
        return "Theta[$Datum]/FairerPreis";
    }

    private String getTheta1wFormula() {
        return "Theta[$Datum]*7";
    }

    private String getTheta1wRelativeFormula() {
        return "Theta[$Datum]*7/FairerPreis";
    }

    private String getTheta1mFormula() {
        return "Theta[$Datum]*30";
    }

    private String getTheta1mRelativeFormula() {
        return "Theta[$Datum]*30/FairerPreis";
    }

    private String getConvexityFormula(BigDecimal marketRate) {
        final String rate;
        synchronized (MM_DF) {
            rate = MM_DF.format(marketRate.doubleValue());
        }
        return "convexity[_;" + rate + "]";
    }

    private String getBrokenPeriodInterestFormula() {
        return "stückzinsen";
    }

    private String getDurationFormula(BigDecimal marketRate) {
        final String rate;
        synchronized (MM_DF) {
            rate = MM_DF.format(marketRate.doubleValue());
        }
        return "duration[_;" + rate + "]";
    }

    private String withFactor(String s, BigDecimal factor) {
        //noinspection NumberEquality
        if (factor == HistoricTimeseriesUtils.DEFAULT_FACTOR) {
            return s;
        }
        return s + "*" + factor.toPlainString();
    }

    private String getHighFormula(Interval interval, BigDecimal factor, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        final String s = "$fundmarkets:=list(\"EDX\";\"KAG\";\"OPC\";\"TIF\";\"ENX\";\"CS\";\"EIX\";\"SAR\");" +
                "$highonlymarkets:=list(\"XEQ\";\"XIR\";\"XCO\");" + // highonlymarkets => see comment in mm-formulas.xconf
                "$corporateActions:=not(is[\"Future\"]);" +
                "$closeMax:=close[_; $corporateActions; false;" + aggregation + "].maximum[" + start + ";" + end + "];" +
                "if(" + start + "<erster_kurs.datum;na;" +
                "if(is[\"Fonds\"] and $fundmarkets.Contains[Platz.Kürzel];" +
                "  $closeMax;" +
                "  with[object](" +
                "    $high:=high[_; $corporateActions; false;" + aggregation + "];" +
                "    $highMax:=$high.maximum[" + start + ";" + end + "];" +
                "    if($highonlymarkets.Contains[Platz.Kürzel];$highMax;" +
                "    if($highMax.Given; if(max($highMax;$closeMax)=$highMax;$highMax;$closeMax); $closeMax)" +
                "  ))" +
                "))";
        return withFactor(s, factor);
    }

    private String getHighDateFormula(Interval interval, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$fundmarkets:=list(\"EDX\";\"KAG\";\"OPC\";\"TIF\";\"ENX\";\"CS\";\"EIX\";\"SAR\");" +
                "$highonlymarkets:=list(\"XEQ\";\"XIR\";\"XCO\");" + // highonlymarkets => see comment in mm-formulas.xconf
                "$corporateActions:=not(is[\"Future\"]);" +
                "$closeMax:=close[_; $corporateActions; false;" + aggregation + "].maximum[" + start + ";" + end + "];" +
                "if(" + start + "<erster_kurs.datum;na;" +
                "if(is[\"Fonds\"] and $fundmarkets.Contains[Platz.Kürzel];" +
                "  $closeMax;" +
                "  with[object](" +
                "    $high:=high[_; $corporateActions; false;" + aggregation + "];" +
                "    $highMax:=$high.maximum[" + start + ";" + end + "];" +
                "    if($highonlymarkets.Contains[Platz.Kürzel];$highMax;" +
                "    if($highMax.Given; if(max($highMax;$closeMax)=$highMax;$highMax;$closeMax); $closeMax)" +
                "  ))" +
                ").Datum)";
    }

    private String getLowFormula(Interval interval, BigDecimal factor, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        final String s = "$fundmarkets:=list(\"EDX\";\"KAG\";\"OPC\";\"TIF\";\"ENX\";\"CS\";\"EIX\";\"SAR\");" +
                "$lowonlymarkets:=list(\"XEQ\";\"XIR\";\"XCO\");" + // lowonlymarkets => see comment in mm-formulas.xconf
                "$corporateActions:=not(is[\"Future\"]);" +
                "$closeMin:=close[_; $corporateActions; false;" + aggregation + "].minimum[" + start + ";" + end + "];" +
                "if(" + start + "<erster_kurs.datum;na;" +
                "if(is[\"Fonds\"] and $fundmarkets.Contains[Platz.Kürzel];" +
                "  $closeMin;" +
                "  with[object](" +
                "    $low:=low[_; $corporateActions; false;" + aggregation + "];" +
                "    $lowMin:=$low.minimum[" + start + ";" + end + "];" +
                "    if($lowonlymarkets.Contains[Platz.Kürzel];$lowMin;" +
                "    if($lowMin.Given; if(min($lowMin;$closeMin)=$lowMin;$lowMin;$closeMin); $closeMin)" +
                "  ))" +
                "))";
        return withFactor(s, factor);
    }

    private String getLowDateFormula(Interval interval, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$fundmarkets:=list(\"EDX\";\"KAG\";\"OPC\";\"TIF\";\"ENX\";\"CS\";\"EIX\";\"SAR\");" +
                "$lowonlymarkets:=list(\"XEQ\";\"XIR\";\"XCO\");" + // lowonlymarkets => see comment in mm-formulas.xconf
                "$corporateActions:=not(is[\"Future\"]);" +
                "$closeMin:=close[_; $corporateActions; false;" + aggregation + "].minimum[" + start + ";" + end + "];" +
                "if(" + start + "<erster_kurs.datum;na;" +
                "if(is[\"Fonds\"] and $fundmarkets.Contains[Platz.Kürzel];" +
                "  $closeMin;" +
                "  with[object](" +
                "    $low:=low[_; $corporateActions; false;" + aggregation + "];" +
                "    $lowMin:=$low.minimum[" + start + ";" + end + "];" +
                "    if($lowonlymarkets.Contains[Platz.Kürzel];$lowMin;" +
                "    if($lowMin.Given; if(min($lowMin;$closeMin)=$lowMin;$lowMin;$closeMin); $closeMin)" +
                "  ))" +
                ").Datum)";
    }

    private String getTrackingErrorFormula(String contextMmwkn, Interval interval) {
        if (!StringUtils.hasText(contextMmwkn)) {
            return "na";
        }
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());

        return String.format("$Konsolidierung:=if(%1$s.MonthsBetween[%2$s].Floor<3;1;if(%1$s.MonthsBetween[%2$s].Floor<12;5;30));"
                + "$Perioden:=if(%1$s.MonthsBetween[%2$s].Floor<3;%1$s.WorkingDaysBetween[%2$s].Floor;if(%1$s.MonthsBetween[%2$s].Floor<12;%1$s.WeeksBetween[%2$s].Floor;%1$s.MonthsBetween[%2$s].Floor));"
                + "$ZR:=#[](Close[_;true;true;$Konsolidierung]);"
                + "$f:=$ZR.apply[object];"
                + "$b:=$ZR.apply[$vgl1];"
                + "$pf:=$f/$f.before[1];"
                + "$pb:=$b/$b.before[1];"
                + "($pf/$pb-1).stdNormal[$Perioden;true].at[%2$s;30].Mult[100]", start, end);
    }

    private String getBetaFaktorFormula(String contextMmwkn, Interval interval) {
        if (!StringUtils.hasText(contextMmwkn)) {
            return "na";
        }
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$close := close;"
                + "$length := $close.fromto[" + end + ";" + start + ";0].length;"
                + "if(" + start + "<erster_kurs.datum;na;$close.BetaFaktor[$vgl1.close;" + end + ";$length-1])";
    }

    private String getAlphaFaktorFormula(String contextMmwkn, Interval interval) {
        if (!StringUtils.hasText(contextMmwkn)) {
            return "na";
        }
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return String.format("$Konsolidierung:=if(%1$s.MonthsBetween[%2$s].Floor<3;1;7);"
                + "$Perioden:=if(%1$s.MonthsBetween[%2$s].Floor<3;%1$s.WorkingDaysBetween[%2$s].Floor;%1$s.WeeksBetween[%2$s].Floor);"
                + "$f:=Price[_; true; true; $Konsolidierung];"
                + "$b:=$vgl1.Price[_; true; true; $Konsolidierung];"
                + "if(%1$s<erster_kurs.datum;na;$f.JensenRegressionAlpha[$b; $Perioden; %2$s; 0; true])", start, end);
    }

    private String getCorrelationFormula(String contextMmwkn, Interval interval) {
        if (!StringUtils.hasText(contextMmwkn)) {
            return "na";
        }
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$close := close;"
                + "$length := $close.fromto[" + end + ";" + start + ";0].length;"
                + "if(" + start + "<erster_kurs.datum;na;$close.Korrelation[$vgl1.close;" + end + ";$length])";
    }

    private String getVolatilityFormula(Interval interval, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$close := close[_;not(is[\"Future\"]);true;" + aggregation + "];"
                + "$length := $close.fromto[" + end + ";" + start + ";0].length;"
                + "if(" + start + "<erster_kurs.datum;na;$close.volatilität[$length-1;" + end + "])";
    }

    private String getVolatilityLikeDeutscheBoerseWithMonthlyAggregationFormula(Interval interval) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$old:=NA;" +
                "$close := close[_;not(is[\"Future\"]);true;30];" +
                "$length := $close.fromto[" + end + ";" + start + ";0].length;" +
                "$yields:=#[]($tmp:=$old;$old:=object.ln;$tmp-$old).MapZ[10000;nein;_;$close];" +
                "$avg:=$yields.sum[$length].at[heute;50]/$length;" +
                "$sum:=#[](sqr(object-$avg)).MapZ[10000;nein;_;$yields].sum[$length];" +
                "if(" + start + "<erster_kurs.datum;na;sqrt($sum.at[heute;50]*12/($length-1)))";
    }

    private String getSharpeRatioFormula(Interval interval) {
        final String end = format(interval.getEnd());
        final int months = interval.toPeriod(PeriodType.months()).getMonths();
        final int aggregation;
        final int bars;
        if (months <= 3) {
            aggregation = 1;
            bars = interval.toPeriod(PeriodType.days()).getDays() * 5 / 7;
        }
        else if (months <= 36) {
            aggregation = 7;
            bars = interval.toPeriod(PeriodType.weeks()).getWeeks();
        }
        else {
            aggregation = 30;
            bars = months;
        }
        return "SharpeRatioEx[" + aggregation + ";" + bars + ";" + end + "]";
    }

    private String getMaximumLossPercentFormula(Interval interval) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$f:=Close[_;true;true];$f.MinimumReturn[" + start + ";" + end + "].Exp - 1";
    }

    private String getLongestContinuousNegativeReturnPeriodFormula(Interval interval) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "$Konsolidierung:=30;"
                + "$f:=close[_; true; true; $Konsolidierung];"
                + "$f.LongestContinuousNegativeReturnPeriod[" + start + ";" + end + "]";
    }

    private String getVolumeLengthFormula(Interval interval) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "if(" + start + "<erster_kurs.datum;na;volume.fromto[" + end + ";" + start + ";0].length)";
    }

    private String getVolumeSumFormula(Interval interval) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "if(" + start + "<erster_kurs.datum;na;volume.summe[" + start + ";" + end + "])";
    }

    private String getPriceLengthFormula(Interval interval, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        return "if(" + start + "<erster_kurs.datum;na;close[_;true;false;" + aggregation + "].fromto[" + end + ";" + start + ";0].length)";
    }

    private String getPriceSumFormula(Interval interval, BigDecimal factor, String aggregation) {
        final String start = format(interval.getStart());
        final String end = format(interval.getEnd());
        final String s = "if(" + start + "<erster_kurs.datum;na;close[_;true;false;" + aggregation + "].summe[" + start + ";" + end + "])";
        return withFactor(s, factor);
    }

    private String getFirstPriceFormula(Interval interval, SymbolQuote quote, BigDecimal factor,
            String aggregation) {
        final String start = format(interval.getStart());

        final String vwdfeed = quote.getSymbolVwdfeed();
        if (vwdfeed != null && vwdfeed.startsWith("5.BMK")) {
            return "$kurse:=openinterest[true;true;" + aggregation + "];"
                    + "$first:=$kurse.at[" + start + "; 10];"
                    + "if($first <= 0,01; na; $first/100)";
        }
        return "$kurse:=close[_;true;true;" + aggregation + "];"
                + "$first:=$kurse.at[" + start + "; 10];"
                + "if($first <= 0,01; na; $first" + factorTerm(factor) + ")";

    }

    private String getCurrentPriceFormula(Interval interval, SymbolQuote quote, BigDecimal factor,
            String aggregation) {
        final String end = format(interval.getEnd());

        final String vwdfeed = quote.getSymbolVwdfeed();
        if (vwdfeed != null && vwdfeed.startsWith("5.BMK")) {
            return "$kurse:=openinterest[true;true;" + aggregation + "];"
                    + "$last:=$kurse.at[" + end + "; 10];"
                    + "if($last <= 0,01; na; $last/100)";
        }
        return "$kurse:=close[_;true;true;" + aggregation + "];"
                + "$last:=$kurse.at[" + end + "; 10];"
                + "if($last <= 0,01; na; $last" + factorTerm(factor) + ")";
    }

    private String factorTerm(BigDecimal factor) {
        //noinspection NumberEquality
        return (factor == HistoricTimeseriesUtils.DEFAULT_FACTOR) ? "" : ("*" + factor.toPlainString());
    }

    private String getFirstPriceBenchmarkFormula(String contextMmwkn, Interval interval) {
        if (!StringUtils.hasText(contextMmwkn)) {
            return "na";
        }

        final String start = format(interval.getStart());
        return "$benchkurse:=$vgl1.Close[_; true; true];"
                + "$benchfirst:=$benchkurse.at[" + start + "; 10];"
                + "if($benchfirst <= 0,01; na; $benchfirst)";
    }

    private String getCurrentPriceBenchmarkFormula(String contextMmwkn, Interval interval) {
        if (!StringUtils.hasText(contextMmwkn)) {
            return "na";
        }

        final String end = format(interval.getEnd());
        return "$benchkurse:=$vgl1.Close[_; true; true];"
                + "$benchkurse.at[" + end + ";10]";
    }

    private static class SignalSystem {
        private final Key key;

        private final String preFormula;

        public SignalSystem(TradingPhase.SignalSystem system,
                TradingPhase.SignalSystem.Strategy strategy, String preFormula) {
            this.key = new Key(system, strategy);
            this.preFormula = preFormula;
        }

        public Key getKey() {
            return key;
        }

        public String getPreFormula() {
            return preFormula;
        }

        public String toString() {
            return "SignalSystem[" + this.key + ", " + this.preFormula + "]";
        }

        private static class Key {
            private final TradingPhase.SignalSystem system;

            private final TradingPhase.SignalSystem.Strategy strategy;

            private Key(TradingPhase.SignalSystem system,
                    TradingPhase.SignalSystem.Strategy strategy) {
                this.system = system;
                this.strategy = strategy;
            }

            public TradingPhase.SignalSystem getSystem() {
                return system;
            }

            public TradingPhase.SignalSystem.Strategy getStrategy() {
                return strategy;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Key key = (Key) o;

                if (strategy != key.strategy) return false;
                //noinspection RedundantIfStatement
                if (system != key.system) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = system != null ? system.hashCode() : 0;
                result = 31 * result + (strategy != null ? strategy.hashCode() : 0);
                return result;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final RmiProxyFactoryBean proxy = new RmiProxyFactoryBean();
        proxy.setServiceUrl("rmi://tepm1:9880/pmserver");
        proxy.setServiceInterface(MMService.class);
        proxy.afterPropertiesSet();


        final HistoricRatiosProviderImpl hrp = new HistoricRatiosProviderImpl();
        hrp.setPm((MMService) proxy.getObject());
        hrp.setSignalSystemsFile(new File(LocalConfigProvider.getProductionBaseDir(), "prog/pmserver/conf/signal-systems.xml"));
        hrp.setCategorizer(new QuoteCategorizerImpl());
        hrp.afterPropertiesSet();


        final CurrencyDp2 currency = new CurrencyDp2();
        currency.setSymbol(KeysystemEnum.ISO, "EUR");

        final QuoteDp2 quote = new QuoteDp2();
        quote.setSymbol(KeysystemEnum.MMWKN, "710000");
        quote.setSymbol(KeysystemEnum.VWDFEED, "1.710000.FFM");
        quote.setCurrency(currency);

        final QuoteDp2 quote2 = new QuoteDp2();
        quote2.setSymbol(KeysystemEnum.MMWKN, "519000");
        quote2.setSymbol(KeysystemEnum.VWDFEED, "1.519000.FFM");
        quote2.setCurrency(currency);

        final QuoteDp2 quote3 = new QuoteDp2();
        quote3.setSymbol(KeysystemEnum.MMWKN, "A0LGV7");
        quote3.setSymbol(KeysystemEnum.VWDFEED, "9.LU0280778662.FONDS.EUR");
        quote3.setCurrency(currency);

        final QuoteDp2 benchmarkQuote = new QuoteDp2();
        benchmarkQuote.setSymbol(KeysystemEnum.MMWKN, "I846900");
        benchmarkQuote.setCurrency(currency);

        final QuoteDp2 thirdQuote = new QuoteDp2();
        thirdQuote.setSymbol(KeysystemEnum.MMWKN, "W0KFRMI");
        thirdQuote.setCurrency(currency);

//        testBasicRatios(hrp, quote, benchmarkQuote);
//        testBasicRatios(hrp, thirdQuote, benchmarkQuote);
//        testHistories(hrp, quote, benchmarkQuote, thirdQuote);
//        testSignals(hrp, quote);
//        testExtendedRatios(hrp);

        final List<Interval> intervals = Collections.singletonList(getInterval());

//        testBR4PR(hrp, quote, intervals);
//        testBR4PR(hrp, quote2, intervals);
        testBR4PR(hrp, quote3, intervals);

        final PortfolioRatiosRequest request = new PortfolioRatiosRequest(new LocalDate().minusDays(1), "EUR", intervals);
//        request.addPosition(quote.getSymbolMmwkn(), quote, BigDecimal.valueOf(100));
//        request.addPosition(quote2.getSymbolMmwkn(), quote2, BigDecimal.valueOf(100));
        request.addPosition(quote3.getSymbolMmwkn(), quote3, BigDecimal.valueOf(100));
        final List<BasicHistoricRatios> portfolioRatios = hrp.getPortfolioRatios(request);
        for (BasicHistoricRatios ratio : portfolioRatios) {
            System.out.println("ratio/vola/" + ratio.getReference() + ": " + ratio.getVolatility());
            System.out.println("ratio/perf/" + ratio.getReference() + ": " + ratio.getPerformance());
        }
    }

    private static void testBR4PR(HistoricRatiosProviderImpl hrp, QuoteDp2 quote,
            List<Interval> intervals) {
        final List<BasicHistoricRatios> bhr = hrp.getBasicHistoricRatios(SymbolQuote.create(quote), null, intervals);
        System.out.println("bhr/ref: " + bhr.get(0).getReference());
        System.out.println("bhr/vola: " + bhr.get(0).getVolatility());
        System.out.println("bhr/perf: " + bhr.get(0).getPerformance());
    }

    private static Interval getInterval() {
        return new Interval(DateUtil.getPeriod("P1Y"), new DateTime().minusDays(1).toLocalDate().toDateTimeAtStartOfDay());
    }

    @SuppressWarnings("UnusedDeclaration")
    private static void testHistories(HistoricRatiosProviderImpl hrp, QuoteDp2 quote,
            QuoteDp2 benchmarkQuote, QuoteDp2 thirdQuote) {
        final List<SymbolQuote> quotes = Arrays.asList(SymbolQuote.create(quote), SymbolQuote.create(benchmarkQuote), SymbolQuote.create(thirdQuote), null);
        final List<LocalDate> dates = Arrays.asList(new LocalDate(2010, 9, 1), new LocalDate(2009, 12, 31));
        final List<List<Price>> prices = hrp.getHistoricPrices(quotes, dates);
        System.out.println(prices);
    }

    @SuppressWarnings("UnusedDeclaration")
    private static void testBasicRatios(HistoricRatiosProviderImpl hrp, QuoteDp2 quote,
            QuoteDp2 benchmarkQuote) {
        final List<Interval> intervals = Collections.singletonList(DateUtil.getInterval("P1Y"));
        final BasicHistoricRatios ratios = hrp.getBasicHistoricRatios(SymbolQuote.create(quote),
                SymbolQuote.create(benchmarkQuote), intervals).get(0);
        System.out.println("high: " + ratios.getHigh());
        System.out.println("low: " + ratios.getLow());
    }

    @SuppressWarnings("UnusedDeclaration")
    private static void testExtendedRatios(HistoricRatiosProviderImpl hrp) {
        final QuoteDp2 quote = new QuoteDp2();
        quote.setSymbol(KeysystemEnum.MMWKN, "710000");
        final QuoteDp2 benchmarkQuote = new QuoteDp2();
        benchmarkQuote.setSymbol(KeysystemEnum.MMWKN, "I846900");

        final List<ExtendedHistoricRatios> ehr =
                hrp.getExtendedHistoricRatios(SymbolQuote.create(quote), SymbolQuote.create(benchmarkQuote),
                        Arrays.asList(new Interval(new DateTime().minusMonths(12), new DateTime())));
        System.out.println("getLongestContinuousNegativeReturnPeriod:" + ehr.get(0).getLongestContinuousNegativeReturnPeriod());
        System.out.println("getMaximumLossPercent:" + ehr.get(0).getMaximumLossPercent());
        System.out.println("getSharpeRatio:" + ehr.get(0).getSharpeRatio());
    }

    private static void testSignals(HistoricRatiosProviderImpl hrp, QuoteDp2 quote) {
//        Map<String, String> m = new HashMap<>();
//        m.put("MACD spec", "Map(#[](Close[_].MACD[12; 26; \"exponentiell\"; 0; 0; \"exponentiell\"; 0; 0; 9; \"exponentiell\"; 0; 0].HSMultilineKreuzung[0; 0].HandelsSystemSignalPhasen[\"$date\"; _]);object).Concatenate");
//        hrp.setTradingSystemsByName(m);

        final Interval month24 = new Interval(new DateTime().minusMonths(24), new DateTime());
        final TimeTaker tt = new TimeTaker();
        final List<TradingPhase> tradingSignals = hrp.getTradingPhases(SymbolQuote.create(quote), new ArrayList<>(hrp.signalSystems.values()), month24, Boolean.FALSE);
        System.out.println("took " + tt);
        for (TradingPhase tradingSignal : tradingSignals) {
            System.out.println(tradingSignal);
        }
    }
}
