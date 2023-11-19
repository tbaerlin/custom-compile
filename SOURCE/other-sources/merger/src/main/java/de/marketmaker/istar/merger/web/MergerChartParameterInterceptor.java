/*
 * MergerChartParameterInterceptor.java
 *
 * Created on 19.06.13 12:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Maps parameters from old merger chart requests to those compatible with
 * {@link de.marketmaker.istar.merger.web.easytrade.chart.ImgChartAnalyseCommand}.<p>
 * Requires that the request's parameter map can be modified, which is the case if the
 * request already passed a {@link ZoneDispatcherServlet}, which wraps the original request
 * with a {@link RequestWrapper}.
 *
 * @author oflege
 */
public class MergerChartParameterInterceptor extends HandlerInterceptorAdapter {
    private static final Pattern SMALL_NUM = Pattern.compile("\\d{1,3}");

    private static final Pattern BENCHMARK_SPLIT = Pattern.compile("[,;\\s]");

    private static final Set<String> INTRADAY_CHART_NAMES = new HashSet<>(Arrays.asList(
            "activechart.gif", "IdChart",
            "imicro.gif", "intrachart.gif", "IntradayChart", "intrahistchart.gif",
            "printintrachart.gif", "printintrahistchart.gif",
            "sBrokerIntrapdfChart", "startchart.gif", "StartIntradayChart"
    ));

    private static final Map<String, String> FREQUENZ = new HashMap<>();

    private static final Map<String, String> CHARTTYP = new HashMap<>();

    private static final Map<String, String> INDICATOR = new HashMap<>();

    static {
        FREQUENZ.put("0", "daily");
        FREQUENZ.put("1", "weekly");
        FREQUENZ.put("2", "monthly");

        INDICATOR.put("1", "roc");
        INDICATOR.put("2", "momentum");
        INDICATOR.put("3", "rsi");
        INDICATOR.put("4", "macd");
        INDICATOR.put("5", "fs");
        INDICATOR.put("6", "obos");
        INDICATOR.put("7", "bb");

        CHARTTYP.put("bar", "bar");
        CHARTTYP.put("candle", "candle");
        CHARTTYP.put("ohlc", "ohlc");
    }

    private static class Mapper {
        private final HttpServletRequest request;

        private final Map<String, String[]> parameters;

        private List<String> benchmarks;

        Mapper(HttpServletRequest request) {
            this.request = request;
            this.parameters = request.getParameterMap();
        }

        private String get(final String name) {
            return request.getParameter(name);
        }

        private void add(String key, String value) {
            add(key, (value != null) ? new String[]{value} : null);
        }

        private void add(String key, String[] values) {
            if (values != null && !this.parameters.containsKey(key)) {
                this.parameters.put(key, values);
            }
        }

        private void addBenchmark(String s) {
            if (StringUtils.hasText(s) && s.trim().length() >= 3) {
                if (this.benchmarks == null) {
                    this.benchmarks = new ArrayList<>(4);
                }
                this.benchmarks.add(s.trim());
            }
        }

        private void addBenchmarks(String s) {
            if (StringUtils.hasText(s)) {
                for (String b : BENCHMARK_SPLIT.split(s)) {
                    addBenchmark(b);
                }
            }
        }

        void mapParameters() {
            add("symbol", get("wkn"));
            add("market", get("platz"));
            add("period", convertRange());
            add("aggregation", FREQUENZ.get(get("frequenz")));
            add("type", CHARTTYP.get(get("charttyp")));
            add("indicator", INDICATOR.get(get("indicator")));

            addBenchmark(get("vgl_index"));
            addBenchmarks(get("wkns"));
            add("benchmark", getBenchmarks());
        }

        private String[] getBenchmarks() {
            return (this.benchmarks != null)
                    ? this.benchmarks.toArray(new String[this.benchmarks.size()]) : null;
        }

        private String convertRange() {
            String range = get("range");
            if (range == null || !SMALL_NUM.matcher(range).matches()) {
                range = isIntraday() ? "0" : "12";
            }
            final int months = Integer.parseInt(range);
            if (months > 120) {
                add("from", "start");
                add("to", "today");
            }
            return (months == 0) ? "P1D" : ("P" + range + "M");
        }

        private boolean isIntraday() {
            return INTRADAY_CHART_NAMES.contains(HttpRequestUtil.getRequestName(request));
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {

        new Mapper(request).mapParameters();
        return true;
    }
}
