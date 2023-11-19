/*
 * RequestContextHolder.java
 *
 * Created on 18.08.2009 17:16:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.context;

import java.util.concurrent.Callable;

import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RequestContextHolder {    
    private static ThreadLocal<RequestContext> holder = new ThreadLocal<>();

    /**
     * Associate the given RequestContext with the current thread.
     * @param requestContext the current RequestContext, or <code>null</code> to reset
     * the thread-bound context
     */
    public static void setRequestContext(RequestContext requestContext) {
        if (requestContext != null) {
            holder.set(requestContext);
        }
        else {
            holder.remove();
        }
    }

    /**
     * Return the RequestContext associated with the current thread,
     * if any.
     * @return the current RequestContext, or <code>null</code> if none
     */
    public static RequestContext getRequestContext() {
        return holder.get();
    }

    /**
     * Calls the supplied Callable while making sure that the thread local RequestContextHolder contains
     * a RequestContext with the given Profile. After c returns, the original RequestContext will
     * be restored in the RequestContextHolder.
     * @param p Profile for calling c
     * @param c to be called
     * @return result of calling c
     * @throws Exception if c throws it
     */
    public static <V> V callWith(Profile p, Callable<V> c) throws Exception {
        return callWith(getRequestContext().withProfile(p), c);
    }

    /**
     * Calls the supplied Callable while making sure that the thread local RequestContextHolder contains
     * a RequestContext with the given Profile. After c returns, the original RequestContext will
     * be restored in the RequestContextHolder.
     * @param ms MarketStrategy for calling c
     * @param c to be called
     * @return result of calling c
     * @throws Exception if c throws it
     */
    public static <V> V callWith(MarketStrategy ms, Callable<V> c) throws Exception {
        return callWith(getRequestContext().withMarketStrategy(ms), c);
    }

    /**
     * Calls the supplied Callable while making sure that the thread local RequestContextHolder contains
     * a RequestContext with the given Profile and MarketStrategy. If a RequestContext exists,
     * it will be used as parent context, so that locales, intradayMap etc. are still available.
     * After c returns, the original RequestContext will be restored in the RequestContextHolder.
     * @param ms MarketStrategy for calling c
     * @param p Profile for calling c
     * @param c to be called
     * @return result of calling c
     * @throws Exception if c throws it
     */
    public static <V> V callWith(Profile p, MarketStrategy ms, Callable<V> c) throws Exception {
        final RequestContext existing = getRequestContext();
        if (existing != null) {
            return callWith(existing.withProfile(p).withMarketStrategy(ms), c);
        }
        else {
            return callWith(new RequestContext(p, ms), c);
        }
    }
    
    private static <V> V callWith(RequestContext context, Callable<V> c) throws Exception {
        final RequestContext current = getRequestContext();
        setRequestContext(context);
        try {
            return c.call();
        } finally {
            setRequestContext(current);
        }
    }
}
