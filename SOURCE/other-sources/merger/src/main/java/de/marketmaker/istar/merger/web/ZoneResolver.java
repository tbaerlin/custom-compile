/*
 * ZoneResolver.java
 *
 * Created on 14.08.2006 15:54:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ZoneResolver extends ZoneProvider {
    Zone resolveZone(HttpServletRequest request);
}
