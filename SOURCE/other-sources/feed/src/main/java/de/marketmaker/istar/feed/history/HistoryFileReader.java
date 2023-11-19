/*
 * HistoryReader.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public class HistoryFileReader {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: genesis history_file");
            System.exit(1);
        }

        LocalDate genesis = HistoryUtil.DTF_DAY.parseLocalDate(args[0]);
        final File file = new File(args[1]);
        final HistoryUnit unit = HistoryUnit.fromExt(file);
        try (
                final HistoryReader<ByteString> reader = new HistoryReader<>(ByteString.class,
                        unit, false);
                final BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        ) {
            final TickType tickType = TickType.valueOf(HistoryUnit.getContentType(file));
            reader.setFile(file);
            final ByteBuffer bb = ByteBuffer.allocate(8 * 1024 * 1024);
            String line;
            String symbol;
            boolean outputAll;
            do {
                System.out.print("symbol[710000.mch / empty to quit]: ");
                line = br.readLine();
                if (line.endsWith("*")) {
                    symbol = line.substring(0, line.lastIndexOf("*"));
                    outputAll = true;
                }
                else {
                    symbol = line;
                    outputAll = false;
                }
                if (StringUtils.isNotBlank(symbol)) {
                    if ("keys".equals(symbol)) {
                        reader.emitKeys(System.out, outputAll);
                    }
                    else {
                        final TimeTaker tt = new TimeTaker();
                        final ByteString key = HistoryUtil.getKey(new ByteString(symbol.toUpperCase()));
                        bb.clear();
                        reader.loadData(key, bb);
                        bb.flip();
                        System.out.println(symbol + "#" + tickType + ", took: " + tt);
                        TickHistoryReaderCli.emitTickHistory(genesis, tickType, outputAll, bb, true);
                    }
                }
            } while (StringUtils.isNotBlank(symbol));
        }
    }
}