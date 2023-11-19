/*
 * LiteralStringMatch.java
 *
 * Created on 21.07.2010 10:57:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.tools.i18n;

/**
 * @author zzhao
 */
public class LiteralStringMatch {
    private final String theMatch;

    private final int startIndex;

    private final int endIndex;


    public LiteralStringMatch(String theMatch, int startIndex, int endIndex) {
        this.theMatch = theMatch;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public String getTheMatch() {
        return theMatch;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}