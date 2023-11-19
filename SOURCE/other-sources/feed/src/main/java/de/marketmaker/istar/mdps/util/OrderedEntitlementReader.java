/*
 * OrderedEntitlementReader.java
 *
 * Created on 04.10.13 09:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * @author oflege
 */
class OrderedEntitlementReader {

    private static final String HEARTBEAT_FIELD_GROUP = "FG_1_E_VWD_20Y";

    private class MarketEntitlementsBuilder {
        private final Map<VendorkeyFilter, List<EntitledFieldGroup>> groups
                = new LinkedHashMap<>();

        public OrderedEntitlementProvider.MarketEntitlements build() {
            return new OrderedEntitlementProvider.MarketEntitlements(createGroups());
        }

        private OrderedEntitlementProvider.FilteredGroup[] createGroups() {
            final OrderedEntitlementProvider.FilteredGroup[] result
                    = new OrderedEntitlementProvider.FilteredGroup[this.groups.size()];
            int i = 0;
            for (Map.Entry<VendorkeyFilter, List<EntitledFieldGroup>> e : groups.entrySet()) {
                result[i++] = new OrderedEntitlementProvider.FilteredGroup(e.getKey(),
                        filterTypes.get(e.getKey()), getFieldGroups(e.getValue()));
            }
            return result;
        }

        private EntitledFieldGroup[] getFieldGroups(
                final List<EntitledFieldGroup> groups) {
            final EntitledFieldGroup[] result
                    = groups.toArray(new EntitledFieldGroup[groups.size()]);
            Arrays.sort(result);
            return result;
        }

