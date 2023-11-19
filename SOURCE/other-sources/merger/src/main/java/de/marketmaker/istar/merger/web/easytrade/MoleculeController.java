/*
 * EasytradeDispatcherServlet.java
 *
 * Created on 14.08.2006 15:05:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import static de.marketmaker.istar.common.log.LoggingUtil.UNIQUE_ID;
import static de.marketmaker.istar.merger.web.ZoneDispatcherServlet.ZONE_ATTRIBUTE;
import static de.marketmaker.istar.merger.web.easytrade.MoleculeRequest.REQUEST_ATTRIBUTE_NAME;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.merger.MergerException;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.HttpException;
import de.marketmaker.istar.merger.web.RequestWrapper;
import de.marketmaker.istar.merger.web.Zone;
import de.marketmaker.istar.merger.web.ZoneDispatcherServlet;
import de.marketmaker.istar.merger.web.easytrade.Error.Level;
import de.marketmaker.istar.merger.web.easytrade.access.notifier.AccessProcessor;
import de.marketmaker.istar.merger.web.easytrade.misc.MetricsUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.mutable.MutableLong;
import org.joda.time.DateTime;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.Controller;

/**
 * A controller that processes requests defined in a {@link MoleculeRequest} and tries to process
 * the contained atom requests in parallel whenever possible.
 * In a loop over all atoms, this controller tries to forward each AtomRequest to a responsible controller
 * and combine all atom ModelAndViews into a unified Model for a view named "molecule", which will
 * finally be returned. The actual atom dispatching is done by an ExecutorService,
 * that is, the atoms will be evaluated in parallel as much as possible.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MoleculeController extends AbstractController implements InitializingBean,
        DisposableBean {
    public static final String SEP_TEMPLATE_AND_VIEW = "/";
    public static final String SEP_TEMPLATE_HIERARCHY = "#";

    private static final String VIEW = "molecule";

    private static final String COUNTER_MOLECULE_IN = "molecule-in";
    private static final String COUNTER_ATOM_IN = "atom-in";
    private static final String COUNTER_ATOM_OUT = "atom-out";
    private static final String TIMER_MOLECULE = "molecule";
    private static final String TIMER_ATOM = "atom";

    protected AtomControllerMapping controllerMapping;

    /**
     * Cache for atom ModelAndViews
     */
    private Ehcache atomsCache;

    /**
     * dispatches atom requests
     */
    private ExecutorService executorService;

    /**
     * Number of threads used to dispatch atom requests
     */
    private int numThreads = 50;

    /**
     * Max number of seconds a molecule request will wait for all its atoms together to be serviced.
     */
    private int maxWaitSeconds = 20;

    /**
     * If a molecule requests contains any atom whose name matches this pattern, the atoms will
     * be executed sequentially.
     */
    private final static Pattern SEQUENTIAL_ATOMS = Pattern.compile("(PF|WL|LT|ALT|MSC_User)_.*");

    private static final Collection<String> DEFAULT_BLOCKS_WITH_UNIQUE_CACHE_KEY = Arrays.asList("DOC_URL", "DOC_Document", "DOC_REG_URL");

    private final Set<String> blocksWithUniqueCacheKey = new HashSet<>(DEFAULT_BLOCKS_WITH_UNIQUE_CACHE_KEY);

    private static final AtomicInteger UNIQUE_CACHE_KEY_SUFFIX = new AtomicInteger(0);

    private final ConcurrentMap<String, Future<ModelAndView>> pending
            = new ConcurrentHashMap<>();
    /**
     * Disable usage of ConcurrentMap pending, e.g., necessary for pm login requests when load testing.
     * If only one login/pwd is used, all simultaneously requested logins share the same response,
     * which leads to subsequent errors.
     */
    private boolean usePending = true;

    public static final String ERROR_VIEWNAME = "error";

    public static final String KEY_ATOMNAME = "atomname";

    public static final String KEY_CORRELATION_ID = "correlationId";

    public static final String KEY_ATOMS = "atoms";

    public static final String KEY_REQUEST = "request";

    public static final String DEPENDENCY_ATTRIBUTE_NAME
            = MoleculeController.class.getName() + ".dependency";

    /**
     * requests taking more time than this threshold will be logged as a warning
     */
    private int warnRequestThreshold = 1000;

    private MeterRegistry meterRegistry;

    private AccessProcessor accessProcessor;

    public void setAccessProcessor(AccessProcessor accessProcessor) {
        this.accessProcessor = accessProcessor;
    }

    public void setMeterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void setWarnRequestThreshold(int warnRequestThreshold) {
        this.warnRequestThreshold = warnRequestThreshold;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void setMaxWaitSeconds(int maxWaitSeconds) {
        this.maxWaitSeconds = maxWaitSeconds;
    }

    public void setAtomsCache(Ehcache atomsCache) {
        this.atomsCache = atomsCache;
        this.logger.info("<setAtomsCache> using atoms cache");
    }

    public void setControllerMapping(AtomControllerMapping controllerMapping) {
        this.controllerMapping = controllerMapping;
    }

    public void setUsePending(boolean usePending) {
        this.usePending = usePending;
    }

    public void setBlocksWithUniqueCacheKey(Collection<String> blocksWithUniqueCacheKey) {
        this.blocksWithUniqueCacheKey.clear();
        this.blocksWithUniqueCacheKey.addAll(blocksWithUniqueCacheKey);
        this.logger.info("<setBlocksWithUniqueCacheKey> " + blocksWithUniqueCacheKey);
    }

    public void afterPropertiesSet() throws Exception {
        this.executorService = new ThreadPoolExecutor(this.numThreads, this.numThreads,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger();

                    public Thread newThread(Runnable r) {
                        return new Thread(r, "AtomExecutor-" + count.incrementAndGet());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.executorService = ExecutorServiceMetrics.monitor(this.meterRegistry,
            this.executorService, "atoms-executor", "molecule-controller",
            MetricsUtil.COMMON_TAGS);

        this.logger.info("<afterPropertiesSet> created ExecutorService with " + this.numThreads
                + " worker threads");
    }


    public void destroy() {
        this.executorService.shutdown();
    }

    class Task implements Callable<ModelAndView> {
        private final MoleculeRequest.AtomRequest atom;

        private final HttpServletRequest request;

        private final HttpServletResponse response;

        private final StopWatch sw = new StopWatch();

        private final long start = System.currentTimeMillis();

        private final String cacheKey;

        private final int cacheTimeSec;

        private ModelAndView cachedResult;

        private Future<ModelAndView> futureResult;

        private boolean parallel;

        private final RequestContext context;

        private Task dependsOn;

        private final Thread caller;

        public Task(MoleculeRequest.AtomRequest atom, HttpServletRequest request,
                    HttpServletResponse response, RequestContext context) {
            this.atom = atom;
            this.request = request;
            this.response = response;
            this.context = context;
            if (MoleculeController.this.blocksWithUniqueCacheKey.contains(atom.getName())) {
                this.cacheKey = String.valueOf(UNIQUE_CACHE_KEY_SUFFIX.incrementAndGet());
            }
            else {
                this.cacheKey = atom.getCacheKey() + "$" + context.getProfile().getName();
            }
            this.cacheTimeSec = ServletRequestUtils.getIntParameter(request,
                    ZoneDispatcherServlet.CACHE_PARAMETER_NAME, -1);
            this.caller = Thread.currentThread();
        }

        public String toString() {
            return "Task[" + this.atom + " / " + getExecutionTimeMillis() + " (" + this.atom.getWaitTime() + ") ms]";
        }

        public long getExecutionTimeMillis() {
            return this.atom.getMs();
        }

        public long getPreCallTimeMillis() {
            return this.atom.getWaitTime();
        }

        int msSinceStart() {
            return (int) (System.currentTimeMillis() - start);
        }

        private void execute() {
            // Check if the result is eligible for being fetched from cache
            if (atomsCache != null && this.cacheTimeSec > 0) {
                final Element element = atomsCache.get(this.cacheKey);
                if (element != null) {
                    // Result was found in cache
                    this.cachedResult = (ModelAndView) element.getObjectValue();
                }
            }

            if (this.cachedResult == null) {
                final FutureTask<ModelAndView> ft = new FutureTask<>(this);

                // Check if the result is already being computed
                this.futureResult = usePending ? pending.putIfAbsent(this.cacheKey, ft) : null;

                // Not in cache and not in calculation pipeline
                if (this.futureResult == null) {
                    // When we get here atom.ms cannot stay -1 due to try..finally setting it
                    // to a value >=0 in Task#call() or alternatively the arfiticial
                    // (timeout+1) value being set in catch(TimeoutException) of
                    // MoleculeController#handleRequestInternal()
                    this.futureResult = ft;
                    if (parallel) {
                        executorService.submit(ft);
                    }
                    else {
                        ft.run();
                    }
                }
            }
        }

        public ModelAndView call() throws Exception {
            this.atom.setWaitTime(msSinceStart());

            final boolean runsAsync = Thread.currentThread() != this.caller;

            final RequestContext oldContext = RequestContextHolder.getRequestContext();
            RequestContextHolder.setRequestContext(this.context);
            final StopWatch oldStopWatch = StopWatchHolder.getStopWatch();
            StopWatchHolder.setStopWatch(this.sw);

            if (runsAsync) {
                MDC.put(UNIQUE_ID, this.context.getUniqueId());
            }

            try {
                return dispatch();
            }
            finally {
                RequestContextHolder.setRequestContext(oldContext);
                StopWatchHolder.setStopWatch(oldStopWatch);
                this.atom.setMs(msSinceStart() - this.atom.getWaitTime());
                if (runsAsync) {
                    MDC.remove(UNIQUE_ID);
                }
            }
        }

        public ModelAndView getResult(MutableLong timeout)
                throws ExecutionException, InterruptedException, TimeoutException {
            final ModelAndView tmp = doGetResult(timeout);
            if (tmp == null) {
                return null;
            }

            // ALWAYS create a copy, since we do not know whether the futureResult is shared
            // by other atoms. If result is shared by atoms in the same request (i.e., thread), we
            // need a copy to support different correlation ids. If atoms in other threads share
            // the model, we also have to think about thread-safety: e.g., an Interceptor
            // possibly modifies the model before it will be rendered. Since the map in ModelAndView
            // is NOT thread-safe, that could bring about all sorts of problems -- and synchronization
            // would be too difficult to achieve.
            // If the futureResult is not shared, copying does no harm anyway, since a million copies
            // per second per thread should be possible on a decent machine.
            final ModelAndView result = new ModelAndView(tmp.getViewName(), tmp.getModel());
            result.addObject(KEY_CORRELATION_ID, this.atom.getId());
            return result;
        }

        private ModelAndView doGetResult(MutableLong timeout)
                throws ExecutionException, InterruptedException, TimeoutException {
            if (this.cachedResult != null) {
                return this.cachedResult;
            }

            if (this.futureResult == null) {
                throw new TimeoutException("Task never started");
            }

            try {
                final ModelAndView result;
                if (this.futureResult.isDone()) {
                    result = this.futureResult.get();
                }
                else {
                    final long start = System.currentTimeMillis();
                    result = this.futureResult.get(timeout.getValue(), TimeUnit.MILLISECONDS);
                    timeout.setValue(Math.max(1, timeout.getValue() - (System.currentTimeMillis() - start)));
                }

                if (this.cachedResult == null && this.cacheTimeSec > 0) {
                    final Element element = new Element(this.cacheKey, result);
                    element.setTimeToLive(this.cacheTimeSec);
                    atomsCache.put(element);
                }

                return result;
            }
            finally {
                if (usePending) {
                    pending.remove(this.cacheKey);
                }
            }
        }

        private Map<String, Object> getModel() {
            try {
                final ModelAndView muv = this.cachedResult != null
                        ? this.cachedResult : this.futureResult.get();
                if (muv == null) {
                    throw new AtomDependencyException("dependent model is null");
                }
                return muv.getModel();
            }
            catch (InterruptedException | ExecutionException e) {
                throw new AtomDependencyException(e);
            }
        }

        private ModelAndView dispatch() {
            try {
                final Controller c = controllerMapping.getController(request);
                if (c == null || c == this) {
                    MoleculeController.this.logger.warn("<dispatch> No atom controller for " + request.getRequestURI());
                    return null;
                }

                if (this.dependsOn != null) {
                    request.setAttribute(DEPENDENCY_ATTRIBUTE_NAME, this.dependsOn.getModel());
                }

                final ModelAndView result = c.handleRequest(request, response);
                if (result != null) {
                    result.addObject(KEY_ATOMNAME, this.atom.getName());
                }
                return result;
            }
            catch (MergerException me) {
                // stack trace does not matter for MergerExceptions
                logger.warn("<dispatch> failed for " + atom + ": " + me.getMessage());
                return getError(this, me.getCode(), me.getMessage());
            }
            catch (InterruptedException ie) {
                // This catch block is useless in regards to the output result (that is actually set in handleRequestInternal)
                // UNLESS this task was pending. In the latter case this catch block prevents HTTP/500 being returned to the caller.
                // It prevents uncessary ERROR messages + stack trace in the log that would otherwise result from catch(Throwable) below.
                logger.warn("<dispatch> cancelled due to timeout for " + atom + ": " + ie.getMessage());
                return getError(this, "atom.cancelled", ie.getMessage());
            }
            catch (Throwable t) {
                logger.error("<dispatch> failed for " + atom, t);
                return getError(this, Level.FATAL_ERROR.name(), t.getMessage());
            }
        }

        private boolean isDone() {
            return this.futureResult == null || this.futureResult.isDone();
        }

        void setDependsOn(Task dependsOn) {
            this.dependsOn = dependsOn;
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            return this.futureResult != null && this.futureResult.cancel(mayInterruptIfRunning);
        }
    }

    /**
     * This controller requires that the following three request attributes are set:<ol>
     * <li> Key {@link de.marketmaker.istar.merger.web.easytrade.MoleculeRequest#REQUEST_ATTRIBUTE_NAME},
     * value {@link MoleculeRequest} object
     * <li> Key {@link de.marketmaker.istar.merger.web.ZoneDispatcherServlet#ZONE_ATTRIBUTE},
     * value {@link de.marketmaker.istar.merger.web.Zone} object
     * </ol>
     *
     * @param request  the request
     * @param response may be used to return a response in case an error is encountered. In that
     *                 case, this method will return null
     * @return molecule model and view
     * @throws Exception if request processing fails
     */
    public ModelAndView handleRequestInternal(final HttpServletRequest request,
                                              HttpServletResponse response) throws Exception {
        final StopWatch sw = StopWatchHolder.getStopWatch();
        if (sw.isRunning()) {
            sw.stop();
        }
        sw.start("execute");

        final Zone zone = (Zone) request.getAttribute(ZONE_ATTRIBUTE);
        final MoleculeRequest mr = (MoleculeRequest) request.getAttribute(REQUEST_ATTRIBUTE_NAME);

        this.meterRegistry.counter(COUNTER_MOLECULE_IN, "zone", zone.getName()).increment();

        final RequestContext context = RequestContextHolder.getRequestContext();

        final ModelAndView result = new ModelAndView(getViewName(request, zone));
        result.addObject(KEY_REQUEST, mr);
        result.addObject("zone", zone.getName());

        final String uriPrefix =
                request.getRequestURI().substring(0, request.getRequestURI().lastIndexOf('/') + 1);

        final boolean parallel = canDoInParallel(mr);

        final List<MoleculeRequest.AtomRequest> atomRequests = mr.getAtomRequests();

        final int atomCount = atomRequests.size();
        context.put(RequestContext.KEY_NUM_ATOMS, atomCount);

        final Task[] tasks = new Task[atomCount];
        for (int i = 0; i < atomCount; i++) {
            final MoleculeRequest.AtomRequest atom = atomRequests.get(i);
            this.meterRegistry.counter(COUNTER_ATOM_IN, "name", atom.getName()).increment();
            final HttpServletRequest atomRequest = createAtomRequest(request, zone, uriPrefix, atom);

            final HttpServletResponse wrappedResponse = new ResponseWrapper(response);
            tasks[i] = new Task(atom, atomRequest, wrappedResponse, context);
            // the last task will always run in this thread
            tasks[i].parallel = parallel && i < (atomCount - 1);
        }

        if (mr.isWithDependencies()) {
            assignDependencies(tasks);
        }

        // FIXME: There is no hard timeout on an atom executed synchronous in this thread
        // This may lead to this thread being blocked for a potentially very long time.
        // At least we try to not over-use by checking after each task for timeout.
        long localThreadTimeoutRemaining = TimeUnit.SECONDS.toMillis(this.maxWaitSeconds);
        for (Task task : tasks) {
            if (localThreadTimeoutRemaining > 0) {
                long startOfLocalTask = System.currentTimeMillis();
                task.execute();
                if (!task.parallel) {
                    long taskDuration = System.currentTimeMillis() - startOfLocalTask;
                    localThreadTimeoutRemaining -= taskDuration;
                }
            }
        }

        final MutableLong timeout = new MutableLong(Math.max(1, localThreadTimeoutRemaining));
        final List<ModelAndView> results = new ArrayList<>(atomCount);
        for (final Task task : tasks) {
            Exception exception = null;
            try {
                final ModelAndView mv = task.getResult(timeout);
                if (mv != null) {
                    mv.addAllObjects((Map) task.request.getAttribute(Zone.ATOM_CONTEXT_ATTRIBUTE));
                    results.add(mv);
                }
                else {
                    results.add(getError(task, "no.content", "no content"));
                }
            }
            catch (InterruptedException e) {
                this.logger.warn("<handleRequestInternal> interrupted?");
                exception = e;
                Thread.currentThread().interrupt();
                throw new HttpException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            catch (ExecutionException e) {
                this.logger.warn("<handleRequestInternal> failed to execute " + task.atom, e.getCause());
                exception = e;
                results.add(getError(task, "unspecified.error", e.getCause().getMessage()));
            }
            catch (TimeoutException e) {
                this.logger.warn("<handleRequestInternal> timeout for " + task.atom);
                exception = e;
                results.add(getError(task, "timeout", "timeout"));
                // Set a grace-period for following tasks
                timeout.setValue(1L);
                // We don't know excatly how long this task is going to take
                // but we set its duration to maxWaitSeconds + 1ms as an indicator value for
                // timeout of a task
                // If cancelling below reacts quickly enough it will overwrite it with the correct
                // value in Task#call()'s finally block
                task.atom.setMs((this.maxWaitSeconds * 1000) + 1);
                // Try stopping the task still running in the ExecutorService
                task.cancel(true);
            } finally {
                ackAtom(zone.getName(), task, exception);
            }
        }

        sw.stop();

        result.addObject(KEY_ATOMS, results);
        result.addObject("generated", new DateTime());

        result.addAllObjects(zone.getContextMap(null));

        result.addObject("took", sw.getTotalTimeMillis());
        result.addObject("tt", new TimeTaker());
        this.meterRegistry.timer(TIMER_MOLECULE, "zone", zone.getName())
            .record(sw.getTotalTimeMillis(), TimeUnit.MILLISECONDS);

        if (sw.getTotalTimeMillis() > this.warnRequestThreshold) {
            this.logger.warn("<handleRequestInternal> " + zone + " slow: " + toString(tasks) + ", "
                + sw.prettyPrint());
        }
        else if (this.logger.isDebugEnabled()) {
            this.logger.debug("<handleRequestInternal> " + toString(tasks) + ", " + sw.toString());
        }

        try {
            return result;
        } finally {
            /*
            Have to be moved here, since otherwise RequestContext will be null when invoked from
            MmwebServiceMethod (dm-iview). Note that MoleculeRequestLogger works since it is set as
            interceptor once in dmxml-servlet.xml and once in gwtrpc-servlet.xml and does not need
            access to RequestContext.
             */
            if (this.accessProcessor != null) {
                this.accessProcessor.process(zone, mr, result);
            }
        }
    }

    private void ackAtom(String zoneName, Task task, Exception ex) {
        final int ms = task.atom.getMs();

        this.meterRegistry.counter(COUNTER_ATOM_OUT,
            "name", task.atom.getName(),
            "processed", ms < 0 ? "0" : "1"
        ).increment();

        if (ms >= 0) {
            this.meterRegistry.timer(TIMER_ATOM,
                "zone", zoneName,
                "name", task.atom.getName(),
                "exception", ex == null ? "none" : ex.getClass().getSimpleName()
            ).record(ms, TimeUnit.MILLISECONDS);
        }
    }

    private void assignDependencies(Task[] tasks) {
        for (Task task : tasks) {
            if (task.atom.getDependsOn() != null) {
                task.setDependsOn(findTaskWithAtom(tasks, task.atom.getDependsOn()));
            }
        }
    }

    private Task findTaskWithAtom(Task[] tasks, MoleculeRequest.AtomRequest atom) {
        for (Task task : tasks) {
            if (task.atom == atom) {
                return task;
            }
        }
        throw new IllegalStateException("no task with atom " + atom);
    }

    private HttpServletRequest createAtomRequest(HttpServletRequest request, Zone zone,
                                                 String uriPrefix, MoleculeRequest.AtomRequest atom) {
        final Map<String, String[]> parameters
                = zone.getParameterMap(atom.getParameterMap(), atom.getName());
        parameters.put(KEY_CORRELATION_ID, new String[]{atom.getId()});
        parameters.put(KEY_ATOMNAME, new String[]{atom.getName()});

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(Zone.ATOM_CONTEXT_ATTRIBUTE, zone.getContextMap(atom.getName()));

        return RequestWrapper.create(request, parameters, uriPrefix + atom.getName(), attributes);
    }

    private String getViewName(HttpServletRequest request, Zone zone) {
        final String viewName = (String) request.getAttribute(MoleculeRequest.VIEW_ATTRIBUTE_NAME);
        if (viewName != null) {
            return viewName;
        }
        String zoneName = zone.getName();
        String templateBase = zone.getTemplateBase();
        if (zoneName.equals(templateBase)) {
            return templateBase + SEP_TEMPLATE_AND_VIEW + VIEW;
        } else {
            return zoneName + SEP_TEMPLATE_HIERARCHY + templateBase + SEP_TEMPLATE_AND_VIEW + VIEW;
        }
    }

    private static boolean canDoInParallel(MoleculeRequest mr) {
        if (mr.isWithDependencies()) {
            return false;
        }

        final List<MoleculeRequest.AtomRequest> requests = mr.getAtomRequests();
        if (requests.size() == 1) {
            return false;
        }
        for (MoleculeRequest.AtomRequest atomRequest : requests) {
            if (SEQUENTIAL_ATOMS.matcher(atomRequest.getName()).matches()) {
                return false;
            }
        }
        return true;
    }

    private static ModelAndView getError(Task task, final String code, final String description) {
        final Map<String, Object> m = new HashMap<>();
        m.put(KEY_ATOMNAME, task.atom.getName());
        m.put(ERROR_VIEWNAME, Error.error(code, description));
        if (task.atom.getId() != null) {
            m.put(KEY_CORRELATION_ID, task.atom.getId());
        }
        return new ModelAndView(ERROR_VIEWNAME, m);
    }

    private String toString(Task[] tasks) {
        final StringBuilder sb = new StringBuilder(tasks.length * 80);
        sb.append("[");
        for (int i = 0; i < tasks.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(tasks[i]);
            if (tasks[i].isDone() && tasks[i].sw.getTaskCount() > 0) {
                this.logger.info("<toString> " + tasks[i].sw.prettyPrint());
            }

        }
        sb.append("]");
        return sb.toString();
    }

    public boolean hasAtomController(String name) {
        return this.controllerMapping.getAtomController(name) != null;
    }
}

