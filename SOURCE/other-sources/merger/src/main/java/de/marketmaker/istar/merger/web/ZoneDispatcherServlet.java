/*
 * ZoneDispatcherServlet.java
 *
 * Created on 14.08.2006 16:04:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.web.easytrade.StopWatchHolder;

/**
 * A DispatcherServlet that maps requests to a {@link Zone} by means of a
 * {@link ZoneResolver} bean. The ZoneResolver is supposed to be defined in the
 * bean factory, where it is expected to be named "zoneResolver". The main purpose
 * of the Zone is to add additional request parameters to an HttpServletRequest,
 * which are either default parameters or fixed parameters. The former can be
 * overridden by actual request parameters whereas the latter override any
 * actual parameters.
 * <p/>
 * In addition to zones, this servlet also provides means to cache ModelAndView objects
 * returned by controllers. To define caches, the servlet's specification in the
 * <tt>web.xml</tt> file has to contain init-parameters with names
 * cache0,...,cache<em>n</em>, whose values denote the names of Ehcache
 * beans in this servlet's bean factory.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ZoneDispatcherServlet extends DispatcherServlet {
    /**
     * Well-known name for the ZoneResolver object in the bean factory for this namespace.
     */
    public static final String ZONE_RESOLVER_BEAN_NAME = "zoneResolver";

    public static final String ZONE_ATTRIBUTE = ZoneDispatcherServlet.class.getName() + ".ZONE";

    private ZoneResolver zoneResolver;

    /**
     * Zone properties can define a map of objects for each zone member; for each atom, the
     * MoleculeController adds this map to the request that is used to execute the atom. For
     * non-atom executions (e.g., charts), this servlet can be instructed to add the context map
     * to the request iff the servlet's init-parameter "addContextMapAttribute" has the
     * value "true".
     */
    private boolean addContextMapAttribute;

    /**
     * Well-known request parameter name for requests whose ModelAndView should be cached
     */
    public static final String CACHE_PARAMETER_NAME = "cacheTimeSec";

    /**
     * Well-known request parameter name for named caches
     */
    protected static final String CACHE_NAME = "cacheName";

    /**
     * Caches for ModelAndView objects
     */
    private Map<String, Ehcache> cachesByName = new ConcurrentHashMap<>();

    /**
     * the cache specified as cache0
     */
    protected String defaultCacheName = null;

    /**
     * Well-known name for the RequestStatistics bean.
     */
    public static final String REQUEST_STATISTICS_BEAN_NAME = "requestStatistics";

    /**
     * Well-known name for the RequestLogger object in the bean factory for this namespace.
     */
    public static final String REQUEST_LOGGER_BEAN_NAME = "requestLogger";

    private RequestLogger requestLogger;

    private int slowResponseMs = 2000;

    protected void initFrameworkServlet() throws ServletException, BeansException {
        super.initFrameworkServlet();
        this.addContextMapAttribute = "true".equals(getInitParameter("addContextMapAttribute"));
        if (getInitParameter("slowResponseMs") != null) {
            slowResponseMs = Integer.parseInt(getInitParameter("slowResponseMs"));
        }
        initZoneResolver();
        initRequestLogger();
        initCaches();
    }

    private void initCaches() {
        int i = 0;
        String cacheName;
        while ((cacheName = getInitParameter("cache" + i++)) != null) {
            try {
                final Ehcache cache = getWebApplicationContext().getBean(cacheName, Ehcache.class);
                this.cachesByName.put(cacheName, cache);
                if (this.defaultCacheName == null) {
                    this.defaultCacheName = cacheName;
                }
                logger.info("<initCaches> added " + cacheName);
            } catch (NoSuchBeanDefinitionException ex) {
                logger.warn("<initCaches> no such cache: " + cacheName);
            }
        }
    }

    private void initZoneResolver() {
        try {
            this.zoneResolver = getWebApplicationContext().getBean(ZONE_RESOLVER_BEAN_NAME, ZoneResolver.class);
            if (logger.isInfoEnabled()) {
                logger.info("Using ZoneResolver [" + this.zoneResolver + "]");
            }
        } catch (NoSuchBeanDefinitionException ex) {
            if (logger.isInfoEnabled()) {
                logger.info("No ZoneResolver in application context: using no zones");
            }
        }
    }

    protected void initRequestLogger() {
        try {
            this.requestLogger = getWebApplicationContext().getBean(REQUEST_LOGGER_BEAN_NAME, RequestLogger.class);
            this.logger.info("<initRequestLogger> may log requests");
        } catch (NoSuchBeanDefinitionException ex) {
            logger.info("<initRequestLogger> will NOT log requests");
        }
    }

    protected void doDispatch(final HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        boolean createdStopwatch = false;
        StopWatch sw = StopWatchHolder.getStopWatch();
        if (sw == null) {
            sw = new StopWatch();
            StopWatchHolder.setStopWatch(sw);
            createdStopwatch = true;
        }
        else {
            if (sw.isRunning()) {
                sw.stop();
            }
        }
        sw.start("handle");

        HttpServletRequest zoneRequest = null;
        try {
            zoneRequest = getZoneRequest(request);

            logRequest(request);

            if (zoneRequest == null) {
                throw new HttpException(HttpServletResponse.SC_NOT_FOUND);
            }

            final ModelAndView mv = getCached(zoneRequest);
            if (mv == null) {
                super.doDispatch(zoneRequest, response);
            }
            else {
                startRenderStopwatch();
                super.render(mv, zoneRequest, response);
            }
        } catch (NoProfileException | NoLicenseException e) {
            onError(HttpServletResponse.SC_FORBIDDEN, e.getMessage(), zoneRequest, response);
        } catch (HttpException he) {
            onError(he.getErrorCode(), he.getMessage(), zoneRequest, response);
        } catch (Throwable t) {
            if (t instanceof Error) {
                this.logger.error("<doDispatch> failed for " + zoneRequest, t);
            }
            else {
                this.logger.warn("<doDispatch> failed for " + zoneRequest, t);
            }
            onError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), zoneRequest, response);
        } finally {
            if (sw.isRunning()) {
                sw.stop();
            }
            if (sw.getTotalTimeMillis() > slowResponseMs) {
                this.logger.warn("<doDispatch> slow: " + getCacheKey(request) + ", " + sw);
            }
            else if (this.logger.isDebugEnabled()) {
                this.logger.debug("<doDispatch> " + sw.prettyPrint());
            }
            if (createdStopwatch) {
                StopWatchHolder.setStopWatch(null);
            }
        }
    }

    private void onError(int errorCode, String errorMessage, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (response.isCommitted()) {
            return;
        }
        response.reset();
        final Zone z = (request != null) ? (Zone) request.getAttribute(ZONE_ATTRIBUTE) : null;
        if (z != null) {
            final ErrorPage ep = z.getErrorPage(request, errorCode, errorMessage);
            if (ep != null) {
                sendError(ep, response);
                return;
            }
        }

        sendError(errorCode, errorMessage, response);
    }

    private void sendError(ErrorPage ep, HttpServletResponse response) throws IOException {
        response.setStatus(ep.getErrorCode());
        if (ep.getContentType() != null) {
            response.setContentType(ep.getContentType());
        }
        else {
            response.setContentType("text/plain");
        }
        response.getWriter().write(ep.getErrorMessage());
    }

    private void sendError(int errorCode, String errorMessage, HttpServletResponse response)
            throws IOException {
        if (errorMessage == null) {
            response.sendError(errorCode);
        }
        else {
            response.setContentType("text/plain");
            response.sendError(errorCode, errorMessage);
        }
    }

    protected HttpServletRequest getZoneRequest(HttpServletRequest request) {
        final Zone zone = resolveZone(request);
        if (zone == null) {
            return null;
        }

        request.setAttribute(ZONE_ATTRIBUTE, zone);
        final String name = HttpRequestUtil.getRequestName(request);
        if (this.addContextMapAttribute) {
            request.setAttribute(Zone.ATOM_CONTEXT_ATTRIBUTE, zone.getContextMap(name));
        }
        //noinspection unchecked
        final Map<String, String[]> map = new HashMap<>(request.getParameterMap());
        addAttribute(request, map, ProfileResolver.AUTHENTICATION_KEY);
        addAttribute(request, map, ProfileResolver.AUTHENTICATION_TYPE_KEY);
        return RequestWrapper.create(request, zone.getParameterMap(map, name));
    }

    private void addAttribute(HttpServletRequest request, Map<String, String[]> map, String key) {
        final String s = (String) request.getAttribute(key);
        if (s != null) {
            map.put(key, new String[]{s});
        }
    }

    protected void render(ModelAndView modelAndView, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ErrorPage ep = ErrorPage.from(modelAndView);
        if (ep != null) {
            sendError(ep, response);
            return;
        }
        startRenderStopwatch();
        cacheModelAndView(modelAndView, request);
        super.render(modelAndView, request, response);
    }

    private void startRenderStopwatch() {
        StopWatch sw = StopWatchHolder.getStopWatch();
        if (sw.isRunning()) {
            sw.stop();
        }
        sw.start("render");
    }

    protected void cacheModelAndView(ModelAndView mv, HttpServletRequest request) {
        final Ehcache cache = getCache(request);
        if (cache == null) { // no caches defined
            return;
        }
        final int cacheTimeSec = ServletRequestUtils.getIntParameter(request, CACHE_PARAMETER_NAME, -1);
        if (cacheTimeSec <= 0) {
            return;
        }

        final Element element = new Element(getCacheKey(request), mv);
        element.setTimeToLive(cacheTimeSec);
        cache.put(element);
    }

    protected Ehcache getCache(HttpServletRequest request) {
        if (this.defaultCacheName == null) {
            return null;
        }
        final String cacheName =
                ServletRequestUtils.getStringParameter(request, CACHE_NAME, this.defaultCacheName);
        return this.cachesByName.get(cacheName);
    }

    protected long getLastModified(HttpServletRequest request) {
        final HttpServletRequest processedRequest = getZoneRequest(request);
        final Ehcache cache = getCache(request);
        if (cache != null) {
            final Element e = cache.get(getCacheKey(request));
            if (e != null) {
                return e.getCreationTime();
            }
        }

        return super.getLastModified(processedRequest);
    }

    protected ModelAndView getCached(HttpServletRequest request) {
        final Ehcache cache = getCache(request);
        if (cache != null) {
            final Element e = cache.get(getCacheKey(request));
            if (e != null) {
                return (ModelAndView) e.getObjectValue();
            }
        }
        return null;
    }

    protected final String getCacheKey(HttpServletRequest request) {
        final String qs = request.getQueryString();
        return request.getRequestURI() + (qs != null ? ("?" + qs) : "");
    }

    protected final Ehcache getCache(String name) {
        return this.cachesByName.get(name);
    }

    protected final Zone resolveZone(HttpServletRequest request) {
        if (this.zoneResolver != null) {
            return this.zoneResolver.resolveZone(request);
        }
        return null;
    }

    protected final void logRequest(HttpServletRequest request) {
        if (this.requestLogger != null) {
            this.requestLogger.log(request);
        }
    }
}
