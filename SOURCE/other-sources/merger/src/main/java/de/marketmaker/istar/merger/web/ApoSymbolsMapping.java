/*
 * ApoSymbolsMapping.java
 *
 * Created on 10.09.2008 11:18:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ApoSymbolsMapping extends ParameterMappingHandlerInterceptor {
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object o) throws Exception {

        final Map<String, String[]> values = request.getParameterMap();
        if (values == null) {
            return true;
        }

        final List<String> isins = getSymbols(values, "isin");
        final List<String> wkns = getSymbols(values, "wkn");
        final List<String> names = getNames(values);

        request.getParameterMap().put("isins", toArray(isins));
        request.getParameterMap().put("wkns", toArray(wkns));
        request.getParameterMap().put("names", toArray(names));

        return true;
    }

    private List<String> getNames(final Map<String, String[]> values) {
        final List<String> result = new ArrayList<>();
        for (Map.Entry<String, String[]> e : values.entrySet()) {
            if (e.getKey().contains("name")) {
                final String value = e.getValue()[0];
                result.add(value);
            }

        }
        return result;
    }

    private List<String> getSymbols(final Map<String, String[]> values, final String symbolId) {
        final List<String> result = new ArrayList<>();
        for (Map.Entry<String, String[]> e : values.entrySet()) {
            if (e.getKey().contains(symbolId)) {
                final String value = e.getValue()[0];
                final String[] tokens = value.trim().split("\\|");
                if (tokens.length != 2) {
                    result.add(tokens[0]);
                    continue;
                }
                final String[] mappedMarkets = translate(new String[]{tokens[1]});
                result.add(tokens[0] + "@" + StringUtils.arrayToCommaDelimitedString(mappedMarkets));

            }
        }
        return result;
    }
}