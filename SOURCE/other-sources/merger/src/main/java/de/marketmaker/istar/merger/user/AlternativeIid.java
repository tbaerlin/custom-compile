package de.marketmaker.istar.merger.user;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * Created on 11.08.11 13:52
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class AlternativeIid {
    private final long iid;
    private final int count;
    private final float weight;

    public AlternativeIid(long iid, int count, float weight) {
        this.iid = iid;
        this.count = count;
        this.weight = weight;
    }

    public long getIid() {
        return iid;
    }

    public int getCount() {
        return count;
    }

    public float getWeight() {
        return weight;
    }

    public static List<String> getIids(List<AlternativeIid> altIids) {
        final List<String> iids = new ArrayList<>();
        for (AlternativeIid altIid : altIids) {
            iids.add(EasytradeInstrumentProvider.iidSymbol(altIid.getIid()));
        }
        return iids;
    }
}