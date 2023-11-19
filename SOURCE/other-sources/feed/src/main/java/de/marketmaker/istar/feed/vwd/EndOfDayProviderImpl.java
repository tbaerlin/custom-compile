/*
 * EndOfDayProvider.java
 *
 * Created on 29.01.2009 15:20:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.marketmaker.istar.feed.VendorkeyFilterFactory.ACCEPT_ALL;
import static java.lang.Integer.toBinaryString;

/**
 * @see EndOfDayProvider
 */
public class EndOfDayProviderImpl implements InitializingBean, EndOfDayProvider,
        ApplicationContextAware {

    static class Item {
        final MarketItems items;

        final VendorkeyFilter filter;

        final LocalTime eodStart;

        final LocalTime eodEnd;

        final int days;

        // effectively final, object will be published only after this has been set
        private VendorkeyFilter notFilter;

        // manipulated by parser thread and scheduler thread in EndOfDayScheduler
        volatile Boolean betweenStartAndEnd;

        private Item(MarketItems items, VendorkeyFilter filter, LocalTime eodStart, LocalTime eodEnd, int days) {
            this.items = items;
            this.filter = filter;
            this.eodStart = eodStart;
            this.eodEnd = eodEnd;
            this.days = days;
        }

        boolean isAcceptable(Vendorkey vkey) {
            return this.filter.test(vkey)
                    && (this.notFilter == null || !this.notFilter.test(vkey));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append(getMarketName()).append("[").append(formatFilter())
                    .append(", ").append(formatTime(this.eodStart));
            if (this.eodEnd == this.eodStart) {
                sb.append("@");
            }
            else if (this.eodEnd != null) {
                sb.append("-").append(formatTime(this.eodEnd));
            }
            return sb.append(", ").append(formatDays())
                    .append(", ").append(formatBetween(this.betweenStartAndEnd))
                    .append("]").toString();
        }

        private char formatBetween(Boolean b) {
            return (b != null) ? (b ? 'Y' : 'N') : '0';
        }

        private String formatTime(LocalTime lt) {
            return lt.getSecondOfMinute() == 0 ? lt.toString("HH:mm") : lt.toString();
        }

        private String formatDays() {
            StringBuilder ds = new StringBuilder(toBinaryString(this.days)).reverse();
            ds.append("-------");
            return ds.substring(1, 8).replace('0', '-').replace('1', '*');
        }

        String formatFilter() {
            if (this.notFilter == null) {
                return this.filter.toString();
            }
            return this.filter.toString() + " w/o " + this.notFilter;
        }

        DateTimeZone getZone() {
            return this.items.getZone();
        }

        ByteString getMarketName() {
            return this.items.market;
        }

        boolean isScheduledForDay(int day) {
            return (this.days & (1 << day)) != 0;
        }

        private boolean isSubsetOf(Item o) {
            return Objects.equals(this.eodStart, o.eodStart)
                    && Objects.equals(this.eodEnd, o.eodEnd)
                    && this.days == o.days
                    && isSubfilterOf(o);
        }

        private boolean isSubfilterOf(Item o) {
            if (o.filter == ACCEPT_ALL) {
                return true;
            }
            // remove parens and spaces to that "t:4" can be recognized as prefix of "((t:4) && (^XY.))"
            String f1 = canonical(this.filter);
            String f2 = canonical(o.filter);
            return f1.startsWith(f2);
        }

        private String canonical(VendorkeyFilter f) {
            return f.toString().replaceAll("[\\(\\) ]", "");
        }
    }

    static class MarketItems implements InitializingBean {
        private static final Logger logger = LoggerFactory.getLogger(MarketItems.class);

        private final ByteString market;

        private final List<Item> items = new ArrayList<>();

        private final DateTimeZone zone;

        private MarketItems(ByteString market, DateTimeZone zone) {
            this.market = market;
            this.zone = zone;
        }

        @Override
        public void afterPropertiesSet() {
            if (items.size() < 2) {
                return;
            }
            removeSubsetItems();
            initNotFilters();
        }

        protected void initNotFilters() {
            int n = this.items.size();
            for (int i = 1; i < n; i++) {
                Item item = this.items.get(i);
                List<VendorkeyFilter> filters = this.items.subList(0, i).stream()
                        .filter((other) -> other.isSubfilterOf(item))
                        .map((other) -> other.filter)
                        .collect(Collectors.toList());
                if (filters.size() > 0) {
                    item.notFilter = VendorkeyFilterFactory.orJoin(filters);
                }
            }
        }

        protected void removeSubsetItems() {
            int n = this.items.size() - 1;
            for (int i = 0; i < n; ) {
                Item item = this.items.get(i);
                Optional<Item> superItem
                        = this.items.subList(i + 1, n + 1).stream().filter(item::isSubsetOf).findAny();
                if (superItem.isPresent()) {
                    logger.debug("<removeSubsetItems> " + item + " << " + superItem.get());
                    this.items.remove(i);
                    n--;
                }
                else {
                    i++;
                }
            }
        }

        @Override
        public String toString() {
            return "MarketItems[" + this.market + ", " + this.zone + ", " + this.items + "]";
        }

        public DateTimeZone getZone() {
            return zone;
        }

        List<Item> getItems() {
            return new ArrayList<>(this.items);
        }

        private void add(VendorkeyFilter filter, LocalTime eodStart, LocalTime eodEnd, int days) {
            final Item item = new Item(this, filter, eodStart, eodEnd, days);
            final String fstr = filter.toString();
            // put more specific filters ahead of less specific ones by comparing lengths
            // as least specific is '*', then 't:i' and most specific 't:i && ^symbol.'
            for (int i = 0; i < items.size(); i++) {
                if (fstr.length() > this.items.get(i).filter.toString().length()) {
                    this.items.add(i, item);
                    return;
                }
            }
            this.items.add(item);
        }

        private DateTime getEodFilterStart(Vendorkey vkey, final DateTime now) {
            final Item item = firstMatchFor(vkey);
            if (item != null) {
                return getEodFilterStart(item, now);
            }
            // eod but no matching rule - we would return a default eod filter, so we
            // also return a default eod end
            return toDateTimeWithTimeInZone(now, LocalTime.MIDNIGHT); // todo: is this ok?
        }

        private DateTime getEodFilterStart(Item item, DateTime now) {
            LocalTime lt = eodStartedAt(item, now);
            if (lt == null) {
                return null;
            }

            return toDateTimeWithTimeInZone(now, lt);
        }

        private DateTime toDateTimeWithTimeInZone(DateTime now, LocalTime lt) {
            DateTime result = now.withZone(this.zone)
                    .withTime(lt.getHourOfDay(), lt.getMinuteOfHour(), lt.getSecondOfMinute(), 0)
                    .withZone(now.getZone());
            return result.isAfter(now) ? result.minusDays(1) : result; // todo: is this ok?
        }

        private Boolean isEodFilterNecessary(Vendorkey vkey, final DateTime now) {
            final Item item = firstMatchFor(vkey);
            return (item != null) ? (eodStartedAt(item, now) != null) : null;
        }

        private Item firstMatchFor(Vendorkey vkey) {
            for (Item item : this.items) {
                // use item.filter directly as we check items in order (an item's notFilter is
                // composed of filters of preceding items, so it can be skipped here)
                if (item.filter.test(vkey)) {
                    return item;
                }
            }
            return null;
        }

        /**
         * if now is a time that makes it necessary to add an eod filter, the LocalTime at which
         * that eod period began is returned; if no eod filter is necessary, null is returned.
         */
        private LocalTime eodStartedAt(Item item, DateTime now) {
            final LocalTime localNow = now.withZone(this.zone).toLocalTime();

            if (item.eodStart == item.eodEnd) {
                return null;
            }
            if (item.eodEnd != null && item.eodStart.isAfter(item.eodEnd)) {
                return localNow.isBefore(item.eodStart) && localNow.isAfter(item.eodEnd) ? item.eodEnd : null;
            }

            if (localNow.isBefore(item.eodStart)) {
                return LocalTime.MIDNIGHT;
            }
/* TODO: disabled temporarily to fix issue in R-40339 (NIKKEI EoD) -- a more general solution is needed
            if (item.rtTo != null && now.isAfter(inLocalTimeZone(item.rtTo, now))) {
                return true;
            }
*/
            return null;
        }
    }

    private static Pattern WHITESPACE_PATTERN = Pattern.compile("\\p{javaWhitespace}+");

    public static final LocalTime START_OF_DAY = new LocalTime(0, 0);

    public static final LocalTime END_OF_DAY = new LocalTime(23, 59);

    private static final String[] DAYS = new String[]{"MO", "TU", "WE", "TH", "FR", "SA", "SU"};

    private ApplicationContext applicationContext;

    private ActiveMonitor activeMonitor;

    private File eodFile;

    private boolean adaptTimes = true;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AtomicReference<Map<ByteString, MarketItems>> mappings =
            new AtomicReference<>(null);

    private AtomicReference<Map<ByteString, DateTimeZone>> timezones =
            new AtomicReference<>(Collections.<ByteString, DateTimeZone>emptyMap());

    public void setAdaptTimes(boolean adaptTimes) {
        this.adaptTimes = adaptTimes;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        readEodFile();
        if (this.activeMonitor != null) {
            initMonitor();
        }
    }

    @Override
    public EndOfDayFilter getEodFilter(Vendorkey vkey) {
        return getEodFilter(vkey, new DateTime());
    }

    @Override
    public DateTime getEodFilterStart(Vendorkey vkey, DateTime now) {
        final MarketItems marketItems = getMarketItems(vkey);
        if (marketItems == null) {
            this.logger.debug("<getEodFilterStart> no marketItems for '" + vkey.getMarketName() + "'");
            return now.withTimeAtStartOfDay();
        }
        return marketItems.getEodFilterStart(vkey, now);
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setEodFile(File eodFile) {
        this.eodFile = eodFile;
    }

    @Override
    public String toString() {
        return getMappings().toString();
    }

    @Override
    public EndOfDayFilter getEodFilter(Vendorkey vkey, DateTime now) {
        final MarketItems marketItems = getMarketItems(vkey);
        if (marketItems == null) {
            this.logger.debug("<getEodFilter> no marketItems for '" + vkey.getMarketName() + "'");
            return EndOfDayFilter.get(getTimeZone(vkey.getMarketName().toString()));
        }
        final Boolean result = marketItems.isEodFilterNecessary(vkey, now);
        if (result == null) {
            this.logger.warn("<getEodFilter> no eod def for '" + vkey.toByteString() + "'");
        }
        return (result != Boolean.FALSE) ? EndOfDayFilter.get(marketItems.getZone()) : null;
    }

    protected MarketItems getMarketItems(Vendorkey vkey) {
        final Map<ByteString, MarketItems> map = this.mappings.get();
        return map.get(vkey.getMarketName());
    }

    public DateTimeZone getTimeZone(String vwdmarket) {
        final Map<ByteString, DateTimeZone> map = this.timezones.get();
        return map.get(new ByteString(vwdmarket));
    }


    private VendorkeyFilter createFilter(String type, String symbol) {
        final StringBuilder sb = new StringBuilder();
        if (!"*".equals(type)) {
            sb.append("t:").append(toNumericType(type));
        }
        if (!"*".equals(symbol)) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            sb.append("^").append(symbol).append(".");
        }
        if (sb.length() == 0) {
            return ACCEPT_ALL;
        }
        return VendorkeyFilterFactory.create(sb.toString());
    }

    private Map<ByteString, MarketItems> doReadEodFile(
            Map<ByteString, DateTimeZone> zones) throws Exception {
        final Map<ByteString, MarketItems> result = new HashMap<>();
        ByteString lastMarket = null;
        MarketItems lastItems = null;
        LocalTime eodStart = null;

        try (Scanner s = new Scanner(this.eodFile)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine();
                if (line.startsWith("#") || !line.contains("EndOfDay")) {
                    continue;
                }

                String[] tokens = WHITESPACE_PATTERN.split(line);
                final ByteString market = new ByteString(tokens[0]);

                final String type = tokens[1];
                if ("OT".equals(type)) { // ignore OT entries
                    continue;
                }
                final String symbol = tokens[2];
                final int days = parseDays(tokens[4]);
                final String time = tokens[5];
                final String action = tokens[6];

                if (lastItems == null || !market.equals(lastMarket)) {
                    if (lastItems != null && !lastItems.items.isEmpty()) {
                        result.put(lastMarket, lastItems);
                    }
                    lastItems = new MarketItems(market, zones.get(market));
                    lastMarket = market;
                    eodStart = null;
                }

                if ("EndOfDayStart".equals(action)) {
                    eodStart = toLocalTime(time);
                }
                else if ("EndOfDay".equals(action)) {
                    if (eodStart == null) {
                        add(lastItems, createFilter(type, symbol), toLocalTime(time), null, days);
                    }
                }
                else if ("EndOfDayEnd".equals(action)) {
                    add(lastItems, createFilter(type, symbol), eodStart, toLocalTime(time), days);
                    eodStart = null;
                }
            }
            if (lastItems != null && !lastItems.items.isEmpty()) {
                result.put(lastMarket, lastItems);
            }
            result.values().stream().forEach(MarketItems::afterPropertiesSet);
            return result;
        }
    }

    private void add(MarketItems items, VendorkeyFilter filter, LocalTime eodStart, LocalTime eodEnd, int days) {
        if (!this.adaptTimes) {
            items.add(filter, eodStart, eodEnd, days);
            return;
        }

        if (eodEnd != null && eodEnd.plusMinutes(1).equals(eodStart)) {
            items.add(filter, eodStart, eodStart, days);
        }
        else if (eodEnd == null && START_OF_DAY.equals(eodStart)) {
            // for some german markets etc. 00:00 means next day. for now, we always filter,
            // yielding older than necessary results between midnight and roll time
            items.add(filter, eodStart.minusMillis(1), null, days);
        }
        else if (eodEnd == null || END_OF_DAY.equals(eodEnd)) {
            items.add(filter, eodStart, null, days);
        }
        else {
            items.add(filter, eodStart, eodEnd, days);
        }
    }

    /**
     * @param spec range of days (e.g., MO-FR) or a single day (e.g., WE)
     * @return int in which a bit is set for each day in <tt>spec</tt>, for which the bits at
     * positions {@link org.joda.time.DateTimeConstants#MONDAY}
     * to {@link org.joda.time.DateTimeConstants#SUNDAY} are used.
     */
    private int parseDays(String spec) {
        String[] fromTo = spec.split("-");
        int result = 0;
        int i = getDayIndex(fromTo[0]);
        for (; ; ) {
            result |= (1 << (i + 1)); // +1 as MONDAY is 1, MO is at pos 0
            if (fromTo.length < 2 || DAYS[i].equals(fromTo[1])) {
                break;
            }
            i = ((i + 1) % DAYS.length);
        }
        return result;
    }

    private int getDayIndex(String day) {
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equals(day)) {
                return i;
            }
        }
        throw new IllegalArgumentException(day);
    }

    private Map<ByteString, DateTimeZone> doReadTimezones() throws Exception {
        return Files.lines(this.eodFile.toPath())
                .map(WHITESPACE_PATTERN::split)
                .filter((t) -> t.length > 3 && t[0].matches("[A-Z]+"))
                .collect(Collectors.toMap((t) -> new ByteString(t[0]),
                        (t) -> DateTimeZone.forID(t[3]), (v1, v2) -> v1));
    }

    Map<ByteString, MarketItems> getMappings() {
        return new TreeMap<>(this.mappings.get());
    }

    Item getItem(Vendorkey vkey) {
        MarketItems marketItems = getMarketItems(vkey);
        return (marketItems == null) ? null : marketItems.firstMatchFor(vkey);
    }

    private void initMonitor() throws Exception {
        final FileResource fileResource = new FileResource(this.eodFile);
        fileResource.addPropertyChangeListener(evt -> readEodFile());
        this.activeMonitor.addResource(fileResource);
    }

    private void readEodFile() {
        final boolean hadMappings = hasMappings();
        try {
            final Map<ByteString, DateTimeZone> t = doReadTimezones();
            this.timezones.set(t);

            final Map<ByteString, MarketItems> m = doReadEodFile(t);
            this.mappings.set(m);

            if (hadMappings && this.applicationContext != null) {
                this.applicationContext.publishEvent(new EndOfDayRulesChangedEvent(this));
            }
            this.logger.info("<readEodFile> succeeded");
        } catch (Exception e) {
            if (hasMappings()) {
                throw new RuntimeException(e);
            }
            this.logger.error("<readEodFile> failed", e);
        }
    }

    private boolean hasMappings() {
        return this.mappings.get() != null;
    }

    private LocalTime toLocalTime(String time) {
        final int hh = Integer.parseInt(time.substring(0, time.indexOf(':')));
        final int mm = Integer.parseInt(time.substring(time.indexOf(':') + 1));
        return new LocalTime(hh, mm);
    }

    private String toNumericType(String type) {
        final String nType = MdpsTypeMappings.toNumericType(type);
        if (nType == null) {
            if ("OT".equals(type)) {
                return "0";
            }
            throw new IllegalArgumentException("Unknown type: " + type);
        }
        return nType;
    }

    public static void main(String[] args) throws Exception {
        final EndOfDayProviderImpl eodp = new EndOfDayProviderImpl();
        eodp.setEodFile(new File(LocalConfigProvider.getProductionBaseDir(),
                "var/data/web/exchange_time_schedule.cfg"));
        eodp.afterPropertiesSet();

        System.out.println(eodp);
        String vk = "1.xxx.Q";
        VendorkeyVwd vkey = VendorkeyVwd.getInstance(vk);
        System.out.println(vk);
        for (int hh = 0; hh < 24; hh++) {
            for (int mm = 0; mm < 46; mm += 15) {
                DateTime dateTime = new LocalTime(hh, mm).toDateTimeToday();
                System.out.println(dateTime + ": fields to be deleted: " + eodp.getEodFilter(vkey, dateTime));
                System.out.println(dateTime + ": previous eod end    : " + eodp.getEodFilterStart(vkey, dateTime));
            }
        }

        System.out.println(new DateTime() + ": fields to be deleted: " + eodp.getEodFilter(vkey, new DateTime()));
    }
}
