/*
 * DelayProviderMdps.java
 *
 * Created on 06.03.2006 16:33:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.delay;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataVkeyOnly;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * A {@link de.marketmaker.istar.feed.delay.DelayProvider} that reads the delay configuration
 * from an input file. Each line in that file is either a comment (line starts with '#') or
 * it consists of 5 tokens separated by whitespace:<br>
 * <tt>rangeStart   rangeEnd   type   market   delayInMinutes</tt>
 * <ul>
 * <li>rangeStart and rangeEnd specify matches against a vendorkey's symbol, a value of '*'
 * denotes a wildcard. Thus, rangeStart="A", rangeEnd="*" would match GS0CCV but not 846900
 * as "8" is lexicographically smaller than the rangeStart "A".
 * <li>type is a vendorkey's type as in the SDMReader Feed (e.g., "6" for 6.846900.ETR)
 * <li>market is a vendorkey's market (e.g., "ETR" for 6.846900.ETR)
 * </ul>
 * To determine the delay for a record with a certain vendorkey,
 * this component tries to match the vendorkey with each of the delay specifications
 * for the vendorkey's market in the order as these specs appear in the input file.
 * As soon as a match is found, the respective delay is the delay for the record. If no
 * spec matches, the default delay is used.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class DelayProviderImpl implements DelayProvider, InitializingBean, VendorkeyFilter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** delay configuration */
    private Resource delayRules;

    private ActiveMonitor activeMonitor;

    /** delay for all records whose vendorkey is not included in delay file's spec */
    private int defaultDelay = -1;

    /** MarketDelay objects keyed by a market name (e.g., ETR for Xetra) */
    private AtomicReference<Map<ByteString, MarketDelay>> mappings =
            new AtomicReference<>();

    /** Delay spec for a single market */
    private class MarketDelay {
        /** items in same order as they appear in input file */
        private List<DelayItem> items = new ArrayList<>(4);


        public String toString() {
            return this.items.toString();
        }

        private DelayItem addIfAbsent(DelayItem item) {
            final String fs = item.filter.toString();
            for (int i = 0; i < items.size(); i++) {
                final DelayItem existing = items.get(i);
                if (fs.equals(existing.filter.toString())) {
                    if (existing.delayInSeconds < item.delayInSeconds) {
                        this.items.set(i, item); // use maximum delay
                        return existing;
                    }
                }
            }
            this.items.add(item);
            return null;
        }

        private int getDelay(Vendorkey v) {
            for (DelayItem item : this.items) {
                if (item.filter.test(v)) {
                    return item.delayInSeconds;
                }
            }
            return DelayProviderImpl.this.defaultDelay;
        }
    }

    /** A delay as specified in a single line of the delayFile */
    private static class DelayItem {
        private final VendorkeyFilter filter;

        /** delay in seconds for this spec */
        private final int delayInSeconds;

        public String toString() {
            return this.filter + "=" + this.delayInSeconds + "s";
        }

        static DelayItem create(String[] tokens) {
            final String from = getSpec(tokens[0]);
            final String to = getSpec(tokens[1]);
            final StringBuilder filterSpec = new StringBuilder("t:").append(tokens[2]);
            if (from != null) {
                filterSpec.append(" && ");
                if (from.equals(to)) {
                    filterSpec.append("^").append(from);
                }
                else {
                    filterSpec.append(">=").append(from).append(" && <=").append(to);
                }
            }

            VendorkeyFilter filter = VendorkeyFilterFactory.create(filterSpec.toString());

            try {
                return new DelayItem(
                        filter,
                        Integer.parseInt(tokens[4]) * DateTimeConstants.SECONDS_PER_MINUTE
                );
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private static String getSpec(final String token) {
            return "*".equals(token) ? null : token;
        }

        private DelayItem(VendorkeyFilter filter, int delayInSeconds) {
            this.filter = filter;
            this.delayInSeconds = delayInSeconds;
        }
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setDefaultDelay(int defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    public void setDelayRules(Resource delayRules) {
        this.delayRules = delayRules;
    }

    public void afterPropertiesSet() throws Exception {
        if (delayRules == null) {
            throw new IllegalStateException("property delay file not set");
        }
        if (this.activeMonitor != null) {
            initResourceMonitor();
        }
        updateDelays();
        if (this.mappings.get() == null || this.mappings.get().isEmpty()) {
            throw new IllegalStateException("no delay rules");
        }
    }

    private void initResourceMonitor() throws Exception {
        final FileResource fileResource = new FileResource(this.delayRules.getFile());
        this.activeMonitor.addResource(fileResource);
        fileResource.addPropertyChangeListener(evt -> updateDelays());
    }

    @ManagedOperation(description = "reload delay rules")
    public void updateDelays() {
        Scanner s = null;

        try {
            s = new Scanner(this.delayRules.getInputStream());
            scanInput(s);
            this.logger.info("<updateDelays> finished: " + mappingsAsString());
        } catch (Exception e) {
            this.logger.error("<updateDelays> failed", e);
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }

    private String mappingsAsString() {
        StringBuilder sb = new StringBuilder(2000);
        final Map<ByteString, MarketDelay> map = this.mappings.get();

        final List<ByteString> keys = new ArrayList<>(map.keySet());
        keys.sort(null);

        for (ByteString key : keys) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(key).append(map.get(key));
        }
        return sb.toString();
    }


    protected void scanInput(Scanner s) throws Exception {
        int n = 0;
        int tmpMaxDelay = 0;
        final Pattern p = Pattern.compile("\\s+"); // used to split on whitespace
        final Map<ByteString, MarketDelay> m = new ConcurrentHashMap<>();

        while (s.hasNextLine()) {
            final String line;
            if (m.isEmpty()) {
                // item to ensure we have delayed data for heartbeat symbol
                line = "CLOCK CLOCK 1 VWD 15";
            }
            else {
                n++;
                line = s.nextLine().trim();
            }
            if (!StringUtils.hasText(line) || line.startsWith("#")) {
                continue;
            }

            final String[] tokens = p.split(line);
            if (tokens.length != 5) {
                this.logger.warn("<scanInput> != 5 tokens (" + Arrays.toString(tokens) + ") in line " + n + ": " + line);
                continue;
            }

            try {
                Integer.parseInt(tokens[2]);
            } catch (NumberFormatException e) {
                tokens[2] = MdpsTypeMappings.toNumericType(tokens[2]);
                if (tokens[2] == null) {
                    this.logger.warn("<scanInput> invalid sectype in line " + n + ": " + line);
                    continue;
                }
            }

            final DelayItem item = DelayItem.create(tokens);
            if (item == null) {
                this.logger.warn("<scanInput> invalid data in line " + n + ": " + line);
                continue;
            }

            if (item.delayInSeconds == this.defaultDelay) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<scanInput> ignoring " + item + ", it uses defaultDelay");
                }
                continue;
            }

            final ByteString market = new ByteString(tokens[3]);
            MarketDelay marketDelay = m.get(market);
            if (marketDelay == null) {
                marketDelay = new MarketDelay();
                m.put(market, marketDelay);
            }

            final DelayItem existing = marketDelay.addIfAbsent(item);
            if (existing != null) {
                this.logger.warn("<scanInput> conflict for market " + market + ": " + item + " vs. " + existing
                        + ": using " + Math.max(item.delayInSeconds, existing.delayInSeconds) + "s delay for both");
            }
            tmpMaxDelay = Math.max(tmpMaxDelay, item.delayInSeconds);
        }

        this.mappings.set(m);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vendorkey", description = "vendorkey")
    })
    public int getDelayInSeconds(String vkey) {
        return getDelayInSeconds(new FeedDataVkeyOnly(VendorkeyVwd.getInstance(new ByteString(vkey))));
    }

    @Override
    public int getDelayInSeconds(FeedData data) {
        // using data.getMarket().getName() instead of data.getVendorkey().getMarketName()
        // avoids finding and creating a ByteString for the market name
        final MarketDelay marketDelay = getMarketDelay(data.getMarket().getName());
        return (marketDelay != null) ? marketDelay.getDelay(data.getVendorkey()) : this.defaultDelay;
    }

    @Override
    public int getDelayInSeconds(Vendorkey key) {
        final MarketDelay marketDelay = getMarketDelay(key.getMarketName());
        return (marketDelay != null) ? marketDelay.getDelay(key) : this.defaultDelay;
    }

    private MarketDelay getMarketDelay(ByteString marketName) {
        return this.mappings.get().get(marketName);
    }

    /**
     * If this component has a reference to a delayProvider and the delay for data is 0, then
     * the delayed data is stored in the realtime snap as well, so we return true regardless of
     * the realtime parameter. Otherwise, we return realtime.
     */
    public Boolean isRealtime(FeedData data, boolean realtime) {
        if (realtime) {
            return Boolean.TRUE;
        }
        final int delayInSeconds = getDelayInSeconds(data);
        if (delayInSeconds < 0) {
            return null;
        }
        return delayInSeconds == 0 ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean test(Vendorkey vkey) {
        final Map<ByteString, MarketDelay> m = this.mappings.get();
        final DelayProviderImpl.MarketDelay marketDelay = m.get(vkey.getMarketName());
        return marketDelay != null && marketDelay.getDelay(vkey) > 0;
    }

    public static void main(String[] args) throws Exception {
        final DelayProviderImpl dp = new DelayProviderImpl();
        dp.setDelayRules(new FileSystemResource(new File(LocalConfigProvider.getProductionBaseDir(),
                "var/data/vwd/delay/DelayRules.txt")));
        dp.afterPropertiesSet();

        for (int i = 1; i < args.length; i++) {
            int delayInSeconds = dp.getDelayInSeconds(args[i]);
            System.out.println(args[i] + ": " + delayInSeconds + "s");
        }
    }
}
