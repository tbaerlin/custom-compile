/*
 * ChainedFilter.java
 *
 * Created on 22.10.2008 18:22:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lucene;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;

/**
 * Basically a copy of {@link org.apache.lucene.misc.ChainedFilter}. Differences are that
 * {@link #initialResult(org.apache.lucene.index.IndexReader, ChainedFilter.Logic, int[])}
 * performs better because it calls {@link #doChain(org.apache.lucene.util.OpenBitSetDISI, ChainedFilter.Logic, org.apache.lucene.search.DocIdSet)}
 * , which is optimized for OpenBitSets,
 * and that the final result of evaluating the filter is never turned into a
 * {@link org.apache.lucene.util.SortedVIntList}, which would only be helpful if we were
 * to cache the ChainedFilter instance which we don't (the caller could still perform the
 * transformation if deemed helpful).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ChainedFilter extends Filter {
    public enum Logic { OR, AND, ANDNOT, XOR }

    /**
     * Logical operation when none is declared. Defaults to
     * OR.
     */
    public static Logic DEFAULT = Logic.OR;

    /**
     * The filter chain
     */
    private Filter[] chain = null;

    private Logic[] logicArray;

    private Logic logic;

    public static Filter create(Collection<Filter> filters, Logic logic) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }
        if (filters.size() == 1) {
            return filters.iterator().next();
        }
        return new ChainedFilter(filters.toArray(new Filter[filters.size()]), logic);
    }

    public static Filter create(Filter f1, Filter f2, Logic logic) {
        if (f1 == null) {
            return f2;
        }
        if (f2 == null) {
            return f1;
        }
        return new ChainedFilter(new Filter[] { f1, f2 }, logic);
    }

    /**
     * Ctor.
     * @param chain The chain of filters
     */
    public ChainedFilter(Filter[] chain) {
        this.chain = chain;
    }

    /**
     * Ctor.
     * @param chain The chain of filters
     * @param logicArray Logical operations to apply between filters
     */
    public ChainedFilter(Filter[] chain, Logic[] logicArray) {
        this.chain = chain;
        this.logicArray = logicArray;
    }

    /**
     * Ctor.
     * @param chain The chain of filters
     * @param logic Logicial operation to apply to ALL filters
     */
    public ChainedFilter(Filter[] chain, Logic logic) {
        this.chain = chain;
        this.logic = logic;
    }

    /**
     * {@link Filter#getDocIdSet}.
     */
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        int[] index = new int[1]; // use array as reference to modifiable int;
        index[0] = 0;             // an object attribute would not be thread safe.
        if (logic != null)
            return getDocIdSet(reader, logic, index);
        else if (logicArray != null)
            return getDocIdSet(reader, logicArray, index);
        else
            return getDocIdSet(reader, DEFAULT, index);
    }

    private OpenBitSetDISI initialResult(IndexReader reader, Logic logic, int[] index)
            throws IOException {
        OpenBitSetDISI result;
        /**
         * First AND operation takes place against a completely false
         * bitset and will always return zero results.
         */
        if (logic == Logic.AND) {
            result = new OpenBitSetDISI(reader.maxDoc());
            doChain(result, Logic.OR, chain[index[0]].getDocIdSet(reader));
            ++index[0];
        }
        else if (logic == Logic.ANDNOT) {
            result = new OpenBitSetDISI(reader.maxDoc());
            doChain(result, Logic.OR, chain[index[0]].getDocIdSet(reader));
            result.flip(0, reader.maxDoc()); // NOTE: may set bits for deleted docs.
            ++index[0];
        }
        else {
            result = new OpenBitSetDISI(reader.maxDoc());
        }
        return result;
    }


    /**
     * Delegates to each filter in the chain.
     * @param reader IndexReader
     * @param logic Logical operation
     * @return DocIdSet
     */
    private DocIdSet getDocIdSet(IndexReader reader, Logic logic, int[] index)
            throws IOException {
        OpenBitSetDISI result = initialResult(reader, logic, index);
        for (; index[0] < chain.length; index[0]++) {
            doChain(result, logic, chain[index[0]].getDocIdSet(reader));
        }
        return result;
    }

    /**
     * Delegates to each filter in the chain.
     * @param reader IndexReader
     * @param logic Logical operation
     * @return DocIdSet
     */
    private DocIdSet getDocIdSet(IndexReader reader, Logic[] logic, int[] index)
            throws IOException {
        if (logic.length != chain.length)
            throw new IllegalArgumentException("Invalid number of elements in logic array");

        OpenBitSetDISI result = initialResult(reader, logic[0], index);
        for (; index[0] < chain.length; index[0]++) {
            doChain(result, logic[index[0]], chain[index[0]].getDocIdSet(reader));
        }
        return result;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer(40);
        sb.append("ChainedFilter: [");
        for (Filter aChain : chain) {
            sb.append(aChain);
            sb.append(' ');
        }
        sb.append(']');
        return sb.toString();
    }

    private static void doChain(OpenBitSetDISI result, Logic logic, DocIdSet dis)
            throws IOException {

        if (dis instanceof OpenBitSet) {
            // optimized case for OpenBitSets
            switch (logic) {
                case OR:
                    result.or((OpenBitSet) dis);
                    break;
                case AND:
                    result.and((OpenBitSet) dis);
                    break;
                case ANDNOT:
                    result.andNot((OpenBitSet) dis);
                    break;
                case XOR:
                    result.xor((OpenBitSet) dis);
                    break;
                default:
                    doChain(result, DEFAULT, dis);
                    break;
            }
        }
        else {
            final DocIdSetIterator disi = dis.iterator();
            switch (logic) {
                case OR:
                    result.inPlaceOr(disi);
                    break;
                case AND:
                    result.inPlaceAnd(disi);
                    break;
                case ANDNOT:
                    result.inPlaceNot(disi);
                    break;
                case XOR:
                    result.inPlaceXor(disi);
                    break;
                default:
                    doChain(result, DEFAULT, dis);
                    break;
            }
        }
    }
}

