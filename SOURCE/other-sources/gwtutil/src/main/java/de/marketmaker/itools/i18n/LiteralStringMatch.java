/*
 * LiteralStringMatch.java
 *
 * Created on 21.07.2010 10:57:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

/**
 * @author zzhao
 */
public class LiteralStringMatch {
    private final String theMatch;

    private final int startIndex;

    private final int endIndex;

    private String propertyKey;

    private boolean isMessage = false;

    public LiteralStringMatch(String theMatch, int startIndex, int endIndex) {
        this.theMatch = theMatch;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getTheMatch() {
        return theMatch;
    }

    public void setIsMessage(boolean message) {
        isMessage = message;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public boolean isMessage() {
        return isMessage;
    }
}