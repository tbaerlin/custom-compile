/*
 * FileTickOutputUnister.java
 *
 * Created on 21.10.2009 20:48:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.boersestg;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.PriceFormatter;
import de.marketmaker.istar.domain.data.DataWithInterval;
import de.marketmaker.istar.domain.data.TickEvent;
import de.marketmaker.istar.domain.timeseries.Timeseries;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Thomas Kiesgen
 */
public class FileTickOutputBoerseStg implements AutoCloseable {
    private final PriceFormatter pf = new PriceFormatter(5, 4);

    private final DateTimeFormatter dft = DateTimeFormat.forPattern("HH:mm:ss");

    private FileWriter out;

    public FileTickOutputBoerseStg(File f) throws IOException {
        this.out = new FileWriter(f);
        this.pf.setShowTrailingZeros(false);
    }

    public void init(String s) throws Exception {
        this.out.write(s + "\n");
    }

    public void process(String symbol, String market, Timeseries<TickEvent> ts) throws Exception {
        for (DataWithInterval<TickEvent> t : ts) {
            final int sec = t.getData().getTime();
            final LocalTime localTime = DateUtil.secondsInDayToLocalTime(sec);
            this.out.write(symbol + ";" + market + ";");
            write(t.getData().isBid(), t.getData().getBidPrice(), t.getData().getBidVolume());
            write(t.getData().isAsk(), t.getData().getAskPrice(), t.getData().getAskVolume());
            write(t.getData().isTrade(), t.getData().getPrice(), t.getData().getVolume());
            this.out.write(dft.print(localTime) + "\n");
        }
    }

    private void write(boolean valid, long price, long volume) throws IOException {
        if (valid) {
            this.out.write(pf.formatPrice(PriceCoder.decode(price)) + ";"
                    + (volume == Long.MIN_VALUE ? "" : volume)
                    + ";");
        } else {
            this.out.write(";;");
        }
    }

    public void close() throws IOException {
        this.out.close();
    }
}