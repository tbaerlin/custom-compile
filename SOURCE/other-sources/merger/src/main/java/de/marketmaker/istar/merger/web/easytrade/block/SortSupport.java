/*
 * SortSupport.java
 *
 * Created on 15.02.12 17:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.ratios.Partition;

/**
 * Supports to sort (and clip) one or more lists with named comparators.
 * @author oflege
 */
public class SortSupport<T> {

    public static Callable<Comparator<Quote>> QUOTE_NAME =
            () -> QuoteComparator.byName(RequestContextHolder.getRequestContext().getQuoteNameStrategy());

    public static Callable<Comparator<Quote>> MARKET_NAME =
            () -> QuoteComparator.byMarketName(RequestContextHolder.getRequestContext().getMarketNameStrategy());

    @SuppressWarnings("unchecked")
    public static Callable<Comparator<Quote>> INSTRUMENT_TYPE_THEN_NAME =
            () -> CollectionUtils.chain(QuoteComparator.BY_INSTRUMENT_TYPE, QUOTE_NAME.call());

    @SuppressWarnings("unchecked")
    public static Callable<Comparator<Quote>> INSTRUMENT_TYPE_DESC_THEN_NAME =
            () -> CollectionUtils.chain(QuoteComparator.BY_INSTRUMENT_TYPE_DESCRIPTION, QUOTE_NAME.call());

    @SuppressWarnings("unchecked")
    public static Callable<Comparator<Quote>> MARKETNAME_THEN_NAME =
            () -> CollectionUtils.chain(MARKET_NAME.call(), QUOTE_NAME.call());

    public static final Comparator<BigDecimal> COMPARATOR_BIGDECIMAL =
            (o1, o2) -> {
                if (o1 == null) {
                    return o2 == null ? 0 : -1;
                }
                else if (o2 == null) {
                    return 1;
                }

                return o1.compareTo(o2);
            };

    public static Callable<Comparator<Quote>> zoneQuoteComparator(
            final Comparator<Quote> defaultValue) {
        return () -> {
            final Comparator<Quote> result
                    = RequestContextHolder.getRequestContext().getQuoteComparator();
            return (result != null) ? result : defaultValue;
        };
    }

    public static class Builder<T> {
        private final Map<String, Comparator<T>> comparators
                = new LinkedHashMap<>();

        private final Map<String, Callable<Comparator<T>>> comparatorFactories
                = new HashMap<>();

        private Builder() {
        }


        public Builder<T> add(String name, Comparator<T> comparator) {
            assertUniqueName(name);
            this.comparators.put(name, comparator);
            return this;
        }

        /**
         * To be used for comparators that need to be created at runtime (e.g., quote names are determined
         * based on a QuoteNameStrategy in the thread-local RequestContext; the comparator is expected
         * to use that strategy).
         * @param name unique name for the comparator
         * @param callable will be called every time when the respective comparator is to be used
         * @return this
         */
        public Builder<T> add(String name, Callable<Comparator<T>> callable) {
            assertUniqueName(name);
            this.comparatorFactories.put(name, callable);
            return this;
        }

        private void assertUniqueName(String name) {
            if (this.comparators.containsKey(name) || this.comparatorFactories.containsKey(name)) {
                throw new IllegalArgumentException("duplicate key: " + name);
            }
        }

        public SortSupport<T> build() {
            return SortSupport.create(this.comparators, this.comparatorFactories);
        }
    }

    public static <T> Builder<T> createBuilder(String name, Comparator<T> comparator) {
        final Builder<T> result = new Builder<>();
        result.add(name, comparator);
        return result;
    }

    public static <T> Builder<T> createBuilder(String name, Callable<Comparator<T>> callable) {
        final Builder<T> result = new Builder<>();
        result.add(name, callable);
        return result;
    }

    private static <T> SortSupport<T> create(Map<String, Comparator<T>> comparators,
            Map<String, Callable<Comparator<T>>> callables) {
        return new SortSupport<>(comparators, callables);
    }

    static List<String> getSortFields(SortSupport<?>... items) {
        final List<String> result = new ArrayList<>();
        for (SortSupport<?> sortSupport : items) {
            for (String s : sortSupport.getSortNames()) {
                if (result.contains(s)) {
                    throw new IllegalStateException("duplicate key: " + s);
                }
                result.add(s);
            }
        }
        return result;
    }

    private final Map<String, Comparator<T>> comparators
            = new LinkedHashMap<>();

