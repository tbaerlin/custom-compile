/*
 * MdpExporterDp2.java
 *
 * Created on 30.06.2005 11:14:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


/**
 * Reads domain context and instruments from an external datasource (i.e., the MDP) and
 * forwards the result to a given {@link ExportHandler}.
 * For reasons of efficiency, the MDP offers two stored procedures, one to retrieve instrument only
 * data and one to retrieve quote only data. The only data shared in both results is the instrument
 * id. Since both result sets are ordered by ascending iid, this class uses two parallel threads
 * to read from both and combine their data to create complete (i.e., containing all quote data)
 * instruments.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author mcoenen
 */
public class MdpExporterDp2 extends JdbcDaoSupport implements Exporter {

    private static final boolean WITH_QUOTELESS_UNDERLYINGS = Boolean.getBoolean("withQuotelessUnderlyings");

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String lockStmt;
    private String unlockStmt;
    private boolean keepDuplicates;
    private int instrumentQueueSize = 1000;
    private int quoteQueueSize = 3000;
    private int fetchSize = 1000;

    public void setInstrumentQueueSize(int instrumentQueueSize) {
        this.instrumentQueueSize = instrumentQueueSize;
    }

    public void setQuoteQueueSize(int quoteQueueSize) {
        this.quoteQueueSize = quoteQueueSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public void setLockStmt(String lockStmt) {
        this.lockStmt = lockStmt;
    }

    public void setUnlockStmt(String unlockStmt) {
        this.unlockStmt = unlockStmt;
    }

    public void setKeepDuplicates(boolean keepDuplicates) {
        this.keepDuplicates = keepDuplicates;
    }

    /**
     * Encapsulate the variables that are created/changed by an export
     */
    private class Context {
        private final TimeTaker tt = new TimeTaker();

        private final ExportHandler handler;

        private DomainContextImpl domainContext = new DomainContextImpl();

        private Context(ExportHandler handler) {
            this.handler = handler;
        }

        public void toLog() {
            logger.info("<export> finished in " + this.tt);
        }
    }

    private static final DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        DF.applyLocalizedPattern("0.#######");
    }

    // MM_IID _must_ be the max of all iids as it is inserted
    // as last member in an already sorted list
    static final long MM_IID = Long.MAX_VALUE - 2;

    private long timeoutMillis = MILLISECONDS.convert(3, HOURS);

    private Context context;

    private ContentFlagsPostProcessor contentFlagsPostProcessor;

    private BlockingQueue<QuotesForInstrument> quotesForInstruments;

    private BlockingQueue<InstrumentAssembleInfo> instrumentInfos;

    private final Set<Long> emptyInstrumentIds = new HashSet<>();

    private final Set<Long> notProcessedInstrumentIds = new HashSet<>();

    private final Set<Long> orphanInstrumentIds = new HashSet<>();

    final AtomicReference<Throwable> exportFailure = new AtomicReference<>();

