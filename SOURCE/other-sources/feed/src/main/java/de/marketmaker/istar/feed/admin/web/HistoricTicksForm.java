/*
 * HistoricTicksCommand.java
 *
 * Created on 24.04.2005 12:28:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricTicksForm {
    public String vendorkey;

    public int date;

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getVendorkey() {
        return vendorkey;
    }

    public void setVendorkey(String vendorkey) {
        this.vendorkey = vendorkey;
    }
}
