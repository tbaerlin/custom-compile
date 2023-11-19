/*
 * ProfileInterceptor.java
 *
 * Created on 27.10.2008 13:06:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.istar.domain.profile.Profile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WebxlProfileInterceptor extends ProfileInterceptor {
    @Override
    protected RequestContext getRequestContext(HttpServletRequest request, Profile profile) {
        return new RequestContext(profile, LbbwMarketStrategy.INSTANCE);
    }
}
