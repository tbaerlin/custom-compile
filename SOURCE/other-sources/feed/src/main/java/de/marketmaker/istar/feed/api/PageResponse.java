/*
 * PageResponse.java
 *
 * Created on 15.06.2005 16:33:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 213546879L;

    private String text;

    private boolean dynamic;

    private long timestamp;

    private List<String> vendorkeys;

    private Set<String> selectors;

    private String nextPageId;

    private String previousPageId;

    public PageResponse() {
        setInvalid();
    }

    public PageResponse(String text, List<String> vendorkeys, Set<String> selectors,
            boolean dynamic, long timestamp, String nextPageId, String previousPageId) {
        this.text = text;
        this.dynamic = dynamic;
        this.timestamp = timestamp;
        this.vendorkeys = vendorkeys;
        this.selectors = selectors;
        this.nextPageId = nextPageId;
        this.previousPageId = previousPageId;
    }

    public String getText() {
        return this.text;
    }

    public List<String> getVendorkeys() {
        return this.vendorkeys;
    }

    public Set<String> getSelectors() {
        return selectors;
    }

    public boolean isDynamic() {
        return this.dynamic;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getNextPageId() {
        return nextPageId;
    }

    public String getPreviousPageId() {
        return previousPageId;
    }

    public void replaceVwdcodesWithVendorkeys(Map<String, String> typedKeys) {
        for (int i = 0; i < this.vendorkeys.size(); i++) {
            String s = this.vendorkeys.get(i);
            String typed = typedKeys.get(s);
            if (typed != null) {
                this.vendorkeys.set(i, typed);
            }
        }
    }
}
