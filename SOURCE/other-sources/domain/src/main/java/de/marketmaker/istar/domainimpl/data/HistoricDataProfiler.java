/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import static de.marketmaker.istar.domain.profile.Selector.*;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.instrument.EntitlementQuote;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

/**
 * Since the historic data for securities are separately priced by data vendors a check is needed for the permission of its display. This class encapsulates the responsibility for determining the
 * permitted start time for historic data based on:
 *
 * <ul>
 *     <li>vwd feed market symbol (ADF_Boerse)</li>
 *     <li>entitled selectors</li>
 *     <li>instrument type</li>
 * </ul>
 *
 * @author Stefan Willenbrock
 */
public class HistoricDataProfiler {

    /**
     * Keep most restricted first.
     */
    public enum Entitlement {
        NONE,
        FTSE_NOT_ENTITLED(Period.days(0)),
        ITRAXX_NOT_ENTITLED(Period.months(6)),
        HISTORICAL_1Y(Period.years(1)),
        HISTORICAL_3Y(Period.years(3)),
        HISTORICAL_5Y(Period.years(5)),
        FTSE_ENTITLED(Period.years(10)),
        HISTORICAL_10Y(Period.years(10)),
        HISTORICAL_20Y(Period.years(20)),
        HISTORICAL_30Y(Period.years(30)),
        ALL;

        private static final DateTime START_OF_TIME = new DateTime(0, 1, 1, 0, 0);

        private final Supplier<DateTime> start;

        Entitlement() {
            this.start = null;
        }

        Entitlement(Period period) {
            this.start = () -> DateTime.now().withTimeAtStartOfDay().minus(period);
        }

        public Optional<DateTime> getStart() {
            return Optional.ofNullable(this.start).map(Supplier::get);
        }

        public Optional<DateTime> getEnd() {
            return Optional.empty(); // A stub implementation for now
        }

        public boolean isRestricted() {
            return this == NONE || getStart().isPresent() || getEnd().isPresent();
        }

        /**
         * @return true, if historical interval is allowed for access due to entitlement
         */
        public boolean validateInterval(Profile profile, Quote quote, Interval interval) {
            if (getStart().isPresent()) {
                if (interval.getStart().isBefore(getStart().get())) {
                    return false;
                }
            }
            if (getEnd().isPresent()) {
                if (interval.getEnd().isAfter(getEnd().get())) {
                    return false;
                }
            }
            return true;
        }

        public DateTime getAllowedStart() {
            if (isRestricted()) {
                return getStart().get();
            } else {
                return START_OF_TIME;
            }
        }

        public String getMessage() {
            final String undefined = "undefined";
            final String start = getStart().isPresent() ? DateUtil.DTF_XML.print(getStart().get()) : undefined;
            final String end = getEnd().isPresent() ? DateUtil.DTF_XML.print(getEnd().get()) : undefined;
            return "Permitted interval [start=" + start +", end=" + end + ']';
        }
    }

    public static final String ITRAXX_VWD_MARKET_SYMBOL = "ITRAXX";

    private final Map<String, Selector> selectorAll, selectorIndex;

    {
        selectorAll = new HashMap<>();
        selectorAll.put("FTSE", HISTORICAL_EOD_DATA_INDICES_FTSE_MY);
        selectorAll.put("DJ", HISTORICAL_EOD_DATA_DJ);
        selectorAll.put("SP", HISTORICAL_EOD_DATA_SP);

        selectorIndex = new HashMap<>();
        selectorIndex.put("IT", HISTORICAL_EOD_DATA_INDICES_IT);
        selectorIndex.put("SIP", HISTORICAL_EOD_DATA_INDICES_SIP);
        selectorIndex.put("JB", HISTORICAL_EOD_DATA_INDICES_JB);
        selectorIndex.put("MY", HISTORICAL_EOD_DATA_INDICES_FTSE_MY);
    }

