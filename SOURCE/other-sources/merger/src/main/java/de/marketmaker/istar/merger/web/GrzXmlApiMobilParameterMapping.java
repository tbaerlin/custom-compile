/*
 * DefXmlApiSymbolsMapping.java
 *
 * Created on 10.09.2008 11:18:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

/**
 * Retrieves symbols from a request (all parameters in the original web request except some
 * specifically excluded are supposed to specify wkns) and adds them as request parameters
 * under the name <tt><em>targetParameterName</em>Symbols</tt>. The origninal wkns may contain a
 * market identifier (separated by "."). Mapped market names will be added as parameter under
 * the name <tt><em>targetParameterName</em>Markets</tt>.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GrzXmlApiMobilParameterMapping extends ParameterMappingHandlerInterceptor {
    /**
     * all parameters in the original request are supposed to specify wkns except for those
     * contained in this Set.
     */
    private static final Set<String> NO_WKN_PARAMETERS = Collections.singleton("longtags");

    public boolean preHandle(HttpServletRequest httpServletRequest,
                             HttpServletResponse httpServletResponse, Object o) throws Exception {

        final List<String> wkns = getWkns(httpServletRequest);
        if (wkns.isEmpty()) {
            return true;
        }

        final String[] symbols = new String[wkns.size()];
        final String[] markets = new String[wkns.size()];

        for (int i = 0; i < wkns.size(); i++) {
            final String id = wkns.get(i);
            final String[] tokens = id.split("\\.");
            symbols[i] = tokens[0];
            if (tokens.length != 2) {
                continue;
            }

            final String[] mappedMarkets = translate(new String[]{tokens[1]});
            markets[i] = StringUtils.arrayToCommaDelimitedString(mappedMarkets);
        }

        httpServletRequest.getParameterMap().put(this.targetParameterName + "Symbols", symbols);
        httpServletRequest.getParameterMap().put(this.targetParameterName + "Markets", markets);

        return true;
    }

    private List<String> getWkns(HttpServletRequest httpServletRequest) {
        final HttpServletRequest delegate
                = (HttpServletRequest) httpServletRequest.getAttribute(RequestWrapper.DELEGATE_REQUEST);

        final List<String> result = new ArrayList<>();

        for (String parameter : Collections.list((Enumeration<String>) delegate.getParameterNames())) {
            if (NO_WKN_PARAMETERS.contains(parameter)) {
                continue;
            }
            for (String value: delegate.getParameterValues(parameter)) {
                if (StringUtils.hasText(value)) {
                    result.add(value);
                }
            }
        }
        return result;
    }
}