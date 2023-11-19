/*
 * CombinedCalculator.java
 *
 * Created on 18.07.12 11:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.instrument.Instrument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps a calculator, keeps track of instruments to be calculated by that calculator,
 * and provides a thread that does the actual calculation.
 */
class CombinedCalculator implements Calculator {
    private static final AtomicInteger INSTANCE_NO = new AtomicInteger();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ExecutorService es;

    private final CalcController controller;

    private final EmbeddedCalculator embeddedCalculator;

    private final MmCalculator mmCalculator;

    private List<Instrument> toCalc = new ArrayList<>();

    private volatile Future<?> future;

    CombinedCalculator(CalcController controller, EmbeddedCalculator embeddedCalculator,
            MmCalculator mmCalculator) {
        this.controller = controller;
        this.mmCalculator = mmCalculator;
        this.embeddedCalculator = embeddedCalculator;

        // single threaded pool
        this.es = Executors.newSingleThreadExecutor((runnable) -> new Thread(runnable, "Calculator-" + INSTANCE_NO.incrementAndGet()));
    }

    public void shutdown() {
        this.es.shutdown();
    }

    void add(Instrument instrument) {
        this.toCalc.add(instrument);
    }

    int size() {
        return this.toCalc.size();
    }

    boolean isIdle() {
        return this.future == null || this.future.isDone();
    }

    void submitCalcTask(final Map<Long, List<Long>> ids, final int frontsize)
            throws InterruptedException {
        final List<Instrument> instruments = this.toCalc;
        this.toCalc = new ArrayList<>(ids.size());
        if (this.future != null) {
            try {
                this.future.get();
            } catch (ExecutionException e) {
                logger.warn("<submitCalcTask> failed ", e);
            }
        }
        this.future = this.es.submit(() -> controller.calc(CombinedCalculator.this, instruments, ids, frontsize));
    }

    @Override
    public void calc(List<CalcData> toCalc, ComputedRatiosHandler handler) {
        delegateCalc(this.mmCalculator, toCalc, handler);
        delegateCalc(this.embeddedCalculator, toCalc, handler);
    }

    private void delegateCalc(Calculator calculator, List<CalcData> toCalc,
        ComputedRatiosHandler handler) {
        try {
            calculator.calc(toCalc, handler);
        } catch (Throwable t) {
            this.logger.warn("<calc> {} failed", calculator.getClass().getSimpleName());
            throw t;
        }
    }

    @Override
    public long unusedSinceMillis() {
        return Math.min(this.mmCalculator.unusedSinceMillis(),
                this.embeddedCalculator.unusedSinceMillis());
    }
}
