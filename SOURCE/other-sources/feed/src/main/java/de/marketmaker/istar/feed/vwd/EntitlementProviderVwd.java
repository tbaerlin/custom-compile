/*
 * EntitlementProvider.java
 *
 * Created on 22.03.2006 14:02:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.EntitlementDp2;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataVkeyOnly;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VendorkeyFilter;

import static de.marketmaker.istar.domain.data.PriceQuality.NOT_NONE;
import static de.marketmaker.istar.domain.data.PriceQuality.REALTIME_OR_DELAYED;

/**
 * Parses information about entitlement field groups and entitlement rules so that
 * it can be queried for the entitlement of a certain field for a certain vendorkey.
 * If this class is connected to a FeedDataRepository, it can also be used to query
 * all vendorkeys for which a certain entitlement may be used.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class EntitlementProviderVwd implements InitializingBean, EntitlementProvider {
    public static final String KAG_MARKET_ENTITLEMENT = "9999";

    public static int DEFAULT_ENTITLEMENT = 0;

    private static final int MIN_NUM_MARKETS = 400;

    private final static VwdEntitlement EMPTY_ENTITLEMENTS = new VwdEntitlement(null);

    private final static long KAG_MARKET_ID = 226;

    private static final BitSet ALWAYS_ALLOWED_FIELDS = new BitSet();

    private static final EntitlementDp2 KAG_MARKET_ENTITLEMENTS = new EntitlementDp2();

    static {
        KAG_MARKET_ENTITLEMENTS.setEntitlements(KeysystemEnum.VWDFEED, new String[]{KAG_MARKET_ENTITLEMENT});

        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.ADF_52_W_Hoch.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.ADF_52_W_Hoch_Datum.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.ADF_52_W_Tief.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.ADF_52_W_Tief_Datum.id());

        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.ADF_TIMEOFARR.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.ADF_DATEOFARR.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Bezahlt_Datum.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Boersenzeit.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Created.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Deleted.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_NAV_Vorvortag.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Schluss_Vorvortag.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Schluss_Vorvortagesdatum.id());
        ALWAYS_ALLOWED_FIELDS.set(VwdFieldDescription.MMF_Updated.id());
    }


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File entitlementFieldGroups;

    private File entitlementRules;

    private Map<String, List<String>> allowedEodFieldsByUser = new HashMap<>();

    private ActiveMonitor activeMonitor;

    private EndOfDayProvider endOfDayProvider;

    private int minNumMarkets = MIN_NUM_MARKETS;

    /**
     * MarketEntitlements objects keyed by a market name (e.g., ETR for Xetra)
     */
    private volatile Map<ByteString, MarketEntitlements> mappings = Collections.emptyMap();

    private static class VwdEntitlement implements Entitlement {
        private final String[] entitlements;

        private VwdEntitlement(String[] entitlements) {
            this.entitlements = entitlements;
        }

        public String[] getEntitlements(KeysystemEnum id) {
            return (id == KeysystemEnum.VWDFEED) ? this.entitlements : null;
        }

        public String toString() {
            return "VwdEntitlement[" + Arrays.toString(this.entitlements) + "]";
        }
    }

    static class FieldGroup {
        final String name;

        final BitSet fields;

        FieldGroup(String name) {
            this.name = name;
            this.fields = new BitSet(VwdFieldDescription.length());
        }

        private FieldGroup(String name, BitSet fields) {
            this.name = name;
            this.fields = fields;
        }

        FieldGroup withName(String name) {
            return new FieldGroup(name, this.fields);
        }

        void set(int fieldid) {
            this.fields.set(fieldid);
        }

        private boolean isSet(int fieldid) {
            return this.fields.get(fieldid);
        }

        public String toString() {
            return this.name + this.fields;
        }
    }

    /**
     * An entitlement
     */
    static class EntitlementItem {
        private final VendorkeyFilter filter;

        private final FieldGroup fieldGroup;

        private final int entitlement;

        EntitlementItem(VendorkeyFilter filter, FieldGroup fieldGroup, int entitlement) {
            this.filter = filter;
            this.fieldGroup = fieldGroup;
            this.entitlement = entitlement;
        }

        public String toString() {
            return "EntitlementItem[" + this.entitlement + ", " + this.filter + ", " + this.fieldGroup.name + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EntitlementItem that = (EntitlementItem) o;

            return this.entitlement == that.entitlement
                    && this.fieldGroup == that.fieldGroup
                    && this.filter == that.filter;
        }

        @Override
        public int hashCode() {
            return 31 * (31 * filter.hashCode() + fieldGroup.hashCode()) + entitlement;
        }
    }

    /**
     * Contains entitlement rules for all vendorkeys of a certain market
     */
    static class MarketEntitlements {
        /**
         * items in same order as they appear in input file but grouped by filter, that is items
         * with the same filter follow each other.
         */
        private List<EntitlementItem> items = new ArrayList<>(4);

        void add(EntitlementItem item) {
            if (this.items.contains(item)) {
                return;
            }
            for (int i = 0; i < this.items.size() - 1; i++) {
                if (this.items.get(i).filter == item.filter) {
                    // filter already present, add this one as next filter:
                    this.items.add(i + 1, item);
                    return;
                }
            }
            this.items.add(item);
        }

        private int getEntitlement(Vendorkey v, int fieldid) {
            VendorkeyFilter lastFilter = null;
            for (EntitlementItem item : this.items) {
                if (lastFilter != null && item.filter != lastFilter) {
                    return DEFAULT_ENTITLEMENT;
                }
                if (lastFilter != null || item.filter.test(v)) {
                    lastFilter = item.filter;
                    if (item.fieldGroup.isSet(fieldid)) {
                        return item.entitlement;
                    }
                }
            }
            return DEFAULT_ENTITLEMENT;
        }

        private int[] getEntitlements(Vendorkey vkey) {
            final int[] tmp = new int[items.size()];
            int n = 0;
            VendorkeyFilter lastFilter = null;
            for (EntitlementItem item : this.items) {
                if (lastFilter != null && item.filter != lastFilter) {
                    break;
                }
                if (lastFilter != null || item.filter.test(vkey)) {
                    if (!contains(tmp, n, item.entitlement)) {
                        tmp[n++] = item.entitlement;
                    }
                    lastFilter = item.filter;
                }
            }
            return Arrays.copyOf(tmp, n);
        }

        private boolean contains(int[] tmp, int n, int entitlement) {
            for (int i = 0; i < n; i++) {
                if (tmp[i] == entitlement) {
                    return true;
                }
            }
            return false;
        }

        private BitSet getFields(Vendorkey vkey) {
            final BitSet result = new BitSet(VwdFieldDescription.length());
            VendorkeyFilter lastFilter = null;
            for (EntitlementItem item : this.items) {
                if (lastFilter != null && item.filter != lastFilter) {
                    break;
                }
                if (lastFilter != null || item.filter.test(vkey)) {
                    result.or(item.fieldGroup.fields);
                    lastFilter = item.filter;
                }
            }
            return result;
        }

        private BitSet getFields(Vendorkey vkey, int... entitlements) {
            if (entitlements == null || entitlements.length == 0) {
                return new BitSet(0);
            }
            VendorkeyFilter lastFilter = null;
            BitSet result = null;
            for (EntitlementItem item : this.items) {
                if (lastFilter != null && item.filter != lastFilter) {
                    break;
                }
                if (lastFilter != null || item.filter.test(vkey)) {
                    if (ArraysUtil.contains(entitlements, item.entitlement)) {
                        if (result == null) {
                            result = new BitSet(item.fieldGroup.fields.size());
                        }
                        result.or(item.fieldGroup.fields);
                    }
                    lastFilter = item.filter;
                }
            }
            return (result != null) ? result : new BitSet(0);
        }
    }

    public void setMinNumMarkets(int minNumMarkets) {
        this.minNumMarkets = minNumMarkets;
    }

    public void setEntitlementFieldGroups(File entitlementFieldGroups) {
        this.entitlementFieldGroups = entitlementFieldGroups;
    }

    public void setEntitlementRules(File entitlementRules) {
        this.entitlementRules = entitlementRules;
    }

    /**
     * Masks certain vwd fields as eod data for a particular user.
     * Only needed as a workaround and should only be used if really necessary.
     */
    public void setAllowedEodFieldsByUser(Map<String, List<String>> allowedEodFieldsByUser) {
        this.allowedEodFieldsByUser = allowedEodFieldsByUser;
    }

    public void setEndOfDayProvider(EndOfDayProvider endOfDayProvider) {
        this.endOfDayProvider = endOfDayProvider;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        this.logger.info("<afterPropertiesSet> file.encoding = " + System.getProperty("file.encoding"));
        loadRules();
        if (this.mappings.isEmpty()) {
            throw new IllegalStateException("failed to load entitlement rules");
        }
        if (this.activeMonitor != null) {
            final FileResource fr = new FileResource(this.entitlementRules);
            fr.addPropertyChangeListener(evt -> loadRules());
            this.activeMonitor.addResource(fr);
            this.logger.info("<afterPropertiesSet> registered "
                    + this.entitlementRules.getAbsolutePath() + " with ActiveMonitor");
        }
    }

    @ManagedOperation
    public void loadRules() {
        final EntitlementReaderVwd reader
                = new EntitlementReaderVwd(this.entitlementRules, this.entitlementFieldGroups);
        if (reader.loadRules(this.minNumMarkets)) {
            this.mappings = reader.getResult();
        }
    }

    public int[] getEntitlements(String vkey) {
        return getEntitlements(VendorkeyVwd.getInstance(new ByteString(vkey)));
    }

    public int[] getEntitlements(FeedData data) {
        return getEntitlements(data.getVendorkey());
    }

    public int[] getEntitlements(Vendorkey vkey) {
        final MarketEntitlements marketEntitlements = getMarketEntitlements(vkey.getMarketName());
        if (marketEntitlements == null) {
            return new int[0];
        }

        return marketEntitlements.getEntitlements(vkey);
    }

    public int getEntitlement(String vkey, int fieldid) {
        return getEntitlement(VendorkeyVwd.getInstance(new ByteString(vkey)), fieldid);
    }

    public int getEntitlement(FeedData data, int fieldid) {
        return getEntitlement(data.getVendorkey(), fieldid);
    }

    public int getEntitlement(Vendorkey vkey, int fieldid) {
        final MarketEntitlements marketEntitlements = getMarketEntitlements(vkey.getMarketName());
        if (marketEntitlements == null) {
            return DEFAULT_ENTITLEMENT;
        }

        return marketEntitlements.getEntitlement(vkey, fieldid);
    }

    public BitSet getFields(String vkey) {
        return getFields(VendorkeyVwd.getInstance(new ByteString(vkey)));
    }

    public BitSet getFields(Vendorkey vkey) {
        return getFields(new FeedDataVkeyOnly(vkey));
    }

    public BitSet getFields(FeedData data) {
        final MarketEntitlements marketEntitlements = getMarketEntitlements(data.getMarket().getName());
        if (marketEntitlements == null) {
            return new BitSet(0);
        }

        return marketEntitlements.getFields(data.getVendorkey());
    }

    public BitSet getFields(String vkey, int... entitlements) {
        return getFields(VendorkeyVwd.getInstance(new ByteString(vkey)), entitlements);
    }

    public BitSet getFields(FeedData data, int... entitlements) {
        return getFields(data.getVendorkey(), entitlements);
    }

    public BitSet getFields(Vendorkey vkey, int... entitlements) {
        final MarketEntitlements marketEntitlements = getMarketEntitlements(vkey.getMarketName());
        if (marketEntitlements == null) {
            return new BitSet(0);
        }

        return marketEntitlements.getFields(vkey, entitlements);
    }

    private MarketEntitlements getMarketEntitlements(final ByteString market) {
        return this.mappings.get(market);
    }

    public Set<String> getMarketNames(Profile p) {
        final Set<String> result = new HashSet<>();

        final BitSet bs = allowedPriceEntitlements(p);
        final Map<ByteString, MarketEntitlements> m = this.mappings;

        NEXT_ENTRY:
        for (Map.Entry<ByteString, MarketEntitlements> entry : m.entrySet()) {
            final MarketEntitlements me = entry.getValue();
            for (EntitlementItem item : me.items) {
                if (bs.get(item.entitlement)) {
                    result.add(entry.getKey().toString());
                    continue NEXT_ENTRY;
                }
            }
        }
        return result;
    }

    private BitSet allowedPriceEntitlements(Profile p) {
        final BitSet result = new BitSet(1040);
        result.or(p.toEntitlements(Profile.Aspect.PRICE, PriceQuality.REALTIME));
        result.or(p.toEntitlements(Profile.Aspect.PRICE, PriceQuality.DELAYED));
        result.or(p.toEntitlements(Profile.Aspect.PRICE, PriceQuality.END_OF_DAY));
        return result;
    }

    public Entitlement getEntitlement(Market market) {
        if (market.getId() == KAG_MARKET_ID) {
            return KAG_MARKET_ENTITLEMENTS;
        }
        return EMPTY_ENTITLEMENTS;
    }

    public Entitlement getEntitlement(String key) {
        if (key == null) {
            return EMPTY_ENTITLEMENTS;
        }

        final int[] entitlements = getEntitlements(key);
        if (entitlements == null) {
            return EMPTY_ENTITLEMENTS;
        }

        final String[] values = new String[entitlements.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = EntitlementsVwd.toEntitlement(entitlements[i]);
        }
        return new VwdEntitlement(values);
    }

    @Override
    public BitSet getAllowedFields(Quote q, Profile profile, Set<PriceQuality> required) {
        final VendorkeyVwd vkey = VendorkeyVwd.getInstance(new ByteString(q.getSymbolVwdfeed()));
        final int[] ents = getEntitlements(vkey);

        final BitSet result = getFields(vkey, getAllowedEntitlements(profile, ents, required));
        result.or(ALWAYS_ALLOWED_FIELDS);
        return result;
    }

    @Override
    public BitSet getAllowedFields(Quote q, Profile profile) {
        return getAllowedFields(q, profile, NOT_NONE);
    }

    @Override
    public BitSet getAllowedPushFields(Quote q, Profile profile) {
        return getAllowedFields(q, profile, REALTIME_OR_DELAYED);
    }

    public EndOfDayFilter getEoDFilter(Quote q, Profile profile) {
        final VendorkeyVwd vkey = VendorkeyVwd.getInstance(q.getSymbolVwdfeed());
        final int[] ents = getEntitlements(vkey);
        for (final int ent : ents) {
            final PriceQuality pq = getPriceQuality(profile, ent);
            if (pq == PriceQuality.END_OF_DAY) {
                return getEodFilter(vkey);
            }
        }
        return null;
    }

    public BitSet getAllowedEodFieldsByProfile(Quote q, Profile profile) {
        List<String> eodFieldNames = Optional.ofNullable(profile)
                .filter(Objects::nonNull)
                .map(Profile::getName)
                .filter(Objects::nonNull)
                .map(name -> this.allowedEodFieldsByUser.get(name))
                .orElse(null);
        if (Objects.nonNull(eodFieldNames)) {
            BitSet result = new BitSet();
            eodFieldNames.stream()
                    .map(VwdFieldDescription::getFieldByName)
                    .mapToInt(VwdFieldDescription.Field::id)
                    .forEach(id -> result.set(id));
            return result;
        }
        return null;
    }

    private int[] getAllowedEntitlements(Profile profile, int[] entitlements, Set<PriceQuality> allowed) {
        int[] result = new int[entitlements.length];
        int idx = 0;
        for (int ent : entitlements) {
            final PriceQuality pq = getPriceQuality(profile, ent);
            if (allowed.contains(pq)) {
                result[idx++] = ent;
            }
        }
        return (idx < result.length) ? Arrays.copyOf(result, idx) : result;
    }


    private PriceQuality getPriceQuality(Profile profile, int ent) {
        return profile.getPriceQuality(EntitlementsVwd.toEntitlement(ent), KeysystemEnum.VWDFEED);
    }

    private PriceQuality getPushPriceQuality(Profile profile, int ent) {
        return profile.getPushPriceQuality(EntitlementsVwd.toEntitlement(ent), KeysystemEnum.VWDFEED);
    }

    private EndOfDayFilter getEodFilter(VendorkeyVwd vkey) {
        if (this.endOfDayProvider == null) {
            return EndOfDayFilter.DEFAULT;
        }
        return this.endOfDayProvider.getEodFilter(vkey);
    }


    public static void main(String[] args) throws Exception {
        final Path dir = args.length > 0 ? Paths.get(args[0]) : Paths.get(System.getProperty("user.home"), "produktion/var/data/web");
        final EntitlementProviderVwd ep = new EntitlementProviderVwd();
        ep.setMinNumMarkets(50);
        ep.setEntitlementFieldGroups(dir.resolve("EntitlementFieldGroups.txt").toFile());
        ep.setEntitlementRules(dir.resolve("EntitlementRules.XFeed.txt").toFile());
        ep.afterPropertiesSet();
        ep.getEntitlement("", 2);
        System.out.println("Ents: " + ep.getEntitlement("1.SOLB.BL"));
        BitSet bs = ep.getFields("1.SOLB.BL");
        System.out.println(bs);
    }
}
