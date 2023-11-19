/*
 * FndFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * Queries future symbols matching given criteria.
 * <p>
 * Future symbols follow a specific pattern, which embodies their expiration information. Symbols of
 * futures with specific underlying continue to be rolled forward and the expired ones become invalid
 * and will not be maintained by instrument server. This service delivers those future symbols that
 * are still valid and meet given requirements like:
 * <ul>
 * <li>symbol prefix</li>
 * <li>symbol suffix pattern</li>
 * <li>an expiration period</li>
 * </ul>
 * </p>
 *
 * @author zzhao
 */
public class FutSymbolFinder extends EasytradeCommandController {

    public static class Command {
        private static final String DEFAULT = "[0-9CONM]{3}";

        private String symbolPrefix;

        private String suffixPattern = DEFAULT;

        private Period period = Period.years(1);

        /**
         * @return Period restriction. Only symbols of those futures would be returned, whose expiration
         *         date is within this given period. Default is one year.
         */
        public Period getPeriod() {
            return period;
        }

        public void setPeriod(Period period) {
            this.period = period;
        }

        /**
         * @return A regex expression to restrict future symbols, whose suffix matches this given
         *         pattern. Default is {@value #DEFAULT}
         */
        public String getSuffixPattern() {
            return suffixPattern;
        }

        public void setSuffixPattern(String suffixPattern) {
            this.suffixPattern = suffixPattern;
        }

        /**
         * @return A prefix by which futures are searched. All returned future symbols would have this
         *         prefix.
         * @sample 846959.DTB.
         * @repeatable
         */
        @NotNull
        @de.marketmaker.istar.common.validator.Pattern(regex = "(\\w+\\.)+(,(\\w+\\.)+)*")
        public String getSymbolPrefix() {
            return symbolPrefix;
        }

        public void setSymbolPrefix(String symbolPrefix) {
            this.symbolPrefix = symbolPrefix;
        }
    }

    private static final Comparator<Quote> FUTURE_COMP = new Comparator<Quote>() {
        @Override
        public int compare(Quote o1, Quote o2) {
            final DateTime ex1 = o1.getInstrument().getExpiration();
            final DateTime ex2 = o2.getInstrument().getExpiration();

            if (null == ex1 && null == ex2) {
                return o1.getSymbolVwdcode().compareTo(o2.getSymbolVwdcode());
            }
            else if (null == ex1) {
                return 1;
            }
            else if (null == ex2) {
                return -1;
            }
            else {
                return ex1.compareTo(ex2);
            }
        }
    };

    private static final String CON_OR = "@";

    private RatiosProvider ratiosProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public FutSymbolFinder() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final HashMap<String, Object> model = new HashMap<>();
        model.put("symbols", getFutures((Command) o));
        return new ModelAndView("futsymbolfinder", model);
    }

    private Map<String, List<Quote>> getFutures(Command cmd) {
        final String symbolPrefix = cmd.getSymbolPrefix().replace(",", CON_OR);
        final List<Instrument> instruments = identifyInstruments(symbolPrefix);
        if (CollectionUtils.isEmpty(instruments)) {
            return Collections.emptyMap();
        }

        final HashMap<String, List<Quote>> ret = prepareMap(symbolPrefix);
        final Pattern pattern = getPattern(symbolPrefix, cmd.getSuffixPattern());

        final DateTime today = new DateTime();
        final Interval interval = new Interval(today, today.plus(cmd.getPeriod()));

        for (Instrument ins : instruments) {
            final DateTime expiration = ins.getExpiration();
            if (expiration != null && !interval.contains(expiration)) {
                continue;
            }

            for (Quote quote : ins.getQuotes()) {
                final String prefix = matchPrefix(quote.getSymbolVwdcode(), pattern);
                if (StringUtils.hasText(prefix)) {
                    ret.get(prefix).add(quote);
                    break;
                }
            }
        }

        for (List<Quote> quotes : ret.values()) {
            quotes.sort(FUTURE_COMP);
        }

        return ret;
    }

    private String matchPrefix(String vwdCode, Pattern pattern) {
        if (!StringUtils.hasText(vwdCode)) {
            return null;
        }

        final Matcher matcher = pattern.matcher(vwdCode);
        if (matcher.matches()) {
            return matcher.group(1);
        }

        return null;
    }


    private Pattern getPattern(String symbolPrefix, String suffixPattern) {
        final String[] parts = symbolPrefix.split(CON_OR);
        final StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (String part : parts) {
            sb.append(Pattern.quote(part)).append('|');
        }
        sb.setCharAt(sb.length() - 1, ')');
        sb.append(suffixPattern);
        return Pattern.compile(sb.toString());
    }

    private List<Instrument> identifyInstruments(String symbolPrefix) {
        final List<Long> iids = getInstrumentIds(symbolPrefix);
        if (CollectionUtils.isEmpty(iids)) {
            return Collections.emptyList();
        }

        return this.instrumentProvider.identifyInstruments(iids);
    }

    private List<Long> getInstrumentIds(String symbolPrefix) {
        final RatioSearchResponse response = doRatiosSearch(symbolPrefix);

        if (!response.isValid() || !(response instanceof DefaultRatioSearchResponse)) {
            this.logger.warn("<getInstrumentIds> invalid or wrong response type: " + response);
            return Collections.emptyList();
        }
        final DefaultRatioSearchResponse drsp = (DefaultRatioSearchResponse) response;
        final List<RatioDataResult> elements = drsp.getElements();
        if (CollectionUtils.isEmpty(elements)) {
            return Collections.emptyList();
        }

        final ArrayList<Long> ret = new ArrayList<>(elements.size());
        for (RatioDataResult element : elements) {
            ret.add(element.getInstrumentid());
        }

        return ret;
    }

    private HashMap<String, List<Quote>> prepareMap(String symbolPrefix) {
        final HashMap<String, List<Quote>> ret = new HashMap<>();
        final String[] pres = symbolPrefix.split(CON_OR);
        for (String pre : pres) {
            ret.put(pre, new ArrayList<Quote>(5));
        }
        return ret;
    }

    private RatioSearchResponse doRatiosSearch(String prefix) {
        final RatioSearchRequest req = new RatioSearchRequest(
                RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        req.setType(InstrumentTypeEnum.FUT);
        req.addParameter("vwdCode", prefix);
        req.addParameter("i", "0");
        req.addParameter("n", "1000");


        return this.ratiosProvider.search(req);
    }

}