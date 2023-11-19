/*
 * ParallelSearcher.java
 *
 * Created on 05.12.2007 13:09:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.Immutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.TimeTaker;

/**
 * Encapsulates searches on a TypeData object, implementation of the MethodObject pattern.
 * Uses a {@link java.util.concurrent.RecursiveTask} to parallelize searching.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class TypeDataSearcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeDataSearcher.class);

    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("[access].[ratios]");

    private class SelectTask extends RecursiveTask<SearchEngineVisitor> {
        private final List<RatioData> ratioDatas;

        private final int from;

        private final int to;

        private final int threshold;

        private final AtomicInteger taskCount;

        SelectTask(AtomicInteger taskCount, List<RatioData> ratioDatas, int from, int to) {
            this(taskCount, ratioDatas, from, to, Math.min(1 << 17, Math.max(1 << 12, ratioDatas.size() / 4)));
        }

        private SelectTask(AtomicInteger taskCount, List<RatioData> ratioDatas, int from, int to,
                int threshold) {
            this.taskCount = taskCount;
            taskCount.incrementAndGet();
            this.ratioDatas = ratioDatas;
            this.from = from;
            this.to = to;
            this.threshold = threshold;
        }

        @Override
        public SearchEngineVisitor compute() {
            if (!spp.isWithParallelVisitor() || (this.to - this.from) < this.threshold) {
                final SearchEngineVisitor visitor = (SearchEngineVisitor) spp.createVisitor();
                for (int i = this.from; i < this.to; i++) {
                    final RatioData data = ratioDatas.get(i);
                    if (data != null && data.select(spp)) {
                        visitor.visit(data);
                    }
                }
                return visitor;
            }

            final List<SelectTask> tasks = createTasks();
            invokeAll(tasks);
            return mergeResults(tasks);
        }

        private MergeableSearchEngineVisitor mergeResults(List<SelectTask> tasks) {
            MergeableSearchEngineVisitor result = (MergeableSearchEngineVisitor) getResult(tasks.get(0));
            for (int i = 1; i < tasks.size() && result != null; i++) {
                final MergeableSearchEngineVisitor v
                        = (MergeableSearchEngineVisitor) getResult(tasks.get(i));
                //noinspection unchecked
                result = (v != null) ? result.merge(v) : null;
            }
            return result;
        }

        private List<SelectTask> createTasks() {
            int n = 1;
            while ((ratioDatas.size() / n) > threshold) {
                n++;
            }
            final List<SelectTask> result = new ArrayList<>(n);
            final int chunkSize = (ratioDatas.size()) / n + 1;
            for (int i = 0; i < n; i++) {
                result.add(new SelectTask(this.taskCount, ratioDatas, i * chunkSize,
                        Math.min(ratioDatas.size(), (i + 1) * chunkSize), chunkSize + 1));
            }
            return result;
        }

        private SearchEngineVisitor getResult(SelectTask st) {
            try {
                return st.get();
            } catch (Throwable t) {
                LOGGER.error("<getResult> failed", t);
                return null;
            }
        }
    }

    private final ForkJoinPool pool;

    private final SearchParameterParser spp;

    private final TypeData typeData;

    TypeDataSearcher(TypeData typeData, SearchParameterParser spp, ForkJoinPool pool) {
        this.typeData = typeData;
        this.spp = spp;
        this.pool = pool;
    }

    SearchEngineVisitor search() {
        if (this.spp.getInstrumentIds() != null) {
            return searchByIids();
        }
        return search(this.typeData.getRatioDatas());
    }

    /**
     * @return ratios for all applicable instruments (most likely paged); performs parallel search.
     */
    private SearchEngineVisitor search(List<RatioData> ratioDatas) {
        final TimeTaker tt = new TimeTaker();
        AtomicInteger taskCount = new AtomicInteger();
        SearchEngineVisitor visitor = null;
        try {
            visitor = this.pool.invoke(new SelectTask(taskCount, ratioDatas, 0, ratioDatas.size()));
        } finally {
            logAccess(ratioDatas.size(), taskCount.get(), visitor, tt);
        }
        return visitor;
    }

    /**
     * @return ratios for a set of instruments, uses visitor and selector(s) to filter data
     */
    private SearchEngineVisitor searchByIids() {
        final TimeTaker tt = new TimeTaker();
        SearchEngineVisitor visitor = null;

        try {
            visitor = (SearchEngineVisitor) this.spp.createVisitor();
            this.spp.getInstrumentIds()
                    .stream()
                    .map(this.typeData::getRatioData)
                    .filter(r -> r != null && r.select(this.spp))
                    .forEach(visitor::visit);
        } finally {
            logAccess(this.spp.getInstrumentIds().size(), 1, visitor, tt);
        }
        return visitor;
    }

    private void logAccess(int numRatios, int numTasks, SearchEngineVisitor v, TimeTaker tt) {
        if (v != null) {
            ACCESS_LOGGER.info(numRatios + " "
                    + this.typeData.getType()
                    + "s, s='" + spp.getSelector()
                    + "', " + (spp.isWithMetadataCounting() ? "w" : "w/o")
                    + " meta, v=" + v.getClass().getSimpleName()
                    + ", #t=" + numTasks + ", took " + tt);
        }
        else {
            ACCESS_LOGGER.info("FAILED for " + this.spp);
        }
    }
}
