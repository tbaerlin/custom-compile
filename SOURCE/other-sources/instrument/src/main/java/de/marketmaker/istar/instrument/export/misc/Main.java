/*
 * Main.java
 *
 * Created on 15.04.2010 16:17:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export.misc;

import java.io.File;
import java.util.List;
import java.util.Random;

import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;

/**
 * @author zzhao
 */
public class Main {

    public static void main(String[] args) throws Exception {
        File dir = new File(args[0]);
        long iid = Long.parseLong(args[1]);
        InstrumentDirDao dao = null;
        try {
            dao = new InstrumentDirDao(dir);

            final Instrument instrument = dao.getInstrument(iid);
            if (null == instrument) {
                System.out.println("no instrument found with iid:'" + iid + "'");
            }
            else {
                for (Quote quote : instrument.getQuotes()) {
                    final long[] flags = ((QuoteDp2) quote).getFlags();
                    System.out.println(quote + ": " + InstrumentUtil.toBase64String(flags));
                    for (long flag : flags) {
                        System.out.println(Long.toBinaryString(flag));
                    }
                }
            }

//            long start = System.nanoTime();
//            List<Long> insIds = dao.getInstruments();
//            long end = System.nanoTime();
//            long dur = TimeUnit.MILLISECONDS.convert(end - start, TimeUnit.NANOSECONDS);
//            System.out.println("getInstruments() took: " + DurationFormatUtils.formatDurationHMS(dur));
//
//            final int num = insIds.size();
//            System.out.println("" + num + " instruments");
//
//            long[] ids = getRandomIds(insIds, 1024);
//
//            int slow = 0;
//            for (int i = 0; i < ids.length; i++) {
//                start = System.nanoTime();
//                dao.getInstrument(ids[i]);
//                end = System.nanoTime() - start;
//                dur = TimeUnit.MILLISECONDS.convert(end, TimeUnit.NANOSECONDS);
//                if (dur > 10) {
//                    ++slow;
//                }
//            }
//
//            System.out.println("" + slow + " getInstrument(id) slower than 10 ms");
//
//            start = System.nanoTime();
//            for (int i = 0; i < ids.length; i++) {
//                dao.getInstrument(ids[i]);
//            }
//            end = System.nanoTime() - start;
//            dur = TimeUnit.MILLISECONDS.convert(end, TimeUnit.NANOSECONDS);
//            System.out.println("getInstrument(id) for all took: " + DurationFormatUtils.formatDurationHMS(dur));
//            double avg = (double) dur / ids.length;
//            System.out.println("getInstrument(id) avg: " + avg + " ms");
//
//            start = System.nanoTime();
//            int n = 0;
//            for (Instrument ins : dao) {
//                ++n;
//            }
//            end = System.nanoTime() - start;
//            dur = TimeUnit.MILLISECONDS.convert(end, TimeUnit.NANOSECONDS);
//            System.out.println("Iterate over all instruments took: " + DurationFormatUtils.formatDurationHMS(dur));
        } finally {
            IoUtils.close(dao);
        }
    }

    private static long[] getRandomIds(List<Long> ll, int num) {
        Random r = new Random(System.currentTimeMillis());
        long[] ret = new long[num];
        for (int i = 0; i < num; i++) {
            ret[i] = ll.get(r.nextInt(ll.size() - 1));
        }

        return ret;
    }
}
