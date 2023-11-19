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

import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author zzhao
 */
public class HistoryGathererYMP<T extends Comparable<T>> extends HistoryGathererBase<T> {

    private final HistoryReader<T> yearReader;

    private final HistoryReader<T> monthReader;

    public HistoryGathererYMP(Class<T> clazz, boolean patchInMem) {
        super(clazz, patchInMem);
        this.yearReader = new HistoryReader<>(clazz, HistoryUnit.Year, false);
        this.monthReader = new HistoryReader<>(clazz, HistoryUnit.Month, false);
    }

    public HistoryReader<T> getYearReader() {
        return yearReader;
    }

    public HistoryReader<T> getMonthReader() {
        return monthReader;
    }

    @Override
    public void updateUnits(File dir, EnumSet<HistoryUnit> units) throws IOException {
        super.updateUnits(dir, units);
        updateReader(dir, units, this.yearReader);
        updateReader(dir, units, this.monthReader);
    }

    @Override
    public void destroy() throws Exception {
        IoUtils.close(this.yearReader);
        IoUtils.close(this.monthReader);
        super.destroy();
    }
}
