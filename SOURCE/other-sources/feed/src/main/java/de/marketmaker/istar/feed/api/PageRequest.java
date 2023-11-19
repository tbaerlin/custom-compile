/*
 * PageRequest.java
 *
 * Created on 15.06.2005 16:32:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 963258741L;

    private final int pagenumber;

    private boolean preferGermanText;

    public PageRequest(int pagenumber) {
        this.pagenumber = pagenumber;
    }

    public int getPagenumber() {
        return pagenumber;
    }

    public String toString() {
        return "PageRequest[" + this.pagenumber + "]";
    }

    public boolean isPreferGermanText() {
        return preferGermanText;
    }

    public void setPreferGermanText(boolean preferGermanText) {
        this.preferGermanText = preferGermanText;
    }
}
