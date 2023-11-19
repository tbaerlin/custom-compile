/*
 * EndOfDayFilter.java
 *
 * Created on 22.10.13 15:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.BitSet;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTimeZone;

import de.marketmaker.istar.common.util.DateUtil;

/**
 * @author oflege
 */
public class EndOfDayFilter {

    /**
     * Set of all fields that are not explicitly allowed as Eod Field; these fields will not be
     * visible for users with an eod permission.
     */
    private static final BitSet DELETE_FOR_EOD = new BitSet(VwdFieldDescription.length());

    static {
        final BitSet bs = VwdFieldDescription.getFieldIds();
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            DELETE_FOR_EOD.set(i, !isEodField(VwdFieldDescription.getField(i)));
        }
    }

    private static boolean isEodField(VwdFieldDescription.Field f) {
        return (f.isStatic() && !f.isDynamic())
                || f.name().contains("Vortag") || f.name().contains("Vorvortag") || f.name().contains("_Prev_")
                || f == VwdFieldDescription.ADF_Vorheriges_Datum || isAverageVolumeField(f);
    }

    /**
     * Temporary fix for CORE-16932.
     *
     * ADF_Durchschnittsumsatz_* fields can be handled like EOD fields,
     * since they are calculated upon older prices.
     */
    private static boolean isAverageVolumeField(VwdFieldDescription.Field f) {
        return VwdFieldDescription.ADF_Durchschnittsumsatz_1M.id() <= f.id()
                && f.id() <= VwdFieldDescription.ADF_Durchschnittsumsatz_10J.id();
    }

    static final EndOfDayFilter DEFAULT = new EndOfDayFilter(DateUtil.DTZ_BERLIN);

    private static final ConcurrentHashMap<DateTimeZone, EndOfDayFilter> INSTANCES
            = new ConcurrentHashMap<>();

    private final DateTimeZone dtz;

    static EndOfDayFilter get(DateTimeZone dtz) {
        if (dtz == null || dtz == DEFAULT.dtz) {
            return DEFAULT;
        }
        final EndOfDayFilter existing = INSTANCES.get(dtz);
        if (existing != null) {
            return existing;
        }
        INSTANCES.putIfAbsent(dtz, new EndOfDayFilter(dtz));
        return INSTANCES.get(dtz);
    }

    private EndOfDayFilter(DateTimeZone dtz) {
        this.dtz = dtz;
    }

    public BitSet getNonEodFields() {
        return DELETE_FOR_EOD;
    }

    public DateTimeZone getZone() {
        return dtz;
    }

    @Override
    public String toString() {
        return "EndOfDayFilter[" + this.dtz.getID() + "]";
    }
}
