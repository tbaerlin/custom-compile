/*
 * MscBasiscRatiosMethod.java
 *
 * Created on 27.11.2010 07:45:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.ReadableInstant;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.BasicHistoricRatios;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullBasicHistoricRatios;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.merger.provider.HistoricRatiosProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import de.marketmaker.istar.merger.web.easytrade.PeriodValidator;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

import static org.joda.time.DateTimeConstants.MILLIS_PER_DAY;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscBasicRatiosMethod {

    private final EasytradeInstrumentProvider instrumentProvider;

    private final ProfiledIndexCompositionProvider indexCompositionProvider;

    private final FundDataProvider fundDataProvider;

    private final IntradayProvider intradayProvider;

    private final HistoricRatiosProvider historicRatiosProvider;

    private final LocalDate referenceDate;

    private SymbolCommand cmd;

    private String[] periods;

    private Period aggregation;

    private Quote quote;

    private Quote quoteBenchmark;

    private PriceRecord quotePriceRecord;

    private PriceRecord benchmarkPriceRecord;

    private final HistoricDataProfiler historicDataProfiler = new HistoricDataProfiler();

    public MscBasicRatiosMethod(SymbolCommand cmd, LocalDate referenceDate,
            String[] periods,
            Period aggregation,
            EasytradeInstrumentProvider instrumentProvider,
            ProfiledIndexCompositionProvider indexCompositionProvider,
            FundDataProvider fundDataProvider,
            IntradayProvider intradayProvider,
            HistoricRatiosProvider historicRatiosProvider) {
        this.cmd = cmd;
        this.referenceDate = referenceDate;
        this.periods = periods;
        this.aggregation = aggregation;
        this.instrumentProvider = instrumentProvider;
        this.indexCompositionProvider = indexCompositionProvider;
        this.fundDataProvider = fundDataProvider;
        this.intradayProvider = intradayProvider;
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public MscBasicRatiosMethod(Quote quote, PriceRecord quotePriceRecord,
            String[] periods,
            Period aggregation,
            EasytradeInstrumentProvider instrumentProvider,
            ProfiledIndexCompositionProvider indexCompositionProvider,
            FundDataProvider fundDataProvider,
            IntradayProvider intradayProvider,
            HistoricRatiosProvider historicRatiosProvider) {
        this(quote, null, quotePriceRecord,
                periods,
                aggregation,
                instrumentProvider,
                indexCompositionProvider,
                fundDataProvider,
                intradayProvider,
                historicRatiosProvider);
    }

    MscBasicRatiosMethod(Quote quote, LocalDate referenceDate, PriceRecord quotePriceRecord,
            String[] periods,
            Period aggregation,
            EasytradeInstrumentProvider instrumentProvider,
            ProfiledIndexCompositionProvider indexCompositionProvider,
            FundDataProvider fundDataProvider,
            IntradayProvider intradayProvider,
            HistoricRatiosProvider historicRatiosProvider) {
        this.quote = quote;
        this.referenceDate = referenceDate;
        this.quotePriceRecord = quotePriceRecord;
        this.periods = periods;
        this.aggregation = aggregation;
        this.instrumentProvider = instrumentProvider;
        this.indexCompositionProvider = indexCompositionProvider;
        this.fundDataProvider = fundDataProvider;
        this.intradayProvider = intradayProvider;
        this.historicRatiosProvider = historicRatiosProvider;
    }

    public Quote getQuote() {
        return quote;
    }

    public Quote getQuoteBenchmark() {
        return quoteBenchmark;
    }

    public List<BasicHistoricRatios> invoke() {
        if (this.quote == null && this.cmd == null) {
            throw new BadRequestException("no symbol defined");
        }

        if (this.quote == null) {
            this.quote = this.instrumentProvider.getQuote(this.cmd);
        }

        this.quoteBenchmark = new BenchmarkQuoteMethod(quote, this.indexCompositionProvider,
                this.fundDataProvider, this.instrumentProvider, null).invoke();

        setPriceRecords();

        if (this.quotePriceRecord.getPriceQuality() == PriceQuality.NONE) {
            return Collections.nCopies(this.periods.length, NullBasicHistoricRatios.INSTANCE);
        }

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final Entitlement entitlement = this.historicDataProfiler.getEntitlement(profile, quote);
        final DateTime earliestAllowedDate = entitlement.getAllowedStart();
        final List<Interval> intervals = getIntervals(earliestAllowedDate);

        final List<Interval> validIntervals =
                intervals.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        List<BasicHistoricRatios> historicRatios = this.historicRatiosProvider.getBasicHistoricRatios(
                SymbolQuote.create(this.quote), SymbolQuote.create(this.quoteBenchmark),
                validIntervals, getAggregation());

        if (intervals.size() != historicRatios.size()) {
            List<BasicHistoricRatios> result = new ArrayList<>();
            for (int i = 0, j = 0, size = intervals.size(); i < size; i++) {
                BasicHistoricRatios elementToAdd =
                        intervals.get(i) != null
                            ? historicRatios.get(j++)
                            : NullBasicHistoricRatios.INSTANCE;
                result.add(elementToAdd);
            }
            historicRatios = result;
        }

        return withCurrentPrices(historicRatios);
    }


    private void setPriceRecords() {
        if (this.quotePriceRecord == null) {
            final List<PriceRecord> priceRecords
                    = this.intradayProvider.getPriceRecords(Arrays.asList(this.quote, this.quoteBenchmark));
            this.quotePriceRecord = priceRecords.get(0);
            this.benchmarkPriceRecord = priceRecords.get(1);
        }
        else {
            this.benchmarkPriceRecord
                    = this.intradayProvider.getPriceRecords(Collections.singletonList(this.quoteBenchmark)).get(0);
        }
    }

    List<BasicHistoricRatios> withCurrentPrices(List<BasicHistoricRatios> historicRatios) {
        final Price current = this.quotePriceRecord.getPrice();
        if (current == null || current == NullPrice.INSTANCE) {
            return historicRatios;
        }
        final List<BasicHistoricRatios> result = new ArrayList<>(historicRatios.size());
        result.addAll(historicRatios);
        IntStream.range(0, this.periods.length)
                .filter(i -> !isCurrentPriceAfterPeriodsEndDate(current, this.periods[i]))
                .forEach(i -> result.set(i, result.get(i).copy(this.quotePriceRecord, this.benchmarkPriceRecord)));
        return result;
    }

    private boolean isCurrentPriceAfterPeriodsEndDate(Price current, String period) {
        final Matcher matcher = PeriodValidator.PERIOD_W_ENDDATE.matcher(period);
        if (matcher.matches()) {
            final DateTime endDate = DateUtil.parseDate(matcher.group(1));
            return current.getDate().isAfter(endDate.withTimeAtStartOfDay().plusDays(1).minusSeconds(1));
        }
        else if (this.referenceDate != null) {
            return current.getDate().isAfter(this.referenceDate.toDateTimeAtStartOfDay().plusDays(1).minusSeconds(1));
        }
        return false;
    }

    private List<Interval> getIntervals(ReadableInstant earliestAllowedDate) {
        final DateTime now = new DateTime();
        final DateTime referenceDate = (this.quotePriceRecord.getDate() != null)
                ? this.quotePriceRecord.getDate()
                : now;

        final List<Interval> result = new ArrayList<>(this.periods.length);
        for (final String p : periods) {
            final Interval intervalWithEnddate = createIntervalFromPeriodWithEndDate(p);
            if (intervalWithEnddate == null) {
                final DateTime end = this.referenceDate != null ? this.referenceDate.toDateTimeAtStartOfDay()
                        : p.endsWith("D") ? now : referenceDate;
                Interval interval = new Interval(end.minus(DateUtil.getPeriod(p)), end);
                interval = interval.getStart().isBefore(earliestAllowedDate) ? null : interval;
                result.add(interval);
            }
            else {
                result.add(intervalWithEnddate);
            }
        }
        return result;
    }

    /**
     * @param period Syntactical equal to ISO8601 period with start date. But the date specifies the end of the interval.
     */
    private Interval createIntervalFromPeriodWithEndDate(String period) {
        final Matcher m = PeriodValidator.PERIOD_W_ENDDATE.matcher(period);
        if (m.matches() && m.groupCount() == 2) {
            final DateTime end = DateUtil.parseDate(m.group(1));
            final String p = m.group(2);
            return new Interval(end.minus(DateUtil.getPeriod(p)), end);
        } else {
            return null;
        }
    }

    public static List<String> getOutputPeriods(String[] periods) {
        final List<String> result = new ArrayList<>();
        for (final String p : periods) {
            final String inPeriod = p.toUpperCase();
            result.add(ensureFormat(inPeriod));
        }
        return result;
    }

    private static String ensureFormat(String s) {
        final Matcher m = PeriodValidator.PERIOD_W_ENDDATE.matcher(s);
        if (m.matches() && m.groupCount() == 2) {
            return s;
        }
        else {
            return s.startsWith("P") ? s : "P" + s;
        }
    }

    private Double getAggregation() {
        if (this.aggregation == null) {
            return null;
        }

        final String aggStr = this.aggregation.toString();
        if ("P1M".equals(aggStr)) {
            return 30d;
        }

        if ("P1W".equals(aggStr)) {
            return 7d;
        }

        final long periodDays
                = this.aggregation.toDurationTo(new DateTime()).getMillis() / MILLIS_PER_DAY;
        // Math.max for errors AND days w/ DST changes from winter to summer
        // + 0.1d for avoiding pm feature of 7 and 30 day period (which are weeks and months)
        return Math.max(1, periodDays) + 0.1d;
    }
}