    private final EnumMap<Selector, Entitlement> periodEntitlement;

    {
        periodEntitlement = new EnumMap<>(Selector.class);
        periodEntitlement.put(HISTORICAL_EOD_DATA_1Y, Entitlement.HISTORICAL_1Y);
        periodEntitlement.put(HISTORICAL_EOD_DATA_3Y, Entitlement.HISTORICAL_3Y);
        periodEntitlement.put(HISTORICAL_EOD_DATA_5Y, Entitlement.HISTORICAL_5Y);
        periodEntitlement.put(HISTORICAL_EOD_DATA_10Y, Entitlement.HISTORICAL_10Y);
        periodEntitlement.put(HISTORICAL_EOD_DATA_20Y, Entitlement.HISTORICAL_20Y);
        periodEntitlement.put(HISTORICAL_EOD_DATA_30Y, Entitlement.HISTORICAL_30Y);
    }

    private boolean isIndexInstrument(String feedMarket, InstrumentTypeEnum type) {
        return selectorIndex.containsKey(feedMarket) && InstrumentTypeEnum.IND == type;
    }

    private boolean isStandardPoorsIndexInstrument(String feedMarket, InstrumentTypeEnum type) {
        return "SP".equals(feedMarket) && InstrumentTypeEnum.IND == type;
    }

    private boolean isAllowed(Profile profile, Selector selector) {
        return profile.isAllowed(selector);
    }

    /**
     * @return true if profile and quote are valid
     */
    private boolean checkArguments(Profile profile, Quote quote) {
        // EntitlementQuote throws UnsupportedOperationException
        if (profile == null || quote == null || quote.isNullQuote() || quote instanceof EntitlementQuote) {
            return false;
        }
        return true;
    }

    /**
     * @return allowed historic data start date
     */
    private Entitlement getEntitlementByMarket(Profile profile, Quote quote) {
        final String feedMarket = quote.getSymbolVwdfeedMarket();
        final InstrumentTypeEnum type = quote.getInstrument() == null ? null : quote.getInstrument().getInstrumentType();

        final Market market = quote.getMarket();
        final String symbolVwdFeed = market == null ? null : market.getSymbolVwdfeed();

        /*
         * Does this checkEntitlement for Itraxx quotes still work properly?
         * Usually quotes always of market FONDS (e.g. LU0290358653) not ITRAXX.
         */
        if (ITRAXX_VWD_MARKET_SYMBOL.equals(symbolVwdFeed)) {
            return isAllowed(profile, DZB_ITRAXX) ? Entitlement.ALL : Entitlement.ITRAXX_NOT_ENTITLED;
        }

        if (isStandardPoorsIndexInstrument(feedMarket, type) && isAllowed(profile, HISTORICAL_EOD_DATA_INDICES_SP)) {
            return Entitlement.FTSE_ENTITLED;
        }

        /*
         * If one of FTSE, DJ and SP restricted index quotes.
         */
        Selector selector;

        if (selectorAll.containsKey(feedMarket)) {
            selector = selectorAll.get(feedMarket);
        } else if (isIndexInstrument(feedMarket, type)) {
            selector = selectorIndex.get(feedMarket);
        } else {
            return Entitlement.ALL;
        }

        return isAllowed(profile, selector) ? Entitlement.FTSE_ENTITLED : Entitlement.FTSE_NOT_ENTITLED;
    }

    /**
     * @return allowed historic data start date
     */
    public Entitlement getEntitlement(Profile profile, Quote quote) {
        if (!checkArguments(profile, quote)) {
            return Entitlement.ALL;
        }

        final Entitlement marketEntitlement = getEntitlementByMarket(profile, quote);

        return periodEntitlement.entrySet().stream()
                .filter(s -> isAllowed(profile, s.getKey()))
                .map(Entry::getValue)
                .findFirst()
                .filter(p -> marketEntitlement.compareTo(p) > 0)
                .orElse(marketEntitlement);
    }
}
