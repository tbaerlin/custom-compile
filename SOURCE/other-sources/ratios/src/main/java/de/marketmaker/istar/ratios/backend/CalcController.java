/*
 * CalcController.java
 *
 * Created on 17.09.2005 15:51:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.ratios.BackendUpdateReceiver;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class CalcController extends ApplicationObjectSupport
        implements InitializingBean, Lifecycle, ComputedRatiosHandler {

    static Set WITHBENCHMARK_NO_FUNDS = new HashSet<>(Arrays.asList(InstrumentTypeEnum.STK, InstrumentTypeEnum.BND, InstrumentTypeEnum.GNS));

    static Set WITHBENCHMARK_FUNDS = Collections.singleton(InstrumentTypeEnum.FND);

    private Thread calcThread;

    private volatile boolean stopped = false;

    private final LongSet quoteids = new LongOpenHashSet();

    private final Set<Long> tracedQuoteids = new ConcurrentSkipListSet<>();

    private boolean underlyingTrigger = false;

    private int atOnce = 100;

    private InstrumentRepository instrumentRepository;

    private PriceProvider priceProvider;

    private int numCalculators = 1;

    /**
     * a wrapper for each calculator
     */
    private CombinedCalculator[] calculatorWrappers;

    private BackendUpdateReceiver frontend;

    private AtomicLong numComputed = new AtomicLong();

    private StaticDataProvider staticDataProvider;

    private Set<InstrumentTypeEnum> types = EnumSet.noneOf(InstrumentTypeEnum.class);

    private int mmManagerPort = 9998;

    private String mmManagerCall = "StartDataUpdate";

    private boolean mmManagerEnabled = true;

    private int mmManagerBefore = 5;

    private int mmManagerAfter = 2;


    private class LogStatusTask extends TimerTask {
        private long lastNumComputed = 0;

        @Override
        public void run() {
            final long tmp = numComputed.get();
            logStatus(tmp - this.lastNumComputed);
            this.lastNumComputed = tmp;
        }
    }

    @ManagedAttribute
    public int getMmManagerPort() {
        return mmManagerPort;
    }

    @ManagedAttribute
    public void setMmManagerPort(int mmManagerPort) {
        this.mmManagerPort = mmManagerPort;
    }

    @ManagedAttribute
    public String getMmManagerCall() {
        return mmManagerCall;
    }

    @ManagedAttribute
    public void setMmManagerCall(String mmManagerCall) {
        this.mmManagerCall = mmManagerCall;
    }

    @ManagedAttribute
    public boolean isMmManagerEnabled() {
        return mmManagerEnabled;
    }

    @ManagedAttribute
    public void setMmManagerEnabled(boolean mmManagerEnabled) {
        this.mmManagerEnabled = mmManagerEnabled;
    }

    @ManagedAttribute
    public int getMmManagerAfter() {
        return mmManagerAfter;
    }

    @ManagedAttribute
    public void setMmManagerAfter(int mmManagerAfter) {
        this.mmManagerAfter = mmManagerAfter;
    }

    @ManagedAttribute
    public int getMmManagerBefore() {
        return mmManagerBefore;
    }

    @ManagedAttribute
    public void setMmManagerBefore(int mmManagerBefore) {
        this.mmManagerBefore = mmManagerBefore;
    }

    public void setStaticDataProvider(StaticDataProvider staticDataProvider) {
        this.staticDataProvider = staticDataProvider;
    }

    public void setFrontend(BackendUpdateReceiver frontend) {
        this.frontend = frontend;
    }

    public void setInstrumentRepository(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public void setTypes(String[] typesStr) {
        this.types.clear();
        for (final String s : typesStr) {
            this.types.add(InstrumentTypeEnum.valueOf(s));
        }
        this.logger.info("<setTypes> types = " + this.types);
    }

    public void setPriceProvider(PriceProvider priceProvider) {
        this.priceProvider = priceProvider;
    }

    public void setNumCalculators(int numCalculators) {
        this.numCalculators = numCalculators;
    }

    public void setAtOnce(int atOnce) {
        this.atOnce = atOnce;
    }

    public void setUnderlyingTrigger(boolean underlyingTrigger) {
        this.underlyingTrigger = underlyingTrigger;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.calculatorWrappers = new CombinedCalculator[this.numCalculators];
        for (int i = 0; i++ < this.numCalculators; ) {
            this.calculatorWrappers[i - 1] = new CombinedCalculator(this,
                    (EmbeddedCalculator) getApplicationContext().getBean("embedded" + i),
                    (MmCalculator) getApplicationContext().getBean("mm" + i)
            );
        }
    }

    @Override
    public boolean isRunning() {
        return this.calcThread != null && this.calcThread.isAlive();
    }

    public void start() {
        this.calcThread = new Thread(this::doRun, getClass().getSimpleName() + "-calc");
        this.calcThread.start();
        new Timer(getClass().getSimpleName() + "-status", true).scheduleAtFixedRate(new LogStatusTask(),
                new DateTime().plusMinutes(1).withSecondOfMinute(0).getMillis() - System.currentTimeMillis(),
                DateTimeConstants.MILLIS_PER_MINUTE);
    }

    private void logStatus(long numUpdatesSinceLast) {
        final StringBuilder sb = new StringBuilder(64).append("<logStatus> ");
        for (CombinedCalculator cw : this.calculatorWrappers) {
            sb.append(cw.isIdle() ? '-' : 'R'); // R for Running
        }
        sb.append(" #computed=").append(numUpdatesSinceLast);
        this.logger.info(sb.toString());
    }

    public void stop() {
        this.logger.info("<stop> about to stop calc controller");
        this.stopped = true;

        try {
            this.calcThread.interrupt();
            this.calcThread.join(15 * 1000);

            if (this.calcThread.isAlive()) {
                this.logger.warn("<stop> failed to join calcThread, exiting anyway");
            }

            for (CombinedCalculator calculatorWrapper : calculatorWrappers) {
                calculatorWrapper.shutdown();
            }
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!?", e);
        }
    }

    private void doRun() {
        while (!this.stopped) {
            calc();
        }
        this.logger.info("<run> calcThread about to finish");
    }

    private void calc() {
        try {
            doCalc();
        } catch (InterruptedException ie) {
            this.logger.info("<calc> interrupted");
        } catch (Throwable tFatal) {
            this.logger.error("<calc> fatal error?!?", tFatal);
        }
    }

    private void doCalc() throws InterruptedException {
        final Map<Long, List<Long>> quoteidsByInstrumentid = new HashMap<>();

        synchronized (this.quoteids) {
            while (this.quoteids.isEmpty()) {
                callDPRead();
                this.logger.info("<doCalc> waiting for quoteids...");
                this.quoteids.wait();
            }

            buildCalcMap(quoteidsByInstrumentid, this.quoteids);

            this.quoteids.clear();
        }

        if (this.underlyingTrigger) {
            for (final Long instrumentid : quoteidsByInstrumentid.keySet()) {
                final Collection<Long> underlyingQuoteids =
                        this.instrumentRepository.getDerivativeQuoteids(instrumentid);
                if (underlyingQuoteids != null) {
                    buildCalcMap(quoteidsByInstrumentid, underlyingQuoteids);
                }
            }
        }

        calc(quoteidsByInstrumentid);
    }

    private void buildCalcMap(Map<Long, List<Long>> quoteidsByInstrumentid,
            Collection<Long> quoteids) {
        for (final Long quoteid : quoteids) {
            final Long instrumentid = this.instrumentRepository.getInstrumentid(quoteid);
            if (instrumentid == null) {
                continue;
            }

            List<Long> qids = quoteidsByInstrumentid.get(instrumentid);
            if (qids == null) {
                qids = new ArrayList<>(4);
                quoteidsByInstrumentid.put(instrumentid, qids);
            }

            if (!qids.contains(quoteid)) {
                qids.add(quoteid);
            }
        }
    }

    public void addQuoteidsToCalc(LongCollection ids) {
        synchronized (this.quoteids) {
            this.quoteids.addAll(ids);
            this.quoteids.notify();
        }
    }

    private void calc(Map<Long, List<Long>> ids) throws InterruptedException {
        int frontsize = ids.size();

        for (final Long instrumentid : ids.keySet()) {
            frontsize--;

            if (this.stopped) {
                return;
            }

            final Instrument instrument = this.instrumentRepository.getInstrument(instrumentid);
            if (instrument == null || !this.types.contains(instrument.getInstrumentType())) {
                continue;
            }

            final CombinedCalculator cw = getCalculator(instrument);
            cw.add(instrument);

            if (cw.size() >= this.atOnce) {
                cw.submitCalcTask(ids, frontsize);
            }
        }

        for (CombinedCalculator cw : this.calculatorWrappers) {
            if (cw.size() > 0) {
                cw.submitCalcTask(ids, frontsize);
            }
        }
    }

    private CombinedCalculator getCalculator(Instrument instrument) {
        // partition instruments by id, so that instruments end up in the same calculator
        // every time; important as it improves cache hit rate in pm.
        return this.calculatorWrappers[((int) (instrument.getId() % calculatorWrappers.length))];
    }

    private int normTime(long millis, int size) {
        if (size == 0) {
            return 0;
        }
        return (int) ((double) millis / size * 100);
    }

    void calc(Calculator c, List<Instrument> instruments,
            Map<Long, List<Long>> ids, int frontsize) {
        try {
            final List<CalcData> toCalc = new ArrayList<>();

            final TimeTaker ttdataget = new TimeTaker();
            final TimeTaker tttotal = new TimeTaker();

            for (final Instrument instrument : instruments) {
                if (this.stopped) {
                    return;
                }

                // almost all quotes of an instrument share the same reference quote
                final Quote referenceQuote = getReferenceQuote(instrument, false);

                final List<Long> quoteids = ids.get(instrument.getId());
                for (final Long quoteid : quoteids) {
                    final Quote quote = instrument.getQuote(quoteid);
                    if (quote == null) {
                        continue;
                    }

                    synchronized (this.quoteids) {
                        this.quoteids.remove(quoteid);
                    }

                    final Quote rq = isDutchFund(instrument, quote)
                            ? getReferenceQuote(instrument, true)
                            : referenceQuote;
                    final SnapRecord rsr = getSnapRecord(rq);

                    final SnapRecord sr = getSnapRecord(quote);
                    toCalc.add(new CalcData(quote, sr, rq, rsr,
                            this.tracedQuoteids.contains(quote.getId())));
                }
            }
            ttdataget.stop();

            final int[] num = new int[1];

            final TimeTaker ttcalc = new TimeTaker();
            c.calc(toCalc, computedRatios -> {
                CalcController.this.handle(computedRatios);
                num[0]++;
            });
            ttcalc.stop();

            if (this.logger.isDebugEnabled()) {
                final int total;
                synchronized (this.quoteids) {
                    total = this.quoteids.size();
                }
                this.logger.debug("took: " + tttotal.getElapsedMs()
                        + " (" + normTime(tttotal.getElapsedMs(), num[0]) + ") for "
                        + num[0] + " (" + toCalc.size() + "), get/calc: "
                        + normTime(ttdataget.getTotalElapsedMs(), num[0])
                        + "/" + normTime(ttcalc.getTotalElapsedMs(), num[0])
                        + ", queue: " + total + "/" + frontsize);
            }
        } catch (Throwable tFatal) {
            this.logger.error("<calc> exception", tFatal);
        }
    }

    private boolean isDutchFund(Instrument instrument, Quote quote) {
        return "FONDNL".equals(quote.getSymbolVwdfeedMarket())
                || (instrument.getInstrumentType() == InstrumentTypeEnum.FND && "NL".equals(quote.getSymbolVwdfeedMarket()));
    }

    @Override
    public void handle(ComputedRatios computedRatios) {
        this.frontend.update(computedRatios.getData());
        this.numComputed.incrementAndGet();
    }

    private SnapRecord getSnapRecord(Quote quote) {
        if (quote == null) {
            return null;
        }
        return this.priceProvider.getSnapRecord(quote.getId());
    }

    private Quote getReferenceQuote(Instrument instrument, boolean useVwdbenlQid) {
        if (WITHBENCHMARK_NO_FUNDS.contains(instrument.getInstrumentType())) {
            return this.instrumentRepository.getBenchmarkQuote(instrument);
        }
        if (WITHBENCHMARK_FUNDS.contains(instrument.getInstrumentType())) {
            final StaticData staticData = this.staticDataProvider.get(instrument.getId());
            if (staticData != null) {
                final Long qid = useVwdbenlQid
                        ? staticData.getVwdbenlBenchmarkQid()
                        : staticData.getVwdBenchmarkQid();

                if (qid != null) {
                    return this.instrumentRepository.getQuote(qid);
                }
            }
        }

        if (instrument instanceof Derivative) {
            return getUnderlyingQuote((Derivative) instrument);
        }

        return null;
    }

    private Quote getUnderlyingQuote(Derivative derivative) {
        final Instrument underlying = this.instrumentRepository.getInstrument(derivative.getUnderlyingId());
        if (underlying == null) {
            return null;
        }

        final Long qid = this.instrumentRepository.getNonStandardUnderlyingQid(underlying.getId());
        if (qid != null) {
            final Quote quote = underlying.getQuote(qid);
            if (quote != null) {
                return quote;
            }
        }


        for (final Quote uq : underlying.getQuotes()) {
            final String vkey = uq.getSymbol(KeysystemEnum.VWDFEED);
            if (vkey != null && vkey.endsWith(".ETR")) {
                return uq;
            }
        }

        for (final Quote uq : underlying.getQuotes()) {
            final String vkey = uq.getSymbol(KeysystemEnum.VWDFEED);
            final String mmsymbol = uq.getSymbol(KeysystemEnum.MMWKN);

            if (mmsymbol != null
                    && vkey != null
                    && mmsymbol.equals(underlying.getSymbol(KeysystemEnum.DEFAULTMMSYMBOL))) {
                return uq;
            }
        }

        for (final Quote uq : underlying.getQuotes()) {
            final String vkey = uq.getSymbol(KeysystemEnum.VWDFEED);

            if (vkey != null) {
                return uq;
            }
        }

        return null;
    }

    @ManagedAttribute
    public String[] getUnusedSinceMillis() {
        final String[] result = new String[this.calculatorWrappers.length];
        for (int i = 0; i < this.calculatorWrappers.length; i++) {
            result[i] = ((i + 1) + ": " + this.calculatorWrappers[i].unusedSinceMillis());
        }
        return result;
    }

    @ManagedAttribute
    public String getTracedQuoteids() {
        return this.tracedQuoteids.toString();
    }

    @ManagedOperation
    public void addQuoteidToTrace(long qid) {
        this.tracedQuoteids.add(qid);
        this.logger.info("TRACE <addQuoteidToTrace> added " + qid + " - now tracing " + getTracedQuoteids());
    }

    @ManagedOperation
    public void clearQuoteidsToTrace() {
        this.tracedQuoteids.clear();
        this.logger.info("TRACE <clearQuoteidsToTrace> cleared");
    }

    @ManagedOperation
    public void callDPRead() {
        if (!this.mmManagerEnabled) {
            return;
        }
        LocalTime two = new LocalTime(this.mmManagerAfter, 0, 0);
        LocalTime five = new LocalTime(this.mmManagerBefore, 0, 0);
        LocalTime now = new LocalTime();
        if (now.isAfter(two) && now.isBefore(five)) {
            try {
                Socket clientSocket = new Socket("localhost", this.mmManagerPort);
                DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                this.logger.info("<callDPRead> sending to MMManager >>>" + this.mmManagerCall + "<<<");
                outToServer.writeBytes(this.mmManagerCall + '\n');
//                String answer = inFromServer.readLine();
//                this.logger.info("<callDPRead> MMManager ansered >>>" + answer + "<<<");
                clientSocket.close();
            } catch (IOException e) {
                this.logger.error("<callDPRead> caught", e);

            }
        }
        else {
            this.logger.info("<callDPRead> no DPRead before "+this.mmManagerAfter+":00 or after "+this.mmManagerBefore+":00");

        }
    }
}
