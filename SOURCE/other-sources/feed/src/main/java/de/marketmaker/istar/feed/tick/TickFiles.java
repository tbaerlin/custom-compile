/*
 * TickFiles.java
 *
 * Created on 29.11.2004 14:36:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.File;

import de.marketmaker.istar.feed.FeedMarket;

import static de.marketmaker.istar.feed.ordered.tick.FileTickStore.*;
import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.*;

public class TickFiles {

    public static final String TDA = ".tda";

    public static AbstractTickRecord.TickItem.Encoding getItemType(File f) {
        if (f == null) {
            return UNDEFINED;
        }
        if (f.getName().endsWith(TDA)) {
            final boolean prof = f.getName().startsWith("PROF-");
            return prof ? PROF : TICK;
        }
        if (f.getName().endsWith(TD3)) {
            return TICK3;
        }
        if (f.getName().endsWith(TDZ)) {
            return TICKZ;
        }
        if (f.getName().endsWith(DD3)) {
            return DUMP3;
        }
        if (f.getName().endsWith(DDZ)) {
            return DUMPZ;
        }
        throw new IllegalArgumentException("unkown tick type for " + f.getName());
    }

    public static String getMarketName(File f) {
        return f.getName().substring(0, f.getName().lastIndexOf('-'));
    }

    public static String getMarketBaseName(File file) {
        String name = getMarketName(file);
        int p = name.indexOf(FeedMarket.PARTITION_SEPARATOR);
        return (p < 0) ? name : name.substring(0, p);
    }

    public static int getDay(File f) {
        return Integer.parseInt(f.getName().substring(
                f.getName().lastIndexOf('-') + 1, f.getName().lastIndexOf('.')));
    }
}
