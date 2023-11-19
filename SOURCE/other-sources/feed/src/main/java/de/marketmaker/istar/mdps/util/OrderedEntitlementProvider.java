/*
 * OrderedEntitlementProvider.java
 *
 * Created on 04.10.13 09:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.util;

import java.io.File;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedDataVkeyOnly;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.mdps.iqs.IqsFeedData;

/**
 * @author oflege
 */
@ManagedResource
public class OrderedEntitlementProvider implements InitializingBean {

    private static final int[] NULL_ENTITLEMENTS = new int[0];

    static class FilteredGroup {
        private final int type;

        private final VendorkeyFilter filter;

        private final EntitledFieldGroup[] fieldGroups;

        private final int[] selectors;

        /**
         * not always needed, so we compute it lazily
         */
        private volatile BitSet fields;

        FilteredGroup(VendorkeyFilter filter, int type, EntitledFieldGroup[] fieldGroups) {
            this.filter = filter;
            this.type = type;
            this.fieldGroups = fieldGroups;

            int n = 0;
            final int[] tmp = new int[fieldGroups.length];
            for (EntitledFieldGroup fieldGroup : fieldGroups) {
                if (!contains(tmp, fieldGroup.selector, n)) {
                    tmp[n++] = fieldGroup.selector;
                }
            }
            Arrays.sort(tmp, 0, n);
            this.selectors = Arrays.copyOf(tmp, n);
        }

        private static boolean contains(int[] tmp, int value, int n) {
            for (int i = 0; i < n; i++) {
                if (tmp[i] == value) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilteredGroup that = (FilteredGroup) o;
            return this.filter.toString().equals(that.filter.toString())
                    && Arrays.equals(fieldGroups, that.fieldGroups);
        }

        @Override
        public int hashCode() {
            return 31 * filter.hashCode() + Arrays.hashCode(fieldGroups);
        }

        @Override
        public String toString() {
            return this.filter + "=>" + Arrays.toString(this.fieldGroups);
        }

        private BitSet getFields() {
            if (this.fields == null) {
                this.fields = computeFields();
            }
            return this.fields;
        }

        private BitSet computeFields() {
            BitSet result = null;
            for (EntitledFieldGroup g : this.fieldGroups) {
                CompactBitSet cbs = g.getFieldOrders();
                if (result == null) {
                    result = cbs.toBitSet();
                }
                else {
                    result.or(cbs.toBitSet());
                }
            }
            return result;
        }
    }

    public static class MarketEntitlements {
        private static Set<Integer> getSelectorsForType(FilteredGroup[] groups, int type) {
            final Set<Integer> result = new TreeSet<>();
            for (FilteredGroup group : groups) {
                if (group.type == type) {
                    for (int selector : group.selectors) {
                        result.add(selector);
                    }
                }
            }
            return result;
        }

        private static int getMaxType(FilteredGroup[] groups) {
            int result = 0;
            for (FilteredGroup group : groups) {
                result = Math.max(result, group.type);
            }
            return result;
        }

        private static int[] toArray(Set<Integer> s) {
            int[] result = new int[s.size()];
            int n = 0;
            for (Integer value : s) {
                result[n++] = value;
            }
            return result;
        }

        private final FilteredGroup[] groups;

        /**
         * selectors by vwd type code. For each type present in at least one element of groups,
         * selectors[type] will be defined and contain the set of numeric selectors for all
         * groups defined for that type.
         */
        private final int[][] selectors;

        MarketEntitlements(FilteredGroup[] groups) {
            this.groups = groups;
            this.selectors = new int[getMaxType(groups) + 1][];
            for (int i = 0; i < this.selectors.length; i++) {
                final Set<Integer> s = getSelectorsForType(groups, i);
                if (!s.isEmpty()) {
                    selectors[i] = toArray(s);
                }
            }
        }

        EntitledFieldGroup[] getGroupFor(FeedData fd) {
            for (FilteredGroup group : groups) {
                if (group.filter.test(fd.getVendorkey())) {
                    return group.fieldGroups;
                }
            }
            return null;
        }

