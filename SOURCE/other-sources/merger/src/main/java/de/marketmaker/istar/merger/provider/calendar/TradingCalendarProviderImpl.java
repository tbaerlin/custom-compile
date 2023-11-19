/*
 * TradingCalendarProviderImpl.java
 *
 * Created on 11.09.2006 10:49:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.calendar;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Market;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.Constants;
import org.springframework.util.StringUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maintains information about holidays and trading hours for various markets.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TradingCalendarProviderImpl implements InitializingBean,
        TradingCalendarProvider, MarketHolidayProvider {

    private static final BitSet DEFAULT_TRADING_DAYS = new BitSet();
    static {
        DEFAULT_TRADING_DAYS.set(DateTimeConstants.MONDAY, DateTimeConstants.SATURDAY);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Matcher timeMatcher =
            Pattern.compile("(\\d{1,2})(:(\\d{2})(:(\\d{2})(\\.((\\d{1,3})))?)?)?").matcher("");

    /**
     * Constants instance for DateTimeConstants
     */
    private static final Constants constants = new Constants(DateTimeConstants.class);

    final AtomicReference<Map<Long, TradingCalendar>> calendarRef = new AtomicReference<>();

    final AtomicReference<Map<Integer, int[]>> holidayRef = new AtomicReference<>();

    private File calendarSpec;

    private File holidaySpec;

    private final TradingCalendar defaultTradingCalendar;

    private ActiveMonitor activeMonitor;

    public TradingCalendarProviderImpl() {
        final MarketTradingCalendar tmp =
                new MarketTradingCalendar(MarketTradingCalendar.DEFAULT_CALENDAR_MARKET_ID, DEFAULT_TRADING_DAYS,
                        DateTimeZone.getDefault(), this);
        tmp.addRegularTradingSession(new LocalTime(8, 0), new LocalTime(20, 0));
        defaultTradingCalendar = tmp;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setCalendarSpec(File calendarSpec) {
        this.calendarSpec = calendarSpec;
    }

    public void setHolidaySpec(File holidaySpec) {
        this.holidaySpec = holidaySpec;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.activeMonitor != null) {
            registerCalendarSpec();
            registerHolidaySpec();
        }

        readCalendarSpec();
        if (this.calendarRef.get() == null) {
            throw new Exception("invalid calendarSpec");
        }
        readHolidaySpec();
        if (this.holidayRef.get() == null) {
            throw new Exception("invalid holidaySpec");
        }
    }

    private void registerCalendarSpec() throws Exception {
        final FileResource resource = new FileResource(this.calendarSpec);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readCalendarSpec();
            }
        });
        this.activeMonitor.addResource(resource);
    }

    private void registerHolidaySpec() throws Exception {
        final FileResource resource = new FileResource(this.holidaySpec);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readHolidaySpec();
            }
        });
        this.activeMonitor.addResource(resource);
    }

    private void readHolidaySpec() {
        try {
            final HolidayReader hr = new HolidayReader();
            this.holidayRef.set(hr.read(this.holidaySpec));
            this.logger.info("<readHolidaySpec> succeeded");
        } catch (Exception e) {
            this.logger.info("<readHolidaySpec> failed", e);
        }
    }

    public boolean isHoliday(LocalDate date, long marketid) {
        final int[] days = this.holidayRef.get().get((int) marketid);
        return (days != null) && Arrays.binarySearch(days, DateUtil.toYyyyMmDd(date)) >= 0;
    }

    private void readCalendarSpec() {
        final Map<Long, TradingCalendar> tmp = new HashMap<>();

        InputStream is = null;
        try {
            is = new FileInputStream(this.calendarSpec);
            final SAXBuilder saxBuilder = new SAXBuilder();
            final Document document = saxBuilder.build(is);
            //noinspection unchecked
            final List<Element> elements = document.getRootElement().getChildren("element");
            for (final Element element : elements) {
                final Long marketid = Long.parseLong(element.getChildTextTrim("marketid"));

                final BitSet regularTradingDays =
                        readRegularTradingDays(element.getChild("daysWithoutTrading"));

                final String zoneId = element.getChildTextTrim("timezone");
                final DateTimeZone zone;
                try {
                    zone = DateTimeZone.forID(zoneId);
                } catch (IllegalArgumentException e) {
                    this.logger.error("<readCalendarSpec> illegal time zone id '" + zoneId + "'");
                    throw new Exception(e);
                }

                final MarketTradingCalendar tc =
                        new MarketTradingCalendar(marketid, regularTradingDays, zone, this);
                tmp.put(marketid, tc);

                readRegularTradingSessions(element, tc);
                readQuoteTradingSessions(element, tc);
            }
            this.calendarRef.set(tmp);
            this.logger.info("<readCalendarSpec> succeeded");
        }
        catch (Exception e) {
            this.logger.error("<readCalendarSpec> failed", e);
        } finally {
            IoUtils.close(is);
        }
    }

    private void readRegularTradingSessions(Element element, MarketTradingCalendar tc) throws Exception {
        final Element rts = element.getChild("regularTradingSessions");
        final List<Element> sessions = rts.getChildren("session");
        for (Element session : sessions) {
            final LocalTime[] times = readSession(session);
            tc.addRegularTradingSession(times[0], times[1]);
        }

    }

    private void readQuoteTradingSessions(Element element, MarketTradingCalendar tc) throws Exception {
        final List<Element> children = element.getChildren("quoteTradingSessions");
        for (Element qts : children) {
            final Collection<Long> qids = getQuoteIds(qts);
            final List<Element> sessions = qts.getChildren("session");
            for (Element session : sessions) {
                final LocalTime[] times = readSession(session);
                for (Long qid: qids) {
                    tc.addQuoteTradingSession(qid, times[0], times[1]);
                }
            }
        }
    }

    private Collection<Long> getQuoteIds(Element qts) {
        // old style: single quoteid as attribute
        final String qid = qts.getAttributeValue("quoteid");
        if (StringUtils.hasText(qid)) {
            return Collections.singleton(Long.parseLong(qid));
        }
        // new style: quote sub-elements with id attribute
        final Collection<Long> result = new HashSet<>();
        final List<Element> quotes = qts.getChildren("quote");
        for (Element quote : quotes) {
            result.add(Long.parseLong(quote.getAttributeValue("id")));
        }
        return result;
    }

    private LocalTime[] readSession(Element session) throws Exception {
        return new LocalTime[]{
                parseTimeOfDay(session.getChildTextTrim("start")),
                parseTimeOfDay(session.getChildTextTrim("end"))
        };
    }

    private LocalTime parseTimeOfDay(String s) throws Exception {
        timeMatcher.reset(s);
        if (!timeMatcher.matches()) {
            throw new Exception("Invalid time: " + s);
        }
        final int hh = Integer.parseInt(timeMatcher.group(1));
        final int mm = parseInt(timeMatcher.group(3));
        final int ss = parseInt(timeMatcher.group(5));
        final int ms = parseInt(timeMatcher.group(7));

        return new LocalTime(hh, mm, ss, ms);
    }

    private int parseInt(String r) {
        return StringUtils.hasText(r) ? Integer.parseInt(r) : 0;
    }

    private BitSet readRegularTradingDays(Element dwt) {
        if (dwt == null) {
            return DEFAULT_TRADING_DAYS;
        }
        final BitSet result = new BitSet();
        result.set(DateTimeConstants.MONDAY, DateTimeConstants.SUNDAY + 1);

        final List<Element> days = dwt.getChildren("dayOfWeek");
        for (Element day : days) {
            result.set(constants.asNumber(day.getTextTrim()).intValue(), false);
        }
        return result;
    }


    public TradingCalendar calendar(Market m) {
        final Map<Long, TradingCalendar> calendarsByMarketId = this.calendarRef.get();
        final TradingCalendar result = calendarsByMarketId.get(m.getId());
        return (result != null) ? result : defaultTradingCalendar;
    }

    private static class HolidayReader extends IstarMdpExportReader<Map<Integer, int[]>> {
        private final Map<Integer, int[]> values = new HashMap<>();
        private final Map<Integer, int[]> cache = new HashMap<>();

        private int[] dates = new int[100];

        private int n;

        private int currentMarket;

        @Override
        protected void handleRow() {
            final int marketId = getInt("MARKET");
            if (currentMarket != marketId) {
                if (currentMarket > 0) {
                    addDates();
                }
                this.currentMarket = marketId;
                this.n = 0;
            }
            if (n == dates.length) {
                dates = Arrays.copyOf(dates, dates.length * 2);
            }
            this.dates[n++] = DateUtil.toYyyyMmDd(getDateTime("HDATE"));
        }

        @Override
        protected void endDocument() {
            addDates();
        }

        private void addDates() {
            Arrays.sort(this.dates, 0, n);
            final int[] value = Arrays.copyOf(this.dates, n);
            final int hash = Arrays.hashCode(value);
            if (cache.containsKey(hash) && Arrays.equals(value, cache.get(hash))) {
                this.values.put(this.currentMarket, cache.get(hash));
            }
            else {
                this.cache.put(hash, value);
                this.values.put(this.currentMarket, value);
            }
        }

        @Override
        protected Map<Integer, int[]> getResult() {
            return values;
        }
    }
}
