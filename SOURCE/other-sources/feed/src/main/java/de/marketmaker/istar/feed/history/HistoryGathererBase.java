/*
 * DayTicker.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.springframework.beans.factory.DisposableBean;

import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author zzhao
 */
public abstract class HistoryGathererBase<T extends Comparable<T>>
        implements DisposableBean, HistoryGatherer {

    protected final Class<T> clazz;

    protected final HistoryReader<T> patchReader;

    public HistoryGathererBase(Class<T> clazz, boolean patchInMem) {
        this.clazz = clazz;
        this.patchReader = new HistoryReader<>(clazz, HistoryUnit.Patch, patchInMem);
    }

    public HistoryReader<T> getPatchReader() {
        return patchReader;
    }

    @Override
    public void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException {
        updateReader(dir, units, this.patchReader);
    }

    protected void updateReader(File dir, EnumSet<HistoryUnit> units,
            HistoryReader<T> reader) throws IOException {
        if (units.contains(reader.getUnit())) {
            reader.setFile(reader.getUnit().getLatestFile(dir));
        }
        else {
            reader.setFile(null);
        }
    }

    @Override
    public void destroy() throws Exception {
        IoUtils.close(this.patchReader);
    }
}