        public void add(VendorkeyFilter filter, int entitlement, CompactBitSet ents) {
            List<EntitledFieldGroup> existing = groups.get(filter);
            if (existing == null) {
                groups.put(filter, existing = new ArrayList<>());
            }
            for (int i = 0; i < existing.size(); i++) {
                final EntitledFieldGroup fieldGroup = existing.get(i);
                if (fieldGroup.selector == entitlement) {
                    if (!fieldGroup.getFieldOrders().equals(ents)) {
                        existing.set(i, new EntitledFieldGroup(entitlement, fieldGroup.getFieldOrders().or(ents)));
                    }
                    return;
                }
            }
            existing.add(new EntitledFieldGroup(entitlement, ents));
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File entitlementRules;

    private final File entitlementFieldGroups;

    private final Map<ByteString, MarketEntitlementsBuilder> builders = new HashMap<>();

    private final Map<CompactBitSet, CompactBitSet> groupByFields = new HashMap<>();

    private final Map<String, CompactBitSet> groups = new HashMap<>();

    private final Map<String, VendorkeyFilter> filtersByName = new HashMap<>();

    private final Map<VendorkeyFilter, Integer> filterTypes = new IdentityHashMap<>();

    private int numIdenticalGroups = 0;

    OrderedEntitlementReader(File entitlementRules, File entitlementFieldGroups) {
        this.entitlementRules = entitlementRules;
        this.entitlementFieldGroups = entitlementFieldGroups;
    }

    Map<ByteString, OrderedEntitlementProvider.MarketEntitlements> loadRules(int minNumMarkets) {
        try {
            readFieldGroups();
            if (groups.isEmpty()) {
                this.logger.error("<loadRules> did not read any field groups");
                return null;
            }
            if (readRules(minNumMarkets)) {
                return createResult();
            }
        } catch (Exception e) {
            this.logger.error("<loadRules> failed", e);
        }
        return null;
    }

    private Map<ByteString, OrderedEntitlementProvider.MarketEntitlements> createResult() {
        final HashMap<ByteString, OrderedEntitlementProvider.MarketEntitlements> result
                = new HashMap<>(builders.size() * 4 / 3);
        for (Map.Entry<ByteString, MarketEntitlementsBuilder> e : builders.entrySet()) {
            result.put(e.getKey(), e.getValue().build());
        }
        return result;
    }

    private boolean readRules(int minNumMarkets) throws Exception {
        final Pattern p = Pattern.compile("\\s+"); // used to split on whitespace
        int n = 0;

        try (Scanner s = new Scanner(this.entitlementRules)) {
            while (s.hasNextLine()) {
                n++;
                final String line = s.nextLine().trim();
                if (!StringUtils.hasText(line) || line.startsWith("#")) {
                    continue;
                }
                final String[] tokens = p.split(line);
                if (tokens.length != 6) {
                    logger.warn("<readRules> != 6 tokens in line " + n + ": '" + line + "'");
                    continue;
                }

                final int type = getVwdType(tokens[2]) & 0xFF;
                if (type == 0) {
                    this.logger.warn("<readRules> invalid sectype in line: " + line);
                    continue;
                }

                tokens[2] = Integer.toString(type);

                final CompactBitSet ents = groups.get(tokens[5]);
                if (ents == null) {
                    logger.warn("<readRules> unknown FieldGroup: " + tokens[5]);
                    continue;
                }

                final int entitlement = EntitlementsVwd.toValue(tokens[4]);

                MarketEntitlementsBuilder me = getMarketEntitlements(new ByteString(tokens[3]));
                me.add(getFilter(type, tokens), entitlement, ents);
            }
        }

        MarketEntitlementsBuilder vwd = getMarketEntitlements(new ByteString("VWD"));
        if (vwd.groups.isEmpty()) {
            vwd.add(getFilter(1, new String[] { "*", "*", "1"}), EntitlementsVwd.toValue("20Y"),
                    groups.get(HEARTBEAT_FIELD_GROUP));
        }


        if (this.builders.size() < minNumMarkets) {
            this.logger.error("<readRules> for " + this.builders.size() + " markets"
                    + " <  expected: " + minNumMarkets);
            return false;
        }

        this.logger.info("<readRules> for " + this.builders.size() + " markets");
        return true;
    }

    private int getVwdType(String t) {
        if (t.length() > 0 && Character.isDigit(t.charAt(0))) {
            return Integer.parseInt(t);
        }
        switch (t.length()) {
            case 1:
                return MdpsTypeMappings.getMappingForMdpsType(0, t.charAt(0));
            case 2:
                return MdpsTypeMappings.getMappingForMdpsType(t.charAt(0), t.charAt(1));
            default:
                return 0;
        }
    }

    private MarketEntitlementsBuilder getMarketEntitlements(ByteString market) {
        MarketEntitlementsBuilder result = this.builders.get(market);
        if (result == null) {
            this.builders.put(market, result = new MarketEntitlementsBuilder());
        }
        return result;
    }

    private VendorkeyFilter getFilter(int type, String[] tokens) {
        final String filterSpec = toFilterSpec(tokens);
        VendorkeyFilter filter = filtersByName.get(filterSpec);
        if (filter == null) {
            filter = VendorkeyFilterFactory.create(filterSpec);
            this.filtersByName.put(filterSpec, filter);
            this.filterTypes.put(filter, type);
        }
        return filter;
    }

    private String toFilterSpec(String[] tokens) {
        final StringBuilder sb = new StringBuilder("t:" + tokens[2]);
        final String from = tokens[0];
        final String to = tokens[1];
        if (!from.equals(to)) {
            sb.append(" && >=").append(from).append(" && <=").append(to);
        }
        else if (!"*".equals(from)) {
            sb.append(" && ^").append(from);
        }
        return sb.toString();
    }

    private void readFieldGroups() throws IOException {
        final BitSet bs = new BitSet(VwdFieldOrder.MAX_ORDER);
        String name = null;

        try (Scanner s = new Scanner(this.entitlementFieldGroups)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                if ("".equals(line) || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("~")) {
                    addGroup(name, bs);
                    name = line.substring(1);
                    bs.clear();
                }
                else if (name != null) {
                    final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(line);
                    if (field != null && VwdFieldOrder.getOrder(field.id()) > 0) {
//                        bs.set(VwdFieldOrder.getOrder(field.id()));
                        bs.set(VwdFieldOrder.getOrder(field.id()));
                    }
                    else {
                        this.logger.warn("<readFieldGroups> ignoring unknown field: " + line);
                    }
                }
            }
            addGroup(name, bs);
        }

        if (!this.groups.containsKey(HEARTBEAT_FIELD_GROUP)) {
            addHeartbeatGroup();
        }

        this.logger.info("<readFieldGroups> read " + groups.size() + " groups, "
                + groupByFields.size() + " with unique fields");
    }

    private void addHeartbeatGroup() {
        BitSet hb = new BitSet(64);
        hb.set(VwdFieldOrder.ORDER_ADF_ZEIT);
        hb.set(VwdFieldOrder.ORDER_ADF_DATUM);
        addGroup(HEARTBEAT_FIELD_GROUP, hb);
    }

    private void addGroup(String name, BitSet bs) {
        if (name == null) {
            return;
        }
        if (bs.isEmpty()) {
            this.logger.warn("<addGroup> ignoring empty group: " + name);
            return;
        }
        final CompactBitSet oes = new CompactBitSet(bs);
        final CompactBitSet existing = groupByFields.get(oes);
        if (existing != null) {
            groups.put(name, existing);
            this.numIdenticalGroups++;
        }
        else {
            groupByFields.put(oes, oes);
            groups.put(name, oes);
        }
    }

    public static void main(String[] args) throws Exception {
        File dir = new File(args.length > 0 ? args[0] : "/Users/oflege/produktion/var/data/vwd/feed/in/");
        final OrderedEntitlementReader r = new OrderedEntitlementReader(
                new File(dir, "EntitlementRules.XFeed.txt")
                , new File(dir, "EntitlementFieldGroups.txt")
        );
        final Map<ByteString, OrderedEntitlementProvider.MarketEntitlements> map = r.loadRules(100);
        System.out.println(map.size());
        System.out.println(r.numIdenticalGroups);
        System.out.println(map.get(new ByteString("A")));
        System.out.println(map.get(new ByteString("EUWAX")));
    }


}
