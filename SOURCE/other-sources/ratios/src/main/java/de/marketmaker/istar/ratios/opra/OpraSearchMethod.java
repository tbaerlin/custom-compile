/*
 * OpraSearchMethod.java
 *
 * Created on 11.08.12 16:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.PagedResultSorter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.SearchParameterParser;
import de.marketmaker.istar.ratios.frontend.Selector;

/**
 * @author oflege
 * @see de.marketmaker.istar.ratios.frontend.TypeDataSearcher
 */
class OpraSearchMethod {
    private static final long SLOW_QUERY_THRESHOLD = 2000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OpraSearchMethod.class);

    private class SelectTask extends RecursiveTask<PagedResultSorter<OpraItem>> {
        private final int from;

        private final int to;

        SelectTask(int from, int to) {
            this.from = from;
            this.to = to;
            TASK_COUNT.incrementAndGet();
        }

        @Override
        protected PagedResultSorter<OpraItem> compute() {
            if ((this.to - this.from) < threshold) {
                final PagedResultSorter<OpraItem> sorter = new PagedResultSorter<>(
                        spp.getStartAt(), spp.getNumResults(), this.to - this.from, comp);
                final Selector selector = spp.getSelector();
                for (int i = this.from; i < this.to; i++) {
                    final OpraItem item = items.get(i);
                    if (selector == null || selector.select(item)) {
                        sorter.add(item);
                    }
                }
                return sorter;
            }

            final int mid = this.from + ((this.to - this.from) / 2);
            final SelectTask[] tasks = new SelectTask[] {
                    new SelectTask(this.from, mid), new SelectTask(mid, this.to)
            };
            invokeAll(Arrays.asList(tasks));

            final PagedResultSorter<OpraItem> result = getResult(tasks[0]);
            result.merge(getResult(tasks[1]));
            return result;
        }

        private PagedResultSorter<OpraItem> getResult(SelectTask st) {
            try {
                return st.get();
            } catch (Throwable t) {
                LOGGER.error("<getResult> failed", t);
                return null;
            }
        }
    }

    private static final AtomicInteger TASK_COUNT = new AtomicInteger();

    private final ForkJoinPool pool;

    private final List<OpraItem> items;

    private final SearchParameterParser spp;

    private final Comparator<OpraItem> comp;

    private final int threshold;

    OpraSearchMethod(ForkJoinPool pool, List<OpraItem> items, RatioSearchRequest request)
            throws Exception {
        this.pool = pool;
        this.items = items;
        this.spp = new SearchParameterParser(request, null);
        this.comp = getComparator(this.spp);
        this.threshold = items.size() / pool.getParallelism();
    }

    OpraRatioSearchResponse invoke() throws Exception {
        final int oldTc = TASK_COUNT.get();
        final TimeTaker tt = new TimeTaker();
        int numFound = -1;
        int n = (spp.getSelector() != Selector.FALSE) ? items.size() : 0;
        try {
            final PagedResultSorter<OpraItem> prs = this.pool.invoke(new SelectTask(0, n));
            numFound = prs.getTotalCount();
            return createResponse(prs);
        } finally {
            final long numTasks = TASK_COUNT.get() - oldTc;
            if (tt.getElapsedMs() > SLOW_QUERY_THRESHOLD) {
                LOGGER.warn("<search> slow for " + spp.getSelector() + " found " + numFound + " in "
                        + items.size() + "/" + numTasks + ", took " + tt);
            }
            else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("<search> for " + spp.getSelector() + " found " + numFound + " in "
                        + items.size() + "/" + numTasks + ", took " + tt);
            }
        }
    }

    private OpraRatioSearchResponse createResponse(PagedResultSorter<OpraItem> prs) {
        final OpraRatioSearchResponse result = new OpraRatioSearchResponse();
        result.setOffset(prs.getStart());
        result.setLength(prs.getLength());
        result.setNumTotal(prs.getTotalCount());
        result.setItems(prs.getResult());
        return result;
    }

    private Comparator<OpraItem> getComparator(SearchParameterParser spp) {
        final RatioFieldDescription.Field sortField = getSortField(spp);
        final Comparator<OpraItem> comparator = OpraItem.getComparator(sortField);

        final boolean descending = "true".equals(spp.getParameters().get("sort1:D"));
        return descending ? Collections.reverseOrder(comparator) : comparator;
    }

    private RatioFieldDescription.Field getSortField(SearchParameterParser spp) {
        final String str = (String) spp.getParameters().get("sort1");
        return (str != null) ? RatioFieldDescription.getFieldByName(str) : RatioFieldDescription.vwdCode;
    }
}
