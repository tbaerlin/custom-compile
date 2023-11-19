/*
 * DefXmlApiSymbolsMapping.java
 *
 * Created on 10.09.2008 11:18:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DefXmlApiSymbolsMapping extends ParameterMappingHandlerInterceptor {
    public boolean preHandle(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse, Object o) throws Exception {

        String[] values = httpServletRequest.getParameterValues(this.sourceParameterName);
        if (values == null) {
            return true;
        }

        final String[] ids = values[0].split(",");

        final String[] symbols = new String[ids.length];
        final String[] markets = new String[ids.length];

        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            final String[] tokens = id.split("\\.");
            if (tokens.length != 2) {
                continue;
            }
            symbols[i] = tokens[0];
            final String[] mappedMarkets = translate(new String[]{tokens[1]});
            markets[i] = StringUtils.arrayToCommaDelimitedString(mappedMarkets);
        }

        httpServletRequest.getParameterMap().put(this.targetParameterName + "Symbols", symbols);
        httpServletRequest.getParameterMap().put(this.targetParameterName + "Markets", markets);

        return true;
    }
}
