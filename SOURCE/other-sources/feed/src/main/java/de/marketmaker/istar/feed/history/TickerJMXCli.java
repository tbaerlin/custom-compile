/*
 * TickHistoryPersisterJMXClient.java
 *
 * Created on 26.07.12 14:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author zzhao
 */
public class TickerJMXCli {

    public static void main(String[] args) throws Exception {
        if (null == args || args.length != 3) {
            System.err.println("Usage: [MBean serverUrl] [ObjectName] [export share]");
            System.exit(1);
        }

        TickerJMXClient client = null;
        BufferedReader br = null;
        try {
            client = new TickerJMXClient(args[0], args[1]);

            final File dir = new File(args[2]);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new IllegalArgumentException("invalid dir for searching tick folders: " + args[2]);
            }

            String input;
            br = new BufferedReader(new InputStreamReader(System.in));
            do {
                System.out.print("[yyyyMMdd | yyyyMMdd-XXX | yyyyMMdd-yyyyMMdd]: ");
                input = br.readLine();
                if (StringUtils.hasText(input)) {
                    if (input.length() == "yyyyMMdd".length()) {
                        final LocalDate dt = DateUtil.yyyyMmDdToLocalDate(Integer.parseInt(input));
                        final File tickDir = findTickDir(dir, dt);
                        tick(client, tickDir, dt);
                    }
                    else if (input.length() == "yyyyMMdd-yyyyMMdd".length()) {
                        LocalDate fromDT = DateUtil.yyyyMmDdToLocalDate(
                                Integer.parseInt(input.substring(0, input.indexOf("-"))));
                        final LocalDate toDT = DateUtil.yyyyMmDdToLocalDate(
                                Integer.parseInt(input.substring(input.indexOf("-") + 1)));
                        while (!fromDT.isAfter(toDT)) {
                            final File tickDir = findTickDir(dir, fromDT);
                            tick(client, tickDir, fromDT);
                            fromDT = fromDT.plusDays(1);
                        }
                    }
                    else {
                        final LocalDate toDT = DateUtil.yyyyMmDdToLocalDate(
                                Integer.parseInt(input.substring(0, input.indexOf("-"))));
                        final int daysBack = Integer.parseInt(input.substring(input.indexOf("-") + 1));
                        final LocalDate fromDT = toDT.minusDays(daysBack);
                        for (int i = 1; i <= daysBack; i++) {
                            final LocalDate dt = fromDT.plusDays(i);
                            final File tickDir = findTickDir(dir, dt);
                            tick(client, tickDir, dt);
                        }
                    }
                }
            } while (StringUtils.hasText(input));

        } finally {
            IoUtils.close(br);
            IoUtils.close(client);
        }
    }

    private static void tick(TickerJMXClient client, File tickDir, LocalDate dt) {
        if (null != tickDir) {
            client.tick(tickDir);
        }
        else {
            System.out.println("cannot find tick dir for: " + HistoryUtil.DTF_DAY.print(dt));
        }
    }

    private static File findTickDir(File dir, LocalDate dt) {
        final String date = HistoryUtil.DTF_DAY.print(dt);
        return TickHistoryController.findTickDir(dir, date);
    }
}
