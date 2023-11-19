/*
 * MergeableSearchEngineVisitor.java
 *
 * Created on 02.08.12 12:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

/**
 * {@link SearchEngineVisitor} that can be used in parallel searches; separate visitors are used
 * to visit different areas of the search space and then the results of those visitors are merged
 * to obtain the final result.
 *
 * @author oflege
 */
public interface MergeableSearchEngineVisitor<T extends MergeableSearchEngineVisitor>
    extends SearchEngineVisitor {

    /**
     * Merge the results of this visitor with that of visitor v;
     * This method will be called <em>after</em> {@link #visit(RatioData)} has been
     * called for all results for this visitor as well as for <tt>v</tt>.
     * @param v another visitor with another (partial) result
     * @return result of merging this and v (usually <tt>this</tt> after results from <tt>v</tt>
     * have been merged in)
     */
    T merge(T v);
}
