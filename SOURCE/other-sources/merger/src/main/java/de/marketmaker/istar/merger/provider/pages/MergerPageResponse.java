/*
 * MergerPageResponse.java
 *
 * Created on 22.02.2008 17:43:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MergerPageResponse {
    private String text;

    private DateTime lastUpdate;

    private boolean pdl;

    private String formattedText;

    private final String pageId;

    private String nextPageId;

    private String previousPageId;

    public MergerPageResponse(String pageId) {
        this.pageId = pageId;
    }

    public String getPageId() {
        return pageId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLastUpdate(DateTime dt) {
        this.lastUpdate = dt;
    }

    public void setPdl(boolean pdl) {
        this.pdl = pdl;
    }

    public void setFormattedText(String formattedText) {
        this.formattedText = formattedText;
    }

    public String getFormattedText() {
        return formattedText;
    }

    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    public boolean isPdl() {
        return pdl;
    }

    public String getText() {
        return text;
    }

    public String getNextPageId() {
        return nextPageId;
    }

    public void setNextPageId(String nextPageId) {
        this.nextPageId = nextPageId;
    }

    public String getPreviousPageId() {
        return previousPageId;
    }

    public void setPreviousPageId(String previousPageId) {
        this.previousPageId = previousPageId;
    }
}