    private final Map<String, Callable<Comparator<T>>> comparatorFactories
            = new HashMap<>();

    private SortSupport(Map<String, Comparator<T>> comparators,
            Map<String, Callable<Comparator<T>>> comparatorFactories) {
        this.comparators.putAll(comparators);
        this.comparatorFactories.putAll(comparatorFactories);
    }

    public List<String> getSortNames() {
        final List<String> result = new ArrayList<>(this.comparators.keySet());
        result.addAll(this.comparatorFactories.keySet());
        return result;
    }

    /**
     * If <code>sortBy</code> matches any of the sorts defined in this object, the corresponding
     * comparator will be used to sort lists, that is, the comparator will be used to sort the
     * first list and the positions of objects in the other lists will change respectively.
     * Also, the lists will be clipped according to the <code>cmd</code>.<br/>
     * If <code>sortBy</code> does not match, the lists will be cleared iff
     * {@link de.marketmaker.istar.merger.web.easytrade.ListCommand#getOffset()} is larger than
     * the size of the list.
     * @param sortBy name of a comparator.
     * @param cmd defines how to sort the lists and how to clip them
     * @param lists to be sorted and clipped
     * @return true iff lists have been sorted.
     */
    public boolean apply(String sortBy, ListCommand cmd, List... lists) {
        final Comparator<T> c = findComparator(sortBy);
        if (c == null) {
            return applyWithoutComparator(sortBy, cmd, lists);
        }

        final MultiListSorter mls = new MultiListSorter(c, !cmd.isAscending());
        mls.sort(lists);

        ListHelper.clipPage(cmd, lists);
        return true;
    }

    private boolean applyWithoutComparator(String sortBy, ListCommand cmd, List... lists) {
        if (isNullSort(sortBy)) {
            ListHelper.clipPage(cmd, lists);
            return true;
        }
        if (cmd.getOffset() > lists[0].size()) {
            ListHelper.clipPage(cmd, lists);
        }
        return false;
    }

    /**
     * @param sortBy name of a comparator.
     * @param cmd defines how to sort the lists and how to clip them
     * @param partitions list of partitions
     * @param lists to be sorted and clipped
     * @return true iff lists have been sorted.
     */
    public boolean apply(List<Partition> partitions, String sortBy, ListCommand cmd,
            List... lists) {
        final Comparator<T> c = findComparator(sortBy);
        if (c == null) {
            return applyWithoutComparator(sortBy, cmd, lists);
        }

        final MultiListSorter mls = new MultiListSorter(c, !cmd.isAscending());
        mls.sort(lists);

        final int offset = cmd.getOffset();
        if (partitions != null && partitions.size() > 0) {
            if (partitions.size() > offset) {
                // deliver requested partition
                Partition partition = partitions.get(offset);
                Partition subsequentPartition = offset + 1 < partitions.size() ? partitions.get(offset + 1) : null;

                for (List list : lists) {
                    if (list != null) {
                        ListHelper.clipPage(list, partition.getOffset(), subsequentPartition == null ? list.size() : subsequentPartition.getOffset());
                    }
                }
                return true;
            }
            else {
                // partition does not exist
                for (List list : lists) {
                    if (list != null) {
                        list.clear();
                    }
                }
                return true;
            }
        }
        else {
            return apply(sortBy, cmd, lists);
        }
    }

    /**
     * Same as {@link #apply(String, de.marketmaker.istar.merger.web.easytrade.ListCommand, java.util.List[])},
     * but does not clip the submitted lists.
     * @param sortBy name of a comparator.
     * @param cmd defines how to sort the lists
     * @param lists to be sorted
     * @return true iff lists have been sorted.
     */
    public boolean applySort(String sortBy, ListCommand cmd, List... lists) {
        final Comparator<T> c = findComparator(sortBy);
        if (c == null) {
            return isNullSort(sortBy);
        }

        final MultiListSorter mls = new MultiListSorter(c, !cmd.isAscending());
        mls.sort(lists);
        return true;
    }

    private boolean isNullSort(String sortBy) {
        return this.comparators.containsKey(sortBy) || this.comparatorFactories.containsKey(sortBy);
    }

    private Comparator<T> findComparator(String name) {
        final Comparator<T> comparator = this.comparators.get(name);
        if (comparator != null) {
            return comparator;
        }
        final Callable<Comparator<T>> callable = this.comparatorFactories.get(name);
        if (callable != null) {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new InternalFailure("findComparator " + name, e);
            }
        }
        return null;
    }
}
