/*
 * OpraSearchMethod.java
 *
 * Created on 11.08.12 16:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.SearchParameterParser;
import de.marketmaker.istar.ratios.frontend.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import static de.marketmaker.istar.ratios.frontend.SearchEngineImpl.SLOW_QUERY_THRESHOLD;

/**
 * @author oflege
 * @see de.marketmaker.istar.ratios.frontend.TypeDataSearcher
 */
class OpraMatrixMetadataMethod {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpraMatrixMetadataMethod.class);

    private class SelectTask extends RecursiveTask<OpraMatrixMetadataVisitor> {
        private final int from;

        private final int to;

        SelectTask(int from, int to) {
            this.from = from;
            this.to = to;
            TASK_COUNT.incrementAndGet();
        }

        @Override
        protected OpraMatrixMetadataVisitor compute() {
            if ((this.to - this.from) < threshold) {
                final OpraMatrixMetadataVisitor visitor = new OpraMatrixMetadataVisitor();
                visitor.init(spp);
                final Selector selector = spp.getSelector();
                for (int i = this.from; i < this.to; i++) {
                    final OpraItem item = items.get(i);
                    if (selector == null || selector.select(item)) {
                        visitor.visit(item);
                    }
                }
                return visitor;
            }

            final int mid = this.from + ((this.to - this.from) / 2);
            final SelectTask[] tasks = new SelectTask[] {
                    new SelectTask(this.from, mid), new SelectTask(mid, this.to)
            };
            invokeAll(Arrays.asList(tasks));

            final OpraMatrixMetadataVisitor result = getResult(tasks[0]);
            result.merge(getResult(tasks[1]));
            return result;
        }

        private OpraMatrixMetadataVisitor getResult(SelectTask st) {
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

    private final int threshold;

    OpraMatrixMetadataMethod(ForkJoinPool pool, List<OpraItem> items, RatioSearchRequest request)
            throws Exception {
        this.pool = pool;
        this.items = items;
        this.spp = new SearchParameterParser(request, null);
        // this.threshold = (int) Math.ceil(items.size() / (double) pool.getParallelism());
        this.threshold = items.size() / pool.getParallelism();
    }

    MatrixMetadataRatioSearchResponse invoke() {
        final int oldTc = TASK_COUNT.get();
        final TimeTaker tt = new TimeTaker();
        int n = (spp.getSelector() != Selector.FALSE) ? items.size() : 0;
        try {
            final OpraMatrixMetadataVisitor visitor = this.pool.invoke(new SelectTask(0, n));
            MatrixMetadataRatioSearchResponse response = visitor.getResponse();
            return response;
        } finally {
            final long numTasks = TASK_COUNT.get() - oldTc;
            if (tt.getElapsedMs() > SLOW_QUERY_THRESHOLD) {
                LOGGER.warn("<search> slow for " + spp.getSelector() + " in "
                        + items.size() + "/" + numTasks + ", took " + tt);
            }
            else if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("<search> for " + spp.getSelector() + " in "
                        + items.size() + "/" + numTasks + ", took " + tt);
            }
        }
    }
}
