/*
 * OptionMapping.java
 *
 * Created on 23.06.2010 14:57:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

/**
 * @author oflege
 */
class OptionFutureMapping {
    private static final Pattern VWD_OPT_PATTERN
            = Pattern.compile(".+\\..+\\..+\\..+");

    private static final Pattern OPT_PATTERN
            = Pattern.compile("([A-Z0-9]+)(JA|FB|MR|AP|MY|JN|JL|AU|SP|OC|NV|DC)([0-9]{2})(C|P)([0-9]+(\\.[0-9]+)?)([A-Z]?)");

    private static final Pattern FUT_PATTERN
            = Pattern.compile("([A-Z0-9]+)(JA|FB|MR|AP|MY|JN|JL|AU|SP|OC|NV|DC)([0-9]{2})");

    private enum Month {
        JA, FB, MR, AP, MY, JN, JL, AU, SP, OC, NV, DC
    }

    private static final Map<String, String> FUTURE_SYMBOLS = Collections.singletonMap("DAX", "846959");

    static boolean isOptionOrFuture(String symbol, String exchange) {
        return OPT_PATTERN.matcher(symbol).matches()
                || VWD_OPT_PATTERN.matcher(symbol).matches()
                || (FUT_PATTERN.matcher(symbol).matches() && "DTB".equals(MarketMapping.getVwdMarketCode(exchange)));
    }

    static RatioSearchRequest getOpraSearchRequest(RequestItem item) {
        final RatioSearchRequest result
                = new RatioSearchRequest(null /*RequestContextHolder.getRequestContext().getProfile()*/);
        result.setType(InstrumentTypeEnum.OPT);
        result.addParameter("i", "0");
        result.addParameter("n", "100");
        result.addParameter("sort1", "vwdMarket");
        result.addParameter("sort1:D", "true");

        final String symbol = item.getMappedSymbol();
        if (item.isItemPerVwdcode() || symbolMatchesVwdSyntax(symbol)) {
            result.addParameter("vwdCode", '"' + symbol + '"');
            return result;
        }

        final Matcher m = OPT_PATTERN.matcher(symbol);
        if (!m.matches()) {
            throw new IllegalArgumentException(symbol);
        }

        final String yearMonth = getYearMonth(m);

        result.addParameter("underlyingWkn", '"' + m.group(1) + '"');
        result.addParameter("expires:L", yearMonth + "-01");
        result.addParameter("expires:U", yearMonth + "-28");
        result.addParameter("osType", '"' + m.group(4) + '"');
        result.addParameter("strikePrice", m.group(5));
        return result;
    }

    private static boolean symbolMatchesVwdSyntax(String symbol) {
        return VWD_OPT_PATTERN.matcher(symbol).matches();
    }

    private static String getYearMonth(Matcher m) {
        final int month = Month.valueOf(m.group(2)).ordinal() + 1;
        return "20" + pad2(m.group(3)) + "-" + pad2(Integer.toString(month));
    }

    private static String pad2(String s) {
        return s.length() == 2 ? s : ("0" + s);
    }


    static String getTicker(String symbol) {
        final Matcher om = OPT_PATTERN.matcher(symbol);
        if (om.matches()) {
            return om.group(1);
        }

        final Matcher fm = FUT_PATTERN.matcher(symbol);
        return fm.matches() ? fm.group(1) : null;
    }

    static List<String> getVwdCodesForOptionOrFuture(RequestItem item, Instrument base) {
        if (item.isItemPerVwdcode() || symbolMatchesVwdSyntax(item.getMappedSymbol())) {
            return Arrays.asList(item.getMappedSymbol());
        }

        final Matcher om = OPT_PATTERN.matcher(item.getMappedSymbol());
        if (om.matches()) {
            return getVwdcodeForOption(item, base, om);
        }

        final Matcher fm = FUT_PATTERN.matcher(item.getMappedSymbol());
        if (fm.matches()) {
            return getVwdcodeForFuture(item, fm);
        }

        return null;
    }

    private static List<String> getVwdcodeForFuture(RequestItem item, Matcher m) {
        final String market = MarketMapping.getVwdMarketCode(item.getExchange());
        if (!"DTB".equals(market)) {
            return null;
        }

        final String symbol = FUTURE_SYMBOLS.get(m.group(1));

        if (symbol == null) {
            return null;
        }

        final Month month = Month.valueOf(m.group(2));
        final String year = m.group(3);

        return Collections.singletonList(symbol + "." + market + "." + year.charAt(1) + (month.ordinal() + 1));
    }


    private static List<String> getVwdcodeForOption(RequestItem item, Instrument base, Matcher m) {
        final List<String> result = new ArrayList<>();

        final List<String> markets = MarketMapping.getVwdMarketCodes(item.getExchange());
        for (final String market : markets) {
            final String symbol;
            if ("DTB".equals(market)) {
                if (base == null) {
                    continue;
                }
                else {
                    symbol = base.getSymbolWkn();
                }
            }
            else {
                symbol = m.group(1);
            }

            final Month month = Month.valueOf(m.group(2));
            final String year = m.group(3);
            final boolean put = "P".equals(m.group(4));
            final String strike = m.group(5);
            final int gen = (!"".equals(m.group(7))) ? m.group(7).charAt(0) - 'A' + 2 : 1;

            final StringBuffer sb = new StringBuffer(20);
            sb.append(symbol).append(".").append(market).append(".");
            sb.append(toVwdStrike(market, strike)).append(".").append(year.charAt(1));
            sb.append((char) ('A' + month.ordinal() + (put ? 12 : 0)));
            // generation numbers may change over time, but only one will be active at a given time,
            // so turn this code into a lucene prefix query by appending the lucene wildcard symbol *
            sb.append("*");

            result.add(sb.toString());
        }

        return result;
    }

    private static String toVwdStrike(String market, String strike) {
        if ("DTB".equals(market)) {
            return toVwdStrikeForDTB(strike);
        }

        return strike.replace('.', '_');
    }

    private static String toVwdStrikeForDTB(String strike) {
        final StringBuilder sb = new StringBuilder(strike.length() + 2);
        int dot = -1;
        for (int i = 0; i < strike.length(); i++) {
            if (strike.charAt(i) != '.') {
                sb.append(strike.charAt(i));
            }
            else {
                dot = i;
            }
        }

        if (dot == -1 || dot == strike.length() - 1) {
            return sb.append("00").toString();
        }
        if (dot == strike.length() - 2) {
            return sb.append("0").toString();
        }
        return sb.toString();
    }
}
