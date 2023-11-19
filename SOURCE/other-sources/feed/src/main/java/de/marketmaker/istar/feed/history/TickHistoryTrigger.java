/*
 * TickHistoryController.java
 *
 * Created on 22.08.12 14:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.feed.history.distruptor.DisruptTicker;

/**
 * @author zzhao
 */
public class TickHistoryTrigger {

    private static final Logger logger = LoggerFactory.getLogger(TickHistoryTrigger.class);

    public static void main(String[] args) throws Exception {
        if (null == args || args.length < 5) {
            System.err.println("Usage: tick_dir tar_dir file_market_neg tick_types future_markets [market_filter_file]");
            System.exit(1);
        }

        final Path path = Paths.get(args[0]);
        final File tickDir = path.toFile();
        if (!tickDir.exists() || !tickDir.isDirectory()) {
            System.err.println("tick dir: " + args[0] + " not found");
            System.exit(1);
        }

        TickHistoryContextImpl ctx = new TickHistoryContextImpl();
        ctx.setMarketsWithNegativeTicks(new File(args[2]/*negative market file*/));
        if (args.length == 6) {
            ctx.setMarketFilter(new File(args[5]));
        }

        final TickHistoryController controller = new TickHistoryController();
        final File workDir = new File(args[1]);
        FutureSymbolRetainer symbolRetainer = new FutureSymbolRetainer();
        symbolRetainer.setMarketsWithFutures(args[4]);
        controller.setArchives(TickHistoryController.getArchives(workDir, args[3], symbolRetainer, ctx));


        final DisruptTicker ticker = new DisruptTicker();
        ticker.setHistoryContext(ctx);

        try {
            ticker.afterPropertiesSet();
            controller.setTicker(ticker);
            controller.tickIntern(tickDir);
        } catch (Throwable t) {
            // log the
            // Exception in thread "main" java.lang.IllegalStateException: won't produce tick history because of gap: TRADE_...
            // in the checked logfile rather than on the console
            logger.error("<main> caught ", t);
        } finally {
            ticker.destroy();
        }
    }
}
