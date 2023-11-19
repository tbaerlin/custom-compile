/*
 * EntitlementReaderVwd.java
 *
 * Created on 12.03.2010 14:14:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;

import static de.marketmaker.istar.feed.vwd.EntitlementProviderVwd.*;

/**
 * @author oflege
 */
class EntitlementReaderVwd {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File entitlementRules;

    private final File entitlementFieldGroups;

    private final Map<ByteString, MarketEntitlements> result = new HashMap<>();

    private final Map<BitSet,FieldGroup> groupByFields = new HashMap<>();

    private final Map<String,FieldGroup> groups = new HashMap<>();

    private final Map<String,VendorkeyFilter> filtersByName = new HashMap<>();

    private final Set<String> unknownFields = new HashSet<>();

    EntitlementReaderVwd(File entitlementRules, File entitlementFieldGroups) {
        this.entitlementRules = entitlementRules;
        this.entitlementFieldGroups = entitlementFieldGroups;
    }

    public Map<ByteString, MarketEntitlements> getResult() {
        return result;
    }

    boolean loadRules(int minNumMarkets) {
        try {
            readFieldGroups();
            if (groups.isEmpty()) {
                this.logger.error("<loadRules> did not read any field groups");
                return false;
            }
            return readRules(minNumMarkets);
        }
        catch (Exception e) {
            this.logger.error("<loadRules> failed", e);
            return false;
        }
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

                if (!tokens[2].matches("\\d+")) {
                    tokens[2] = MdpsTypeMappings.toNumericType(tokens[2]);
                    if (tokens[2] == null) {
                        this.logger.warn("<readRules> invalid sectype in line: " + line);
                        continue;
                    }
                }

                final FieldGroup fieldGroup = groups.get(tokens[5]);
                if (fieldGroup == null) {
                    logger.warn("<readRules> unknown FieldGroup: " + tokens[5]);
                    continue;
                }

                final int entitlement = EntitlementsVwd.toValue(tokens[4]);

                MarketEntitlements me = getMarketEntitlements(new ByteString(tokens[3]));
                me.add(new EntitlementItem(getFilter(tokens), fieldGroup, entitlement));
            }
        }


        if (this.result.size() < minNumMarkets) {
            this.logger.error("<readRules> for " + this.result.size() + " markets"
                +   " <  expected: " + minNumMarkets);
            return false;
        }

        this.logger.info("<readRules> for " + this.result.size() + " markets");
        return true;
    }

    private MarketEntitlements getMarketEntitlements(ByteString market) {
        MarketEntitlements result = this.result.get(market);
        if (result == null) {
            this.result.put(market, result = new MarketEntitlements());
        }
        return result;
    }

    private VendorkeyFilter getFilter(String[] tokens) {
        final String filterSpec = toFilterSpec(tokens);
        VendorkeyFilter filter = filtersByName.get(filterSpec);
        if (filter == null) {
            filter = VendorkeyFilterFactory.create(filterSpec);
            filtersByName.put(filterSpec, filter);
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
        FieldGroup fg = null;
        try (Scanner s = new Scanner(this.entitlementFieldGroups)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                if ("".equals(line) || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("~")) {
                    addGroup(fg);
                    fg = new FieldGroup(line.substring(1));
                }
                else if (fg != null) {
                    final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(line);
                    if (field != null) {
                        fg.set(field.id());
                    }
                    else if (this.unknownFields.add(line)) {
                        this.logger.warn("<readFieldGroups> ignoring unknown field: " + line);
                    }
                }
            }
            addGroup(fg);
        }
        this.logger.info("<readFieldGroups> read " + groups.size() + " groups, "
                + groupByFields.size() + " with unique fields");
    }

    private void addGroup(FieldGroup fg) {
        if (fg != null) {
            if (groupByFields.containsKey(fg.fields)) {
                groups.put(fg.name, groupByFields.get(fg.fields).withName(fg.name));
            }
            else {
                groupByFields.put(fg.fields, fg);
                groups.put(fg.name, fg);
            }
        }
    }
}