        BitSet getFieldsFor(FeedData fd) {
            for (FilteredGroup group : groups) {
                if (group.filter.test(fd.getVendorkey())) {
                    return group.getFields();
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MarketEntitlements that = (MarketEntitlements) o;
            return Arrays.equals(groups, that.groups);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(groups);
        }

        @Override
        public String toString() {
            return Arrays.toString(this.groups);
        }

        private int[] getSelectors(int[] types) {
            if (types.length == 1) {
                int[] result = getSelectors(types[0]);
                return result != null ? result : NULL_ENTITLEMENTS;
            }
            Set<Integer> result = new TreeSet<>();
            for (int type : types) {
                int[] ss = getSelectors(type);
                if (ss != null) {
                    for (int s : ss) {
                        result.add(s);
                    }
                }
            }
            return toArray(result);
        }

        private int[] getSelectors(int type) {
            return (type >= 0 && type < this.selectors.length) ? this.selectors[type] : null;
        }
    }

    private static class FileMemento {
        private final File file;
        private long lastModfied;
        private long size;

        private FileMemento(File file) {
            this.file = file;
        }

        private boolean isChanged() {
            long newLastModified = file.lastModified();
            long newSize = file.length();
            final boolean result = (this.lastModfied != newLastModified || this.size != newSize)
                    && newLastModified != 0 && newSize != 0;
            this.lastModfied = newLastModified;
            this.size = newSize;
            return result;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File entitlementRules;

    private File entitlementFieldGroups;

    private FileMemento rulesMemento;

    private FileMemento groupsMemento;

    private volatile Map<ByteString, MarketEntitlements> marketEntitlements = null;

    private int minNumMarkets = 50;

    private FeedDataRepository repository;

    public void setRepository(FeedDataRepository repository) {
        this.repository = repository;
    }

    public void setEntitlementRules(File entitlementRules) {
        this.entitlementRules = entitlementRules;
        this.rulesMemento = new FileMemento(entitlementRules);
    }

    public void setEntitlementFieldGroups(File entitlementFieldGroups) {
        this.entitlementFieldGroups = entitlementFieldGroups;
        this.groupsMemento = new FileMemento(entitlementFieldGroups);
    }

    public void setMinNumMarkets(int minNumMarkets) {
        this.minNumMarkets = minNumMarkets;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reloadEntitlements();
        if (this.marketEntitlements == null) {
            throw new IllegalStateException();
        }
    }

    @ManagedOperation
    public void reloadEntitlements() {
        if (!filesChanged()) {
            return;
        }
        final OrderedEntitlementReader r
                = new OrderedEntitlementReader(this.entitlementRules, this.entitlementFieldGroups);
        final Map<ByteString, MarketEntitlements> map = r.loadRules(minNumMarkets);
        if (map == null) {
            return;
        }
        if (this.marketEntitlements != null && this.repository != null) {
            invalidateChangedMappings(map);
        }
        this.marketEntitlements = map;
    }

    private boolean filesChanged() {
        // do NOT USE SHORTCUT ||
        return this.rulesMemento.isChanged() | this.groupsMemento.isChanged();
    }

    private void invalidateChangedMappings(Map<ByteString, MarketEntitlements> updates) {
        for (FeedMarket market : this.repository.getMarkets()) {
            final MarketEntitlements existing = this.marketEntitlements.get(market.getName());
            final MarketEntitlements update = updates.get(market.getName());
            if (!Objects.equals(existing, update)) {
                market.applyToElements(fd -> ((IqsFeedData) fd).removeCachedFieldGroups());
                this.logger.info("<invalidateChangedMappings> for " + market);
            }
        }
    }

    public EntitledFieldGroup[] getGroups(FeedData fd) {
        final MarketEntitlements mes = this.marketEntitlements.get(fd.getMarket().getName());
        if (mes == null) {
            return null;
        }
        return mes.getGroupFor(fd);
    }

    public BitSet getFields(FeedData fd) {
        final MarketEntitlements mes = this.marketEntitlements.get(fd.getMarket().getName());
        if (mes == null) {
            return null;
        }
        return mes.getFieldsFor(fd);
    }

    public int[] getSelectors(ByteString market, int[] types) {
        final MarketEntitlements mes = this.marketEntitlements.get(market);
        if (mes == null) {
            return NULL_ENTITLEMENTS;
        }
        return mes.getSelectors(types);
    }

    public boolean hasEntitlementsForMarket(ByteString marketName) {
        return this.marketEntitlements.get(marketName) != null;
    }

    public static void main(String[] args) throws Exception {
        final FeedDataRepository fdr = new FeedDataRepository();
        fdr.registerMarket(new ByteString("EUWAX"));

        File dir = new File(LocalConfigProvider.getProductionBaseDir(), "var/data/vwd/feed/in");
        final OrderedEntitlementProvider p = new OrderedEntitlementProvider();
        p.setEntitlementRules(new File(dir, "EntitlementRules.EOD.txt"));
        p.setEntitlementFieldGroups(new File(dir, "EntitlementFieldGroups.EOD.txt"));
        p.setRepository(fdr);

        p.afterPropertiesSet();

        final FeedDataVkeyOnly fd = new FeedDataVkeyOnly(VendorkeyVwd.getInstance("17.FOOBAR.EUWAX"));
        final EntitledFieldGroup[] groups = p.getGroups(fd);
        System.out.println(Arrays.toString(groups));
        System.out.println(p.getFields(fd));
        System.out.println(Arrays.toString(p.getSelectors(new ByteString("ETR"), new int[]{1,3,4})));
    }

}
