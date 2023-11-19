/*
 * TickHistoryPersisterJMXClient.java
 *
 * Created on 26.07.12 14:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history.distruptor;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.concurrent.ExecutorServiceUtil;
import de.marketmaker.istar.domain.data.TickType;
import de.marketmaker.istar.feed.history.TickHistoryContextImpl;
import de.marketmaker.istar.feed.history.HistoryWriter;
import de.marketmaker.istar.feed.history.MinuteTickerImpl;
import de.marketmaker.istar.feed.ordered.tick.TickDirectory;

/**
 * @author zzhao
 */
public class DisruptTicker extends MinuteTickerImpl implements InitializingBean, DisposableBean {

    private final AtomicInteger ser = new AtomicInteger(-1);

    private ExecutorService executorService;

    private int bufferSize;

    private int threads;

    @Override
    public void destroy() throws Exception {
        ExecutorServiceUtil.shutdownAndAwaitTermination(this.executorService, 120);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        this.threads = Math.min(5, availableProcessors / 2 + 1); // one thread for main
        this.logger.info("<afterPropertiesSet> {} available processors, {} threads used",
                availableProcessors, this.threads);
        this.bufferSize = 128;
        this.logger.info("<afterPropertiesSet> buffer size {}", this.bufferSize);

        this.executorService = Executors.newFixedThreadPool(this.threads, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Ticker-" + ser.incrementAndGet());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        if (null == args || args.length < 3) {
            System.err.println("Usage: tick_dir tick_types tar_dir");
            System.err.println("Options:" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_GENESIS + "=20040101" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_LENGTH_BITS + "=24" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_MARKETS_WITH_NEGATIVE_TICKS + "={file}" +
                    " -D" + TickHistoryContextImpl.ENV_KEY_MARKET_FILTER + "={file}");
            System.exit(1);
        }

        final DisruptTicker ticker = new DisruptTicker();
        ticker.setHistoryContext(TickHistoryContextImpl.fromEnv());

        final File targetDir = new File(args[2]);
        try {
            ticker.afterPropertiesSet();
            final String[] split = args[1].split(",");
            final EnumMap<TickType, File> map = new EnumMap<>(TickType.class);
            for (String str : split) {
                map.put(TickType.valueOf(str.toUpperCase()), targetDir);
            }

            ticker.produceMinuteTicks(new File(args[0]), map);
        } finally {
            ticker.destroy();
        }
    }

    protected void produce(TickDirectory tickDirectory, int days, TreeSet<File> tickFiles,
            EnumMap<TickType, HistoryWriter<ByteString>> writers) {
        final EnumSet<TickType> tickTypes = EnumSet.copyOf(writers.keySet());

        try (final TickOrganizer organizer = new TickOrganizer(tickDirectory, tickFiles, tickTypes,
                this.historyContext)) {
            final TickJournalizer ticker = new TickJournalizer(this.threads,
                    this.executorService, this.bufferSize);
            ticker.prepare(days, tickTypes, writers, this.historyContext);
            ticker.journalize(organizer);
            ticker.finish();
        } catch (Exception e) {
            this.logger.error("<produce> failed producing day ticks", e);
        }
    }
}
