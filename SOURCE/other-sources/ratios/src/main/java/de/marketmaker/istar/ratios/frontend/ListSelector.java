/*
 * AndSelector.java
 *
 * Created on 26.10.2005 10:46:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains a set of selection criteria and provides a method
 * to test whether a Selectable object fulfills those criteria. Supports
 * two modes: either fulfilling all criteria is required (and-mode) or
 * it's sufficient to fulfill at least single a one (or-mode).
 * <p>
 * This class is optimized for maximum performance.
 *
 * @author Oliver Flege
 */
@Immutable
public final class ListSelector implements Selector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListSelector.class);

    /** selectors to be tested */
    private final List<Selector> selectors;

    /** true iff all selectors must match */
    private final boolean andMode;

    private final int cost;

    /**
     * Create new object
     * @param andMode if true, fulfilling all criteria is required (and-mode)
     * otherwise it's sufficient to fulfill at least a single one (or-mode).
     * @param selectors list of selectors to be anded or ored
     */
    public static Selector create(boolean andMode, Selector... selectors) {
        return create(andMode, Arrays.asList(selectors));
    }

    /**
     * Create new object
     * @param andMode if true, fulfilling all criteria is required (and-mode)
     * otherwise it's sufficient to fulfill at least a single one (or-mode).
     * @param selectors list of selectors to be anded or ored
     */
    public static Selector create(boolean andMode, List<Selector> selectors) {
        if (selectors.isEmpty()) {
            return null;
        }
        if (selectors.size() == 1) {
            return selectors.get(0);
        }
        if (andMode && selectors.contains(Selector.FALSE)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("<create> rewrite " + selectors + " => " + Selector.FALSE);
            }
            return Selector.FALSE;
        }
        return new ListSelector(andMode, selectors);
    }

    /**
     * Create new object
     * @param andMode if true, fulfilling all criteria is required (and-mode)
     * otherwise it's sufficient to fulfill at least a single one (or-mode).
     * @param selectors list of selectors to be anded or ored
     */
    private ListSelector(boolean andMode, List<Selector> selectors) {
        this.andMode = andMode;
        this.selectors = new ArrayList<>(selectors.size());
        this.selectors.addAll(selectors);
        int n = 0;
        for (Selector selector : selectors) {
            n += selector.getCost();
        }
        this.cost = n;
        this.selectors.sort(Selector.BY_COST);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.andMode ? "AND[" : "OR[");
        for (int i = 0, n = this.selectors.size(); i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            final Selector s = this.selectors.get(i);
            sb.append(s.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Test whether the given Selectable fulfills the criteria in this object.
     * @param s object to be tested.
     */
    public final boolean select(final Selectable s) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, n = this.selectors.size(); i < n; i++) {
            final Selector c = this.selectors.get(i);
            if (this.andMode ^ c.select(s)) {
                return !this.andMode;
            }
        }
        return this.andMode;
    }

    @Override
    public int getCost() {
        return this.cost;
    }
}
