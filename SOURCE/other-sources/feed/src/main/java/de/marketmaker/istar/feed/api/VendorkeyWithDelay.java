/*
 * VendorkeyWithDelay.java
 *
 * Created on 10.02.2005 12:16:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VendorkeyWithDelay implements Serializable {
    static final long serialVersionUID = -7460423813470866973L;

    private final String vendorkey;
    private final boolean realtime;

    public VendorkeyWithDelay(String vendorkey, boolean realtime) {
        this.vendorkey = vendorkey;
        this.realtime = realtime;
    }

    public String getVendorkey() {
        return vendorkey;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public String toString() {
        return "VendorkeyWithDelay[vkey=" + this.vendorkey + ", realtime?=" + this.realtime + "]";
    }
}
