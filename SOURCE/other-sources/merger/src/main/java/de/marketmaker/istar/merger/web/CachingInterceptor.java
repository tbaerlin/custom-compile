/*
 * ProfileInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.ServletRequestBindingException;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * An interceptor that caches ModelAndView objects for requests. A ModelAndView will be cached if
 * the request contains a "{@value #CACHETIME_PARAMETER}" int parameter with a value &gt; 0 and either
 * <ul>
 * <li> the request contains a "{@value #CACHENAME_PARAMETER}" parameter that matches a cache
 * in this object's cachesByName map, or
 * <li> the request does not contain a "{@value #CACHENAME_PARAMETER}" parameter
 * but a defaultCache has been set for this object.
 * </ul>
 * <p>
 * If a cached value is found, it will be added to the request as an attribute with name
 * "{@value #CACHED_MUV_ATTRIBUTE}". In order to make use of this value, a HandlerAdapter should
 * query whether that attribute exists and return the result without invoking a controller or
 * s.th. like that.
 *
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CachingInterceptor extends HandlerInterceptorAdapter {
    /**
     * Well-known request attribute name for cached model and view
     */
    public static final String CACHED_MUV_ATTRIBUTE = "cachedModelAndView";
    /**
     * Well-known request parameter name for requests whose ModelAndView should be cached
     */
    public static final String CACHETIME_PARAMETER = "cacheTimeSec";

    /**
     * Well-known request parameter name for named caches
     */
    protected static final String CACHENAME_PARAMETER = "cacheName";

    /**
     * Caches for ModelAndView objects
     */
    private final Map<String, Ehcache> cachesByName = new ConcurrentHashMap<>();

    /**
     * the default cache, if any
     */
    private Ehcache defaultCache = null;

    public long getLastModified(HttpServletRequest request) {
        final Ehcache cache;
        try {
            cache = getCache(request);
        } catch (ServletRequestBindingException e) {
            return -1L;
        }
        if (cache != null) {
            final Element e = cache.get(getCacheKey(request));
            if (e != null) {
                return e.getCreationTime();
            }
        }

        return -1L;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, ModelAndView mv) throws Exception {
        final Ehcache cache = getCache(request);
        if (cache == null) { // no caches defined
            return;
        }
        final int cacheTimeSec = ServletRequestUtils.getIntParameter(request, CACHETIME_PARAMETER, -1);
        if (cacheTimeSec <= 0) {
            return;
        }

        final Element element = new Element(getCacheKey(request), mv);
        element.setTimeToLive(cacheTimeSec);
        cache.put(element);
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o)
            throws Exception {
        final ModelAndView mv = getCached(request);
        if (mv != null) {
            request.setAttribute(CACHED_MUV_ATTRIBUTE, mv);
        }
        return true;
    }

    public void setCachesByName(Map<String, Ehcache> cachesByName) {
        this.cachesByName.putAll(cachesByName);
    }

    public void setDefaultCache(Ehcache defaultCache) {
        this.defaultCache = defaultCache;
    }

    protected Ehcache getCache(HttpServletRequest request) throws ServletRequestBindingException {
        if (this.cachesByName.isEmpty() && this.defaultCache == null) {
            return null;
        }
        final String cacheName = ServletRequestUtils.getStringParameter(request, CACHENAME_PARAMETER);
        return (cacheName != null)
            ? this.cachesByName.get(cacheName)
            : this.defaultCache;
    }

    protected final Ehcache getCache(String name) {
        return this.cachesByName.get(name);
    }

    protected final String getCacheKey(HttpServletRequest request) {
        final String qs = request.getQueryString();
        return request.getRequestURI() + (qs != null ? ("?" + qs) : "");
    }

    protected ModelAndView getCached(HttpServletRequest request) throws ServletRequestBindingException {
        final Ehcache cache = getCache(request);
        if (cache != null) {
            final Element e = cache.get(getCacheKey(request));
            if (e != null) {
                return (ModelAndView) e.getObjectValue();
            }
        }
        return null;
    }
}