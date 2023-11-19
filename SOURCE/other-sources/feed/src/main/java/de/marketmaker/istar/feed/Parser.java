/*
 * Parser.java
 *
 * Created on 25.10.2004 13:19:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.netflix.servo.annotations.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.core.Ordered;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.statistics.HasStatistics;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import static com.netflix.servo.annotations.DataSourceType.COUNTER;
import io.micrometer.core.instrument.Timer;

/**
 * Base class for all feed parsers. Can be used in two different modes:
 * <dl>
 *  <dt>Active mode</dt>
 *  <dd>If a {@link RecordSource} is assigned and this object's {@link #start()} method is
 *  called, it will create a new thread that runs in an endless loop in which it fetches the
 *  next record from the record source, parses it and forwards it to interested handlers.</dd>
 *  <dt>Passive mode</dt>
 *  <dd>If no RecordSource is assigned, invoking {@link #start()} will have no effect.
 *  The parser expects to be controlled by an external thread that invokes
 *  {@link #accept(FeedRecord)} for all records to be parsed.</dd>
 * </dl>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public abstract class Parser implements InitializingBean, Lifecycle, Runnable,
        Ordered, HasStatistics, BeanNameAware, Consumer<FeedRecord> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private RecordSource recordSource = NullRecordSource.INSTANCE;

    private FeedBuilder[] feedBuilders = new FeedBuilder[0];

    /**
     * Relevant builders for all possible message types, so we do not need to compute the
     * relevant builders for each parsed record. The 2nd dimension contains a boolean array
     * of the same length as the feedBuilders array (or null if no builder is interested in the
     * respective message type).
     */
    private boolean[][] relevantBuilders = new boolean[Byte.MAX_VALUE][];

    private Thread parseThread;

    /**
     * whether the parseThread should stop
     */
    private final AtomicBoolean stop = new AtomicBoolean();

    private String parseProblem;

    protected VendorkeyFilter vendorkeyFilter = VendorkeyFilterFactory.ACCEPT_ALL;

    private VendorkeyFilterFactory filterFactory;

    private boolean trimStrings = false;

    protected FeedDataRegistry registry;

    protected final ParsedRecord parsedRecord;

    private String name = "";

    protected final AtomicLong numRecordsParsed = new AtomicLong();

    @Monitor(type = COUNTER)
    protected final AtomicInteger numParseErrors = new AtomicInteger();

    @Monitor(type = COUNTER)
    protected final AtomicInteger numParseWarnings = new AtomicInteger();

    private DateTimeProvider dateTimeProvider = DateTimeProviderImpl.INSTANCE;

    private ParserErrorHandler errorHandler = null;

    private boolean logDuplicateFields = true;

    private boolean neverRegisterKey = false;

    private int order = Ordered.LOWEST_PRECEDENCE;

    protected boolean addToaAndDoa = true;

    /**
     * {@link #ackUpdate(FeedData)} modifies the state of a FeedData object according to the
     * current message. Whenever a FeedData object is shared for realtime and delayed data, the
     * delayed parser should <em>not</em> update the state, so this flag should be set to false.
     */
    private boolean ackUpdates = true;

    private boolean registerCreatedKey = true;

    protected Parser() {
        this.parsedRecord = new ParsedRecord();
    }

    public void setFilterFactory(VendorkeyFilterFactory filterFactory) {
        this.filterFactory = filterFactory;
    }

    protected void setRegisterCreatedKey(boolean registerCreatedKey) {
        this.registerCreatedKey = registerCreatedKey;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setAckUpdates(boolean ackUpdates) {
        this.ackUpdates = ackUpdates;
    }

    /**
     * Setting this to true will prohibit the parser from registering any keys in the FeedDataRegistry.
     * It will merely look up keys and ignore records for keys not present in the repository.
     * Appropriate in a push server in which clients register keys and the server just parses
     * data for those keys.
     *
     * @param neverRegisterKey true iff no registration is wanted
     */
    public void setNeverRegisterKey(boolean neverRegisterKey) {
        this.neverRegisterKey = neverRegisterKey;
    }

    public void setLogDuplicateFields(boolean logDuplicateFields) {
        this.logDuplicateFields = logDuplicateFields;
    }

    public void setDayAndTimeProvider(DateTimeProvider dateTimeProvider) {
        this.dateTimeProvider = dateTimeProvider;
    }

    public void setErrorHandler(ParserErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Timer parseTimer;

    public void setParseTimer(Timer timer) {
        this.parseTimer = timer;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.registry == null) {
            this.registry = new VolatileFeedDataRegistry();
        }
    }

    public void resetStatistics() {
        resetNumRecordsParsed();
        resetNumParseErrors();
        resetNumParseWarnings();
    }

    @ManagedOperation
    public void resetNumRecordsParsed() {
        this.numRecordsParsed.set(0);
    }

    @ManagedOperation
    public void resetNumParseErrors() {
        this.numParseErrors.set(0);
    }

    @ManagedOperation
    public void resetNumParseWarnings() {
        this.numParseWarnings.set(0);
    }

    @ManagedAttribute
    public final long getNumParseErrors() {
        return this.numParseErrors.get();
    }

    @ManagedAttribute
    public final long getNumParseWarnings() {
        return this.numParseWarnings.get();
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public void setRepository(FeedDataRegistry registry) {
        this.registry = registry;
    }

    public void setVendorkeyFilter(VendorkeyFilter vendorkeyFilter) {
        this.vendorkeyFilter = vendorkeyFilter;
        this.logger.info("<setVendorkeyFilter> " + vendorkeyFilter);
    }

    public void setRecordSource(RecordSource recordSource) {
        this.recordSource = recordSource;
    }

    public void setFeedBuilders(FeedBuilder... feedBuilders) {
        this.feedBuilders = Arrays.copyOf(feedBuilders, feedBuilders.length);

        for (int i = 0; i < feedBuilders.length; i++) {
            final byte[] applicableMessageTypes = feedBuilders[i].getApplicableMessageTypes();
            for (final byte b : applicableMessageTypes) {
                if (b < 0) {
                    continue;
                }
                if (this.relevantBuilders[b] == null) {
                    this.relevantBuilders[b] = new boolean[this.feedBuilders.length];
                }
                this.relevantBuilders[b][i] = true;
            }
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return this.recordSource == NullRecordSource.INSTANCE || this.parseThread != null;
    }

    public synchronized void start() {
        if (this.parseThread != null) {
            throw new IllegalStateException();
        }
        if (this.recordSource == NullRecordSource.INSTANCE) {
            this.logger.info("<start> ignoring start with NullRecordSource");
            return;
        }

        if (this.recordSource instanceof Initializable) {
            try {
                ((Initializable) this.recordSource).initialize();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        this.stop.set(false);
        this.parseThread = new Thread(this, this.name);
        this.parseThread.start();
    }

    public synchronized void stop() {
        this.logger.info("<stop> about to stop parser");
        if (this.parseThread == null || Thread.currentThread() == this.parseThread) {
            return;
        }
        this.stop.set(true);

        try {
            // interrupting is necessary, because the thread might wait for input in the
            // recordsource's getFeedRecord method; w/o interrupt, the stop condition would only
            // be recognized when further data arrives -- this is not acceptable for
            // feeds with a lot of idle time (e.g., news).
            this.parseThread.interrupt();
            this.parseThread.join(15 * 1000);

            if (this.parseThread.isAlive()) {
                this.logger.error("<stop> failed to join parseThread, exiting anyway");
            }
            else {
                this.logger.info("<stop> Parser stopped, parsed " + this.numRecordsParsed.get() + " records");
            }
        }
        catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!?", e);
        }
        finally {
            this.parseThread = null;
        }
    }

    public void run() {
        while (!this.stop.get()) {
            try {
                parseRecord();
            }
            catch (InterruptedException ie) {
                this.logger.info("<run> interrupted");
            }
            catch (OutOfMemoryError e) {
                break;
            }
            catch (Throwable tFatal) {
                this.logger.error("<run> fatal error?!?", tFatal);
                break;
            }
        }
        this.logger.info("<run> parser thread about to finish");
    }

    public void parseRecord() throws InterruptedException {
        accept(fetchNextRecord());
    }

    @Override
    public void accept(FeedRecord feedRecord) {
        if (feedRecord == null) {
            return;
        }
        if (feedRecord == FeedRecord.SYNC) {
            onSync();
            return;
        }

        final long start = System.nanoTime();
        this.parseProblem = null;
        Throwable t = null;

        try {
            parse(feedRecord);
        }
        catch (OutOfMemoryError e) {
            this.logger.error("<run> out of memory", e);
            throw e;
        }
        catch (Throwable t1) {
            t = t1;
            this.parseProblem = t.getMessage();
        }

        if (this.parseProblem != null || t != null) {
            handleParseProblem(feedRecord, t);
        }
        else if (this.logDuplicateFields && this.parsedRecord.isAnyDuplicateField()) {
            final BitSet duplicateFields = this.parsedRecord.getDuplicateFields();
            this.logger.warn("<run> duplicate field(s) " + duplicateFields.toString()
                    + " for feedrecord: " + feedRecord);
        }

        if (this.parseTimer != null) {
            this.parseTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    protected abstract void parse(FeedRecord feedRecord);

    protected void handleParseProblem(FeedRecord feedRecord, Throwable t) {
        if (t instanceof InvalidFieldsException) {
            this.numParseWarnings.incrementAndGet();
        }
        else {
            this.numParseErrors.incrementAndGet();
        }

        if (this.errorHandler != null) {
            this.errorHandler.handle(feedRecord, this.parsedRecord, t);
        }

        if (this.parseProblem == null) {
            this.parseProblem = "n/a";
        }

        if (t != null) {
            this.logger.error("<logParseProblem> " + this.parseProblem
                    + ", after " + this.parsedRecord.getLastParsedField()
                    + ", feedRecord=" + feedRecord.toFullString()
                    + ", feedRecord debug=" + feedRecord.toDebugString(), t);
        }
        else {
            this.logger.warn("<logParseProblem> " + this.parseProblem
                    + ", after " + this.parsedRecord.getLastParsedField()
                    + ", feedRecord=" + feedRecord.toFullString()
                    + ", feedRecord debug=" + feedRecord.toDebugString());
        }
    }

    public void setTrimStrings(boolean trimStrings) {
        this.trimStrings = trimStrings;
    }

    protected final boolean isAnyBuilderInterested(byte messageType) {
        return messageType >= 0 && this.relevantBuilders[messageType] != null;
    }

    /**
     * Forwards the parsed record to all relevant builders
     */
    protected final void build() {
        doBuild(null);
    }

    /**
     * Forwards data and the parsed record to all relevant builders while being synchronized on data
     *
     * @param data may be null if parsing is not for FeedData objects
     */
    protected void build(final FeedData data) {
        if (this.addToaAndDoa) {
            setToaAndDoa();
        }

        if (data != null) {
            synchronized (data) {
                doBuild(data);
                // some builders need the current state of data, so set state _after_ invoking them:
                ackUpdate(data);
            }
        }
        else {
            doBuild(null);
        }
    }

    private void ackUpdate(FeedData data) {
        if (!this.ackUpdates) {
            return;
        }
        if (this.parsedRecord.isDelete()) {
            this.registry.unregister(data.getVendorkey());
        }
        else {
            data.setState(FeedData.STATE_UPDATED);
        }
    }

    private void    doBuild(FeedData data) {
        ackRecordParsed();
        for (int i = 0; i < this.feedBuilders.length; i++) {
            if (this.relevantBuilders[this.parsedRecord.getMessageType()][i]) {
                this.feedBuilders[i].process(data, this.parsedRecord);
            }
        }
    }

    protected final void setParseProblem(final String parseProblem) {
        this.parseProblem = parseProblem;
    }

    protected FeedData getFeedData(final VendorkeyVwd vkey) {
        // check for existing key first, avoids having to evaluate the filter again and again
        final FeedData existing = this.registry.get(vkey);
        if (existing != null || this.neverRegisterKey) {
            return existing;
        }
        if (!this.vendorkeyFilter.test(vkey)) {
            return null;
        }
        final FeedData result = this.registry.create(vkey);
        if (result != null && this.registerCreatedKey && !this.parsedRecord.isDelete()) {
            register(result);
        }
        return result;
    }

    protected void register(FeedData result) {
        if (result instanceof OrderedFeedData) {
            ((OrderedFeedData) result).setCreatedTimestamp(getMessageTimestamp());
        }
        this.registry.register(result);
    }

    private FeedRecord fetchNextRecord() throws InterruptedException {
        return this.recordSource.getFeedRecord();
    }

    protected void ackRecordParsed() {
        this.numRecordsParsed.incrementAndGet();
    }

    protected final boolean isTrimStrings() {
        return trimStrings;
    }

    protected void onSync() {
        onSync(this.registry);
        for (FeedBuilder feedBuilder : feedBuilders) {
            onSync(feedBuilder);
        }
        if (this.filterFactory != null && !this.filterFactory.isSingleton()) {
            this.vendorkeyFilter = this.filterFactory.getObject();
            this.logger.info("<onSync> filter = " + this.vendorkeyFilter);
        }
    }

    private void onSync(Object o) {
        if (o instanceof FeedSync) {
            ((FeedSync) o).onSync();
        }
    }

    protected void setToaAndDoa() {
        if (this.parsedRecord.getAdfTimeOfArrival() == Integer.MIN_VALUE) {
            final DateTimeProvider.Timestamp timestamp = this.dateTimeProvider.current();
            this.parsedRecord.setField(VwdFieldDescription.ADF_TIMEOFARR.id(),
                    timestamp.secondOfDay);
            this.parsedRecord.setField(VwdFieldDescription.ADF_DATEOFARR.id(), timestamp.yyyyMmDd);
        }
    }

    protected int getMessageTimestamp() {
        return this.dateTimeProvider.current().feedTimestamp;
    }

    public void setAddToaAndDoa(boolean addToaAndDoa) {
        this.addToaAndDoa = addToaAndDoa;
    }
}