    private final ScheduledExecutorService es = Executors.newScheduledThreadPool(3, r -> {
        final Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public void setConnectionTimeoutMinutes(int connectionTimeoutMinutes) {
        this.timeoutMillis = MILLISECONDS.convert(connectionTimeoutMinutes, MINUTES);
    }

    public ContentFlagsPostProcessor getContentFlagsPostProcessor() {
        return contentFlagsPostProcessor;
    }

    public void setContentFlagsPostProcessor(ContentFlagsPostProcessor contentFlagsPostProcessor) {
        this.contentFlagsPostProcessor = contentFlagsPostProcessor;
    }

    @Override
    public void export(ExportHandler handler, ExportParameters parameters) throws Exception {
        this.logger.info("<export> securities for " + parameters);

        this.context = new Context(handler);
        if (parameters.getDomainContext() != null) {
            this.context.domainContext = parameters.getDomainContext();
        }
        else {
            exportContext();
        }

        handler.handle(this.context.domainContext);
        exportInstruments(parameters);

        this.context.toLog();
        this.context.domainContext = null;
        this.context = null;
    }

    public BlockingQueue<QuotesForInstrument> getQuotesForInstruments() {
        return this.quotesForInstruments;
    }

    public BlockingQueue<InstrumentAssembleInfo> getInstrumentInfos() {
        return this.instrumentInfos;
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    private void exportInstruments(ExportParameters parameters) throws Exception {
        this.instrumentInfos = new LinkedBlockingQueue<>(this.instrumentQueueSize);
        this.quotesForInstruments = new LinkedBlockingQueue<>(this.quoteQueueSize);
        lock();

        try {
            this.es.submit(new QuotesReader(this, parameters, this.keepDuplicates)::run);
            this.es.submit(new InstrumentReader(this, parameters, this.keepDuplicates)::run);
            mergeInstrumentsAndQuotes();
        } catch (Exception e) {
            this.exportFailure.compareAndSet(null, e);
            this.es.shutdownNow(); // interrupts worker threads
            throw e;
        } finally {
            // unlock even if reading tasks exited unexpected
            unlock();
        }

        this.logger.info("<exportInstruments> items created: " + QuotesForInstrument.INSTANCE_COUNT.get());
        inspectExportProblem(this.notProcessedInstrumentIds, "instruments not-processed because of exception");
        inspectExportProblem(this.emptyInstrumentIds, "empty instruments");
        inspectExportProblem(this.orphanInstrumentIds, "orphan instruments");
    }

    private void lock() throws SQLException {
        if (StringUtils.hasText(this.lockStmt)) {
            final Statement statement = getDataSource().getConnection().createStatement();
            statement.execute(this.lockStmt);
        }
        this.logger.info("<lock> did set the lock via \"" + this.lockStmt + "\"");
    }

    private void unlock() throws SQLException {
        if (StringUtils.hasText(this.unlockStmt)) {
            final Statement statement = getDataSource().getConnection().createStatement();
            statement.execute(this.unlockStmt);
        }
        this.logger.info("<unlock> did release the lock via \"" + this.unlockStmt + "\"");
    }

    private void exportContext() throws Exception {
        new DomainContextReader(getDataSource(), getDomainContext(), this.fetchSize).read();
        this.logger.info("<exportContext> read " + getDomainContext().getSectors().size() + " sectors");
        this.logger.info("<exportContext> read " + getDomainContext().getCurrencies().size() + " currencies");
        this.logger.info("<exportContext> read " + getDomainContext().getCountries().size() + " countries");
        this.logger.info("<exportContext> read " + getDomainContext().getMarkets().size() + " markets");
    }

    /**
     * Instrument Id from readQuotes is ensured to be able to be found in readInstruments, but there
     * could be instruments without quotes.
     */
    private void mergeInstrumentsAndQuotes() throws Exception {
        this.logger.info("<mergeInstrumentsAndQuotes> start");
        DomainContextImpl dc = getDomainContext();

        // timeout is just used for initial take from both queues
        // as it is quite long (i.e., hours), it makes no sense to use it for each subsequent take
        final Thread t = Thread.currentThread();
        ScheduledFuture<?> sf = this.es.schedule(t::interrupt, this.timeoutMillis, MILLISECONDS);

        QuotesForInstrument quotes = takeQuotes();
        this.logger.info("<mergeInstrumentsAndQuotes> got first quotes");

        InstrumentAssembleInfo info = takeInstrument();
        this.logger.info("<mergeInstrumentsAndQuotes> got first instrument");

        sf.cancel(false);

        while (true) {
            if (quotes == QuotesForInstrument.EOF) {
                drainInstruments(info);
                return;
            }
            if (info == InstrumentAssembleInfo.EOF) {
                drainQuotes(quotes);
                return;
            }
            if (quotes.iid < info.getIid()) {
                this.orphanInstrumentIds.add(quotes.iid);
                quotes = takeQuotes();
                continue;
            }
            if (quotes.iid > info.getIid()) {
                if (WITH_QUOTELESS_UNDERLYINGS && info.isUnderlying()) {
                    processInstrument(info.getInstrument(null, dc));
                }
                else {
                    this.emptyInstrumentIds.add(info.getIid());
                }
                info = takeInstrument();
                continue;
            }
            processInstrument(info.getInstrument(quotes, dc));
            quotes = takeQuotes();
            info = takeInstrument();
        }
    }

    private void drainInstruments(InstrumentAssembleInfo first) throws Exception {
        InstrumentAssembleInfo info = first;
        while (info != InstrumentAssembleInfo.EOF) {
            this.emptyInstrumentIds.add(info.getIid());
            info = takeInstrument();
        }
    }

    private void drainQuotes(QuotesForInstrument first) throws Exception {
        QuotesForInstrument quotes = first;
        while (quotes != QuotesForInstrument.EOF) {
            this.orphanInstrumentIds.add(quotes.iid);
            quotes = takeQuotes();
        }
    }


    private QuotesForInstrument takeQuotes() throws InterruptedException {
        return this.quotesForInstruments.take();
    }

    private InstrumentAssembleInfo takeInstrument() throws InterruptedException {
        return this.instrumentInfos.take();
    }

    private void inspectExportProblem(Collection<Long> col, String problem) {
        if (!CollectionUtils.isEmpty(col)) {
            this.logger.warn("<inspectExportProblem> " + problem + ": " + col.size());
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<inspectExportProblem> " + problem + ": " + col);
            }
        }
    }

    private void processInstrument(InstrumentDp2 instrument) {
        if (instrument == null) {
            return;
        }

        try {
            this.context.handler.handle(instrument);
        } catch (Exception e) {
            this.notProcessedInstrumentIds.add(instrument.getId());
            this.logger.error("<processInstrument> failed for " + instrument.getId(), e);
        }
    }


    DomainContextImpl getDomainContext() {
        return this.context.domainContext;
    }
}