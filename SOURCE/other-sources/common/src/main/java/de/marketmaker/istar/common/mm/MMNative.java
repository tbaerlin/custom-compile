/*
 * MMNative.java
 *
 * Created on 17.03.2005 07:56:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mm;

import static de.marketmaker.istar.common.monitor.MeterSupport.record;
import static de.marketmaker.istar.common.util.LambdaUtil.wrap;

import de.marketmaker.istar.common.lifecycle.Disposable;
import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.DllLoader;
import de.marketmaker.istar.common.util.TimeTaker;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Uses native COM to connect to a pm Instance and evaluate mmtalk formulas.
 * <p>
 * <b>Thread-safety</b> is achieved by thread confinement. All native methods are invoked
 * by running code inside a single-threaded ExecutorService.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MMNative implements MMService, Initializable, Disposable {
    public static final LocalDate FIRST_DATE = new LocalDate(1899, 12, 31);

    private final Logger logger = LoggerFactory.getLogger(MMNative.class);

    private final Logger callLogger = LoggerFactory.getLogger("mmnative.logger");

    private static final int UNDEFINED_HANDLE = -1;

    /**
     * name of the dll that connects to MarketMaker
     */
    private final static String DLL_NAME = "jni2mm";

    private String username;

    private String password;

    private String license;

    private int instanceNo = 0;

    private boolean initialized = false;

    private MMIntradayPusher intradayPusher;

    private boolean ensureTimeseriesFlags = true;

    private final ExecutorService executor
            = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(100), r -> {
        return new Thread(r, "mmnative");
    });

    private int timeoutMillis = (int) TimeUnit.SECONDS.toMillis(10);

    private volatile boolean logCalls = Boolean.getBoolean("mm.logCalls");

    private AtomicInteger numRejected = new AtomicInteger();

    private AtomicInteger numFailed = new AtomicInteger();

    private AtomicInteger numTimeout = new AtomicInteger();

    private AtomicInteger numEvaluated = new AtomicInteger();

    private MeterRegistry meterRegistry;

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setEnsureTimeseriesFlags(boolean ensureTimeseriesFlags) {
        this.ensureTimeseriesFlags = ensureTimeseriesFlags;
    }

    /**
     * Base class for timeseries and table requests.
     */
    private abstract class CallableRequest<V extends MMServiceRequest> implements
            Callable<Object[]> {
        protected final V delegate;

        protected int handles[];

        protected CallableRequest(V delegate) {
            this.delegate = delegate;
        }

        public CallableRequest(V delegate, int[] handles) {
            this.delegate = delegate;
            this.handles = handles;
        }

        protected int[] getHandles() throws Exception {
            return (this.handles != null)
                    ? this.handles
                    : MMNative.this.getHandles(getKeyArray(), getKeytype());
        }

        protected int size() {
            return (this.handles != null) ? this.handles.length : getKeys().size();
        }

        protected String handleStr() {
            if (this.handles != null) {
                return Arrays.toString(this.handles);
            }
            return String.valueOf(getKeytype()) + getKeys();
        }

        private MMKeyType getKeytype() {
            return this.delegate.getKeytype();
        }

        private List<String> getKeys() {
            return this.delegate.getKeys();
        }

        protected List<String> getFormulas() {
            return this.delegate.getFormulas();
        }

        protected String[] getFormulaArray() {
            final List<String> formulas = this.delegate.getFormulas();
            return formulas.toArray(new String[formulas.size()]);
        }

        protected String[] getKeyArray() {
            final List<String> keys = this.delegate.getKeys();
            return keys.toArray(new String[keys.size()]);
        }
    }

    private final class CallableMMTimeseriesRequest extends CallableRequest<MMTimeseriesRequest> {
        CallableMMTimeseriesRequest(MMTimeseriesRequest delegate) {
            super(delegate);
        }

        public String toString() {
            return "MMTimeseriesRequest[" + delegate.getClientInfo() + ", " + handleStr()
                    + ", " + getFormulas() + ", " + this.delegate.getFrom() + ", " + this.delegate.getTo() + "]";
        }

        public Object[] call() throws Exception {
            boolean switchOff = false;

            final int[] handles;
            final int[] definedHandles;
            final Object[] mmResult;
            try {
                final List<MMPriceUpdate> priceUpdates = this.delegate.getPriceUpdates();
                if (intradayPusher != null && priceUpdates != null && !priceUpdates.isEmpty()) {
                    switchOff = true;

                    for (final MMPriceUpdate priceUpdate : priceUpdates) {
                        intradayPusher.pushData(priceUpdate);
                    }
                }

                final double comFrom = asComDate(this.delegate.getFrom());
                final double comTo = asComDate(this.delegate.getTo());

                handles = getHandles();
                definedHandles = filterDefined(handles);
                mmResult = (definedHandles.length > 0)
                        ? getTimeseries(instanceNo, definedHandles, getFormulaArray(), comFrom, comTo)
                        : new Object[0];
            } finally {
                if (switchOff) {
                    intradayPusher.clear();
                    intradayPusher.offline();
                }
            }

            if (handles == definedHandles) {
                return mmResult;
            }
            // mix in null results for non-existing keys
            final Object[] nullResult = new Object[getFormulas().size()];

            final Object[] result = new Object[handles.length];
            int j = 0;
            for (int i = 0; i < result.length; i++) {
                result[i] = (handles[i] != UNDEFINED_HANDLE) ? mmResult[j++] : nullResult;
            }
            return result;
        }
    }

    private final class CallableMMTalkTableRequest extends CallableRequest<MMTalkTableRequest> {
        CallableMMTalkTableRequest(MMTalkTableRequest delegate) {
            super(delegate);
        }

        CallableMMTalkTableRequest(MMTalkTableRequest delegate, int[] handles) {
            super(delegate, handles);
        }

        public String toString() {
            return "MMTalkTableRequest[" + delegate.getClientInfo() + ", " + handleStr() + ", "
                    + getFormulas() + ", pre=" + getPreFormula() + ", context=" + getContextHandle() + "]";
        }

        private String getContextHandle() {
            return this.delegate.getContextHandle();
        }

        private String getPreFormula() {
            return this.delegate.getPreFormula();
        }

        public Object[] call() throws Exception {
            final int[] handles = getHandles();
            final int[] definedHandles = filterDefined(handles);
            if (definedHandles.length == 0) {
                return new Object[size() * getFormulas().size()];
            }
            final Object[] mmResult = getMMTalkTable(instanceNo, definedHandles,
                    getContextHandle(), getFormulaArray(), getPreFormula());
            if (handles == definedHandles) {
                return mmResult;
            }
            // mix in null results for non-existing keys
            final Object[] result = new Object[size() * getFormulas().size()];
            for (int f = 0; f < getFormulas().size(); f++) {
                int j = 0;
                for (int k = 0; k < size(); k++) {
                    if (handles[k] != UNDEFINED_HANDLE) {
                        result[f * size() + k] = mmResult[f * definedHandles.length + j++];
                    }
                    else {
                        result[f * size() + k] = null;
                    }
                }
            }
            return result;
        }
    }

    public void setInstanceNo(int instanceNo) {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }
        if (instanceNo < 0 || instanceNo > 15) {
            throw new IllegalArgumentException("not in [0..15]: " + instanceNo);
        }
        this.instanceNo = instanceNo;
    }

    /**
     * must be called before other native methods can be used
     * returns the MMError code returned by MM98 on Login, so that
     * a value of 0 indicates success. Throws a ComFailException
     * if the Login method could not be called at all.
     */
    private native int natInit(int instanceNo, String username, String password, String license)
            throws Exception;

    /**
     * should be called when the program terminates
     */
    private native void natShutdown(int instanceNo);

    /**
     * when a COM call fails, all subsequent calls will also fail until
     * MM98 is successfully restarted again using this function. If restarting
     * does not succeed, a ComFailException is thrown.
     * calling method must be sync'd on mmMutex
     */
    private native void natRestartMm(int instanceNo) throws Exception;

    /**
     * retrieves data for a set of handles according to a set of formulas;
     * calling method must be sync'd on mmMutex
     */
    private native Object[] natGetMMTalkTable(int instanceNo, int[] handles,
            String contextHandle,
            String[] formulas,
            String preFormula)
            throws Exception;

    private Object[] getMMTalkTable(int instanceNo, int[] handles, String contextHandle,
        String[] formulas, String preFormula) throws Exception {
        return record(this.meterRegistry, "nat.mmtalk.table",
            wrap(() -> natGetMMTalkTable(instanceNo, handles, contextHandle, formulas, preFormula)),
            Tags.of("instance", "" + instanceNo).and("formula.size", "" + formulas.length));
    }

    /**
     * returns a Timeseries
     */
    private native Object[] natGetTimeseries(int instanceNo, int[] handles,
            String[] formulas,
            double startDay,
            double endDay)
            throws Exception;

    private Object[] getTimeseries(int instanceNo, int[] handles, String[] formulas,
        double startDay, double endDay) throws Exception {
        return record(this.meterRegistry, "nat.timeseries",
            wrap(() -> natGetTimeseries(instanceNo, handles, formulas, startDay, endDay)),
            Tags.of("instance", "" + instanceNo).and("formula.size", "" + formulas.length));
    }

    /**
     * Returns handles for the object specified by handleStr of the given type
     * @param handleStr name of object
     * @param mmKeyType type of object
     * @return handle for the object or -1 if no such object exists
     */
    private native int[] natGetHandles(int instanceNo, String handleStr, int mmKeyType)
            throws Exception;

    private int[] getHandles(int instanceNo, String handleStr, int mmKeyType) {
        return record(this.meterRegistry, "nat.handles",
            wrap(() -> natGetHandles(instanceNo, handleStr, mmKeyType)),
            Tags.of("instance", "" + instanceNo));
    }


    public void setLicense(String license) {
        this.license = license;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setIntradayPusher(MMIntradayPusher intradayPusher) {
        this.intradayPusher = intradayPusher;
    }

    public void initialize() throws Exception {
        try {
            DllLoader.load(DLL_NAME);
            final int success = natInit(this.instanceNo, this.username, this.password, this.license);

            this.logger.info("<initialize> connected to MM with instance " + this.instanceNo + " with login status " + success);

            if (success > 0) {
                throw new Exception("<initialize> login failed, status=" + success);
            }
            this.initialized = true;

            ensureTimeseriesFlags(true);

            warmup();
        } catch (Throwable t) {
            throw new Exception("<initialize> failed", t);
        }
    }

    @ManagedOperation
    public void ensureTimeseriesFlags() throws Exception {
        ensureTimeseriesFlags(false);
    }

    private void ensureTimeseriesFlags(boolean throwException) throws Exception {
        if (this.intradayPusher == null || !this.ensureTimeseriesFlags) {
            this.logger.warn("<ensureTimeseriesFlags> no intradayPusher set OR ensureTimeseriesFlags == TRUE => no timeseries flags check possible");
            return;
        }

        final BigDecimal hv = BigDecimal.valueOf(100_000);
        // push very high value as kassa to pm to check for High-"Kursergänzung" (pm => Konfiguration / Einstellungen / Zeitreihen, unterste Checkbox)
        this.intradayPusher.pushData(new MMPriceUpdate("I846900", true, false, new LocalDate(), null, null, null, null, hv, null, null, null));

        final MMTalkTableRequest request = new MMTalkTableRequest(MMKeyType.SECURITY_WKN);
        request.withKey("I846900")
                .appendFormula("high[_; true; false].maximum[letzter_kurs.datum.AddMonths[-1];heute]")
                .appendFormula("kassa[_; true; false].maximum[letzter_kurs.datum.AddMonths[-1];heute]");
        final Object[] objects = new CallableMMTalkTableRequest(request).call();

        this.intradayPusher.clear();

        final Double high = (Double) objects[0];
        final Double kassa = (Double) objects[1];

        final double cmp = hv.doubleValue();

        if (high == null || kassa == null || (kassa == cmp && high.equals(kassa))) {
            // Kursergänzung is active
            this.logger.error("<ensureTimeseriesFlags> Kursergänzung (pm => Konfiguration / Einstellungen / Zeitreihen, unterste Checkbox) is active " +
                    "==> deactivate OR set ensureTimeseriesFlags as config paraemter of this bean to FALSE (objects = " + Arrays.deepToString(objects) + ")");
            if (throwException) {
                throw new IllegalStateException("Kursergänzung is active ==> see previous log message");
            }
            return;
        }

        this.logger.info("<ensureTimeseriesFlags> Kursergänzung high<-kassa is not active ==> OK");
    }

    /**
     * Initial queries after login may take some time. In order to avoid delays for
     * requestors, run some warmup queries
     * @throws Exception if warmup fails.
     */
    private void warmup() throws Exception {
        this.logger.info("<warmup> ...");
        final TimeTaker tt = new TimeTaker();
        final List<Long> millis = new ArrayList<>();
        for (String s : new String[]{"I710000", "W0557EP", "519000", "I846900",
                "I766400", "I648300", "IBAY001", "I716460"}) {
            final TimeTaker subTt = new TimeTaker();
            final MMTalkTableRequest r1 = new MMTalkTableRequest(MMKeyType.SECURITY_WKN);
            r1.withKey(s)
                    .appendFormula("$kurse:=Close[_; true; true]; $first:=$kurse.at[Heute-7; 10]; if($first < 0,01; na; $first)")
                    .appendFormula("Name");
            // do not use executor, we expect timeouts
            new CallableMMTalkTableRequest(r1).call();

            final MMTimeseriesRequest r2 = new MMTimeseriesRequest(MMKeyType.SECURITY_WKN,
                    new LocalDate().minusYears(1), new LocalDate());
            r2.withKey(s);
            new CallableMMTimeseriesRequest(r2).call();

            final MMTalkTableRequest r3
                    = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withPreFormula("\"" + s + "\".FindWp[\"WKN\"]")
                    .withFormula("Platz").withFormula("WKN");
            new CallableMMTalkTableRequest(r3).call();
            millis.add(subTt.getElapsedMs());
        }
        Collections.sort(millis);
        this.logger.info("<warmup> took " + tt
                + ", min=" + millis.get(0)
                + ", max=" + millis.get(millis.size() - 1)
                + ", mean=" + millis.get(millis.size() / 2));
    }

    public void dispose() throws Exception {
        this.executor.shutdown();
        natShutdown(this.instanceNo);
        this.logger.info("<dispose> finished");
    }

    /**
     * Issue a warning, if more time than half of the timeout is used
     */
    private boolean warningMillis(long millis) {
        return millis > this.timeoutMillis * 0.5;
    }

    /**
     * Any code that uses a native method has to be submitted as a Callable to this method, so
     * that thread-safety can be ensured by thread confinement.
     * @throws MMTalkException if either the execution was rejected due to too many pending requests,
     * it took longer than {@link #timeoutMillis} to wait for the result, the waiting thread
     * was interrupted (interruption status will be restored), or the execution of the Callable
     * throwed an exception.
     */
    private <T> T invoke(final Callable<T> c) throws MMTalkException {
        if (this.logCalls) {
            this.callLogger.info(c.toString());
        }
        try {
            final Future<T> f = executor.submit(() -> {
                final TimeTaker tt = new TimeTaker();
                try {
                    return c.call();
                } finally {
                    if (warningMillis(tt.getElapsedMs())) {
                        logger.warn("<call> slow! took " + tt + " for " + c);
                    }
                }
            });
            final T result = f.get(this.timeoutMillis, TimeUnit.MILLISECONDS);
            this.numEvaluated.incrementAndGet();
            return result;
        } catch (RejectedExecutionException e) {
            this.numRejected.incrementAndGet();
            this.logger.error("<invoke> rejected " + c);
            throw new MMTalkException("rejected", e.getCause());
        } catch (ExecutionException e) {
            this.numFailed.incrementAndGet();
            this.logger.warn("<invoke> failed " + c, e);
            throw new MMTalkException("execution exception");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MMTalkException("interrupted", e);
        } catch (TimeoutException e) {
            this.numTimeout.incrementAndGet();
            this.logger.warn("<invoke> timeout " + c);
            throw new MMTalkException("timeout");
        }
    }

    public int[] getHandles(final String handleStr,
            final MMKeyType keytype) throws MMTalkException {

        return invoke(new Callable<int[]>() {
            public int[] call() throws Exception {
                return getHandles(instanceNo, handleStr, keytype.getValue());
            }

            public String toString() {
                return "getHandles[" + handleStr + ", " + keytype + "]";
            }
        });
    }

    /**
     * Returns an array of those handles in the handles array that are not undefined.
     */
    private static int[] filterDefined(int[] handles) {
        final int tmp[] = new int[handles.length];
        int j = 0;
        for (int handle : handles) {
            if (handle != UNDEFINED_HANDLE) {
                tmp[j++] = handle;
            }
        }
        if (j == handles.length) {
            return handles;
        }
        if (j == 0) {
            return new int[0];
        }
        final int[] result = new int[j];
        System.arraycopy(tmp, 0, result, 0, j);
        return result;
    }

    private int[] getHandles(String[] keys, MMKeyType keytype) throws Exception {
        final int[] handles = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            handles[i] = getHandle(keys[i], keytype);
        }
        return handles;
    }

    private int getHandle(String key, MMKeyType keytype) throws Exception {
        final int[] handles = getHandles(instanceNo, key, keytype.getValue());
        if (handles != null && handles.length == 1 && handles[0] != -1) {
            return handles[0];
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getHandle> no result for '" + key + "', type " + keytype);
        }
        return UNDEFINED_HANDLE;
    }

    public Object[] getMMTalkTable(final int[] handles, final String contextHandle,
            final String[] formulas, final String preFormula)
            throws MMTalkException {

        final MMTalkTableRequest request = new MMTalkTableRequest(null)
                .withContextHandle(contextHandle)
                .withFormulas(Arrays.asList(formulas))
                .withPreFormula(preFormula);

        return invoke(new CallableMMTalkTableRequest(request, handles));
    }

    public Object[] getMMTalkTable(String[] keys, MMKeyType keytype, String contextHandle,
            String[] formulas, String preFormula) throws MMTalkException {

        final MMTalkTableRequest request = new MMTalkTableRequest(keytype)
                .withKeys(Arrays.asList(keys))
                .withContextHandle(contextHandle)
                .withFormulas(Arrays.asList(formulas))
                .withPreFormula(preFormula);
        final MMServiceResponse response = getMMTalkTable(request);
        return response.getData();
    }

    public Object[] getTimeseries(final String[] keys, final MMKeyType keytype,
            final String[] formulas, final YearMonthDay from, final YearMonthDay to) throws MMTalkException {
        final MMTimeseriesRequest request = new MMTimeseriesRequest(keytype, convert(from), convert(to))
                .withKeys(Arrays.asList(keys))
                .withFormulas(Arrays.asList(formulas));

        final MMServiceResponse response = getTimeseries(request);
        return response.getData();
    }

    public MMServiceResponse getTimeseries(MMTimeseriesRequest request) throws MMTalkException {
        if (request.getFrom().isBefore(FIRST_DATE)) {
            this.logger.warn("<getTimeseries> illegal interval " + request.getFrom() + ".." + request.getTo());
            return createInvalidResponse(request);
        }
        final Object[] result = invoke(new CallableMMTimeseriesRequest(request));
        return new MMServiceResponse(result);
    }

    private MMServiceResponse createInvalidResponse(MMTimeseriesRequest request) {
        final Object[] data = new Object[request.getKeys().size()];
        Arrays.fill(data, new Object[request.getFormulas().size()]);
        final MMServiceResponse result = new MMServiceResponse(data);
        result.setInvalid();
        return result;
    }

    public MMServiceResponse getMMTalkTable(MMTalkTableRequest request) throws MMTalkException {
        final Object[] result = invoke(new CallableMMTalkTableRequest(request));
        return new MMServiceResponse(result);
    }

    private double asComDate(LocalDate ld) {
        return DateUtil.javaDateToComDate(ld.toDateTimeAtStartOfDay().toDate());
    }

    @ManagedAttribute
    public boolean isLogCalls() {
        return logCalls;
    }

    @ManagedAttribute
    public void setLogCalls(boolean logCalls) {
        this.logCalls = logCalls;
    }

    @ManagedAttribute
    public int getNumEvaluated() {
        return numEvaluated.get();
    }

    @ManagedAttribute
    public int getNumFailed() {
        return numFailed.get();
    }

    @ManagedAttribute
    public int getNumRejected() {
        return numRejected.get();
    }

    @ManagedAttribute
    public int getNumTimeout() {
        return numTimeout.get();
    }

    public static void main(String[] args) throws Exception {
        testInstance();
    }

    private static void testInstance() throws Exception {
        MMNative m = new MMNative();
        m.setUsername("merger");
        m.setPassword("");
        m.setLicense("MMSAG-ON\\QF7G]BAFIYJ<K");
        m.setInstanceNo(0);
        m.initialize();
        query(m);

        m.dispose();
    }

    private static void query(MMNative m) throws MMTalkException {
        final MMTalkTableRequest request
                = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                .withPreFormula("\"DE0008478033\".FindWp[\"ISIN\"]")
                .withFormula("Platz").withFormula("WKN");

        final MMServiceResponse response = m.getMMTalkTable(request);
        System.out.println(Arrays.deepToString(response.getData()));
    }

    private static LocalDate convert(YearMonthDay ymd) {
        return new LocalDate(ymd.getYear(), ymd.getMonthOfYear(), ymd.getDayOfMonth());
    }
}
