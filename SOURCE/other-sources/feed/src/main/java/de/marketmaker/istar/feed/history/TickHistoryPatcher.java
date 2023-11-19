/*
 * Csv2History.java
 *
 * Created on 27.09.12 15:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.util.ByteString;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public class TickHistoryPatcher {

    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.err.println("Usage: target_dir tick_dir file_market_neg tick_type (symbol file 1|symbol 1) [(symbol file 2|symbol 2)] ...)");
            System.exit(1);
        }

        final File targetDir = new File(args[0]);
        final File tickDir = new File(args[1]);

        TickHistoryContextImpl ctx = new TickHistoryContextImpl();
        ctx.setMarketsWithNegativeTicks(new File(args[2]/*negative market file*/));

        TickType tickType = TickType.valueOf(args[3]);

        TickHistoryArchive archive = new TickHistoryArchive(tickType.name());
        archive.setWorkDir(targetDir);
        archive.setContext(ctx);

        String[] symbols =
                Arrays.stream(Arrays.copyOfRange(args, 4, args.length))
                        .flatMap(s -> {
                            File possibleSymbolList = new File(s);
                            if (possibleSymbolList.exists() && possibleSymbolList.isFile()) {
                                try {
                                    return Files.readAllLines(possibleSymbolList.toPath()).stream();
                                } catch (IOException e) {
                                    System.err.println("Error reading file " + possibleSymbolList + ". Skipping.\n" + e);
                                    return Stream.of();
                                }
                            }
                            else {
                                return Stream.of(s);
                            }
                        }).toArray(String[]::new);

        final TickPatcher tickPatcher = new TickPatcher(tickDir, symbols, tickType, ctx);

        final int date = Integer.parseInt(tickDir.getName());
        final File changeFile = HistoryUtil.createChangeFile(ByteString.class,
            ctx.getOffsetLengthCoder().getLengthBits(), archive.getContentType(), targetDir,
            tickPatcher, date, date);
        archive.update(HistoryUnit.Change, changeFile);
    }
}
