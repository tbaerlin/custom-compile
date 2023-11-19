/*
 * FuzzyMatches.java
 *
 * Created on 22.02.2005 17:14:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FuzzyMatches {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String searchTerm;
    private final Map<String, FuzzyMatch> matches = new HashMap<>();
    private float topBoost=0;

    public FuzzyMatches(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public void add(FuzzyMatch fuzzyMatch) {
        this.matches.put(fuzzyMatch.getText(), fuzzyMatch);
        this.topBoost = Math.max(this.topBoost,fuzzyMatch.getBoost());
    }

    public boolean isEmpty() {
        return matches.isEmpty();
    }

    public float getTopBoost() {
        return topBoost;
    }

    public void setCount(String text, int count) {
        final FuzzyMatch fuzzyMatch = this.matches.get(text);
        fuzzyMatch.setCount(count);
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public List<String> getTopRanked(int level) {
        final List<FuzzyMatch> fms = new ArrayList<>(this.matches.values());

        if (fms.isEmpty()) {
            return Collections.emptyList();
        }

        fms.sort(FuzzyMatch.COMPARATOR_BOOST);
        final List<String> result = new ArrayList<>();
        int numLevels = 0;
        float currentBoost = fms.get(0).getBoost();
        for (final FuzzyMatch fm : fms) {
            if (currentBoost != fm.getBoost()) {
                currentBoost = fm.getBoost();
                numLevels++;

                if (numLevels == level) {
                    break;
                }
            }
            result.add(fm.getText());
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getTopRanked> add " + fm);
            }
        }
        return result;
    }

    public String toString() {
        return "FuzzyMatches[searchTerm=" + searchTerm
                + ", matches=" + this.matches.values()
                + "]";
    }
}
