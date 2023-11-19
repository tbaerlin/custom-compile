/*
 * VendorkeyHistory.java
 *
 * Created on 24.09.2009
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.io.File;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Scanner;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * @author Martin Wilke
 * @author Oliver Flege
 */
@ManagedResource
public class VendorkeyHistory implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Maps a vendorkey that is valid since a specific date to the previously used vendorkey.
     */
    private volatile NavigableMap<VkeyChange, String> changes;

    private File changeListFile; // Format 20090918 1.575200.ETR 1.BAY001.ETR

    public void setChangeListFile(File changeListFile) {
        this.changeListFile = changeListFile;
    }

    public void afterPropertiesSet() throws Exception {
        reloadChangeList();
        if (this.changes == null) {
            throw new IllegalStateException();
        }
    }

    @ManagedOperation
    public void reloadChangeList() {
        try (Scanner s = new Scanner(this.changeListFile)) {
            final NavigableMap<VkeyChange, String> tmp = new TreeMap<>();
            while (s.hasNextLine()) {
                final String line = s.nextLine();
                if (line.startsWith("#") || !StringUtils.hasText(line)) {
                    continue;
                }

                final Scanner ls = new Scanner(line);
                final int yyyyMMdd = ls.nextInt();
                final String oldVk = ls.next();
                final String newVk = ls.next();
                tmp.put(new VkeyChange(newVk, yyyyMMdd), oldVk);
            }
            this.changes = tmp;
            this.logger.info("<reloadChangeList> " + tmp.size() + " entries loaded");
        }
        catch (Exception e) {
            this.logger.error("<reloadChangeList> failed", e);
        }
    }

    /**
     *
     * @param vendorkey the current vendorkey
     * @param date we look for the key on this date, format yyyyMMdd
     * @return the vendorkey that was valid on the given date
     */
    public String getVendorkey(String vendorkey, int date) {
        return getVendorkey(vendorkey, date, Integer.MAX_VALUE);
    }

    private String getVendorkey(String vendorkey, int date, int queryDate) {
        Map.Entry<VkeyChange, String> e = this.changes.floorEntry(new VkeyChange(vendorkey, queryDate));
        if (e == null || !vendorkey.equals(e.getKey().key) || date >= e.getKey().validSince) {
            return vendorkey;
        }
        return getVendorkey(e.getValue(), date, e.getKey().validSince - 1);
    }

    private static class VkeyChange implements Comparable<VkeyChange> {
        final int validSince;

        final String key;

        private VkeyChange(String key, int validSince) {
            this.validSince = validSince;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final VkeyChange that = (VkeyChange) o;
            return this.validSince == that.validSince && this.key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return 31 * validSince + key.hashCode();
        }

        @Override
        public int compareTo(VkeyChange o) {
            final int cmp = this.key.compareTo(o.key);
            return (cmp != 0) ? cmp : (this.validSince - o.validSince);
        }

        @Override
        public String toString() {
            return key + ":" + validSince;
        }
    }
}
