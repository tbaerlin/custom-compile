/*
 * Foo.java
 *
 * Created on 19.04.13 13:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.FatalExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.TickHistoryContext;
import de.marketmaker.istar.feed.history.HistoryUtil;
import de.marketmaker.istar.feed.history.HistoryWriter;


class TickJournalizer {

    private static final EventFactory<RequestItem> WORK_FACTORY = new EventFactory<RequestItem>() {
        @Override
        public RequestItem newInstance() {
            return new RequestItem();
        }
    };

    private static final EventFactory<ResultItem> RESULT_FACTORY = new EventFactory<ResultItem>() {
        @Override
        public ResultItem newInstance() {
            return new ResultItem();
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final int threads;

    private final ExecutorService executorService;

    private final int bufferSize;

    private WorkerPool<RequestItem> workerPool;

    private Disruptor<ResultItem> resultDisruptor;

    private RingBuffer<RequestItem> workBuffer;

    private RingBuffer<ResultItem> resultBuffer;

    private List<WorkReporter> reporters;

    public TickJournalizer(int threads, ExecutorService es, int bufferSize) {
        this.threads = threads;
        this.executorService = es;
        this.bufferSize = bufferSize;
    }

    void prepare(int days, EnumSet<TickType> tickTypes, Map<TickType,
            HistoryWriter<ByteString>> writers, TickHistoryContext ctx) {
        this.reporters = new ArrayList<>(this.threads);

        final ResultHandler resultHandler = new ResultHandler(days, writers, ctx);
        this.resultDisruptor = new Disruptor<>(RESULT_FACTORY, this.bufferSize, this.executorService,
                ProducerType.MULTI, new YieldingWaitStrategy());
        this.resultDisruptor.handleEventsWith(resultHandler);
        this.resultBuffer = this.resultDisruptor.start();


        final WorkHandler[] handlers = new WorkHandler[this.threads - 1]; // one used by result handler
        for (int i = 0; i < handlers.length; i++) {
            final TickWorker tickWorker = new TickWorker(
                    HistoryUtil.daysFromBegin2Date(ctx.getGenesis(), days), tickTypes,
                    this.resultBuffer, i);
            handlers[i] = tickWorker;
            this.reporters.add(tickWorker);
        }

        this.reporters.add(resultHandler);

        this.workerPool = new WorkerPool<>(WORK_FACTORY, new FatalExceptionHandler(), handlers);
        this.workBuffer = this.workerPool.start(this.executorService);
    }

    void journalize(TickOrganizer organizer) {
        while (organizer.hasWork()) {
            final long seq = this.workBuffer.next();
            final RequestItem requestItem = this.workBuffer.get(seq);
            try {
                requestItem.reset();
                requestItem.withSequence(this.resultBuffer.next());
                organizer.assignWork(requestItem);
            } catch (Exception e) {
                this.logger.error("<journalize> failed assigning work", e);
                requestItem.setDamaged(true);
            } finally {
                this.workBuffer.publish(seq);
            }
        }
    }

    void finish() {
        this.workerPool.drainAndHalt();
        this.resultDisruptor.shutdown();
        for (WorkReporter reporter : reporters) {
            reporter.report();
        }
        this.reporters.clear();
    }
}
