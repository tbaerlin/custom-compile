/*
 * CompComp.java
 *
 * Created on 30.01.13 08:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.util.concurrent.ExecutorServiceUtil;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InstrumentProvider;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;

/**
 * @author zzhao
 */
@ManagedResource
public class HTProviderComparator implements DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private InstrumentProvider instrumentProvider;

    private Path instrumentDir;

    private Path sampleFile;

    private HistoricTimeseriesProvider impl;

    private HistoricTimeseriesProvider eod;

    private final ExecutorService executorService;

    public HTProviderComparator() {
        this.executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "COMP");
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        ExecutorServiceUtil.shutdownAndAwaitTermination(this.executorService, 10);
    }

    public void setSampleFile(String sampleFile) {
        this.sampleFile = Paths.get(sampleFile);
    }

    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setInstrumentDir(String path) {
        this.instrumentDir = Paths.get(path);
    }

    public void setProviderImpl(HistoricTimeseriesProvider impl) {
        this.impl = impl;
    }

    public void setProviderEod(HistoricTimeseriesProvider eod) {
        this.eod = eod;
    }

    @ManagedOperation
    public String comp() {
        this.executorService.execute(new Runnable() {
            @Override
            public void run() {
                final TimeTaker tt = new TimeTaker();
                try {
                    RequestContextHolder.setRequestContext(
                            new RequestContext(ProfileFactory.valueOf(true),
                                    LbbwMarketStrategy.INSTANCE));
                    final Path csvPath = Paths.get("d:/temp/comp.csv");
                    try (
                            final InstrumentDirDao idd =
                                    new InstrumentDirDao(instrumentDir.toFile());
                            final BufferedWriter writer = Files.newBufferedWriter(csvPath,
                                    Charset.defaultCharset(),
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING);
                    ) {
                        final MyQuote myQuote = new MyQuote();
                        for (Instrument instrument : idd) {
                            final Instrument ins =
                                    instrumentProvider.identifyByIid(instrument.getId());
                            if (null == ins) {
                                continue;
                            }
                            withInstrument(writer, myQuote, ins);
                        }
                    }
                } catch (Exception e) {
                    logger.error("<run> failed", e);
                }
                logger.info("<compSample> took {}", tt);
            }
        });

        return "scheduled";
    }

    @ManagedOperation
    public String compSample() {
        this.executorService.execute(new Runnable() {
            @Override
            public void run() {
                final TimeTaker tt = new TimeTaker();
                try {
                    RequestContextHolder.setRequestContext(
                            new RequestContext(ProfileFactory.valueOf(true),
                                    LbbwMarketStrategy.INSTANCE));
                    final Path csvPath = Paths.get("d:/temp/compSample.csv");
                    try (
                            final BufferedWriter writer = Files.newBufferedWriter(csvPath,
                                    Charset.defaultCharset(),
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING);
                    ) {
                        final MyQuote myQuote = new MyQuote();
                        final List<String> list =
                                Files.readAllLines(sampleFile, Charset.defaultCharset());
                        for (String symbol : list) {
                            Instrument ins = null;
                            try {
                                ins = SymbolUtil.toInstrument(instrumentProvider, symbol);
                            } catch (Exception e) {
                                logger.error("<compSample> failed to identify instrument {}", symbol);
                            }
                            if (null == ins) {
                                continue;
                            }
                            withInstrument(writer, myQuote, ins);
                        }
                    }
                } catch (Exception e) {
                    logger.error("<run> failed", e);
                }
                logger.info("<compSample> took {}", tt);
            }
        });

        return "scheduled";
    }

    private void withInstrument(BufferedWriter writer, MyQuote myQuote, Instrument ins) {
        for (Quote quote : ins.getQuotes()) {
            if (null == quote.getSymbolVwdfeed()) {
                continue;
            }
            try {
                myQuote.reset(quote);
                testWith(myQuote, writer);
            } catch (Exception e) {
                logger.error("<compSample> failed for quote {}", quote, e);
            }
        }
    }

    private static final class MyQuote {
        private Quote quote;

        private QuoteDef quoteDef;

        private InstrumentTypeEnum insType;

        private String currency;

        private void reset(Quote quote) {
            this.quote = quote;
            this.quoteDef = QuoteDef.fromQuote(quote);
            this.insType = quote.getInstrument().getInstrumentType();
            this.currency = quote.getCurrency().getSymbolIso();
        }
    }

    private void testWith(MyQuote quote, BufferedWriter writer) throws IOException {
        for (Mode mode : getModes(quote.quoteDef)) testWith(quote, mode, writer);
    }

    private static EnumSet<Mode> getModes(QuoteDef quoteDef) {
        switch (quoteDef) {
            case OCHLKVC:
            case OCHLSVOI:
            case FUND:
            case MSCI_Indexe:
            case Luxembourg:
            case Qualifizierter_Einzelwert:
            case Crossrate_calculated:
            case Waehrung:
            case LME_Lagerbestaende:
            case OCHLKVC_und_Rendite_Rendexp:
            case OCHLKVC_und_Rendite_Kursexp:
            case OCHLKVC_dt_Praesenzboersen:
            case MSCI_Indexe_EUR:
            case XETRA_INAV_Indices:
            case Pfandbriefkurve:
            case Endlosfutures_ExFeed:
            case OCHLKVC_und_Rendite_Geld:
            case Geld_Brief_Bezahlt:
            case Bezahlt:
            case Volume_Information_Octopus:
                return EnumSet.allOf(Mode.class);
            default:
                return EnumSet.of(Mode.Base);
        }
    }

    private void testWith(MyQuote quote, Mode mode, BufferedWriter writer) throws IOException {
        final List<HistoricTimeseriesRequest> reqs = mode.getRequests(quote);
        if (!CollectionUtils.isEmpty(reqs)) {
            for (HistoricTimeseriesRequest req : reqs) {
                if (null != req) {
                    testWith(req, fromFormulas(req.getFormulas()), mode, writer);
                }
            }
        }
    }

    private List<PriceType> fromFormulas(String[] formulas) {
        final List<PriceType> ret = new ArrayList<>(formulas.length);
        for (String formula : formulas) {
            ret.add(PriceType.fromFormula(formula));
        }
        return ret;
    }

    private void testWith(HistoricTimeseriesRequest req, List<PriceType> pts, Mode mode,
            BufferedWriter writer) throws IOException {
        comp(req.getQuote(), this.impl.getTimeseries(req), this.eod.getTimeseries(req), pts, mode,
                writer);
    }

    private void comp(Quote quote, List<HistoricTimeseries> implRes,
            List<HistoricTimeseries> eodRes, List<PriceType> pts, Mode mode,
            BufferedWriter writer) throws IOException {
        if (implRes.size() != eodRes.size()) {
            emit(writer, quote, "result size diff", implRes.size(), eodRes.size(), pts);
        }
        else {
            for (int i = 0; i < implRes.size(); i++) {
                comp(quote, implRes.get(i), eodRes.get(i), pts.get(i), mode, writer);
            }
        }
    }

    private void comp(Quote quote, HistoricTimeseries htImpl, HistoricTimeseries htEod,
            PriceType priceType, Mode mode, BufferedWriter writer) throws IOException {
        if (null == htImpl && null == htEod) {
            // no diff
        }
        else if (null == htImpl) {
            emit(writer, quote, priceType, mode, "null IMPL");
        }
        else if (null == htEod) {
            emit(writer, quote, priceType, mode, "null EoD");
        }
        else {
            final LocalDate startDayImpl = htImpl.getStartDay();
            final LocalDate startDayEod = htEod.getStartDay();
            final int offset = DateUtil.daysBetween(startDayEod, startDayImpl);
            final double[] valuesImpl = htImpl.getValues();
            final double[] valuesEod = htEod.getValues();

            int idxImpl = 0;
            int idxEod = 0;
            if (offset < 0) {
                while (idxImpl < -offset) {
                    if (!Double.isNaN(valuesImpl[idxImpl])) {
                        emit(writer, quote, priceType, mode, yyyyMMdd(startDayImpl.plusDays(idxImpl)),
                                valuesImpl[idxImpl], "?");
                    }
                    idxImpl++;
                }
            }
            else if (offset > 0) {
                while (idxEod < offset) {
                    if (!Double.isNaN(valuesEod[idxEod])) {
                        emit(writer, quote, priceType, mode, yyyyMMdd(startDayEod.plusDays(idxEod)), "?",
                                valuesEod[idxEod]);
                    }
                    idxEod++;
                }
            }

            final int len = Math.min(valuesImpl.length, valuesEod.length);
            for (int i = 0; i < len; i++) {
                final double valImpl = valuesImpl[idxImpl];
                final double valEod = valuesEod[idxEod];
                if (Double.isNaN(valImpl) && Double.isNaN(valEod)) {
                    // same
                }
                else if (Double.isNaN(valImpl)) {
                    emit(writer, quote, priceType, mode, yyyyMMdd(startDayImpl.plusDays(idxImpl)),
                            "?", valEod);
                }
                else if (Double.isNaN(valEod)) {
                    emit(writer, quote, priceType, mode, yyyyMMdd(startDayImpl.plusDays(idxImpl)),
                            valImpl, "?");
                }
                else {
                    final BigDecimal bdImpl = new BigDecimal(valImpl).setScale(5, RoundingMode.HALF_EVEN);
                    final BigDecimal bdEod = new BigDecimal(valEod).setScale(5, RoundingMode.HALF_EVEN);
                    if (bdImpl.compareTo(bdEod) != 0) {
                        emit(writer, quote, priceType, mode,
                                yyyyMMdd(startDayImpl.plusDays(idxImpl)), bdImpl, bdEod);
                    }
                }
                idxImpl++;
                idxEod++;
            }

            while (idxImpl < valuesImpl.length) {
                if (!Double.isNaN(valuesImpl[idxImpl])) {
                    emit(writer, quote, priceType, mode, yyyyMMdd(startDayImpl.plusDays(idxImpl)),
                            valuesImpl[idxImpl], "?");
                }
                idxImpl++;
            }
            while (idxEod < valuesEod.length) {
                if (!Double.isNaN(valuesEod[idxEod])) {
                    emit(writer, quote, priceType, mode, yyyyMMdd(startDayEod.plusDays(idxEod)), "?",
                            valuesEod[idxEod]);
                }
                idxEod++;
            }
        }

    }

    private String yyyyMMdd(LocalDate startDayImpl) {
        return HistoryUtil.DTF_DAY.print(startDayImpl);
    }

    private static final DecimalFormat NF = new DecimalFormat("#.########");

    private static final StringBuilder sb = new StringBuilder(512);

    private void emit(BufferedWriter writer, Object... objs) throws IOException {
        sb.setLength(0);
        for (Object obj : objs) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            if (obj instanceof BigDecimal) {
                sb.append(NF.format(obj));
            }
            else {
                sb.append(String.valueOf(obj));
            }
        }
        writer.write(sb.toString());
        writer.newLine();
    }

    private static final LocalDate FROM = new LocalDate(1900, 1, 1);

    private static final LocalDate TO = new LocalDate(2013, 1, 10);

    private static enum Mode {
        Base,
        Split,
        Dividend,
        SplitAndDividend;

        private static final Logger log = LoggerFactory.getLogger(Mode.class);

        private List<HistoricTimeseriesRequest> getRequests(MyQuote quote) {
            final List<HistoricTimeseriesRequest> ret = new ArrayList<>(2);
            HistoricTimeseriesRequest req = request(quote);
            req.addClose(null);
            req.addOpen(null);
            req.addHigh(null);
            req.addLow(null);
            req.addVolume(null);
            req.addKontrakt(null);
            ret.add(req); // always ask OHLCV

            switch (quote.quoteDef) {
                case OCHLSVOI:
                case LME_Lagerbestaende:
                case OCHLKVC_und_Rendite_Geld:
                    // for YIELD
                    req = request(quote);
                    req.withSpecialBaseType(TickImpl.Type.YIELD);
                    req.addClose(null);
                    ret.add(req);
                    break;
                case FUND:
                    // for FUND
                    if (InstrumentUtil.isVwdFund(quote.quote)) {
                        req = request(quote);
                        req.addFundRepurchaingPrice(null);
                        req.addFundIssuePrice(null);
                        ret.add(req);
                    }
                    break;
            }

            return ret;
        }

        private HistoricTimeseriesRequest request(MyQuote quote) {
            switch (this) {
                case Base:
                    return getBaseRequest(quote.quote).withCurrency(quote.currency);
                case Split:
                    return getBaseRequest(quote.quote).withSplit(true);
                case Dividend:
                    return getBaseRequest(quote.quote).withDividend(true);
                case SplitAndDividend:
                    return getBaseRequest(quote.quote).withDividend(true).withSplit(true);
                default:
                    throw new UnsupportedOperationException("no support for: " + this);
            }
        }
    }

    private static HistoricTimeseriesRequest getBaseRequest(Quote quote) {
        return new HistoricTimeseriesRequest(quote, FROM, TO).withAggregationPeriod(Period.days(1))
                .withSplit(false).withDividend(false);
    }
}
