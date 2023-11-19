/*
 * FuzzyMatch.java
 *
 * Created on 22.02.2005 17:14:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Comparator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FuzzyMatch {
    final static Comparator<FuzzyMatch> COMPARATOR_BOOST = new Comparator<FuzzyMatch>() {
        public int compare(FuzzyMatch fm1, FuzzyMatch fm2) {
            final float diff = fm1.getBoost() - fm2.getBoost();
            return diff < 0 ? 1 : diff > 0 ? -1 : 0;
        }
    };

    private final String text;
    private final float boost;
    private int count;

    public FuzzyMatch(String text, float boost) {
        this.text = text;
        this.boost = boost;
    }

    public String getText() {
        return text;
    }

    public float getBoost() {
        return boost;
    }

    public int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }

    public String toString() {
        return "FuzzyMatch[text=" + text
                + ", boost=" + boost
                + ", count=" + count
                + "]";

    }
}
